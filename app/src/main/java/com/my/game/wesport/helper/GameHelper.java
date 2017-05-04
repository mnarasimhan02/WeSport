package com.my.game.wesport.helper;

import com.my.game.wesport.R;
import com.my.game.wesport.model.GameCategoryModel;

import java.util.ArrayList;
import java.util.List;

public class GameHelper {
    private static List<GameCategoryModel> gameCategoryList = new ArrayList<>();

    static {
        gameCategoryList.add(new GameCategoryModel("Basketball", 1, R.drawable.basketball));
        gameCategoryList.add(new GameCategoryModel("Cricket",    2, R.drawable.cricket));
        gameCategoryList.add(new GameCategoryModel("Football",   3, R.drawable.football));
        gameCategoryList.add(new GameCategoryModel("Tennis",     4, R.drawable.tennis));
        gameCategoryList.add(new GameCategoryModel("Frisbee",    5, R.drawable.frisbee));
        gameCategoryList.add(new GameCategoryModel("Pingpong",   6, R.drawable.pingpong));
        gameCategoryList.add(new GameCategoryModel("Soccer",     7, R.drawable.soccer));
        gameCategoryList.add(new GameCategoryModel("Volleyball", 8, R.drawable.volleyball));
        gameCategoryList.add(new GameCategoryModel("Other",      9, R.drawable.ic_sports));
    }

    public static GameCategoryModel getGameCategory(int id) {
        for (GameCategoryModel gameCategoryModel : gameCategoryList) {
            if (gameCategoryModel.getId() == id) {
                return gameCategoryModel;
            }
        }

        return null;
    }

    public static List<GameCategoryModel> getGameCategoryList() {
        return gameCategoryList;
    }
}
