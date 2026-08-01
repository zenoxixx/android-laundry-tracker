package com.example.laundrytracker.ui.batch;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.laundrytracker.R;
import com.example.laundrytracker.model.ClothingItem;
import com.example.laundrytracker.model.Status;
import com.google.android.material.chip.Chip;

import java.io.File;

public class ItemAdapter extends ListAdapter<ClothingItem, ItemAdapter.VH> {

    public interface Listener {
        void onClick(ClothingItem item);
        void onStatusChange(ClothingItem item, boolean isReturned);
        void onImageClick(ClothingItem item);
    }

    private final Listener listener;

    public ItemAdapter(Listener l) {
        super(new DiffUtil.ItemCallback<ClothingItem>() {
            @Override public boolean areItemsTheSame(@NonNull ClothingItem a, @NonNull ClothingItem b) { return a.id.equals(b.id); }
            @Override public boolean areContentsTheSame(@NonNull ClothingItem a, @NonNull ClothingItem b) {
                return a.status == b.status 
                    && java.util.Objects.equals(a.clothingName, b.clothingName)
                    && java.util.Objects.equals(a.brandName, b.brandName)
                    && java.util.Objects.equals(a.clothingType, b.clothingType)
                    && java.util.Objects.equals(a.photoPath, b.photoPath)
                    && java.util.Objects.equals(a.price, b.price)
                    && java.util.Objects.equals(a.billNumber, b.billNumber)
                    && java.util.Objects.equals(a.notes, b.notes);
            }
        });
        listener = l;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_clothing, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        ClothingItem it = getItem(position);
        Glide.with(h.img).load(new File(it.photoPath == null ? "" : it.photoPath))
            .placeholder(R.drawable.ic_photo).into(h.img);
        
        String label = it.clothingName;
        if (label == null || label.isEmpty()) label = it.clothingType;
        if (label == null || label.isEmpty()) label = h.itemView.getContext().getString(R.string.unnamed_item);
        h.name.setText(label);
        
        boolean returned = it.status == Status.RETURNED;
        h.chip.setText(returned ? R.string.status_returned : R.string.status_given);
        h.chip.setChipBackgroundColorResource(returned ? R.color.chip_returned : R.color.chip_given);
        h.chip.setTextColor(ContextCompat.getColor(h.itemView.getContext(), returned ? R.color.chip_on_returned : R.color.chip_on_given));
        
        h.cb.setOnCheckedChangeListener(null);
        h.cb.setChecked(returned);
        h.cb.setOnCheckedChangeListener((v, checked) -> listener.onStatusChange(it, checked));
        
        h.itemView.setAlpha(returned ? 0.6f : 1.0f);
        
        h.img.setOnClickListener(v -> listener.onImageClick(it));
        h.itemView.setOnClickListener(v -> listener.onClick(it));
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView img;
        final TextView name;
        final Chip chip;
        final CheckBox cb;
        VH(View v) {
            super(v);
            img = v.findViewById(R.id.iv_photo);
            name = v.findViewById(R.id.tv_name);
            chip = v.findViewById(R.id.chip_status);
            cb = v.findViewById(R.id.cb_returned);
        }
    }
}
