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
| Base de datos | MODELO LÓGICO DOCUMENTADO — AG-00 y AG-01 (`03_BASE_DE_DATOS_NAYRA.md` §16); esquema físico y migraciones PENDIENTES (D-051) |
| Biometría de voz | DISEÑO TÉCNICO APROBADO (AG-02, `05_BIOMETRIA_NAYRA.md` §27): modelo, anti-spoofing, reconocimiento del desafío, almacenamiento y comunicación decididos; **valores de umbral PENDIENTES DE VALIDACIÓN**; implementación NO INICIADA |
| Seguridad | PRINCIPIOS Y CONTROLES DOCUMENTADOS — controles AG-01 en `06_SEGURIDAD_NAYRA.md` §36 |
| Decisiones técnicas | REGISTRADAS hasta D-061 (AG-02); siguen PENDIENTES, entre otras, D-014, D-016, D-018 (mecanismo), D-044 (detalle), D-045, D-046 (resto), D-047, D-055 (valores de umbral), D-060 |
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

**Estado de implementación (verificado en el repositorio, commit `127a601`, 2026-09-26):** ESQUELETO PARCIAL. Sin compilación ni ejecución verificadas.

- Existe el proyecto Spring Boot (`Nayra-Back/`).
- `Users` y `Role` están implementados como entidades JPA, con repositorio, servicio y `UsuarioController` (CRUD). El modelo **no** corresponde al aprobado (`03_BASE_DE_DATOS_NAYRA.md` §16).
- Hay una configuración de Spring Security con filtro JWT: **no aprobada** (D-018), sin endpoint de inicio de sesión.
- `Cuentas`, `Sesiones`, `Dispositivos`, `Operaciones`, `Notificaciones`, `Solicitudes_atencion` y `Auditoria` son **clases vacías**, con repositorios y servicios vacíos.
- Ninguna funcionalidad de negocio de Nayra está implementada.
- Existen problemas de seguridad que deben corregirse antes de nuevas funcionalidades (ver §11 y `06_SEGURIDAD_NAYRA.md` §36.7).

---

### 3.2 Backend Python

**Tecnología:** Python.

Responsabilidad:

- procesamiento especializado relacionado con voz;
- extracción/procesamiento de características de voz;
- integración con modelos preentrenados;
- biometría de voz;
- mecanismos de detección de intentos de suplantación, cuando hayan sido implementados.

**Estado:** DISEÑO TÉCNICO APROBADO / IMPLEMENTACIÓN NO INICIADA. **No existe código Python en el repositorio** (verificado 2026-09-26).  
AG-02 (2026-09-27): decididos FastAPI (D-010), SpeechBrain ECAPA-TDNN (D-011), AASIST (D-012), almacenamiento del embedding cifrado sin audio (D-013) y Vosk para el contenido del desafío (D-046, parcial). El servicio Python aplica los umbrales técnicos y Spring Boot decide la autenticación (D-056). Pendientes: contrato en `04_API.md` (D-014), valores de umbral (D-055) y despliegue (D-016).

---

### 3.3 Frontend / aplicación móvil

La tecnología definitiva del frontend todavía debe considerarse una decisión técnica pendiente si no ha sido aprobada formalmente.

**Estado:** POR DEFINIR. **No existe código de aplicación móvil ni de panel web en el repositorio** (verificado 2026-09-26). AG-02: la aplicación móvil será **Flutter** (D-007), con canal Kotlin para el almacén de claves (D-048); implementación no iniciada. El panel web sigue pendiente (D-045).

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
**Implementación física:** POR VERIFICAR EN EL REPOSITORIO. Las decisiones de AG-00 (D-024 a D-033) son solo documentales: **no se han implementado** en el código.

No deben agregarse tablas biométricas como `VOICE_BIOMETRICS`, `VOICE_EMBEDDINGS` o `ANTI_SPOOFING` sin una decisión explícita. _(AG-02: la única estructura biométrica aprobada es `biometria.PERFILES_VOZ`, D-013, `03_BASE_DE_DATOS_NAYRA.md` §16.11; su tabla física depende de D-051.)_

---

## 5. Biometría de voz

La biometría de voz constituye uno de los componentes centrales de Nayra.

### Definiciones actuales

- Se utilizarán modelos preentrenados.
- Python será utilizado para el procesamiento especializado.
- SpeechBrain ECAPA-TDNN aprobado para la verificación 1:1 (AG-02, D-011).
- El objetivo conceptual es realizar **verificación de voz**, no identificación abierta.
- Se considera protección frente a intentos de suplantación mediante reproducción o voz sintética/manipulada.
- AG-01: enrolamiento durante el registro con anti-spoofing; verificación 1:1 tras la contraseña; desafío variable con comprobación de contenido; referencia biométrica persistente en el backend (D-036 a D-038, `05_BIOMETRIA_NAYRA.md` §26).

### Decisiones todavía pendientes

- ~~modelo definitivo~~ (D-011), ~~algoritmo de comparación~~ (coseno 1:1, D-011), ~~estrategia de almacenamiento~~ (D-013), ~~retención de audio~~ (no se guarda audio, D-013), ~~anti-spoofing~~ (D-012), ~~integración con Java~~ (D-010), ~~responsable de aplicar los umbrales técnicos~~ (servicio Python, D-056) — resueltos en AG-02;
- valores de umbral (D-055; primero un umbral provisional por piloto);
- dataset de calibración y consentimiento (D-060);
- herramienta de VAD (D-058), lista de palabras y vida del desafío (D-054);
- resto del reconocimiento del habla: comando y PIN dictado (D-046).

**Estado:** DISEÑO TÉCNICO APROBADO / IMPLEMENTACIÓN NO INICIADA / VALIDACIÓN PENDIENTE.

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

**Estado:** REQUISITOS Y PRINCIPIOS DOCUMENTADOS; IMPLEMENTACIÓN POR VERIFICAR.

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

**Estado actual (verificado 2026-09-26):** ninguna funcionalidad de Nayra implementada.

### En desarrollo

Deben registrarse aquí las funcionalidades que tengan código parcial pero que todavía no estén completas.

**Estado actual:** código parcial no alineado con las decisiones vigentes: modelo `Users`/`Role`, CRUD de usuarios y configuración de seguridad JWT (no aprobada). Requieren revisión antes de reutilizarse.

### Pendientes

Deben registrarse las funcionalidades que todavía no tengan una implementación funcional.

**Estado actual:** todas las funcionalidades del primer entregable (`01_REQUISITOS_NAYRA.md` §12.4): registro, enrolamiento, inicio de sesión, dispositivo, recuperación, saldo, movimientos, transferencias, QR, tutorial, panel del administrador y auditoría.

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

**Pruebas automatizadas:** solo existe `NayraBackendApplicationTests.contextLoads`; no se ha verificado que se ejecute correctamente.  
**Pruebas de integración:** POR VERIFICAR.  
**Pruebas de biometría:** PENDIENTES DE IMPLEMENTACIÓN/VALIDACIÓN.  
**Pruebas de seguridad:** POR VERIFICAR.

---

## 11. Problemas y bloqueos

Los problemas deben registrarse cuando afecten el desarrollo.

Formato recomendado:

| ID | Problema | Impacto | Estado | Responsable |
|---|---|---|---|---|
| P-001 | Credenciales de PostgreSQL en texto plano en `application.properties`; secretos (`jwt.secret`, clave de API) presentes en el historial de Git | Seguridad — **deuda técnica** (D-017, categoría C) | ABIERTO — no bloquea el primer entregable; se corrige antes de producción (segundo entregable) | Equipo |
| P-002 | `POST /usuarios` guarda contraseñas en texto plano y permite que el cliente elija su rol | Seguridad crítica | ABIERTO | — |
| P-003 | `GET /usuarios` es público y expone datos personales y el campo contraseña | Seguridad crítica | ABIERTO | — |
| P-004 | Mecanismo JWT implementado sin aprobación (D-018 pendiente), sin endpoint de inicio de sesión | Alto | ABIERTO (AG-02: D-018 solo aprobó el cierre automático tras 5 min de inactividad, sin aviso previo; el mecanismo sigue pendiente) | — |
| P-005 | Código heredado ajeno a Nayra (reglas `/bicicletas`, `/alquileres`, comentarios de plantilla) y CORS abierto | Medio | ABIERTO | — |
| P-006 | `ddl-auto=update` crea el esquema sin estrategia de migraciones (D-051) | Alto | ABIERTO | — |
| P-007 | Posible incompatibilidad de versiones en `pom.xml` (Spring Boot 4.1.1 con `spring-boot-starter-security` 3.5.5); compilación sin verificar | Alto | POR VERIFICAR | — |
| P-008 | Modelo `Users` no corresponde al aprobado (faltan nombres, apellidos y estado; sobran campos sin HU) | Medio | ABIERTO | — |

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

