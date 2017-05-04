package com.my.game.wesport.model;

import android.support.annotation.DrawableRes;

/**
 * Created by admin on 22/04/2017.
 */

public class GameCategoryModel {
    private String title;
    @DrawableRes
    private int image;
    private int id;

    public GameCategoryModel(String title, int id, @DrawableRes int image) {
        this.title = title;
        this.image = image;
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public int getImage() {
        return image;
    }

    public int getId() {
        return id;
    }
}
