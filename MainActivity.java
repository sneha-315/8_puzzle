package com.example.eightpuzzle;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity 
        implements GameBoard.OnGameStateChangeListener {
    
    private GameBoard gameBoard;
    private TextView tvMoves;
    private TextView tvManhattan;
    private TextView tvStatus;
    private Button btnHint;
    private Button btnShuffle;
    private Button btnSolve;
    private MediaPlayer moveSound;
    private MediaPlayer winSound;
    private Handler handler = new Handler();
    private SharedPreferences prefs;
    private int bestScore = Integer.MAX_VALUE;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // Initialize views
        gameBoard = findViewById(R.id.gameBoard);
        tvMoves = findViewById(R.id.tvMoves);
        tvManhattan = findViewById(R.id.tvManhattan);
        tvStatus = findViewById(R.id.tvStatus);
        btnHint = findViewById(R.id.btnHint);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnSolve = findViewById(R.id.btnSolve);
        
        // Setup toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // Load preferences
        prefs = getSharedPreferences("game_prefs", MODE_PRIVATE);
        bestScore = prefs.getInt("best_score", Integer.MAX_VALUE);
        
        // Initialize sounds
        try {
            moveSound = MediaPlayer.create(this, R.raw.move_sound);
            winSound = MediaPlayer.create(this, R.raw.win_sound);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Setup listeners
        gameBoard.setOnGameStateChangeListener(this);
        
        btnHint.setOnClickListener(v -> {
            gameBoard.showHint();
            Snackbar.make(v, "Hint: Move highlighted tile", Snackbar.LENGTH_SHORT).show();
        });
        
        btnShuffle.setOnClickListener(v -> {
            showShuffleConfirmation();
        });
        
        btnSolve.setOnClickListener(v -> {
            showSolveConfirmation();
        });
        
        // Show welcome message
        showWelcomeDialog();
    }
    
    private void showWelcomeDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Welcome to 8-Puzzle!");
        builder.setMessage("Arrange the tiles in order 1-8 with the empty space at bottom-right.\n\n" +
                          "• Tap tiles adjacent to empty space to move\n" +
                          "• Use Hint button for suggestions\n" +
                          "• Try to solve in minimum moves!");
        builder.setPositiveButton("Start Playing", null);
        builder.show();
    }
    
    private void showShuffleConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Shuffle Board");
        builder.setMessage("Are you sure you want to shuffle? Current progress will be lost.");
        builder.setPositiveButton("Shuffle", (dialog, which) -> {
            gameBoard.shuffleBoard();
            Snackbar.make(gameBoard, "Board shuffled!", Snackbar.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    private void showSolveConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Auto-Solve");
        builder.setMessage("Watch the computer solve the puzzle?");
        builder.setPositiveButton("Solve", (dialog, which) -> {
            gameBoard.solvePuzzle();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        
        if (id == R.id.action_new_game) {
            gameBoard.shuffleBoard();
            return true;
        } else if (id == R.id.action_reset) {
            gameBoard.shuffleBoard(); // Actually re-shuffle
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialog();
            return true;
        } else if (id == R.id.action_settings) {
            showSettingsDialog();
            return true;
        }
        
        return super.onOptionsItemSelected(item);
    }
    
    private void showAboutDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("About 8-Puzzle");
        builder.setMessage("8-Puzzle v1.0\n\n" +
                          "A classic sliding puzzle game with A* solver\n" +
                          "using Manhattan distance heuristic.\n\n" +
                          "© 2025 Your Company\n" +
                          "All rights reserved.");
        builder.setPositiveButton("OK", null);
        builder.show();
    }
    
    private void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings");
        String[] options = {"Enable Sound", "Enable Vibration", "Show Move Counter"};
        boolean[] checked = {true, true, true};
        
        builder.setMultiChoiceItems(options, checked, (dialog, which, isChecked) -> {
            // Handle preference changes
        });
        
        builder.setPositiveButton("Save", (dialog, which) -> {
            Snackbar.make(gameBoard, "Settings saved", Snackbar.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancel", null);
        builder.show();
    }
    
    @Override
    public void onMoveMade(int moveCount, int manhattanDistance) {
        tvMoves.setText("Moves: " + moveCount);
        tvManhattan.setText("Distance: " + manhattanDistance);
        
        // Play sound effect
        if (moveSound != null) {
            moveSound.start();
        }
        
        // Update status
        if (manhattanDistance < 5) {
            tvStatus.setText("Almost there!");
            tvStatus.setTextColor(getColor(R.color.warning_color));
        } else if (manhattanDistance < 10) {
            tvStatus.setText("Getting closer...");
            tvStatus.setTextColor(getColor(R.color.info_color));
        } else {
            tvStatus.setText("Keep going!");
            tvStatus.setTextColor(getColor(R.color.normal_color));
        }
    }
    
    @Override
    public void onGameSolved(int moveCount) {
        tvStatus.setText("SOLVED!");
        tvStatus.setTextColor(getColor(R.color.success_color));
        
        // Play win sound
        if (winSound != null) {
            winSound.start();
        }
        
        // Check for new best score
        if (moveCount < bestScore) {
            bestScore = moveCount;
            prefs.edit().putInt("best_score", bestScore).apply();
            
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("NEW RECORD!");
            builder.setMessage("Congratulations! You solved the puzzle in " + moveCount + " moves!\n" +
                              "This is your new best score!");
            builder.setPositiveButton("Awesome!", null);
            builder.show();
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Puzzle Solved!");
            builder.setMessage("Well done! You solved it in " + moveCount + " moves.\n" +
                              "Best score: " + (bestScore == Integer.MAX_VALUE ? "N/A" : bestScore));
            builder.setPositiveButton("Play Again", (dialog, which) -> {
                gameBoard.shuffleBoard();
            });
            builder.setNegativeButton("Close", null);
            builder.show();
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (moveSound != null) {
            moveSound.release();
            moveSound = null;
        }
        if (winSound != null) {
            winSound.release();
            winSound = null;
        }
    }
}