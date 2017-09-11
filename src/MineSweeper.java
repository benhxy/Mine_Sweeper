import java.util.Scanner;
import java.util.Random;
import java.util.Queue;
import java.util.LinkedList;

public class MineSweeper {

    //instance variables
    //'_' -> not visited; num -> num of surrounding mines; 'F' -> flag; '.' no mine around
    private char[][] userBoard;
    //true -> has mine
    private boolean[][] realBoard;
    private int flagCount;

    //global variables
    private static int NUMBER_OF_MINES = 40;
    private static int NUMBER_OF_ROWS = 16;
    private static int NUMBER_OF_COLS = 16;

    //imbedded class
    private class Node {
        int x, y;
        public Node (int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    //main function
    public static void main (String[] args) {
        //initiate game
        MineSweeper game = new MineSweeper();

        //initiate variables
        game.initiateGame();

        //start game
        game.gameControl();
    }

    //game loop
    private void gameControl() {
        Scanner sc = new Scanner(System.in);

        while (true) {
            System.out.println(printBoard());
            System.out.println("You have marked " + this.flagCount + " out of " + NUMBER_OF_MINES + " mines.");

            //get instruction
            int instruction = 0;
            while (true) {
                System.out.println("Input 1 to dig, 2 to mark/unmark a flag, 3 to quit");
                instruction = sc.nextInt();
                if (instruction == 1 || instruction == 2 || instruction == 3) {
                    break;
                }
            }

            if (instruction == 3) {
                System.out.println("Bye bye.");
                return;
            }

            //get coordinate
            int row = 0;
            int col = 0;
            while (true) {
                System.out.println("Input row number: ");
                row = sc.nextInt();
                System.out.println("Input column number: ");
                col = sc.nextInt();
                if (row < 0 || row >= NUMBER_OF_ROWS || col < 0 || col >= NUMBER_OF_COLS) {
                    System.out.println("This coordinate is not on the board. Please reenter the numbers");
                } else {
                    break;
                }
            }

            //mark mine
            if (instruction == 2) {
                this.flagMine(row, col);
                continue;
            }

            //if hit a mine
            if (this.realBoard[row][col] == true) {
                //mark mines
                this.markAllMines();

                //print board and messages
                System.out.println(this.printBoard());
                System.out.println("Sorry, you hit a mine. Try next time!");
                return;
            }

            //if not, bfs and expand the uncovered area
            this.bfs(row, col);

            if (this.win()) {
                System.out.println(printBoard());
                System.out.println("Congratulations! You have won the game!");
                return;
            }
        }
    }

    //initiate variables
    private void initiateGame() {
        //create boards
        userBoard = new char[NUMBER_OF_ROWS][NUMBER_OF_COLS];
        realBoard = new boolean[NUMBER_OF_ROWS][NUMBER_OF_COLS];
        flagCount = 0;

        //generate random mines
        int mineCount = 0;
        Random rand = new Random();
        while (mineCount < NUMBER_OF_MINES) {
            int pos = Math.abs(rand.nextInt()) % (NUMBER_OF_ROWS * NUMBER_OF_COLS);
            int row = pos / NUMBER_OF_COLS;
            int col = pos % NUMBER_OF_COLS;
            if (!realBoard[row][col]) {
                realBoard[row][col] = true;
                mineCount++;
            }
        }

        //populate userBoard
        for (int i = 0; i < NUMBER_OF_ROWS; i++) {
            for (int j = 0; j < NUMBER_OF_COLS; j++) {
                userBoard[i][j] = '_';
            }
        }
    }

    //calculate surrounding mines
    private int countMines(int row, int col) {
        int[][] dir = {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};
        int count = 0;
        for (int i = 0; i < dir.length; i++) {
            int newRow = row + dir[i][0];
            int newCol = col + dir[i][1];
            if (newRow >= 0 && newRow < NUMBER_OF_ROWS
                    && newCol >= 0 && newCol < NUMBER_OF_COLS
                    && this.realBoard[newRow][newCol] == true) {
                count++;
            }
        }
        return count;
    }

    //bfs traverse empty cells
    private void bfs(int row, int col) {
        //bfs data structures
        Queue<Node> queue = new LinkedList<>();
        boolean[][] visited = new boolean[NUMBER_OF_ROWS][NUMBER_OF_COLS];
        int[][] dir = {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {-1, 0}, {-1, 1}, {0, 1}, {1, 1}};

        Node root = new Node(row, col);
        queue.offer(root);

        while (!queue.isEmpty()) {
            int size = queue.size();
            for (int i = 0; i < size; i++) {
                Node cur = queue.poll();
                visited[cur.x][cur.y] = true;

                int surrounding = countMines(cur.x, cur.y);
                if (surrounding > 0) {
                    this.userBoard[cur.x][cur.y] = (char) ('0' + surrounding);
                    continue;
                }

                this.userBoard[cur.x][cur.y] = '.';
                //traverse all neighbor nodes, must be not visited, not mine and not flag
                for (int j = 0; j < dir.length; j++) {
                    int newX = cur.x + dir[j][0];
                    int newY = cur.y + dir[j][1];
                    if (newX >= 0 && newX < NUMBER_OF_ROWS
                            && newY >= 0 && newY < NUMBER_OF_COLS
                            && this.realBoard[newX][newY] == false
                            && this.userBoard[newX][newY] == '_'
                            && visited[newX][newY] == false) {
                        Node nbr = new Node(newX, newY);
                        queue.offer(nbr);
                    }
                }
            }
        }
    }

    //print board
    private String printBoard() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < NUMBER_OF_ROWS; i++) {
            for (int j = 0; j < NUMBER_OF_COLS; j++) {
                sb.append(this.userBoard[i][j]);
                sb.append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    //mark all the mines
    private void markAllMines() {
        for (int i = 0; i < NUMBER_OF_ROWS; i++) {
            for (int j = 0; j < NUMBER_OF_COLS; j++) {
                if (realBoard[i][j]) {
                    userBoard[i][j] = 'X';
                }
            }
        }
    }

    //flag/ unflag a mine
    private void flagMine(int row, int col) {
        //error input
        if (this.userBoard[row][col] != '_' && this.userBoard[row][col] != 'F') {
            System.out.println("This cell has been digged. Choose another one");
            return;
        }

        if (this.userBoard[row][col] == '_') {
            this.userBoard[row][col] = 'F';
            this.flagCount++;
        } else {
            this.userBoard[row][col] = '_';
            this.flagCount--;
        }
    }

    //see if user wins
    private boolean win() {
        for (int i = 0; i < NUMBER_OF_ROWS; i++) {
            for (int j = 0; j < NUMBER_OF_COLS; j++) {
                if (userBoard[i][j] == '_') {
                    return false;
                }
                if (realBoard[i][j] && userBoard[i][j] != 'F') {
                    return false;
                }
            }
        }
        return true;
    }

}
