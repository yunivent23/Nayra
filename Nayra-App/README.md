# Nayra-App — aplicación móvil (prototipo AG-13)

Flutter (D-007) con canal Kotlin para la clave del dispositivo en el Android Keystore (D-048).

Flujo de inicio de sesión: **Iniciar sesión Nayra** → firma del nonce con la clave del dispositivo → PIN de 6 dígitos (teclado accesible o dictado, D-061) → desafío palabra + 3 dígitos + palabra (D-054) → grabación manual → decisión de Spring Boot (D-056).

"Registro de prototipo" es un registro mínimo **provisional** (PIN, vinculación y enrolamiento de voz) para poder probar el flujo. **No** es el registro asistido de D-052.

## Ejecutar

```bash
tool/descargar_modelo_vosk.sh   # modelo de Vosk en español (40 MB) en assets/modelos/, fuera de git (D-081)
flutter pub get
flutter test
flutter run --dart-define=NAYRA_BACKEND_URL=http://10.0.2.2:8080   # backend con el perfil "prototipo"
```

El HTTP sin TLS solo está habilitado en la variante de depuración para el backend local.

## Comando «Iniciar sesión Nayra» (D-081)

**Estado: implementado, no ejecutado todavía en Android.**

En la primera pantalla del inicio de sesión, cuando Nayra termina de hablar, la app escucha con Vosk en el celular el
comando «Iniciar sesión Nayra» con una gramática cerrada. Al reconocerlo, detiene su micrófono y sigue el mismo flujo
que el botón de voz. El comando solo inicia el flujo; no autentica. El modelo `vosk-model-small-es-0.42` debe estar
en `assets/modelos/vosk-model-small-es-0.42.zip` antes de compilar (lo descarga `tool/descargar_modelo_vosk.sh`).
Si falta el modelo o falla Vosk, la app sigue funcionando con el botón de voz.

- **Validado:** pruebas automatizadas con un Vosk simulado y prueba del modelo en el entorno de desarrollo con voz
  sintética.
- **Pendiente:** ejecución real en Android, voces humanas reales, ruido, falsos positivos y comportamiento real del
  micrófono.
- **Limitación conocida:** con voz sintética, «Iniciar sesión Maira» también se reconoce como el comando.
