package com.fiberfox.fxt.utils;

import static androidx.core.util.Preconditions.checkArgument;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * 日期工具类
 */
public class TimeUtil {

    /**
     * 日期格式：17th Aug 2022 09:00:39
     * @param date data
     * @return 指定的日期格式
     */
    public static String getFormattedDate(Date date){
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        String dayNumberSuffix = getDayOfMonthSuffix(cal.get(Calendar.DATE));
        return new SimpleDateFormat("d'" + dayNumberSuffix + "' MMM yyyy HH:mm:ss", Locale.ENGLISH).format(date);
    }

    private static String getDayOfMonthSuffix(final int n) {
        checkArgument(n >= 1 && n <= 31, "illegal day of month: " + n);
        if (n >= 11 && n <= 13) {
            return "th";
        }
        switch (n % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }
}
