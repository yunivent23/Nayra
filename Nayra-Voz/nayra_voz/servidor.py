"""Arranque del servicio: carga los modelos una sola vez (D-010).

Si falta algún modelo o la clave de cifrado, la API arranca igual pero responde
503 ("servicio de voz no disponible"), que Spring Boot no cuenta como intento
fallido (D-010, D-044).
"""
from __future__ import annotations

import logging
import os
from pathlib import Path

from .almacen import AlmacenPerfilesEnMemoria, almacen_desde_entorno, cifrador_desde_entorno
from .antispoofing import DetectorAasist, EvaluadorSpoofing
from .api import Componentes, crear_app
from .calidad import EvaluadorCalidad
from .config import cargar_parametros
from .contenido import ReconocedorVosk, TranscriptorPin, VerificadorContenido
from .locutor import ExtractorEcapa
from .pipeline import Pipeline
from .vocabulario import cargar_vocabulario

log = logging.getLogger("nayra_voz")
RAIZ_MODELOS = Path(os.environ.get("NAYRA_VOZ_MODELOS", Path(__file__).resolve().parent.parent / "modelos"))


def construir_componentes() -> Componentes:
    parametros = cargar_parametros()
    almacen = almacen_desde_entorno()
    if isinstance(almacen, AlmacenPerfilesEnMemoria):
        log.warning("NAYRA_VOZ_BD no está definida: perfiles en memoria (solo desarrollo; se pierden al reiniciar).")
    vocabulario = cargar_vocabulario()
    reconocedor = ReconocedorVosk(str(RAIZ_MODELOS / parametros.contenido.modelo))
    pipeline = Pipeline(
        parametros=parametros,
        calidad=EvaluadorCalidad(parametros.calidad),
        contenido=VerificadorContenido(reconocedor, vocabulario, parametros.contenido),
        spoofing=EvaluadorSpoofing(
            DetectorAasist(str(RAIZ_MODELOS / "aasist" / "AASIST.pth"), parametros.antispoofing.muestras_entrada),
            parametros.antispoofing,
        ),
        extractor=ExtractorEcapa(str(RAIZ_MODELOS / "spkrec-ecapa-voxceleb")),
        almacen=almacen,
        cifrador=cifrador_desde_entorno(),
    )
    return Componentes(parametros, pipeline, TranscriptorPin(reconocedor, vocabulario, parametros.contenido))


def crear_app_desde_entorno():
    logging.basicConfig(level=logging.INFO)
    try:
        componentes = construir_componentes()
    except Exception as exc:  # noqa: BLE001 — cualquier fallo de carga deja el servicio en 503
        log.error("Servicio de voz no disponible: %s", type(exc).__name__)
        componentes = None
    return crear_app(componentes)


app = crear_app_desde_entorno() if os.environ.get("NAYRA_VOZ_AUTOARRANQUE", "1") == "1" else None
