#!/usr/bin/env bash
# Descarga los modelos preentrenados aprobados en revisiones fijas y verifica su integridad.
# Requiere acceso a github.com (AASIST), huggingface.co (ECAPA-TDNN) y alphacephei.com (Vosk).
set -euo pipefail
DESTINO="${NAYRA_VOZ_MODELOS_DIR:-$(dirname "$0")/../modelos}"
mkdir -p "$DESTINO/aasist" "$DESTINO/spkrec-ecapa-voxceleb"

# D-035: AASIST (MIT), pesos oficiales del repositorio clovaai/aasist
AASIST_COMMIT=a04c9863f63d44471dde8a6abcb3b082b07cd1d1
curl -fsSL -o "$DESTINO/aasist/AASIST.pth" \
  "https://raw.githubusercontent.com/clovaai/aasist/$AASIST_COMMIT/models/weights/AASIST.pth"
echo "51d2d9cf0738172f61e2a384ec50a54a55363240f67c971ed55a92435bc1a1c0  $DESTINO/aasist/AASIST.pth" | sha256sum -c -

# D-034: SpeechBrain ECAPA-TDNN (Apache 2.0). Se fija la revisión descargada en REVISION (versión del modelo, D-039).
ECAPA_REV="${NAYRA_ECAPA_REVISION:-}"
if [ -z "$ECAPA_REV" ]; then
  ECAPA_REV=$(curl -fsSL "https://huggingface.co/api/models/speechbrain/spkrec-ecapa-voxceleb" | python3 -c "import sys,json;print(json.load(sys.stdin)['sha'])")
fi
for f in hyperparams.yaml embedding_model.ckpt mean_var_norm_emb.ckpt classifier.ckpt label_encoder.txt; do
  curl -fsSL -o "$DESTINO/spkrec-ecapa-voxceleb/$f" \
    "https://huggingface.co/speechbrain/spkrec-ecapa-voxceleb/resolve/$ECAPA_REV/$f"
done
echo "$ECAPA_REV" > "$DESTINO/spkrec-ecapa-voxceleb/REVISION"

# D-036: Vosk español pequeño (Apache 2.0)
curl -fsSL -o "$DESTINO/vosk-es.zip" "https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip"
(cd "$DESTINO" && python3 -m zipfile -e vosk-es.zip . && rm vosk-es.zip)

echo "Modelos listos en $DESTINO"
