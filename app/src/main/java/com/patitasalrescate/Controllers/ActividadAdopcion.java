package com.patitasalrescate.Controllers;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAOMascota;
import com.patitasalrescate.accesoADatos.DAORefugio;
import com.patitasalrescate.accesoADatos.SupabaseService;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.model.Refugio;

public class ActividadAdopcion extends AppCompatActivity {

    private DAOMascota daoMascota;
    private DAORefugio daoRefugio;
    private SupabaseService supabaseService;

    private String idMascota;  // ← String
    private Mascota mascota;
    private Refugio refugio;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_adopcion);

        daoMascota = new DAOMascota(this);
        daoRefugio = new DAORefugio(this);
        supabaseService = new SupabaseService();

        idMascota = getIntent().getStringExtra("id_mascota_key");  // ← getStringExtra
        if (idMascota == null) {
            Toast.makeText(this, "Error: no llegó la mascota", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        mascota = daoMascota.obtenerPorId(idMascota);
        if (mascota == null) {
            Toast.makeText(this, "Mascota no encontrada", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        refugio = daoRefugio.obtenerPorId(mascota.getIdRefugio());

        TextView txtTitulo = findViewById(R.id.txtTituloAdopcion);
        TextView txtDetalle = findViewById(R.id.txtDetalleAdopcion);
        Button btnConfirmar = findViewById(R.id.btnConfirmarAdopcion);
        Button btnWhatsapp = findViewById(R.id.btnContactarWhatsapp);

        txtTitulo.setText("Confirmar adopción 🐾");
        txtDetalle.setText("Mascota: " + mascota.getNombre() + "\n"
                + "Refugio: " + (refugio != null ? refugio.getNombre() : "No identificado"));

        btnConfirmar.setOnClickListener(v -> {
            if (mascota.isEsAdoptado()) {
                Toast.makeText(this, "Esta mascota ya está adoptada", Toast.LENGTH_SHORT).show();
                return;
            }
            mascota.setEsAdoptado(true);

            // 1) Actualizar local
            int filas = daoMascota.actualizar(mascota);
            if (filas > 0) {
                Toast.makeText(this, "Adopción registrada ✅", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "No se pudo actualizar el estado", Toast.LENGTH_SHORT).show();
            }

            // 2) Best effort: actualizar en la nube
            new Thread(() -> {
                try {
                    supabaseService.actualizarEstadoAdoptado(mascota.getIdMascota(), true);
                } catch (Exception ignored) {
                }
            }).start();
        });

        btnWhatsapp.setOnClickListener(v -> abrirWhatsapp());
    }

    private void abrirWhatsapp() {
        if (refugio == null || refugio.getNumCelular() == null || refugio.getNumCelular().trim().isEmpty()) {
            Toast.makeText(this, "El refugio no tiene número registrado", Toast.LENGTH_SHORT).show();
            return;
        }

        String telefono = refugio.getNumCelular().trim();
        if (!telefono.startsWith("+")) {
            telefono = "+51" + telefono;
        }

        String mensaje = "Hola, quiero adoptar a " + mascota.getNombre()
                + " 🐶🐱. ¿Podemos coordinar?";
        String url = "https://wa.me/" + telefono.replace("+", "") + "?text=" + Uri.encode(mensaje);

        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}