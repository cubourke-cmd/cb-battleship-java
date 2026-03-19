package battleship;

import java.util.ArrayList;
import java.util.List;

public class GameModel {
    private final Board playerBoard;
    private final Board aiBoard;
    private final AIPlayer aiPlayer;
    private GamePhase phase;
    private boolean playerTurn;
    private String message;
    private String winner; // "player", "ai", or null
    private Orientation placementOrientation;
    private int currentShipIndex;
    private final List<GameListener> listeners;

    public interface GameListener {
        void onGameStateChanged();
    }

    public GameModel() {
        playerBoard = new Board();
        aiBoard = new Board();
        aiPlayer = new AIPlayer();
        listeners = new ArrayList<>();
        reset();
    }

    public void addListener(GameListener listener) {
        listeners.add(listener);
    }

    private void notifyListeners() {
        for (GameListener listener : listeners) {
            listener.onGameStateChanged();
        }
    }

    public void reset() {
        playerBoard.clear();
        aiBoard.clear();
        aiPlayer.reset();
        phase = GamePhase.PLACEMENT;
        playerTurn = true;
        winner = null;
        placementOrientation = Orientation.HORIZONTAL;
        currentShipIndex = 0;
        message = "Place your " + getCurrentShipType().getDisplayName()
                + " (" + getCurrentShipType().getSize() + " cells)";
        notifyListeners();
    }

    public Board getPlayerBoard() {
        return playerBoard;
    }

    public Board getAIBoard() {
        return aiBoard;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public boolean isPlayerTurn() {
        return playerTurn;
    }

    public String getMessage() {
        return message;
    }

    public String getWinner() {
        return winner;
    }

    public Orientation getPlacementOrientation() {
        return placementOrientation;
    }

    public void toggleOrientation() {
        if (phase == GamePhase.PLACEMENT) {
            placementOrientation = placementOrientation.toggle();
            notifyListeners();
        }
    }

    public ShipType getCurrentShipType() {
        ShipType[] types = ShipType.values();
        if (currentShipIndex < types.length) {
            return types[currentShipIndex];
        }
        return null;
    }

    public boolean tryPlaceShip(int row, int col) {
        if (phase != GamePhase.PLACEMENT) return false;
        ShipType type = getCurrentShipType();
        if (type == null) return false;

        if (!playerBoard.canPlaceShip(row, col, type.getSize(), placementOrientation)) {
            return false;
        }

        playerBoard.placeShip(type, row, col, placementOrientation);
        currentShipIndex++;

        ShipType nextType = getCurrentShipType();
        if (nextType != null) {
            message = "Place your " + nextType.getDisplayName()
                    + " (" + nextType.getSize() + " cells)";
        } else {
            startGame();
        }
        notifyListeners();
        return true;
    }

    public void randomPlacement() {
        if (phase != GamePhase.PLACEMENT) return;
        playerBoard.placeShipsRandomly();
        currentShipIndex = ShipType.values().length;
        startGame();
        notifyListeners();
    }

    private void startGame() {
        aiBoard.placeShipsRandomly();
        phase = GamePhase.PLAYING;
        playerTurn = true;
        message = "Your turn \u2014 fire at the enemy grid!";
    }

    public boolean playerShoot(int row, int col) {
        if (phase != GamePhase.PLAYING || !playerTurn) return false;

        CellState state = aiBoard.getCell(row, col);
        if (state == CellState.HIT || state == CellState.MISS || state == CellState.SUNK) {
            return false;
        }

        boolean hit = aiBoard.fireAt(row, col);

        if (hit) {
            PlacedShip sunkShip = aiBoard.getShipSunkAt(row, col);
            if (sunkShip != null) {
                message = "You sank their " + sunkShip.getName() + "!";
            } else {
                message = "Direct hit!";
            }
        } else {
            message = "Miss...";
        }

        // Check player win
        if (aiBoard.allShipsSunk()) {
            phase = GamePhase.GAME_OVER;
            winner = "player";
            message = "You win! All enemy ships destroyed!";
            notifyListeners();
            return true;
        }

        playerTurn = false;
        notifyListeners();
        return true;
    }

    public void aiShoot() {
        if (phase != GamePhase.PLAYING || playerTurn) return;

        int[] shot = aiPlayer.getShot(playerBoard);
        if (shot == null) return;

        int aiRow = shot[0];
        int aiCol = shot[1];
        boolean hit = playerBoard.fireAt(aiRow, aiCol);

        if (hit) {
            PlacedShip sunkShip = playerBoard.getShipSunkAt(aiRow, aiCol);
            if (sunkShip != null) {
                aiPlayer.reportSunk(sunkShip);
                message = "Enemy fired at " + Board.ROW_LABELS[aiRow] + (aiCol + 1)
                        + " \u2014 Sank your " + sunkShip.getName() + "!";
            } else {
                aiPlayer.reportHit(aiRow, aiCol);
                message = "Enemy fired at " + Board.ROW_LABELS[aiRow] + (aiCol + 1)
                        + " \u2014 hit! Your turn.";
            }
        } else {
            message = "Enemy fired at " + Board.ROW_LABELS[aiRow] + (aiCol + 1)
                    + " \u2014 miss. Your turn.";
        }

        // Check AI win
        if (playerBoard.allShipsSunk()) {
            phase = GamePhase.GAME_OVER;
            winner = "ai";
            message = "You lost! All your ships were destroyed.";
            notifyListeners();
            return;
        }

        playerTurn = true;
        notifyListeners();
    }
}
