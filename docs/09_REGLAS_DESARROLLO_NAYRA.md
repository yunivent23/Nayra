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

1. `00_CONTEXT.md`
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

- framework de frontend móvil (D-007) y del panel web (D-045);
- mecanismo técnico de autenticación (el flujo funcional del usuario está aprobado en D-037);
- mecanismo técnico de autorización (los roles USER/ADMIN están aprobados en D-041);
- comunicación Java ↔ Python;
- reconocimiento del habla (D-046);
- modelo biométrico;
- modelo anti-spoofing;
- umbral biométrico;
- almacenamiento biométrico;
- algoritmo de hash, política y normalización de la contraseña (D-047);
- vinculación técnica del dispositivo (D-048);
- procedimiento de recuperación asistida (D-049);
- autenticación del administrador (D-050);
- estrategia de migraciones (D-051);
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

---

## 26. Reglas del primer entregable (AG-01, 2026-09-26)

Decisiones de referencia: `07_DECISIONES_TECNICAS_NAYRA.md`, D-034 a D-053.

1. **Alcance acotado (D-034).** Construir el prototipo funcional, no un sistema productivo. Antes de agregar algo complejo, preguntarse si es indispensable para demostrar el objetivo del primer entregable; si no lo es, clasificarlo como futuro. Alcance por HU: `01_REQUISITOS_NAYRA.md` §12.4.
2. **No agregar por iniciativa propia:** bancos, pagos o APIs reales; SMS, OTP, WhatsApp o llamadas; chatbot; contacto de confianza; múltiples dispositivos activos; agenda del teléfono; retiro asistido; eliminación de cuenta; infraestructura cloud compleja; nuevos roles; nuevas APIs externas; modelos entrenados desde cero; funcionalidades sin HU. Si una de ellas parece técnicamente necesaria, reportarla como dependencia antes de implementarla.
3. **Seguridad primero (`06_SEGURIDAD_NAYRA.md` §36.7).** Antes de nuevas funcionalidades se deben corregir los problemas del código actual: contraseñas en texto plano, rol elegido por el cliente, endpoints expuestos, JWT no aprobado y código heredado. **Excepción (AG-01 v5):** los secretos y credenciales existentes son **deuda técnica** (D-017): no eliminarlos, no rotarlos ni limpiar el historial de Git en esta etapa, y no detener el desarrollo por ello; tampoco agregar secretos nuevos. Se corrigen antes de producción (`06_SEGURIDAD_NAYRA.md` §36.8).
4. **Identidad simulada y registro asistido (D-035, D-052).** La consulta de identidad usa el registro de identidad simulado; no integrar APIs de identidad reales. La consulta del DNI **no** prueba la identidad: un representante autorizado la valida (regla de negocio). **No** crear un rol nuevo para el representante (puede ser un administrador u otra persona autorizada); los roles siguen siendo `USER` y `ADMIN`. **Registro inicial ≠ inicio de sesión ≠ cambio/recuperación de dispositivo (D-053):** el DNI **no** se pide en el inicio de sesión habitual.
5. **Contraseña (D-037).** Solo hash seguro; nunca texto plano, audio ni logs. No usarla como identificador de cuenta. No asumir un algoritmo por existir en el código.
6. **Biometría (D-037, D-038).** Solo verificación 1:1; nunca 1:N. La muestra es la respuesta a un desafío variable cuyo contenido se comprueba; ni el comando de activación ni la contraseña son muestras. Anti-spoofing en enrolamiento y autenticación. No reducir el umbral para aceptar voces alteradas.
7. **Dispositivo (D-039).** Un único dispositivo activo por cuenta de acceso; la reinstalación no se asume reconocible.
8. **Prohibición expresa (D-040).** Nunca implementar `DNI → cuenta encontrada → registrar nueva voz → acceso`. Un nuevo enrolamiento solo tras validar al titular.
9. **Administrador (D-041).** No puede modificar saldos ni operaciones financieras. Toda acción administrativa se audita (actor, afectado, acción, fecha).
10. **Intentos (D-044).** 3 intentos; no implementar el detalle de qué cuenta como intento hasta que se decida. Los errores técnicos no cuentan como intentos del usuario.
11. **QR (D-042).** HU-123 y HU-124 quedan documentadas pero **fuera de la implementación del primer entregable** (siguiente entregable).
12. **Clasificación de pendientes (`07_DECISIONES_TECNICAS_NAYRA.md`, AG-01 v5).** Distinguir A (bloqueantes funcionales), B (decisiones técnicas que se resuelven durante el desarrollo) y C (deudas técnicas). Una deuda técnica no bloquea el desarrollo; solo una decisión A bloquea, y únicamente las funcionalidades que dependen de ella.

## 27. Reglas del módulo de voz, dispositivo y sesión (AG-02, 2026-09-27)

Decisiones de referencia: `07_DECISIONES_TECNICAS_NAYRA.md`, D-007, D-010 a D-013, D-018, D-046, D-048, D-054 a D-061. Diseño en `05_BIOMETRIA_NAYRA.md` §27.

1. **Modelos fijados.** Usar solo SpeechBrain ECAPA-TDNN (D-011), AASIST (D-012) y Vosk con gramática cerrada (D-046). Cambiar de modelo o de versión exige registrarlo en `07` y re-enrolar a los usuarios.
2. **Umbrales solo en configuración** versionada con el modelo; nunca literales en el código. El umbral provisional debe estar registrado en `07` antes de usarse (D-055). El servicio Python/FastAPI aplica los umbrales técnicos y devuelve veredictos por etapa; Spring Boot toma la decisión final de autenticación y controla intentos, bloqueo, sesión y auditoría (D-056). Esto **no** significa que los valores numéricos estén definidos.
3. **Nunca persistir audio** (ni en disco, ni en base de datos, ni en logs). Solo el embedding cifrado en `biometria.PERFILES_VOZ` (D-013).
4. **El PIN no entra al pipeline biométrico.** Ni el PIN ni el comando de activación son muestras de voz (D-037, D-061). El PIN nunca se registra ni se anuncia en voz alta.
5. **Desafío y nonce de un solo uso**, generados en Spring Boot con `SecureRandom` y verificados en el servidor (D-054, D-048).
6. **La clave privada del dispositivo nunca sale del almacén de hardware**; el backend guarda solo la clave pública (D-048).
7. **Sesión:** cierre automático tras 5 minutos de inactividad controlado en el servidor, sin aviso previo, sin opción de continuar y sin tiempo ajustable (D-018). No implementar JWT, token opaco u otro mecanismo hasta cerrar D-018.
8. **Formato de audio:** WAV PCM 16 kHz mono 16 bits; rechazar otros formatos en el servicio de voz (D-057).
9. **Anti-spoofing en enrolamiento y autenticación**; orden calidad → contenido → anti-spoofing → 1:1 (D-059).
10. **No crear endpoints del servicio de voz** hasta documentar y aprobar el contrato en `04_API.md` (D-014, pendiente).
