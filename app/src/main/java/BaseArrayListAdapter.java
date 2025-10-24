package com.gmail.yahlieyal.lostnfound;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Calendar;

public class BaseArrayListAdapter extends ArrayAdapter<BaseLostFound> {

    private String[] Objects = {"Creditcard", "Headphones", "Bag", "Document", "Watch", "Magnetic card", "Laptop", "Book", "Other object"};
    private String[] Pets = { "Cat","Rabbit","Hamsters", "Other pet"};
    private String[] Clothing = {"Sun glasses", "Earring", "Jewel", "Hat", "Sweatshirt", "Coat", "Umbrella", "Other clothing"};
    private boolean isManager=false;

    private final Context context;
    private final ArrayList<BaseLostFound> ArrList;

    DataBaseHelper db;

    TextView tvDescription;
    TextView tvType;
    TextView tvDate;
    TextView tvSeqTip; // shows security or tip field depends if the item is L/F
    ImageView imgIcon; // Image of item
    ImageView ivBlackLock; // security field icon
    RelativeLayout backgroundLayout;

    //  @param _context - the context
    // * @param _values - the array

    // Constructor for an ArrrayList

    public BaseArrayListAdapter(Context _context, ArrayList<BaseLostFound> ArrList)
    {
        super(_context, R.layout.base_item_list_adapter, ArrList);
        this.context = _context;
        this.ArrList = ArrList;
    }

    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.base_item_list_adapter, parent, false);

        tvDescription = (TextView) rowView.findViewById(R.id.tvDescription);
        tvType = (TextView) rowView.findViewById(R.id.tvShowType);
        tvDate = (TextView) rowView.findViewById(R.id.tvShowDate);
        tvSeqTip = (TextView) rowView.findViewById(R.id.tvSeqTip);
        imgIcon = (ImageView) rowView.findViewById(R.id.imgShowIcon);
        ivBlackLock = (ImageView) rowView.findViewById(R.id.ivBlackLock);
        backgroundLayout = (RelativeLayout) rowView.findViewById(R.id.backgroundLayout);

        setIsManger();
        handleArrayList(position);

        return rowView;
    }

    private void handleArrayList(int position)
    {
        String description=ArrList.get(position).getDescription();
        String type=ArrList.get(position).getType();
        tvType.setText(type);
        if (type.length()*1.3+description.length()>34.8)
            description = description.substring(0, Math.toIntExact(Math.round(34.8-type.length()*1.3))) +"...";
        tvDescription.setText(description);
        String Date;
        if (isManager)
            Date = "Date of upload: "+ArrList.get(position).getDateUploaded();
        else
            Date = ArrList.get(position).getDate();
        tvDate.setText(Date);
        if (ArrList.get(position) instanceof Lost) {
            ivBlackLock.setBackgroundColor(Color.TRANSPARENT);
            RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams)tvSeqTip.getLayoutParams();
            params.setMargins(40, 1, 0, 0);
            tvSeqTip.setLayoutParams(params);
            tvType.setTextColor(Color.rgb(249, 232, 232));
            tvDescription.setTextColor(Color.rgb(249, 232, 232));
            backgroundLayout.setBackgroundColor(Color.rgb(215, 83, 27));
            if (((Lost) ArrList.get(position)).getTip()==0)
                tvSeqTip.setText("Tip: No");
            else if (!isManager)
                tvSeqTip.setText("Tip: " + ((Lost) ArrList.get(position)).getTip());
        }
        else {
            tvType.setTextColor(Color.rgb(53, 50, 50));
            tvDescription.setTextColor(Color.rgb(53, 50, 50));
            backgroundLayout.setBackgroundColor(Color.rgb(67, 232, 216));
            if (((Found) ArrList.get(position)).getSecurityQustation()==null)
                tvSeqTip.setText("No");
            else if (!isManager){
                String securityQ = ((Found) ArrList.get(position)).getSecurityQustation();
                if (securityQ.length()>60)
                    securityQ = securityQ.substring(0,58)+"...";
                tvSeqTip.setText(securityQ);
            }
        }
        if (isManager) {
            ivBlackLock.setBackgroundColor(Color.TRANSPARENT);
            tvSeqTip.setText("Reports: "+ArrList.get(position).getReports());
        }
        if (ArrList.get(position).getImg()!=null) {
            Picasso.with(this.context)
                    .load(ArrList.get(position).getImg())
                    .placeholder(R.mipmap.ic_launcher)
                    .fit()
                    .centerCrop()
                    .into(imgIcon);
        }
        else
            setImage(ArrList.get(position).getType());
    }

    private void setImage(String type)
    {
            if (type.equals("Dog"))
                imgIcon.setImageResource(R.drawable.dogicon);
            else if (type.equals("Wallet"))
                imgIcon.setImageResource(R.drawable.walleticon);
            else if (type.equals("Phone"))
                imgIcon.setImageResource(R.drawable.phoneicon);
            else if (type.equals("Keys"))
                imgIcon.setImageResource(R.drawable.keysicon);
            else {
                for (int i=0; i<Objects.length; i++) {
                    if(type.equals(Objects[i])) {
                        imgIcon.setImageResource(R.drawable.objectsicon);
                        break;
                    }
                }
                for (int i=0; i<Pets.length; i++) {
                    if(type.equals(Pets[i])) {
                        imgIcon.setImageResource(R.drawable.petsicon);
                        break;
                    }
                }
                for (int i=0; i<Clothing.length; i++) {
                    if(type.equals(Clothing[i])) {
                        imgIcon.setImageResource(R.drawable.clothingicon);
                        break;
                    }
                }
            }
    }

    public void setIsManger() {
        db = new DataBaseHelper(context);
        Cursor res = db.getAllData();
        res.moveToFirst();
        String strIsManager = res.getString(2);
        isManager = (strIsManager.equals("true"));
    }
}
