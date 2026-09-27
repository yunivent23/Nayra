# Nayra-Voz — servicio de voz (prototipo AG-13)

Servicio interno FastAPI (D-010). Solo lo llama Spring Boot; nunca la app ni internet.

Pipeline (D-059): calidad (WebRTC VAD, candidata provisional D-058) → contenido (Vosk, gramática cerrada, D-046) → anti-spoofing (AASIST, D-012) → verificación 1:1 (ECAPA-TDNN, coseno, D-011). Python aplica los umbrales y devuelve veredictos por etapa; Spring Boot decide (D-056).

Todos los valores de `config/parametros_provisionales.yaml` son **PROVISIONAL — PENDIENTE DE VALIDACIÓN** (`docs/07`, «Parámetros provisionales del prototipo»).

## Ejecutar

```bash
python3 -m venv .venv && . .venv/bin/activate
pip install -r requirements-dev.txt
scripts/descargar_modelos.sh          # AASIST, Vosk y ECAPA en modelos/ (no se versionan)
export NAYRA_VOZ_TOKEN_SERVICIO=...   # token de servicio, mismo valor que usa Spring Boot
export NAYRA_VOZ_CLAVE_EMBEDDINGS=... # 32 bytes en Base64 (AES-256-GCM)
uvicorn nayra_voz.servidor:app --host 127.0.0.1 --port 8001
pytest
```

Ningún secreto se escribe en el repositorio (D-017). Si falta un modelo, el servicio arranca y responde 503.

## Provisional por dependencias externas

- Contrato bajo `/prototipo/v1` hasta `docs/04_API.md` (D-014).
- Referencias de voz cifradas **en memoria** en lugar del esquema `biometria` (D-051).
- El estado del enrolamiento en curso es por instancia (relevante con 2 instancias, D-023).

`nayra_voz/terceros/aasist/` contiene el modelo AASIST de clovaai/aasist (licencia MIT, ver `NOTICE.md`).
