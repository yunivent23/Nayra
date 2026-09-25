# 01_REQUISITOS.md — Requisitos del sistema Nayra

> **Fuente:** `REQUERIMIENTOS(7).xlsx`. Este documento consolida las historias de usuario, su clasificación técnica y el modelo de tablas CORE proporcionados en el archivo. No se agregan requisitos funcionales nuevos.
>
> **Actualización AG-00 (2026-09-25):** se aplicaron las decisiones aprobadas de AG-00 registradas en `07_DECISIONES_TECNICAS_NAYRA.md` (D-024 a D-033): glosario de términos, eliminación de IDs no aplicables, consolidación de HUs duplicadas y ajustes del modelo de cuenta financiera y entidad bancaria. El detalle de trazabilidad está en la sección 11. **Las HUs no se renumeraron.**

## 1. Propósito

Este documento constituye la referencia de requisitos para el desarrollo de Nayra. Define las historias de usuario, actores, prioridad y clasificación funcional/no funcional. La implementación debe trazarse contra estos requisitos.

## 2. Actores identificados

- **Usuario:** persona que utiliza Nayra y sus funcionalidades de billetera y autenticación.
- **Administrador:** responsable de gestión, supervisión, seguridad y métricas de la plataforma.
- **Personal de atención:** usuario autorizado para asistir a usuarios mediante el panel correspondiente.

## 3. Convenciones

- **F:** requisito funcional.
- **NF:** requisito no funcional.
- **Alta / Media:** prioridad registrada en el archivo fuente.
- Las historias de usuario se mantienen con su redacción original, excepto las HUs consolidadas por AG-00 (D-031), cuya redacción unificada se indica en la propia historia y se traza en la sección 11.
- La tecnología indicada en la clasificación es una referencia de implementación del archivo fuente; no reemplaza una decisión arquitectónica aprobada.
- Los IDs de HU **no se renumeran**. Los huecos de numeración resultantes de AG-00 son intencionales (ver sección 11).

### 3.1 Glosario (AG-00, D-024)

| Término | Significado |
|---|---|
| **Cuenta de acceso** (perfil Nayra) | Identidad y acceso del usuario a Nayra. Sobre ella actúan el registro, el inicio de sesión, la desactivación/reactivación y el bloqueo/desbloqueo. |
| **Cuenta financiera** | Única cuenta simulada del usuario dentro del entorno bancario simulado, asociada a una entidad bancaria simulada. Corresponde a la tabla `CUENTAS` (D-025, D-027). |
| **Entidad bancaria** | Banco simulado del entorno controlado (D-021). Corresponde a la tabla de referencia `ENTIDADES_BANCARIAS` (D-026). |
| **Billetera** | Término usado en las HUs sin definición formal. Su relación exacta con la cuenta financiera queda **pendiente de confirmación** (ver sección 11.4). |

Regla: el término "cuenta" en una HU debe interpretarse según su contexto como **cuenta de acceso** o **cuenta financiera**. Interpretación aplicada a las HUs vigentes que usan el término:

| HU | Interpretación |
|---|---|
| HU-01, HU-04, HU-09, HU-12, HU-13, HU-14, HU-15, HU-16, HU-17, HU-18, HU-19, HU-20, HU-21, HU-24, HU-51, HU-78, HU-81, HU-83, HU-99, HU-101 | Cuenta de acceso |
| HU-03 | Cuenta de acceso ("la cuenta corresponde a mi persona") |
| HU-67, HU-77 | Cuenta financiera |
| HU-93 | Cuenta de acceso para bloqueos/desbloqueos; si "modificaciones relevantes" incluye la cuenta financiera queda **pendiente** |
| HU-104 | **Pendiente** (duplicado dudoso D1) |
| HU-110 | **Pendiente** (cuentas de acceso, cuentas financieras o ambas) |

## 4. Épicas e historias de usuario


### EP-01 Gestión de cuentas y usuarios

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-01 | Usuario | Como usuario, quiero registrarme en Nayra para crear una cuenta y utilizar los servicios de la billetera digital. | Alta | F |
| HU-02 | Usuario | Como usuario, quiero proporcionar mis datos personales durante el registro para crear mi perfil dentro de Nayra. | Alta | F |
| HU-03 | Usuario | Como usuario, quiero verificar mi identidad durante el registro para asegurar que la cuenta corresponde a mi persona. | Alta | F |
| HU-04 | Usuario | Como usuario, quiero recibir una confirmación cuando mi cuenta haya sido creada correctamente para saber que puedo comenzar a utilizar Nayra. | Media | F |
| HU-09 | Usuario | Como usuario, quiero consultar mis datos personales para verificar la información asociada a mi cuenta. | Media | F |
| HU-10 | Usuario | Como usuario, quiero actualizar determinados datos personales para mantener mi información vigente. | Alta | F |
| HU-12 | Usuario | Como usuario registrado, quiero acceder a mi cuenta desde la aplicación para utilizar las funcionalidades de Nayra. | Alta | F |
| HU-13 | Usuario | Como usuario, quiero cerrar mi sesión para proteger mi cuenta cuando termine de utilizar la aplicación. | Alta | F |
| HU-14 | Usuario | Como usuario, quiero gestionar mis sesiones activas para controlar los dispositivos desde los cuales se accede a mi cuenta. | Media | F |
| HU-15 | Usuario | Como usuario, quiero conocer el estado de mi cuenta para saber si se encuentra disponible para su utilización. | Media | F |
| HU-16 | Usuario | Como usuario, quiero solicitar la desactivación de mi cuenta para dejar de utilizar temporalmente la billetera digital. | Alta | F |
| HU-17 | Usuario | Como usuario, quiero solicitar la reactivación de mi cuenta desactivada para volver a utilizar la billetera digital. | Alta | F |
| HU-18 | Usuario | Como usuario, quiero recuperar el acceso a mi cuenta cuando se encuentre bloqueada o no pueda acceder para volver a utilizar Nayra. | Alta | F |
| HU-19 | Administrador | Como administrador, quiero gestionar el bloqueo y desbloqueo de las cuentas de acceso de los usuarios, ante incidencias de seguridad o solicitudes autorizadas, para protegerlas o restablecer su acceso cuando corresponda. _(Consolida HU-82 y HU-105 — AG-00)_ | Alta | F |
| HU-20 | Administrador | Como administrador, quiero consultar los usuarios registrados para gestionar las cuentas de la plataforma. _(Junto con HU-21, absorbe HU-103 — AG-00)_ | Alta | F |
| HU-21 | Administrador | Como administrador, quiero buscar usuarios mediante sus datos identificativos para localizar rápidamente una cuenta. _(Junto con HU-20, absorbe HU-103 — AG-00)_ | Alta | F |
| HU-22 | Administrador | Como administrador, quiero consultar información básica de un usuario para atender necesidades administrativas o de seguridad. | Alta | F |
| HU-23 | Administrador | Como administrador, quiero modificar determinados datos personales de un usuario cuando sea necesario corregir información registrada incorrectamente. | Alta | F |
| HU-24 | Administrador | Como administrador, quiero gestionar las cuentas de acceso del personal de atención para controlar quién puede acceder al panel de atención y realizar funciones autorizadas dentro de la plataforma. _(Consolida HU-106 — AG-00)_ | Alta | F |
| HU-25 | Administrador | Como administrador, quiero asignar roles y permisos al personal autorizado para controlar y limitar las funcionalidades disponibles según sus responsabilidades. _(Consolida HU-107 — AG-00)_ | Alta | F |

### EP-02 Registro y gestión de biometría de voz

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-26 | Usuario | Como usuario, quiero registrar mi voz para crear mi perfil biométrico y utilizarlo posteriormente para autenticarme en Nayra. | Alta | F |
| HU-27 | Usuario | Como usuario, quiero recibir instrucciones auditivas antes y durante el registro de mi voz para realizar correctamente la captura de mi muestra. | Alta | F |
| HU-28 | Usuario | Como usuario, quiero controlar manualmente el inicio y finalización de la grabación de mi voz para proporcionar una muestra adecuada. | Alta | F |
| HU-29 | Usuario | Como usuario, quiero realizar una frase de desafío indicada por la aplicación para proporcionar una muestra de voz controlada durante mi registro biométrico. | Alta | F |
| HU-30 | Usuario | Como usuario, quiero repetir una grabación cuando la muestra no sea adecuada para completar correctamente mi registro biométrico. | Alta | F |
| HU-31 | Usuario | Como usuario, quiero recibir información sobre la calidad de mi grabación para saber si la muestra es adecuada para continuar con el registro. | Alta | F |
| HU-32 | Usuario | Como usuario, quiero recibir indicaciones cuando las condiciones del entorno puedan afectar la calidad de mi grabación para poder realizar una nueva captura. | Alta | F |
| HU-33 | Usuario | Como usuario, quiero recibir una confirmación cuando mi perfil biométrico haya sido registrado correctamente para saber que puedo utilizarlo en futuras autenticaciones. | Alta | F |
| HU-34 | Usuario | Como usuario, quiero actualizar mi perfil biométrico de voz cuando sea necesario para mantener la precisión de mi autenticación. | Media | F |
| HU-35 | Usuario | Como usuario, quiero volver a registrar mi voz cuando mi perfil biométrico requiera una actualización para mantenerlo vigente. | Media | F |
| HU-36 | Usuario | Como usuario, quiero solicitar la eliminación de mi información biométrica para controlar el uso de mis datos biométricos. | Alta | F |
| HU-37 | Usuario | Como usuario, quiero recibir asistencia de personal autorizado durante mi registro biométrico cuando necesite ayuda para completar el proceso. | Alta | F |

### EP-03 Autenticación biométrica por voz

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-40 | Usuario | Como usuario, quiero iniciar el proceso de autenticación mediante mi voz para verificar mi identidad y acceder de forma segura a Nayra. | Alta | F |
| HU-41 | Usuario | Como usuario, quiero recibir instrucciones auditivas antes y durante la autenticación para realizar correctamente la captura de mi voz. | Alta | F |
| HU-42 | Usuario | Como usuario, quiero controlar manualmente el inicio y finalización de la grabación de mi voz durante la autenticación para proporcionar una muestra adecuada. | Alta | F |
| HU-43 | Usuario | Como usuario, quiero realizar una frase de desafío indicada por la aplicación durante la autenticación para proporcionar una muestra de voz controlada. | Alta | F |
| HU-44 | Usuario | Como usuario, quiero repetir la captura de mi voz cuando la muestra no sea válida para poder completar correctamente la autenticación. | Alta | F |
| HU-45 | Usuario | Como usuario, quiero recibir información sobre el resultado de mi autenticación para saber si mi identidad fue validada correctamente. | Alta | F |
| HU-46 | Usuario | Como usuario, quiero autenticarme mediante voz antes de realizar operaciones que requieran un nivel adicional de seguridad para proteger mis fondos. | Alta | F |
| HU-47 | Usuario | Como usuario, quiero recibir instrucciones accesibles cuando mi autenticación no sea exitosa para poder intentarlo nuevamente. | Alta | F |

### EP-04 Detección de spoofing y voces sintéticas

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-48 | Usuario | Como usuario, quiero que mi muestra de voz sea validada para asegurar que pueda utilizarse de forma segura durante la autenticación. | Alta | F |
| HU-49 | Usuario | Como usuario, quiero recibir una indicación cuando mi muestra de voz sea rechazada por posibles problemas de autenticidad para poder realizar una nueva captura. | Alta | F |
| HU-50 | Usuario | Como usuario, quiero recibir instrucciones accesibles cuando deba repetir una captura rechazada para poder continuar con el proceso de autenticación. | Alta | F |
| HU-51 | Administrador | Como administrador, quiero consultar los eventos de spoofing detectados para identificar y analizar posibles intentos de suplantación y amenazas contra las cuentas de acceso. _(Consolida HU-92 — AG-00)_ | Alta | F |
| HU-52 | Administrador | Como administrador, quiero consultar estadísticas y métricas de detección de spoofing para evaluar el comportamiento y desempeño del mecanismo de protección. _(Consolida HU-112 — AG-00)_ | Alta | F |

### EP-05 Accesibilidad e interacción inclusiva

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-53 | Usuario | Como usuario con discapacidad visual, quiero utilizar Nayra mediante una interfaz accesible para realizar las operaciones de la billetera de forma independiente. | Alta | NF |
| HU-54 | Usuario | Como usuario, quiero recibir instrucciones y mensajes mediante audio para comprender las acciones disponibles y el estado de los procesos en la aplicación. | Alta | F |
| HU-55 | Usuario | Como usuario, quiero navegar por las funcionalidades de Nayra mediante controles compatibles con tecnologías de asistencia para acceder a las opciones disponibles. | Alta | NF |
| HU-56 | Usuario | Como usuario, quiero que los elementos interactivos de la aplicación tengan etiquetas descriptivas para identificar correctamente su función mediante tecnologías de asistencia. | Alta | F |
| HU-57 | Usuario | Como usuario, quiero recibir confirmaciones, advertencias y mensajes de error mediante señales auditivas diferenciadas para comprender el resultado de mis acciones. | Alta | F |
| HU-58 | Usuario | Como usuario, quiero repetir una instrucción auditiva cuando no haya comprendido el mensaje anterior para poder continuar con el proceso. | Alta | F |
| HU-59 | Usuario | Como usuario, quiero recibir una descripción auditiva de las opciones disponibles en cada sección para seleccionar la acción que necesito. | Alta | F |
| HU-60 | Usuario | Como usuario, quiero confirmar o cancelar operaciones importantes antes de ejecutarlas para evitar acciones accidentales. | Alta | F |
| HU-61 | Usuario | Como usuario, quiero escuchar los datos principales de una operación antes de confirmarla para verificar que la información sea correcta. | Alta | F |
| HU-62 | Usuario | Como usuario, quiero controlar manualmente determinadas capturas de audio para realizar la interacción de acuerdo con mis necesidades. | Alta | F |
| HU-64 | Usuario | Como usuario, quiero recibir información accesible sobre los errores y las acciones que puedo realizar para corregirlos y continuar con el proceso. | Alta | F |
| HU-65 | Usuario | Como usuario, quiero utilizar las funciones principales de Nayra sin depender permanentemente de la asistencia de otra persona. | Alta | NF |

### EP-06 Gestión de operaciones de la billetera

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-66 | Usuario | Como usuario, quiero consultar mi saldo disponible para conocer el dinero que tengo en mi billetera. | Alta | F |
| HU-67 | Usuario | Como usuario, quiero consultar el historial de mis movimientos para conocer las operaciones realizadas desde mi cuenta financiera. _(Consolida HU-84 — AG-00)_ | Alta | F |
| HU-68 | Usuario | Como usuario, quiero consultar el detalle de una operación para conocer la información específica asociada a un movimiento. _(Consolida HU-85 — AG-00)_ | Alta | F |
| HU-69 | Usuario | Como usuario, quiero realizar una transferencia a otro usuario de Nayra para enviar dinero desde mi billetera. | Alta | F |
| HU-70 | Usuario | Como usuario, quiero revisar y confirmar los datos de una transferencia antes de ejecutarla para asegurar que la operación sea correcta. | Alta | F |
| HU-71 | Usuario | Como usuario, quiero cancelar una transferencia antes de confirmarla para evitar realizar una operación no deseada. | Alta | F |
| HU-72 | Usuario | Como usuario, quiero recibir una confirmación del resultado de una transferencia para conocer si la operación fue realizada correctamente. | Alta | F |
| HU-73 | Usuario | Como usuario, quiero realizar un pago desde mi billetera para utilizar mi saldo en una operación de pago simulada. | Alta | F |
| HU-74 | Usuario | Como usuario, quiero revisar y confirmar los datos de un pago antes de ejecutarlo para asegurar que la operación sea correcta. | Alta | F |
| HU-75 | Usuario | Como usuario, quiero recibir una confirmación del resultado de un pago para conocer si la operación fue realizada correctamente. | Alta | F |
| HU-76 | Usuario | Como usuario, quiero consultar y filtrar mis operaciones por tipo o periodo para localizar rápidamente un movimiento específico. _(Consolida HU-86 — AG-00)_ | Media | F |

### EP-07 Seguridad y protección de operaciones

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-77 | Usuario | Como usuario, quiero recibir una notificación cuando se realice una operación en mi cuenta para identificar actividades que no reconozca. | Alta | F |
| HU-78 | Usuario | Como usuario, quiero recibir una alerta cuando se detecte una actividad inusual en mi cuenta para conocer posibles intentos de acceso no autorizado. | Alta | F |
| HU-79 | Administrador | Como administrador, quiero consultar las alertas de seguridad para identificar actividades potencialmente sospechosas. | Alta | F |
| HU-80 | Administrador | Como administrador, quiero consultar los intentos fallidos de autenticación para identificar posibles accesos no autorizados e incidentes de seguridad. _(Consolida HU-91 — AG-00)_ | Alta | F |
| HU-81 | Administrador | Como administrador, quiero consultar los eventos de seguridad asociados a una cuenta para analizar posibles incidentes. | Alta | F |
| HU-83 | Administrador | Como administrador, quiero consultar el estado de seguridad de las cuentas para identificar aquellas que requieren atención. | Media | F |

### EP-08 Registro, historial y auditoría

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-87 | Usuario | Como usuario, quiero identificar el estado de una operación para saber si fue completada, rechazada, cancelada o se encuentra pendiente. | Alta | F |
| HU-88 | Usuario | Como usuario, quiero consultar el identificador de una operación para poder reconocerla y realizar consultas posteriores. | Media | F |
| HU-89 | Administrador | Como administrador, quiero consultar los registros de auditoría para analizar las actividades realizadas en la plataforma. | Alta | F |
| HU-90 | Administrador | Como administrador, quiero buscar y filtrar eventos de auditoría mediante diferentes criterios para localizar rápidamente información relevante. | Alta | F |
| HU-93 | Administrador | Como administrador, quiero consultar los bloqueos, desbloqueos y modificaciones relevantes de las cuentas para supervisar las acciones realizadas sobre ellas. | Alta | F |
| HU-94 | Administrador | Como administrador, quiero consultar las acciones realizadas por el personal autorizado, incluido el personal de atención, para supervisar y verificar el uso adecuado de sus permisos. _(Consolida HU-108 — AG-00)_ | Alta | F |
| HU-95 | Administrador | Como administrador, quiero consultar estadísticas de los eventos registrados para apoyar el análisis de seguridad de la plataforma. | Media | F |

### EP-09 Gestión administrativa y atención al usuario

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-96 | Personal de atención | Como personal de atención, quiero iniciar sesión en el panel de atención para acceder a las funcionalidades que corresponden a mi rol. | Alta | F |
| HU-97 | Administrador | Como administrador, quiero iniciar sesión en el panel administrativo para gestionar y supervisar la plataforma. | Alta | F |
| HU-98 | Personal de atención | Como personal de atención, quiero buscar y consultar los datos básicos de un usuario para brindarle asistencia. | Alta | F |
| HU-99 | Personal de atención | Como personal de atención, quiero iniciar y gestionar un registro asistido para ayudar a un usuario a crear su cuenta. | Alta | F |
| HU-100 | Personal de atención | Como personal de atención, quiero corregir determinados datos personales de un usuario cuando este solicite asistencia para mantener su información actualizada. | Alta | F |
| HU-101 | Personal de atención | Como personal de atención, quiero consultar el estado de una cuenta para conocer si se encuentra activa, bloqueada, suspendida o desactivada. | Alta | F |
| HU-102 | Personal de atención | Como personal de atención, quiero registrar y consultar las solicitudes o incidencias reportadas por los usuarios para darles seguimiento. | Media | F |
| HU-104 | Administrador | Como administrador, quiero consultar y modificar determinados datos de una cuenta para corregir información registrada incorrectamente. | Alta | F |

### Monitoreo y métricas

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-109 | Administrador | Como administrador, quiero consultar un resumen del estado de la plataforma para conocer su funcionamiento general. | Alta | F |
| HU-110 | Administrador | Como administrador, quiero consultar indicadores sobre las cuentas y operaciones realizadas para conocer el nivel de utilización de la plataforma. | Media | F |
| HU-111 | Administrador | Como administrador, quiero consultar métricas de autenticación biométrica para evaluar el desempeño del mecanismo de autenticación por voz. | Alta | F |
| HU-113 | Administrador | Como administrador, quiero consultar métricas de seguridad para identificar eventos que requieran atención. | Alta | F |
| HU-114 | Administrador | Como administrador, quiero consultar métricas de rendimiento de los servicios para identificar posibles problemas de funcionamiento. | Media | F |
| HU-115 | Administrador | Como administrador, quiero visualizar indicadores de seguridad, rendimiento y funcionamiento mediante un panel para facilitar el análisis de la plataforma. | Alta | F |
| HU-116 | Administrador | Como administrador, quiero consultar las métricas recopiladas durante las pruebas de validación para evaluar el cumplimiento de los objetivos del proyecto. | Alta | F |

## 5. Clasificación técnica registrada en el Excel

La hoja `CLASIFICACION` relaciona las historias de usuario con un componente, tecnología y base de datos/procesamiento. Esta clasificación se conserva como **referencia del requerimiento**, pero no debe interpretarse como la arquitectura definitiva del sistema.

> **AG-00:** se retiraron de esta tabla las filas de los IDs eliminados (HU-05, 06, 07, 08, 11, 38, 39, 63) y de los IDs absorbidos por consolidación (HU-82, 84, 85, 86, 91, 92, 103, 105, 106, 107, 108, 112). Sus filas originales se conservan en la sección 11 para trazabilidad. En el componente de HU-15, HU-16 y HU-17 se aplicó el glosario (cuenta de acceso). La columna **Tecnología** no se modificó: sigue siendo una referencia del archivo fuente y no una decisión aprobada.

| HU | Componente | Tecnología | BD / procesamiento |
|---|---|---|---|
| HU-01 | Gestión de usuarios | Flutter + Java/Spring Boot | PostgreSQL |
| HU-02 | Gestión de usuarios | Flutter + Java/Spring Boot | PostgreSQL |
| HU-03 | Gestión de identidad | Flutter + Java/Spring Boot | PostgreSQL + almacenamiento de documentos, si aplica |
| HU-04 | Gestión de usuarios / notificaciones | Flutter + Java/Spring Boot | PostgreSQL |
| HU-09 | Gestión de usuarios | Flutter + Java/Spring Boot | PostgreSQL |
| HU-10 | Gestión de usuarios | Flutter + Java/Spring Boot | PostgreSQL |
| HU-12 | Autenticación/acceso | Flutter + Java/Spring Boot | PostgreSQL |
| HU-13 | Gestión de sesiones | Flutter + Java/Spring Boot | PostgreSQL |
| HU-14 | Gestión de sesiones | Flutter + Java/Spring Boot | PostgreSQL |
| HU-15 | Gestión de cuenta de acceso | Flutter + Java/Spring Boot | PostgreSQL |
| HU-16 | Gestión de cuenta de acceso | Flutter + Java/Spring Boot | PostgreSQL |
| HU-17 | Gestión de cuenta de acceso | Flutter + Java/Spring Boot | PostgreSQL |
| HU-18 | Recuperación de cuenta | Flutter + Java/Spring Boot | PostgreSQL |
| HU-19 | Seguridad de cuentas | Flutter/Web + Java/Spring Boot | PostgreSQL |
| HU-20 | Administración | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-21 | Administración | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-22 | Administración | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-23 | Administración | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-24 | Administración / usuarios internos | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-25 | Administración / seguridad | Panel administrativo + Java/Spring Boot + Spring Security | PostgreSQL |
| HU-26 | Registro biométrico | Flutter + Java + Python | BD biométrica + almacenamiento de audio + procesamiento Python |
| HU-27 | Asistencia de captura | Flutter | — |
| HU-28 | Captura de voz | Flutter + Python | Almacenamiento de audio + procesamiento Python |
| HU-29 | Frase de desafío | Flutter + Python | BD biométrica + procesamiento de voz |
| HU-30 | Repetición de captura | Flutter + Python | Almacenamiento de audio + BD biométrica |
| HU-31 | Evaluación de calidad de voz | Flutter + Python | Procesamiento Python |
| HU-32 | Detección de condiciones ambientales | Flutter + Python | Procesamiento de audio Python |
| HU-33 | Confirmación de registro | Flutter + Java | PostgreSQL / BD biométrica |
| HU-34 | Actualización del perfil biométrico | Flutter + Java + Python | BD biométrica + procesamiento Python |
| HU-35 | Nuevo registro de voz | Flutter + Java + Python | BD biométrica + almacenamiento de audio |
| HU-36 | Eliminación de información biométrica | Flutter + Java + Python | BD biométrica + almacenamiento de audio |
| HU-37 | Asistencia de registro biométrico | Flutter + Java | BD operacional/biométrica |
| HU-40 | Inicio de autenticación biométrica | Flutter + Java + Python | BD biométrica + procesamiento Python |
| HU-41 | Asistencia durante autenticación | Flutter | — |
| HU-42 | Captura de voz para autenticación | Flutter + Python | Almacenamiento temporal + procesamiento Python |
| HU-43 | Frase de desafío | Flutter + Python | BD biométrica + procesamiento Python |
| HU-44 | Repetición de captura | Flutter + Python | Procesamiento Python |
| HU-45 | Resultado de autenticación | Flutter + Java + Python | BD biométrica |
| HU-46 | Autenticación previa a operación | Flutter + Java + Python | BD operacional + BD biométrica |
| HU-47 | Instrucciones ante autenticación fallida | Flutter + Java | BD biométrica/operacional |
| HU-48 | Validación biométrica / Anti-spoofing | Python + FastAPI | Procesamiento Python + modelo anti-spoofing |
| HU-49 | Resultado de detección | Flutter + Java + Python | BD biométrica |
| HU-50 | Asistencia ante rechazo | Flutter | — |
| HU-51 | Administración / monitoreo de seguridad | Panel administrativo + Java/Spring Boot | PostgreSQL / BD biométrica |
| HU-52 | Estadísticas de spoofing | Panel administrativo + Java/Spring Boot + Python | BD biométrica + procesamiento estadístico |
| HU-53 | Interfaz accesible | Flutter | — |
| HU-54 | Asistencia auditiva | Flutter + TTS | — |
| HU-55 | Navegación accesible | Flutter + tecnologías de asistencia | — |
| HU-56 | Etiquetas accesibles | Flutter | — |
| HU-57 | Señales auditivas | Flutter + TTS | — |
| HU-58 | Repetición de instrucciones | Flutter + TTS | — |
| HU-59 | Descripción auditiva | Flutter + TTS | — |
| HU-60 | Confirmación/cancelación | Flutter + Java/Spring Boot | PostgreSQL, cuando corresponda registrar la operación |
| HU-61 | Lectura de datos de operación | Flutter + TTS + Java/Spring Boot | PostgreSQL |
| HU-62 | Control de captura de audio | Flutter | — |
| HU-64 | Mensajes accesibles de error | Flutter + TTS | — |
| HU-65 | Uso independiente de funciones | Flutter + Java/Spring Boot | Según la funcionalidad utilizada |
| HU-66 | Consulta de saldo | Flutter + Java/Spring Boot | PostgreSQL |
| HU-67 | Consulta de movimientos | Flutter + Java/Spring Boot | PostgreSQL |
| HU-68 | Detalle de operación | Flutter + Java/Spring Boot | PostgreSQL |
| HU-69 | Transferencias | Flutter + Java/Spring Boot | PostgreSQL |
| HU-70 | Confirmación de transferencia | Flutter + Java/Spring Boot | PostgreSQL |
| HU-71 | Cancelación de transferencia | Flutter + Java/Spring Boot | PostgreSQL |
| HU-72 | Resultado de transferencia | Flutter + Java/Spring Boot | PostgreSQL |
| HU-73 | Pagos | Flutter + Java/Spring Boot | PostgreSQL |
| HU-74 | Confirmación de pago | Flutter + Java/Spring Boot | PostgreSQL |
| HU-75 | Resultado de pago | Flutter + Java/Spring Boot | PostgreSQL |
| HU-76 | Filtrado de operaciones | Flutter + Java/Spring Boot | PostgreSQL |
| HU-77 | Notificaciones de operaciones | Flutter + Java/Spring Boot | PostgreSQL |
| HU-78 | Detección de actividad inusual | Java/Spring Boot + Python* | PostgreSQL / procesamiento de seguridad |
| HU-79 | Administración de alertas | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-80 | Consulta de intentos fallidos | Panel administrativo + Java/Spring Boot | PostgreSQL / BD biométrica |
| HU-81 | Eventos de seguridad | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-83 | Estado de seguridad | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-87 | Estado de operación | Flutter + Java/Spring Boot | PostgreSQL |
| HU-88 | Identificador de operación | Flutter + Java/Spring Boot | PostgreSQL |
| HU-89 | Consulta de auditoría | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-90 | Búsqueda y filtros de auditoría | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-93 | Bloqueos y modificaciones | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-94 | Acciones del personal | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-95 | Estadísticas de eventos | Panel administrativo + Java/Spring Boot | PostgreSQL + procesamiento estadístico |
| HU-96 | Panel de atención | Web + Java/Spring Boot | PostgreSQL |
| HU-97 | Panel administrativo | Web + Java/Spring Boot | PostgreSQL |
| HU-98 | Consulta de usuarios | Panel de atención + Java/Spring Boot | PostgreSQL |
| HU-99 | Registro asistido | Panel de atención + Java/Spring Boot | PostgreSQL |
| HU-100 | Corrección de datos | Panel de atención + Java/Spring Boot | PostgreSQL |
| HU-101 | Estado de cuenta | Panel de atención + Java/Spring Boot | PostgreSQL |
| HU-102 | Solicitudes/incidencias | Panel de atención + Java/Spring Boot | PostgreSQL |
| HU-104 | Modificación de cuentas | Panel administrativo + Java/Spring Boot | PostgreSQL |
| HU-109 | Panel de monitoreo | Panel administrativo + Java/Spring Boot | PostgreSQL + consultas agregadas |
| HU-110 | Métricas de utilización | Panel administrativo + Java/Spring Boot | PostgreSQL + procesamiento estadístico |
| HU-111 | Métricas de autenticación biométrica | Panel administrativo + Java/Spring Boot + Python | BD biométrica + procesamiento estadístico |
| HU-113 | Métricas de seguridad | Panel administrativo + Java/Spring Boot | PostgreSQL + procesamiento estadístico |
| HU-114 | Métricas de rendimiento | Panel administrativo + Java/Spring Boot | Sistema de monitoreo / almacenamiento de métricas |
| HU-115 | Dashboard general | Panel administrativo + Java/Spring Boot | PostgreSQL + consultas agregadas |
| HU-116 | Métricas de validación | Panel administrativo + Python | BD biométrica / conjunto de datos de validación |

## 6. Modelo CORE registrado

La hoja `TABLAS CORE` contiene una propuesta inicial de estructuras de datos. Estas estructuras deben validarse posteriormente en `03_BASE_DE_DATOS.md` antes de generar migraciones o código definitivo.

> **AG-00:** las filas marcadas con _(AG-00)_ en `CUENTAS` y `OPERACIONES`, y la tabla `ENTIDADES_BANCARIAS`, no proceden del Excel original: incorporan las decisiones D-025 a D-029. Los tipos de dato de las nuevas referencias siguen la estrategia de identificadores, que continúa pendiente (`03_BASE_DE_DATOS_NAYRA.md` §13).

### ROLES

| Atributo | Tipo | Descripción |
|---|---|---|
| PK | Identificador |  |
| VARCHAR(50) | Usuario, Administrador, etc. |  |
| VARCHAR(200) | Descripción del rol |  |
| VARCHAR(20) | ACTIVO / INACTIVO |  |

### USUARIOS

| Atributo | Tipo | Descripción |
|---|---|---|

### CUENTAS

Cuenta financiera simulada. Cada usuario tiene una única cuenta financiera (D-025). La tabla conserva el nombre `CUENTAS` (D-027).

| Atributo | Tipo | Descripción |
|---|---|---|
| UUID / BIGINT | PK |  |
| FK → USUARIOS, UNIQUE | Propietario. Relación 1:1 con el usuario; se establece al vincular la cuenta durante el registro (D-028) _(AG-00)_ |  |
| FK → ENTIDADES_BANCARIAS | Entidad bancaria simulada a la que pertenece la cuenta (D-026) _(AG-00)_ |  |
| VARCHAR(30) | Identificador de la cuenta |  |
| DECIMAL(15,2) | Saldo disponible |  |
| VARCHAR(3) | PEN, USD, etc. |  |
| VARCHAR(20) | ACTIVA, BLOQUEADA, CERRADA |  |
| TIMESTAMP | Fecha de creación |  |
| TIMESTAMP | Última actualización |  |

### ENTIDADES_BANCARIAS _(AG-00)_

Catálogo de referencia de entidades bancarias simuladas (D-026). No es gestionado por el administrador ni representa bancos reales.

| Atributo | Tipo | Descripción |
|---|---|---|
| PK | Identificador (`id`) |  |
| Texto (longitud pendiente) | Nombre de la entidad bancaria simulada (`nombre`) |  |

### SESIONES

| Atributo | Tipo | Descripción |
|---|---|---|

### OPERACIONES

| Atributo | Tipo | Descripción |
|---|---|---|
| UUID / BIGINT | PK |  |
| FK → CUENTAS | Cuenta financiera de origen (cuenta que realiza la operación) (D-029) |  |
| VARCHAR(30) | TRANSFERENCIA, PAGO, RECARGA, etc. |  |
| DECIMAL(15,2) | Monto de la operación |  |
| VARCHAR(3) | Moneda |  |
| VARCHAR(255) | Detalle |  |
| FK → CUENTAS | Cuenta financiera de destino, cuando corresponda (D-029). Sustituye al campo de texto `VARCHAR(30)` del Excel _(AG-00)_ |  |
| VARCHAR(100) | Código/referencia de operación |  |
| VARCHAR(20) | PENDIENTE, EXITOSA, RECHAZADA |  |
| TIMESTAMP | Fecha y hora |  |
| TIMESTAMP | Última actualización |  |

### DISPOSITIVOS

| Atributo | Tipo | Descripción |
|---|---|---|

### NOTIFICACIONES

| Atributo | Tipo | Descripción |
|---|---|---|
| UUID | PK |  |
| FK | Destinatario |  |
| VARCHAR(30) | OPERACION, SEGURIDAD, SISTEMA |  |
| VARCHAR(150) | Título |  |
| TEXT | Contenido |  |
| BOOLEAN | Si fue leída |  |
| TIMESTAMP | Fecha de generación |  |
| TIMESTAMP | Fecha en que se leyó |  |

### SOLICITUDES_ATENCION

| Atributo | Tipo | Descripción |
|---|---|---|

### AUDITORÍA

| Atributo | Tipo | Descripción |
|---|---|---|
| UUID | PK |  |
| FK | Usuario relacionado |  |
| VARCHAR(50) | Tipo de acción |  |
| TEXT | Detalle |  |
| VARCHAR(20) | EXITOSO / FALLIDO |  |
| VARCHAR(45) | IP |  |
| FK | Dispositivo utilizado |  |
| TIMESTAMP | Fecha y hora |  |

## 7. Observaciones de consistencia entre hojas

La versión `REQUERIMIENTOS(7).xlsx` contiene dos fuentes relacionadas que deben interpretarse de forma distinta:

- La hoja `HU` es la fuente de las **historias de usuario vigentes y su redacción**.
- La hoja `CLASIFICACION` contiene referencias de componente, tecnología y procesamiento/BD asociadas a determinados IDs.

En versiones anteriores de este documento, la hoja `CLASIFICACION` contenía referencias para los IDs **HU-05, HU-06, HU-07, HU-08, HU-11, HU-38, HU-39 y HU-63**, sin historia de usuario completa en la hoja `HU`. Por decisión **AG-00 (D-031)** esos IDs quedaron **eliminados** por no ser aplicables y se retiraron de `CLASIFICACION`. Sus filas originales, incluida la anotación "no va" de HU-05, se conservan únicamente como traza en la sección 11.

Esto **no autoriza a Claude a inventar historias** para esos IDs ni a reutilizarlos. Para cualquier otro caso análogo, debe aplicarse la siguiente regla:

> Si un ID aparece únicamente en `CLASIFICACION` y no tiene una historia completa en `HU`, su contenido funcional se considera **no definido en la fuente de requisitos vigente** hasta que el equipo incorpore una descripción aprobada.

Además, la hoja `HU` presenta saltos de numeración, a los que se suman los huecos producidos por AG-00 (IDs eliminados y absorbidos). Estos saltos deben conservarse y **no deben interpretarse automáticamente como requisitos faltantes**. Las HUs no se renumeran mientras no exista una decisión explícita.

Por tanto, para la implementación se establece esta precedencia:

```text
Historia funcional y redacción
        ↓
HU

Clasificación técnica de una HU existente
        ↓
CLASIFICACION

Si el ID existe solo en CLASIFICACION
        ↓
No inventar la historia
        ↓
Solicitar/esperar definición aprobada
```

## 8. Reglas de implementación derivadas de los requisitos

1. **No inventar historias de usuario.** Si una funcionalidad no está en los requisitos aprobados, no incorporarla como requisito del sistema.
2. **No eliminar requisitos.** Una historia marcada como prioritaria debe mantenerse durante el diseño salvo decisión explícita del equipo.
3. **Prioridad no equivale a orden arquitectónico.** La prioridad Alta/Media indica la prioridad registrada en el Excel, no determina por sí sola el orden de construcción.
4. **F/NF debe conservarse.** Los requisitos no funcionales deben considerarse restricciones de diseño y aceptación, no funcionalidades de usuario.
5. **La clasificación técnica no sustituye la arquitectura.** La columna `Componente` y las tecnologías de `CLASIFICACION` son referencias del archivo fuente.
6. **La base de datos CORE requiere validación.** No generar automáticamente todas las tablas del Excel sin revisar relaciones, cardinalidades, seguridad y correspondencia con los requisitos.
7. **Las operaciones financieras son de alcance simulado cuando el requisito lo indica.** HU-73 especifica expresamente un pago desde la billetera en una operación de pago simulada.
8. **La accesibilidad es parte del alcance.** HU-53 y HU-55 están clasificadas como NF, mientras que varias historias de interacción accesible están clasificadas como F.
9. **Biometría y spoofing son áreas diferenciadas.** Las historias de EP-02, EP-03 y EP-04 deben mantenerse trazables por separado.
10. **La implementación debe mantener trazabilidad:** requisito → diseño → componente → API/lógica → código → prueba.

## 9. Trazabilidad mínima recomendada

| Elemento | Documento donde se define |
|---|---|
| Qué debe hacer el sistema | `01_REQUISITOS.md` |
| Arquitectura | `02_ARQUITECTURA.md` |
| Modelo de datos definitivo | `03_BASE_DE_DATOS.md` |
| Contratos de API | `04_API.md` |
| Biometría y spoofing | `05_BIOMETRIA.md` |
| Seguridad | `06_SEGURIDAD.md` |
| Decisiones tecnológicas | `07_DECISIONES_TECNICAS.md` |
| Estado real de implementación | `08_ESTADO_PROYECTO.md` |
| Reglas de código | `09_REGLAS_DESARROLLO.md` |

## 10. Nota sobre decisiones arquitectónicas anteriores

Las propuestas arquitectónicas realizadas en etapas anteriores y posteriormente descartadas por el equipo/profesorado **no forman parte de los requisitos vigentes**. Este documento no debe utilizarse para reconstruir esas arquitecturas. La arquitectura vigente se definirá exclusivamente en `02_ARQUITECTURA.md`.

## 11. Depuración de requisitos — AG-00 (2026-09-25)

Decisiones de referencia: `07_DECISIONES_TECNICAS_NAYRA.md`, D-024 a D-033. Las HUs **no se renumeraron** y cada HU vigente permanece en su épica.

Resultado: de 108 HUs en la hoja `HU` quedan **96 HUs vigentes** (12 absorbidas por consolidación). Además se retiraron de `CLASIFICACION` 8 IDs que no tenían historia en la hoja `HU`.

### 11.1 IDs eliminados por no ser aplicables

Fila original en `CLASIFICACION`, conservada solo como traza. Estos IDs no deben reutilizarse.

| ID | Componente | Tecnología | BD / procesamiento | Observación |
|---|---|---|---|---|
| HU-05 | Registro asistido | Flutter + Java/Spring Boot | PostgreSQL | La hoja fuente incluía la anotación "no va" |
| HU-06 | Registro asistido | Flutter + Java/Spring Boot | PostgreSQL | — |
| HU-07 | Registro asistido | Flutter + Java/Spring Boot | PostgreSQL | — |
| HU-08 | Registro asistido / identidad | Flutter + Java/Spring Boot | PostgreSQL | — |
| HU-11 | Gestión de usuarios | Flutter + Java/Spring Boot | PostgreSQL | — |
| HU-38 | Registro biométrico asistido | Flutter + Java + Python | BD biométrica + almacenamiento de audio | — |
| HU-39 | Transferencia de control de captura | Flutter + Java | BD operacional | Ninguna HU vigente cubre esta idea |
| HU-63 | Control de volumen | Flutter | Configuración local del dispositivo | Ninguna HU vigente cubre esta idea |

### 11.2 HUs consolidadas (C1–C11)

Criterios aplicados: se conserva la HU de primera aparición en su épica actual; se conserva la prioridad más alta; el texto consolidado une el contenido funcional de todas las HUs fusionadas.

| # | HU vigente (épica) | HUs absorbidas |
|---|---|---|
| C1 | HU-19 (EP-01) | HU-82, HU-105 |
| C2 | HU-20 y HU-21 (EP-01), sin cambio de texto | HU-103 |
| C3 | HU-24 (EP-01); prioridad sube de Media a Alta | HU-106 |
| C4 | HU-25 (EP-01) | HU-107 |
| C5 | HU-67 (EP-06) | HU-84 |
| C6 | HU-68 (EP-06) | HU-85 |
| C7 | HU-76 (EP-06) | HU-86 |
| C8 | HU-80 (EP-07) | HU-91 |
| C9 | HU-51 (EP-04) | HU-92 |
| C10 | HU-94 (EP-08) | HU-108 |
| C11 | HU-52 (EP-04); prioridad sube de Media a Alta | HU-112 |

Texto original y clasificación original de las HUs absorbidas (traza; no son HUs vigentes):

| ID | Épica original | Historia original | Prioridad | Clasificación original (Componente / Tecnología / BD) | Absorbida por |
|---|---|---|---|---|---|
| HU-82 | EP-07 | Como administrador, quiero bloquear o desbloquear una cuenta ante una incidencia de seguridad para protegerla o restablecer su acceso cuando corresponda. | Alta | Bloqueo/desbloqueo / Panel administrativo + Java/Spring Boot / PostgreSQL | HU-19 |
| HU-84 | EP-08 | Como usuario, quiero consultar el historial de mis operaciones para conocer las actividades realizadas desde mi cuenta. | Alta | Historial de operaciones / Flutter + Java/Spring Boot / PostgreSQL | HU-67 |
| HU-85 | EP-08 | Como usuario, quiero consultar el detalle de una operación para conocer la información asociada a un movimiento específico. | Alta | Detalle de operación / Flutter + Java/Spring Boot / PostgreSQL | HU-68 |
| HU-86 | EP-08 | Como usuario, quiero filtrar mi historial de operaciones por tipo o periodo para localizar rápidamente una operación determinada. | Media | Filtro de operaciones / Flutter + Java/Spring Boot / PostgreSQL | HU-76 |
| HU-91 | EP-08 | Como administrador, quiero consultar los intentos de autenticación fallidos para identificar posibles incidentes de seguridad. | Alta | Intentos fallidos de autenticación / Panel administrativo + Java/Spring Boot / PostgreSQL / BD biométrica | HU-80 |
| HU-92 | EP-08 | Como administrador, quiero consultar los eventos de spoofing detectados para analizar posibles intentos de suplantación. | Alta | Eventos de spoofing / Panel administrativo + Java/Spring Boot / BD biométrica | HU-51 |
| HU-103 | EP-09 | Como administrador, quiero consultar y buscar los usuarios registrados para gestionar las cuentas de la plataforma. | Alta | Gestión de usuarios / Panel administrativo + Java/Spring Boot / PostgreSQL | HU-20 y HU-21 |
| HU-105 | EP-09 | Como administrador, quiero gestionar el estado de las cuentas para bloquearlas o desbloquearlas cuando exista una incidencia de seguridad. | Alta | Bloqueo/desbloqueo / Panel administrativo + Java/Spring Boot / PostgreSQL | HU-19 |
| HU-106 | EP-09 | Como administrador, quiero gestionar las cuentas del personal de atención para controlar quién puede acceder al panel correspondiente. | Alta | Gestión de personal / Panel administrativo + Java/Spring Boot / PostgreSQL | HU-24 |
| HU-107 | EP-09 | Como administrador, quiero asignar roles y permisos al personal autorizado para controlar las funcionalidades disponibles según sus responsabilidades. | Alta | Roles y permisos / Panel administrativo + Java/Spring Boot / PostgreSQL | HU-25 |
| HU-108 | EP-09 | Como administrador, quiero consultar las acciones realizadas por el personal de atención para supervisar el uso adecuado de sus permisos. | Media | Auditoría del personal / Panel administrativo + Java/Spring Boot / PostgreSQL | HU-94 |
| HU-112 | Monitoreo y métricas | Como administrador, quiero consultar métricas de detección de spoofing para evaluar el desempeño del mecanismo de protección. | Alta | Métricas de spoofing / Panel administrativo + Java/Spring Boot + Python / BD biométrica + procesamiento estadístico | HU-52 |

Texto original de las HUs vigentes cuya redacción cambió por la consolidación:

| HU | Texto original | Prioridad original |
|---|---|---|
| HU-19 | Como administrador, quiero gestionar el bloqueo y desbloqueo de las cuentas para responder ante incidencias de seguridad o solicitudes autorizadas. | Alta |
| HU-24 | Como administrador, quiero gestionar las cuentas del personal de atención para controlar quién puede realizar funciones autorizadas dentro de la plataforma. | Media |
| HU-25 | Como administrador, quiero asignar roles y permisos al personal autorizado para limitar las funcionalidades disponibles según sus responsabilidades. | Alta |
| HU-51 | Como administrador, quiero consultar los eventos relacionados con intentos de spoofing para identificar posibles amenazas contra las cuentas. | Alta |
| HU-52 | Como administrador, quiero consultar estadísticas de detección de spoofing para evaluar el comportamiento del mecanismo de protección. | Media |
| HU-67 | Como usuario, quiero consultar mis movimientos para conocer las operaciones realizadas desde mi billetera. | Alta |
| HU-68 | Como usuario, quiero consultar el detalle de una operación para conocer la información específica de un movimiento. | Alta |
| HU-76 | Como usuario, quiero consultar mis operaciones por tipo o periodo para localizar rápidamente un movimiento específico. | Media |
| HU-80 | Como administrador, quiero consultar los intentos fallidos de autenticación para identificar posibles intentos de acceso no autorizado. | Alta |
| HU-94 | Como administrador, quiero consultar las acciones realizadas por el personal autorizado para verificar el uso adecuado de sus permisos. | Alta |

### 11.3 Posibles duplicados pendientes (D1–D6)

Se mantienen **separados y sin modificar** hasta una decisión explícita:

| ID | HUs | Motivo de la duda |
|---|---|---|
| D1 | HU-23 / HU-104 | HU-104 habla de "datos de una cuenta": si es la cuenta de acceso, duplica HU-22/HU-23; si es la cuenta financiera, es un requisito distinto |
| D2 | HU-34 / HU-35 | "Actualizar" frente a "volver a registrar" el perfil biométrico; depende de D-011 |
| D3 | HU-62 / HU-28 / HU-42 | HU-62 generaliza el control manual de captura de HU-28 y HU-42 |
| D4 | HU-95 / HU-113 | Estadísticas de eventos frente a métricas de seguridad |
| D5 | HU-109 / HU-115 | Resumen de estado frente a panel de indicadores |
| D6 | HU-60 / HU-70 / HU-71 / HU-74 | HU-60 es un principio transversal de confirmación/cancelación |

### 11.4 Reglas funcionales derivadas de AG-00

- **Cuenta financiera única (D-025):** cada usuario tiene una sola cuenta financiera, asociada a una entidad bancaria simulada (D-026).
- **Asociación por DNI (D-028):** durante el registro, el DNI se utiliza para localizar la cuenta financiera simulada; la relación persistente entre `CUENTAS` y `USUARIOS` es una FK con restricción de unicidad.
- **Transferencias (D-030):** el emisor opera con su única cuenta financiera y el sistema identifica la única cuenta financiera del destinatario; no existe selección entre cuentas. Las operaciones referencian la cuenta financiera de origen y la de destino (D-029).
- **Registro con voz (D-032):** el registro de Nayra incluye el registro de la voz del usuario (HU-26 a HU-33) para su uso posterior en la autenticación biométrica. HU-01 y HU-26 se mantienen como HUs independientes en sus épicas. El mecanismo exacto de identificación durante el registro queda **pendiente para AG-01**.
- **Exclusiones (D-033):** no se incorporan funcionalidades de apertura de cuentas bancarias, múltiples cuentas por usuario, transferencias entre cuentas propias ni gestión de entidades bancarias por parte del administrador.

### 11.5 Pendientes derivados de AG-00

- Relación exacta del término "billetera" con la cuenta financiera.
- Interpretación de "cuenta" en HU-93 (modificaciones relevantes), HU-104 (D1) y HU-110.
- Casos del registro por DNI: DNI sin cuenta simulada, cuenta ya vinculada a otro usuario, cuentas duplicadas en los datos simulados, documentos distintos del DNI (p. ej. CE), corrección posterior del DNI, y si el personal de atención y el administrador tienen cuenta financiera.
- Dato del entorno simulado con el que se localiza la cuenta por DNI (ver `03_BASE_DE_DATOS_NAYRA.md` §15).
- Destino de las operaciones de tipo PAGO (HU-73) y regla de moneda en transferencias.
- Estado CANCELADA de operaciones (HU-87) y estados de la cuenta de acceso frente a los de la cuenta financiera (AG-05).
- Nombre de EP-01 y código de épica para "Monitoreo y métricas".
- Renumeración de HUs (aplazada).
