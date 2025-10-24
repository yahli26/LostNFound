package com.gmail.yahlieyal.lostnfound;


public class Lost extends BaseLostFound {

    protected int tip; // the tip that the man who found this lost will get

    public Lost() {
    }

    public int getTip() {
        return tip;
    }

    public void setTip(int tip) {
        if (tip != 0) {
            this.tip = tip;
        }
    }
}
// imgLost = (ImageView)findViewById(R.id.imgLost);


