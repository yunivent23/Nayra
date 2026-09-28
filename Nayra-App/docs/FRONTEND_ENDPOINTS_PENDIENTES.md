# Endpoints que la app necesita y Nayra-Back todavía no expone

Estado al 2026-09-28. La app **no** usa datos ficticios en su lugar: `BilleteraApiPendiente`
(`lib/api/billetera_api.dart`) lanza `FuncionNoDisponible` y cada pantalla informa
"Esta función todavía no está disponible en Nayra.". Cuando exista el endpoint, basta con implementar
`BilleteraApi` sobre `ClienteHttp` respetando el contrato real y registrarla en `Dependencias`.

Ninguna ruta, campo ni código de error de esta lista está decidido: la app no los inventa.
Todos deben usar sesión JWT (`Authorization: Bearer`) y el formato de error `{"error":"CODIGO"}`.

| Función | Historia | Qué debe proveer (según docs) | Uso en la app |
|---|---|---|---|
| Consultar saldo | HU-66 | Saldo disponible de la cuenta propia en PEN, con 2 decimales | `BilleteraApi.saldo()` → `PantallaSaldo` |
| Consultar movimientos | HU-67 | Lista de operaciones propias: fecha, monto, sentido, contraparte, estado (EXITOSO/FALLIDO/CANCELADO), código de referencia de 6 dígitos | `movimientos()` → `PantallaMovimientos` (uno en uno) |
| Transferir | HU-69 a HU-72 | Envío de monto > 0 y < 500 PEN, 2 decimales, al destinatario ya localizado por celular; errores MONTO_FUERA_DE_RANGO, MONTO_ESCALA_INVALIDA, CUENTAS_IGUALES; resultado con estado y código de referencia. **Pendiente:** qué dato identifica al destinatario en la solicitud (el celular otra vez o una referencia de la búsqueda) | `transferir(destinatario, monto)` → C7/C8 |
| Avisos / notificaciones | HU-77 | Lista de avisos propios (fecha, texto, leído) | `avisos()` → `PantallaAvisos` |
| Marcar aviso como leído | HU-77 | Marcar un aviso | No implementado en la app (sin contrato) |
| Dictar PIN en el registro | D-061 | Equivalente de `/transacciones/{id}/pin-dictado` para el registro | El teclado del registro solo permite teclear |
| Autenticación por voz antes de operar | HU-46 | Paso de verificación de voz ligado a una operación | No implementado |
| Dictado del celular del destinatario y respuesta hablada Sí/No | G-1, D-046 | Reconocimiento del número dictado y de "sí"/"no" en Nayra-Voz | Hoy: teclado grande y dos botones |

`IOperacionesService` e `INotificacionesService` existen vacíos en Nayra-Back; la regla del monto
(`ReglaMonto` en `lib/modelos/billetera.dart`) replica la validación del modelo v4 §2.8 solo como
ayuda en pantalla: el backend sigue siendo quien decide.

## Endpoint ya integrado: búsqueda del destinatario (G-1, 2026-09-28)

`POST /api/v1/destinatarios/busqueda` (sesión JWT) con `{"celular": "912345678"}` → `{"nombreVisible": "María Sala..."}` (o "María De la...", "María Pérez"; el backend genera el nombre parcial).
Errores: 400 `CELULAR_INVALIDO`, 404 `DESTINATARIO_NO_ENCONTRADO` (también para una cuenta bloqueada, inactiva o sin
cuenta financiera activa), 409 `CUENTAS_IGUALES` (número propio). La app lo usa en `DestinatarioApi`
(`lib/api/destinatario_api.dart`). No devuelve el ID interno de la cuenta ni ningún otro dato. La ruta es provisional
hasta `docs/04_API.md` (D-014). La agenda del teléfono sigue fuera del primer entregable (D-042).

## Decisiones abiertas que afectan a estos endpoints

- **Registro asistido (A2/A3):** el mockup v3 los pone en el teléfono de la persona, pero
  `POST /api/v1/admin/registros` y `/validacion-identidad` exigen sesión ADMIN. La app los ofrece como
  "Registro asistido" en el menú del ADMIN y entrega el código de registro que la persona escribe en su
  teléfono. Falta decidir el flujo definitivo.
- **"No coincide" en la validación asistida:** no hay endpoint para rechazar; la app solo cancela en local.
- **Rutas `/prototipo/...`** (autenticación y enrolamiento de voz): provisionales (D-014). La app las
  aísla en `AutenticacionApi` y `RegistroApi` para cambiarlas en un solo lugar.
