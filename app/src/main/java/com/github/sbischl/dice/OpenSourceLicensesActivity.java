package com.github.sbischl.dice;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

public class OpenSourceLicensesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_source_licenses);
        getSupportActionBar().setTitle(getResources().getString(R.string.opensource_title));



    }
}
