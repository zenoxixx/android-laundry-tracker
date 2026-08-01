package com.example.laundrytracker.db;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.laundrytracker.model.ClosetItem;

import java.util.List;

@Dao
public interface ClosetItemDao {
    @Query("SELECT * FROM closet_item ORDER BY clothingName ASC")
    LiveData<List<ClosetItem>> observeAll();

    @Query("SELECT * FROM closet_item WHERE id = :id")
    LiveData<ClosetItem> observeById(String id);

    @Query("SELECT * FROM closet_item")
    List<ClosetItem> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ClosetItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ClosetItem> items);

    @Update
    void update(ClosetItem item);

    @Delete
    void delete(ClosetItem item);
}
