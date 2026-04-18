import torch
import torch.onnx
from services.wilderchess.scripts.model import AnimaliaNet


def export():
    weights_path = "best_wilderchess_model.pth"
    state_dict = torch.load(weights_path)

    input_dim = state_dict['fc1.weight'].shape[1]
    print(f"🔍 Detected input_dim: {input_dim}")

    model = AnimaliaNet(input_dim)
    model.load_state_dict(state_dict)
    model.eval()

    dummy_input = torch.randn(1, input_dim)

    torch.onnx.export(
        model,
        dummy_input,
        "wilderchess.onnx",
        export_params=True,
        opset_version=17,  # Better compatibility with your new Java library
        do_constant_folding=True,
        input_names=['input'],
        output_names=['win_prob', 'hp_deltas'],
        # Explicitly ensure we don't use external data
        # If your model is > 2GB this will fail, but yours should be small!
        # use_external_data_format=False,
        dynamic_axes={'input': {0: 'batch_size'},
                      'win_prob': {0: 'batch_size'},
                      'hp_deltas': {0: 'batch_size'}}
    )
    print("🚀 Success! Model exported to wilderchess.onnx")


if __name__ == "__main__":
    export()
