import onnxruntime as ort
import numpy as np

# Load the model
session = ort.InferenceSession("wilderchess.onnx")

# Create a random board state (matching your 213 inputs)
dummy_input = np.random.randn(1, 213).astype(np.float32)

# Run it!
outputs = session.run(None, {"input": dummy_input})

print("Win Probability:", outputs[0])
print("HP Deltas:", outputs[1])
print("✅ ONNX file is working perfectly!")
