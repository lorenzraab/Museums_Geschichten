/**
 * Created by lutz on 10/04/15.
 */
package com.drl.museums_geschichten.recorder;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.IOException;

public class SoundRecorder3gpp extends SoundRecorder {

    private MediaRecorder mRecorder;
    private File audioFile = null;

    private boolean isRecording = false;

    public SoundRecorder3gpp(Context context) {
        super(context);

        mRecorder = new MediaRecorder();
    }

    @Override
    public void startRecording() throws IOException {

        File sampleDir = context.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
        audioFile = File.createTempFile("sound", ".3gp", sampleDir);

        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        mRecorder.setAudioEncodingBitRate(16);
        mRecorder.setAudioSamplingRate(44100);
        mRecorder.setOutputFile(audioFile.getAbsolutePath());
        mRecorder.prepare();
        mRecorder.start();

        isRecording = true;
    }

    @Override
    public void stopRecording() {
        if (mRecorder == null || !isRecording)
            return;

        mRecorder.stop();

        isRecording = false;
    }

    @Override
    public void reset() {
        audioFile = null;

        stopRecording();
        mRecorder.reset();
    }

    @Override
    public void release() {
        if (mRecorder != null)
            mRecorder.release();
    }

    @Override
    public File save() {

        if (audioFile == null)
            return null;

        ContentValues values = new ContentValues(4);
        long current = System.currentTimeMillis();
        values.put(MediaStore.Audio.Media.TITLE, "audio" + audioFile.getName());
        values.put(MediaStore.Audio.Media.DATE_ADDED, (int) (current / 1000));
        values.put(MediaStore.Audio.Media.MIME_TYPE, "audio/3gpp");
        values.put(MediaStore.Audio.Media.DATA, audioFile.getAbsolutePath());
        ContentResolver contentResolver = context.getContentResolver();

        Uri base = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        Uri newUri = contentResolver.insert(base, values);

        //activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, newUri));
        //return newUri;
        return null;
    }

}

