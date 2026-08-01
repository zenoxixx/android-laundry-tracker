package com.example.laundrytracker.ui.batch;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.laundrytracker.model.BatchWithItems;
import com.example.laundrytracker.repo.LaundryRepository;

public class BatchDetailsViewModel extends AndroidViewModel {
    private final LaundryRepository repo;
    public BatchDetailsViewModel(@NonNull Application app) { super(app); repo = LaundryRepository.get(app); }

    public LiveData<BatchWithItems> batch(String id) { return repo.observeBatch(id); }
    public void markAllReturned(String batchId) { repo.markBatchReturned(batchId); }
    public void setItemReturned(String itemId, String batchId, boolean returned) { repo.setItemReturned(itemId, batchId, returned); }
    public void deleteBatch(BatchWithItems b) { if (b != null && b.batch != null) repo.deleteBatch(b.batch); }
}
