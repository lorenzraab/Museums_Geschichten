package com.drl.museums_geschichten.activities;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.drl.museums_geschichten.R;
import com.drl.museums_geschichten.database.Story;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;

public class OrtAuswahlActivity extends BaseActivity {


    private static final int PLACE_PICKER_REQUEST_CODE = 2;
    ViewGroup mainLayout;
    Story story;
    TextView locationText;

    EditText yearEd;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ort_auswahl);
        mainLayout = (ViewGroup) findViewById(R.id.map_layout);

        //Restrict to portrait on smaller screens
        if (getResources().getBoolean(R.bool.portrait_only))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //Retrieve storydata
        Intent i = getIntent();
        story = (Story)i.getSerializableExtra("story");

        setupUI(findViewById(R.id.map_layout));


        yearEd = (EditText)findViewById(R.id.year_text);

        if (story.date.length() > 0)
            yearEd.setText(story.date);

        //Listener for storyYear field
        yearEd.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                    story.date = s.toString();
                    updateUi();
            }
            @Override
            public void afterTextChanged(Editable s) {
            }
        });



        locationText = (TextView) findViewById(R.id.location_text);

        updateUi();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //hide Spinner
        hideOverlay();

        //Handling results of Google API PlacePicker
        if (requestCode == PLACE_PICKER_REQUEST_CODE
                && resultCode == Activity.RESULT_OK) {

            final Place place = PlacePicker.getPlace(this, data);
            final LatLng location = place.getLatLng();
            final CharSequence address = place.getAddress();

            story.loc_latitude = location.latitude;
            story.loc_longitude = location.longitude;
            story.loc_name = address.toString();

            if (story.loc_name.isEmpty())
                story.loc_name = "Unbekannte Adresse";

            updateUi();
        }
    }


            @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    private void updateUi() {

        View saveButton = findViewById(R.id.save_button);

        // button not active by default
        saveButton.setAlpha(.5f);
        saveButton.setClickable(false);
        ((CheckBox)findViewById(R.id.check_year)).setChecked(false);


        if (story.loc_name != null){
            locationText.setText(story.loc_name);
            ((CheckBox)findViewById(R.id.check_place)).setChecked(true);

            //active туче игеещт if location is not null
            saveButton.setAlpha(1.f);
            saveButton.setClickable(true);

        }

        // chaeck year and activate next button if it is valid, otherwise display error message(toast)
        if (story.date.length() > 0) {

            if (Integer.parseInt(story.date) > Calendar.getInstance().get(Calendar.YEAR)) {
                    Toast toast = Toast.makeText(getApplicationContext(), "Falsches Jahresangabe", Toast.LENGTH_LONG);
                    toast.setGravity(Gravity.CENTER, 0, 0);
                    toast.show();
            } else {
                ((CheckBox) findViewById(R.id.check_year)).setChecked(true);

                //active if date is set to anything
                saveButton.setAlpha(1.f);
                saveButton.setClickable(true);
            }
        }

    }


    public void onZuruckButtonClicked(View view) {
        Intent intent = new Intent(this, StoryBeschreibungActivity.class);
        intent.putExtra("story", this.story);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onSaveButtonClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), PictureActivity.class);
        intent.putExtra("story", this.story);
        startActivity(intent);
    }




    public void onLocationButtonClicked(View view) {

        showOverlay("Loading Map", mainLayout);

        // Strat google maps Place Picker
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST_CODE);

            //show spinner
            findViewById(R.id.spinner_overlay).setVisibility(View.VISIBLE);
        } catch (GooglePlayServicesRepairableException e) {
            showAlert("Error", e.getMessage());
            hideOverlay();
        } catch (GooglePlayServicesNotAvailableException e) {
            showAlert("Error", e.getMessage());
            hideOverlay();
        }

    }


    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(OrtAuswahlActivity.this);
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
