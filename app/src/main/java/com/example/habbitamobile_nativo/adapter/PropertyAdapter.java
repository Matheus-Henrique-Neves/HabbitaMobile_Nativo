package com.example.habbitamobile_nativo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.habbitamobile_nativo.R;
import com.example.habbitamobile_nativo.model.Property;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private final List<Property> properties;

    public PropertyAdapter(List<Property> properties) {
        this.properties = properties;
    }

    @NonNull
    @Override
    public PropertyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_property, parent, false);
        return new PropertyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PropertyViewHolder holder, int position) {
        holder.bind(properties.get(position));
    }

    @Override
    public int getItemCount() {
        return properties.size();
    }

    static class PropertyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProperty;
        private final TextView txtTitle;
        private final TextView txtPrice;
        private final TextView txtBedrooms;
        private final TextView txtBathrooms;
        private final TextView txtGarages;
        private final TextView txtAddress;

        PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProperty = itemView.findViewById(R.id.imgProperty);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtBedrooms = itemView.findViewById(R.id.txtBedrooms);
            txtBathrooms = itemView.findViewById(R.id.txtBathrooms);
            txtGarages = itemView.findViewById(R.id.txtGarages);
            txtAddress = itemView.findViewById(R.id.txtAddress);
        }

        void bind(Property property) {
            txtTitle.setText(property.getTitle());
            txtPrice.setText(formatarPreco(property.getPrice()));
            txtBedrooms.setText(property.getBedrooms() + " quartos");
            txtBathrooms.setText(property.getBathrooms() + " banheiros");
            txtGarages.setText(property.getGarages() + " vagas");
            txtAddress.setText(property.getAddress());

            if (property.getImageUrl() != null && !property.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(property.getImageUrl())
                        .centerCrop()
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_gallery)
                        .into(imgProperty);
            } else {
                imgProperty.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        }

        private String formatarPreco(double price) {
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
            return "R$ " + nf.format((long) price);
        }
    }
}
