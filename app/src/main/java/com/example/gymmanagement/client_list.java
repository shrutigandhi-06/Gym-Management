package com.example.gymmanagement;

public class client_list {

    String name, phone, email, join_date, due_date, plan, amount, amt_paid, amt_due, uri;

    public client_list() {
    }

    public client_list(String name, String phone, String email, String join_date, String due_date, String plan, String amount, String amt_paid, String amt_due, String uri) {
        this.name = name;
        this.phone = phone;
        this.join_date = join_date;
        this.due_date = due_date;
        this.plan = plan;
        this.amount = amount;
        this.amt_paid = amt_paid;
        this.amt_due = amt_due;
        this.email = email;
        this.uri = uri;
    }

    public String getName() {
        return name;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() { return email; }

    public String getJoin_date() {
        return join_date;
    }

    public String getDue_date() {
        return due_date;
    }

    public String getPlan() {
        return plan;
    }

    public String getAmount() {
        return amount;
    }

    public String getAmt_paid() {
        return amt_paid;
    }

    public String getAmt_due() {
        return amt_due;
    }

    public String getUri() { return uri; }
}
