"""API interna del servicio de voz (04_API.md §3, D-040).

Devuelve puntajes técnicos; no aplica umbrales ni decide autenticaciones (lo hace Spring Boot, D-038).
El audio se procesa en memoria y nunca se escribe en disco ni en logs.
"""
from __future__ import annotations

import hmac
import logging
import uuid
from enum import Enum

from fastapi import Depends, FastAPI, File, Form, Header, HTTPException, UploadFile
from fastapi.responses import JSONResponse

from . import biometria, calidad
from .audio import AudioInvalido, leer_wav
from .cifrado import CifradoEmbedding
from .contenido import coincide
from .motores import Motores
from .repositorio import PerfilCifrado, RepositorioPerfiles

log = logging.getLogger("nayra_voz")

MUESTRAS_ENROLAMIENTO = 3  # D-039


class Modo(str, Enum):
    evaluar = "evaluar"
    guardar = "guardar"


def crear_app(token_servicio: str, motores: Motores, repositorio: RepositorioPerfiles,
              cifrado: CifradoEmbedding, vad_agresividad: int = 2) -> FastAPI:
    app = FastAPI(title="Nayra - servicio de voz (interno)", docs_url=None, redoc_url=None, openapi_url=None)

    def autenticar_servicio(x_nayra_service_token: str = Header(default="")) -> None:
        if not hmac.compare_digest(x_nayra_service_token.encode(), token_servicio.encode()):
            raise HTTPException(status_code=401, detail="No autorizado")

    @app.exception_handler(AudioInvalido)
    async def audio_invalido(_, e: AudioInvalido):
        return JSONResponse(status_code=422, content={"error": "AUDIO_INVALIDO", "detalle": str(e)})

    async def muestras_de(archivo: UploadFile):
        return leer_wav(await archivo.read())

    def analizar(muestras, texto_esperado: str) -> dict:
        c = calidad.medir(muestras, vad_agresividad)
        t = motores.contenido.transcribir(muestras)
        return {
            "calidad": {"duracion_s": c.duracion_s, "voz_neta_s": c.voz_neta_s,
                        "snr_db": c.snr_db, "saturacion": c.saturacion},
            "contenido": {"transcripcion": t.texto, "coincide": coincide(t.texto, texto_esperado),
                          "confianza": t.confianza},
            "spoofing": {"puntaje": motores.spoofing.puntaje(muestras)},
        }

    def modelos() -> dict:
        return {"biometria": f"{motores.biometria.nombre}@{motores.biometria.version}",
                "antispoofing": motores.spoofing.nombre, "contenido": motores.contenido.nombre}

    @app.get("/interno/v1/salud", dependencies=[Depends(autenticar_servicio)])
    def salud():
        return {"estado": "OK", "modelos": modelos()}

    @app.post("/interno/v1/verificaciones", dependencies=[Depends(autenticar_servicio)])
    async def verificar(usuario_id: int = Form(...), texto_esperado: str = Form(...), audio: UploadFile = File(...)):
        request_id = str(uuid.uuid4())
        muestras = await muestras_de(audio)
        resultado = analizar(muestras, texto_esperado)

        perfil = repositorio.obtener(usuario_id)
        similitud = None
        encontrado = perfil is not None and perfil.modelo_version == motores.biometria.version
        if encontrado:
            referencia = cifrado.descifrar(perfil.embedding_cifrado, perfil.iv, usuario_id, perfil.modelo_version)
            similitud = biometria.similitud(motores.biometria.embedding(muestras), referencia)
        log.info("verificacion request_id=%s perfil=%s", request_id, encontrado)
        return {"request_id": request_id, **resultado,
                "biometria": {"similitud": similitud, "perfil_encontrado": encontrado,
                              "requiere_reenrolamiento": perfil is not None and not encontrado},
                "modelos": modelos()}

    @app.post("/interno/v1/analisis", dependencies=[Depends(autenticar_servicio)])
    async def analisis(texto_esperado: str = Form(...), audio: UploadFile = File(...)):
        return {"request_id": str(uuid.uuid4()), **analizar(await muestras_de(audio), texto_esperado),
                "modelos": modelos()}

    @app.post("/interno/v1/perfiles/{usuario_id}", dependencies=[Depends(autenticar_servicio)])
    async def enrolar(usuario_id: int, modo: Modo, audio_1: UploadFile = File(...),
                      audio_2: UploadFile = File(...), audio_3: UploadFile = File(...)):
        embeddings = [motores.biometria.embedding(await muestras_de(a)) for a in (audio_1, audio_2, audio_3)]
        c = biometria.centroide(embeddings)
        similitudes = [round(biometria.similitud(e, c), 4) for e in embeddings]
        if modo is Modo.guardar:
            version = motores.biometria.version
            emb_cifrado, iv = cifrado.cifrar(c, usuario_id, version)
            repositorio.guardar(PerfilCifrado(usuario_id, emb_cifrado, iv, motores.biometria.nombre,
                                              version, MUESTRAS_ENROLAMIENTO))
        return {"modelo": motores.biometria.nombre, "modelo_version": motores.biometria.version,
                "num_muestras": MUESTRAS_ENROLAMIENTO, "similitud_al_centroide": similitudes,
                "guardado": modo is Modo.guardar}

    @app.delete("/interno/v1/perfiles/{usuario_id}", status_code=204, dependencies=[Depends(autenticar_servicio)])
    def eliminar(usuario_id: int):
        repositorio.eliminar(usuario_id)

    return app
