package com.drl.museums_geschichten.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.drl.museums_geschichten.R;

/**
 * Created by lutz on 02/11/16.
 */

public class BaseActivity extends AppCompatActivity {

    private View overlay;


    // Storage Permissions
    protected static final int REQUEST_STORY_PERMISSIONS = 1;
    protected static String[] PERMISSIONS_STORY = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    // Record Permissions
    protected static final int REQUEST_AUDIO_RECORD = 2;
    protected static String[] PERMISSIONS_AUDIO_RECORD = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO
    };
    // Camera Permissions
    protected static final int REQUEST_CAMERA = 3;
    protected static String[] PERMISSIONS_CAMERA = {
            Manifest.permission.CAMERA
    };

    public void showOverlay(String text, ViewGroup parent) {
        LayoutInflater inflater = this.getLayoutInflater();
        if (overlay == null)
            overlay = inflater.inflate(R.layout.overlay_spinner,parent,true);
        overlay.findViewById(R.id.spinner_overlay).setVisibility(View.VISIBLE);
        ((TextView) overlay.findViewById(R.id.spinner_text)).setText(text);
    }

    public void hideOverlay() {
        if (overlay != null)
            overlay.findViewById(R.id.spinner_overlay).setVisibility(View.GONE);
    }


    public void showAlert(final String title,final String message,  final boolean finishAfter) {

        AlertDialog alertDialog = new AlertDialog.Builder(this).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Ok",
            new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    if (finishAfter)
                        finish();
                }
            });
        alertDialog.show();
    }

    public void showAlert(final String title,final String message) {
        showAlert(title, message, false);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORY_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    showAlert("Error","This app requires to write audio and image file to external storage.", true);
                }
                return;
            }
            case REQUEST_AUDIO_RECORD: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    showAlert("Error","This app requires to record audio files.", true);
                }
                return;
            }
            case REQUEST_CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                } else {
                    showAlert("Error","This app requires to take camera pictures", true);
                }
                return;
            }
        }
    }

}
