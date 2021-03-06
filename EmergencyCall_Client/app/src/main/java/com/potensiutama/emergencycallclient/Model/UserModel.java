package com.potensiutama.emergencycallclient.Model;

public class UserModel {
    private String uid,name,address,phone;
    private String username,password;

    public UserModel() {
    }

    public UserModel(String uid, String name, String address, String phone, String username, String password) {
        this.uid = uid;
        this.name = name;
        this.address = address;
        this.phone = phone;
        this.username = username;
        this.password = password;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
