package com.example.laundrytracker.ui.viewer;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.example.laundrytracker.R;
import com.example.laundrytracker.model.ClosetItem;
import com.github.chrisbanes.photoview.PhotoView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;

public class ImageViewerActivity extends AppCompatActivity {
    public static final String EXTRA_IMAGE_PATH = "image_path";
    public static final String EXTRA_CLOSET_ITEM_ID = "closet_item_id";

    private ImageViewerViewModel vm;
    private ClosetItem currentItem;
    private BottomSheetBehavior<View> sheetBehavior;
    private PhotoView photoView;
    private View dragHandleHint;

    private TextInputEditText etName, etBrand, etType, etPrice, etBill, etNotes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_viewer);

        photoView = findViewById(R.id.photo_view);
        dragHandleHint = findViewById(R.id.drag_handle_hint);
        findViewById(R.id.btn_close).setOnClickListener(v -> finish());

        String path = getIntent().getStringExtra(EXTRA_IMAGE_PATH);
        String itemId = getIntent().getStringExtra(EXTRA_CLOSET_ITEM_ID);

        if (path != null) {
            Glide.with(this).load(new File(path)).into(photoView);
        } else {
            finish();
            return;
        }

        setupBottomSheet();
        
        if (itemId != null) {
            setupGestures();
            animateHintIn();
            vm = new ViewModelProvider(this).get(ImageViewerViewModel.class);
            vm.getClosetItem(itemId).observe(this, item -> {
                if (item != null) {
                    currentItem = item;
                    if (isSheetClosed()) {
                        populateFields(item);
                    }
                }
            });

            getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    if (!isSheetClosed()) {
                        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    } else {
                        setEnabled(false);
                        getOnBackPressedDispatcher().onBackPressed();
                    }
                }
            });
        } else {
            findViewById(R.id.bottom_sheet).setVisibility(View.GONE);
            dragHandleHint.setVisibility(View.GONE);
        }
    }

    private void animateHintIn() {
        dragHandleHint.setTranslationY(50);
        dragHandleHint.setAlpha(0f);
        dragHandleHint.animate()
                .alpha(1f)
                .translationY(0)
                .setDuration(800)
                .setStartDelay(300)
                .setInterpolator(new OvershootInterpolator())
                .start();
    }

    private void setupBottomSheet() {
        View bottomSheet = findViewById(R.id.bottom_sheet);
        sheetBehavior = BottomSheetBehavior.from(bottomSheet);
        
        sheetBehavior.setPeekHeight(0);
        sheetBehavior.setHideable(true);
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        sheetBehavior.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                
                // Toggle handle visibility based on sheet state
                if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                    dragHandleHint.animate().alpha(1f).setDuration(200).start();
                } else if (newState == BottomSheetBehavior.STATE_DRAGGING || newState == BottomSheetBehavior.STATE_EXPANDED) {
                    dragHandleHint.animate().alpha(0f).setDuration(200).start();
                }
            }
            @Override public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                // Smoothly fade out handle as the sheet slides up
                dragHandleHint.setAlpha(1f - Math.min(1f, slideOffset * 2f));
            }
        });

        etName = findViewById(R.id.et_name);
        etBrand = findViewById(R.id.et_brand);
        etType = findViewById(R.id.et_type);
        etPrice = findViewById(R.id.et_price);
        etBill = findViewById(R.id.et_bill);
        etNotes = findViewById(R.id.et_notes);

        findViewById(R.id.btn_save).setOnClickListener(v -> saveChanges());
    }

    private boolean isSheetClosed() {
        int state = sheetBehavior.getState();
        return state == BottomSheetBehavior.STATE_HIDDEN || state == BottomSheetBehavior.STATE_COLLAPSED;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setupGestures() {
        GestureDetector detector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onDown(MotionEvent e) {
                return true; 
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (!isSheetClosed()) {
                    sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    return true;
                }
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (e1 == null || e2 == null) return false;
                if (photoView.getScale() > 1.05f) return false;

                float deltaY = e1.getY() - e2.getY();
                float deltaX = e1.getX() - e2.getX();

                if (Math.abs(deltaY) > Math.abs(deltaX)) {
                    if (deltaY > 80 && isSheetClosed()) {
                        populateFields(currentItem);
                        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        return true;
                    } else if (deltaY < -80 && !isSheetClosed()) {
                        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        return true;
                    }
                }
                return false;
            }

            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                if (photoView.getScale() > 1.05f) return false;
                
                if (Math.abs(velocityY) > Math.abs(velocityX)) {
                    if (velocityY < -300 && isSheetClosed()) {
                        populateFields(currentItem);
                        sheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                        return true;
                    } else if (velocityY > 300 && !isSheetClosed()) {
                        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        return true;
                    }
                }
                return false;
            }
        });

        photoView.setOnTouchListener((v, event) -> {
            boolean detectorHandled = detector.onTouchEvent(event);
            if (!isSheetClosed()) {
                return true; 
            }
            if (event.getAction() == MotionEvent.ACTION_UP) {
                v.performClick();
            }
            return detectorHandled;
        });
    }

    private void populateFields(ClosetItem item) {
        if (item == null) return;
        etName.setText(item.clothingName != null ? item.clothingName : "");
        etBrand.setText(item.brandName != null ? item.brandName : "");
        etType.setText(item.clothingType != null ? item.clothingType : "");
        etPrice.setText(item.price != null ? String.valueOf(item.price) : "");
        etBill.setText(item.billNumber != null ? item.billNumber : "");
        etNotes.setText(item.notes != null ? item.notes : "");
    }

    private void saveChanges() {
        if (currentItem == null) return;

        ClosetItem updated = new ClosetItem();
        updated.id = currentItem.id;
        updated.photoPath = currentItem.photoPath;
        updated.clothingName = etName.getText().toString();
        updated.brandName = etBrand.getText().toString();
        updated.clothingType = etType.getText().toString();
        try {
            String p = etPrice.getText().toString();
            updated.price = (p == null || p.isEmpty()) ? null : Double.parseDouble(p);
        } catch (Exception e) {
            updated.price = null;
        }
        updated.billNumber = etBill.getText().toString();
        updated.notes = etNotes.getText().toString();

        vm.save(updated);
        Toast.makeText(this, R.string.saved, Toast.LENGTH_SHORT).show();
        sheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }
}
