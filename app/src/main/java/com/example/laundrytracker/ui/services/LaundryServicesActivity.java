package com.example.laundrytracker.ui.services;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrytracker.R;
import com.example.laundrytracker.model.LaundryService;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

public class LaundryServicesActivity extends AppCompatActivity {

    private ServicesViewModel vm;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        vm = new ViewModelProvider(this).get(ServicesViewModel.class);

        RecyclerView rv = findViewById(R.id.rv_services);
        View empty = findViewById(R.id.empty_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        ServiceAdapter adapter = new ServiceAdapter(new ServiceAdapter.Listener() {
            @Override public void onEdit(LaundryService s) { showEditDialog(s); }
            @Override public void onDelete(LaundryService s) {
                new MaterialAlertDialogBuilder(LaundryServicesActivity.this)
                    .setTitle(R.string.delete_service)
                    .setMessage(R.string.delete_service_msg)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete, (d, w) -> vm.delete(s))
                    .show();
            }
        });
        rv.setAdapter(adapter);
        vm.services().observe(this, list -> {
            adapter.submitList(list);
            empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        ExtendedFloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> showEditDialog(null));
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }

    private void showEditDialog(LaundryService existing) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_edit_service, null);
        TextInputEditText name = form.findViewById(R.id.et_name);
        TextInputEditText manager = form.findViewById(R.id.et_manager);
        TextInputEditText phone = form.findViewById(R.id.et_phone);
        TextInputEditText address = form.findViewById(R.id.et_address);
        TextInputEditText notes = form.findViewById(R.id.et_notes);
        if (existing != null) {
            name.setText(existing.name);
            manager.setText(existing.managerName);
            phone.setText(existing.phone);
            address.setText(existing.address);
            notes.setText(existing.notes);
        }
        new MaterialAlertDialogBuilder(this)
            .setTitle(existing == null ? R.string.add_service : R.string.edit_service)
            .setView(form)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, (d, w) -> {
                String n = name.getText() == null ? "" : name.getText().toString().trim();
                if (n.isEmpty()) return;
                
                // Fix: Create NEW instance to trigger DiffUtil
                LaundryService s = new LaundryService();
                if (existing != null) s.id = existing.id;

                s.name = n;
                s.managerName = manager.getText() == null ? null : manager.getText().toString();
                s.phone = phone.getText() == null ? null : phone.getText().toString();
                s.address = address.getText() == null ? null : address.getText().toString();
                s.notes = notes.getText() == null ? null : notes.getText().toString();
                vm.save(s);
            })
            .show();
    }
}
