package com.hello.khushboo.replonguesty;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;

import java.util.List;

public class GuestDataFirebase {

    String check_in_time;
    String car_type;
    Boolean checkout;
    String checkout_time;
    DocumentReference document_id;
    String flat_no;
    Boolean frequent_visitor;
    String name;
    String phone_no;
    String profile_image_url;
    String purpose;
    String user_id;
    String vehicle_image_url;
    String vehicle_no;

    public GuestDataFirebase(String car_type, Boolean checkout, String checkout_time,String check_in_time, DocumentReference document_id, String flat_no, Boolean frequent_visitor, String name, String phone_no, String profile_image_url, String purpose, String user_id, String vehicle_image_url, String vehicle_no) {
        this.check_in_time=check_in_time;
        this.car_type = car_type;
        this.checkout = checkout;
        this.checkout_time = checkout_time;
        this.document_id = document_id;
        this.flat_no = flat_no;
        this.frequent_visitor = frequent_visitor;
        this.name = name;
        this.phone_no = phone_no;
        this.profile_image_url = profile_image_url;
        this.purpose = purpose;
        this.user_id = user_id;
        this.vehicle_image_url = vehicle_image_url;
        this.vehicle_no = vehicle_no;
    }

    public String getCheck_in_time() {
        return check_in_time;
    }

    public void setCheck_in_time(String check_in_time) {
        this.check_in_time = check_in_time;
    }

    public String getCheckout_time() {
        return checkout_time;
    }

    public void setCheckout_time(String checkout_time) {
        this.checkout_time = checkout_time;
    }

    public String getCar_type() {
        return car_type;
    }

    public void setCar_type(String car_type) {
        this.car_type = car_type;
    }

    public Boolean getCheckout() {
        return checkout;
    }

    public void setCheckout(Boolean checkout) {
        this.checkout = checkout;
    }


    public DocumentReference getDocument_id() {
        return document_id;
    }

    public void setDocument_id(DocumentReference document_id) {
        this.document_id = document_id;
    }

    public String getFlat_no() {
        return flat_no;
    }

    public void setFlat_no(String flat_no) {
        this.flat_no = flat_no;
    }

    public Boolean getFrequent_visitor() {
        return frequent_visitor;
    }

    public void setFrequent_visitor(Boolean frequent_visitor) {
        this.frequent_visitor = frequent_visitor;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone_no() {
        return phone_no;
    }

    public void setPhone_no(String phone_no) {
        this.phone_no = phone_no;
    }

    public String getProfile_image_url() {
        return profile_image_url;
    }

    public void setProfile_image_url(String profile_image_url) {
        this.profile_image_url = profile_image_url;
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

    public String getVehicle_image_url() {
        return vehicle_image_url;
    }

    public void setVehicle_image_url(String vehicle_image_url) {
        this.vehicle_image_url = vehicle_image_url;
    }

    public String getVehicle_no() {
        return vehicle_no;
    }

    public void setVehicle_no(String vehicle_no) {
        this.vehicle_no = vehicle_no;
    }
}
