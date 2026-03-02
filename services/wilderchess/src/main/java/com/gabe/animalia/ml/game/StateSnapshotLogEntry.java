package com.gabe.animalia.ml.game;

// NOTE: TwoPlayerGameState definition is omitted for maximum simplicity,
// using Object as a placeholder for the payload type.
public class StateSnapshotLogEntry {
    private String snapshotId;
    private TwoPlayerGameState state;

    public StateSnapshotLogEntry() {}

    public StateSnapshotLogEntry(String snapshotId, TwoPlayerGameState state) {
        this.snapshotId = snapshotId;
        this.state = state;
    }

    public String getSnapshotId() { return snapshotId; }
    public void setSnapshotId(String snapshotId) { this.snapshotId = snapshotId; }
    public TwoPlayerGameState getState() { return state; }
    public void setState(TwoPlayerGameState state) { this.state = state; }
}
