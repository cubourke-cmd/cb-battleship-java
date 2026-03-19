package battleship;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Board {
    public static final int SIZE = 10;
    public static final String[] ROW_LABELS = {"A", "B", "C", "D", "E", "F", "G", "H", "I", "J"};

    private final CellState[][] cells;
    private final List<PlacedShip> ships;
    private final Random random;

    public Board() {
        cells = new CellState[SIZE][SIZE];
        ships = new ArrayList<>();
        random = new Random();
        clear();
    }

    public void clear() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                cells[r][c] = CellState.EMPTY;
            }
        }
        ships.clear();
    }

    public CellState getCell(int row, int col) {
        return cells[row][col];
    }

    public void setCell(int row, int col, CellState state) {
        cells[row][col] = state;
    }

    public CellState[][] getCells() {
        return cells;
    }

    public List<PlacedShip> getShips() {
        return ships;
    }

    public boolean canPlaceShip(int row, int col, int size, Orientation orientation) {
        for (int i = 0; i < size; i++) {
            int r = orientation == Orientation.VERTICAL ? row + i : row;
            int c = orientation == Orientation.HORIZONTAL ? col + i : col;
            if (r >= SIZE || c >= SIZE) return false;
            if (cells[r][c] != CellState.EMPTY) return false;
        }
        return true;
    }

    public PlacedShip placeShip(ShipType type, int row, int col, Orientation orientation) {
        List<int[]> positions = new ArrayList<>();
        for (int i = 0; i < type.getSize(); i++) {
            int r = orientation == Orientation.VERTICAL ? row + i : row;
            int c = orientation == Orientation.HORIZONTAL ? col + i : col;
            cells[r][c] = CellState.SHIP;
            positions.add(new int[]{r, c});
        }
        PlacedShip ship = new PlacedShip(type, positions);
        ships.add(ship);
        return ship;
    }

    public void placeShipsRandomly() {
        clear();
        for (ShipType type : ShipType.values()) {
            boolean placed = false;
            while (!placed) {
                Orientation orientation = random.nextBoolean() ? Orientation.HORIZONTAL : Orientation.VERTICAL;
                int row = random.nextInt(SIZE);
                int col = random.nextInt(SIZE);
                if (canPlaceShip(row, col, type.getSize(), orientation)) {
                    placeShip(type, row, col, orientation);
                    placed = true;
                }
            }
        }
    }

    public boolean allShipsSunk() {
        return ships.stream().allMatch(PlacedShip::isSunk);
    }

    /**
     * Fire at a cell. Returns true if it was a hit.
     */
    public boolean fireAt(int row, int col) {
        if (cells[row][col] == CellState.SHIP) {
            cells[row][col] = CellState.HIT;
            // Check if any ship is now sunk
            for (PlacedShip ship : ships) {
                if (!ship.isSunk() && ship.checkSunk(cells)) {
                    ship.setSunk(true);
                    markSunk(ship);
                }
            }
            return true;
        } else if (cells[row][col] == CellState.EMPTY) {
            cells[row][col] = CellState.MISS;
            return false;
        }
        return false;
    }

    private void markSunk(PlacedShip ship) {
        for (int[] pos : ship.getPositions()) {
            cells[pos[0]][pos[1]] = CellState.SUNK;
        }
    }

    public PlacedShip getLastSunkShip() {
        for (PlacedShip ship : ships) {
            if (ship.isSunk()) {
                for (int[] pos : ship.getPositions()) {
                    if (cells[pos[0]][pos[1]] == CellState.SUNK) {
                        return ship;
                    }
                }
            }
        }
        return null;
    }

    public PlacedShip getShipSunkAt(int row, int col) {
        for (PlacedShip ship : ships) {
            if (ship.isSunk()) {
                for (int[] pos : ship.getPositions()) {
                    if (pos[0] == row && pos[1] == col) {
                        return ship;
                    }
                }
            }
        }
        return null;
    }
}
