import os
import tempfile

# Must run before app.train is imported so tests never touch the real artifacts/ folder.
os.environ["MODEL_ARTIFACT_DIR"] = tempfile.mkdtemp(prefix="model-test-")
