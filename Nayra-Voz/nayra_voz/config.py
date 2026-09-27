"""Carga de parámetros técnicos (D-055).

Los valores numéricos viven en `config/parametros_provisionales.yaml`, nunca en el
código (05 §12, 09 §27 regla 2). Todos están marcados como
PROVISIONAL — PENDIENTE DE VALIDACIÓN.
"""
from __future__ import annotations

import os
from dataclasses import dataclass
from pathlib import Path

import yaml

RUTA_POR_DEFECTO = Path(__file__).resolve().parent.parent / "config" / "parametros_provisionales.yaml"
RUTA_VOCABULARIO_POR_DEFECTO = (
    Path(__file__).resolve().parent.parent.parent / "shared" / "desafio" / "vocabulario_v2.json"
)


@dataclass(frozen=True)
class ParametrosAudio:
    frecuencia_hz: int
    canales: int
    bits: int
    duracion_minima_s: float
    duracion_maxima_s: float


@dataclass(frozen=True)
class ParametrosCalidad:
    herramienta_vad: str
    vad_agresividad: int
    vad_trama_ms: int
    voz_neta_minima_s: float
    snr_minimo_db: float
    saturacion_maxima_fraccion: float


@dataclass(frozen=True)
class ParametrosContenido:
    modelo: str
    confianza_minima_por_palabra: float


@dataclass(frozen=True)
class ParametrosAntispoofing:
    modelo: str
    muestras_entrada: int
    probabilidad_bona_fide_minima: float


@dataclass(frozen=True)
class ParametrosVerificacion:
    modelo: str
    similitud_coseno_minima: float


@dataclass(frozen=True)
class ParametrosEnrolamiento:
    muestras_validas: int
    muestras_maximas: int
    similitud_minima_al_resto: float
    vida_enrolamiento_pendiente_s: int


@dataclass(frozen=True)
class Parametros:
    version: str
    estado: str
    audio: ParametrosAudio
    calidad: ParametrosCalidad
    contenido: ParametrosContenido
    antispoofing: ParametrosAntispoofing
    verificacion: ParametrosVerificacion
    enrolamiento: ParametrosEnrolamiento


def cargar_parametros(ruta: str | os.PathLike | None = None) -> Parametros:
    ruta = Path(ruta or os.environ.get("NAYRA_VOZ_PARAMETROS", RUTA_POR_DEFECTO))
    datos = yaml.safe_load(ruta.read_text(encoding="utf-8"))
    return Parametros(
        version=str(datos["version"]),
        estado=str(datos["estado"]),
        audio=ParametrosAudio(**datos["audio"]),
        calidad=ParametrosCalidad(**datos["calidad"]),
        contenido=ParametrosContenido(**datos["contenido"]),
        antispoofing=ParametrosAntispoofing(**datos["antispoofing"]),
        verificacion=ParametrosVerificacion(**datos["verificacion"]),
        enrolamiento=ParametrosEnrolamiento(**datos["enrolamiento"]),
    )
