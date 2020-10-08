package com.example.gymmanagement;

public class sessions_getter_setter {

    String trainer_attended, date, arrival_time;

    public sessions_getter_setter() {
    }

    public sessions_getter_setter(String trainer_attended, String date, String time) {
        this.trainer_attended = trainer_attended;
        this.date = date;
        this.arrival_time = arrival_time;
    }

    public String getTrainer_attended() {
        return trainer_attended;
    }

    public String getDate() {
        return date;
    }

    public String getArrival_time() { return arrival_time; }
}
