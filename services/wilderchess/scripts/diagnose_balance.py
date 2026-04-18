import pandas as pd
import glob
import os


def diagnose_balance(directory='ml_data/'):
    files = glob.glob(os.path.join(directory, "*.csv"))
    if not files:
        print("No files found.")
        return

    results = []
    for f in files:
        df = pd.read_csv(f)
        first_val = df['final_win_label'].iloc[0]
        last_val = df['final_win_label'].iloc[-1]

        # Check if the win label is actually being updated
        results.append({
            'file': os.path.basename(f),
            'winner': last_val,
            'is_consistent': first_val == last_val
        })

    df_results = pd.DataFrame(results)
    print("--- Diagnostic Report ---")
    print(f"Value Distribution:\n{df_results['winner'].value_counts()}")

    inconsistent = df_results[df_results['is_consistent'] == False]
    if not inconsistent.empty:
        print(
            f"ALERT: Found {len(inconsistent)} games where winner changed mid-file!")
    else:
        print(
            "Consistency Check: All games had the same winner label from turn 1 to the end.")


if __name__ == "__main__":
    diagnose_balance()
