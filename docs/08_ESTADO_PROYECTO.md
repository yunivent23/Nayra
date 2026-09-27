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

> **Actualización del 2026-09-27 (modelo de datos v4, sin commit, pendiente de revisión de Yuni):** migraciones Flyway
> V005–V011 del esquema `nayra` (documento DNI/CE, estados ACTIVO/BLOQUEADO/INACTIVO, `credenciales`, `codigo_qr`
> PROVISIONAL, solo ANDROID, `sesiones` con JWT `jti`, `operaciones`, `notificaciones`, permisos por servicio) y
> V001–V003 del esquema `biometria` (`perfiles_voz`, historial propio, en `Nayra-Voz/migraciones/biometria`).
> Persistencia del perfil de voz con psycopg. Implementado y probado contra PostgreSQL local; el extractor ECAPA real
> no se ejecutó, así que el CHECK de 784 bytes de `embedding_cifrado` no se aplicó. G-1 sigue sin decidir. Detalle en
> `/mnt/project-files/analisis/IMPLEMENTACION_MODELO_DATOS_V4_2026-09-27.md`.

---

## 2. Estado general

**Proyecto:** Nayra  
**Tipo:** Prototipo de solución móvil de autenticación mediante voz para usuarios con discapacidad visual en el contexto de billeteras digitales.

**Etapa actual:** Diseño y desarrollo del prototipo, correspondiente principalmente al alcance del Objetivo 2.

**Primer entregable (D-034):** prototipo funcional con aplicación móvil accesible por voz, autenticación contraseña + voz 1:1 + anti-spoofing, entorno financiero simulado (saldo, movimientos y transferencias directas a otros usuarios) y panel web del administrador. Registro **asistido** por un representante autorizado (D-052). El QR (HU-123, HU-124) queda para un siguiente entregable. Alcance por HU en `01_REQUISITOS_NAYRA.md` §12.4.

El short paper contempla hasta el Objetivo 2, incluyendo el diseño de la solución y parte del desarrollo.

### Estado documental

| Área | Estado |
|---|---|
| Contexto del proyecto | COMPLETADO |
| Requisitos | DOCUMENTADO — depurado por AG-00 y actualizado por AG-01 (HU-117 a HU-125; alcance del primer entregable; D1–D6 pendientes) |
| Arquitectura | EN DEFINICIÓN CONTROLADA — componentes y flujos lógicos del primer entregable documentados (AG-01) |
| Base de datos | MODELO LÓGICO DOCUMENTADO — AG-00 y AG-01 (`03_BASE_DE_DATOS_NAYRA.md` §16); esquema físico de las 6 tablas del núcleo IMPLEMENTADO en el árbol de trabajo (2026-09-27, sin commit) con Flyway, esquema `nayra` (D-051, D-009; `03` §17); `biometria` y demás tablas PENDIENTES |
| Biometría de voz | DISEÑO TÉCNICO APROBADO (AG-13, `05_BIOMETRIA_NAYRA.md` §27): modelo, anti-spoofing, reconocimiento del desafío, almacenamiento y comunicación decididos; **valores de umbral PENDIENTES DE VALIDACIÓN**; prototipo funcional EN DESARROLLO (2026-09-27, ver §3 y §5) |
| Seguridad | PRINCIPIOS Y CONTROLES DOCUMENTADOS — controles AG-01 en `06_SEGURIDAD_NAYRA.md` §36 |
| Decisiones técnicas | REGISTRADAS hasta D-061 (revisión del 2026-09-27; trazabilidad temática AG en `07_DECISIONES_TECNICAS_NAYRA.md`); siguen PENDIENTES, entre otras, D-014, D-016, D-018 (mecanismo), D-044 (detalle), D-045, D-046 (resto), D-047, D-055 (valores de umbral), D-060 |
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

**Estado de implementación (verificado en el árbol de trabajo el 2026-09-27, sin commit):** BACKEND GENERAL EN DESARROLLO, con persistencia en PostgreSQL para las 6 tablas del núcleo (sesiones siguen en memoria, D-018).

- **Retirado** por inseguro o no aprobado (`06` §36.7): CRUD de `Users`/`Role` con contraseña en texto plano y rol elegido por el cliente, `GET /usuarios` público, JWT (D-018), CORS abierto y reglas heredadas (`/bicicletas`, `/alquileres`), `ddl-auto=update` (ahora `validate` con migraciones Flyway, D-051). Las credenciales existentes **no** se tocaron (D-017).
- **Dominio según `03` §16**: `Usuario` (estado ACTIVA/BLOQUEADA, rol USER o ADMIN), `Dispositivos`, `Sesiones`, `Auditoria` (actor, usuario afectado, acción, resultado, motivo, dispositivo), `Cuentas`, `EntidadBancaria` y `RegistroIdentidadSimulado`. Mapeo JPA sobre el esquema `nayra` (`03` §17) con adaptadores PostgreSQL detrás de los mismos puertos de repositorio; los adaptadores en memoria quedan solo para pruebas unitarias. `Sesiones` sigue en memoria (PROVISIONAL, D-018). Datos **ficticios** del entorno simulado en `entorno-simulado/datos-ficticios.json`.
- **Registro inicial asistido (flujo de D-052)**: el representante proporciona el DNI y valida la identidad; la persona confirma sus datos, registra su celular, crea su PIN (solo hash), vincula la clave del dispositivo, enrola su voz (AG-13) y finaliza. Se vincula la cuenta financiera simulada por DNI. DNI ya registrado → rechazo auditado. **Provisional del prototipo** (no son decisiones de D-052): solo un ADMIN actúa como representante; el paso al celular de la persona usa un código de un solo uso (900 s); si la persona no confirma sus datos, el registro se cancela; formatos de DNI y celular.
- **Sesión**: se crea al autenticar por voz. Cierre tras 5 minutos de inactividad controlado en el servidor (aprobado, D-018), cierre de sesión (HU-13) y revocación al bloquear la cuenta o revocar el dispositivo (D-040). Token opaco aleatorio en `Authorization: Bearer`, del que solo se guarda el hash, sin duración máxima absoluta ni renovación: mecanismo **provisional** del prototipo; D-018 sigue parcial.
- **Autorización** (roles USER y ADMIN de D-041; mecanismo **provisional** mientras D-009 siga pendiente): `/api/v1/admin/**` solo ADMIN, `/api/v1/**` con sesión, `/prototipo/**` por la excepción del primer entregable, todo lo demás denegado; respuestas 401/403 en JSON.
- **Administración** (solo API; panel web pendiente, D-045): listar y buscar usuarios (HU-20, HU-21), detalle y estado (HU-22, HU-101), bloqueo y desbloqueo (HU-19), consulta y revocación del dispositivo (HU-125) y consultas de auditoría (HU-89, HU-80, HU-94). Toda acción queda auditada. Regla técnica **provisional** añadida (no proviene de D-041): un ADMIN no puede bloquearse ni revocar su propio dispositivo.
- **Usuario**: datos propios y estado (HU-09, HU-15).
- **Errores**: formato único `{"error": "CODIGO"}` sin datos sensibles.
- **Organización de la API**: `/api/v1/...` para la API general y `/prototipo/...` para voz y arranque. Es una propuesta **provisional**: el contrato sigue pendiente (D-014).
- Sin código: operaciones, notificaciones, solicitudes de atención, saldo y transferencias (clases vacías sin uso).
- Compila con JDK 21 (`mvn -Djava.version=21`; el `pom.xml` pide Java 25). Arranca sin `jwt.secret`.

**Prototipo AG-13 (verificado el 2026-09-27): EN DESARROLLO, integrado con el backend general.** Activado con el perfil `prototipo`:

- Implementado: nonce y firma ECDSA P-256 del dispositivo (D-048), PIN de 6 dígitos con hash y PIN dictado (D-061), desafío palabra + 3 dígitos + palabra con `SecureRandom` y un solo uso (D-054), cliente del servicio de voz (D-010), decisión final, intentos, bloqueo y auditoría (D-056), creación de la sesión al autenticar.
- AG-13 usa el usuario, el dispositivo, la sesión y la auditoría generales; el backend general no depende de AG-13. Java no accede a embeddings (D-013).
- Provisional: política de intentos propuesta en D-044, hash del PIN con BCrypt sin pepper (D-047; no cumple el pepper que exige D-061), rutas `/prototipo/...` (D-014), el ADMIN se autentica con el mismo flujo del usuario (D-050 pendiente) y el primer ADMIN se crea con una ruta de arranque del perfil `prototipo`, separada de D-052 y sin validación por representante.
- Pruebas Java: 68 ejecutadas, 0 fallos y 1 omitida (contrato, necesita el servicio Python en ejecución). 19 de ellas necesitan PostgreSQL y solo corren con `NAYRA_TEST_DB_URL` definida.

---

### 3.2 Backend Python

**Tecnología:** Python.

Responsabilidad:

- procesamiento especializado relacionado con voz;
- extracción/procesamiento de características de voz;
- integración con modelos preentrenados;
- biometría de voz;
- mecanismos de detección de intentos de suplantación, cuando hayan sido implementados.

**Estado (verificado el 2026-09-27):** PROTOTIPO EN DESARROLLO en `Nayra-Voz/` (FastAPI): audio, calidad con WebRTC VAD (candidata provisional), Vosk, AASIST, ECAPA-TDNN, cifrado AES-256-GCM del embedding, enrolamiento con centroide y transcripción del PIN dictado. 40 pruebas OK con modelos simulados y WebRTC VAD real; AASIST cargado y ejecutado con sus pesos publicados. ECAPA-TDNN y Vosk **no se ejecutaron** porque sus modelos no pudieron descargarse en el entorno de desarrollo. Referencias en memoria (estructura física de `biometria.perfiles_voz` pendiente) y parámetros PROVISIONALES.

Antes (2026-09-26): no existía código Python en el repositorio.

Revisión del 2026-09-27 (AG-13; D-010 y D-056 en AG-12): decididos FastAPI (D-010), SpeechBrain ECAPA-TDNN (D-011), AASIST (D-012), almacenamiento del embedding cifrado sin audio (D-013) y Vosk para el contenido del desafío (D-046, parcial). El servicio Python aplica los umbrales técnicos y Spring Boot decide la autenticación (D-056). Pendientes: contrato en `04_API.md` (D-014), valores de umbral (D-055) y despliegue (D-016).

---

### 3.3 Frontend / aplicación móvil

La tecnología definitiva del frontend todavía debe considerarse una decisión técnica pendiente si no ha sido aprobada formalmente.

**Estado:** POR DEFINIR. **No existe código de aplicación móvil ni de panel web en el repositorio** (verificado 2026-09-26). 2026-09-27: la aplicación móvil será **Flutter** (D-007), con canal Kotlin para el almacén de claves (D-048). El panel web sigue pendiente (D-045).

**Prototipo (verificado el 2026-09-27): EN DESARROLLO** en `Nayra-App/`: teclado de PIN accesible (solo anuncia el avance), PIN dictado, clave del dispositivo en el Android Keystore mediante canal Kotlin, desafío en pantalla y para el lector de pantalla, grabación manual WAV 16 kHz mono en memoria, registro en el celular con el código de registro (D-052), sesión tras autenticar, "Mis datos" y cierre de sesión. El tutorial (paso 12 de D-052) no está implementado porque su contenido no está definido. `flutter analyze` sin observaciones y 9 pruebas OK. **No se compiló para Android** (no hay Android SDK en el entorno de desarrollo), por lo que el canal Kotlin no está verificado.

Claude no debe asumir un framework de frontend sin consultar `07_DECISIONES_TECNICAS_NAYRA.md` y el repositorio.

---

## 4. Base de datos

### Tecnología

**PostgreSQL:** tecnología considerada para la base de datos relacional.

### Modelo CORE

El modelo documentado contempla, entre otras, las siguientes entidades:

- ROLES
- USUARIOS
- CUENTAS
- SESIONES
- DISPOSITIVOS
- OPERACIONES
- NOTIFICACIONES
- SOLICITUDES_ATENCION
- AUDITORÍA

Además, el modelo incluye las tablas de referencia del entorno simulado:

- ENTIDADES_BANCARIAS (AG-00, D-026)
- REGISTRO_IDENTIDAD_SIMULADO (AG-01, D-035)

El modelo lógico vigente de las tablas afectadas por AG-01 está en `03_BASE_DE_DATOS_NAYRA.md` §16.

El detalle oficial del modelo se encuentra en:

`03_BASE_DE_DATOS_NAYRA.md`

### Estado

**Modelo documentado:** SÍ.  
**Implementación física:** 6 tablas del núcleo en el esquema `nayra` (2026-09-27, sin commit): `entidades_bancarias`, `registro_identidad_simulado`, `usuarios`, `cuentas`, `dispositivos` y `auditoria`, creadas por migraciones Flyway V001–V004 con `ddl-auto=validate` (D-051, D-009; detalle en `03` §17). Sesiones, operaciones, solicitudes de atención, notificaciones y `biometria.perfiles_voz` siguen sin tabla física.

No deben agregarse tablas biométricas como `VOICE_BIOMETRICS`, `VOICE_EMBEDDINGS` o `ANTI_SPOOFING` sin una decisión explícita. _(AG-13: la única estructura biométrica aprobada es `biometria.PERFILES_VOZ`, D-013, `03_BASE_DE_DATOS_NAYRA.md` §16.11; el esquema y su historial de migraciones propio están decididos (D-051), pero su estructura física sigue pendiente.)_

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
- valores de umbral (D-055; primero un umbral provisional por piloto);
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

**Estado:** REQUISITOS Y PRINCIPIOS DOCUMENTADOS. Implementado en el árbol de trabajo (2026-09-27, sin commit): correcciones de `06` §36.7, autorización USER/ADMIN con denegación por defecto, PIN solo con hash, sesión con token del que solo se guarda el hash, firma del dispositivo contra replay, auditoría sin PIN, audio, embeddings ni secretos, y errores sin datos sensibles. Sigue pendiente: TLS, secretos (D-017), hash definitivo del PIN (D-047) y mecanismo de sesión (D-018).

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

### 7.3 Cloud

GCP se encuentra como tecnología/plataforma considerada.

La configuración física definitiva debe consultarse en la documentación de arquitectura y decisiones técnicas.

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
- no se incluyen apertura de cuentas, múltiples cuentas, transferencias entre cuentas propias ni gestión de entidades por el administrador.

---

## 9. Funcionalidades

### Implementadas

La lista de funcionalidades implementadas debe mantenerse sincronizada con el repositorio.

**Estado actual (verificado 2026-09-27, sin commit):** ninguna funcionalidad se considera terminada: todas dependen de piezas provisionales (sesiones en memoria, D-018; biometría en memoria). Ver «En desarrollo».

### En desarrollo

Deben registrarse aquí las funcionalidades que tengan código parcial pero que todavía no estén completas.

**Estado actual (2026-09-27, sin commit):** de extremo a extremo entre `Nayra-App/`, `Nayra-Back/` (perfil `prototipo`) y `Nayra-Voz/`, con el núcleo persistido en PostgreSQL (sesiones y biometría en memoria) y parámetros provisionales:

- registro inicial asistido con enrolamiento de voz (D-052; sin tutorial);
- inicio de sesión por dispositivo, PIN (tecleado o dictado), desafío y voz, con sesión y cierre por inactividad;
- datos propios, estado de la cuenta y cierre de sesión;
- API del administrador: usuarios, bloqueo, dispositivo y auditoría (sin panel web, D-045).

### Pendientes

Deben registrarse las funcionalidades que todavía no tengan una implementación funcional.

**Estado actual:** cambio de dispositivo y recuperación (D-049), solicitudes de atención y pérdida del celular (estados sin definir), actualización de datos (HU-10), saldo, movimientos y transferencias, tutorial, panel web del administrador (D-045) y persistencia en PostgreSQL de sesiones, operaciones y biometría. El QR queda para un siguiente entregable.

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

**Pruebas automatizadas (2026-09-27):** 68 pruebas Java ejecutadas con 0 fallos y 1 omitida (contrato con el servicio real), 40 pruebas Python y 9 pruebas Flutter OK. `contextLoads`, `ApiHttpTest` y `PersistenciaPostgresTest` necesitan PostgreSQL: corren solo con `NAYRA_TEST_DB_URL` y reconstruyen el esquema con Flyway (clean + migrate). Los modelos ECAPA-TDNN y Vosk se sustituyen por dobles de prueba.

**Pruebas de integración:** prueba de contrato Java ↔ Python y recorrido HTTP completo contra los servicios en ejecución (Spring Boot con PostgreSQL local encendido y FastAPI con modelos simulados): arranque del administrador, registro asistido, enrolamiento, inicio de sesión, autorización USER/ADMIN, bloqueo, desbloqueo, revocación del dispositivo, cierre de sesión y auditoría. 42 de 42 comprobaciones OK el 2026-09-27.

**Pruebas de biometría:** PENDIENTES DE IMPLEMENTACIÓN/VALIDACIÓN.  
**Pruebas de seguridad:** POR VERIFICAR.

---

## 11. Problemas y bloqueos

Los problemas deben registrarse cuando afecten el desarrollo.

Formato recomendado:

| ID | Problema | Impacto | Estado | Responsable |
|---|---|---|---|---|
| P-001 | Credenciales de PostgreSQL en texto plano en `application.properties`; secretos (`jwt.secret`, clave de API) presentes en el historial de Git | Seguridad — **deuda técnica** (D-017, categoría C) | ABIERTO — no bloquea el primer entregable; se corrige antes de producción (segundo entregable) | Equipo |
| P-002 | `POST /usuarios` guardaba contraseñas en texto plano y permitía que el cliente eligiera su rol | Seguridad crítica | RESUELTO en el árbol de trabajo (2026-09-27, sin commit): CRUD retirado; el rol lo fija el registro asistido | — |
| P-003 | `GET /usuarios` era público y exponía datos personales y el campo contraseña | Seguridad crítica | RESUELTO en el árbol de trabajo (2026-09-27, sin commit): ruta retirada; la consulta de usuarios exige ADMIN | — |
| P-004 | Mecanismo JWT implementado sin aprobación (D-018 pendiente) | Alto | RESUELTO en el árbol de trabajo (2026-09-27, sin commit): JWT retirado; sesión con token opaco **provisional** hasta decidir D-018 | — |
| P-005 | Código heredado ajeno a Nayra (reglas `/bicicletas`, `/alquileres`, comentarios de plantilla) y CORS abierto | Medio | RESUELTO en el árbol de trabajo (2026-09-27, sin commit) | — |
| P-006 | `ddl-auto=update` creaba el esquema sin estrategia de migraciones (D-051) | Alto | RESUELTO en el árbol de trabajo (2026-09-27, sin commit): Flyway y `ddl-auto=validate` (D-051) | — |
| P-007 | Posible incompatibilidad de versiones en `pom.xml` (Spring Boot 4.1.1 con `spring-boot-starter-security` 3.5.5) | Alto | 2026-09-27: la versión fija de seguridad se retiró (la gestiona Spring Boot); compila y arranca con JDK 21 (`-Djava.version=21`); el `pom.xml` pide Java 25 | — |
| P-009 | La aplicación no arrancaba sin la propiedad `jwt.secret` | Medio | RESUELTO en el árbol de trabajo (2026-09-27, sin commit): al retirar JWT ya no se necesita | — |
| P-008 | Modelo `Users` no correspondía al aprobado | Medio | RESUELTO en el árbol de trabajo (2026-09-27, sin commit): `Usuario` según `03` §16.1, mapeado a `nayra.usuarios` (`03` §17.3) | — |

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
4. Modelo de datos v1 con migraciones y datos simulados.
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

