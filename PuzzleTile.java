package com.example.eightpuzzle;

import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;

/**
 * Represents a single tile in the 8-puzzle game
 * Handles tile properties, drawing calculations, and animations
 */
public class PuzzleTile {
    private int value;           // The number on the tile (1-8, 0 for empty)
    private int currentRow;       // Current row position
    private int currentCol;       // Current column position
    private int targetRow;        // Target row for animation
    private int targetCol;        // Target column for animation
    
    // Animation properties
    private float animatedX;       // Current X position for smooth animation
    private float animatedY;       // Current Y position for smooth animation
    private boolean isAnimating;   // Whether tile is currently moving
    private long animationStartTime;
    private long animationDuration = 200; // 200ms animation
    
    // Visual properties
    private int tileColor;
    private int textColor;
    private float cornerRadius = 20f;
    
    // Static colors (can be overridden by theme)
    private static final int DEFAULT_TILE_COLOR = Color.rgb(102, 126, 234);
    private static final int DEFAULT_TEXT_COLOR = Color.WHITE;
    private static final int EMPTY_TILE_COLOR = Color.LTGRAY;
    
    public PuzzleTile(int value, int row, int col) {
        this.value = value;
        this.currentRow = row;
        this.currentCol = col;
        this.targetRow = row;
        this.targetCol = col;
        this.animatedX = col;
        this.animatedY = row;
        this.isAnimating = false;
        
        // Set colors based on value
        if (value == 0) {
            this.tileColor = EMPTY_TILE_COLOR;
            this.textColor = Color.GRAY;
        } else {
            this.tileColor = getTileColorForValue(value);
            this.textColor = DEFAULT_TEXT_COLOR;
        }
    }
    
    /**
     * Get different colors for different tile values
     */
    private int getTileColorForValue(int value) {
        switch (value) {
            case 1: return Color.rgb(102, 126, 234);  // Purple-blue
            case 2: return Color.rgb(76, 175, 80);    // Green
            case 3: return Color.rgb(255, 152, 0);    // Orange
            case 4: return Color.rgb(233, 30, 99);    // Pink
            case 5: return Color.rgb(33, 150, 243);   // Blue
            case 6: return Color.rgb(156, 39, 176);   // Purple
            case 7: return Color.rgb(0, 150, 136);    // Teal
            case 8: return Color.rgb(244, 67, 54);    // Red
            default: return DEFAULT_TILE_COLOR;
        }
    }
    
    /**
     * Start animation to new position
     */
    public void animateTo(int newRow, int newCol) {
        this.targetRow = newRow;
        this.targetCol = newCol;
        this.isAnimating = true;
        this.animationStartTime = System.currentTimeMillis();
    }
    
    /**
     * Update animation based on elapsed time
     */
    public boolean updateAnimation() {
        if (!isAnimating) return false;
        
        long elapsed = System.currentTimeMillis() - animationStartTime;
        float progress = Math.min(1.0f, (float) elapsed / animationDuration);
        
        // Ease-out interpolation for smooth motion
        progress = 1 - (1 - progress) * (1 - progress);
        
        // Interpolate position
        animatedX = currentCol + (targetCol - currentCol) * progress;
        animatedY = currentRow + (targetRow - currentRow) * progress;
        
        if (progress >= 1.0f) {
            // Animation complete
            currentRow = targetRow;
            currentCol = targetCol;
            animatedX = currentCol;
            animatedY = currentRow;
            isAnimating = false;
        }
        
        return true;
    }
    
    /**
     * Snap instantly to position (no animation)
     */
    public void snapTo(int row, int col) {
        this.currentRow = row;
        this.currentCol = col;
        this.targetRow = row;
        this.targetCol = col;
        this.animatedX = col;
        this.animatedY = row;
        this.isAnimating = false;
    }
    
    /**
     * Check if this tile is empty (0)
     */
    public boolean isEmpty() {
        return value == 0;
    }
    
    /**
     * Check if this tile is adjacent to given position
     */
    public boolean isAdjacentTo(int row, int col) {
        return (Math.abs(currentRow - row) == 1 && currentCol == col) ||
               (Math.abs(currentCol - col) == 1 && currentRow == row);
    }
    
    /**
     * Get the rectangle for drawing this tile
     */
    public RectF getTileRect(float tileSize, float padding) {
        float left = animatedX * tileSize + padding;
        float top = animatedY * tileSize + padding;
        float right = left + tileSize - 2 * padding;
        float bottom = top + tileSize - 2 * padding;
        return new RectF(left, top, right, bottom);
    }
    
    /**
     * Draw the tile on canvas
     */
    public void draw(Canvas canvas, float tileSize, Paint paint) {
        if (isEmpty()) return; // Don't draw empty tile
        
        float padding = tileSize * 0.05f;
        RectF rect = getTileRect(tileSize, padding);
        
        // Draw tile background
        paint.setColor(tileColor);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        
        // Draw tile border
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(4);
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, paint);
        
        // Draw tile number
        paint.setColor(textColor);
        paint.setTextSize(tileSize * 0.4f);
        paint.setTextAlign(Paint.Align.CENTER);
        paint.setStyle(Paint.Style.FILL);
        
        float x = rect.centerX();
        float y = rect.centerY() - (paint.descent() + paint.ascent()) / 2;
        canvas.drawText(String.valueOf(value), x, y, paint);
    }
    
    // Getters and Setters
    public int getValue() { return value; }
    public void setValue(int value) { this.value = value; }
    
    public int getCurrentRow() { return currentRow; }
    public int getCurrentCol() { return currentCol; }
    
    public int getTargetRow() { return targetRow; }
    public int getTargetCol() { return targetCol; }
    
    public boolean isAnimating() { return isAnimating; }
    
    public void setTileColor(int color) { this.tileColor = color; }
    public void setTextColor(int color) { this.textColor = color; }
    
    /**
     * Create a copy of this tile
     */
    public PuzzleTile copy() {
        PuzzleTile copy = new PuzzleTile(value, currentRow, currentCol);
        copy.tileColor = this.tileColor;
        copy.textColor = this.textColor;
        return copy;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PuzzleTile other = (PuzzleTile) obj;
        return value == other.value && 
               currentRow == other.currentRow && 
               currentCol == other.currentCol;
    }
    
    @Override
    public String toString() {
        return value == 0 ? " " : String.valueOf(value);
    }
}