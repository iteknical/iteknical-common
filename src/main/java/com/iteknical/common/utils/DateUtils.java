package com.iteknical.common.utils;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author wangshenxiang
 */
public class DateUtils {

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
        DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss");

    public static Date toDate(String date) {
        return DATE_TIME_FORMATTER.parseDateTime(date).toDate();
    }

    public static String toString(Date date) {
        return DATE_TIME_FORMATTER.print(new DateTime(date));
    }
}
