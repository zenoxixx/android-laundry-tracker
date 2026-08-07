package com.example.laundrytracker.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.laundrytracker.db.AppDatabase;
import com.example.laundrytracker.model.ClosetItem;
import com.example.laundrytracker.model.ClothingItem;
import com.example.laundrytracker.model.LaundryBatch;
import com.example.laundrytracker.model.LaundryService;
import com.example.laundrytracker.model.Status;

import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupManager {
    private static final String TAG = "BackupManager";
    private static final String MANIFEST_NAME = "manifest.json";
    private static final String DB_NAME = "laundry.db";
    private static final String PHOTOS_DIR = "photos";
    private static final int DB_VERSION = 2;

    public static void createBackup(Context context, Uri targetUri) throws Exception {
        AppLogger.i(TAG, "Starting backup creation to: " + targetUri);

        SupportSQLiteDatabase writableDb = AppDatabase.get(context).getOpenHelper().getWritableDatabase();
        // SupportSQLiteDatabase.query(String, Object[]) requires bindArgs to be non-null.
        // We use an empty array to avoid the NullPointerException and iterate the cursor to ensure execution.
        try (Cursor cursor = writableDb.query("PRAGMA wal_checkpoint(FULL)", new Object[0])) {
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    // Result not needed, just triggering the checkpoint
                }
            }
        }

        AppDatabase.get(context).close();
        AppDatabase.destroyInstance();
        AppLogger.i(TAG, "Database closed and checkpointed");

        try (OutputStream os = context.getContentResolver().openOutputStream(targetUri);
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os))) {

            JSONObject manifest = new JSONObject();
            manifest.put("version", DB_VERSION);
            byte[] manifestBytes = manifest.toString().getBytes(StandardCharsets.UTF_8);
            zos.putNextEntry(new ZipEntry(MANIFEST_NAME));
            zos.write(manifestBytes);
            zos.closeEntry();

            File dbFile = context.getDatabasePath(DB_NAME);
            addFileToZip(zos, dbFile, "db/" + DB_NAME);
            addFileToZip(zos, new File(dbFile.getPath() + "-shm"), "db/" + DB_NAME + "-shm");
            addFileToZip(zos, new File(dbFile.getPath() + "-wal"), "db/" + DB_NAME + "-wal");

            File photosDir = new File(context.getFilesDir(), PHOTOS_DIR);
            if (photosDir.exists() && photosDir.isDirectory()) {
                File[] files = photosDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        addFileToZip(zos, file, PHOTOS_DIR + "/" + file.getName());
                    }
                }
            }
            AppLogger.i(TAG, "Zip file written successfully");
        }
    }

    private static void addFileToZip(ZipOutputStream zos, File file, String entryName) throws IOException {
        if (!file.exists()) return;
        zos.putNextEntry(new ZipEntry(entryName));
        try (InputStream is = new FileInputStream(file)) {
            byte[] buffer = new byte[8192];
            int length;
            while ((length = is.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        }
        zos.closeEntry();
    }

    public static void restoreBackup(Context context, Uri sourceUri, boolean merge) throws Exception {
        AppLogger.i(TAG, "Starting restore from: " + sourceUri + " (merge=" + merge + ")");
        File tempDir = new File(context.getCacheDir(), "restore_temp");
        if (tempDir.exists()) deleteRecursive(tempDir);
        if (!tempDir.mkdirs()) {
            AppLogger.w(TAG, "Could not create restore temp directory");
        }

        try (InputStream is = context.getContentResolver().openInputStream(sourceUri);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {

            ZipEntry entry;
            boolean hasManifest = false;
            while ((entry = zis.getNextEntry()) != null) {
                File outFile = new File(tempDir, entry.getName());
                if (entry.isDirectory()) {
                    outFile.mkdirs();
                } else {
                    File parent = outFile.getParentFile();
                    if (parent != null) parent.mkdirs();
                    try (OutputStream os = new FileOutputStream(outFile)) {
                        byte[] buffer = new byte[8192];
                        int length;
                        while ((length = zis.read(buffer)) > 0) {
                            os.write(buffer, 0, length);
                        }
                    }
                }
                if (MANIFEST_NAME.equals(entry.getName())) hasManifest = true;
                zis.closeEntry();
            }

            if (!hasManifest) {
                AppLogger.e(TAG, "Invalid backup: Missing manifest", null);
                throw new Exception("Invalid backup: Missing manifest");
            }

            File manifestFile = new File(tempDir, MANIFEST_NAME);
            String content = readFileToString(manifestFile);
            JSONObject manifest = new JSONObject(content);
            int version = manifest.getInt("version");
            if (version > DB_VERSION) {
                AppLogger.e(TAG, "Backup is from a newer version: " + version, null);
                throw new Exception("Backup is from a newer version of the app");
            }
            AppLogger.i(TAG, "Manifest validated successfully");

            if (merge) {
                performMerge(context, tempDir);
            } else {
                performReplace(context, tempDir);
            }
        } finally {
            deleteRecursive(tempDir);
        }
    }

    private static void performReplace(Context context, File tempDir) throws Exception {
        AppDatabase.get(context).close();
        AppDatabase.destroyInstance();

        File currentDb = context.getDatabasePath(DB_NAME);
        File backupDb = new File(tempDir, "db/" + DB_NAME);
        if (backupDb.exists()) {
            copyFile(backupDb, currentDb);
            copyFile(new File(backupDb.getPath() + "-shm"), new File(currentDb.getPath() + "-shm"));
            copyFile(new File(backupDb.getPath() + "-wal"), new File(currentDb.getPath() + "-wal"));
        }

        File currentPhotos = new File(context.getFilesDir(), PHOTOS_DIR);
        File backupPhotos = new File(tempDir, PHOTOS_DIR);
        if (backupPhotos.exists()) {
            if (currentPhotos.exists()) deleteRecursive(currentPhotos);
            if (!backupPhotos.renameTo(currentPhotos)) {
                AppLogger.w(TAG, "Could not rename photos directory during restore");
            }
        }
        AppLogger.i(TAG, "Restore replace complete");
    }

    private static void performMerge(Context context, File tempDir) throws Exception {
        File backupDbFile = new File(tempDir, "db/" + DB_NAME);
        if (!backupDbFile.exists()) {
            AppLogger.e(TAG, "Backup database file missing during merge", null);
            throw new Exception("Backup database file missing");
        }

        SQLiteDatabase backupDb = SQLiteDatabase.openDatabase(backupDbFile.getPath(), null, SQLiteDatabase.OPEN_READONLY);
        AppDatabase db = AppDatabase.get(context);

        try {
            db.runInTransaction(() -> {
                // 1. Services - Deduplicate by name
                List<LaundryService> backupServices = readServices(backupDb);
                List<LaundryService> currentServices = db.serviceDao().getAll();
                Map<String, String> serviceIdMap = new HashMap<>(); // backupId -> currentId

                for (LaundryService bs : backupServices) {
                    LaundryService match = findServiceMatch(bs.name, currentServices);
                    if (match != null) {
                        serviceIdMap.put(bs.id, match.id);
                    } else {
                        db.serviceDao().upsert(bs);
                        serviceIdMap.put(bs.id, bs.id);
                    }
                }

                // 2. Batches
                List<LaundryBatch> backupBatches = readBatches(backupDb);
                for (LaundryBatch b : backupBatches) {
                    if (b.laundryServiceId != null && serviceIdMap.containsKey(b.laundryServiceId)) {
                        b.laundryServiceId = serviceIdMap.get(b.laundryServiceId);
                    }
                    db.batchDao().upsert(b);
                }

                // 3. Clothing Items
                List<ClothingItem> backupItems = readClothingItems(backupDb);
                db.itemDao().upsertAll(backupItems);

                // 4. Closet Items
                List<ClosetItem> backupCloset = readClosetItems(backupDb);
                db.closetDao().upsertAll(backupCloset);
            });

            // 5. Photos
            File currentPhotosDir = new File(context.getFilesDir(), PHOTOS_DIR);
            File backupPhotosDir = new File(tempDir, PHOTOS_DIR);
            if (backupPhotosDir.exists() && backupPhotosDir.isDirectory()) {
                File[] photos = backupPhotosDir.listFiles();
                if (photos != null) {
                    for (File photo : photos) {
                        File dest = new File(currentPhotosDir, photo.getName());
                        if (!dest.exists()) {
                            copyFile(photo, dest);
                        }
                    }
                }
            }
            AppLogger.i(TAG, "Restore merge complete");
        } finally {
            backupDb.close();
        }
    }

    private static LaundryService findServiceMatch(String name, List<LaundryService> services) {
        if (name == null) return null;
        String search = name.trim();
        for (LaundryService s : services) {
            if (s.name != null && s.name.trim().equalsIgnoreCase(search)) return s;
        }
        return null;
    }

    private static List<LaundryService> readServices(SQLiteDatabase db) {
        List<LaundryService> list = new ArrayList<>();
        try (Cursor c = db.query("laundry_service", null, null, null, null, null, null)) {
            while (c.moveToNext()) {
                LaundryService s = new LaundryService();
                s.id = c.getString(c.getColumnIndexOrThrow("id"));
                s.name = c.getString(c.getColumnIndexOrThrow("name"));
                s.managerName = c.getString(c.getColumnIndexOrThrow("managerName"));
                s.phone = c.getString(c.getColumnIndexOrThrow("phone"));
                s.address = c.getString(c.getColumnIndexOrThrow("address"));
                s.notes = c.getString(c.getColumnIndexOrThrow("notes"));
                list.add(s);
            }
        }
        return list;
    }

    private static List<LaundryBatch> readBatches(SQLiteDatabase db) {
        List<LaundryBatch> list = new ArrayList<>();
        try (Cursor c = db.query("laundry_batch", null, null, null, null, null, null)) {
            while (c.moveToNext()) {
                LaundryBatch b = new LaundryBatch();
                b.id = c.getString(c.getColumnIndexOrThrow("id"));
                b.dateGiven = c.getLong(c.getColumnIndexOrThrow("dateGiven"));
                b.laundryServiceId = c.getString(c.getColumnIndexOrThrow("laundryServiceId"));
                b.notes = c.getString(c.getColumnIndexOrThrow("notes"));
                b.status = Status.valueOf(c.getString(c.getColumnIndexOrThrow("status")));
                list.add(b);
            }
        }
        return list;
    }

    private static List<ClothingItem> readClothingItems(SQLiteDatabase db) {
        List<ClothingItem> list = new ArrayList<>();
        try (Cursor c = db.query("clothing_item", null, null, null, null, null, null)) {
            while (c.moveToNext()) {
                ClothingItem i = new ClothingItem();
                i.id = c.getString(c.getColumnIndexOrThrow("id"));
                i.batchId = c.getString(c.getColumnIndexOrThrow("batchId"));
                i.photoPath = c.getString(c.getColumnIndexOrThrow("photoPath"));
                i.clothingName = c.getString(c.getColumnIndexOrThrow("clothingName"));
                i.brandName = c.getString(c.getColumnIndexOrThrow("brandName"));
                i.clothingType = c.getString(c.getColumnIndexOrThrow("clothingType"));
                int priceIdx = c.getColumnIndexOrThrow("price");
                i.price = c.isNull(priceIdx) ? null : c.getDouble(priceIdx);
                i.billNumber = c.getString(c.getColumnIndexOrThrow("billNumber"));
                i.notes = c.getString(c.getColumnIndexOrThrow("notes"));
                i.status = Status.valueOf(c.getString(c.getColumnIndexOrThrow("status")));
                int dateIdx = c.getColumnIndexOrThrow("dateReturned");
                i.dateReturned = c.isNull(dateIdx) ? null : c.getLong(dateIdx);
                list.add(i);
            }
        }
        return list;
    }

    private static List<ClosetItem> readClosetItems(SQLiteDatabase db) {
        List<ClosetItem> list = new ArrayList<>();
        try (Cursor c = db.query("closet_item", null, null, null, null, null, null)) {
            while (c.moveToNext()) {
                ClosetItem i = new ClosetItem();
                i.id = c.getString(c.getColumnIndexOrThrow("id"));
                i.photoPath = c.getString(c.getColumnIndexOrThrow("photoPath"));
                i.clothingName = c.getString(c.getColumnIndexOrThrow("clothingName"));
                i.brandName = c.getString(c.getColumnIndexOrThrow("brandName"));
                i.clothingType = c.getString(c.getColumnIndexOrThrow("clothingType"));
                int priceIdx = c.getColumnIndexOrThrow("price");
                i.price = c.isNull(priceIdx) ? null : c.getDouble(priceIdx);
                i.billNumber = c.getString(c.getColumnIndexOrThrow("billNumber"));
                i.notes = c.getString(c.getColumnIndexOrThrow("notes"));
                list.add(i);
            }
        }
        return list;
    }

    private static void copyFile(File src, File dst) throws IOException {
        if (!src.exists()) return;
        File parent = dst.getParentFile();
        if (parent != null) {
            if (!parent.exists() && !parent.mkdirs()) {
                throw new IOException("Could not create destination directory: " + parent);
            }
        }
        try (InputStream in = new FileInputStream(src);
             OutputStream out = new FileOutputStream(dst)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = in.read(buf)) > 0) out.write(buf, 0, len);
        }
    }

    private static void deleteRecursive(File file) {
        if (file.isDirectory()) {
            File[] children = file.listFiles();
            if (children != null) {
                for (File child : children) deleteRecursive(child);
            }
        }
        file.delete();
    }

    private static String readFileToString(File file) throws IOException {
        try (InputStream is = new FileInputStream(file)) {
            byte[] data = new byte[(int) file.length()];
            int read = is.read(data);
            return new String(data, 0, read != -1 ? read : 0, StandardCharsets.UTF_8);
        }
    }
}
