# 03_BASE_DE_DATOS.md — Modelo de datos de Nayra

> **Fuente principal:** hoja `TABLAS CORE` del archivo `REQUERIMIENTOS(6).xlsx`.
>
> Este documento registra las estructuras de datos que el equipo había planteado para el proyecto. Se diferencian las estructuras propuestas de las decisiones definitivas de implementación. No se agregan tablas ajenas a la fuente sin una decisión posterior.

## 1. Propósito

Definir y documentar el modelo de datos utilizado por Nayra, manteniendo trazabilidad con los requisitos y evitando que la implementación genere estructuras que no estén justificadas.

## 2. Motor de base de datos

La tecnología considerada para la base de datos relacional del proyecto es **PostgreSQL**. La versión concreta y la configuración de despliegue deberán registrarse en `07_DECISIONES_TECNICAS.md`.

## 3. Tablas CORE

Las siguientes tablas corresponden a la propuesta registrada en la hoja `TABLAS CORE` del Excel de requerimientos.

### 3.1 ROLES

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

| Campo | Tipo | Descripción |
|---|---|---|
| Tipo | Descripción |  |
| UUID / BIGINT | PK |  |
| FK | Propietario |  |
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
| FK | Cuenta que realiza la operación |  |
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

Los detalles de seguridad deben documentarse en `06_SEGURIDAD.md`.

## 8. Información biométrica y datos de voz

La biometría de voz es un componente del proyecto, pero la estrategia de almacenamiento de audio, características biométricas o representaciones derivadas todavía debe definirse.

Por tanto:

- no asumir que los archivos de audio deben almacenarse directamente en PostgreSQL;
- no crear automáticamente una tabla de biometría;
- no almacenar representaciones biométricas sin definir previamente su propósito;
- definir los requisitos de seguridad y retención antes de implementar almacenamiento biométrico.

La estrategia definitiva debe documentarse en `05_BIOMETRIA.md` y `06_SEGURIDAD.md`.

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

Estas decisiones deben registrarse en `07_DECISIONES_TECNICAS.md` cuando constituyan decisiones tecnológicas.

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
