package net.liucs.delacruz.memorygamewithmenu;

import android.content.Intent;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.TextView;
import java.util.ArrayList;

//NOT MADE FOR LANDSCAPE VIEW


public class HardMemoryGame extends ActionBarActivity implements Runnable, View.OnClickListener {

    private MediaPlayer startSound, endSound, backgroundMusic,
            finishSound, equalSound, timesUp, wrongChoice, waitingSound;

    private static final String LOG_NAME = "MemoryGame";
    private static final String ID_CARDS_REMAINING = "totalNumberOfCards";
    private static final String ID_CARD_VALUES = "cardValues";
    private static final String ID_FACE_UP_INDICES = "faceUpIndices";
    private static final String ID_REMOVED_INDICES = "removedIndices";
    private static final String ID_MUSIC_POSITION = "musicPosition";
    private static final String ID_SECONDS_LEFT = "timeRemain";

    private final Handler handler = new Handler();

    private TextView textView, timerText, winnerView, loserView, scoreView;
    private ImageView cardViews[];
    private Button mainMenuButton, restartButton;

    //state of game
    private int totalNumberOfCards;
    private int totalCardsLeft;
    private int cardValues[];
    private ArrayList<Integer> faceUpIndices = new ArrayList<>();
    private ArrayList<Integer> removedIndices = new ArrayList<>();
    private int musicPosition;
    private int click = 0;
    private long timeRemain;
    private CountDownTimer myTimer;
    private int score = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hard_memory_game);
        startSound = MediaPlayer.create(this, R.raw.start_game);
        endSound = MediaPlayer.create(this, R.raw.end_game);
        backgroundMusic = MediaPlayer.create(this, R.raw.background_game);
        finishSound = MediaPlayer.create(this, R.raw.finish_game);
        equalSound = MediaPlayer.create(this, R.raw.same_game);
        timesUp = MediaPlayer.create(this, R.raw.end_game);
        wrongChoice = MediaPlayer.create(this, R.raw.wrong_game);
        waitingSound = MediaPlayer.create(this, R.raw.waiting_music);

        GridLayout tileGrid = (GridLayout) findViewById(R.id.tileGrid);
        textView = (TextView) findViewById(R.id.textView);
        timerText = (TextView) findViewById(R.id.timerText);
        winnerView = (TextView) findViewById(R.id.winnerView);
        loserView = (TextView) findViewById(R.id.loserView);
        scoreView = (TextView) findViewById(R.id.scoreView);
        mainMenuButton = (Button) findViewById(R.id.mainMenuButton);
        restartButton = (Button) findViewById(R.id.restart_button);
        mainMenuButton.setOnClickListener(this);
        restartButton.setOnClickListener(this);
        restartButton.setVisibility(View.INVISIBLE);
        mainMenuButton.setVisibility(View.INVISIBLE);
        if (savedInstanceState != null) {
            totalNumberOfCards = savedInstanceState.getInt(ID_CARDS_REMAINING);
            cardValues = savedInstanceState.getIntArray(ID_CARD_VALUES);
            faceUpIndices = savedInstanceState.getIntegerArrayList(ID_FACE_UP_INDICES);
            removedIndices = savedInstanceState.getIntegerArrayList(ID_REMOVED_INDICES);
            musicPosition = savedInstanceState.getInt(ID_MUSIC_POSITION);
            timeRemain = savedInstanceState.getLong(ID_SECONDS_LEFT);
        } else { //Start fresh
            timeRemain = 160 * 1000;
            totalNumberOfCards = tileGrid.getRowCount() * tileGrid.getColumnCount();
            Log.i(LOG_NAME, "You created " + totalNumberOfCards + " cards");

            cardValues = new int[totalNumberOfCards];
            for (int i = 0; i < totalNumberOfCards; i++) {
                cardValues[i] = i / 2;
            }
            shuffle(cardValues);

        }
        cardViews = new ImageView[totalNumberOfCards];
        for (int i = 0; i < totalNumberOfCards; i++) {
            final int j = i;
            cardViews[i] = new ImageView(this);
            cardViews[i].setImageResource(R.drawable.back_card);
            if (removedIndices.contains(i)) {
                cardViews[i].setVisibility(View.INVISIBLE);
            }
            cardViews[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Animation a = AnimationUtils.loadAnimation(HardMemoryGame.this, R.anim.flip);
                    cardViews[j].startAnimation(a);

                    Log.i(LOG_NAME, "You clicked on tile #" + j);
                    Log.i(LOG_NAME, "That tile's front side is " + cardValues[j]);
                    if (!faceUpIndices.contains(j)     // This card not already face up
                            && faceUpIndices.size() < 2) { // Not yet two cards face up
                        faceUpIndices.add(j);
                        revealCardAtPosition(j);
                        if (faceUpIndices.size() == 2) {
                            handler.postDelayed(HardMemoryGame.this, 1000);
                            click++;
                            Log.i(LOG_NAME, "That was click # " + click);
                        }
                    }
                }
            });
            tileGrid.addView(cardViews[i]);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(LOG_NAME, "Resuming music at " + musicPosition);
        Log.i(LOG_NAME, "Resuming timer at " + timeRemain);
        totalCardsLeft = totalNumberOfCards;
        startSound.start();
        backgroundMusic.start();
        backgroundMusic.setLooping(true);
        scoreView.setTextColor(Color.BLACK);
        scoreView.setText("Score: " + score);
        myTimer = new CountDownTimer(timeRemain, 1000) {

            public void onTick(long timeLeft) {
                timerText.setTextColor(Color.BLACK);
                timerText.setText("Seconds remaining: " + timeLeft / 1000);
                timeRemain = timeLeft;
                if (timeLeft <= 30000) {
                    timerText.setTextColor(Color.RED);
                    scoreView.setTextColor(Color.RED);
                }
            }

            public void onFinish() {
                textView.setTextColor(Color.RED);
                loserView.setTextColor(Color.RED);
                textView.setText("Game Over. You ran out of time");
                timerText.setVisibility(View.INVISIBLE);
                scoreView.setVisibility(View.INVISIBLE);
                myTimer.cancel();
                timesUp.start();
                loserView.setText("You had a total of : " + totalCardsLeft / 2 + " pairs left");
                removeAllCards();
                waitingSound.start();
                waitingSound.setLooping(true);
                restartButton.setVisibility(View.VISIBLE);
                mainMenuButton.setVisibility(View.VISIBLE);
            }
        }.start();
    }
    @Override
    protected void onPause() {
        super.onPause();
        musicPosition = backgroundMusic.getCurrentPosition();
        Log.i(LOG_NAME, "Paused music at " + musicPosition);
        Log.i(LOG_NAME, "Paused timer at " + timeRemain);
        backgroundMusic.pause();
        timesUp.pause();
        myTimer.cancel();
    }
    protected void onDestroy() {
        super.onDestroy();
        endSound.release();
        startSound.release();
        backgroundMusic.release();
        equalSound.release();
        finishSound.release();
        timesUp.release();
        myTimer.cancel();
        wrongChoice.release();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(ID_CARDS_REMAINING, totalNumberOfCards);
        outState.putIntArray(ID_CARD_VALUES, cardValues);
        outState.putIntegerArrayList(ID_FACE_UP_INDICES, faceUpIndices);
        outState.putIntegerArrayList(ID_REMOVED_INDICES, removedIndices);
        Log.i(LOG_NAME, "Saving instance state.");
        outState.putInt(ID_MUSIC_POSITION, musicPosition);
        outState.putLong(ID_SECONDS_LEFT, timeRemain);
    }

    private void removeAllCards(){
        for (int i = 0; i < totalNumberOfCards; i++) {
            cardViews[i].setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void run() {
        // Do they match
        if (cardValues[faceUpIndices.get(0)] ==
                cardValues[faceUpIndices.get(1)]) {
            equalSound.start();
            for (int i = 0; i <= 1; i++) {
                Animation b = AnimationUtils.loadAnimation(HardMemoryGame.this, R.anim.fling);
                cardViews[faceUpIndices.get(i)].startAnimation(b);
                cardViews[faceUpIndices.get(i)].setVisibility(View.INVISIBLE);
                removedIndices.add(faceUpIndices.get(i));
                totalCardsLeft-- ;
                score += 10;
                scoreView.setText("Score: " + score);
            }
        } else {
            for (int i = 0; i <= 1; i++) {
                Animation a = AnimationUtils.loadAnimation(HardMemoryGame.this, R.anim.flip);
                cardViews[faceUpIndices.get(i)].startAnimation(a);
                wrongChoice.start();
                cardViews[faceUpIndices.get(i)].setImageResource(R.drawable.back_card);
                score -= 4;
                scoreView.setText("Score: " + score);
            }
        }
        // Nothing is face up now
        faceUpIndices.clear();
        if (removedIndices.size() == totalNumberOfCards) {
            textView.setTextColor(Color.GREEN);
            winnerView.setTextColor(Color.GREEN);
            scoreView.setTextColor(Color.GREEN);
            textView.setText("Congratulations...You have won!!");
            winnerView.setText("You needed a total of " + click +
                    " tries to complete this game");
            scoreView.setText("Score: " + score + " out of a possible 360");
            myTimer.cancel();
            backgroundMusic.stop();
            timesUp.stop();
            finishSound.start();
            waitingSound.start();
            waitingSound.setLooping(true);
            timerText.setVisibility(View.INVISIBLE);
            restartButton.setVisibility(View.VISIBLE);
            mainMenuButton.setVisibility(View.VISIBLE);
        }
    }

    // from http://www.dotnetperls.com/shuffle-java
    private static void shuffle(int[] array) {
        int n = array.length;
        for (int i = 0; i < array.length; i++) {
            // Get a random index of the array past i.
            int random = i + (int) (Math.random() * (n - i));
            // Swap the random element with the present element.
            int randomElement = array[random];
            array[random] = array[i];
            array[i] = randomElement;
        }
    }

    @Override
    public void onClick(View v) {
        if (v == mainMenuButton) {
            Log.i("demo", "You clicked the Main Menu button");
            Intent menu = new Intent(this, MainMenu.class);
            waitingSound.stop();
            startActivity(menu);
        }
        if (v == restartButton) {
            Log.i("demo","You clicked the Restart button");
            Intent restart = new Intent(this, EasyMemoryGame.class);
            waitingSound.stop();
            startActivity(restart);
        }
    }



    private void revealCardAtPosition(int j) {
        switch(cardValues[j]) {
            case 0: cardViews[j].setImageResource(R.drawable.atl_hawks); break;
            case 1: cardViews[j].setImageResource(R.drawable.bk_nets); break;
            case 2: cardViews[j].setImageResource(R.drawable.chi_bulls); break;
            case 3: cardViews[j].setImageResource(R.drawable.cle_cavaliers); break;
            case 4: cardViews[j].setImageResource(R.drawable.dallas_mavs); break;
            case 5: cardViews[j].setImageResource(R.drawable.gs_warriors); break;
            case 6: cardViews[j].setImageResource(R.drawable.hou_rockets); break;
            case 7: cardViews[j].setImageResource(R.drawable.la_clippers); break;
            case 8: cardViews[j].setImageResource(R.drawable.la_lakers); break;
            case 9: cardViews[j].setImageResource(R.drawable.mia_heat); break;
            case 10: cardViews[j].setImageResource(R.drawable.nba_logo); break;
            case 11: cardViews[j].setImageResource(R.drawable.ny_knicks); break;
            case 12: cardViews[j].setImageResource(R.drawable.okc_thunder); break;
            case 13: cardViews[j].setImageResource(R.drawable.por_trail); break;
            case 14: cardViews[j].setImageResource(R.drawable.sa_spurs); break;
            case 15: cardViews[j].setImageResource(R.drawable.wash_wizards); break;
            case 16: cardViews[j].setImageResource(R.drawable.tor_raptors); break;
            case 17: cardViews[j].setImageResource(R.drawable.sac_kings); break;
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}