package com.example.laundrytracker.db;

import androidx.room.TypeConverter;
import com.example.laundrytracker.model.Status;

public class Converters {
    @TypeConverter public static String fromStatus(Status s) { return s == null ? null : s.name(); }
    @TypeConverter public static Status toStatus(String s) { return s == null ? null : Status.valueOf(s); }
}
