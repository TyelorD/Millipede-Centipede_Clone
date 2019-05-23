package com.klein.tyelor.tkleinmillipede;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends A_GameActivity {
    private int playerSpeed = 85;
    private GameBoard gameBoard;
    ImageView leftButton, rightButton;

    private void makeToast(final String text) {
        Toast.makeText(this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        leftButton = (ImageView) findViewById(R.id.leftButton);
        rightButton = (ImageView) findViewById(R.id.rightButton);
        gameBoard = (GameBoard) findViewById(R.id.gameBoardView);
        gameBoard.resume();
        gameBoard.connectScoreText((TextView) findViewById(R.id.livesScoreTextView));

        if(savedInstanceState != null)
            playerSpeed = savedInstanceState.getInt("playerSpeed");


        leftButton.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        gameBoard.movePlayer(-1);
                        mHandler = new Handler();
                        mHandler.postDelayed(movePlayer, playerSpeed);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(movePlayer);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable movePlayer = new Runnable() {
                @Override
                public void run() {
                    gameBoard.movePlayer(-1);
                    mHandler.postDelayed(this, playerSpeed);
                }
            };

        });

        rightButton.setOnTouchListener(new View.OnTouchListener() {

            private Handler mHandler;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        if (mHandler != null)
                            return true;
                        gameBoard.movePlayer(1);
                        mHandler = new Handler();
                        mHandler.postDelayed(movePlayer, playerSpeed);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (mHandler == null)
                            return true;
                        mHandler.removeCallbacks(movePlayer);
                        mHandler = null;
                        break;
                }
                return false;
            }

            Runnable movePlayer = new Runnable() {
                @Override
                public void run() {
                    gameBoard.movePlayer(1);
                    mHandler.postDelayed(this, playerSpeed);
                }
            };

        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // This adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here
        int id = item.getItemId();
        if(id == R.id.action_about) {
            makeToast("Tyelor Klein's Millipede, Spring 2017, Tyelor D Klein");
            return true;
        } else if(id == R.id.action_settings) {
            gameBoard.playClip(R.raw.openmenu);
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        gameBoard.pause();
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        int newSpeed = preferences.getInt(getString(R.string.prefs_PlayerSpeed), 85);
        if(playerSpeed != newSpeed)
            gameBoard.resetBoard(true, false);
        playerSpeed = newSpeed;
        int snakeSegs = preferences.getInt(getString(R.string.prefs_SnakeSegs), 10);
        int numRocks = preferences.getInt(getString(R.string.prefs_NumRocks), 15);
        int numLives = preferences.getInt(getString(R.string.prefs_NumLives), 3);
        int soundVolume = preferences.getInt(getString(R.string.prefs_SoundVolume), 50);
        int snakeSpeed = preferences.getInt(getString(R.string.prefs_SnakeSpeed), 5);
        int powerUpSpeed = preferences.getInt(getString(R.string.prefs_PowerUpsSpeed), 16);
        boolean snakeNum = preferences.getBoolean(getString(R.string.prefs_SnakeNum), false);
        boolean playSounds = preferences.getBoolean(getString(R.string.prefs_PlaySounds), true);
        boolean powerUpsOn = preferences.getBoolean(getString(R.string.prefs_PowerUpsOn), true);
        boolean deathReset = preferences.getBoolean(getString(R.string.prefs_ResetOnDeath), true);

        gameBoard.resume();
        gameBoard.setSettings(snakeSegs, numRocks, numLives, snakeSpeed, snakeNum, soundVolume, playSounds,
                powerUpsOn, powerUpSpeed, deathReset);
        gameBoard.playClip(R.raw.closemenu);
    }

    @Override
    protected void onStop() {
        gameBoard.pause();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        gameBoard.pause();
        gameBoard.clearGameBoard(true);
        gameBoard = null;
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt("playerSpeed", playerSpeed);
        super.onSaveInstanceState(outState);
    }

    public void onFirePressed(View view) {
        gameBoard.fireBullet();
    }

    @Override
    public void reportLose(final int kills, final int score) {
        if(kills == 0 && score < 200)
            makeToast("You suck! You didn't get one kill and only got a score of " + score + "!");
        else
            makeToast("You Lost! You killed " + kills + " millipedes and got a score of " + score + "!");
    }
}
