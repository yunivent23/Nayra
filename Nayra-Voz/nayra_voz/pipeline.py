"""Pipeline de verificación y enrolamiento (D-059, D-056).

Orden: calidad → contenido del desafío → anti-spoofing → verificación 1:1.
En operación se detiene en la primera etapa fallida. Este servicio aplica los
umbrales técnicos y devuelve veredictos por etapa; Spring Boot decide.
"""
from __future__ import annotations

import threading
import time
from dataclasses import dataclass, field

import numpy as np

from .almacen import AlmacenPerfiles, CifradorEmbeddings
from .antispoofing import EvaluadorSpoofing
from .audio import Audio
from .calidad import EvaluadorCalidad
from .config import Parametros
from .contenido import VerificadorContenido
from .locutor import ExtractorEmbedding, centroide, similitud_coseno
from .resultados import Etapa, Motivo, ResultadoEtapa, ResultadoPipeline


@dataclass
class _EnrolamientoPendiente:
    embeddings: list[np.ndarray] = field(default_factory=list)
    muestras_recibidas: int = 0
    iniciado: float = field(default_factory=time.monotonic)


class Pipeline:
    def __init__(
        self,
        parametros: Parametros,
        calidad: EvaluadorCalidad,
        contenido: VerificadorContenido,
        spoofing: EvaluadorSpoofing,
        extractor: ExtractorEmbedding,
        almacen: AlmacenPerfiles,
        cifrador: CifradorEmbeddings,
    ):
        self._p = parametros
        self._calidad = calidad
        self._contenido = contenido
        self._spoofing = spoofing
        self._extractor = extractor
        self._almacen = almacen
        self._cifrador = cifrador
        # PROVISIONAL: estado del enrolamiento en memoria de esta instancia.
        # Con 2 instancias (D-023) haría falta afinidad o un almacén compartido.
        self._pendientes: dict[str, _EnrolamientoPendiente] = {}
        self._candado = threading.Lock()

    def _resultado(self, etapas: list[ResultadoEtapa], motivo: Motivo | None) -> ResultadoPipeline:
        return ResultadoPipeline(motivo is None, motivo, etapas, self._p.version)

    def _etapas_comunes(self, audio: Audio, desafio: str) -> tuple[list[ResultadoEtapa], Motivo | None]:
        etapas = [self._calidad.evaluar(audio)]
        if not etapas[-1].aprobada:
            return etapas, Motivo.CALIDAD_INSUFICIENTE
        etapas.append(self._contenido.evaluar(audio, desafio))
        if not etapas[-1].aprobada:
            return etapas, Motivo.CONTENIDO_INCORRECTO
        etapas.append(self._spoofing.evaluar(audio))
        if not etapas[-1].aprobada:
            return etapas, Motivo.POSIBLE_SPOOFING
        return etapas, None

    # --- Verificación (inicio de sesión y cambio de dispositivo) ---

    def verificar(self, usuario_id: str, audio: Audio, desafio: str) -> ResultadoPipeline:
        perfil = self._almacen.obtener(usuario_id)
        if perfil is None or not perfil.activo:
            # Sin perfil o con el perfil revocado (B-8, B-13): hace falta volver a enrolar.
            return self._resultado([], Motivo.SIN_REFERENCIA)
        etapas, motivo = self._etapas_comunes(audio, desafio)
        if motivo:
            return self._resultado(etapas, motivo)
        if perfil.modelo != self._extractor.nombre_modelo or perfil.version_modelo != self._extractor.version_modelo:
            # Solo se compara contra un perfil del mismo modelo y versión (B-9); si no, hay que re-enrolar (D-013).
            return self._resultado(etapas, Motivo.SIN_REFERENCIA)
        referencia = self._cifrador.descifrar(perfil)
        similitud = similitud_coseno(self._extractor.extraer(audio.muestras), referencia)
        umbral = self._p.verificacion.similitud_coseno_minima
        etapas.append(ResultadoEtapa(Etapa.VERIFICACION, similitud >= umbral, round(similitud, 4), umbral))
        return self._resultado(etapas, None if similitud >= umbral else Motivo.NO_COINCIDE)

    # --- Enrolamiento (D-052 paso 11) ---

    def _limpiar_vencidos(self) -> None:
        limite = time.monotonic() - self._p.enrolamiento.vida_enrolamiento_pendiente_s
        for clave in [k for k, v in self._pendientes.items() if v.iniciado < limite]:
            del self._pendientes[clave]

    def agregar_muestra(self, usuario_id: str, audio: Audio, desafio: str) -> tuple[ResultadoPipeline, int]:
        """Evalúa una muestra; si es válida guarda su embedding (en memoria) hasta finalizar."""
        with self._candado:
            self._limpiar_vencidos()
            pendiente = self._pendientes.setdefault(usuario_id, _EnrolamientoPendiente())
            if pendiente.muestras_recibidas >= self._p.enrolamiento.muestras_maximas:
                return self._resultado([], Motivo.LIMITE_MUESTRAS), len(pendiente.embeddings)
            pendiente.muestras_recibidas += 1
        etapas, motivo = self._etapas_comunes(audio, desafio)
        if motivo is None:
            embedding = self._extractor.extraer(audio.muestras)
            with self._candado:
                pendiente.embeddings.append(embedding)
        return self._resultado(etapas, motivo), len(pendiente.embeddings)

    def finalizar_enrolamiento(self, usuario_id: str) -> tuple[bool, str | None, int]:
        """Descarta muestras atípicas, calcula el centroide y guarda solo el embedding cifrado.

        El perfil se guarda solo si el enrolamiento terminó bien; si ya había uno, se reemplaza (B-13).
        """
        with self._candado:
            pendiente = self._pendientes.get(usuario_id)
        if pendiente is None:
            return False, "SIN_MUESTRAS", 0
        validas = descartar_atipicas(pendiente.embeddings, self._p.enrolamiento.similitud_minima_al_resto)
        if len(validas) < self._p.enrolamiento.muestras_validas:
            return False, "MUESTRAS_INSUFICIENTES", len(validas)
        perfil = self._cifrador.cifrar(usuario_id, centroide(validas), self._extractor.nombre_modelo,
                                       self._extractor.version_modelo, len(validas))
        self._almacen.guardar(perfil)
        with self._candado:
            self._pendientes.pop(usuario_id, None)
        return True, None, len(validas)

    def cancelar_enrolamiento(self, usuario_id: str) -> None:
        with self._candado:
            self._pendientes.pop(usuario_id, None)


def descartar_atipicas(embeddings: list[np.ndarray], similitud_minima: float) -> list[np.ndarray]:
    """Criterio PROVISIONAL de muestra atípica: similitud con el centroide del resto (D-059)."""
    if len(embeddings) < 3:
        return list(embeddings)
    conservadas = []
    for i, e in enumerate(embeddings):
        resto = [x for j, x in enumerate(embeddings) if j != i]
        if similitud_coseno(e, centroide(resto)) >= similitud_minima:
            conservadas.append(e)
    return conservadas
