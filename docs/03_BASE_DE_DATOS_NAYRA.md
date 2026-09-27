# 03_BASE_DE_DATOS_NAYRA.md — Modelo de datos de Nayra

> **Fuente principal:** hoja `TABLAS CORE` del archivo `REQUERIMIENTOS(6).xlsx`.
>
> Este documento registra las estructuras de datos que el equipo había planteado para el proyecto. Se diferencian las estructuras propuestas de las decisiones definitivas de implementación. No se agregan tablas ajenas a la fuente sin una decisión posterior.
>
> **Actualización AG-00 (2026-09-25):** por decisión aprobada se incorporó la tabla de referencia `ENTIDADES_BANCARIAS` (D-026). También se definieron la relación 1:1 entre `USUARIOS` y `CUENTAS` (D-025, D-028), la relación entre `CUENTAS` y `ENTIDADES_BANCARIAS` (D-026) y las referencias de `OPERACIONES` a las cuentas de origen y destino (D-029). Ver sección 15.
>
> **Actualización AG-01 (2026-09-26):** se incorporó el registro de identidad simulado (`REGISTRO_IDENTIDAD_SIMULADO`, D-035) y se actualizó el modelo lógico de `USUARIOS`, `ROLES`, `CUENTAS`, `DISPOSITIVOS`, `SESIONES`, `OPERACIONES`, `SOLICITUDES_ATENCION` y `AUDITORÍA` según D-035 a D-043. **La sección 16 es la referencia vigente** de esas tablas; las tablas de la sección 3 conservan la propuesta original del Excel. No se crean tablas físicas ni migraciones hasta decidir D-051.
>
> **Actualización del 2026-09-27:** se documenta el modelo lógico de la referencia biométrica (`biometria.PERFILES_VOZ`, D-013, AG-13, §16.11) y se actualizan `USUARIOS` (hash del PIN, D-061; origen AG-01; aprobada el 2026-09-27), `DISPOSITIVOS` (clave pública, D-048; origen AG-01; aprobada el 2026-09-27), `SESIONES` (5 minutos de inactividad, D-018, AG-02) y los eventos de `AUDITORÍA`. Siguen sin crearse tablas físicas hasta decidir D-051.
>
> **Actualización del 2026-09-27 (modelo físico):** D-051 y D-009 quedan aprobadas. La **sección 17** registra el modelo físico de las seis tablas implementadas con migraciones Flyway en el esquema `nayra`. `SESIONES`, `OPERACIONES`, `SOLICITUDES_ATENCION`, `NOTIFICACIONES` y `biometria.PERFILES_VOZ` siguen sin tabla física.
>
> **Actualización del 2026-09-27 (modelo de datos v4, implementado):** se crean `credenciales` (PIN fuera de `USUARIOS`), `sesiones`, `operaciones`, `notificaciones` y `biometria.perfiles_voz`; documento DNI/CE; estados `ACTIVO`/`BLOQUEADO`/`INACTIVO`; moneda `PEN`; `codigo_qr` provisional; solo `ANDROID`. **La sección 17 es la referencia vigente del modelo físico** (migraciones `nayra` V001–V011 y `biometria` V001–V003, commits `5bbd505` y `ca98bb3`). La sección 16 conserva el modelo lógico de AG-01 con notas de actualización. Siguen sin tabla `ROLES` y `SOLICITUDES_ATENCION`.

## 1. Propósito

Definir y documentar el modelo de datos utilizado por Nayra, manteniendo trazabilidad con los requisitos y evitando que la implementación genere estructuras que no estén justificadas.

## 2. Motor de base de datos

La tecnología considerada para la base de datos relacional del proyecto es **PostgreSQL**. La versión concreta y la configuración de despliegue deberán registrarse en `07_DECISIONES_TECNICAS_NAYRA.md`.

## 3. Tablas CORE

Las siguientes tablas corresponden a la propuesta registrada en la hoja `TABLAS CORE` del Excel de requerimientos. La tabla `ENTIDADES_BANCARIAS` (§3.10) y las filas marcadas con _(AG-00)_ no proceden del Excel: se incorporaron por decisiones aprobadas de AG-00.

### 3.1 ROLES

_(Propuesta original del Excel. **No se implementa como tabla:** el rol es `usuarios.rol` con `USER`/`ADMIN`, D-009.)_

| Campo | Tipo | Descripción |
|---|---|---|

### 3.2 USUARIOS

| Campo | Tipo | Descripción |
|---|---|---|
| Tipo sugerido | Descripción |  |
| UUID / BIGINT | PK. Identificador único |  |
| VARCHAR(100) | Nombres del usuario |  |
| VARCHAR(100) | Apellidos |  |
| VARCHAR(20) | DNI, CE, etc. |  |
| VARCHAR(20) | Documento de identidad |  |

### 3.3 CUENTAS

Representa la **cuenta financiera** simulada del usuario (D-024). Se mantiene el nombre `CUENTAS` (D-027). Cada usuario tiene una única cuenta financiera (D-025).

| Campo | Tipo | Descripción |
|---|---|---|
| Tipo | Descripción |  |
| UUID / BIGINT | PK |  |
| FK → USUARIOS, UNIQUE | Propietario. Relación 1:1 con `USUARIOS`; se establece al vincular la cuenta durante el registro (D-028) _(AG-00)_ |  |
| FK → ENTIDADES_BANCARIAS | Entidad bancaria simulada de la cuenta (D-026) _(AG-00)_ |  |
| VARCHAR(30) | Identificador de la cuenta |  |
| DECIMAL(15,2) | Saldo disponible |  |
| VARCHAR(3) | PEN, USD, etc. |  |
| VARCHAR(20) | ACTIVA, BLOQUEADA, CERRADA |  |
| TIMESTAMP | Fecha de creación |  |

### 3.4 SESIONES

| Campo | Tipo | Descripción |
|---|---|---|
| Tipo | Descripción |  |

### 3.5 OPERACIONES

| Campo | Tipo | Descripción |
|---|---|---|
| Tipo | Descripción |  |
| UUID / BIGINT | PK |  |
| FK → CUENTAS | Cuenta financiera de origen (cuenta que realiza la operación) (D-029) |  |
| FK → CUENTAS | Cuenta financiera de destino, cuando corresponda (D-029) _(AG-00)_ |  |
| VARCHAR(30) | TRANSFERENCIA, PAGO, RECARGA, etc. |  |
| DECIMAL(15,2) | Monto de la operación |  |
| VARCHAR(3) | Moneda |  |
| VARCHAR(255) | Detalle |  |

### 3.6 DISPOSITIVOS

| Campo | Tipo | Descripción |
|---|---|---|
| Tipo | Descripción |  |
| UUID | PK |  |
| FK | Propietario |  |
| VARCHAR(255) | Identificador único |  |
| VARCHAR(30) | ANDROID / IOS |  |

### 3.7 NOTIFICACIONES

| Campo | Tipo | Descripción |
|---|---|---|
| Tipo | Descripción |  |
| UUID | PK |  |
| FK | Destinatario |  |
| VARCHAR(30) | OPERACION, SEGURIDAD, SISTEMA |  |
| VARCHAR(150) | Título |  |

### 3.8 SOLICITUDES_ATENCION

| Campo | Tipo | Descripción |
|---|---|---|
| Tipo | Descripción |  |
| UUID | PK |  |
| FK | Usuario que solicita atención |  |
| VARCHAR(50) | CONSULTA, RECLAMO, INCIDENTE, etc. |  |

### 3.9 AUDITORÍA

| Campo | Tipo | Descripción |
|---|---|---|
| Tipo | Descripción |  |
| UUID | PK |  |
| FK | Usuario relacionado |  |
| VARCHAR(50) | Tipo de acción |  |
| TEXT | Detalle |  |
| VARCHAR(20) | EXITOSO / FALLIDO |  |
| VARCHAR(45) | IP |  |
| FK | Dispositivo utilizado |  |
| TIMESTAMP | Fecha y hora |  |

### 3.10 ENTIDADES_BANCARIAS _(AG-00)_

Catálogo de referencia de entidades bancarias simuladas del entorno controlado (D-021, D-026). Tiene únicamente los campos mínimos aprobados. **No** representa bancos reales (D-022) y **no** es gestionado por el administrador (D-033): no existe HU para su gestión, por lo que no le corresponde CRUD ni endpoint de administración.

| Campo | Tipo | Descripción |
|---|---|---|
| `id` | PK (tipo según la estrategia de identificadores, pendiente) | Identificador de la entidad |
| `nombre` | Texto (longitud pendiente) | Nombre de la entidad bancaria simulada |

## 4. Relaciones entre tablas

Las relaciones definitivas entre las tablas deberán establecerse antes de generar las migraciones o el esquema físico de PostgreSQL.

Como regla general, se deberá identificar para cada relación:

- tabla origen;
- tabla destino;
- clave primaria;
- clave foránea;
- cardinalidad;
- obligatoriedad;
- comportamiento ante eliminación;
- comportamiento ante actualización.

**No se deben inventar relaciones únicamente por intuición.** Si una relación no está explícitamente definida en la documentación de requisitos o en el diseño de base de datos aprobado, deberá marcarse como pendiente de decisión.

### 4.1 Relaciones aprobadas (AG-00)

| Origen | Destino | Cardinalidad | Clave | Decisión |
|---|---|---|---|---|
| `CUENTAS` | `USUARIOS` | 1:1 (un usuario tiene una única cuenta financiera; una cuenta pertenece a un único usuario) | FK de `CUENTAS` al propietario, con restricción `UNIQUE` | D-025, D-028 |
| `CUENTAS` | `ENTIDADES_BANCARIAS` | N:1 (cada cuenta pertenece a una entidad; una entidad puede tener muchas cuentas) | FK de `CUENTAS` a la entidad | D-026 |
| `OPERACIONES` | `CUENTAS` (origen) | N:1 | FK a la cuenta financiera de origen | D-029 |
| `OPERACIONES` | `CUENTAS` (destino) | N:0..1 (solo cuando la operación tiene destino) — _modelo v4: N:1 obligatorio, destino distinto del origen_ | FK a la cuenta financiera de destino | D-029 |

Siguen **pendientes** para estas relaciones: obligatoriedad (`NULL/NOT NULL`) de cada FK, comportamiento ante eliminación y actualización, y tipos de dato de las claves (estrategia de identificadores). Las demás relaciones del modelo CORE siguen pendientes.

Relaciones aprobadas en AG-01 (detalle en §16):

| Origen | Destino | Cardinalidad | Clave | Decisión |
|---|---|---|---|---|
| `CUENTAS` | `REGISTRO_IDENTIDAD_SIMULADO` (titular) | 1:1 (cada cuenta simulada tiene un titular; un titular tiene una cuenta) | FK de `CUENTAS` al titular simulado | D-025, D-035 |
| `CUENTAS` | `USUARIOS` | 0..1:1 — la FK es **opcional** hasta que la cuenta se vincula en el registro | FK con `UNIQUE` | D-028, D-035 |
| `DISPOSITIVOS` | `USUARIOS` | N:1, con **como máximo un dispositivo ACTIVO** por usuario | FK + restricción de unicidad del dispositivo activo | D-039 |
| `SESIONES` | `USUARIOS` / `DISPOSITIVOS` | N:1 / N:1 | FK | D-037, D-039 |
| `SOLICITUDES_ATENCION` | `USUARIOS` (solicitante) / `USUARIOS` (administrador que atiende) | N:1 / N:0..1 | FK | D-040, D-041 |
| `AUDITORÍA` | `USUARIOS` (actor) / `USUARIOS` (afectado) | N:0..1 / N:0..1 | FK | D-041 |

Relaciones del modelo de datos v4 (2026-09-27, implementadas; detalle en §17):

| Origen | Destino | Cardinalidad | Clave | Decisión |
|---|---|---|---|---|
| `CREDENCIALES` | `USUARIOS` | 1:1 | FK `NOT NULL` con `UNIQUE` | Modelo v4 (P-1), D-061 |
| `SESIONES` | `USUARIOS` / `DISPOSITIVOS` | N:1 / N:1 | FK `NOT NULL` | D-018, modelo v4 |
| `OPERACIONES` | `CUENTAS` (origen) / `CUENTAS` (destino) | N:1 / N:1, origen ≠ destino | FK `NOT NULL` | D-029, modelo v4 |
| `NOTIFICACIONES` | `USUARIOS` (destinatario) / `OPERACIONES` | N:1 / N:1 | FK `NOT NULL`; `UNIQUE (operacion_id, destinatario_id)` (confirmada, H-02) | Modelo v4 |
| `biometria.PERFILES_VOZ` | `USUARIOS` | 1:1 lógica | Sin FK física; `UNIQUE (usuario_id)` | D-013, D-051 (B-6, B-7) |

`SOLICITUDES_ATENCION` no tiene tabla física.

## 5. Claves primarias y foráneas

Cada tabla deberá tener una clave primaria claramente identificada.

Las claves foráneas deberán utilizarse cuando exista una relación de integridad entre entidades.

Antes de implementar el esquema definitivo se debe verificar:

1. unicidad de las claves primarias;
2. compatibilidad de tipos entre PK y FK;
3. obligatoriedad (`NULL/NOT NULL`);
4. restricciones `UNIQUE`;
5. restricciones `CHECK`;
6. comportamiento ante eliminación;
7. comportamiento ante actualización.

La definición final debe quedar registrada en una versión aprobada de este documento.

## 6. Integridad y consistencia

La base de datos debe preservar la integridad de la información mediante:

- claves primarias;
- claves foráneas;
- restricciones de unicidad;
- restricciones de obligatoriedad;
- validaciones de dominio;
- transacciones cuando una operación involucre múltiples modificaciones.

Las reglas de negocio que no correspondan al nivel de base de datos deberán mantenerse en el backend.

## 7. Seguridad de la información

La base de datos puede contener información personal, información de cuentas, sesiones, operaciones y registros de auditoría.

Por ello, la implementación deberá considerar:

- credenciales fuera del código fuente;
- control de acceso;
- protección de conexiones;
- mínimo privilegio;
- protección de información sensible;
- registro de operaciones relevantes;
- copias de seguridad según la estrategia definida.

Los detalles de seguridad deben documentarse en `06_SEGURIDAD_NAYRA.md`.

## 8. Información biométrica y datos de voz

La biometría de voz es un componente del proyecto, pero la estrategia de almacenamiento de audio, características biométricas o representaciones derivadas todavía debe definirse.

Por tanto:

- no asumir que los archivos de audio deben almacenarse directamente en PostgreSQL;
- no crear automáticamente una tabla de biometría;
- no almacenar representaciones biométricas sin definir previamente su propósito;
- definir los requisitos de seguridad y retención antes de implementar almacenamiento biométrico.

La estrategia definitiva debe documentarse en `05_BIOMETRIA_NAYRA.md` y `06_SEGURIDAD_NAYRA.md`.

_(AG-13: estrategia aprobada en D-013 — solo embedding cifrado, sin audio, en el esquema `biometria`; modelo lógico en §16.11.)_ _(Modelo de datos v4, 2026-09-27: `biometria.perfiles_voz` implementada; modelo físico en §17.11.)_

## 9. Relación con el backend

La implementación del backend deberá mantener correspondencia entre:

```text
Tabla
  ↓
Entidad / modelo
  ↓
Repository
  ↓
Service
  ↓
Controller / API
```

No todas las tablas requieren necesariamente exposición directa mediante un endpoint. La existencia de una tabla no implica que deba existir un CRUD público para ella.

## 10. Relación con los requisitos

Cada tabla debe poder justificarse mediante uno o más requisitos o necesidades técnicas documentadas.

La trazabilidad recomendada es:

```text
Historia de usuario
       ↓
Dato requerido
       ↓
Tabla / campo
       ↓
Entidad
       ↓
Lógica de negocio
       ↓
API
```

Si un campo no puede relacionarse con una necesidad del sistema, debe revisarse antes de incorporarlo al esquema definitivo.

## 11. Diferencia entre modelo propuesto y modelo aprobado

Las tablas de la sección `TABLAS CORE` representan la propuesta registrada por el equipo en el Excel de requerimientos.

Esto significa:

- sirven como punto de partida;
- deben conservarse como referencia;
- no deben modificarse arbitrariamente;
- tampoco deben considerarse automáticamente el esquema físico definitivo.

El modelo definitivo será el resultado de validar estas estructuras con los requisitos, la arquitectura, la seguridad y las necesidades de implementación.

## 12. Reglas para Claude

1. Utilizar `TABLAS CORE` como fuente inicial del modelo de datos.
2. No crear tablas nuevas sin justificar su necesidad.
3. No eliminar tablas existentes sin una decisión explícita.
4. No modificar nombres de campos sin documentar el cambio.
5. No inventar relaciones entre tablas.
6. No asumir que cada tabla necesita un endpoint CRUD.
7. No almacenar datos biométricos sin una estrategia aprobada.
8. Mantener consistencia entre PostgreSQL y las entidades del backend.
9. Antes de realizar cambios estructurales, explicar el impacto.
10. Mantener trazabilidad entre requisitos y estructuras de datos.
## 13. Decisiones pendientes

Antes de generar el esquema definitivo de PostgreSQL deben quedar resueltos, cuando correspondan:

- claves primarias definitivas;
- claves foráneas;
- cardinalidades;
- restricciones;
- índices;
- nombres definitivos;
- estrategia de identificadores;
- estrategia de auditoría;
- estrategia de sesiones;
- estrategia de almacenamiento biométrico;
- estrategia de retención de información;
- estrategia de copias de seguridad;
- estrategia de migraciones;
- versión de PostgreSQL;
- estrategia de despliegue de la base de datos.

Estas decisiones deben registrarse en `07_DECISIONES_TECNICAS_NAYRA.md` cuando constituyan decisiones tecnológicas.

## 14. Fuente de verdad

Para el desarrollo de la base de datos se utilizará la siguiente prioridad:

1. requisitos aprobados;
2. diseño de base de datos aprobado;
3. decisiones técnicas aprobadas;
4. código existente.

Las propuestas anteriores o estructuras creadas automáticamente por herramientas no deben considerarse fuente de verdad si contradicen la documentación aprobada.


## Contexto de las tablas financieras

Las tablas `CUENTAS` y `OPERACIONES` forman parte del modelo de datos del prototipo dentro de un **entorno bancario simulado**. No representan una conexión directa con cuentas bancarias reales ni implican una integración con sistemas core de entidades financieras.

Su finalidad es permitir que el prototipo pueda representar de manera controlada las cuentas y operaciones necesarias para probar el flujo de autenticación y las funcionalidades de la billetera digital.

Las transacciones almacenadas durante las pruebas serán **simuladas**, sin movimiento de fondos reales.

La tabla de referencia `ENTIDADES_BANCARIAS` forma parte del mismo entorno simulado: sus registros son entidades bancarias simuladas, no bancos reales.

## 15. Decisiones AG-00 sobre el modelo financiero (2026-09-25)

Decisiones registradas en `07_DECISIONES_TECNICAS_NAYRA.md`:

1. **Terminología (D-024):** `CUENTAS` representa la **cuenta financiera**. La **cuenta de acceso** del usuario a Nayra no es una fila de `CUENTAS`; sus datos y estado corresponden a `USUARIOS` (el estado de la cuenta de acceso queda pendiente para AG-05).
2. **Cuenta financiera única (D-025):** cada usuario tiene una sola cuenta financiera → restricción `UNIQUE` sobre la FK del propietario en `CUENTAS`.
3. **Entidad bancaria (D-026):** cada cuenta financiera pertenece a una entidad bancaria simulada → FK de `CUENTAS` a `ENTIDADES_BANCARIAS` (campos mínimos `id`, `nombre`).
4. **Nombre de tabla (D-027):** se mantiene `CUENTAS`; no se renombra.
5. **Asociación por DNI (D-028, alternativa A1):** el DNI se usa **durante el registro** para localizar la cuenta financiera simulada; la relación persistente es la FK con `UNIQUE`, no el valor del DNI.
6. **Operaciones (D-029):** `OPERACIONES` referencia directamente la cuenta financiera de origen y la de destino.
7. **Exclusiones (D-033):** no se modelan apertura de cuentas, múltiples cuentas por usuario, transferencias entre cuentas propias ni gestión de entidades por el administrador.

### Pendientes que afectan al modelo

- ~~**Dato con el que se localiza la cuenta por DNI.**~~ **Resuelto por D-035 (AG-01):** la cuenta financiera simulada referencia a su titular en `REGISTRO_IDENTIDAD_SIMULADO`, que contiene el DNI (ver §16).
- ~~**Obligatoriedad de la FK al propietario.**~~ **Resuelto por D-035 (AG-01):** las cuentas simuladas existen antes del registro; la FK a `USUARIOS` es opcional hasta la vinculación y única una vez establecida.
- **Destino de las operaciones de tipo PAGO** (HU-73): no está definido si un pago tiene cuenta financiera de destino. _Los pagos no forman parte del primer entregable (D-034)._
- **Cuenta financiera del administrador:** no está definido si un usuario con rol `ADMIN` tiene cuenta financiera. _(El personal de atención dejó de existir, D-041.)_
- **Estados:** estado CANCELADA en `OPERACIONES` (HU-87) y estados de la cuenta de acceso frente a los de la cuenta financiera (AG-05). _(Modelo v4, 2026-09-27: `OPERACIONES` usa `EXITOSO`/`FALLIDO`/`CANCELADO`; la cuenta de acceso usa `ACTIVO`/`BLOQUEADO`/`INACTIVO` y la cuenta financiera `ACTIVA`/`BLOQUEADA`/`CERRADA`.)_

## 16. Modelo lógico actualizado — AG-01 (2026-09-26)

Esta sección es la **referencia vigente** del modelo lógico para las tablas indicadas. Describe **atributos lógicos y reglas**, no el esquema físico: los tipos de dato, nombres definitivos de columnas, índices, obligatoriedad fina y comportamiento ante eliminación siguen pendientes (§13), así como la estrategia de identificadores y de migraciones (**D-051**). **No se crean tablas físicas** hasta cerrar esas decisiones. **No se crean tablas biométricas** hasta decidir D-013. _(AG-13: D-013 decidida; su modelo lógico está en §16.11, pero la tabla física se crea solo con la estrategia de migraciones, D-051.)_ _(2026-09-27: D-051 aprobada; el modelo físico de `USUARIOS`, `REGISTRO_IDENTIDAD_SIMULADO`, `ENTIDADES_BANCARIAS`, `CUENTAS`, `DISPOSITIVOS` y `AUDITORÍA` está en §17.)_ _(2026-09-27, modelo de datos v4: se implementaron además `CREDENCIALES`, `SESIONES`, `OPERACIONES`, `NOTIFICACIONES` y `biometria.PERFILES_VOZ`. Donde esta sección difiere de §17, rige §17.)_

### 16.1 `USUARIOS` (cuenta de acceso)

| Atributo lógico | Regla | Decisión |
|---|---|---|
| Identificador | PK | §13 |
| DNI | Obligatorio y **único**: un DNI no puede tener dos cuentas de acceso. _(Modelo v4: documento de identidad `DNI` o `CE`, único por tipo y número; validación local/simulada provisional, P-2)_ | D-036 |
| Nombres, apellidos | Obtenidos del registro de identidad simulado una vez que el representante autorizado confirma la validación de identidad | D-035, D-052 |
| Número de celular | Dato de contacto; sin validación OTP en el primer entregable | D-043 |
| Hash del PIN | PIN de 6 dígitos (D-061). Solo el hash (con *pepper* fuera de la BD); nunca texto plano ni audio. Algoritmo y parámetros pendientes. _(Modelo v4: se trasladó a `CREDENCIALES`, junto con el contador de intentos; ya no está en `USUARIOS`)_ | D-037, D-061, D-047 |
| Rol | `USER` o `ADMIN` (valor fijo, sin tabla `ROLES`, D-009) | D-041 |
| Estado de la cuenta de acceso | Al menos `ACTIVA` y `BLOQUEADA` (bloqueo por 3 intentos, por pérdida o por el administrador). Otros estados pendientes (AG-05). _(Modelo v4: `ACTIVO`, `BLOQUEADO`, `INACTIVO`; el bloqueo automático es por 3 PIN incorrectos)_ | D-040, D-044 |
| Fechas de creación/actualización | Pendiente de definir | — |

**No forman parte del modelo aprobado:** `username`, `email`, `direccion`, `fecha_nacimiento`, `foto_usuario`. Existen en el código actual (`Users.java`) sin respaldo en HU ni decisión.

**Referencia biométrica:** permanece asociada a la cuenta de acceso en el backend (D-038). **No** se agrega a `USUARIOS`: por D-013 vive en `biometria.PERFILES_VOZ` (§16.11), gestionada solo por el servicio Python.

### 16.2 `ROLES`

Catálogo con dos valores: `USER` y `ADMIN` (D-041). Si un usuario puede tener uno o varios roles, y la representación exacta (catálogo con FK o valor fijo), quedan pendientes dentro de D-009. El rol **nunca** lo elige el cliente al registrarse.

_(2026-09-27: D-009 aprobada, opción A. El rol es un valor fijo en `usuarios.rol`; **no** existe tabla `ROLES` y un usuario tiene un solo rol. Ver §17.)_

### 16.3 `REGISTRO_IDENTIDAD_SIMULADO` (nueva — entorno simulado)

Fuente de identidad simulada (D-035). Datos de referencia del entorno controlado; no es una API real ni representa datos de personas reales.

| Atributo lógico | Regla |
|---|---|
| Identificador | PK |
| DNI | Único _(modelo v4: tipo `DNI`/`CE` + número, único por tipo y número)_ |
| Nombres | — |
| Apellidos | — |

No se gestiona desde el panel administrativo (no hay HU). El nombre de la tabla es provisional hasta definir nombres definitivos (§13).

### 16.4 `CUENTAS` (cuenta financiera simulada)

Mantiene lo aprobado en AG-00 (D-025 a D-029) y agrega:

| Atributo lógico | Regla | Decisión |
|---|---|---|
| Titular simulado | FK a `REGISTRO_IDENTIDAD_SIMULADO`, único (una cuenta por titular) | D-025, D-035 |
| Propietario (usuario de Nayra) | FK a `USUARIOS`, **opcional hasta la vinculación** y **única** una vez vinculada | D-028, D-035 |
| Entidad bancaria | FK a `ENTIDADES_BANCARIAS` | D-026 |
| Identificador para QR | **Pendiente** (ver 16.10). _(Modelo v4: `codigo_qr` fijo y único, valor aleatorio PROVISIONAL; formato P-3)_ | D-042 |
| Moneda | _(Modelo v4: `PEN`)_ | Modelo v4 |

Localización durante el registro: DNI → `REGISTRO_IDENTIDAD_SIMULADO` → cuenta cuyo titular es ese registro → vinculación con `USUARIOS`.

### 16.5 `DISPOSITIVOS`

| Atributo lógico | Regla | Decisión |
|---|---|---|
| Propietario | FK a `USUARIOS` | D-039 |
| Identificación técnica del dispositivo | Identificador del dispositivo + **clave pública** del par de claves generado en el almacén de hardware del teléfono y su algoritmo (ECDSA P-256). La clave privada **nunca** se almacena en el backend | D-048 |
| Plataforma | Según propuesta original (ANDROID / IOS). _(Modelo v4: solo `ANDROID`; iOS fuera de alcance)_ | Excel, modelo v4 |
| Estado | Al menos `ACTIVO` y `REVOCADO` | D-039, D-040 |
| Fechas de vinculación y revocación | Para trazabilidad | D-040 |

Regla de integridad: **como máximo un dispositivo `ACTIVO` por usuario**. Al autorizar un nuevo dispositivo, el anterior pasa a `REVOCADO`.

### 16.6 `SESIONES`

| Atributo lógico | Regla | Decisión |
|---|---|---|
| Usuario | FK a `USUARIOS` | D-037 |
| Dispositivo | FK a `DISPOSITIVOS` | D-039 |
| Creación / expiración / revocación | La sesión se crea solo tras contraseña + desafío + anti-spoofing + verificación 1:1. Se revoca al bloquear la cuenta o revocar el dispositivo | D-037, D-040 |

| Último acceso | Necesario para cerrar la sesión tras **5 minutos de inactividad**, controlado en el servidor | D-018 (parcial) |

Duración máxima absoluta, renovación, formato de sesión o token (incluido si se usa JWT) siguen **pendientes** (D-018). _(Modelo v4, 2026-09-27: JWT con `jti` = `sesiones.id`; el token no se guarda; sin renovación, refresh token ni duración máxima absoluta; `exp`, claims, algoritmo, custodia de clave y máximo de sesiones pendientes, P-5. Ver §17.8.)_

**Desafíos y nonces (D-048, D-054):** son de un solo uso y vida corta. Dónde se guardan (tabla, caché o memoria compartida entre las 2 instancias de D-023) queda **pendiente**; no se crea tabla para ellos todavía. _(Modelo v4: viven en la memoria del servicio de autenticación, sin tabla; un almacenamiento compartido entre instancias se evaluará más adelante.)_

### 16.7 `OPERACIONES`

Mantiene lo aprobado en AG-00 (origen y destino como FK a `CUENTAS`). En el primer entregable el tipo utilizado es **TRANSFERENCIA directa** entre usuarios de Nayra (D-042). La transferencia mediante QR corresponde a un siguiente entregable; registrar el canal (directa/QR) es opcional y queda pendiente. El administrador **no** puede crear, modificar ni eliminar operaciones (D-041). ~~El estado CANCELADA (HU-87) sigue pendiente.~~ _(Resuelto por el modelo v4: existe `CANCELADO`; no hay estado pendiente.)_ _(Modelo v4, 2026-09-27: estados `EXITOSO`/`FALLIDO`/`CANCELADO`; destino obligatorio y distinto del origen; moneda `PEN`; canal `MOVIL`; código de referencia de 6 dígitos único; monto mayor que 0 y menor que 500. El destinatario se identifica por el ID interno de la cuenta destino (G-1, cerrada). Ver §17.9.)_

### 16.8 `SOLICITUDES_ATENCION`

Representa la atención sin chatbot (D-040):

| Atributo lógico | Regla | Decisión |
|---|---|---|
| Solicitante | FK a `USUARIOS` | Excel |
| Tipo | Además de CONSULTA, RECLAMO, INCIDENTE: **PERDIDA_DISPOSITIVO** y **RECUPERACION_ACCESO** | D-040 |
| Estado de la solicitud | Pendiente de definir sus valores | — |
| Administrador que atiende | FK a `USUARIOS` (rol `ADMIN`), opcional | D-041 |
| Fechas de creación y resolución | Para trazabilidad | — |

El cambio de dispositivo normal (contraseña + voz) no requiere solicitud; solo la recuperación asistida y la pérdida del celular.

### 16.9 `AUDITORÍA`

Además de los campos originales, para cumplir D-041:

| Atributo lógico | Regla |
|---|---|
| Actor | Usuario que ejecuta la acción (usuario o administrador) |
| Usuario afectado | Usuario sobre el que recae la acción, cuando difiere del actor |
| Tipo de acción, resultado, fecha, IP, dispositivo | Según propuesta original |

Eventos mínimos a registrar (revisión del 2026-09-27: los intentos de autenticación registran el **motivo** del fallo — PIN incorrecto, calidad insuficiente, contenido incorrecto, posible spoofing, no coincide, servicio no disponible — sin PIN, audio, embeddings ni puntajes biométricos; esto sirve a HU-80 y, luego, a HU-51/HU-52): registro, validación asistida de identidad por el representante autorizado (D-052; qué datos del representante se registran queda pendiente, sin crear un rol nuevo), rechazo por DNI existente, intentos de autenticación (con tipo de fallo, sin contraseña, audio ni representación biométrica), bloqueos y desbloqueos, vinculación y revocación de dispositivos, solicitudes y su atención, y toda acción administrativa. El catálogo definitivo de eventos y campos sigue pendiente (D-019).

### 16.10 Identificador del QR (pendiente — siguiente entregable)

El QR no forma parte de la implementación del primer entregable (D-042, AG-01 v5); esta decisión no bloquea el desarrollo actual.

El QR solo debe identificar la cuenta/destinatario dentro del entorno simulado (D-042). Alternativas a decidir:

- reutilizar el identificador de la cuenta ya existente en `CUENTAS`;
- un identificador público no secuencial propio del QR.

La segunda evita exponer identificadores internos o datos financieros innecesarios. No se agrega el campo hasta decidirlo.

_(Modelo v4, 2026-09-27: se agregó `cuentas.codigo_qr`, fijo, único y sin datos personales, con un valor aleatorio **PROVISIONAL**. El formato definitivo sigue **pendiente (P-3)** y las funciones de QR siguen fuera del primer entregable.)_

### 16.11 `biometria.PERFILES_VOZ` (nueva — AG-13, D-013)

Referencia biométrica de voz. Vive en un **esquema propio (`biometria`) del mismo PostgreSQL**, con usuario de base de datos exclusivo del servicio Python. Spring Boot y el administrador **no** acceden a ella. Modelo lógico (tipos físicos y nombres definitivos pendientes, §13 y D-051):

| Atributo lógico | Regla | Decisión |
|---|---|---|
| Identificador | PK | §13 |
| Usuario | Referencia lógica al identificador de `USUARIOS`; **único** (un perfil por cuenta de acceso). Si será FK física entre esquemas queda pendiente (D-051). _(2026-09-27: sin FK física por ahora; referencia lógica, D-051)_ | D-038, D-013 |
| Embedding cifrado | Centroide normalizado de 192 valores, cifrado con AES-256-GCM | D-011, D-013 |
| Vector de inicialización | Propio de cada cifrado | D-013 |
| Modelo | Nombre del modelo (p. ej. `speechbrain/spkrec-ecapa-voxceleb`) | D-011, D-013 |
| Versión del modelo | Si cambia, la referencia no es comparable y se requiere re-enrolamiento | D-013 |
| Número de muestras | Muestras válidas usadas en el centroide | D-059 |
| Estado | Al menos `ACTIVO` y `REVOCADO` | D-013 |
| Fechas de creación y actualización | Trazabilidad | — |

_(Modelo v4, 2026-09-27: tabla implementada; modelo físico en §17.11, con B-1 a B-13.)_

Reglas: **no se guarda audio** en ninguna tabla; la clave de cifrado vive fuera del código y de la base de datos (D-017); eliminación física según HU-36 con evento de auditoría sin el embedding; actualización solo por re-enrolamiento tras validar al titular (D-040).

## 17. Modelo físico — D-051, D-009 y modelo de datos v4 (2026-09-27)

Modelo físico **implementado** en el repositorio (commits `5bbd505` y `ca98bb3` de `yuniv`, sin push). Decisiones en `07_DECISIONES_TECNICAS_NAYRA.md` (D-051, D-009 y «Decisiones del modelo de datos v4»). Migraciones:

- esquema `nayra`: `Nayra-Back/src/main/resources/db/migration/nayra/V001` a `V011` (V001–V004: modelo físico D-051; V005–V011: modelo v4);
- esquema `biometria`: `Nayra-Voz/migraciones/biometria/V001` a `V003`, con historial propio.

**Reglas comunes:** nombres en `snake_case` sin tildes; PK `id uuid` (UUID v4 generado por la aplicación); FK con `ON DELETE RESTRICT ON UPDATE RESTRICT`; fechas `timestamptz`; estados como `varchar` con `CHECK`. Hibernate usa `ddl-auto=validate`.

**Propiedad por servicio (separación lógica del modelo v4; no sustituye a las 2 instancias de D-023):**

| Servicio | Tablas |
|---|---|
| Negocio | `usuarios`, `registro_identidad_simulado`, `entidades_bancarias`, `cuentas`, `operaciones`, `notificaciones` |
| Autenticación | `credenciales`, `dispositivos`, `sesiones` (desafíos y nonces en memoria, sin tabla) |
| Ambos (solo inserción) | `auditoria` |
| Biométrico (Python) | `biometria.perfiles_voz` |

Hoy Negocio y Autenticación son una sola aplicación Spring Boot. V011 prepara usuarios de base de datos por servicio (`nayra_negocio`, `nayra_autenticacion`) mediante placeholders; qué servicio ejecuta Flyway, los permisos definitivos y el orden de despliegue siguen **PENDIENTES (P-4)**.

**Reglas confirmadas (H-02, cerrada el 2026-09-27):** las seis reglas del modelo v4 que estaban marcadas "(a confirmar)" quedan **confirmadas formalmente**, tal como están aplicadas físicamente en la base de datos: CHECK `intentos_fallidos BETWEEN 0 AND 3` (§17.4), `DEFAULT 'PEN'` (§17.5, §17.9), `CHECK (fecha_ultimo_acceso >= fecha_creacion)` (§17.8), `CHECK (leida = (fecha_lectura IS NOT NULL))` y `UNIQUE (operacion_id, destinatario_id)` (§17.10), e índices de `sesiones` y `operaciones` (§17.12).

### 17.1 `nayra.entidades_bancarias` (`ENTIDADES_BANCARIAS`)

| Columna | Tipo | Nulo | Restricción |
|---|---|---|---|
| `id` | `uuid` | NOT NULL | PK |
| `nombre` | `varchar(100)` | NOT NULL | — (sin UNIQUE) |

### 17.2 `nayra.registro_identidad_simulado` (`REGISTRO_IDENTIDAD_SIMULADO`)

| Columna | Tipo | Nulo | Restricción |
|---|---|---|---|
| `id` | `uuid` | NOT NULL | PK |
| `tipo_documento_identidad` | `varchar(20)` | NOT NULL | `CHECK (tipo_documento_identidad IN ('DNI','CE'))` (V005) |
| `numero_documento` | `varchar(30)` | NOT NULL | `uq_registro_identidad_simulado_documento (tipo_documento_identidad, numero_documento)` (V005; reemplaza a `dni`) |
| `nombres` | `varchar(100)` | NOT NULL | — |
| `apellidos` | `varchar(100)` | NOT NULL | — |

### 17.3 `nayra.usuarios` (`USUARIOS`)

| Columna | Tipo | Nulo | Restricción |
|---|---|---|---|
| `id` | `uuid` | NOT NULL | PK |
| `tipo_documento_identidad` | `varchar(20)` | NOT NULL | `CHECK (tipo_documento_identidad IN ('DNI','CE'))` |
| `numero_documento` | `varchar(30)` | NOT NULL | `uq_usuarios_documento (tipo_documento_identidad, numero_documento)`. Formato por tipo PROVISIONAL (P-2) |
| `nombres` | `varchar(100)` | NOT NULL | — |
| `apellidos` | `varchar(100)` | NOT NULL | — |
| `numero_celular` | `varchar(20)` | NOT NULL | — |
| `rol` | `varchar(10)` | NOT NULL | `CHECK (rol IN ('USER','ADMIN'))` (D-009) |
| `estado` | `varchar(20)` | NOT NULL, `DEFAULT 'ACTIVO'` | `CHECK (estado IN ('ACTIVO','BLOQUEADO','INACTIVO'))` (V005) |
| `fecha_creacion` | `timestamptz` | NOT NULL | — |
| `fecha_actualizacion` | `timestamptz` | NOT NULL | — |

`pin_hash` e `intentos_fallidos` **ya no están en `usuarios`**: V006 los copió a `credenciales`, verificó la copia y eliminó las columnas. Sin `username`, `email`, `direccion` ni otros atributos no documentados. La credencial de usuario y contraseña del administrador (D-050, aprobada funcionalmente) **no tiene modelo todavía**: su almacenamiento sigue PENDIENTE.

### 17.4 `nayra.credenciales` (servicio de autenticación, V006)

| Columna | Tipo | Nulo | Restricción |
|---|---|---|---|
| `id` | `uuid` | NOT NULL | PK |
| `usuario_id` | `uuid` | NOT NULL | FK → `usuarios.id`; `uq_credenciales_usuario_id` (1:1) |
| `pin_hash` | `text` | NOT NULL | Hash autodescriptivo del PIN de 6 dígitos (D-061); nunca el PIN. Algoritmo y *pepper*: D-047 (PENDIENTE; BCrypt provisional) |
| `intentos_fallidos` | `smallint` | NOT NULL, `DEFAULT 0` | `CHECK (intentos_fallidos BETWEEN 0 AND 3)` (confirmada, H-02) |
| `fecha_creacion` | `timestamptz` | NOT NULL | — |
| `fecha_actualizacion` | `timestamptz` | NOT NULL | — |

`pin_hash` no sale del servicio de autenticación ni se expone en ninguna API. Solo el PIN incorrecto incrementa `intentos_fallidos` (D-044); al tercero la cuenta pasa a `BLOQUEADO` y se revocan sus sesiones. El incremento es atómico (`UPDATE … SET intentos_fallidos = intentos_fallidos + 1 … WHERE intentos_fallidos < 3`, corrección E-01). Un PIN correcto devuelve `intentos_fallidos` a 0 de inmediato, antes del paso de voz (H-01, cerrada; coincide con el código).

### 17.5 `nayra.cuentas` (`CUENTAS`)

| Columna | Tipo | Nulo | Restricción |
|---|---|---|---|
| `id` | `uuid` | NOT NULL | PK. Es el **ID interno de la cuenta destino** que usa el backend para las transferencias (G-1, cerrada) |
| `titular_id` | `uuid` | NOT NULL | FK → `registro_identidad_simulado.id`; `uq_cuentas_titular_id` |
| `propietario_id` | `uuid` | NULL | FK → `usuarios.id`; `uq_cuentas_propietario_id` (admite varios NULL) |
| `entidad_bancaria_id` | `uuid` | NOT NULL | FK → `entidades_bancarias.id` |
| `codigo_cuenta` | `varchar(30)` | NOT NULL | `uq_cuentas_codigo_cuenta` |
| `codigo_qr` | `varchar(64)` | NOT NULL | `uq_cuentas_codigo_qr` (V007). Identificador fijo sin datos personales; valor aleatorio **PROVISIONAL**; formato definitivo PENDIENTE (P-3) |
| `saldo` | `numeric(15,2)` | NOT NULL | `CHECK (saldo >= 0)` |
| `moneda` | `char(3)` | NOT NULL, `DEFAULT 'PEN'` (confirmada, H-02) | `CHECK (moneda = 'PEN')` (V007) |
| `estado` | `varchar(20)` | NOT NULL, `DEFAULT 'ACTIVA'` | `CHECK (estado IN ('ACTIVA','BLOQUEADA','CERRADA'))` |
| `fecha_creacion` | `timestamptz` | NOT NULL | — |
| `fecha_actualizacion` | `timestamptz` | NOT NULL | — |

Los estados `ACTIVA`/`BLOQUEADA`/`CERRADA` son de la **cuenta financiera**; los de la cuenta de acceso (`ACTIVO`/`BLOQUEADO`/`INACTIVO`) están en `usuarios`.

### 17.6 `nayra.dispositivos` (`DISPOSITIVOS`)

| Columna | Tipo | Nulo | Restricción |
|---|---|---|---|
| `id` | `uuid` | NOT NULL | PK. También es el identificador que la app firma junto al nonce (D-048) |
| `usuario_id` | `uuid` | NOT NULL | FK → `usuarios.id` |
| `clave_publica` | `bytea` | NOT NULL | X.509 SubjectPublicKeyInfo (DER) |
| `algoritmo_clave` | `varchar(30)` | NOT NULL, `DEFAULT 'ECDSA_P256_SHA256'` | `CHECK (algoritmo_clave = 'ECDSA_P256_SHA256')` |
| `plataforma` | `varchar(10)` | NOT NULL | `CHECK (plataforma IN ('ANDROID'))` (V008: solo Android; iOS fuera de alcance) |
| `estado` | `varchar(20)` | NOT NULL, `DEFAULT 'ACTIVO'` | `CHECK (estado IN ('ACTIVO','REVOCADO'))` |
| `fecha_vinculacion` | `timestamptz` | NOT NULL | — |
| `fecha_revocacion` | `timestamptz` | NULL | — |

Un solo dispositivo `ACTIVO` por usuario: índice único parcial `uq_dispositivos_usuario_activo (usuario_id) WHERE estado = 'ACTIVO'`. No se crea el campo VARCHAR(255) de identificador de hardware del Excel (§3.6) ni se identifica el teléfono por su hardware.

### 17.7 `nayra.auditoria` (`AUDITORÍA`)

| Columna | Tipo | Nulo | Restricción |
|---|---|---|---|
| `id` | `uuid` | NOT NULL | PK |
| `fecha` | `timestamptz` | NOT NULL | — |
| `actor_id` | `uuid` | NULL | FK → `usuarios.id` |
| `usuario_afectado_id` | `uuid` | NULL | FK → `usuarios.id` |
| `accion` | `varchar(50)` | NOT NULL | Catálogo pendiente (D-019) |
| `resultado` | `varchar(10)` | NOT NULL | `CHECK (resultado IN ('EXITOSO','FALLIDO'))` |
| `motivo` | `varchar(50)` | NULL | Código, nunca texto libre |
| `ip` | `inet` | NULL | La aplicación todavía no la captura |
| `dispositivo_id` | `uuid` | NULL | FK → `dispositivos.id` |

No se crean `detalle` ni `usuario_relacionado`. Solo inserción: el usuario de ejecución no tiene `UPDATE` ni `DELETE` (V004). El modelo v4 no cambia esta tabla y deja fuera la auditoría avanzada (catálogos, IP, reglas nuevas); las consultas administrativas de auditoría existentes se mantienen dentro del alcance de `01` §12.4 (H-03, cerrada).

### 17.8 `nayra.sesiones` (servicio de autenticación, V009)

| Columna | Tipo | Nulo | Restricción |
|---|---|---|---|
| `id` | `uuid` | NOT NULL | PK. **Igual al claim `jti` del JWT**; el JWT no se guarda |
| `usuario_id` | `uuid` | NOT NULL | FK → `usuarios.id` |
| `dispositivo_id` | `uuid` | NOT NULL | FK → `dispositivos.id` |
| `fecha_creacion` | `timestamptz` | NOT NULL | — |
| `fecha_ultimo_acceso` | `timestamptz` | NOT NULL | `CHECK (fecha_ultimo_acceso >= fecha_creacion)` (confirmada, H-02) |
| `fecha_revocacion` | `timestamptz` | NULL | — |

Seis columnas: no guarda el token, su hash, `exp` ni motivo. La sesión es válida si no está revocada y su último acceso está dentro de los **5 minutos de inactividad**; no hay renovación, refresh token ni duración máxima absoluta. Revocación y registro de acceso con UPDATE condicionales: una sesión revocada no puede reabrirse por una escritura obsoleta y la fecha de último acceso nunca retrocede (corrección E-02; sin `@Version`, que exigiría una séptima columna). `exp`, claims, algoritmo, custodia de la clave y máximo de sesiones: PENDIENTES (P-5).

### 17.9 `nayra.operaciones` (V010)

| Columna | Tipo | Nulo | Restricción |
|---|---|---|---|
| `id` | `uuid` | NOT NULL | PK |
| `cuenta_origen_id` | `uuid` | NOT NULL | FK → `cuentas.id` |
| `cuenta_destino_id` | `uuid` | NOT NULL | FK → `cuentas.id`; `CHECK (cuenta_origen_id <> cuenta_destino_id)` |
| `tipo` | `varchar(30)` | NOT NULL | `CHECK (tipo IN ('TRANSFERENCIA'))` |
| `monto` | `numeric(15,2)` | NOT NULL | `CHECK (monto > 0 AND monto < 500)` |
| `moneda` | `char(3)` | NOT NULL, `DEFAULT 'PEN'` (confirmada, H-02) | `CHECK (moneda = 'PEN')` |
| `detalle` | `varchar(255)` | NULL | — |
| `codigo_referencia` | `char(6)` | NOT NULL | `uq_operaciones_codigo_referencia`; `CHECK (codigo_referencia ~ '^[0-9]{6}$')` |
| `estado` | `varchar(20)` | NOT NULL | `CHECK (estado IN ('EXITOSO','FALLIDO','CANCELADO'))` |
| `canal` | `varchar(10)` | NOT NULL, `DEFAULT 'MOVIL'` | `CHECK (canal = 'MOVIL')` |
| `fecha` | `timestamptz` | NOT NULL | — |
| `fecha_actualizacion` | `timestamptz` | NOT NULL | — |

Transferencias **simuladas**. **Monto (corrección E-03):** la entidad valida el `BigDecimal` antes de persistir: escala ≤ 2 sin redondeo (`MONTO_ESCALA_INVALIDA`), precisión ≤ 15 y 0 < monto < 500 (`MONTO_FUERA_DE_RANGO`); no se depende del redondeo implícito de `numeric(15,2)`. El destinatario se identifica por el ID interno de la cuenta destino (G-1). Las transferencias todavía **no tienen endpoint** (D-014). Nota: el comentario SQL de la tabla en V010 todavía dice "G-1 PENDIENTE BLOQUEANTE"; quedó desactualizado y no se edita una migración ya aplicada.

### 17.10 `nayra.notificaciones` (V010)

| Columna | Tipo | Nulo | Restricción |
|---|---|---|---|
| `id` | `uuid` | NOT NULL | PK |
| `destinatario_id` | `uuid` | NOT NULL | FK → `usuarios.id` |
| `operacion_id` | `uuid` | NOT NULL | FK → `operaciones.id`; `UNIQUE (operacion_id, destinatario_id)` (confirmada, H-02) |
| `tipo` | `varchar(30)` | NOT NULL | `CHECK (tipo IN ('OPERACION'))` |
| `titulo` | `varchar(150)` | NOT NULL | — |
| `contenido` | `text` | NOT NULL | Texto generado, p. ej. "Juan Per... te realizó una transferencia de S/ 150.00." |
| `leida` | `boolean` | NOT NULL, `DEFAULT false` | `CHECK (leida = (fecha_lectura IS NOT NULL))` (confirmada, H-02) |
| `fecha_generacion` | `timestamptz` | NOT NULL | — |
| `fecha_lectura` | `timestamptz` | NULL | — |

Los tipos `SEGURIDAD` y `SISTEMA` del Excel (§3.7) quedan fuera del primer modelo.

### 17.11 `biometria.perfiles_voz` (servicio biométrico, `biometria` V002)

| Columna | Tipo | Nulo | Restricción |
|---|---|---|---|
| `id` | `uuid` | NOT NULL | PK |
| `usuario_id` | `uuid` | NOT NULL | `uq_perfiles_voz_usuario` (**un perfil por usuario**, B-7). Referencia lógica a `nayra.usuarios.id`, **sin FK física** (B-6, D-051) |
| `embedding_cifrado` | `bytea` | NOT NULL | Cifrado AES-GCM + etiqueta (B-1, B-3). CHECK de 784 bytes PENDIENTE hasta ejecutar ECAPA real |
| `iv` | `bytea` | NOT NULL | `CHECK (octet_length(iv) = 12)`; nuevo en cada cifrado (B-2) |
| `clave_version` | `smallint` | NOT NULL | `CHECK (clave_version > 0)`. La clave nunca está en PostgreSQL, en el código ni en el repositorio (B-5) |
| `modelo` | `varchar(100)` | NOT NULL | B-9 |
| `version_modelo` | `varchar(64)` | NOT NULL | B-9 |
| `numero_muestras` | `smallint` | NOT NULL | `CHECK (numero_muestras > 0)`; máximo PENDIENTE (D-059) |
| `estado` | `varchar(10)` | NOT NULL, `DEFAULT 'ACTIVO'` | `CHECK (estado IN ('ACTIVO','REVOCADO'))` (B-8) |
| `fecha_creacion` | `timestamptz` | NOT NULL | — |
| `fecha_actualizacion` | `timestamptz` | NOT NULL | — |
| `fecha_revocacion` | `timestamptz` | NULL | Coherente con el estado (B-11) |

**Sin audio** en ninguna tabla (lo verifica `PersistenciaPostgresTest.ningunaTablaGuardaAudio`). Volver a enrolar borra el perfil anterior e inserta uno nuevo (B-13); no hay historial de embeddings. AAD del cifrado: PROVISIONAL, definitiva PENDIENTE (B-4). Frecuencia de rotación de claves PENDIENTE (B-5). El servicio Python persiste el perfil con psycopg cuando existe `NAYRA_VOZ_BD`; sin esa variable lo guarda en memoria (solo desarrollo). Fuera de las pruebas, ningún proceso ejecuta todavía las migraciones de `biometria` (P-4).

### 17.12 Índices

`uq_usuarios_documento`, `uq_registro_identidad_simulado_documento`, `uq_credenciales_usuario_id`, `uq_cuentas_titular_id`, `uq_cuentas_propietario_id`, `uq_cuentas_codigo_cuenta`, `uq_cuentas_codigo_qr`, `uq_dispositivos_usuario_activo` (parcial), `ix_dispositivos_usuario_id`, `ix_cuentas_entidad_bancaria_id`, `ix_auditoria_fecha` (descendente), `ix_auditoria_actor_id`, `ix_auditoria_usuario_afectado_id`, `ix_sesiones_usuario_id` e `ix_sesiones_dispositivo_id` (confirmada, H-02), `uq_operaciones_codigo_referencia`, `ix_operaciones_cuenta_origen_id` e `ix_operaciones_cuenta_destino_id` (confirmada, H-02), `uq_notificaciones_operacion_destinatario` y `biometria.uq_perfiles_voz_usuario`.

### 17.13 Pendientes que afectan al modelo físico

- **Auditoría del registro asistido (D-052):** la FK `usuario_afectado_id` no admite el identificador de una cuenta que todavía no existe. Mientras no se decida, los eventos anteriores a la creación de la cuenta se registran **sin usuario afectado** (PROVISIONAL).
- **P-3:** formato definitivo de `codigo_qr`. **P-4:** ejecución de Flyway, permisos y orden de despliegue. **P-5:** `exp`, claims, algoritmo, custodia de clave y máximo de sesiones.
- **B-4** (AAD), **B-5** (rotación), **D-059** (rango de `numero_muestras`) y CHECK de 784 bytes de `embedding_cifrado`.
- **D-050:** modelo y almacenamiento de la credencial administrativa (usuario y contraseña).
- **P-11:** si el desbloqueo administrativo reinicia `intentos_fallidos`.
- Sin tabla, por decisión: `roles`, `solicitudes_atencion`, desafíos y nonces (en memoria del servicio de autenticación), historial de embeddings y tablas nuevas de auditoría.
