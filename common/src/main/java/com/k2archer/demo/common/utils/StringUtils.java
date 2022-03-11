package com.k2archer.demo.common.utils;

public class StringUtils {
    public static String formatTime(long millisecond) {
        int minute = (int) ((millisecond / 1000) / 60);
        int second = (int) ((millisecond / 1000) % 60);
        if (minute < 10) {
            if (second < 10) {
                return "0" + minute + ":" + "0" + second;
            } else {
                return "0" + minute + ":" + second;
            }
        } else {
            if (second < 10) {
                return minute + ":" + "0" + second;
            } else {
                return minute + ":" + second;
            }
        }
    }
}
