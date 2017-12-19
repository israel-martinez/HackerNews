package com.israel_martinez.hackernewsalgolia.Adapters;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.israel_martinez.hackernewsalgolia.EDA.Hit;
import com.israel_martinez.hackernewsalgolia.R;

import java.util.List;

public class HitsAdapter extends ArrayAdapter<Hit> {

    public HitsAdapter(Context context, int resource, List<Hit> objects) {
        super(context, resource, objects);
    }

    public View getView(int position, @Nullable View row, @NonNull ViewGroup parent) {

        LayoutInflater mInflater = (LayoutInflater) getContext()
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

        if (row == null){
            row = mInflater.inflate(R.layout.hits_row, null);
        }

        TextView hitTitle = (TextView) row.findViewById(R.id.hit_title);
        TextView hitText = (TextView) row.findViewById(R.id.hit_text);


        Hit hit = getItem(position);
        hitTitle.setText(getTitle(hit));
        hitText.setText(hit.getAuthor().concat(" - " + hit.getCreatedAt()));

        return row;
    }

    public String getTitle(Hit hit){
        try{
            if(hit.getStoryTitle() != null) return hit.getStoryTitle().toString();
            else
                if(hit.getTitle() != null) return hit.getTitle();

        }catch(Error error){
            error.printStackTrace();
        }

        return "";
    }
}
