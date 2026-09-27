# 05_BIOMETRIA_NAYRA.md — Biometría de voz y protección anti-spoofing

## 1. Propósito

Este documento define el contexto técnico, alcance y reglas para el componente de biometría de voz de Nayra.

La biometría de voz es uno de los elementos centrales de la solución, debido a que el proyecto busca utilizar la voz como mecanismo de autenticación para personas con discapacidad visual en el contexto de billeteras digitales.

Este documento **no fija todavía un modelo biométrico, algoritmo, umbral o arquitectura definitiva de procesamiento**. Esos elementos deberán seleccionarse y documentarse mediante decisiones técnicas aprobadas.

> **Actualización AG-02 (2026-09-27):** el modelo biométrico, la estrategia anti-spoofing, el reconocimiento del contenido del desafío, el almacenamiento biométrico y la comunicación con Java quedaron aprobados (D-010 a D-013, D-046 parcial, D-054 a D-059). El diseño técnico vigente está en la **sección 27**. Los **valores** de umbrales siguen sin definir hasta la calibración (D-055).

---

# 2. Objetivo del componente biométrico

El componente biométrico debe permitir verificar si una entrada de voz corresponde al usuario que intenta autenticarse.

De manera conceptual:

```text
Usuario
   ↓
Entrada de voz
   ↓
Procesamiento
   ↓
Representación de la voz
   ↓
Comparación / verificación
   ↓
Resultado
```

El resultado biométrico deberá ser utilizado por el backend dentro de las reglas de autenticación definidas por el sistema.

La biometría no debe ejecutar directamente una operación sensible.

---

# 3. Alcance

El componente biométrico contempla, según los requisitos aprobados:

- captura o recepción de voz;
- procesamiento de audio;
- extracción o generación de características;
- representación biométrica;
- comparación/verificación;
- detección de posibles intentos de spoofing;
- generación de un resultado para el sistema de autenticación.

El alcance exacto depende de las historias de usuario relacionadas con biometría y de las decisiones técnicas aprobadas.

---

# 4. Tecnologías consideradas

La estrategia del proyecto es utilizar **modelos preentrenados**, frameworks especializados o APIs existentes en lugar de entrenar modelos de inteligencia artificial desde cero.

Se ha considerado:

- Python;
- SpeechBrain;
- modelos preentrenados para procesamiento de voz.

Estas tecnologías son **opciones consideradas**, no una obligación de utilizar una implementación específica.

_(AG-02: SpeechBrain ECAPA-TDNN, AASIST y Vosk quedaron aprobados; ver §27 y D-011, D-012, D-046.)_

La selección definitiva debe registrarse en:

`07_DECISIONES_TECNICAS_NAYRA.md`

---

# 5. Separación entre procesamiento y lógica de negocio

El procesamiento biométrico debe mantenerse separado de la lógica principal de negocio.

Conceptualmente:

```text
Aplicación móvil
       ↓
Backend principal
       ↓
Solicitud de autenticación por voz
       ↓
Componente biométrico
       ↓
Resultado biométrico
       ↓
Backend principal
       ↓
Reglas de autenticación
       ↓
Respuesta
```

El componente biométrico no debe decidir por sí mismo si una operación financiera puede ejecutarse.

El backend debe validar el resultado de acuerdo con las reglas de seguridad y negocio.

---

# 6. Verificación biométrica

El sistema debe distinguir entre:

- identificación;
- verificación.

Para Nayra, el objetivo principal es la **verificación de identidad**.

Conceptualmente:

```text
Identidad declarada
       +
Muestra de voz
       ↓
Sistema biométrico
       ↓
Comparación
       ↓
Resultado de verificación
```

El resultado puede utilizarse posteriormente como parte del proceso de autenticación.

---

# 7. Flujo conceptual de autenticación por voz

El flujo general esperado es:

```text
1. Usuario inicia autenticación
              ↓
2. Aplicación solicita la interacción por voz
              ↓
3. Se obtiene una muestra de voz
              ↓
4. Se envía la información al backend
              ↓
5. Se procesa la muestra
              ↓
6. Se verifica spoofing
              ↓
7. Se realiza la verificación biométrica
              ↓
8. Se devuelve el resultado
              ↓
9. Backend aplica las reglas de autenticación
              ↓
10. Se permite o rechaza la autenticación
```

Este flujo es conceptual. El protocolo de comunicación y la distribución física de los componentes se definirán en `02_ARQUITECTURA_NAYRA.md`.

---

# 8. Anti-spoofing

El proyecto contempla mecanismos para reducir el riesgo de suplantación mediante entradas de voz que no correspondan a una interacción legítima del usuario.

Entre las amenazas consideradas conceptualmente se encuentran:

- reproducción de una grabación;
- voz sintética;
- voz manipulada;
- otras formas de presentación de una voz no legítima.

No se debe asumir que estos ataques serán detectados mediante una única técnica.

La solución anti-spoofing deberá seleccionarse de acuerdo con la literatura revisada, las capacidades de los modelos disponibles y el alcance de la tesis.

---

# 9. Flujo conceptual anti-spoofing

El flujo esperado puede representarse como:

```text
Muestra de voz
      ↓
Análisis anti-spoofing
      ↓
¿Entrada sospechosa?
   ┌───────┴───────┐
  Sí               No
   ↓                ↓
Rechazar       Verificación
                  biométrica
                     ↓
                  Resultado
```

La ubicación exacta del mecanismo anti-spoofing dentro de la arquitectura deberá definirse en `02_ARQUITECTURA_NAYRA.md`.

---

# 10. Orden entre anti-spoofing y verificación biométrica

El orden conceptual considerado para el flujo es:

```text
Entrada de voz
      ↓
Anti-spoofing
      ↓
Verificación biométrica
```

Sin embargo, la implementación definitiva debe basarse en la arquitectura y en la estrategia biométrica aprobadas.

No modificar este flujo únicamente por conveniencia de programación sin documentar la decisión.

---

# 11. Modelos y algoritmos

El modelo biométrico definitivo todavía debe seleccionarse.

Antes de implementar se deberán definir:

- modelo o framework;
- tipo de representación de voz;
- método de comparación;
- método de entrenamiento/adaptación, si corresponde;
- criterios de aceptación;
- criterios de rechazo;
- estrategia anti-spoofing;
- métricas de evaluación.

No se debe inventar un algoritmo o modelo como si hubiera sido aprobado por el proyecto.

---

# 12. Umbrales de decisión

La autenticación biométrica requiere definir un criterio de aceptación.

Conceptualmente:

```text
Resultado biométrico
        ↓
Comparación con umbral
        ↓
┌───────────────┴───────────────┐
Aceptado                    Rechazado
```

El valor del umbral **no está definido todavía en este documento**.

No se debe colocar un valor arbitrario en el código.

El umbral deberá determinarse a partir de la estrategia de evaluación y validación del proyecto.

---

# 13. Métricas

La evaluación del sistema biométrico deberá considerar métricas apropiadas para sistemas de verificación de voz.

Dependiendo del método seleccionado, podrán considerarse métricas como:

- False Acceptance Rate (FAR);
- False Rejection Rate (FRR);
- Equal Error Rate (EER);
- tasa de detección de spoofing;
- otras métricas pertinentes al modelo utilizado.

Las métricas definitivas deberán establecerse antes de la etapa de validación.

No presentar valores como resultados del proyecto hasta que hayan sido obtenidos mediante experimentación.

---

# 14. Datos de voz

La estrategia de almacenamiento de los datos de voz todavía debe definirse.

No asumir automáticamente que:

```text
Audio → PostgreSQL
```

es la solución definitiva.

Debe evaluarse qué información necesita conservar el sistema:

- audio original;
- características;
- representaciones biométricas;
- metadatos;
- resultados de verificación;
- información de auditoría.

La decisión deberá considerar seguridad, privacidad, rendimiento, costo y alcance del proyecto.

---

# 15. Protección de la información biométrica

La información biométrica debe recibir un tratamiento de seguridad acorde con su sensibilidad.

Se deben considerar:

- protección durante la transmisión;
- control de acceso;
- protección durante el almacenamiento;
- minimización;
- retención limitada;
- eliminación cuando corresponda;
- auditoría de accesos;
- separación de responsabilidades.

Los mecanismos específicos se documentarán en `06_SEGURIDAD_NAYRA.md`.

---

# 16. No almacenar innecesariamente audio

La implementación no debe almacenar permanentemente muestras de voz únicamente porque hayan sido utilizadas durante una autenticación.

Antes de almacenar audio se debe justificar:

1. por qué es necesario;
2. cuánto tiempo debe conservarse;
3. quién puede acceder;
4. cómo se protege;
5. cuándo se elimina.

Si el sistema puede funcionar con una representación biométrica o resultado procesado sin conservar el audio original, esta alternativa debe evaluarse.

---

# 17. Integración con el backend

El componente biométrico debe entregar un resultado que pueda ser interpretado por el backend principal.

Conceptualmente:

```text
Python
   ↓
Resultado biométrico
   ↓
Backend Java
   ↓
Validación
   ↓
Autenticación
```

El formato exacto del resultado será definido posteriormente en `04_API.md`, cuando la API haya sido diseñada.

No crear endpoints biométricos definitivos antes de definir la arquitectura y el contrato de API.

---

# 18. Manejo de errores

El componente biométrico debe distinguir, cuando corresponda, entre situaciones como:

- muestra inválida;
- audio insuficiente;
- error de procesamiento;
- posible spoofing;
- coincidencia insuficiente;
- usuario no verificado;
- servicio biométrico no disponible.

Los mensajes técnicos internos no deben exponerse directamente al usuario si contienen información sensible.

La aplicación debe proporcionar una respuesta comprensible y accesible.

---

# 19. Seguridad contra abuso

El sistema debe considerar mecanismos para evitar abusos del componente biométrico, según corresponda:

- limitación de intentos;
- control de sesiones;
- registro de eventos;
- detección de comportamiento anómalo;
- protección de APIs;
- controles de autorización.

Las medidas concretas deberán definirse en `06_SEGURIDAD_NAYRA.md`.

---

# 20. Accesibilidad

La autenticación biométrica debe diseñarse teniendo en cuenta al usuario objetivo.

El flujo debe evitar depender innecesariamente de elementos visuales para:

- iniciar la autenticación;
- proporcionar instrucciones;
- indicar el resultado;
- informar errores;
- solicitar reintentos.

La implementación concreta de accesibilidad pertenece principalmente a la aplicación móvil, pero el componente biométrico debe proporcionar estados que permitan generar una experiencia comprensible.

---

# 21. Privacidad

La solución debe aplicar principios de minimización y protección de datos.

En particular, debe evitarse:

- conservar información biométrica sin propósito;
- registrar muestras de voz completas en logs;
- incluir información biométrica en mensajes de error;
- exponer identificadores sensibles;
- almacenar secretos en código fuente.

Los requisitos legales y de protección de datos aplicables deberán documentarse en la sección correspondiente de la tesis.

---

# 22. Trazabilidad con requisitos

Las funcionalidades biométricas deben mantener trazabilidad con las historias de usuario correspondientes.

La relación general será:

```text
Historia de usuario
       ↓
Necesidad biométrica
       ↓
Funcionalidad
       ↓
Componente
       ↓
Modelo / algoritmo
       ↓
Implementación
       ↓
Prueba
       ↓
Métrica
```

No implementar una capacidad biométrica que no pueda justificarse mediante un requisito o una decisión técnica aprobada.

---

# 23. Reglas para Claude

Claude debe aplicar las siguientes reglas al trabajar con biometría:

1. No entrenar modelos desde cero salvo que el equipo lo decida explícitamente.
2. No inventar modelos biométricos.
3. No inventar umbrales.
4. No inventar resultados experimentales.
5. No presentar métricas hipotéticas como resultados.
6. No crear tablas de biometría automáticamente.
7. No asumir que el audio debe almacenarse.
8. No exponer información biométrica en logs.
9. No cambiar el flujo anti-spoofing sin justificarlo.
10. Mantener separada la lógica biométrica de la lógica de negocio.
11. No crear endpoints definitivos hasta que `04_API.md` esté definido.
12. No modificar la arquitectura sin una decisión documentada.
13. Cuando falte una decisión biométrica, marcarla como pendiente.
14. Priorizar reproducibilidad y trazabilidad de los experimentos.

---

# 24. Decisiones pendientes

Antes de implementar definitivamente el componente biométrico deben definirse:

> **Estado AG-02 (2026-09-27):** resueltos modelo, framework, anti-spoofing, extracción de características, representación, método de comparación, estrategia de almacenamiento (sin audio: no hay retención de audio), protocolo con Java, reconocimiento del contenido del desafío y generación del desafío (estructura). El responsable de aplicar los umbrales técnicos también quedó aprobado (servicio Python, D-056). Siguen pendientes: **valores** de umbral (D-055), dataset y procedimiento de evaluación (D-060), detalle de reintentos (D-044), herramienta de VAD (D-058), lista de palabras y vida del desafío (D-054) y el resto del reconocimiento del habla (D-046).

- modelo de reconocimiento/verificación;
- framework definitivo;
- modelo anti-spoofing;
- estrategia de extracción de características;
- representación biométrica;
- método de comparación;
- umbral;
- métricas;
- dataset o fuentes de muestras;
- procedimiento de evaluación;
- estrategia de almacenamiento;
- período de retención;
- protocolo de comunicación con Java;
- manejo de errores;
- estrategia de reintentos (el valor de 3 intentos está aprobado en D-044; los tipos de fallo que cuentan siguen pendientes);
- controles anti-abuso;
- reconocimiento del habla para comprobar el contenido del desafío (D-046);
- generación de las frases de desafío (vocabulario, longitud, pronunciabilidad).

Estas decisiones deben registrarse en `07_DECISIONES_TECNICAS_NAYRA.md`.

---

# 25. Regla principal

> **La biometría de Nayra debe implementarse utilizando modelos y métodos documentados y aprobados, manteniendo separación entre procesamiento biométrico, lógica de negocio y seguridad.**

Si una decisión no está definida:

```text
No asumir
   ↓
Identificar la decisión pendiente
   ↓
Investigar / evaluar
   ↓
Seleccionar
   ↓
Documentar
   ↓
Implementar
   ↓
Validar
```

---

# 26. Decisiones funcionales de AG-01 (2026-09-26)

Registradas en `07_DECISIONES_TECNICAS_NAYRA.md` (D-036 a D-040, D-044, D-052). Son **requisitos funcionales**; no eligen modelo, framework, umbral, almacenamiento ni protocolo.

## 26.1 Enrolamiento

- El enrolamiento de voz forma parte del registro asistido y se realiza **después** de que un representante autorizado valida el DNI y la identidad de la persona, y después de la creación de la contraseña y la vinculación del dispositivo (D-052, que modifica D-036). La biometría **no** se usa para validar la identidad en el registro.
- Se aplica **anti-spoofing durante el enrolamiento**, para impedir que se registre una voz sintética, manipulada o reproducida.
- Se aplican las HUs de captura existentes (instrucciones, control manual, frase de desafío, calidad, condiciones del entorno, repetición: HU-27 a HU-32).

## 26.2 Verificación en el inicio de sesión

- La biometría es una **verificación 1:1** contra la referencia de la cuenta determinada por el **dispositivo vinculado**. **No** se utiliza identificación 1:N (D-037).
- La verificación biométrica ocurre **después** de validar la contraseña. _(AG-02: la contraseña se concreta como **PIN de 6 dígitos**, D-061.)_
- La muestra biométrica es la respuesta a una **frase de desafío variable** propuesta en cada autenticación. Se debe **comprobar que el contenido** de la respuesta corresponde al desafío; sin esa comprobación, una grabación de la voz del usuario podría superar la verificación.
- Orden conceptual (se mantiene §10): respuesta al desafío → comprobación del contenido → **anti-spoofing** → **verificación 1:1** → resultado → el backend aplica las reglas (3 intentos, D-044).
- **No** son muestras biométricas: el comando de activación "Iniciar sesión Nayra" ni la contraseña dictada. La contraseña dictada se descarta tras calcular su hash y nunca se almacena como audio.

## 26.3 Referencia biométrica

- Permanece **asociada a la cuenta de acceso en el backend** y no depende exclusivamente del dispositivo (D-038). Tras un cambio de celular sigue disponible para la verificación 1:1.
- **Pendiente:** ubicación, formato, modelo que la genera y si es un embedding u otra representación (D-011, D-013).
- Criterios que cualquier alternativa debe cumplir: no guardar audio innecesariamente (§16); proteger la referencia en tránsito y en reposo; acceso restringido al componente que verifica (el administrador no accede a ella); separación de los datos personales; nunca en logs; eliminación conforme a HU-36.

## 26.4 Cambio de dispositivo y nuevo enrolamiento

- En el cambio de celular, el titular se valida con **contraseña + verificación 1:1 contra la referencia existente + anti-spoofing** (D-040).
- Un **nuevo enrolamiento** solo puede permitirse **después** de validar al titular. Está prohibido el flujo `DNI → cuenta encontrada → registrar nueva voz → acceso`.
- Si el usuario no supera la validación, se deriva a recuperación asistida (procedimiento pendiente, D-049).

## 26.5 Voz alterada temporalmente

No se asume ninguna solución fiable para validar voces alteradas por enfermedad o afonía. **No debe reducirse el umbral** para aceptarlas. Si la verificación no es posible, el usuario utiliza el proceso de recuperación (D-040). Su estudio queda como posible experimento futuro, no como funcionalidad del primer entregable.

## 26.6 Reconocimiento del habla

El prototipo necesita reconocimiento del habla (distinto de la verificación del locutor) para el comando de activación, el DNI (si se dicta; forma de ingreso pendiente, D-052), la contraseña dictada y la comprobación del contenido del desafío. Su tecnología y ubicación (dispositivo o servidor) están **pendientes (D-046)**. Si procesa la contraseña dictada, no debe almacenar ni registrar el audio ni la transcripción.

_(AG-02: el contenido del desafío se reconoce con Vosk en el servidor (D-046, parcial); la contraseña se concreta como PIN de 6 dígitos (D-061) y si puede dictarse sigue pendiente; el comando y el DNI siguen pendientes.)_

---

# 27. Diseño técnico aprobado del módulo de voz — AG-02 (2026-09-27)

Decisiones de referencia: `07_DECISIONES_TECNICAS_NAYRA.md`, D-010 a D-013, D-046 (parcial), D-048, D-054 a D-061. Ningún valor numérico de umbral está aprobado; ver §27.7.

## 27.1 Modelos y herramientas

| Etapa | Herramienta / modelo | Licencia (según análisis A–K, reconfirmar al fijar versión) | Entrada | Salida | Decisión |
|---|---|---|---|---|---|
| Calidad de audio | VAD + SNR + saturación (herramienta de VAD pendiente; opción: VAD de SpeechBrain) | — | WAV 16 kHz mono | Aprobado / motivo | D-058 |
| Contenido del desafío | Vosk `vosk-model-small-es-0.42` con gramática cerrada (respaldo: `vosk-model-es-0.42`, `faster-whisper small`) | Apache 2.0 | WAV + vocabulario | Secuencia reconocida + confianza por palabra | D-046 |
| Anti-spoofing | AASIST preentrenado (extensión: SSL-AASIST) | MIT | WAV (~4 s, recorte/relleno) | Puntaje bona fide / spoof | D-012 |
| Verificación 1:1 | SpeechBrain ECAPA-TDNN `speechbrain/spkrec-ecapa-voxceleb` | Apache 2.0 | WAV | Embedding 192-d → similitud coseno | D-011 |

Todas corren en el **servicio Python** (D-002), en CPU, expuesto solo al backend principal mediante REST interno con FastAPI (D-010).

## 27.2 Formato de audio

WAV PCM 16 kHz, mono, 16 bits, sin compresión con pérdida (D-057). Captura manual por el usuario (HU-28, HU-42, HU-62). Duraciones mínima y máxima: a calibrar.

## 27.3 Desafío

Estructura **palabra + 3 dígitos + palabra**, generado por Spring Boot con `SecureRandom`, de un solo uso, ligado a la cuenta y al contexto, emitido después de validar dispositivo y PIN, nunca 6 dígitos seguidos (D-054). Repetir la lectura no lo cambia; enviar un audio lo consume. El vocabulario es un archivo versionado compartido por Spring Boot y Python. Lista de palabras y vida del desafío: pendientes.

## 27.4 Pipeline de verificación (inicio de sesión y cambio de dispositivo)

```text
Audio de la respuesta al desafío (en memoria)
   ↓
a) Calidad (voz neta, SNR, saturación) ── falla → CALIDAD_INSUFICIENTE
   ↓
b) Contenido (Vosk, gramática cerrada) ── falla → CONTENIDO_INCORRECTO
   ↓
c) Anti-spoofing (AASIST) ────────────── falla → POSIBLE_SPOOFING
   ↓
d) Embedding ECAPA + coseno 1:1 contra la referencia cifrada ── falla → NO_COINCIDE
   ↓
Resultado por etapa → Spring Boot aplica las reglas de autenticación (D-044) → audio descartado
```

- Se mantiene el orden de §10 y §26.2 (contenido → anti-spoofing → verificación), con la calidad como etapa previa.
- En operación, el pipeline se detiene en la primera etapa fallida; en modo de evaluación se registran todos los puntajes anonimizados (D-059).
- El servicio Python/FastAPI **aplica los umbrales técnicos** en cada etapa y devuelve el veredicto técnico de cada una junto con sus puntajes (D-056). Los **valores** de esos umbrales siguen pendientes de calibración (D-055). La decisión final de autenticar, los intentos, el bloqueo, la sesión y la auditoría son de Spring Boot (§5).
- En el **cambio de dispositivo** (D-040), el desafío se liga a la solicitud de cambio y al dispositivo nuevo, y la verificación es contra la **referencia existente**.

## 27.5 Enrolamiento

Dentro del registro asistido (D-052, paso 11), con dispositivo ya vinculado y PIN creado:

1. Spring Boot emite un desafío distinto por muestra.
2. Por cada muestra: calidad → contenido → **anti-spoofing** (obligatorio en el enrolamiento, §26.1) → embedding.
3. **3 muestras válidas** como valor inicial (hasta 5 si alguna falla); se descartan muestras muy alejadas del resto (HU-30).
4. Se guarda el **centroide** cifrado con nombre y versión del modelo (D-013). El audio se descarta.
5. Spring Boot recibe solo "enrolamiento correcto" o el motivo del fallo; confirmación accesible (HU-33).

## 27.6 Referencia biométrica

Solo embedding cifrado (AES-256-GCM), en el esquema `biometria` del PostgreSQL del proyecto, accesible únicamente por el servicio Python (D-013, `03_BASE_DE_DATOS_NAYRA.md` §16.11). Sin audio, sin adaptación automática, actualización solo por re-enrolamiento tras validar al titular (§26.4), borrado físico según HU-36. Java y el administrador no acceden a la referencia.

## 27.7 Umbrales y calibración (D-055)

- Umbrales en configuración versionada con el modelo, nunca en el código.
- **Umbral provisional** para el prototipo, obtenido en un piloto pequeño y registrado como provisional en `07`.
- Valores definitivos por calibración con voluntarios (dataset y consentimiento pendientes, D-060), desarrollo y prueba separados, FAR/FRR/EER, punto de operación de baja FAR.
- No reducir el umbral para voces alteradas (§26.5).

## 27.8 Aspectos que solo se confirman con pruebas

Precisión biométrica en español y con celulares; umbral provisional y definitivo; rechazo de grabaciones reproducidas (antiguas y en tiempo real); detección de TTS y conversión de voz; falsos rechazos del anti-spoofing con micrófonos de celular; precisión del reconocimiento del desafío con acentos y ruido; valores de calidad de audio; duración útil de la muestra; comprensión y memoria del desafío con personas con discapacidad visual; número de muestras de enrolamiento; latencia y consumo de recursos. **No existen resultados todavía** (§13, §23).

## 27.9 Limitaciones conocidas

- No hay cifras públicas validadas de ECAPA, AASIST ni Vosk con español peruano grabado en celulares.
- AASIST se entrenó con ataques de 2019 en inglés; su generalización a voces clonadas modernas es limitada.
- El replay en tiempo real no queda cubierto por el desafío ni por AASIST; es un riesgo aceptado del prototipo.
- Un cambio de modelo invalida las referencias existentes y exige re-enrolamiento.
