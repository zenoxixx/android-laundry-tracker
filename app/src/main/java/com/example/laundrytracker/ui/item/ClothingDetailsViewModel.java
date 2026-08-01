package com.example.laundrytracker.ui.item;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.laundrytracker.model.ClothingItem;
import com.example.laundrytracker.repo.LaundryRepository;

public class ClothingDetailsViewModel extends AndroidViewModel {
    private final LaundryRepository repo;
    public ClothingDetailsViewModel(@NonNull Application app) { super(app); repo = LaundryRepository.get(app); }

    public LiveData<ClothingItem> item(String id) { return repo.observeItem(id); }
    public void update(ClothingItem i) { repo.updateItem(i); }
    public void markReturned(String id, String batchId) { repo.markItemReturned(id, batchId); }
    public void delete(ClothingItem i) { repo.deleteItem(i); }
}
