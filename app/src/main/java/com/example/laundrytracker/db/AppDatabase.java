package com.example.laundrytracker.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.example.laundrytracker.model.ClothingItem;
import com.example.laundrytracker.model.ClosetItem;
import com.example.laundrytracker.model.LaundryBatch;
import com.example.laundrytracker.model.LaundryService;

@Database(
    entities = { LaundryService.class, LaundryBatch.class, ClothingItem.class, ClosetItem.class },
    version = 2,
    exportSchema = true
)
@TypeConverters(Converters.class)
public abstract class AppDatabase extends RoomDatabase {
    public abstract LaundryServiceDao serviceDao();
    public abstract LaundryBatchDao batchDao();
    public abstract ClothingItemDao itemDao();
    public abstract ClosetItemDao closetDao();

    private static volatile AppDatabase INSTANCE;

    public static AppDatabase get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        ctx.getApplicationContext(),
                        AppDatabase.class,
                        "laundry.db"
                    )
                    // Keep destructive migration only for legacy versions (1 and 2)
                    // to prevent data loss on future version bumps.
                    .fallbackToDestructiveMigrationFrom(1, 2)
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }
}
