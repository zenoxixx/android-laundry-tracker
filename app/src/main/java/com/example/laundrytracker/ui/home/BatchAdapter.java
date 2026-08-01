package com.example.laundrytracker.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.laundrytracker.R;
import com.example.laundrytracker.model.BatchWithItems;
import com.example.laundrytracker.model.Status;
import com.example.laundrytracker.util.DateUtils;
import com.google.android.material.chip.Chip;

public class BatchAdapter extends ListAdapter<BatchWithItems, BatchAdapter.VH> {

    public interface Listener {
        void onClick(BatchWithItems b);
        void onLongClick(BatchWithItems b);
    }

    private final Listener listener;

    public BatchAdapter(Listener l) {
        super(new DiffUtil.ItemCallback<BatchWithItems>() {
            @Override public boolean areItemsTheSame(@NonNull BatchWithItems a, @NonNull BatchWithItems b) {
                return a.batch.id.equals(b.batch.id);
            }
            @Override public boolean areContentsTheSame(@NonNull BatchWithItems a, @NonNull BatchWithItems b) {
                return a.batch.status == b.batch.status
                    && a.batch.dateGiven == b.batch.dateGiven
                    && a.items.size() == b.items.size()
                    && java.util.Objects.equals(
                        a.service == null ? null : a.service.name,
                        b.service == null ? null : b.service.name);
            }
        });
        this.listener = l;
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_batch, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        BatchWithItems b = getItem(position);
        h.date.setText(DateUtils.format(b.batch.dateGiven));
        h.service.setText(b.service != null && b.service.name != null ? b.service.name : h.itemView.getContext().getString(R.string.no_service));
        int count = b.items == null ? 0 : b.items.size();
        h.count.setText(h.itemView.getResources().getQuantityString(R.plurals.items_count, count, count));
        
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
        
        h.chip.setText(textRes);
        h.chip.setChipBackgroundColorResource(bgRes);
        h.chip.setTextColor(ContextCompat.getColor(h.itemView.getContext(), fgRes));

        h.itemView.setOnClickListener(v -> listener.onClick(b));
        h.itemView.setOnLongClickListener(v -> { listener.onLongClick(b); return true; });
    }

    static class VH extends RecyclerView.ViewHolder {
        final TextView date, service, count;
        final Chip chip;
        VH(View v) {
            super(v);
            date = v.findViewById(R.id.tv_date);
            service = v.findViewById(R.id.tv_service);
            count = v.findViewById(R.id.tv_count);
            chip = v.findViewById(R.id.chip_status);
        }
    }
}
