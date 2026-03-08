package com.example.eightpuzzle;

import java.util.*;

/**
 * A* algorithm implementation for solving 8-puzzle
 * Uses Manhattan distance as heuristic
 */
public class PuzzleSolver {
    
    // Node class for A* search
    private static class Node implements Comparable<Node> {
        int[][] board;
        int g; // cost from start to current node
        int h; // heuristic value (Manhattan distance)
        Node parent;
        int emptyRow;
        int emptyCol;
        
        Node(int[][] board, int g, Node parent, int emptyRow, int emptyCol) {
            this.board = new int[3][3];
            for (int i = 0; i < 3; i++) {
                System.arraycopy(board[i], 0, this.board[i], 0, 3);
            }
            this.g = g;
            this.h = ManhattanHeuristic.calculate(board);
            this.parent = parent;
            this.emptyRow = emptyRow;
            this.emptyCol = emptyCol;
        }
        
        int f() {
            return g + h;
        }
        
        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.f(), other.f());
        }
        
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            Node other = (Node) obj;
            return Arrays.deepEquals(board, other.board);
        }
        
        @Override
        public int hashCode() {
            return Arrays.deepHashCode(board);
        }
    }
    
    /**
     * Find solution path using A* algorithm
     * @param startBoard initial board state
     * @return list of board states representing solution path
     */
    public static List<int[][]> findSolution(int[][] startBoard) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<Node> closedSet = new HashSet<>();
        
        // Find empty tile position
        int[] emptyPos = findEmptyTile(startBoard);
        Node startNode = new Node(startBoard, 0, null, emptyPos[0], emptyPos[1]);
        openSet.add(startNode);
        
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            
            // Check if goal state
            if (current.h == 0) {
                return reconstructPath(current);
            }
            
            closedSet.add(current);
            
            // Explore neighbors
            int[][][] possibleMoves = ManhattanHeuristic.getPossibleMoves(
                current.board, current.emptyRow, current.emptyCol);
            
            for (int[][] move : possibleMoves) {
                int[] newEmptyPos = findEmptyTile(move);
                Node neighbor = new Node(move, current.g + 1, current, 
                                        newEmptyPos[0], newEmptyPos[1]);
                
                if (closedSet.contains(neighbor)) {
                    continue;
                }
                
                if (!openSet.contains(neighbor)) {
                    openSet.add(neighbor);
                }
            }
        }
        
        return null; // No solution found
    }
    
    private static int[] findEmptyTile(int[][] board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{0, 0};
    }
    
    private static List<int[][]> reconstructPath(Node node) {
        LinkedList<int[][]> path = new LinkedList<>();
        while (node != null) {
            path.addFirst(node.board);
            node = node.parent;
        }
        return path;
    }
    
    /**
     * Get hint for next move (lowest Manhattan distance neighbor)
     */
    public static int[][] getHint(int[][] currentBoard) {
        int[] emptyPos = findEmptyTile(currentBoard);
        int[][][] possibleMoves = ManhattanHeuristic.getPossibleMoves(
            currentBoard, emptyPos[0], emptyPos[1]);
        
        int[][] bestMove = null;
        int minDistance = Integer.MAX_VALUE;
        
        for (int[][] move : possibleMoves) {
            int distance = ManhattanHeuristic.calculate(move);
            if (distance < minDistance) {
                minDistance = distance;
                bestMove = move;
            }
        }
        
        return bestMove;
    }
}