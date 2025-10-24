package com.gmail.yahlieyal.lostnfound;

public class Button_Item extends Type_Item {

    private Boolean isExistent; // V sign is painted if text field not empty
    private Boolean isMust; // show asterisk if the field is "must"


    public Button_Item(String Name, int Img, Boolean isExistent, Boolean isMust) {
        super(Name, Img);
        this.isExistent = isExistent;
        this.isMust=isMust;
    }

    public Boolean getExistent() {
        return isExistent;
    }

    public void setExistent(Boolean existent) {
        isExistent = existent;
    }

    public Boolean getMust() {
        return isMust;
    }
}

