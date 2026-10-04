#!/usr/bin/env bash
# Descarga el modelo pequeño de Vosk en español para el comando «Iniciar sesión Nayra» (D-081) y comprueba su
# huella. El modelo (unos 40 MB) no se versiona; sin él la app funciona, pero solo con el botón de voz.
set -euo pipefail
cd "$(dirname "$0")/.."
destino=assets/modelos/vosk-model-small-es-0.42.zip
sha256=09b239888f633ef2f0b4e09736e3d9936acfd810bc65d53fad45261762c6511f
if [ ! -f "$destino" ]; then
  curl -fL -o "$destino.tmp" https://alphacephei.com/vosk/models/vosk-model-small-es-0.42.zip
  mv "$destino.tmp" "$destino"
fi
echo "$sha256  $destino" | sha256sum -c -
