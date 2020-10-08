package com.example.gymmanagement;

public class trainer_list {

    private String name, phone, email, address, blood_grp, uri;

    public trainer_list() {}

    public trainer_list(String name, String phone, String email, String address, String blood_grp, String uri) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.blood_grp = blood_grp;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public String getBlood_grp() {
        return blood_grp;
    }

    public String getUri() {return uri;}
}
