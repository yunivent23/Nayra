# 06_SEGURIDAD_NAYRA.md — Seguridad de la información de Nayra

## 1. Propósito

Este documento establece los principios y controles de seguridad que deberán considerarse durante el diseño, desarrollo, integración y despliegue de Nayra.

La seguridad es un requisito transversal debido a que el sistema contempla:

- autenticación de usuarios;
- información personal;
- sesiones;
- dispositivos;
- operaciones;
- información administrativa;
- registros de auditoría;
- procesamiento biométrico de voz;
- comunicación entre componentes.

Este documento define criterios de seguridad, pero **no reemplaza las decisiones técnicas específicas** que deberán registrarse en `07_DECISIONES_TECNICAS_NAYRA.md`.

---

# 2. Objetivos de seguridad

La solución debe procurar proteger:

- confidencialidad;
- integridad;
- disponibilidad;
- autenticidad;
- trazabilidad;
- privacidad de la información.

Los controles concretos deberán ser proporcionales al alcance de la tesis y a los riesgos identificados.

---

# 3. Principios de seguridad

## 3.1 Mínimo privilegio

Cada usuario, servicio y componente debe tener únicamente los permisos necesarios para realizar sus funciones.

No se deben utilizar cuentas con privilegios administrativos para operaciones normales.

## 3.2 Defensa en profundidad

La seguridad no debe depender de un único mecanismo.

Debe existir protección en diferentes niveles:

```text
Cliente
   ↓
Comunicación
   ↓
API
   ↓
Autenticación / autorización
   ↓
Lógica de negocio
   ↓
Datos
```

## 3.3 Seguridad por diseño

Los controles de seguridad deben considerarse desde el diseño y no agregarse únicamente al final de la implementación.

## 3.4 No confiar en el cliente

La aplicación móvil no debe considerarse una fuente confiable para decisiones críticas.

Las validaciones importantes deben realizarse en el backend.

## 3.5 No almacenar secretos en el código

Contraseñas, claves, tokens, credenciales de servicios y otros secretos no deben estar escritos directamente en el código fuente.

---

# 4. Autenticación

La autenticación debe permitir determinar si el usuario que intenta acceder corresponde a una identidad registrada.

Nayra contempla la autenticación mediante voz como una funcionalidad central.

La autenticación biométrica debe integrarse con el mecanismo general de autenticación definido para el sistema.

El mecanismo definitivo de autenticación deberá establecerse en `07_DECISIONES_TECNICAS_NAYRA.md`.

No asumir automáticamente:

- JWT;
- OAuth;
- sesiones tradicionales;
- refresh tokens;
- autenticación multifactor;

hasta que la decisión correspondiente haya sido aprobada.

**Actualización AG-01 (D-037):** la autenticación del usuario queda **aprobada funcionalmente** como contraseña + verificación biométrica de voz 1:1 + anti-spoofing (dos factores: algo que el usuario sabe y algo que es). JWT, OAuth, sesiones tradicionales, refresh tokens y cualquier mecanismo técnico concreto siguen **sin asumirse** (D-018). La autenticación del administrador sigue pendiente (D-050). Ver §36.

---

# 5. Autorización

La autenticación y la autorización son conceptos diferentes.

```text
Autenticación
¿Quién eres?
       ↓
Autorización
¿Qué puedes hacer?
```

El sistema debe impedir que un usuario ejecute operaciones que no correspondan a sus permisos.

Los permisos deben estar relacionados con los roles y requisitos definidos en `01_REQUISITOS_NAYRA.md`.

---

# 6. Roles y control de acceso

El modelo de datos contempla una estructura de roles.

El control de acceso deberá considerar:

- identidad del usuario;
- rol;
- permisos;
- operación solicitada;
- estado de la sesión;
- contexto de seguridad cuando corresponda.

El backend debe validar los permisos.

No confiar únicamente en restricciones implementadas en el frontend.

---

# 7. Protección de credenciales

Las credenciales de los usuarios deben protegerse adecuadamente.

No almacenar contraseñas en texto plano.

Cuando se utilicen contraseñas, deberán almacenarse mediante un mecanismo de hash seguro y apropiado para credenciales.

Las credenciales de servicios deben mantenerse separadas del código fuente.

---

# 8. Gestión de sesiones

Las sesiones deben gestionarse de manera segura.

El sistema deberá considerar:

- expiración;
- invalidación;
- protección contra reutilización indebida;
- control de sesiones activas;
- protección de tokens;
- cierre de sesión.

La estrategia concreta de sesión deberá definirse en `07_DECISIONES_TECNICAS_NAYRA.md`.

---

# 9. Protección de APIs

Las APIs deberán aplicar controles de seguridad apropiados.

Como mínimo deberán considerarse:

- autenticación;
- autorización;
- validación de entradas;
- control de acceso;
- manejo seguro de errores;
- protección de información sensible;
- limitación de intentos cuando corresponda.

No se deben exponer endpoints administrativos o sensibles sin controles de autorización.

---

# 10. Validación de entradas

Todo dato recibido desde el cliente debe considerarse no confiable.

El backend debe validar:

- tipos;
- formatos;
- longitud;
- valores permitidos;
- relaciones entre campos;
- permisos;
- estado de la operación.

La validación debe realizarse también en el backend aunque exista validación en el frontend.

---

# 11. Protección contra inyección

La implementación deberá prevenir ataques de inyección, incluyendo aquellos relacionados con consultas a la base de datos.

Se debe evitar la construcción insegura de consultas mediante concatenación de entradas del usuario.

La implementación debe utilizar mecanismos seguros del framework y del motor de persistencia.

---

# 12. Protección de comunicaciones

La comunicación entre componentes debe protegerse frente a interceptación y modificación.

Cuando corresponda, se deberá utilizar:

- HTTPS;
- TLS;
- certificados válidos;
- canales seguros entre servicios.

No transmitir credenciales, tokens o información biométrica mediante canales sin protección.

El protocolo exacto de comunicación entre Java y Python deberá definirse en la arquitectura y en `07_DECISIONES_TECNICAS_NAYRA.md`.

---

# 13. Protección de información personal

El sistema puede procesar información personal de los usuarios.

Por ello se debe considerar:

- minimización de datos;
- control de acceso;
- protección durante la transmisión;
- protección durante el almacenamiento;
- eliminación cuando corresponda;
- prevención de exposición en logs.

No utilizar información personal real innecesariamente durante el desarrollo.

---

# 14. Seguridad de la biometría de voz

La biometría de voz requiere controles adicionales.

La solución debe considerar:

- protección de las muestras de voz;
- protección de representaciones biométricas;
- acceso restringido;
- protección durante transmisión;
- protección durante almacenamiento;
- minimización;
- retención limitada;
- auditoría.

La estrategia de almacenamiento biométrico debe definirse en `05_BIOMETRIA_NAYRA.md`.

No asumir que el audio original debe conservarse.

---

# 15. Anti-spoofing

El mecanismo anti-spoofing constituye un control de seguridad del proceso de autenticación por voz.

Debe considerar amenazas como:

- reproducción de grabaciones;
- voces sintéticas;
- voces manipuladas;
- otras entradas fraudulentas.

El mecanismo concreto deberá definirse en `05_BIOMETRIA_NAYRA.md` y `07_DECISIONES_TECNICAS_NAYRA.md`.

No afirmar que una técnica detecta todos los ataques.

---

# 16. Protección frente a ataques de repetición

Cuando el sistema utilice tokens, sesiones o desafíos de autenticación, debe evitar que una solicitud válida pueda reutilizarse indebidamente.

La estrategia concreta dependerá del mecanismo de autenticación seleccionado.

No implementar mecanismos de replay protection sin documentar previamente su relación con el flujo de autenticación.

---

# 17. Control de intentos

Las funcionalidades de autenticación deben contemplar controles frente a intentos repetidos o automatizados.

Cuando corresponda se podrán considerar:

- límites de intentos;
- bloqueos temporales;
- rate limiting;
- detección de actividad anómala;
- registro de intentos.

Los valores concretos deben determinarse mediante una decisión técnica.

No colocar límites arbitrarios únicamente para completar el código.

**Actualización AG-01 (D-044):** el límite aprobado es **3 intentos fallidos**, tras los cuales se bloquea la cuenta de acceso. Qué resultados cuentan como intento (contraseña, voz no coincidente, spoofing, mala calidad) y si el contador es único o separado siguen pendientes. Los **errores técnicos del servicio no cuentan** como intentos del usuario salvo decisión expresa.

---

# 18. Seguridad de dispositivos y sesiones

El sistema contempla información relacionada con dispositivos y sesiones.

La implementación deberá considerar:

- identificación del dispositivo cuando corresponda;
- asociación segura con la cuenta;
- invalidación de sesiones;
- protección de tokens;
- cierre de sesiones comprometidas;
- control de dispositivos autorizados cuando sea requerido.

La estrategia definitiva debe alinearse con los requisitos y el modelo de datos.

---

# 19. Auditoría

La base de datos contempla una tabla de `AUDITORÍA`.

La auditoría debe permitir registrar eventos relevantes de seguridad y operaciones administrativas.

Los eventos que podrían requerir auditoría incluyen:

- autenticaciones;
- fallos de autenticación;
- cambios de información;
- operaciones administrativas;
- cambios de permisos;
- eventos relacionados con seguridad;
- acciones sensibles.

No almacenar información biométrica completa en logs de auditoría.

Los eventos y campos definitivos deberán definirse en `03_BASE_DE_DATOS_NAYRA.md`.

---

# 20. Logs y monitoreo

Los logs deben servir para detectar errores y eventos de seguridad sin convertirse en una fuente de exposición de información sensible.

No registrar:

- contraseñas;
- tokens completos;
- claves secretas;
- muestras de voz;
- representaciones biométricas;
- información personal innecesaria.

Cuando sea necesario registrar un identificador, utilizar una representación apropiada que permita la trazabilidad sin exponer información sensible.

---

# 21. Manejo de errores

Los errores internos no deben exponerse directamente al usuario.

Por ejemplo, evitar respuestas que revelen:

- stack traces;
- nombres internos de tablas;
- rutas internas;
- credenciales;
- configuración del servidor;
- detalles innecesarios de la infraestructura.

El usuario debe recibir mensajes comprensibles y seguros.

---

# 22. Seguridad de la base de datos

La base de datos debe protegerse mediante:

- cuentas con privilegios mínimos;
- credenciales seguras;
- conexiones protegidas;
- restricciones de acceso;
- copias de seguridad;
- control de cambios;
- separación entre ambientes cuando corresponda.

La aplicación no debe utilizar una cuenta de base de datos con permisos administrativos completos si no es necesario.

---

# 23. Gestión de secretos

Los secretos deberán mantenerse fuera del repositorio de código.

Ejemplos:

- credenciales de PostgreSQL;
- claves API;
- secretos JWT;
- credenciales cloud;
- certificados privados;
- tokens de servicios.

Durante desarrollo se podrán utilizar variables de entorno o mecanismos de gestión de secretos apropiados.

Los archivos que contengan secretos no deben subirse a GitHub.

---

# 24. Configuración por ambiente

La configuración debe permitir separar, cuando corresponda:

```text
Desarrollo
    ↓
Pruebas
    ↓
Producción
```

No reutilizar automáticamente credenciales o datos reales en ambientes de desarrollo.

La estrategia exacta de ambientes será definida posteriormente.

---

# 25. Dependencias de terceros

Las dependencias utilizadas por el proyecto deben revisarse antes de incorporarlas.

Se debe considerar:

- versión;
- compatibilidad;
- mantenimiento;
- vulnerabilidades conocidas;
- licencia;
- necesidad real.

No agregar una dependencia únicamente porque facilite una pequeña parte del desarrollo si puede introducir riesgos o complejidad innecesaria.

---

# 26. Seguridad del código

El código debe seguir principios de desarrollo seguro.

Se debe evitar:

- secretos hardcodeados;
- consultas inseguras;
- deserialización insegura;
- validaciones insuficientes;
- exposición innecesaria de datos;
- permisos excesivos;
- manejo inseguro de archivos;
- mensajes de error demasiado detallados.

Las decisiones concretas de seguridad deben mantenerse documentadas.

---

# 27. Seguridad del componente Python

El componente Python para procesamiento biométrico debe considerarse un componente interno del sistema y no una fuente de confianza automática.

Debe:

- validar entradas;
- limitar el tipo de información recibida;
- evitar exposición innecesaria;
- manejar errores;
- proteger dependencias;
- evitar almacenar muestras sin necesidad;
- devolver únicamente la información necesaria al backend principal.

---

# 28. Seguridad del frontend

La aplicación móvil no debe almacenar información sensible innecesariamente.

Debe evitarse:

- almacenar contraseñas en texto plano;
- almacenar tokens de forma insegura;
- registrar información biométrica;
- exponer datos sensibles en notificaciones;
- confiar exclusivamente en validaciones del cliente.

Las operaciones críticas deben ser validadas por el backend.

---

# 29. Seguridad de operaciones

Las operaciones relacionadas con billetera deben estar protegidas mediante:

- autenticación;
- autorización;
- validación de reglas de negocio;
- validación de datos;
- control de sesión;
- trazabilidad.

La biometría no debe considerarse por sí sola suficiente para ejecutar una operación sin las validaciones adicionales que establezca el sistema.

---

# 30. Disponibilidad y recuperación

La disponibilidad debe considerarse como parte de la seguridad, especialmente para los componentes necesarios para autenticación.

La estrategia concreta de:

- respaldo;
- recuperación;
- disponibilidad;
- redundancia;
- monitoreo;

deberá definirse según la arquitectura aprobada y el alcance de la tesis.

No asumir una infraestructura de alta disponibilidad que todavía no haya sido aprobada.

---

# 31. Consideraciones de privacidad

La solución debe aplicar principios de:

- minimización;
- finalidad;
- control de acceso;
- protección;
- retención limitada;
- eliminación cuando corresponda.

Las obligaciones legales específicas aplicables al tratamiento de datos personales y biométricos deberán ser revisadas y documentadas en la sección académica/legal correspondiente.

---

# 32. Trazabilidad de seguridad

La seguridad debe relacionarse con los requisitos:

```text
Requisito
   ↓
Riesgo
   ↓
Control de seguridad
   ↓
Componente
   ↓
Implementación
   ↓
Prueba
```

Ejemplo conceptual:

```text
Autenticación por voz
       ↓
Riesgo de spoofing
       ↓
Anti-spoofing
       ↓
Componente biométrico
       ↓
Implementación Python
       ↓
Prueba de ataques simulados
```

---

# 33. Reglas para Claude

Claude deberá:

1. no inventar mecanismos de seguridad como si fueran decisiones aprobadas;
2. no asumir JWT, OAuth u otro mecanismo sin aprobación;
3. no almacenar secretos en el código;
4. no registrar contraseñas, tokens completos o biometría;
5. no crear tablas de seguridad adicionales sin justificación;
6. no confiar exclusivamente en el frontend;
7. validar las entradas en el backend;
8. aplicar mínimo privilegio;
9. mantener trazabilidad de eventos relevantes;
10. mantener separadas las responsabilidades entre Java y Python;
11. no modificar la arquitectura de seguridad sin documentar el cambio;
12. señalar cualquier riesgo relevante detectado durante la implementación;
13. no presentar una medida de seguridad como implementada si solamente está documentada;
14. no presentar resultados de seguridad no medidos como resultados reales.

---

# 34. Decisiones pendientes

Antes de considerar la seguridad como completamente especificada deberán definirse:

- mecanismo técnico de autenticación (el flujo funcional del usuario está aprobado en D-037; pendiente la autenticación del administrador, D-050);
- mecanismo técnico de autorización (roles aprobados en D-041);
- estrategia de sesiones;
- uso o no de JWT;
- expiración de tokens;
- refresh tokens, si corresponden;
- algoritmo de hash de contraseñas, política y normalización de la contraseña dictada (D-047; la existencia de contraseñas está aprobada en D-037);
- vinculación técnica del dispositivo (D-048);
- procedimiento de recuperación asistida (D-049);
- estrategia de almacenamiento de secretos (deuda técnica, D-017; no bloquea el primer entregable);
- protocolo de comunicación Java-Python;
- configuración TLS;
- rate limiting;
- detalle de la política de intentos (el número máximo, 3, está aprobado en D-044);
- estrategia de desbloqueo;
- estrategia de auditoría;
- retención de logs;
- estrategia de copias de seguridad;
- estrategia de recuperación;
- protección de información biométrica;
- almacenamiento o no de audio;
- gestión técnica de dispositivos (la regla de un único dispositivo activo está aprobada en D-039);
- separación de ambientes.

Estas decisiones deberán registrarse en:

`07_DECISIONES_TECNICAS_NAYRA.md`

---

# 35. Regla principal

> **La seguridad debe estar integrada desde el diseño y no añadirse únicamente después de implementar el sistema.**

Ante una decisión de seguridad no definida:

```text
Identificar riesgo
      ↓
Analizar alternativas
      ↓
Seleccionar control
      ↓
Documentar decisión
      ↓
Implementar
      ↓
Probar
```


## Alcance de las operaciones financieras

Las operaciones financieras de Nayra se realizarán dentro de un **entorno simulado**. El sistema no tendrá, dentro del alcance actual, conexión directa con bancos reales ni procesará fondos reales.

Aun tratándose de operaciones simuladas, deberán aplicarse controles de seguridad sobre autenticación, autorización, integridad de datos, sesiones, auditoría y protección contra operaciones no autorizadas, debido a que el prototipo busca representar de forma realista el flujo de una billetera digital.

---

# 36. Controles de seguridad derivados de AG-01 (2026-09-26)

Decisiones de referencia: `07_DECISIONES_TECNICAS_NAYRA.md`, D-034 a D-044.

## 36.1 Contraseña

- Se crea en el registro y puede dictarse por voz (D-037).
- Se almacena **solo mediante hash seguro**; nunca en texto plano, nunca como audio, nunca en logs, ni siquiera en intentos fallidos.
- La contraseña **no identifica la cuenta**: la cuenta la determina el dispositivo vinculado. No se permite buscar cuentas a partir de la contraseña.
- **Pendiente (D-047):** algoritmo de hash, política compatible con el dictado y normalización de la contraseña dictada (mayúsculas, espacios, números, tildes), que debe ser idéntica en registro e inicio de sesión. La existencia de BCrypt en el código actual **no** lo convierte en algoritmo aprobado.
- Riesgo conocido: dictar la contraseña en voz alta puede exponerla a terceros cercanos; el segundo factor (voz) mitiga, pero no elimina, este riesgo.

## 36.2 Registro

- El DNI existente **no** permite crear una segunda cuenta; se deriva a recuperación/cambio de dispositivo (D-036).
- El rol **nunca** lo elige el cliente; los usuarios registrados desde la aplicación reciben el rol `USER`.
- La consulta de identidad usa un **registro simulado** (D-035): no se envían datos a terceros.
- **Registro asistido (D-052):** la consulta del DNI solo recupera datos y **no** prueba la titularidad ni la identidad. Un **representante autorizado** (administrador u otra persona autorizada) valida la identidad de la persona y luego la persona confirma sus datos. La biometría no se usa para validar la identidad en el registro. Es una regla de negocio: **no** se crea un rol del sistema para el representante (roles: `USER`, `ADMIN`).
- Pendiente: cómo se registra técnicamente quién realizó la validación asistida y cómo se audita, sin crear un rol nuevo.
- **Usos del DNI (D-053):** registro inicial, recuperación, cambio o pérdida del dispositivo y otros procesos excepcionales para localizar la cuenta. El DNI **no** forma parte del inicio de sesión habitual.

## 36.3 Dispositivo y sesiones

- **Un único dispositivo activo** por cuenta de acceso (D-039). Al autorizar uno nuevo, el anterior se revoca.
- Al bloquear la cuenta o revocar el dispositivo, se **revocan las sesiones** correspondientes (D-040).
- La reinstalación no se asume reconocible; si el vínculo no puede verificarse, se usa el flujo de cambio de dispositivo/recuperación.
- Mecanismo técnico de vinculación (D-048) y estrategia de sesiones (D-018) pendientes.

## 36.4 Recuperación y cambio de dispositivo

- **Prohibido:** `DNI → cuenta encontrada → registrar nueva voz → acceso` (riesgo de suplantación).
- Cambio de dispositivo normal: contraseña + verificación 1:1 contra la referencia biométrica existente + anti-spoofing (D-040).
- Pérdida del celular: solicitud → revisión por el administrador → bloqueo de cuenta/dispositivo → revocación de sesiones → auditoría.
- La recuperación asistida es el punto más expuesto a la ingeniería social; su procedimiento **no está definido (D-049)** y no debe improvisarse en la implementación.

## 36.5 Administrador

- Solo existen los roles `USER` y `ADMIN` (D-041).
- El administrador **no puede** modificar saldos ni operaciones financieras, ni usar privilegios administrativos para alterar información financiera. Tampoco accede a la referencia biométrica.
- Toda acción administrativa se audita: quién, cuándo, qué acción y sobre qué usuario.
- Autenticación del administrador pendiente (D-050).

## 36.6 QR

El QR **no** es un mecanismo de autenticación y no contiene contraseña, datos biométricos ni información sensible innecesaria; solo identifica la cuenta/destinatario dentro del entorno simulado (D-042). Antes de ejecutar una transferencia por QR se confirman por voz los datos de la operación. _(AG-01 v5: el QR queda fuera de la implementación del primer entregable; estas reglas aplican cuando se implemente.)_

## 36.7 Correcciones obligatorias antes de nuevas funcionalidades

El código actual del backend presenta problemas de seguridad identificados en la auditoría de AG-01. Antes de implementar funcionalidades nuevas deben corregirse:

1. contraseñas guardadas en texto plano;
2. rol elegido libremente por el cliente;
3. endpoints sin protección y exposición pública de datos sensibles (p. ej. listado público de usuarios);
4. ~~secretos y credenciales en el código/configuración, y revisión de las credenciales expuestas en el historial de Git (rotación);~~ **AG-01 v5: reclasificado como deuda técnica (D-017, categoría C).** No bloquea el primer entregable; ver §36.8;
5. revisión del mecanismo JWT existente antes de reutilizarlo (no está aprobado, D-018);
6. retiro del código heredado ajeno a Nayra.

El código existente **no** debe considerarse correcto solo porque existe.

## 36.8 Deuda técnica de seguridad: secretos y credenciales (D-017)

Las credenciales de PostgreSQL, las claves JWT y otros valores sensibles presentes hoy en la configuración y en el historial de Git constituyen **deuda técnica** reconocida (D-017, categoría C). En el primer entregable:

- **no** se eliminan automáticamente las credenciales actuales;
- **no** se limpia el historial de Git;
- **no** se rotan credenciales solo por esta revisión;
- **no** se detiene el desarrollo funcional por este motivo.

La regla de no introducir **nuevos** secretos en el código se mantiene (§3.5, §23). La corrección completa (variables de entorno, gestión de secretos, rotación, claves JWT, credenciales de base de datos, limpieza del historial si fuera necesaria y separación desarrollo/producción) se realizará **antes de cualquier despliegue en producción**, previsiblemente en el segundo entregable.
