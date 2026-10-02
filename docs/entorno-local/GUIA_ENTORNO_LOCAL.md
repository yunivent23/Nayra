# Guía del entorno local de NAYRA

**Tipo:** configuración exclusiva del entorno local. Esta guía **no** forma parte de la arquitectura objetivo en GCP (D-062 a D-074) y no la modifica.
**Fecha:** 2026-10-01.
**Objetivo:** ejecutar NAYRA completo en una PC y probarlo desde un celular Android físico, con los modelos biométricos reales.

## 1. Qué corre y dónde

```text
PC (Docker Desktop + WSL2)
  postgres     PostgreSQL 16; puerto 5432 solo en 127.0.0.1
  nayra-voz    FastAPI + Vosk + WebRTC VAD + AASIST + ECAPA-TDNN; sin puerto publicado (solo red interna)
  nayra-back   Spring Boot (Java 21); puerto 8080 publicado en la PC
Celular Android (Nayra-App, Flutter)  --Wi-Fi o USB-->  http://IP_DE_LA_PC:8080
```

**Reparto de trabajo entre el celular y el servidor:**
- El celular **solo** se encarga de la interfaz accesible, el PIN, la captura de audio y la comunicación con Nayra-Back.
- **Toda** la inferencia biométrica ocurre en Nayra-Voz, en la PC.
- La app no incluye modelos.
- El audio se mantiene solo en memoria y no se guarda en el celular (D-013).
- La app tiene solo dos dependencias de ejecución: `http` y `record`.

Así la app sigue siendo viable en celulares Android de gama baja o media.

## 2. Requisitos de la PC

Valores medidos en la PC de referencia (HP 250 G8):

| Recurso | Mínimo práctico | Notas |
|---|---|---|
| Sistema | Windows 10/11 x64 con virtualización activa | WSL2 y Docker Desktop |
| RAM | 8 GB (16 GB recomendado) | Con 8 GB **no** cabe un emulador Android junto a Docker: usar un celular físico |
| Disco | 60 GB libres | Las imágenes ocupan unos 3 GB y el SDK de Android con Flutter unos 25 GB |
| CPU | 4 núcleos | Los modelos corren en CPU |

Consumo medido con los contenedores en reposo:

| Contenedor | RAM en reposo |
|---|---|
| nayra-voz, con los modelos cargados | ~430 MB (límite: 3 GB) |
| nayra-back | ~300 MB (límite: 1,5 GB) |
| postgres | ~70 MB |

## 3. Software y versiones

| Herramienta | Versión | Para qué |
|---|---|---|
| Git | 2.x | Control de versiones |
| Docker Desktop (motor WSL2) | 4.x, Compose v2 | PostgreSQL, Nayra-Voz y Nayra-Back |
| JDK | Temurin 21 (D-069) | Solo para compilar o probar Nayra-Back fuera de Docker (`mvnw.cmd`) |
| Flutter / Dart | 3.47.5 / 3.13.4 | Nayra-App |
| Android SDK | platform 36, build-tools 36.0.0, NDK 28.2.13676358, platform-tools (adb) | Compilar e instalar el APK |
| Android Studio | Opcional | Solo para gestionar el SDK. No se necesita emulador |

Python 3.11 **no** hace falta en Windows: Nayra-Voz corre dentro de su contenedor.

Archivo `C:\Users\<usuario>\.wslconfig` recomendado con 8 GB de RAM:

```ini
[wsl2]
memory=5GB
processors=4
swap=6GB
```

## 4. Primera puesta en marcha

Todos los comandos se ejecutan en PowerShell, en la raíz del repositorio y en la rama `yuniv`.

### 4.1 Archivo `.env`

```powershell
Copy-Item .env.example .env
```

Completa los valores marcados `[SECRETO]` con valores aleatorios. Las contraseñas deben ser alfanuméricas y sin espacios.

```powershell
# Contraseñas y token (32 caracteres hexadecimales):
-join ((1..16) | % { '{0:x2}' -f (Get-Random -Max 256) })
# NAYRA_JWT_CLAVE y NAYRA_VOZ_CLAVE_EMBEDDINGS (32 bytes en base64):
[Convert]::ToBase64String((1..32 | % { [byte](Get-Random -Max 256) }))
```

El archivo `.env` está en `.gitignore` y **nunca** se sube a Git.

> **Aviso:** si se pierde `NAYRA_VOZ_CLAVE_EMBEDDINGS`, los perfiles de voz guardados dejan de poder leerse.

### 4.2 Construir las imágenes

La primera vez descarga y verifica los modelos reales con SHA-256: unos 2,4 GB, y tarda.

```powershell
docker compose build
```

### 4.3 Levantar el sistema (en este orden)

```powershell
docker compose --profile local up -d postgres                # 1. PostgreSQL 16 con los roles ya creados
docker compose --profile migraciones run --rm migraciones    # 2. Flyway: esquemas nayra y biometria
docker compose up -d                                         # 3. nayra-voz y después nayra-back
```

- El paso 2 solo hace falta la primera vez y cuando haya migraciones nuevas. Repetirlo no cambia nada.
- Nayra-Back espera a que Nayra-Voz esté **healthy**, es decir, con los modelos cargados.

### 4.4 Revisar el estado

```powershell
docker compose ps                 # los tres servicios "Up"; nayra-voz y postgres "healthy"
docker compose logs nayra-back    # o nayra-voz / postgres
docker stats --no-stream          # consumo de memoria
```

Si PostgreSQL 18 está instalado en Windows, su servicio debe estar **detenido**: también usa el puerto 5432. En la PC de referencia quedó con inicio manual.

### 4.5 Detener

```powershell
docker compose stop               # detiene y conserva todo
docker compose down               # elimina los contenedores y CONSERVA la base de datos (volumen)
docker compose --profile local down -v   # ¡BORRA TAMBIÉN LA BASE DE DATOS!
```

> **ADVERTENCIA:** `down -v` elimina el volumen de PostgreSQL: usuarios, cuentas, sesiones y perfiles de voz. Úsalo solo cuando quieras reinicializar el entorno. Después hay que repetir el paso 2.

## 5. Acceso desde el celular

### 5.1 Firewall de Windows

Permite la entrada al puerto 8080 **solo en redes privadas**. Ejecuta esto en PowerShell como administrador:

```powershell
New-NetFirewallRule -DisplayName "NAYRA local 8080" -Direction Inbound -Protocol TCP -LocalPort 8080 -Action Allow -Profile Private
```

La red Wi-Fi de la PC debe estar marcada como **privada**.

### 5.2 IP de la PC

```powershell
ipconfig    # "Dirección IPv4" del adaptador Wi-Fi, por ejemplo 192.168.1.50
```

Desde el navegador del celular, `http://IP_DE_LA_PC:8080/v3/api-docs` debe responder.

### 5.3 Celular por USB

1. En el celular, abre Ajustes → Acerca del teléfono y toca 7 veces "Número de compilación". Así se activan las Opciones de desarrollador.
2. En las Opciones de desarrollador, activa la **Depuración por USB**.
3. Conecta el celular y acepta la huella RSA cuando la pida. Con `adb devices`, el celular debe aparecer como `device`.
4. Algunos fabricantes necesitan en Windows el driver USB del fabricante o el "Google USB Driver".

### 5.4 Celular por Wi-Fi (Android 11 o posterior)

1. En las Opciones de desarrollador, activa la **Depuración inalámbrica**.
2. Elige "Vincular dispositivo con código", ejecuta `adb pair IP:PUERTO` y escribe el código.
3. Después ejecuta `adb connect IP:PUERTO`, con el puerto que muestra la pantalla principal de la depuración inalámbrica.

La PC y el celular deben estar en la **misma red**.

### 5.5 Compilar e instalar Nayra-App

```powershell
cd Nayra-App
flutter pub get
# Recomendado en celular físico: compilación "profile" (AOT, ligera, como release; HTTP local permitido solo aquí)
flutter run --profile --dart-define=NAYRA_BACKEND_URL=http://IP_DE_LA_PC:8080
```

Otras opciones:
- **Depuración** (más pesada; permite recarga en caliente): `flutter run --dart-define=NAYRA_BACKEND_URL=http://IP_DE_LA_PC:8080`.
- **Emulador** (no recomendado con 8 GB de RAM): `--dart-define=NAYRA_BACKEND_URL=http://10.0.2.2:8080`.

`10.0.2.2` solo funciona desde el emulador. En un celular físico se usa la IP de la PC.

La URL **nunca** se escribe en el código. La compilación `release` no permite HTTP sin TLS.

## 6. Demostración funcional

### 6.1 Administrador inicial (una sola vez por base de datos)

El primer usuario es el administrador. Su registro se inicia desde la PC con un DNI del registro de identidad simulado (`Nayra-Back/src/main/resources/entorno-simulado/datos-ficticios.json`, por ejemplo `10000001`):

```powershell
curl.exe -s -X POST http://localhost:8080/prototipo/arranque/administrador -H "Content-Type: application/json" -d '{\"tipoDocumentoIdentidad\":\"DNI\",\"numeroDocumento\":\"10000001\"}'
```

La respuesta incluye un `codigoRegistro`.

En el celular:
1. Pulsa **Registrarme** y escribe el código.
2. Confirma tus datos.
3. Ingresa un celular válido (9 dígitos que empiecen por 9) y un PIN de 6 dígitos.
4. Graba las **3 muestras** de voz: dices la frase que la app muestra y lee.
5. La cuenta queda creada y el celular vinculado.

### 6.2 Otros usuarios

El administrador inicia sesión y abre el menú del representante, que inicia el registro de otro DNI y valida su identidad. La persona completa su registro con el código.

Un celular solo puede estar vinculado a una cuenta (D-039). Cada usuario de prueba necesita su propio celular. En su defecto, hay que reinstalar la app, lo que equivale a un cambio de dispositivo.

### 6.3 Casos de prueba

En todos los casos el flujo es **Iniciar sesión Nayra → (firma del dispositivo) → PIN → frase de desafío → resultado**.

| Caso | Cómo | Resultado esperado | Etapa que decide |
|---|---|---|---|
| 1. Usuario legítimo | PIN correcto y la frase dicha por la persona registrada, en un lugar tranquilo | Autenticado | ECAPA ≥ 0.80 y AASIST ≥ 0.90 (ver §7) |
| 2. PIN incorrecto | Un PIN distinto | "El PIN no es correcto" con los intentos restantes. Al 3.º, bloqueo (D-044) | Nayra-Back |
| 3. Otra persona | PIN correcto, pero la frase la dice otra persona | "No se pudo verificar su voz" | ECAPA (verificación 1:1) |
| 4. Grabación reproducida | PIN correcto y se reproduce, desde otro celular o un altavoz, una grabación de la persona diciendo **la frase actual** | "No se pudo verificar su voz" cuando AASIST la detecta | AASIST |
| 5. Contenido incorrecto | PIN correcto y se dice otra frase | "La frase no coincide con la solicitada" | Vosk (gramática cerrada) |
| 6. Servicio caído | `docker compose stop nayra-voz` y se intenta la voz | Mensaje de servicio no disponible, sin cierre de la app | Nayra-Back |
| 7. Backend apagado | `docker compose stop nayra-back` | "No se pudo conectar con Nayra…", sin cierre de la app | App |

Estas pruebas son **funcionales**. **No** miden FAR, FRR, EER ni exactitud, y no deben presentarse como validación científica (D-060).

## 7. Umbrales: estado conocido antes de probar con voces reales

Los umbrales vigentes son los de D-055: similitud ≥ 0.80 y bona fide ≥ 0.90, en escala [0,1]. No están calibrados (D-060) y **no se modificaron**.

El 2026-10-01 se hizo una medición informal con los modelos reales del contenedor. Las grabaciones fueron 8 de prueba de SpeechBrain (2 locutores en inglés), 1 grabación limpia y voz sintética de espeak-ng.

| Medición | Valor observado | Lectura |
|---|---|---|
| ECAPA, mismo locutor (centroide de 3 muestras frente a otra frase) | 0.70 – 0.73 | **Por debajo de 0.80**: con este umbral, el usuario legítimo podría ser rechazado |
| ECAPA, otro locutor | −0.04 – 0.02 | Rechazo claro |
| AASIST, voz humana limpia | 0.95 | Aceptada |
| AASIST, voz humana de corpus comprimido (LibriSpeech) | 0.00 – 0.60 | **Por debajo de 0.90**: voz real marcada como posible suplantación |
| AASIST, voz sintética (espeak-ng) | 0.00 | Rechazo correcto |

Son muy pocas muestras y en otro idioma, así que **no es una calibración**. Sirven solo para anticipar que el caso 1 puede fallar con los umbrales actuales. Si falla, **no** se bajan los umbrales: se registra el motivo y se decide por D-060.

## 8. Solución de problemas

| Síntoma | Causa probable | Qué hacer |
|---|---|---|
| `nayra-voz` no llega a healthy | Modelos no cargados o falta de memoria | `docker compose logs nayra-voz`; revisa `.wslconfig` (5 GB) |
| `migraciones` no conecta | Red de migraciones o host incorrecto | En `.env`: `NAYRA_RED_MIGRACIONES=nayra_default` y `NAYRA_DB_HOST=postgres` |
| El puerto 5432 está ocupado | PostgreSQL de Windows encendido | Detén el servicio `postgresql-x64-18` |
| El celular no conecta | Firewall, red pública o IP incorrecta | §5.1 y §5.2; prueba `http://IP:8080/v3/api-docs` desde el navegador del celular |
| "La frase no coincide" con la frase correcta | Ruido, pausas largas o palabras cortadas | Habla claro y a ritmo normal, en un lugar tranquilo |
