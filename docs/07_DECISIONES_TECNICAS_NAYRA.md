# 07_DECISIONES_TECNICAS_NAYRA.md — Decisiones técnicas de Nayra

## 1. Propósito

Este documento registra las decisiones técnicas que afectan el diseño, desarrollo, integración y despliegue de Nayra.

Su objetivo principal es evitar que durante la implementación se tomen decisiones importantes de manera implícita o contradictoria.

Claude debe consultar este documento antes de seleccionar tecnologías, modificar componentes o establecer mecanismos que todavía no hayan sido definidos.

---

# 2. Estados de una decisión

Cada decisión debe tener uno de estos estados:

- **APROBADA:** decisión aceptada por el equipo y lista para ser utilizada.
- **PENDIENTE:** todavía no existe una decisión definitiva.
- **EN EVALUACIÓN:** existen alternativas que están siendo analizadas.
- **DESCARTADA:** alternativa evaluada y no seleccionada.
- **REEMPLAZADA:** decisión que anteriormente estuvo aprobada pero fue sustituida.

Claude no debe tratar una decisión `PENDIENTE`, `EN EVALUACIÓN`, `DESCARTADA` o `REEMPLAZADA` como una decisión vigente.

---

# 3. Principios para tomar decisiones

Las decisiones técnicas deberán considerar:

1. requisitos del sistema;
2. alcance de la tesis;
3. seguridad;
4. accesibilidad;
5. complejidad;
6. mantenibilidad;
7. costo;
8. disponibilidad de herramientas;
9. facilidad de implementación;
10. capacidad de validación;
11. coherencia con la arquitectura;
12. trazabilidad académica.

La solución elegida no debe ser innecesariamente compleja para el alcance del proyecto.

---

# 4. Decisiones actualmente consideradas

## D-001 — Backend principal

**Estado:** APROBADA / POR VALIDAR EN DOCUMENTACIÓN FINAL

**Decisión:** utilizar Java como tecnología del backend principal.

**Framework considerado:** Spring Boot.

**Motivo:** permite estructurar el backend mediante capas y componentes claramente diferenciados y cuenta con herramientas para APIs, seguridad, persistencia y pruebas.

**Alcance:** lógica principal del sistema.

**Documentos relacionados:**

- `02_ARQUITECTURA_NAYRA.md`
- `01_REQUISITOS_NAYRA.md`

---

## D-002 — Procesamiento especializado de voz

**Estado:** APROBADA COMO ENFOQUE / IMPLEMENTACIÓN PENDIENTE

**Decisión:** utilizar Python para el procesamiento especializado de voz.

**Motivo:** permite utilizar frameworks y modelos especializados para procesamiento y biometría de voz.

**Alcance:** procesamiento biométrico y funcionalidades relacionadas con voz que se asignen al componente especializado.

**Nota:** la división exacta de responsabilidades entre Java y Python todavía debe definirse.

---

## D-003 — Base de datos relacional

**Estado:** APROBADA COMO TECNOLOGÍA CONSIDERADA

**Decisión:** utilizar PostgreSQL como base de datos relacional de referencia.

**Motivo:** el modelo CORE definido en los requerimientos presenta información estructurada y relacional.

**Nota:** la configuración, versión y estrategia de despliegue todavía deben definirse.

---

## D-004 — Modelos de inteligencia artificial

**Estado:** APROBADA COMO PRINCIPIO

**Decisión:** utilizar modelos preentrenados, frameworks especializados o APIs existentes en lugar de entrenar modelos desde cero como estrategia principal.

**Motivo:** el alcance del proyecto se centra en diseñar e implementar la solución, no en desarrollar desde cero un modelo de IA.

---

## D-005 — Framework para biometría de voz

**Estado:** APROBADA — concretada en **D-011** (AG-13, 2026-09-27)

**Actualización AG-13:** SpeechBrain queda seleccionado mediante el modelo concreto definido en D-011. El texto original se conserva para trazabilidad.

**Tecnología considerada:** SpeechBrain.

**Motivo:** proporciona herramientas y modelos relacionados con procesamiento y reconocimiento de voz.

**Nota:** todavía debe evaluarse el modelo concreto, compatibilidad, desempeño y estrategia de integración.

No debe considerarse que SpeechBrain constituye automáticamente la decisión final del modelo biométrico.

---

## D-006 — Plataforma cloud

**Estado:** EN EVALUACIÓN / CONSIDERADA

**Tecnología considerada:** Google Cloud Platform (GCP).

**Motivo:** ha sido considerada como plataforma para el despliegue de la solución.

**Nota importante:** no se ha aprobado mediante este documento ninguna arquitectura específica de GCP.

No asumir automáticamente:

- número de instancias;
- regiones;
- zonas;
- VPC;
- subredes;
- DMZ;
- API Gateway;
- balanceador;
- firewall;
- servicios administrados;
- almacenamiento específico.

---

# 5. Decisiones todavía pendientes

## D-007 — Tecnología del frontend

**Estado:** APROBADA para la aplicación móvil: **Flutter** (2026-09-27; sin AG temático). El panel web del administrador sigue PENDIENTE (D-045).

**Actualización del 2026-09-27:** la aplicación móvil se desarrolla con **Flutter**. El código nativo necesario para el almacén de claves del dispositivo (D-048) se implementa mediante un canal de plataforma en Kotlin. Las librerías concretas de accesibilidad, audio y TTS no quedan aprobadas por esta decisión y deben cumplir los criterios de D-057 (formato de audio) y de las HUs de accesibilidad. Texto original:

Debe seleccionarse la tecnología definitiva para la aplicación móvil.

**Nota AG-01:** la aplicación móvil es el componente principal del usuario en el primer entregable (D-034). La tecnología del **panel web del administrador** se registra por separado en D-045 (PENDIENTE).

La selección deberá considerar:

- accesibilidad;
- compatibilidad con autenticación por voz;
- integración con APIs;
- facilidad de desarrollo;
- mantenimiento;
- capacidad de pruebas.

---

## D-008 — Mecanismo de autenticación

**Estado:** APROBADA FUNCIONALMENTE PARA EL USUARIO (ver D-037) / PENDIENTE EN LO TÉCNICO

La autenticación del usuario queda definida funcionalmente en **D-037** (contraseña + desafío de voz variable + anti-spoofing + verificación biométrica 1:1). Siguen **pendientes**: la autenticación del administrador (D-050), el hash y la política de contraseña (D-047), el reconocimiento del habla (D-046), la estrategia de sesiones (D-018) y los mecanismos técnicos concretos.

**Actualización del 2026-09-27:** la credencial de conocimiento es un **PIN de 6 dígitos** (D-061, modifica D-037); el dispositivo se vincula con un **par de claves** (D-048); la sesión se cierra tras **5 minutos de inactividad** (D-018, parcial); el contenido del desafío se reconoce en el servidor (D-046, parcial). Siguen pendientes: hash del PIN (D-047), mecanismo técnico de sesión (D-018), resto del reconocimiento del habla (D-046) y autenticación del administrador (D-050).

**Actualización del modelo de datos v4 (2026-09-27):** la sesión usa JWT con `jti` = `sesiones.id` (D-018; P-5 pendiente) y D-050 queda aprobada funcionalmente (usuario y contraseña en el panel web), sin implementar. Siguen pendientes D-047, el resto de D-046, P-5 y el modelo de la credencial administrativa.

Texto original de esta decisión (se mantiene para lo que sigue pendiente):

Debe definirse el mecanismo mediante el cual el sistema gestiona la autenticación general.

No asumir automáticamente:

- JWT;
- OAuth;
- sesiones tradicionales;
- tokens propios;
- autenticación multifactor.

La biometría de voz es un componente del proceso de autenticación, pero no define por sí sola toda la estrategia de sesión y seguridad.

---

## D-009 — Autorización

**Estado:** APROBADA (2026-09-27, AG-03) — representación de los roles: **opción A, valor fijo** (ver «Decisiones aprobadas el 2026-09-27 — Modelo físico de la base de datos»). El mecanismo de autorización por rutas del prototipo sigue como mecanismo provisional (tabla «Mecanismos técnicos provisionales del prototipo»).

Texto anterior (se conserva para trazabilidad): **PARCIAL — roles aprobados en D-041; mecanismo técnico PENDIENTE.**

Los roles del sistema quedan fijados en **D-041** (USER y ADMIN). Siguen pendientes la representación de roles y permisos en el modelo de datos y el punto de aplicación técnica de la autorización.

Debe definirse el mecanismo mediante el cual se controlarán los permisos de los usuarios y administradores.

Debe mantener correspondencia con:

- roles;
- requisitos;
- backend;
- seguridad.

---

## D-010 — Comunicación Java ↔ Python

**Estado:** APROBADA (AG-12, 2026-09-27) — ver detalle en la sección de decisiones del 2026-09-27. Autenticación entre servicios en la nube PENDIENTE (D-016).

**Actualización AG-12:** API REST interna con FastAPI en el servicio Python, accesible solo desde la red interna y únicamente por el backend principal. Texto original:

Debe definirse cómo se comunicará el backend principal con el componente especializado de voz.

Alternativas posibles deberán evaluarse antes de seleccionar una:

- API REST;
- otro mecanismo de comunicación;
- otra alternativa técnicamente justificada.

No implementar una alternativa como definitiva sin documentarla aquí.

---

## D-011 — Modelo biométrico

**Estado:** APROBADA (selección) / desempeño PENDIENTE DE VALIDACIÓN (AG-13, 2026-09-27) — ver sección de decisiones del 2026-09-27.

Texto original:

Debe definirse:

- modelo;
- representación de voz;
- método de comparación;
- estrategia de verificación;
- requisitos de rendimiento.

La selección debe basarse en la investigación realizada y en la capacidad de validación dentro del alcance del proyecto.

---

## D-012 — Modelo anti-spoofing

**Estado:** APROBADA (estrategia) / tasa de detección PENDIENTE DE VALIDACIÓN (AG-13, 2026-09-27) — ver sección de decisiones del 2026-09-27.

Texto original:

Debe definirse la estrategia concreta para detectar intentos de spoofing.

Se deben considerar las amenazas identificadas en `05_BIOMETRIA_NAYRA.md`.

---

## D-013 — Almacenamiento de información biométrica

**Estado:** APROBADA (AG-13, 2026-09-27) — ver sección de decisiones del 2026-09-27. Gestión de la clave de cifrado vinculada a D-017.

Texto original:

Debe definirse si el sistema conservará:

- audio original;
- características;
- embeddings/representaciones;
- metadatos;
- únicamente resultados de verificación.

No asumir que el audio se almacenará en PostgreSQL.

---

## D-014 — API

**Estado:** PENDIENTE

La especificación de endpoints se definirá después de cerrar las decisiones arquitectónicas y de integración.

El contrato de API deberá documentarse posteriormente en:

`04_API.md`

---

## D-015 — Arquitectura física

**Estado:** PENDIENTE

**Actualización 2026-09-29:** la topología objetivo en GCP queda aprobada en D-063 (etapa futura, no desplegada).

La arquitectura física definitiva todavía debe definirse.

No asumir ninguna de las arquitecturas anteriormente descartadas.

La arquitectura aprobada deberá registrarse en `02_ARQUITECTURA_NAYRA.md`.

---

## D-016 — Despliegue cloud

**Estado:** PENDIENTE

**Actualización 2026-09-29:** región, topología, máquinas, migraciones, dominio y contenedores quedan aprobados en D-062 a D-074 (etapa futura, no desplegada). Monitoreo y almacenamiento siguen pendientes.

Debe definirse:

- servicios GCP;
- redes;
- seguridad;
- almacenamiento;
- ejecución de backend;
- ejecución del componente Python;
- base de datos;
- monitoreo;
- estrategia de despliegue.

---

## D-017 — Gestión de secretos

**Estado:** DEUDA TÉCNICA / PENDIENTE DE SEGURIDAD (categoría C, no bloqueante para el primer entregable — AG-01 v5, 2026-09-26)

**Actualización AG-01 v5:** la exposición actual de credenciales, claves JWT, configuración de PostgreSQL y otros valores sensibles en el repositorio (incluido su historial de Git) se reconoce como **deuda técnica**. **No** detiene ni bloquea el desarrollo funcional del primer entregable. Durante esta etapa:

- **no** se eliminan automáticamente las credenciales actuales;
- **no** se limpia el historial de Git;
- **no** se rotan credenciales solo por esta revisión.

La corrección se retomará **antes de cualquier despliegue en producción**, previsiblemente en el **segundo entregable** (endurecimiento de seguridad). En ese momento se evaluará una estrategia para: variables de entorno, gestión de secretos, rotación de credenciales, claves JWT, credenciales de base de datos, limpieza del historial si fuera necesaria y separación entre configuración de desarrollo y producción.

La regla general de no introducir **nuevos** secretos en el código sigue vigente.

Texto original de esta decisión:

Debe definirse el mecanismo para almacenar:

- credenciales;
- claves;
- tokens;
- secretos de servicios;
- credenciales de base de datos.

Nunca deberán incluirse directamente en el código fuente.

---

## D-018 — Estrategia de sesiones

**Estado:** PARCIAL — regla de **cierre tras 5 minutos de inactividad** APROBADA (AG-02, 2026-09-27); **mecanismo JWT con `jti` y tabla `sesiones` APROBADO** (modelo de datos v4, 2026-09-27); `exp`, claims definitivos, algoritmo definitivo, custodia de la clave de firma y número máximo de sesiones simultáneas PENDIENTES (**P-5**).

**Actualización del modelo de datos v4 (2026-09-27):** la sesión se representa con un **JWT** cuyo único claim propio es `jti`, igual a `nayra.sesiones.id`. El JWT **no** se guarda (ni su hash); la validez depende de la fila de `sesiones` (6 columnas: `id`, `usuario_id`, `dispositivo_id`, `fecha_creacion`, `fecha_ultimo_acceso`, `fecha_revocacion`). Cierre tras 5 minutos sin actividad; **sin duración máxima absoluta, sin renovación y sin refresh token**. El servicio de autenticación es la autoridad de la sesión. La revocación y el registro de acceso usan UPDATE condicionales, de modo que una sesión revocada no puede reabrirse por una escritura obsoleta (corrección E-02; no se añadió `@Version` porque `sesiones` mantiene 6 columnas). En el prototipo el JWT es HS256 con clave leída de `NAYRA_JWT_CLAVE` (PROVISIONAL, P-5 y D-017). Detalle en `03_BASE_DE_DATOS_NAYRA.md` §17 y en «Decisiones del modelo de datos v4».

**Actualización AG-02:** la sesión se **cierra automáticamente** tras **5 minutos sin actividad**. El control de la inactividad se realiza en el servidor. **No** hay aviso por voz previo al cierre ni opción de continuar, y el tiempo no es ajustable por el usuario. No se aprueba todavía JWT, token opaco ni otro mecanismo; el análisis A-K (punto I) queda como insumo. Texto original:

Debe definirse:

- duración;
- expiración;
- invalidación;
- renovación;
- almacenamiento;
- cierre de sesión.

---

## D-019 — Estrategia de auditoría

**Estado:** PENDIENTE

Debe definirse qué eventos deben registrarse en la tabla `AUDITORÍA` y qué información puede conservarse sin exponer datos sensibles.

---

## D-020 — Estrategia de disponibilidad

**Estado:** PENDIENTE

La existencia de **2 instancias de aplicación ubicadas en zonas diferentes** ya está aprobada mediante D-023.

Sin embargo, la estrategia integral de disponibilidad continúa pendiente y deberá definir, entre otros aspectos:

- distribución del tráfico;
- comportamiento ante la caída de una instancia;
- monitoreo;
- recuperación;
- criterios de disponibilidad;
- componentes adicionales de infraestructura, si fueran necesarios.

No asumir automáticamente regiones adicionales, balanceadores u otros mecanismos de alta disponibilidad sin una decisión técnica aprobada.

**Actualización 2026-09-29:** la distribución del tráfico en GCP (API Gateway y balanceador regional hacia un grupo de instancias en 2 zonas) y los costos por fases quedan aprobados en D-063. La recuperación, el monitoreo y los criterios de disponibilidad siguen pendientes.

**Aclaración (2026-09-27):** la separación en tres servicios del modelo de datos v4 (Negocio, Autenticación y Biométrico) es una separación **lógica, por responsabilidad**. No sustituye ni reinterpreta las 2 instancias de D-023, y "3 servicios" no significa "3 réplicas".

---

# 6. Arquitecturas anteriores descartadas

Las propuestas arquitectónicas realizadas durante etapas anteriores del proyecto y posteriormente observadas como incorrectas o descartadas **no constituyen decisiones técnicas vigentes**.

Por tanto, deben considerarse:

**Estado: DESCARTADA / NO VIGENTE**

Claude no debe utilizar esas propuestas para:

- generar infraestructura;
- crear componentes;
- diseñar APIs;
- crear redes;
- crear servidores;
- establecer relaciones entre servicios;
- implementar código.

Una arquitectura nueva solo será considerada vigente cuando esté documentada y aprobada en `02_ARQUITECTURA_NAYRA.md`.

---

# 7. Registro de cambios

Cuando una decisión cambie, no se debe modificar silenciosamente la historia de la decisión.

Se recomienda registrar:

| ID | Decisión | Estado anterior | Nuevo estado | Motivo | Fecha |
|---|---|---|---|---|---|
| D-XXX | Pendiente | PENDIENTE | APROBADA | Por definir | YYYY-MM-DD |

Registro:

| ID | Decisión | Estado anterior | Nuevo estado | Motivo | Fecha |
|---|---|---|---|---|---|
| D-024 | Terminología: cuenta de acceso / cuenta financiera | — (nueva) | APROBADA | AG-00: eliminar la ambigüedad del término "cuenta" | 2026-09-25 |
| D-025 | Una única cuenta financiera por usuario | — (nueva) | APROBADA | AG-00: simplificación del modelo; se descartó el modelo de varias cuentas por usuario | 2026-09-25 |
| D-026 | `ENTIDADES_BANCARIAS` como catálogo de referencia (T1: `id`, `nombre`) | — (nueva) | APROBADA | AG-00: representar las entidades bancarias del entorno simulado (D-021) | 2026-09-25 |
| D-027 | Mantener el nombre de la tabla `CUENTAS` | — (nueva) | APROBADA | AG-00 | 2026-09-25 |
| D-028 | Asociación de la cuenta financiera por DNI (A1) | — (nueva) | APROBADA | AG-00 | 2026-09-25 |
| D-029 | `OPERACIONES` referencia cuenta financiera de origen y de destino | — (nueva) | APROBADA | AG-00 | 2026-09-25 |
| D-030 | Transferencias sin selección de cuenta | — (nueva) | APROBADA | AG-00: consecuencia de D-025 | 2026-09-25 |
| D-031 | Depuración de HUs (eliminación, consolidación, sin renumeración) | — (nueva) | APROBADA | AG-00 | 2026-09-25 |
| D-032 | El registro de Nayra incluye el registro de voz | — (nueva) | APROBADA | AG-00 | 2026-09-25 |
| D-033 | Exclusiones de alcance del modelo financiero | — (nueva) | APROBADA | AG-00 | 2026-09-25 |
| D-008 | Mecanismo de autenticación | PENDIENTE | APROBADA FUNCIONALMENTE PARA EL USUARIO (D-037) / técnico PENDIENTE | AG-01 | 2026-09-26 |
| D-009 | Autorización | PENDIENTE | PARCIAL (roles en D-041) | AG-01 | 2026-09-26 |
| D-034 | Alcance del primer entregable (prototipo funcional) | — (nueva) | APROBADA | AG-01 | 2026-09-26 |
| D-035 | Fuente de identidad simulada | — (nueva) | APROBADA | AG-01: sin API real de terceros; resuelve el pendiente de D-028 | 2026-09-26 |
| D-036 | Flujo de registro del usuario | — (nueva) | APROBADA | AG-01 | 2026-09-26 |
| D-037 | Autenticación del usuario: contraseña + voz 1:1 + anti-spoofing | — (nueva) | APROBADA FUNCIONALMENTE | AG-01 | 2026-09-26 |
| D-038 | Referencia biométrica persistente en backend | — (nueva) | APROBADA COMO PRINCIPIO | AG-01 | 2026-09-26 |
| D-039 | Un único dispositivo activo por cuenta de acceso | — (nueva) | APROBADA | AG-01 | 2026-09-26 |
| D-040 | Cambio de dispositivo, pérdida y recuperación | — (nueva) | APROBADA (recuperación asistida PENDIENTE, D-049) | AG-01 | 2026-09-26 |
| D-041 | Roles USER y ADMIN; límites del administrador | — (nueva) | APROBADA | AG-01 | 2026-09-26 |
| D-042 | Transferencias a usuarios de Nayra y QR | — (nueva) | APROBADA | AG-01 | 2026-09-26 |
| D-043 | Número de celular como dato de contacto; sin OTP/SMS | — (nueva) | APROBADA | AG-01 | 2026-09-26 |
| D-044 | Política de 3 intentos de autenticación | — (nueva) | APROBADA (valor) / detalle PENDIENTE | AG-01 | 2026-09-26 |
| D-045 a D-051 | Decisiones técnicas abiertas por AG-01 | — (nuevas) | PENDIENTE | AG-01 | 2026-09-26 |
| D-052 | Registro asistido por representante autorizado (regla de negocio) | — (nueva) | APROBADA | AG-01 v5: la consulta del DNI no prueba la identidad; sin nuevo rol | 2026-09-26 |
| D-036 | Flujo de registro del usuario | APROBADA | APROBADA — MODIFICADA POR D-052 | AG-01 v5 | 2026-09-26 |
| D-052 | Registro asistido | APROBADA | APROBADA — flujo actualizado (13 pasos; representante = administrador o persona autorizada; la persona confirma sus datos; celular en el paso 8) | AG-01 v6 | 2026-09-26 |
| D-053 | Flujos diferenciados y usos del DNI | — (nueva) | APROBADA | AG-01 v6: el DNI no forma parte del inicio de sesión habitual | 2026-09-26 |
| D-017 | Gestión de secretos | PENDIENTE | DEUDA TÉCNICA (categoría C) | AG-01 v5: no bloquea el primer entregable; se aborda antes de producción | 2026-09-26 |
| D-042 | QR (HU-123, HU-124) | APROBADA | APROBADA — fuera de la implementación del primer entregable | AG-01 v5: siguiente entregable | 2026-09-26 |
| D-005 | Framework para biometría de voz | EN EVALUACIÓN | APROBADA — concretada en D-011 | AG-13: revisión del 2026-09-27 | 2026-09-27 |
| D-007 | Tecnología de la aplicación móvil | PENDIENTE | APROBADA (Flutter); panel web sigue en D-045 | Revisión del 2026-09-27 (sin AG temático) | 2026-09-27 |
| D-010 | Comunicación Java ↔ Python | PENDIENTE | APROBADA (REST interno, FastAPI) | AG-12 | 2026-09-27 |
| D-011 | Modelo biométrico | PENDIENTE | APROBADA (SpeechBrain ECAPA-TDNN); desempeño por validar | AG-13 | 2026-09-27 |
| D-012 | Anti-spoofing | PENDIENTE | APROBADA (defensa en capas con AASIST); detección por validar | AG-13 | 2026-09-27 |
| D-013 | Almacenamiento biométrico | PENDIENTE | APROBADA (solo embedding cifrado, sin audio) | AG-13 | 2026-09-27 |
| D-018 | Estrategia de sesiones | PENDIENTE | PARCIAL (5 minutos de inactividad aprobados; mecanismo pendiente) | AG-02 | 2026-09-27 |
| D-046 | Reconocimiento del habla | PENDIENTE | PARCIAL (contenido del desafío aprobado) | AG-13 (parte del desafío) | 2026-09-27 |
| D-048 | Vinculación del dispositivo | PENDIENTE | APROBADA (par de claves del dispositivo) | Origen histórico: AG-01. Aprobada el 2026-09-27 (revisión del 2026-09-27). Impacto secundario: AG-11 (anti-replay) | 2026-09-27 |
| D-037 | Autenticación del usuario | APROBADA FUNCIONALMENTE | APROBADA FUNCIONALMENTE — MODIFICADA POR D-061 (PIN de 6 dígitos) | Revisión del 2026-09-27: modificada por D-061 (D-061: origen AG-01; aprobada el 2026-09-27) | 2026-09-27 |
| D-054, D-057 a D-059 | Decisiones nuevas del módulo de voz | — (nuevas) | APROBADAS (con valores pendientes de validación donde se indica) | AG-13 | 2026-09-27 |
| D-061 | PIN de 6 dígitos como credencial de conocimiento (modifica D-037) | — (nueva) | APROBADA | Origen histórico: AG-01 (modifica D-037). Aprobada el 2026-09-27 (revisión del 2026-09-27). Impacto secundario: AG-11 (hash del PIN) | 2026-09-27 |
| D-055 | Umbrales y calibración | — (nueva) | Estrategia y proceso de calibración APROBADOS; **valores PENDIENTES** de calibración | AG-13 | 2026-09-27 |
| D-060 | Dataset y consentimiento | — (nueva) | PENDIENTE | AG-13 | 2026-09-27 |
| D-056 | Responsable de aplicar los umbrales técnicos | — (nueva) | APROBADA (servicio Python/FastAPI aplica los umbrales técnicos; Spring Boot decide la autenticación; valores en D-055) | AG-12: aprobación final | 2026-09-27 |
| D-018 | Estrategia de sesiones | PARCIAL | PARCIAL — cierre automático tras 5 min de inactividad, sin aviso previo ni opción de continuar; mecanismo pendiente | AG-02: aprobación final | 2026-09-27 |
| D-051 | Estrategia de migraciones de base de datos | PENDIENTE | APROBADA (Flyway, esquema `nayra`, UUID v4, nombres físicos, FK `RESTRICT`, historial propio de `biometria`) | AG-10: aprobada por el equipo tras la propuesta `PROPUESTA_MODELO_FISICO_D051_D009_2026-09-27.md` | 2026-09-27 |
| D-009 | Autorización | PARCIAL | APROBADA — rol como valor fijo (opción A); sin tabla `ROLES` ni varios roles por usuario | AG-03: misma revisión | 2026-09-27 |
| D-051 | Estrategia de migraciones de base de datos | APROBADA | APROBADA — ampliada por el modelo de datos v4 (V005–V011 de `nayra`; V001–V003 de `biometria`) | Modelo de datos v4, decidido por el equipo | 2026-09-27 |
| D-013 | Almacenamiento biométrico | APROBADA | APROBADA — estructura física de `biometria.perfiles_voz` definida (B-1 a B-13); AAD (B-4), frecuencia de rotación (B-5) y CHECK de 784 bytes PENDIENTES | Modelo de datos v4 | 2026-09-27 |
| D-018 | Estrategia de sesiones | PARCIAL | PARCIAL — JWT con `jti` = `sesiones.id`, tabla `sesiones`, sin renovación ni refresh token; `exp`, claims, algoritmo, custodia de clave y máximo de sesiones PENDIENTES (P-5) | Modelo de datos v4 | 2026-09-27 |
| D-044 | Política de 3 intentos | APROBADA (valor) / detalle PENDIENTE | APROBADA — solo cuenta el PIN incorrecto; límite 3; un PIN correcto devuelve el contador a 0 (H-01); límite biométrico PENDIENTE (P-8) | Modelo de datos v4 | 2026-09-27 |
| D-042 | Transferencias: dato del destinatario | APROBADA / dato del destinatario PENDIENTE | APROBADA — el backend usa el **ID interno de la cuenta destino** (G-1 cerrada); formato del QR PENDIENTE (P-3) | Decisión del equipo (G-1) | 2026-09-27 |
| D-035 | Validación del documento de identidad | APROBADA | APROBADA — documento DNI o CE; sin APIs externas; validación local/simulada **provisional** (P-2) | Modelo de datos v4 y decisión del equipo (P-2) | 2026-09-27 |
| D-050 | Autenticación del administrador | PENDIENTE | APROBADA FUNCIONALMENTE — usuario y contraseña en el panel web; **no implementada**; modelo y almacenamiento de la credencial administrativa PENDIENTES | Decisión del equipo | 2026-09-27 |
| D-055 | Umbrales y calibración | Estrategia APROBADA / valores PENDIENTES | APROBADA — similitud mínima **0.80** y probabilidad bona fide mínima **0.90**, escala [0,1]; **provisionales hasta la calibración** (D-060) | Decisión del equipo | 2026-09-27 |
| D-042 | Transferencias: dato del destinatario | APROBADA — ID interno de la cuenta destino (G-1) | APROBADA — **G-1 modificada:** el destinatario se busca por su **número de celular registrado en Nayra**; el ID interno de la cuenta sigue solo en el backend; la agenda del teléfono sigue fuera del primer entregable | Decisión del equipo (G-1) | 2026-09-28 |
| D-043 | Número de celular | APROBADA — dato de contacto | APROBADA — además de dato de contacto, **localiza al destinatario** de una transferencia; único y en formato canónico de Perú (9 dígitos, empieza por 9, sin `+51`); no prueba la titularidad de la línea | Decisión del equipo (G-1) | 2026-09-28 |
| D-048 | Vinculación del dispositivo | APROBADA (iOS no definido) | APROBADA — alcance del primer entregable solo Android (APK); iOS fuera de alcance | Modelo de datos v4 | 2026-09-27 |
| H-01 | Reinicio del contador de PIN | Contradicción abierta | CERRADA — PIN correcto → 0, inmediatamente | Decisión del equipo; coincide con el código | 2026-09-27 |
| H-02 | Seis reglas de BD "(a confirmar)" | Aplicadas sin confirmación formal | CERRADA — confirmadas | Decisión del equipo | 2026-09-27 |
| H-03 | Alcance de auditoría | Pendiente | CERRADA — se mantiene `01` §12.4 | Decisión del equipo | 2026-09-27 |

Esto permitirá mantener trazabilidad de las decisiones de diseño.

---

# 8. Reglas para Claude

Claude debe aplicar las siguientes reglas:

1. Consultar este documento antes de tomar decisiones técnicas importantes.
2. Tratar únicamente las decisiones `APROBADAS` como decisiones vigentes.
3. No implementar decisiones `PENDIENTES`.
4. No utilizar decisiones `DESCARTADAS`.
5. No utilizar arquitecturas anteriores descartadas.
6. Si existe una alternativa técnica no documentada, presentarla como propuesta.
7. No modificar una decisión aprobada sin explicar el motivo.
8. Mantener coherencia con `01_REQUISITOS_NAYRA.md`.
9. Mantener coherencia con `02_ARQUITECTURA_NAYRA.md`.
10. Mantener coherencia con `03_BASE_DE_DATOS_NAYRA.md`.
11. Mantener coherencia con `05_BIOMETRIA_NAYRA.md`.
12. Mantener coherencia con `06_SEGURIDAD_NAYRA.md`.
13. Registrar las decisiones nuevas antes de utilizarlas como base para cambios estructurales.
14. No introducir tecnologías adicionales únicamente porque sean técnicamente posibles.

---

# 9. Flujo de aprobación de una decisión

Una decisión técnica debe seguir este proceso:

```text
Necesidad
   ↓
Alternativas
   ↓
Evaluación
   ↓
Selección
   ↓
Justificación
   ↓
Registro en este documento
   ↓
Actualización de arquitectura/documentación
   ↓
Implementación
```

---

# 10. Fuente de verdad

El registro debe distinguir entre decisiones parciales ya aprobadas y la arquitectura integral todavía pendiente. Por ejemplo, D-023 aprueba las 2 instancias en zonas diferentes, pero no aprueba automáticamente el resto de la infraestructura asociada.

Para decisiones técnicas:

```text
07_DECISIONES_TECNICAS_NAYRA.md
          ↓
02_ARQUITECTURA_NAYRA.md
          ↓
Documentos especializados
          ↓
Código
```

El código no debe introducir silenciosamente decisiones que contradigan la documentación.

---

# 11. Regla principal

> **Si una decisión técnica no está aprobada y documentada, Claude debe tratarla como pendiente y no asumirla como parte de la solución.**


## Decisiones incorporadas

### D-023 — Dos instancias de aplicación en zonas diferentes
**Estado:** APROBADA

La solución contará con **2 instancias de aplicación ubicadas en zonas diferentes**, con el propósito de mejorar la disponibilidad y reducir el impacto de una falla localizada en una zona.

Esta decisión aprueba únicamente la existencia y distribución de las dos instancias. No determina por sí sola otros componentes de infraestructura, tales como balanceador, mecanismo de distribución de tráfico, VPC, subredes, firewall, monitoreo o recuperación ante fallos. Estos elementos deberán aprobarse y registrarse de manera independiente.

**Aclaración (2026-09-27):** D-023 se mantiene sin cambios. Los tres servicios del modelo de datos v4 (Negocio, Autenticación y Biométrico) son una separación **lógica, por responsabilidad**; no sustituyen a las 2 instancias ni significan 3 réplicas.

### D-021 — Entorno bancario simulado
**Estado:** APROBADA

Nayra no realizará integración directa con bancos reales dentro del alcance actual. Las entidades bancarias, cuentas y operaciones necesarias para el prototipo se representarán mediante un **entorno bancario simulado y controlado**.

Las transacciones serán simuladas y no involucrarán fondos reales.

### D-022 — Integración con bancos reales
**Estado:** DESCARTADA PARA EL ALCANCE ACTUAL

La integración con APIs o sistemas bancarios reales queda fuera del alcance del proyecto actual. No debe implementarse ni asumirse como requisito salvo que el alcance sea modificado y la decisión sea aprobada explícitamente.

## Decisiones aprobadas de AG-00 — Terminología, modelo financiero y depuración de requisitos (2026-09-25)

Estas decisiones son **funcionales y de modelo de datos**. No aprueban ninguna tecnología, mecanismo de autenticación, contrato de API ni componente de infraestructura. Documentos actualizados: `01_REQUISITOS_NAYRA.md` (§3.1, §4, §5, §6, §7, §11) y `03_BASE_DE_DATOS_NAYRA.md` (§3.3, §3.5, §3.10, §4.1, §15).

### D-024 — Terminología: cuenta de acceso y cuenta financiera
**Estado:** APROBADA

El término "cuenta" se interpreta según el contexto de cada HU:

- **cuenta de acceso** (perfil Nayra): identidad y acceso del usuario a Nayra;
- **cuenta financiera**: cuenta simulada del entorno bancario controlado (tabla `CUENTAS`).

Cuando el contexto permita aclararlo, no debe mantenerse la ambigüedad. La interpretación aplicada a cada HU consta en `01_REQUISITOS_NAYRA.md` §3.1.

### D-025 — Una única cuenta financiera por usuario
**Estado:** APROBADA

Cada usuario tendrá **una sola cuenta financiera** dentro de Nayra. Durante AG-00 se evaluó un modelo con varias cuentas financieras por usuario en distintas entidades, con selección de la cuenta destino en las transferencias; ese modelo quedó **descartado** para simplificar el proyecto.

### D-026 — Entidad bancaria simulada y tabla `ENTIDADES_BANCARIAS`
**Estado:** APROBADA

- Cada cuenta financiera está asociada a **una entidad bancaria simulada**.
- Se incorpora la tabla `ENTIDADES_BANCARIAS` como **catálogo de referencia** del entorno simulado (alternativa T1), con los campos mínimos `id` y `nombre`.
- La tabla no representa bancos reales (D-022) y no es gestionada por el administrador (D-033).

### D-027 — Nombre de la tabla `CUENTAS`
**Estado:** APROBADA

Se mantiene el nombre `CUENTAS`. No se renombra a `CUENTAS_FINANCIERAS`.

### D-028 — Asociación de la cuenta financiera mediante DNI (alternativa A1)
**Estado:** APROBADA

El DNI se utiliza **durante el registro** para localizar la cuenta financiera simulada del usuario. La relación persistente entre `CUENTAS` y `USUARIOS` se realiza mediante **FK con restricción de unicidad**, no mediante el valor del DNI.

**Pendiente asociado (resuelto por D-035, AG-01):** el dato que permite localizar la cuenta por DNI reside en el **registro de identidad simulado**; la cuenta financiera simulada referencia a su titular en ese registro, y la FK hacia `USUARIOS` es opcional hasta la vinculación (ver `03_BASE_DE_DATOS_NAYRA.md` §16).

### D-029 — Referencias de `OPERACIONES`
**Estado:** APROBADA

Las operaciones referencian directamente la **cuenta financiera de origen** y la **cuenta financiera de destino** (cuando corresponda). La cuenta destino deja de ser un campo de texto.

### D-030 — Transferencias sin selección de cuenta
**Estado:** APROBADA

Como cada usuario tiene una única cuenta financiera, no existe selección entre varias cuentas. El emisor utiliza su única cuenta financiera y el sistema identifica la única cuenta financiera del destinatario.

**Pendiente asociado:** la regla de moneda en transferencias. El destinatario es otro usuario de Nayra, seleccionado/buscado o identificado mediante QR (D-042); el dato concreto para buscar/seleccionar al destinatario sigue **pendiente**.

### D-031 — Depuración de historias de usuario
**Estado:** APROBADA

1. **Eliminación** de 8 IDs no aplicables que solo existían en `CLASIFICACION`: HU-05, HU-06, HU-07, HU-08, HU-11, HU-38, HU-39 y HU-63.
2. **Consolidación** de 11 grupos de HUs duplicadas (C1–C11), que absorbe 12 IDs: HU-82, HU-84, HU-85, HU-86, HU-91, HU-92, HU-103, HU-105, HU-106, HU-107, HU-108 y HU-112. Se conserva la HU de primera aparición en su épica, con la prioridad más alta y el texto unificado.
3. Los posibles duplicados **D1–D6** se mantienen **separados y pendientes**.
4. **No se renumeran** las HUs; los huecos resultantes son intencionales.
5. Cada HU se mantiene en su épica actual.

Resultado: 96 HUs vigentes. Trazabilidad completa en `01_REQUISITOS_NAYRA.md` §11.

### D-032 — El registro incluye el registro de voz
**Estado:** APROBADA

El registro de Nayra incluye el **registro de la voz del usuario** (EP-02), que será utilizada posteriormente para la autenticación biométrica.

**No aprobado por esta decisión:** el mecanismo exacto de identificación durante el registro (DNI, voz o ambos) queda **pendiente para AG-01 / D-008**. _(Resuelto en AG-01 por D-035, D-036 y D-052: un representante autorizado valida el DNI y la identidad de la persona, apoyado en los datos del registro de identidad simulado; la consulta del DNI por sí sola no prueba la identidad. La voz se enrola después y no se usa para identificación 1:N.)_ Esta decisión tampoco define el modelo biométrico (D-011), el anti-spoofing (D-012) ni el almacenamiento biométrico (D-013).

### D-033 — Exclusiones de alcance del modelo financiero
**Estado:** APROBADA

No se incorporan funcionalidades de:

- apertura de cuentas bancarias;
- múltiples cuentas financieras por usuario;
- transferencias entre cuentas propias;
- gestión de entidades bancarias por parte del administrador.

## Decisiones aprobadas de AG-01 — Registro, autenticación, dispositivo, recuperación y alcance del primer entregable (2026-09-26)

Estas decisiones son **funcionales**. No aprueban tecnologías concretas de frontend, reconocimiento del habla, biometría, anti-spoofing, almacenamiento, sesiones, hash ni comunicación Java ↔ Python, que siguen **PENDIENTES** (ver D-045 a D-051 y las decisiones pendientes previas). Documentos actualizados: `01`, `02`, `03`, `05`, `06`, `08`, `09` y `CLAUDE.md`.

### D-034 — Alcance del primer entregable
**Estado:** APROBADA

El primer entregable es un **prototipo funcional** de Nayra que demuestra que una persona con discapacidad visual puede realizar acciones digitales mediante una aplicación accesible, principalmente por voz, con autenticación de contraseña + voz y anti-spoofing. No es un sistema bancario real ni una solución productiva completa. **Todo el entorno financiero es simulado.**

Componentes del prototipo: aplicación móvil (usuario), backend principal (Java/Spring Boot), procesamiento biométrico de voz (Python), base de datos (PostgreSQL), entorno financiero simulado (incluye el registro de identidad simulado) y panel web del administrador.

Prioridad: 1) funcionamiento; 2) accesibilidad; 3) autenticación; 4) biometría; 5) seguridad básica; 6) operaciones financieras simuladas; 7) panel administrativo; 8) trazabilidad.

**Fuera del primer entregable:** retiro asistido por administrador; eliminación de cuenta; chatbot; OTP real, SMS, WhatsApp o llamadas automáticas; contacto de confianza; integración con la agenda del teléfono; múltiples dispositivos activos; bancos, pagos o APIs reales; proveedores externos con costo; modelos de IA entrenados desde cero; funcionalidades no respaldadas por HU. La clasificación de HUs por alcance está en `01_REQUISITOS_NAYRA.md` §12.4.

**Actualización AG-01 v5:** las funcionalidades de **QR** (HU-123, HU-124) se mantienen documentadas y trazadas, pero quedan **fuera de la implementación del primer entregable**, como funcionalidades secundarias para un **siguiente entregable** (ver D-042).

### D-035 — Fuente de identidad simulada
**Estado:** APROBADA

La consulta de identidad durante el registro utiliza un **registro de identidad simulado** dentro del entorno controlado, que relaciona **DNI, nombres y apellidos**. **No** se utiliza una API real de terceros ni proveedores con costo en el primer entregable.

Flujo: DNI dictado → búsqueda en el registro simulado → datos encontrados → Nayra comunica los datos necesarios → el usuario confirma.

**Precisión AG-01 v5 (D-052):** la consulta del DNI en el registro simulado **solo recupera los datos asociados**; **no** constituye por sí sola prueba de titularidad ni de identidad. La validación de identidad la realiza un **representante autorizado** en el registro asistido (D-052).

Esta decisión **resuelve el pendiente de D-028**: la cuenta financiera simulada referencia a su titular en el registro de identidad simulado, lo que permite localizarla por DNI durante el registro.

### D-036 — Flujo de registro del usuario
**Estado:** APROBADA — **MODIFICADA POR D-052** (AG-01 v5, 2026-09-26)

El flujo vigente es el de **D-052** (registro asistido por un representante autorizado). Se conserva abajo el flujo original para trazabilidad; sus pasos 1 a 3 (DNI por voz y confirmación de identidad solo por el usuario) y el orden entre enrolamiento y vinculación quedan reemplazados por D-052. Se mantiene la regla del DNI ya registrado.

Flujo original:

1. DNI mediante voz.
2. Consulta al registro de identidad simulado (D-035).
3. Confirmación de identidad por el usuario.
4. Registro del número de celular (D-043).
5. Creación de contraseña (D-037).
6. Enrolamiento biométrico de voz, con anti-spoofing durante el enrolamiento.
7. Vinculación del dispositivo (D-039).
8. Confirmación del registro.
9. Tutorial inicial accesible por voz.

**Si el DNI ya pertenece a una cuenta de acceso:** no se crea una segunda cuenta; se deriva al proceso de recuperación/cambio de dispositivo (D-040).

### D-037 — Autenticación del usuario
**Estado:** APROBADA FUNCIONALMENTE — **MODIFICADA POR D-061** (2026-09-27): la "contraseña" se concreta como **PIN de 6 dígitos**. El resto del flujo se mantiene.

La autenticación del usuario se compone de **contraseña + verificación biométrica de voz 1:1 + anti-spoofing**. Flujo de inicio de sesión:

`"Iniciar sesión Nayra"` (solo activa el proceso) → el **dispositivo vinculado** determina la cuenta → contraseña → frase de **desafío variable** → comprobación del **contenido** del desafío → anti-spoofing → verificación biométrica **1:1** → sesión.

Reglas:

- **No** se utiliza identificación biométrica 1:N.
- El usuario **no** dicta su DNI en cada inicio de sesión.
- La contraseña **no** identifica la cuenta; la cuenta la determina el dispositivo vinculado.
- La contraseña se crea en el registro, puede dictarse, se almacena **solo mediante hash seguro**; nunca en texto plano, nunca como audio y nunca en logs.
- Ni el comando de activación ni la contraseña se usan como muestra biométrica; la muestra es la respuesta al desafío.

**Pendiente:** algoritmo de hash, política y normalización de la contraseña dictada (D-047); reconocimiento del habla (D-046); sesiones (D-018; mecanismo JWT con `jti` aprobado en el modelo v4, P-5 pendiente); autenticación del administrador (D-050; aprobada funcionalmente el 2026-09-27, sin implementar). El algoritmo existente en el código (BCrypt) **no** se considera aprobado por existir.

### D-038 — Referencia biométrica persistente en backend
**Estado:** APROBADA COMO PRINCIPIO

La referencia biométrica permanece **asociada a la cuenta de acceso en el backend** y no depende exclusivamente del dispositivo. Tras un cambio de celular sigue disponible para la verificación 1:1.

**No aprobado:** dónde se almacena, su formato, el modelo que la genera ni si se guarda como embedding u otra representación (D-011, D-013 siguen PENDIENTES). No se almacena audio de voz innecesariamente.

**Actualización AG-13 (2026-09-27):** resuelto por D-011 y D-013. "Backend" en esta decisión comprende el **servicio Python de voz**, que es el único componente que guarda y lee la referencia (embedding cifrado) en un esquema propio del PostgreSQL del proyecto.

### D-039 — Un único dispositivo activo por cuenta de acceso
**Estado:** APROBADA

- Una cuenta de acceso tiene **un solo dispositivo activo**; no se permiten varios dispositivos activos simultáneamente.
- El dispositivo se vincula al final del registro y determina la cuenta al iniciar sesión.
- **Reinstalación:** no se asume que la aplicación reconozca siempre el dispositivo tras reinstalarse. Si el vínculo puede verificarse, se continúa; si no, se usa el flujo de cambio de dispositivo/recuperación (D-040). No se desarrolla una solución compleja de identificación de dispositivos solo para la reinstalación.

**Pendiente:** mecanismo técnico de vinculación del dispositivo (D-048). _(2026-09-27: resuelto por D-048 — par de claves del dispositivo.)_

### D-040 — Cambio de dispositivo, pérdida y recuperación
**Estado:** APROBADA (procedimiento de recuperación asistida PENDIENTE, D-049)

**Cambio de celular:** en el nuevo dispositivo el usuario dicta su DNI → Nayra detecta la cuenta existente → **no** se crea una cuenta nueva → se valida al titular con **contraseña + verificación de voz 1:1 contra la referencia existente + anti-spoofing** → si es correcto, se autoriza el nuevo dispositivo y **se revoca el anterior** → se puede permitir un nuevo enrolamiento biométrico **después** de haber validado al titular.

**Prohibido expresamente:** `DNI → cuenta encontrada → registrar nueva voz → acceso`, por riesgo de suplantación.

**Pérdida del celular:** el usuario solicita asistencia → se genera una solicitud → el administrador la revisa → puede bloquear la cuenta de acceso y/o el dispositivo → se revocan las sesiones correspondientes → la acción queda auditada. El administrador no modifica saldo ni operaciones.

**Recuperación:** cubre pérdida del celular, cambio de celular, cuenta bloqueada y problemas de autenticación. Si el usuario no supera la validación normal, se requiere un **proceso de recuperación asistido cuyo procedimiento exacto queda PENDIENTE (D-049)**. La atención se representa mediante solicitudes y el panel administrativo, sin chatbot.

### D-041 — Roles y límites del administrador
**Estado:** APROBADA

- Solo existen dos roles: **USER** y **ADMIN**. No existe un rol independiente de soporte/personal de atención. Las HUs que mencionaban al personal de atención se reasignan, se declaran sin objeto o se excluyen del primer entregable (`01_REQUISITOS_NAYRA.md` §12.3).
- El administrador **no puede** modificar saldos ni operaciones financieras, ni usar privilegios administrativos para alterar información financiera.
- Toda acción administrativa queda **auditada** (quién, cuándo, qué acción y sobre qué usuario).
- Panel web del administrador en el primer entregable: autenticación; consulta de usuarios y del estado de sus cuentas; bloqueo/desbloqueo; consulta y gestión del dispositivo asociado; gestión de solicitudes de recuperación; consulta de auditoría; métricas básicas respaldadas por HU existentes.

### D-042 — Transferencias a usuarios de Nayra y QR
**Estado:** APROBADA

- Un **"contacto"** para transferencias es **otro usuario registrado en Nayra**. No se integra la agenda del teléfono.
- Flujo: usuario A → selecciona/busca al usuario B → confirma destinatario → indica monto → confirma operación → transferencia simulada.
- Cada cuenta financiera simulada tiene un **QR asociado**. Nayra puede generar/mostrar el QR de la propia cuenta, escanear un QR, obtener el destinatario, confirmar por voz los datos y realizar la transferencia simulada.
- El QR **no** es un mecanismo de autenticación y **no** contiene contraseña, datos biométricos ni información sensible innecesaria; solo permite identificar la cuenta/destinatario dentro del entorno simulado.
- **AG-01 v5:** el QR (HU-123, HU-124) **no forma parte de la implementación del primer entregable**. Se mantiene documentado y trazado como funcionalidad **secundaria para un siguiente entregable**, cuando el producto tenga mayor madurez y pueda validarse con más personas. La transferencia del primer entregable es la transferencia directa a otro usuario de Nayra.

**Pendiente (texto de AG-01):** dato concreto con el que se busca/selecciona al destinatario; identificador que codifica el QR (`03_BASE_DE_DATOS_NAYRA.md` §16).

**Actualización del 2026-09-27:**
- ~~**G-1 — CERRADA:** el backend identifica al destinatario de una transferencia por el **ID interno de la cuenta financiera de destino** (`cuentas.id`, que `operaciones.cuenta_destino_id` referencia). El frontend usa ese ID para obtener y mostrar la información del destinatario.~~ _(Modificada el 2026-09-28: ver abajo.)_ Las transferencias **todavía no están implementadas** (no hay endpoint); el contrato sigue en D-014.
- **QR:** `cuentas.codigo_qr` existe como identificador fijo, único y sin datos personales; su valor actual es un identificador aleatorio **PROVISIONAL**. El formato definitivo sigue **PENDIENTE (P-3)** y las funciones de QR (HU-123, HU-124) siguen fuera del primer entregable.

**Actualización del 2026-09-28 — G-1 modificada (identificación del destinatario):**
- El destinatario de una transferencia se **busca por su número de celular registrado en Nayra**. La persona escribe el número con un teclado grande y accesible; Nayra-Back localiza la cuenta Nayra asociada y usa internamente el ID de la cuenta destino (`cuentas.id`) para la operación. El ID interno **no** se pide ni se muestra al usuario.
- Flujo: celular → búsqueda → nombre parcial → confirmación → Sí (continúa al monto) / No, buscar otro número (se reinicia el ingreso del número).
- Tras encontrar el número se muestra solo el **primer nombre y el primer apellido parcial (con sus partículas De, Del, La, Las, Los; cuatro letras sin contar espacios, sin cortar una partícula; entero si tiene cinco letras o menos)** ("María Sala...", "María De la..."), con la pregunta "¿Desea transferir a María Sala...?". Regla modificada el 2026-09-28; ver «Nombre parcial del destinatario». El número no es el elemento principal de la confirmación.
- La búsqueda solo afirma que **existe una cuenta Nayra asociada a ese número**. No valida la titularidad de la línea (sin OSIPTEL ni operadores).
- **Agenda del teléfono:** sigue fuera del primer entregable (se mantiene lo anterior de esta decisión).
- Una cuenta de acceso no activa, o sin cuenta financiera activa, se informa igual que un número sin cuenta (`DESTINATARIO_NO_ENCONTRADO`), para no revelar el estado de la cuenta.

### D-043 — Número de celular
**Estado:** APROBADA — ampliada el 2026-09-28 (G-1)

El número de celular forma parte del registro y se almacena como **dato de contacto** para procesos de atención y recuperación. **No** se implementan SMS reales, proveedores OTP, WhatsApp ni llamadas automáticas. Una eventual simulación de validación del número no es dependencia del primer entregable.

**Actualización del 2026-09-28 (G-1):** el celular también **localiza al destinatario** de una transferencia. Por eso es **único** (`UNIQUE`, migración V012) y se guarda y se busca en un único formato canónico: celular de Perú, 9 dígitos que empiezan por 9; el prefijo `+51` se quita antes de validar. El registro rechaza un número ya asociado a otro usuario (`CELULAR_REGISTRADO`). Sin validación de titularidad.

### D-044 — Política de 3 intentos
**Estado:** APROBADA — valor (AG-01) y qué cuenta como intento (modelo de datos v4, 2026-09-27). Reinicio del contador con el PIN correcto (**H-01**, cerrada el 2026-09-27); límite propio de los fallos biométricos PENDIENTE (**P-8**); efecto del desbloqueo administrativo PENDIENTE (**P-11**).

**Actualización del modelo de datos v4 (2026-09-27):**
- **Solo el PIN incorrecto cuenta** como intento fallido. Los fallos de voz, contenido, anti-spoofing o calidad **no** suman al contador (su límite propio es P-8, pendiente); los errores técnicos tampoco.
- Contador `intentos_fallidos` en `nayra.credenciales` (no en `usuarios`), con CHECK 0..3. Al **tercer** PIN incorrecto la cuenta de acceso pasa a `BLOQUEADO` y se revocan sus sesiones.
- El incremento es **atómico** (UPDATE condicional `intentos_fallidos + 1 … WHERE intentos_fallidos < 3`): dos PIN incorrectos simultáneos cuentan como dos y el contador nunca pasa de 3 (corrección E-01).
- **H-01 (cerrada el 2026-09-27):** `intentos_fallidos` vuelve a 0 **inmediatamente** cuando el PIN introducido es correcto ("PIN correcto → 0"), antes del paso de voz. Coincide con el comportamiento actual del código. Antes de esta decisión, el modelo v4 §2.2 decía "tras una autenticación correcta con PIN" y §A.5 "un PIN correcto → 0".
- La propuesta de la revisión del 2026-09-27 citada abajo, que contaba también la voz, **no se adoptó**.

Texto original (AG-01):

Tras **3 intentos fallidos** de autenticación la cuenta de acceso se bloquea.

**Pendiente** antes de implementar: qué resultados cuentan como intento fallido (contraseña incorrecta, voz no coincidente, spoofing, mala calidad de audio); si hay un contador único o separado; ventana y reinicio del contador. Los **errores técnicos del servicio no se consideran intentos fallidos del usuario** salvo decisión expresa en contrario.

**Propuesta de la revisión del 2026-09-27 (PENDIENTE DE DECISIÓN, no aprobada):** cuentan como intento fallido el PIN incorrecto, el contenido del desafío incorrecto, el posible spoofing y la voz no coincidente; la calidad de audio insuficiente permite repetir la captura sin contar (HU-44); los errores técnicos no cuentan; contador único por cuenta de acceso.

### D-052 — Registro asistido con validación de identidad por un representante autorizado
**Estado:** APROBADA (regla de negocio — AG-01 v5, 2026-09-26; flujo actualizado en AG-01 v6, 2026-09-26). **Modifica D-036.**

El registro inicial es un proceso **asistido** por un **representante autorizado de la organización**. El representante puede ser un **administrador o una persona autorizada** para asistir en el registro. La consulta del DNI en el registro de identidad simulado recupera los datos asociados, pero **no es prueba suficiente** de titularidad o identidad: la validación de identidad corresponde al representante.

Flujo conceptual vigente (AG-01 v6):

1. La persona solicita registrarse.
2. El representante autorizado asiste a la persona.
3. Se proporciona el DNI de la persona.
4. El sistema consulta el registro de identidad simulado asociado al DNI (D-035).
5. Se muestran los datos correspondientes.
6. El representante valida la identidad de la persona.
7. La persona confirma sus datos.
8. Se registra el número de celular como dato de contacto (D-043).
9. La persona crea su contraseña (D-037).
10. Se vincula el dispositivo/celular (D-039).
11. Se realiza el enrolamiento de voz, con anti-spoofing (D-038).
12. Se realiza el tutorial inicial.
13. Finaliza el registro.

Si el DNI ya pertenece a una cuenta de acceso, no se crea otra y se deriva a recuperación/cambio de dispositivo (D-040).

La biometría de voz **no** se utiliza para validar la identidad durante el registro inicial; se usa después para autenticación y, cuando corresponda, en recuperación/cambio de dispositivo (D-040).

**Es una regla de negocio, no un rol.** Los roles de la aplicación siguen siendo **únicamente `USER` y `ADMIN`** (D-041). No se crea un rol "REPRESENTANTE" salvo decisión explícita posterior.

**Pendiente (categoría B):** cómo se registra técnicamente quién realizó la validación asistida (en particular cuando el representante no es un usuario `ADMIN`) y cómo se refleja ese evento en la auditoría. No se define todavía una solución técnica.

_Resueltos en AG-01 v6:_ momento del registro del celular (paso 8) y posición del DNI (paso 3, proporcionado durante la asistencia; el medio concreto de ingreso es un detalle de implementación).

### D-053 — Flujos diferenciados: registro inicial, inicio de sesión y cambio/recuperación de dispositivo
**Estado:** APROBADA (AG-01 v6, 2026-09-26)

**REGISTRO INICIAL ≠ INICIO DE SESIÓN ≠ CAMBIO/RECUPERACIÓN DE DISPOSITIVO.**

| Flujo | Decisión | ¿Usa DNI? | Validación de identidad |
|---|---|---|---|
| **Registro inicial** | D-052 | **Sí** — para consultar el registro de identidad simulado | Representante autorizado (regla de negocio); la persona confirma sus datos. Sin biometría |
| **Inicio de sesión habitual** | D-037 | **No** — el DNI **no** forma parte del inicio de sesión habitual | Dispositivo vinculado → contraseña → desafío variable → comprobación del contenido → verificación biométrica 1:1 y anti-spoofing → sesión; 3 intentos (D-044) |
| **Cambio o pérdida de dispositivo / recuperación** | D-040 | **Sí** — para localizar la cuenta existente | Validación de titularidad mediante el proceso de recuperación correspondiente (procedimiento asistido pendiente, D-049). Nunca se crea una cuenta nueva ni se reemplaza la voz sin validar la titularidad |

**Usos permitidos del DNI:** registro inicial, procesos de recuperación, cambio o pérdida del dispositivo y otros procesos excepcionales en los que sea necesario localizar la cuenta. **No** se agrega el DNI como requisito del inicio de sesión habitual.

### Decisiones técnicas abiertas por AG-01

| ID | Decisión | Estado |
|---|---|---|
| D-045 | Tecnología del panel web del administrador | PENDIENTE |
| D-046 | Reconocimiento del habla (comando, DNI, contraseña dictada, contenido del desafío): tecnología y ubicación (dispositivo o servidor) | **PARCIAL (AG-13):** contenido del desafío APROBADO (Vosk en servidor); comando, tecnología del PIN dictado (dictado aprobado por D-061) y DNI PENDIENTES |
| D-047 | Algoritmo de hash, política de contraseña compatible con dictado y normalización de la contraseña dictada | PENDIENTE — **AG-11** (2026-09-27): se aplica al **PIN de 6 dígitos** (D-061); el análisis A-K (Argon2id + pepper + límite de intentos) queda como insumo, no aprobado |
| D-048 | Mecanismo técnico de vinculación del dispositivo | **APROBADA:** par de claves del dispositivo (origen histórico: AG-01; aprobada el 2026-09-27; impacto secundario: AG-11 por anti-replay) (ver sección de decisiones del 2026-09-27) |
| D-049 | Procedimiento de recuperación asistida | PENDIENTE |
| D-050 | Autenticación del administrador | **APROBADA FUNCIONALMENTE (2026-09-27):** usuario y contraseña en el panel web. **No implementada**; modelo y almacenamiento de la credencial administrativa PENDIENTES. El prototipo mantiene provisionalmente el acceso ADMIN con dispositivo + PIN + voz |
| D-051 | Estrategia de migraciones de base de datos | **APROBADA (2026-09-27, AG-10)** — ver «Modelo físico de la base de datos» |

Siguen también **PENDIENTES**: D-007 (frontend móvil), D-010 (Java ↔ Python), D-011 (modelo biométrico), D-012 (anti-spoofing), D-013 (almacenamiento biométrico), umbral biométrico, D-014 (API), D-018 (sesiones) y D-019 (auditoría detallada). D-017 (secretos) pasa a **deuda técnica** (categoría C).

_(Revisión del 2026-09-27: D-007 (móvil), D-010, D-011, D-012, D-013, D-048 y D-056 quedan aprobadas; D-018 y D-046 pasan a parciales; el umbral biométrico se rige por D-055 (estrategia aprobada, valores pendientes de calibración). Ver sección de decisiones del 2026-09-27.)_

### Clasificación de lo pendiente (AG-01 v5)

Una deuda técnica pendiente **no** se convierte automáticamente en un bloqueo para todo el proyecto. Categorías:

- **A. Bloqueantes funcionales:** impiden implementar una funcionalidad del primer entregable hasta que se decidan.
- **B. Decisiones técnicas pendientes:** deben definirse para implementar correctamente una funcionalidad, pero pueden resolverse durante el desarrollo, al llegar a esa funcionalidad.
- **C. Deudas técnicas:** problemas conocidos que deben corregirse después y que no impiden continuar el desarrollo académico actual.

| Categoría | Decisiones | Estado de la clasificación |
|---|---|---|
| **A** | D-045 (panel web), D-046 (resto del reconocimiento del habla: comando y tecnología del PIN dictado, cuyo dictado ya aprobó D-061) | Propuesta para revisión (actualizada el 2026-09-27) |
| **B** | D-047 (hash del PIN), D-018 (mecanismo técnico de sesión), D-044 (detalle de intentos), D-050 (autenticación del administrador), D-049 (recuperación asistida), D-052 (registro técnico y auditoría de quién realizó la validación asistida), D-014 (API; incluye el contrato interno del servicio de voz), D-019 (auditoría detallada), D-058 (herramienta de VAD), D-054 (lista de palabras y TTL del desafío), D-060 (dataset y consentimiento), D-016 (despliegue del servicio de voz) | Propuesta para revisión (actualizada el 2026-09-27) |
| **Cerradas el 2026-09-27** | D-007 (app móvil), D-009 (representación de roles), D-010, D-011, D-012, D-013, D-048, D-051 (migraciones e identificadores), D-056 (responsable de aplicar los umbrales técnicos), umbral biométrico (solo la estrategia, en D-055; los valores siguen pendientes de calibración) | APROBADAS (2026-09-27) |
| **C** | **D-017** (secretos y credenciales) | **Aprobada** (AG-01 v5) |

Cada decisión A afecta solo a las funcionalidades que dependen de ella; las demás pueden avanzar en paralelo.

**Actualización del modelo de datos v4 (2026-09-27):** de la categoría B quedan resueltos D-018 en su mecanismo base (JWT con `jti`; P-5 sigue pendiente) y D-044 en qué cuenta como intento (solo el PIN; el PIN correcto devuelve el contador a 0, H-01). D-050 queda aprobada funcionalmente, pero su implementación depende del modelo de la credencial administrativa (pendiente). D-055 fija valores provisionales (0.80 / 0.90) hasta la calibración de D-060. Ver «Decisiones del modelo de datos v4».

## Decisiones aprobadas el 2026-09-27 — Módulo de autenticación por voz, anti-spoofing, dispositivo, PIN y sesiones

_Bloque sin número de agenda (revisión del 2026-09-27); la trazabilidad temática de cada decisión está en «Agendas temáticas (AG)»._

Origen: revisión cruzada entre el análisis técnico A–K (2026-09-26) y la documentación de la rama `yuniv` (commit `e16cb45`), aprobada por el equipo el 2026-09-27 junto con cuatro puntos que no estaban sincronizados en el repositorio: **PIN de 6 dígitos** (D-061), **par de claves del dispositivo** (D-048), **cierre de sesión tras 5 minutos de inactividad** (D-018) y **Flutter** para la aplicación móvil (D-007).

Reglas comunes a todo este bloque:

- **Ningún valor numérico** (umbrales, duraciones, vida del desafío, número de muestras, recursos) queda aprobado salvo que se indique expresamente. Cada valor definitivo se registrará aquí junto con el experimento que lo respalde.
- Las cifras de desempeño publicadas por los autores de los modelos **no** son resultados de Nayra (`05_BIOMETRIA_NAYRA.md` §13, §23).
- Las licencias indicadas provienen del análisis A–K (verificadas el 2026-09-26) y deben reconfirmarse al fijar versiones.
- El diseño técnico detallado del módulo está en `05_BIOMETRIA_NAYRA.md` §27; la arquitectura lógica en `02_ARQUITECTURA_NAYRA.md`.

### D-011 — Modelo biométrico (detalle)
**Estado:** APROBADA (selección) / PENDIENTE DE VALIDACIÓN (desempeño)

- **Modelo:** SpeechBrain ECAPA-TDNN preentrenado `speechbrain/spkrec-ecapa-voxceleb` (código y pesos Apache 2.0).
- **Representación:** embedding de 192 dimensiones, normalizado (L2).
- **Comparación:** similitud coseno, **verificación 1:1** contra la referencia de la cuenta de acceso. Nunca 1:N (D-037).
- **Ejecución:** en el servicio Python (D-002), en CPU.
- **Motivo:** continuidad con D-005; D-004 (preentrenado); licencia sin restricciones; integración directa en Python; D-034 excluye proveedores con costo.
- **Comparación experimental opcional:** WeSpeaker (VoxBlink2 o ResNet34) solo como experimento de validación (Objetivo 3), no desplegado en el prototipo.
- **Pendiente de validación:** FAR, FRR y EER con voces en español y celulares del proyecto (ver D-055).
- **Descartadas:** NVIDIA NeMo TitaNet (dependencias pesadas), Resemblyzer/GE2E (menor precisión), pyannote embedding (acceso restringido), APIs comerciales de voz (costo y datos biométricos a terceros, D-034).

### D-012 — Anti-spoofing (detalle)
**Estado:** APROBADA (estrategia) / PENDIENTE DE VALIDACIÓN (tasa de detección)

Defensa en capas:

1. **Desafío variable de un solo uso con comprobación de contenido** (D-054, D-046): defensa principal contra la reproducción de grabaciones antiguas.
2. **Firma del dispositivo vinculado** sobre un nonce de un solo uso (D-048): impide enviar la solicitud desde otro equipo.
3. **AASIST** preentrenado (MIT) para detectar voz sintética (TTS) y conversión de voz, aplicado **en el enrolamiento y en la autenticación** (`05_BIOMETRIA_NAYRA.md` §26.1, §26.2).
4. **SSL-AASIST** (wav2vec 2.0 XLS-R + AASIST) solo como **extensión** si la evaluación muestra que AASIST no basta y se dispone de GPU. No forma parte del primer entregable.

**Riesgos aceptados y documentados:** replay en tiempo real (grabar y reproducir el desafío al instante) y deepfakes modernos no vistos en el entrenamiento de AASIST (ASVspoof 2019, inglés). No se afirma que la solución detecte todos los ataques (`06_SEGURIDAD_NAYRA.md` §15).

**Descartadas como principal:** RawNet2 (inferior a AASIST en los mismos datos) y modelos de "detección de deepfake" de terceros sin procedencia académica clara.

### D-013 — Almacenamiento biométrico (detalle)
**Estado:** APROBADA

- Se guarda **solo el embedding de referencia** y sus metadatos. **El audio no se guarda**: se procesa en memoria y se descarta (`05_BIOMETRIA_NAYRA.md` §16).
- Referencia = **centroide** (promedio renormalizado) de las muestras válidas del enrolamiento; número inicial de muestras en D-059.
- Cifrado **AES-256-GCM** en reposo; la clave vive fuera del código y de la base de datos (vinculado a D-017).
- Ubicación: **esquema propio (`biometria`) dentro del PostgreSQL del proyecto**, con usuario de base de datos exclusivo del servicio Python. El backend principal (Java) **no** lee ni recibe embeddings; solo conoce el identificador del usuario y el resultado. No es una base de datos adicional (`02_ARQUITECTURA_NAYRA.md` §19).
- Se registran el **nombre y la versión del modelo**; si cambia el modelo, la referencia no es comparable y se requiere re-enrolamiento.
- **Actualización:** solo por re-enrolamiento explícito tras validar al titular (D-040; HU-34/HU-35). Sin adaptación automática de la plantilla.
- **Eliminación (HU-36):** borrado físico de la referencia y registro en auditoría, sin el embedding.
- El administrador **no** accede a la referencia (`06_SEGURIDAD_NAYRA.md` §36.5).
- Modelo lógico en `03_BASE_DE_DATOS_NAYRA.md` §16.11. La tabla física se crea con la estrategia de migraciones (D-051, aprobada el 2026-09-27: historial de migraciones propio para `biometria`; la estructura física de `perfiles_voz` sigue pendiente).
- **Actualización del modelo de datos v4 (2026-09-27):** `biometria.perfiles_voz` está creada (`Nayra-Voz/migraciones/biometria`, V001–V003, historial propio) con la estructura B-1 a B-13 (`03` §17): **un perfil por usuario** (`UNIQUE(usuario_id)`), `embedding_cifrado` (cifrado AES-GCM + etiqueta) con `iv` de 12 bytes nuevo en cada cifrado, `clave_version` (la clave nunca está en PostgreSQL, en el código ni en el repositorio), modelo y versión del modelo, `numero_muestras`, estado `ACTIVO`/`REVOCADO`; **sin audio**; sin FK física hacia `nayra.usuarios` (referencia lógica). Volver a enrolar borra el perfil anterior e inserta uno nuevo; no hay historial de embeddings. El servicio Python persiste el perfil con psycopg cuando existe `NAYRA_VOZ_BD`; sin esa variable usa memoria (solo desarrollo). **PENDIENTES:** AAD definitiva (**B-4**; hoy PROVISIONAL `nayra-voz:v1|id|usuario_id|modelo|version_modelo`), frecuencia de rotación de claves (**B-5**), CHECK de 784 bytes de `embedding_cifrado` (se aplicará tras ejecutar ECAPA real) y rango de `numero_muestras` (**D-059**).

### D-010 — Comunicación Java ↔ Python (detalle)
**Estado:** APROBADA / autenticación entre servicios en la nube PENDIENTE (D-016)

- **API REST interna** con **FastAPI** + Uvicorn en el servicio Python; modelos cargados una vez al iniciar.
- Solo red interna; nunca expuesta a internet ni a la aplicación móvil. La app habla únicamente con Spring Boot.
- Audio enviado como `multipart/form-data` en el formato de D-057.
- **Autenticación entre servicios** en el prototipo: token de servicio en la cabecera `Authorization`, leído de variable de entorno (no se agregan secretos al código, D-017), comparado en tiempo constante. En la nube: identidad de servicio o mTLS, a decidir en D-016.
- Timeouts de conexión y lectura configurables; **sin reintentos automáticos** que dupliquen un intento del usuario.
- Error 5xx o timeout → "servicio de voz no disponible"; **no cuenta como intento fallido** (D-044).
- El audio no se escribe en disco ni en logs; los logs solo llevan un identificador de solicitud y motivos.
- El contrato se documentará en `04_API.md` (D-014, PENDIENTE); debe reflejar que el servicio Python devuelve veredictos técnicos por etapa (D-056).
- **Descartadas:** gRPC (complejidad sin beneficio a este volumen), colas de mensajes (la autenticación es síncrona), ejecutar los modelos en Java con ONNX (contradice D-002).

### D-046 — Reconocimiento del habla (actualización parcial)
**Estado:** PARCIAL

- **Contenido del desafío — APROBADO:** **Vosk** (Apache 2.0) con **gramática restringida** al vocabulario del desafío, ejecutado **en el servidor** (servicio Python). Modelo inicial `vosk-model-small-es-0.42`; `vosk-model-es-0.42` si la precisión no alcanza; `faster-whisper` (`small`) como respaldo si la evaluación muestra fallas con acentos o ruido. Criterio: coincidencia exacta de la secuencia más confianza por palabra (umbral en D-055). Motivo de la ubicación en servidor: `06_SEGURIDAD_NAYRA.md` §3.4 (no confiar en el cliente).
- **Descartado para verificar el desafío:** reconocedor del sistema operativo del teléfono (no controlable por el backend y el audio puede salir a servidores de terceros).
- **PENDIENTE:** reconocimiento del comando "Iniciar sesión Nayra", tecnología definitiva de reconocimiento del PIN dictado (el dictado está aprobado por D-061) y del DNI en recuperación/cambio de dispositivo. Al procesar el PIN dictado no se almacena ni registra el audio ni la transcripción (`05_BIOMETRIA_NAYRA.md` §26.6), y ese audio **nunca** entra al pipeline biométrico.
- **PIN dictado — candidata PROVISIONAL del prototipo (2026-09-27):** el dictado quedó permitido (D-061). Para el prototipo se usa Vosk en el servicio Python con una gramática restringida a los diez dígitos, que exige exactamente 6 dígitos. Es una candidata **no definitiva, PENDIENTE DE VALIDACIÓN**; el comando "Iniciar sesión Nayra" y el DNI siguen PENDIENTES.

### D-048 — Vinculación del dispositivo mediante par de claves
**Estado:** APROBADA

- La app genera un **par de claves dentro del almacén de claves de hardware del teléfono** (Android Keystore; StrongBox cuando exista). La clave privada es **no exportable** y nunca sale del dispositivo.
- Algoritmo: **ECDSA P-256 (secp256r1) con SHA-256**.
- Implementación: **canal de plataforma propio en Kotlin** expuesto a Flutter (D-007). No se usa un plugin que exija huella o rostro del sistema (agregaría otra credencial) ni una clave generada en Dart (sería exportable).
- La clave pública se envía al vincular el dispositivo (D-052, paso 10) y el backend guarda **solo la clave pública** en `DISPOSITIVOS`.
- En cada autenticación, Spring Boot emite un **nonce aleatorio de un solo uso y vida corta** (`SecureRandom`); la app firma nonce + identificador del dispositivo + propósito; el backend verifica la firma con la clave pública y rechaza nonces usados o vencidos.
- Revocación: el dispositivo pasa a `REVOCADO` y toda firma posterior se rechaza; se revocan sus sesiones (D-040).
- Reinstalación o borrado de datos: la clave se pierde; se trata como dispositivo nuevo (flujo D-040), coherente con D-039.
- **No aprobado:** Key Attestation (extensión opcional); vida exacta del nonce.
- **Plataforma (modelo de datos v4, 2026-09-27):** el primer entregable es **solo Android**, distribuido como **APK**; `dispositivos.plataforma` solo admite `ANDROID`. **iOS queda fuera de alcance.**

### D-054 — Generación del desafío de voz
**Estado:** APROBADA (estructura) / lista v2 y vida del desafío PROVISIONALES — PENDIENTES DE VALIDACIÓN (2026-09-27) / comprensión PENDIENTE DE VALIDACIÓN con usuarios

- Estructura: **5 elementos: palabra + 3 dígitos + palabra** (ej.: "sol, cuatro, siete, dos, mesa").
- Lista cerrada de ~40 palabras comunes, bisílabas, fonéticamente distintas y sin homófonos (la lista se aprueba aparte). Dígitos del 0 al 9 dichos uno por uno.
- **Nunca 6 dígitos seguidos**, para que el desafío no se confunda con el PIN (D-061) ni induzca a decir el PIN en voz alta.
- Generado en **Spring Boot** con `SecureRandom`; **un solo uso**; vida corta (valor pendiente, debe contemplar el tiempo que una persona ciega necesita para escuchar, repetir y grabar).
- Se emite **después** de validar el dispositivo y el PIN (D-037) y queda ligado a la cuenta y al contexto: dispositivo vinculado (inicio de sesión) o solicitud de cambio de dispositivo (D-040).
- Repetir la lectura (HU-58) **no** genera un desafío nuevo; enviar un audio **consume** el desafío.
- En el enrolamiento se usan desafíos distintos por muestra (D-059).
- El vocabulario es un archivo de configuración versionado, compartido por Spring Boot (generación) y Python (gramática de Vosk).
- Esta estructura se considera una "frase de desafío" en el sentido de HU-29, HU-43 y del glosario de `01_REQUISITOS_NAYRA.md`.
- **Lista v2 (2026-09-27, reemplaza a la v1):** 40 palabras **bisílabas**, comunes, sin homófonos con seseo o yeísmo y no parecidas a los dígitos, en `shared/desafio/vocabulario_v2.json`, compartido por Spring Boot y Python. La v1 se retiró porque incluía una palabra monosílaba (*tren*) y catorce de tres sílabas. La verificación de sílabas, las confusiones que se mantienen y las palabras descartadas están en `/mnt/project-files/analisis/BACKEND_GENERAL_AUDITORIA_2026-09-27.md`, parte 1. Queda **PROVISIONAL — PENDIENTE DE VALIDACIÓN** y **no es una lista aprobada**: falta validar la comprensión con usuarios, la diferenciación fonética y la presencia de las 40 palabras en el léxico de Vosk.
- **Vida del desafío:** valor PROVISIONAL del prototipo en la tabla «Parámetros provisionales del prototipo».

### D-055 — Umbrales y calibración
**Estado:** APROBADA (estrategia) / **valores de similitud y anti-spoofing fijados provisionalmente el 2026-09-27** / calibración PENDIENTE (D-060) / demás valores PENDIENTES DE VALIDACIÓN

- **Valores fijados por el equipo (2026-09-27), escala [0,1]:** similitud de voz mínima (coseno, ECAPA-TDNN) **0.80**; probabilidad bona fide mínima del anti-spoofing (AASIST) **0.90**. Es decir, 80 % = 0.80 y 90 % = 0.90; no se interpretan como 80 ni 90. Aplicados en `Nayra-Voz/config/parametros_provisionales.yaml` (commit `ca98bb3`). Son **provisionales hasta la calibración estadística** de D-060: no provienen de ningún estudio de Nayra y no deben presentarse como resultados. Su efecto real no se ha medido: ECAPA, AASIST y Vosk reales no se ejecutaron en el entorno actual.

- Umbrales de similitud, anti-spoofing, calidad de audio y confianza del reconocimiento en **configuración versionada junto con el modelo**; **nunca en el código** (`05_BIOMETRIA_NAYRA.md` §12).
- **Umbral provisional:** para que el prototipo funcione (D-034), se fija un valor provisional a partir de un piloto pequeño con consentimiento, registrado aquí como PROVISIONAL con el piloto que lo respalda.
- **Autorización del 2026-09-27:** mientras no exista el piloto, el equipo autorizó valores provisionales **sin estudio que los respalde** para construir el prototipo funcional, marcados `PROVISIONAL — PENDIENTE DE VALIDACIÓN` (tabla «Parámetros provisionales del prototipo»). No son resultados de Nayra ni sustituyen al piloto ni a la calibración.
- **Valor definitivo:** calibración con voluntarios (D-060), conjuntos de desarrollo y prueba separados, curvas DET/ROC, FAR, FRR y EER; punto de operación orientado a **baja FAR** (contexto de billetera).
- No se reduce el umbral para aceptar voces alteradas (`05_BIOMETRIA_NAYRA.md` §26.5).
- Normalización de puntajes (AS-norm) solo si la calibración la justifica.

### D-056 — Responsable de aplicar los umbrales técnicos
**Estado:** APROBADA (AG-12, 2026-09-27). Los valores numéricos de los umbrales **no** quedan aprobados por esta decisión: los de similitud y anti-spoofing se fijaron provisionalmente en D-055 (0.80 / 0.90) y todos siguen pendientes de calibración (D-060).

- El **servicio Python/FastAPI** aplica los umbrales técnicos (calidad, confianza del reconocimiento, anti-spoofing y similitud) dentro del pipeline biométrico, con los valores versionados junto con el modelo.
- Python realiza las evaluaciones técnicas y devuelve el **veredicto técnico de cada etapa** junto con sus puntajes.
- **Spring Boot** toma la **decisión final de autenticación** y mantiene el control de intentos (D-044), bloqueo, sesión (D-018) y auditoría (`05_BIOMETRIA_NAYRA.md` §5).
- Motivo: el umbral cambia con el modelo; mantenerlo junto al modelo evita desincronizar dos servicios (y dos instancias, D-023).
- **Descartada:** que Python devuelva solo puntajes y Spring Boot aplique todos los umbrales.

### D-057 — Formato y captura de audio
**Estado:** APROBADA (formato) / duraciones PENDIENTES DE VALIDACIÓN

- **WAV PCM, 16 kHz, mono, 16 bits, sin compresión con pérdida** (AAC/Opus eliminan artefactos que usa el anti-spoofing).
- Duración mínima y máxima de la muestra: a calibrar (D-055).
- Captura controlada manualmente por el usuario (HU-28, HU-42, HU-62).

### D-058 — Control de calidad de audio
**Estado:** APROBADA (etapa) / herramienta de VAD: candidata PROVISIONAL (2026-09-27) / valores PENDIENTES DE VALIDACIÓN

- Etapa previa en el servicio Python: voz neta (detección de actividad de voz), relación señal/ruido estimada y saturación.
- Devuelve un motivo que la app convierte en instrucción accesible (HU-31, HU-32, HU-44).
- Opción sin dependencia nueva para la VAD: el modelo de VAD de SpeechBrain (a evaluar).
- **Candidata PROVISIONAL (2026-09-27):** **WebRTC VAD** (paquete `webrtcvad-wheels`) como herramienta inicial del prototipo. **No es definitiva**; sus parámetros son PROVISIONALES — PENDIENTES DE VALIDACIÓN.

### D-059 — Orden del pipeline y enrolamiento
**Estado:** APROBADA / número de muestras PENDIENTE DE VALIDACIÓN

- Orden en el servicio Python: **calidad → contenido del desafío → anti-spoofing → verificación 1:1**. En operación se detiene en la primera etapa fallida (ahorra cómputo y no ofrece un "oráculo" biométrico a un atacante). En modo de evaluación se registran todos los puntajes de forma anonimizada.
- Enrolamiento (D-052, paso 11): **3 muestras válidas** como valor inicial (hasta 5 si alguna falla), cada una con calidad, contenido y anti-spoofing; se descartan muestras muy alejadas del resto; se guarda el centroide (D-013).

### D-060 — Dataset de calibración y consentimiento
**Estado:** PENDIENTE DE DECISIÓN

Debe definirse: número de voluntarios (incluidas personas con discapacidad visual), protocolo de consentimiento informado, almacenamiento de las grabaciones de investigación **separado del sistema**, uso de ataques generados (TTS/VC) solo con voluntarios que consientan, y borrado al terminar la tesis.

### D-061 — PIN de 6 dígitos como credencial de conocimiento
**Estado:** APROBADA — **modifica D-037** (la "contraseña" se concreta como PIN)

- La credencial que el usuario conoce es un **PIN numérico de 6 dígitos**, creado en el registro (D-052, paso 9). No se agrega otra credencial.
- Se aplican todas las reglas de D-037 sobre la contraseña: no identifica la cuenta; solo hash seguro; nunca texto plano, audio ni logs; no es muestra biométrica.
- Validación **solo en el servidor**; el PIN nunca se guarda en el teléfono.
- El espacio de 10⁶ combinaciones exige, además del hash, un secreto del servidor (*pepper*) y límite estricto de intentos; algoritmo y parámetros siguen en **D-047 (PENDIENTE)**.
- Ingreso accesible: teclado numérico propio con distribución fija, cada tecla etiquetada y anuncio solo del avance ("3 de 6 dígitos"), nunca de los dígitos; sin aleatorizar teclas.
- **Dictado del PIN — APROBADO (2026-09-27):** el PIN puede dictarse por voz además de ingresarse con el teclado. Sigue siendo una **credencial de conocimiento, no biometría**: el audio del dictado nunca entra al pipeline biométrico, y ni el PIN, ni el audio ni la transcripción se almacenan en texto plano ni aparecen en logs. Riesgo conocido: dictarlo en voz alta puede exponerlo a terceros (HU-118). Reconocimiento: D-046. El PIN y el desafío de voz siguen siendo pasos separados y no se fusionan (D-054). D-061 se mantiene como modificación de D-037, con origen AG-01.
- **PENDIENTE:** rechazo de PIN triviales (000000, 123456…).
- El texto de HU-118 se conserva sin cambios; su interpretación bajo esta decisión consta en `01_REQUISITOS_NAYRA.md` §13.

### D-018 — Sesiones (actualización parcial)
Ver la actualización en la sección D-018: **cierre automático tras 5 minutos de inactividad**, controlado en el servidor, sin aviso por voz previo, sin opción de continuar y con tiempo no ajustable por el usuario. Mecanismo técnico pendiente. _(Actualización del modelo de datos v4, 2026-09-27: mecanismo JWT con `jti` = `sesiones.id`, sin renovación; P-5 pendiente. Ver la sección D-018.)_

### D-016 — Despliegue del servicio de voz (sin cambio de estado)
**Estado:** PENDIENTE. Estimación del análisis A–K, a medir: contenedor o VM solo CPU (2–4 vCPU, 4 GB RAM). Falta decidir si el servicio Python se replica en las 2 instancias de D-023.

### Parámetros provisionales del prototipo (autorizados el 2026-09-27)

Todos los valores de esta tabla son **PROVISIONAL — PENDIENTE DE VALIDACIÓN**. Se autorizaron solo para construir el prototipo funcional (D-034, D-055), no provienen de ningún estudio y no deben presentarse como resultados. Los del servicio de voz viven en `Nayra-Voz/config/parametros_provisionales.yaml` (versionados con el modelo, D-055); los de Spring Boot, en `Nayra-Back/src/main/resources/application-prototipo.properties` (voz) y `application.properties` (nonce y registro en curso, prefijo `nayra.`).

| Parámetro | Valor provisional | Dónde | Decisión |
|---|---|---|---|
| Vida del desafío | 120 s | Spring Boot | D-054 |
| Duración mínima / máxima de la muestra | 1,5 s / 20 s | Python (la app corta a 20 s) | D-057 |
| VAD (WebRTC): agresividad / trama | 2 / 30 ms | Python | D-058 |
| Voz neta mínima | 1,0 s | Python | D-058 |
| Relación señal/ruido mínima | 10 dB | Python | D-058 |
| Saturación máxima | 1 % de las muestras | Python | D-058 |
| Confianza mínima por palabra (Vosk) | 0,60 | Python | D-046, D-055 |
| Probabilidad mínima de voz genuina (AASIST) | **0,90** (fijado por D-055 el 2026-09-27, escala [0,1]; antes 0,50) — provisional hasta D-060 | Python | D-012, D-055 |
| Similitud coseno mínima (ECAPA-TDNN) | **0,80** (fijado por D-055 el 2026-09-27, escala [0,1]; antes 0,25, valor por defecto de SpeechBrain) — provisional hasta D-060 | Python | D-011, D-055 |
| Muestra atípica en el enrolamiento | similitud < 0,50 con el centroide del resto | Python | D-059 |

**Parámetros autorizados provisionalmente para el prototipo (2026-09-27).** Se mantienen solo para esta entrega; **no** son decisiones aprobadas y ninguna decisión D los fija:

| Parámetro | Valor provisional | Dónde | Decisión |
|---|---|---|---|
| Vida del nonce del dispositivo | 60 s | Spring Boot (`nayra.dispositivo.vida-nonce`) | D-048 |
| Vida de la transacción de inicio de sesión | 5 min | Spring Boot | D-037 |
| Timeout de conexión / lectura del cliente del servicio de voz | 2 s / 30 s | Spring Boot | D-010 |
| Vida máxima de un enrolamiento incompleto | 900 s | Python | D-059 |
| Vida del código de registro en curso | 900 s | Spring Boot (`nayra.registro.vida-registro-en-curso`) | Relacionada con D-052, sin ser decisión de D-052 |

### Mecanismos técnicos provisionales del prototipo (autorizados el 2026-09-27)

Estos mecanismos pueden usarse en el prototipo, pero son **PROVISIONALES**: no son decisiones aprobadas y **no cierran** las decisiones relacionadas, que siguen en su estado.

| Mecanismo | Decisión relacionada (sigue en su estado) | Qué sí está definido |
|---|---|---|
| ~~Sesión con token opaco aleatorio; solo se guarda su hash~~ — **reemplazado** por el modelo de datos v4: JWT con `jti` = `sesiones.id` en `Authorization: Bearer`, sin guardar el token; HS256 con clave de `NAYRA_JWT_CLAVE` (algoritmo y custodia PROVISIONALES, P-5) | D-018 (PARCIAL) | Cierre tras 5 min de inactividad; JWT con `jti` y tabla `sesiones`; sin renovación |
| El ADMIN se autentica con dispositivo + PIN + voz (acceso provisional del prototipo) | D-050 (APROBADA FUNCIONALMENTE: usuario y contraseña en el panel web; no implementada; modelo de la credencial administrativa PENDIENTE) | Que el administrador usará usuario y contraseña |
| Ruta de arranque del primer ADMIN (perfil `prototipo`). Separada del registro asistido: **no cumple** D-052 porque el primer ADMIN no pasa por la validación de un representante | D-050 (no implementada); relacionada con D-052 | — |
| Código de registro de un solo uso para pasar del representante al celular de la persona | D-052 (registro técnico pendiente, categoría B) | Los 13 pasos del flujo |
| Solo un ADMIN actúa como representante | D-052 | D-052 admite un administrador u otra persona autorizada; queda pendiente si puede ser otra persona |
| Un ADMIN no puede bloquearse ni revocar su propio dispositivo (regla añadida para no dejar el prototipo sin administrador; no proviene de D-041) | — | — |
| Formatos: DNI de 8 dígitos; CE alfanumérico de hasta 30 caracteres (modelo v4) | D-035, P-2 (provisional) | Los datos, no el formato |
| Organización de rutas `/api/v1/...` para la API general (propuesta, no contrato) | D-014 (PENDIENTE) | — |
| Cancelar el registro si la persona no confirma sus datos | D-052 | Que la persona confirma sus datos (paso 7) |
| El primer ADMIN puede no tener cuenta financiera (no es una exención definitiva) | D-025 | Cada usuario tiene una única cuenta financiera |
| Mecanismo de autorización por rutas con denegación por defecto | D-009 (APROBADA solo en la representación de roles) | Roles USER y ADMIN (D-041); un rol por usuario como valor fijo (D-009, 2026-09-27) |

Valores ya aprobados que el prototipo usa sin cambios: 3 intentos de PIN (D-044), WAV PCM 16 kHz mono 16 bits (D-057) y umbrales de similitud y anti-spoofing 0.80 / 0.90 (D-055, provisionales hasta D-060). El prototipo usa además 3 muestras válidas como valor inicial (hasta 5), provisional y pendiente de validación según D-059.

### Alternativas descartadas el 2026-09-27

| Alternativa | Decisión | Motivo |
|---|---|---|
| NVIDIA NeMo TitaNet-Large | D-011 | Dependencias pesadas para un servicio pequeño; entrenado solo con inglés |
| Resemblyzer / GE2E | D-011 | Menor precisión |
| pyannote embedding | D-011 | Modelo con acceso restringido |
| APIs comerciales de voz | D-011, D-046 | Costo y datos biométricos a terceros (D-034) |
| RawNet2 como detector principal | D-012 | Inferior a AASIST en los mismos datos |
| Detectores de deepfake de terceros sin procedencia clara | D-012 | Calidad, licencia y datos de entrenamiento difíciles de justificar |
| Reconocedor del sistema operativo para verificar el desafío | D-046 | No controlable desde el backend (`06` §3.4) |
| gRPC / colas de mensajes | D-010 | Complejidad sin beneficio (`02` §19) |
| Modelos en Java con ONNX | D-010 | Contradice D-002 |
| Plugin de firma con huella/rostro del sistema | D-048 | Agrega otra credencial |
| Clave del dispositivo generada en Dart | D-048 | Clave privada exportable |
| RSA / Ed25519 para la clave del dispositivo | D-048 | Firmas grandes (RSA) / compatibilidad de dispositivos (Ed25519) |
| Adaptación automática de la plantilla de voz | D-013 | Riesgo de envenenamiento de la plantilla |
| Guardar audio de enrolamiento o autenticación | D-013 | Minimización (`05` §16) |

## Decisiones aprobadas el 2026-09-27 — Modelo físico de la base de datos (D-051, D-009)

Origen: propuesta `/mnt/project-files/analisis/PROPUESTA_MODELO_FISICO_D051_D009_2026-09-27.md`, revisada y aprobada por el equipo el 2026-09-27 (tras la auditoría `AUDITORIA_MODELO_DATOS_2026-09-27.md`). Nombres, tipos y restricciones de cada tabla en `03_BASE_DE_DATOS_NAYRA.md` §17.

### D-051 — Estrategia de migraciones de base de datos
**Estado:** APROBADA (AG-10)

- **Herramienta:** Flyway con migraciones en SQL plano versionado (`V001__...sql`). Spring Boot ejecuta las migraciones del esquema general al arrancar (`Nayra-Back/src/main/resources/db/migration/nayra`). Hibernate usa `ddl-auto=validate`; **nunca** `update`.
- **Esquema general:** `nayra`. No se usa `public`.
- **Identificadores:** UUID v4, columna PostgreSQL `uuid`, generado por la aplicación con `UUID.randomUUID()`.
- **Nombres físicos:** tablas y columnas en `snake_case`, minúsculas y sin tildes; se conservan los nombres documentados (`AUDITORÍA` → `auditoria`; `registro_identidad_simulado` se mantiene). Columnas nuevas: `codigo_cuenta`, `numero_celular`.
- **FK:** `ON DELETE RESTRICT ON UPDATE RESTRICT` por defecto. Sin `CASCADE` ni `SET NULL` salvo decisión explícita posterior.
- **Usuarios de base de datos:** el usuario de migración es distinto del usuario de ejecución. El usuario de ejecución no tiene `UPDATE` ni `DELETE` sobre `auditoria` (solo inserción).
- **Biometría:** `biometria` sigue como esquema separado, con **historial de migraciones propio** y usuario de migración distinto del de ejecución. **Sin FK física** entre `biometria.perfiles_voz.usuario_id` y `nayra.usuarios.id` por ahora: referencia lógica, para preservar la separación de D-013. La estructura física de `perfiles_voz` **sigue pendiente** y no se crea todavía.
- **Tablas implementadas al aprobar D-051 (V001–V004):** `entidades_bancarias`, `registro_identidad_simulado`, `usuarios`, `cuentas`, `dispositivos`, `auditoria`. En ese momento no se creaban `sesiones`, `operaciones`, `solicitudes_atencion`, `notificaciones`, almacenamiento de desafíos y nonces, identificador del QR ni `biometria.perfiles_voz`.
- ~~PROVISIONAL: la columna `usuarios.intentos_fallidos` depende del detalle de D-044.~~ Retirada por el modelo v4: el contador vive en `nayra.credenciales` (V006).
- **Actualización del modelo de datos v4 (2026-09-27):** V005–V011 añaden documento DNI/CE y estados `ACTIVO`/`BLOQUEADO`/`INACTIVO` (V005), `credenciales` (V006, con `pin_hash` e `intentos_fallidos` retirados de `usuarios`), `codigo_qr` PROVISIONAL y moneda `PEN` (V007), plataforma solo `ANDROID` (V008), `sesiones` (V009), `operaciones` y `notificaciones` (V010) y permisos por servicio (V011). El esquema `biometria` tiene V001–V003 con `perfiles_voz`. **Siguen sin crearse:** `roles`, `solicitudes_atencion`, almacenamiento de desafíos y nonces, historial de embeddings y tablas nuevas de auditoría.
- **PENDIENTE (P-4):** qué servicio ejecuta Flyway en `nayra` y en `biometria`, con qué usuario, permisos definitivos por servicio y orden de despliegue. Hoy Spring Boot ejecuta las de `nayra` al arrancar; las de `biometria` solo se ejecutan en pruebas.

**No aprobado por esta decisión:** versión de PostgreSQL, despliegue de la base de datos (D-016), retención y copias de seguridad (`03` §13), creación de los usuarios de base de datos y sus credenciales (D-017).

### D-009 — Autorización (representación de roles)
**Estado:** APROBADA (AG-03) — opción A

- El rol es un **valor fijo**: `usuarios.rol varchar(10) NOT NULL CHECK (rol IN ('USER', 'ADMIN'))`.
- **No** se crea la tabla `ROLES`. **No** hay varios roles por usuario.
- En Java se mantiene el enum `Rol`, mapeado como `EnumType.STRING`.

## Decisiones del modelo de datos v4 (2026-09-27)

Origen: `/mnt/project-files/analisis/MODELO_DATOS_NAYRA_COMPLETO_2026-09-27.md` (versión 4), decidido por el equipo el 2026-09-27, y decisiones posteriores del mismo día (G-1, P-2, D-050, D-055). Implementado en los commits `5bbd505` y `ca98bb3` de `yuniv` (todavía sin push). Los identificadores P-, B-, G-, E- y H- son los del documento v4 y de la auditoría `AUDITORIA_CONSISTENCIA_V4_2026-09-27.md`; no son decisiones D nuevas. Sin AG temático asignado. Detalle físico en `03_BASE_DE_DATOS_NAYRA.md` §17.

### Decisiones aprobadas

| Tema | Decisión | Decisión D relacionada |
|---|---|---|
| Credencial del PIN (P-1) | Tabla `nayra.credenciales` del servicio de autenticación, 1:1 con `usuarios` (`UNIQUE(usuario_id)`), con `pin_hash` e `intentos_fallidos`. `usuarios` ya no contiene `pin_hash` ni `intentos_fallidos`. `pin_hash` no sale del servicio de autenticación ni se expone en ninguna API | D-061, D-047, D-044 |
| Roles | Sin tabla `roles`: `usuarios.rol` con `USER`/`ADMIN` | D-009, D-041 |
| Documento de identidad | `tipo_documento_identidad` (`DNI`, `CE`) + `numero_documento varchar(30)`, `UNIQUE(tipo, número)` en `usuarios` y `registro_identidad_simulado`. Donde AG-01 dice "DNI", el dato físico es el documento de identidad | D-035, D-053 |
| P-2 (cerrada provisionalmente) | No se usan APIs externas para validar el documento; la validación es local/simulada. El formato por tipo de documento queda provisional | D-035 |
| Estados de la cuenta de acceso | `ACTIVO`, `BLOQUEADO`, `INACTIVO` | D-044, D-041 |
| Intentos | Solo el PIN incorrecto cuenta; límite 3; bloqueo y revocación de sesiones al tercero | D-044 |
| Reinicio del contador (H-01, cerrada) | `intentos_fallidos` vuelve a 0 inmediatamente cuando el PIN es correcto ("PIN correcto → 0"), antes del paso de voz. Coincide con el código actual | D-044 |
| Reglas de BD confirmadas (H-02, cerrada) | Se confirman formalmente las seis reglas que v4 marcaba "(a confirmar)", ya aplicadas en las migraciones: CHECK `intentos_fallidos BETWEEN 0 AND 3`; `DEFAULT 'PEN'` en `cuentas` y `operaciones`; `CHECK (fecha_ultimo_acceso >= fecha_creacion)` en `sesiones`; `CHECK (leida = (fecha_lectura IS NOT NULL))` y `UNIQUE (operacion_id, destinatario_id)` en `notificaciones`; índices de `sesiones` y `operaciones` | D-051 |
| Alcance de auditoría (H-03, cerrada) | Se mantiene el alcance de `01` §12.4: HU-80, HU-89, HU-93, HU-94, HU-102 y HU-121 siguen en el primer entregable y las consultas administrativas de auditoría existentes permanecen. Sin tablas nuevas de auditoría | D-034, D-041 |
| Estados de operación (HU-87) | `EXITOSO`, `FALLIDO` y `CANCELADO`; no existe estado pendiente | D-029 |
| Moneda | `PEN` en `cuentas` y `operaciones` | D-021 |
| QR | `cuentas.codigo_qr` fijo y `UNIQUE`; valor aleatorio PROVISIONAL; formato definitivo P-3 | D-042 |
| Plataforma | Solo Android (APK) en el primer entregable; iOS fuera de alcance | D-048, D-007 |
| Servicios | Tres servicios por responsabilidad: **Negocio** (usuarios, cuentas, entidades, registro de identidad, operaciones, notificaciones), **Autenticación** (`credenciales`, `dispositivos`, `sesiones`; desafíos y nonces en memoria; decisión final de autenticación, D-056) y **Biométrico** (Python/FastAPI, `biometria.perfiles_voz`). Hoy Negocio y Autenticación son **una sola aplicación Spring Boot**. Separación lógica: **no** sustituye a las 2 instancias de D-023 ni significa 3 réplicas. PostgreSQL compartido | D-023, D-002, D-010, D-056 |
| Sesiones | JWT con `jti` = `sesiones.id`; tabla `sesiones` de 6 columnas; el token no se guarda; 5 min de inactividad; sin renovación ni refresh token; el servicio de autenticación es la autoridad de la sesión | D-018 |
| Operaciones | Solo `TRANSFERENCIA`; estados `EXITOSO`/`FALLIDO`/`CANCELADO`; `codigo_referencia` de 6 dígitos único global; canal `MOVIL`; monto mayor que 0 y menor que 500 (0.01 a 499.99); origen y destino obligatorios y distintos | D-029, D-030 |
| Destinatario (G-1, modificada el 2026-09-28) | ~~El backend usa el ID interno de la cuenta destino; el frontend lo usa para obtener y mostrar al destinatario.~~ Se busca por el **celular registrado en Nayra** (único, formato canónico de Perú); el ID interno de la cuenta destino solo lo usa el backend. Transferencias todavía **no implementadas** | D-042, D-043 |
| Notificaciones | Solo tipo `OPERACION`; `operacion_id` obligatorio; texto "Nombres Ape... te realizó una transferencia de S/ …" | — |
| Biometría | `biometria.perfiles_voz`: un perfil por usuario, embedding cifrado, sin audio (B-1 a B-13) | D-013 |
| Administrador (D-050) | Aprobada funcionalmente: usuario y contraseña en el panel web. **No implementada**: el modelo y el almacenamiento de la credencial administrativa siguen pendientes. El prototipo mantiene provisionalmente el acceso ADMIN con dispositivo + PIN + voz | D-050, D-045 |
| Umbrales (D-055) | Similitud 0.80 y bona fide 0.90, escala [0,1], provisionales hasta D-060 | D-055 |
| Fuera del primer modelo | Tablas `roles`, `solicitudes_atencion`, desafíos y nonces, historial de embeddings y tablas nuevas de auditoría | — |

### Correcciones técnicas de desarrollo (E-01, E-02, E-03)

Registradas como decisiones técnicas de implementación (categoría B), sin cambios de esquema:

- **E-01 — contador del PIN:** incremento atómico con `UPDATE … SET intentos_fallidos = intentos_fallidos + 1 … WHERE intentos_fallidos < 3` y lectura en la misma transacción. Dos PIN incorrectos simultáneos cuentan como dos; el contador nunca pasa de 3.
- **E-02 — sesiones:** alta con `crear` (rechaza un id existente); revocación y registro de acceso con UPDATE condicionales (`fecha_revocacion IS NULL`, último acceso dentro del límite de inactividad, fecha que nunca retrocede). Una sesión revocada no puede reabrirse por una escritura obsoleta. No se añadió `@Version`: exigiría una séptima columna y `sesiones` mantiene 6.
- **E-03 — monto:** `BigDecimal` validado antes de persistir: escala ≤ 2 (sin redondeo; `MONTO_ESCALA_INVALIDA`), precisión ≤ 15 y 0 < monto < 500 (`MONTO_FUERA_DE_RANGO`). No se depende del redondeo implícito de `numeric(15,2)` en PostgreSQL.

Pruebas en `ConcurrenciaPostgresTest`, `ModeloDatosV4Test` y `ModeloDatosV4PostgresTest`.

### Pendientes que se mantienen

| ID | Pendiente | Tipo |
|---|---|---|
| P-3 | Formato definitivo del QR | No bloqueante |
| P-4 | Servicio que ejecuta Flyway (`nayra` y `biometria`), permisos definitivos por servicio y orden de despliegue | No bloqueante; antes de las migraciones definitivas |
| P-5 | `exp`, claims definitivos, algoritmo definitivo, custodia de la clave de firma y número máximo de sesiones | No bloqueante; antes de producción |
| P-7 / D-059 | Rango definitivo de muestras de enrolamiento | No bloqueante |
| P-8 | Límite propio de reintentos biométricos | No bloqueante |
| P-10 | Orquestación definitiva del registro asistido entre servicios | No bloqueante |
| P-11 | Mecanismo de desbloqueo administrativo y si reinicia el contador | No bloqueante |
| B-4 | AAD definitiva del cifrado biométrico | No bloqueante |
| B-5 | Frecuencia de rotación de claves | No bloqueante |
| — | CHECK de 784 bytes de `embedding_cifrado` (tras ejecutar ECAPA real) | Deuda técnica, no es una decisión abierta |
| — | Comentario SQL de V010 sobre `operaciones` que todavía dice "G-1 PENDIENTE BLOQUEANTE" (G-1 está decidida; V010 no se edita y V012 no toca ese comentario) | Deuda documental |
| D-050 | Modelo y almacenamiento de la credencial administrativa | Pendiente |

Siguen también pendientes, sin cambios: D-047 (hash del PIN y *pepper*), D-014 (contrato de API), D-046 en lo que permanece abierto (comando "Iniciar sesión Nayra", tecnología definitiva del PIN dictado y del documento), D-060 (dataset y calibración), D-061 en el rechazo de PIN triviales y D-045 (panel web).

## Destinatario por celular (G-1 modificada, 2026-09-28)

Decidido por el equipo el 2026-09-28 (aprobado y cerrado ese mismo día) tras la auditoría `AUDITORIA_G1_DESTINATARIO_CELULAR_2026-09-28.md`. Actualiza D-042 y D-043 (ver sus secciones). Alcance: solo la búsqueda y resolución del destinatario; la transferencia sigue sin implementarse.

| Tema | Decisión |
|---|---|
| Identificación | Celular registrado en Nayra; el ID interno de la cuenta destino queda en el backend |
| Unicidad | `usuarios.numero_celular` `UNIQUE` (V012); el registro rechaza un número ya registrado (`CELULAR_REGISTRADO`) |
| Formato | Perú: 9 dígitos que empiezan por 9; `+51` se quita antes de validar, guardar y buscar |
| Ingreso | Teclado grande y accesible |
| Confirmación | Primer nombre + primer apellido parcial con partículas (ver «Nombre parcial del destinatario»); "¿Desea transferir a María Sala...?"; botones **Sí** y **No, buscar otro número** |
| Titularidad | No se valida con operadores ni OSIPTEL; solo se afirma que existe una cuenta Nayra asociada al número |
| Agenda del teléfono | Fuera del primer entregable (D-042) |
| Privacidad | Número sin cuenta, usuario bloqueado o inactivo y sin cuenta financiera activa responden igual: "No hay una cuenta Nayra asociada a ese número." (intencional; confirmado por Yuni el 2026-09-28) |
| Estado | Aprobada y cerrada por Yuni el 2026-09-28 |

Implementación **provisional** hasta el contrato de D-014: `POST /api/v1/destinatarios/busqueda {celular}` con sesión → `{nombreVisible}`; errores `CELULAR_INVALIDO` (400), `DESTINATARIO_NO_ENCONTRADO` (404), `CUENTAS_IGUALES` (409, número propio).

### Nombre parcial del destinatario (Yuni, 2026-09-28)

Sustituye a "primer nombre + tres primeras letras del primer apellido". Lo genera solo el backend (`OperacionesServiceImplement.nombreVisible`); la app lo muestra tal cual y el lector de pantalla lee la misma porción sin los puntos suspensivos.

- Se usa el primer nombre y el **primer apellido**; el segundo apellido no se muestra.
- El primer apellido incluye sus partículas iniciales **De, Del, La, Las, Los** (sin distinguir mayúsculas; se conserva la escritura original) y la palabra que las sigue.
- Se muestran sus **cuatro primeras letras**, sin contar espacios, seguidas de "...". Si el corte cae dentro de una partícula, la partícula se muestra completa.
- Si el primer apellido tiene **cinco letras o menos**, se muestra entero y sin "...".

| Apellidos | Se muestra | Se lee |
|---|---|---|
| Pérez | María Pérez | María Pérez |
| Salazar | María Sala... | María Sala |
| De la Cruz | María De la... | María De la |
| Del Río | Juan Del R... | Juan Del R |
| De Los Santos | Ana De Los... | Ana De Los |

Los cinco ejemplos son los de Yuni. Yuni confirmó la regla como definitiva el 2026-09-28 (cuatro letras significativas, entero con cinco letras o menos); un apellido de seis letras se acorta ("Torres" → "Torr..."). El texto de la notificación de transferencia recibida (`Notificaciones.contenidoTransferencia`) sigue con tres letras: queda fuera de este cambio y se alineará al implementar HU-77 y las transferencias.

**Pendientes de esta decisión:**

| Pendiente | Tipo |
|---|---|
| Dictado del número del destinatario (reconocimiento de voz) | Pendiente, junto con D-046 / Nayra-Voz |
| Respuesta hablada "Sí/No" a la confirmación y voz propia de Nayra | Pendiente, junto con D-046 |
| Medidas contra la enumeración de números (límite de búsquedas por sesión, auditoría de búsquedas). Hoy solo: sesión obligatoria y la misma respuesta para "sin cuenta" y "cuenta que no puede recibir" | Pendiente de decisión |
| Qué dato identifica al destinatario en la solicitud de transferencia (el celular otra vez o una referencia devuelta por la búsqueda). Hoy la búsqueda no devuelve identificadores | Pendiente; con el contrato de transferencias (D-014) |
| Apellidos compuestos ("De la Cruz") en el nombre parcial | Cerrado el 2026-09-28 (ver «Nombre parcial del destinatario») |
| Alinear el nombre parcial de la notificación de transferencia recibida (hoy tres letras) | Pendiente; con HU-77 y las transferencias |
| Tratamiento de datos existentes con celular repetido o fuera de formato: V012 se detiene y no los modifica | Se decide solo si aparecen |

## Decisiones aprobadas el 2026-09-29 — Despliegue futuro en GCP

Yuni aprobó estas decisiones el 2026-09-29, después de la auditoría final de despliegue (`docs/despliegue/AUDITORIA_FINAL_DESPLIEGUE_GCP_2026-09-29.md`).

Alcance de estas decisiones:
- Describen la **arquitectura objetivo en GCP, que es una etapa futura**. No se ha creado ningún recurso en GCP.
- Aprobar una decisión **no** equivale a implementarla. Las que exigen código (D-064, D-065, D-067) siguen **sin implementar**.
- El entorno local de desarrollo (Docker Compose en una PC) es solo una forma de ejecutar el sistema y **no modifica** estas decisiones.

### D-062 — Región de despliegue

**Estado:** APROBADA (2026-09-29).

Todo se despliega en **`us-east1`**.

Motivo: API Gateway no está disponible en ninguna región de Sudamérica. Detalle en la auditoría, §D-2.

### D-063 — Topología objetivo en GCP

**Estado:** APROBADA (2026-09-29). Concreta D-015, D-016 y D-020 para GCP y es compatible con D-023. **No desplegada.**

Recorrido de una petición:

`Internet → API Gateway → balanceador de aplicaciones externo regional → firewall → VPC → subred de solo proxy (la DMZ) → grupo de instancias administrado regional en 2 zonas → Cloud SQL (PostgreSQL 16) con IP privada`.

Reglas:
- **2 instancias en zonas diferentes** (D-023). Nayra-Back y Nayra-Voz corren en la misma VM. Nayra-Voz solo es accesible por la red interna (D-010).
- La separación entre Negocio, Autenticación y Biométrico sigue siendo **lógica**: no hay tres grupos de servidores.
- **Costos:**
  - En desarrollo (fase A) hay **1 instancia activa**. Esto es una configuración temporal y **no** permite afirmar alta disponibilidad.
  - En las pruebas de disponibilidad (fase B) hay 2 instancias activas.
  - La fase B requiere que D-064 y D-065 estén implementadas.
- **Base de datos:** Cloud SQL se crea como una base nueva, **sin migrar datos locales**.

Con esta decisión, API Gateway, el balanceador, la VPC y la DMZ dejan de ser «componentes no asumidos» (`CLAUDE.md` §4.2, `08` §7.2), pero **solo** en la forma que describe D-063.

### D-064 — Estado temporal compartido en Nayra-Back (R-1)

**Estado:** APROBADA (2026-09-29); **NO IMPLEMENTADA**. Modifica la exclusión de «tablas de desafíos o nonces» del modelo v4 (`CLAUDE.md` §8), solo para estas tablas.

Problema: el estado que hoy vive en memoria del proceso no se comparte entre las 2 instancias.

Solución aprobada: moverlo a PostgreSQL con **4 tablas temporales**, en la migración **V013** del esquema `nayra`:
- `nonces_dispositivo`
- `desafios`
- `transacciones_autenticacion`
- `registros_en_curso`

Cada fila vence según los tiempos provisionales que ya están aprobados. No se usa afinidad de sesión.

### D-065 — Enrolamiento pendiente compartido en Nayra-Voz (R-1)

**Estado:** APROBADA (2026-09-29); **NO IMPLEMENTADA**.

Se crea la tabla temporal **`biometria.enrolamientos_pendientes`** en la migración V004 del esquema `biometria`:
- Guarda los embeddings **cifrados con AES-GCM**, con la misma clave que usa el perfil de voz.
- Cada registro vive 900 s y se borra al finalizar el enrolamiento o al vencer.
- **No** es un historial de embeddings y no guarda audio (D-013).

### D-066 — Dominio y HTTPS

**Estado:** APROBADA (2026-09-29).

Ahora mismo **no** se compra ningún dominio. Yuni registrará uno en la etapa del balanceador y HTTPS, y el balanceador usará un **subdominio exclusivo** con un certificado administrado por Google.

### D-067 — Endpoint de salud

**Estado:** APROBADA (2026-09-29); **NO IMPLEMENTADA**.

Nayra-Back tendrá un endpoint de salud que compruebe tres cosas: el propio Back, PostgreSQL y Nayra-Voz.

Mientras no exista, en la fase A se acepta de forma temporal una comprobación TCP al puerto 8080.

### D-068 — Rutas expuestas y protección del balanceador

**Estado:** APROBADA (2026-09-29).

- El mapa de URLs del balanceador funciona como **lista blanca** desde la fase A. No publica Swagger ni endpoints internos.
- Antes de cualquier uso fuera de pruebas se debe **evaluar Cloud Armor**.

### D-069 — Versión de Java

**Estado:** APROBADA (2026-09-29).

Nayra-Back usa **Java 21**. El `pom.xml` pasa de 25 a 21, y Spring Boot 4.1.1 es compatible con esa versión.

### D-070 — Versión de Python y dependencias

**Estado:** APROBADA (2026-09-29).

Nayra-Voz usa **Python 3.11** con las versiones de dependencias **fijadas** en `requirements.txt`. PyTorch se instala en su versión para CPU.

### D-071 — Ejecución de las migraciones en GCP

**Estado:** APROBADA (2026-09-29). Resuelve la parte de P-4 que corresponde a **quién y dónde** se ejecutan las migraciones; los permisos definitivos siguen pendientes.

- Flyway se ejecuta como **tarea puntual**, una para `nayra` y otra para `biometria`.
- Se lanza desde la VM de la zona A, accediendo por IAP.
- Las instancias arrancan con `SPRING_FLYWAY_ENABLED=false`.

### D-072 — Tipo de máquina

**Estado:** APROBADA (2026-09-29).

Cada instancia es una VM **e2-standard-2**.

### D-073 — Clave antigua de Gemini

**Estado:** APROBADA (2026-09-29).

- La clave de Gemini que aparece en el historial de Git pertenece a un proyecto anterior y no forma parte de NAYRA.
- Yuni debe revocarla en la consola de Google.
- NAYRA **no** añade ninguna dependencia de Gemini.
- El resto de D-017 sigue como deuda técnica.

### D-074 — Empaquetado en contenedores

**Estado:** APROBADA (2026-09-29).

- Nayra-Back y Nayra-Voz se empaquetan en imágenes Docker, que son las mismas en local y en GCP.
- PostgreSQL corre en Docker **solo en local**; en GCP se usa Cloud SQL.
- La app Flutter recibe la dirección del backend con `NAYRA_BACKEND_URL` mediante `--dart-define`, sin URL fija en el código.

---

## Configuración del entorno local (2026-10-01)

Esta sección **no contiene decisiones de arquitectura** y no modifica D-062 a D-074. Registra solo la configuración que hizo falta para ejecutar NAYRA en una PC y probarlo desde un celular físico. Guía de uso: `docs/entorno-local/GUIA_ENTORNO_LOCAL.md`.

| ID | Tipo | Qué | Por qué | Impacto |
|---|---|---|---|---|
| LOC-01 | Configuración exclusiva del entorno local | En local, la tarea de migraciones usa la red de Compose: `NAYRA_RED_MIGRACIONES=nayra_default` y `NAYRA_DB_HOST=postgres` | En Docker Desktop (Windows), la red `host` es la de la máquina virtual de WSL2, no la de Windows | Ninguno en GCP: allí sigue la red `host` hacia Cloud SQL (D-071) |
| LOC-02 | Configuración exclusiva del entorno local | La compilación `profile` de Nayra-App permite HTTP sin TLS hacia la PC, igual que `debug` | `profile` compila AOT y es mucho más ligera que `debug`. Así se puede probar en celulares de gama baja o media sin el costo del modo depuración | La compilación `release` sigue sin permitir HTTP. El transporte seguro de producción sigue pendiente (06) |
| LOC-03 | Mejora de implementación, permanente | El botón de grabación captura los fallos del micrófono: descarta lo grabado, anuncia el error al lector de pantalla y permite reintentar. También ignora una segunda pulsación mientras el micrófono arranca o se detiene | Que la app no quede bloqueada ni se cierre por un error del sistema de audio (HU-47, HU-64) | No cambia el flujo ni la API |
| LOC-04 | Verificación, sin cambio | Java 21 se mantiene (D-069). El `pom.xml` declaraba 25 desde el primer commit (`af02e8b`), sin ninguna API de Java 22 o posterior en el código. Con 21 pasan 122 pruebas (1 omitida) contra PostgreSQL 16 | Estabilidad: 21 es LTS y es la versión de las imágenes | Ninguno |

**Hallazgo previo a las pruebas con voces reales (no es una decisión).**
- Una medición informal con los modelos reales (`GUIA_ENTORNO_LOCAL.md` §7) sugiere que los umbrales provisionales de D-055 pueden rechazar a usuarios legítimos:
  - ECAPA, mismo locutor: 0.70–0.73, frente al umbral de 0.80.
  - AASIST, voz real de corpus comprimido: 0.00–0.60, frente al umbral de 0.90.
- Los umbrales **no** se modificaron. Cualquier cambio corresponde a D-060 (calibración) y requiere una decisión.

---

## Agendas temáticas (AG)

Las agendas temáticas (AG) agrupan las decisiones por tema. Son distintas del **origen histórico** de cada decisión, es decir, la ronda en la que se decidió. AG-00 y AG-01 son **rondas históricas cerradas**: no se reabren y sus decisiones conservan su origen. El bloque aprobado el 2026-09-27 se denomina **revisión del 2026-09-27** y no tiene número de AG. La asignación temática no modifica el contenido, el estado ni la numeración de ninguna decisión.

| AG | Título | Decisiones (primarias) | Impacto secundario |
|---|---|---|---|
| AG-00 | Terminología, modelo financiero y depuración de requisitos (ronda histórica cerrada) | Origen histórico: D-024 a D-033 | — |
| AG-01 | Registro, autenticación, dispositivo, recuperación y alcance del primer entregable (ronda histórica cerrada) | Origen histórico: D-034 a D-053 (D-045, D-047 y D-051 tienen además AG temático propio o ninguno, ver filas siguientes). D-048 y D-061: origen AG-01; aprobada el 2026-09-27 | AG-11 para D-048 (anti-replay) y D-061 (hash del PIN) |
| AG-02 | Sesiones | D-018 | — |
| AG-03 | Autorización | D-009 | — |
| AG-04 | Modelo funcional de usuario | Sin decisiones asignadas actualmente | — |
| AG-05 | Estados de cuenta y sus efectos | Sin decisiones asignadas actualmente | — |
| AG-06 | Eliminación/ciclo de vida de datos | Sin decisiones asignadas actualmente | — |
| AG-07 | Modelo de roles | Sin decisiones asignadas actualmente | — |
| AG-08 | Convenciones y contrato de API | D-014 | — |
| AG-09 | Modelo CORE de base de datos | Sin decisiones asignadas actualmente | — |
| AG-10 | Migraciones de base de datos | D-051 | — |
| AG-11 | Seguridad | D-017, D-047 | Aspectos de seguridad de D-048 (anti-replay), D-054, D-061 (hash del PIN) y D-013, cuando corresponda |
| AG-12 | Frontera/responsabilidades Java ↔ Python | D-010, D-056 | D-057 y D-059, cuando corresponda a la frontera entre servicios |
| AG-13 | Biometría y autenticación por voz | D-005 (concretada en D-011), D-011, D-012, D-013, D-046 (solo la parte del desafío/reconocimiento de voz), D-054, D-055, D-057, D-058, D-059, D-060 | — |

**Sin AG temático por ahora:** D-007 (aplicación móvil, Flutter), D-045 (panel web del administrador) y D-062 a D-074 (despliegue futuro en GCP, 2026-09-29).

**Origen histórico en AG-01 sin referencias secundarias todavía:** D-040, D-041, D-044, D-049, D-050 y D-052.
