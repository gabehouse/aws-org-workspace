import pandas as pd

# Load the dataset
df = pd.read_csv('ml_data/training_set.csv')

# 1. Check the shape
print(f"Total turns recorded: {len(df)}")

# 2. Check the win rate (Balance check)
win_rate = df['final_win_label'].mean()
print(f"Player A Win Rate: {win_rate:.2%}")

# 3. Check for 'Dead' columns (all zeros)
zero_cols = df.columns[(df == 0).all()]
print(f"Columns with no data: {list(zero_cols)}")

# 4. Preview the first few rows
print(df.head())
