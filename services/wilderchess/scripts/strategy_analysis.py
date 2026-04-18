import pandas as pd
import seaborn as sns
import matplotlib.pyplot as plt
import glob
import os

# 1. Configuration & Data Loading
path = 'test_data'
all_files = glob.glob(os.path.join(path, "*.csv"))

ability_map = {
    0: "NONE", 1: "MOVE", 2: "BENCH", 3: "UNBENCH", 4: "CHARGE",
    5: "HACK", 6: "SHIELD_STRIKE", 7: "TOSS", 8: "RUNNING_START",
    12: "KICK", 13: "INTERVENTION", 14: "GUST", 15: "HYMN",
    17: "FIREBALL", 18: "IGNITE", 19: "INFERNO", 21: "DEFEND",
    22: "LICK_WOUNDS", 23: "POUNCE", 24: "SLASH", 25: "BENCH_COACH",
    28: "TAXIDERMY", 30: "SHELL_STANCE", 31: "REFLECT", 32: "SMACK",
    33: "SOOTHE", 34: "STOUT_SHIELD", 36: "MELD", 38: "STAB",
    39: "BRAY", 40: "DONKEY_BLUES", 41: "INVIGORATE", 42: "MERCY",
    43: "NEWTBREW", 44: "STOKE", 45: "CONTAGION", 46: "EXECUTE",
    47: "TRANQUILIZER", 48: "SHADOW_LUNGE", 49: "CRIMBERRY_PACK",
    50: "OIL_BOMB", 51: "COXCOMB", 52: "SMOKE_BOMB",
    53: "THIEF_GLOVES", 54: "WAR_HORN"
}

if not all_files:
    print(f"Error: No CSV files found in {path}/")
    exit()

print(f"Analyzing {len(all_files)} simulation files...")
df = pd.concat((pd.read_csv(f) for f in all_files), ignore_index=True)

# 2. Extraction Logic with Ability Mapping


def extract_action_counts(player_prefix):
    ability_cols = [c for c in df.columns if c.startswith(
        f'{player_prefix}_act') and c.endswith('_abilityId')]
    actions = df[ability_cols].melt()['value']

    # Filter and Map to Human-Readable Names
    valid_actions = actions[actions > 0]
    return valid_actions.map(ability_map).fillna(valid_actions.astype(str))


# 3. Strategic Comparison (Neural Net vs. Heuristic)
ai_counts = extract_action_counts('pA').value_counts(normalize=True) * 100
med_counts = extract_action_counts('pB').value_counts(normalize=True) * 100

comparison = pd.DataFrame({
    'Neural Network (%)': ai_counts,
    'Heuristic Baseline (%)': med_counts
}).fillna(0)

# Calculate Strategic Variance
comparison['Strategic Variance'] = comparison['Neural Network (%)'] - \
    comparison['Heuristic Baseline (%)']
comparison = comparison.sort_values(by='Strategic Variance', ascending=False)

# 4. Heatmap Generation
plt.figure(figsize=(12, 14))
# 'RdYlGn' (Red-Yellow-Green) is great for showing preference shifts
sns.heatmap(comparison[['Neural Network (%)', 'Heuristic Baseline (%)']],
            annot=True,
            fmt=".1f",
            cmap="YlGnBu",
            cbar_kws={'label': 'Selection Frequency (%)'})

plt.title(
    f'Wilderchess: Decision Matrix Comparison\n({len(df):,} State-Action Pairs Analyzed)', fontsize=14, pad=20)
plt.ylabel('Tactical Ability')
plt.xlabel('Agent Architecture')

plt.savefig('strategy_heatmap.png', dpi=300, bbox_inches='tight')
print("Success: Strategy Heatmap saved to 'strategy_heatmap.png'")

print("\n--- TOP STRATEGIC PREFERENCES (AI over Heuristic) ---")
print(comparison['Strategic Variance'].head(5))
