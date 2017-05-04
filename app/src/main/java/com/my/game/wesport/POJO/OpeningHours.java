package com.my.game.wesport.POJO;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class OpeningHours {

    @SerializedName("open_now")
    @Expose
    private String openNow;
    @SerializedName("weekday_text")
    @Expose
    private List<Object> weekdayText = new ArrayList<Object>();

    /**
     *
     * @return
     * The openNow
     */
    public String getOpenNow() {
        return openNow;
    }

    /**
     *
     * @param openNow
     * The open_now
     */
    public void setOpenNow(String openNow) {
        this.openNow = openNow;
    }

    /**
     *
     * @return
     * The weekdayText
     */
    public List<Object> getWeekdayText() {
        return weekdayText;
    }

    /**
     *
     * @param weekdayText
     * The weekday_text
     */
    public void setWeekdayText(List<Object> weekdayText) {
        this.weekdayText = weekdayText;
    }

}