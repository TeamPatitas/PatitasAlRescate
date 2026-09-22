package com.patitasalrescate.controllers.lists;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.patitasalrescate.R;
import com.patitasalrescate.data.mock.DAORefugio;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.ui.AdaptadorRefugios;

import java.util.List;

public class FragmentListarRefugios extends Fragment {
    private RecyclerView recycler;
    private DAORefugio dao;
    private TextView txtVacio;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fg_listar_refugios, container, false);

        dao = new DAORefugio(requireContext());

        recycler = view.findViewById(R.id.recycler_refugios);
        recycler.setLayoutManager(new LinearLayoutManager(requireContext()));
        txtVacio = view.findViewById(R.id.txt_refugios_vacio);

        cargarDatosLocal();
        updateTitle("Refugios");
        return view;
    }

    private void cargarDatosLocal() {
        if (dao != null) {
            List<Refugio> lista = dao.listarTodos();
            actualizarUI(lista);
        }
    }

    private void updateTitle(String title) {
        if (getActivity() instanceof AppCompatActivity) {
            AppCompatActivity activity = (AppCompatActivity) getActivity();
            if (activity.getSupportActionBar() != null) {
                activity.getSupportActionBar().setTitle(title);
            }
        }
    }

    private void actualizarUI(List<Refugio> lista) {
        if (lista == null || lista.isEmpty()) {
            if (recycler != null) recycler.setVisibility(View.GONE);
            if (txtVacio != null) txtVacio.setVisibility(View.VISIBLE);
        } else {
            if (recycler != null) {
                recycler.setVisibility(View.VISIBLE);
                recycler.setAdapter(new AdaptadorRefugios(requireContext(), lista));
            }
            if (txtVacio != null) txtVacio.setVisibility(View.GONE);
        }
    }
}