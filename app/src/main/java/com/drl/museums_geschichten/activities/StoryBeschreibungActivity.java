package com.drl.museums_geschichten.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;

import com.drl.museums_geschichten.R;
import com.drl.museums_geschichten.database.Story;


public class StoryBeschreibungActivity extends BaseActivity {



    Story story;

    TextView titleEdit;
    TextView textEdit;


    ViewGroup mainLayout;


    private boolean deletedStory = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story_beschreibung);
        setupUI(findViewById(R.id.storyEditing));

        //Restrict to portrait on smaller screens
        if (getResources().getBoolean(R.bool.portrait_only))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // request permissions
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_STORY, REQUEST_STORY_PERMISSIONS);
        }

        // request storydata
        if (getIntent().hasExtra("story"))
            story = (Story) getIntent().getSerializableExtra("story");
        else
            story = new Story();

        mainLayout = (ViewGroup) findViewById(R.id.main_layout);

        titleEdit = (TextView) findViewById(R.id.edit_text_title);
        textEdit = (TextView) findViewById(R.id.edit_text_text);


        titleEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateCheckmarks();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        updateUi();
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!deletedStory)
            saveStory();
    }

    protected void updateUi() {

        titleEdit.setText(story.title);
        textEdit.setText(story.text);


        updateCheckmarks();

    }

    protected void updateCheckmarks() {

        View saveButton = findViewById(R.id.nextw);

        //set checkbox if valid title and make next button active
        boolean isTextSet = titleEdit.getText().length() > 3;
        ((CheckBox)findViewById(R.id.check_text)).setChecked(isTextSet);
        if (isTextSet) {
            saveButton.setAlpha(1.0f);
            saveButton.setClickable(true);
        } else{
            saveButton.setAlpha(.5f);
            saveButton.setClickable(false);
        }


    }




    public void saveStory() {

        if (story.isEmpty())
            return;

        // get values
        if (titleEdit.getText().length() > 0)
            story.title = titleEdit.getText().toString();
        else
            story.title = null;

        if (textEdit.getText().length() > 0)
            story.text = textEdit.getText().toString();
        else
            story.text = null;




    }

    public void onWeiterButtonClicked(View view) {

        saveStory();
        Intent intent = new Intent(getApplicationContext(), OrtAuswahlActivity.class);
        intent.putExtra("story", story);
        startActivity(intent);
    }

    public void onBackButtonClicked(View view) {
        saveStory();

        Intent intent = new Intent(this, NewRecorderActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("story", story);
        startActivity(intent);
    }





    // hiding software keyboard
    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(StoryBeschreibungActivity.this);
                    return false;
                }
            });
        }

        //If a layout container, iterate over children and seed recursion.
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                View innerView = ((ViewGroup) view).getChildAt(i);
                setupUI(innerView);
            }
        }
    }


}



