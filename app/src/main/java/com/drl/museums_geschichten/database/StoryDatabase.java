package com.drl.museums_geschichten.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.text.Normalizer;

/**
 * Created by lutz on 01/11/16.
 */
public class StoryDatabase extends SQLiteOpenHelper {

    public StoryDatabase(Context context) {
        super(context, StoryContract.DB_NAME, null, StoryContract.DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = StoryContract.createTableString();
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + StoryContract.StoryEntry.TABLE);
        onCreate(db);
    }

    public synchronized Long upsertStory(SQLiteDatabase db, Story story) {

        ContentValues insertValues = new ContentValues();
        insertValues.put(StoryContract.StoryEntry.COL_TITLE, story.title);
        insertValues.put(StoryContract.StoryEntry.COL_TEXT, story.text);
        insertValues.put(StoryContract.StoryEntry.COL_LOCATION_LAT, story.loc_latitude);
        insertValues.put(StoryContract.StoryEntry.COL_LOCATION_LONG, story.loc_longitude);
        insertValues.put(StoryContract.StoryEntry.COL_LOCATION_NAME, story.loc_name);
        insertValues.put(StoryContract.StoryEntry.COL_IMAGE, story.imageFile);
        insertValues.put(StoryContract.StoryEntry.COL_AUDIOFILE, story.audioFile);
        insertValues.put(StoryContract.StoryEntry.COL_DATE, story.date);

        if (story._id < 0)
            story._id = db.insert(StoryContract.StoryEntry.TABLE, null, insertValues);
        else
            db.update(StoryContract.StoryEntry.TABLE,insertValues, StoryContract.StoryEntry._ID + "=" + story._id, null);

        return story._id;
    }

    public synchronized Story getStory(SQLiteDatabase db, long id) {

        Cursor cursor = db.rawQuery("SELECT * FROM " + StoryContract.StoryEntry.TABLE +
                " WHERE " + StoryContract.StoryEntry._ID + "=" + Long.toString(id), null);
        cursor.moveToFirst();

        return new Story(cursor);
    }

    public synchronized Cursor getAllStories(SQLiteDatabase db) {

        return db.rawQuery("SELECT * FROM "+ StoryContract.StoryEntry.TABLE, null);

    }

    public synchronized boolean checkIfExists(SQLiteDatabase db, long id) {


        Cursor cursor = db.rawQuery("SELECT TOP 1 FROM " + StoryContract.StoryEntry.TABLE  +
                " WHERE "+ StoryContract.StoryEntry._ID + "=" + Long.toString(id), null);

        boolean ret = ((cursor != null) && (cursor.getCount() > 0)) ? true : false;

        if (cursor != null) {
            cursor.close();
        }

        return ret;
    }



    public synchronized void deleteStory(SQLiteDatabase db, Story story) {

        //delete audio file
        if (story.audioFile != null) {
            File file = new File(story.audioFile);
            if (file.exists())
                file.delete();
        }

        db.delete(StoryContract.StoryEntry.TABLE,
            StoryContract.StoryEntry._ID + " = ?", new String[]{Long.toString(story._id)});
    }

    static public String escape(String str) {
        if (str == null)
            return null;
        str = Normalizer.normalize(str, Normalizer.Form.NFD);
        return DatabaseUtils.sqlEscapeString(str);
    }

}