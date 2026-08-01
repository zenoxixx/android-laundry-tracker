package com.example.laundrytracker.db;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.laundrytracker.model.ClothingItem;
import com.example.laundrytracker.model.Status;

import java.util.List;

@Dao
public interface ClothingItemDao {
    @Query("SELECT * FROM clothing_item WHERE batchId = :batchId")
    LiveData<List<ClothingItem>> observeForBatch(String batchId);

    @Query("SELECT * FROM clothing_item WHERE id = :id")
    LiveData<ClothingItem> observeById(String id);

    @Query("SELECT * FROM clothing_item WHERE batchId = :batchId")
    List<ClothingItem> getForBatch(String batchId);

    @Query("SELECT * FROM clothing_item")
    List<ClothingItem> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(ClothingItem item);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<ClothingItem> items);

    @Update
    void update(ClothingItem item);

    @Query("UPDATE clothing_item SET status = :status, dateReturned = :date WHERE id = :id")
    void updateStatus(String id, Status status, Long date);

    @Query("UPDATE clothing_item SET status = :status, dateReturned = :date WHERE batchId = :batchId")
    void updateStatusForBatch(String batchId, Status status, Long date);

    @Delete
    void delete(ClothingItem item);
}
