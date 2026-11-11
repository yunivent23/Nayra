"""Protocolo de ensayos de verificación 1:1 que reproduce el flujo de Nayra (D-059, D-011).

Para cada hablante: el perfil es el centroide de `muestras_enrolamiento` grabaciones (igual que
`Pipeline.finalizar_enrolamiento`, sin descarte de atípicas); las demás grabaciones son intentos genuinos.
Los intentos de impostor son las grabaciones de prueba de OTRAS personas comparadas contra ese perfil
(ataque de «otra persona habla», sin grabación ni síntesis: eso lo cubre AASIST, D-012).

Los hablantes se reparten de forma disjunta entre DESARROLLO (para elegir umbral y entrenar el calibrador) y
PRUEBA (para informar las tasas). Nunca se informa una tasa medida sobre los mismos datos con que se eligió el
umbral.
"""
from __future__ import annotations

import hashlib
from dataclasses import dataclass, field

import numpy as np

from ..locutor import centroide


@dataclass
class Hablante:
    id: str
    genero: str | None
    embeddings: list[np.ndarray] = field(default_factory=list)


@dataclass(frozen=True)
class Ensayos:
    genuinos: np.ndarray
    impostores: np.ndarray
    impostores_mismo_genero: np.ndarray
    hablantes: int


def particion(id_hablante: str, semilla: str = "nayra-d060") -> str:
    """Asigna cada hablante a «desarrollo» o «prueba» de forma estable (50 %/50 %)."""
    h = hashlib.sha256(f"{semilla}:{id_hablante}".encode()).digest()[0]
    return "desarrollo" if h % 2 == 0 else "prueba"


def construir_ensayos(hablantes: list[Hablante], muestras_enrolamiento: int, max_pruebas_por_hablante: int = 10,
                      semilla: int = 0) -> Ensayos:
    rng = np.random.default_rng(semilla)
    perfiles, pruebas, generos = [], [], []
    for h in hablantes:
        if len(h.embeddings) < muestras_enrolamiento + 1:
            continue
        orden = rng.permutation(len(h.embeddings))
        enrol = [h.embeddings[k] for k in orden[:muestras_enrolamiento]]
        resto = [h.embeddings[k] for k in orden[muestras_enrolamiento:muestras_enrolamiento + max_pruebas_por_hablante]]
        perfiles.append(centroide(enrol))
        pruebas.append(np.stack(resto))
        generos.append(h.genero)
    if len(perfiles) < 2:
        raise ValueError("Se necesitan al menos 2 hablantes con suficientes grabaciones.")

    P = np.stack(perfiles)                       # (H, D), normalizados
    genuinos, impostores, mismo_genero = [], [], []
    for b, T in enumerate(pruebas):
        T = T / np.linalg.norm(T, axis=1, keepdims=True)
        puntajes = T @ P.T                       # (n_b, H): cada prueba de b contra todos los perfiles
        genuinos.append(puntajes[:, b])
        otros = np.ones(len(P), dtype=bool)
        otros[b] = False
        impostores.append(puntajes[:, otros].ravel())
        mismo = otros & np.array([g is not None and g == generos[b] for g in generos])
        mismo_genero.append(puntajes[:, mismo].ravel())
    return Ensayos(np.concatenate(genuinos), np.concatenate(impostores), np.concatenate(mismo_genero), len(P))
