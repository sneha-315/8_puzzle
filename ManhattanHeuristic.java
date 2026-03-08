package com.example.eightpuzzle;

/**
 * Calculates Manhattan distance heuristic for 8-puzzle
 * This is the key algorithm for A* search
 */
public class ManhattanHeuristic {
    
    // Goal state positions [row][col] for each tile value (1-8, 0 for empty)
    private static final int[][] GOAL_POSITIONS = {
        {0, 0}, // tile 1 at (0,0)
        {0, 1}, // tile 2 at (0,1)
        {0, 2}, // tile 3 at (0,2)
        {1, 0}, // tile 4 at (1,0)
        {1, 1}, // tile 5 at (1,1)
        {1, 2}, // tile 6 at (1,2)
        {2, 0}, // tile 7 at (2,0)
        {2, 1}, // tile 8 at (2,1)
        {2, 2}  // empty tile (0) at (2,2)
    };
    
    /**
     * Calculate Manhattan distance for current board state
     * @param board 3x3 array representing current puzzle state
     * @return total Manhattan distance (lower = closer to solution)
     */
    public static int calculate(int[][] board) {
        int distance = 0;
        
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                int tileValue = board[row][col];
                if (tileValue != 0) { // Skip empty tile
                    // Tile value is 1-8, convert to 0-based index for GOAL_POSITIONS
                    int tileIndex = tileValue - 1;
                    int goalRow = GOAL_POSITIONS[tileIndex][0];
                    int goalCol = GOAL_POSITIONS[tileIndex][1];
                    
                    // Calculate Manhattan distance for this tile
                    distance += Math.abs(row - goalRow) + Math.abs(col - goalCol);
                }
            }
        }
        return distance;
    }
    
    /**
     * Check if puzzle is solved (distance = 0)
     */
    public static boolean isSolved(int[][] board) {
        return calculate(board) == 0;
    }
    
    /**
     * Get all possible moves from current state
     * @param board current board state
     * @param emptyRow row of empty tile
     * @param emptyCol col of empty tile
     * @return array of possible new board states
     */
    public static int[][][] getPossibleMoves(int[][] board, int emptyRow, int emptyCol) {
        // Directions: up, down, left, right
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        int validMoves = 0;
        
        // First count valid moves
        for (int[] dir : directions) {
            int newRow = emptyRow + dir[0];
            int newCol = emptyCol + dir[1];
            if (newRow >= 0 && newRow < 3 && newCol >= 0 && newCol < 3) {
                validMoves++;
            }
        }
        
        // Create array of possible moves
        int[][][] moves = new int[validMoves][3][3];
        int moveIndex = 0;
        
        for (int[] dir : directions) {
            int newRow = emptyRow + dir[0];
            int newCol = emptyCol + dir[1];
            
            if (newRow >= 0 && newRow < 3 && newCol >= 0 && newCol < 3) {
                // Copy current board
                int[][] newBoard = new int[3][3];
                for (int i = 0; i < 3; i++) {
                    System.arraycopy(board[i], 0, newBoard[i], 0, 3);
                }
                
                // Swap empty tile with adjacent tile
                newBoard[emptyRow][emptyCol] = newBoard[newRow][newCol];
                newBoard[newRow][newCol] = 0;
                
                moves[moveIndex++] = newBoard;
            }
        }
        
        return moves;
    }
}