
package com.drl.museums_geschichten.activities;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.drl.museums_geschichten.R;
import com.drl.museums_geschichten.database.Story;
import com.drl.museums_geschichten.database.StoryDatabase;
import com.drl.museums_geschichten.utils.Utils;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.google.android.gms.maps.model.LatLng;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

public class OverviewActivity extends BaseActivity {

    //Not used in final version
    //private static final String LOG_TAG = "StoryActivity";
    //public static final String PICTURE_FILES_DIRECTORY = "Stories";

    private static final int PLACE_PICKER_REQUEST_CODE = 2;


    int maxPicSize;

    Story story;

    EditText titleEdit;
    EditText textEdit;
    TextView locationText;
    EditText yearText;
    TextView recordingText;
    ImageView pictureView;

    ImageView audio;

    boolean playing = false;


    ViewGroup mainLayout;


    int devHei,devWidt;

    private boolean deletedStory = false;

    private StoryDatabase database;

    MediaPlayer mp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Restrict to portrait on smaller screens and display another layout for them
        if (getResources().getBoolean(R.bool.portrait_only)){
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            setContentView(R.layout.overview_portrait);
        } else {
            setContentView(R.layout.activity_overview);
        }

        //Retrieve storydata
        Intent i = getIntent();
        story = (Story)i.getSerializableExtra("story");

        // setup action bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Getting device measurments to adapt view for smaller screns
        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        devHei = dm.heightPixels / 4;
        devWidt = dm.widthPixels / 3;

        //Story title in support bar
        if (story._id > 0)
            getSupportActionBar().setTitle(getString(R.string.Geschichte) + story.title);
        else
            getSupportActionBar().setTitle(R.string.Neue_Geschichte);

        mainLayout = (ViewGroup) findViewById(R.id.main_layout);

        // Initialization of fields to edit
        titleEdit = (EditText) findViewById(R.id.edit_text_title);
        textEdit = (EditText) findViewById(R.id.edit_text_text);
        locationText = (TextView) findViewById(R.id.location_text);
        yearText = (EditText) findViewById(R.id.year_text);
        recordingText = (TextView) findViewById(R.id.recording_text);
        pictureView = (ImageView) findViewById(R.id.picture_view);
        audio = (ImageView) findViewById(R.id.audio);

        //restrict picture size
        maxPicSize = pictureView.getWidth();


        database = new StoryDatabase(getApplicationContext());

        //creaitig Media Player instance
        mp = MediaPlayer.create(getApplicationContext(), Uri.fromFile(new File(story.audioFile)));


        setupUI(findViewById(R.id.main_layout));

        updateUi();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }



    public void onPlay(View view) throws IOException {


        if (!playing){
            mp.start();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audio.setImageDrawable(getResources().getDrawable(R.drawable.stop, getApplicationContext().getTheme()));
            } else {
                audio.setImageDrawable(getResources().getDrawable(R.drawable.stop));
            }

            playing = true;
        }
        else {
            mp.stop();
            mp.prepare();
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                audio.setImageDrawable(getResources().getDrawable(R.drawable.play, getApplicationContext().getTheme()));
            } else {
                audio.setImageDrawable(getResources().getDrawable(R.drawable.play));
            }
            playing = false;

        }



    }


    @Override
    protected void onPause() {
        super.onPause();
        if (!deletedStory)
            saveData();
    }

    protected void updateUi() {

        titleEdit.setText(story.title);
        textEdit.setText(story.text);
        if (story.loc_name != null)
            locationText.setText(story.loc_name);

        if (story.date.length() > 0) {
                yearText.setText(story.date);

            }


        if (story.audioFile != null) {
            File file = new File(story.audioFile);
            recordingText.setText(file.getName());
        }



        //update image in background task
        AsyncTask task = new AsyncTask<Object, Void, Bitmap>() {


            @Override
            protected Bitmap doInBackground(Object... params) {
                if (story.imageFile != null && Utils.fileExists(story.imageFile)) {
                    Bitmap bmp = BitmapFactory.decodeFile(story.imageFile);
                    Bitmap scaledBmp = null;
                    try {

                        scaledBmp = Utils.scaleBitmap(bmp, devHei, devWidt, story.imageFile);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return scaledBmp;
                }
                return null;
            }

            @Override
            protected void onPostExecute(Bitmap result) {
                if (result != null)
                    pictureView.setImageBitmap(result);
            }
        };
        task.execute();




    }


        @Override
        public void onConfigurationChanged(Configuration configuration){
            setupUI(findViewById(R.id.main_layout));
        }

    public void saveStory() {

        if (story.isEmpty())
            return;

        // add to database
        Long id = database.upsertStory(database.getWritableDatabase(), story);
        database.close();
    }


    public void saveData(){

        if (titleEdit.getText().length() > 0)
            story.title = titleEdit.getText().toString();
        else
            story.title = null;

        if (textEdit.getText().length() > 0)
            story.text = textEdit.getText().toString();
        else
            story.text = null;

        if (yearText.getText().length() > 0)
            story.date = yearText.getText().toString();
        else
            story.date = "";



        if (story.isEmpty())
            return;



    }


    public void onZuruckButtonClicked(View view) {

        saveData();

        Intent intent = new Intent(this, PictureActivity.class);
        intent.putExtra("story", this.story);
        startActivity(intent);



    }


    public void onImageButtonClicked(View view) {
        saveData();

        Intent intent = new Intent(this, PictureActivity.class);
        intent.putExtra("story", this.story);

        startActivity(intent);
    }

    public void onLocationButtonClicked(View view) {
        saveData();

        showOverlay("Loading Map", mainLayout);

        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST_CODE);

            //show spinner
            findViewById(R.id.spinner_overlay).setVisibility(View.VISIBLE);
        } catch ( GooglePlayServicesRepairableException e) {
            showAlert("Error", e.getMessage());
            hideOverlay();
        } catch ( GooglePlayServicesNotAvailableException e) {
            showAlert("Error", e.getMessage());
            hideOverlay();
        }
    }


    public void onRecordButtonClicked(View view) {
        saveData();

        Intent intent = new Intent(this, NewRecorderActivity.class);
        intent.putExtra("story", this.story);

        startActivity(intent);


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //hide Spinner
        hideOverlay();

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

            //saveStory();
            updateUi();
        }
    }





        public void onDeleteButtonClicked(View view) {

        database.deleteStory(database.getWritableDatabase(), story);
        deletedStory = true;
        Toast toast = Toast.makeText(getApplicationContext(), "Geschichte " + story.title + " gelöscht .", Toast.LENGTH_LONG);
        toast.setGravity(Gravity.CENTER,0,0);
        toast.show();
        finish();
    }

    public void onUploadButtonClicked(View view) {
        saveData();
        saveStory();

        if (story.title.length() < 4){
            Toast toast = Toast.makeText(getApplicationContext(), "Titel ist zu kurz(mindestens 4 zeichen)", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
            titleEdit.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(titleEdit, InputMethodManager.SHOW_IMPLICIT);


            return;
        }

        if (story.date.length() > 0) {

            if (Integer.parseInt(story.date) > Calendar.getInstance().get(Calendar.YEAR)) {
                Toast toast = Toast.makeText(getApplicationContext(), "Falsches Jahresangabe", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER, 0, 0);
                toast.show();
                yearText.requestFocus();
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.showSoftInput(yearText, InputMethodManager.SHOW_IMPLICIT);

                return;
            }
        }



                if (haveNetworkConnection()){
            Intent intent = new Intent(getApplicationContext(), UploadActivity.class);
            intent.putExtra("story",this.story);
            startActivity(intent);
        }
        else {
            Toast toast = Toast.makeText(getApplicationContext(), "Ihr Beitrag wurde gespeichert. Sobald eine Internetverbindung besteht,\n" +
                    "wird er auf die Geschichtswerkstatt hochgeladen.", Toast.LENGTH_LONG);
            toast.setGravity(Gravity.CENTER,0,0);
            toast.show();
            startActivity(new Intent(getApplicationContext(), MainActivity.class));

        }



    }


    private boolean haveNetworkConnection() {
        boolean haveConnectedWifi = false;
        boolean haveConnectedMobile = false;

        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInfo = cm.getAllNetworkInfo();
        for (NetworkInfo ni : netInfo) {
            if (ni.getTypeName().equalsIgnoreCase("WIFI"))
                if (ni.isConnected())
                    haveConnectedWifi = true;
            if (ni.getTypeName().equalsIgnoreCase("MOBILE"))
                if (ni.isConnected())
                    haveConnectedMobile = true;
        }
        return haveConnectedWifi || haveConnectedMobile;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_story_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button

            case R.id.action_save:
                saveData();
                saveStory();
                //NavUtils.navigateUpFromSameTask(this);
                Toast toast = Toast.makeText(getApplicationContext(), "Geschichte " + story.title + " gespeichert.", Toast.LENGTH_LONG);
                toast.setGravity(Gravity.CENTER,0,0);
                toast.show();

                Intent intent = new Intent(getApplicationContext(), StoryListActivity.class);
                startActivity(intent);


                // NavUtils.navigateUpTo(this, intent);
                return true;
            case R.id.action_delete:
                onDeleteButtonClicked(getCurrentFocus());
        }
        return super.onOptionsItemSelected(item);
    }




    public static void hideSoftKeyboard(Activity activity) {
        InputMethodManager inputMethodManager =
                (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(
                activity.getCurrentFocus().getWindowToken(), 0);
    }

    public void setupUI(View view) {

        // force reload layout
        pictureView.requestLayout();

        // Set up touch listener for non-text box views to hide keyboard.
        if (!(view instanceof EditText)) {
            view.setOnTouchListener(new View.OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    hideSoftKeyboard(OverviewActivity.this);
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

