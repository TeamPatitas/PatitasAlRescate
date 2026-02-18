package com.patitasalrescate.Controllers;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.accesoADatos.DAOFavoritos;
import com.patitasalrescate.accesoADatos.DAOMascota;
import com.patitasalrescate.accesoADatos.SupabaseService;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.ui.AdaptadorMascotas;

import java.util.List;

public class ActividadMisFavoritos extends AppCompatActivity {

    private RecyclerView recycler;
    private TextView txtVacio;

    private DAOFavoritos daoFavoritos;
    private DAOMascota daoMascota;
    private SupabaseService supabase;

    private String idUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_mis_favoritos);

        recycler = findViewById(R.id.recycler_mascotas);
        txtVacio = findViewById(R.id.txt_lista_vacia);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        daoFavoritos = new DAOFavoritos(this);
        daoMascota = new DAOMascota(this);
        supabase = new SupabaseService();

        // ID adoptante recibido
        idUsuario = getIntent().getStringExtra(
                ActividadIniciarSesion.EXTRA_ID_USUARIO
        );

        if (idUsuario == null) {
            finish();
            return;
        }

        cargarFavoritos();
    }

    // ===== CARGAR LISTA =====
    private void cargarFavoritos() {

        List<Mascota> favoritos =
                daoFavoritos.getFavoritosPorAdoptante(idUsuario);

        if (favoritos == null || favoritos.isEmpty()) {

            recycler.setVisibility(View.GONE);
            txtVacio.setVisibility(View.VISIBLE);
            txtVacio.setText("No tienes favoritos ❤️");
            return;
        }

        recycler.setVisibility(View.VISIBLE);
        txtVacio.setVisibility(View.GONE);

        // 🔥 USAMOS TU ADAPTADOR REAL
        AdaptadorMascotas adapter =
                new AdaptadorMascotas(
                        favoritos,
                        this,
                        idUsuario,
                        daoMascota,
                        supabase,
                        daoFavoritos
                );

        recycler.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        cargarFavoritos(); // refresca automáticamente
    }
}
