# 07_DECISIONES_TECNICAS.md — Decisiones técnicas de Nayra

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

- `02_ARQUITECTURA.md`
- `01_REQUISITOS.md`

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

La selección deberá considerar:

- accesibilidad;
- compatibilidad con autenticación por voz;
- integración con APIs;
- facilidad de desarrollo;
- mantenimiento;
- capacidad de pruebas.

---

## D-008 — Mecanismo de autenticación

**Estado:** PENDIENTE

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

**Estado:** PENDIENTE

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

Se deben considerar las amenazas identificadas en `05_BIOMETRIA.md`.

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

La arquitectura aprobada deberá registrarse en `02_ARQUITECTURA.md`.

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

**Estado:** PENDIENTE

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

Una arquitectura nueva solo será considerada vigente cuando esté documentada y aprobada en `02_ARQUITECTURA.md`.

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
8. Mantener coherencia con `01_REQUISITOS.md`.
9. Mantener coherencia con `02_ARQUITECTURA.md`.
10. Mantener coherencia con `03_BASE_DE_DATOS.md`.
11. Mantener coherencia con `05_BIOMETRIA.md`.
12. Mantener coherencia con `06_SEGURIDAD.md`.
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
07_DECISIONES_TECNICAS.md
          ↓
02_ARQUITECTURA.md
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

**Pendiente asociado:** dónde reside, dentro del entorno simulado, el dato que permite localizar la cuenta por DNI; obligatoriedad de la FK antes de la vinculación (ver `03_BASE_DE_DATOS_NAYRA.md` §15).

### D-029 — Referencias de `OPERACIONES`
**Estado:** APROBADA

Las operaciones referencian directamente la **cuenta financiera de origen** y la **cuenta financiera de destino** (cuando corresponda). La cuenta destino deja de ser un campo de texto.

### D-030 — Transferencias sin selección de cuenta
**Estado:** APROBADA

Como cada usuario tiene una única cuenta financiera, no existe selección entre varias cuentas. El emisor utiliza su única cuenta financiera y el sistema identifica la única cuenta financiera del destinatario.

**Pendiente asociado:** con qué dato identifica el emisor al destinatario (AG-01/AG-04) y la regla de moneda en transferencias.

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

**No aprobado por esta decisión:** el mecanismo exacto de identificación durante el registro (DNI, voz o ambos) queda **pendiente para AG-01 / D-008**. Esta decisión tampoco define el modelo biométrico (D-011), el anti-spoofing (D-012) ni el almacenamiento biométrico (D-013).

### D-033 — Exclusiones de alcance del modelo financiero
**Estado:** APROBADA

No se incorporan funcionalidades de:

- apertura de cuentas bancarias;
- múltiples cuentas financieras por usuario;
- transferencias entre cuentas propias;
- gestión de entidades bancarias por parte del administrador.
