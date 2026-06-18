package com.patitasalrescate.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.model.Evento;

import java.util.List;

public class AdaptadorEventos extends RecyclerView.Adapter<AdaptadorEventos.EventoViewHolder> {

    private List<Evento> lista;
    private Context context;

    public AdaptadorEventos(List<Evento> lista, Context context) {
        this.lista = lista;
        this.context = context;
    }

    @NonNull
    @Override
    public EventoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ly_eventos_element, parent, false);
        return new EventoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventoViewHolder holder, int position) {
        Evento evento = lista.get(position);

        holder.txtNombre.setText(evento.getNombre());
        holder.txtFecha.setText(evento.getFecha());
        holder.txtDescripcion.setText(evento.getDescripcion());

        if (evento.getFotoUrl() != null && !evento.getFotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(evento.getFotoUrl())
                    .centerCrop()
                    .into(holder.imgFoto);
        } else {
            holder.imgFoto.setImageResource(R.drawable.bg_circle_image); // Default or placeholder
        }

        holder.btnVerDetalles.setOnClickListener(v -> {
            // Logic to see event details
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtFecha, txtDescripcion;
        ImageView imgFoto;
        Button btnVerDetalles;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txt_nombre_evento);
            txtFecha = itemView.findViewById(R.id.txt_fecha_evento);
            txtDescripcion = itemView.findViewById(R.id.txt_descripcion_evento);
            imgFoto = itemView.findViewById(R.id.img_foto_evento);
            btnVerDetalles = itemView.findViewById(R.id.btn_ver_evento);
        }
    }
}