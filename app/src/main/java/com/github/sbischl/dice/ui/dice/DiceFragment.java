package com.github.sbischl.dice.ui.dice;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.sbischl.dice.Dice;
import com.github.sbischl.dice.R;

public class DiceFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    private SwipeRefreshLayout swipeToDiceLayout;
    private LinearLayout diceLayout;
    private LinearLayout layoutTurn;
    //Setup the SharedPreferences:
    private SharedPreferences persistenDict;
    private Dice dice;
    private int rollDelay;
    private TextView textTurn;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_dice, container, false);
        diceLayout = root.findViewById(R.id.dice_layout);

        // Initiliaze Dictionary and dice
        persistenDict = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        dice = new Dice(persistenDict, getActivity().getApplicationContext());

        //Debug Methods
        //dice.purgeDict();
        //dice.simulate(120);
        //Debug Methods

        //Load Last Diceroll and Display it:
        updateDices(dice.getLastRoll());

        //LoadrollDelay if it exists:
        rollDelay = persistenDict.getInt("roll_delay", Integer.parseInt(getResources().getString(R.string.roll_delay)));

        //Set KeepScreenOn Flag
        boolean keepScreenOn = persistenDict.getBoolean("keep_screen_on", Boolean.parseBoolean(getResources().getString(R.string.default_keep_screen_on)));
        root.setKeepScreenOn(keepScreenOn);

        swipeToDiceLayout = root.findViewById(R.id.refresh_to_draw);
        swipeToDiceLayout.setOnRefreshListener(this);
        return root;
    }

    public void onResume() {
        super.onResume();
        //Check if we are in GameMode and if so inflate the textView and attach it to LinearLayout to display whose turn it is
        //Reload the dice
        dice = new Dice(persistenDict, getActivity().getApplicationContext());
        if (dice.gameMode()) {
            layoutTurn = getView().findViewById(R.id.layout_turn);
            layoutTurn.removeAllViews();
            textTurn =  (TextView) getLayoutInflater().inflate(R.layout.text_playerturn, layoutTurn, false);
            textTurn.setText(getResources().getString(R.string.turn_text_1) +  dice.whoseTurn() + getResources().getString(R.string.turn_text_2));
            layoutTurn.addView(textTurn);

            Button skipButton = (Button) getLayoutInflater().inflate(R.layout.button_skip, layoutTurn, false);
            skipButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dice.updateNext();
                    updateTurn();
                }
            });
            layoutTurn.addView(skipButton);
        }

        //Set Subtitle:
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getResources().getString(R.string.dice_subtitle));
    }

    public void updateTurn() {
        textTurn.setText(getResources().getString(R.string.turn_text_1) +  dice.whoseTurn() + getResources().getString(R.string.turn_text_2));
    }

    public void updateDices(int[] draws) {
        diceLayout.removeAllViews();
        for (int number : draws) {
            drawDice("ic_dice_" + number, draws.length);
        }
    }

    public void drawDice(String drawable, int count) {
        // Inflate the ImageView
        ImageView imageOfDice = (ImageView) getLayoutInflater().inflate(R.layout.imageview_dice, diceLayout, false);
        // Get the id of the drawable resource
        int resId = getResources().getIdentifier(drawable, "drawable", getContext().getPackageName());
        //Assign the drawable resource to the ImageView
        imageOfDice.setImageResource(resId);

        //Get the width of the screen and the width of the dice in dp
        DisplayMetrics displayInfo = getContext().getResources().getDisplayMetrics();
        float diceWidth = (getContext().getResources().getDimension(R.dimen.diceWidth)  + getContext().getResources().getDimension(R.dimen.dicePadRight) + getContext().getResources().getDimension(R.dimen.dicePadLeft))/ displayInfo.density;
        float width = displayInfo.widthPixels / displayInfo.density;

        //Decide whether the view has to be scaled
        if (width < count * diceWidth) {
            //Linear Layout does not fit in. We have to scale;
            LinearLayout.LayoutParams scaledLayout = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1);
            imageOfDice.setLayoutParams(scaledLayout);
        }
        //Finally add the view to the layout
        diceLayout.addView(imageOfDice);
    }

    public void mystifyDices(int count) {
        diceLayout.removeAllViews();
        for (int i = 0; i < count; i++) {
            drawDice("ic_dice_question", count);
        }
    }

    @Override
    public void onRefresh() {
        mystifyDices(dice.getNumberOfDices());
        Handler delay = new Handler();
        delay.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Dice!
                updateDices(dice.rollAndStore(Dice.diceTypes.STANDARD));

                //dice.simulate(53);
                //dice.sumOfDeviations();
                //dice.outputLog();

                // If we are in gamemode this gets more complicated:
                if (dice.gameMode()) {
                    dice.updateNext();
                    updateTurn();
                }

                swipeToDiceLayout.setRefreshing(false);
            }
        }, rollDelay);

    }


}
