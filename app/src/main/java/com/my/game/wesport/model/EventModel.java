package com.my.game.wesport.model;


public class EventModel {
    private String title;
    private String description;
    private String startDate;
    private String time;
    private String author;

    public EventModel() {
    }

    public EventModel(String title, String description, String startDate, String time, String author) {
        this.title = title;
        this.description = description;
        this.startDate = startDate;
        this.time = time;
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }
}
