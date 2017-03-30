package com.my.game.wesport.helper;

import android.support.annotation.DrawableRes;

import com.my.game.wesport.R;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by sabeeh on 27-Mar-17.
 */

public class GameHelper {
    private static Map<String, Object> map = new HashMap<>();
    private static int[] gridViewImageId = {
            R.drawable.basketball, R.drawable.cricket, R.drawable.football, R.drawable.tennis,
            R.drawable.frisbee, R.drawable.pingpong, R.drawable.soccer, R.drawable.volleyball
    };

    static {
        map.put("basketball", R.drawable.basketball);
        map.put("cricket", R.drawable.cricket);
        map.put("football", R.drawable.football);
        map.put("tennis", R.drawable.tennis);
        map.put("frisbee", R.drawable.frisbee);
        map.put("pingpong", R.drawable.pingpong);
        map.put("volleyball", R.drawable.volleyball);
    }

    public static String getGameNameByIndex(int gameIndex) {
        switch (gridViewImageId[gameIndex]) {
            case R.drawable.basketball:
                return "basketball";
            case R.drawable.cricket:
                return "cricket";
            case R.drawable.football:
                return "football";
            case R.drawable.tennis:
                return "tennis";
            case R.drawable.frisbee:
                return "frisbee";
            case R.drawable.pingpong:
                return "pingpong";
            case R.drawable.volleyball:
                return "volleyball";
            default:
                return "basketball";
        }
    }

    public static int[] getImages() {
        return gridViewImageId;
    }

    public static int getGameImage(String key) {
        if (map.containsKey(key)) {
            return (int) map.get(key);
        } else {
            return R.drawable.basketball;
        }
    }
}
