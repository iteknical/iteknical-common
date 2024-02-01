package com.iteknical.common.utils;

import java.util.Date;

public class ConsoleLogUtils {

    public static void debug(String line) {
        log("DEBUG", line);
    }

    public static void info(String line) {
        log("INFO", line);
    }

    public static void warn(String line) {
        log("WARN", line);
    }

    public static void error(String line) {
        log("ERROR", line);
    }

    private static void log(String level, String line) {
        System.out.println(DateUtils.toString(new Date()) + " " + "[" + level + "] " + line);
    }
}
