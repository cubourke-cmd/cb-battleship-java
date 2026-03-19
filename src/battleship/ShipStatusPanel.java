package battleship;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ShipStatusPanel extends JPanel {
    private static final Color BG_COLOR = new Color(30, 41, 59);
    private static final Color BORDER_COLOR = new Color(71, 85, 105);
    private static final Color TEXT_COLOR = Color.WHITE;
    private static final Color SUNK_TEXT_COLOR = new Color(248, 113, 113);
    private static final Color ALIVE_DOT = new Color(34, 197, 94);
    private static final Color SUNK_DOT = new Color(220, 38, 38);

    private final String title;
    private List<PlacedShip> ships;
    private boolean showDefaults;

    public ShipStatusPanel(String title) {
        this.title = title;
        this.showDefaults = true;
        setBackground(BG_COLOR);
        setBorder(BorderFactory.createLineBorder(BORDER_COLOR));
        setPreferredSize(new Dimension(200, 140));
    }

    public void setShips(List<PlacedShip> ships) {
        this.ships = ships;
        this.showDefaults = (ships == null || ships.isEmpty());
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // Title
        g2.setColor(new Color(148, 163, 184));
        g2.setFont(new Font("SansSerif", Font.BOLD, 10));
        g2.drawString(title.toUpperCase(), 10, 18);

        int y = 35;
        if (showDefaults) {
            for (ShipType type : ShipType.values()) {
                drawShipRow(g2, type.getDisplayName(), type.getSize(), false, y);
                y += 20;
            }
        } else if (ships != null) {
            for (PlacedShip ship : ships) {
                drawShipRow(g2, ship.getName(), ship.getSize(), ship.isSunk(), y);
                y += 20;
            }
        }
    }

    private void drawShipRow(Graphics2D g2, String name, int size, boolean sunk, int y) {
        // Ship name
        g2.setFont(new Font("SansSerif", Font.PLAIN, 12));
        if (sunk) {
            g2.setColor(SUNK_TEXT_COLOR);
            // Draw strikethrough
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(name, 10, y);
            int textWidth = fm.stringWidth(name);
            int lineY = y - fm.getAscent() / 3;
            g2.drawLine(10, lineY, 10 + textWidth, lineY);
        } else {
            g2.setColor(TEXT_COLOR);
            g2.drawString(name, 10, y);
        }

        // Dots
        int dotX = getWidth() - 10 - size * 14;
        for (int i = 0; i < size; i++) {
            g2.setColor(sunk ? SUNK_DOT : ALIVE_DOT);
            g2.fillRoundRect(dotX + i * 14, y - 9, 10, 10, 3, 3);
        }
    }
}
