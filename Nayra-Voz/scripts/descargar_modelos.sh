#!/usr/bin/env bash
# Descarga los modelos preentrenados aprobados (D-011, D-012, D-046) en ./modelos.
# Los modelos no se versionan en el repositorio. Licencias a reconfirmar al fijar versiones.
set -euo pipefail
DESTINO="${NAYRA_VOZ_MODELOS:-$(dirname "$0")/../modelos}"
mkdir -p "$DESTINO/aasist" "$DESTINO/spkrec-ecapa-voxceleb"

# AASIST (MIT) — pesos preentrenados ASVspoof 2019 LA
curl -fsSL -o "$DESTINO/aasist/AASIST.pth" \
  https://raw.githubusercontent.com/clovaai/aasist/main/models/weights/AASIST.pth

# Vosk (Apache 2.0) — modelo pequeño en español
curl -fsSL -o /tmp/vosk-es.zip https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip
unzip -q -o /tmp/vosk-es.zip -d "$DESTINO" && rm -f /tmp/vosk-es.zip

# SpeechBrain ECAPA-TDNN (Apache 2.0)
for f in hyperparams.yaml embedding_model.ckpt mean_var_norm_emb.ckpt classifier.ckpt label_encoder.txt; do
  curl -fsSL -o "$DESTINO/spkrec-ecapa-voxceleb/$f" \
    "https://huggingface.co/speechbrain/spkrec-ecapa-voxceleb/resolve/main/$f"
done
echo "Modelos descargados en $DESTINO"
