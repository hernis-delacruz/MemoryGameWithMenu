package net.liucs.delacruz.memorygamewithmenu;


import android.content.Intent;
import android.media.MediaPlayer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

//NOT MADE FOR LANDSCAPE VIEW

public class MainMenu extends ActionBarActivity implements View.OnClickListener{

    private Button Easy;
    private Button Hard;
    private MediaPlayer menuMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
        Easy = (Button) findViewById(R.id.button1);
        Hard = (Button) findViewById(R.id.button2);
        Easy.setOnClickListener(this);
        Hard.setOnClickListener(this);
        menuMusic = MediaPlayer.create(this, R.raw.menu_music);
        menuMusic.start();
        menuMusic.setLooping(true);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        menuMusic.release();
    }

    @Override
    public void onClick(View v) {
        if (v == Easy) {
            Log.i("demo", "You clicked the EASY LEVEL");
            Intent easy = new Intent(this, EasyMemoryGame.class);
            menuMusic.stop();
            startActivity(easy);
        }
        if (v == Hard) {
            Log.i("demo","You clicked the HARD LEVEL");
            Intent hard = new Intent(this, HardMemoryGame.class);
            menuMusic.stop();
            startActivity(hard);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_menu, menu);
        return true;
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
