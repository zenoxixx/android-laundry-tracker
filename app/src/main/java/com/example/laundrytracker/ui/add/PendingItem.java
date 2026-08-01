package com.example.laundrytracker.ui.add;

import com.example.laundrytracker.model.ClothingItem;

/** Wrapper used by AddLaundry screen while user is filling in items. */
public class PendingItem {
    public final ClothingItem item = new ClothingItem();
    public boolean saveToCloset = false;
    public boolean isFromCloset = false;

    public PendingItem(String photoPath) { 
        item.photoPath = photoPath; 
    }
}
