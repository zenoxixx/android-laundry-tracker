package com.example.laundrytracker.ui.home;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrytracker.R;
import com.example.laundrytracker.ui.add.AddLaundryActivity;
import com.example.laundrytracker.ui.batch.BatchDetailsActivity;
import com.example.laundrytracker.ui.closet.ClosetActivity;
import com.example.laundrytracker.ui.services.LaundryServicesActivity;
import com.example.laundrytracker.util.BackupManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

    private HomeViewModel vm;
    private BatchAdapter adapter;
    private View empty;

    private final ActivityResultLauncher<String> createBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.CreateDocument("application/zip"),
            uri -> {
                if (uri != null) performBackup(uri);
            }
    );

    private final ActivityResultLauncher<String[]> restoreBackupLauncher = registerForActivityResult(
            new ActivityResultContracts.OpenDocument(),
            uri -> {
                if (uri != null) showRestoreChoiceDialog(uri);
            }
    );

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setSupportActionBar(findViewById(R.id.toolbar));

        vm = new ViewModelProvider(this).get(HomeViewModel.class);

        RecyclerView rv = findViewById(R.id.rv_batches);
        empty = findViewById(R.id.empty_view);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new BatchAdapter(new BatchAdapter.Listener() {
            @Override public void onClick(com.example.laundrytracker.model.BatchWithItems b) {
                Intent i = new Intent(HomeActivity.this, BatchDetailsActivity.class);
                i.putExtra(BatchDetailsActivity.EXTRA_BATCH_ID, b.batch.id);
                startActivity(i);
            }
            @Override public void onLongClick(com.example.laundrytracker.model.BatchWithItems b) {
                new MaterialAlertDialogBuilder(HomeActivity.this)
                    .setTitle(R.string.delete_batch)
                    .setMessage(R.string.delete_batch_msg)
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete, (d, w) -> vm.deleteBatch(b))
                    .show();
            }
        });
        rv.setAdapter(adapter);

        vm.batches().observe(this, list -> {
            adapter.submitList(list);
            empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
        });

        ExtendedFloatingActionButton fab = findViewById(R.id.fab_add);
        fab.setOnClickListener(v -> startActivity(new Intent(this, AddLaundryActivity.class)));
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_home, menu);
        MenuItem search = menu.findItem(R.id.action_search);
        SearchView sv = (SearchView) search.getActionView();
        if (sv != null) {
            sv.setQueryHint(getString(R.string.search_hint));
            sv.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override public boolean onQueryTextSubmit(String q) { vm.setQuery(q); return true; }
                @Override public boolean onQueryTextChange(String q) { vm.setQuery(q); return true; }
            });
        }
        return true;
    }

    @Override public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_services) {
            startActivity(new Intent(this, LaundryServicesActivity.class));
            return true;
        } else if (id == R.id.action_closet) {
            startActivity(new Intent(this, ClosetActivity.class));
            return true;
        } else if (id == R.id.action_backup) {
            String date = new SimpleDateFormat("yyyy-MM-dd", Locale.US).format(new Date());
            createBackupLauncher.launch("laundrytracker_backup_" + date + ".zip");
            return true;
        } else if (id == R.id.action_restore) {
            restoreBackupLauncher.launch(new String[]{"application/zip"});
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void performBackup(Uri uri) {
        new Thread(() -> {
            try {
                BackupManager.createBackup(this, uri);
                runOnUiThread(() -> Toast.makeText(this, R.string.backup_success, Toast.LENGTH_LONG).show());
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, R.string.backup_failed, Toast.LENGTH_LONG).show());
            }
        }).start();
    }

    private void showRestoreChoiceDialog(Uri uri) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.restore_confirm_title)
                .setMessage(R.string.restore_confirm_msg)
                .setNeutralButton(R.string.cancel, null)
                .setPositiveButton(R.string.restore_action_merge, (d, w) -> performRestore(uri, true))
                .setNegativeButton(R.string.restore_action_replace, (d, w) -> confirmReplace(uri))
                .show();
    }

    private void confirmReplace(Uri uri) {
        new MaterialAlertDialogBuilder(this)
                .setTitle(R.string.restore_replace_confirm_title)
                .setMessage(R.string.restore_replace_confirm_msg)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.delete, (d, w) -> performRestore(uri, false))
                .show();
    }

    private void performRestore(Uri uri, boolean merge) {
        new Thread(() -> {
            try {
                BackupManager.restoreBackup(this, uri, merge);
                runOnUiThread(() -> {
                    if (merge) {
                        Toast.makeText(this, R.string.restore_success, Toast.LENGTH_LONG).show();
                        // ViewModels should refresh automatically as they observe LiveData from the DB
                    } else {
                        Toast.makeText(this, R.string.restore_success_restart, Toast.LENGTH_LONG).show();
                        // Restart app for clean state after full replacement
                        Intent intent = getPackageManager().getLaunchIntentForPackage(getPackageName());
                        if (intent != null) {
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                        }
                        System.exit(0);
                    }
                });
            } catch (Exception e) {
                runOnUiThread(() -> {
                    String msg = getString(R.string.restore_failed, e.getMessage());
                    Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
}
