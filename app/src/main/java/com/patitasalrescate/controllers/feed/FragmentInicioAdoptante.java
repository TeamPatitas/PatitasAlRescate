package com.patitasalrescate.controllers.feed;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.patitasalrescate.R;
import com.patitasalrescate.controllers.management.ActividadPerfilUsuario;
import com.patitasalrescate.utils.PatitasSessionManager;

public class FragmentInicioAdoptante extends Fragment {
    public FragmentInicioAdoptante() {
        // Required empty public constructor
    }
    public static FragmentInicioAdoptante newInstance(String param1, String param2) {
        FragmentInicioAdoptante fragment = new FragmentInicioAdoptante();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        updateTitle("Inicio");
        return inflater.inflate(R.layout.fg_inicio_adoptante, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String nombre = PatitasSessionManager.getInstance(requireContext()).getUserName();
        TextView txtBienvenido = view.findViewById(R.id.txtBienvenidoAdoptante);
        txtBienvenido.setText("Bienvenido " + nombre);

        txtBienvenido.setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), ActividadPerfilUsuario.class));
        });
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