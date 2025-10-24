package com.gmail.yahlieyal.lostnfound;

import com.google.firebase.firestore.Exclude;
import com.google.firebase.firestore.GeoPoint;

public abstract class BaseLostFound  {

    protected String LF; // one letter L or F that helps to create array list of lost & found from queryDocumentSnapshots
    protected String type; // the type of item
    protected String description; // short description (more specific than type)
    protected GeoPoint location; // location in Lat and Lng
    protected int radius; // in meters
    protected String creatorUsername; // the username that uploaded the item
    protected String date; // day/month/year that saved as string in cloud
    protected String dateUploaded; // date that item uploaded
    protected String img; // url string of the img that in the cloud
    protected boolean relevant; // represent if the item has been given to the owner
    protected int reports; // counts the sum of reports that users did
    protected String documentId; // the ID of the item in the BaseLostFound collection

    // every lost/found will be connected to mail

    public BaseLostFound() { }


    //action that copies BaseLostFound to the fields
    // it saves double writing in the Set in create item activity
    public void setByParent(BaseLostFound baseLostFound) {
        this.relevant=true;
        this.reports=baseLostFound.getReports();
        this.creatorUsername=baseLostFound.getCreatorUsername();
        this.description=baseLostFound.getDescription();
        this.location=baseLostFound.getLocation();
        this.radius=baseLostFound.getRadius();
        this.img=baseLostFound.getImg();
        this.type=baseLostFound.getType();
        this.date=baseLostFound.getDate();
        this.dateUploaded=baseLostFound.getDateUploaded();
    }

    public String getDateUploaded() {
        return dateUploaded;
    }

    public void setDateUploaded(String dateUploaded) {
        this.dateUploaded = dateUploaded;
    }

    public String getType() {
        return type;
    }

    public String getLF() {
        return LF;
    }

    public void setLF(String LF) {
        this.LF = LF;
    }

    public int getRadius() {
        return radius;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }


    public String getDescription() {
        return description;
    }

    public GeoPoint getLocation() {
        return location;
    }


    public void setisRelevant(boolean isRelevant) {
        this.relevant = isRelevant;
    }

    public boolean isRelevant() {
        return relevant;
    }

    public int getReports() {
        return reports;
    }

    @Exclude
    public String getDocumentId() {
        return documentId;
    }

    public void setDocumentId(String documentId) {
        this.documentId = documentId;
    }

    public void addReport() {
        this.reports++;
    }

    public void clearReport() {
        this.reports=0;
    }

    public void setLocation(GeoPoint location) {
        this.location = location;
    }

    public void setDescription(String description) {
            this.description = description;
    }

    public void setType(String type) {
        if (type.equals("Type")==false)
            this.type = type;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public void setDate(String date) {
        this.date =date;
    }

    public String getDate() {
        return date;
    }

    public String getImg() {
        return img;
    }

    public void setImg(String img) {
        this.img = img;
    }

}
