package com.patitasalrescate.controllers.feed;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.patitasalrescate.R;
import com.patitasalrescate.utils.PatitasSessionManager;

public class FragmentInicioRefugio extends Fragment {

    public FragmentInicioRefugio() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fg_inicio_refugio, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        PatitasSessionManager session = PatitasSessionManager.getInstance(requireContext());
        String nombreRefugio = session.getUserName();

        if (nombreRefugio == null || nombreRefugio.isEmpty()) {
            nombreRefugio = "Refugio (Modo Prueba)";
        }

        TextView textobienvenida = view.findViewById(R.id.txtBienvenidoRefugio);
        textobienvenida.setText("Bienvenido refugio: " + nombreRefugio);
        textobienvenida.setGravity(Gravity.CENTER);
    }
}