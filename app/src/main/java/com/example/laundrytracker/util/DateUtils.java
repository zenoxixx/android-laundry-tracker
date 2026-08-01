package com.example.laundrytracker.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class DateUtils {
    private static final SimpleDateFormat FMT = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());

    public static String format(long millis) {
        return FMT.format(new Date(millis));
    }

    public static String formatOrDash(Long millis) {
        return millis == null ? "—" : format(millis);
    }
}
