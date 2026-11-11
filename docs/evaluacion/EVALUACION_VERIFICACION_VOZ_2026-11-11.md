# Evaluación preliminar de la verificación de voz con modelos reales — 2026-11-11

> **Alcance.** Primera medición con los modelos **reales** del servicio (`speechbrain/spkrec-ecapa-voxceleb`,
> D-011, y AASIST, D-012), **sin reentrenarlos**, sobre un corpus **público** de voces peruanas. Es una referencia
> de desarrollo para D-055: **no** es la calibración con voluntarios de D-060 (dataset y consentimiento siguen
> PENDIENTES) y **no** cambia `Nayra-Voz/config/parametros_provisionales.yaml`. Los umbrales 0.80 / 0.90 siguen
> vigentes hasta que el equipo decida.

## 1. Por qué no se entrena un modelo

ECAPA-TDNN ya está entrenado con VoxCeleb (miles de hablantes) y produce una huella de voz de 192 dimensiones;
AASIST ya está entrenado con ASVspoof 2019 LA. Nayra solo necesita (a) enrolar: promedio de 3 huellas, ya
implementado en `Pipeline.finalizar_enrolamiento`, y (b) **elegir el umbral con datos**, que es lo que faltaba.
La única pieza «entrenada» en este trabajo es un **calibrador logístico de 2 parámetros** sobre los puntajes
coseno (`nayra_voz/evaluacion/calibrador.py`), que traduce la similitud a probabilidad de «misma persona». No
reemplaza a ECAPA ni está conectado al servicio.

## 2. Datos y protocolo

| Aspecto | Valor |
|---|---|
| Corpus | OpenSLR 73, español de Perú (`es_pe_female`, `es_pe_male`), CC BY-SA 4.0; frases leídas, 48 kHz remuestreadas a 16 kHz |
| Personas | 38 (18 mujeres, 20 hombres); 40 grabaciones por persona → 1518 grabaciones |
| Partición | por persona, disjunta: **desarrollo** 21 personas (elegir umbral, entrenar calibrador) / **prueba** 17 personas (informar tasas) |
| Enrolamiento | centroide de 3 grabaciones (`enrolamiento.muestras_validas`), como en el servicio |
| Intentos en prueba | 627 genuinos; 10 032 de impostor (4720 del mismo género) |
| Tipo de impostor | otra persona habla con su propia voz (no grabaciones ni voz sintética) |
| Duraciones | frase completa (~4–8 s) y **recorte central de 3 s**, parecido a la frase del desafío (D-054) |

Herramienta: `python -m nayra_voz.evaluacion.evaluar` (ver `Nayra-Voz/README.md`). Resultados completos en
`docs/evaluacion/resultados_2026-11-11/`.

## 3. Resultados de verificación (conjunto de PRUEBA)

Los umbrales se eligieron en desarrollo y se miden en prueba. FAR = impostor aceptado; FRR = persona correcta
rechazada. IC 95 % de Wilson entre corchetes.

### Recorte de 3 s (lo más parecido al desafío)

| Criterio (elegido en desarrollo) | Umbral coseno | FAR | FAR mismo género | FRR |
|---|---|---|---|---|
| **Vigente D-055** | **0.80** | 0 [0 – 0.04 %] | 0 | **88.0 %** [85.3 – 90.4 %] |
| EER | 0.441 | 1.67 % | 3.54 % | 1.60 % |
| FAR ≤ 1 % | 0.483 | 0.54 % | 1.14 % | 2.7 % |
| FAR ≤ 0.1 % | 0.564 | 0.01 % [0.002 – 0.06 %] | 0.02 % | 8.0 % [6.1 – 10.4 %] |
| minDCF (p = 0.01) | 0.575 | 0 [0 – 0.04 %] | 0 | 8.6 % |

EER en prueba: 1.6 % (todos), 1.8 % (mismo género).

### Frase completa

| Criterio | Umbral | FAR | FAR mismo género | FRR |
|---|---|---|---|---|
| **Vigente D-055** | **0.80** | 0 | 0 | **52.6 %** [48.7 – 56.5 %] |
| FAR ≤ 0.1 % | 0.608 | 0.04 % | 0.09 % | 4.3 % |
| minDCF (p = 0.01) | 0.653 | 0 | 0 | 7.8 % |

EER en prueba: 0.8 % (todos), 1.2 % (mismo género).

**Lectura.** Con 0.80 casi nadie entraría: la mediana de similitud de la persona correcta es 0.72 con 3 s
(0.80 con frase completa), así que 0.80 rechaza a 9 de cada 10 personas legítimas. El modelo sí separa bien a
las personas (EER ≈ 1–2 %); el problema es el umbral. Un umbral cercano a **0.56–0.60** mantiene la FAR en el
orden de 0.1 % o menos con un FRR de 4–8 % en estos datos.

**Calibrador** (3 s): `P = 1 / (1 + e^-(17.46·s − 8.04))`. Con él, 0.80 equivale a P = 0.997 y P = 0.99
corresponde a coseno 0.72. Entropía cruzada en prueba: 0.14 bits (1 bit = no informa; menor es mejor).

## 4. Anti-spoofing (AASIST) con voz real

Solo mide cuántas grabaciones **reales** rechazaría el umbral bona fide 0.90. **No** mide la detección de
ataques: el corpus no tiene audio falso.

| Entrada a AASIST (300 grabaciones) | Voz real rechazada con 0.90 |
|---|---|
| Frase completa (primeros 4 s, con silencios) | 3.3 % [1.8 – 6.0 %] |
| Recorte de 3 s **sin** silencio alrededor | **77.0 %** [71.9 – 81.4 %] |
| Recorte de 3 s **con 0.5 s de silencio** antes y después | 5.3 % [3.3 – 8.5 %] |

**Hallazgo importante.** AASIST depende del silencio que rodea a la voz: el mismo audio pasa o falla según
tenga silencio al inicio y al final. Es una debilidad conocida de los modelos entrenados con ASVspoof 2019
(el silencio actúa como atajo). Consecuencias para Nayra:

1. La app graba desde que la persona toca hasta que vuelve a tocar, así que normalmente habrá silencio y la
   tasa de rechazo real debería parecerse al 5 %. Si se recorta el silencio (por ejemplo, con el VAD) antes de
   AASIST, la tasa se dispararía. **No recortar el audio antes de AASIST.**
2. Un atacante podría aprovechar el mismo atajo (agregar silencio a una grabación). La protección real de
   Nayra contra reproducción es el **desafío variable** (D-054), no solo AASIST. Esto refuerza dejar AASIST
   como una capa más y medir ataques reales antes de presentarlo como protección.

## 5. Limitaciones (no omitir al citar estos números)

- Corpus de lectura en condiciones relativamente limpias y **una sola sesión** por persona: la verificación real
  (otro día, otro celular, ruido) será peor. El FRR real será mayor.
- 38 personas: la FAR mínima medible en prueba es ~0.01 % y los intervalos son amplios.
- No hay personas con discapacidad visual ni grabaciones con el celular de la app: es justamente lo que D-060
  debe aportar.
- No se midieron ataques (reproducción, voz sintética, conversión de voz).
- El recorte de 3 s es una aproximación: no es la frase «palabra + 3 dígitos + palabra».

## 6. Qué falta para cerrar D-055

1. **Decisión del equipo:** mantener 0.80 o adoptar un umbral provisional informado por esta medición
   (p. ej. 0.60). Cualquier valor sigue siendo provisional hasta D-060.
2. **D-060:** grabar voluntarios con la app (consentimiento), al menos 2 sesiones por persona, y correr la misma
   herramienta con `--manifiesto` y `--sin-cache`.
3. Medir ataques de reproducción con el celular para AASIST y el desafío.
