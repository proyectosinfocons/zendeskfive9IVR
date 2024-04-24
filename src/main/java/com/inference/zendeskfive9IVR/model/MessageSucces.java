package com.inference.zendeskfive9IVR.model;

public class MessageSucces {

    private String result;

    private String lead_code;

    public MessageSucces(String result, String lead_code) {
        this.result = result;
        this.lead_code = lead_code;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getLead_code() {
        return lead_code;
    }

    public void setLead_code(String lead_code) {
        this.lead_code = lead_code;
    }

}