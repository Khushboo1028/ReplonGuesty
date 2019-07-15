package com.hello.khushboo.replonguesty.AddGuests;

public class ContentAddGuest {

    String name;
    String phone;
    String purpose;
    String flat;

    public ContentAddGuest(String name, String phone, String purpose, String flat) {
        this.name = name;
        this.phone = phone;
        this.purpose = purpose;
        this.flat = flat;
    }

    public ContentAddGuest(){

    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getFlat() {
        return flat;
    }

    public void setFlat(String flat) {
        this.flat = flat;
    }
}
