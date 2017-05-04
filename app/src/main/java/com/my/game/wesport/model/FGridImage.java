package com.my.game.wesport.model;

/**
 * Created by sabeeh on 30-Mar-17.
 */

public class FGridImage {
    private String standard;
    private String thumb;

    public FGridImage() {
    }

    public FGridImage(String standard, String thumb) {
        this.standard = standard;
        this.thumb = thumb;
    }

    public String getStandard() {
        return standard;
    }

    public void setStandard(String standard) {
        this.standard = standard;
    }

    public String getThumb() {
        return thumb;
    }

    public void setThumb(String thumb) {
        this.thumb = thumb;
    }
}
