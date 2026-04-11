import pandas as pd
import torch
import torch.nn as nn
import torch.optim as optim
import numpy as np
import os
import joblib
import fastparquet
from model import AnimaliaNet


def get_java_aligned_features():
    features = ['turn_number']

    # Match appendPlayerState(features, pA) then (features, pB)
    for p in ['pA', 'pB']:
        features.append(f'{p}_morale')
        for f in range(4):  # FIGHTER_COUNT
            prefix = f'{p}_f{f}'
            features += [
                f'{prefix}_typeId', f'{prefix}_spot', f'{prefix}_hp_pct',
                f'{prefix}_en_pct', f'{prefix}_isAlive', f'{prefix}_isStunned'
            ]

    # Match appendPlayerActions(features, jointActions, 0) then (1)
    for p in ['pA', 'pB']:
        for a in range(10):  # MAX_ACTIONS_PER_PLAYER
            prefix = f'{p}_act{a}'
            features += [
                f'{prefix}_subjId', f'{prefix}_targetId', f'{prefix}_abilityId',
                f'{prefix}_cat', f'{prefix}_statId', f'{prefix}_time',
                f'{prefix}_energy', f'{prefix}_dmg', f'{prefix}_heal',
                f'{prefix}_blk', f'{prefix}_enRes', f'{prefix}_statVal',
                f'{prefix}_dur', f'{prefix}_stack', f'{prefix}_targetType'
            ]
    return features


# --- 1. SETUP & SCALER ---
# --- 1. SETUP & SCALER ---
print("📥 Initializing Streaming Trainer...")
if not os.path.exists('scaler.pkl'):
    raise FileNotFoundError("Run scaler_fit.py first!")

scaler = joblib.load('scaler.pkl')

# Open the file metadata to get column names without loading the whole thing
pf = fastparquet.ParquetFile("master_data.parquet")
all_columns = pf.columns

# Strictly use your loop-generated list for training
feature_cols = get_java_aligned_features()

print(f"✅ Input Dim: {len(feature_cols)}")

# --- 2. PREPARE VALIDATION SET ---
print("🧪 Loading validation chunk...")
val_df = pf[len(pf.row_groups) - 1].to_pandas()
val_df = val_df.dropna().copy()
val_df = val_df[val_df['final_win_label'].isin([0, 1])]

# Keep as DataFrame to preserve feature names for the scaler
v_X_df = val_df[feature_cols]
v_y_win = torch.tensor(
    val_df['final_win_label'].values, dtype=torch.float32).view(-1, 1)
v_y_del = torch.tensor(
    val_df[['p1_hp_delta', 'p2_hp_delta']].values, dtype=torch.float32)

# Scaling with DataFrame input silences the UserWarning
v_X_scaled = torch.tensor(scaler.transform(v_X_df), dtype=torch.float32)

# --- 3. SETUP MODEL ---
model = AnimaliaNet(len(feature_cols))
optimizer = optim.Adam(model.parameters(), lr=0.001)
criterion_win = nn.BCELoss()
criterion_del = nn.MSELoss()

epochs = 100
best_val_loss = float('inf')
patience = 7
counter = 0

# --- 4. TRAINING LOOP (STREAMING) ---
for epoch in range(epochs):
    model.train()
    epoch_losses = []

    for i, df_chunk in enumerate(pf.iter_row_groups()):
        if i == len(pf.row_groups) - 1:
            break

        # Data Cleaning
        df_chunk = df_chunk.dropna().copy()
        df_chunk = df_chunk[df_chunk['final_win_label'].isin([0, 1])]

        # Safety Check: Skip empty chunks
        if df_chunk.empty:
            continue

        X_df = df_chunk[feature_cols]
        y_win = torch.tensor(
            df_chunk['final_win_label'].values, dtype=torch.float32).view(-1, 1)
        y_del = torch.tensor(
            df_chunk[['p1_hp_delta', 'p2_hp_delta']].values, dtype=torch.float32)

        X_scaled = torch.tensor(scaler.transform(X_df), dtype=torch.float32)

        optimizer.zero_grad()
        pred_win, pred_del = model(X_scaled)

        loss_win = criterion_win(pred_win, y_win)
        loss_del = criterion_del(pred_del, y_del)
        total_loss = loss_win + (loss_del * 0.05)

        total_loss.backward()
        optimizer.step()
        epoch_losses.append(total_loss.item())

    # --- 5. VALIDATION PHASE ---
    model.eval()
    with torch.no_grad():
        v_win, v_del = model(v_X_scaled)

        # Calculate Validation Metrics
        v_loss_win = criterion_win(v_win, v_y_win)
        v_loss_del = criterion_del(v_del, v_y_del)
        val_loss = v_loss_win + (v_loss_del * 0.01)

        preds = (v_win > 0.5).float()
        acc = (preds == v_y_win).float().mean() * 100

        # Sanity Check: What is the model actually predicting?
        avg_pred = v_win.mean().item()

    avg_train_loss = np.mean(epoch_losses)

    # ADDED PRINTS FOR MONITORING
    print(
        f"Epoch {epoch:03d} | Train Loss: {avg_train_loss:.4f} | Val Loss: {val_loss:.4f}")
    print(
        f"          | Win Acc: {acc:.2f}% | Avg Win Pred: {avg_pred:.3f} | HP Delta MSE: {v_loss_del:.6f}")

    if val_loss < best_val_loss:
        best_val_loss = val_loss
        torch.save(model.state_dict(), 'best_wilderchess_model.pth')
        print(f"✨ New Best Model Saved!")
        counter = 0
    else:
        counter += 1
        if counter >= patience:
            print(f"🛑 Early stopping at epoch {epoch}.")
            break

print("🚀 Exporting to ONNX...")
model.load_state_dict(torch.load('best_wilderchess_model.pth'))
model.eval()
dummy_input = torch.randn(1, 351)
torch.onnx.export(model, dummy_input, "wilderchess.onnx",
                  input_names=['input'], output_names=['output'],
                  dynamic_axes={'input': {0: 'batch_size'}})
print("✨ Done! Move wilderchess.onnx to your Java resources folder.")
