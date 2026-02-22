package com.patitasalrescate.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;

import java.util.List;

public class AdaptadorFotosPreview extends RecyclerView.Adapter<AdaptadorFotosPreview.FotoViewHolder> {
    private final List<Uri> listaUris;
    public AdaptadorFotosPreview(List<Uri> lista) {
        this.listaUris = lista;
    }
    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ly_item_foto_preview, parent, false);
        return new FotoViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        Uri uri = listaUris.get(position);
        holder.imgPreview.setImageURI(uri);
    }
    @Override
    public int getItemCount() {
        return listaUris.size();
    }

    static class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgPreview;

        public FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgPreview = itemView.findViewById(R.id.img_foto_preview);
        }
    }
}