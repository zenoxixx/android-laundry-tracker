package com.example.laundrytracker.ui.closet;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrytracker.R;
import com.example.laundrytracker.model.ClosetItem;
import com.example.laundrytracker.ui.viewer.ImageViewerActivity;
import com.example.laundrytracker.util.ImageStorage;
import com.example.laundrytracker.util.PermissionUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ClosetActivity extends AppCompatActivity {

    public static final String EXTRA_PICK_MODE = "pick_mode";
    public static final String RESULT_SELECTED_IDS = "selected_ids";

    private ClosetViewModel vm;
    private ClosetAdapter adapter;
    private boolean pickMode;

    private Uri pendingCameraUri;
    private String pendingCameraPath;

    private final ActivityResultLauncher<String> requestCamera =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
            if (granted) launchCamera();
            else Toast.makeText(this, R.string.camera_denied, Toast.LENGTH_SHORT).show();
        });

    private final ActivityResultLauncher<Uri> takePicture =
        registerForActivityResult(new ActivityResultContracts.TakePicture(), success -> {
            if (success != null && success && pendingCameraPath != null) {
                showEditDialog(pendingCameraPath, null);
            } else if (pendingCameraPath != null) {
                ImageStorage.deleteQuietly(pendingCameraPath);
            }
            pendingCameraUri = null;
            pendingCameraPath = null;
        });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickImage =
        registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri == null) return;
            try {
                String path = ImageStorage.copyToInternal(this, uri);
                showEditDialog(path, null);
            } catch (Exception e) {
                Toast.makeText(this, R.string.image_failed, Toast.LENGTH_SHORT).show();
            }
        });

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_closet);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        pickMode = getIntent().getBooleanExtra(EXTRA_PICK_MODE, false);
        if (pickMode) {
            setTitle(R.string.select_items);
        }

        vm = new ViewModelProvider(this).get(ClosetViewModel.class);

        RecyclerView rv = findViewById(R.id.rv_closet);
        rv.setLayoutManager(new GridLayoutManager(this, 2));
        adapter = new ClosetAdapter(new ClosetAdapter.Listener() {
            @Override public void onClick(ClosetItem item) {
                if (!pickMode) showEditDialog(item.photoPath, item);
            }
            @Override public void onLongClick(ClosetItem item) {
                if (!pickMode) {
                    new MaterialAlertDialogBuilder(ClosetActivity.this)
                        .setTitle(R.string.delete_closet_item)
                        .setMessage(R.string.delete_closet_item_msg)
                        .setNegativeButton(R.string.cancel, null)
                        .setPositiveButton(R.string.delete, (d, w) -> vm.delete(item))
                        .show();
                }
            }
            @Override public void onImageClick(ClosetItem item) {
                Intent i = new Intent(ClosetActivity.this, ImageViewerActivity.class);
                i.putExtra(ImageViewerActivity.EXTRA_IMAGE_PATH, item.photoPath);
                i.putExtra(ImageViewerActivity.EXTRA_CLOSET_ITEM_ID, item.id);
                startActivity(i);
            }
        }, pickMode);
        rv.setAdapter(adapter);

        View empty = findViewById(R.id.empty_view);
        vm.items().observe(this, list -> {
            adapter.submitList(list);
            empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        ExtendedFloatingActionButton fab = findViewById(R.id.fab_add);
        if (pickMode) {
            fab.setText(R.string.confirm);
            fab.setIconResource(R.drawable.ic_confirm);
            fab.setOnClickListener(v -> {
                Intent data = new Intent();
                data.putStringArrayListExtra(RESULT_SELECTED_IDS, new ArrayList<>(adapter.getSelectedIds()));
                setResult(RESULT_OK, data);
                finish();
            });
        } else {
            fab.setOnClickListener(v -> {
                String[] options = {getString(R.string.take_photo), getString(R.string.pick_gallery)};
                new MaterialAlertDialogBuilder(this)
                    .setItems(options, (dialog, which) -> {
                        if (which == 0) {
                            if (PermissionUtils.hasCamera(this)) launchCamera();
                            else requestCamera.launch(Manifest.permission.CAMERA);
                        } else {
                            pickImage.launch(new PickVisualMediaRequest.Builder()
                                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build());
                        }
                    }).show();
            });
        }
    }

    private void launchCamera() {
        File f = ImageStorage.newPhotoFile(this);
        pendingCameraPath = f.getAbsolutePath();
        pendingCameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", f);
        takePicture.launch(pendingCameraUri);
    }

    private void showEditDialog(String photoPath, ClosetItem existing) {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_edit_closet_item, null);
        TextInputEditText name = form.findViewById(R.id.et_name);
        TextInputEditText brand = form.findViewById(R.id.et_brand);
        TextInputEditText type = form.findViewById(R.id.et_type);
        TextInputEditText price = form.findViewById(R.id.et_price);
        TextInputEditText bill = form.findViewById(R.id.et_bill);
        TextInputEditText notes = form.findViewById(R.id.et_notes);

        if (existing != null) {
            name.setText(existing.clothingName);
            brand.setText(existing.brandName);
            type.setText(existing.clothingType);
            price.setText(existing.price == null ? "" : String.valueOf(existing.price));
            bill.setText(existing.billNumber);
            notes.setText(existing.notes);
        }

        new MaterialAlertDialogBuilder(this)
            .setTitle(existing == null ? R.string.add_to_closet : R.string.edit_closet_item)
            .setView(form)
            .setNegativeButton(R.string.cancel, (d, w) -> {
                if (existing == null) ImageStorage.deleteQuietly(photoPath);
            })
            .setPositiveButton(R.string.save, (d, w) -> {
                // IMPORTANT: Create a NEW instance instead of mutating 'existing'.
                // This ensures DiffUtil in the adapter can detect the change.
                ClosetItem item = new ClosetItem();
                if (existing != null) {
                    item.id = existing.id;
                }
                item.photoPath = photoPath;
                item.clothingName = name.getText().toString();
                item.brandName = brand.getText().toString();
                item.clothingType = type.getText().toString();
                try {
                    String p = price.getText().toString();
                    item.price = p.isEmpty() ? null : Double.parseDouble(p);
                } catch (Exception e) { item.price = null; }
                item.billNumber = bill.getText().toString();
                item.notes = notes.getText().toString();
                vm.save(item);
            })
            .show();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
