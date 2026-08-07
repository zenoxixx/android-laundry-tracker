package com.example.laundrytracker.util;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AppLogger {
    private static final String LOG_DIR = "logs";
    private static final String LOG_FILE = "app_log.txt";
    private static final long MAX_FILE_SIZE = 500 * 1024; // 500 KB
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.US);
    
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    private static File logFile;

    public static void init(Context context) {
        File dir = new File(context.getFilesDir(), LOG_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        logFile = new File(dir, LOG_FILE);
    }

    public static void i(String tag, String message) {
        Log.i(tag, message);
        logToFile("INFO", tag, message, null);
    }

    public static void w(String tag, String message) {
        Log.w(tag, message);
        logToFile("WARN", tag, message, null);
    }

    public static void e(String tag, String message, Throwable t) {
        Log.e(tag, message, t);
        logToFile("ERROR", tag, message, t);
    }

    private static void logToFile(String level, String tag, String message, Throwable t) {
        if (logFile == null) return;
        
        executor.execute(() -> {
            try {
                if (logFile.exists() && logFile.length() > MAX_FILE_SIZE) {
                    trimLogFile();
                }
                
                try (FileOutputStream fos = new FileOutputStream(logFile, true);
                     PrintWriter writer = new PrintWriter(fos)) {
                    String timestamp = DATE_FORMAT.format(new Date());
                    writer.println(String.format("%s %s/%s: %s", timestamp, level, tag, message));
                    if (t != null) {
                        writer.println(Log.getStackTraceString(t));
                    }
                    writer.flush();
                }
            } catch (IOException e) {
                Log.e("AppLogger", "Failed to write log to file", e);
            }
        });
    }

    private static void trimLogFile() {
        try {
            byte[] bytes = java.nio.file.Files.readAllBytes(logFile.toPath());
            String content = new String(bytes, StandardCharsets.UTF_8);
            String[] lines = content.split("\n");
            int linesToDrop = (int) (lines.length * 0.3);
            
            try (FileOutputStream fos = new FileOutputStream(logFile, false);
                 PrintWriter writer = new PrintWriter(fos)) {
                for (int i = linesToDrop; i < lines.length; i++) {
                    writer.println(lines[i]);
                }
                writer.flush();
            }
        } catch (IOException e) {
            Log.e("AppLogger", "Failed to trim log file", e);
        }
    }
    
    public static File getLogFile() {
        return logFile;
    }
}
