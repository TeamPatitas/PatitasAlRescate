package com.patitasalrescate.ui;

import android.content.Context;
import android.content.Intent;
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
import com.patitasalrescate.controllers.management.ActividadDetalleEvento;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.utils.ApiApp;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdaptadorEventos extends RecyclerView.Adapter<AdaptadorEventos.EventoViewHolder> {

    private static final String TAG = "AdaptadorEventos";

    private List<Evento> lista;
    private Context context;
    private boolean esModoRefugio;

    public AdaptadorEventos(List<Evento> lista, Context context) {
        this(lista, context, false);
    }

    public AdaptadorEventos(List<Evento> lista, Context context, boolean esModoRefugio) {
        this.lista = lista;
        this.context = context;
        this.esModoRefugio = esModoRefugio;
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
        if (evento == null) return;

        holder.txtNombre.setText(evento.getNombre() == null ? "" : evento.getNombre());
        holder.txtFecha.setText(ApiApp.fechaBonita(evento.getFecha()));
        String descripcion = evento.getDescripcion();
        if (descripcion == null || descripcion.isEmpty()) {
            holder.txtDescripcion.setVisibility(View.GONE);
        } else {
            holder.txtDescripcion.setVisibility(View.VISIBLE);
            holder.txtDescripcion.setText(descripcion);
        }

        // Eliminar solo en modo refugio (DELETE /event/{id}).
        if (esModoRefugio) {
            holder.btnEliminar.setVisibility(View.VISIBLE);
            holder.btnEliminar.setOnClickListener(v -> confirmarEliminar(evento));
        } else {
            holder.btnEliminar.setVisibility(View.GONE);
            holder.btnEliminar.setOnClickListener(null);
        }

        if (evento.getFotoUrl() != null && !evento.getFotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(evento.getFotoUrl())
                    .placeholder(R.drawable.evento_default)
                    .error(R.drawable.evento_default)
                    .centerCrop()
                    .into(holder.imgFoto);
        } else {
            holder.imgFoto.setImageResource(R.drawable.eventos);
        }

        holder.btnVerDetalles.setOnClickListener(v -> {
            Intent intent = new Intent(context, ActividadDetalleEvento.class);
            intent.putExtra("evento_key", evento);
            context.startActivity(intent);
        });
    }

    private void confirmarEliminar(Evento evento) {
        if (!(context instanceof android.app.Activity)) return;
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Eliminar evento")
                .setMessage("¿Eliminar '" + evento.getNombre() + "' de forma permanente?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarEvento(evento))
                .show();
    }

    private void eliminarEvento(Evento evento) {
        if (!ApiApp.exigirOnline(context)) return;
        android.util.Log.d(TAG, "DELETE event/" + evento.getIdEvento());
        ApiApp.client().events.deleteEvent(evento.getIdEvento()).enqueue(new Callback<String>() {
            @Override public void onResponse(Call<String> call, Response<String> response) {
                android.util.Log.d(TAG, "DELETE event -> HTTP " + response.code());
                if (response.isSuccessful()) {
                    int pos = lista.indexOf(evento);
                    if (pos >= 0) {
                        lista.remove(pos);
                        notifyItemRemoved(pos);
                    }
                    android.widget.Toast.makeText(context, "Evento eliminado",
                            android.widget.Toast.LENGTH_SHORT).show();
                } else if (response.code() == 403) {
                    android.widget.Toast.makeText(context, "Sin permiso para eliminar este evento",
                            android.widget.Toast.LENGTH_LONG).show();
                } else {
                    android.widget.Toast.makeText(context,
                            "No se pudo eliminar (" + response.code() + ")",
                            android.widget.Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<String> call, Throwable error) {
                android.util.Log.e(TAG, "DELETE event onFailure: " + error, error);
                android.widget.Toast.makeText(context, "Sin conexión al eliminar",
                        android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class EventoViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtFecha, txtDescripcion;
        ImageView imgFoto;
        Button btnVerDetalles, btnEliminar;

        public EventoViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txt_nombre_evento);
            txtFecha = itemView.findViewById(R.id.txt_fecha_evento);
            txtDescripcion = itemView.findViewById(R.id.txt_descripcion_evento);
            imgFoto = itemView.findViewById(R.id.img_foto_evento);
            btnVerDetalles = itemView.findViewById(R.id.btn_ver_evento);
            btnEliminar = itemView.findViewById(R.id.btn_eliminar_evento);
        }
    }
}