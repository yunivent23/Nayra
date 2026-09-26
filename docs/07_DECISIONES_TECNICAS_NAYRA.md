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

**Estado:** APROBADA — resuelta por D-034 (2026-09-26)

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

**Estado:** APROBADA — Flutter (Android), confirmada por la aprobación de D-044 (2026-09-26); conciliar con AG-01

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

**Estado:** PARCIALMENTE APROBADA — factores y orden en D-041, D-043, D-046; sesiones en D-042 (2026-09-26). Pendiente: autenticación del personal de atención y del administrador

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

**Estado:** APROBADA — resuelta por D-040 (2026-09-26)

Debe definirse cómo se comunicará el backend principal con el componente especializado de voz.

Alternativas posibles deberán evaluarse antes de seleccionar una:

- API REST;
- otro mecanismo de comunicación;
- otra alternativa técnicamente justificada.

No implementar una alternativa como definitiva sin documentarla aquí.

---

## D-011 — Modelo biométrico

**Estado:** APROBADA — resuelta por D-034 (2026-09-26)

Debe definirse:

- modelo;
- representación de voz;
- método de comparación;
- estrategia de verificación;
- requisitos de rendimiento.

La selección debe basarse en la investigación realizada y en la capacidad de validación dentro del alcance del proyecto.

---

## D-012 — Modelo anti-spoofing

**Estado:** APROBADA — resuelta por D-035 (2026-09-26)

Debe definirse la estrategia concreta para detectar intentos de spoofing.

Se deben considerar las amenazas identificadas en `05_BIOMETRIA.md`.

---

## D-013 — Almacenamiento de información biométrica

**Estado:** APROBADA — resuelta por D-039 (2026-09-26)

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

**Estado:** PARCIALMENTE APROBADA — secretos por variables de entorno fuera del repositorio (D-039, D-043); gestor de secretos cloud pendiente de D-016

Debe definirse el mecanismo para almacenar:

- credenciales;
- claves;
- tokens;
- secretos de servicios;
- credenciales de base de datos.

Nunca deberán incluirse directamente en el código fuente.

---

## D-018 — Estrategia de sesiones

**Estado:** APROBADA — resuelta por D-042 (2026-09-26)

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
| D-005 / D-011 | Framework y modelo biométrico | EN EVALUACIÓN / PENDIENTE | APROBADA (D-034) | Aprobación de la propuesta A–K | 2026-09-26 |
| D-007 | Tecnología del frontend | PENDIENTE | APROBADA (Flutter) | Aprobación de D-044 | 2026-09-26 |
| D-008 | Mecanismo de autenticación | PENDIENTE | PARCIALMENTE APROBADA | D-041, D-042, D-043, D-046 | 2026-09-26 |
| D-010 | Comunicación Java ↔ Python | PENDIENTE | APROBADA (D-040) | Aprobación de la propuesta A–K | 2026-09-26 |
| D-012 | Modelo anti-spoofing | PENDIENTE | APROBADA (D-035) | Aprobación de la propuesta A–K | 2026-09-26 |
| D-013 | Almacenamiento biométrico | PENDIENTE | APROBADA (D-039) | Aprobación de la propuesta A–K | 2026-09-26 |
| D-017 | Gestión de secretos | PENDIENTE | PARCIALMENTE APROBADA | Variables de entorno; gestor cloud pendiente de D-016 | 2026-09-26 |
| D-018 | Estrategia de sesiones | PENDIENTE | APROBADA (D-042) | Aprobación de la propuesta A–K | 2026-09-26 |
| D-034 a D-051 | Decisiones A–K y L1–L7 | — (nuevas) | APROBADA | Aprobación de Yuni sobre `PROPUESTA_DECISIONES_TECNICAS_A-K.md` | 2026-09-26 |
| D-052 | Herramienta de migraciones (Flyway) | — (nueva) | ADOPTADA EN IMPLEMENTACIÓN | Decisión de bajo impacto necesaria para "verificar migraciones"; revisable | 2026-09-26 |

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


## Decisiones aprobadas — Propuesta A–K y L1–L7 (2026-09-26)

**Origen:** propuesta `PROPUESTA_DECISIONES_TECNICAS_A-K.md` (carpeta compartida del proyecto, `decisiones/`), aprobada por Yuni el 2026-09-26 con estas condiciones:

1. Las recomendaciones de la propuesta son las decisiones técnicas del proyecto.
2. Ninguna tecnología aprobada se sustituye sin consulta previa.
3. Los valores pendientes de calibración se mantienen como **parámetros configurables** y **no se inventan**.

Regla transversal para todos los parámetros marcados como _(configurable, valor pendiente)_: el código lee el valor desde configuración externa; si el valor no está definido, el componente funciona en **modo calibración** (registra el dato, no decide con un valor inventado) o rechaza la operación de forma segura, según se indica en cada caso.

### D-034 — Modelo de verificación de voz (A)
**Estado:** APROBADA — reemplaza el estado EN EVALUACIÓN de D-005 y resuelve D-011.

- **Modelo principal:** SpeechBrain ECAPA-TDNN, modelo preentrenado `speechbrain/spkrec-ecapa-voxceleb` (Apache 2.0 en código y pesos).
- **Entrada:** audio mono 16 kHz (D-045). **Salida:** embedding de 192 valores, normalizado L2.
- **Comparación 1:1:** similitud coseno entre el embedding de la muestra y el embedding de referencia del usuario (D-039).
- **Alternativa experimental:** WeSpeaker, solo para comparación en la evaluación de la tesis; no se despliega.
- **Documentos afectados:** `05_BIOMETRIA` §26, `02_ARQUITECTURA`.

### D-035 — Anti-spoofing (B)
**Estado:** APROBADA — resuelve D-012.

- **Modelo principal:** AASIST (clovaai/aasist, MIT), preentrenado en ASVspoof 2019 LA, para voz sintetizada y voz convertida.
- **Defensa complementaria contra replay:** desafío dinámico de un solo uso (D-037) y firma del dispositivo (D-041). AASIST no está entrenado para replay físico; esa limitación se documenta en la tesis.
- **Extensión posible, no obligatoria:** SSL-AASIST (wav2vec 2.0 XLS-R + AASIST).
- Se mantiene el orden de `05_BIOMETRIA` §10: anti-spoofing antes de la verificación biométrica.
- Anti-spoofing y biometría son **componentes distintos**.

### D-036 — Reconocimiento del contenido hablado (C)
**Estado:** APROBADA.

- **Tecnología:** Vosk con el modelo `vosk-model-small-es-0.42` (Apache 2.0), ejecutado en el servicio Python.
- **Gramática restringida** al vocabulario permitido por los desafíos (D-037).
- **Alternativa de respaldo:** faster-whisper (MIT); solo se activa previa consulta.
- La verificación del contenido se realiza en el servidor; no se confía en resultados del cliente.

### D-037 — Generación del desafío (D)
**Estado:** APROBADA.

- **Formato:** palabra + 3 dígitos + palabra (p. ej., "sol, cuatro, siete, dos, mesa").
- **Nunca** 6 dígitos consecutivos.
- Generado en Spring Boot con `SecureRandom`; **un solo uso**; asociado al usuario, al dispositivo y al nonce (D-041).
- **Tiempo de vida:** corto y configurable (D-048).
- La app permite **repetir** el desafío mediante una acción accesible (HU-58).
- **Lista de palabras:** lista cerrada de palabras comunes, bisílabas, fonéticamente distintas y sin homófonos, mantenida en configuración versionada (`challenge-words.es.txt` del backend) y compartida con la gramática de Vosk. La lista inicial es revisable por el equipo sin cambiar esta decisión.

### D-038 — Umbrales de decisión (E)
**Estado:** APROBADA (estrategia). **Valores:** PENDIENTES DE CALIBRACIÓN.

- No se fijan valores arbitrarios. Los umbrales de similitud biométrica, anti-spoofing, calidad de audio y confianza del contenido son **parámetros configurables**, versionados junto con el nombre y la versión del modelo.
- **Modo calibración inicial:** mientras un umbral no tenga valor aprobado, el backend **no autentica** con ese factor y responde `CALIBRACION`. Los puntajes para calcular los umbrales se obtienen con la herramienta de evaluación fuera de línea del servicio de voz (`nayra-voz/evaluacion/`) sobre el dataset de calibración (D-049), no con datos de usuarios del sistema.
- **Método:** conjunto de calibración con voluntarios (D-049), separación desarrollo/prueba, punto de operación orientado a baja FAR, registro de FAR, FRR y EER (y tasa de detección por tipo de ataque para anti-spoofing).
- Cada valor definitivo se registrará como una decisión nueva en este documento junto con el experimento que lo respalda.

### D-039 — Almacenamiento biométrico (F)
**Estado:** APROBADA — resuelve D-013.

- Se guarda **únicamente** el embedding de referencia y metadatos necesarios. **No se almacena audio** como almacenamiento biométrico permanente; el audio se procesa en memoria y se descarta.
- **Enrolamiento:** 3 muestras válidas (3 desafíos distintos).
- **Combinación:** centroide de embeddings normalizados L2, renormalizado.
- Se registran **modelo y versión**; un cambio de modelo exige re-enrolamiento.
- **Actualización:** solo por re-enrolamiento explícito (HU-34/HU-35). Sin adaptación automática.
- **Protección:** embedding cifrado con AES-256-GCM; clave fuera del código y de la base de datos (variable de entorno; gestor cloud pendiente de D-016).
- **Separación de responsabilidades:** el servicio Python es el único dueño de los datos biométricos, en un esquema propio (`biometria`) con usuario de base de datos propio. Spring Boot solo conoce `usuario_id` y los resultados técnicos.
- Estructura en `03_BASE_DE_DATOS` §16.

### D-040 — Comunicación Java ↔ Python (G)
**Estado:** APROBADA — resuelve D-010.

- **REST interno** con FastAPI.
- El servicio Python **no es accesible** desde la aplicación móvil ni desde internet; solo Spring Boot lo invoca.
- **Autenticación entre servicios:** token de servicio en cabecera, almacenado como secreto (D-017), comparado en tiempo constante. En GCP podrá reemplazarse por identidad de servicio cuando se apruebe D-016.
- **Timeouts:** configurables (conexión y lectura). Sin reintentos automáticos en verificación, salvo fallo de conexión antes del envío.
- **Python** devuelve resultados técnicos estructurados (puntajes y estados); **Java** aplica umbrales y reglas de negocio y determina la decisión final.
- Contrato en `04_API.md` §3.

### D-041 — Dispositivo y criptografía (H)
**Estado:** APROBADA.

- Par de claves **ECDSA P-256** (secp256r1) con firma **SHA256withECDSA**, generado en **Android Keystore** (StrongBox si existe), mediante un **canal de plataforma Kotlin propio**.
- Clave privada **no exportable** y **nunca** almacenada en el backend. El backend guarda solo la **clave pública**.
- **Nonce de un solo uso** emitido por el backend (TTL según D-048); la app firma el mensaje canónico definido en `04_API.md` §2.4.
- **Un dispositivo activo por usuario.** Registrar un dispositivo nuevo **revoca** el anterior y cierra sus sesiones.

### D-042 — Sesiones (I)
**Estado:** APROBADA — resuelve D-018 y **reemplaza el mecanismo JWT** presente en el código inicial (el JWT nunca fue una decisión aprobada; D-008 estaba PENDIENTE).

- **Token opaco** aleatorio de 256 bits (`SecureRandom`), entregado una sola vez al cliente.
- En `SESIONES` se guarda **solo el hash SHA-256** del token.
- **Cierre por 5 minutos de inactividad**, controlado en el servidor.
- **Revocación inmediata** (cierre de sesión, revocación del dispositivo, bloqueo de cuenta).
- Compatible con las **2 instancias** (D-023): el estado vive en PostgreSQL, no en memoria.
- Duración máxima absoluta: _(configurable, valor pendiente)_.
- La app avisa por voz antes del cierre por inactividad (D-044).

### D-043 — PIN (J)
**Estado:** APROBADA.

- PIN de **6 dígitos** (requisito aprobado). No se agrega otra credencial.
- Hash **Argon2id** con parámetros m = 19 MiB, t = 2, p = 1 (referencia OWASP).
- **Pepper** (HMAC-SHA256 previo al hash) almacenado **fuera de la base de datos y del repositorio**.
- Validación solo en el servidor, en tiempo constante.
- **Límite de intentos y bloqueo** (valores en D-047).
- El PIN **nunca** aparece en logs, auditoría ni mensajes de error.

### D-044 — Accesibilidad (K)
**Estado:** APROBADA.

- Enfoque **híbrido**: TalkBack para navegación; voz propia (`flutter_tts`) para desafío, instrucciones de grabación y avisos, coordinada para no hablar encima de TalkBack.
- Referencia: **WCAG 2.2 AA**.
- Flutter `Semantics`; grabación **WAV PCM 16 kHz mono**; teclado numérico accesible de distribución fija; retroalimentación auditiva y háptica diferenciada.
- Pruebas automáticas (guías de accesibilidad de Flutter) y pruebas manuales con TalkBack.

### D-045 — Formato de audio (L1)
**Estado:** APROBADA.

- WAV PCM, 16 kHz, mono, 16 bits, sin compresión con pérdida.
- Duración mínima y máxima de la grabación: _(configurable, valor pendiente de calibración)_. El backend rechaza archivos que excedan un tamaño máximo técnico configurable.

### D-046 — Orden y combinación de factores (L2)
**Estado:** APROBADA.

- Orden: **firma del dispositivo → PIN → voz** (voz = calidad, contenido, anti-spoofing, verificación biométrica).
- **Todos** los factores deben aprobarse para autenticar.
- La respuesta al usuario no revela qué factor falló cuando esa información beneficia a un atacante; los mensajes accesibles indican la acción posible (repetir la grabación, esperar, contactar a atención).

### D-047 — Límite de intentos y bloqueo (L3)
**Estado:** APROBADA (mecanismo). **Valores:** _(configurables, pendientes)_.

- Contadores de intentos fallidos de PIN y de voz por cuenta (y por dispositivo).
- Al alcanzar el máximo se **bloquea** la cuenta de acceso; el desbloqueo sigue HU-18 / HU-19.
- Si el máximo no está configurado, el backend **no arranca** (no se permite un valor implícito).

### D-048 — Vida útil del desafío y del nonce (L4)
**Estado:** APROBADA (mecanismo). **Valores:** _(configurables, pendientes)_.

- Desafío y nonce de **un solo uso**, invalidados al usarse o al expirar.
- Tiempo de vida corto y configurable; si no está configurado, el backend no arranca.

### D-049 — Dataset de calibración y consentimiento (L5)
**Estado:** APROBADA (estrategia).

- Grabaciones de voluntarios con **consentimiento informado**, incluyendo personas con discapacidad visual cuando sea posible.
- Almacenamiento **separado** del sistema productivo y de la base de datos de Nayra; borrado al terminar la tesis.
- Separación desarrollo/prueba (D-038).
- Número de voluntarios y protocolo de consentimiento: _(pendientes de definición por el equipo)_.

### D-050 — Despliegue inicial del servicio Python (L6)
**Estado:** APROBADA.

- Contenedor/VM **solo CPU**; la combinación ECAPA + AASIST + Vosk pequeño no requiere GPU.
- Recursos estimados 2–4 vCPU y 4 GB de RAM, **a medir** en el prototipo.
- La infraestructura cloud concreta sigue pendiente (D-016).

### D-051 — Documentación AG-01 pendiente (L7)
**Estado:** APROBADA (acción).

- Los cambios de AG-01 (v4–v6: `CLAUDE.md` y documentos de `docs/`) deben subirse a la rama `yuniv` y conciliarse con estas decisiones. Hasta entonces, los flujos de registro y autenticación que dependan de AG-01 no se implementan de forma definitiva.

### D-052 — Herramienta de migraciones
**Estado:** ADOPTADA DURANTE LA IMPLEMENTACIÓN (bajo impacto, revisable).

- Flyway (Apache 2.0) para versionar el esquema de PostgreSQL. Se reemplaza `spring.jpa.hibernate.ddl-auto=update` por `validate`.
- **Motivo:** el esquema debe poder verificarse y reproducirse; no cambia la arquitectura ni la seguridad.

### Decisiones nuevas detectadas durante la implementación (PENDIENTES)

| ID | Decisión | Motivo |
|---|---|---|
| P-A01 | Autenticación del personal de atención y del administrador (HU-96, HU-97) | Los factores aprobados (dispositivo + PIN + voz) corresponden al usuario de la app. El código inicial usa usuario/contraseña, que no es una decisión aprobada. |
| P-A02 | Identificador con el que el usuario inicia sesión en la app | Depende de AG-01. |
