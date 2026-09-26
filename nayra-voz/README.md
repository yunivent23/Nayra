# nayra-voz — Servicio interno de voz de Nayra

Servicio Python (FastAPI) que calcula **puntajes técnicos** de una muestra de voz. **No decide** autenticaciones: Spring Boot aplica los umbrales y las reglas (D-038, D-040). Solo debe ser accesible desde la red interna; la app móvil nunca lo llama.

| Componente | Tecnología aprobada | Decisión |
|---|---|---|
| Verificación de hablante | SpeechBrain ECAPA-TDNN `speechbrain/spkrec-ecapa-voxceleb`, similitud coseno | D-034 |
| Anti-spoofing | AASIST (ASVspoof 2019 LA), código vendorizado en `nayra_voz/terceros/aasist` (MIT) | D-035 |
| Contenido hablado | Vosk `vosk-model-small-es-0.42` con gramática restringida al desafío | D-036 |
| Calidad | Duración, voz neta (WebRTC VAD), SNR estimada, saturación | D-038 |
| Almacenamiento | Centroide de 3 muestras, AES-256-GCM, esquema `biometria`, sin audio | D-039 |
| API | `04_API.md` §3 | D-040 |

## Puesta en marcha

```bash
python3.11 -m venv .venv && . .venv/bin/activate
pip install torch torchaudio --index-url https://download.pytorch.org/whl/cpu   # D-050: solo CPU
pip install -r requirements-dev.txt
./scripts/descargar_modelos.sh          # AASIST (verificado por SHA-256), ECAPA (revisión fijada en REVISION), Vosk

# Base de datos (una vez, como superusuario): roles separados y esquema biometria
psql -d nayra -v app_pass="'...'" -v voz_pass="'...'" -f db/01_roles_y_esquemas.sql
psql -d nayra -U nayra_voz -f db/02_perfiles_voz.sql

# Variables: ver .env.example
uvicorn nayra_voz.main:app --host 127.0.0.1 --port 8001
```

## Pruebas

```bash
pytest                                   # unitarias + API con dobles; AASIST real si está descargado
NAYRA_VOZ_IT_DSN=postgresql://nayra_voz:...@localhost/BD_DE_PRUEBAS pytest   # + persistencia real
```

## Calibración de umbrales (D-038, D-049)

`evaluacion/calibrar.py` calcula FAR, FRR, EER y el umbral para una FAR objetivo desde un CSV de puntajes del **dataset de calibración** (voluntarios con consentimiento, fuera del sistema). El umbral se elige en desarrollo y se reporta en prueba:

```bash
python -m evaluacion.calibrar desarrollo.csv --far-objetivo 0.01
python -m evaluacion.calibrar prueba.csv --umbral 0.63
```

Los valores resultantes se registran como decisión en `docs/07_DECISIONES_TECNICAS_NAYRA.md` y se configuran en el backend (`NAYRA_UMBRAL_*`).

## Privacidad

El audio se procesa en memoria; no se escribe en disco ni en logs. Los logs solo contienen `request_id`.
