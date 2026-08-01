package com.example.laundrytracker.db;

import androidx.lifecycle.LiveData;
import androidx.room.*;

import com.example.laundrytracker.model.LaundryService;

import java.util.List;

@Dao
public interface LaundryServiceDao {
    @Query("SELECT * FROM laundry_service ORDER BY name COLLATE NOCASE")
    LiveData<List<LaundryService>> observeAll();

    @Query("SELECT * FROM laundry_service ORDER BY name COLLATE NOCASE")
    List<LaundryService> getAll();

    @Query("SELECT * FROM laundry_service WHERE id = :id")
    LaundryService findById(String id);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void upsert(LaundryService service);

    @Delete
    void delete(LaundryService service);
}
