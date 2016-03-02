package com.stellarin.pira.sharedata.Activity.Adapter;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.stellarin.pira.sharedata.R;

import java.util.Arrays;
import java.util.List;

/**
 * Created by pira on 15/11/15.
 */
public class GridAdapter extends ArrayAdapter<String> {

    Context context;
    int layoutResourceId;
    List<String > user = null;
    UserHolder holder = null;
    String[] typeaudio = {"mp3"};
    String[] typevideo = {"mp4"};
    String[] typeDoc = {"docx","doc","pdf","txt"};
    String[] typeImg = {"jpeg","jpg","png","gif"};
    List<String> extension;
    public GridAdapter(Context context, int layoutResourceId,
                             List<String> user,List<String> extension) {
        super(context, layoutResourceId, user);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.user = user;
        this.extension = extension;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;

        final int p = position;
        Boolean find = false;
        if (row == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            holder = new UserHolder();
            holder.imageView = (ImageView) row.findViewById(R.id.item_img);
            holder.textView =(TextView)row.findViewById(R.id.text);
            row.setTag(holder);
        } else {
            holder = (UserHolder) row.getTag();
        }
        if(!find) {
            final String friend = user.get(position);
            String ex = extension.get(position);
            holder.textView.setText(friend);
            if(Arrays.asList(typeaudio).contains(ex)){
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_album));
            }else if(Arrays.asList(typevideo).contains(ex)){
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_local_movies));
            }else if(Arrays.asList(typeDoc).contains(ex)){
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_description));
            }else if(Arrays.asList(typeImg).contains(ex)){
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_panorama));
            }else{
                holder.imageView.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_folder));
            }
        }
        return row;
    }

    static class UserHolder {
        //ImageButton addbtn;
        ImageView imageView;
        TextView textView;
    }

}
