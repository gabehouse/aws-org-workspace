import pandas as pd
import numpy as np
import glob
import os
from itertools import combinations

# --- 1. CONFIGURATION ---
CRITTER_MAP = {
    0: "None", 1: "Lion", 2: "Donkey", 3: "Newt", 4: "Wolf",
    5: "Fox", 6: "Bull", 7: "Turtle", 8: "Dove", 9: "Pig",
    10: "Heron", 11: "Bat", 12: "Hawk"
}


def run_analysis(directory='ml_data/'):
    files = glob.glob(os.path.join(directory, "*_SUCCESS_*.csv"))
    if not files:
        print(f"Error: No files found in {directory}.")
        return

    all_teams = []
    for f in files:
        try:
            # engine='python' + sep=None auto-detects delimiter
            df = pd.read_csv(f, sep=None, engine='python')

            if 'final_win_label' not in df.columns:
                continue

            outcome = df['final_win_label'].iloc[0]

            def get_team(p):
                # UPDATED: Matches your actual column names (e.g., pA_f0_typeId)
                cols = [c for c in df.columns if c.startswith(
                    f'{p}_f') and c.endswith('_typeId')]
                if not cols:
                    return None

                # Get IDs from the very first row of the log
                first_row = df.iloc[0]
                ids = []
                for col in cols:
                    val = first_row[col]
                    if pd.notnull(val) and val > 0:
                        ids.append(int(val))

                # Sort and map to names
                return [CRITTER_MAP.get(cid, f"Unknown({cid})") for cid in sorted(set(ids))]

            # Process Player A and Player B
            for p, win_val in [('pA', 0.0), ('pB', 1.0)]:
                names = get_team(p)
                if names and len(names) >= 2:
                    all_teams.append({
                        'team': ", ".join(names),
                        'list': names,
                        'win': 1 if outcome == win_val else 0
                    })
        except Exception as e:
            # print(f"Skipping {f}: {e}") # Uncomment if you want to see specific file errors
            continue

    if not all_teams:
        print("\nError: Still no teams found. Check if 'final_win_label' is at the end of the file.")
        return

    # --- ANALYSIS ---
    results_df = pd.DataFrame(all_teams)
    pair_rows = []
    for _, row in results_df.iterrows():
        # Decompose every team of 3 into its 3 possible pairs
        for pair in combinations(sorted(row['list']), 2):
            pair_rows.append({'pair': " + ".join(pair), 'win': row['win']})

    pair_stats = pd.DataFrame(pair_rows).groupby('pair')[
        'win'].agg(['count', 'sum'])
    pair_stats['Win Rate %'] = (
        pair_stats['sum'] / pair_stats['count'] * 100).round(1)

    # Filter for significant sample size (at least 5 games)
    report = pair_stats[pair_stats['count'] >= 5].sort_values(
        'Win Rate %', ascending=False)

    print("\n" + "="*60)
    print(" WILDERCHESS SYNERGY REPORT (Pairs with 5+ Games)")
    print("="*60)
    if report.empty:
        print("No pairs found with 5+ games. Try running more simulations.")
    else:
        print(report.head(15))
    print("="*60)


if __name__ == "__main__":
    run_analysis()
