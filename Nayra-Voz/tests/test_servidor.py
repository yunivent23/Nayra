from fastapi.testclient import TestClient


def test_sin_modelos_el_servicio_arranca_en_503(monkeypatch, tmp_path):
    monkeypatch.setenv("NAYRA_VOZ_TOKEN_SERVICIO", "t")
    monkeypatch.setenv("NAYRA_VOZ_MODELOS", str(tmp_path))
    import importlib

    import nayra_voz.servidor as servidor

    importlib.reload(servidor)
    c = TestClient(servidor.crear_app_desde_entorno())
    assert c.get("/prototipo/v1/salud", headers={"Authorization": "Bearer t"}).json()["listo"] is False
