# syntax=docker/dockerfile:1
# Nayra-Voz (FastAPI, Python 3.11). Servicio INTERNO (D-010): solo lo llama Nayra-Back; no se publica su puerto.
# Contexto de build: raíz del repositorio (vocabulario compartido del desafío, D-054):
#   docker build -f Nayra-Voz/Dockerfile -t nayra-voz .
# Los modelos reales (ECAPA-TDNN D-011, AASIST D-012, Vosk D-046) se descargan durante el build con
# scripts/descargar_modelos.sh y se verifican con SHA-256: si una fuente cambia, el build falla en vez de
# incorporar otro modelo. Así la imagen es inmutable y no descarga nada al arrancar.
# Sin secretos en la imagen (D-017): token de servicio, clave de embeddings y conexión llegan por el entorno.

ARG PYTHON_IMAGEN=python:3.11-slim-bookworm@sha256:a36c24f9cbdf4fd0f52d67f0823eeac19c2028c637cecc392d97f980d4fec56b

FROM ${PYTHON_IMAGEN} AS modelos
RUN apt-get update && apt-get install -y --no-install-recommends curl unzip ca-certificates \
    && rm -rf /var/lib/apt/lists/*
COPY Nayra-Voz/scripts/descargar_modelos.sh /tmp/descargar_modelos.sh
RUN NAYRA_VOZ_MODELOS=/modelos bash /tmp/descargar_modelos.sh
# SHA-256 medidos el 2026-09-29 sobre las descargas del script (licencias: Apache-2.0 ECAPA y Vosk, MIT AASIST).
RUN cd /modelos && printf '%s\n' \
    "51d2d9cf0738172f61e2a384ec50a54a55363240f67c971ed55a92435bc1a1c0  aasist/AASIST.pth" \
    "fd9e3634fe68bd0a427c95e354c0c677374f62b3f434e45b78599950d860d535  spkrec-ecapa-voxceleb/classifier.ckpt" \
    "0575cb64845e6b9a10db9bcb74d5ac32b326b8dc90352671d345e2ee3d0126a2  spkrec-ecapa-voxceleb/embedding_model.ckpt" \
    "6f78854fa04ba59e761437b76a2575d3aba5e5016de3e9b69f0c9a5077fb1a41  spkrec-ecapa-voxceleb/hyperparams.yaml" \
    "e13c3a167bb4112685670ee896d20e2b565af16b3a4ceeaa8689fa4d22adb8b9  spkrec-ecapa-voxceleb/label_encoder.txt" \
    "cd70225b05b37be64fc5a95e24395d804231d43f74b2e1e5a513db7b69b34c33  spkrec-ecapa-voxceleb/mean_var_norm_emb.ckpt" \
    "ff9508e4db01d2e2375da3887973f94ef8057ee85411f4cb10a5c174ce7a4425  vosk-model-small-es-0.42/am/final.mdl" \
    "ce42fba42b99076f16bfe2892c3cab0936fb7409edba95c545be81c74f2f59e5  vosk-model-small-es-0.42/graph/HCLr.fst" \
    "0ddc8aacbc80b2e8d470060a429eb26706b2ae1d32615a499befd60cc86a0304  vosk-model-small-es-0.42/graph/Gr.fst" \
    | sha256sum -c -
# SpeechBrain busca label_encoder.ckpt en la carpeta del modelo; si no existe, lo descarga de HuggingFace AL
# ARRANCAR (hyperparams.yaml apunta a speechbrain/spkrec-ecapa-voxceleb). Sin salida a Internet el servicio
# quedaría en 503. Se deja la copia local para que la carga sea 100 % offline (verificado con HF_HUB_OFFLINE=1).
RUN cp /modelos/spkrec-ecapa-voxceleb/label_encoder.txt /modelos/spkrec-ecapa-voxceleb/label_encoder.ckpt

FROM ${PYTHON_IMAGEN} AS dependencias
RUN python -m venv /opt/venv
ENV PATH=/opt/venv/bin:$PATH
COPY Nayra-Voz/requirements.txt /tmp/requirements.txt
# torch/torchaudio solo CPU (sin CUDA); después el resto, con las mismas versiones fijadas.
RUN pip install --no-cache-dir --index-url https://download.pytorch.org/whl/cpu \
        "torch==$(sed -n 's/^torch==//p' /tmp/requirements.txt)" \
        "torchaudio==$(sed -n 's/^torchaudio==//p' /tmp/requirements.txt)" \
    && pip install --no-cache-dir -r /tmp/requirements.txt \
    && pip check

FROM ${PYTHON_IMAGEN}
RUN useradd --system --uid 10001 --no-create-home nayra
COPY --from=dependencias /opt/venv /opt/venv
COPY --from=modelos /modelos /app/modelos
WORKDIR /app/Nayra-Voz
COPY Nayra-Voz/nayra_voz nayra_voz
COPY Nayra-Voz/config config
COPY shared/desafio/vocabulario_v2.json /app/shared/desafio/vocabulario_v2.json
ENV PATH=/opt/venv/bin:$PATH \
    PYTHONDONTWRITEBYTECODE=1 \
    PYTHONUNBUFFERED=1 \
    HF_HUB_OFFLINE=1 \
    NAYRA_VOZ_MODELOS=/app/modelos \
    NAYRA_VOZ_VOCABULARIO=/app/shared/desafio/vocabulario_v2.json
USER nayra
EXPOSE 8001
# Un solo worker: cada worker cargaría los modelos otra vez y el enrolamiento en curso vive en el proceso.
CMD ["uvicorn", "nayra_voz.servidor:app", "--host", "0.0.0.0", "--port", "8001", "--workers", "1"]
