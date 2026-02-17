package com.patitasalrescate.ui;

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
import com.patitasalrescate.model.Refugio;
import java.util.List;

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
        holder.txtDireccion.setText(r.getDireccion());

        if (r.getFotoUrl() != null && !r.getFotoUrl().isEmpty()) {
            Glide.with(context)
                    .load(r.getFotoUrl())
                    .centerCrop()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(holder.imgFoto);
        } else {
            holder.imgFoto.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // BOTÓN WHATSAPP
        holder.btnWhatsapp.setOnClickListener(v -> {
            String fono = r.getNumCelular();
            if (fono != null && !fono.isEmpty()) {
                fono = fono.replace(" ", "").replace("+", "");
                if (!fono.startsWith("51")) fono = "51" + fono;

                String url = "https://wa.me/" + fono + "?text=" + Uri.encode("¡Hola! Vi su refugio en Patitas al Rescate 🐾");
                try {
                    context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
                } catch (Exception e) {
                    Toast.makeText(context, "WhatsApp no instalado", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Número no disponible", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnMapa.setOnClickListener(v -> {
            Uri uriMapa = null;

            // CASO A: Tenemos Coordenadas exactas
            if (r.getLatitud() != 0 && r.getLongitud() != 0) {
                String label = Uri.encode(r.getNombre());
                // Formato para marcador exacto
                String uriString = "geo:" + r.getLatitud() + "," + r.getLongitud() + "?q=" + r.getLatitud() + "," + r.getLongitud() + "(" + label + ")";
                uriMapa = Uri.parse(uriString);
            }
            // CASO B: No tenemos coordenadas, probamos con la Dirección
            else if (r.getDireccion() != null && !r.getDireccion().trim().isEmpty()) {
                String query = Uri.encode(r.getDireccion());
                // Formato de búsqueda por texto
                uriMapa = Uri.parse("geo:0,0?q=" + query);
            }

            // EJECUTAR INTENT
            if (uriMapa != null) {
                Intent intent = new Intent(Intent.ACTION_VIEW, uriMapa);
                intent.setPackage("com.google.android.apps.maps"); // Priorizar Google Maps
                try {
                    context.startActivity(intent);
                } catch (Exception e) {
                    // Fallback a navegador u otra app de mapas
                    try {
                        context.startActivity(new Intent(Intent.ACTION_VIEW, uriMapa));
                    } catch (Exception ex) {
                        Toast.makeText(context, "No hay aplicación de mapas instalada", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                // CASO C: No hay ni coordenadas ni dirección
                Toast.makeText(context, "Ubicación no disponible", Toast.LENGTH_SHORT).show();
            }
        });
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