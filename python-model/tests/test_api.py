from fastapi.testclient import TestClient

from app.main import app


def test_health_and_predict():
    with TestClient(app) as client:  # context manager runs the lifespan (loads/trains model)
        assert client.get("/health").json()["model_loaded"] is True

        r = client.post("/predict", json={
            "sepal_length": 5.1, "sepal_width": 3.5, "petal_length": 1.4, "petal_width": 0.2,
        })
        assert r.status_code == 200
        body = r.json()
        assert body["label"] == "setosa"
        assert abs(sum(body["probabilities"].values()) - 1.0) < 1e-3


def test_retrain_and_validation():
    with TestClient(app) as client:
        r = client.post("/train", json={"n_estimators": 20, "max_depth": 3})
        assert r.status_code == 200
        assert r.json()["n_estimators"] == 20
        assert r.json()["test_accuracy"] > 0.8

        bad = client.post("/predict", json={"sepal_length": -1, "sepal_width": 3,
                                            "petal_length": 1, "petal_width": 0.2})
        assert bad.status_code == 422
