package com.example.laundrytracker.db;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.laundrytracker.model.BatchWithItems;
import com.example.laundrytracker.model.LaundryBatch;
import com.example.laundrytracker.model.Status;

import java.util.List;

@Dao
public interface LaundryBatchDao {

    @Transaction
    @Query("SELECT * FROM laundry_batch ORDER BY dateGiven DESC")
    LiveData<List<BatchWithItems>> observeAllWithItems();

    @Transaction
    @Query("SELECT * FROM laundry_batch WHERE id = :id")
    LiveData<BatchWithItems> observeById(String id);

    @Transaction
    @Query("SELECT * FROM laundry_batch WHERE id = :id")
    BatchWithItems getById(String id);

    @Query("SELECT * FROM laundry_batch")
    List<LaundryBatch> getAll();

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(LaundryBatch batch);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsertAll(List<LaundryBatch> batches);

    @Query("UPDATE laundry_batch SET status = :status WHERE id = :id")
    void updateStatus(String id, Status status);

    @Delete
    void delete(LaundryBatch batch);
}
