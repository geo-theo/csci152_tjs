import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class GoBoard {
    /* 9x9 crosshach grid */
    static final int SIZE = 9;
    static final int GAP = 35;
    static final int MARGIN = 25;

    static final String BLACK = "@";
    static final String WHITE = "O";

    static String[][] goBoard = new String[SIZE][SIZE];
    static boolean[][] lives = new boolean[SIZE][SIZE];
    static boolean[][] territory = new boolean[SIZE][SIZE];
    static boolean[][] beenChecked = new boolean[SIZE][SIZE];

    static String[][] territoryOwner = new String[SIZE][SIZE];

    static boolean blackTurn = true;
    static int blackCaptures = 0;
    static int whiteCaptures = 0;
    static int blackTerritoryCount = 0;
    static int whiteTerritoryCount = 0;
    static String statusText = "Black turn";

    static JLabel statusLabel = new JLabel("", SwingConstants.CENTER);
    static JLabel scoreLabel = new JLabel("", SwingConstants.CENTER);
    static Image boardBackground = new ImageIcon("LeeSeDol_AlphaGo.png").getImage();

    static ArrayList<Point> currentGroup = new ArrayList<Point>();
    static ArrayList<Point> currentRegion = new ArrayList<Point>();
    static boolean currentGroupAlive = false;
    static boolean regionTouchesBlack = false;
    static boolean regionTouchesWhite = false;

    static final String[][] testBoardRows = {
            {null, null, null, null, null, null, null, null, null},
            {null, null, WHITE, WHITE, WHITE, null, null, null, null},
            {null, null, WHITE, BLACK, WHITE, null, null, null, null},
            {null, null, WHITE, BLACK, WHITE, null, null, null, null},
            {null, null, null, WHITE, null, BLACK, null, null, null},
            {null, null, null, null, null, BLACK, null, null, null},
            {null, null, null, null, null, BLACK, null, null, null},
            {null, null, null, null, null, BLACK, null, null, null},
            {null, null, null, null, null, null, null, null, null}
    };

    public static void main(String[] args) {
        startNewGame();

        JFrame frame = new JFrame("Go");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new BorderLayout());

        JPanel topPanel = new JPanel();
        JButton newGameButton = new JButton("New Game");
        JButton testBoardButton = new JButton("Load Test Board");
        topPanel.add(newGameButton);
        topPanel.add(testBoardButton);

        JPanel boardPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                g.drawImage(boardBackground, 0, 0, getWidth(), getHeight(), this);
                drawBoard(g);
            }
        };

        boardPanel.setPreferredSize(new Dimension(MARGIN * 2 + GAP * (SIZE - 1), MARGIN * 2 + GAP * (SIZE - 1)));
        boardPanel.addMouseListener(new MouseAdapter() {
            public void mousePressed(MouseEvent e) {
                handleMove(e.getX(), e.getY(), boardPanel);
            }
        });

        newGameButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                startNewGame();
                boardPanel.repaint();
            }
        });

        testBoardButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                loadTestBoard();
                boardPanel.repaint();
            }
        });

        JPanel bottomPanel = new JPanel(new GridLayout(2, 1));
        bottomPanel.add(statusLabel);
        bottomPanel.add(scoreLabel);

        frame.add(topPanel, BorderLayout.NORTH);
        frame.add(boardPanel, BorderLayout.CENTER);
        frame.add(bottomPanel, BorderLayout.SOUTH);

        updateLabels();

        frame.pack();
        frame.setResizable(false);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }

    static void startNewGame() {
        clearBoard();
        blackTurn = true;
        blackCaptures = 0;
        whiteCaptures = 0;
        statusText = "Black turn";
        updateBoardState();
    }

    static void loadTestBoard() {
        clearBoard();
        for (int row = 0; row < SIZE; row++) {
            for (int col = 0; col < SIZE; col++) {
                goBoard[col][row] = testBoardRows[row][col];
            }
        }
        blackTurn = true;
        blackCaptures = 0;
        whiteCaptures = 0;
        statusText = "Test board loaded. Black turn";
        updateBoardState();
    }

    static void clearBoard() {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                goBoard[x][y] = null;
            }
        }
        clearBooleanBoard(lives);
        clearBooleanBoard(territory);
        clearBooleanBoard(beenChecked);
        clearStringBoard(territoryOwner);
    }

    static void clearBooleanBoard(boolean[][] board) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                board[x][y] = false;
            }
        }
    }

    static void clearStringBoard(String[][] board) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                board[x][y] = null;
            }
        }
    }

    static void handleMove(int mouseX, int mouseY, JPanel boardPanel) {
        int x = Math.round((float) (mouseX - MARGIN) / GAP);
        int y = Math.round((float) (mouseY - MARGIN) / GAP);

        if (!isInsideBoard(x, y)) {
            statusText = "That click is outside the board.";
            updateLabels();
            boardPanel.repaint();
            return;
        }

        if (goBoard[x][y] != null) {
            statusText = "That point is already occupied.";
            updateLabels();
            boardPanel.repaint();
            return;
        }

        /* alternate pla cing of black and white pieces */
        String stone = blackTurn ? BLACK : WHITE;
        String enemy = blackTurn ? WHITE : BLACK;
        String playerName = blackTurn ? "Black" : "White";
        String nextPlayerName = blackTurn ? "White" : "Black";

        String[][] oldBoard = copyBoard();

        goBoard[x][y] = stone;

        /* Capture */
        int captured = removeCapturedGroups(enemy);

        if (!groupHasLife(x, y)) {
            restoreBoard(oldBoard);
            updateBoardState();
            statusText = "That move has no liberties.";
            updateLabels();
            boardPanel.repaint();
            return;
        }

        if (BLACK.equals(stone)) {
            blackCaptures = blackCaptures + captured;
        } else {
            whiteCaptures = whiteCaptures + captured;
        }

        blackTurn = !blackTurn;
        updateBoardState();

        if (captured > 0) {
            statusText = playerName + " captured " + captured + ". " + nextPlayerName + " turn";
        } else {
            statusText = nextPlayerName + " turn";
        }

        updateLabels();
        boardPanel.repaint();
    }

    /* out-of-bounds detection */
    static boolean isInsideBoard(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    static String[][] copyBoard() {
        String[][] copy = new String[SIZE][SIZE];
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                copy[x][y] = goBoard[x][y];
            }
        }
        return copy;
    }

    static void restoreBoard(String[][] copy) {
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                goBoard[x][y] = copy[x][y];
            }
        }
    }

    static int removeCapturedGroups(String color) {
        int removed = 0;

        clearBooleanBoard(lives);
        clearBooleanBoard(beenChecked);

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (goBoard[x][y] != null && goBoard[x][y].equals(color) && !beenChecked[x][y]) {
                    currentGroup = new ArrayList<Point>();
                    currentGroupAlive = false;
                    searchGroup(x, y, color);

                    for (int i = 0; i < currentGroup.size(); i++) {
                        Point point = currentGroup.get(i);
                        lives[point.x][point.y] = currentGroupAlive;
                    }

                    if (!currentGroupAlive) {
                        for (int i = 0; i < currentGroup.size(); i++) {
                            Point point = currentGroup.get(i);
                            goBoard[point.x][point.y] = null;
                            lives[point.x][point.y] = false;
                            removed++;
                        }
                    }
                }
            }
        }

        return removed;
    }

    static boolean groupHasLife(int x, int y) {
        if (!isInsideBoard(x, y)) {
            return false;
        }

        if (goBoard[x][y] == null) {
            return false;
        }

        clearBooleanBoard(beenChecked);

        currentGroup = new ArrayList<Point>();
        currentGroupAlive = false;
        searchGroup(x, y, goBoard[x][y]);

        for (int i = 0; i < currentGroup.size(); i++) {
            Point point = currentGroup.get(i);
            lives[point.x][point.y] = currentGroupAlive;
        }

        return currentGroupAlive;
    }

    /* capture recursoin (life check) */
    static void searchGroup(int x, int y, String color) {
        if (!isInsideBoard(x, y)) {
            return;
        }

        if (goBoard[x][y] == null) {
            currentGroupAlive = true;
            return;
        }

        if (!goBoard[x][y].equals(color)) {
            return;
        }

        if (beenChecked[x][y]) {
            return;
        }

        beenChecked[x][y] = true;
        currentGroup.add(new Point(x, y));

        searchGroup(x + 1, y, color);
        searchGroup(x - 1, y, color);
        searchGroup(x, y + 1, color);
        searchGroup(x, y - 1, color);
    }

    static void updateBoardState() {
        updateLivesBoard();
        calculateTerritory();
        updateLabels();
    }

    static void updateLivesBoard() {
        clearBooleanBoard(lives);
        clearBooleanBoard(beenChecked);

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (goBoard[x][y] != null && !beenChecked[x][y]) {
                    currentGroup = new ArrayList<Point>();
                    currentGroupAlive = false;
                    searchGroup(x, y, goBoard[x][y]);

                    for (int i = 0; i < currentGroup.size(); i++) {
                        Point point = currentGroup.get(i);
                        lives[point.x][point.y] = currentGroupAlive;
                    }
                }
            }
        }
    }

    /* Scoring */
    static void calculateTerritory() {
        blackTerritoryCount = 0;
        whiteTerritoryCount = 0;

        clearBooleanBoard(territory);
        clearBooleanBoard(beenChecked);
        clearStringBoard(territoryOwner);

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (goBoard[x][y] == null && !beenChecked[x][y]) {
                    currentRegion = new ArrayList<Point>();
                    regionTouchesBlack = false;
                    regionTouchesWhite = false;

                    searchTerritory(x, y);

                    if (regionTouchesBlack && !regionTouchesWhite) {
                        blackTerritoryCount = blackTerritoryCount + currentRegion.size();
                        for (int i = 0; i < currentRegion.size(); i++) {
                            Point point = currentRegion.get(i);
                            territory[point.x][point.y] = true;
                            territoryOwner[point.x][point.y] = BLACK;
                        }
                    } else if (regionTouchesWhite && !regionTouchesBlack) {
                        whiteTerritoryCount = whiteTerritoryCount + currentRegion.size();
                        for (int i = 0; i < currentRegion.size(); i++) {
                            Point point = currentRegion.get(i);
                            territory[point.x][point.y] = true;
                            territoryOwner[point.x][point.y] = WHITE;
                        }
                    }
                }
            }
        }
    }

    static void searchTerritory(int x, int y) {
        if (!isInsideBoard(x, y)) {
            return;
        }

        if (goBoard[x][y] != null) {
            if (goBoard[x][y].equals(BLACK)) {
                regionTouchesBlack = true;
            } else if (goBoard[x][y].equals(WHITE)) {
                regionTouchesWhite = true;
            }
            return;
        }

        if (beenChecked[x][y]) {
            return;
        }

        beenChecked[x][y] = true;
        currentRegion.add(new Point(x, y));

        searchTerritory(x + 1, y);
        searchTerritory(x - 1, y);
        searchTerritory(x, y + 1);
        searchTerritory(x, y - 1);
    }

    static void updateLabels() {
        int blackScore = blackCaptures + blackTerritoryCount;
        int whiteScore = whiteCaptures + whiteTerritoryCount;

        statusLabel.setText(statusText);
        scoreLabel.setText("Black score: " + blackScore + "  Captures: " + blackCaptures + "  Territory: " + blackTerritoryCount
                + "     White score: " + whiteScore + "  Captures: " + whiteCaptures + "  Territory: " + whiteTerritoryCount);
    }

    static void drawBoard(Graphics g) {
        g.setColor(Color.BLACK);

        for (int i = 0; i < SIZE; i++) {
            g.drawLine(MARGIN, MARGIN + i * GAP, MARGIN + (SIZE - 1) * GAP, MARGIN + i * GAP);
            g.drawLine(MARGIN + i * GAP, MARGIN, MARGIN + i * GAP, MARGIN + (SIZE - 1) * GAP);
        }

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (territory[x][y]) {
                    if (BLACK.equals(territoryOwner[x][y])) {
                        g.setColor(new Color(70, 70, 70));
                    } else {
                        g.setColor(new Color(240, 240, 240));
                    }
                    g.fillRect(MARGIN + x * GAP - 4, MARGIN + y * GAP - 4, 8, 8);
                }
            }
        }

        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                if (goBoard[x][y] != null) {
                    if (goBoard[x][y].equals(BLACK)) {
                        g.setColor(Color.BLACK);
                    } else {
                        g.setColor(Color.WHITE);
                    }
                    g.fillOval(MARGIN + x * GAP - 12, MARGIN + y * GAP - 12, 24, 24);
                    g.setColor(Color.BLACK);
                    g.drawOval(MARGIN + x * GAP - 12, MARGIN + y * GAP - 12, 24, 24);
                }
            }
        }
    }
}
