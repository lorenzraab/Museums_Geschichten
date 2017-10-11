package com.drl.museums_geschichten.adapters;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.drl.museums_geschichten.R;
import com.drl.museums_geschichten.database.Story;

/**
 * Created by lutz on 02/11/16.
 */
public class StoryAdapter extends CursorAdapter {

    public StoryAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    // The newView method is used to inflate a new view and return it,
    // you don't bind any data to the view at this point.
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.item_story, parent, false);
    }

    // The bindView method is used to bind all data to a given view
    // such as setting the text on a TextView.
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        // setup story model
        Story story = new Story(cursor);


        TextView titleView = (TextView) view.findViewById(R.id.story_title);

//        ImageButton imageButton = (ImageButton) view.findViewById(R.id.imageButton);

//        if (story.validate() == null) {
//            imageButton.setVisibility(View.INVISIBLE);
//        }


        // Populate fields with properties
        if (story.title != null)
            titleView.setText(story.title);


        //set model to view
        view.setTag(story);

    }
}