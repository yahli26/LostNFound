package com.gmail.yahlieyal.lostnfound;

import android.location.Location;
import android.location.LocationManager;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;

public class SearchUsers {

    private String orderBy;
    private String Username;

    public SearchUsers(String orderBy, String username) {
        this.orderBy = orderBy;
        this.Username = username;
    }

    public SearchUsers() {
        this.orderBy = null;
        this.Username = null;
    }

    public ArrayList<User> SearchNow(ArrayList<User> list) {
        if (Username!=null)  // remove all users that don't have the right parameters
            RemoveUsername(list);
        if (this.orderBy != null && this.orderBy.equals("Order")==false) { // order the remain users by parameter
            if (this.orderBy.equals("Date"))
                OrderDateRegistration(list);
            else if (this.orderBy.equals("Uploads"))
                OrderUploads(list);
        }
        return list;
    }

    public void RemoveUsername(ArrayList<User> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUsername().toLowerCase().contains(this.Username.toLowerCase()) == false && list.get(i).getFirstName().toLowerCase().contains(this.Username.toLowerCase()) == false)
            {
                list.remove(i);
                i--;
            }
        }
    }

    public void OrderUploads(ArrayList<User> list) {
        boolean isActive=true;
        while (isActive) {
            isActive = false;
            for (int i = 0; i < list.size()-1; i++) {
                if (list.get(i).getSumUploads() > list.get(i + 1).getSumUploads()) {
                    isActive = true;
                    User temp = list.get(i);
                    list.set(i, list.get(i + 1));
                    list.set(i + 1, temp);
                }
            }
        }
    }

    public void OrderDateRegistration(ArrayList<User> list) {
        boolean isActive = true;
        while (isActive) {
            isActive = false;
            for (int i = 0; i < list.size() - 1; i++) {
                if (StrToCalender(list.get(i).getDateRegistration()).after(StrToCalender(list.get(i+1).getDateRegistration()))) {
                    isActive = true;
                    User temp = list.get(i);
                    list.set(i, list.get(i + 1));
                    list.set(i + 1, temp);
                }
            }
        }
    }

    public Calendar StrToCalender(String date) {
        String[] DateArray = date.split("\\s*/\\s*");
        Calendar result = Calendar.getInstance();
        result.set(Integer.parseInt(DateArray[2]), Integer.parseInt(DateArray[1])-1,  Integer.parseInt(DateArray[0]));
        return result;
    }
}
