package com.example.gymmanagement;

public class trainer_sessions_getter_setter {

    String t_arrival_date, t_arrival_time, client_attended;

    public trainer_sessions_getter_setter() {
    }

    public trainer_sessions_getter_setter(String t_arrival_date, String t_arrival_time, String client_attended) {
        this.t_arrival_date = t_arrival_date;
        this.t_arrival_time = t_arrival_time;
        this.client_attended = client_attended;
    }

    public String getT_arrival_date() {
        return t_arrival_date;
    }

    public String getT_arrival_time() {
        return t_arrival_time;
    }

    public String getClient_attended() {
        return client_attended;
    }
}
