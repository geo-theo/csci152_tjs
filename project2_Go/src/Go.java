public class Go {

    static String[][] board = new String[9][9];

    public static void main(String[] args){
        board[3][3] = "o";
        board[4][3] = "@";
        for(int i = 0 ; i < board.length; i++){
            for(int j = 0 ; j < board.length; j++){
                if(board[i][j] == null){
                    System.out.print("+");
                }else{
                    System.out.print(board[i][j]);
                }
            }
            System.out.println();
        }
    }
}
