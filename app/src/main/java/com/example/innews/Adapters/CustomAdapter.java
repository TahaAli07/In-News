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

public class CustomAdapter extends RecyclerView.Adapter<CustomAdapter.GuestViewHolder> {
    private Cursor cursor;
    private Context context;

    public CustomAdapter(Context context, Cursor cursor) {
        this.context = context;
        this.cursor = cursor;
    }

    @Override
    public GuestViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.news_list_layout, parent, false);
        return new GuestViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GuestViewHolder holder, int position) {
        if (!cursor.moveToPosition(position))
            return; // return if returned null

        String title = cursor.getString(cursor.getColumnIndex("TITLE"));
        String description = cursor.getString(cursor.getColumnIndex("DESCRIPTION"));
        String urlToImage = cursor.getString(cursor.getColumnIndex("URLTOIMAGE"));
        int id = cursor.getInt(cursor.getColumnIndex("ID"));

        holder.titleTextView.setText(title);
        if (urlToImage.compareTo("") != 0) {
            Picasso.get().load(urlToImage).fit().into(holder.pictureImageView);
        }
        holder.itemView.setTag(id);
    }


    @Override
    public int getItemCount() {
        return cursor.getCount();
    }

    class GuestViewHolder extends RecyclerView.ViewHolder {
        ImageView pictureImageView;
        TextView titleTextView;

        private GuestViewHolder(View itemView) {
            super(itemView);
            pictureImageView = (ImageView) itemView.findViewById(R.id.picture_image_view);
            titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
        }
    }
}