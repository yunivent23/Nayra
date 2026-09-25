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

El short paper contempla hasta el Objetivo 2, incluyendo el diseño de la solución y parte del desarrollo.

### Estado documental

| Área | Estado |
|---|---|
| Contexto del proyecto | COMPLETADO |
| Requisitos | DOCUMENTADO |
| Arquitectura | EN DEFINICIÓN CONTROLADA |
| Base de datos | MODELO CORE DOCUMENTADO |
| Biometría de voz | DISEÑO CONCEPTUAL DOCUMENTADO |
| Seguridad | PRINCIPIOS Y CONTROLES DOCUMENTADOS |
| Decisiones técnicas | REGISTRADAS |
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

**Estado de implementación:** POR VERIFICAR DIRECTAMENTE EN EL REPOSITORIO.

---

### 3.2 Backend Python

**Tecnología:** Python.

Responsabilidad:

- procesamiento especializado relacionado con voz;
- extracción/procesamiento de características de voz;
- integración con modelos preentrenados;
- biometría de voz;
- mecanismos de detección de intentos de suplantación, cuando hayan sido implementados.

**Estado:** EN DISEÑO / DESARROLLO.  
La implementación concreta de modelos, librerías, endpoints y mecanismo de comunicación debe verificarse en el repositorio.

---

### 3.3 Frontend / aplicación móvil

La tecnología definitiva del frontend todavía debe considerarse una decisión técnica pendiente si no ha sido aprobada formalmente.

**Estado:** POR DEFINIR / VERIFICAR.

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

El detalle oficial del modelo se encuentra en:

`03_BASE_DE_DATOS_NAYRA.md`

### Estado

**Modelo documentado:** SÍ.  
**Implementación física:** POR VERIFICAR EN EL REPOSITORIO.

No deben agregarse tablas biométricas como `VOICE_BIOMETRICS`, `VOICE_EMBEDDINGS` o `ANTI_SPOOFING` sin una decisión explícita.

---

## 5. Biometría de voz

La biometría de voz constituye uno de los componentes centrales de Nayra.

### Definiciones actuales

- Se utilizarán modelos preentrenados.
- Python será utilizado para el procesamiento especializado.
- SpeechBrain se encuentra bajo evaluación.
- El objetivo conceptual es realizar **verificación de voz**, no identificación abierta.
- Se considera protección frente a intentos de suplantación mediante reproducción o voz sintética/manipulada.

### Decisiones todavía pendientes

- modelo definitivo;
- algoritmo de comparación;
- umbral de aceptación;
- estrategia de almacenamiento de datos biométricos;
- estrategia de retención de audio;
- modelo definitivo de anti-spoofing;
- mecanismo de integración con el backend Java.

**Estado:** DISEÑO / EVALUACIÓN.

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

---

## 9. Funcionalidades

### Implementadas

La lista de funcionalidades implementadas debe mantenerse sincronizada con el repositorio.

**Estado actual documentado:** POR VERIFICAR.

### En desarrollo

Deben registrarse aquí las funcionalidades que tengan código parcial pero que todavía no estén completas.

**Estado actual:** POR VERIFICAR.

### Pendientes

Deben registrarse las funcionalidades que todavía no tengan una implementación funcional.

**Estado actual:** POR VERIFICAR.

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

**Pruebas automatizadas:** POR VERIFICAR.  
**Pruebas de integración:** POR VERIFICAR.  
**Pruebas de biometría:** PENDIENTES DE IMPLEMENTACIÓN/VALIDACIÓN.  
**Pruebas de seguridad:** POR VERIFICAR.

---

## 11. Problemas y bloqueos

Los problemas deben registrarse cuando afecten el desarrollo.

Formato recomendado:

| ID | Problema | Impacto | Estado | Responsable |
|---|---|---|---|---|
| P-001 | Pendiente de identificar mediante revisión del repositorio | — | POR VERIFICAR | — |

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

