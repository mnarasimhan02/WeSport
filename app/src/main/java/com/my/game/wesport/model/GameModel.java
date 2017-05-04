package com.my.game.wesport.model;


public class GameModel {
    private String gameDescription;
    private String gameDate;
    private String startTime;
    private String endTime;
    private int skillLevel;
    private String notes;
    private String parkId;
    private String address;
    private int categoryId;
    private String author;
    private String authorName;

    public GameModel() {
    }

    public GameModel(String gameDescription, String gameDate, String startTime, String endTime, int skillLevel, String notes, String parkId, String address, int categoryId, String author, String authorName) {

        this.gameDescription = gameDescription;
        this.gameDate = gameDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.skillLevel = skillLevel;
        this.notes = notes;
        this.parkId = parkId;
        this.address = address;
        this.categoryId = categoryId;
        this.author = author;
        this.authorName = authorName;
    }

    public String getGameDescription() {
        return gameDescription;
    }

    public void setGameDescription(String gameDescription) {
        this.gameDescription = gameDescription;
    }

    public String getGameDate() {
        return gameDate;
    }


    public String getStartTime() {
        return startTime;
    }


    public String getEndTime() {
        return endTime;
    }


    public int getSkillLevel() {
        return skillLevel;
    }


    public String getNotes() {
        return notes;
    }


    public String getParkId() {
        return parkId;
    }

    public String getAddress() {
        return address;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public void setGameDate(String gameDate) {
        this.gameDate = gameDate;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public void setSkillLevel(int skillLevel) {
        this.skillLevel = skillLevel;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public void setParkId(String parkId) {
        this.parkId = parkId;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
