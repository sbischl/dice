package com.github.sbischl.dice.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.github.sbischl.dice.Dice;
import com.github.sbischl.dice.R;

public class SettingsFragment extends Fragment  implements AdapterView.OnItemSelectedListener  {

    private Spinner numberOfDicesSpinner;
    private Spinner numberOfSidesSpinner;
    private SharedPreferences persistenDict;
    private int rollDelay;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_settings, container, false);
        persistenDict = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());

        numberOfDicesSpinner =  root.findViewById(R.id.select_number_dices);
        ArrayAdapter<CharSequence> adapterNumberDices = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.number_of_dices, android.R.layout.simple_spinner_item);
        adapterNumberDices.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numberOfDicesSpinner.setAdapter(adapterNumberDices);
        numberOfDicesSpinner.setOnItemSelectedListener(this);
        numberOfDicesSpinner.setSelection(persistenDict.getInt("number_dices",1) -1);

        numberOfSidesSpinner =  root.findViewById(R.id.select_number_sides);
        ArrayAdapter<CharSequence> adapterNumberSides = ArrayAdapter.createFromResource(getActivity().getApplicationContext(), R.array.number_of_sides, android.R.layout.simple_spinner_item);
        adapterNumberSides.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        numberOfSidesSpinner.setAdapter(adapterNumberSides);
        numberOfSidesSpinner.setOnItemSelectedListener(this);
        numberOfSidesSpinner.setSelection(persistenDict.getInt( "number_sides_dice", 1) - 1);

        rollDelay = persistenDict.getInt("roll_delay", Integer.parseInt(getResources().getString(R.string.roll_delay)));
        final EditText rollDelayField = root.findViewById(R.id.editRollDelay);
        rollDelayField.setText("" + rollDelay);
        rollDelayField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
            @Override
            public void afterTextChanged(Editable s) {
                try {
                    int delay = Integer.parseInt(rollDelayField.getText().toString());
                    persistenDict.edit().putInt("roll_delay", delay).apply();
                }
                catch (NumberFormatException e) {}
            }
        });

        boolean keepScreenOn = persistenDict.getBoolean("keep_screen_on", Boolean.parseBoolean(getResources().getString(R.string.default_keep_screen_on)));
        Switch keepScreenOnSwitch = root.findViewById(R.id.keepDisplayOn);
        keepScreenOnSwitch.setChecked(keepScreenOn);

        keepScreenOnSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                persistenDict.edit().putBoolean("keep_screen_on", isChecked).apply();
            }
        });

        return root;
    }

    public void onResume() {
        super.onResume();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setSubtitle(getResources().getString(R.string.settings_subtitle));
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if(parent.getId() == R.id.select_number_dices)
        {
            // the select number of dices spinner was triggered
            String selectedItem = parent.getItemAtPosition(position).toString();
            selectedItem = selectedItem.substring(0,1);
            int numberOfDices = Integer.parseInt(selectedItem);
            if (numberOfDices != persistenDict.getInt("number_dices",-1)) {
                //we have to update the dictionary:
                Dice dice = new Dice(persistenDict, getActivity().getApplicationContext());
                dice.setNumberOfDices(numberOfDices);
                dice.writeNumberOfDices(numberOfDices);
                dice.resetDictKeys();
            }

        }
        else if(parent.getId() == R.id.select_number_sides)
        {
            // the select number of dice sides spinner was triggered
            String selectedItem = parent.getItemAtPosition(position).toString();
            selectedItem = selectedItem.substring(0,1);
            int SidesOfDice = Integer.parseInt(selectedItem);
            if (SidesOfDice != persistenDict.getInt("number_sides_dice",-1)) {
                //we have to update the dictionary:
                Dice dice = new Dice(persistenDict, getActivity().getApplicationContext());
                dice.setNumberOfSides(SidesOfDice);
                dice.writeNumberOfSides(SidesOfDice);
                dice.resetDictKeys();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
}
