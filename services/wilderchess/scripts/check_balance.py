import pandas as pd
import numpy as np
import glob
import os
from scipy import stats


def analyze_game_balance(directory='ml_data/'):
    files = glob.glob(os.path.join(directory, "*_SUCCESS_*.csv"))
    if not files:
        print(f"No successful game logs found in {directory}")
        return

    winners = []
    for f in files:
        try:
            # -1 = Tie, 0.0 = Player 1, 1.0 = Player 2
            df = pd.read_csv(f, usecols=['final_win_label'], nrows=1)
            winners.append(df['final_win_label'].iloc[0])
        except Exception as e:
            print(f"Error reading {f}: {e}")

    # 3. Calculate Distribution
    series = pd.Series(winners)
    total_games = len(series)
    counts = series.value_counts().sort_index()

    # Mapping for your site's display
    name_map = {
        -1.0: "Draw/Tie",
        0.0: "Player 1 (Agent A)",
        1.0: "Player 2 (Agent B)"
    }

    print(f"\n{'='*30}")
    print(f" STATISTICAL BALANCE REPORT")
    print(f" Total Games Analyzed: {total_games:,}")
    print(f"{'='*30}\n")

    for label, count in counts.items():
        name = name_map.get(label, f"Unknown [{label}]")
        win_rate = count / total_games

        # Calculate 95% Confidence Interval (Normal Approximation)
        # Formula: z * sqrt(p(1-p)/n)
        z = 1.96
        margin_of_error = z * \
            np.sqrt((win_rate * (1 - win_rate)) / total_games)

        lower_ci = max(0, (win_rate - margin_of_error) * 100)
        upper_ci = min(100, (win_rate + margin_of_error) * 100)

        print(
            f" {name:<20} | {count:>5} wins | {win_rate*100:>5.2f}% (±{margin_of_error*100:.2f}%)")
        print(f"   ↳ 95% CI: [{lower_ci:.1f}% - {upper_ci:.1f}%]\n")


if __name__ == "__main__":
    analyze_game_balance()
