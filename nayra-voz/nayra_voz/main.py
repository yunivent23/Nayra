"""Punto de entrada: carga modelos reales desde NAYRA_VOZ_MODELOS_DIR y expone la API interna.

Ejecutar solo en red interna:  uvicorn nayra_voz.main:app --host 127.0.0.1 --port 8001
"""
from __future__ import annotations

import logging

from .antispoofing import DetectorAasist
from .app import crear_app
from .biometria import ExtractorEcapa
from .cifrado import CifradoEmbedding
from .config import Config
from .contenido import ReconocedorVosk
from .motores import Motores
from .repositorio import RepositorioPostgres
from .vocabulario import cargar_palabras, gramatica

logging.basicConfig(level=logging.INFO)

config = Config.desde_entorno()
_dir = config.modelos_dir
_version_ecapa = (_dir / "spkrec-ecapa-voxceleb" / "REVISION").read_text().strip()

app = crear_app(
    token_servicio=config.token_servicio,
    motores=Motores(
        contenido=ReconocedorVosk(_dir / "vosk-model-small-es-0.42", gramatica(cargar_palabras(config.vocabulario))),
        spoofing=DetectorAasist(_dir / "aasist" / "AASIST.pth"),
        biometria=ExtractorEcapa(_dir / "spkrec-ecapa-voxceleb", _version_ecapa),
    ),
    repositorio=RepositorioPostgres(config.db_dsn),
    cifrado=CifradoEmbedding(config.clave_cifrado),
    vad_agresividad=config.vad_agresividad,
)
