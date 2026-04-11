import pandas as pd
import fastparquet

# 1. Load just the metadata/first row to save RAM
pf = fastparquet.ParquetFile("master_data.parquet")
first_row = pf[0].to_pandas().head(1)

# 2. Extract feature names (excluding your labels/deltas)
# This should match your get_java_aligned_features() exactly
all_cols = first_row.columns.tolist()
feature_cols = [c for c in all_cols if c not in [
    'final_win_label', 'p1_hp_delta', 'p2_hp_delta']]

# 3. Validation Prints
print(f"📊 Total Columns in Parquet: {len(all_cols)}")
print(f"🎯 Feature Columns Count: {len(feature_cols)}")
print("-" * 30)
print("First 10 Features:", feature_cols[:10])
print("Last 10 Features:", feature_cols[-10:])

if len(feature_cols) == 351:
    print("\n✅ Match! The dimension is exactly 351.")
else:
    print(
        f"\n❌ Mismatch! Found {len(feature_cols)} features. Check your compacting script.")
