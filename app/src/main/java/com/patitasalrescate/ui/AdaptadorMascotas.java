package com.patitasalrescate.ui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.util.Log;
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
import com.patitasalrescate.controllers.management.ActividadPerfilMascota;
import com.patitasalrescate.R;
import com.patitasalrescate.data.repository.PetApiRepository;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.PatitasSessionManager;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdaptadorMascotas extends RecyclerView.Adapter<AdaptadorMascotas.MascotaViewHolder> {

    private static final String TAG = "AdaptadorMascotas";

    private List<Mascota> lista;
    private boolean esModoRefugio;
    private boolean esModoFavoritos = false;
    private Context context;
    private String idUsuario;
    private String tipoUsuario;

    private PetApiRepository petApiRepository;

    public AdaptadorMascotas(List<Mascota> lista, boolean esModoRefugio,
                             Context context) {
        this.lista = lista;
        this.esModoRefugio = esModoRefugio;
        this.context = context;
        this.petApiRepository = ApiApp.client().pets;

        PatitasSessionManager session = PatitasSessionManager.getInstance(context);
        this.idUsuario = session.getUserId();
        this.tipoUsuario = session.getSessionType();
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
        holder.txtRaza.setText(m.getRaza() == null ? "" : m.getRaza());

        // Primera foto registrada (índice 0). Sin fotos: placeholder para no reciclar
        // la imagen de otra celda.
        if (m.getFotos() != null && !m.getFotos().isEmpty()
                && m.getFotos().get(0) != null && !m.getFotos().get(0).isEmpty()) {
            Glide.with(context).load(m.getFotos().get(0))
                    .placeholder(R.drawable.img_default_refugio)
                    .error(R.drawable.img_default_refugio)
                    .centerCrop().into(holder.imgFoto);
        } else {
            holder.imgFoto.setImageResource(R.drawable.img_default_refugio);
        }

        float density = context.getResources().getDisplayMetrics().density;

        if (esModoRefugio) {
            boolean disponible = "DISPONIBLE".equals(m.getEstado());
            holder.txtNombre.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 20);
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
                            holder.txtNombre.getLayoutParams();
            params.topMargin = 0;
            holder.txtNombre.setLayoutParams(params);
            holder.txtRaza.setVisibility(View.VISIBLE);
            holder.txtEstado.setVisibility(View.VISIBLE);
            holder.txtEstado.setText(disponible ? "Disponible" : "No disponible");
            holder.txtEstado.setBackgroundResource(R.drawable.bg_badge_pill);
            holder.txtEstado.setBackgroundTintList(ColorStateList.valueOf(
                    context.getColor(disponible ? R.color.verde_persona : R.color.grisAcento)));
            holder.txtEstado.setTextColor(context.getColor(R.color.blanco));

            holder.btnPrincipal.setVisibility(View.VISIBLE);
            holder.btnPrincipal.setText("Editar mascota");
            holder.btnPrincipal.setOnClickListener(v -> abrirPerfil(m, true));

            holder.btnRapido.setVisibility(View.VISIBLE);
            holder.btnRapido.setText("Eliminar");
            holder.btnRapido.setBackgroundTintList(ColorStateList.valueOf(0xFFD32F2F));
            holder.btnRapido.setOnClickListener(v -> confirmarEliminar(m, holder.getAdapterPosition()));

            holder.btnRechazar.setVisibility(View.GONE);

        } else {
            // Modo adoptante: solo el nombre, más grande y centrado verticalmente junto a la foto.
            holder.txtNombre.setTextSize(android.util.TypedValue.COMPLEX_UNIT_SP, 24);
            androidx.constraintlayout.widget.ConstraintLayout.LayoutParams paramsNombre =
                    (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams)
                            holder.txtNombre.getLayoutParams();
            paramsNombre.topMargin = (int) (30 * density);
            holder.txtNombre.setLayoutParams(paramsNombre);
            holder.txtRaza.setVisibility(View.GONE);
            holder.txtEstado.setVisibility(View.GONE);

            // Sin "Quiero Adoptar" en la lista: la adopción se hace en el detalle.
            holder.btnPrincipal.setVisibility(View.VISIBLE);
            holder.btnPrincipal.setText("Ver detalles");
            holder.btnPrincipal.setOnClickListener(v -> abrirPerfil(m, false));

            holder.btnRapido.setVisibility(View.GONE);
            holder.btnRechazar.setVisibility(View.GONE);
        }
    }

    private void confirmarEliminar(Mascota m, int pos) {
        if (!(context instanceof Activity)) return;
        new androidx.appcompat.app.AlertDialog.Builder(context)
                .setTitle("Eliminar mascota")
                .setMessage("¿Eliminar a " + m.getNombre() + " de forma permanente?")
                .setNegativeButton("Cancelar", null)
                .setPositiveButton("Eliminar", (dialog, which) -> eliminarMascota(m, pos))
                .show();
    }

    private void eliminarMascota(Mascota m, int pos) {
        if (!ApiApp.exigirOnline(context)) return;
        Log.d(TAG, "DELETE pet/" + m.getIdMascota());
        petApiRepository.deletePet(m.getIdMascota()).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded()) return;
                Log.d(TAG, "DELETE pet -> HTTP " + response.code());
                if (response.isSuccessful()) {
                    int actual = lista.indexOf(m);
                    if (actual == -1) actual = pos;
                    if (actual >= 0 && actual < lista.size()) {
                        lista.remove(actual);
                        actualizarListaEliminada(actual);
                    }
                    mostrarToast("Mascota eliminada");
                } else if (response.code() == 403) {
                    mostrarToast("Sin permiso para eliminar esta mascota");
                } else {
                    mostrarToast("No se pudo eliminar (" + response.code() + ")");
                }
            }
            @Override public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded()) return;
                Log.e(TAG, "DELETE pet onFailure: " + t, t);
                mostrarToast("Error de conexión al eliminar");
            }
        });
    }

    private void mostrarToast(String mensaje) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() ->
                    Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
            );
        }
    }

    private void actualizarListaEliminada(int pos) {
        if (context instanceof Activity) {
            ((Activity) context).runOnUiThread(() -> notifyItemRemoved(pos));
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

    private boolean isAdded() {
        return context instanceof Activity && !((Activity) context).isFinishing();
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