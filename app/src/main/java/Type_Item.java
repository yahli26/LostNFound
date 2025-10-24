package com.gmail.yahlieyal.lostnfound;

public class Type_Item { // represent line in the menu

    private String Name;
    private int Img;

    public Type_Item(String Name, int Img) {
        this.Name = Name;
        this.Img = Img;
    }

    public String getName() {
        return Name;
    }

    public int getImg() {
        return Img;
    }

    public void setName(String name) {
        Name = name;
    }
}
