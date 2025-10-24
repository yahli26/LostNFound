package com.gmail.yahlieyal.lostnfound;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

public class ArrayButtonListAdapter extends ArrayAdapter<Button_Item>  {

        private final Context context;
        private ArrayList<Button_Item> btnList;

        TextView tvButtonType; //name of btn
        ImageView imgIcon; //icon of btn
        ImageView isExistent;// V sign is painted if text field not empty
        ImageView isMust; // show asterisk if the field is "must"
    ;

        //  @param _context - the context
        // * @param _values - the array

        // Constructor for an ArrrayList

    public ArrayButtonListAdapter(Context _context, ArrayList<Button_Item> btnList)
    {
        super(_context, R.layout.button_adapter, btnList);
        this.context = _context;
        this.btnList = btnList;
    }

        public View getView(int position, View convertView, ViewGroup parent)
        {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View rowView = inflater.inflate(R.layout.button_adapter, parent, false);

            tvButtonType = (TextView) rowView.findViewById(R.id.tvButtonType);
            imgIcon = (ImageView) rowView.findViewById(R.id.imgIcon);
            isExistent = (ImageView) rowView.findViewById(R.id.isExistent);
            isMust = (ImageView) rowView.findViewById(R.id.ivIsMust);
            handleArray(position);

            return rowView;
        }

        private void handleArray(int position)
        {
                tvButtonType.setText(btnList.get(position).getName());
                imgIcon.setImageResource(btnList.get(position).getImg());
                if (btnList.get(position).getExistent()) {
                    isExistent.setImageResource(R.drawable.checkboxorange);
                }
                else {
                    isExistent.setImageResource(R.drawable.checkboxgray);
                }
                if (btnList.get(position).getMust()) {
                    isMust.setImageResource(R.drawable.asteriskicon);
                }
        }
    }

