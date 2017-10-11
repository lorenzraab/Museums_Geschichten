package com.drl.museums_geschichten.activities;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.drl.museums_geschichten.R;
import com.drl.museums_geschichten.database.Story;
import com.drl.museums_geschichten.database.StoryDatabase;
import com.drl.museums_geschichten.utils.StoryUploader;

import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UploadActivity extends BaseActivity {

    private static final String LOG_TAG = "UploadActivity";

    private Call<ResponseBody> httpRequest = null;

    private Story story;

    private StoryDatabase database;

    private boolean uploading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload);

        //Restrict to portrait on smaller screens
        if (getResources().getBoolean(R.bool.portrait_only))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        // hide done layout
        findViewById(R.id.done_layout).setVisibility(View.GONE);

        if (getIntent().hasExtra("story"))
            story = (Story) getIntent().getSerializableExtra("story");
        else
            showAlert("Error","Keine Geschichte zum upload ausgewählt",true);

        database = new StoryDatabase(getApplicationContext());

        if (!uploading)
            uploadStory();
    }

    public void uploadStory() {

        uploading = true;



        httpRequest = StoryUploader.upload(this.story, new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call,
                                   Response<ResponseBody> response) {
                hideOverlay();


                JSONObject json = new JSONObject();

                try {
                    String string = response.body().string();
                    Log.e(LOG_TAG,"Upload response: " + string);
                    json = new JSONObject(string);
                    if (!json.getBoolean("success")) {
                        showAlert("Fehler","Upload fehlgeschlagen: "+string, true);
                        return;
                    }
                } catch (Exception e) {
                    showAlert("Fehler","Upload fehlgeschlagen: "+e.getMessage(), true);
                    e.printStackTrace();
                    return;
                }


                //delete story
                database.deleteStory(database.getWritableDatabase(), story);

                //Toast.makeText(getApplicationContext(), "Geschichte " + story.title + " wurde erfolgreich hochgeladen.", Toast.LENGTH_LONG).show();

                findViewById(R.id.uploading_layout).setVisibility(View.GONE);
                findViewById(R.id.done_layout).setVisibility(View.VISIBLE);
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showAlert("Fehler","Es konnte keine Verbindung zum Server hergestellt werden: " + t.getMessage(),true);
            }
        });
    }

    public void onUrlClicked(View view) {

        TextView textview = (TextView) view;
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(textview.getText().toString()));
        startActivity(browserIntent);
    }

    public void onBackButtonClicked(View view) {
            Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
