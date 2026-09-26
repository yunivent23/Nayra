"""Calcula FAR, FRR, EER y el umbral para una FAR objetivo desde un CSV de puntajes.

CSV de entrada (conjunto de DESARROLLO para elegir el umbral; luego se reporta sobre PRUEBA con --umbral):
    etiqueta,puntaje
    genuino,0.71
    impostor,0.12

Ejemplos:
    python -m evaluacion.calibrar desarrollo.csv --far-objetivo 0.01
    python -m evaluacion.calibrar prueba.csv --umbral 0.63
"""
from __future__ import annotations

import argparse
import csv

import numpy as np

from .metricas import eer, far_frr, umbral_para_far


def leer(ruta: str) -> tuple[np.ndarray, np.ndarray]:
    genuinos, impostores = [], []
    with open(ruta, newline="", encoding="utf-8") as f:
        for fila in csv.DictReader(f):
            (genuinos if fila["etiqueta"].strip() == "genuino" else impostores).append(float(fila["puntaje"]))
    if not genuinos or not impostores:
        raise SystemExit("El CSV debe tener puntajes 'genuino' e 'impostor'")
    return np.array(genuinos), np.array(impostores)


def main() -> None:
    p = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    p.add_argument("csv")
    g = p.add_mutually_exclusive_group(required=True)
    g.add_argument("--far-objetivo", type=float, help="Elegir umbral en desarrollo")
    g.add_argument("--umbral", type=float, help="Reportar un umbral ya elegido sobre el conjunto de prueba")
    a = p.parse_args()

    gen, imp = leer(a.csv)
    valor_eer, umbral_eer = eer(gen, imp)
    print(f"Pares genuinos: {len(gen)}  impostores: {len(imp)}")
    print(f"EER: {valor_eer:.4f} (umbral {umbral_eer:.4f})")
    if a.far_objetivo is not None:
        u, far, frr = umbral_para_far(gen, imp, a.far_objetivo)
        print(f"Umbral para FAR <= {a.far_objetivo}: {u:.4f}  FAR={far:.4f}  FRR={frr:.4f}")
    else:
        far, frr = far_frr(gen, imp, a.umbral)
        print(f"Umbral {a.umbral:.4f}: FAR={far:.4f}  FRR={frr:.4f}")


if __name__ == "__main__":
    main()
