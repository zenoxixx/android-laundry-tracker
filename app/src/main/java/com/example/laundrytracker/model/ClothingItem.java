package com.example.laundrytracker.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(
    tableName = "clothing_item",
    foreignKeys = @ForeignKey(
        entity = LaundryBatch.class,
        parentColumns = "id",
        childColumns = "batchId",
        onDelete = ForeignKey.CASCADE),
    indices = { @Index("batchId") }
)
public class ClothingItem {
    @PrimaryKey @NonNull public String id = UUID.randomUUID().toString();
    public String batchId;
    public String photoPath;
    public String clothingName;
    public String brandName;
    public String clothingType;
    public Double price;
    public String billNumber;
    public String notes;
    public Status status = Status.GIVEN;
    public Long dateReturned;

    public ClothingItem() {}
}
