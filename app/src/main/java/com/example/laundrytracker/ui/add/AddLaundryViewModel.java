package com.example.laundrytracker.ui.add;

import android.app.Application;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.laundrytracker.model.ClothingItem;
import com.example.laundrytracker.model.ClosetItem;
import com.example.laundrytracker.model.LaundryBatch;
import com.example.laundrytracker.model.LaundryService;
import com.example.laundrytracker.repo.LaundryRepository;

import java.util.ArrayList;
import java.util.List;

public class AddLaundryViewModel extends AndroidViewModel {
    private final LaundryRepository repo;
    private final MutableLiveData<List<PendingItem>> items = new MutableLiveData<>(new ArrayList<>());
    public String selectedServiceId;
    public String notes;

    public AddLaundryViewModel(@NonNull Application app) { super(app); repo = LaundryRepository.get(app); }

    public LiveData<List<PendingItem>> items() { return items; }
    public LiveData<List<LaundryService>> services() { return repo.observeServices(); }

    public void addPhoto(String path, boolean saveToCloset) {
        List<PendingItem> list = items.getValue();
        if (list == null) list = new ArrayList<>();
        PendingItem pending = new PendingItem(path);
        pending.saveToCloset = saveToCloset;
        list.add(pending);
        items.setValue(list);
    }

    public void addPhotos(List<String> paths, boolean saveToCloset) {
        List<PendingItem> list = items.getValue();
        if (list == null) list = new ArrayList<>();
        for (String path : paths) {
            PendingItem pending = new PendingItem(path);
            pending.saveToCloset = saveToCloset;
            list.add(pending);
        }
        items.setValue(list);
    }

    public void addFromClosetSync(Context ctx, ClosetItem closetItem) {
        List<PendingItem> list = items.getValue();
        if (list == null) list = new ArrayList<>();
        ClothingItem ci = closetItem.toClothingItem(ctx, null);
        PendingItem pi = new PendingItem(ci.photoPath);
        pi.item.clothingName = ci.clothingName;
        pi.item.brandName = ci.brandName;
        pi.item.clothingType = ci.clothingType;
        pi.item.price = ci.price;
        pi.item.billNumber = ci.billNumber;
        pi.item.notes = ci.notes;
        pi.isFromCloset = true;
        list.add(pi);
        items.setValue(list);
    }

    public void removeAt(int index) {
        List<PendingItem> list = items.getValue();
        if (list == null || index < 0 || index >= list.size()) return;
        list.remove(index);
        items.setValue(list);
    }

    public boolean save(Context ctx) {
        List<PendingItem> list = items.getValue();
        if (list == null || list.isEmpty()) return false;
        
        LaundryBatch batch = new LaundryBatch();
        batch.dateGiven = System.currentTimeMillis();
        batch.laundryServiceId = selectedServiceId;
        batch.notes = notes;
        
        List<ClothingItem> out = new ArrayList<>();
        for (PendingItem p : list) {
            out.add(p.item);
            if (p.saveToCloset && !p.isFromCloset) {
                repo.upsertClosetItem(ClosetItem.fromClothingItem(ctx, p.item));
            }
        }
        repo.saveBatchWithItems(batch, out);
        return true;
    }

    public void createService(LaundryService s) { repo.upsertService(s); }
    
    public LiveData<ClosetItem> getClosetItem(String id) {
        return repo.observeClosetItem(id);
    }
}
