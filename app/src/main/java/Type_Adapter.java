package com.gmail.yahlieyal.lostnfound;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class Type_Adapter extends ArrayAdapter<Type_Item> { // used in the menu. Has name and image view.

    public Type_Adapter(Context context, ArrayList<Type_Item> typeList) {
        super(context, 0, typeList);
    }

    public Type_Adapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull Type_Item[] objects) {
        super(context, resource, textViewResourceId, objects);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    @Override
    public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        return initView(position, convertView, parent);
    }

    private View initView (int position, View convertView, ViewGroup parent) {
        if (convertView==null) {
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.spinner_adapter, parent, false
            );
        }
        ImageView imageViewType= convertView.findViewById(R.id.typeIcon);
        TextView textType= convertView.findViewById(R.id.typetext);
        Type_Item currentItem = getItem(position);
        if (currentItem!=null) {
            imageViewType.setImageResource(currentItem.getImg());
            textType.setText(currentItem.getName());
        }
        return convertView;
    }
}
