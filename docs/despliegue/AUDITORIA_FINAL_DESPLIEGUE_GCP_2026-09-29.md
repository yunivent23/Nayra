# Auditoría final y preparación del despliegue de NAYRA en GCP

> **Documento histórico de referencia (nota añadida al versionarlo, 2026-09-29).**
> - Esta auditoría refleja el estado del proyecto **en el momento en que se redactó**.
> - Tanto su línea «Estado» como la sección M («Decisiones que necesito que confirmes») muestran las decisiones como **propuestas pendientes en aquel momento**. **No** describen el estado actual.
> - Después, las decisiones se aprobaron y se formalizaron en `docs/07_DECISIONES_TECNICAS_NAYRA.md` como **D-062 a D-074**. Ese documento es la **fuente de verdad actual**.
> - Esta nota no cambia el contenido técnico ni los hallazgos de la auditoría.
> - Las rutas `/mnt/project-files/...` corresponden a la carpeta de trabajo compartida y no forman parte del repositorio.

**Fecha:** 2026-09-29 (trabajo iniciado el 2026-09-28)
**Base:** rama `yuniv`, commit `672bf2c`, árbol limpio (verificado con `git status --short --ignored` al empezar y al terminar).
**Reemplaza:** `/mnt/project-files/analisis/PLAN_DESPLIEGUE_GCP_2026-09-28.md` en lo que difieran.
**Estado en la fecha de la auditoría:** PROPUESTA — PENDIENTE DE APROBACIÓN DE YUNI. (Histórico: ya está aprobada como D-062 a D-074; ver la nota inicial.)

### Qué se hizo y qué no

- **No** se creó ningún recurso en GCP. **No** se escribieron Dockerfiles, workflows ni archivos en el repositorio. **No** se cambió código, **no** hubo commit ni push y **no** se ejecutaron migraciones en GCP.
- **Sí** se hicieron verificaciones **locales y desechables** en el contenedor de trabajo (4 vCPU, 15 GB de RAM, Ubuntu 24.04). Todo quedó fuera del repositorio y se borró al terminar:
  - `mvn -Djava.version=21 -DskipTests package` con JDK 21.0.10;
  - un PostgreSQL 16.13 temporal con roles separados;
  - migraciones de `nayra` (por Flyway, al arrancar Nayra-Back) y de `biometria` (aplicadas una sola vez con `psql`);
  - Nayra-Voz con los **modelos reales** descargados;
  - Nayra-Back conectado a ambos.
- Estas mediciones son de **este entorno**, no de una VM de GCP. Donde un dato depende de GCP, está marcado como pendiente.

Convenciones:

- **[Verificado-doc]**: documentación oficial de Google, Spring u otro proveedor, con enlace.
- **[Verificado-local]**: ejecutado aquí.
- **[Repo]**: leído en el código, con `archivo:línea`.
- **[Inferido]**: razonado, no comprobado.
- **[Pendiente]**: falta confirmar.

---

## A. Estado actual

### Implementado y comprobado en esta auditoría

| Pieza | Evidencia |
|---|---|
| Nayra-Back compila y arranca con **Java 21** | [Verificado-local] `package` OK; arranque en 9,5 s con perfil `prototipo` |
| Flyway aplica **V001–V012** sobre **PostgreSQL 16** | [Verificado-local] log: «Successfully applied 12 migrations to schema "nayra", now at version v012»; datos ficticios cargados (4 cuentas) |
| Separación de permisos con roles creados antes de migrar | [Verificado-local] el usuario de ejecución recibe `permission denied for table auditoria` al intentar `UPDATE` |
| Migraciones `biometria` V001–V003 con usuario propio; el usuario del servicio biométrico puede leer `perfiles_voz` | [Verificado-local] |
| Nayra-Voz arranca con **ECAPA-TDNN, AASIST y Vosk reales** y responde `listo: true` | [Verificado-local] `GET /prototipo/v1/salud` → `{"listo":true,...}` en ~4 s |
| El pipeline real procesa audio de extremo a extremo por HTTP | [Verificado-local] muestra de enrolamiento: CALIDAD aprobada (WebRTC VAD real) → CONTENIDO rechazado con Vosk real (se usó un audio en inglés, así que el rechazo es el resultado correcto) |
| Pruebas Python con los modelos presentes | [Verificado-local] 50 correctas, 7 omitidas (necesitan `NAYRA_VOZ_BD` o los pesos en la ruta del repo) |
| URL del backend configurable en Flutter con `--dart-define=NAYRA_BACKEND_URL` | [Repo] `Nayra-App/lib/config.dart:9-12` |

### Falta

| Pieza | Estado |
|---|---|
| **Validación biométrica real con voz en español** (enrolar 3 muestras del desafío y verificar) | [Pendiente] Hacen falta grabaciones reales; aquí solo se comprobó que los modelos cargan y procesan audio |
| Estado compartido entre instancias (R-1) | No implementado: bloquea la Fase B (§D-1) |
| Endpoint de salud HTTP de Nayra-Back | No existe (§D-6) |
| Dockerfiles, Compose, `.dockerignore`, `.env.example` | No existen |
| `requirements.txt` con versiones fijadas | Sin versiones [Repo] |
| TLS, dominio, secretos en GCP | Por hacer |
| Saldo, movimientos, transferencias, notificaciones | Sin endpoint (no bloquea el despliegue) |
| Documentación de la arquitectura de despliegue (texto de las 2 instancias, D-XXX de topología) | Por redactar tras tu aprobación |

---

## B. Decisiones confirmadas

Las que diste en este mensaje, más lo que ya estaba aprobado en `07`:

1. Arquitectura objetivo: Internet → API Gateway → Load Balancer → Firewall → VPC → DMZ / red de aplicación → **2 instancias en zonas diferentes** → PostgreSQL (D-023 aprobada; el resto, definido por ti como objetivo).
2. **Nayra-Back y Nayra-Voz en la misma VM**, Voz solo interno, comunicación Java → Python por red interna (coherente con D-010).
3. **PostgreSQL en Cloud SQL con IP privada**, base nueva, **sin migrar datos locales**.
4. Separación lógica Negocio / Autenticación / Biométrico **sin** tres grupos de servidores (D-023, modelo v4).
5. **Estrategia de costos**: 1 instancia activa en desarrollo; 2 activas solo en pruebas de disponibilidad. Es configuración temporal, no cambia la arquitectura, y **no** se afirma alta disponibilidad con una sola instancia activa.
6. Solución preferida para R-1: **PostgreSQL como almacén compartido**, sin afinidad de sesión.
7. P-4 (propuesta tuya): **Flyway como tarea puntual** desde un entorno controlado.
8. Preferencias técnicas, **confirmadas compatibles** por esta auditoría: **Java 21** (§D-8) y **PostgreSQL 16** (§D-9).
9. Flutter: `NAYRA_BACKEND_URL` por `--dart-define`; local `http://10.0.2.2:8080`; GCP con la URL HTTPS del API Gateway; sin URL de producción en el código.
10. Docker para Back y Voz; PostgreSQL en Docker **solo** en local.

---

## C. Decisiones pendientes

Solo las que realmente requieren tu confirmación (la lista para responder está en M):

| # | Decisión | Por qué no puedo decidirla yo |
|---|---|---|
| C-1 | **Región** | Santiago no admite API Gateway (§D-2). Hay que elegir entre alternativas con distinto costo y latencia |
| C-2 | **Registrar formalmente la topología** en `07` (D-062 o siguiente) y el texto de las 2 instancias en `02` y `08` | `CLAUDE.md` §4.2 y `08` §7.2 prohíben asumir API Gateway, balanceador, VPC y DMZ sin una decisión documentada |
| C-3 | **R-1: tablas de estado temporal en PostgreSQL** | Contradice `CLAUDE.md` §8 y el modelo v4, que dejan explícitamente fuera las «tablas de desafíos o nonces». Hay que modificar esa decisión |
| C-4 | **Enrolamiento pendiente de Nayra-Voz** | Guardar embeddings temporales roza D-013 («sin historial de embeddings»); hay 3 opciones (§D-1) |
| C-5 | **Dominio / subdominio** para el certificado del balanceador | Implica comprar o usar un dominio (§H-4) |
| C-6 | **Endpoint de salud** en Nayra-Back (incluye comprobar Nayra-Voz) | Es código nuevo; sin él, la prueba "Nayra-Voz sigue disponible" no se puede automatizar (§D-6) |
| C-7 | **Bloqueo del acceso directo al balanceador** (sin pasar por API Gateway) | Opciones con costo o con código (§D-4) |
| C-8 | **Pila de versiones de Python** a fijar | Propongo la que funcionó aquí (§G) |
| C-9 | **Secretos que rotar** (contraseña de PostgreSQL local, clave de Gemini del historial) | D-017 difiere la rotación; tú decides si se adelanta (§H-1) |

---

## D. Hallazgos técnicos

### D-1. Estado en memoria entre instancias (R-1) — bloqueador de la Fase B

**Qué está en memoria hoy** [Repo]:

| Estado | Dónde | Contenido | Vida | Un solo uso |
|---|---|---|---|---|
| Nonces del dispositivo | `Nayra-Back/.../serviceimplements/DispositivoServiceImplement.java:40,47` | nonce → (dispositivoId, expira) | 60 s (`application.properties`, provisional) | Sí |
| Transacciones de inicio de sesión | `.../AutenticacionVozServiceImplement.java:50-77` | id, cuentaId, dispositivoId, expira, paso (máquina de estados), desafioId | 5 min (`application-prototipo.properties`) | — |
| Desafíos de voz | `.../DesafioServiceImplement.java:41`; `IDesafioService.java:11` | id, texto, cuentaId, contexto, expira | 120 s | Sí |
| Registros asistidos en curso | `.../RegistroServiceImplement.java:64`; `entities/RegistroEnCurso.java:14-31` | código, usuario previsto, documento, nombres, apellidos, rol, representante, expira, paso, celular, **hash del PIN**, **clave pública** | 900 s | — |
| Enrolamientos pendientes (Voz) | `Nayra-Voz/nayra_voz/pipeline.py:26-29,52` | **embeddings** de cada muestra válida y contador | 900 s | — |
| Clave JWT si no se define `NAYRA_JWT_CLAVE` | `securities/TokenSesionJwt.java:45-51`; `application.properties:33-36` | clave aleatoria por proceso | vida del proceso | — |

**Ya está en PostgreSQL y es compartido**: sesiones (`nayra.sesiones`, `jti`), contador de intentos del PIN (`credenciales`, incremento atómico E-01), dispositivos, usuarios, auditoría y perfiles de voz (`biometria.perfiles_voz`). Los repositorios `repositories/memoria/*` solo se usan en pruebas unitarias.

**Qué pasa si la petición N entra en A y la N+1 en B** [Inferido de la lectura del código]:

- **Inicio de sesión** (5 peticiones: nonce → transacción → PIN → [PIN dictado] → voz):
  - si el nonce sale de A y la firma llega a B, B no conoce el nonce y rechaza;
  - si la transacción está en A y el PIN llega a B, B responde "transacción no encontrada";
  - si el desafío sale de A y la voz llega a B, el desafío no existe.

  El PIN incorrecto no se cuenta de más (el contador está en BD), pero el usuario **no puede completar** el inicio de sesión.
- **Registro asistido**: el ADMIN crea el código en A. El celular de la persona consulta `/api/v1/registros/{codigo}` y cae en B, que responde que no existe.
- **Enrolamiento**: la muestra 1 va a Voz-A y la muestra 2 a Voz-B, así que Voz-B no tiene la muestra 1 y al finalizar faltan muestras.
- **Sesión ya abierta**: funciona en cualquier instancia **si** `NAYRA_JWT_CLAVE` es la misma en ambas (la sesión se valida contra la BD).
- **Verificación de voz** (inicio de sesión): Voz no guarda estado propio; lee el perfil de PostgreSQL. Funciona en cualquier instancia.

**Propuesta mínima** (sin implementar):

*Nayra-Back → PostgreSQL* (migración **V013**, requiere C-3):

- 4 tablas temporales en `nayra`, propiedad del servicio de Autenticación (y de Negocio para el registro):
  - `nonces_dispositivo`
  - `desafios`
  - `transacciones_autenticacion`
  - `registros_en_curso`

  Cada una con `expira` e índice por expiración.
- Consumo de un solo uso **atómico** con `DELETE … RETURNING` o `UPDATE … WHERE usado IS NULL`, el mismo patrón que E-01/E-02.
- Limpieza de vencidos al consultar, más una tarea programada simple.
- `GRANT` por servicio al estilo de V011.
- En Java: extraer los 4 `ConcurrentHashMap` a puertos de repositorio con adaptador PostgreSQL y adaptador en memoria para pruebas, igual que el resto del proyecto. **Sin** cambios en la API ni en Flutter.

*Nayra-Voz, enrolamiento pendiente* (requiere C-4). Opciones:

- **(a)** Tabla temporal `biometria.enrolamientos_pendientes` con los embeddings **cifrados** con la misma clave AES-GCM, vida de 900 s y borrado al finalizar o al vencer (V004 de `biometria`, cambios en `pipeline.py` y `almacen.py`). Es la **recomendada**: es la única que respeta "PostgreSQL como almacén compartido". Requiere aclarar que no es "historial de embeddings" (D-013).
- **(b)** Que Nayra-Back guarde a qué VM pertenece cada enrolamiento y siempre llame a esa Voz. Obliga a abrir el 8001 entre VMs y rompe el aislamiento por VM.
- **(c)** Aceptar que el enrolamiento falle si cambia de instancia a mitad del proceso (solo en Fase B) y reintentarlo.

**¿Requiere SQL?** Sí: V013 (`nayra`) y, con la opción (a), V004 (`biometria`).
**¿Requiere Java/Python?** Java sí (4 servicios). Python solo con la opción (a).
**Lo que ni siquiera R-1 resuelve**: una petición **en curso** en la instancia que cae se pierde; el usuario repite ese paso o reinicia el flujo. Es inevitable en esta arquitectura y hay que documentarlo en las pruebas.

### D-2. Región: Santiago no admite API Gateway

- [Verificado-doc] API Gateway solo puede desplegar gateways en: `asia-northeast1`, `australia-southeast1`, `europe-west1`, `europe-west2`, `us-east1`, `us-east4`, `us-central1`, `us-west2`, `us-west3`, `us-west4`. **Ninguna región de Sudamérica** ([modelo de despliegue de API Gateway](https://docs.cloud.google.com/api-gateway/docs/deployment-model)).
- [Verificado-doc] Cloud SQL sí existe en `southamerica-west1` (Santiago) y en `us-east1` ([ubicaciones de Cloud SQL](https://docs.cloud.google.com/sql/docs/postgres/locations)).
- Compute Engine, VPC, balanceador regional, Secret Manager y Artifact Registry están disponibles en ambas regiones [Inferido; confirmar en consola al crear el proyecto]. Zonas: `us-east1` tiene 3 zonas (`b`, `c`, `d`) [Inferido; confirmar con `gcloud compute zones list`].
- **Opción 1 (recomendada): todo en `us-east1` (Carolina del Sur).** Todos los componentes en una región. Es la región más cercana a Perú entre las que admiten API Gateway [Inferido: la costa este de EE. UU. está más cerca de Lima que las demás de la lista; latencia sin medir]. Está en el nivel gratuito de Compute Engine y de Cloud Storage [Verificado-doc] y suele ser más barata que Sudamérica [Pendiente de precio oficial].
- **Opción 2: API Gateway en `us-east1` y el resto en Santiago.** Es técnicamente posible, porque API Gateway llama a una URL pública y no necesita estar en la misma región. Pero cada petición haría Lima → Carolina del Sur → Santiago → Carolina del Sur → Lima [Inferido]: más latencia que la opción 1 y dos regiones que operar. No la recomiendo.
- `us-east4` (Virginia) es equivalente a `us-east1`. Alternativa si falta capacidad en `us-east1`.

### D-3. API Gateway con balanceador regional: viable, con condiciones

- [Verificado-doc] El `address` de `x-google-backend` acepta `http` o `https`. El gateway **no entra en la VPC**: su backend debe ser **la dirección pública del balanceador** ([extensiones OpenAPI de API Gateway](https://docs.cloud.google.com/api-gateway/docs/oasv2-extensions)).
- [Verificado-doc] **Cabecera `Authorization`**: si la autenticación hacia el backend está activa, API Gateway copia el `Authorization` original a `X-Forwarded-Authorization` y lo reemplaza por un token de Google (misma página). Como el JWT de NAYRA viaja en `Authorization: Bearer` (`FiltroSesion`), **hay que poner `disable_auth: true`** en cada ruta. Si no, todas las rutas con sesión devuelven 401. No hace falta cambiar código.
- [Verificado-doc] **Timeout**: el `deadline` por defecto es **15 s**, configurable (máximo 600 s en gateways sin streaming).
- **No hace falta autenticación de cliente** en el gateway (API keys): la app no guarda secretos (`config.dart`) y la seguridad la dan la firma del dispositivo, el PIN, la voz y la sesión.
- Consecuencia: **dos componentes reciben tráfico público**. API Gateway es el que usa la app. El balanceador también tiene IP pública, porque el gateway no llega a IPs privadas. Ver D-4.

### D-4. El balanceador también es alcanzable desde Internet

Quien conozca el dominio del balanceador puede saltarse API Gateway. Mitigaciones, de menor a mayor esfuerzo:

1. **Mapa de URLs del balanceador como lista blanca** (sin costo adicional ni código): publicar solo las rutas que usa la app (§H-7) y mandar el resto a una redirección o respuesta por defecto. Así el balanceador no expone más que el gateway: nada de `/prototipo/arranque/**`, Swagger ni `/v3/api-docs`. [Inferido: los mapas de URLs regionales admiten reglas por ruta y redirección por defecto; confirmar al configurar.]
2. **Cloud Armor** (política de seguridad regional) para filtrar además por país o reglas L7. [Pendiente de precio.]
3. **Que Nayra-Back exija la identidad del gateway** (activar la autenticación hacia el backend y leer la sesión de `X-Forwarded-Authorization`). Es cambio de código.

Recomiendo (1) desde la Fase A y decidir (2) o (3) antes de un uso fuera de pruebas (C-7).

### D-5. Timeouts de operaciones biométricas: 60 s confirmados como suficientes

- Medido aquí [Verificado-local, 4 vCPU, audio de 3,3 s]: ECAPA 0,33 s, AASIST 0,33 s, Vosk con gramática 0,10 s. Una petición HTTP de enrolamiento tardó 0,14 s (quedó rechazada en el contenido, así que no llegó a AASIST ni a ECAPA). **La inferencia completa ronda ~1 s en este entorno**. En una VM de 2 vCPU será algo más lenta [Pendiente de medir en GCP].
- Cadena actual:
  - app: 20 s por petición y 45 s con audio [Repo `config.dart`];
  - Back → Voz: conexión 2 s, lectura 30 s [Repo `application-prototipo.properties`];
  - balanceador: **30 s por defecto** en el backend service [Verificado-doc: [descripción de backend services](https://docs.cloud.google.com/load-balancing/docs/backend-service), «the default value for the backend service timeout is 30 seconds»];
  - API Gateway: **15 s por defecto** [Verificado-doc].
- **Propuesta**:
  - `deadline: 60` en las rutas con audio del gateway (`/voz`, `/pin-dictado`, `/muestras`) y 20 s en el resto;
  - `timeoutSec: 60` en el backend service;
  - se mantienen los 30 s de Back → Voz y los 45 s de la app.

  Así nunca corta antes el gateway o el balanceador que el backend. 60 s es holgado frente a lo medido.

### D-6. Health check y Nayra-Voz

- [Verificado-local] Todo lo no listado devuelve 401 (`GET /` → 401). `GET /v3/api-docs` y Swagger devuelven 200 porque están permitidos (`WebSecurityConfig`).
- Sin cambiar código, el health check del balanceador puede ser:
  - **TCP 8080** (recomendado para Fase A);
  - **HTTP a `/v3/api-docs`** (devuelve 200, pero genera el documento OpenAPI en cada sonda).
- Ninguno detecta que **solo** Nayra-Voz se cayó dentro de una VM sana: el balanceador seguiría enviando tráfico y fallarían las operaciones de voz (503, que no cuenta como intento, D-010). Para la Fase B conviene un endpoint de salud que compruebe Back, la BD y Voz (C-6, cambio de código pequeño).
- MIG: usar **autocuración** (autohealing) con su propio health check. Durante las pruebas de caída hay que tenerlo en cuenta, porque recreará la VM.

### D-7. DMZ como subred de solo proxy: viable

[Verificado-doc] El balanceador de aplicaciones externo **regional** necesita una **subred de solo proxy** (`purpose=REGIONAL_MANAGED_PROXY`, una por región y red) donde Google ejecuta los proxies Envoy. Firewall requerido:

- ingreso desde `35.191.0.0/16` y `130.211.0.0/22` (health checks);
- ingreso desde el rango de la subred de solo proxy hacia el puerto de los backends.

Los backends usan IPs internas ([configuración del balanceador regional externo](https://docs.cloud.google.com/load-balancing/docs/https/setting-up-reg-ext-https-lb)).

Representación: **DMZ = subred de solo proxy**, una zona de red sin VMs y única origen permitido hacia las instancias. Es la traducción más fiel que ofrece GCP; no existe un recurso llamado "DMZ".

### D-8. Java

- [Repo] `pom.xml:30` declara `java.version` **25**. JDK usado en todas las pruebas: **21** (aquí, 21.0.10).
- [Verificado-doc] Spring Boot **4.1.1** exige **Java 17 como mínimo y es compatible hasta Java 26** ([requisitos de Spring Boot](https://docs.spring.io/spring-boot/system-requirements.html)).
- [Verificado-local] Dependencias resueltas: Spring Framework 7.0.9, Flyway 12.4.0, driver PostgreSQL 42.7.13, springdoc 2.8.13. Compila y arranca con 21.
- Riesgo menor: springdoc **2.x** es la línea de Spring Boot 3 [Inferido]. Aun así, `/v3/api-docs` y Swagger respondieron 200 con Boot 4.1.1 [Verificado-local].
- Docker y GCP no imponen restricción: la imagen lleva su propio JRE.
- **Conclusión: Java 21 es compatible.** Hace falta que el `pom.xml` diga 21, o pasar `-Djava.version=21` en el Dockerfile. Recomiendo cambiar el `pom.xml` (una línea), para que el build local y el de Docker coincidan.

### D-9. PostgreSQL 16

- [Verificado-doc] Cloud SQL admite PostgreSQL 9.6 a 18. La 18 es la predeterminada. **La 16 tiene soporte estándar hasta el 1-feb-2029** y extendido hasta el 1-feb-2032 ([versiones de Cloud SQL](https://docs.cloud.google.com/sql/docs/postgres/db-versions)).
- [Verificado-local] Flyway 12.4.0 + PostgreSQL 16.13 + V001–V012 + `ddl-auto=validate` funcionan. El único WARN relevante es informativo («schema "nayra" already exists, skipping»), porque el esquema se creó antes al conceder permisos.
- Migraciones sin extensiones ni `SUPERUSER`: solo `CREATE SCHEMA`, tablas, `GRANT` y bloques `DO` [Repo]. **Compatibles con Cloud SQL**, cuyo usuario administrador no es superusuario real [Inferido de las sentencias; confirmar en la primera ejecución].
- **Conclusión: PostgreSQL 16 es adecuado.**

### D-10. Consumo de CPU y RAM

[Verificado-local; este entorno, no GCP]:

| Proceso | RAM residente | Notas |
|---|---:|---|
| Nayra-Voz (Uvicorn, 1 worker) con ECAPA + AASIST + Vosk cargados | **~535 MB** | Pico de ~765 MB en el script de medición |
| — de ello: import de torch/speechbrain | ~296 MB | |
| — ECAPA-TDNN | +~107 MB | 85 MB en disco |
| — AASIST | ~0 MB | 1,3 MB en disco |
| — Vosk small es 0.42 | +~101 MB | 58 MB descomprimido |
| Nayra-Back (JVM, sin límite de heap, en reposo) | **~410 MB** | Crecerá con carga; fijar `MaxRAMPercentage` |
| Arranque | Voz ~4 s; Back ~9,5 s | |
| GPU | **No necesaria** | torch CPU; ninguna dependencia CUDA instalada |

- **Lectura**: ambos servicios juntos usan ~1 GB en reposo. Una **`e2-standard-2` (2 vCPU, 8 GB)** tiene margen amplio para el primer despliegue [Inferido].
- La estimación previa de `07` D-016 (2–4 vCPU y 4 GB solo para Voz) queda por encima de lo medido.
- **Pendiente**: medir en la VM de GCP, con carga concurrente y con voz real en español.

### D-11. Modelos biométricos

| Modelo | Fuente (script del repo) | Tamaño medido | Licencia | Autenticación |
|---|---|---:|---|---|
| ECAPA-TDNN `speechbrain/spkrec-ecapa-voxceleb` | HuggingFace | 85 MB | **Apache-2.0**; no restringido (`gated: false`) [Verificado: API de HuggingFace] | **No requiere** |
| AASIST (pesos ASVspoof 2019 LA) | GitHub `clovaai/aasist` | 1,3 MB | Código **MIT** [Repo `terceros/aasist/LICENSE`]; los pesos vienen del mismo repositorio. Reconfirmar las condiciones del dataset ASVspoof 2019 [Pendiente] | No |
| Vosk `vosk-model-small-es-0.42` | alphacephei.com | 39 MB zip / 58 MB | **Apache 2.0** [Verificado-doc: [modelos de Vosk](https://alphacephei.com/vosk/models)] | No |

- **HuggingFace**: el bloqueo anterior era de la red de este entorno, no del modelo. Hoy la descarga funcionó sin token [Verificado-local].
- **Dónde almacenarlos**: **dentro de la imagen Docker** de Nayra-Voz, descargados en el build con el script del repo y comprobados con una suma SHA-256 fijada. Son ~145 MB en total.
  - Ventajas: imagen inmutable, sin descargas al arrancar y sin Cloud NAT.
  - Cloud Storage queda **opcional**, como copia de respaldo si una fuente externa desaparece.
- **Funcionan en CPU**: sí [Verificado-local].
- **No validado**: la **precisión**, es decir, aceptar a la persona correcta y rechazar a otra o una grabación, con voz real en español y los umbrales provisionales de D-055. Requiere grabaciones reales. Es la validación más importante antes del despliegue definitivo.
- Detalle técnico: `torchaudio.load` ya no funciona sin `torchcodec` en la versión instalada [Verificado-local]. NAYRA no lo usa, porque decodifica el WAV por su cuenta (`audio.py`), pero conviene no introducirlo.

### D-12. Otros hallazgos

- **Nayra-Back ejecuta Flyway al arrancar** [Repo `application.properties:12-14`]. Con 2 instancias, ambas lo intentan. Flyway toma un bloqueo en PostgreSQL [Inferido]. Aun así, tu preferencia (tarea puntual) se consigue **sin cambiar código** con `SPRING_FLYWAY_ENABLED=false` en las VMs. Así las VMs ni siquiera necesitan las credenciales de migración.
- **`spring.jpa.show-sql=true`** [Repo `application.properties:4`]: llena los logs. Poner `SPRING_JPA_SHOW_SQL=false` en GCP.
- **Aviso de Hibernate** sobre el dialecto explícito: cosmético [Verificado-local].
- **`/prototipo/arranque/administrador`** crea el primer ADMIN sin validación mientras no exista ninguno [Repo `ArranquePrototipoController.java`]. Por eso no debe publicarse y el ADMIN se crea desde dentro de la VPC nada más desplegar.
- **Android bloquea HTTP en claro** con los `targetSdk` actuales; el manifiesto no declara excepciones [Repo `AndroidManifest.xml`; comportamiento inferido]. El APK de GCP debe usar HTTPS.

---

## E. Arquitectura de despliegue propuesta

### Arquitectura objetivo (no cambia entre fases)

```text
Internet
   │  HTTPS
   ▼
API Gateway (us-east1)                         ← recibe el tráfico público de la app (*.gateway.dev)
   │  HTTPS (dominio propio + certificado administrado)
   ▼
Balanceador de aplicaciones externo REGIONAL   ← IP pública; mapa de URLs = lista blanca
   │  (Certificate Manager, DNS authorization)
   ▼
Firewall de VPC (deny por defecto; solo LB, health checks e IAP)
   ▼
VPC "nayra" (us-east1)
 ├── DMZ = subred de solo proxy (REGIONAL_MANAGED_PROXY): proxies Envoy, sin VMs
 └── Red de aplicación = subred privada (Private Google Access, sin IPs públicas)
       ├── Zona A → Instancia 1 (MIG regional): contenedores nayra-back :8080 + nayra-voz :8001 (interno)
       └── Zona B → Instancia 2 (MIG regional): contenedores nayra-back :8080 + nayra-voz :8001 (interno)
   │  IP privada (Private Service Access), SSL
   ▼
Cloud SQL PostgreSQL 16 (IP privada, sin IP pública): esquemas nayra y biometria
```

Texto para la documentación, **tal como lo pediste** (se añadirá a `02`/`07`/`08` tras tu aprobación):

> La arquitectura contempla dos instancias de aplicación distribuidas en diferentes zonas para proporcionar disponibilidad. Durante el desarrollo y pruebas, se podrá mantener una sola instancia activa para optimizar costos. Las dos instancias serán habilitadas simultáneamente durante las pruebas de disponibilidad y tolerancia a fallos.

### Fase A — primer despliegue (configuración temporal de desarrollo)

- Todos los recursos anteriores **creados**. El **MIG regional** se configura con distribución en las zonas A y B, pero con **tamaño objetivo 1**: una VM activa y la segunda sin crear.
- **No hay alta disponibilidad real** en esta fase, y no debe presentarse como tal.
- Es suficiente para Flutter → API Gateway → balanceador → Nayra-Back → Nayra-Voz → Cloud SQL con un celular real.
- R-1 no afecta a la Fase A, porque todo el tráfico cae en la misma instancia.

### Fase B — validación de disponibilidad

- Requisito previo: **R-1 resuelto** (§D-1) y, idealmente, el endpoint de salud (C-6).
- MIG a **tamaño 2** con una VM en cada zona. Pruebas de §J. Después se vuelve a tamaño 1.

---

## F. Estrategia de costos

### Arquitectura objetivo vs. configuración temporal

| Recurso | Arquitectura objetivo | Desarrollo normal | Pruebas de disponibilidad |
|---|---|---|---|
| Instancias (MIG regional, zonas A y B) | 2 activas | **1 activa** (tamaño 1); **0** cuando no pruebas | **2 activas** |
| Cloud SQL | Activo | Activo mientras pruebas; **detenido** entre sesiones largas (el almacenamiento se sigue cobrando) | Activo |
| Balanceador + IP + certificado | Activo | Activo (se cobra por hora aunque no haya tráfico) | Activo |
| API Gateway, Secret Manager, Artifact Registry, Logging | Activos | Activos (se cobran por uso) | Activos |
| VPC, subredes, firewall | Activos | Activos (sin costo propio) | Activos |

Cómo reducir: redimensionar el MIG (`resize` a 1 o 0) en vez de borrar recursos; detener Cloud SQL con la política de activación "NEVER". Volver a subir cuando haga falta.

### Precios

- **[Verificado-doc]** ([funciones gratuitas de Google Cloud](https://docs.cloud.google.com/free/docs/free-cloud-features)):
  - crédito de bienvenida de **USD 300 para 90 días**;
  - nivel gratuito: 1 `e2-micro` en `us-east1`/`us-central1`/`us-west1` (no sirve para NAYRA);
  - 5 GB-mes de Cloud Storage en esas regiones;
  - Artifact Registry 0,5 GB/mes;
  - Secret Manager 6 versiones activas y 10 000 accesos/mes;
  - Logging 50 GiB/proyecto/mes.
  - Cloud SQL, Load Balancing y API Gateway **no** figuran en esa tabla.
- **No pude obtener precios unitarios oficiales**: las páginas de precios de Compute Engine y API Gateway cargan las tablas dinámicamente y no se pudieron leer desde aquí. Por eso **no presento importes como definitivos**. Estos son los SKUs a consultar en la [calculadora oficial](https://cloud.google.com/products/calculator) para `us-east1`:

| SKU a cotizar | Cantidad Fase A | Cantidad Fase B (pruebas) |
|---|---|---|
| Compute Engine `e2-standard-2` (bajo demanda) | 1 VM × horas encendidas | 2 VMs × horas de prueba |
| Disco persistente balanceado | ~20–30 GB por VM (imagen de Voz ~1,6 GB [Inferido de lo medido: 1,3 GB de dependencias Python + modelos]) | ×2 |
| Cloud SQL PostgreSQL Enterprise, tier pequeño (shared-core o 1 vCPU), 10 GB SSD, copias automáticas | 1 instancia, una zona | igual (HA solo si se aprueba D-020) |
| Regla de reenvío del balanceador regional + proxies Envoy (por hora) + procesamiento de datos | 1 | 1 |
| IP externa estática regional | 1 | 1 |
| API Gateway (llamadas/mes) | pocas miles | pocas miles |
| Artifact Registry (GB-mes por encima de 0,5 GB) | ~2 GB | ~2 GB |
| Egreso a Internet | < 1 GB | < 1 GB |
| Secret Manager (versiones por encima de 6) | ~7–8 | ~7–8 |
| Dominio (fuera de GCP o Cloud Domains) | 1 | 1 |

- Orientativo **no verificado** (de memoria, solo para dimensionar el crédito; no usar como cifra oficial):
  - Fase A 24/7: ~USD 85–115/mes, dominado por la VM, Cloud SQL y el balanceador;
  - con la VM encendida solo en horas de prueba: bastante menos;
  - 2 VMs 24/7: ~USD 140–180/mes.
- Qué consume más crédito: 1) VMs; 2) Cloud SQL; 3) balanceador (fijo por hora).
- Riesgos de gasto:
  - dejar 2 VMs encendidas;
  - activar HA de Cloud SQL por error;
  - logs verbosos;
  - una imagen con CUDA;
  - el fin del periodo de prueba.
- Medida: **presupuesto con alertas** (50 %, 90 %, 100 %) desde el primer día; no tiene costo.

---

## G. Docker

Archivos que **se crearían tras tu aprobación** (ninguno existe hoy):

```text
Nayra-Back/Dockerfile
Nayra-Voz/Dockerfile
docker-compose.yml        (raíz)
.dockerignore             (raíz)
.env.example              (raíz)
```

Contexto de build = **raíz del repositorio**: ambos servicios leen `shared/desafio/vocabulario_v2.json`.

**`Nayra-Back/Dockerfile`**

1. Etapa de build: `eclipse-temurin:21-jdk` más el wrapper `./mvnw -B -DskipTests package`. El Maven Wrapper ya está en el repo. Las pruebas con BD van aparte.
2. Etapa final: `eclipse-temurin:21-jre` con usuario no root. Copia el `.jar` y `shared/desafio/vocabulario_v2.json`.
3. `ENV SPRING_PROFILES_ACTIVE=prototipo NAYRA_VOCABULARIO=/app/shared/desafio/vocabulario_v2.json JAVA_TOOL_OPTIONS=-XX:MaxRAMPercentage=60`.
4. `EXPOSE 8080`. Sin secretos.
5. Versiones de imagen **fijadas por digest** para que el build sea reproducible.

**`Nayra-Voz/Dockerfile`**

1. `python:3.11-slim` fijado por digest: es la versión con la que se probó aquí (3.11.15).
2. `pip install` desde un `requirements.txt` **con versiones fijadas**. torch y torchaudio desde el índice **CPU** de PyTorch. Conjunto que funcionó aquí [Verificado-local; `pip check` sin conflictos; propuesta C-8]:
   - fastapi 0.141.1, uvicorn 0.54.0, python-multipart 0.0.32
   - numpy 2.4.6, PyYAML 6.0.3, cryptography 50.0.1
   - psycopg[binary] 3.3.6, webrtcvad-wheels 2.0.14
   - vosk 0.3.45, speechbrain 1.1.1
   - torch 2.14.0+cpu, torchaudio 2.11.0+cpu
3. Etapa de modelos: `scripts/descargar_modelos.sh` más la verificación SHA-256. Se copian a `/app/modelos`.
4. Copia `nayra_voz/`, `config/` y el vocabulario. `ENV NAYRA_VOZ_MODELOS=/app/modelos NAYRA_VOZ_VOCABULARIO=/app/shared/desafio/vocabulario_v2.json`.
5. Usuario no root; `CMD uvicorn nayra_voz.servidor:app --host 0.0.0.0 --port 8001 --workers 1`. Un solo worker, porque cada worker cargaría los modelos y el enrolamiento vive en memoria del proceso.
6. Tamaño esperado de la imagen: ~1,6–1,8 GB [Inferido de lo medido].

**`docker-compose.yml`**

- Servicios:
  - `postgres:16` (**solo perfil `local`**, con volumen y un script `init` que crea los roles);
  - `migraciones` (imagen oficial `flyway/flyway` 12.4.0, una sola ejecución: primero `nayra` y luego `biometria`);
  - `nayra-voz` (sin `ports:`, solo en la red interna de Compose);
  - `nayra-back` (puerto 8080 publicado; `NAYRA_VOZ_URL=http://nayra-voz:8001`; `SPRING_FLYWAY_ENABLED=false`).
- En la VM de GCP se usa el mismo archivo sin el perfil `local`, apuntando a Cloud SQL, con las variables inyectadas desde Secret Manager.

**`.dockerignore`**: `.git`, `Nayra-App/`, `**/target/`, `**/.venv`, `**/__pycache__`, `Nayra-Voz/modelos/`, `.env`.

**`.env.example`**: solo los nombres de §H-3, sin valores.

**Cómo se usaría en local**: `docker compose --profile local up`. Levanta PostgreSQL, aplica las migraciones una vez, arranca Voz y luego Back. La app en el emulador usa `http://10.0.2.2:8080`.

---

## H. Seguridad

### H-1. Secretos encontrados (valores NO reproducidos)

| # | Dónde | Qué | ¿En el árbol actual? | ¿Rotar antes de desplegar? |
|---|---|---|---|---|
| 1 | `Nayra-Back/src/main/resources/application.properties:28` | Contraseña de PostgreSQL literal (usuario `postgres`). **Es la misma desde el commit `af02e8b`** (2026-09-07) | Sí | No se reutiliza en GCP (credenciales nuevas). Rotarla en tu PostgreSQL local es opcional (D-017) |
| 2 | Historial: `af02e8b` añade y `cdbccfc` borra, en `Nayra-Backend/src/main/resources/application.properties` | `jwt.secret` del JWT heredado ya retirado | No, solo historial | No afecta: el JWT actual usa `NAYRA_JWT_CLAVE`, nueva |
| 3 | Historial: mismos commits y archivo | `gemini.api.key` (clave de API de Google Gemini) | No, solo historial | **Sí, recomendable revocarla** en la consola de Google AI si sigue activa: es una credencial de un servicio real y el historial es legible por quien tenga el repo (C-9) |
| 4 | `Nayra-Back/src/test/resources/application-pruebasbd.properties:6` | Contraseña por defecto dentro de `${NAYRA_TEST_DB_PASSWORD:…}` | Sí | Solo pruebas locales; no se usa en GCP |

Revisados sin hallazgos: código Java, Python y Dart (las coincidencias eran variables `token` del código), `application-prototipo.properties` (solo referencias `${…}`), `Nayra-Voz/config`, scripts, Android (sin keystores, `key.properties` ni `google-services.json`). Historial completo: 13 commits; los únicos secretos en archivos de configuración son los de las filas 1–3.

No se borra historial ni se hace limpieza destructiva (D-017).

### H-2. Secret Manager

| Secreto | Consumidor | Notas |
|---|---|---|
| `nayra-db-app-password` | Nayra-Back (VMs) | Usuario de ejecución `nayra_app` |
| `nayra-jwt-clave` | Nayra-Back (VMs) | **Misma versión en ambas instancias** (sin ella, cada una genera su propia clave) |
| `nayra-voz-token-servicio` | Nayra-Back y Nayra-Voz (VMs) | Token Java → Python |
| `nayra-voz-clave-embeddings` (+ versión) | Nayra-Voz (VMs) | AES-256-GCM. **Si se pierde, no se pueden leer los perfiles de voz** |
| `nayra-voz-db-password` | Nayra-Voz (VMs) | Usuario `nayra_biometria` |
| `nayra-db-migracion-password` | Solo la tarea de migración | **No** accesible desde las VMs de aplicación |
| `nayra-biometria-migracion-password` | Solo la tarea de migración | Ídem |

Son 7 secretos, uno por encima del gratuito. Acceso:

- cuenta de servicio **`nayra-app`** (VMs), con `roles/secretmanager.secretAccessor` solo sobre las 5 primeras;
- cuenta de servicio o identidad de quien migra, sobre las 2 últimas.

El script de arranque de la VM lee los secretos y los pasa como variables de entorno a los contenedores. Nunca van a la imagen, a los metadatos de la plantilla ni al repositorio.

### H-3. Variables de entorno (no secretas)

- **Back**:
  - `SPRING_PROFILES_ACTIVE=prototipo`
  - `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`
  - `SPRING_FLYWAY_ENABLED=false`
  - `NAYRA_DB_ROL_EJECUCION`, `NAYRA_DB_ROL_NEGOCIO`, `NAYRA_DB_ROL_AUTENTICACION`
  - `NAYRA_VOZ_URL=http://nayra-voz:8001`
  - `NAYRA_VOCABULARIO`
  - `SPRING_JPA_SHOW_SQL=false`
  - `JAVA_TOOL_OPTIONS`
- **Voz**:
  - `NAYRA_VOZ_MODELOS`, `NAYRA_VOZ_VOCABULARIO`, `NAYRA_VOZ_PARAMETROS`
  - `NAYRA_VOZ_CLAVE_EMBEDDINGS_VERSION`
  - `NAYRA_VOZ_BD`: se arma en el arranque con la contraseña del secreto; conviene `sslmode=require`.
- **Migración**:
  - `FLYWAY_URL`, `FLYWAY_USER`, `FLYWAY_SCHEMAS`, `FLYWAY_LOCATIONS`
  - `FLYWAY_PLACEHOLDERS_ROL_EJECUCION`, `FLYWAY_PLACEHOLDERS_ROL_NEGOCIO`, `FLYWAY_PLACEHOLDERS_ROL_AUTENTICACION`, `FLYWAY_PLACEHOLDERS_ROL_BIOMETRIA`
- **Flutter** (compilación): `NAYRA_BACKEND_URL`.

### H-4. HTTPS y dominio

| Tramo | TLS | Requisito |
|---|---|---|
| App → API Gateway | Sí, certificado de Google en `*.gateway.dev` | **Ninguno**: la app usa esta URL |
| API Gateway → balanceador | Sí, obligatorio (lleva audio, PIN y JWT) | **Subdominio propio** (p. ej., `api.<tu-dominio>`) con **certificado administrado regional de Certificate Manager** mediante **autorización DNS** ([Verificado-doc](https://docs.cloud.google.com/certificate-manager/docs/deploy-google-managed-regional); sin dominio propio no se emite) |
| Balanceador → VM | HTTP en la red privada | Aceptable en esta fase (tráfico interno de la VPC). HTTPS hasta la VM añadiría certificados en cada VM sin beneficio claro ahora |
| Back → Voz | HTTP en la red interna de Docker de la misma VM | Token de servicio (D-010) |
| VMs → Cloud SQL | SSL exigido en Cloud SQL, `sslmode=require` | — |

Del dominio dependen: el certificado del balanceador, el `address` del gateway y el registro DNS (A) hacia la IP estática del balanceador. **No hace falta comprarlo todavía**; basta con decidir si usas uno existente o registras uno (C-5). La app **no** depende del dominio.

### H-5. Firewall (VPC `nayra`; el ingreso está denegado por defecto)

| Regla | Origen | Destino (etiqueta `nayra-app`) | Puerto |
|---|---|---|---|
| `allow-health-checks` | `35.191.0.0/16`, `130.211.0.0/22` [Verificado-doc] | VMs | tcp:8080 |
| `allow-lb-proxies` | Rango de la subred de solo proxy (DMZ) [Verificado-doc] | VMs | tcp:8080 |
| `allow-iap-ssh` | `35.235.240.0/20` (rango de IAP) | VMs | tcp:22 |

- **No** hay regla para 8001: Nayra-Voz no se publica en el host y solo existe en la red de Docker.
- **No** hay regla para 5432: Cloud SQL no tiene IP pública y se accede por Private Service Access desde la subred.
- VMs **sin IP pública**. Sin Cloud NAT: Artifact Registry, Secret Manager y Logging se alcanzan con **Private Google Access**, y los modelos van en la imagen.
- Opcional: restringir el egreso a los rangos de Google APIs y de Cloud SQL.

### H-6. Cloud SQL privado

- **Private Service Access**: reservar un rango interno y hacer el peering con `servicenetworking`.
- Instancia **sin IP pública**, SSL exigido y copias automáticas.
- Crear roles **antes** de migrar (§I). Nunca usar `postgres` desde la aplicación.

### H-7. Rutas a publicar en API Gateway y en el mapa de URLs del balanceador

Solo las que usa la app [Repo `Nayra-App/lib/api/*`]:

| Método | Ruta | Deadline |
|---|---|---:|
| POST | `/prototipo/autenticacion/nonces` | 20 s |
| POST | `/prototipo/autenticacion/transacciones` | 20 s |
| POST | `/prototipo/autenticacion/transacciones/{id}/pin` | 20 s |
| POST | `/prototipo/autenticacion/transacciones/{id}/pin-dictado` | **60 s** |
| POST | `/prototipo/autenticacion/transacciones/{id}/voz` | **60 s** |
| POST | `/prototipo/registro/{codigo}/enrolamiento/desafios` | 20 s |
| POST | `/prototipo/registro/{codigo}/enrolamiento/muestras` | **60 s** |
| POST | `/prototipo/registro/{codigo}/enrolamiento/finalizacion` | 20 s |
| GET | `/api/v1/registros/{codigo}` | 20 s |
| POST | `/api/v1/registros/{codigo}/datos`, `/api/v1/registros/{codigo}/finalizacion` | 20 s |
| GET | `/api/v1/usuarios/me` | 20 s |
| DELETE | `/api/v1/sesiones/actual` | 20 s |
| POST | `/api/v1/destinatarios/busqueda` | 20 s |
| GET/POST | `/api/v1/admin/**`: `usuarios`, `usuarios/{id}`, bloqueo, desbloqueo, dispositivo, revocación, `registros`, `registros/{codigo}/validacion-identidad`, `auditoria`, `auditoria/intentos-fallidos`, `auditoria/acciones-administrativas` | 20 s |

- **No publicar**: `/prototipo/arranque/**`, `/swagger-ui/**` ni `/v3/api-docs/**`.
- Definición OpenAPI 2.0 con `x-google-backend` usando:
  - `address: https://api.<dominio>`;
  - `path_translation: APPEND_PATH_TO_ADDRESS`;
  - `disable_auth: true`;
  - el `deadline` de la tabla.
- Tamaño de las peticiones: el backend acepta hasta 3 MB [Repo]. Confirmar el límite de API Gateway al configurarlo [Pendiente].

---

## I. Migraciones

### Inventario [Repo]

| Esquema | Versión | Contenido | Tipo |
|---|---|---|---|
| `nayra` | V001 | Crea el esquema | Normal |
| | V002 | Tablas núcleo | Normal |
| | V003 | Índices y restricciones | Normal |
| | V004 | Permisos del usuario de ejecución (`${rol_ejecucion}`; si el rol no existe, **WARNING y se omite**) | Permisos |
| | V005 | Documento DNI/CE y estados | Normal |
| | V006 | `credenciales` (bloque `DO` que verifica datos) | Normal, sensible (PIN) |
| | V007 | `codigo_qr` y moneda | Normal |
| | V008 | Solo Android | Normal |
| | V009 | `sesiones` | Normal |
| | V010 | `operaciones` y `notificaciones` | Normal |
| | V011 | Permisos por servicio (`${rol_ejecucion}`, `${rol_negocio}`, `${rol_autenticacion}`; mismo comportamiento si faltan) | Permisos |
| | V012 | Celular único (bloque `DO`: se detiene si hay datos inválidos) | Normal |
| `biometria` | V001 | Crea el esquema | Biometría |
| | V002 | `perfiles_voz` | Biometría |
| | V003 | Permisos del servicio biométrico (`${rol_biometria}`) | Biometría / permisos |

Todas son versionadas y se ejecutan **una sola vez** por base (historial de Flyway). No hay repetibles (`R__`).

### Plan (P-4 según tu propuesta; sin ejecutar en GCP)

1. **Como administrador de Cloud SQL** (una vez), crear:
   - la base `nayra`;
   - los roles `nayra_migracion` (dueño de la base), `nayra_app` (LOGIN), `nayra_negocio` y `nayra_autenticacion` (NOLOGIN), `biometria_migracion` y `nayra_biometria` (LOGIN);
   - `GRANT CREATE ON DATABASE nayra TO biometria_migracion`.
   **Crear los roles antes de migrar es obligatorio**: si no, V004/V011/V003 omiten los `GRANT` y quedan marcadas como aplicadas.
2. **Flyway `nayra`** (tarea puntual): `schemas=nayra`, `defaultSchema=nayra`, ubicación `db/migration/nayra`, los 3 placeholders, usuario `nayra_migracion`. Crea `nayra.flyway_schema_history`, el mismo historial que hoy crea Spring Boot, así que ambos mecanismos son compatibles [Inferido de la configuración].
3. **Flyway `biometria`** (tarea puntual): `schemas=biometria`, placeholder `rol_biometria`, usuario `biometria_migracion`, con su propio historial.
4. Orden entre 2 y 3: **independiente**, porque no hay FK entre esquemas (D-051). Se propone `nayra` → `biometria`.
5. Arrancar las VMs con `SPRING_FLYWAY_ENABLED=false`. Hibernate `validate` comprobará el esquema y `EntornoSimuladoInicial` cargará los datos ficticios en el primer arranque.
6. Desde dónde ejecutar: un contenedor `flyway/flyway:12.4.0` lanzado **a mano por SSH con IAP en la VM de la zona A**, con las credenciales de migración leídas en el momento y no guardadas en la VM. Alternativa: una VM temporal que se borra después.

**Verificado localmente** en este mismo orden, con `psql` para `biometria` y Spring Flyway para `nayra`:

- 12 + 3 migraciones aplicadas;
- `nayra_app` sin `UPDATE` en `auditoria`;
- `nayra_biometria` con acceso a `perfiles_voz`.

**Riesgos**:

- olvidar los roles;
- que dos instancias migren a la vez (se evita con `SPRING_FLYWAY_ENABLED=false`);
- que V012 se detenga si alguna vez se cargan datos con celulares inválidos (en una base nueva no ocurre);
- que la migración futura V013 (R-1) cambie permisos.

---

## J. Validación

### Fase A (una instancia)

1. Arranque:
   - la VM queda sana en el health check;
   - Voz `listo: true`;
   - Back conectado a Cloud SQL, sin errores de `validate`.
2. Las rutas no publicadas devuelven error, tanto por el gateway como por el dominio del balanceador (`/prototipo/arranque/...`, `/swagger-ui/...`).
3. Timeouts: una petición de voz de más de 15 s no se corta en el gateway (comprobar con un audio de 20 s).
4. Recorrido completo con el APK en un celular real con datos móviles:
   - crear el ADMIN inicial desde dentro de la VPC;
   - registro asistido;
   - enrolamiento con voz real en español;
   - inicio de sesión (PIN tecleado y dictado + voz);
   - "Mis datos";
   - búsqueda de destinatario;
   - cierre de sesión;
   - cierre por inactividad a los 5 min.
5. Seguridad:
   - la IP del balanceador no expone Swagger;
   - Cloud SQL sin IP pública;
   - 8001 inaccesible desde fuera de la VM;
   - auditoría registrada sin PIN, audio ni embeddings;
   - logs sin SQL ni secretos.
6. Biometría real (primera vez):
   - la misma persona es aceptada;
   - otra persona es rechazada;
   - una reproducción grabada es evaluada por AASIST.

   Se registra como **prueba funcional**, no como métrica de desempeño (D-060).

### Fase B (dos instancias), requiere R-1 resuelto

| # | Prueba | Cómo | ¿Posible hoy? |
|---|---|---|---|
| 1 | Ambas instancias reciben tráfico | Añadir el hostname de la VM a los logs de acceso o consultar los logs del balanceador (backend que atendió) con 50 peticiones | Sí (logs del balanceador) |
| 2 | Una instancia deja de responder | Detener el contenedor `nayra-back` en la VM A o apagar la VM | Sí |
| 3 | El balanceador la detecta | Estado del backend `UNHEALTHY` en el tiempo configurado (intervalo × umbral) | Sí |
| 4 | El tráfico va a la otra | Las peticiones siguientes responden desde B | Sí |
| 5 | No se pierde el estado necesario | Iniciar sesión en A, tumbar A a mitad del flujo y continuar | **No hoy** (R-1). Tras R-1: sí, salvo la petición en curso en el momento de la caída |
| 6 | Nayra-Voz sigue disponible | Si cae la VM entera, Voz-B atiende. Si solo cae el contenedor de Voz en A, un health check TCP **no** lo detecta | **Parcial**: completa solo con el endpoint de salud (C-6) |
| 7 | PostgreSQL accesible | Operaciones de lectura y escritura desde B tras la caída de A | Sí |
| 8 | JWT válido | Token emitido por A usado en B (`/api/v1/usuarios/me` → 200) | Sí, si `NAYRA_JWT_CLAVE` es común |
| 9 | El usuario puede seguir operando | Sesión abierta en A sigue en B; nuevo inicio de sesión completo en B | Sesión: sí. Flujo a medias: **solo tras R-1** |

Después de las pruebas: MIG de vuelta a tamaño 1 y autocuración revisada.

---

## K. Orden de implementación

Tu orden, con tres ajustes justificados (marcados ▲):

1. Resolver las decisiones de M y registrarlas en `07` (topología), `02` y `08` (texto de las 2 instancias).
2. Ajustes mínimos del repositorio:
   - `pom.xml` → Java 21;
   - `requirements.txt` con versiones fijadas;
   - Dockerfiles, Compose, `.dockerignore`, `.env.example`.
3. Docker local: construir las imágenes.
4. Probar Nayra-Back en su contenedor.
5. Probar Nayra-Voz con modelos reales **y voz real en español** (§J-6).
6. Docker Compose local completo: migraciones puntuales + ambos servicios + APK en el emulador.
7. Crear el proyecto GCP.
8. Billing y presupuesto con alertas.
9. VPC: subred de aplicación, subred de solo proxy (DMZ), firewall y Private Google Access.
10. Cloud SQL: Private Service Access, instancia PG16, base y roles.
11. Secret Manager: los 7 secretos y las cuentas de servicio.
12. Artifact Registry: subir las imágenes.
13. Compute Engine / MIG regional (zonas A y B) con **tamaño 1**.
14. ▲ Migraciones: tarea puntual desde la VM de la zona A. Justificación: necesita una máquina dentro de la VPC para llegar a Cloud SQL por IP privada, así que va después del paso 13. Arrancar Back **después** de migrar.
15. ▲ **Dominio y certificado (tu paso 17) antes del balanceador.** Justificación: la autorización DNS del certificado administrado regional puede tardar en activarse, y el proxy HTTPS del balanceador necesita el certificado.
16. Balanceador regional: backend service (`timeoutSec` 60, health check), mapa de URLs con lista blanca, proxy HTTPS, IP estática y DNS.
17. API Gateway (OpenAPI con `disable_auth`, `deadline`, rutas de H-7).
18. APK con `--dart-define=NAYRA_BACKEND_URL=https://<gateway>.gateway.dev`.
19. Validación de la Fase A (§J).
20. ▲ **Resolver R-1** (y el endpoint de salud si se aprueba), desplegar una nueva versión y volver a validar la Fase A. Justificación: sin esto, las pruebas 5, 6 y 9 de la Fase B no pueden pasar.
21. Activar la segunda instancia (MIG tamaño 2).
22. Validación de la Fase B y vuelta a tamaño 1.

---

## L. Bloqueadores

**Para empezar la Fase A:**

1. **Región** (C-1): sin ella no se puede crear nada. Santiago queda descartada para API Gateway.
2. **Dominio o subdominio** (C-5): sin él no hay HTTPS válido entre el gateway y el balanceador.
3. **Aprobación para modificar el repositorio**: Dockerfiles, `pom.xml` a 21 y `requirements.txt` fijado.
4. **Voz real en español** para validar la biometría (§J-6). No bloquea el despliegue técnico, pero sí considerarlo correcto.
5. Una **cuenta de GCP con facturación** activada, que solo tú puedes crear.

**Para la Fase B:**

6. **R-1** (C-3, C-4): el estado temporal en memoria impide completar flujos entre instancias.
7. El **endpoint de salud** (C-6), para detectar la caída de solo Nayra-Voz. Sin él, esa prueba queda parcial.

**No bloquean** (resueltos en esta auditoría): Java 21, PostgreSQL 16, compatibilidad de las migraciones con PG16, descarga y licencias de los modelos, ejecución de los modelos en CPU y timeouts de 60 s.

---

## M. Decisiones que necesito que confirmes

1. **Región**: todo en `us-east1` (**recomendado**), `us-east4`, o API Gateway en `us-east1` con el resto en Santiago.
2. **Registrar la topología** en `07` como decisión nueva (API Gateway → balanceador regional → firewall → VPC con subred de solo proxy como DMZ → MIG regional de 2 zonas → Cloud SQL privado), y añadir tu texto de las 2 instancias en `02` y `08`: sí / no.
3. **R-1 en Nayra-Back**: crear V013 con 4 tablas temporales (nonces, desafíos, transacciones de autenticación, registros en curso), modificando la exclusión de «tablas de desafíos o nonces» del modelo v4: sí / no.
4. **R-1 en Nayra-Voz** (enrolamiento pendiente): (a) tabla temporal cifrada en `biometria` (**recomendada**), (b) llamar siempre a la Voz de la misma VM, o (c) aceptar el reintento.
5. **Dominio**: usar uno que ya tengas (dime cuál) o registrar uno nuevo más adelante. Solo necesito un subdominio para el balanceador (p. ej., `api.<dominio>`).
6. **Endpoint de salud** que compruebe Back, la BD y Voz, para la Fase B: sí / no. Mientras tanto, TCP 8080 en la Fase A.
7. **Acceso directo al balanceador**: mapa de URLs como lista blanca desde la Fase A (**recomendado**, sin costo) y, además, Cloud Armor o validación de la identidad del gateway antes de un uso fuera de pruebas: cuál.
8. **Java 21**: cambiar `pom.xml` de 25 a 21 (**recomendado**) o solo pasar `-Djava.version=21` en Docker.
9. **Versiones de Python**: fijar Python 3.11 y el conjunto listado en §G.
10. **Migraciones (P-4)**: Flyway puntual para `nayra` y `biometria` desde la VM de la zona A por IAP, con `SPRING_FLYWAY_ENABLED=false` en las instancias: confirmar.
11. **Tipo de VM inicial**: `e2-standard-2`, a revisar tras medir en GCP.
12. **Clave de Gemini del historial**: revocarla ahora (**recomendado**) o dejarla para el endurecimiento de D-017.
13. **Cuándo modificar el repositorio**: autorizas el paso 2 del orden (Dockerfiles, Compose, `.dockerignore`, `.env.example`, `pom.xml`, `requirements.txt`) como un commit aparte, o prefieres revisarlo antes sin commit.
