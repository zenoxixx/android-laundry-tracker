package com.example.laundrytracker.ui.add;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrytracker.R;
import com.example.laundrytracker.model.LaundryService;
import com.example.laundrytracker.ui.closet.ClosetActivity;
import com.example.laundrytracker.util.ImageStorage;
import com.example.laundrytracker.util.PermissionUtils;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.MaterialAutoCompleteTextView;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class AddLaundryActivity extends AppCompatActivity {

    private AddLaundryViewModel vm;
    private NewItemAdapter adapter;
    private MaterialAutoCompleteTextView serviceDropdown;
    private List<LaundryService> services = new ArrayList<>();

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
                showSaveToClosetPrompt(pendingCameraPath);
            } else if (pendingCameraPath != null) {
                ImageStorage.deleteQuietly(pendingCameraPath);
            }
            pendingCameraUri = null;
            pendingCameraPath = null;
        });

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMultiple =
        registerForActivityResult(new ActivityResultContracts.PickMultipleVisualMedia(10), uris -> {
            if (uris == null || uris.isEmpty()) return;
            List<String> paths = new ArrayList<>();
            for (Uri uri : uris) {
                try {
                    paths.add(ImageStorage.copyToInternal(this, uri));
                } catch (Exception e) {
                    Toast.makeText(this, R.string.image_failed, Toast.LENGTH_SHORT).show();
                }
            }
            if (!paths.isEmpty()) {
                showSaveMultipleToClosetPrompt(paths);
            }
        });

    private final ActivityResultLauncher<Intent> pickFromCloset =
        registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                List<String> ids = result.getData().getStringArrayListExtra(ClosetActivity.RESULT_SELECTED_IDS);
                if (ids != null) {
                    for (String id : ids) {
                        vm.getClosetItem(id).observe(this, item -> {
                            if (item != null) {
                                vm.addFromClosetSync(this, item);
                            }
                        });
                    }
                }
            }
        });

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_laundry);
        setSupportActionBar(findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        vm = new ViewModelProvider(this).get(AddLaundryViewModel.class);

        RecyclerView rv = findViewById(R.id.rv_items);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NewItemAdapter(pos -> vm.removeAt(pos));
        rv.setAdapter(adapter);
        View empty = findViewById(R.id.empty_items);
        vm.items().observe(this, list -> {
            adapter.submit(list);
            empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        serviceDropdown = findViewById(R.id.dropdown_service);
        vm.services().observe(this, list -> {
            services = list == null ? new ArrayList<>() : list;
            List<String> labels = new ArrayList<>();
            for (LaundryService s : services) labels.add(s.name);
            labels.add(getString(R.string.new_service));
            ArrayAdapter<String> a = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, labels);
            serviceDropdown.setAdapter(a);
        });
        serviceDropdown.setOnItemClickListener((parent, view, position, id) -> {
            if (position == services.size()) {
                serviceDropdown.setText("", false);
                showNewServiceDialog();
            } else {
                LaundryService s = services.get(position);
                vm.selectedServiceId = s.id;
                serviceDropdown.setText(s.name, false);
            }
        });

        TextInputEditText etNotes = findViewById(R.id.et_batch_notes);

        findViewById(R.id.btn_camera).setOnClickListener(v -> {
            if (PermissionUtils.hasCamera(this)) launchCamera();
            else requestCamera.launch(Manifest.permission.CAMERA);
        });

        findViewById(R.id.btn_gallery).setOnClickListener(v ->
            pickMultiple.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE).build()));

        findViewById(R.id.btn_closet).setOnClickListener(v -> {
            Intent i = new Intent(this, ClosetActivity.class);
            i.putExtra(ClosetActivity.EXTRA_PICK_MODE, true);
            pickFromCloset.launch(i);
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            vm.notes = etNotes.getText() == null ? null : etNotes.getText().toString();
            if (vm.save(this)) finish();
            else Toast.makeText(this, R.string.add_at_least_one_photo, Toast.LENGTH_SHORT).show();
        });
    }

    private void launchCamera() {
        File f = ImageStorage.newPhotoFile(this);
        pendingCameraPath = f.getAbsolutePath();
        pendingCameraUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", f);
        takePicture.launch(pendingCameraUri);
    }

    private void showSaveToClosetPrompt(String path) {
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_to_closet)
            .setMessage(R.string.save_to_closet_prompt)
            .setPositiveButton(R.string.save_to_closet_yes, (d, w) -> vm.addPhoto(path, true))
            .setNegativeButton(R.string.save_to_closet_no, (d, w) -> vm.addPhoto(path, false))
            .show();
    }

    private void showSaveMultipleToClosetPrompt(List<String> paths) {
        String msg = getResources().getQuantityString(R.plurals.items_count, paths.size(), paths.size()) + " " + getString(R.string.save_to_closet_prompt);
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_to_closet)
            .setMessage(msg)
            .setPositiveButton(R.string.save_to_closet_yes, (d, w) -> vm.addPhotos(paths, true))
            .setNegativeButton(R.string.save_to_closet_no, (d, w) -> vm.addPhotos(paths, false))
            .show();
    }

    private void showNewServiceDialog() {
        View form = LayoutInflater.from(this).inflate(R.layout.dialog_edit_service, null);
        TextInputEditText name = form.findViewById(R.id.et_name);
        TextInputEditText manager = form.findViewById(R.id.et_manager);
        TextInputEditText phone = form.findViewById(R.id.et_phone);
        TextInputEditText address = form.findViewById(R.id.et_address);
        TextInputEditText notes = form.findViewById(R.id.et_notes);
        new MaterialAlertDialogBuilder(this)
            .setTitle(R.string.add_service)
            .setView(form)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save, (d, w) -> {
                String n = name.getText() == null ? "" : name.getText().toString().trim();
                if (n.isEmpty()) return;
                LaundryService s = new LaundryService();
                s.name = n;
                s.managerName = manager.getText() == null ? null : manager.getText().toString();
                s.phone = phone.getText() == null ? null : phone.getText().toString();
                s.address = address.getText() == null ? null : address.getText().toString();
                s.notes = notes.getText() == null ? null : notes.getText().toString();
                vm.createService(s);
                vm.selectedServiceId = s.id;
                serviceDropdown.setText(s.name, false);
            })
            .show();
    }

    @Override public boolean onSupportNavigateUp() { finish(); return true; }
}
