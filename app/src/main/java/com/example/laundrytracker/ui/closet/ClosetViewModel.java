package com.example.laundrytracker.ui.closet;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.laundrytracker.model.ClosetItem;
import com.example.laundrytracker.repo.LaundryRepository;

import java.util.List;

public class ClosetViewModel extends AndroidViewModel {
    private final LaundryRepository repo;

    public ClosetViewModel(@NonNull Application app) {
        super(app);
        repo = LaundryRepository.get(app);
    }

    public LiveData<List<ClosetItem>> items() {
        return repo.observeCloset();
    }

    public void save(ClosetItem item) {
        repo.upsertClosetItem(item);
    }

    public void delete(ClosetItem item) {
        repo.deleteClosetItem(item);
    }
}
