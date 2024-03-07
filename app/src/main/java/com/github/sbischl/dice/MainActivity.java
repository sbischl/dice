package com.github.sbischl.dice;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.preference.PreferenceManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private SharedPreferences persistenDict;
    private Menu optionsMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        BottomNavigationView navView = findViewById(R.id.nav_view);
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        persistenDict = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.navigation_dice, R.id.navigation_stats, R.id.navigation_settings)
                .build();

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(navView, navController);

        //Design Toolbar:
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher_round);

    }

    public void onRestart() {
        super.onRestart();
        if (persistenDict.getBoolean("game_mode", false)) {
            MenuItem startStopGame = optionsMenu.findItem(R.id.item_start_stop_game);
            startStopGame.setTitle(getResources().getString(R.string.leave_game_mode_text));
        } else {
            MenuItem startStopGame = optionsMenu.findItem(R.id.item_start_stop_game);
            startStopGame.setTitle(getResources().getString(R.string.game_mode_text));
        }
    }

    public boolean onCreateOptionsMenu (Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu, menu);
        this.optionsMenu = menu;
        if (persistenDict.getBoolean("game_mode", false)) {
            MenuItem startStopGame = menu.findItem(R.id.item_start_stop_game);
            startStopGame.setTitle(getResources().getString(R.string.leave_game_mode_text));
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.item_exit_app) {
            // quit the app
            this.finishAffinity();
        }
        else if (item.getItemId() == R.id.item_start_stop_game) {
            if (persistenDict.getBoolean("game_mode", false)) {
                // stop Game
                stopGame();
                MenuItem startStopGame = optionsMenu.findItem(R.id.item_start_stop_game);
                startStopGame.setTitle(getResources().getString(R.string.game_mode_text));
            }
            else {
                // start game
                Intent startGameConfig = new Intent(getApplicationContext(), ConfigureGame.class);
                startActivity(startGameConfig);
            }
        }
        else if (item.getItemId() == R.id.item_about) {
            //start the about page activity
            Intent starAboutPage = new Intent(getApplicationContext(), AboutActivity.class);
            startActivity(starAboutPage);

        }
        else {
            return super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void stopGame() {
        for (int i = 0; i < persistenDict.getInt("number_of_players", 0); i++) {
            persistenDict.edit().remove("playername_" + i).apply();
        }
        Dice createDictKeysDice = new Dice(persistenDict, getApplicationContext());
        createDictKeysDice.removeGameDictKeys();
        persistenDict.edit().putBoolean("game_mode",false).apply();

        // Reload App. A Bit Hackish. It should be enough to reload the fragment but i dunno how
        Intent intent = getIntent();
        finish();
        startActivity(intent);
    }

}
