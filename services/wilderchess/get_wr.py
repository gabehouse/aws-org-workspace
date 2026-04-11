import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import glob
import os
import numpy as np
import random

# Set seed for reproducible sampling
random.seed(42)
# Configurable sample size for the portfolio chart
num_samples = 2300


def get_win_rates(directory, label_name):
    # 1. Identify all successful game logs
    files = glob.glob(os.path.join(directory, "*_SUCCESS_*.csv"))

    if not files:
        print(f"Error: No files found in {directory}")
        return {"Trial": label_name, "P1 Win Rate": 0, "Error": 0}

    # 2. Random Sampling (to remove temporal/JVM bias)
    if len(files) > num_samples:
        print(
            f"[{label_name}] Sampling {num_samples} random games from {len(files)} total.")
        files = random.sample(files, num_samples)
    else:
        print(f"[{label_name}] Processing all {len(files)} available games.")

    winners = []
    for f in files:
        try:
            # Load only the win label to save memory
            df = pd.read_csv(f, usecols=['final_win_label'], nrows=1)
            winners.append(df['final_win_label'].iloc[0])
        except Exception:
            continue

    series = pd.Series(winners)
    # 0.0 = Player 1, 1.0 = Player 2
    p1_wins = (series == 0.0).sum()
    p2_wins = (series == 1.0).sum()
    total = p1_wins + p2_wins

    if total == 0:
        return {"Trial": label_name, "P1 Win Rate": 0, "Error": 0}

    # 3. Statistical Analysis (Normal Approximation of Binomial Distribution)
    p1_rate = p1_wins / total
    # 95% Confidence Interval z-score is 1.96
    margin_of_error = 1.96 * np.sqrt((p1_rate * (1 - p1_rate)) / total)

    return {
        "Trial": label_name,
        "P1 Win Rate": p1_rate * 100,
        "Error": margin_of_error * 100
    }


# --- Execution ---
# Ensure these directory names match your actual folders
baseline = get_win_rates('baseline_data', 'Baseline (Med vs Med)')
test = get_win_rates('test_data', 'Test (AI vs Med)')

df_plot = pd.DataFrame([baseline, test])

# --- Plotting ---
# --- AWS Dark Mode Palette ---
AWS_BG = "#0f172a"          # Deep Navy/Slate background
AWS_SURFACE = "#1e293b"     # Slightly lighter surface for grid
AWS_ACCENT = "#ff9900"      # Classic AWS Orange
AWS_BLUE = "#0073bb"        # AWS Primary Blue
AWS_TEXT = "#f1f5f9"        # Off-white text for readability
AWS_GRID = "#334155"        # Muted grid lines

# --- Plotting (AWS Theme) ---
plt.figure(figsize=(10, 6), facecolor=AWS_BG)

# Set the style manually for precision
sns.set_style("darkgrid", {
    "axes.facecolor": AWS_BG,
    "grid.color": AWS_GRID,
    "axes.edgecolor": AWS_GRID,
    "figure.facecolor": AWS_BG
})

# Create the bars
bars = plt.bar(df_plot['Trial'], df_plot['P1 Win Rate'],
               yerr=df_plot['Error'],
               capsize=12,
               color=[AWS_BLUE, AWS_ACCENT],
               edgecolor='white',
               alpha=0.9,
               error_kw={'ecolor': 'white', 'lw': 2, 'capthick': 2})

# Perfect balance line using a subtle white or a brand secondary
plt.axhline(50, color='#94a3b8', linestyle='--',
            linewidth=2, label='Perfect Game Balance')

# Limits and Labels
plt.ylim(0, 100)
plt.ylabel('P1 Win Probability (%)', fontsize=11,
           fontweight='bold', color=AWS_TEXT)
plt.title(f'Wilderchess: Agent Performance Convergence\n(n={num_samples} Sampled Games per Batch)',
          fontsize=13, pad=15, color=AWS_TEXT, fontweight='bold')

# Legend Styling
legend = plt.legend(loc='lower right', frameon=True,
                    facecolor=AWS_BG, edgecolor=AWS_GRID)
plt.setp(legend.get_texts(), color=AWS_TEXT)

# White ticks for visibility
plt.tick_params(colors=AWS_TEXT, which='both')

# Add Percentage Labels
for bar in bars:
    height = bar.get_height()
    plt.text(bar.get_x() + bar.get_width()/2., height + 3,
             f'{height:.1f}%', ha='center', va='bottom',
             fontweight='bold', fontsize=11, color=AWS_TEXT)

plt.tight_layout()
plt.savefig('portfolio_aws_dark.png', dpi=300, facecolor=AWS_BG)
