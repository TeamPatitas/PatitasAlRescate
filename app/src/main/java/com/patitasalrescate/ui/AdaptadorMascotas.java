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
import com.patitasalrescate.Controllers.ActividadIniciarSesion;
import com.patitasalrescate.Controllers.ActividadPerfilMascota;
import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAOMascota;
import com.patitasalrescate.accesoADatos.SupabaseService;
import com.patitasalrescate.model.Mascota;

import java.util.List;

public class AdaptadorMascotas extends RecyclerView.Adapter<AdaptadorMascotas.MascotaViewHolder> {

    private List<Mascota> lista;
    private boolean esModoRefugio;
    private Context context;
    private String idUsuario;
    private String tipoUsuario;

    private DAOMascota daoMascota;
    private SupabaseService supabaseService;

    public AdaptadorMascotas(List<Mascota> lista, boolean esModoRefugio, Context context,
                             String idUsuario, String tipoUsuario,
                             DAOMascota dao, SupabaseService supabase) {
        this.lista = lista;
        this.esModoRefugio = esModoRefugio;
        this.context = context;
        this.idUsuario = idUsuario;
        this.tipoUsuario = tipoUsuario;
        this.daoMascota = dao;
        this.supabaseService = supabase;
    }

    @NonNull
    @Override
    public MascotaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.ly_item_cardview_mascota, parent, false);
        return new MascotaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MascotaViewHolder holder, int position) {
        Mascota m = lista.get(position);

        holder.txtNombre.setText(m.getNombre());
        holder.txtRaza.setText(m.getRaza() + " (" + m.getEdad() + " meses)");

        if (m.getFotos() != null && !m.getFotos().isEmpty()) {
            Glide.with(context).load(m.getFotos().get(0)).centerCrop().into(holder.imgFoto);
        } else {
            holder.imgFoto.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // --- LÓGICA DE ESTADO ---
        String estado = m.getEstado() != null ? m.getEstado() : "DISPONIBLE";

        // --- CONFIGURACIÓN DE BOTONES SEGÚN ROL ---
        if (esModoRefugio) {
            holder.btnPrincipal.setText("Editar");
            holder.btnPrincipal.setOnClickListener(v -> abrirPerfil(m, true));
            holder.txtEstado.setText("Estado: " + estado);

            if ("EN_PROCESO".equals(estado)) {
                // CASO 1: Hay una solicitud pendiente -> Mostrar APROBAR y RECHAZAR
                holder.btnRapido.setVisibility(View.VISIBLE);
                holder.btnRapido.setText("Aprobar");
                holder.btnRapido.setOnClickListener(v -> marcarComoAdoptado(m, holder.getAdapterPosition()));

                holder.btnRechazar.setVisibility(View.VISIBLE);
                holder.btnRechazar.setOnClickListener(v -> rechazarSolicitud(m, holder.getAdapterPosition()));

            } else if ("DISPONIBLE".equals(estado)) {
                // CASO 2: Está libre -> Mostrar solo marcar como adoptado (manual)
                holder.btnRapido.setVisibility(View.VISIBLE);
                holder.btnRapido.setText("Adoptado");
                holder.btnRapido.setOnClickListener(v -> marcarComoAdoptado(m, holder.getAdapterPosition()));

                holder.btnRechazar.setVisibility(View.GONE); // Ocultar rechazar

            } else {
                // CASO 3: Ya está ADOPTADO -> Ocultar botones de acción
                holder.btnRapido.setVisibility(View.GONE);
                holder.btnRechazar.setVisibility(View.GONE);
            }

        } else {
            // [ MODO ADOPTANTE ]
            holder.txtEstado.setText(estado.equals("DISPONIBLE") ? "Disponible para ti" : "En Proceso");
            holder.btnPrincipal.setText("Ver Detalles");
            holder.btnPrincipal.setOnClickListener(v -> abrirPerfil(m, false));

            holder.btnRapido.setVisibility(View.GONE);
            holder.btnRechazar.setVisibility(View.GONE);
        }
    }

    // --- APROBAR ADOPCIÓN ---
    private void marcarComoAdoptado(Mascota m, int pos) {
        m.setEstado("ADOPTADO");
        daoMascota.actualizar(m);

        new Thread(() -> {
            try {
                // 1. Tabla mascotas -> ADOPTADO
                supabaseService.actualizarEstadoMascota(m.getIdMascota(), "ADOPTADO");
                // 2. Tabla adopciones -> aprobada
                supabaseService.aprobarAdopcionPorMascota(m.getIdMascota());

                mostrarToast("¡Adopción Aprobada! 🐶");
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        actualizarListaVisual(m, pos);
    }

    // --- RECHAZAR ADOPCIÓN (NUEVO) ---
    private void rechazarSolicitud(Mascota m, int pos) {
        // 1. Volver estado local a DISPONIBLE
        m.setEstado("DISPONIBLE");
        daoMascota.actualizar(m);

        new Thread(() -> {
            try {
                // 2. Tabla mascotas -> DISPONIBLE
                supabaseService.actualizarEstadoMascota(m.getIdMascota(), "DISPONIBLE");
                // 3. Tabla adopciones -> rechazada
                supabaseService.rechazarAdopcionPorMascota(m.getIdMascota());

                mostrarToast("Solicitud Rechazada ❌");
            } catch (Exception e) { e.printStackTrace(); }
        }).start();

        // Actualizar visualmente (refrescar el item para que se oculten los botones)
        notifyItemChanged(pos);
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
            ((Activity) context).runOnUiThread(() -> {
                // Si estamos filtrando, quizás debamos quitarlo
                // Para este caso, simplemente notificamos el cambio
                notifyItemChanged(pos);
            });
        }
    }

    private void abrirPerfil(Mascota m, boolean editar) {
        Intent i = new Intent(context, ActividadPerfilMascota.class);
        i.putExtra("id_mascota_key", m.getIdMascota());
        i.putExtra("es_modo_edicion", editar);
        i.putExtra(ActividadIniciarSesion.EXTRA_TIPO_USUARIO, tipoUsuario);
        i.putExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO, idUsuario);
        context.startActivity(i);
    }

    @Override
    public int getItemCount() { return lista != null ? lista.size() : 0; }

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