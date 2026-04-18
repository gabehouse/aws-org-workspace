import torch
import torch.nn as nn


class AnimaliaNet(nn.Module):
    def __init__(self, input_dim):
        super(AnimaliaNet, self).__init__()
        # ... (Your layer definitions are perfect) ...
        self.fc1 = nn.Linear(input_dim, 256)
        self.bn1 = nn.BatchNorm1d(256)
        self.fc2 = nn.Linear(256, 128)
        self.bn2 = nn.BatchNorm1d(128)
        self.fc3 = nn.Linear(128, 64)
        self.bn3 = nn.BatchNorm1d(64)
        self.win_head = nn.Linear(64, 1)
        self.delta_head = nn.Linear(64, 2)
        self.dropout = nn.Dropout(0.4)
        self.relu = nn.ReLU()

    # --- INDENT THIS WHOLE BLOCK ---
    def forward(self, x):
        # 1. Shared Base
        x = self.relu(self.bn1(self.fc1(x)))
        x = self.dropout(x)

        # 2. Deep Reasoning Core (Size: 128)
        x = self.relu(self.bn2(self.fc2(x)))
        core_feat = self.dropout(x)

        # 3. Decision Branch (Refinement) (Size: 64)
        win_feat = self.relu(self.bn3(self.fc3(core_feat)))
        win_prob = torch.sigmoid(self.win_head(win_feat))

        # 4. Value Branch (Direct Estimation)
        # CHANGE THIS: Use win_feat (64) instead of core_feat (128)
        hp_deltas = self.delta_head(win_feat)

        return win_prob, hp_deltas
