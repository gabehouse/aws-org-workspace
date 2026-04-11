import pandas as pd
import glob
import os
import numpy as np


def analyze_game_length(directory='ml_data/'):
    files = glob.glob(os.path.join(directory, "*_SUCCESS_*.csv"))
    if not files:
        print("No successful game logs found.")
        return

    lengths = []

    for f in files:
        # We only need the row count, so we use low_memory to speed it up
        df = pd.read_csv(f, usecols=[0])
        lengths.append(len(df))

    lengths = np.array(lengths)

    print("--- Game Length Report ---")
    print(f"Total Games:   {len(lengths)}")
    print(f"Average (Mean): {lengths.mean():.2f} turns")
    print(f"Median:         {np.median(lengths):.0f} turns")
    print(f"Shortest Game:  {lengths.min()} turns")
    print(f"Longest Game:   {lengths.max()} turns")

    # Distribution breakdown
    print("\n--- Length Distribution ---")
    bins = [0, 5, 10, 15, 20, 30, 50]
    hist, bin_edges = np.histogram(lengths, bins=bins)
    for i in range(len(hist)):
        print(
            f"{bin_edges[i]:>2}-{bin_edges[i+1]:>2} turns: {hist[i]:>5} games ({(hist[i]/len(lengths))*100:>5.1f}%)")


if __name__ == "__main__":
    analyze_game_length()
