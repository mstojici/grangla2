package com.boardgame.miljac.grangla.menu;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.boardgame.miljac.grangla.R;

import java.util.ArrayList;

public class Player2PictureSpinnerAdapter extends ArrayAdapter<PlayerImageListItem> {

    LayoutInflater inflater;
    ArrayList<PlayerImageListItem> objects;
    ViewHolder holder = null;

    public Player2PictureSpinnerAdapter(Context context, int textViewResourceId, ArrayList<PlayerImageListItem> objects) {
        super(context, textViewResourceId, objects);
        inflater = ((Activity) context).getLayoutInflater();
        this.objects = objects;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        return getCustomView(position, convertView, parent);
    }

    public View getCustomView(int position, View row, ViewGroup parent) {

        PlayerImageListItem playerImageListItem = objects.get(position);

        if (null == row) {
            holder = new ViewHolder();
            row = inflater.inflate(R.layout.row, parent, false);
            holder.imgThumb = (ImageView) row.findViewById(R.id.imgThumb);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        holder.imgThumb.setBackgroundResource(playerImageListItem.getLogo());
        return row;
    }

    static class ViewHolder {
        ImageView imgThumb;
    }
}