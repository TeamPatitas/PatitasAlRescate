package com.patitasalrescate.controllers.lists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.utils.PatitasSessionManager;
import com.patitasalrescate.data.mock.DAOFavoritos;
import com.patitasalrescate.data.mock.DAOMascota;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.ui.AdaptadorMascotas;

import java.util.List;

public class FragmentFavoritos extends Fragment {
    private RecyclerView recycler;
    private TextView txtVacio;
    private DAOFavoritos daoFavoritos;
    private DAOMascota daoMascota;
    private String idUsuario;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fg_favoritos, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewCompat.setOnApplyWindowInsetsListener(view.findViewById(R.id.misfavoritos), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        recycler = view.findViewById(R.id.recycler_mascotas);
        txtVacio = view.findViewById(R.id.txt_lista_vacia);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));

        daoFavoritos = new DAOFavoritos(requireContext());
        daoMascota = new DAOMascota(requireContext());

        idUsuario = PatitasSessionManager.getInstance(requireContext()).getUserId();
        if (idUsuario == null || idUsuario.isEmpty()) {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
            return;
        }
        cargarFavoritos();
        updateTitle("Mis Favoritos");
    }

    private void cargarFavoritos() {
        List<Mascota> favoritos = daoFavoritos.getFavoritosPorAdoptante(idUsuario);

        if (favoritos == null || favoritos.isEmpty()) {
            recycler.setVisibility(View.GONE);
            txtVacio.setVisibility(View.VISIBLE);
            txtVacio.setText("No tienes favoritos ❤️");
            return;
        }

        recycler.setVisibility(View.VISIBLE);
        txtVacio.setVisibility(View.GONE);

        AdaptadorMascotas adapter = new AdaptadorMascotas(
                favoritos,
                requireContext(),
                daoMascota,
                daoFavoritos
        );
        recycler.setAdapter(adapter);
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarFavoritos();
    }

    private void updateTitle(String title) {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(title);
            }
        }
    }
}
