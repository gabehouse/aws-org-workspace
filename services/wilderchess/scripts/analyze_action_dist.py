import pandas as pd
import glob
import os
from collections import Counter


def analyze_detailed_meta(directory='ml_data/'):
    files = glob.glob(os.path.join(directory, "*_SUCCESS_*.csv"))
    if not files:
        print("No successful game logs found.")
        return

    # Category Mapping
    cat_map = {0: "NONE", 1: "ATTACK", 2: "BLOCK", 3: "SUPPORT",
               4: "DEBUFF", 5: "ULT", 6: "MOVE", 7: "BENCH"}

    # ---------------------------------------------------------
    # PASTE YOUR ActionEnum MAPPING HERE
    # ---------------------------------------------------------
    ability_map = {
        0: "NONE",
        1: "MOVE",
        2: "BENCH",
        3: "UNBENCH",
        4: "CHARGE",
        5: "HACK",
        6: "SHIELD_STRIKE",
        7: "TOSS",
        8: "RUNNING_START",
        12: "KICK",
        13: "INTERVENTION",
        14: "GUST",
        15: "HYMN",
        17: "FIREBALL",
        18: "IGNITE",
        19: "INFERNO",
        21: "DEFEND",
        22: "LICK_WOUNDS",
        23: "POUNCE",
        24: "SLASH",
        25: "BENCH_COACH",
        28: "TAXIDERMY",
        30: "SHELL_STANCE",
        31: "REFLECT",
        32: "SMACK",
        33: "SOOTHE",
        34: "STOUT_SHIELD",
        36: "MELD",
        38: "STAB",
        39: "BRAY",
        40: "DONKEY_BLUES",
        41: "INVIGORATE",
        42: "MERCY",
        43: "NEWTBREW",
        44: "STOKE",
        45: "CONTAGION",
        46: "EXECUTE",
        47: "TRANQUILIZER",
        48: "SHADOW_LUNGE",
        49: "CRIMBERRY_PACK",
        50: "OIL_BOMB",
        51: "COXCOMB",
        52: "SMOKE_BOMB",
        53: "THIEF_GLOVES",
        54: "WAR_HORN"
    }

    all_cats = []
    ability_counts = Counter()
    total_games = len(files)

    print(f"Analyzing {total_games} games...")

    for f in files:
        df = pd.read_csv(f)
        print(f"Reading: {f}")
        # Checking all 10 slots for both players
        for i in range(10):
            for prefix in ['pA_', 'pB_']:
                c_col = f'{prefix}act{i}_cat'
                a_col = f'{prefix}act{i}_abilityId'

                if c_col in df.columns and a_col in df.columns:
                    # Only count rows where an action actually occurred
                    active_rows = df[df[c_col] > 0]
                    all_cats.extend(active_rows[c_col].tolist())
                    ability_counts.update(active_rows[a_col].tolist())

    # 1. Category Distribution
    print("\n" + "="*45)
    print(f"{'STRATEGY CATEGORY':<18} | {'DISTRIBUTION'}")
    print("-" * 45)
    cat_series = pd.Series(all_cats)
    cat_dist = cat_series.value_counts(normalize=True) * 100
    for val, pct in cat_dist.sort_index().items():
        name = cat_map.get(int(val), f"ID {val}")
        print(f"{name:<18} | {pct:>10.2f}%")

# 2. Complete Ability Tally (Including 0-usage actions)
    print("\n" + "="*55)
    print(f"{'ABILITY NAME':<20} | {'ID':<4} | {'TOTAL':<8} | {'PER GAME'}")
    print("-" * 55)

    # Sort the map by ID or Name, but iterate through the MAP, not the counts
    for abil_id in sorted(ability_map.keys()):
        name = ability_map[abil_id]
        count = ability_counts[abil_id]  # Counter returns 0 if ID isn't found
        avg_per_game = count / total_games

        # Highlight dead actions for easier spotting
        status = " [!!]" if count == 0 else ""

        print(f"{name:<20} | {abil_id:<4} | {count:<8} | {avg_per_game:>8.2f}{status}")

    print("="*45)


if __name__ == "__main__":
    analyze_detailed_meta()
