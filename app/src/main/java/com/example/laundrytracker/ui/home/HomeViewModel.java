package com.example.laundrytracker.ui.home;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.laundrytracker.model.BatchWithItems;
import com.example.laundrytracker.repo.LaundryRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeViewModel extends AndroidViewModel {
    private final LaundryRepository repo;
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<List<BatchWithItems>> filtered = new MediatorLiveData<>();

    public HomeViewModel(@NonNull Application app) {
        super(app);
        repo = LaundryRepository.get(app);
        LiveData<List<BatchWithItems>> src = repo.observeBatches();
        filtered.addSource(src, list -> apply(list, query.getValue()));
        filtered.addSource(query, q -> apply(src.getValue(), q));
    }

    private void apply(List<BatchWithItems> list, String q) {
        if (list == null) { filtered.setValue(new ArrayList<>()); return; }
        if (q == null || q.trim().isEmpty()) { filtered.setValue(list); return; }
        String needle = q.toLowerCase(Locale.getDefault());
        List<BatchWithItems> out = new ArrayList<>();
        for (BatchWithItems b : list) {
            String serviceName = b.service != null && b.service.name != null ? b.service.name.toLowerCase(Locale.getDefault()) : "";
            String notes = b.batch != null && b.batch.notes != null ? b.batch.notes.toLowerCase(Locale.getDefault()) : "";
            if (serviceName.contains(needle) || notes.contains(needle)) out.add(b);
        }
        filtered.setValue(out);
    }

    public LiveData<List<BatchWithItems>> batches() { return filtered; }
    public void setQuery(String q) { query.setValue(q); }
    public void deleteBatch(BatchWithItems b) { if (b != null && b.batch != null) repo.deleteBatch(b.batch); }
}
