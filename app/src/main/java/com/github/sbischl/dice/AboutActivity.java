package com.github.sbischl.dice;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);
        getSupportActionBar().setTitle(getResources().getString(R.string.about_title));

        TextView versionText = findViewById(R.id.version_text);
        versionText.setText(getResources().getString(R.string.version) + " " +  BuildConfig.VERSION_NAME);

        Button openSourceLicenses = findViewById(R.id.button_openscoureLicences);
        openSourceLicenses.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent openSourceLicences = new Intent(getApplicationContext(), OpenSourceLicensesActivity.class);
                startActivity(openSourceLicences);
            }
        });

    }
}
