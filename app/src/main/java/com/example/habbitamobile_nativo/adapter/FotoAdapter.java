package com.example.habbitamobile_nativo.adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.habbitamobile_nativo.R;

import java.util.List;

public class FotoAdapter extends RecyclerView.Adapter<FotoAdapter.FotoViewHolder> {

    private final List<Uri> fotos;
    private final OnRemoverFotoListener listener;

    public interface OnRemoverFotoListener {
        void onRemover(int posicao);
    }

    public FotoAdapter(List<Uri> fotos, OnRemoverFotoListener listener) {
        this.fotos = fotos;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_foto, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        holder.bind(fotos.get(position), position);
    }

    @Override
    public int getItemCount() {
        return fotos.size();
    }

    class FotoViewHolder extends RecyclerView.ViewHolder {

        private final ImageView imgFoto;
        private final ImageButton btnRemover;

        FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.imgFoto);
            btnRemover = itemView.findViewById(R.id.btnRemover);
        }

        void bind(Uri uri, int posicao) {
            Glide.with(itemView.getContext())
                    .load(uri)
                    .centerCrop()
                    .into(imgFoto);

            btnRemover.setOnClickListener(v -> listener.onRemover(posicao));
        }
    }
}
