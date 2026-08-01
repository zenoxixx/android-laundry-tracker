package com.example.laundrytracker.ui.services;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.laundrytracker.model.LaundryService;
import com.example.laundrytracker.repo.LaundryRepository;

import java.util.List;

public class ServicesViewModel extends AndroidViewModel {
    private final LaundryRepository repo;
    public ServicesViewModel(@NonNull Application app) { super(app); repo = LaundryRepository.get(app); }
    public LiveData<List<LaundryService>> services() { return repo.observeServices(); }
    public void save(LaundryService s) { repo.upsertService(s); }
    public void delete(LaundryService s) { repo.deleteService(s); }
}
