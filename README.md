# ml-platform — Spring Boot 3.5 (Java 21) + Python FastAPI model, built with Gradle

```
ml-platform/
├── build.gradle / settings.gradle    root Gradle build (multi-project)
├── spring-app/                       Spring Boot 3.5.7, Java 21  → :8080
│   └── RestClient → calls the Python service
└── python-model/                     FastAPI + scikit-learn      → :8000
    ├── app/train.py                  trains RandomForest on Iris → artifacts/model.joblib
    ├── app/main.py                   /health /model /train /predict
    └── build.gradle                  Gradle tasks that drive Python (venv, pip, train, test, run)
```

## Prerequisites
- JDK 21
- Python 3.10+ on PATH as `python3` (or pass `-PpythonExe=/path/to/python`)
- No Gradle install needed — use `./gradlew` (Windows: `gradlew.bat`)

## Run it (two terminals)
```bash
# 1. Train the model and start the Python service (creates python-model/.venv automatically)
./gradlew :python-model:runModelServer

# 2. Start Spring Boot
./gradlew :spring-app:bootRun
```

## Or run everything with Docker
```bash
docker compose up --build        # first build takes a few minutes (Gradle + pip downloads)
docker compose down              # stop (add -v to also delete the saved model volume)
```
- The Python image trains a model at build time, so it serves predictions immediately.
- Spring Boot waits until the model service's healthcheck passes before starting.
- Models you retrain via `/train` are kept in the `model-artifacts` volume across restarts.

## Try it
```bash
# Predict through Spring Boot
curl -X POST localhost:8080/api/model/predict -H 'Content-Type: application/json' \
  -d '{"sepal_length":6.7,"sepal_width":3.0,"petal_length":5.2,"petal_width":2.3}'
# → {"label":"virginica","class_index":2,"probabilities":{...}}

# Retrain with new hyperparameters (all fields optional)
curl -X POST localhost:8080/api/model/train -H 'Content-Type: application/json' \
  -d '{"n_estimators":200,"max_depth":4}'

# Current model metadata (accuracy, trained_at, ...)
curl localhost:8080/api/model
```
FastAPI's interactive docs: http://localhost:8000/docs

## Gradle tasks
| Task | What it does |
|---|---|
| `:python-model:pipInstall` | Creates `.venv` and installs `requirements.txt` |
| `:python-model:trainModel` | Trains and writes `artifacts/model.joblib` + `metadata.json` |
| `:python-model:pyTest` | Runs pytest (uses a temp dir, never overwrites your model) |
| `:python-model:runModelServer` | Trains if needed, then serves on port 8000 (`-PpyPort=9000` to change) |
| `:spring-app:bootRun` / `:spring-app:test` | Run / test the Spring Boot app |
| `buildAll` | Python tests + training + Spring Boot jar |

## Config
`MODEL_SERVICE_URL` (default `http://localhost:8000`) tells Spring where the model lives —
set it when running the Python service in another container/host.

## Swapping in your own model
Edit `app/train.py` (data + estimator) and the `PredictRequest` fields in `app/main.py`,
then mirror the field names in `spring-app/.../dto/Dtos.java`.
