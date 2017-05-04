package com.my.game.wesport.helper;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Created by admin on 29/03/2017.
 */

public class DateHelper {
    public static void getDate(String date) {

    }


    public static SimpleDateFormat getServerDateFormatter() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
    }

    public static SimpleDateFormat getAppDateFormatter() {
        return new SimpleDateFormat("MMMM dd", Locale.getDefault());
    }

    public static String getWeekDay(int day) {
        switch (day) {
            case Calendar.MONDAY:
                return "Monday";
            case Calendar.TUESDAY:
                return "Tuesday";
            case Calendar.WEDNESDAY:
                return "Wednesday";
            case Calendar.THURSDAY:
                return "Thursday";
            case Calendar.FRIDAY:
                return "Friday";
            case Calendar.SATURDAY:
                return "Saturday";
            case Calendar.SUNDAY:
                return "Sunday";
            default:
                return "";
        }
    }
}
