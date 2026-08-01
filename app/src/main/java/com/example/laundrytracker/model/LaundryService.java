package com.example.laundrytracker.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "laundry_service")
public class LaundryService {
    @PrimaryKey @NonNull public String id = UUID.randomUUID().toString();
    public String name;
    public String managerName;
    public String phone;
    public String address;
    public String notes;

    public LaundryService() {}

    @NonNull @Override public String toString() {
        return name == null ? "" : name;
    }
}
