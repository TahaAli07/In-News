package com.example.innews.Adapters;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.innews.R;
import com.squareup.picasso.Picasso;

//Takes input Cursor from db.query and the context and Returns an adapter for the Recycler View

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.GuestViewHolder> {

    // Holds on to the cursor to display the waitlist
    private Cursor cursor;
    private Context context;

    public CustomAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public GuestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Get the RecyclerView item layout
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.news_list_layout, parent, false);
        return new GuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GuestViewHolder holder, int position) {
        // Move the mCursor to the position of the item to be displayed
        if (!cursor.moveToPosition(position))
            return; // return if returned null

        String title = cursor.getString(cursor.getColumnIndex("TITLE"));
        String description = cursor.getString(cursor.getColumnIndex("DESCRIPTION"));
        String urlToImage = cursor.getString(cursor.getColumnIndex("URLTOIMAGE"));
        int id = cursor.getInt(cursor.getColumnIndex("ID"));

        //holder.nameTextView.setText(name + "    " +time.substring(10));
        holder.titleTextView.setText(title);
        Picasso.get().load(urlToImage).into(holder.pictureImageView);
        holder.itemView.setTag(id);
    }


    @Override
    public int getItemCount() {
        return cursor.getCount();
    }


    // Swaps the Cursor currently held in the adapter with a new one
    // and triggers a UI refresh

    public void swapCursor(Cursor newCursor) {
        // Always close the previous mCursor first
        if (cursor != null) cursor.close();
        cursor = newCursor;
        if (newCursor != null) {
            // Force the RecyclerView to refresh
            this.notifyDataSetChanged();
        }
    }


    // Inner class to hold the views needed to display a single item in the recycler-view

    class GuestViewHolder extends RecyclerView.ViewHolder {

        // Will display the guest name
        ImageView pictureImageView;
        // Will display the party size number
        TextView titleTextView;

        // Constructor for our ViewHolder. Within this constructor, we get a reference to our TextViews

        private GuestViewHolder(View itemView) {
            super(itemView);
            pictureImageView = (ImageView) itemView.findViewById(R.id.picture_image_view);
            titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
        }

    }
}