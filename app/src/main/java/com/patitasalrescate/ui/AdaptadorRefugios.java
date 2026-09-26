package com.patitasalrescate.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.patitasalrescate.R;
import com.patitasalrescate.data.remote.dto.ShelterResponse;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.utils.ApiApp;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdaptadorRefugios extends RecyclerView.Adapter<AdaptadorRefugios.RefugioViewHolder> {
    private Context context;
    private List<Refugio> listaRefugios;
    public AdaptadorRefugios(Context context, List<Refugio> listaRefugios) {
        this.context = context;
        this.listaRefugios = listaRefugios;
    }

    @NonNull
    @Override
    public RefugioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.ly_item_cardview_refugio, parent, false);
        return new RefugioViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull RefugioViewHolder holder, int position) {
        Refugio r = listaRefugios.get(position);

        holder.txtNombre.setText(r.getNombre());
        // El resumen no trae address: se muestra al abrir el detalle/mapa.
        holder.txtDireccion.setText(r.getDireccion() == null || r.getDireccion().isEmpty()
                ? "Toca para ver detalles" : r.getDireccion());

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, com.patitasalrescate.controllers.management.ActividadPerfilRefugio.class);
            intent.putExtra("id_refugio_key", r.getIdRefugio());
            context.startActivity(intent);
        });

        if (r.getFotoUrl() != null && !r.getFotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(r.getFotoUrl())
                    .placeholder(R.drawable.img_default_refugio)
                    .error(R.drawable.img_default_refugio)
                    .centerCrop()
                    .into(holder.imgFoto);
        } else {
            holder.imgFoto.setImageResource(R.drawable.img_default_refugio);
        }
        holder.btnWhatsapp.setOnClickListener(v -> contactarRefugio(r));

        holder.btnMapa.setOnClickListener(v -> abrirMapa(r));
    }

    /** Normaliza el phoneNumber a formato WhatsApp (51 + 9 dígitos). Null si no hay. */
    private String normalizarTelefono(String crudo) {
        if (crudo == null) return null;
        String digitos = crudo.replaceAll("[^0-9]", "");
        if (digitos.startsWith("51") && digitos.length() > 9) return digitos;
        if (digitos.length() == 9) return "51" + digitos;
        return digitos.isEmpty() ? null : digitos;
    }

    private void contactarRefugio(Refugio r) {
        String fono = normalizarTelefono(r.getNumCelular());
        if (fono != null) {
            abrirWhatsapp(fono);
            return;
        }
        // El resumen de la lista no trae phoneNumber: se pide el detalle una sola vez.
        if (r.getIdRefugio() == null || r.getIdRefugio().isEmpty()) {
            Toast.makeText(context, "Número no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "Obteniendo contacto…", Toast.LENGTH_SHORT).show();
        ApiApp.client().shelters.getShelterById(r.getIdRefugio()).enqueue(new Callback<ShelterResponse>() {
            @Override public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(context, "No se pudo obtener el contacto", Toast.LENGTH_SHORT).show();
                    return;
                }
                r.setNumCelular(response.body().phoneNumber);
                String fonoDetalle = normalizarTelefono(response.body().phoneNumber);
                if (fonoDetalle != null) {
                    abrirWhatsapp(fonoDetalle);
                } else {
                    Toast.makeText(context, "El refugio no tiene teléfono registrado", Toast.LENGTH_LONG).show();
                }
            }
            @Override public void onFailure(Call<ShelterResponse> call, Throwable error) {
                Toast.makeText(context, "Sin conexión al obtener el contacto", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirWhatsapp(String fono) {
        if (context instanceof Activity && ((Activity) context).isFinishing()) return;
        String url = "https://wa.me/" + fono + "?text=" + Uri.encode("¡Hola! Vi su refugio en Patitas al Rescate 🐾");
        try {
            context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show();
        }
    }

    private void abrirMapa(Refugio r) {
        String direccion = r.getDireccion();
        if (direccion != null && !direccion.trim().isEmpty()) {
            abrirMapaConDireccion(direccion.trim());
            return;
        }
        // El resumen de la lista no trae address: se pide el detalle una sola vez.
        if (r.getIdRefugio() == null || r.getIdRefugio().isEmpty()) {
            Toast.makeText(context, "Dirección no disponible", Toast.LENGTH_SHORT).show();
            return;
        }
        Toast.makeText(context, "Obteniendo dirección…", Toast.LENGTH_SHORT).show();
        ApiApp.client().shelters.getShelterById(r.getIdRefugio()).enqueue(new Callback<ShelterResponse>() {
            @Override public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || response.body().address == null || response.body().address.trim().isEmpty()) {
                    Toast.makeText(context, "Dirección no disponible", Toast.LENGTH_SHORT).show();
                    return;
                }
                r.setDireccion(response.body().address);
                if (response.body().phoneNumber != null) r.setNumCelular(response.body().phoneNumber);
                abrirMapaConDireccion(response.body().address.trim());
            }
            @Override public void onFailure(Call<ShelterResponse> call, Throwable error) {
                Toast.makeText(context, "Sin conexión al obtener la dirección", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void abrirMapaConDireccion(String direccion) {
        // Se usa el address tal cual: Maps lo geocodifica (sin ciudad hardcodeada).
        Uri uriMapa = Uri.parse("geo:0,0?q=" + Uri.encode(direccion));
        Intent intent = new Intent(Intent.ACTION_VIEW, uriMapa);
        intent.setPackage("com.google.android.apps.maps");
        try {
            context.startActivity(intent);
        } catch (Exception e) {
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, uriMapa));
            } catch (Exception ex) {
                Toast.makeText(context, "No hay aplicación de mapas instalada", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public int getItemCount() { return listaRefugios.size(); }

    public static class RefugioViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtDireccion;
        ImageView imgFoto;
        ImageButton btnMapa, btnWhatsapp;

        public RefugioViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txt_nombre_refugio);
            txtDireccion = itemView.findViewById(R.id.txt_direccion_refugio);
            imgFoto = itemView.findViewById(R.id.img_foto_refugio);
            btnMapa = itemView.findViewById(R.id.btn_ver_mapa);
            btnWhatsapp = itemView.findViewById(R.id.btn_whatsapp_refugio);
        }
    }
}