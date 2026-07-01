package com.patitasalrescate.controllers.lists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.data_access.DAOMascota;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.ui.AdaptadorMascotas;

import java.util.ArrayList;
import java.util.List;

public class FragmentListarMascotas extends Fragment {
    private RecyclerView recycler;
    private TextView txtVacio;
    private Button btnEnAdopcion, btnAdoptados;
    private DAOMascota dao;
    private List<Mascota> listaCacheRefugio;
    private boolean esModoRefugio = false;
    private String idUsuario;
    private boolean verAdoptadosRefugio = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fg_listar_mascotas, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.listarmascotas), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        dao = new DAOMascota(requireContext());
        listaCacheRefugio = new ArrayList<>();

        PatitasSessionManager session = PatitasSessionManager.getInstance(requireContext());
        esModoRefugio = session.isRefugio();
        idUsuario = session.getUserId();

        if (idUsuario == null || idUsuario.isEmpty()) {
            Toast.makeText(requireContext(), "Error de sesión. Vuelve a ingresar.", Toast.LENGTH_SHORT).show();
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return;
        }

        recycler = view.findViewById(R.id.recycler_mascotas);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        txtVacio = view.findViewById(R.id.txt_lista_vacia);
        LinearLayout lyFiltros = view.findViewById(R.id.layout_filtros_refugio);
        btnEnAdopcion = view.findViewById(R.id.btn_filtro_en_adopcion);
        btnAdoptados = view.findViewById(R.id.btn_filtro_adoptados);

        if (esModoRefugio) {
            updateTitle("Gestionar Mis Mascotas");
            lyFiltros.setVisibility(View.VISIBLE);

            btnEnAdopcion.setOnClickListener(v -> {
                verAdoptadosRefugio = false;
                actualizarEstiloFiltros();
                filtrarYMostrarRefugio();
            });

            btnAdoptados.setOnClickListener(v -> {
                verAdoptadosRefugio = true;
                actualizarEstiloFiltros();
                filtrarYMostrarRefugio();
            });
            actualizarEstiloFiltros();
        } else {
            updateTitle("Mascotas en Adopción");
            lyFiltros.setVisibility(View.GONE);
        }
        cargarDatosLocales();
    }

    private void updateTitle(String title) {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(title);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (idUsuario != null) {
            cargarDatosLocales();
        }
    }

    private void cargarDatosLocales() {
        try {
            if (esModoRefugio) {
                if (idUsuario != null) {
                    listaCacheRefugio = dao.listarPorRefugio(idUsuario);
                    filtrarYMostrarRefugio();
                }
            } else {
                List<Mascota> lista = dao.listarDisponibles();
                mostrarListaEnRecycler(lista);
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Error cargando datos locales", Toast.LENGTH_SHORT).show();
        }
    }

    private void filtrarYMostrarRefugio() {
        if (listaCacheRefugio == null) return;

        List<Mascota> listaFiltrada = new ArrayList<>();
        for (Mascota m : listaCacheRefugio) {
            String estado = m.getEstado() != null ? m.getEstado() : "DISPONIBLE";

            if (verAdoptadosRefugio) {
                if ("ADOPTADO".equals(estado)) listaFiltrada.add(m);
            } else {
                if (!"ADOPTADO".equals(estado)) listaFiltrada.add(m);
            }
        }
        mostrarListaEnRecycler(listaFiltrada);
    }

    private void mostrarListaEnRecycler(List<Mascota> listaFinal) {
        if (listaFinal == null || listaFinal.isEmpty()) {
            recycler.setVisibility(View.GONE);
            txtVacio.setVisibility(View.VISIBLE);
            txtVacio.setText(esModoRefugio ? "No tienes mascotas en esta categoría" : "No hay mascotas disponibles 🐾");
        } else {
            recycler.setVisibility(View.VISIBLE);
            txtVacio.setVisibility(View.GONE);

            AdaptadorMascotas adapter = new AdaptadorMascotas(
                    listaFinal,
                    esModoRefugio,
                    requireContext(),
                    dao
            );
            recycler.setAdapter(adapter);
        }
    }

    private void actualizarEstiloFiltros() {
        if (verAdoptadosRefugio) {
            btnAdoptados.setAlpha(1.0f);
            btnEnAdopcion.setAlpha(0.5f);
        } else {
            btnEnAdopcion.setAlpha(1.0f);
            btnAdoptados.setAlpha(0.5f);
        }
    }
}
