# CLAUDE.md — Nayra

## 1. Propósito

Este archivo es la guía principal para Claude Code al trabajar en el proyecto Nayra.

Claude debe utilizar la documentación de `docs/` como fuente de contexto y decisión antes de crear o modificar código. Su función es implementar el proyecto de acuerdo con los requisitos y decisiones del equipo, no diseñar autónomamente nuevos requisitos o una arquitectura distinta.

---

## 2. Orden obligatorio de lectura

Antes de realizar cambios importantes, leer según corresponda:

1. `docs/00_CONTEXT.md`
2. `docs/01_REQUISITOS_NAYRA.md`
3. `docs/02_ARQUITECTURA_NAYRA.md`
4. `docs/03_BASE_DE_DATOS_NAYRA.md`
5. `docs/05_BIOMETRIA_NAYRA.md` — si la tarea involucra voz o biometría.
6. `docs/06_SEGURIDAD_NAYRA.md` — si la tarea involucra seguridad.
7. `docs/07_DECISIONES_TECNICAS_NAYRA.md`
8. `docs/08_ESTADO_PROYECTO.md`
9. `docs/09_REGLAS_DESARROLLO_NAYRA.md`
10. El código existente relacionado con la tarea.

Si existe una contradicción entre una instrucción aislada y la documentación aprobada, no asumir una solución silenciosamente: identificar la contradicción y resolverla mediante una decisión documentada.

---

## 3. Fuentes de verdad

### Requisitos

La fuente principal de requisitos es:

`docs/01_REQUISITOS_NAYRA.md`

No inventar historias de usuario, actores, permisos, reglas de negocio ni funcionalidades que no estén justificadas.

### Arquitectura

La arquitectura se rige por:

`docs/02_ARQUITECTURA_NAYRA.md`

No reutilizar arquitecturas anteriores descartadas.

### Base de datos

La referencia del modelo de datos es:

`docs/03_BASE_DE_DATOS_NAYRA.md`

No crear tablas, campos o relaciones arbitrariamente.

### Biometría

Las reglas de biometría y anti-spoofing están en:

`docs/05_BIOMETRIA_NAYRA.md`

### Seguridad

Las reglas de seguridad están en:

`docs/06_SEGURIDAD_NAYRA.md`

### Decisiones técnicas

Las decisiones técnicas se registran en:

`docs/07_DECISIONES_TECNICAS_NAYRA.md`

Una decisión con estado `PENDIENTE`, `EN EVALUACIÓN`, `DESCARTADA` o `REEMPLAZADA` no debe tratarse como decisión vigente.

### Estado de implementación

El estado real del proyecto se registra en:

`docs/08_ESTADO_PROYECTO.md`

Sin embargo, el estado debe verificarse siempre contra el repositorio. La documentación de estado no sustituye la evidencia del código.

### Reglas de desarrollo

Las reglas específicas de implementación están en:

`docs/09_REGLAS_DESARROLLO_NAYRA.md`

---

## 4. Reglas críticas del proyecto

### 4.1 No inventar

No inventar:

- requisitos;
- historias de usuario;
- endpoints;
- tablas;
- campos;
- relaciones;
- roles;
- permisos;
- integraciones;
- reglas de negocio;
- modelos biométricos;
- umbrales;
- resultados experimentales;
- componentes de infraestructura.

Si una tarea requiere una decisión que todavía no existe, marcarla como pendiente y explicar qué información hace falta.

### 4.2 No recuperar arquitecturas descartadas

Las arquitecturas anteriores que fueron descartadas no forman parte de Nayra.

No asumir ni recuperar automáticamente:

- DMZ;
- API Gateway;
- balanceadores;
- VPC o subredes concretas;
- topologías de red anteriores;
- distribución específica de servidores;
- regiones o zonas concretas no aprobadas;
- servicios cloud concretos no aprobados.

Si alguno de estos elementos vuelve a ser necesario, debe existir una decisión documentada.

### 4.3 Arquitectura aprobada actualmente

La documentación actual establece que Nayra contempla:

- 2 instancias de aplicación;
- ubicadas en zonas diferentes;
- con el objetivo de mejorar la disponibilidad.

Esta decisión no autoriza a inventar otros componentes de infraestructura.

### 4.4 Entorno financiero

Nayra utiliza un entorno bancario simulado/controlado.

No existe integración directa con bancos reales dentro del alcance actual.

No:

- crear integraciones con APIs bancarias reales;
- solicitar credenciales bancarias;
- asumir sistemas core bancarios reales;
- tratar las operaciones del prototipo como transacciones financieras reales.

Las cuentas y operaciones son simuladas.

Decisiones AG-00 (`docs/07_DECISIONES_TECNICAS_NAYRA.md`, D-024 a D-033):

- distinguir según contexto entre **cuenta de acceso** (acceso a Nayra) y **cuenta financiera** (tabla `CUENTAS`);
- cada usuario tiene **una única cuenta financiera**, asociada a una **entidad bancaria simulada**;
- no crear funcionalidades de apertura de cuentas, múltiples cuentas, transferencias entre cuentas propias ni gestión de entidades bancarias por el administrador.

### 4.5 Primer entregable y decisiones AG-01

Decisiones en `docs/07_DECISIONES_TECNICAS_NAYRA.md`, D-034 a D-053. Reglas operativas en `docs/09_REGLAS_DESARROLLO_NAYRA.md` §26.

- **Alcance (D-034):** prototipo funcional, no sistema productivo. Alcance por HU en `docs/01_REQUISITOS_NAYRA.md` §12.4.
- **Roles (D-041):** solo `USER` y `ADMIN`. No existe personal de atención. El administrador no puede modificar saldos ni operaciones; sus acciones se auditan.
- **Identidad (D-035):** registro de identidad simulado (DNI, nombres, apellidos); sin APIs reales de terceros.
- **Registro inicial asistido (D-052, modifica D-036):** la persona solicita registrarse → un representante autorizado (administrador u otra persona autorizada) la asiste → se proporciona el DNI → consulta al registro de identidad simulado → se muestran los datos → el representante valida la identidad → la persona confirma sus datos → celular → contraseña → vinculación del dispositivo → enrolamiento de voz con anti-spoofing → tutorial → fin. La consulta del DNI no prueba la identidad; la biometría no valida la identidad en el registro. El representante **no** es un rol del sistema. DNI existente → recuperación/cambio de dispositivo.
- **Flujos diferenciados (D-053):** registro inicial ≠ inicio de sesión ≠ cambio/recuperación de dispositivo. El DNI se usa en el registro, la recuperación y el cambio o pérdida del dispositivo; **no** forma parte del inicio de sesión habitual.
- **Autenticación (D-037, modificada por D-061):** "Iniciar sesión Nayra" → dispositivo vinculado (firma con par de claves, D-048) → **PIN de 6 dígitos** (solo hash, D-061) → desafío variable → comprobación del contenido → anti-spoofing → verificación 1:1 → sesión (cierre automático tras 5 min de inactividad, sin aviso previo, D-018). Nunca 1:N; el DNI no se pide al iniciar sesión.
- **Dispositivo (D-039, D-040):** un único dispositivo activo. Prohibido `DNI → cuenta → nueva voz → acceso`.
- **Excluido del primer entregable:** chatbot, OTP/SMS, contacto de confianza, retiro asistido, eliminación de cuenta, múltiples dispositivos. El QR (HU-123, HU-124) queda documentado para un siguiente entregable.
- **Módulo de voz (AG-13; frontera Java↔Python en AG-12; 2026-09-27):** SpeechBrain ECAPA-TDNN (D-011), AASIST (D-012), Vosk con gramática cerrada para el desafío (D-046, parcial), solo embedding cifrado sin audio (D-013), REST interno con FastAPI (D-010), desafío palabra + 3 dígitos + palabra (D-054). El servicio Python aplica los umbrales técnicos y Spring Boot decide la autenticación (D-056); los valores de los umbrales siguen pendientes de calibración (D-055). Diseño en `docs/05_BIOMETRIA_NAYRA.md` §27; reglas en `docs/09_REGLAS_DESARROLLO_NAYRA.md` §27.
- **Agendas temáticas (AG):** trazabilidad D → AG en `docs/07_DECISIONES_TECNICAS_NAYRA.md`, sección «Agendas temáticas (AG)». AG-02 = Sesiones; AG-13 = Biometría y autenticación por voz; AG-00 y AG-01 son rondas históricas cerradas.
- **Pendientes (AG-01 v5):** distinguir A (bloqueantes funcionales), B (decisiones técnicas que se resuelven durante el desarrollo) y C (deudas técnicas); ver `docs/07_DECISIONES_TECNICAS_NAYRA.md`. D-017 (secretos) es deuda técnica y no bloquea el desarrollo.

---

## 5. Stack tecnológico de referencia

Las tecnologías actualmente consideradas/aprobadas según la documentación son:

### Backend principal

- Java
- Spring Boot

Responsabilidad principal:

- lógica de negocio;
- gestión de usuarios;
- cuentas;
- operaciones simuladas;
- sesiones;
- autorización;
- notificaciones;
- solicitudes de atención;
- auditoría;
- coordinación con componentes especializados.

### Procesamiento especializado

- Python

Responsabilidad principal:

- procesamiento de voz;
- biometría;
- modelos de aprendizaje automático;
- anti-spoofing cuando corresponda.

### Base de datos

- PostgreSQL

### Inteligencia artificial

La estrategia del proyecto prioriza modelos preentrenados, frameworks especializados o APIs existentes.

No entrenar modelos desde cero salvo decisión explícita.

### Biometría

SpeechBrain ECAPA-TDNN está aprobado (D-011, AG-13) junto con AASIST (D-012) y Vosk (D-046, parcial). Los umbrales no tienen valores aprobados (D-055).

### Cloud

GCP está considerada como plataforma de despliegue.

No asumir servicios específicos de GCP sin aprobación.

### Frontend

La aplicación móvil se desarrolla con **Flutter** (D-007, aprobada el 2026-09-27; sin AG temático), con un canal de plataforma Kotlin para el almacén de claves del dispositivo (D-048). El panel web del administrador (D-045) sigue PENDIENTE.

---

## 6. Jerarquía arquitectónica

Cuando se modele la solución según las reglas del proyecto, respetar:

**Servicio → Función → Componente**

Donde:

- Servicio = nivel superior.
- Función = nivel intermedio.
- Componente = nivel inferior.

No modificar esta jerarquía arbitrariamente.

---

## 7. Separación Java / Python

Mantener responsabilidades separadas.

### Java

Debe concentrar principalmente la lógica de negocio y coordinación general.

### Python

Debe concentrar el procesamiento especializado de voz y modelos biométricos.

No trasladar lógica general de negocio a Python innecesariamente.

La comunicación Java ↔ Python es REST interno con FastAPI (D-010, AG-12). El servicio Python aplica los umbrales técnicos y devuelve veredictos por etapa; Spring Boot toma la decisión final de autenticación (D-056). El contrato todavía no está documentado (`04_API.md`, D-014); no inventarlo.

---

## 8. Base de datos

Usar `docs/03_BASE_DE_DATOS_NAYRA.md` como referencia inicial.

Tablas CORE documentadas:

- ROLES
- USUARIOS
- CUENTAS
- SESIONES
- DISPOSITIVOS
- OPERACIONES
- NOTIFICACIONES
- SOLICITUDES_ATENCION
- AUDITORÍA

Tablas de referencia del entorno simulado:

- ENTIDADES_BANCARIAS (AG-00, D-026: catálogo con `id` y `nombre`; sin gestión por el administrador)
- REGISTRO_IDENTIDAD_SIMULADO (AG-01, D-035: DNI, nombres, apellidos)

El modelo lógico vigente tras AG-01 está en `docs/03_BASE_DE_DATOS_NAYRA.md` §16. El modelo físico aprobado (D-051, D-009) está en §17: migraciones Flyway en `Nayra-Back/src/main/resources/db/migration/nayra/`, `ddl-auto=validate`. No crear tablas fuera de §17 (sesiones, operaciones, `biometria.perfiles_voz`) sin decisión explícita.

Reglas:

- No agregar tablas arbitrariamente.
- No eliminar tablas sin decisión explícita.
- No inventar relaciones.
- No modificar campos estructurales importantes sin justificarlo.
- No crear automáticamente tablas biométricas.
- No asumir que el audio se almacenará en PostgreSQL.
- No crear un CRUD simplemente porque exista una tabla.

Las tablas `CUENTAS`, `OPERACIONES` y `ENTIDADES_BANCARIAS` pertenecen al entorno bancario simulado. `CUENTAS` representa la cuenta financiera: relación 1:1 con `USUARIOS` mediante FK con restricción de unicidad, y N:1 con `ENTIDADES_BANCARIAS`. `OPERACIONES` referencia la cuenta financiera de origen y la de destino (D-025 a D-029).

---

## 9. Biometría y anti-spoofing

El objetivo biométrico principal es la verificación de voz.

El flujo conceptual documentado contempla:

1. captura de voz;
2. procesamiento;
3. análisis anti-spoofing;
4. verificación biométrica;
5. resultado;
6. aplicación de las reglas de autenticación por el backend.

Este flujo es conceptual y no debe convertirse en una arquitectura física o contrato técnico definitivo sin las decisiones correspondientes.

No inventar:

- modelo biométrico;
- modelo anti-spoofing;
- umbral;
- métricas;
- dataset;
- estrategia de almacenamiento.

No almacenar muestras de voz innecesariamente.

No registrar audio o representaciones biométricas en logs.

---

## 10. Seguridad

Todo código nuevo debe considerar:

- validación de entradas;
- autenticación;
- autorización;
- mínimo privilegio;
- manejo seguro de errores;
- protección contra inyección;
- protección de datos personales;
- protección de datos biométricos;
- sesiones seguras;
- auditoría cuando corresponda;
- gestión segura de secretos.

Nunca incluir en el código:

- contraseñas reales;
- tokens;
- API keys;
- claves privadas;
- credenciales de base de datos;
- secretos cloud.

No asumir JWT, OAuth, refresh tokens u otro mecanismo específico si todavía aparece como pendiente en las decisiones técnicas. El flujo funcional de autenticación del usuario está aprobado (D-037, con PIN por D-061) y el dispositivo usa par de claves (D-048); siguen pendientes el mecanismo técnico de sesión (D-018; solo la regla de 5 min está aprobada) y el hash del PIN (D-047). Antes de nuevas funcionalidades, corregir los problemas de seguridad del código actual (`docs/06_SEGURIDAD_NAYRA.md` §36.7). Las credenciales ya existentes en la configuración y el historial son **deuda técnica** (D-017, `docs/06_SEGURIDAD_NAYRA.md` §36.8): no eliminarlas, rotarlas ni limpiar el historial en el primer entregable, y no agregar secretos nuevos.

---

## 11. Accesibilidad

Nayra está orientada a usuarios con discapacidad visual.

El desarrollo debe considerar, según corresponda:

- compatibilidad con lectores de pantalla;
- etiquetas accesibles;
- navegación clara;
- instrucciones auditivas;
- mensajes comprensibles;
- controles identificables;
- reducción de dependencia de elementos exclusivamente visuales;
- confirmación y cancelación de operaciones importantes;
- información accesible ante errores.

No inventar una implementación concreta de accesibilidad que dependa de una tecnología frontend todavía no aprobada.

---

## 12. API

La API formal todavía debe considerarse pendiente mientras no exista un contrato aprobado en `docs/04_API.md`.

No inventar endpoints definitivos.

Antes de crear un endpoint, verificar:

1. requisito asociado;
2. responsabilidad del componente;
3. modelo de datos;
4. autenticación/autorización aprobada;
5. impacto de seguridad;
6. integración con Python cuando corresponda.

No crear endpoints únicamente porque sean convenientes para el programador.

---

## 13. Forma de trabajar

Antes de modificar código:

1. Identificar el requisito relacionado.
2. Leer las decisiones técnicas relevantes.
3. Revisar la arquitectura aplicable.
4. Buscar código existente relacionado.
5. Revisar entidades, servicios, repositorios y controladores existentes.
6. Identificar dependencias.
7. Identificar cambios de base de datos.
8. Identificar implicaciones de seguridad.
9. Identificar implicaciones de biometría si corresponde.
10. Implementar el cambio mínimo necesario.

Después de modificar:

1. Compilar.
2. Ejecutar las pruebas disponibles.
3. Revisar errores.
4. Corregir problemas.
5. Revisar que no se hayan introducido decisiones no aprobadas.
6. Actualizar documentación cuando corresponda.
7. Informar claramente qué se modificó.

---

## 14. No duplicar código

Antes de crear una clase, función, endpoint, servicio, entidad o componente:

- buscar si ya existe;
- revisar su comportamiento;
- reutilizarlo si corresponde;
- evitar duplicaciones;
- mantener el estilo existente.

No reestructurar grandes partes del proyecto si la tarea puede resolverse mediante un cambio localizado.

---

## 15. Cambios incrementales

Preferir cambios pequeños, verificables y reversibles.

No realizar simultáneamente:

- cambios arquitectónicos;
- migraciones de base de datos;
- refactorizaciones masivas;
- incorporación de nuevas tecnologías;

si no son necesarios para la tarea actual.

---

## 16. Git

Usar Git para el control de versiones.

Buenas prácticas:

- commits pequeños y descriptivos;
- revisar cambios antes de commit;
- no incluir secretos;
- no subir archivos innecesarios;
- evitar modificar código no relacionado;
- no ejecutar operaciones destructivas de Git sin autorización explícita.

---

## 17. Dependencias

Antes de agregar una dependencia:

1. verificar si ya existe una solución en el proyecto;
2. comprobar si realmente es necesaria;
3. revisar compatibilidad;
4. evitar dependencias redundantes;
5. considerar mantenimiento y seguridad.

No incorporar una librería únicamente por comodidad.

---

## 18. Pruebas

Una funcionalidad no debe considerarse terminada solo porque compile.

Cuando sea posible, validar mediante:

1. pruebas unitarias;
2. pruebas de integración;
3. pruebas funcionales;
4. pruebas de seguridad;
5. pruebas específicas de biometría.

No presentar pruebas no ejecutadas como realizadas.

No presentar métricas hipotéticas como resultados reales.

---

## 19. Trazabilidad

Mantener la relación:

**Requisito → necesidad → servicio → función → componente → implementación → prueba**

Cuando sea posible, utilizar el ID de la historia de usuario, por ejemplo:

`HU-40`

No implementar una funcionalidad relevante sin poder explicar de qué requisito o decisión proviene.

---

## 20. Manejo de incertidumbre

Si falta información:

### Caso A — No afecta una decisión
Continuar usando las decisiones aprobadas.

### Caso B — Requiere una decisión
Detener esa parte y señalar qué decisión falta.

### Caso C — Hay varias alternativas
Presentar las alternativas, sus implicaciones y esperar la selección si la decisión afecta la arquitectura, seguridad, datos o integración.

### Regla

**No asumir → identificar → proponer si se solicita → aprobar → documentar → implementar.**

---

## 21. Estado del proyecto

Después de cambios relevantes, revisar si corresponde actualizar:

`docs/08_ESTADO_PROYECTO.md`

No marcar como implementado algo que solamente está:

- documentado;
- diseñado;
- parcialmente desarrollado;
- pendiente de pruebas.

El estado debe basarse en el repositorio.

---

## 22. Qué hacer ante contradicciones

Si Claude detecta contradicciones entre:

- requisitos;
- arquitectura;
- base de datos;
- biometría;
- seguridad;
- decisiones técnicas;
- código;

debe:

1. detener el cambio que dependa de la contradicción;
2. identificar exactamente qué documentos entran en conflicto;
3. explicar el impacto;
4. proponer alternativas solo si se solicita;
5. esperar una decisión cuando sea necesaria;
6. actualizar la documentación antes de implementar un cambio estructural.

No resolver silenciosamente una contradicción mediante una suposición.

---

## 23. Regla final

Claude Code debe actuar como **desarrollador del proyecto Nayra**, no como diseñador autónomo de requisitos.

Debe:

- implementar lo documentado;
- respetar las decisiones aprobadas;
- detectar inconsistencias;
- evitar invenciones;
- mantener trazabilidad;
- proteger la seguridad y los datos biométricos;
- validar los cambios;
- mantener la documentación sincronizada con el código.

**Cuando exista incertidumbre, preguntar o marcar la decisión como pendiente. No inventar.**
