package com.patitasalrescate.controllers.management;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.R;
import com.patitasalrescate.data_access.DAOAdopcion;
import com.patitasalrescate.data_access.DAOMascota;
import com.patitasalrescate.data_access.DAORefugio;
import com.patitasalrescate.model.Adopcion;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.model.Refugio;
import java.util.UUID;

public class ActividadAdopcion extends AppCompatActivity {

    private DAOMascota daoMascota;
    private DAORefugio daoRefugio;
    private DAOAdopcion daoAdopcion;

    private String idMascota;
    private String idAdoptante;
    private Mascota mascota;
    private Refugio refugio;
    private EditText edtMensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_adopcion);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        daoMascota = new DAOMascota(this);
        daoRefugio = new DAORefugio(this);
        daoAdopcion = new DAOAdopcion(this);

        idMascota = getIntent().getStringExtra("id_mascota_key");
        idAdoptante = PatitasSessionManager.getInstance(this).getUserId();

        if (idMascota == null || idAdoptante == null || idAdoptante.isEmpty()) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
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
        if (refugio == null) {
            // Si no existe, creamos uno mock para que la demo no se rompa
            refugio = new Refugio();
            refugio.setIdRefugio(mascota.getIdRefugio());
            refugio.setNombre("Refugio Demo");
            refugio.setDireccion("Calle Demo 123");
            refugio.setNumCelular("987654321");
            refugio.setCorreo("demo@refugio.com");
            refugio.setPassword("123");
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
        
        Adopcion nuevaAdopcion = new Adopcion(
                UUID.randomUUID().toString(),
                idAdoptante,
                mascota.getIdMascota(),
                refugio.getIdRefugio(),
                "EN_PROCESO",
                textoIngresado
        );

        daoAdopcion.insertar(nuevaAdopcion);
        mascota.setEstado("EN_PROCESO");
        daoMascota.actualizar(mascota);

        Toast.makeText(this, "¡Solicitud enviada (Demo)! Redirigiendo a WhatsApp...", Toast.LENGTH_SHORT).show();
        abrirWhatsapp(textoIngresado);
        finish();
    }

    private void abrirWhatsapp(String mensajeBase) {
        String telefono = "51987654321"; // Teléfono demo
        if (refugio.getNumCelular() != null && !refugio.getNumCelular().isEmpty()) {
             telefono = "51" + refugio.getNumCelular();
        }

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
