package com.example.eightpuzzle;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Handler;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameBoard extends View {
    
    private int[][] board = new int[3][3];
    private int tileSize;
    private Paint textPaint;
    private Paint tilePaint;
    private Paint borderPaint;
    private Paint hintPaint;
    private int emptyRow, emptyCol;
    private int moveCount = 0;
    private boolean showHint = false;
    private int[][] hintBoard;
    private OnGameStateChangeListener listener;
    private Vibrator vibrator;
    private Handler handler = new Handler();
    
    public interface OnGameStateChangeListener {
        void onMoveMade(int moveCount, int manhattanDistance);
        void onGameSolved(int moveCount);
    }
    
    public GameBoard(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }
    
    private void init() {
        textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(80);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setTextAlign(Paint.Align.CENTER);
        
        tilePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        tilePaint.setColor(ContextCompat.getColor(getContext(), R.color.tile_color));
        tilePaint.setShadowLayer(8, 0, 4, Color.parseColor("#80000000"));
        
        borderPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        borderPaint.setColor(ContextCompat.getColor(getContext(), R.color.border_color));
        borderPaint.setStyle(Paint.Style.STROKE);
        borderPaint.setStrokeWidth(4);
        
        hintPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        hintPaint.setColor(ContextCompat.getColor(getContext(), R.color.hint_color));
        hintPaint.setStyle(Paint.Style.STROKE);
        hintPaint.setStrokeWidth(8);
        
        vibrator = (Vibrator) getContext().getSystemService(Context.VIBRATOR_SERVICE);
        
        initializeBoard();
    }
    
    private void initializeBoard() {
        // Create initial solved state
        int counter = 1;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (i == 2 && j == 2) {
                    board[i][j] = 0; // empty tile
                } else {
                    board[i][j] = counter++;
                }
            }
        }
        emptyRow = 2;
        emptyCol = 2;
    }
    
    public void shuffleBoard() {
        Random random = new Random();
        // Perform 50 random moves to shuffle
        for (int i = 0; i < 50; i++) {
            List<int[]> possibleMoves = getPossibleMoves();
            if (!possibleMoves.isEmpty()) {
                int[] move = possibleMoves.get(random.nextInt(possibleMoves.size()));
                swapTiles(move[0], move[1]);
            }
        }
        moveCount = 0;
        showHint = false;
        invalidate();
        
        if (listener != null) {
            listener.onMoveMade(moveCount, ManhattanHeuristic.calculate(board));
        }
    }
    
    private List<int[]> getPossibleMoves() {
        List<int[]> moves = new ArrayList<>();
        int[][] directions = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        
        for (int[] dir : directions) {
            int newRow = emptyRow + dir[0];
            int newCol = emptyCol + dir[1];
            if (newRow >= 0 && newRow < 3 && newCol >= 0 && newCol < 3) {
                moves.add(new int[]{newRow, newCol});
            }
        }
        return moves;
    }
    
    private void swapTiles(int row, int col) {
        board[emptyRow][emptyCol] = board[row][col];
        board[row][col] = 0;
        emptyRow = row;
        emptyCol = col;
    }
    
    public boolean moveTile(int row, int col) {
        // Check if the touched tile is adjacent to empty space
        if ((Math.abs(row - emptyRow) == 1 && col == emptyCol) ||
            (Math.abs(col - emptyCol) == 1 && row == emptyRow)) {
            
            swapTiles(row, col);
            moveCount++;
            
            // Vibrate on successful move
            if (vibrator != null && vibrator.hasVibrator()) {
                vibrator.vibrate(VibrationEffect.createOneShot(20, 
                    VibrationEffect.DEFAULT_AMPLITUDE));
            }
            
            int manhattan = ManhattanHeuristic.calculate(board);
            
            if (listener != null) {
                listener.onMoveMade(moveCount, manhattan);
                
                if (manhattan == 0) {
                    listener.onGameSolved(moveCount);
                    // Celebration effect
                    handler.postDelayed(() -> {
                        // Could add confetti or animation here
                    }, 100);
                }
            }
            
            showHint = false;
            invalidate();
            return true;
        }
        return false;
    }
    
    public void showHint() {
        hintBoard = PuzzleSolver.getHint(board);
        showHint = true;
        invalidate();
        
        // Auto-hide hint after 3 seconds
        handler.postDelayed(() -> {
            showHint = false;
            invalidate();
        }, 3000);
    }
    
    public void solvePuzzle() {
        List<int[][]> solution = PuzzleSolver.findSolution(board);
        if (solution != null && solution.size() > 1) {
            // Animate solution moves
            animateSolution(solution, 1);
        }
    }
    
    private void animateSolution(List<int[][]> solution, int index) {
        if (index < solution.size()) {
            board = solution.get(index);
            emptyRow = findEmptyTile(board)[0];
            emptyCol = findEmptyTile(board)[1];
            moveCount++;
            invalidate();
            
            int manhattan = ManhattanHeuristic.calculate(board);
            if (listener != null) {
                listener.onMoveMade(moveCount, manhattan);
            }
            
            handler.postDelayed(() -> animateSolution(solution, index + 1), 200);
        }
    }
    
    private int[] findEmptyTile(int[][] board) {
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                if (board[i][j] == 0) {
                    return new int[]{i, j};
                }
            }
        }
        return new int[]{2, 2};
    }
    
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        tileSize = Math.min(w, h) / 3;
        
        // Adjust text size based on tile size
        textPaint.setTextSize(tileSize * 0.4f);
    }
    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                int left = j * tileSize;
                int top = i * tileSize;
                int right = left + tileSize;
                int bottom = top + tileSize;
                
                // Draw tile background
                Rect tileRect = new Rect(left, top, right, bottom);
                
                if (board[i][j] != 0) {
                    // Draw regular tile
                    canvas.drawRect(tileRect, tilePaint);
                    
                    // Draw tile number
                    String text = String.valueOf(board[i][j]);
                    float x = left + tileSize / 2f;
                    float y = top + tileSize / 2f - ((textPaint.descent() + textPaint.ascent()) / 2);
                    canvas.drawText(text, x, y, textPaint);
                    
                    // Draw hint border if this tile should move
                    if (showHint && hintBoard != null && 
                        board[i][j] != hintBoard[i][j]) {
                        canvas.drawRect(left + 4, top + 4, right - 4, bottom - 4, hintPaint);
                    }
                } else {
                    // Draw empty tile (just border)
                    canvas.drawRect(tileRect, borderPaint);
                }
                
                // Draw grid lines
                canvas.drawRect(tileRect, borderPaint);
            }
        }
    }
    
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            int x = (int) event.getX();
            int y = (int) event.getY();
            
            int col = x / tileSize;
            int row = y / tileSize;
            
            if (row >= 0 && row < 3 && col >= 0 && col < 3) {
                moveTile(row, col);
            }
            return true;
        }
        return super.onTouchEvent(event);
    }
    
    public int getManhattanDistance() {
        return ManhattanHeuristic.calculate(board);
    }
    
    public int getMoveCount() {
        return moveCount;
    }
    
    public void setOnGameStateChangeListener(OnGameStateChangeListener listener) {
        this.listener = listener;
    }
}