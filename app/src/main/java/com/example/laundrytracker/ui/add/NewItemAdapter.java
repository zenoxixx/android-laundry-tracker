package com.example.laundrytracker.ui.add;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.laundrytracker.R;

import java.io.File;
import java.util.List;

public class NewItemAdapter extends RecyclerView.Adapter<NewItemAdapter.VH> {

    public interface Listener { void onRemove(int position); }

    private List<PendingItem> data;
    private final Listener listener;

    public NewItemAdapter(Listener l) { this.listener = l; }

    public void submit(List<PendingItem> list) {
        this.data = list;
        notifyDataSetChanged();
    }

    @NonNull @Override public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_new_clothing, parent, false);
        return new VH(v);
    }

    @Override public void onBindViewHolder(@NonNull VH h, int position) {
        PendingItem p = data.get(position);
        Glide.with(h.thumb).load(new File(p.item.photoPath)).placeholder(R.drawable.ic_photo).into(h.thumb);
        h.bind(p);
        h.remove.setOnClickListener(v -> {
            int pos = h.getBindingAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) listener.onRemove(pos);
        });
    }

    @Override public int getItemCount() { return data == null ? 0 : data.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView thumb;
        final ImageButton remove;
        final EditText name, brand, type, price, bill, notes;
        PendingItem current;

        VH(View v) {
            super(v);
            thumb = v.findViewById(R.id.iv_photo);
            remove = v.findViewById(R.id.btn_remove);
            name = v.findViewById(R.id.et_name);
            brand = v.findViewById(R.id.et_brand);
            type = v.findViewById(R.id.et_type);
            price = v.findViewById(R.id.et_price);
            bill = v.findViewById(R.id.et_bill);
            notes = v.findViewById(R.id.et_notes);
            attach(name, s -> current.item.clothingName = s);
            attach(brand, s -> current.item.brandName = s);
            attach(type, s -> current.item.clothingType = s);
            attach(price, s -> {
                try { current.item.price = s.isEmpty() ? null : Double.parseDouble(s); }
                catch (NumberFormatException e) { current.item.price = null; }
            });
            attach(bill, s -> current.item.billNumber = s);
            attach(notes, s -> current.item.notes = s);
        }

        void bind(PendingItem p) {
            this.current = p;
            name.setText(p.item.clothingName);
            brand.setText(p.item.brandName);
            type.setText(p.item.clothingType);
            price.setText(p.item.price == null ? "" : String.valueOf(p.item.price));
            bill.setText(p.item.billNumber);
            notes.setText(p.item.notes);
        }

        interface Setter { void set(String s); }

        void attach(EditText et, Setter setter) {
            et.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
                @Override public void afterTextChanged(Editable s) {
                    if (current != null) setter.set(s.toString());
                }
            });
        }
    }
}
