package com.patitasalrescate.ui;

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
import com.patitasalrescate.Controllers.ActividadIniciarSesion;
import com.patitasalrescate.Controllers.ActividadPerfilMascota;
import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAOFavoritos;
import com.patitasalrescate.accesoADatos.DAOMascota;
import com.patitasalrescate.accesoADatos.SupabaseService;
import com.patitasalrescate.model.Mascota;

import java.util.List;

public class AdaptadorMascotas extends RecyclerView.Adapter<AdaptadorMascotas.MascotaViewHolder> {

    private List<Mascota> lista;
    private boolean esModoRefugio;
    private boolean esModoFavoritos = false;

    private Context context;
    private String idUsuario;
    private String tipoUsuario;

    private DAOMascota daoMascota;
    private SupabaseService supabaseService;
    private DAOFavoritos daoFavoritos;

    // CONSTRUCTOR NORMAL
    public AdaptadorMascotas(List<Mascota> lista, boolean esModoRefugio,
                             Context context, String idUsuario,
                             String tipoUsuario,
                             DAOMascota dao, SupabaseService supabase) {

        this.lista = lista;
        this.esModoRefugio = esModoRefugio;
        this.context = context;
        this.idUsuario = idUsuario;
        this.tipoUsuario = tipoUsuario;
        this.daoMascota = dao;
        this.supabaseService = supabase;
    }

    // CONSTRUCTOR FAVORITOS
    public AdaptadorMascotas(List<Mascota> lista,
                             Context context,
                             String idUsuario,
                             DAOMascota dao,
                             SupabaseService supabase,
                             DAOFavoritos daoFavoritos) {

        this.lista = lista;
        this.context = context;
        this.idUsuario = idUsuario;
        this.daoMascota = dao;
        this.supabaseService = supabase;
        this.daoFavoritos = daoFavoritos;

        this.esModoFavoritos = true;
        this.esModoRefugio = false;
        this.tipoUsuario = "ADOPTANTE";
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
            holder.btnPrincipal.setText("Editar");
            holder.btnPrincipal.setOnClickListener(v -> abrirPerfil(m,true));

            holder.btnRapido.setVisibility(View.GONE);
            holder.btnRechazar.setVisibility(View.GONE);

        } else {

            // BOTÓN ADOPTAR SOLO DISPONIBLE
            if ("DISPONIBLE".equals(estado)) {
                holder.btnPrincipal.setVisibility(View.VISIBLE);
                holder.btnPrincipal.setText("Quiero Adoptar");
                holder.btnPrincipal.setOnClickListener(v -> abrirPerfil(m,false));
            } else {
                holder.btnPrincipal.setVisibility(View.GONE);
            }

            if (esModoFavoritos) {

                holder.btnRapido.setVisibility(View.VISIBLE);
                holder.btnRapido.setText("Eliminar ❤️");

                holder.btnRapido.setOnClickListener(v -> {
                    daoFavoritos.removeFavorito(idUsuario,m.getIdMascota());
                    new Thread(() -> {
                        try {
                            supabaseService.eliminarFavorito(
                                    idUsuario,
                                    m.getIdMascota()
                            );
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }).start();

                    lista.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());

                    Toast.makeText(context,
                            "Eliminado de favoritos",
                            Toast.LENGTH_SHORT).show();
                });

                holder.btnRechazar.setVisibility(View.GONE);
            } else {
                holder.btnRapido.setVisibility(View.GONE);
                holder.btnRechazar.setVisibility(View.GONE);
            }
        }
    }

    private void abrirPerfil(Mascota m, boolean editar) {
        Intent i = new Intent(context, ActividadPerfilMascota.class);
        i.putExtra("id_mascota_key", m.getIdMascota());
        i.putExtra("es_modo_edicion", editar);
        i.putExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO, idUsuario);
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
