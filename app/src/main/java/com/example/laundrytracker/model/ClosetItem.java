package com.example.laundrytracker.model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.example.laundrytracker.util.ImageStorage;
import java.io.IOException;
import java.util.UUID;

@Entity(tableName = "closet_item")
public class ClosetItem {
    @PrimaryKey @NonNull public String id = UUID.randomUUID().toString();
    public String photoPath;
    public String clothingName;
    public String brandName;
    public String clothingType;
    public Double price;
    public String billNumber;
    public String notes;

    public ClosetItem() {}
    
    public static ClosetItem fromClothingItem(Context ctx, ClothingItem item) {
        ClosetItem closetItem = new ClosetItem();
        try {
            closetItem.photoPath = ImageStorage.copyFile(ctx, item.photoPath);
        } catch (IOException e) {
            closetItem.photoPath = item.photoPath;
        }
        closetItem.clothingName = item.clothingName;
        closetItem.brandName = item.brandName;
        closetItem.clothingType = item.clothingType;
        closetItem.price = item.price;
        closetItem.billNumber = item.billNumber;
        closetItem.notes = item.notes;
        return closetItem;
    }

    public ClothingItem toClothingItem(Context ctx, String batchId) {
        ClothingItem item = new ClothingItem();
        item.batchId = batchId;
        try {
            item.photoPath = ImageStorage.copyFile(ctx, this.photoPath);
        } catch (IOException e) {
            item.photoPath = this.photoPath;
        }
        item.clothingName = this.clothingName;
        item.brandName = this.brandName;
        item.clothingType = this.clothingType;
        item.price = this.price;
        item.billNumber = this.billNumber;
        item.notes = this.notes;
        item.status = Status.GIVEN;
        return item;
    }
}
