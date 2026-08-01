package com.example.laundrytracker.util;

import android.content.Context;
import android.net.Uri;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ImageStorage {

    public static File photosDir(Context ctx) {
        File dir = new File(ctx.getFilesDir(), "photos");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    /** Creates an empty file for camera capture. */
    public static File newPhotoFile(Context ctx) {
        return new File(photosDir(ctx), UUID.randomUUID().toString() + ".jpg");
    }

    /** Copies content from a URI into internal storage and returns the absolute file path. */
    public static String copyToInternal(Context ctx, Uri uri) throws IOException {
        File dest = newPhotoFile(ctx);
        try (InputStream in = ctx.getContentResolver().openInputStream(uri);
             OutputStream out = new FileOutputStream(dest)) {
            if (in == null) throw new IOException("Could not open " + uri);
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        }
        return dest.getAbsolutePath();
    }

    /** Copies an existing file to a new internal location. */
    public static String copyFile(Context ctx, String sourcePath) throws IOException {
        if (sourcePath == null) return null;
        File source = new File(sourcePath);
        if (!source.exists()) return null;

        File dest = newPhotoFile(ctx);
        try (InputStream in = new FileInputStream(source);
             OutputStream out = new FileOutputStream(dest)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
        }
        return dest.getAbsolutePath();
    }

    /** Deletes a file at the given path if it exists. */
    public static void deleteQuietly(String path) {
        if (path == null || path.isEmpty()) return;
        try {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception ignored) {}
    }
}
