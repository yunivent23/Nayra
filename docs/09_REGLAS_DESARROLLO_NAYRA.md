# 09 — REGLAS DE DESARROLLO NAYRA

## 1. Propósito

Este documento establece las reglas que Claude Code debe seguir al analizar, modificar y crear código para el proyecto Nayra.

El objetivo es evitar:

- implementación de requisitos inexistentes;
- decisiones técnicas no aprobadas;
- cambios innecesarios de arquitectura;
- duplicación de funcionalidades;
- modificaciones incompatibles con el proyecto;
- pérdida de trazabilidad entre requisitos, diseño y código.

---

## 2. Regla principal

**Claude debe entender antes de modificar.**

Antes de implementar una funcionalidad, debe revisar como mínimo:

1. `00_CONTEXT_NAYRA_v2.md`
2. `01_REQUISITOS_NAYRA.md`
3. `02_ARQUITECTURA_NAYRA.md`
4. `03_BASE_DE_DATOS_NAYRA.md`
5. `05_BIOMETRIA_NAYRA.md`, si la tarea involucra voz;
6. `06_SEGURIDAD_NAYRA.md`, si la tarea involucra seguridad;
7. `07_DECISIONES_TECNICAS_NAYRA.md`
8. `08_ESTADO_PROYECTO.md`
9. el código existente relacionado con la tarea.

No debe implementar basándose únicamente en una instrucción aislada si esta contradice la documentación aprobada.

---

## 3. No inventar requisitos

Claude no debe:

- crear historias de usuario inexistentes;
- agregar funcionalidades porque parezcan útiles;
- inventar campos de base de datos;
- inventar roles;
- inventar permisos;
- inventar endpoints;
- inventar integraciones externas;
- inventar reglas de negocio.

Si una implementación requiere una decisión que no está documentada, debe identificarla como **PENDIENTE** antes de asumirla.

---

## 4. No asumir decisiones técnicas pendientes

Las decisiones pendientes deben permanecer pendientes.

Ejemplos:

- framework de frontend;
- mecanismo definitivo de autenticación;
- autorización;
- comunicación Java ↔ Python;
- modelo biométrico;
- modelo anti-spoofing;
- almacenamiento biométrico;
- diseño final de API;
- configuración física de infraestructura;
- estrategia de sesiones;
- estrategia de auditoría;
- gestión de secretos.

Claude puede proponer alternativas cuando se le solicite, pero no debe convertir una propuesta en una decisión aprobada.

---

## 5. Arquitectura

La arquitectura actual no debe mezclarse con arquitecturas anteriores descartadas.

No recuperar ni asumir componentes previamente descartados como:

- DMZ;
- API Gateway;
- balanceador;
- VPC;
- topologías específicas;
- zonas o regiones concretas;
- otros componentes no aprobados.

La arquitectura contempla actualmente **2 instancias de aplicación ubicadas en zonas diferentes** para mejorar la disponibilidad.

Cualquier nuevo componente de infraestructura debe estar respaldado por una decisión técnica.

---

## 6. Jerarquía arquitectónica

Cuando se utilice la representación arquitectónica definida para el proyecto, respetar la jerarquía:

**Servicio → Función → Componente**

Donde:

- Servicio = nivel superior;
- Función = nivel intermedio;
- Componente = nivel inferior.

No modificar esta jerarquía arbitrariamente.

---

## 7. Separación de responsabilidades

### Java / Spring Boot

Debe concentrar principalmente:

- lógica principal;
- reglas de negocio;
- gestión de usuarios;
- gestión de cuentas;
- operaciones simuladas;
- sesiones;
- autorización;
- notificaciones;
- solicitudes de atención;
- auditoría.

### Python

Debe utilizarse para las tareas especializadas relacionadas con:

- procesamiento de voz;
- biometría de voz;
- modelos de aprendizaje automático;
- anti-spoofing.

No trasladar innecesariamente lógica de negocio general al servicio Python.

---

## 8. Biometría

Para funcionalidades biométricas:

1. utilizar modelos preentrenados salvo decisión contraria;
2. no entrenar modelos desde cero sin autorización;
3. separar procesamiento biométrico de la lógica general del negocio;
4. evitar almacenar audio innecesariamente;
5. proteger los datos biométricos;
6. registrar las decisiones relacionadas con modelos y umbrales;
7. no inventar un modelo biométrico definitivo.

SpeechBrain está bajo evaluación y no debe tratarse como decisión definitiva hasta que se actualice el registro técnico.

---

## 9. Base de datos

La base de datos considerada es PostgreSQL.

Las tablas CORE documentadas en `03_BASE_DE_DATOS_NAYRA.md` son la referencia inicial.

Claude no debe:

- crear tablas arbitrarias;
- eliminar tablas;
- modificar relaciones;
- cambiar tipos de datos;
- agregar campos;
- crear tablas biométricas adicionales;

sin justificar el cambio y verificar que sea compatible con los requisitos y decisiones técnicas.

Cualquier modificación estructural importante debe quedar documentada.

---

## 10. Entorno bancario

Nayra **no se integra con bancos reales** dentro del alcance actual.

Las cuentas y operaciones deben implementarse dentro de un **entorno bancario simulado**.

Claude no debe:

- crear integraciones con APIs bancarias reales;
- solicitar credenciales de bancos;
- asumir acceso a sistemas core bancarios;
- tratar las transacciones del prototipo como operaciones financieras reales.

Las operaciones deben entenderse como simulaciones necesarias para demostrar el funcionamiento de la solución.

---

## 11. Seguridad

Todo código nuevo debe considerar:

- validación de entradas;
- control de acceso;
- protección de credenciales;
- manejo seguro de errores;
- protección contra inyección;
- protección de datos personales;
- protección de datos biométricos;
- control de sesiones;
- auditoría cuando corresponda;
- no exposición de secretos.

Nunca colocar:

- contraseñas reales;
- tokens;
- API keys;
- claves privadas;
- credenciales de bases de datos;

directamente en el código fuente.

---

## 12. Manejo de errores

Los errores deben:

- ser controlados;
- proporcionar mensajes útiles al usuario cuando corresponda;
- evitar exponer información sensible;
- registrarse adecuadamente en backend;
- conservar suficiente información para depuración.

No mostrar stack traces ni credenciales al usuario final.

---

## 13. Código existente

Antes de crear una clase, función, endpoint o componente:

1. buscar si ya existe;
2. revisar cómo se implementa actualmente;
3. reutilizarlo si corresponde;
4. evitar duplicaciones;
5. mantener el estilo existente.

No reestructurar grandes partes del proyecto si la tarea solicitada puede resolverse con un cambio localizado.

---

## 14. Cambios incrementales

Preferir cambios pequeños y verificables.

Después de implementar una modificación:

1. compilar;
2. ejecutar las pruebas disponibles;
3. revisar errores;
4. corregir problemas;
5. documentar el cambio cuando sea relevante.

No realizar múltiples cambios arquitectónicos simultáneos sin necesidad.

---

## 15. Trazabilidad

Cuando una funcionalidad provenga de un requisito, mantener trazabilidad mediante una referencia identificable.

Ejemplo:

`HU-XX → servicio → función → componente → implementación`

No es obligatorio utilizar exactamente ese formato en el código si existe otro mecanismo de trazabilidad establecido, pero la relación debe poder identificarse.

---

## 16. Git y control de versiones

Usar Git para controlar cambios.

Buenas prácticas:

- commits pequeños y descriptivos;
- evitar incluir secretos;
- no subir archivos innecesarios;
- revisar cambios antes del commit;
- evitar modificar código no relacionado con la tarea.

No realizar operaciones destructivas de Git sin autorización explícita.

---

## 17. Dependencias

Antes de agregar una dependencia:

1. verificar si ya existe una solución disponible en el proyecto;
2. evaluar si realmente es necesaria;
3. comprobar compatibilidad con la versión utilizada;
4. evitar dependencias redundantes;
5. documentar dependencias relevantes.

No agregar librerías por conveniencia si una funcionalidad simple puede implementarse con las herramientas existentes.

---

## 18. API

La API definitiva todavía no está completamente definida.

Claude no debe inventar contratos de API definitivos.

Cuando se requiera crear un endpoint durante el desarrollo, primero verificar:

- requisito asociado;
- responsabilidad del servicio;
- modelo de datos;
- mecanismo de autenticación/autorización aprobado;
- comunicación con Python, si corresponde.

La documentación formal de API se completará cuando la arquitectura y las decisiones técnicas sean suficientemente estables.

---

## 19. Frontend

No asumir el framework definitivo del frontend mientras la decisión permanezca pendiente.

Cuando se apruebe:

- framework;
- estructura;
- navegación;
- gestión de estado;
- comunicación con backend;
- accesibilidad;

deberá registrarse en `07_DECISIONES_TECNICAS_NAYRA.md`.

---

## 20. Accesibilidad

La aplicación está orientada a usuarios con discapacidad visual.

El desarrollo debe considerar, según corresponda:

- compatibilidad con lectores de pantalla;
- etiquetas accesibles;
- navegación clara;
- mensajes comprensibles;
- interacción por voz cuando esté definida;
- contraste adecuado;
- controles identificables;
- reducción de dependencia de elementos exclusivamente visuales.

No asumir una implementación concreta de accesibilidad que no haya sido definida para el frontend.

---

## 21. Pruebas

Toda funcionalidad nueva debe ser validada en la medida que el estado del proyecto lo permita.

Prioridades:

1. pruebas unitarias;
2. pruebas de integración;
3. pruebas funcionales;
4. pruebas de seguridad;
5. pruebas específicas de biometría cuando corresponda.

No declarar una funcionalidad como terminada únicamente porque el código compile.

---

## 22. Estado del proyecto

`08_ESTADO_PROYECTO.md` es el documento utilizado para registrar el estado de implementación.

Claude debe actualizarlo cuando:

- una funcionalidad pasa a implementada;
- una funcionalidad pasa a pruebas;
- una funcionalidad queda bloqueada;
- una decisión modifica el plan;
- una tarea importante se completa.

---

## 23. Cuando falte información

Si una tarea requiere una decisión que no está definida:

### Caso A — Puede resolverse sin decisión
Implementar utilizando las decisiones existentes.

### Caso B — Requiere una decisión
Detener la implementación de esa parte y señalar claramente qué decisión falta.

### Caso C — Existen varias alternativas razonables
Presentar las alternativas y sus implicaciones, sin asumir una de ellas como aprobada.

---

## 24. Qué debe hacer Claude antes de cada tarea

Checklist mínimo:

- [ ] Identificar el requisito relacionado.
- [ ] Revisar decisiones técnicas relevantes.
- [ ] Revisar arquitectura relevante.
- [ ] Revisar código existente.
- [ ] Verificar dependencias.
- [ ] Identificar posibles cambios de base de datos.
- [ ] Identificar implicaciones de seguridad.
- [ ] Identificar implicaciones de biometría si corresponde.
- [ ] Implementar el cambio mínimo necesario.
- [ ] Ejecutar compilación/pruebas disponibles.
- [ ] Revisar errores.
- [ ] Actualizar documentación si corresponde.
- [ ] Informar qué se modificó.

---

## 25. Regla final

**Claude debe actuar como desarrollador del proyecto, no como diseñador autónomo de requisitos.**

Su responsabilidad es implementar las decisiones tomadas por el equipo, detectar inconsistencias, señalar decisiones pendientes y mantener la trazabilidad.

Cuando exista incertidumbre, debe **preguntar o marcar la decisión como pendiente**, en lugar de inventar una solución y tratarla como parte oficial de Nayra.
