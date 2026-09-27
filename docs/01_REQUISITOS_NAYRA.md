# 01_REQUISITOS_NAYRA.md — Requisitos del sistema Nayra

> **Fuente:** `REQUERIMIENTOS(7).xlsx`. Este documento consolida las historias de usuario, su clasificación técnica y el modelo de tablas CORE proporcionados en el archivo. No se agregan requisitos funcionales nuevos.
>
> **Actualización AG-00 (2026-09-25):** se aplicaron las decisiones aprobadas de AG-00 registradas en `07_DECISIONES_TECNICAS_NAYRA.md` (D-024 a D-033): glosario de términos, eliminación de IDs no aplicables, consolidación de HUs duplicadas y ajustes del modelo de cuenta financiera y entidad bancaria. El detalle de trazabilidad está en la sección 11. **Las HUs no se renumeraron.**
>
> **Actualización AG-01 (2026-09-26):** se aplicaron las decisiones D-034 a D-044 (`07_DECISIONES_TECNICAS_NAYRA.md`): alcance del primer entregable, identidad simulada, registro, autenticación, dispositivo único, recuperación, roles USER/ADMIN, transferencias y QR. Se agregaron las HU-117 a HU-125, se reasignaron o marcaron las HUs del antiguo actor "Personal de atención" y se clasificó el alcance del primer entregable. Detalle en la sección 12.
>
> **Actualización AG-01 v5 (2026-09-26):** el registro pasa a ser **asistido** por un representante autorizado que valida el DNI y la identidad (D-052, regla de negocio, sin nuevo rol); el QR (HU-123, HU-124) queda como funcionalidad secundaria para un siguiente entregable. Ver sección 12.
>
> **Actualización AG-01 v6 (2026-09-26):** flujo de registro asistido definitivo en 13 pasos (D-052) y separación explícita entre registro inicial, inicio de sesión y cambio/recuperación de dispositivo; el DNI **no** forma parte del inicio de sesión habitual (D-053).
>
> **Actualización AG-02 (2026-09-27):** decisiones técnicas del módulo de voz (D-010 a D-013, D-046 parcial, D-054 a D-060) y cuatro decisiones que afectan la interpretación de las HUs: **PIN de 6 dígitos** como credencial (D-061, modifica D-037), **par de claves del dispositivo** (D-048), **cierre de sesión tras 5 minutos de inactividad** (D-018, parcial) y **Flutter** para la aplicación móvil (D-007). No se agregan HUs ni se modifica la redacción de las existentes; las interpretaciones están en la sección 13.

## 1. Propósito

Este documento constituye la referencia de requisitos para el desarrollo de Nayra. Define las historias de usuario, actores, prioridad y clasificación funcional/no funcional. La implementación debe trazarse contra estos requisitos.

## 2. Actores identificados

- **Usuario** (rol `USER`): persona que utiliza Nayra desde la aplicación móvil y sus funcionalidades de billetera y autenticación.
- **Administrador** (rol `ADMIN`): responsable de gestión, supervisión, seguridad, atención de solicitudes y métricas de la plataforma, desde el panel web. No puede modificar saldos ni operaciones financieras (D-041).

> **AG-01 (D-041):** solo existen los roles `USER` y `ADMIN`. El actor **Personal de atención** fue retirado; sus HUs se reasignaron al administrador, se declararon sin objeto o quedaron fuera del primer entregable (sección 12.3). Los nombres de rol en las HUs ("usuario", "administrador") corresponden a `USER` y `ADMIN`.

## 3. Convenciones

- **F:** requisito funcional.
- **NF:** requisito no funcional.
- **Alta / Media:** prioridad registrada en el archivo fuente.
- Las historias de usuario se mantienen con su redacción original, excepto las HUs consolidadas por AG-00 (D-031) y las reasignadas por AG-01 (D-041), cuya redacción se indica en la propia historia y se traza en las secciones 11 y 12.
- Marcadores AG-01 en las HUs: **[SIN OBJETO]** (la HU se conserva por trazabilidad pero no aplica con las decisiones vigentes), **[FUERA DEL PRIMER ENTREGABLE]** (la HU sigue vigente, pero no se implementa en el prototipo actual), **[INCLUIDA — D-052]** (HU reincorporada al primer entregable por el registro asistido) y _(Nueva — AG-01)_.
- La tecnología indicada en la clasificación es una referencia de implementación del archivo fuente; no reemplaza una decisión arquitectónica aprobada.
- Los IDs de HU **no se renumeran**. Los huecos de numeración resultantes de AG-00 son intencionales (ver sección 11).

### 3.1 Glosario (AG-00, D-024)

| Término | Significado |
|---|---|
| **Cuenta de acceso** (perfil Nayra) | Identidad y acceso del usuario a Nayra. Sobre ella actúan el registro, el inicio de sesión, la desactivación/reactivación y el bloqueo/desbloqueo. |
| **Cuenta financiera** | Única cuenta simulada del usuario dentro del entorno bancario simulado, asociada a una entidad bancaria simulada. Corresponde a la tabla `CUENTAS` (D-025, D-027). |
| **Entidad bancaria** | Banco simulado del entorno controlado (D-021). Corresponde a la tabla de referencia `ENTIDADES_BANCARIAS` (D-026). |
| **Billetera** | Término usado en las HUs sin definición formal. Su relación exacta con la cuenta financiera queda **pendiente de confirmación** (ver sección 11.5). |
| **Registro de identidad simulado** | Fuente de identidad del entorno controlado que relaciona DNI, nombres y apellidos; se consulta durante el registro (D-035). No es una API real. Consultarlo **no** prueba la titularidad ni la identidad (D-052). |
| **Representante autorizado** | Administrador u otra persona autorizada de la organización que, en el registro asistido, asiste a la persona y valida su identidad (D-052). Es parte de una **regla de negocio**, no un rol de la aplicación. |
| **Dispositivo vinculado** | Único dispositivo activo asociado a una cuenta de acceso; determina qué cuenta intenta iniciar sesión (D-039). Se identifica mediante un par de claves cuya clave privada no sale del teléfono (D-048). |
| **PIN** | Credencial de conocimiento de 6 dígitos que concreta la "contraseña" de D-037 y de HU-118 (D-061). |
| **Contacto** (para transferencias) | Otro usuario registrado en Nayra. No incluye la agenda del teléfono (D-042). |
| **QR de la cuenta** | Código asociado a una cuenta financiera simulada que solo identifica al destinatario dentro del entorno simulado; no es un mecanismo de autenticación (D-042). |
| **Frase de desafío** | Frase variable que Nayra propone en cada autenticación; la respuesta del usuario es la muestra biométrica (D-037). Su estructura es palabra + 3 dígitos + palabra (D-054). |

Regla: el término "cuenta" en una HU debe interpretarse según su contexto como **cuenta de acceso** o **cuenta financiera**. Interpretación aplicada a las HUs vigentes que usan el término:

| HU | Interpretación |
|---|---|
| HU-01, HU-04, HU-09, HU-12, HU-13, HU-14, HU-15, HU-16, HU-17, HU-18, HU-19, HU-20, HU-21, HU-24, HU-51, HU-78, HU-81, HU-83, HU-99, HU-101 | Cuenta de acceso |
| HU-03 | La verificación de identidad la realiza un **representante autorizado** (D-052), apoyado en los datos del registro de identidad simulado. La consulta del DNI por sí sola **no** prueba la titularidad ni la identidad, y la biometría **no** se usa para validar la identidad en el registro. | D-035, D-052 |
| HU-67, HU-77 | Cuenta financiera |
| HU-93 | Cuenta de acceso para bloqueos/desbloqueos; si "modificaciones relevantes" incluye la cuenta financiera queda **pendiente** |
| HU-104 | **Pendiente** (duplicado dudoso D1) |
| HU-110 | **Pendiente** (cuentas de acceso, cuentas financieras o ambas) |

## 4. Épicas e historias de usuario


### EP-01 Gestión de cuentas y usuarios

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-01 | Usuario | Como usuario, quiero registrarme en Nayra para crear una cuenta y utilizar los servicios de la billetera digital. | Alta | F |
| HU-02 | Los datos de identidad (nombres, apellidos) se obtienen del registro de identidad simulado a partir del DNI y la **persona confirma sus datos** tras la validación del representante; el celular y la contraseña se cubren en HU-117 y HU-118. | D-035, D-052 |
| HU-03 | Usuario | Como usuario, quiero verificar mi identidad durante el registro para asegurar que la cuenta corresponde a mi persona. | Alta | F |
| HU-04 | La confirmación de la cuenta de acceso se entrega al **finalizar el registro**, después de la vinculación del dispositivo, el enrolamiento de voz y el tutorial. | D-052 |
| HU-09 | Usuario | Como usuario, quiero consultar mis datos personales para verificar la información asociada a mi cuenta. | Media | F |
| HU-10 | Usuario | Como usuario, quiero actualizar determinados datos personales para mantener mi información vigente. | Alta | F |
| HU-12 | Usuario | Como usuario registrado, quiero acceder a mi cuenta desde la aplicación para utilizar las funcionalidades de Nayra. | Alta | F |
| HU-13 | Usuario | Como usuario, quiero cerrar mi sesión para proteger mi cuenta cuando termine de utilizar la aplicación. | Alta | F |
| HU-14 | Usuario | Como usuario, quiero gestionar mis sesiones activas para controlar los dispositivos desde los cuales se accede a mi cuenta. _(Interpretación AG-01: una cuenta de acceso tiene un único dispositivo activo, D-039; ver §12.1)_ | Media | F |
| HU-15 | Usuario | Como usuario, quiero conocer el estado de mi cuenta para saber si se encuentra disponible para su utilización. | Media | F |
| HU-16 | Usuario | Como usuario, quiero solicitar la desactivación de mi cuenta para dejar de utilizar temporalmente la billetera digital. | Alta | F |
| HU-17 | Usuario | Como usuario, quiero solicitar la reactivación de mi cuenta desactivada para volver a utilizar la billetera digital. | Alta | F |
| HU-18 | Usuario | Como usuario, quiero recuperar el acceso a mi cuenta cuando se encuentre bloqueada o no pueda acceder para volver a utilizar Nayra. | Alta | F |
| HU-19 | Administrador | Como administrador, quiero gestionar el bloqueo y desbloqueo de las cuentas de acceso de los usuarios, ante incidencias de seguridad o solicitudes autorizadas, para protegerlas o restablecer su acceso cuando corresponda. _(Consolida HU-82 y HU-105 — AG-00)_ | Alta | F |
| HU-20 | Administrador | Como administrador, quiero consultar los usuarios registrados para gestionar las cuentas de la plataforma. _(Junto con HU-21, absorbe HU-103 — AG-00)_ | Alta | F |
| HU-21 | Administrador | Como administrador, quiero buscar usuarios mediante sus datos identificativos para localizar rápidamente una cuenta. _(Junto con HU-20, absorbe HU-103 — AG-00)_ | Alta | F |
| HU-22 | Administrador | Como administrador, quiero consultar información básica de un usuario para atender necesidades administrativas o de seguridad. | Alta | F |
| HU-23 | Administrador | Como administrador, quiero modificar determinados datos personales de un usuario cuando sea necesario corregir información registrada incorrectamente. | Alta | F |
| HU-24 | Administrador | **[SIN OBJETO — D-041: no existe rol de personal de atención]** Como administrador, quiero gestionar las cuentas de acceso del personal de atención para controlar quién puede acceder al panel de atención y realizar funciones autorizadas dentro de la plataforma. _(Consolida HU-106 — AG-00)_ | Alta | F |
| HU-25 | Administrador | **[FUERA DEL PRIMER ENTREGABLE — D-041: roles fijos USER/ADMIN; reformulación pendiente]** Como administrador, quiero asignar roles y permisos al personal autorizado para controlar y limitar las funcionalidades disponibles según sus responsabilidades. _(Consolida HU-107 — AG-00)_ | Alta | F |
| HU-117 | Usuario | Como usuario, quiero registrar mi número de celular durante el registro para que Nayra cuente con un medio de contacto en procesos de atención y recuperación. _(Nueva — AG-01)_ | Alta | F |
| HU-118 | Usuario | Como usuario, quiero crear una contraseña de acceso durante el registro, pudiendo dictarla mediante voz, para utilizarla junto con mi voz al iniciar sesión. _(Nueva — AG-01)_ | Alta | F |
| HU-119 | Usuario | Como usuario, quiero que mi dispositivo quede vinculado a mi cuenta de acceso al finalizar el registro para que Nayra reconozca mi cuenta al iniciar sesión sin pedirme el DNI. _(Nueva — AG-01)_ | Alta | F |
| HU-120 | Usuario | Como usuario, quiero cambiar de celular validando mi identidad con mi contraseña y mi voz para seguir utilizando Nayra en el nuevo dispositivo. _(Nueva — AG-01)_ | Alta | F |
| HU-121 | Usuario | Como usuario, quiero solicitar el bloqueo de mi cuenta de acceso y de mi dispositivo cuando pierda mi celular para evitar que otra persona lo utilice. _(Nueva — AG-01)_ | Alta | F |

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
| HU-37 | Usuario | **[FUERA DEL PRIMER ENTREGABLE — D-034]** Como usuario, quiero recibir asistencia de un administrador durante mi registro biométrico cuando necesite ayuda para completar el proceso. _(Reasignada de "personal autorizado" — D-041)_ | Alta | F |

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
| HU-122 | Usuario | Como usuario, quiero recibir un tutorial inicial guiado por voz al completar mi registro para aprender las principales acciones y la forma de interactuar con Nayra. _(Nueva — AG-01)_ | Alta | F |

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
| HU-123 | Usuario | Como usuario, quiero generar y mostrar el código QR de mi cuenta financiera para que otro usuario de Nayra pueda identificarme como destinatario de una transferencia. _(Nueva — AG-01)_ | Alta | F |
| HU-124 | Usuario | Como usuario, quiero escanear un código QR para identificar al destinatario de una transferencia y confirmar por voz los datos de la operación antes de ejecutarla. _(Nueva — AG-01)_ | Alta | F |

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
| HU-94 | Administrador | Como administrador, quiero consultar las acciones realizadas por los administradores para supervisar y verificar el uso adecuado de sus permisos. _(Consolida HU-108 — AG-00; reformulada por D-041)_ | Alta | F |
| HU-95 | Administrador | Como administrador, quiero consultar estadísticas de los eventos registrados para apoyar el análisis de seguridad de la plataforma. | Media | F |

### EP-09 Gestión administrativa y atención al usuario

| ID | Actor | Historia de usuario | Prioridad | Tipo |
|---|---|---|---|---|
| HU-96 | Personal de atención | **[SIN OBJETO — D-041: cubierta por HU-97]** Como personal de atención, quiero iniciar sesión en el panel de atención para acceder a las funcionalidades que corresponden a mi rol. | Alta | F |
| HU-97 | Administrador | Como administrador, quiero iniciar sesión en el panel administrativo para gestionar y supervisar la plataforma. | Alta | F |
| HU-98 | Personal de atención | **[SIN OBJETO — D-041: cubierta por HU-21 y HU-22]** Como personal de atención, quiero buscar y consultar los datos básicos de un usuario para brindarle asistencia. | Alta | F |
| HU-99 | Administrador | **[INCLUIDA — D-052: registro asistido por un representante autorizado (administrador u otra persona autorizada); sin nuevo rol]** Como administrador, quiero iniciar y gestionar un registro asistido para ayudar a un usuario a crear su cuenta de acceso. _(Reasignada de Personal de atención — D-041)_ | Alta | F |
| HU-100 | Personal de atención | **[SIN OBJETO — D-041: cubierta por HU-23]** Como personal de atención, quiero corregir determinados datos personales de un usuario cuando este solicite asistencia para mantener su información actualizada. | Alta | F |
| HU-101 | Administrador | Como administrador, quiero consultar el estado de una cuenta de acceso para conocer si se encuentra activa, bloqueada, suspendida o desactivada. _(Reasignada de Personal de atención — D-041; los estados definitivos quedan pendientes)_ | Alta | F |
| HU-102 | Administrador | Como administrador, quiero registrar, consultar y gestionar las solicitudes de atención o incidencias reportadas por los usuarios, incluidas las solicitudes de recuperación, para darles seguimiento. _(Reasignada de Personal de atención — D-041; ampliada por D-040)_ | Media | F |
| HU-104 | Administrador | Como administrador, quiero consultar y modificar determinados datos de una cuenta para corregir información registrada incorrectamente. | Alta | F |
| HU-125 | Administrador | Como administrador, quiero consultar el dispositivo vinculado a una cuenta de acceso y revocarlo cuando corresponda para atender pérdidas o cambios de dispositivo. _(Nueva — AG-01)_ | Alta | F |

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
>
> **AG-01:** las filas HU-117 a HU-125 no proceden del Excel; corresponden a las HUs nuevas de AG-01 y su tecnología figura como "Por definir" hasta cerrar las decisiones pendientes (D-007, D-045, D-048). Las filas de HUs reasignadas o marcadas no se modificaron.

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
| HU-117 | Registro de usuario / contacto | Por definir (D-007) + Java/Spring Boot | PostgreSQL |
| HU-118 | Registro de usuario / credenciales | Por definir (D-007) + Java/Spring Boot | PostgreSQL (solo hash; D-047) |
| HU-119 | Vinculación de dispositivo | Por definir (D-007, D-048) + Java/Spring Boot | PostgreSQL |
| HU-120 | Cambio de dispositivo | Por definir (D-007) + Java/Spring Boot + Python | PostgreSQL + verificación biométrica 1:1 |
| HU-121 | Pérdida de dispositivo | Por definir (D-007) + Java/Spring Boot | PostgreSQL |
| HU-122 | Tutorial inicial | Por definir (D-007) | — |
| HU-123 | QR de cuenta financiera | Por definir (D-007) + Java/Spring Boot | PostgreSQL |
| HU-124 | Transferencia mediante QR | Por definir (D-007) + Java/Spring Boot | PostgreSQL |
| HU-125 | Administración de dispositivos | Por definir (D-045) + Java/Spring Boot | PostgreSQL |

## 6. Modelo CORE registrado

La hoja `TABLAS CORE` contiene una propuesta inicial de estructuras de datos. Estas estructuras deben validarse posteriormente en `03_BASE_DE_DATOS_NAYRA.md` antes de generar migraciones o código definitivo.

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

### REGISTRO_IDENTIDAD_SIMULADO _(AG-01)_

Fuente de identidad simulada del entorno controlado (D-035). Relaciona DNI, nombres y apellidos; se consulta durante el registro. No es una API real.

| Atributo | Tipo | Descripción |
|---|---|---|
| PK | Identificador |  |
| Texto, UNIQUE | DNI |  |
| Texto | Nombres |  |
| Texto | Apellidos |  |

> **AG-01:** el modelo actualizado de `USUARIOS`, `ROLES`, `CUENTAS`, `DISPOSITIVOS`, `SESIONES`, `OPERACIONES`, `SOLICITUDES_ATENCION` y `AUDITORÍA` según D-035 a D-043 se documenta en `03_BASE_DE_DATOS_NAYRA.md` §16, que es la referencia vigente del modelo de datos. Las tablas de esta sección conservan la propuesta del Excel.

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
| Qué debe hacer el sistema | `01_REQUISITOS_NAYRA.md` |
| Arquitectura | `02_ARQUITECTURA_NAYRA.md` |
| Modelo de datos definitivo | `03_BASE_DE_DATOS_NAYRA.md` |
| Contratos de API | `04_API.md` |
| Biometría y spoofing | `05_BIOMETRIA_NAYRA.md` |
| Seguridad | `06_SEGURIDAD_NAYRA.md` |
| Decisiones tecnológicas | `07_DECISIONES_TECNICAS_NAYRA.md` |
| Estado real de implementación | `08_ESTADO_PROYECTO.md` |
| Reglas de código | `09_REGLAS_DESARROLLO_NAYRA.md` |

## 10. Nota sobre decisiones arquitectónicas anteriores

Las propuestas arquitectónicas realizadas en etapas anteriores y posteriormente descartadas por el equipo/profesorado **no forman parte de los requisitos vigentes**. Este documento no debe utilizarse para reconstruir esas arquitecturas. La arquitectura vigente se definirá exclusivamente en `02_ARQUITECTURA_NAYRA.md`.

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
- **Registro con voz (D-032):** el registro de Nayra incluye el registro de la voz del usuario (HU-26 a HU-33) para su uso posterior en la autenticación biométrica. HU-01 y HU-26 se mantienen como HUs independientes en sus épicas. El mecanismo exacto de identificación durante el registro quedó pendiente para AG-01 y fue **resuelto por D-035, D-036 y D-052** (registro asistido: un representante autorizado valida el DNI y la identidad con apoyo del registro de identidad simulado; ver sección 12).
- **Exclusiones (D-033):** no se incorporan funcionalidades de apertura de cuentas bancarias, múltiples cuentas por usuario, transferencias entre cuentas propias ni gestión de entidades bancarias por parte del administrador.

### 11.5 Pendientes derivados de AG-00

- Relación exacta del término "billetera" con la cuenta financiera.
- Interpretación de "cuenta" en HU-93 (modificaciones relevantes), HU-104 (D1) y HU-110.
- Casos del registro por DNI: DNI inexistente en el registro de identidad simulado, DNI sin cuenta financiera simulada, documentos distintos del DNI (p. ej. CE), corrección posterior del DNI, y si el administrador tiene cuenta financiera. _(AG-01: el DNI ya registrado deriva a recuperación/cambio de dispositivo, D-036; el personal de atención dejó de existir, D-041.)_
- ~~Dato del entorno simulado con el que se localiza la cuenta por DNI~~ — **resuelto por D-035** (registro de identidad simulado).
- Destino de las operaciones de tipo PAGO (HU-73) — _los pagos no forman parte del primer entregable_ (sección 12.4) — y regla de moneda en transferencias.
- Estado CANCELADA de operaciones (HU-87) y estados de la cuenta de acceso frente a los de la cuenta financiera (AG-05).
- Nombre de EP-01 y código de épica para "Monitoreo y métricas".
- Renumeración de HUs (aplazada).

## 12. Registro, autenticación, dispositivo y alcance del primer entregable — AG-01 (2026-09-26)

Decisiones de referencia: `07_DECISIONES_TECNICAS_NAYRA.md`, D-034 a D-044. Las HUs **no se renumeraron**. Resultado: 105 HUs en las tablas (96 de AG-00 + 9 nuevas), de las cuales 4 quedan marcadas **[SIN OBJETO]** y 2 **[FUERA DEL PRIMER ENTREGABLE]** por D-041/D-034. _(AG-01 v5: HU-99 pasa a incluida por D-052.)_

### 12.1 Aclaraciones de HUs existentes

Estas aclaraciones precisan cómo se interpretan las HUs bajo las decisiones de AG-01, sin cambiar su redacción:

| HU | Aclaración | Decisión |
|---|---|---|
| HU-01 | El registro es **asistido** (D-052): la persona solicita registrarse → un representante autorizado (administrador u otra persona autorizada) la asiste → se proporciona el DNI → el sistema consulta el registro de identidad simulado y muestra los datos → el representante valida la identidad → la persona confirma sus datos → celular (HU-117) → contraseña (HU-118) → vinculación del dispositivo (HU-119) → enrolamiento de voz con anti-spoofing → tutorial (HU-122) → fin del registro. Si el DNI ya tiene cuenta de acceso, no se crea otra y se deriva a recuperación/cambio de dispositivo. | D-052, D-053 |
| HU-02 | Los datos de identidad (nombres, apellidos) se obtienen del registro de identidad simulado a partir del DNI; el celular y la contraseña se cubren en HU-117 y HU-118. | D-035, D-043 |
| HU-03 | La verificación de identidad la realiza un **representante autorizado** (D-052), apoyado en los datos del registro de identidad simulado. La consulta del DNI por sí sola **no** prueba la titularidad ni la identidad. | D-035, D-052 |
| HU-04 | La confirmación de la cuenta de acceso se entrega después de la vinculación del dispositivo y del enrolamiento de voz. | D-052 |
| HU-12 | El acceso requiere: comando "Iniciar sesión Nayra" → dispositivo vinculado → contraseña → desafío de voz variable → comprobación del contenido → anti-spoofing → verificación 1:1. El DNI **no** forma parte del inicio de sesión habitual (D-053). | D-037, D-039, D-053 |
| HU-14 | Con un único dispositivo activo por cuenta, la HU se refiere a la sesión y al dispositivo vinculado de la cuenta; su reformulación queda pendiente. | D-039 |
| HU-18 | La recuperación cubre pérdida del celular, cambio de celular, cuenta bloqueada y problemas de autenticación. El procedimiento asistido queda pendiente (D-049). | D-040 |
| HU-19 | Además del bloqueo manual por el administrador, la cuenta de acceso se bloquea automáticamente tras 3 intentos fallidos y puede bloquearse por pérdida del celular. | D-040, D-044 |
| HU-26, HU-29 | El enrolamiento se realiza durante el registro, después de confirmar la identidad, con anti-spoofing durante el enrolamiento. | D-036, D-038 |
| HU-35 | Un nuevo enrolamiento solo se permite después de validar al titular (nunca DNI → cuenta → nueva voz). | D-040 |
| HU-40 | "Iniciar sesión Nayra" es solo un comando de activación; la verificación biométrica es 1:1 contra la referencia de la cuenta determinada por el dispositivo y se realiza después de validar la contraseña. | D-037 |
| HU-43 | La frase de desafío es **variable** en cada autenticación y su contenido se comprueba; su respuesta es la muestra biométrica. | D-037 |
| HU-44, HU-47 | Aplica la política de 3 intentos; los detalles (qué cuenta como intento) quedan pendientes. | D-044 |
| HU-48 | El anti-spoofing se aplica en el enrolamiento y en la autenticación. | D-036, D-037 |
| HU-69 | El destinatario es otro usuario registrado en Nayra (seleccionado/buscado o identificado por QR, HU-124). El dato concreto de búsqueda queda pendiente. | D-042 |

### 12.2 HUs nuevas

| HU | Épica | Resumen | Decisión |
|---|---|---|---|
| HU-117 | EP-01 | Registrar el número de celular | D-043 |
| HU-118 | EP-01 | Crear contraseña de acceso (dictable) | D-037 |
| HU-119 | EP-01 | Vincular el dispositivo a la cuenta de acceso | D-039 |
| HU-120 | EP-01 | Cambiar de celular validando contraseña + voz | D-040 |
| HU-121 | EP-01 | Solicitar bloqueo por pérdida del celular | D-040 |
| HU-122 | EP-05 | Tutorial inicial guiado por voz | D-036 |
| HU-123 | EP-06 | Generar/mostrar el QR de la propia cuenta financiera | D-042 |
| HU-124 | EP-06 | Transferir escaneando un QR, con confirmación por voz | D-042 |
| HU-125 | EP-09 | Consultar/revocar el dispositivo vinculado (administrador) | D-039, D-040, D-041 |

La prioridad **Alta** de las HUs nuevas se asignó por su inclusión en el primer entregable y queda sujeta a confirmación del equipo. No se creó HU para el contacto de confianza, el chatbot, el OTP, el retiro asistido ni la eliminación de cuenta (fuera del primer entregable, D-034).

### 12.3 HUs del antiguo actor "Personal de atención" (D-041)

| HU | Texto original | Resultado | Motivo |
|---|---|---|---|
| HU-24 | Como administrador, quiero gestionar las cuentas de acceso del personal de atención para controlar quién puede acceder al panel de atención y realizar funciones autorizadas dentro de la plataforma. | **SIN OBJETO** | No existe rol de personal de atención |
| HU-25 | Como administrador, quiero asignar roles y permisos al personal autorizado para controlar y limitar las funcionalidades disponibles según sus responsabilidades. | **FUERA DEL PRIMER ENTREGABLE** | Roles fijos USER/ADMIN; su reformulación queda pendiente |
| HU-37 | Como usuario, quiero recibir asistencia de personal autorizado durante mi registro biométrico cuando necesite ayuda para completar el proceso. | Reasignada al administrador; **FUERA DEL PRIMER ENTREGABLE** | El registro del prototipo es autónomo por voz |
| HU-94 | Como administrador, quiero consultar las acciones realizadas por el personal autorizado, incluido el personal de atención, para supervisar y verificar el uso adecuado de sus permisos. | Reformulada: acciones de los administradores | Auditoría de acciones administrativas (D-041) |
| HU-96 | Como personal de atención, quiero iniciar sesión en el panel de atención para acceder a las funcionalidades que corresponden a mi rol. | **SIN OBJETO** | Cubierta por HU-97 |
| HU-98 | Como personal de atención, quiero buscar y consultar los datos básicos de un usuario para brindarle asistencia. | **SIN OBJETO** | Cubierta por HU-21 y HU-22 |
| HU-99 | Como personal de atención, quiero iniciar y gestionar un registro asistido para ayudar a un usuario a crear su cuenta. | Reasignada; **INCLUIDA** por D-052 (AG-01 v5) | El registro es asistido por un representante autorizado; que puede ser un administrador u otra persona autorizada; sin crear un nuevo rol. Registro técnico y auditoría de quién validó: pendiente |
| HU-100 | Como personal de atención, quiero corregir determinados datos personales de un usuario cuando este solicite asistencia para mantener su información actualizada. | **SIN OBJETO** | Cubierta por HU-23 |
| HU-101 | Como personal de atención, quiero consultar el estado de una cuenta para conocer si se encuentra activa, bloqueada, suspendida o desactivada. | Reasignada al administrador | Consulta de estado en el panel (D-041) |
| HU-102 | Como personal de atención, quiero registrar y consultar las solicitudes o incidencias reportadas por los usuarios para darles seguimiento. | Reasignada al administrador y ampliada a solicitudes de recuperación | Gestión de solicitudes en el panel (D-040, D-041) |

### 12.4 Alcance del primer entregable (D-034)

Clasificación según D-034 y el diagnóstico AG-01 aprobado. Las HUs **secundarias** siguen vigentes pero no bloquean la entrega; las **no incluidas** siguen vigentes para etapas posteriores salvo que se indique lo contrario.

| Grupo | HUs |
|---|---|
| **Incluidas — registro y acceso** | HU-01, HU-02, HU-03, HU-04, HU-99 (registro asistido, D-052), HU-12, HU-13, HU-18, HU-19, HU-117, HU-118, HU-119, HU-120, HU-121 |
| **Incluidas — biometría y anti-spoofing** | HU-26, HU-27, HU-28, HU-29, HU-30, HU-31, HU-32, HU-33, HU-40, HU-41, HU-42, HU-43, HU-44, HU-45, HU-47, HU-48, HU-49, HU-50 |
| **Incluidas — accesibilidad** | HU-53, HU-54, HU-55, HU-56, HU-57, HU-58, HU-59, HU-60, HU-61, HU-62, HU-64, HU-65, HU-122 |
| **Incluidas — operaciones simuladas** | HU-66, HU-67, HU-69, HU-70, HU-71, HU-72 |
| **Incluidas — panel del administrador** | HU-97, HU-20, HU-21, HU-22, HU-101, HU-102, HU-125, HU-80, HU-89, HU-93, HU-94; métricas básicas: HU-109, HU-110 |
| **Secundarias** | HU-09, HU-10, HU-14, HU-15, HU-34, HU-35, HU-36, HU-46, HU-51, HU-52, HU-68, HU-76, HU-77, HU-78, HU-79, HU-81, HU-83, HU-87, HU-88, HU-90, HU-111, HU-113 |
| **Secundarias — siguiente entregable** (documentadas, fuera de la implementación actual; AG-01 v5) | HU-123, HU-124 (QR) |
| **No incluidas en el primer entregable** | HU-16, HU-17 (desactivación/reactivación, por confirmar), HU-23, HU-73, HU-74, HU-75 (pagos), HU-95, HU-104 (D1 pendiente), HU-114, HU-115, HU-116 (métricas de validación, Objetivo 3) |
| **Fuera del primer entregable (marcadas)** | HU-25, HU-37 |
| **Sin objeto (marcadas)** | HU-24, HU-96, HU-98, HU-100 |

Funcionalidades **fuera del primer entregable sin HU**: retiro asistido por administrador, eliminación de cuenta, chatbot, OTP/SMS/WhatsApp, contacto de confianza (posible evolución futura), agenda del teléfono, múltiples dispositivos activos.

### 12.5 Pendientes funcionales derivados de AG-01

- Dato con el que el emisor busca/selecciona al destinatario de una transferencia (HU-69).
- Identificador que codifica el QR (ver `03_BASE_DE_DATOS_NAYRA.md` §16) — no bloquea el primer entregable (QR en siguiente entregable).
- Estados de la cuenta de acceso (al menos ACTIVA y BLOQUEADA por D-044/D-040; "registro incompleto", "suspendida", "desactivada" pendientes).
- Detalle de la política de 3 intentos (D-044) y procedimiento de recuperación asistida (D-049).
- Reformulación de HU-14 y HU-25.
- Confirmación de la prioridad de las HUs nuevas y de la clasificación de alcance de la sección 12.4.
- Canal por el que un usuario que perdió su celular solicita asistencia (HU-121), sin chatbot.
- Registro asistido (D-052): cómo se registra técnicamente quién realizó la validación asistida (sobre todo si el representante no es `ADMIN`) y cómo se refleja en la auditoría. _(Resueltos en AG-01 v6: momento del celular y posición del DNI.)_

## 13. Interpretación de HUs por las decisiones de AG-02 (2026-09-27)

Decisiones de referencia: `07_DECISIONES_TECNICAS_NAYRA.md`, D-007, D-018, D-048, D-054 a D-061. No se agregan HUs ni se cambia su redacción.

| HU | Interpretación | Decisión |
|---|---|---|
| HU-118 | La "contraseña de acceso" se concreta como **PIN de 6 dígitos**, ingresado con un teclado accesible que anuncia solo el avance. **Pendiente:** si además puede dictarse por voz, como menciona la HU (dictarlo en voz alta lo expone a terceros) | D-061, D-046 |
| HU-12, HU-40 | El acceso sigue el flujo de D-037 con PIN en lugar de contraseña; el dispositivo vinculado se comprueba mediante la firma de un nonce con la clave del dispositivo | D-037, D-048, D-061 |
| HU-13 | Además del cierre manual, la sesión se cierra automáticamente tras 5 minutos de inactividad | D-018 |
| HU-119 | La vinculación registra la clave pública del par de claves generado en el teléfono | D-048 |
| HU-120 | En el cambio de celular, el nuevo dispositivo genera su propio par de claves; se valida con PIN + voz 1:1 + anti-spoofing y se revoca la clave del dispositivo anterior | D-040, D-048, D-061 |
| HU-29, HU-43 | La frase de desafío tiene la estructura palabra + 3 dígitos + palabra, de un solo uso; su contenido se comprueba en el servidor | D-054, D-046 |
| HU-31, HU-32 | La calidad de la grabación se evalúa en el servicio de voz (voz neta, ruido, saturación) y se informa con un motivo accesible | D-058 |
| HU-26, HU-48 | El enrolamiento aplica calidad, contenido y anti-spoofing a cada muestra; se guarda solo un embedding cifrado | D-012, D-013, D-059 |
| HU-36 | La eliminación borra físicamente la referencia biométrica; no existe audio que eliminar | D-013 |

**Clasificación técnica (sección 5):** las filas de HU-117 a HU-124 que indican "Por definir (D-007)" deben leerse como **Flutter** tras AG-02; las referencias del Excel a "almacenamiento de audio" (HU-26, HU-28, HU-30, HU-35, HU-36) **no** aplican, porque D-013 prohíbe guardar audio. La clasificación del Excel se conserva sin modificar como referencia del archivo fuente.
