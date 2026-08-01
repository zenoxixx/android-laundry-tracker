package com.example.laundrytracker.ui.batch;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrytracker.R;
import com.example.laundrytracker.model.BatchWithItems;
import com.example.laundrytracker.model.ClothingItem;
import com.example.laundrytracker.model.Status;
import com.example.laundrytracker.ui.item.ClothingDetailsActivity;
import com.example.laundrytracker.ui.viewer.ImageViewerActivity;
import com.example.laundrytracker.util.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class BatchDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_BATCH_ID = "batchId";

    private BatchDetailsViewModel vm;
    private BatchWithItems current;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_batch_details);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String batchId = getIntent().getStringExtra(EXTRA_BATCH_ID);
        if (batchId == null) { finish(); return; }

        vm = new ViewModelProvider(this).get(BatchDetailsViewModel.class);

        TextView service = findViewById(R.id.tv_service);
        TextView date = findViewById(R.id.tv_date);
        TextView notes = findViewById(R.id.tv_notes);
        Chip chip = findViewById(R.id.chip_status);
        MaterialButton markAll = findViewById(R.id.btn_mark_returned);
        MaterialButton delete = findViewById(R.id.btn_delete);

        RecyclerView rv = findViewById(R.id.rv_items);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        ItemAdapter adapter = new ItemAdapter(new ItemAdapter.Listener() {
            @Override
            public void onClick(ClothingItem item) {
                Intent i = new Intent(BatchDetailsActivity.this, ClothingDetailsActivity.class);
                i.putExtra(ClothingDetailsActivity.EXTRA_ITEM_ID, item.id);
                startActivity(i);
            }

            @Override
            public void onStatusChange(ClothingItem item, boolean isReturned) {
                vm.setItemReturned(item.id, batchId, isReturned);
            }

            @Override
            public void onImageClick(ClothingItem item) {
                Intent i = new Intent(BatchDetailsActivity.this, ImageViewerActivity.class);
                i.putExtra(ImageViewerActivity.EXTRA_IMAGE_PATH, item.photoPath);
                startActivity(i);
            }
        });
        rv.setAdapter(adapter);

        vm.batch(batchId).observe(this, b -> {
            if (b == null || b.batch == null) { finish(); return; }
            current = b;
            service.setText(b.service != null && b.service.name != null ? b.service.name : getString(R.string.no_service));
            date.setText(getString(R.string.given_on, DateUtils.format(b.batch.dateGiven)));
            notes.setVisibility(b.batch.notes == null || b.batch.notes.isEmpty() ? View.GONE : View.VISIBLE);
            notes.setText(b.batch.notes);
            
            Status status = b.batch.status;
            int textRes, bgRes, fgRes;
            if (status == Status.RETURNED) {
                textRes = R.string.status_returned;
                bgRes = R.color.chip_returned;
                fgRes = R.color.chip_on_returned;
            } else if (status == Status.PARTIALLY_RETURNED) {
                textRes = R.string.status_partially_returned;
                bgRes = R.color.chip_partially_returned;
                fgRes = R.color.chip_on_partially_returned;
            } else {
                textRes = R.string.status_given;
                bgRes = R.color.chip_given;
                fgRes = R.color.chip_on_given;
            }
            
            chip.setText(textRes);
            chip.setChipBackgroundColorResource(bgRes);
            chip.setTextColor(ContextCompat.getColor(this, fgRes));
            
            markAll.setEnabled(status != Status.RETURNED);
            adapter.submitList(b.items);
        });

        markAll.setOnClickListener(v ->
            new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.mark_all_returned)
                .setMessage(R.string.mark_all_returned_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, (d, w) -> vm.markAllReturned(batchId))
                .show());

        delete.setOnClickListener(v ->
            new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_batch)
                .setMessage(R.string.delete_batch_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    vm.deleteBatch(current);
                    finish();
                })
                .show());
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
