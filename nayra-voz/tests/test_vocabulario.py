from pathlib import Path

from nayra_voz.config import RAIZ
from nayra_voz.contenido import coincide
from nayra_voz.vocabulario import DIGITOS, cargar_palabras, gramatica

LISTA_JAVA = Path(__file__).resolve().parents[2] / "Nayra-Back/src/main/resources/challenge-words.es.txt"


def test_lista_identica_a_la_del_backend():
    assert cargar_palabras(RAIZ / "challenge-words.es.txt") == cargar_palabras(LISTA_JAVA)


def test_gramatica_contiene_palabras_digitos_y_unk():
    g = gramatica(cargar_palabras(LISTA_JAVA))
    assert set(DIGITOS) <= set(g) and g[-1] == "[unk]"
    assert not set(DIGITOS) & set(cargar_palabras(LISTA_JAVA))


def test_coincidencia_exacta_de_la_secuencia():
    assert coincide("sol cuatro siete dos mesa", "sol cuatro siete dos mesa")
    assert not coincide("sol cuatro siete mesa", "sol cuatro siete dos mesa")
    assert not coincide("sol siete cuatro dos mesa", "sol cuatro siete dos mesa")
