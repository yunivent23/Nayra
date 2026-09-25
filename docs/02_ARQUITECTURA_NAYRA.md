# 02_ARQUITECTURA.md — Arquitectura del sistema Nayra

## 1. Propósito

Este documento define la arquitectura técnica vigente de Nayra.

**Estado actual:** la arquitectura definitiva se encuentra en proceso de definición y validación.

Por tanto, este archivo establece las reglas arquitectónicas que deberán respetarse, pero **no inventa ni reutiliza una arquitectura anterior que haya sido descartada**.

La arquitectura final deberá construirse a partir de:

1. los requisitos aprobados en `01_REQUISITOS.md`;
2. las decisiones técnicas aprobadas en `07_DECISIONES_TECNICAS.md`;
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

La arquitectura de datos debe mantener coherencia con `03_BASE_DE_DATOS.md`.

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

`05_BIOMETRIA.md`

y

`06_SEGURIDAD.md`.

No asumir que las grabaciones de voz deben almacenarse permanentemente.

---

# 11. Frontend / aplicación móvil

Nayra contempla una aplicación móvil como interfaz principal para el usuario.

La tecnología definitiva del frontend todavía debe ser registrada en:

`07_DECISIONES_TECNICAS.md`.

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

`06_SEGURIDAD.md`.

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

1. consultar `01_REQUISITOS.md`;
2. consultar este documento;
3. revisar `07_DECISIONES_TECNICAS.md`;
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
- tecnología del frontend;
- estructura definitiva del backend;
- división exacta de responsabilidades Java/Python;
- protocolo de comunicación Java-Python;
- mecanismo de autenticación;
- mecanismo de autorización;
- estrategia de sesiones;
- estructura definitiva de APIs;
- estrategia de almacenamiento biométrico;
- estrategia anti-spoofing;
- servicios concretos de GCP;
- red y segmentación;
- estrategia de disponibilidad;
- mecanismo de despliegue;
- mecanismos de observabilidad;
- estrategia de auditoría.

Estas decisiones deben registrarse en:

`07_DECISIONES_TECNICAS.md`.

---

# 23. Criterio de aprobación

La arquitectura se considerará aprobada para implementación únicamente cuando:

- sea coherente con los requisitos;
- sea compatible con el alcance de la tesis;
- haya sido revisada por el equipo;
- haya sido validada académicamente cuando corresponda;
- esté documentada en este archivo;
- las decisiones tecnológicas relevantes estén registradas en `07_DECISIONES_TECNICAS.md`.

Hasta entonces, Claude debe tratar las partes pendientes como **no definidas**.

---

# 24. Regla principal

> **Claude debe implementar la arquitectura documentada y aprobada, no diseñar silenciosamente una arquitectura alternativa.**

Si una decisión no está definida:

**no asumir → identificar → proponer si se solicita → aprobar → documentar → implementar.**


## Infraestructura de aplicación definida

La arquitectura contempla **2 instancias de aplicación**, ubicadas en **zonas diferentes**, para mejorar la disponibilidad del sistema. La distribución en zonas diferentes busca evitar que una falla localizada en una zona provoque la indisponibilidad total del servicio.

Esta decisión no implica asumir automáticamente otros componentes de infraestructura. La arquitectura física deberá documentar por separado los elementos que sean aprobados, evitando incorporar componentes como DMZ, balanceador, API Gateway, VPC u otros sin una decisión explícita.

## Integración bancaria y alcance de las transacciones

Nayra **no se conectará directamente con bancos reales** dentro del alcance del proyecto. Para demostrar el funcionamiento de la autenticación y de las operaciones de una billetera digital, se utilizará un **entorno bancario simulado/controlado**.

El entorno simulado permitirá representar:
- entidades bancarias;
- cuentas asociadas al usuario;
- operaciones/transacciones;
- estados necesarios para las pruebas del prototipo.

Las operaciones realizadas durante el desarrollo y la validación serán **simuladas** y no involucrarán dinero real ni cuentas bancarias reales.

Por tanto, la arquitectura debe contemplar la interacción de Nayra con el entorno simulado, pero **no debe asumir integraciones con APIs, sistemas core bancarios o servicios externos de entidades financieras reales**.
