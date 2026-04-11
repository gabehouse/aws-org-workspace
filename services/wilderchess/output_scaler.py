import joblib
import numpy as np

# Load your saved scaler
scaler = joblib.load('scaler.pkl')


def print_java_array(name, data):
    # Formats the numpy array into a Java-style {val, val, ...} string
    formatted_data = ", ".join([f"{x:.8f}f" for x in data])
    print(f"private static final float[] {name} = {{ {formatted_data} }};")


print("// Copy these into your Inference.java class\n")
print_java_array("MEAN", scaler.mean_)
print_java_array("SCALE", scaler.scale_)
