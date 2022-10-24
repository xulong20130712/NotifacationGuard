package com.notificationguard;

public class DataBean {

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getNickName() {
        return nickName;
    }

    @Override
    public String toString() {
        return "DataBean{" +
                "price='" + price + '\'' +
                ", nickName='" + nickName + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getExtra() {
        return extra;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    /**
     * {
     *  sum: 100,
     * project: "default",
     * exttra:"其他信息比如单号，用户名"
     * }
     */


    private double price;
    private String nickName;
    private String extra;
}
