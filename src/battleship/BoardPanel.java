package battleship;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.HashSet;
import java.util.Set;

public class BoardPanel extends JPanel {
    private static final int CELL_SIZE = 36;
    private static final int HEADER_SIZE = 24;
    private static final int BOARD_SIZE = Board.SIZE;

    private final Board board;
    private final boolean isPlayerBoard;
    private final GameModel model;
    private final Set<String> hoverCells;
    private boolean invalidHover;
    private CellClickListener cellClickListener;

    public interface CellClickListener {
        void onCellClicked(int row, int col);
    }

    // Colors
    private static final Color OCEAN = new Color(14, 65, 108);
    private static final Color OCEAN_BORDER = new Color(30, 90, 140);
    private static final Color SHIP_COLOR = new Color(100, 116, 139);
    private static final Color SHIP_BORDER = new Color(148, 163, 184);
    private static final Color HIT_COLOR = new Color(220, 38, 38);
    private static final Color HIT_BORDER = new Color(248, 113, 113);
    private static final Color MISS_COLOR = new Color(12, 45, 82);
    private static final Color MISS_BORDER = new Color(30, 70, 110);
    private static final Color SUNK_COLOR = new Color(153, 27, 27);
    private static final Color SUNK_BORDER = new Color(220, 38, 38);
    private static final Color HOVER_VALID = new Color(22, 163, 74);
    private static final Color HOVER_VALID_BORDER = new Color(74, 222, 128);
    private static final Color HOVER_INVALID = new Color(248, 113, 113);
    private static final Color HOVER_INVALID_BORDER = new Color(252, 165, 165);
    private static final Color BG_COLOR = new Color(8, 27, 52);
    private static final Color HEADER_COLOR = new Color(125, 211, 252);

    public BoardPanel(Board board, boolean isPlayerBoard, GameModel model) {
        this.board = board;
        this.isPlayerBoard = isPlayerBoard;
        this.model = model;
        this.hoverCells = new HashSet<>();
        this.invalidHover = false;

        int totalWidth = HEADER_SIZE + BOARD_SIZE * CELL_SIZE;
        int totalHeight = HEADER_SIZE + BOARD_SIZE * CELL_SIZE;
        setPreferredSize(new Dimension(totalWidth, totalHeight));
        setBackground(BG_COLOR);

        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int col = (e.getX() - HEADER_SIZE) / CELL_SIZE;
                int row = (e.getY() - HEADER_SIZE) / CELL_SIZE;
                if (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE) {
                    if (cellClickListener != null) {
                        cellClickListener.onCellClicked(row, col);
                    }
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                hoverCells.clear();
                invalidHover = false;
                repaint();
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if (isPlayerBoard && model.getPhase() == GamePhase.PLACEMENT) {
                    int col = (e.getX() - HEADER_SIZE) / CELL_SIZE;
                    int row = (e.getY() - HEADER_SIZE) / CELL_SIZE;
                    updateHover(row, col);
                }
            }
        });
    }

    public void setCellClickListener(CellClickListener listener) {
        this.cellClickListener = listener;
    }

    private void updateHover(int row, int col) {
        hoverCells.clear();
        ShipType currentShip = model.getCurrentShipType();
        if (currentShip == null) return;

        boolean valid = true;
        Orientation orientation = model.getPlacementOrientation();
        for (int i = 0; i < currentShip.getSize(); i++) {
            int r = orientation == Orientation.VERTICAL ? row + i : row;
            int c = orientation == Orientation.HORIZONTAL ? col + i : col;
            if (r >= BOARD_SIZE || c >= BOARD_SIZE || r < 0 || c < 0
                    || board.getCell(r, c) != CellState.EMPTY) {
                valid = false;
            }
            hoverCells.add(r + "," + c);
        }
        invalidHover = !valid;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Draw column headers
        g2.setColor(HEADER_COLOR);
        g2.setFont(new Font("SansSerif", Font.BOLD, 11));
        for (int c = 0; c < BOARD_SIZE; c++) {
            String label = String.valueOf(c + 1);
            FontMetrics fm = g2.getFontMetrics();
            int x = HEADER_SIZE + c * CELL_SIZE + (CELL_SIZE - fm.stringWidth(label)) / 2;
            int y = HEADER_SIZE - 6;
            g2.drawString(label, x, y);
        }

        // Draw row headers
        for (int r = 0; r < BOARD_SIZE; r++) {
            String label = Board.ROW_LABELS[r];
            FontMetrics fm = g2.getFontMetrics();
            int x = (HEADER_SIZE - fm.stringWidth(label)) / 2;
            int y = HEADER_SIZE + r * CELL_SIZE + (CELL_SIZE + fm.getAscent()) / 2 - 2;
            g2.drawString(label, x, y);
        }

        // Draw cells
        for (int r = 0; r < BOARD_SIZE; r++) {
            for (int c = 0; c < BOARD_SIZE; c++) {
                int x = HEADER_SIZE + c * CELL_SIZE;
                int y = HEADER_SIZE + r * CELL_SIZE;

                CellState state = board.getCell(r, c);
                String key = r + "," + c;
                boolean isHover = hoverCells.contains(key);

                Color bg;
                Color border;
                String content = null;

                if (isHover) {
                    bg = invalidHover ? HOVER_INVALID : HOVER_VALID;
                    border = invalidHover ? HOVER_INVALID_BORDER : HOVER_VALID_BORDER;
                } else if (state == CellState.SHIP && isPlayerBoard) {
                    bg = SHIP_COLOR;
                    border = SHIP_BORDER;
                } else if (state == CellState.SHIP && !isPlayerBoard) {
                    // Hide enemy ships
                    bg = OCEAN;
                    border = OCEAN_BORDER;
                } else if (state == CellState.HIT) {
                    bg = HIT_COLOR;
                    border = HIT_BORDER;
                    content = "\u2620"; // skull and crossbones
                } else if (state == CellState.MISS) {
                    bg = MISS_COLOR;
                    border = MISS_BORDER;
                    content = "\u2022"; // bullet
                } else if (state == CellState.SUNK) {
                    bg = SUNK_COLOR;
                    border = SUNK_BORDER;
                    content = "\uD83D\uDD25"; // fire
                } else {
                    bg = OCEAN;
                    border = OCEAN_BORDER;
                }

                // Fill cell
                g2.setColor(bg);
                g2.fillRect(x, y, CELL_SIZE, CELL_SIZE);

                // Draw border
                g2.setColor(border);
                g2.drawRect(x, y, CELL_SIZE, CELL_SIZE);

                // Draw content
                if (content != null) {
                    g2.setColor(Color.WHITE);
                    g2.setFont(new Font("SansSerif", Font.BOLD, 16));
                    FontMetrics fm = g2.getFontMetrics();
                    int tx = x + (CELL_SIZE - fm.stringWidth(content)) / 2;
                    int ty = y + (CELL_SIZE + fm.getAscent()) / 2 - 3;
                    g2.drawString(content, tx, ty);
                }

                // Draw cursor for clickable cells
                boolean clickable = false;
                if (isPlayerBoard && model.getPhase() == GamePhase.PLACEMENT) {
                    clickable = true;
                } else if (!isPlayerBoard && model.getPhase() == GamePhase.PLAYING
                        && model.isPlayerTurn()
                        && state != CellState.HIT && state != CellState.MISS && state != CellState.SUNK) {
                    clickable = true;
                }

                if (clickable && !isHover) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        }
    }
}
