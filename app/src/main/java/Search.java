package com.gmail.yahlieyal.lostnfound;

import android.location.Location;
import android.location.LocationManager;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Calendar;

public class Search {

    private String description;
    private Boolean isLost;
    private boolean isMyList;
    private String type;
    private String orderBy;
    private String myUsername; // username of the searcher
    private Location myLocation;

    public Search(String description, Boolean isLost, boolean isMyList, String type, String orderBy, String myUsername) {
        this.description = description;
        this.isLost = isLost;
        this.isMyList = isMyList;
        this.type = type;
        this.orderBy = orderBy;
        this.myUsername = myUsername;
    }

    public Search() {
        this.description = null;
        this.type = null;
        this.isLost = null;
        this.orderBy = null;
    }

    public ArrayList<BaseLostFound> SearchNow(ArrayList<BaseLostFound> list) { // remove all items that don't have the right parameters
        if (myUsername!=null)
            RemoveUsers(list, this.isMyList);
        if (isLost!=null)
            Removelostfound(list);
        if (type!=null)
        if (type.equals("Type")==false)
            Removetype(list);
        if (this.description.equals("")==false)
            RemoveDescription(list);
        if (orderBy!=null)
        if (this.orderBy != null && this.orderBy.equals("Order")==false) { // order the remain items by parameter
            if (this.orderBy.equals("Tip"))
                OrderTip(list);
            else if (this.orderBy.equals("Date"))
                OrderDate(list);
            else if (this.orderBy.equals("Upload"))
                OrderUpload(list);
            else if (this.orderBy.equals("Location"))
                OrderLocation(list);
            else if (this.orderBy.equals("Report"))
                OrderReport(list);
        }
        return list;
    }

    public void RemoveUsers(ArrayList<BaseLostFound> list, boolean isMyList) {
        if (isMyList) {
            for (int i=0; i<list.size(); i++) {
                if (list.get(i).getCreatorUsername().equals(this.myUsername)==false) {
                    list.remove(i);
                    i--;
                }
            }
        }
        else {
            for (int i=0; i<list.size(); i++) {
                if (list.get(i).getCreatorUsername().equals(this.myUsername)) {
                    list.remove(i);
                    i--;
                }
            }
        }

    }

    public void Removelostfound(ArrayList<BaseLostFound> list) {
        for (int i=0; i<list.size(); i++) {
            if (this.isLost) {
                if(list.get(i) instanceof Found) {
                    list.remove(i);
                    i--;
                }
            }
            else if (this.isLost==false) {
                if(list.get(i) instanceof Lost) {
                    list.remove(i);
                    i--;
                }
            }
        }
    }

    public void Removetype(ArrayList<BaseLostFound> list) {
        for (int i=0; i<list.size(); i++) {
            if (list.get(i).getType().equals(this.type) == false) {
                list.remove(i);
                i--;
            }
        }
    }

    public void RemoveDescription(ArrayList<BaseLostFound> list) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getDescription().toLowerCase().contains(this.description.toLowerCase()) == false)
            {
                list.remove(i);
                i--;
            }
        }
    }

    public void OrderTip(ArrayList<BaseLostFound> list) {
        Boolean isActive = true;
        while (isActive) {
            isActive = false;
            for (int i = 0; i < list.size(); i++) {
                if (((Lost) list.get(i)).getTip()==0) {
                    list.remove(i);
                    i--;
                }
                else if (i != list.size()-1) {
                    if (((Lost) list.get(i + 1)).getTip() == 0) {
                        list.remove(i + 1);
                        i--;
                    } else if (((Lost) list.get(i)).getTip() < ((Lost) list.get(i + 1)).getTip()) {
                        isActive = true;
                        BaseLostFound temp = list.get(i);
                        list.set(i, list.get(i + 1));
                        list.set(i + 1, temp);
                    }
                }
                else
                    break;
            }
        }
    }

    public void OrderReport(ArrayList<BaseLostFound> list) {
        boolean isActive=true;
        while (isActive) {
            isActive = false;
            for (int i = 0; i < list.size()-1; i++) {
                if (list.get(i).getReports() < list.get(i + 1).getReports()) {
                    isActive = true;
                    BaseLostFound temp = list.get(i);
                    list.set(i, list.get(i + 1));
                    list.set(i + 1, temp);
                }
            }
        }
    }

    public void OrderDate(ArrayList<BaseLostFound> list) {
        boolean isActive = true;
        while (isActive) {
            isActive = false;
            for (int i = 0; i < list.size() - 1; i++) {
                if (StrToCalender(list.get(i).getDate()).before(StrToCalender(list.get(i+1).getDate()))) {
                    isActive = true;
                    BaseLostFound temp = list.get(i);
                    list.set(i, list.get(i + 1));
                    list.set(i + 1, temp);
                }
            }
        }
    }

    public void OrderUpload (ArrayList<BaseLostFound> list) {
        boolean isActive = true;
        while (isActive) {
            isActive = false;
            for (int i = 0; i < list.size() - 1; i++) {
                if (StrToCalender(list.get(i).getDateUploaded()).before(StrToCalender(list.get(i+1).getDateUploaded()))) {
                    isActive = true;
                    BaseLostFound temp = list.get(i);
                    list.set(i, list.get(i + 1));
                    list.set(i + 1, temp);
                }
            }
        }
    }

    public Location geoToLocation (GeoPoint geoPoint) {
        Location temp = new Location(LocationManager.GPS_PROVIDER);;
        temp.setLatitude(geoPoint.getLatitude() / 1E6);
        temp.setLongitude(geoPoint.getLongitude() / 1E6);
       return temp;
    }

    public void OrderLocation(ArrayList<BaseLostFound> list) {
        if (this.myLocation != null) {
            boolean isActive = true;
            while (isActive) {
                isActive = false;
                for (int i = 0; i < list.size() - 1; i++) {
                    double d1 = geoToLocation(list.get(i).getLocation()).distanceTo(this.myLocation );
                    double d2 = geoToLocation(list.get(i + 1).getLocation()).distanceTo(this.myLocation );
                    if (d1 > d2) {
                        isActive = true;
                        BaseLostFound temp = list.get(i);
                        list.set(i, list.get(i + 1));
                        list.set(i + 1, temp);
                    }
                }
            }
        }
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setLost(Boolean lost) {
        isLost = lost;
    }

    public void setType(String type) {
        if (type.equals("Type")==false)
            this.type = type;
    }

    public void setMyLocation(Location myLocation) {
        this.myLocation = myLocation;
    }

    public void setOrderBy(String orderBy) {
        if (orderBy.equals("Order by")==false)
            this.orderBy = orderBy;
    }

    public void setMyUsername(String myUsername) {
        this.myUsername = myUsername;
    }

    public Calendar StrToCalender(String date) {
        String[] DateArray = date.split("\\s*/\\s*");
        Calendar result = Calendar.getInstance();
        result.set(Integer.parseInt(DateArray[2]), Integer.parseInt(DateArray[1])-1,  Integer.parseInt(DateArray[0]));
        return result;
    }
}
