"""Etapa de calidad de audio (D-058): voz neta, SNR estimada y saturación.

Herramienta de detección de actividad de voz: WebRTC VAD.
PROVISIONAL — candidato inicial para el prototipo, pendiente de prueba técnica.
"""
from __future__ import annotations

import numpy as np

from .audio import Audio
from .config import ParametrosCalidad
from .resultados import Etapa, ResultadoEtapa

_UMBRAL_SATURACION = 32767 * 0.99


class EvaluadorCalidad:
    def __init__(self, parametros: ParametrosCalidad):
        if parametros.herramienta_vad != "webrtcvad":
            raise ValueError(f"Herramienta de VAD no soportada: {parametros.herramienta_vad}")
        import webrtcvad

        self._parametros = parametros
        self._vad = webrtcvad.Vad(parametros.vad_agresividad)

    def evaluar(self, audio: Audio) -> ResultadoEtapa:
        p = self._parametros
        muestras_por_trama = audio.frecuencia_hz * p.vad_trama_ms // 1000
        bytes_por_trama = muestras_por_trama * 2
        enteros = np.frombuffer(audio.pcm16, dtype="<i2")

        energia_voz: list[float] = []
        energia_silencio: list[float] = []
        tramas_voz = 0
        for inicio in range(0, len(audio.pcm16) - bytes_por_trama + 1, bytes_por_trama):
            trama = audio.pcm16[inicio:inicio + bytes_por_trama]
            bloque = enteros[inicio // 2:inicio // 2 + muestras_por_trama].astype(np.float64)
            energia = float(np.mean(bloque ** 2)) + 1e-9
            if self._vad.is_speech(trama, audio.frecuencia_hz):
                tramas_voz += 1
                energia_voz.append(energia)
            else:
                energia_silencio.append(energia)

        voz_neta_s = tramas_voz * p.vad_trama_ms / 1000.0
        if energia_voz and energia_silencio:
            snr_db = 10.0 * np.log10(np.mean(energia_voz) / np.mean(energia_silencio))
        elif energia_voz:
            snr_db = float("inf")  # sin tramas de silencio para estimar el ruido
        else:
            snr_db = 0.0
        saturacion = float(np.mean(np.abs(enteros) >= _UMBRAL_SATURACION)) if len(enteros) else 0.0

        fallos = []
        if voz_neta_s < p.voz_neta_minima_s:
            fallos.append("VOZ_INSUFICIENTE")
        if snr_db < p.snr_minimo_db:
            fallos.append("RUIDO_EXCESIVO")
        if saturacion > p.saturacion_maxima_fraccion:
            fallos.append("SATURACION")

        return ResultadoEtapa(
            etapa=Etapa.CALIDAD,
            aprobada=not fallos,
            puntaje=None,
            umbral=None,
            detalle={
                "vozNetaS": round(voz_neta_s, 3),
                "snrDb": None if snr_db == float("inf") else round(float(snr_db), 2),
                "saturacion": round(saturacion, 5),
                "fallos": fallos,
            },
        )
