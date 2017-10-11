package com.drl.museums_geschichten.database;

/**
 * Created by lutz on 01/11/16.
 */

import android.provider.BaseColumns;

class StoryContract {

    static final String DB_NAME = "com.drl.museums_geschichten.db";
    static final int DB_VERSION = 12;

    class StoryEntry implements BaseColumns {

        static final String TABLE = "stories";

        static final String COL_TITLE = "title";
        static final String COL_TEXT = "text";
        static final String COL_DATE = "date";
        static final String COL_LOCATION_LONG = "loc_long";
        static final String COL_LOCATION_LAT = "loc_lang";
        static final String COL_LOCATION_NAME = "loc_name";

        static final String COL_AUDIOFILE = "recording";
        static final String COL_IMAGE = "image";
    }

    static String createTableString() {

        return "CREATE TABLE " + StoryEntry.TABLE + " ( " +
                StoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                StoryEntry.COL_TITLE + " TEXT," +
                StoryEntry.COL_TEXT + " TEXT," +
                StoryEntry.COL_LOCATION_LAT + " REAL, " +
                StoryEntry.COL_LOCATION_LONG + " REAL, " +
                StoryEntry.COL_LOCATION_NAME + " TEXT, " +
                StoryEntry.COL_IMAGE + " TEXT ," +
                StoryEntry.COL_AUDIOFILE + " TEXT ," +
                StoryEntry.COL_DATE + " TEXT" +
                ");";
    }
}
