package com.example.laundrytracker.ui.item;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.laundrytracker.R;
import com.example.laundrytracker.model.ClothingItem;
import com.example.laundrytracker.model.Status;
import com.example.laundrytracker.ui.viewer.ImageViewerActivity;
import com.example.laundrytracker.util.DateUtils;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

public class ClothingDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_ITEM_ID = "itemId";

    private ClothingDetailsViewModel vm;
    private ClothingItem current;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clothing_details);
        setSupportActionBar(findViewById(R.id.toolbar));
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        String id = getIntent().getStringExtra(EXTRA_ITEM_ID);
        if (id == null) { finish(); return; }

        vm = new ViewModelProvider(this).get(ClothingDetailsViewModel.class);

        ImageView photo = findViewById(R.id.iv_photo);
        Chip chip = findViewById(R.id.chip_status);
        TextView dateReturned = findViewById(R.id.tv_date_returned);

        TextInputEditText etName = findViewById(R.id.et_name);
        TextInputEditText etBrand = findViewById(R.id.et_brand);
        TextInputEditText etType = findViewById(R.id.et_type);
        TextInputEditText etPrice = findViewById(R.id.et_price);
        TextInputEditText etBill = findViewById(R.id.et_bill);
        TextInputEditText etNotes = findViewById(R.id.et_notes);

        MaterialButton save = findViewById(R.id.btn_save);
        MaterialButton mark = findViewById(R.id.btn_mark_returned);
        MaterialButton delete = findViewById(R.id.btn_delete);

        photo.setOnClickListener(v -> {
            if (current != null && current.photoPath != null) {
                Intent i = new Intent(this, ImageViewerActivity.class);
                i.putExtra(ImageViewerActivity.EXTRA_IMAGE_PATH, current.photoPath);
                // ClothingItems are not ClosetItems, so we don't pass an ID for the editor
                startActivity(i);
            }
        });

        vm.item(id).observe(this, item -> {
            if (item == null) { finish(); return; }
            current = item;
            Glide.with(photo).load(new File(item.photoPath == null ? "" : item.photoPath))
                .placeholder(R.drawable.ic_photo).into(photo);
            
            // Only set text if not already modified by user to avoid losing cursor position
            if (etName.getText().length() == 0) etName.setText(item.clothingName);
            if (etBrand.getText().length() == 0) etBrand.setText(item.brandName);
            if (etType.getText().length() == 0) etType.setText(item.clothingType);
            if (etPrice.getText().length() == 0) etPrice.setText(item.price == null ? "" : String.valueOf(item.price));
            if (etBill.getText().length() == 0) etBill.setText(item.billNumber);
            if (etNotes.getText().length() == 0) etNotes.setText(item.notes);

            boolean returned = item.status == Status.RETURNED;
            chip.setText(returned ? R.string.status_returned : R.string.status_given);
            chip.setChipBackgroundColorResource(returned ? R.color.chip_returned : R.color.chip_given);
            chip.setTextColor(ContextCompat.getColor(this, returned ? R.color.chip_on_returned : R.color.chip_on_given));
            mark.setEnabled(!returned);
            dateReturned.setVisibility(returned ? View.VISIBLE : View.GONE);
            dateReturned.setText(getString(R.string.returned_on, DateUtils.formatOrDash(item.dateReturned)));
        });

        save.setOnClickListener(v -> {
            if (current == null) return;
            
            // Fix: Create NEW instance to trigger DiffUtil correctly in observers
            ClothingItem updated = new ClothingItem();
            updated.id = current.id;
            updated.batchId = current.batchId;
            updated.photoPath = current.photoPath;
            updated.status = current.status;
            updated.dateReturned = current.dateReturned;
            
            updated.clothingName = txt(etName);
            updated.brandName = txt(etBrand);
            updated.clothingType = txt(etType);
            try {
                String p = txt(etPrice);
                updated.price = (p == null || p.isEmpty()) ? null : Double.parseDouble(p);
            } catch (NumberFormatException e) { updated.price = null; }
            updated.billNumber = txt(etBill);
            updated.notes = txt(etNotes);
            
            vm.update(updated);
            finish();
        });

        mark.setOnClickListener(v ->
            new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.mark_returned)
                .setMessage(R.string.mark_returned_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, (d, w) -> {
                    if (current != null) vm.markReturned(id, current.batchId);
                })
                .show());

        delete.setOnClickListener(v ->
            new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.delete_item)
                .setMessage(R.string.delete_item_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> {
                    if (current != null) { vm.delete(current); finish(); }
                })
                .show());
    }

    private String txt(TextInputEditText et) {
        return et.getText() == null ? null : et.getText().toString();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
