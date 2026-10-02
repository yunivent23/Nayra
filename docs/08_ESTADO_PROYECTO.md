# 08 — ESTADO DEL PROYECTO NAYRA

## 1. Propósito

Este documento registra el estado actual de desarrollo del proyecto Nayra.

Su objetivo es permitir que cualquier integrante del equipo o herramienta de desarrollo, incluido Claude Code, pueda distinguir entre:

- elementos ya definidos;
- elementos implementados;
- elementos en desarrollo;
- elementos pendientes;
- elementos que deben ser verificados directamente en el repositorio.

Este documento **no reemplaza** los requisitos, la arquitectura ni el registro de decisiones técnicas.

> **Regla principal:** el estado de implementación debe basarse en evidencia del repositorio y no en suposiciones.

> **Actualización del 2026-09-28 (G-1 modificada y cerrada; commit «feat: implementa busqueda de destinatario por celular»):** el destinatario de una transferencia se busca por
> su **número de celular registrado en Nayra** (D-042, D-043). Implementado: migración V012 (celular `UNIQUE` y en
> formato canónico de Perú), normalización de `+51` y rechazo de celular repetido en el registro, endpoint provisional
> `POST /api/v1/destinatarios/busqueda` (solo devuelve el nombre parcial, p. ej. "María De la...") y, en `Nayra-App/`, la pantalla de búsqueda con
> teclado grande y la confirmación **Sí** / **No, buscar otro número**. La transferencia sigue sin implementarse; el
> dictado del número, la respuesta hablada y la agenda del teléfono quedan fuera. Detalle en
> `/mnt/project-files/analisis/G1_DESTINATARIO_CELULAR_IMPLEMENTACION_2026-09-28.md`.

> **Actualización del 2026-09-27 (modelo de datos v4, commits `5bbd505` y `ca98bb3` en `yuniv`, todavía sin push):**
> migraciones Flyway V001–V011 del esquema `nayra` (D-051/D-009 y modelo v4: documento DNI/CE, estados
> ACTIVO/BLOQUEADO/INACTIVO, `credenciales`, `codigo_qr` PROVISIONAL, solo ANDROID, `sesiones` con JWT `jti`,
> `operaciones`, `notificaciones`, permisos por servicio) y V001–V003 del esquema `biometria` (`perfiles_voz`, historial
> propio, en `Nayra-Voz/migraciones/biometria`). Persistencia del perfil de voz con psycopg. Correcciones de
> concurrencia y montos E-01, E-02 y E-03 aplicadas y probadas. Umbrales D-055: similitud 0.80 y bona fide 0.90
> (escala [0,1], provisionales hasta D-060). Decisiones cerradas el mismo día: G-1 (ID interno de la cuenta destino; modificada el 2026-09-28: celular),
> P-2 (provisional, sin APIs externas) y D-050 (aprobada funcionalmente, no implementada). El extractor ECAPA real no se
> ejecutó, así que el CHECK de 784 bytes de `embedding_cifrado` no se aplicó. Detalle en
> `/mnt/project-files/analisis/IMPLEMENTACION_MODELO_DATOS_V4_2026-09-27.md` y
> `/mnt/project-files/analisis/CORRECCIONES_E01_E02_E03_2026-09-27.md`.

---

## 2. Estado general

**Proyecto:** Nayra  
**Tipo:** Prototipo de solución móvil de autenticación mediante voz para usuarios con discapacidad visual en el contexto de billeteras digitales.

**Etapa actual:** Diseño y desarrollo del prototipo, correspondiente principalmente al alcance del Objetivo 2.

**Primer entregable (D-034):** prototipo funcional con aplicación móvil accesible por voz (solo Android, instalada mediante APK; iOS fuera de alcance), autenticación PIN + voz 1:1 + anti-spoofing, entorno financiero simulado (saldo, movimientos y transferencias directas a otros usuarios) y panel web del administrador. Registro **asistido** por un representante autorizado (D-052). El QR (HU-123, HU-124) queda para un siguiente entregable. Alcance por HU en `01_REQUISITOS_NAYRA.md` §12.4.

El short paper contempla hasta el Objetivo 2, incluyendo el diseño de la solución y parte del desarrollo.

### Estado documental

| Área | Estado |
|---|---|
| Contexto del proyecto | COMPLETADO |
| Requisitos | DOCUMENTADO — depurado por AG-00 y actualizado por AG-01 (HU-117 a HU-125; alcance del primer entregable; D1–D6 pendientes) |
| Arquitectura | EN DEFINICIÓN CONTROLADA — componentes y flujos lógicos del primer entregable documentados (AG-01); 2 instancias en zonas diferentes (D-023) y 3 servicios lógicos por responsabilidad (modelo v4: Negocio, Autenticación y Biométrico; Negocio y Autenticación en una sola aplicación Spring Boot) |
| Base de datos | MODELO DE DATOS v4 IMPLEMENTADO (2026-09-27, commit `5bbd505`, sin push): 10 tablas en el esquema `nayra` (V001–V011; V012 del 2026-09-28, celular único) y `biometria.perfiles_voz` (V001–V003), con Flyway y `ddl-auto=validate` (D-051, D-009; `03` §17). Sin tabla `roles` ni `solicitudes_atencion`. Pendientes: P-3, P-4 y el CHECK de 784 bytes (las seis reglas de H-02 quedaron confirmadas) |
| Biometría de voz | DISEÑO TÉCNICO APROBADO (AG-13, `05_BIOMETRIA_NAYRA.md` §27): modelo, anti-spoofing, reconocimiento del desafío, almacenamiento y comunicación decididos; umbrales de similitud 0.80 y bona fide 0.90 fijados como **provisionales hasta la calibración** (D-055, D-060); prototipo funcional EN DESARROLLO (2026-09-27, ver §3 y §5); **biometría real no validada** en el entorno actual |
| Seguridad | PRINCIPIOS Y CONTROLES DOCUMENTADOS — controles AG-01 en `06_SEGURIDAD_NAYRA.md` §36 |
| Decisiones técnicas | REGISTRADAS hasta D-061 más las decisiones del modelo de datos v4 (2026-09-27; `07_DECISIONES_TECNICAS_NAYRA.md`), D-062 a D-074 (despliegue en GCP, 2026-09-29) y D-075 a D-080 (interacción voice-first, 2026-10-02; aprobadas, **no implementadas**); siguen PENDIENTES, entre otras, D-014, D-016, D-045, D-046 (resto), D-047, D-060, P-3, P-4, P-5, P-7/D-059, P-8, P-10, P-11, B-4, B-5 y el modelo de la credencial administrativa (D-050); H-01, H-02 y H-03 quedaron cerradas el 2026-09-27 |
| API | PENDIENTE DE DEFINICIÓN FINAL |
| Estado de implementación | ESTE DOCUMENTO |
| Reglas de desarrollo | DOCUMENTADAS EN `09_REGLAS_DESARROLLO_NAYRA.md` |

---

## 3. Implementación

La implementación debe verificarse directamente en el repositorio de código.

Este documento no debe afirmar que una funcionalidad está implementada únicamente porque aparezca en los requisitos o documentos de diseño.

### 3.1 Backend Java

**Tecnología considerada/aprobada:** Java + Spring Boot.

Responsabilidad general:

- lógica principal del sistema;
- gestión de usuarios;
- autenticación y autorización;
- gestión de cuentas y operaciones simuladas;
- gestión de sesiones;
- notificaciones;
- solicitudes de atención;
- auditoría;
- comunicación con servicios especializados cuando corresponda.

**Estado de implementación (verificado en el repositorio el 2026-09-27, commits `5bbd505` y `ca98bb3`, sin push):** BACKEND GENERAL EN DESARROLLO, con persistencia en PostgreSQL de las tablas del modelo v4 (incluidas `credenciales` y `sesiones`).

- **Retirado** por inseguro o no aprobado (`06` §36.7): CRUD de `Users`/`Role` con contraseña en texto plano y rol elegido por el cliente, `GET /usuarios` público, el JWT heredado sin aprobación (D-018; el JWT actual es el del modelo v4, ver «Sesión»), CORS abierto y reglas heredadas (`/bicicletas`, `/alquileres`), `ddl-auto=update` (ahora `validate` con migraciones Flyway, D-051). Las credenciales existentes **no** se tocaron (D-017).
- **Dominio según `03` §17**: `Usuario` (documento DNI/CE, estado ACTIVO/BLOQUEADO/INACTIVO, rol USER o ADMIN), `Credenciales` (hash del PIN e intentos), `Dispositivos`, `Sesiones`, `Auditoria` (actor, usuario afectado, acción, resultado, motivo, dispositivo), `Cuentas` (con `codigo_qr` provisional y moneda PEN), `EntidadBancaria`, `RegistroIdentidadSimulado`, `Operaciones` y `Notificaciones`. Mapeo JPA sobre el esquema `nayra` con adaptadores PostgreSQL detrás de los puertos de repositorio; los adaptadores en memoria quedan solo para pruebas unitarias. Datos **ficticios** del entorno simulado en `entorno-simulado/datos-ficticios.json`.
- **Registro inicial asistido (flujo de D-052)**: el representante proporciona el DNI y valida la identidad; la persona confirma sus datos, registra su celular, crea su PIN (solo hash), vincula la clave del dispositivo, enrola su voz (AG-13) y finaliza. Se vincula la cuenta financiera simulada por DNI. DNI ya registrado → rechazo auditado. **Provisional del prototipo** (no son decisiones de D-052): solo un ADMIN actúa como representante; el paso al celular de la persona usa un código de un solo uso (900 s); si la persona no confirma sus datos, el registro se cancela; formato del DNI. El celular es único y de Perú (9 dígitos, empieza por 9, sin `+51`; G-1, 2026-09-28). _(2026-10-02: el registro autónomo (D-077) y la validación posterior del enrolamiento (D-078) están aprobados pero **no implementados**.)_
- **Sesión**: se crea al autenticar por voz y se persiste en `nayra.sesiones`. JWT en `Authorization: Bearer` cuyo claim `jti` es `sesiones.id`; el token no se guarda. Cierre tras 5 minutos de inactividad controlado en el servidor (D-018), sin renovación ni duración máxima absoluta; cierre de sesión (HU-13) y revocación al bloquear la cuenta o revocar el dispositivo (D-040). Revocación y registro de acceso con UPDATE condicionales (E-02). HS256 con clave de `NAYRA_JWT_CLAVE`: algoritmo y custodia PROVISIONALES (P-5).
- **Autorización** (roles USER y ADMIN de D-041, representados como valor fijo en `usuarios.rol` por D-009; el mecanismo por rutas sigue siendo **provisional**): `/api/v1/admin/**` solo ADMIN, `/api/v1/**` con sesión, `/prototipo/**` por la excepción del primer entregable, todo lo demás denegado; respuestas 401/403 en JSON.
- **Administración** (solo API; panel web pendiente, D-045): listar y buscar usuarios (HU-20, HU-21), detalle y estado (HU-22, HU-101), bloqueo y desbloqueo (HU-19), consulta y revocación del dispositivo (HU-125) y consultas de auditoría (HU-89, HU-80, HU-94). Toda acción queda auditada. Regla técnica **provisional** añadida (no proviene de D-041): un ADMIN no puede bloquearse ni revocar su propio dispositivo.
- **Usuario**: datos propios y estado (HU-09, HU-15).
- **Errores**: formato único `{"error": "CODIGO"}` sin datos sensibles.
- **Organización de la API**: `/api/v1/...` para la API general y `/prototipo/...` para voz y arranque. Es una propuesta **provisional**: el contrato sigue pendiente (D-014).
- Operaciones y notificaciones: entidades y repositorios PostgreSQL implementados (modelo v4), con validación del monto antes de persistir (E-03); **sin servicio ni endpoint**: saldo, movimientos, transferencias y notificaciones no están implementados. Solicitudes de atención: sin tabla ni código (clase vacía).
- Compila con JDK 21 (`mvn -Djava.version=21`; el `pom.xml` pide Java 25). Arranca sin `jwt.secret`.

**Prototipo AG-13 (verificado el 2026-09-27): EN DESARROLLO, integrado con el backend general.** Activado con el perfil `prototipo`:

- Implementado: nonce y firma ECDSA P-256 del dispositivo (D-048), PIN de 6 dígitos con hash y PIN dictado (D-061), desafío palabra + 3 dígitos + palabra con `SecureRandom` y un solo uso (D-054), cliente del servicio de voz (D-010), decisión final, intentos, bloqueo y auditoría (D-056), creación de la sesión al autenticar.
- AG-13 usa el usuario, el dispositivo, la sesión y la auditoría generales; el backend general no depende de AG-13. Java no accede a embeddings (D-013).
- Intentos (D-044, modelo v4): solo el PIN incorrecto cuenta; contador en `credenciales` con incremento atómico (E-01); al tercero, bloqueo y revocación de sesiones. El contador vuelve a 0 en el paso del PIN correcto, antes de la voz, tal como decidió H-01 (cerrada).
- Provisional: hash del PIN con BCrypt sin pepper (D-047; no cumple el pepper que exige D-061), rutas `/prototipo/...` (D-014), el ADMIN se autentica con el mismo flujo del usuario (dispositivo + PIN + voz) aunque D-050 ya definió usuario y contraseña en el panel web, cuyo modelo de credencial sigue pendiente, y el primer ADMIN se crea con una ruta de arranque del perfil `prototipo`, separada de D-052 y sin validación por representante.
- Pruebas Java (2026-09-27, con `NAYRA_TEST_DB_URL`): 110 ejecutadas, 109 correctas, 0 fallos y 1 omitida (`ContratoServicioVozTest`, necesita el servicio Python en ejecución). Las pruebas de persistencia necesitan PostgreSQL y solo corren con `NAYRA_TEST_DB_URL` definida.

---

### 3.2 Backend Python

**Tecnología:** Python.

Responsabilidad:

- procesamiento especializado relacionado con voz;
- extracción/procesamiento de características de voz;
- integración con modelos preentrenados;
- biometría de voz;
- mecanismos de detección de intentos de suplantación, cuando hayan sido implementados.

**Estado (verificado el 2026-09-27):** PROTOTIPO EN DESARROLLO en `Nayra-Voz/` (FastAPI): audio, calidad con WebRTC VAD (candidata provisional), Vosk, AASIST, ECAPA-TDNN, cifrado AES-256-GCM del embedding, enrolamiento con centroide, transcripción del PIN dictado y persistencia del perfil en `biometria.perfiles_voz` con psycopg (modelo v4; en memoria si no se define `NAYRA_VOZ_BD`). Pruebas actuales: 57 ejecutadas, 56 correctas, 0 fallos y 1 omitida (`test_aasist_real`, pesos no descargados), con modelos simulados y WebRTC VAD real. **ECAPA-TDNN, AASIST y Vosk reales no están validados en el entorno actual**: sus modelos no pudieron descargarse (en un registro anterior del mismo día AASIST llegó a cargarse con sus pesos publicados; esa prueba hoy se omite). Parámetros PROVISIONALES; similitud 0.80 y bona fide 0.90 fijados por D-055 hasta la calibración (D-060).

Antes (2026-09-26): no existía código Python en el repositorio.

Revisión del 2026-09-27 (AG-13; D-010 y D-056 en AG-12): decididos FastAPI (D-010), SpeechBrain ECAPA-TDNN (D-011), AASIST (D-012), almacenamiento del embedding cifrado sin audio (D-013) y Vosk para el contenido del desafío (D-046, parcial). El servicio Python aplica los umbrales técnicos y Spring Boot decide la autenticación (D-056). Pendientes: contrato en `04_API.md` (D-014), calibración de los umbrales (D-055 fijó valores provisionales; calibración en D-060) y despliegue (D-016).

---

### 3.3 Frontend / aplicación móvil

La tecnología definitiva del frontend todavía debe considerarse una decisión técnica pendiente si no ha sido aprobada formalmente.

**Estado:** POR DEFINIR. **No existe código de aplicación móvil ni de panel web en el repositorio** (verificado 2026-09-26). 2026-09-27: la aplicación móvil será **Flutter** (D-007), con canal Kotlin para el almacén de claves (D-048). El panel web sigue pendiente (D-045).

**Prototipo (verificado el 2026-09-27): EN DESARROLLO** en `Nayra-App/` (solo Android; iOS fuera de alcance): teclado de PIN accesible (solo anuncia el avance), PIN dictado, clave del dispositivo en el Android Keystore mediante canal Kotlin, desafío en pantalla y para el lector de pantalla, grabación manual WAV 16 kHz mono en memoria, registro en el celular con el código de registro (D-052), sesión tras autenticar, "Mis datos" y cierre de sesión. El tutorial (paso 12 de D-052) no está implementado porque su contenido no está definido. `flutter analyze` sin observaciones y 9 pruebas OK. **No se compiló para Android** (no hay Android SDK en el entorno de desarrollo), por lo que el canal Kotlin no está verificado.

Claude no debe asumir un framework de frontend sin consultar `07_DECISIONES_TECNICAS_NAYRA.md` y el repositorio.

---

## 4. Base de datos

### Tecnología

**PostgreSQL:** tecnología considerada para la base de datos relacional.

### Modelo CORE

El modelo documentado contempla, entre otras, las siguientes entidades:

- ROLES _(propuesta original; no se implementa como tabla: `usuarios.rol`, D-009)_
- USUARIOS
- CREDENCIALES _(modelo v4)_
- CUENTAS
- SESIONES
- DISPOSITIVOS
- OPERACIONES
- NOTIFICACIONES
- SOLICITUDES_ATENCION _(sin tabla física)_
- AUDITORÍA

Además, el modelo incluye las tablas de referencia del entorno simulado:

- ENTIDADES_BANCARIAS (AG-00, D-026)
- REGISTRO_IDENTIDAD_SIMULADO (AG-01, D-035)

El modelo lógico vigente de las tablas afectadas por AG-01 está en `03_BASE_DE_DATOS_NAYRA.md` §16; el modelo físico implementado (v4), en §17.

El detalle oficial del modelo se encuentra en:

`03_BASE_DE_DATOS_NAYRA.md`

### Estado

**Modelo documentado:** SÍ.  
**Implementación física (commit `5bbd505`, sin push):** esquema `nayra` con `entidades_bancarias`, `registro_identidad_simulado`, `usuarios`, `credenciales`, `cuentas`, `dispositivos`, `sesiones`, `operaciones`, `notificaciones` y `auditoria` (Flyway V001–V012, `ddl-auto=validate`; D-051, D-009, modelo v4 y V012 del 2026-09-28) y esquema `biometria` con `perfiles_voz` (V001–V003, historial propio). Detalle en `03` §17. Sin tabla: `roles`, `solicitudes_atencion`, desafíos y nonces. Las seis reglas que v4 marcaba "(a confirmar)" están aplicadas en la base de datos y quedaron confirmadas formalmente (H-02, cerrada el 2026-09-27). Qué servicio ejecuta las migraciones y con qué permisos sigue pendiente (P-4).

No deben agregarse tablas biométricas como `VOICE_BIOMETRICS`, `VOICE_EMBEDDINGS` o `ANTI_SPOOFING` sin una decisión explícita. _(AG-13: la única estructura biométrica aprobada es `biometria.PERFILES_VOZ`, D-013, `03_BASE_DE_DATOS_NAYRA.md` §16.11; implementada en el modelo de datos v4, `03` §17.11.)_

---

## 5. Biometría de voz

La biometría de voz constituye uno de los componentes centrales de Nayra.

### Definiciones actuales

- Se utilizarán modelos preentrenados.
- Python será utilizado para el procesamiento especializado.
- SpeechBrain ECAPA-TDNN aprobado para la verificación 1:1 (AG-13, D-011).
- El objetivo conceptual es realizar **verificación de voz**, no identificación abierta.
- Se considera protección frente a intentos de suplantación mediante reproducción o voz sintética/manipulada.
- AG-01: enrolamiento durante el registro con anti-spoofing; verificación 1:1 tras la contraseña; desafío variable con comprobación de contenido; referencia biométrica persistente en el backend (D-036 a D-038, `05_BIOMETRIA_NAYRA.md` §26).

### Decisiones todavía pendientes

- ~~modelo definitivo~~ (D-011), ~~algoritmo de comparación~~ (coseno 1:1, D-011), ~~estrategia de almacenamiento~~ (D-013), ~~retención de audio~~ (no se guarda audio, D-013), ~~anti-spoofing~~ (D-012), ~~integración con Java~~ (D-010), ~~responsable de aplicar los umbrales técnicos~~ (servicio Python, D-056) — resueltos el 2026-09-27 (AG-13; D-010 y D-056 en AG-12);
- calibración de los umbrales (D-055 fijó provisionalmente similitud 0.80 y bona fide 0.90, escala [0,1]; los valores definitivos dependen de D-060);
- límite propio de reintentos biométricos (P-8), rango de muestras de enrolamiento (P-7 / D-059), AAD definitiva (B-4), frecuencia de rotación de claves (B-5) y CHECK de 784 bytes;
- dataset de calibración y consentimiento (D-060);
- herramienta de VAD definitiva (D-058; WebRTC VAD es candidata provisional desde el 2026-09-27);
- validación de la lista v2 y de la vida del desafío (D-054, ambas PROVISIONALES);
- resto del reconocimiento del habla: comando y DNI (D-046); el PIN dictado usa una candidata provisional.

**Estado:** DISEÑO TÉCNICO APROBADO / PROTOTIPO EN DESARROLLO (2026-09-27) / VALIDACIÓN PENDIENTE.

---

## 6. Seguridad

La solución debe aplicar principios de:

- mínimo privilegio;
- defensa en profundidad;
- seguridad desde el diseño;
- validación de entradas;
- protección de credenciales;
- protección de sesiones;
- HTTPS/TLS;
- protección de datos personales;
- protección de información biométrica;
- control de intentos;
- protección contra replay;
- auditoría;
- gestión segura de secretos.

Los mecanismos tecnológicos concretos deben consultarse en `07_DECISIONES_TECNICAS_NAYRA.md`.

**Estado:** REQUISITOS Y PRINCIPIOS DOCUMENTADOS. Implementado en el repositorio (2026-09-27, commits `5bbd505` y `ca98bb3`, sin push): correcciones de `06` §36.7, autorización USER/ADMIN con denegación por defecto, PIN solo con hash en `credenciales` (nunca expuesto en APIs), contador de intentos atómico (E-01), sesión JWT con `jti` sin guardar el token y revocación que no puede deshacerse por escrituras obsoletas (E-02), validación del monto sin redondeo (E-03), firma del dispositivo contra replay, auditoría sin PIN, audio, embeddings ni secretos, y errores sin datos sensibles. Sigue pendiente: TLS, secretos (D-017), hash definitivo del PIN (D-047) y claims, `exp`, algoritmo y custodia de la clave del JWT (P-5).

---

## 7. Infraestructura

### 7.1 Instancias

La arquitectura contempla:

**2 instancias de aplicación**, ubicadas en **zonas diferentes**.

Objetivo:

- mejorar la disponibilidad;
- reducir el impacto de una falla localizada en una zona;
- mantener la continuidad del servicio cuando sea posible.

### 7.2 Componentes no asumidos

No se deben asumir automáticamente:

- DMZ;
- API Gateway;
- balanceador;
- VPC específica;
- nombres concretos de zonas;
- configuración concreta de red;
- servicios cloud específicos.

Estos elementos solo deben incorporarse cuando exista una decisión aprobada.

**Actualización 2026-09-29:** API Gateway, el balanceador regional, la VPC, la DMZ (subred de solo proxy) y la región `us-east1` quedan **aprobados como arquitectura objetivo** en D-062 a D-074. Su estado es **no desplegado**: no se ha creado ningún recurso en GCP.

Pendiente de implementar antes de la fase de 2 instancias activas:
- D-064: tablas temporales V013;
- D-065: `biometria.enrolamientos_pendientes`;
- D-067: endpoint de salud.

### 7.3 Cloud

GCP se encuentra como tecnología/plataforma considerada.

La configuración física definitiva debe consultarse en la documentación de arquitectura y decisiones técnicas.

**2026-10-01 — entorno local (no es GCP):**
- Docker Compose levanta PostgreSQL 16, Nayra-Voz (modelos reales: Vosk, WebRTC VAD, AASIST y ECAPA-TDNN) y Nayra-Back (Java 21). Verificado en un entorno de pruebas, con migraciones V001–V012 y biometria V001–V003 aplicadas.
- Ruta Back → Voz verificada: el pipeline real se ejecuta etapa por etapa. La voz sintética se rechaza por anti-spoofing y una frase distinta, por contenido.
- **Pendiente en el celular físico:** registro y autenticación con voces humanas reales (casos 1 a 5 de la guía). Ver `docs/entorno-local/GUIA_ENTORNO_LOCAL.md`.

**2026-09-29:** la configuración objetivo en GCP está aprobada (D-062 a D-074) y documentada en `02_ARQUITECTURA_NAYRA.md` §17 y en `docs/despliegue/`. El despliegue está **pendiente**: todavía no existe proyecto, red ni instancias.

---

## 8. Entorno bancario simulado

Nayra **no se conectará directamente con bancos reales** dentro del alcance actual.

Para las pruebas y demostración del prototipo se utilizará un **entorno bancario simulado y controlado**.

Este entorno permitirá representar:

- entidades bancarias;
- cuentas;
- operaciones;
- estados de operaciones;
- escenarios necesarios para probar los flujos de autenticación y transacción.

Las operaciones serán simuladas y **no involucrarán fondos reales ni cuentas bancarias reales**.

La integración con APIs o sistemas core de bancos reales está fuera del alcance actual.

Definiciones aprobadas en AG-00 (ver `07_DECISIONES_TECNICAS_NAYRA.md`, D-024 a D-033):

- cada usuario tiene **una única cuenta financiera**, asociada a una **entidad bancaria simulada**;
- la cuenta financiera se localiza por DNI durante el registro y se vincula al usuario mediante FK con restricción de unicidad;
- las operaciones referencian la cuenta financiera de origen y la de destino;
- las transferencias no requieren seleccionar cuenta;
- AG-01: la identidad se consulta en un registro de identidad simulado (D-035); las transferencias son a otros usuarios de Nayra, directamente o mediante QR (D-042);
- modelo v4 (2026-09-27): documento DNI o CE sin APIs externas (P-2, provisional); moneda PEN; el destinatario se identifica por el ID interno de la cuenta destino (G-1), que desde el 2026-09-28 el backend obtiene del celular del destinatario; monto mayor que 0 y menor que 500;
- no se incluyen apertura de cuentas, múltiples cuentas, transferencias entre cuentas propias ni gestión de entidades por el administrador.

---

## 9. Funcionalidades

### Implementadas

La lista de funcionalidades implementadas debe mantenerse sincronizada con el repositorio.

**Estado actual (verificado 2026-09-27, commits `5bbd505` y `ca98bb3`, sin push):** ninguna funcionalidad se considera terminada: todas dependen de piezas provisionales (umbrales sin calibrar, biometría real no validada, P-5, D-047). Ver «En desarrollo».

### En desarrollo

Deben registrarse aquí las funcionalidades que tengan código parcial pero que todavía no estén completas.

**Estado actual (2026-09-27):** de extremo a extremo entre `Nayra-App/`, `Nayra-Back/` (perfil `prototipo`) y `Nayra-Voz/`, con el modelo v4 persistido en PostgreSQL (sesiones incluidas; perfil de voz en `biometria` si se define `NAYRA_VOZ_BD`) y parámetros provisionales:

- registro inicial asistido con enrolamiento de voz (D-052; sin tutorial);
- inicio de sesión por dispositivo, PIN (tecleado o dictado), desafío y voz, con sesión y cierre por inactividad;
- datos propios, estado de la cuenta y cierre de sesión;
- búsqueda del destinatario de una transferencia por celular y confirmación Sí / No (G-1, 2026-09-28; sin transferencia);
- API del administrador: usuarios, bloqueo, dispositivo y auditoría (sin panel web, D-045; el acceso del ADMIN usa provisionalmente dispositivo + PIN + voz, no el usuario y contraseña de D-050; las consultas de auditoría se mantienen en el alcance, H-03 cerrada).

### Pendientes

Deben registrarse las funcionalidades que todavía no tengan una implementación funcional.

**Estado actual:** cambio de dispositivo y recuperación (D-049), solicitudes de atención y pérdida del celular (estados sin definir), actualización de datos (HU-10), saldo, movimientos y ejecución de transferencias (tablas creadas, sin servicio ni endpoint; solo existe la búsqueda del destinatario), notificaciones, tutorial, panel web del administrador (D-045), autenticación del administrador con usuario y contraseña (D-050, modelo pendiente) y ejecución de las migraciones de `biometria` fuera de las pruebas (P-4). El QR queda para un siguiente entregable.

> No convertir automáticamente una historia de usuario en una funcionalidad implementada.

---

## 10. Pruebas

Las pruebas deben registrar como mínimo:

- funcionalidad evaluada;
- escenario;
- resultado esperado;
- resultado obtenido;
- estado;
- evidencia cuando corresponda.

### Estado actual

**Pruebas automatizadas (2026-09-27, tras E-01, E-02, E-03 y D-055):**

- Java: 110 ejecutadas, 109 correctas, 0 fallos, 1 omitida (`ContratoServicioVozTest`, contrato con el servicio real). Incluye `ConcurrenciaPostgresTest` (12 pruebas de E-01 y E-02), `ModeloDatosV4Test`, `ModeloDatosV4PostgresTest`, `PermisosPorServicioTest`, `MigracionDatosDesarrolloTest` y `PersistenciaPostgresTest`. Las pruebas con PostgreSQL corren solo con `NAYRA_TEST_DB_URL` y reconstruyen el esquema con Flyway (clean + migrate).
- Python: 57 ejecutadas, 56 correctas, 0 fallos, 1 omitida (`test_aasist_real`). Los modelos ECAPA-TDNN, AASIST y Vosk se sustituyen por dobles de prueba: **la biometría real no se ejecutó**.
- Flutter: 9 pruebas OK (registro anterior del 2026-09-27; no se volvieron a ejecutar en esta sincronización).

**2026-09-28 (G-1, destinatario por celular, cierre):** Java 122 ejecutadas, 121 correctas, 0 fallos, 1 omitida (la misma). Flutter: `flutter analyze` sin observaciones y 42 pruebas OK (incluye el frontend reescrito el 2026-09-27). Las pruebas que registraban varios usuarios con el mismo celular se adaptaron a la unicidad de V012. V012 se validó solo sobre las bases de prueba disponibles (`nayra_test` y la base de datos de desarrollo simulada de `MigracionDatosDesarrolloTest`); no se aplicó sobre una base de desarrollo real.

**Pruebas de integración:** prueba de contrato Java ↔ Python y recorrido HTTP completo contra los servicios en ejecución (Spring Boot con PostgreSQL local encendido y FastAPI con modelos simulados): arranque del administrador, registro asistido, enrolamiento, inicio de sesión, autorización USER/ADMIN, bloqueo, desbloqueo, revocación del dispositivo, cierre de sesión y auditoría. 42 de 42 comprobaciones OK el 2026-09-27, **antes** del modelo de datos v4; no se volvió a ejecutar después.

**Pruebas de biometría:** PENDIENTES DE IMPLEMENTACIÓN/VALIDACIÓN.  
**Pruebas de seguridad:** POR VERIFICAR.

---

## 11. Problemas y bloqueos

Los problemas deben registrarse cuando afecten el desarrollo.

Formato recomendado:

| ID | Problema | Impacto | Estado | Responsable |
|---|---|---|---|---|
| P-001 | Credenciales de PostgreSQL en texto plano en `application.properties`; secretos (`jwt.secret`, clave de API) presentes en el historial de Git | Seguridad — **deuda técnica** (D-017, categoría C) | ABIERTO — no bloquea el primer entregable; se corrige antes de producción (segundo entregable) | Equipo |
| P-002 | `POST /usuarios` guardaba contraseñas en texto plano y permitía que el cliente eligiera su rol | Seguridad crítica | RESUELTO (2026-09-27; incluido en los commits locales `ef007fd`/`5bbd505`, sin push): CRUD retirado; el rol lo fija el registro asistido | — |
| P-003 | `GET /usuarios` era público y exponía datos personales y el campo contraseña | Seguridad crítica | RESUELTO (2026-09-27; incluido en los commits locales `ef007fd`/`5bbd505`, sin push): ruta retirada; la consulta de usuarios exige ADMIN | — |
| P-004 | Mecanismo JWT implementado sin aprobación (D-018 pendiente) | Alto | RESUELTO (2026-09-27): el JWT heredado se retiró; después el modelo de datos v4 aprobó un JWT con `jti` = `sesiones.id` (D-018), implementado en `5bbd505`; P-5 sigue pendiente | — |
| P-005 | Código heredado ajeno a Nayra (reglas `/bicicletas`, `/alquileres`, comentarios de plantilla) y CORS abierto | Medio | RESUELTO (2026-09-27; incluido en los commits locales `ef007fd`/`5bbd505`, sin push) | — |
| P-006 | `ddl-auto=update` creaba el esquema sin estrategia de migraciones (D-051) | Alto | RESUELTO (2026-09-27; incluido en los commits locales `ef007fd`/`5bbd505`, sin push): Flyway y `ddl-auto=validate` (D-051) | — |
| P-007 | Posible incompatibilidad de versiones en `pom.xml` (Spring Boot 4.1.1 con `spring-boot-starter-security` 3.5.5) | Alto | 2026-09-27: la versión fija de seguridad se retiró (la gestiona Spring Boot); compila y arranca con JDK 21 (`-Djava.version=21`); el `pom.xml` pide Java 25 | — |
| P-009 | La aplicación no arrancaba sin la propiedad `jwt.secret` | Medio | RESUELTO (2026-09-27; incluido en los commits locales `ef007fd`/`5bbd505`, sin push): al retirar JWT ya no se necesita | — |
| P-008 | Modelo `Users` no correspondía al aprobado | Medio | RESUELTO (2026-09-27, commit `5bbd505`): `Usuario` según `03` §16.1, mapeado a `nayra.usuarios` (`03` §17.3) | — |
| P-010 | Contradicción documental sobre el momento en que vuelve a 0 el contador de PIN (H-01): el modelo v4 dice "tras una autenticación correcta con PIN" y "un PIN correcto → 0"; el código lo reinicia en el paso del PIN correcto, antes de la voz | Medio | RESUELTO (2026-09-27): H-01 cerrada, PIN correcto → 0; coincide con el código | Equipo |
| P-011 | Consultas administrativas de auditoría en el código frente al alcance del modelo v4 (H-03); `01` §12.4 no se modificó | Medio | RESUELTO (2026-09-27): H-03 cerrada, se mantiene `01` §12.4 y las consultas existentes | Equipo |
| P-012 | El comentario SQL de `nayra.operaciones` en V010 todavía dice "G-1 PENDIENTE BLOQUEANTE", aunque G-1 se cerró | Bajo | ACEPTADO como deuda documental: no se edita V010; V012 (celular único) no toca ese comentario; la documentación registra G-1 | Equipo |
| P-013 | En Flutter, la pantalla B4 del inicio de sesión ("Repita la frase") se desborda con el tamaño de letra del sistema al 200 % | Medio (accesibilidad) | ABIERTO: deuda de accesibilidad de la autenticación, independiente de G-1; se corrige en un cambio aparte | Equipo |

No inventar problemas si no existe evidencia.

---

## 12. Próximos pasos

Los próximos pasos deben derivarse de:

1. requisitos pendientes;
2. decisiones técnicas pendientes;
3. estado real del repositorio;
4. objetivos del proyecto;
5. dependencias entre componentes.

Antes de comenzar una nueva tarea, Claude debe verificar si existe una decisión pendiente que pueda afectar la implementación.

Orden de trabajo aprobado para el primer entregable (AG-01):

0. ~~Higiene del repositorio: secretos fuera de la configuración, rotación de credenciales.~~ **AG-01 v5:** reclasificado como deuda técnica (D-017); no forma parte del trabajo previo al desarrollo.
1. Documentación de AG-01 (completada en documentación el 2026-09-26; pendiente de revisión).
2. Cierre de las decisiones de base que bloquean el desarrollo (`07_DECISIONES_TECNICAS_NAYRA.md`).
3. Backend base: compilación, corrección del `pom.xml`, retiro del código heredado, manejo de errores y validación.
4. Modelo de datos v1 con migraciones y datos simulados. _(2026-09-27: modelo de datos v4 implementado, `5bbd505`.)_
5. Auditoría (transversal).
6. Registro asistido sin biometría (validación por representante autorizado, D-052).
7. Componente Python (en paralelo desde el paso 3).
8. Inicio de sesión completo con política de intentos.
9. Aplicación móvil accesible (en paralelo cuando D-007 y D-046 estén decididas).
10. Operaciones simuladas: saldo, movimientos, transferencia.
11. Panel web del administrador.
12. Recuperación, pérdida y cambio de dispositivo.
13. Secundarias: métricas básicas y notificaciones. El QR (HU-123, HU-124) queda para un siguiente entregable.

---

## 13. Reglas para actualizar este documento

1. Actualizarlo cuando cambie materialmente el estado de implementación.
2. No marcar como COMPLETADO algo que solo esté diseñado.
3. No marcar como IMPLEMENTADO algo que solo esté documentado.
4. Verificar el repositorio antes de afirmar que una funcionalidad existe.
5. Registrar las funcionalidades parcialmente implementadas como EN DESARROLLO.
6. Mantener las decisiones técnicas en `07_DECISIONES_TECNICAS_NAYRA.md`.
7. Mantener los requisitos en `01_REQUISITOS_NAYRA.md`.
8. No modificar requisitos para justificar una implementación.
9. No recuperar arquitecturas descartadas.
10. Si existe contradicción entre este documento y el código, verificar primero el repositorio y registrar el cambio correspondiente.

---

## 14. Fuente de verdad

Para conocer el estado de implementación:

**Repositorio de código > documentación de estado > documentación de diseño > suposición**

Para conocer requisitos:

**`01_REQUISITOS_NAYRA.md` > decisiones del equipo > suposiciones de Claude**

Para conocer decisiones técnicas:

**`07_DECISIONES_TECNICAS_NAYRA.md` > documentación arquitectónica > suposiciones de Claude**

