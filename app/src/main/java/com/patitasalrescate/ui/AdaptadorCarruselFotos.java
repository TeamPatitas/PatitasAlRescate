package com.patitasalrescate.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.patitasalrescate.R;

import java.util.List;

public class AdaptadorCarruselFotos extends RecyclerView.Adapter<AdaptadorCarruselFotos.FotoViewHolder> {

    private final Context context;
    private final List<String> fotos;

    public AdaptadorCarruselFotos(Context context, List<String> fotos) {
        this.context = context;
        this.fotos = fotos;
    }

    @NonNull
    @Override
    public FotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_carrusel_foto, parent, false);
        return new FotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FotoViewHolder holder, int position) {
        String url = fotos.get(position);
        Glide.with(context)
                .load(url)
                .centerCrop()
                .into(holder.imgFoto);
    }

    @Override
    public int getItemCount() {
        return fotos != null ? fotos.size() : 0;
    }

    static class FotoViewHolder extends RecyclerView.ViewHolder {
        ImageView imgFoto;

        FotoViewHolder(@NonNull View itemView) {
            super(itemView);
            imgFoto = itemView.findViewById(R.id.img_carrusel_item);
        }
    }
}
