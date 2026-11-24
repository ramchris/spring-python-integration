"""FastAPI service exposing training and prediction for the Iris model."""
from __future__ import annotations

import json
import threading
from contextlib import asynccontextmanager

import joblib
import numpy as np
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from app import train as trainer


class _ModelStore:
    """Holds the loaded model; a lock keeps retraining and prediction from colliding."""

    def __init__(self) -> None:
        self.model = None
        self.metadata: dict | None = None
        self.lock = threading.RLock()

    def load(self) -> bool:
        with self.lock:
            if not trainer.MODEL_PATH.exists():
                return False
            self.model = joblib.load(trainer.MODEL_PATH)
            self.metadata = json.loads(trainer.META_PATH.read_text())
            return True


store = _ModelStore()


@asynccontextmanager
async def lifespan(_: FastAPI):
    if not store.load():
        trainer.train()  # first boot with no artifact: train once
        store.load()
    yield


app = FastAPI(title="Iris Model Service", version="0.1.0", lifespan=lifespan)


class PredictRequest(BaseModel):
    sepal_length: float = Field(..., gt=0, examples=[5.1])
    sepal_width: float = Field(..., gt=0, examples=[3.5])
    petal_length: float = Field(..., gt=0, examples=[1.4])
    petal_width: float = Field(..., gt=0, examples=[0.2])


class PredictResponse(BaseModel):
    label: str
    class_index: int
    probabilities: dict[str, float]


class TrainRequest(BaseModel):
    n_estimators: int = Field(100, ge=1, le=1000)
    max_depth: int | None = Field(None, ge=1, le=50)


@app.get("/health")
def health() -> dict:
    return {"status": "UP", "model_loaded": store.model is not None}


@app.get("/model")
def model_info() -> dict:
    if store.metadata is None:
        raise HTTPException(status_code=503, detail="Model not loaded")
    return store.metadata


@app.post("/train")
def train_endpoint(req: TrainRequest | None = None) -> dict:
    req = req or TrainRequest()
    with store.lock:
        metadata = trainer.train(n_estimators=req.n_estimators, max_depth=req.max_depth)
        store.load()
    return metadata


@app.post("/predict", response_model=PredictResponse)
def predict(req: PredictRequest) -> PredictResponse:
    with store.lock:
        if store.model is None or store.metadata is None:
            raise HTTPException(status_code=503, detail="Model not loaded")
        x = np.array([[req.sepal_length, req.sepal_width, req.petal_length, req.petal_width]])
        probs = store.model.predict_proba(x)[0]
        classes = store.metadata["classes"]
    idx = int(np.argmax(probs))
    return PredictResponse(
        label=classes[idx],
        class_index=idx,
        probabilities={c: round(float(p), 4) for c, p in zip(classes, probs)},
    )
