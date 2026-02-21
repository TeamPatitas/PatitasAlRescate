package com.patitasalrescate.Controllers;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAOAdopcion;
import com.patitasalrescate.accesoADatos.DAOMascota;
import com.patitasalrescate.accesoADatos.DAORefugio;
import com.patitasalrescate.accesoADatos.SupabaseService;
import com.patitasalrescate.model.Adopcion;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.model.Refugio;
import java.util.UUID;

public class ActividadAdopcion extends AppCompatActivity {

    private DAOMascota daoMascota;
    private DAORefugio daoRefugio;
    private DAOAdopcion daoAdopcion;
    private SupabaseService supabaseService;

    private String idMascota;
    private String idAdoptante;
    private Mascota mascota;
    private Refugio refugio;
    private EditText edtMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_adopcion);

        daoMascota = new DAOMascota(this);
        daoRefugio = new DAORefugio(this);
        daoAdopcion = new DAOAdopcion(this);
        supabaseService = new SupabaseService();

        idMascota = getIntent().getStringExtra("id_mascota_key");
        idAdoptante = getIntent().getStringExtra(ActividadIniciarSesion.EXTRA_ID_USUARIO);

        if (idAdoptante == null) {
            SharedPreferences prefs = getSharedPreferences("sesion_adoptante", MODE_PRIVATE);
            idAdoptante = prefs.getString("id_adoptante", null);
        }

        if (idMascota == null || idAdoptante == null) {
            Toast.makeText(this, "Error de sesión: Reinicia la app", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // --- DIAGNÓSTICO DE CARGA ---
        mascota = daoMascota.obtenerPorId(idMascota);
        if (mascota == null) {
            Toast.makeText(this, "Error: Mascota no hallada en la base local", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        refugio = daoRefugio.obtenerPorId(mascota.getIdRefugio());
        if (refugio == null) {
            Toast.makeText(this, "Error: El refugio (" + mascota.getIdRefugio() + ") no existe localmente", Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        TextView txtTitulo = findViewById(R.id.txtTituloAdopcion);
        TextView txtDetalle = findViewById(R.id.txtDetalleAdopcion);
        edtMensaje = findViewById(R.id.edt_mensaje_adopcion);
        Button btnWhatsapp = findViewById(R.id.btnContactarWhatsapp);

        txtTitulo.setText("Adopta a " + mascota.getNombre() + " 🐾");
        txtDetalle.setText("Al confirmar, solicitaremos la adopción al refugio '" + refugio.getNombre() + "'.");

        btnWhatsapp.setOnClickListener(v -> procesarSolicitud());
    }

    private void procesarSolicitud() {
        if ("ADOPTADO".equals(mascota.getEstado())) {
            Toast.makeText(this, "Esta mascota ya tiene un hogar.", Toast.LENGTH_SHORT).show();
            return;
        }

        String textoIngresado = edtMensaje.getText().toString().trim();
        if (textoIngresado.isEmpty()) {
            textoIngresado = "Hola, estoy interesado en adoptar a " + mascota.getNombre();
        }
        final String mensajeFinalParaWhatsapp = textoIngresado;

        // Creamos el objeto Adopción
        Adopcion nuevaAdopcion = new Adopcion(
                UUID.randomUUID().toString(),
                idAdoptante,
                mascota.getIdMascota(),
                refugio.getIdRefugio(),
                "pendiente",
                mensajeFinalParaWhatsapp
        );

        Toast.makeText(this, "Sincronizando con la nube...", Toast.LENGTH_SHORT).show();
        Button btn = findViewById(R.id.btnContactarWhatsapp);
        btn.setEnabled(false);

        // --- FLUJO DE NUBE ---
        new Thread(() -> {
            try {
                supabaseService.insertarAdopcionDetallada(nuevaAdopcion);

                boolean exitoEstadoNube = supabaseService.actualizarEstadoMascota(mascota.getIdMascota(), "EN_PROCESO");

                runOnUiThread(() -> {
                    if (exitoEstadoNube) {
                        daoAdopcion.insertar(nuevaAdopcion);
                        mascota.setEstado("EN_PROCESO");
                        daoMascota.actualizar(mascota);

                        Toast.makeText(getApplicationContext(), "¡Solicitud enviada con éxito! ☁️", Toast.LENGTH_LONG).show();
                        abrirWhatsapp(mensajeFinalParaWhatsapp);
                        finish();
                    } else {
                        Toast.makeText(getApplicationContext(), "⚠️ Adopción creada pero falló actualizar estado mascota.", Toast.LENGTH_LONG).show();
                        btn.setEnabled(true);
                    }
                });

            } catch (Exception e) {
                String mensajeError = e.getMessage();
                e.printStackTrace();

                runOnUiThread(() -> {
                    Toast.makeText(getApplicationContext(), "❌ ERROR NUBE: " + mensajeError, Toast.LENGTH_LONG).show();
                    btn.setEnabled(true);
                });
            }
        }).start();
    }

    private void abrirWhatsapp(String mensajeBase) {
        if (refugio.getNumCelular() == null || refugio.getNumCelular().trim().isEmpty()) {
            Toast.makeText(this, "El refugio no tiene WhatsApp registrado", Toast.LENGTH_SHORT).show();
            return;
        }
        String telefono = refugio.getNumCelular().trim().replace("+", "").replace(" ", "");
        if (!telefono.startsWith("51")) telefono = "51" + telefono;

        String mensajeFinal = "👋 ¡Hola " + refugio.getNombre() + "!\n\n"
                + "🐾 Estoy interesado en adoptar a *" + mascota.getNombre() + "*.\n\n"
                + "💬 Mensaje: " + mensajeBase;

        String url = "https://api.whatsapp.com/send?phone=" + telefono + "&text=" + Uri.encode(mensajeFinal);
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "Instala WhatsApp para continuar", Toast.LENGTH_SHORT).show();
        }
    }
}