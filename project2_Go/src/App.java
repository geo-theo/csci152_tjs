public class App {

    static final int SIZE = 9;
    static String[][] board = new String[SIZE][SIZE];

    static void initBoard() {
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                board[r][c] = " ";
            }
        }

        // ASCII stones (works in any terminal)
        board[2][2] = "B"; // black
        board[6][6] = "W"; // white
    }

    static void printBoard() {
        for (int r = 0; r < SIZE; r++) {

            for (int c = 0; c < SIZE; c++) {
                if (!board[r][c].equals(" ")) {
                    System.out.print(board[r][c]);
                } else {
                    System.out.print("+");
                }

                if (c < SIZE - 1) System.out.print("---");
            }
            System.out.println();

            if (r < SIZE - 1) {
                for (int c = 0; c < SIZE; c++) {
                    System.out.print("|");
                    if (c < SIZE - 1) System.out.print("   ");
                }
                System.out.println();
            }
        }
    }

    public static void main(String[] args) {
        initBoard();
        printBoard();
    }
}
