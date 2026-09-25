# 05_BIOMETRIA.md — Biometría de voz y protección anti-spoofing

## 1. Propósito

Este documento define el contexto técnico, alcance y reglas para el componente de biometría de voz de Nayra.

La biometría de voz es uno de los elementos centrales de la solución, debido a que el proyecto busca utilizar la voz como mecanismo de autenticación para personas con discapacidad visual en el contexto de billeteras digitales.

Este documento **no fija todavía un modelo biométrico, algoritmo, umbral o arquitectura definitiva de procesamiento**. Esos elementos deberán seleccionarse y documentarse mediante decisiones técnicas aprobadas.

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

La selección definitiva debe registrarse en:

`07_DECISIONES_TECNICAS.md`

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

Este flujo es conceptual. El protocolo de comunicación y la distribución física de los componentes se definirán en `02_ARQUITECTURA.md`.

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

La ubicación exacta del mecanismo anti-spoofing dentro de la arquitectura deberá definirse en `02_ARQUITECTURA.md`.

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

Los mecanismos específicos se documentarán en `06_SEGURIDAD.md`.

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

Las medidas concretas deberán definirse en `06_SEGURIDAD.md`.

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
- estrategia de reintentos;
- controles anti-abuso.

Estas decisiones deben registrarse en `07_DECISIONES_TECNICAS.md`.

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
