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
| Requisitos | DOCUMENTADO — depurado por AG-00 (96 HUs vigentes; D1–D6 pendientes) |
| Arquitectura | EN DEFINICIÓN CONTROLADA |
| Base de datos | MODELO CORE DOCUMENTADO — actualizado por AG-00 (`ENTIDADES_BANCARIAS`, relaciones financieras) |
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

Además, por AG-00 (D-026), el modelo incluye la tabla de referencia del entorno simulado:

- ENTIDADES_BANCARIAS

El detalle oficial del modelo se encuentra en:

`03_BASE_DE_DATOS_NAYRA.md`

### Estado

**Modelo documentado:** SÍ.  
**Implementación física:** POR VERIFICAR EN EL REPOSITORIO. Las decisiones de AG-00 (D-024 a D-033) son solo documentales: **no se han implementado** en el código.

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

Definiciones aprobadas en AG-00 (ver `07_DECISIONES_TECNICAS_NAYRA.md`, D-024 a D-033):

- cada usuario tiene **una única cuenta financiera**, asociada a una **entidad bancaria simulada**;
- la cuenta financiera se localiza por DNI durante el registro y se vincula al usuario mediante FK con restricción de unicidad;
- las operaciones referencian la cuenta financiera de origen y la de destino;
- las transferencias no requieren seleccionar cuenta;
- no se incluyen apertura de cuentas, múltiples cuentas, transferencias entre cuentas propias ni gestión de entidades por el administrador.

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


---

## 15. Estado de implementación verificado — 2026-09-26

Rama de trabajo: `claude/project-thread-irkos4` (desde `yuniv`). Decisiones implementadas: D-034 a D-052 (`07_DECISIONES_TECNICAS_NAYRA.md`).

### 15.1 Documentación

| Documento | Cambio |
|---|---|
| `07_DECISIONES_TECNICAS_NAYRA.md` | D-034 a D-052 registradas; estados de D-005, D-007, D-008, D-010 a D-013, D-017 y D-018 actualizados; pendientes P-A01 y P-A02 |
| `02_ARQUITECTURA_NAYRA.md` | Arquitectura lógica de autenticación y voz |
| `03_BASE_DE_DATOS_NAYRA.md` | §16: `usuarios` (PIN, intentos, bloqueo), `dispositivos`, `sesiones`, `desafios_autenticacion`, `biometria.perfiles_voz` |
| `04_API.md` | **Nuevo**: API pública (borrador sujeto a AG-01) y API interna Java → Python (aprobada) |
| `05_BIOMETRIA_NAYRA.md` | §26: decisiones biométricas y limitaciones a declarar |
| `06_SEGURIDAD_NAYRA.md` | Controles aprobados, secretos e incidente de claves publicadas |

### 15.2 Backend Java (`Nayra-Back/`)

| Funcionalidad | Estado | Evidencia |
|---|---|---|
| Esquema con Flyway (V1, V2), `ddl-auto=validate` | IMPLEMENTADO Y PROBADO | Migraciones aplicadas sobre PostgreSQL 16 vacío; Hibernate valida el esquema al arrancar |
| Configuración por variables de entorno; arranque bloqueado si faltan parámetros obligatorios | IMPLEMENTADO Y PROBADO | Sin `NAYRA_INTENTOS_MAXIMO` la app no arranca ("Failed to bind properties under 'nayra.seguridad.intentos.maximo'") |
| JWT eliminado; sesión con token opaco, hash SHA-256, 5 min de inactividad, revocación | IMPLEMENTADO Y PROBADO | `AutenticacionIntegracionTest`: inicio, cierre, inactividad, revocación por nuevo dispositivo |
| PIN Argon2id + pepper | IMPLEMENTADO Y PROBADO | `PinServiceImplementTest` (parámetros m=19456, t=2, p=1 verificados en el hash) |
| Firma ECDSA P-256 del dispositivo; un dispositivo activo | IMPLEMENTADO Y PROBADO | `FirmaDispositivoServiceImplementTest`, `AutenticacionIntegracionTest` |
| Desafío palabra + 3 dígitos + palabra, un solo uso, expiración | IMPLEMENTADO Y PROBADO | `DesafioServiceImplementTest` (200 repeticiones), pruebas de reutilización y expiración |
| Límite de intentos y bloqueo | IMPLEMENTADO Y PROBADO | Bloqueo al tercer PIN incorrecto con valor de prueba 3 |
| Umbrales configurables y modo calibración | IMPLEMENTADO Y PROBADO | `CalibracionIntegracionTest` |
| Inicio de sesión `POST /api/v1/auth/desafios` y `/api/v1/auth/sesiones` | IMPLEMENTADO, **identificador provisional** | El dispositivo identifica al usuario (opción recomendada en P-A02, pendiente de confirmación) |
| `/api/v1/sesiones` (HU-13, HU-14) | IMPLEMENTADO Y PROBADO | |
| `/usuarios` | ASEGURADO | Solo `ADMINISTRADOR`; respuestas sin contraseña ni PIN. Inutilizable hasta resolver P-A01 |
| Registro de usuario, creación de PIN, vinculación de cuenta por DNI, enrolamiento público | PENDIENTE | Depende de AG-01 (D-051) |
| Registro de dispositivo (servicio) | IMPLEMENTADO Y PROBADO; sin endpoint público | El endpoint depende del flujo de registro de AG-01 |

**Pruebas:** 225 pruebas, 0 fallos (3 ejecuciones consecutivas), con `NAYRA_IT_DB_URL` (PostgreSQL) y `NAYRA_IT_VOZ_URL` (servicio de voz en ejecución). Sin esas variables se ejecutan solo las unitarias (208).

### 15.3 Servicio de voz Python (`nayra-voz/`)

| Funcionalidad | Estado | Evidencia |
|---|---|---|
| API interna FastAPI con token de servicio (04_API §3) | IMPLEMENTADO Y PROBADO | `tests/test_app.py` |
| Validación de audio WAV 16 kHz mono 16 bits | IMPLEMENTADO Y PROBADO | `tests/test_audio_calidad.py` |
| Calidad (duración, voz neta, SNR, saturación) | IMPLEMENTADO Y PROBADO con señales sintéticas | |
| AASIST con pesos oficiales | IMPLEMENTADO Y PROBADO (carga e inferencia) | `tests/test_aasist_real.py` con pesos verificados por SHA-256 |
| ECAPA-TDNN (SpeechBrain 1.1.1) | IMPLEMENTADO, **NO PROBADO CON EL MODELO REAL** | `huggingface.co` bloqueado por la red del entorno de desarrollo |
| Vosk con gramática | IMPLEMENTADO, **NO PROBADO CON EL MODELO REAL** | `alphacephei.com` bloqueado por la red del entorno de desarrollo |
| Centroide, similitud coseno, cifrado AES-256-GCM | IMPLEMENTADO Y PROBADO | `tests/test_biometria_cifrado.py` |
| Esquema `biometria` con usuario propio; el backend no puede leerlo | IMPLEMENTADO Y PROBADO | `permission denied for schema biometria` para `nayra_app`; `tests/test_repositorio_postgres.py` |
| Contrato Java ↔ Python | PROBADO | `VozContratoIntegracionTest` contra el servicio en ejecución (AASIST real; dobles para ECAPA y Vosk) |
| Herramienta de calibración (FAR, FRR, EER) | IMPLEMENTADO Y PROBADO | `tests/test_metricas.py` |

**Pruebas:** 24 pruebas, 0 fallos.

### 15.4 Problemas y bloqueos

| ID | Problema | Impacto | Estado |
|---|---|---|---|
| P-001 | Documentación AG-01 (v4–v6) sin subir a `yuniv` | Bloquea registro, PIN, enrolamiento y conciliación de `usuarios` | ABIERTO — acción del equipo (D-051) |
| P-002 | Red del entorno de desarrollo bloquea `huggingface.co`, `alphacephei.com` y `download.pytorch.org` | ECAPA y Vosk no se probaron con modelos reales | ABIERTO — configuración del entorno |
| P-003 | Clave de Gemini y secreto JWT publicados en el historial de `main` | Riesgo de uso indebido | ABIERTO — revocar/rotar en los proveedores |
| P-004 | Autenticación del personal y del administrador sin decidir (P-A01) | Paneles y `/usuarios` sin acceso | ABIERTO — decisión del equipo |
| P-005 | Base de datos local creada con `ddl-auto=update` en versiones anteriores | Flyway no migra sobre tablas existentes | Usar una base de datos nueva (`nayra`) para esta versión |

### 15.5 Próximos pasos

1. Subir AG-01 y conciliarlo con estas decisiones.
2. Confirmar P-A02 (identificador de inicio de sesión) y decidir P-A01.
3. Habilitar los dominios bloqueados y probar ECAPA y Vosk reales.
4. Registro, PIN y enrolamiento según AG-01; después, funcionalidad financiera simulada, Flutter y accesibilidad.
