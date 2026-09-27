"""Veredictos técnicos por etapa (D-056).

El servicio Python aplica los umbrales y devuelve el veredicto de cada etapa;
la decisión final de autenticación es de Spring Boot.
"""
from __future__ import annotations

from dataclasses import dataclass, field
from enum import Enum


class Etapa(str, Enum):
    FORMATO = "FORMATO"
    CALIDAD = "CALIDAD"
    CONTENIDO = "CONTENIDO"
    ANTISPOOFING = "ANTISPOOFING"
    VERIFICACION = "VERIFICACION"


class Motivo(str, Enum):
    # Motivos de 05 §27.4
    FORMATO_INVALIDO = "FORMATO_INVALIDO"
    CALIDAD_INSUFICIENTE = "CALIDAD_INSUFICIENTE"
    CONTENIDO_INCORRECTO = "CONTENIDO_INCORRECTO"
    POSIBLE_SPOOFING = "POSIBLE_SPOOFING"
    NO_COINCIDE = "NO_COINCIDE"
    SIN_REFERENCIA = "SIN_REFERENCIA"
    # Enrolamiento: se alcanzó el máximo de muestras (D-059, "hasta 5")
    LIMITE_MUESTRAS = "LIMITE_MUESTRAS"


@dataclass
class ResultadoEtapa:
    etapa: Etapa
    aprobada: bool
    puntaje: float | None = None
    umbral: float | None = None
    detalle: dict = field(default_factory=dict)

    def como_dict(self) -> dict:
        return {
            "etapa": self.etapa.value,
            "aprobada": self.aprobada,
            "puntaje": self.puntaje,
            "umbral": self.umbral,
            "detalle": self.detalle,
        }


@dataclass
class ResultadoPipeline:
    aprobado: bool
    motivo: Motivo | None
    etapas: list[ResultadoEtapa]
    version_parametros: str

    def como_dict(self) -> dict:
        return {
            "aprobado": self.aprobado,
            "motivo": self.motivo.value if self.motivo else None,
            "etapas": [e.como_dict() for e in self.etapas],
            "versionParametros": self.version_parametros,
        }
