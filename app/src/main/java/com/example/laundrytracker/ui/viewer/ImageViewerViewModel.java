package com.example.laundrytracker.ui.viewer;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import com.example.laundrytracker.model.ClosetItem;
import com.example.laundrytracker.repo.LaundryRepository;

public class ImageViewerViewModel extends AndroidViewModel {
    private final LaundryRepository repo;

    public ImageViewerViewModel(@NonNull Application application) {
        super(application);
        repo = LaundryRepository.get(application);
    }

    public LiveData<ClosetItem> getClosetItem(String id) {
        return repo.observeClosetItem(id);
    }

    public void save(ClosetItem item) {
        repo.upsertClosetItem(item);
    }
}
