package com.drl.museums_geschichten.activities;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;

import com.drl.museums_geschichten.R;
import com.drl.museums_geschichten.database.Story;
import com.drl.museums_geschichten.utils.Utils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;


public class PictureActivity extends BaseActivity {

    public File imageFile;
    private static final int REQUEST_IMAGE_CAPTURE_CODE = 3;
    private static final int REQUEST_IMAGE_CHOISE_CODE = 1;

    public static final String PICTURE_FILES_DIRECTORY = "Stories";
    Story story;
    ImageView pictureView;

    public String userChoosenTask;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_picture);


        //Restrict to portrait on smaller screens
        if (getResources().getBoolean(R.bool.portrait_only))
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

//        // try uncommenting this if expirience problems with permissions on older devices
//        if (ContextCompat.checkSelfPermission(this,
//                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, PERMISSIONS_STORY, REQUEST_STORY_PERMISSIONS);
//        }


        Intent i = getIntent();
        story = (Story)i.getSerializableExtra("story");
        pictureView = (ImageView) findViewById(R.id.picture_view);
        updateUi();



    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

    }


    //Display image editing options(take from camera, choose from gallery, delete existing, or cancel dialog)
    public void onImageButtonClicked(View view) {

        final CharSequence[] items = { "Foto Aufnehmen", "Ein Bild aus Galerie auswählen", "Bild Löschen",
                "Zurückziehen" };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Foto Aufnehmen");
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                boolean result= Utils.checkPermission(PictureActivity.this);

                if (items[item].equals("Foto Aufnehmen")) {
                    userChoosenTask="Take Photo";
                    if(result)
                        cameraIntent();

                } else if (items[item].equals("Ein Bild aus Galerie auswählen")) {
                    userChoosenTask="Choose from Library";
                    if(result)
                        galleryIntent();

                } else if (items[item].equals("Bild Löschen")) {
                    if(result)
                        story.imageFile = null;
                        dialog.dismiss();
                        goToOverview();


                } else if (items[item].equals("Zurückziehen")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();


    }

    private static File getOutputMediaFileName() {
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), PICTURE_FILES_DIRECTORY);

        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());

        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");

        return mediaFile;
    }

    public void onZuruckButtonClicked(View view) {
        Intent intent = new Intent(this, OrtAuswahlActivity.class);
        //intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("story", this.story);

        startActivity(intent);
    }


    public void onSkipButtonClicked(View view) {
        Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
        intent.putExtra("story", this.story);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        //hide Spinner
        hideOverlay();

        if (resultCode == RESULT_OK) {

            if (requestCode == REQUEST_IMAGE_CAPTURE_CODE) {
                if (imageFile.exists()) {
                    story.imageFile = imageFile.getAbsolutePath();
                }
                goToOverview();

            } else if (requestCode == REQUEST_IMAGE_CHOISE_CODE) {

                Uri selectedImage = data.getData();
                story.imageFile = getFilePath(this, selectedImage);

                goToOverview();


            } else
                super.onActivityResult(requestCode, resultCode, data);


        }
    }




    private void goToOverview(){
        Intent intent = new Intent(getApplicationContext(), OverviewActivity.class);
        intent.putExtra("story", this.story);

        startActivity(intent);
    }


    //Getting filepath for different systemversions taken from stackoverflow
    @SuppressLint("NewApi")
    public static String getFilePath(Context context, Uri uri) {
        String selection = null;
        String[] selectionArgs = null;
        // Uri is different in versions after KITKAT (Android 4.4), we need to
        if (Build.VERSION.SDK_INT >= 19 && DocumentsContract.isDocumentUri(context.getApplicationContext(), uri)) {
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                return Environment.getExternalStorageDirectory() + "/" + split[1];
            } else if (isDownloadsDocument(uri)) {
                final String id = DocumentsContract.getDocumentId(uri);
                uri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
            } else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("image".equals(type)) {
                    uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                selection = "_id=?";
                selectionArgs = new String[]{
                        split[1]
                };
            }
        }
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] projection = {
                    MediaStore.Images.Media.DATA
            };
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver()
                        .query(uri, projection, selection, selectionArgs, null);
                int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                if (cursor.moveToFirst()) {
                    return cursor.getString(column_index);
                }
            } catch (Exception e) {
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }
        return null;
    }

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }









    protected void updateUi() {

        //get rid of additional info, once picture is taken
        if (story.imageFile!=null){
            findViewById(R.id.image_icon).setVisibility(View.GONE);
            findViewById(R.id.image_text).setVisibility(View.GONE);
            findViewById(R.id.save_button).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.image_icon).setVisibility(View.VISIBLE);
            findViewById(R.id.image_text).setVisibility(View.VISIBLE);
            findViewById(R.id.save_button).setVisibility(View.GONE);
        }


        final int halfwidth = pictureView.getWidth()/2;
        final int halfheight = pictureView.getHeight()/2;
        // Asyncronous bitmap posting
        AsyncTask task = new AsyncTask<Object, Void, Bitmap>() {

            @Override
            protected Bitmap doInBackground(Object... params) {
                if (story.imageFile != null && Utils.fileExists(story.imageFile)) {
                    Bitmap bmp = BitmapFactory.decodeFile(story.imageFile);


                    Bitmap scaledBmp = null;
                    try {
                        scaledBmp = Utils.scaleBitmap(bmp, halfwidth, halfheight, story.imageFile);
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





    //taking picture from internal camera
    private void cameraIntent()
    {
        // Create the File where the photo should go
        imageFile = getOutputMediaFileName();

        // start the image capture activity
        if (imageFile != null) {
            Uri file = Uri.fromFile(imageFile);
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, file);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE_CODE);
        } else {
            showAlert("Error", "Could not create image file.");
        }
    }

    //choosing picturefrom device gallery
    private void galleryIntent()
    {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);//

            if (intent.resolveActivity(getPackageManager()) != null)
                startActivityForResult(Intent.createChooser(intent, "Select File"), REQUEST_IMAGE_CHOISE_CODE);

    }




}
