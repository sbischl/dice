package com.github.sbischl.dice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.List;

public class ConfigureGame extends AppCompatActivity {
    private int numberPlayers;
    private LinearLayout playersSectionLayout;
    private LinearLayout configPlayersLayout;
    private LinearLayout playerNamesLayout;
    private ScrollView view;
    private SharedPreferences persistenDict;
    List<String> playerNames;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_configure_game);

        //Get Dictionary:
        persistenDict = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //Get Scrollview
        view = findViewById(R.id.gameconfig_scrollview);
        //Get configPlayersLayout
        configPlayersLayout = findViewById(R.id.gameconfig_linlayout);

        //Set the title
        getSupportActionBar().setTitle(getResources().getString(R.string.game_mode_text));

        //Get the button
        final Button confirmPlayers = findViewById(R.id.button_confirm_nr_players);
        confirmPlayers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText numberOfPlayersField = findViewById(R.id.edit_number_players);

                try {
                    numberPlayers = Integer.parseInt(numberOfPlayersField.getText().toString());
                    if (numberPlayers > 0) {
                        updateNumberPlayers();
                        confirmPlayers.setText("Update");
                    }
                }
                catch (NumberFormatException e) {
                    numberPlayers = Integer.parseInt(numberOfPlayersField.getHint().toString());
                    numberOfPlayersField.setText("" + numberPlayers);
                    updateNumberPlayers();
                    confirmPlayers.setText("Update");
                }
            }
        });
    }

    public void updateNumberPlayers() {
        if (playersSectionLayout == null) {
            //we need to inflate the linear layout
            playersSectionLayout = (LinearLayout) getLayoutInflater().inflate(R.layout.linearlayout_players, configPlayersLayout, false);
            configPlayersLayout.addView(playersSectionLayout);

            //Handle what happens when the start game button is pressed
            Button startGameButton = findViewById(R.id.button_startgame);
            startGameButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startGame();
                }
            });


            //This line has to come after Addview, otherwise the playerNamesLayout is null
            playerNamesLayout = findViewById(R.id.player_names_layout);
        }
        int currentPlayers = playerNamesLayout.getChildCount();

        for (int i = currentPlayers; i > numberPlayers; i--) {
            playerNamesLayout.removeViewAt(i - 1);
        }

        for (int i = currentPlayers; i < numberPlayers; i++) {
            // Add a EditField for every player
            EditText editPlayerName = (EditText) getLayoutInflater().inflate(R.layout.edittext_playername, playerNamesLayout, false);
            editPlayerName.setHint(getResources().getString(R.string.player_text) + " " + (i+1));
            playerNamesLayout.addView(editPlayerName);
        }


    }

    public void startGame() {
        playerNames = new ArrayList<>();
        for (int i = 0; i < playerNamesLayout.getChildCount(); i++) {
            String name =  ((EditText)playerNamesLayout.getChildAt(i)).getText().toString();
            if (name.equals("")) {
                name = ((EditText)playerNamesLayout.getChildAt(i)).getHint().toString();
            }
            playerNames.add(name);
        }
        generateDictEntriesForGame();
        finish();
    }

    public void generateDictEntriesForGame() {
        persistenDict.edit().putInt("number_of_players", playerNames.size()).apply();
        for (int i = 0; i < playerNames.size(); i++) {
            persistenDict.edit().putString("playername_" + i, playerNames.get(i)).apply();
        }
        Dice createDictKeysDice = new Dice(persistenDict, getApplicationContext());
        createDictKeysDice.setPlayerNames(playerNames);
        createDictKeysDice.writeGameDictKeys();
        persistenDict.edit().putInt("whose_turn",0).apply();
        persistenDict.edit().putBoolean("game_mode",true).apply();
    }




}
