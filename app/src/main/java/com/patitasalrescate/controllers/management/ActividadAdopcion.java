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
import com.patitasalrescate.model.Adopcion;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.data.remote.dto.PetResponse;
import com.patitasalrescate.data.remote.dto.ShelterResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.util.UUID;

public class ActividadAdopcion extends com.patitasalrescate.controllers.base.BaseActivity {

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

        idMascota = getIntent().getStringExtra("id_mascota_key");
        idAdoptante = PatitasSessionManager.getInstance(this).getUserId();

        if (idMascota == null || idAdoptante == null || idAdoptante.isEmpty()) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        cargarDatosMascota();
    }

    private void cargarDatosMascota() {
        ApiApp.client().pets.getPetById(idMascota).enqueue(new Callback<PetResponse>() {
            @Override
            public void onResponse(Call<PetResponse> call, Response<PetResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    Toast.makeText(ActividadAdopcion.this, "Mascota no encontrada", Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                mascota = ApiApp.pet(response.body());
                cargarDatosRefugio();
            }

            @Override
            public void onFailure(Call<PetResponse> call, Throwable error) {
                if (!isFinishing()) {
                    Toast.makeText(ActividadAdopcion.this, "Sin conexión con mascotas", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });
    }

    private void cargarDatosRefugio() {
        String shelterId = mascota.getIdRefugio();
        if (shelterId == null || shelterId.isEmpty()) {
            // Fallback mock
            refugio = new Refugio();
            refugio.setIdRefugio("unknown");
            refugio.setNombre("Refugio Demo");
            refugio.setDireccion("Calle Demo 123");
            refugio.setNumCelular("987654321");
            refugio.setCorreo("demo@refugio.com");
            mostrarDatos();
            return;
        }

        ApiApp.client().shelters.getShelterById(shelterId).enqueue(new Callback<ShelterResponse>() {
            @Override
            public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (isFinishing() || isDestroyed()) return;
                if (!response.isSuccessful() || response.body() == null) {
                    // Fallback mock
                    refugio = new Refugio();
                    refugio.setIdRefugio(shelterId);
                    refugio.setNombre("Refugio Demo");
                    refugio.setDireccion("Calle Demo 123");
                    refugio.setNumCelular("987654321");
                    refugio.setCorreo("demo@refugio.com");
                } else {
                    refugio = ApiApp.shelter(response.body());
                }
                mostrarDatos();
            }

            @Override
            public void onFailure(Call<ShelterResponse> call, Throwable error) {
                if (!isFinishing()) {
                    // Fallback mock
                    refugio = new Refugio();
                    refugio.setIdRefugio(shelterId);
                    refugio.setNombre("Refugio Demo");
                    refugio.setDireccion("Calle Demo 123");
                    refugio.setNumCelular("987654321");
                    refugio.setCorreo("demo@refugio.com");
                    mostrarDatos();
                }
            }
        });
    }

    private void mostrarDatos() {
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

        // TODO: Replace with API call when adoption endpoint is available
        // For now just show success and open WhatsApp
        Toast.makeText(this, "¡Solicitud enviada! Redirigiendo a WhatsApp...", Toast.LENGTH_SHORT).show();
        abrirWhatsapp(textoIngresado);
        finish();
    }

    /** Normaliza el phoneNumber del refugio a formato WhatsApp (51 + 9 dígitos). Null si no hay. */
    private String normalizarTelefono(String crudo) {
        if (crudo == null) return null;
        String digitos = crudo.replaceAll("[^0-9]", "");
        if (digitos.startsWith("51") && digitos.length() > 9) return digitos;
        if (digitos.length() == 9) return "51" + digitos;
        return digitos.isEmpty() ? null : digitos;
    }

    private void abrirWhatsapp(String mensajeBase) {
        // Usa el phoneNumber real del refugio (nueva propiedad de la API).
        String telefono = normalizarTelefono(
                refugio == null ? null : refugio.getNumCelular());
        if (telefono == null) {
            Toast.makeText(this, "El refugio no tiene teléfono registrado", Toast.LENGTH_LONG).show();
            return;
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