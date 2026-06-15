package com.patitasalrescate.controllers.lists;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.controllers.auth.ActividadIniciarSesion;
import com.patitasalrescate.data_access.DAOFavoritos;
import com.patitasalrescate.data_access.DAOMascota;
import com.patitasalrescate.data_access.SupabaseService;
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.misfavoritos), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recycler = findViewById(R.id.recycler_mascotas);
        txtVacio = findViewById(R.id.txt_lista_vacia);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        daoFavoritos = new DAOFavoritos(this);
        daoMascota = new DAOMascota(this);
        supabase = new SupabaseService();

        idUsuario = getIntent().getStringExtra(
                ActividadIniciarSesion.EXTRA_ID_USUARIO
        );
        if (idUsuario == null) {
            finish();
            return;
        }
        cargarFavoritos();
    }
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
        cargarFavoritos();
    }
}