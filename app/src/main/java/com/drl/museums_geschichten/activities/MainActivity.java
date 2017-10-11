package com.drl.museums_geschichten.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;

import com.drl.museums_geschichten.R;

public class MainActivity extends BaseActivity {

    ImageView logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Restrict to portrait on smaller screens
        if (getResources().getBoolean(R.bool.portrait_only))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        logo = (ImageView) findViewById(R.id.logo);



        setupUI();


    }



    public void setupUI(){

        // Programmatically changes size of the logo to half of the scree size
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();

        logo.getLayoutParams().height = dm.heightPixels / 2;

        logo.getLayoutParams().width = dm.widthPixels / 2;
    }

    public void onRecorderButtonClicked(View view) {
        Intent intent = new Intent(this, NewRecorderActivity.class);
        startActivity(intent);
    }

    public void onListButtonClicked(View view) {
        Intent intent = new Intent(this, StoryListActivity.class);
        startActivity(intent);
    }

    public void onScannerButtonClicked(View view) {
        Intent intent = new Intent(this, ScannerActivity.class);
        startActivity(intent);
    }
}
