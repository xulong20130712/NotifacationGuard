package com.notificationguard;

public class RequestBean {

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    private String data;

    @Override
    public String toString() {
        return "RequestBean{" +
                "data='" + data + '\'' +
                '}';
    }
}
