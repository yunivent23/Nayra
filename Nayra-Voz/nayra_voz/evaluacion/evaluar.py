"""Evaluación y calibración de la verificación de voz con los modelos REALES (D-055, D-060).

Uso (desde Nayra-Voz/, con los modelos descargados por scripts/descargar_modelos.sh):

    python -m nayra_voz.evaluacion.evaluar --manifiesto datos.csv --salida resultados/
    python -m nayra_voz.evaluacion.evaluar --openslr /ruta/es_pe_female /ruta/es_pe_male --salida resultados/

`--manifiesto`: CSV con columnas `ruta,hablante[,genero]` (WAV PCM de 16 bits, cualquier frecuencia: se
remuestrea a 16 kHz). Es el formato pensado para las grabaciones de voluntarios de D-060.
`--openslr`: carpetas del corpus OpenSLR 73 (español latinoamericano, Perú); el hablante sale del nombre del
archivo (`pef_01234_...` → `pef_01234`, género f/m).

Qué hace:
1. Extrae el embedding ECAPA-TDNN de cada grabación con el mismo extractor del servicio (`ExtractorEcapa`).
2. Reparte los hablantes en DESARROLLO y PRUEBA (disjuntos) y arma intentos genuinos e impostores con el
   enrolamiento de Nayra (centroide de `enrolamiento.muestras_validas` muestras).
3. En DESARROLLO: EER, umbral para FAR objetivo, minDCF y calibrador logístico.
4. En PRUEBA: FAR y FRR (con IC 95 % de Wilson) del umbral vigente (0.80) y de los umbrales elegidos en desarrollo.
5. Opcional (`--antispoofing N`): pasa N grabaciones genuinas por AASIST y mide cuántas rechazaría el umbral
   bona fide vigente (falso rechazo de voz real). NO mide ataques: el corpus no tiene grabaciones falsas.

No modifica `config/parametros_provisionales.yaml`: solo propone. Los valores definitivos se aprueban según D-055.

Privacidad: la caché `embeddings.npz` contiene huellas de voz. Con datos de voluntarios, guardarla solo con el
consentimiento de D-060 y borrarla al terminar (`--sin-cache` evita escribirla). El audio nunca se copia.
"""
from __future__ import annotations

import argparse
import csv
import json
import re
import sys
import time
import wave
from datetime import date
from pathlib import Path

import numpy as np

from ..config import cargar_parametros
from . import metricas
from .calibrador import entrenar, entropia_cruzada
from .protocolo import Hablante, construir_ensayos, particion

RAIZ = Path(__file__).resolve().parents[2]
FS = 16000
_PATRON_OPENSLR = re.compile(r"^(?P<hablante>[a-z]{2}(?P<genero>[fm])_\d+)_")


def leer_wav_16k(ruta: Path) -> np.ndarray:
    with wave.open(str(ruta), "rb") as w:
        if w.getsampwidth() != 2:
            raise ValueError(f"{ruta}: se espera PCM de 16 bits")
        fs, canales = w.getframerate(), w.getnchannels()
        x = np.frombuffer(w.readframes(w.getnframes()), dtype="<i2").astype(np.float32) / 32768.0
    if canales > 1:
        x = x.reshape(-1, canales).mean(axis=1)
    if fs != FS:
        import torch
        import torchaudio.functional as F

        x = F.resample(torch.from_numpy(x), fs, FS).numpy()
    return x


def manifiesto_desde_csv(ruta: Path) -> list[tuple[Path, str, str | None]]:
    filas = []
    with open(ruta, newline="", encoding="utf-8") as f:
        for fila in csv.DictReader(f):
            archivo = Path(fila["ruta"])
            if not archivo.is_absolute():
                archivo = ruta.parent / archivo
            filas.append((archivo, fila["hablante"], fila.get("genero") or None))
    return filas


def manifiesto_desde_openslr(carpetas: list[Path]) -> list[tuple[Path, str, str | None]]:
    filas = []
    for carpeta in carpetas:
        for archivo in sorted(carpeta.rglob("*.wav")):
            m = _PATRON_OPENSLR.match(archivo.name)
            if m:
                filas.append((archivo, m["hablante"], m["genero"]))
    return filas


def seleccionar(filas, max_por_hablante: int, semilla: int = 0):
    """Limita las grabaciones por hablante (orden aleatorio estable) para acotar el tiempo de CPU."""
    rng = np.random.default_rng(semilla)
    por_hablante: dict[str, list] = {}
    for fila in filas:
        por_hablante.setdefault(fila[1], []).append(fila)
    elegidas = []
    for h in sorted(por_hablante):
        grupo = por_hablante[h]
        elegidas += [grupo[k] for k in rng.permutation(len(grupo))[:max_por_hablante]]
    return elegidas


def recortar(x: np.ndarray, segundos: float | None) -> np.ndarray:
    """Recorte central a `segundos`: imita la duración de una frase de desafío (D-054) en corpus de frases largas."""
    if not segundos:
        return x
    n = int(segundos * FS)
    if len(x) <= n:
        return x
    inicio = (len(x) - n) // 2
    return x[inicio:inicio + n]


def _clave(archivo: Path, segundos: float | None) -> str:
    return f"{archivo}|{segundos or 0:g}"


def extraer_embeddings(filas, ruta_modelo: Path, cache: Path | None,
                       segundos: float | None = None) -> dict[str, np.ndarray]:
    previos: dict[str, np.ndarray] = {}
    if cache and cache.exists():
        datos = np.load(cache)
        previos = {k: datos[k] for k in datos.files}
    pendientes = [f for f in filas if _clave(f[0], segundos) not in previos]
    if pendientes:
        from ..locutor import ExtractorEcapa

        extractor = ExtractorEcapa(str(ruta_modelo))
        inicio = time.time()
        for n, (archivo, _, _) in enumerate(pendientes, 1):
            previos[_clave(archivo, segundos)] = extractor.extraer(recortar(leer_wav_16k(archivo), segundos))
            if n % 200 == 0:
                print(f"  embeddings {n}/{len(pendientes)} ({time.time() - inicio:.0f} s)", file=sys.stderr)
        if cache:
            np.savez_compressed(cache, **previos)
    return previos


def evaluar_antispoofing(filas, ruta_pesos: Path, muestras_entrada: int, umbral: float, n: int,
                         segundos: float | None = None, silencio_s: float = 0.0) -> dict:
    from ..antispoofing import DetectorAasist

    detector = DetectorAasist(str(ruta_pesos), muestras_entrada)
    elegidas = [filas[k] for k in np.random.default_rng(1).permutation(len(filas))[:n]]
    margen = np.zeros(int(silencio_s * FS), dtype=np.float32)

    def preparar(archivo: Path) -> np.ndarray:
        # La app graba desde que la persona toca hasta que vuelve a tocar: hay silencio antes y después de la voz.
        return np.concatenate([margen, recortar(leer_wav_16k(archivo), segundos), margen])

    probabilidades = np.array([detector.probabilidad_bona_fide(preparar(f[0])) for f in elegidas])
    rechazos = int(np.sum(probabilidades < umbral))
    bajo, alto = metricas.intervalo_wilson(rechazos, len(probabilidades))
    return {
        "grabacionesGenuinas": len(probabilidades),
        "umbralBonaFide": umbral,
        "falsoRechazoBonaFide": round(rechazos / len(probabilidades), 4),
        "ic95": [round(bajo, 4), round(alto, 4)],
        "percentiles": {p: round(float(np.percentile(probabilidades, p)), 4) for p in (5, 10, 25, 50)},
        "silencioAgregadoS": silencio_s,
        "nota": "Solo voz real (bona fide). No mide la detección de ataques: el corpus no contiene audio falso.",
    }


def _punto_en_prueba(nombre: str, umbral: float, ensayos) -> dict:
    p = metricas.tasas(ensayos.genuinos, ensayos.impostores, umbral)
    pm = metricas.tasas(ensayos.genuinos, ensayos.impostores_mismo_genero, umbral)
    fa = int(round(p.far * ensayos.impostores.size))
    fr = int(round(p.frr * ensayos.genuinos.size))
    return {
        "criterio": nombre,
        "umbral": round(umbral, 4),
        "far": round(p.far, 5),
        "farIc95": [round(v, 5) for v in metricas.intervalo_wilson(fa, ensayos.impostores.size)],
        "farMismoGenero": round(pm.far, 5),
        "frr": round(p.frr, 5),
        "frrIc95": [round(v, 5) for v in metricas.intervalo_wilson(fr, ensayos.genuinos.size)],
    }


def ejecutar(args) -> dict:
    parametros = cargar_parametros(args.parametros)
    filas = manifiesto_desde_csv(args.manifiesto) if args.manifiesto else manifiesto_desde_openslr(args.openslr)
    filas = seleccionar(filas, args.max_por_hablante)
    if not filas:
        raise SystemExit("No se encontraron grabaciones.")
    args.salida.mkdir(parents=True, exist_ok=True)
    cache = None if args.sin_cache else args.salida / "embeddings.npz"
    print(f"{len(filas)} grabaciones de {len({f[1] for f in filas})} hablantes", file=sys.stderr)
    embeddings = extraer_embeddings(filas, args.modelos / "spkrec-ecapa-voxceleb", cache, args.duracion_s)

    grupos: dict[str, dict[str, Hablante]] = {"desarrollo": {}, "prueba": {}}
    for archivo, id_h, genero in filas:
        h = grupos[particion(id_h)].setdefault(id_h, Hablante(id_h, genero))
        h.embeddings.append(embeddings[_clave(archivo, args.duracion_s)])
    n_enrol = parametros.enrolamiento.muestras_validas
    dev = construir_ensayos(list(grupos["desarrollo"].values()), n_enrol, args.max_por_hablante)
    test = construir_ensayos(list(grupos["prueba"].values()), n_enrol, args.max_por_hablante)

    eer_dev = metricas.eer(dev.genuinos, dev.impostores_mismo_genero)
    candidatos = {"EER (desarrollo)": eer_dev.umbral}
    for objetivo in args.far_objetivo:
        punto = metricas.umbral_para_far(dev.genuinos, dev.impostores_mismo_genero, objetivo)
        candidatos[f"FAR <= {objetivo:g} (desarrollo)"] = punto.umbral
    dcf = metricas.min_dcf(dev.genuinos, dev.impostores_mismo_genero)
    candidatos["minDCF p=0.01 (desarrollo)"] = dcf.umbral
    vigente = parametros.verificacion.similitud_coseno_minima

    calibrador = entrenar(dev.genuinos, dev.impostores_mismo_genero)
    (args.salida / "calibrador.json").write_text(calibrador.a_json(), encoding="utf-8")

    resultado = {
        "fecha": date.today().isoformat(),
        "modelo": "speechbrain/spkrec-ecapa-voxceleb (preentrenado, sin reentrenar)",
        "fuente": str(args.manifiesto) if args.manifiesto else "OpenSLR 73 (es_pe)",
        "grabaciones": len(filas),
        "muestrasEnrolamiento": n_enrol,
        "duracionPorGrabacionS": args.duracion_s or "completa",
        "desarrollo": {
            "hablantes": dev.hablantes, "genuinos": int(dev.genuinos.size),
            "impostores": int(dev.impostores.size), "impostoresMismoGenero": int(dev.impostores_mismo_genero.size),
            "eerMismoGenero": round(eer_dev.far, 5), "umbralEer": round(eer_dev.umbral, 4),
            "eerTodos": round(metricas.eer(dev.genuinos, dev.impostores).far, 5),
        },
        "prueba": {
            "hablantes": test.hablantes, "genuinos": int(test.genuinos.size),
            "impostores": int(test.impostores.size), "impostoresMismoGenero": int(test.impostores_mismo_genero.size),
            "eerMismoGenero": round(metricas.eer(test.genuinos, test.impostores_mismo_genero).far, 5),
            "eerTodos": round(metricas.eer(test.genuinos, test.impostores).far, 5),
            "farMinimaMedible": metricas.far_minima_medible(int(test.impostores.size)),
            "puntos": [_punto_en_prueba(f"Vigente D-055 ({vigente})", vigente, test)]
            + [_punto_en_prueba(nombre, u, test) for nombre, u in candidatos.items()],
        },
        "puntajes": {
            "genuinosPrueba": {p: round(float(np.percentile(test.genuinos, p)), 4) for p in (1, 5, 10, 50)},
            "impostoresPrueba": {p: round(float(np.percentile(test.impostores, p)), 4) for p in (50, 99, 99.9, 100)},
        },
        "calibrador": {
            "a": round(calibrador.a, 4), "b": round(calibrador.b, 4),
            "entropiaCruzadaPruebaBits": round(entropia_cruzada(calibrador, test.genuinos, test.impostores_mismo_genero), 4),
            "probabilidadEnUmbralVigente": round(float(calibrador.probabilidad([vigente])[0]), 4),
            "umbralCosenoParaP099": round(calibrador.umbral_coseno_para_probabilidad(0.99), 4),
        },
    }
    if args.antispoofing:
        resultado["antispoofing"] = evaluar_antispoofing(
            filas, args.modelos / "aasist" / "AASIST.pth", parametros.antispoofing.muestras_entrada,
            parametros.antispoofing.probabilidad_bona_fide_minima, args.antispoofing, args.duracion_s,
            args.silencio_s)
    (args.salida / "resultados.json").write_text(json.dumps(resultado, indent=2, ensure_ascii=False), encoding="utf-8")
    np.savez_compressed(args.salida / "puntajes_prueba.npz", genuinos=test.genuinos,
                        impostores=test.impostores, impostores_mismo_genero=test.impostores_mismo_genero)
    return resultado


def main(argv=None) -> None:
    ap = argparse.ArgumentParser(description=__doc__.split("\n\n")[0])
    fuente = ap.add_mutually_exclusive_group(required=True)
    fuente.add_argument("--manifiesto", type=Path)
    fuente.add_argument("--openslr", type=Path, nargs="+")
    ap.add_argument("--salida", type=Path, required=True)
    ap.add_argument("--modelos", type=Path, default=RAIZ / "modelos")
    ap.add_argument("--parametros", type=Path, default=None)
    ap.add_argument("--max-por-hablante", type=int, default=40)
    ap.add_argument("--far-objetivo", type=float, nargs="+", default=[0.01, 0.001])
    ap.add_argument("--antispoofing", type=int, default=0, metavar="N")
    ap.add_argument("--duracion-s", type=float, default=None,
                    help="recorta cada grabación a N segundos (p. ej. 3 para imitar la frase del desafío)")
    ap.add_argument("--silencio-s", type=float, default=0.0,
                    help="silencio agregado antes y después del recorte, solo para AASIST (imita la captura de la app)")
    ap.add_argument("--sin-cache", action="store_true")
    print(json.dumps(ejecutar(ap.parse_args(argv)), indent=2, ensure_ascii=False))


if __name__ == "__main__":
    main()
