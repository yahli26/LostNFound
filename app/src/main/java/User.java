package com.gmail.yahlieyal.lostnfound;


public class User {

    private String Username;
    private String firstName;
    private String password;
    private String phoneNumber;
    private String mail;
    private String dateRegistration; // date user created
    private int sumUploads; // counts the sum of uploads of the user
    private boolean isManager;

    public User() { }

    public User(String username, String password, String firstName, String phoneNumber, String mail, String dateRegistration, boolean isManager) {
        this.Username=username;
        this.password = password;
        this.firstName = firstName;
        this.phoneNumber = phoneNumber;
        this.mail = mail;
        this.dateRegistration = dateRegistration;
        this.sumUploads = 0;
        this.isManager = isManager;
    }

    public String getUsername() {
        return Username;
    }

    public void setUsername(String username) {
        Username = username;
    }

    public int getSumUploads() {
        return sumUploads;
    }

    public String getDateRegistration() {
        return dateRegistration;
    }

    public String getFirstName() {
        return firstName;
    }


    public String getMail() {
        return mail;
    }

    public boolean getIsManager() {
        return isManager;
    }


    public String getPassword() {
        return password;
    }


    public String getPhoneNumber() {
        return phoneNumber;
    }

}
