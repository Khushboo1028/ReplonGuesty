package com.hello.khushboo.replonguesty;

import com.google.firebase.Timestamp;

public class GuestDataFirebase {

    Timestamp date_created;
    String document_id;
    String flat_no;
    String name;
    Long phone_number;
    String purpose;
    String user_id;
    String vehicle_no;
    String image_url;

    public GuestDataFirebase(Timestamp date_created, String document_id, String flat_no, String name, Long phone_number, String purpose, String user_id, String vehicle_no,String image_url) {
        this.date_created = date_created;
        this.document_id = document_id;
        this.flat_no = flat_no;
        this.name = name;
        this.phone_number = phone_number;
        this.purpose = purpose;
        this.user_id = user_id;
        this.vehicle_no = vehicle_no;
        this.image_url=image_url;
    }

    public GuestDataFirebase(){}

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public Timestamp getDate_created() {
        return date_created;
    }

    public void setDate_created(Timestamp date_created) {
        this.date_created = date_created;
    }

    public String getDocument_id() {
        return document_id;
    }

    public void setDocument_id(String document_id) {
        this.document_id = document_id;
    }

    public String getFlat_no() {
        return flat_no;
    }

    public void setFlat_no(String flat_no) {
        this.flat_no = flat_no;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(Long phone_number) {
        this.phone_number = phone_number;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getVehicle_no() {
        return vehicle_no;
    }

    public void setVehicle_no(String vehicle_no) {
        this.vehicle_no = vehicle_no;
    }
}
