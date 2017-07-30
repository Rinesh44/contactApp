package com.example.android.contacts;


public class Contact {
    public String Name;
    public String Phone;
    public String Mobile;
    public String Email;
    public String Address;
    public String Image;

    public Contact() {

    }

    public Contact(String Name, String Phone, String Mobile, String Email, String Address, String Image) {

        this.Name = Name;
        this.Phone = Phone;
        this.Address = Address;
        this.Email = Email;
        this.Mobile = Mobile;
        this.Image = Image;
    }

}