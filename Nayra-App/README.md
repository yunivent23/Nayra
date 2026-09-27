# Nayra-App — aplicación móvil (prototipo AG-13)

Flutter (D-007) con canal Kotlin para la clave del dispositivo en el Android Keystore (D-048).

Flujo de inicio de sesión: **Iniciar sesión Nayra** → firma del nonce con la clave del dispositivo → PIN de 6 dígitos (teclado accesible o dictado, D-061) → desafío palabra + 3 dígitos + palabra (D-054) → grabación manual → decisión de Spring Boot (D-056).

"Registro de prototipo" es un registro mínimo **provisional** (PIN, vinculación y enrolamiento de voz) para poder probar el flujo. **No** es el registro asistido de D-052.

## Ejecutar

```bash
flutter pub get
flutter test
flutter run --dart-define=NAYRA_BACKEND_URL=http://10.0.2.2:8080   # backend con el perfil "prototipo"
```

El HTTP sin TLS solo está habilitado en la variante de depuración para el backend local.
