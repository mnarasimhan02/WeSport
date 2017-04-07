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
    private String image;
    private String author;
    private String authorName;

    public GameModel() {
    }

    public GameModel(String gameDescription, String gameDate, String startTime, String endTime, int skillLevel, String notes, String parkId, String address, String image, String author, String authorName) {

        this.gameDescription = gameDescription;
        this.gameDate = gameDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.skillLevel = skillLevel;
        this.notes = notes;
        this.parkId = parkId;
        this.address = address;
        this.image = image;
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

    public String getImage() {
        return image;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
}
