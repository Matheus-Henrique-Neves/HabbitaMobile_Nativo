package com.example.habbitamobile_nativo.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.habbitamobile_nativo.R;
import com.example.habbitamobile_nativo.model.Property;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

public class PropertyAdapter extends RecyclerView.Adapter<PropertyAdapter.PropertyViewHolder> {

    private final ArrayList<Property> properties;
    private final HashSet<String> favoritados;
    private final OnFavoritoToggleListener favoritoListener;

    public interface OnFavoritoToggleListener {
        void onToggle(String propertyId, boolean agoraFavoritado);
    }

    public interface OnPropertyClickListener {
        void onClick(Property property);
    }

    public interface OnPropertyLongClickListener {
        void onLongClick(Property property);
    }

    private OnPropertyClickListener clickListener;
    private OnPropertyLongClickListener longClickListener;

    public PropertyAdapter(List<Property> properties) {
        this.properties = new ArrayList<>(properties);
        this.favoritados = null;
        this.favoritoListener = null;
    }

    public PropertyAdapter(List<Property> properties, HashSet<String> favoritados, OnFavoritoToggleListener listener) {
        this.properties = new ArrayList<>(properties);
        this.favoritados = favoritados;
        this.favoritoListener = listener;
    }

    public void setOnPropertyClickListener(OnPropertyClickListener listener) {
        this.clickListener = listener;
    }

    public void setOnPropertyLongClickListener(OnPropertyLongClickListener listener) {
        this.longClickListener = listener;
    }

    public void adicionarItens(List<Property> novos) {
        int inicio = properties.size();
        properties.addAll(novos);
        notifyItemRangeInserted(inicio, novos.size());
    }

    public void removerItem(String propertyId) {
        for (int i = 0; i < properties.size(); i++) {
            if (properties.get(i).getId().equals(propertyId)) {
                properties.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
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

    class PropertyViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgProperty;
        private final ImageButton btnFavorito;
        private final TextView txtTitle;
        private final TextView txtPrice;
        private final TextView txtBedrooms;
        private final TextView txtBathrooms;
        private final TextView txtGarages;
        private final TextView txtAddress;
        private final TextView txtBadgeTipo;

        PropertyViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProperty = itemView.findViewById(R.id.imgProperty);
            btnFavorito = itemView.findViewById(R.id.btnFavorito);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtBedrooms = itemView.findViewById(R.id.txtBedrooms);
            txtBathrooms = itemView.findViewById(R.id.txtBathrooms);
            txtGarages = itemView.findViewById(R.id.txtGarages);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtBadgeTipo = itemView.findViewById(R.id.txtBadgeTipo);
        }

        void bind(Property property) {
            itemView.setOnClickListener(v -> {
                if (clickListener != null) clickListener.onClick(property);
            });

            itemView.setOnLongClickListener(v -> {
                if (longClickListener != null) {
                    longClickListener.onLongClick(property);
                    return true;
                }
                return false;
            });

            txtTitle.setText(property.getTitle());
            txtPrice.setText(formatarPreco(property.getPrice()));
            txtBedrooms.setText(property.getBedrooms() + " quartos");
            txtBathrooms.setText(property.getBathrooms() + " banheiros");
            txtGarages.setText(property.getGarages() + " vagas");
            txtAddress.setText(property.getAddress());

            String tipo = property.getTransactionType();
            if (tipo != null && !tipo.isEmpty()) {
                txtBadgeTipo.setVisibility(View.VISIBLE);
                txtBadgeTipo.setText("rent".equalsIgnoreCase(tipo) ? "Aluguel" : "Venda");
            } else {
                txtBadgeTipo.setVisibility(View.GONE);
            }

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

            if (favoritoListener != null && favoritados != null) {
                btnFavorito.setVisibility(View.VISIBLE);
                boolean isFav = favoritados.contains(property.getId());
                btnFavorito.setImageResource(isFav ? R.drawable.ic_heart_filled : R.drawable.ic_heart_border);

                btnFavorito.setOnClickListener(v -> {
                    boolean currentlyFav = favoritados.contains(property.getId());
                    if (currentlyFav) {
                        favoritados.remove(property.getId());
                        btnFavorito.setImageResource(R.drawable.ic_heart_border);
                    } else {
                        favoritados.add(property.getId());
                        btnFavorito.setImageResource(R.drawable.ic_heart_filled);
                    }
                    favoritoListener.onToggle(property.getId(), !currentlyFav);
                });
            } else {
                btnFavorito.setVisibility(View.GONE);
            }
        }

        private String formatarPreco(double price) {
            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("pt", "BR"));
            return "R$ " + nf.format((long) price);
        }
    }
}
