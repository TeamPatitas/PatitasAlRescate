package com.patitasalrescate.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.data.remote.dto.FormResponse;
import com.patitasalrescate.utils.ApiApp;

import java.util.List;

public class AdaptadorForms extends RecyclerView.Adapter<AdaptadorForms.ViewHolderForm> {

    private final List<FormResponse> listaForms;
    private final Context contexto;

    public AdaptadorForms(List<FormResponse> listaForms, Context contexto) {
        this.listaForms = listaForms;
        this.contexto = contexto;
    }

    @NonNull
    @Override
    public ViewHolderForm onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.ly_item_cardview_form, parent, false);
        return new ViewHolderForm(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolderForm holder, int position) {
        FormResponse form = listaForms.get(position);
        String nombre = (form.name != null && !form.name.trim().isEmpty())
                ? form.name.trim()
                : "Formulario PDF";
        holder.txtNombre.setText(nombre);

        String fecha = ApiApp.fechaCorta(form.createdAt);
        holder.txtFecha.setText(fecha.isEmpty() ? "" : fecha);
        holder.txtFecha.setVisibility(fecha.isEmpty() ? View.GONE : View.VISIBLE);

        holder.btnVer.setOnClickListener(v -> {
            if (form.fileUrl == null || form.fileUrl.trim().isEmpty()) {
                Toast.makeText(contexto, "No hay enlace disponible para este formulario", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(form.fileUrl.trim()));
                contexto.startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(contexto, "No se pudo abrir el formulario PDF", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return listaForms != null ? listaForms.size() : 0;
    }

    public static class ViewHolderForm extends RecyclerView.ViewHolder {
        TextView txtNombre, txtFecha;
        Button btnVer;

        public ViewHolderForm(@NonNull View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txt_nombre_form);
            txtFecha = itemView.findViewById(R.id.txt_fecha_form);
            btnVer = itemView.findViewById(R.id.btn_ver_form);
        }
    }
}
