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

**Estado:** EN EVALUACIÓN

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

**Estado:** PENDIENTE

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

**Estado:** PARCIAL — roles aprobados en D-041; mecanismo técnico PENDIENTE

Los roles del sistema quedan fijados en **D-041** (USER y ADMIN). Siguen pendientes la representación de roles y permisos en el modelo de datos y el punto de aplicación técnica de la autorización.

Debe definirse el mecanismo mediante el cual se controlarán los permisos de los usuarios y administradores.

Debe mantener correspondencia con:

- roles;
- requisitos;
- backend;
- seguridad.

---

## D-010 — Comunicación Java ↔ Python

**Estado:** PENDIENTE

Debe definirse cómo se comunicará el backend principal con el componente especializado de voz.

Alternativas posibles deberán evaluarse antes de seleccionar una:

- API REST;
- otro mecanismo de comunicación;
- otra alternativa técnicamente justificada.

No implementar una alternativa como definitiva sin documentarla aquí.

---

## D-011 — Modelo biométrico

**Estado:** PENDIENTE

Debe definirse:

- modelo;
- representación de voz;
- método de comparación;
- estrategia de verificación;
- requisitos de rendimiento.

La selección debe basarse en la investigación realizada y en la capacidad de validación dentro del alcance del proyecto.

---

## D-012 — Modelo anti-spoofing

**Estado:** PENDIENTE

Debe definirse la estrategia concreta para detectar intentos de spoofing.

Se deben considerar las amenazas identificadas en `05_BIOMETRIA_NAYRA.md`.

---

## D-013 — Almacenamiento de información biométrica

**Estado:** PENDIENTE

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

La arquitectura física definitiva todavía debe definirse.

No asumir ninguna de las arquitecturas anteriormente descartadas.

La arquitectura aprobada deberá registrarse en `02_ARQUITECTURA_NAYRA.md`.

---

## D-016 — Despliegue cloud

**Estado:** PENDIENTE

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

**Estado:** PENDIENTE

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
**Estado:** APROBADA FUNCIONALMENTE

La autenticación del usuario se compone de **contraseña + verificación biométrica de voz 1:1 + anti-spoofing**. Flujo de inicio de sesión:

`"Iniciar sesión Nayra"` (solo activa el proceso) → el **dispositivo vinculado** determina la cuenta → contraseña → frase de **desafío variable** → comprobación del **contenido** del desafío → anti-spoofing → verificación biométrica **1:1** → sesión.

Reglas:

- **No** se utiliza identificación biométrica 1:N.
- El usuario **no** dicta su DNI en cada inicio de sesión.
- La contraseña **no** identifica la cuenta; la cuenta la determina el dispositivo vinculado.
- La contraseña se crea en el registro, puede dictarse, se almacena **solo mediante hash seguro**; nunca en texto plano, nunca como audio y nunca en logs.
- Ni el comando de activación ni la contraseña se usan como muestra biométrica; la muestra es la respuesta al desafío.

**Pendiente:** algoritmo de hash, política y normalización de la contraseña dictada (D-047); reconocimiento del habla (D-046); sesiones (D-018); autenticación del administrador (D-050). El algoritmo existente en el código (BCrypt) **no** se considera aprobado por existir.

### D-038 — Referencia biométrica persistente en backend
**Estado:** APROBADA COMO PRINCIPIO

La referencia biométrica permanece **asociada a la cuenta de acceso en el backend** y no depende exclusivamente del dispositivo. Tras un cambio de celular sigue disponible para la verificación 1:1.

**No aprobado:** dónde se almacena, su formato, el modelo que la genera ni si se guarda como embedding u otra representación (D-011, D-013 siguen PENDIENTES). No se almacena audio de voz innecesariamente.

### D-039 — Un único dispositivo activo por cuenta de acceso
**Estado:** APROBADA

- Una cuenta de acceso tiene **un solo dispositivo activo**; no se permiten varios dispositivos activos simultáneamente.
- El dispositivo se vincula al final del registro y determina la cuenta al iniciar sesión.
- **Reinstalación:** no se asume que la aplicación reconozca siempre el dispositivo tras reinstalarse. Si el vínculo puede verificarse, se continúa; si no, se usa el flujo de cambio de dispositivo/recuperación (D-040). No se desarrolla una solución compleja de identificación de dispositivos solo para la reinstalación.

**Pendiente:** mecanismo técnico de vinculación del dispositivo (D-048).

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

**Pendiente:** dato concreto con el que se busca/selecciona al destinatario; identificador que codifica el QR (`03_BASE_DE_DATOS_NAYRA.md` §16).

### D-043 — Número de celular
**Estado:** APROBADA

El número de celular forma parte del registro y se almacena como **dato de contacto** para procesos de atención y recuperación. **No** se implementan SMS reales, proveedores OTP, WhatsApp ni llamadas automáticas. Una eventual simulación de validación del número no es dependencia del primer entregable.

### D-044 — Política de 3 intentos
**Estado:** APROBADA (valor) / detalle PENDIENTE

Tras **3 intentos fallidos** de autenticación la cuenta de acceso se bloquea.

**Pendiente** antes de implementar: qué resultados cuentan como intento fallido (contraseña incorrecta, voz no coincidente, spoofing, mala calidad de audio); si hay un contador único o separado; ventana y reinicio del contador. Los **errores técnicos del servicio no se consideran intentos fallidos del usuario** salvo decisión expresa en contrario.

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
| D-046 | Reconocimiento del habla (comando, DNI, contraseña dictada, contenido del desafío): tecnología y ubicación (dispositivo o servidor) | PENDIENTE |
| D-047 | Algoritmo de hash, política de contraseña compatible con dictado y normalización de la contraseña dictada | PENDIENTE |
| D-048 | Mecanismo técnico de vinculación del dispositivo | PENDIENTE |
| D-049 | Procedimiento de recuperación asistida | PENDIENTE |
| D-050 | Autenticación del administrador | PENDIENTE |
| D-051 | Estrategia de migraciones de base de datos | PENDIENTE |

Siguen también **PENDIENTES**: D-007 (frontend móvil), D-010 (Java ↔ Python), D-011 (modelo biométrico), D-012 (anti-spoofing), D-013 (almacenamiento biométrico), umbral biométrico, D-014 (API), D-018 (sesiones) y D-019 (auditoría detallada). D-017 (secretos) pasa a **deuda técnica** (categoría C).

### Clasificación de lo pendiente (AG-01 v5)

Una deuda técnica pendiente **no** se convierte automáticamente en un bloqueo para todo el proyecto. Categorías:

- **A. Bloqueantes funcionales:** impiden implementar una funcionalidad del primer entregable hasta que se decidan.
- **B. Decisiones técnicas pendientes:** deben definirse para implementar correctamente una funcionalidad, pero pueden resolverse durante el desarrollo, al llegar a esa funcionalidad.
- **C. Deudas técnicas:** problemas conocidos que deben corregirse después y que no impiden continuar el desarrollo académico actual.

| Categoría | Decisiones | Estado de la clasificación |
|---|---|---|
| **A** | D-007 (app móvil), D-045 (panel web), D-046 (reconocimiento del habla), D-010 (Java ↔ Python), D-011 (modelo biométrico), D-012 (anti-spoofing), umbral biométrico, D-013 (almacenamiento de la referencia) | Propuesta para revisión |
| **B** | D-047 (hash y política de contraseña), D-018 (sesiones), D-048 (vinculación del dispositivo), D-051 (migraciones e identificadores), D-044 (detalle de intentos), D-050 (autenticación del administrador), D-049 (recuperación asistida), D-052 (registro técnico y auditoría de quién realizó la validación asistida), D-014 (API), D-019 (auditoría detallada) | Propuesta para revisión |
| **C** | **D-017** (secretos y credenciales) | **Aprobada** (AG-01 v5) |

Cada decisión A afecta solo a las funcionalidades que dependen de ella; las demás pueden avanzar en paralelo.
