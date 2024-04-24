package com.inference.zendeskfive9IVR.model;

public class RequestNubyx {

    private String id;
    private String number;
    private String channel;

    public RequestNubyx() {
    }

    public RequestNubyx(String id, String number, String channel) {
        this.id = id;
        this.number = number;
        this.channel = channel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}