# 02_ARQUITECTURA_NAYRA.md — Arquitectura del sistema Nayra

## 1. Propósito

Este documento define la arquitectura técnica vigente de Nayra.

**Estado actual:** la arquitectura definitiva se encuentra en proceso de definición y validación.

Por tanto, este archivo establece las reglas arquitectónicas que deberán respetarse, pero **no inventa ni reutiliza una arquitectura anterior que haya sido descartada**.

La arquitectura final deberá construirse a partir de:

1. los requisitos aprobados en `01_REQUISITOS_NAYRA.md`;
2. las decisiones técnicas aprobadas en `07_DECISIONES_TECNICAS_NAYRA.md`;
3. las restricciones de seguridad, biometría y accesibilidad;
4. las indicaciones académicas del proyecto.

---

# 2. Regla fundamental de arquitectura

> **Ninguna arquitectura propuesta anteriormente debe considerarse vigente si no aparece explícitamente aprobada en este documento.**

Las versiones anteriores de la arquitectura fueron propuestas durante etapas de diseño y algunas fueron descartadas o modificadas.

Por lo tanto, Claude no debe reutilizar automáticamente:

- diagramas anteriores;
- distribución anterior de servidores;
- DMZ;
- API Gateway;
- balanceadores;
- zonas de disponibilidad;
- ubicación de instancias;
- VPC;
- relaciones específicas entre componentes;
- servicios cloud concretos;
- flujos arquitectónicos anteriores.

Si una de estas tecnologías o elementos vuelve a ser necesario, debe justificarse y registrarse como una nueva decisión.

---

# 3. Objetivos arquitectónicos

La arquitectura deberá permitir:

- soportar la aplicación móvil;
- implementar las funcionalidades definidas en los requisitos;
- separar responsabilidades;
- permitir el procesamiento especializado de voz;
- integrar autenticación biométrica;
- incorporar mecanismos anti-spoofing;
- proteger información sensible;
- mantener trazabilidad;
- facilitar mantenimiento y evolución;
- permitir despliegue controlado;
- mantener una estructura comprensible para el contexto académico del proyecto.

La arquitectura debe ser proporcional al alcance de la tesis.

No se debe agregar complejidad únicamente por utilizar patrones o tecnologías de moda.

---

# 4. Capas conceptuales

La solución puede analizarse conceptualmente mediante las siguientes capas:

```text
Usuario
   ↓
Aplicación / Interfaz
   ↓
Servicios de aplicación
   ↓
Lógica de negocio
   ↓
Persistencia / procesamiento especializado
```

Esta representación es conceptual y **no constituye todavía un diagrama físico de despliegue**.

La arquitectura definitiva deberá especificar qué componentes concretos pertenecen a cada capa.

---

# 5. Componentes funcionales que la arquitectura debe soportar

A partir de los requisitos, la arquitectura deberá poder soportar funcionalidades relacionadas con:

- gestión de usuarios;
- autenticación;
- autorización;
- sesiones;
- dispositivos;
- operaciones;
- notificaciones;
- solicitudes de atención;
- auditoría;
- procesamiento de voz;
- autenticación biométrica;
- detección de spoofing;
- funcionalidades administrativas;
- métricas;
- accesibilidad.

La presencia de una funcionalidad en esta lista significa que la arquitectura debe poder soportarla, no que deba implementarse como un microservicio independiente.

---

# 6. Backend principal

El proyecto contempla un backend principal para gestionar la lógica de negocio de la aplicación.

La tecnología considerada actualmente es:

- Java;
- Spring Boot.

Las responsabilidades exactas del backend deberán definirse con base en los requisitos y en las decisiones técnicas aprobadas.

Conceptualmente, el backend será responsable de:

- recibir solicitudes de la aplicación;
- autenticar y autorizar solicitudes;
- validar reglas de negocio;
- gestionar usuarios;
- gestionar operaciones;
- gestionar información persistente;
- coordinar servicios especializados;
- aplicar controles de seguridad;
- generar respuestas para el cliente.

---

# 7. Procesamiento especializado de voz

El proyecto contempla un componente especializado para procesamiento de voz.

La tecnología considerada es:

- Python.

Este componente podrá encargarse de tareas como:

- procesamiento de audio;
- extracción de características;
- generación de representaciones biométricas;
- comparación/verificación;
- detección de spoofing;
- procesamiento mediante modelos preentrenados.

La distribución definitiva de responsabilidades entre Java y Python debe definirse antes de implementar la integración.

---

# 8. Relación conceptual entre backend y procesamiento de voz

El flujo conceptual esperado es:

```text
Aplicación móvil
       ↓
Backend principal
       ↓
Servicio de procesamiento de voz
       ↓
Procesamiento biométrico
       ↓
Resultado
       ↓
Backend principal
       ↓
Aplicación móvil
```

Este flujo no establece todavía:

- protocolo de comunicación;
- URL;
- puerto;
- infraestructura;
- ubicación física;
- tecnología de mensajería;
- mecanismo de descubrimiento;
- configuración cloud.

Esos elementos deberán definirse posteriormente.

---

# 9. Base de datos

La tecnología considerada para la persistencia estructurada es:

**PostgreSQL**

La base de datos deberá almacenar únicamente la información que corresponda según:

- requisitos;
- reglas de negocio;
- modelo de datos;
- decisiones de seguridad.

La arquitectura de datos debe mantener coherencia con `03_BASE_DE_DATOS_NAYRA.md`.

No se debe crear una tabla únicamente porque un componente del sistema necesite temporalmente almacenar información.

---

# 10. Información biométrica

La información relacionada con biometría de voz debe tratarse como información sensible.

La arquitectura deberá contemplar:

- protección durante la transmisión;
- control de acceso;
- protección durante el almacenamiento;
- minimización de datos;
- separación de responsabilidades;
- trazabilidad de operaciones relevantes.

La estrategia concreta de almacenamiento de audio y/o representaciones biométricas se definirá en:

`05_BIOMETRIA_NAYRA.md`

y

`06_SEGURIDAD_NAYRA.md`.

No asumir que las grabaciones de voz deben almacenarse permanentemente.

---

# 11. Frontend / aplicación móvil

Nayra contempla una aplicación móvil como interfaz principal para el usuario.

La tecnología definitiva del frontend todavía debe ser registrada en:

`07_DECISIONES_TECNICAS_NAYRA.md`.

La aplicación deberá considerar:

- accesibilidad;
- interacción mediante voz;
- navegación comprensible;
- autenticación;
- gestión de sesión;
- comunicación segura con el backend;
- manejo de errores;
- retroalimentación al usuario.

No seleccionar una tecnología de frontend sin documentar previamente la decisión.

---

# 12. API

La comunicación entre la aplicación y el backend deberá realizarse mediante una interfaz de servicios claramente definida.

La especificación de endpoints deberá documentarse en:

`04_API.md`.

Cada endpoint deberá estar relacionado con uno o más requisitos.

La API debe definir como mínimo, cuando corresponda:

- método HTTP;
- ruta;
- autenticación requerida;
- parámetros;
- body;
- respuesta;
- códigos de estado;
- errores;
- permisos.

No implementar endpoints únicamente por conveniencia del programador.

---

# 13. Seguridad arquitectónica

La seguridad es transversal a toda la arquitectura.

Se deben considerar, como mínimo:

- autenticación;
- autorización;
- protección de credenciales;
- gestión de sesiones;
- cifrado de comunicaciones;
- validación de entradas;
- control de acceso;
- protección de información personal;
- protección de información biométrica;
- auditoría;
- protección frente a spoofing.

La especificación detallada pertenece a:

`06_SEGURIDAD_NAYRA.md`.

---

# 14. Arquitectura y accesibilidad

La arquitectura debe permitir que las funcionalidades de accesibilidad definidas en los requisitos sean implementables.

La accesibilidad debe considerarse en:

- interfaz;
- navegación;
- mensajes;
- interacción por voz;
- autenticación;
- manejo de errores;
- retroalimentación del sistema.

La arquitectura no debe introducir una dependencia visual innecesaria para las funciones destinadas al usuario objetivo.

_(2026-10-02: la interacción es **voice-first** (D-075 a D-080): TTS propio en la app, botón de voz fijo que delimita cada captura, semidúplex y diálogo guiado con gramáticas cerradas. Ver «Interacción voice-first» en la arquitectura lógica del módulo de voz y `09_REGLAS_DESARROLLO_NAYRA.md` §28.)_

---

# 15. ArchiMate

La representación arquitectónica del proyecto utilizará ArchiMate cuando corresponda.

La estructura conceptual deberá mantener la jerarquía establecida para el proyecto:

```text
Servicio
   ↓
Función
   ↓
Componente
```

Donde:

- **Servicio** representa el nivel superior;
- **Función** representa una capacidad funcional interna;
- **Componente** representa el nivel inferior de la jerarquía utilizada para describir la solución.

La modelación concreta de elementos ArchiMate deberá realizarse después de definir la arquitectura técnica.

No utilizar un elemento ArchiMate únicamente por su nombre. Su tipo debe corresponder a su responsabilidad arquitectónica.

---

# 16. Arquitectura lógica vs. física

Debe distinguirse claramente entre:

## Arquitectura lógica

Describe:

- servicios;
- funciones;
- componentes;
- responsabilidades;
- relaciones;
- flujos lógicos.

## Arquitectura física

Describe:

- servidores;
- instancias;
- infraestructura;
- redes;
- almacenamiento;
- despliegue;
- ubicaciones;
- servicios cloud.

Una decisión tomada para la arquitectura lógica no debe interpretarse automáticamente como una decisión de infraestructura física.

---

# 17. Infraestructura cloud

Google Cloud Platform (GCP) ha sido considerada como plataforma de despliegue.

Sin embargo:

> **La configuración definitiva de GCP todavía no está aprobada en este documento.**

No asumir automáticamente:

- número de instancias;
- regiones;
- zonas;
- VPC;
- subredes;
- DMZ;
- firewall;
- balanceador;
- API Gateway;
- almacenamiento;
- servicios administrados.

Todos estos elementos deberán documentarse cuando formen parte de la arquitectura final.

**Actualización 2026-09-29 — arquitectura objetivo en GCP (etapa futura, no desplegada).**

Las decisiones D-062 a D-074 (`07_DECISIONES_TECNICAS_NAYRA.md`) fijan la región `us-east1` y este recorrido:

`Internet → API Gateway → balanceador de aplicaciones externo regional → firewall → VPC → subred de solo proxy (DMZ) → grupo de instancias administrado regional con **2 instancias en zonas diferentes** (D-023) → Cloud SQL PostgreSQL 16 con IP privada`.

Cada instancia es una VM e2-standard-2 que ejecuta Nayra-Back y Nayra-Voz en contenedores. Nayra-Voz solo es accesible desde la red interna.

En desarrollo hay 1 instancia activa. Es una configuración temporal: con una sola instancia no se afirma alta disponibilidad. Las 2 instancias se activan en las pruebas de disponibilidad, y para eso primero hay que implementar el estado compartido (D-064 y D-065).

Estas decisiones no autorizan otros componentes. El análisis completo está en `docs/despliegue/AUDITORIA_FINAL_DESPLIEGUE_GCP_2026-09-29.md`.

---

# 18. Alta disponibilidad

La alta disponibilidad es un aspecto arquitectónico que deberá evaluarse según:

- requisitos;
- alcance;
- presupuesto;
- complejidad;
- necesidades del sistema;
- indicaciones académicas.

No asumir que "alta disponibilidad" implica automáticamente múltiples regiones, múltiples zonas o múltiples servidores.

La estrategia final deberá quedar documentada en la arquitectura física aprobada.

---

# 19. Principio de mínima complejidad

La arquitectura debe ser suficientemente robusta para cumplir los requisitos, pero no debe introducir complejidad innecesaria.

No crear automáticamente:

- microservicios independientes;
- colas;
- buses de integración;
- API gateways;
- balanceadores;
- caches;
- múltiples bases de datos;
- sistemas distribuidos;

si los requisitos y la arquitectura aprobada no los justifican.

---

# 20. Trazabilidad arquitectónica

Toda decisión arquitectónica importante debe poder relacionarse con una necesidad del proyecto.

La trazabilidad recomendada es:

```text
Requisito
   ↓
Necesidad técnica
   ↓
Decisión arquitectónica
   ↓
Componente
   ↓
Implementación
```

Ejemplo conceptual:

```text
HU
 ↓
Necesidad de autenticación por voz
 ↓
Servicio de autenticación biométrica
 ↓
Componente de procesamiento de voz
 ↓
Implementación Python
```

Este ejemplo no define por sí mismo la arquitectura final; únicamente muestra la forma de mantener trazabilidad.

---

# 21. Reglas para Claude

Cuando Claude trabaje sobre la arquitectura deberá:

1. consultar `01_REQUISITOS_NAYRA.md`;
2. consultar este documento;
3. revisar `07_DECISIONES_TECNICAS_NAYRA.md`;
4. no asumir decisiones que no estén aprobadas;
5. no reutilizar arquitecturas descartadas;
6. no crear componentes innecesarios;
7. no cambiar la arquitectura silenciosamente;
8. explicar cualquier cambio estructural;
9. mantener consistencia entre arquitectura lógica y física;
10. mantener trazabilidad entre requisitos y componentes.

Si una implementación requiere una decisión arquitectónica no documentada, Claude debe señalarla como pendiente.

---

# 22. Decisiones pendientes

Antes de considerar este documento como arquitectura definitiva, deberán definirse:

- arquitectura lógica final;
- arquitectura física final;
- tecnología del frontend móvil (D-007, _resuelta el 2026-09-27: Flutter_) y del panel web (D-045);
- estructura definitiva del backend;
- división exacta de responsabilidades Java/Python (_AG-12: definida en la arquitectura lógica del módulo de voz y en D-056_);
- protocolo de comunicación Java-Python (_AG-12: REST interno, D-010_);
- reconocimiento del habla: tecnología y ubicación (D-046) — _AG-13: contenido del desafío resuelto; resto pendiente. 2026-10-02: para comandos y dictados, Vosk en el servidor es una propuesta a probar (D-076)_;
- mecanismo técnico de autenticación (el flujo funcional del usuario está aprobado en D-037; siguen pendientes hash D-047, dispositivo D-048 y autenticación del administrador D-050); _(2026-09-27: D-048 aprobada; D-050 aprobada funcionalmente —usuario y contraseña en el panel web— y no implementada)_;
- mecanismo técnico de autorización (roles aprobados en D-041);
- estrategia de sesiones (_modelo de datos v4: JWT con `jti` y tabla `sesiones`, 5 min de inactividad, sin renovación; P-5 pendiente_);
- estructura definitiva de APIs;
- estrategia de almacenamiento biométrico (_AG-13: D-013_);
- estrategia anti-spoofing (_AG-13: D-012_);
- servicios concretos de GCP;
- red y segmentación;
- estrategia de disponibilidad;
- mecanismo de despliegue;
- mecanismos de observabilidad;
- estrategia de auditoría.

Estas decisiones deben registrarse en:

`07_DECISIONES_TECNICAS_NAYRA.md`.

---

# 23. Criterio de aprobación

La arquitectura se considerará aprobada para implementación únicamente cuando:

- sea coherente con los requisitos;
- sea compatible con el alcance de la tesis;
- haya sido revisada por el equipo;
- haya sido validada académicamente cuando corresponda;
- esté documentada en este archivo;
- las decisiones tecnológicas relevantes estén registradas en `07_DECISIONES_TECNICAS_NAYRA.md`.

Hasta entonces, Claude debe tratar las partes pendientes como **no definidas**.

---

# 24. Regla principal

> **Claude debe implementar la arquitectura documentada y aprobada, no diseñar silenciosamente una arquitectura alternativa.**

Si una decisión no está definida:

**no asumir → identificar → proponer si se solicita → aprobar → documentar → implementar.**


## Infraestructura de aplicación definida

La arquitectura contempla **2 instancias de aplicación**, ubicadas en **zonas diferentes**, para mejorar la disponibilidad del sistema. La distribución en zonas diferentes busca evitar que una falla localizada en una zona provoque la indisponibilidad total del servicio.

Esta decisión no implica asumir automáticamente otros componentes de infraestructura. La arquitectura física deberá documentar por separado los elementos que sean aprobados, evitando incorporar componentes como DMZ, balanceador, API Gateway, VPC u otros sin una decisión explícita.

### Servicios lógicos por responsabilidad (modelo de datos v4, 2026-09-27)

El modelo de datos v4 separa la solución en **tres servicios por responsabilidad**. Es una separación **lógica**: no sustituye a las 2 instancias en zonas diferentes (D-023), "3 servicios" **no** significa "3 réplicas" y no agrega infraestructura.

| Servicio | Responsabilidad | Datos que posee | Implementación actual |
|---|---|---|---|
| **Negocio** | Usuarios, cuentas financieras, entidades, registro de identidad simulado, operaciones, notificaciones, administración | `usuarios`, `registro_identidad_simulado`, `entidades_bancarias`, `cuentas`, `operaciones`, `notificaciones` | Spring Boot (`Nayra-Back`) |
| **Autenticación** | PIN, dispositivos, desafíos y nonces (en memoria), sesiones (JWT con `jti`) y decisión final de autenticación (D-056). Es la única autoridad sobre la sesión: Negocio le consulta si una sesión es válida y no implementa lógica propia de sesiones | `credenciales`, `dispositivos`, `sesiones` | Spring Boot (`Nayra-Back`, **misma aplicación** que Negocio) |
| **Biométrico** | Calidad, contenido del desafío, anti-spoofing, verificación 1:1 y referencia biométrica cifrada | `biometria.perfiles_voz` | Python + FastAPI (`Nayra-Voz`) |

Ambos servicios Java insertan en `auditoria`. PostgreSQL es compartido: un esquema `nayra` con FK entre dominios y el esquema `biometria` separado, sin FK física. Separar físicamente Negocio y Autenticación, la orquestación del registro asistido entre servicios (P-10) y la ejecución de las migraciones y permisos por servicio (P-4) siguen **pendientes**. Flujo de la sesión: APK → Negocio → Autenticación.

## Integración bancaria y alcance de las transacciones

Nayra **no se conectará directamente con bancos reales** dentro del alcance del proyecto. Para demostrar el funcionamiento de la autenticación y de las operaciones de una billetera digital, se utilizará un **entorno bancario simulado/controlado**.

El entorno simulado permitirá representar:
- entidades bancarias;
- la cuenta financiera asociada a cada usuario (una por usuario, D-025);
- el registro de identidad simulado utilizado durante el registro (D-035);
- operaciones/transacciones;
- estados necesarios para las pruebas del prototipo.

Las operaciones realizadas durante el desarrollo y la validación serán **simuladas** y no involucrarán dinero real ni cuentas bancarias reales.

Por tanto, la arquitectura debe contemplar la interacción de Nayra con el entorno simulado, pero **no debe asumir integraciones con APIs, sistemas core bancarios o servicios externos de entidades financieras reales**.

## Componentes del primer entregable (AG-01, D-034)

El primer entregable es un **prototipo funcional**. Su arquitectura lógica contempla los siguientes componentes, sin fijar todavía tecnologías pendientes, protocolos ni despliegue:

| Componente | Responsabilidad en el prototipo | Tecnología |
|---|---|---|
| **Aplicación móvil** | _(Modelo v4: solo Android, instalada mediante APK; iOS fuera de alcance.)_ Interfaz principal del usuario: interacción por voz, registro, autenticación, tutorial, saldo, movimientos, transferencias a otros usuarios (el QR queda para un siguiente entregable, D-042). Genera y custodia el par de claves del dispositivo (D-048). _(2026-10-02, D-075: interacción voice-first con TTS propio y un botón de voz fijo que delimita cada captura.)_ | **Flutter** (D-007) + canal de plataforma Kotlin para el almacén de claves |
| **Backend principal** | _(Modelo v4: contiene los servicios lógicos Negocio y Autenticación en una sola aplicación.)_ Lógica de negocio, cuentas de acceso, PIN (hash en `credenciales`, D-061), dispositivos (clave pública y verificación de firma, D-048), desafíos y nonces (en memoria), sesiones (JWT con `jti` = `sesiones.id`, 5 min de inactividad, sin renovación, D-018), autorización, operaciones simuladas, solicitudes, auditoría, decisión de autenticación y coordinación con el componente biométrico | Java + Spring Boot (D-001) |
| **Procesamiento biométrico de voz** | Calidad de audio, contenido del desafío, anti-spoofing, enrolamiento, verificación 1:1 y custodia de la referencia biométrica cifrada | Python (D-002) + FastAPI (D-010) + SpeechBrain ECAPA-TDNN (D-011) + AASIST (D-012) + Vosk (D-046, parcial); referencia en esquema `biometria` (D-013) |
| **Reconocimiento del habla** | Contenido del desafío (APROBADO: Vosk en el servicio Python); comando de activación, tecnología del PIN dictado (el dictado está aprobado por D-061; en el prototipo, candidata provisional) y DNI en recuperación (PENDIENTES). _(2026-10-02: comandos, dictados y respuestas Sí/No con Vosk en el servicio Python y gramática por estado del diálogo, como **propuesta a probar**, D-076.)_ | D-046 (parcial) |
| **Base de datos** | Persistencia estructurada | PostgreSQL (D-003) |
| **Entorno financiero simulado** | Entidades bancarias, cuentas financieras, operaciones y registro de identidad simulado | Dentro del modelo de datos (D-021, D-035) |
| **Panel web del administrador** | Usuarios, estado, bloqueo/desbloqueo, dispositivo, solicitudes, auditoría, métricas básicas. Autenticación con usuario y contraseña (D-050, aprobada funcionalmente, **no implementada**; el prototipo usa provisionalmente dispositivo + PIN + voz para el ADMIN). Las consultas de auditoría existentes se mantienen en el alcance de `01` §12.4 (H-03, cerrada) | PENDIENTE (D-045) |

No se incorporan en el primer entregable: APIs externas de identidad, SMS/OTP, WhatsApp, chatbot (el diálogo guiado de D-076 no lo es), bancos o pagos reales, múltiples dispositivos activos ni infraestructura cloud compleja (D-034, D-043).

### Flujos lógicos aprobados

Estos flujos son **conceptuales**; no definen protocolo, endpoints, ubicación física ni tecnología.

```text
REGISTRO INICIAL CON AYUDA (D-052, modifica D-036; desde el 2026-10-02 modalidad opcional, D-077) — usa DNI
Persona solicita registrarse → representante autorizado (administrador u otra persona autorizada) asiste
    → se proporciona el DNI → Backend consulta el registro de identidad simulado → se muestran los datos
    → el representante valida la identidad  [registro técnico y auditoría de quién validó: pendiente]
    → la persona confirma sus datos → celular (D-043) → contraseña (Backend guarda solo hash)
    → vinculación del dispositivo
    → enrolamiento de voz: App → Backend → componente biométrico (anti-spoofing + referencia)
    → tutorial → fin del registro
La consulta del DNI por sí sola no prueba la identidad. La biometría no valida la identidad en el registro.
DNI ya registrado → flujo de cambio de dispositivo / recuperación

REGISTRO INICIAL AUTÓNOMO (D-077, D-078; 2026-10-02) — usa DNI/CE declarado
Persona inicia el registro por voz → dicta tipo y número de documento → lectura de vuelta   [identidad declarada]
    → Backend consulta el registro de identidad simulado → la persona confirma sus datos   [datos verificados]
    → celular → PIN (Backend guarda solo hash) → vinculación del dispositivo
    → enrolamiento de voz: App → Backend → componente biométrico   [voz registrada]
    → validación posterior con desafío nuevo (contenido → anti-spoofing → 1:1)   [voz validada]
    → tutorial guiado por voz → registro completado
No prueba la titularidad del documento (riesgo aceptado del prototipo). Cómo se guardan la modalidad y el nivel: pendiente.
Documento ya registrado → flujo de cambio de dispositivo / recuperación

INICIO DE SESIÓN HABITUAL (D-037, D-053) — NO usa DNI
"Iniciar sesión Nayra" → dispositivo vinculado → cuenta
    → contraseña (Backend) → desafío variable → respuesta por voz
    → comprobación del contenido → anti-spoofing → verificación 1:1 (componente biométrico)
    → Backend aplica reglas (3 intentos de PIN, D-044) → sesión (JWT con jti, D-018)

CAMBIO / PÉRDIDA DE DISPOSITIVO Y RECUPERACIÓN (D-040, D-053) — usa DNI para localizar la cuenta
Nuevo dispositivo → DNI → cuenta existente → contraseña + verificación 1:1 contra la referencia existente + anti-spoofing
    → nuevo dispositivo ACTIVO, anterior REVOCADO → (opcional) nuevo enrolamiento
Prohibido: DNI → cuenta → nueva voz → acceso
```

La referencia biométrica permanece asociada a la cuenta en el backend (D-038); su almacenamiento concreto sigue pendiente (D-013). _(AG-13: resuelto por D-013, ver abajo.)_

### Arquitectura lógica del módulo de voz (AG-13 y AG-12, 2026-09-27)

Decisiones: D-007, D-010 a D-013, D-018, D-046 (parcial), D-048, D-054 a D-061. Este flujo es **lógico**: no define URL, puertos, rutas de endpoints ni infraestructura física (§8, §16). El contrato se documentará en `04_API.md` (D-014) cuando se decida D-056.

```text
INICIO DE SESIÓN
APP (Flutter)                         SPRING BOOT                                SERVICIO DE VOZ (Python, FastAPI)
1. "Iniciar sesión Nayra" (activa)
2. Solicita inicio ───────────────►  Emite nonce de un solo uso
3. Firma el nonce con la clave   ──►  Verifica firma con la clave pública del
   privada del dispositivo           dispositivo ACTIVO → determina la cuenta
4. PIN (teclado accesible)       ──►  Verifica PIN (hash, D-047) → si falla: intento (D-044)
                                 ◄──  Emite desafío (D-054), un solo uso, ligado a cuenta
5. Lee el desafío (TTS), permite      + dispositivo
   repetir, graba WAV 16 kHz
6. Envía audio + id del desafío  ──►  Verifica y consume el desafío
                                      Reenvía audio + texto esperado ─────────►  calidad → contenido → anti-spoofing
                                                                                 → embedding + coseno 1:1 contra la
                                                                                 referencia cifrada (esquema biometria)
                                      ◄──────────── resultado por etapa ───────  (audio descartado)
                                      Decide: intentos, bloqueo, sesión
                                 ◄──  (cierre automático tras 5 min de inactividad), auditoría
7. Resultado accesible
```

| Decisión | Responsable |
|---|---|
| Activar el flujo | App (sin valor de seguridad) |
| Determinar la cuenta (firma del dispositivo) | Spring Boot (D-048) |
| Validar el PIN | Spring Boot (D-061, D-047) |
| Generar, guardar e invalidar desafío y nonce | Spring Boot (D-054, D-048) |
| Calidad, contenido, anti-spoofing, similitud 1:1 | Servicio Python (D-011, D-012, D-046, D-058, D-059) |
| Aplicar los umbrales técnicos y devolver veredictos por etapa | Servicio Python/FastAPI (D-056); similitud 0.80 y bona fide 0.90 (D-055, provisionales hasta la calibración de D-060) |
| Aceptar o rechazar la autenticación, intentos, bloqueo, sesión | Spring Boot, servicio lógico de Autenticación (D-044: solo el PIN cuenta; D-018: JWT con `jti`) |
| Guardar y leer la referencia biométrica | Servicio Python (D-013) |
| Auditoría (sin audio, embeddings ni PIN) | Spring Boot |

**Interacción voice-first (D-075, D-076, 2026-10-02):** la app habla con TTS propio del dispositivo y el botón de micrófono fijo delimita cada captura (toca para empezar, toca para terminar). Los comandos y el dictado viajan App → Spring Boot → servicio de voz, que los reconoce con una gramática cerrada según el estado del diálogo (Vosk en servidor, propuesta a probar). El audio de comandos, dictado y PIN nunca entra al pipeline biométrico; solo el modo verificación o registro de voz usa la cadena calidad → contenido → anti-spoofing → 1:1. El enrolamiento puede ser autónomo o asistido (D-077) y termina con una validación posterior con desafío nuevo (D-078).

**Enrolamiento (registro asistido, D-052 paso 11; autónomo, D-077):** App → Spring Boot (emite un desafío por muestra) → servicio Python (calidad, contenido, anti-spoofing, embedding; guarda el centroide cifrado) → Spring Boot recibe solo el resultado.

**Cambio de dispositivo (D-040):** mismo pipeline; la cuenta se localiza por DNI y el desafío se liga a la solicitud de cambio y al dispositivo nuevo; la verificación es contra la referencia existente; si todo pasa, Spring Boot registra la nueva clave pública y revoca el dispositivo anterior. La jerarquía **Servicio → Función → Componente** y su representación en ArchiMate se aplicarán a estos componentes cuando se modele la arquitectura lógica.
