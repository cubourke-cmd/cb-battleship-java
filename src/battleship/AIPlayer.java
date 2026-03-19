package battleship;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AIPlayer {
    private enum Mode { HUNT, TARGET }

    private Mode mode;
    private final List<int[]> targets;
    private final List<int[]> hits;
    private final Random random;

    public AIPlayer() {
        mode = Mode.HUNT;
        targets = new ArrayList<>();
        hits = new ArrayList<>();
        random = new Random();
    }

    public void reset() {
        mode = Mode.HUNT;
        targets.clear();
        hits.clear();
    }

    public int[] getShot(Board playerBoard) {
        // Target mode: try adjacent cells to previous hits
        if (mode == Mode.TARGET && !targets.isEmpty()) {
            List<int[]> validTargets = new ArrayList<>();
            for (int[] t : targets) {
                int r = t[0], c = t[1];
                if (r >= 0 && r < Board.SIZE && c >= 0 && c < Board.SIZE) {
                    CellState state = playerBoard.getCell(r, c);
                    if (state != CellState.HIT && state != CellState.MISS && state != CellState.SUNK) {
                        validTargets.add(t);
                    }
                }
            }

            if (!validTargets.isEmpty()) {
                int idx = random.nextInt(validTargets.size());
                int[] shot = validTargets.get(idx);
                targets.remove(shot);
                return shot;
            } else {
                // No valid targets, go back to hunt mode
                mode = Mode.HUNT;
                targets.clear();
                hits.clear();
            }
        }

        // Hunt mode: checkerboard pattern for efficiency
        List<int[]> available = new ArrayList<>();
        for (int r = 0; r < Board.SIZE; r++) {
            for (int c = 0; c < Board.SIZE; c++) {
                CellState state = playerBoard.getCell(r, c);
                if (state != CellState.HIT && state != CellState.MISS && state != CellState.SUNK
                        && (r + c) % 2 == 0) {
                    available.add(new int[]{r, c});
                }
            }
        }

        // Fallback to any available cell
        if (available.isEmpty()) {
            for (int r = 0; r < Board.SIZE; r++) {
                for (int c = 0; c < Board.SIZE; c++) {
                    CellState state = playerBoard.getCell(r, c);
                    if (state != CellState.HIT && state != CellState.MISS && state != CellState.SUNK) {
                        available.add(new int[]{r, c});
                    }
                }
            }
        }

        if (available.isEmpty()) {
            return null;
        }

        int idx = random.nextInt(available.size());
        return available.get(idx);
    }

    public void reportHit(int row, int col) {
        mode = Mode.TARGET;
        hits.add(new int[]{row, col});
        addAdjacentTargets(row, col);
    }

    public void reportSunk(PlacedShip ship) {
        // Remove sunk ship positions from hits and targets
        List<int[]> shipPositions = ship.getPositions();

        hits.removeIf(h -> shipPositions.stream().anyMatch(p -> p[0] == h[0] && p[1] == h[1]));
        targets.removeIf(t -> shipPositions.stream().anyMatch(p -> p[0] == t[0] && p[1] == t[1]));

        if (hits.isEmpty()) {
            mode = Mode.HUNT;
            targets.clear();
        }
    }

    private void addAdjacentTargets(int row, int col) {
        int[][] adjacent = {{row - 1, col}, {row + 1, col}, {row, col - 1}, {row, col + 1}};
        for (int[] adj : adjacent) {
            int r = adj[0], c = adj[1];
            if (r >= 0 && r < Board.SIZE && c >= 0 && c < Board.SIZE) {
                boolean alreadyExists = targets.stream().anyMatch(t -> t[0] == r && t[1] == c);
                if (!alreadyExists) {
                    targets.add(new int[]{r, c});
                }
            }
        }
    }
}
