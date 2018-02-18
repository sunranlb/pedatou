package com.sr.pedatou.util;

import java.util.Calendar;

/**
 * Created by SR on 2016/8/26.
 */

public class Tools {
    public static String transDB2RV(String s) {
        if (s.length() != 12) return new String();

        String month;
        if (s.charAt(4) == '0') month = s.substring(5, 6);
        else month = s.substring(4, 6);
        month = Integer.parseInt(month) + 1 + "";

        String day;
        if (s.charAt(6) == '0') day = s.substring(7, 8);
        else day = s.substring(6, 8);

        String hour = s.substring(8, 10);

        String min = s.substring(10, 12);

        return month + "/" + day + " " + hour + ":" + min;
    }

    public static Calendar dbToCalendarAccurateToDay(String timeFromDB) {
//        System.out.println(timeFromDB);
//        System.out.println(timeFromDB.substring(0,3));
        Calendar r = Calendar.getInstance();
        r.set(Integer.parseInt(timeFromDB.substring(0, 4)),
                Integer.parseInt(timeFromDB.substring(4, 6)),
                Integer.parseInt(timeFromDB.substring(6, 8)),
                0, 0, 0);
        return r;
    }

    public static String calendarToDb(Calendar cal) {
        StringBuilder sb = new StringBuilder(12);
        sb.append(cal.get(Calendar.YEAR));
        sb.append(cal.get(Calendar.MONTH));
        sb.append(cal.get(Calendar.DAY_OF_MONTH));
        sb.append(cal.get(Calendar.HOUR_OF_DAY));
        sb.append(cal.get(Calendar.MINUTE));

        Calendar r = Calendar.getInstance();
        r.set(Integer.parseInt(timeFromDB.substring(0, 4)),
                Integer.parseInt(timeFromDB.substring(4, 6)),
                Integer.parseInt(timeFromDB.substring(6, 8)),
                0, 0, 0);
        return r;
    }
}
