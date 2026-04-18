import pandas as pd
import fastparquet
from sklearn.preprocessing import StandardScaler
import joblib

# Keep your alignment function consistent across all files


def get_java_aligned_features():
    features = ['turn_number']
    for p in ['pA', 'pB']:
        features.append(f'{p}_morale')
        for f in range(4):
            prefix = f'{p}_f{f}'
            features += [
                f'{prefix}_typeId', f'{prefix}_spot', f'{prefix}_hp_pct',
                f'{prefix}_en_pct', f'{prefix}_isAlive', f'{prefix}_isStunned'
            ]
    for p in ['pA', 'pB']:
        for a in range(10):
            prefix = f'{p}_act{a}'
            features += [
                f'{prefix}_subjId', f'{prefix}_targetId', f'{prefix}_abilityId',
                f'{prefix}_cat', f'{prefix}_statId', f'{prefix}_time',
                f'{prefix}_energy', f'{prefix}_dmg', f'{prefix}_heal',
                f'{prefix}_blk', f'{prefix}_enRes', f'{prefix}_statVal',
                f'{prefix}_dur', f'{prefix}_stack', f'{prefix}_targetType'
            ]
    return features


# 1. Initialize
scaler = StandardScaler()
pf = fastparquet.ParquetFile("master_data.parquet")

# 2. DEFINE THE EXPLICIT ORDER (REPLACED SORTED LOGIC)
feature_names = get_java_aligned_features()

print(f"📊 Features: {len(feature_names)} (Aligned with Java & Trainer)")

# 3. Stream through the file
for i, df_chunk in enumerate(pf.iter_row_groups()):
    df_chunk = df_chunk.dropna().copy()

    # Select columns in the EXPLICIT alignment order
    X_chunk = df_chunk[feature_names]

    # Update the scaler incrementally
    scaler.partial_fit(X_chunk)

    if i % 10 == 0:
        print(f"✅ Processed group {i}...")

# 4. Save results
joblib.dump(scaler, 'scaler.pkl')
joblib.dump(feature_names, 'feature_names.pkl')
print("✨ Scaler fitted and saved with Java-aligned feature order.")
