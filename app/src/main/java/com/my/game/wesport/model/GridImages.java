package com.my.game.wesport.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by sb on 16-8-29.
 */

public class GridImages {
    List<String> standard;
    List<String> thumb;


    public GridImages() {
        standard = new ArrayList<>();
        thumb = new ArrayList<>();

    }

    public void addNewImage(String standardString, String thumbString) {
        standard.add(standardString);
        thumb.add(thumbString);
    }

    public List<String> getStandard() {
        return standard;
    }

    public List<String> getThumb() {
        return thumb;
    }

    public void removeThumb(int pos) {
        if (standard.size() > pos) {
            standard.remove(pos);
        }
        if (thumb.size() > pos) {
            thumb.remove(pos);
        }

    }

    public void swapItems(int pos1, int pos2) {
        String standard1 = standard.get(pos1);
        String standard2 = standard.get(pos2);

        standard.remove(pos1);
        standard.add(pos1, standard2);
        standard.remove(pos2);
        standard.add(pos2, standard1);


        String item1 = thumb.get(pos1);
        String item2 = thumb.get(pos2);

        thumb.remove(pos1);
        thumb.add(pos1, item2);
        thumb.remove(pos2);
        thumb.add(pos2, item1);
    }

    public void removeThumb(String selectedItem) {
        int selectedIndex = -1;
        for (int i = 0; i < thumb.size(); i++) {
            if (thumb.get(i).equals(selectedItem)) {
                selectedIndex = i;
                break;
            }
        }
        if (selectedIndex > -1) {
            removeThumb(selectedIndex);
        }
    }

    public String getThumb(int pos) {
        if (thumb.size() > pos) {
            return thumb.get(pos);
        } else {
            return null;
        }
    }

    public String getStandard(int pos) {
        if (standard.size() > pos) {
            return standard.get(pos);
        } else {
            return null;
        }
    }

    public int size() {
        return thumb.size();
    }
}
