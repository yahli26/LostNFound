package com.gmail.yahlieyal.lostnfound;

public class Found extends BaseLostFound {

    protected String securityQustation;
    protected String securityAnswer;

    public Found() {
    }

    public String getSecurityQustation() {
        return securityQustation;
    }

    public String getSecurityAnswer() {
        return securityAnswer;
    }

    public void setSecurityQustation(String securityQustation) {
            this.securityQustation = securityQustation;
    }

    public void setSecurityAnswer(String securityAnswer) { // comparing securityAnswer that given with the securityAnswer that should be
        if (securityAnswer!=null)
            if (securityAnswer.trim().equals("")==false)
        this.securityAnswer = securityAnswer.toLowerCase();
    }

    public Boolean checkSecAnswer(String answer) {
        return answer.toLowerCase().equals(securityAnswer);
    }

}
