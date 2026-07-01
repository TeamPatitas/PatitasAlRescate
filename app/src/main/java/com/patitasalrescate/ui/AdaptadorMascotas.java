package com.patitasalrescate.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.patitasalrescate.controllers.auth.ActividadIniciarSesion;
import com.patitasalrescate.controllers.management.ActividadPerfilMascota;
import com.patitasalrescate.R;
import com.patitasalrescate.data_access.DAOFavoritos;
import com.patitasalrescate.data_access.DAOMascota;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.utils.PatitasSessionManager;

import java.util.List;

public class AdaptadorMascotas extends RecyclerView.Adapter<AdaptadorMascotas.MascotaViewHolder> {

    private List<Mascota> lista;
    private boolean esModoRefugio;
    private boolean esModoFavoritos = false;
    private Context context;
    private String idUsuario;
    private String tipoUsuario;

    private DAOMascota daoMascota;
    private DAOFavoritos daoFavoritos;

    public AdaptadorMascotas(List<Mascota> lista, boolean esModoRefugio,
                             Context context,
                             DAOMascota dao) {
        this.lista = lista;
        this.esModoRefugio = esModoRefugio;
        this.context = context;
        this.daoMascota = dao;
        
        PatitasSessionManager session = PatitasSessionManager.getInstance(context);
        this.idUsuario = session.getUserId();
        this.tipoUsuario = session.getUserType();
    }

    public AdaptadorMascotas(List<Mascota> lista,
                             Context context,
                             DAOMascota dao,
                             DAOFavoritos daoFavoritos) {
        this.lista = lista;
        this.context = context;
        this.daoMascota = dao;
        this.daoFavoritos = daoFavoritos;

        PatitasSessionManager session = PatitasSessionManager.getInstance(context);
        this.idUsuario = session.getUserId();
        this.tipoUsuario = session.getUserType();
        this.esModoFavoritos = true;
        this.esModoRefugio = false;
    }

    @NonNull
    @Override
    public MascotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ly_item_cardview_mascota, parent, false);
        return new MascotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MascotaViewHolder holder, int position) {
        Mascota m = lista.get(position);

        holder.txtNombre.setText(m.getNombre());
        holder.txtRaza.setText(m.getRaza() + " (" + m.getEdad() + " meses)");

        if (m.getFotos() != null && !m.getFotos().isEmpty()) {
            Glide.with(context).load(m.getFotos().get(0))
                    .centerCrop().into(holder.imgFoto);
        }

        String estado = m.getEstado() != null ? m.getEstado() : "DISPONIBLE";
        holder.txtEstado.setText("Estado: " + estado);

        if (esModoRefugio) {
            holder.btnPrincipal.setVisibility(View.VISIBLE);
            holder.btnPrincipal.setText("Editar");
            holder.btnPrincipal.setOnClickListener(v -> abrirPerfil(m, true));

            if (!"DISPONIBLE".equals(estado) && !"ADOPTADO".equals(estado)) {
                holder.btnRapido.setVisibility(View.VISIBLE);
                holder.btnRapido.setText("Aprobar Adopción");
                holder.btnRapido.setOnClickListener(v -> marcarComoAdoptado(m, holder.getAdapterPosition()));

                holder.btnRechazar.setVisibility(View.VISIBLE);
                holder.btnRechazar.setText("Rechazar Adopción");
                holder.btnRechazar.setOnClickListener(v -> rechazarSolicitud(m, holder.getAdapterPosition()));
            } else {
                holder.btnRapido.setVisibility(View.GONE);
                holder.btnRechazar.setVisibility(View.GONE);
            }

        } else {
            if ("DISPONIBLE".equals(estado)) {
                holder.btnPrincipal.setVisibility(View.VISIBLE);
                holder.btnPrincipal.setText("Quiero Adoptar");
                holder.btnPrincipal.setOnClickListener(v -> abrirPerfil(m, false));
            } else {
                holder.btnPrincipal.setVisibility(View.GONE);
            }

            if (esModoFavoritos) {
                holder.btnRapido.setVisibility(View.VISIBLE);
                holder.btnRapido.setText("Eliminar Favoritos");
                holder.btnRapido.setOnClickListener(v -> {
                    daoFavoritos.removeFavorito(idUsuario, m.getIdMascota());
                    lista.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                    mostrarToast("Eliminado de favoritos");
                });

                holder.btnRechazar.setVisibility(View.GONE);
            } else {
                holder.btnRapido.setVisibility(View.GONE);
                holder.btnRechazar.setVisibility(View.GONE);
            }
        }
    }

    private void marcarComoAdoptado(Mascota m, int pos) {
        m.setEstado("ADOPTADO");
        daoMascota.actualizar(m);
        mostrarToast("¡Adopción Aprobada! 🐶");
        actualizarListaVisual(m, pos);
    }

    private void rechazarSolicitud(Mascota m, int pos) {
        m.setEstado("DISPONIBLE");
        daoMascota.actualizar(m);
        mostrarToast("Solicitud Rechazada ❌");
        actualizarListaVisual(m, pos);
    }

    private void mostrarToast(String mensaje) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void actualizarListaVisual(Mascota m, int pos) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> notifyItemChanged(pos));
        }
    }

    private void abrirPerfil(Mascota m, boolean editar) {
        Intent i = new Intent(context, ActividadPerfilMascota.class);
        i.putExtra("id_mascota_key", m.getIdMascota());
        i.putExtra("es_modo_edicion", editar);
        context.startActivity(i);
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    static class MascotaViewHolder extends RecyclerView.ViewHolder {
        TextView txtNombre, txtRaza, txtEstado;
        ImageView imgFoto;
        Button btnPrincipal, btnRapido, btnRechazar;
        public MascotaViewHolder(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txt_nombre_mascota);
            txtRaza = itemView.findViewById(R.id.txt_raza_mascota);
            txtEstado = itemView.findViewById(R.id.txt_estado_mascota);
            imgFoto = itemView.findViewById(R.id.img_foto_mascota);
            btnPrincipal = itemView.findViewById(R.id.btn_principal);
            btnRapido = itemView.findViewById(R.id.btn_accion_rapida);
            btnRechazar = itemView.findViewById(R.id.btn_rechazar);
        }
    }
}
