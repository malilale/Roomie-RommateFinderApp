package com.roomie.app;

public class Notification {
    private String userId, date, time, message, imgUrl;

    public Notification() {
    }

    public Notification(String userId, String date, String time, String message, String imgUrl) {
        this.userId = userId;
        this.date = date;
        this.time = time;
        this.message = message;
        this.imgUrl = imgUrl;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String name) {
        this.userId = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }
}
