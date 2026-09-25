# 00_CONTEXT.md — Contexto general del proyecto Nayra

## 1. Identificación del proyecto

**Nombre del proyecto:** Nayra

**Tipo:** Proyecto de tesis / desarrollo de una solución tecnológica

**Área:** Ingeniería de Sistemas de Información

**Tema general:** Aplicación móvil de autenticación por voz orientada a personas con discapacidad visual en el contexto de billeteras digitales.

Nayra busca diseñar y desarrollar una solución que facilite la autenticación mediante la voz, incorporando biometría de voz y mecanismos de seguridad frente a intentos de suplantación o spoofing.

Este documento establece únicamente el **contexto general vigente del proyecto**. Las especificaciones detalladas deben consultarse en los documentos específicos del proyecto.

---

# 2. Propósito del proyecto

El propósito de Nayra es desarrollar una solución tecnológica que permita una autenticación accesible mediante la voz para usuarios con discapacidad visual dentro del contexto de las billeteras digitales.

La propuesta integra:

- accesibilidad;
- autenticación biométrica por voz;
- procesamiento de voz;
- verificación de identidad;
- mecanismos de detección de spoofing;
- seguridad de la información;
- interacción mediante una aplicación móvil.

La solución debe buscar que el mecanismo de autenticación sea comprensible y utilizable por el usuario objetivo, manteniendo controles de seguridad adecuados.

---

# 3. Problema general

Las personas con discapacidad visual pueden encontrar dificultades al utilizar mecanismos tradicionales de autenticación e interacción en servicios digitales, especialmente cuando estos dependen de elementos visuales.

La propuesta de Nayra busca abordar esta problemática mediante una alternativa basada en la voz, utilizando características biométricas para verificar la identidad del usuario.

El problema no se limita al reconocimiento de voz. La solución debe considerar también la seguridad del mecanismo biométrico, debido a la posibilidad de ataques mediante grabaciones, voces sintéticas, voces manipuladas u otros mecanismos de suplantación.

---

# 4. Usuarios objetivo

## 4.1 Usuario principal

Personas con discapacidad visual que utilizan o requieren utilizar servicios de billetera digital y que necesitan un mecanismo de autenticación accesible.

## 4.2 Usuario administrativo

El sistema contempla funcionalidades administrativas para la gestión de información y consulta de métricas.

Las funciones y permisos concretos de cada tipo de usuario deben definirse en `01_REQUISITOS.md`.

---

# 5. Alcance general

El proyecto contempla el diseño y desarrollo de una solución tecnológica que incluye, según los requisitos aprobados:

- aplicación móvil;
- backend para la lógica de negocio;
- componente especializado para procesamiento de voz;
- mecanismo de autenticación biométrica;
- mecanismo de detección de spoofing;
- base de datos;
- APIs para comunicación entre componentes;
- mecanismos de seguridad;
- funcionalidades administrativas;
- infraestructura necesaria para ejecutar la solución.

El alcance definitivo de cada funcionalidad será determinado por los requisitos oficiales del proyecto.

No deben incorporarse funcionalidades únicamente porque sean técnicamente posibles o habituales en sistemas similares.

---

# 6. Alcance de la investigación

La investigación se organiza en cuatro objetivos principales.

## Objetivo 1 — Identificación y análisis del problema

Comprende:

- identificación del problema;
- análisis de necesidades;
- revisión de antecedentes;
- revisión del estado del arte;
- análisis de tecnologías relacionadas;
- identificación de oportunidades de solución.

## Objetivo 2 — Diseño de la solución

Comprende el diseño de la propuesta tecnológica, incluyendo, según corresponda:

- diseño de la solución;
- arquitectura;
- componentes;
- tecnologías;
- diseño de la aplicación;
- diseño de servicios;
- diseño de base de datos;
- mecanismos de autenticación;
- componente biométrico;
- mecanismos anti-spoofing;
- mockups;
- diseño de la implementación.

## Objetivo 3 — Validación

Comprende la evaluación de la solución con el grupo objetivo definido en la investigación.

Los resultados de validación no deben presentarse como obtenidos hasta que la actividad haya sido ejecutada.

## Objetivo 4 — Plan de continuidad

Comprende las acciones necesarias para la continuidad, evolución y sostenibilidad de la solución.

---

# 7. Alcance del Short Paper

El Short Paper del proyecto se desarrolla principalmente hasta el **Objetivo 2: diseño de la solución**, incluyendo la parte de desarrollo que corresponda a dicha etapa.

La investigación previa sirve como fundamento para justificar la propuesta y las decisiones tomadas.

La validación, los resultados experimentales completos y el plan de continuidad corresponden a etapas posteriores y no deben presentarse como resultados ya obtenidos si todavía no han sido ejecutados.

---

# 8. Principios tecnológicos

## 8.1 Uso de modelos preentrenados

El proyecto no contempla entrenar modelos de inteligencia artificial desde cero como estrategia principal.

Se prioriza el uso de:

- modelos preentrenados;
- frameworks especializados;
- APIs o servicios existentes cuando sean apropiados.

Para procesamiento y biometría de voz se ha considerado el uso de tecnologías como SpeechBrain.

La tecnología definitiva debe quedar registrada en `07_DECISIONES_TECNICAS.md`.

## 8.2 Separación de responsabilidades

El sistema contempla una separación entre:

- backend principal y lógica de negocio;
- procesamiento especializado de voz;
- persistencia de información;
- aplicación móvil.

La implementación concreta de esta separación deberá definirse en `02_ARQUITECTURA.md`.

## 8.3 Seguridad transversal

La seguridad debe considerarse durante todo el ciclo de desarrollo:

- autenticación;
- autorización;
- protección de comunicaciones;
- protección de información personal;
- protección de información biométrica;
- manejo de sesiones;
- validación de entradas;
- protección frente a spoofing;
- registro de eventos relevantes.

Los detalles se especificarán en `06_SEGURIDAD.md`.

---

# 9. Tecnologías actualmente consideradas

Las siguientes tecnologías forman parte del contexto técnico de trabajo, pero **no deben considerarse decisiones definitivas si todavía no han sido aprobadas formalmente**.

### Backend

- Java
- Spring Boot

### Procesamiento biométrico

- Python
- SpeechBrain
- modelos preentrenados relacionados con procesamiento de voz

### Base de datos

- PostgreSQL

### Infraestructura

- Google Cloud Platform (GCP), como plataforma considerada para el despliegue.

### Arquitectura

- ArchiMate como lenguaje para representar la arquitectura, cuando corresponda.

### Control de versiones

- Git
- GitHub

### Frontend

La tecnología definitiva del frontend debe documentarse en `07_DECISIONES_TECNICAS.md`.

---

# 10. Biometría de voz

La biometría de voz constituye uno de los elementos centrales de Nayra.

A nivel conceptual, el procesamiento puede comprender:

```text
Voz del usuario
      ↓
Captura de audio
      ↓
Preprocesamiento
      ↓
Extracción / representación de características
      ↓
Verificación biométrica
      ↓
Resultado
```

La solución también debe considerar mecanismos para detectar intentos de spoofing:

```text
Entrada de voz
      ↓
Detección de spoofing
      ↓
¿Entrada válida?
      ├── No → Rechazar
      └── Sí → Continuar con verificación
```

Los modelos, algoritmos, métricas, umbrales y procedimientos concretos deberán documentarse en `05_BIOMETRIA.md`.

---

# 11. Inteligencia artificial

La inteligencia artificial se utilizará principalmente mediante modelos preentrenados, frameworks especializados o APIs.

Los modelos de IA no deben sustituir las reglas de negocio del sistema.

Cuando un modelo produzca una interpretación o resultado que pueda conducir a una operación del sistema, este resultado debe pasar por las validaciones correspondientes.

Conceptualmente:

```text
Usuario
   ↓
Procesamiento de voz / IA
   ↓
Interpretación
   ↓
Backend
   ↓
Validación
   ↓
Operación autorizada
```

Las operaciones sensibles no deben ejecutarse únicamente porque un modelo de IA haya generado una determinada respuesta.

---

# 12. Base de datos

PostgreSQL es la tecnología considerada para la persistencia estructurada de la aplicación.

La implementación de la base de datos debe mantener consistencia entre:

```text
Requisitos
    ↓
Modelo de datos
    ↓
Tablas
    ↓
Entidades
    ↓
Repositories
    ↓
Servicios
    ↓
API
```

No deben crearse tablas, columnas o relaciones sin una justificación basada en:

1. un requisito;
2. una necesidad técnica documentada; o
3. una decisión aprobada.

El modelo definitivo se documentará en `03_BASE_DE_DATOS.md`.

---

# 13. Arquitectura del sistema

## 13.1 Estado de la arquitectura

**La arquitectura definitiva del sistema NO está establecida en este documento.**

Durante etapas anteriores del proyecto se realizaron diferentes propuestas arquitectónicas. Algunas de esas propuestas fueron descartadas o requieren modificaciones.

Por esta razón:

> **Las arquitecturas anteriores no deben utilizarse como fuente de verdad para la implementación.**

La arquitectura vigente será aquella que posteriormente quede documentada y aprobada en:

`02_ARQUITECTURA.md`

## 13.2 Regla para Claude

Claude no debe:

- reutilizar automáticamente diagramas arquitectónicos anteriores;
- asumir que una arquitectura previamente discutida sigue vigente;
- implementar componentes de arquitecturas descartadas;
- asumir una distribución determinada de servidores;
- asumir una configuración específica de GCP;
- asumir una DMZ;
- asumir un API Gateway;
- asumir un balanceador;
- asumir zonas de disponibilidad;
- asumir la ubicación de instancias;
- asumir relaciones entre componentes que todavía no hayan sido aprobadas.

Si una implementación requiere una decisión arquitectónica que no está documentada, Claude debe identificarla como pendiente.

---

# 14. Relación entre investigación y desarrollo

La investigación académica sirve como fundamento para las decisiones del sistema, pero no todo lo mencionado en la investigación constituye automáticamente un requisito de software.

Debe mantenerse la siguiente distinción:

```text
Investigación
      ↓
Fundamentación
      ↓
Decisiones del proyecto
      ↓
Requisitos
      ↓
Diseño
      ↓
Implementación
```

Claude no debe convertir automáticamente una recomendación, antecedente o tecnología mencionada en un paper en una funcionalidad del sistema.

---

# 15. Trazabilidad

El desarrollo debe procurar mantener trazabilidad entre la investigación, los requisitos y el código.

Idealmente:

```text
Objetivo
   ↓
Requisito
   ↓
Funcionalidad
   ↓
Componente
   ↓
Endpoint / lógica
   ↓
Código
   ↓
Prueba
```

Esto permitirá justificar durante la tesis por qué cada funcionalidad implementada forma parte de la solución.

---

# 16. Reglas generales para Claude

## Regla 1 — No inventar requisitos

Si un requisito no está documentado, no debe asumirse como existente.

## Regla 2 — No asumir decisiones pendientes

Si una tecnología, arquitectura o mecanismo no está aprobado, debe considerarse pendiente.

## Regla 3 — No reutilizar arquitectura descartada

Las arquitecturas anteriores son únicamente antecedentes del proceso de diseño y no deben utilizarse para implementar el sistema actual.

## Regla 4 — Consultar la documentación específica

Para cada tarea se debe consultar el documento correspondiente:

```text
Contexto              → 00_CONTEXT.md
Requisitos            → 01_REQUISITOS.md
Arquitectura          → 02_ARQUITECTURA.md
Base de datos         → 03_BASE_DE_DATOS.md
API                   → 04_API.md
Biometría             → 05_BIOMETRIA.md
Seguridad             → 06_SEGURIDAD.md
Decisiones técnicas   → 07_DECISIONES_TECNICAS.md
Estado del proyecto   → 08_ESTADO_PROYECTO.md
Reglas de desarrollo  → 09_REGLAS_DESARROLLO.md
```

## Regla 5 — No cambiar arquitectura automáticamente

Si una mejora técnica implica modificar la arquitectura, debe presentarse primero como propuesta.

## Regla 6 — Mantener trazabilidad

Las funcionalidades implementadas deben poder relacionarse con requisitos.

## Regla 7 — Evitar sobreingeniería

No agregar tecnologías, microservicios, patrones, endpoints o componentes que no sean necesarios para los requisitos.

## Regla 8 — Mantener separación de responsabilidades

Java, Python, frontend, base de datos e infraestructura deben tener responsabilidades claramente definidas cuando la arquitectura lo establezca.

## Regla 9 — Código mantenible

El código debe ser:

- organizado;
- legible;
- modular;
- documentado cuando sea necesario;
- reproducible;
- coherente con la arquitectura aprobada.

## Regla 10 — Señalar contradicciones

Si dos documentos contienen información incompatible, Claude debe señalar la contradicción y no escoger arbitrariamente una de las alternativas.

---

# 17. Decisiones pendientes

Las siguientes decisiones deben definirse y registrarse antes de utilizarlas como decisiones definitivas de implementación:

- arquitectura definitiva del sistema;
- tecnología definitiva del frontend;
- estructura definitiva del backend;
- comunicación entre Java y Python;
- mecanismo de autenticación;
- mecanismo de autorización;
- estrategia de sesiones/JWT, si corresponde;
- modelo biométrico definitivo;
- estrategia anti-spoofing;
- almacenamiento de información de voz;
- estrategia de almacenamiento de archivos de audio;
- umbrales biométricos;
- endpoints definitivos;
- modelo definitivo de base de datos;
- infraestructura definitiva en GCP;
- estrategia de despliegue;
- mecanismos concretos de seguridad.

Estas decisiones deben registrarse en:

`07_DECISIONES_TECNICAS.md`

---

# 18. Fuentes de verdad del proyecto

Para evitar inconsistencias, se utilizará la siguiente jerarquía conceptual:

### 1. Requisitos aprobados

`01_REQUISITOS.md`

Define **qué debe hacer el sistema**.

### 2. Arquitectura aprobada

`02_ARQUITECTURA.md`

Define **cómo se estructura el sistema**.

### 3. Decisiones técnicas aprobadas

`07_DECISIONES_TECNICAS.md`

Define **qué tecnologías y decisiones específicas han sido seleccionadas**.

### 4. Documentación especializada

Los documentos de base de datos, API, biometría y seguridad detallan cada área.

### 5. Código

El código representa la implementación de las decisiones anteriores y no debe convertirse automáticamente en la fuente de verdad arquitectónica.

---

# 19. Regla principal del proyecto

> **Nayra debe implementarse a partir de los requisitos y decisiones arquitectónicas aprobadas, no a partir de suposiciones ni de propuestas descartadas.**

En caso de que una decisión necesaria para programar todavía no esté definida:

1. identificar la decisión pendiente;
2. explicar por qué es necesaria;
3. presentar alternativas técnicas si se solicita;
4. esperar la decisión correspondiente;
5. documentar la decisión antes de incorporarla al código.

---

# 20. Documentos relacionados

Este archivo constituye el contexto general del proyecto y debe utilizarse junto con:

```text
01_REQUISITOS.md
02_ARQUITECTURA.md
03_BASE_DE_DATOS.md
04_API.md
05_BIOMETRIA.md
06_SEGURIDAD.md
07_DECISIONES_TECNICAS.md
08_ESTADO_PROYECTO.md
09_REGLAS_DESARROLLO.md
```

Los detalles técnicos no deben duplicarse innecesariamente en este documento. Cada aspecto debe mantenerse en su documento específico para evitar versiones contradictorias.


## Decisiones de alcance incorporadas

### Infraestructura de aplicación
El sistema contempla **2 instancias de aplicación**, ubicadas en **zonas diferentes**, con el objetivo de mejorar la disponibilidad y permitir la continuidad del servicio ante una eventual falla que afecte a una de las zonas.

Esta definición forma parte del diseño actual. No se deben asumir componentes de infraestructura adicionales (por ejemplo, balanceador, DMZ, VPC, API Gateway u otros) si no han sido aprobados explícitamente en la documentación de arquitectura.

### Entorno bancario simulado
Nayra **no contempla una integración directa con bancos reales** dentro del alcance del proyecto. Para el desarrollo, las pruebas y la validación se utilizará un **entorno controlado y simulado**, en el que se representarán entidades bancarias, cuentas y operaciones necesarias para demostrar el funcionamiento de la solución.

Las transacciones del prototipo serán **simuladas y no involucrarán fondos reales ni cuentas bancarias reales**. Por ello, las entidades `CUENTAS` y `OPERACIONES` deben interpretarse dentro del contexto del entorno de prueba de Nayra, salvo que posteriormente se apruebe un cambio de alcance.
