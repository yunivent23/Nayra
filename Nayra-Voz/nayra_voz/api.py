"""API REST interna del servicio de voz (D-010: FastAPI, solo red interna, solo Spring Boot).

CONTRATO PROVISIONAL — el contrato formal Java ↔ Python sigue pendiente en
`docs/04_API.md` (D-014). Estas rutas existen solo para el prototipo y viven bajo
`/prototipo/v1`; deben revisarse cuando se apruebe el contrato.

- Autenticación entre servicios: token en la cabecera Authorization, leído de la
  variable NAYRA_VOZ_TOKEN_SERVICIO y comparado en tiempo constante (D-010).
- El audio llega como multipart/form-data (D-010, D-057), se procesa en memoria y
  se descarta. No se registra el audio, el PIN ni los embeddings (D-013, D-061).
"""
from __future__ import annotations

import hmac
import logging
import os
import uuid
from dataclasses import dataclass

from fastapi import Depends, FastAPI, File, Form, Header, HTTPException, UploadFile, status

from .audio import AudioInvalido, decodificar_wav
from .config import Parametros
from .contenido import TranscriptorPin
from .pipeline import Pipeline
from .resultados import Etapa, Motivo, ResultadoEtapa, ResultadoPipeline

log = logging.getLogger("nayra_voz")
VARIABLE_TOKEN = "NAYRA_VOZ_TOKEN_SERVICIO"
TAMANO_MAXIMO_BYTES = 2 * 1024 * 1024  # 20 s de WAV 16 kHz mono 16 bits ≈ 640 KB; margen amplio


@dataclass
class Componentes:
    parametros: Parametros
    pipeline: Pipeline
    transcriptor_pin: TranscriptorPin


def crear_app(componentes: Componentes | None, token_servicio: str | None = None) -> FastAPI:
    """`componentes=None` levanta la API sin modelos: todas las rutas responden 503."""
    app = FastAPI(title="Nayra — servicio de voz (prototipo)", docs_url=None, redoc_url=None, openapi_url=None)
    token = token_servicio if token_servicio is not None else os.environ.get(VARIABLE_TOKEN, "")

    def autorizar(authorization: str | None = Header(default=None)) -> None:
        esperado = f"Bearer {token}"
        if not token or authorization is None or not hmac.compare_digest(authorization.encode(), esperado.encode()):
            raise HTTPException(status.HTTP_401_UNAUTHORIZED, "No autorizado.")

    def listos() -> Componentes:
        if componentes is None:
            raise HTTPException(status.HTTP_503_SERVICE_UNAVAILABLE, "Servicio de voz no disponible.")
        return componentes

    async def leer_audio(archivo: UploadFile, c: Componentes):
        datos = await archivo.read(TAMANO_MAXIMO_BYTES + 1)
        if len(datos) > TAMANO_MAXIMO_BYTES:
            return None
        try:
            return decodificar_wav(datos, c.parametros.audio)
        except AudioInvalido:
            return None

    def formato_invalido(c: Componentes) -> dict:
        etapa = ResultadoEtapa(Etapa.FORMATO, False)
        return ResultadoPipeline(False, Motivo.FORMATO_INVALIDO, [etapa], c.parametros.version).como_dict()

    @app.get("/prototipo/v1/salud")
    def salud(_: None = Depends(autorizar)) -> dict:
        return {
            "listo": componentes is not None,
            "versionParametros": componentes.parametros.version if componentes else None,
            "estadoParametros": componentes.parametros.estado if componentes else None,
        }

    @app.post("/prototipo/v1/verificaciones")
    async def verificar(
        usuario_id: uuid.UUID = Form(..., alias="usuarioId"),
        desafio: str = Form(..., max_length=200),
        audio: UploadFile = File(...),
        _: None = Depends(autorizar),
    ) -> dict:
        c = listos()
        solicitud = uuid.uuid4().hex
        muestra = await leer_audio(audio, c)
        if muestra is None:
            log.info("verificacion solicitud=%s motivo=%s", solicitud, Motivo.FORMATO_INVALIDO.value)
            return formato_invalido(c)
        resultado = c.pipeline.verificar(str(usuario_id), muestra, desafio)
        log.info("verificacion solicitud=%s motivo=%s", solicitud, resultado.motivo.value if resultado.motivo else "OK")
        return resultado.como_dict()

    @app.post("/prototipo/v1/enrolamientos/{usuario_id}/muestras")
    async def agregar_muestra(
        usuario_id: uuid.UUID,
        desafio: str = Form(..., max_length=200),
        audio: UploadFile = File(...),
        _: None = Depends(autorizar),
    ) -> dict:
        c = listos()
        muestra = await leer_audio(audio, c)
        if muestra is None:
            return {**formato_invalido(c), "muestrasValidas": None}
        resultado, validas = c.pipeline.agregar_muestra(str(usuario_id), muestra, desafio)
        log.info("enrolamiento muestra motivo=%s", resultado.motivo.value if resultado.motivo else "OK")
        return {**resultado.como_dict(), "muestrasValidas": validas}

    @app.post("/prototipo/v1/enrolamientos/{usuario_id}/finalizacion")
    def finalizar(usuario_id: uuid.UUID, _: None = Depends(autorizar)) -> dict:
        c = listos()
        correcto, motivo, validas = c.pipeline.finalizar_enrolamiento(str(usuario_id))
        log.info("enrolamiento finalizado correcto=%s motivo=%s", correcto, motivo or "OK")
        return {"correcto": correcto, "motivo": motivo, "muestrasValidas": validas}

    @app.delete("/prototipo/v1/enrolamientos/{usuario_id}", status_code=status.HTTP_204_NO_CONTENT)
    def cancelar(usuario_id: uuid.UUID, _: None = Depends(autorizar)) -> None:
        listos().pipeline.cancelar_enrolamiento(str(usuario_id))

    @app.post("/prototipo/v1/transcripciones-pin")
    async def transcribir_pin(audio: UploadFile = File(...), _: None = Depends(autorizar)) -> dict:
        """PROVISIONAL (D-046): transcribe el PIN dictado. Fuera del pipeline biométrico; sin registro."""
        c = listos()
        muestra = await leer_audio(audio, c)
        pin = c.transcriptor_pin.transcribir(muestra) if muestra is not None else None
        return {"reconocido": pin is not None, "pin": pin}

    return app
