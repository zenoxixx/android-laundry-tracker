package com.example.laundrytracker.repo;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.example.laundrytracker.db.AppDatabase;
import com.example.laundrytracker.db.ClothingItemDao;
import com.example.laundrytracker.db.ClosetItemDao;
import com.example.laundrytracker.db.LaundryBatchDao;
import com.example.laundrytracker.db.LaundryServiceDao;
import com.example.laundrytracker.model.BatchWithItems;
import com.example.laundrytracker.model.ClothingItem;
import com.example.laundrytracker.model.ClosetItem;
import com.example.laundrytracker.model.LaundryBatch;
import com.example.laundrytracker.model.LaundryService;
import com.example.laundrytracker.model.Status;
import com.example.laundrytracker.util.ImageStorage;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LaundryRepository {
    private static volatile LaundryRepository INSTANCE;
    private final LaundryServiceDao serviceDao;
    private final LaundryBatchDao batchDao;
    private final ClothingItemDao itemDao;
    private final ClosetItemDao closetDao;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private LaundryRepository(Context ctx) {
        AppDatabase db = AppDatabase.get(ctx);
        serviceDao = db.serviceDao();
        batchDao = db.batchDao();
        itemDao = db.itemDao();
        closetDao = db.closetDao();
    }

    public static LaundryRepository get(Context ctx) {
        if (INSTANCE == null) {
            synchronized (LaundryRepository.class) {
                if (INSTANCE == null) INSTANCE = new LaundryRepository(ctx);
            }
        }
        return INSTANCE;
    }

    public ExecutorService io() { return io; }

    // Services
    public LiveData<List<LaundryService>> observeServices() { return serviceDao.observeAll(); }
    public void upsertService(LaundryService s) { io.execute(() -> serviceDao.upsert(s)); }
    public void deleteService(LaundryService s) { io.execute(() -> serviceDao.delete(s)); }

    // Batches
    public LiveData<List<BatchWithItems>> observeBatches() { return batchDao.observeAllWithItems(); }
    public LiveData<BatchWithItems> observeBatch(String id) { return batchDao.observeById(id); }

    public void saveBatchWithItems(LaundryBatch batch, List<ClothingItem> items) {
        io.execute(() -> {
            batchDao.upsert(batch);
            for (ClothingItem i : items) i.batchId = batch.id;
            itemDao.upsertAll(items);
        });
    }

    public void deleteBatch(LaundryBatch b) {
        io.execute(() -> {
            List<ClothingItem> items = itemDao.getForBatch(b.id);
            for (ClothingItem item : items) {
                ImageStorage.deleteQuietly(item.photoPath);
            }
            batchDao.delete(b);
        });
    }

    public void markBatchReturned(String batchId) {
        io.execute(() -> {
            long now = System.currentTimeMillis();
            itemDao.updateStatusForBatch(batchId, Status.RETURNED, now);
            batchDao.updateStatus(batchId, Status.RETURNED);
        });
    }

    private void updateBatchStatusFromItems(String batchId) {
        List<ClothingItem> items = itemDao.getForBatch(batchId);
        if (items.isEmpty()) return;

        int returnedCount = 0;
        for (ClothingItem item : items) {
            if (item.status == Status.RETURNED) {
                returnedCount++;
            }
        }

        Status newStatus;
        if (returnedCount == items.size()) {
            newStatus = Status.RETURNED;
        } else if (returnedCount > 0) {
            newStatus = Status.PARTIALLY_RETURNED;
        } else {
            newStatus = Status.GIVEN;
        }
        batchDao.updateStatus(batchId, newStatus);
    }

    // Items
    public LiveData<ClothingItem> observeItem(String id) { return itemDao.observeById(id); }
    public void updateItem(ClothingItem i) { 
        io.execute(() -> {
            itemDao.update(i);
            updateBatchStatusFromItems(i.batchId);
        });
    }
    public void deleteItem(ClothingItem i) { 
        io.execute(() -> {
            ImageStorage.deleteQuietly(i.photoPath);
            itemDao.delete(i);
            updateBatchStatusFromItems(i.batchId);
        }); 
    }

    public void setItemReturned(String itemId, String batchId, boolean returned) {
        io.execute(() -> {
            Status status = returned ? Status.RETURNED : Status.GIVEN;
            Long date = returned ? System.currentTimeMillis() : null;
            itemDao.updateStatus(itemId, status, date);
            updateBatchStatusFromItems(batchId);
        });
    }

    public void markItemReturned(String itemId, String batchId) {
        setItemReturned(itemId, batchId, true);
    }

    // Closet
    public LiveData<List<ClosetItem>> observeCloset() { return closetDao.observeAll(); }
    public LiveData<ClosetItem> observeClosetItem(String id) { return closetDao.observeById(id); }
    public void upsertClosetItem(ClosetItem item) { io.execute(() -> closetDao.upsert(item)); }
    public void deleteClosetItem(ClosetItem item) {
        io.execute(() -> {
            ImageStorage.deleteQuietly(item.photoPath);
            closetDao.delete(item);
        });
    }
}
