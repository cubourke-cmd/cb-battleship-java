package battleship;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class BattleshipFrame extends JFrame implements GameModel.GameListener {
    private final GameModel model;
    private final BoardPanel playerBoardPanel;
    private final BoardPanel aiBoardPanel;
    private final ShipStatusPanel playerShipStatus;
    private final ShipStatusPanel aiShipStatus;
    private final JLabel messageLabel;
    private final JPanel messagePanel;
    private final JButton rotateButton;
    private final JButton randomButton;
    private final JButton playAgainButton;
    private final JPanel controlPanel;
    private final JPanel aiBoardContainer;
    private Timer aiTimer;

    // Colors
    private static final Color BG_DARK = new Color(15, 23, 42);
    private static final Color BG_GRADIENT_START = new Color(15, 23, 42);
    private static final Color MSG_DEFAULT_BG = new Color(51, 65, 85);
    private static final Color MSG_DEFAULT_BORDER = new Color(100, 116, 139);
    private static final Color MSG_HIT_BG = new Color(154, 52, 18);
    private static final Color MSG_HIT_BORDER = new Color(234, 88, 12);
    private static final Color MSG_WIN_BG = new Color(21, 128, 61);
    private static final Color MSG_WIN_BORDER = new Color(34, 197, 94);
    private static final Color MSG_LOSE_BG = new Color(153, 27, 27);
    private static final Color MSG_LOSE_BORDER = new Color(220, 38, 38);
    private static final Color ACCENT_COLOR = new Color(56, 189, 248);

    public BattleshipFrame() {
        model = new GameModel();
        model.addListener(this);

        setTitle("Battleship");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setResizable(false);

        // Main panel with gradient background
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gradient = new GradientPaint(
                        0, 0, BG_GRADIENT_START,
                        getWidth(), getHeight(), new Color(8, 47, 73));
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setOpaque(false);
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(15, 0, 5, 0));

        JPanel titleRow = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
        titleRow.setOpaque(false);
        JLabel titleBattle = new JLabel("BATTLE");
        titleBattle.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleBattle.setForeground(ACCENT_COLOR);
        JLabel titleShip = new JLabel("SHIP");
        titleShip.setFont(new Font("SansSerif", Font.BOLD, 32));
        titleShip.setForeground(Color.WHITE);
        titleRow.add(titleBattle);
        titleRow.add(titleShip);

        JLabel subtitle = new JLabel("NAVAL COMBAT STRATEGY GAME");
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subtitle.setForeground(new Color(125, 211, 252));
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        titleRow.setAlignmentX(Component.CENTER_ALIGNMENT);
        headerPanel.add(titleRow);
        headerPanel.add(Box.createVerticalStrut(2));
        headerPanel.add(subtitle);
        mainPanel.add(headerPanel);

        // Message bar
        messagePanel = new JPanel(new BorderLayout());
        messagePanel.setOpaque(true);
        messagePanel.setBackground(MSG_DEFAULT_BG);
        messagePanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createEmptyBorder(8, 40, 8, 40),
                BorderFactory.createEmptyBorder(0, 0, 0, 0)));
        messagePanel.setMaximumSize(new Dimension(900, 50));

        JPanel msgInner = new JPanel(new BorderLayout());
        msgInner.setBackground(MSG_DEFAULT_BG);
        msgInner.setBorder(BorderFactory.createLineBorder(MSG_DEFAULT_BORDER));

        messageLabel = new JLabel(model.getMessage(), SwingConstants.CENTER);
        messageLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        messageLabel.setForeground(Color.WHITE);
        messageLabel.setBorder(BorderFactory.createEmptyBorder(8, 20, 8, 20));
        msgInner.add(messageLabel, BorderLayout.CENTER);

        messagePanel.removeAll();
        messagePanel.setLayout(new BorderLayout());
        messagePanel.add(msgInner, BorderLayout.CENTER);
        messagePanel.setBorder(BorderFactory.createEmptyBorder(8, 40, 4, 40));
        mainPanel.add(messagePanel);

        // Controls
        controlPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        controlPanel.setOpaque(false);
        controlPanel.setMaximumSize(new Dimension(900, 45));

        rotateButton = createStyledButton("Rotate (R) \u2014 \u2192", new Color(3, 105, 161), new Color(7, 89, 133));
        randomButton = createStyledButton("Random Placement", new Color(180, 83, 9), new Color(146, 64, 14));
        playAgainButton = createStyledButton("Play Again", new Color(21, 128, 61), new Color(22, 101, 52));

        rotateButton.addActionListener(e -> model.toggleOrientation());
        randomButton.addActionListener(e -> model.randomPlacement());
        playAgainButton.addActionListener(e -> {
            if (aiTimer != null) {
                aiTimer.stop();
                aiTimer = null;
            }
            model.reset();
        });

        controlPanel.add(rotateButton);
        controlPanel.add(randomButton);
        controlPanel.add(playAgainButton);
        mainPanel.add(controlPanel);

        // Boards
        JPanel boardsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        boardsPanel.setOpaque(false);

        // Player board + status
        JPanel playerContainer = new JPanel();
        playerContainer.setLayout(new BoxLayout(playerContainer, BoxLayout.Y_AXIS));
        playerContainer.setOpaque(false);

        JLabel playerLabel = new JLabel("YOUR FLEET");
        playerLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        playerLabel.setForeground(Color.WHITE);
        playerLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerContainer.add(playerLabel);
        playerContainer.add(Box.createVerticalStrut(8));

        playerBoardPanel = new BoardPanel(model.getPlayerBoard(), true, model);
        playerBoardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerBoardPanel.setCellClickListener((row, col) -> {
            if (model.getPhase() == GamePhase.PLACEMENT) {
                model.tryPlaceShip(row, col);
            }
        });
        playerContainer.add(playerBoardPanel);
        playerContainer.add(Box.createVerticalStrut(8));

        playerShipStatus = new ShipStatusPanel("Your Ships");
        playerShipStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        playerContainer.add(playerShipStatus);

        boardsPanel.add(playerContainer);

        // AI board + status
        aiBoardContainer = new JPanel();
        aiBoardContainer.setLayout(new BoxLayout(aiBoardContainer, BoxLayout.Y_AXIS));
        aiBoardContainer.setOpaque(false);

        JLabel aiLabel = new JLabel("ENEMY WATERS");
        aiLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        aiLabel.setForeground(Color.WHITE);
        aiLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        aiBoardContainer.add(aiLabel);
        aiBoardContainer.add(Box.createVerticalStrut(8));

        aiBoardPanel = new BoardPanel(model.getAIBoard(), false, model);
        aiBoardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        aiBoardPanel.setCellClickListener((row, col) -> {
            if (model.getPhase() == GamePhase.PLAYING && model.isPlayerTurn()) {
                boolean fired = model.playerShoot(row, col);
                if (fired && model.getPhase() != GamePhase.GAME_OVER) {
                    // Cancel any existing AI timer before starting a new one
                    if (aiTimer != null) {
                        aiTimer.stop();
                    }
                    aiTimer = new Timer(600, e -> {
                        aiTimer = null;
                        model.aiShoot();
                    });
                    aiTimer.setRepeats(false);
                    aiTimer.start();
                }
            }
        });
        aiBoardContainer.add(aiBoardPanel);
        aiBoardContainer.add(Box.createVerticalStrut(8));

        aiShipStatus = new ShipStatusPanel("Enemy Ships");
        aiShipStatus.setAlignmentX(Component.CENTER_ALIGNMENT);
        aiBoardContainer.add(aiShipStatus);

        aiBoardContainer.setVisible(false);
        boardsPanel.add(aiBoardContainer);

        mainPanel.add(boardsPanel);

        // Footer
        JLabel footer = new JLabel("Click to place ships \u2022 Press R to rotate \u2022 Sink all enemy ships to win");
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        footer.setForeground(new Color(56, 119, 169));
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.setBorder(BorderFactory.createEmptyBorder(5, 0, 10, 0));
        mainPanel.add(footer);

        // Keyboard shortcut for rotation
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(e -> {
            if (e.getID() == KeyEvent.KEY_PRESSED
                    && (e.getKeyChar() == 'r' || e.getKeyChar() == 'R')
                    && model.getPhase() == GamePhase.PLACEMENT) {
                model.toggleOrientation();
                return true;
            }
            return false;
        });

        setContentPane(mainPanel);
        onGameStateChanged();
        pack();
        setLocationRelativeTo(null);
    }

    private JButton createStyledButton(String text, Color bg, Color hover) {
        JButton button = new JButton(text);
        button.setFont(new Font("SansSerif", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBackground(bg);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(bg.brighter()),
                BorderFactory.createEmptyBorder(6, 14, 6, 14)));
        button.setFocusPainted(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        button.setOpaque(true);

        button.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                button.setBackground(hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                button.setBackground(bg);
            }
        });

        return button;
    }

    @Override
    public void onGameStateChanged() {
        // Update message
        String msg = model.getMessage();
        messageLabel.setText(msg);

        // Update message bar color
        Container msgInner = (Container) messagePanel.getComponent(0);
        String winner = model.getWinner();
        Color msgBg;
        Color msgBorder;
        if ("player".equals(winner)) {
            msgBg = MSG_WIN_BG;
            msgBorder = MSG_WIN_BORDER;
        } else if ("ai".equals(winner)) {
            msgBg = MSG_LOSE_BG;
            msgBorder = MSG_LOSE_BORDER;
        } else if (msg.contains("hit") || msg.contains("Hit") || msg.contains("sank") || msg.contains("Sank")) {
            msgBg = MSG_HIT_BG;
            msgBorder = MSG_HIT_BORDER;
        } else {
            msgBg = MSG_DEFAULT_BG;
            msgBorder = MSG_DEFAULT_BORDER;
        }
        msgInner.setBackground(msgBg);
        if (msgInner instanceof JPanel) {
            ((JPanel) msgInner).setBorder(BorderFactory.createLineBorder(msgBorder));
        }

        // Update controls visibility
        boolean isPlacement = model.getPhase() == GamePhase.PLACEMENT;
        boolean isGameOver = model.getPhase() == GamePhase.GAME_OVER;
        rotateButton.setVisible(isPlacement);
        randomButton.setVisible(isPlacement);
        playAgainButton.setVisible(isGameOver);

        // Update rotate button text
        String arrow = model.getPlacementOrientation() == Orientation.HORIZONTAL ? "\u2192" : "\u2193";
        rotateButton.setText("Rotate (R) \u2014 " + arrow);

        // Show/hide AI board
        aiBoardContainer.setVisible(model.getPhase() != GamePhase.PLACEMENT);

        // Update ship status panels
        playerShipStatus.setShips(model.getPlayerBoard().getShips());
        aiShipStatus.setShips(model.getAIBoard().getShips());

        // Repaint boards
        playerBoardPanel.repaint();
        aiBoardPanel.repaint();

        pack();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {
            }
            BattleshipFrame frame = new BattleshipFrame();
            frame.setVisible(true);
        });
    }
}
