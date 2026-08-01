package com.example.laundrytracker.model;

import androidx.room.Embedded;
import androidx.room.Relation;

import java.util.List;

public class BatchWithItems {
    @Embedded public LaundryBatch batch;

    @Relation(parentColumn = "id", entityColumn = "batchId")
    public List<ClothingItem> items;

    @Relation(parentColumn = "laundryServiceId", entityColumn = "id")
    public LaundryService service;
}
