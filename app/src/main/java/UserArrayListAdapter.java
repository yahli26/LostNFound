package com.gmail.yahlieyal.lostnfound;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;

public class UserArrayListAdapter extends ArrayAdapter<User> {


    private final Context context;
    private final ArrayList<User> ArrList;

    TextView tvUserName;
    TextView tvName;
    TextView tvPhoneNumber;
    TextView tvMail;
    TextView tvDateRegistration; // date user created
    TextView tvSumUploads; // counts the sum of uploads of the user
    RelativeLayout backgroundLayout;

    //  @param _context - the context
    // * @param _values - the array

    // Constructor for an ArrrayList

    public UserArrayListAdapter(Context _context, ArrayList<User> ArrList)
    {
        super(_context, R.layout.users_list_adapter, ArrList);
        this.context = _context;
        this.ArrList = ArrList;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.users_list_adapter, parent, false);

        tvUserName = rowView.findViewById(R.id.tvUserName);
        tvName = rowView.findViewById(R.id.tvName);
        tvPhoneNumber = rowView.findViewById(R.id.tvPhoneNumber);
        tvMail = rowView.findViewById(R.id.tvMail);
        tvDateRegistration = rowView.findViewById(R.id.tvDateRegistration);
        tvSumUploads = rowView.findViewById(R.id.tvSumUploads);
        backgroundLayout = rowView.findViewById(R.id.backgroundLayout);

        handleArrayList(position);
        return rowView;
    }

    private void handleArrayList(int position)
    {
        tvUserName.setText(ArrList.get(position).getUsername());
        tvName.setText(ArrList.get(position).getFirstName());
        tvDateRegistration.setText("Date created: "+ArrList.get(position).getDateRegistration());
        tvMail.setText(ArrList.get(position).getMail());
        tvPhoneNumber.setText(ArrList.get(position).getPhoneNumber()+",");
        tvSumUploads.setText("Number of uploads: "+ArrList.get(position).getSumUploads());
        if (ArrList.get(position).getIsManager()) // color for manger
            backgroundLayout.setBackgroundColor(Color.rgb(238, 163, 1));
        else // color for regular user
            backgroundLayout.setBackgroundColor(Color.rgb(245, 219, 164));
    }
}
