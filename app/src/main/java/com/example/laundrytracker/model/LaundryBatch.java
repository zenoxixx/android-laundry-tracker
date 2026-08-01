package com.example.laundrytracker.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(
    tableName = "laundry_batch",
    foreignKeys = @ForeignKey(
        entity = LaundryService.class,
        parentColumns = "id",
        childColumns = "laundryServiceId",
        onDelete = ForeignKey.SET_NULL),
    indices = { @Index("laundryServiceId") }
)
public class LaundryBatch {
    @PrimaryKey @NonNull public String id = UUID.randomUUID().toString();
    public long dateGiven;
    public String laundryServiceId;
    public String notes;
    public Status status = Status.GIVEN;

    public LaundryBatch() {}
}
