# 04_API.md — Contratos de API de Nayra

**Versión:** 0.1 (2026-09-26)
**Estado:** contrato inicial derivado de las decisiones D-034 a D-051 (`07_DECISIONES_TECNICAS_NAYRA.md`).

| Sección | Estado |
|---|---|
| §1 Convenciones | APROBADA para implementación |
| §2 API pública de autenticación, dispositivos y sesiones | **BORRADOR** — la mecánica (desafío, nonce, firma, PIN, voz, token) está aprobada; el identificador de inicio de sesión y los pasos de registro dependen de AG-01 (D-051, P-A02) |
| §3 API interna Java → Python | APROBADA para implementación (D-040) |
| §4 Códigos de error | APROBADA para implementación |

Reglas: cada endpoint se relaciona con HUs; no se crean endpoints fuera de este documento; ningún endpoint devuelve contraseñas, hashes, PIN, tokens almacenados ni datos biométricos.

---

## 1. Convenciones

- Prefijo público: `/api/v1`. Prefijo interno (solo red interna): `/interno/v1`.
- JSON en UTF-8, nombres de campos en `camelCase` para la API pública y `snake_case` para la API interna Python.
- Autenticación pública: cabecera `Authorization: Bearer <token_opaco>` (D-042). El token es opaco: el cliente no debe interpretarlo.
- Errores: cuerpo uniforme

```json
{ "codigo": "SESION_EXPIRADA", "mensaje": "Tu sesión se cerró por inactividad. Vuelve a iniciar sesión." }
```

  `mensaje` es comprensible y apto para ser leído por voz (HU-47, HU-64). Nunca incluye detalles internos.
- Fechas en ISO-8601 UTC.

---

## 2. API pública (aplicación móvil → Spring Boot)

### 2.1 Solicitar desafío de autenticación — HU-40, HU-43

`POST /api/v1/auth/desafios` — sin sesión.

Request:
```json
{ "identificador": "<pendiente AG-01>", "dispositivoId": "b3f1..." }
```

Response `201`:
```json
{
  "desafioId": "5a0c...",
  "elementos": ["sol", "cuatro", "siete", "dos", "mesa"],
  "texto": "sol, cuatro, siete, dos, mesa",
  "nonce": "base64url(32 bytes)",
  "expiraEn": "2026-09-26T18:00:00Z"
}
```

- Formato del desafío: palabra + 3 dígitos + palabra, nunca 6 dígitos seguidos (D-037).
- Un solo uso; vida útil configurable (D-048).
- Por privacidad, si el identificador o el dispositivo no son válidos la respuesta es igualmente `201` con un desafío que luego fallará; el rechazo se produce al intentar autenticar (no permite enumerar usuarios).

### 2.2 Autenticar y crear sesión — HU-12, HU-40 a HU-45, HU-48 a HU-50

`POST /api/v1/auth/sesiones` — `multipart/form-data`, sin sesión.

| Parte | Tipo | Descripción |
|---|---|---|
| `desafioId` | texto | Desafío de §2.1 |
| `firma` | texto | Firma ECDSA P-256 (DER, base64url) del mensaje canónico §2.4 con propósito `LOGIN` |
| `pin` | texto | 6 dígitos (D-043); viaja solo por TLS |
| `audio` | archivo | WAV PCM 16 kHz mono 16 bits (D-045) |

Orden de validación (D-046): desafío vigente → firma del dispositivo activo → PIN → voz (Python §3.2) → umbrales en Java (D-038).

Response `201`:
```json
{ "token": "base64url(32 bytes)", "inactividadMaximaSegundos": 300 }
```

Response `401`/`423`/`503`: cuerpo de error §1 con los códigos de §4.

### 2.3 Registrar o reemplazar el dispositivo — HU-14 (dispositivos), D-041

`POST /api/v1/dispositivos` — el contexto de autorización (registro inicial o cambio de dispositivo tras verificar identidad) depende de AG-01.

Request:
```json
{
  "clavePublica": "base64(SubjectPublicKeyInfo DER, EC P-256)",
  "nonce": "base64url",
  "firma": "base64url(DER)",
  "plataforma": "ANDROID",
  "nombre": "Pixel 7"
}
```

- La firma prueba la posesión de la clave privada (propósito `REGISTRO_DISPOSITIVO`).
- Un dispositivo activo por usuario: registrar uno nuevo revoca el anterior y cierra sus sesiones.

Response `201`: `{ "dispositivoId": "b3f1..." }`

### 2.4 Mensaje canónico firmado

```text
NAYRA|v1|<PROPOSITO>|<nonce>|<dispositivoId o "-">
```

`PROPOSITO` ∈ {`REGISTRO_DISPOSITIVO`, `LOGIN`, `REAUTENTICACION`}. Codificación UTF-8. Algoritmo `SHA256withECDSA`.

### 2.5 Sesiones — HU-13, HU-14

| Método y ruta | Descripción | HU |
|---|---|---|
| `DELETE /api/v1/auth/sesiones/actual` | Cierra la sesión actual (revocación inmediata) | HU-13 |
| `GET /api/v1/sesiones` | Lista las sesiones activas del usuario (dispositivo, inicio, último acceso) | HU-14 |
| `DELETE /api/v1/sesiones/{id}` | Cierra una sesión propia | HU-14 |

Toda solicitud autenticada renueva la ventana de inactividad de 5 minutos (D-042). Una sesión inactiva responde `401 SESION_EXPIRADA`.

### 2.6 Pendientes de AG-01

Registro de usuario (EP-01), vinculación de cuenta financiera por DNI (D-028), creación del PIN, enrolamiento biométrico público (EP-02) y reautenticación para operaciones (HU-46). Se agregarán aquí cuando AG-01 esté en la rama `yuniv`.

---

## 3. API interna (Spring Boot → servicio Python) — D-040

- Solo red interna. Cabecera obligatoria `X-Nayra-Service-Token: <secreto>` (D-017); comparación en tiempo constante.
- Python devuelve **puntajes y estados técnicos**; **no** aplica umbrales de decisión ni reglas de negocio (Java lo hace, D-038).
- El audio nunca se escribe en disco ni en logs.
- Timeouts configurables en Java; sin reintentos automáticos en verificación.

### 3.1 Salud

`GET /interno/v1/salud` → `200 { "estado": "OK", "modelos": { "biometria": "...", "antispoofing": "...", "contenido": "..." } }`

### 3.2 Verificación — HU-40 a HU-45, HU-48

`POST /interno/v1/verificaciones` — `multipart/form-data`

| Parte | Descripción |
|---|---|
| `usuario_id` | Identificador del usuario |
| `texto_esperado` | Elementos del desafío separados por espacio (`sol cuatro siete dos mesa`) |
| `audio` | WAV PCM 16 kHz mono |

Response `200`:
```json
{
  "request_id": "uuid",
  "calidad":    { "duracion_s": 4.1, "voz_neta_s": 3.4, "snr_db": 21.0, "saturacion": 0.0 },
  "contenido":  { "transcripcion": "sol cuatro siete dos mesa", "coincide": true, "confianza": 0.93 },
  "spoofing":   { "puntaje": 1.7 },
  "biometria":  { "similitud": 0.71, "perfil_encontrado": true },
  "modelos":    { "biometria": "speechbrain/spkrec-ecapa-voxceleb@<rev>", "antispoofing": "aasist@<rev>", "contenido": "vosk-model-small-es-0.42" }
}
```

- `spoofing.puntaje`: mayor = más parecido a voz genuina (convención de AASIST).
- Si no existe perfil biométrico: `biometria.perfil_encontrado = false`, `similitud = null`.

### 3.3 Análisis de una muestra — HU-29 a HU-32, HU-48

`POST /interno/v1/analisis` — `multipart/form-data` con `texto_esperado` y `audio`.

Devuelve `calidad`, `contenido` y `spoofing` con el mismo formato de §3.2, sin comparación biométrica. Lo usa Java durante el enrolamiento para decidir, con sus umbrales, qué muestras son válidas.

### 3.4 Enrolamiento — HU-26, HU-33, HU-34, HU-35

`POST /interno/v1/perfiles/{usuario_id}?modo=evaluar|guardar` — `multipart/form-data` con `audio_1`, `audio_2`, `audio_3` (las 3 muestras que Java ya consideró válidas con §3.3).

- `modo=evaluar`: calcula los embeddings y el centroide **sin guardar nada** y devuelve la similitud de cada muestra con el centroide, para que Java aplique su criterio de consistencia (D-038).
- `modo=guardar`: calcula el centroide, lo cifra (AES-256-GCM) y lo guarda, reemplazando el perfil anterior si existía (re-enrolamiento, D-039).

El servicio no guarda estado entre llamadas (compatible con 2 instancias, D-023); Java reenvía los audios que tiene en memoria durante la misma solicitud del usuario.

Response `200`:
```json
{ "modelo": "speechbrain/spkrec-ecapa-voxceleb", "modelo_version": "<rev>", "num_muestras": 3,
  "similitud_al_centroide": [0.91, 0.88, 0.90], "guardado": true }
```

### 3.5 Eliminación — HU-36

`DELETE /interno/v1/perfiles/{usuario_id}` → `204`. Borrado físico del perfil.

---

## 4. Códigos de error

| Código | HTTP | Significado | Mensaje al usuario (resumen) |
|---|---|---|---|
| `DESAFIO_INVALIDO` | 401 | Desafío inexistente, usado o expirado | Pide un nuevo desafío |
| `AUTENTICACION_FALLIDA` | 401 | Firma, PIN o voz no aprobados (sin detallar cuál, D-046) | No se pudo verificar tu identidad; quedan N intentos |
| `CALIDAD_INSUFICIENTE` | 422 | Audio con poca voz, ruido o saturación (HU-31, HU-32) | Repite en un lugar más silencioso |
| `CONTENIDO_INCORRECTO` | 422 | No se reconoció el desafío completo | Repite la frase indicada |
| `CALIBRACION` | 503 | Umbral sin valor aprobado (D-038) | La autenticación por voz aún no está habilitada |
| `CUENTA_BLOQUEADA` | 423 | Límite de intentos alcanzado (D-047) | Contacta a atención para desbloquear |
| `SESION_EXPIRADA` | 401 | Inactividad de 5 minutos o sesión revocada | Vuelve a iniciar sesión |
| `SERVICIO_VOZ_NO_DISPONIBLE` | 503 | Timeout o error del servicio Python; no cuenta como intento fallido | Inténtalo de nuevo en unos momentos |

`CALIDAD_INSUFICIENTE` y `CONTENIDO_INCORRECTO` se informan de forma explícita porque ayudan al usuario a corregir la captura y no revelan si la voz coincide.
