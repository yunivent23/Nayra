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

## Evaluación y calibración de umbrales (D-055, D-060)

No hace falta entrenar ECAPA-TDNN ni AASIST: se usan preentrenados (D-011, D-012). Lo que falta para cerrar
D-055 es **medir** con voces reales qué tan bien separan a la persona correcta de otra y elegir el umbral con
datos. Para eso está `nayra_voz/evaluacion/` (no lo usa el servicio):

```bash
# Corpus público de español peruano (OpenSLR 73, CC BY-SA 4.0), solo como referencia de desarrollo
python -m nayra_voz.evaluacion.evaluar --openslr datos/es_pe_female datos/es_pe_male --salida resultados/ \
    --duracion-s 3 --antispoofing 300
# Grabaciones de voluntarios de Nayra (D-060): CSV con columnas ruta,hablante[,genero]
python -m nayra_voz.evaluacion.evaluar --manifiesto voluntarios.csv --salida resultados/ --sin-cache
```

Reparte a las personas en desarrollo y prueba (disjuntos), reproduce el enrolamiento de Nayra (centroide de
3 muestras), calcula EER, FAR, FRR (con IC 95 %), minDCF y el umbral para una FAR objetivo, y entrena un
calibrador logístico de dos parámetros (`calibrador.json`) que traduce la similitud coseno a probabilidad de
«misma persona». Con `--antispoofing N` mide cuántas grabaciones reales rechazaría AASIST con el umbral
vigente; no mide ataques. Nunca modifica `config/parametros_provisionales.yaml`. Resultados de la primera
corrida: `docs/evaluacion/EVALUACION_VERIFICACION_VOZ_2026-11-11.md`.
