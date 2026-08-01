package com.example.laundrytracker.ui.closet;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.laundrytracker.R;
import com.example.laundrytracker.model.ClosetItem;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

public class ClosetAdapter extends ListAdapter<ClosetItem, ClosetAdapter.VH> {

    public interface Listener {
        void onClick(ClosetItem item);
        void onLongClick(ClosetItem item);
        void onImageClick(ClosetItem item);
    }

    private final Listener listener;
    private final boolean selectionMode;
    private final Set<String> selectedIds = new HashSet<>();

    public ClosetAdapter(Listener l, boolean selectionMode) {
        super(new DiffUtil.ItemCallback<ClosetItem>() {
            @Override public boolean areItemsTheSame(@NonNull ClosetItem a, @NonNull ClosetItem b) { return a.id.equals(b.id); }
            @Override public boolean areContentsTheSame(@NonNull ClosetItem a, @NonNull ClosetItem b) {
                return java.util.Objects.equals(a.clothingName, b.clothingName)
                    && java.util.Objects.equals(a.photoPath, b.photoPath)
                    && java.util.Objects.equals(a.brandName, b.brandName)
                    && java.util.Objects.equals(a.clothingType, b.clothingType)
                    && java.util.Objects.equals(a.price, b.price)
                    && java.util.Objects.equals(a.billNumber, b.billNumber)
                    && java.util.Objects.equals(a.notes, b.notes);
            }
        });
        this.listener = l;
        this.selectionMode = selectionMode;
    }

    public void toggleSelection(String id) {
        if (selectedIds.contains(id)) selectedIds.remove(id);
        else selectedIds.add(id);
        notifyItemChanged(getItemPosition(id));
    }

    private int getItemPosition(String id) {
        for (int i = 0; i < getItemCount(); i++) {
            if (getItem(i).id.equals(id)) return i;
        }
        return -1;
    }

    public Set<String> getSelectedIds() {
        return selectedIds;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_closet, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        ClosetItem it = getItem(position);
        Glide.with(h.img).load(new File(it.photoPath == null ? "" : it.photoPath))
            .placeholder(R.drawable.ic_photo).into(h.img);
        
        String name = it.clothingName;
        if (name == null || name.isEmpty()) name = h.itemView.getContext().getString(R.string.unnamed_item);
        h.name.setText(name);
        h.type.setText(it.clothingType);
        h.type.setVisibility(it.clothingType == null || it.clothingType.isEmpty() ? View.GONE : View.VISIBLE);

        boolean selected = selectedIds.contains(it.id);
        h.overlay.setVisibility(selected ? View.VISIBLE : View.GONE);
        h.check.setVisibility(selected ? View.VISIBLE : View.GONE);

        h.img.setOnClickListener(v -> listener.onImageClick(it));
        h.itemView.setOnClickListener(v -> {
            if (selectionMode) {
                toggleSelection(it.id);
            }
            listener.onClick(it);
        });
        h.itemView.setOnLongClickListener(v -> {
            listener.onLongClick(it);
            return true;
        });
    }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView img, check;
        final TextView name, type;
        final View overlay;
        VH(View v) {
            super(v);
            img = v.findViewById(R.id.iv_photo);
            name = v.findViewById(R.id.tv_name);
            type = v.findViewById(R.id.tv_type);
            overlay = v.findViewById(R.id.selection_overlay);
            check = v.findViewById(R.id.iv_selected);
        }
    }
}
