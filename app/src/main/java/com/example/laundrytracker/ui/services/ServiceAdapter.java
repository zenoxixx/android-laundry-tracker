package com.example.laundrytracker.ui.services;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrytracker.R;
import com.example.laundrytracker.model.LaundryService;

public class ServiceAdapter extends ListAdapter<LaundryService, ServiceAdapter.VH> {

    public interface Listener {
        void onEdit(LaundryService s);
        void onDelete(LaundryService s);
    }

    private final Listener listener;

    public ServiceAdapter(Listener l) {
        super(new DiffUtil.ItemCallback<LaundryService>() {
            @Override public boolean areItemsTheSame(@NonNull LaundryService a, @NonNull LaundryService b) { return a.id.equals(b.id); }
            @Override public boolean areContentsTheSame(@NonNull LaundryService a, @NonNull LaundryService b) {
                return java.util.Objects.equals(a.name, b.name)
                    && java.util.Objects.equals(a.managerName, b.managerName)
                    && java.util.Objects.equals(a.phone, b.phone)
                    && java.util.Objects.equals(a.address, b.address)
                    && java.util.Objects.equals(a.notes, b.notes);
            }
        });
        listener = l;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_service, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        LaundryService s = getItem(position);
        h.name.setText(s.name);
        StringBuilder sb = new StringBuilder();
        if (s.managerName != null && !s.managerName.isEmpty()) sb.append(s.managerName);
        if (s.phone != null && !s.phone.isEmpty()) { if (sb.length() > 0) sb.append(" • "); sb.append(s.phone); }
        h.sub.setText(sb.toString());
        h.sub.setVisibility(sb.length() == 0 ? View.GONE : View.VISIBLE);
        h.itemView.setOnClickListener(v -> listener.onEdit(s));
        h.itemView.setOnLongClickListener(v -> { listener.onDelete(s); return true; });
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView name, sub;
        VH(View v) { super(v); name = v.findViewById(R.id.tv_name); sub = v.findViewById(R.id.tv_sub); }
    }
}
