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
import com.patitasalrescate.data.remote.dto.ShelterResponse;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.PatitasSessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FragmentInicioRefugio extends Fragment {

    private TextView textoBienvenida;

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

        textoBienvenida = view.findViewById(R.id.txtBienvenidoRefugio);
        textoBienvenida.setGravity(Gravity.CENTER);
    }

    @Override
    public void onResume() {
        super.onResume();
        cargarNombreRefugio();
    }

    private void cargarNombreRefugio() {
        if (textoBienvenida == null) return;
        String shelterId = PatitasSessionManager.getInstance(requireContext()).getShelterId();
        if (shelterId == null || shelterId.isEmpty()) {
            textoBienvenida.setText("Mi Refugio");
            return;
        }
        textoBienvenida.setText("Cargando refugio…");
        ApiApp.client().shelters.getShelterById(shelterId).enqueue(new Callback<ShelterResponse>() {
            @Override public void onResponse(Call<ShelterResponse> call, Response<ShelterResponse> response) {
                if (!isAdded() || textoBienvenida == null) return;
                if (response.isSuccessful() && response.body() != null && response.body().name != null
                        && !response.body().name.isEmpty()) {
                    textoBienvenida.setText(response.body().name);
                } else {
                    textoBienvenida.setText("Mi Refugio");
                }
            }
            @Override public void onFailure(Call<ShelterResponse> call, Throwable error) {
                if (!isAdded() || textoBienvenida == null) return;
                textoBienvenida.setText("Mi Refugio");
            }
        });
    }
}