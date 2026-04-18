import pandas as pd
import glob
import numpy as np
from pathlib import Path

# Configuration
DATA_DIR = "ml_data"
THRESHOLD = 1e-5  # Tolerance for floating point differences


def verify_file(file_path):
    df = pd.read_csv(file_path)

    # 1. Use the actual columns present in your CSV
    p1_start_cols = [f'pA_f{i}_hp_pct' for i in range(4)]
    p1_end_cols = [f'pA_final_f{i}_hp_pct' for i in range(4)]

    # 2. Calculate Team HP Totals from percentages
    # If Java: (RawFinal - RawInitial) / 500
    # And CSV has: Pct = Raw / 100
    # Then Python needs: (SumPctFinal * 100 - SumPctInitial * 100) / 500
    p1_start_total_raw = df[p1_start_cols].sum(axis=1) * 100
    p1_end_total_raw = df[p1_end_cols].sum(axis=1) * 100

    # 3. Calculate the Expected Delta
    # We multiply by 100 above to get back to the "Raw" scale your Java code uses
    calculated_delta = (p1_end_total_raw - p1_start_total_raw) / 500.0

    # 4. Comparison with a slightly wider threshold for string rounding
    # Since Java uses %.4f, 1e-4 is the most precise we can be
    differences = (df['p1_hp_delta'] - calculated_delta).abs()
    mismatches = differences > 1e-4

    num_mismatches = mismatches.sum()
    max_error = differences.max()

    return len(df), num_mismatches, max_error


# Execution
csv_files = glob.glob(f"{DATA_DIR}/*.csv")

if not csv_files:
    print(f"❌ No CSV files found in {DATA_DIR}")
else:
    print(f"--- Global Data Verification ---")
    total_rows = 0
    total_mismatches = 0
    file_count = 0

    for f in sorted(csv_files):
        rows, errors, biggest_gap = verify_file(f)
        total_rows += rows
        total_mismatches += errors
        file_count += 1

        status = "✅" if errors == 0 else "❌"
        print(
            f"{status} {Path(f).name}: {errors}/{rows} mismatches (Max Error: {biggest_gap:.6f})")

    print(f"\n--- Final Summary ---")
    print(f"Files Scanned: {file_count}")
    print(f"Total Rows:    {total_rows}")
    print(f"Total Errors:  {total_mismatches}")

    if total_mismatches == 0:
        print("🏆 All data is mathematically consistent. Ready for training.")
    else:
        error_rate = (total_mismatches / total_rows) * 100
        print(
            f"⚠️ Error Rate: {error_rate:.2f}%. Check for logic drift in the Java 'checkDeath' sequence.")
