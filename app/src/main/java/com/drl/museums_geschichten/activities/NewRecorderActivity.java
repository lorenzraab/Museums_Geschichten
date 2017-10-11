package com.drl.museums_geschichten.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.drl.museums_geschichten.R;
import com.drl.museums_geschichten.database.Story;
import com.drl.museums_geschichten.database.StoryDatabase;
import com.drl.museums_geschichten.recorder.SoundRecorder;
import com.drl.museums_geschichten.recorder.SoundRecorderWav;
import com.drl.museums_geschichten.views.WaveformView;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class NewRecorderActivity extends BaseActivity {

    Story story;

    public static final int RECORDING_MAX_TIME = 1000 * 60 * 5;

    RecorderState state = RecorderState.INIT;

    CountDownTimer timer = null;
    long recordTime = 0;

    SoundRecorder recorder;
    WaveformView waveformView;
    TextView timerView;
    ProgressBar progressBar;

    PowerManager.WakeLock wakeLock;


    private StoryDatabase database;

    private enum RecorderState {
        INIT, RECORDING, STOPPED
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_recorder);

        //Restrict to portrait on smaller screens
        if (getResources().getBoolean(R.bool.portrait_only))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        // Gets the story data if it was passed from previous activity, otherwise creates new one
        if (getIntent().hasExtra("story"))
            story = (Story) getIntent().getSerializableExtra("story");
        else
            story = new Story();

        //Permission requst to record audio
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, PERMISSIONS_AUDIO_RECORD, REQUEST_AUDIO_RECORD);
        }

        // initialization of adjustable screen element
        timerView = (TextView) findViewById(R.id.elapsed_time);
        waveformView = (WaveformView) findViewById(R.id.waveform_view);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        // init soundrecorder
        recorder = new SoundRecorderWav(getApplicationContext());
        recorder.setAudioBufferCallback(new SoundRecorder.AudioBufferCallback() {
            @Override
            public void onNewData(int max) {
                waveformView.updateAudioData(max);
            }
        });

        // init wakelock
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Recorder Wake Lock");

        // keep screen always on
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        database = new StoryDatabase(getApplicationContext());

        updateUi();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (wakeLock.isHeld())
            wakeLock.release();
        recorder.release();
    }

    public void startRecording() {

        //start Counting Time, set maximum time
        timer = new CountDownTimer(RECORDING_MAX_TIME - recordTime, 25) {

            long startRecordTime = recordTime;
            long startTime = System.currentTimeMillis();

            public void onTick(long millisUntilFinished) {
                long elapsedTime = System.currentTimeMillis() - startTime;
                recordTime = startRecordTime + elapsedTime;
                timerView.setText(convertTimeString(recordTime));

                float progress = recordTime/(float)RECORDING_MAX_TIME;
                progressBar.setProgress((int)(progress*100));
            }

            public void onFinish() {
                stopRecording();
            }
        }.start();

        try {
            recorder.prepare();
            recorder.startRecording();
            state = RecorderState.RECORDING;
            wakeLock.acquire();
        } catch (Exception e) {
            showAlert("Error", e.getMessage());
            stopRecording();
        }


        updateUi();
    }


    public void stopRecording() {

        //stop timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        try {
            recorder.stopRecording();
            state = RecorderState.STOPPED;
            wakeLock.release();
        } catch (IOException e) {
            showAlert("Error", e.getMessage());
        }

        updateUi();
    }

    public void resetRecording() {

        //stop timer
        if (timer != null) {
            timer.cancel();
            timer = null;
        }

        recordTime = 0;

        try {
            recorder.reset();
            state = RecorderState.INIT;
        } catch (IOException e) {
            showAlert("Error", e.getMessage());
        }

        updateUi();
    }

    public File saveRecording() {

        File file = null;

        try {
            file = recorder.save();
            recorder.reset();
        } catch (Exception e) {
            showAlert("Error",e.getMessage());
        }

        return file;
    }

    private void updateUi() {

        // initialization of elements to prevent cancelling actions during recording
        TextView textView = (TextView) findViewById(R.id.text_view);
        View saveButton = findViewById(R.id.save_button);
        View cancelButton = findViewById(R.id.zuruck_button);

        ToggleButton recordButton = (ToggleButton) findViewById(R.id.record_button);

        if (state == RecorderState.RECORDING) {
            textView.setText(R.string.rec_stop);
            saveButton.setAlpha(.5f);
            saveButton.setClickable(false);

            cancelButton.setVisibility(View.INVISIBLE);

            recordButton.setChecked(true);
        } else if (state == RecorderState.STOPPED) {
            textView.setText(R.string.rec_continue);

            saveButton.setAlpha(1.0f);
            saveButton.setClickable(true);

            cancelButton.setVisibility(View.VISIBLE);

            recordButton.setChecked(false);
            waveformView.clearAudioData();
        } else {
            textView.setText(R.string.rec_start);
            progressBar.setProgress(0);
            timerView.setText(convertTimeString(0));

            saveButton.setAlpha(.5f);
            saveButton.setClickable(false);

            recordButton.setChecked(false);
            waveformView.clearAudioData();
        }
    }

    public void onRecordButtonClicked(View view) {
        if (state == RecorderState.INIT)
            startRecording();
        else if (state == RecorderState.STOPPED) {
            startRecording();
        } else if (state == RecorderState.RECORDING)
            stopRecording();
    }



    public void saveStory() {

        if (story.isEmpty())
            return;

        // add to database

        List<String> errors = story.validate();
        if (errors == null) {
            Long id = database.upsertStory(database.getWritableDatabase(), story);
            database.close();
        }
    }


    public void onZuruckButtonClicked(View view) {

        // save only if story validates - has recording and title
        saveStory();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //intent.putExtra("story", this.story);
        startActivity(intent);
    }

    public void onSaveButtonClicked(View view) {
        if (state == RecorderState.STOPPED) {
            showOverlay("Aufnahme wird gespeichert",(ViewGroup)findViewById(R.id.main_layout));
            File file = saveRecording();
            if (file != null) {
                story.audioFile = file.getAbsolutePath();
            }

        }
        Intent intent = new Intent(getApplicationContext(), StoryBeschreibungActivity.class);
        intent.putExtra("story", this.story);
        startActivity(intent);
    }


    private String convertTimeString(long milliseconds) {

        long hsecs = (milliseconds % 1000 ) / 10;
        long seconds = (milliseconds / 1000) % 60;
        long minutes = (milliseconds / 1000) / 60;

        return String.format("%02d:%02d.%02d", minutes, seconds, hsecs);
    }



}
