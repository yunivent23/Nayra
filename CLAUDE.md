# CLAUDE.md — Nayra

## 1. Propósito

Este archivo es la guía principal para Claude Code al trabajar en el proyecto Nayra.

Claude debe utilizar la documentación de `docs/` como fuente de contexto y decisión antes de crear o modificar código. Su función es implementar el proyecto de acuerdo con los requisitos y decisiones del equipo, no diseñar autónomamente nuevos requisitos o una arquitectura distinta.

---

## 2. Orden obligatorio de lectura

Antes de realizar cambios importantes, leer según corresponda:

1. `docs/00_CONTEXT_NAYRA_v2.md`
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

SpeechBrain está bajo evaluación. No tratarlo como decisión definitiva si el registro técnico no ha sido actualizado.

### Cloud

GCP está considerada como plataforma de despliegue.

No asumir servicios específicos de GCP sin aprobación.

### Frontend

La tecnología definitiva debe verificarse en `07_DECISIONES_TECNICAS_NAYRA.md` y en el repositorio.

No asumir Flutter únicamente por aparecer en la clasificación técnica del Excel si la decisión final no está registrada.

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

La comunicación Java ↔ Python todavía debe verificarse según las decisiones técnicas vigentes.

No inventar un contrato definitivo de comunicación si todavía está pendiente.

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

Reglas:

- No agregar tablas arbitrariamente.
- No eliminar tablas sin decisión explícita.
- No inventar relaciones.
- No modificar campos estructurales importantes sin justificarlo.
- No crear automáticamente tablas biométricas.
- No asumir que el audio se almacenará en PostgreSQL.
- No crear un CRUD simplemente porque exista una tabla.

Las tablas `CUENTAS` y `OPERACIONES` pertenecen al entorno bancario simulado.

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

No asumir JWT, OAuth, refresh tokens u otro mecanismo específico si todavía aparece como pendiente en las decisiones técnicas.

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
