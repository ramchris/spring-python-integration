"""Trains an Iris classifier and saves it with metadata.

Run directly:  python -m app.train
Or via Gradle: ./gradlew :python-model:trainModel
"""
from __future__ import annotations

import json
import os
from datetime import datetime, timezone
from pathlib import Path

import joblib
from sklearn.datasets import load_iris
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score
from sklearn.model_selection import train_test_split

ARTIFACT_DIR = Path(
    os.environ.get("MODEL_ARTIFACT_DIR", Path(__file__).resolve().parent.parent / "artifacts")
)
MODEL_PATH = ARTIFACT_DIR / "model.joblib"
META_PATH = ARTIFACT_DIR / "metadata.json"


def train(n_estimators: int = 100, max_depth: int | None = None, random_state: int = 42) -> dict:
    data = load_iris()
    X_train, X_test, y_train, y_test = train_test_split(
        data.data, data.target, test_size=0.2, random_state=random_state, stratify=data.target
    )

    model = RandomForestClassifier(
        n_estimators=n_estimators, max_depth=max_depth, random_state=random_state
    )
    model.fit(X_train, y_train)
    accuracy = float(accuracy_score(y_test, model.predict(X_test)))

    ARTIFACT_DIR.mkdir(parents=True, exist_ok=True)
    joblib.dump(model, MODEL_PATH)

    metadata = {
        "model": "RandomForestClassifier",
        "dataset": "iris",
        "features": list(data.feature_names),
        "classes": [str(c) for c in data.target_names],
        "n_estimators": n_estimators,
        "max_depth": max_depth,
        "test_accuracy": round(accuracy, 4),
        "trained_at": datetime.now(timezone.utc).isoformat(),
    }
    META_PATH.write_text(json.dumps(metadata, indent=2))
    return metadata


if __name__ == "__main__":
    print(json.dumps(train(), indent=2))
