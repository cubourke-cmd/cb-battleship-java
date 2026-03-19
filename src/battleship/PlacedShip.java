package battleship;

import java.util.List;

public class PlacedShip {
    private final ShipType type;
    private final List<int[]> positions;
    private boolean sunk;

    public PlacedShip(ShipType type, List<int[]> positions) {
        this.type = type;
        this.positions = positions;
        this.sunk = false;
    }

    public ShipType getType() {
        return type;
    }

    public String getName() {
        return type.getDisplayName();
    }

    public int getSize() {
        return type.getSize();
    }

    public List<int[]> getPositions() {
        return positions;
    }

    public boolean isSunk() {
        return sunk;
    }

    public void setSunk(boolean sunk) {
        this.sunk = sunk;
    }

    public boolean checkSunk(CellState[][] board) {
        for (int[] pos : positions) {
            CellState state = board[pos[0]][pos[1]];
            if (state != CellState.HIT && state != CellState.SUNK) {
                return false;
            }
        }
        return true;
    }
}
