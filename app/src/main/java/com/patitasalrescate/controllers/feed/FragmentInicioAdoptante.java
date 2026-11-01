package com.patitasalrescate.controllers.feed;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.patitasalrescate.R;
import com.patitasalrescate.data.remote.dto.EventSummaryResponse;
import com.patitasalrescate.data.remote.dto.PetSummaryResponse;
import com.patitasalrescate.data.remote.dto.ShelterSummaryResponse;
import com.patitasalrescate.data.remote.dto.UserResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import com.patitasalrescate.model.Evento;
import com.patitasalrescate.model.Mascota;
import com.patitasalrescate.model.Refugio;
import com.patitasalrescate.ui.AdaptadorEventos;
import com.patitasalrescate.ui.AdaptadorMascotas;
import com.patitasalrescate.ui.AdaptadorRefugios;
import com.patitasalrescate.utils.ApiApp;
import com.patitasalrescate.utils.ApiPages;
import com.patitasalrescate.utils.PatitasSessionManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class FragmentInicioAdoptante extends Fragment {

    private TextView txtBienvenido;
    private View cardAvisoCorreoNoVerificado;
    private Button btnSolicitarVerificacionInicio;
    private TextView txtEstadoProximoEvento, txtEstadoUltimasMascotas, txtEstadoRefugiosDestacados;
    private RecyclerView recyclerProximoEvento, recyclerUltimasMascotas, recyclerRefugiosDestacados;

    public FragmentInicioAdoptante() {
        // Required empty public constructor
    }

    public static FragmentInicioAdoptante newInstance(String param1, String param2) {
        FragmentInicioAdoptante fragment = new FragmentInicioAdoptante();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
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

        txtBienvenido = view.findViewById(R.id.txtBienvenidoAdoptante);
        cardAvisoCorreoNoVerificado = view.findViewById(R.id.cardAvisoCorreoNoVerificado);
        btnSolicitarVerificacionInicio = view.findViewById(R.id.btnSolicitarVerificacionInicio);

        if (cardAvisoCorreoNoVerificado != null) {
            cardAvisoCorreoNoVerificado.setOnClickListener(v ->
                    startActivity(new android.content.Intent(requireContext(),
                            com.patitasalrescate.controllers.management.ActividadPerfilUsuario.class)));
        }
        if (btnSolicitarVerificacionInicio != null) {
            btnSolicitarVerificacionInicio.setOnClickListener(v -> solicitarCorreoVerificacion());
        }

        txtEstadoProximoEvento = view.findViewById(R.id.txtEstadoProximoEvento);
        txtEstadoUltimasMascotas = view.findViewById(R.id.txtEstadoUltimasMascotas);
        txtEstadoRefugiosDestacados = view.findViewById(R.id.txtEstadoRefugiosDestacados);

        recyclerProximoEvento = view.findViewById(R.id.recyclerProximoEvento);
        recyclerUltimasMascotas = view.findViewById(R.id.recyclerUltimasMascotas);
        recyclerRefugiosDestacados = view.findViewById(R.id.recyclerRefugiosDestacados);

        recyclerProximoEvento.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerUltimasMascotas.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerRefugiosDestacados.setLayoutManager(new LinearLayoutManager(requireContext()));

        String nombre = PatitasSessionManager.getInstance(requireContext()).getUserName();
        if (nombre != null && !nombre.trim().isEmpty()) {
            txtBienvenido.setText("¡Hola, " + nombre.trim() + "!");
        } else {
            txtBienvenido.setText("¡Bienvenido!");
        }

        // Aviso "Quiero adoptar pero no tengo refugio"
        view.findViewById(R.id.cardUserPublication).setOnClickListener(v ->
                startActivity(new android.content.Intent(requireContext(),
                        com.patitasalrescate.controllers.management.UserPublication.class)));

        // Enlaces "Ver todos/todas"
        view.findViewById(R.id.btnVerTodasMascotas).setOnClickListener(v -> navegarADestino(R.id.fragmentListarMascotas, R.id.itemListarMascotasAdoptante));
        view.findViewById(R.id.btnVerTodosRefugios).setOnClickListener(v -> navegarADestino(R.id.fragmentListarRefugios, R.id.itemListarRefugios));
        view.findViewById(R.id.btnVerTodosEventos).setOnClickListener(v -> navegarADestino(R.id.fragmentEventosLista, R.id.itemEventosAdoptante));
    }

    @Override
    public void onResume() {
        super.onResume();
        updateTitle("Inicio");
        if (ApiApp.client().session.isAuthenticated()) {
            verificarEstadoCorreo();
            cargarProximoEvento();
            cargarUltimasMascotas();
            cargarRefugiosDestacados();
        }
    }

    private void verificarEstadoCorreo() {
        ApiApp.client().admin.getCurrentUser().enqueue(new Callback<UserResponse>() {
            @Override
            public void onResponse(Call<UserResponse> call, Response<UserResponse> response) {
                if (!isAdded() || cardAvisoCorreoNoVerificado == null) return;
                if (response.isSuccessful() && response.body() != null) {
                    boolean verificado = Boolean.TRUE.equals(response.body().isEmailConfirmed);
                    cardAvisoCorreoNoVerificado.setVisibility(verificado ? View.GONE : View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<UserResponse> call, Throwable t) {
            }
        });
    }

    private void solicitarCorreoVerificacion() {
        if (!isAdded() || getContext() == null) return;
        if (!ApiApp.exigirOnline(requireContext())) return;
        btnSolicitarVerificacionInicio.setEnabled(false);
        btnSolicitarVerificacionInicio.setText("Enviando...");
        ApiApp.client().auth.sendVerificationEmail().enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (!isAdded() || getContext() == null) return;
                btnSolicitarVerificacionInicio.setEnabled(true);
                if (response.isSuccessful()) {
                    btnSolicitarVerificacionInicio.setText("Enviado");
                    Toast.makeText(requireContext(),
                            "Correo de verificación enviado. Revisa tu bandeja de entrada.",
                            Toast.LENGTH_LONG).show();
                } else {
                    btnSolicitarVerificacionInicio.setText("Verificar");
                    String msg = response.code() == 429
                            ? "Espera 1 minuto antes de volver a solicitar el correo"
                            : "No se pudo enviar el correo (HTTP " + response.code() + ")";
                    Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                if (!isAdded() || getContext() == null) return;
                btnSolicitarVerificacionInicio.setEnabled(true);
                btnSolicitarVerificacionInicio.setText("Verificar");
                Toast.makeText(requireContext(), "Sin conexión para enviar el correo", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void navegarADestino(int destinationId, int bottomMenuItemId) {
        if (getActivity() instanceof ActividadFeedAdoptante) {
            ActividadFeedAdoptante activity = (ActividadFeedAdoptante) getActivity();
            if (bottomMenuItemId != -1) {
                BottomNavigationView menu = activity.findViewById(R.id.menuInicioAdoptante);
                if (menu != null) {
                    menu.setSelectedItemId(bottomMenuItemId);
                    return;
                }
            }
            activity.navigate(destinationId);
        }
    }

    private void cargarProximoEvento() {
        txtEstadoProximoEvento.setVisibility(View.VISIBLE);
        txtEstadoProximoEvento.setText("Cargando próximos eventos...");
        recyclerProximoEvento.setVisibility(View.GONE);

        ApiPages.load(page -> ApiApp.client().events.getAllEvents(page, 50),
                data -> data.totalPages, data -> data.items,
                events -> {
                    if (!isAdded() || getContext() == null) return;
                    List<Evento> vigentes = new ArrayList<>();
                    if (events != null) {
                        for (EventSummaryResponse event : events) {
                            if (event == null || event.id == null || event.id.isEmpty()) continue;
                            if (Boolean.FALSE.equals(event.isActive)) continue;
                            if (ApiApp.esPasado(event.eventDate)) continue;
                            Evento item = ApiApp.event(event);
                            if (item.getDescripcion() == null) item.setDescripcion("");
                            vigentes.add(item);
                        }
                    }

                    if (vigentes.isEmpty()) {
                        recyclerProximoEvento.setVisibility(View.GONE);
                        txtEstadoProximoEvento.setVisibility(View.VISIBLE);
                        txtEstadoProximoEvento.setText("No hay próximos eventos programados");
                    } else {
                        vigentes.sort((a, b) -> {
                            long ma = ApiApp.millisEvento(a.getFecha());
                            long mb = ApiApp.millisEvento(b.getFecha());
                            if (ma < 0) ma = Long.MAX_VALUE;
                            if (mb < 0) mb = Long.MAX_VALUE;
                            return Long.compare(ma, mb);
                        });
                        List<Evento> masProximo = Collections.singletonList(vigentes.get(0));
                        txtEstadoProximoEvento.setVisibility(View.GONE);
                        recyclerProximoEvento.setVisibility(View.VISIBLE);
                        recyclerProximoEvento.setAdapter(new AdaptadorEventos(masProximo, requireContext(), false));
                    }
                }, error -> {
                    if (!isAdded()) return;
                    recyclerProximoEvento.setVisibility(View.GONE);
                    txtEstadoProximoEvento.setVisibility(View.VISIBLE);
                    txtEstadoProximoEvento.setText("No se pudieron cargar los próximos eventos");
                });
    }

    private void cargarUltimasMascotas() {
        txtEstadoUltimasMascotas.setVisibility(View.VISIBLE);
        txtEstadoUltimasMascotas.setText("Cargando mascotas recientes...");
        recyclerUltimasMascotas.setVisibility(View.GONE);

        ApiPages.load(page -> ApiApp.client().pets.getAllPets(page, 50),
                data -> data.totalPages, data -> data.items,
                pets -> {
                    if (!isAdded() || getContext() == null) return;
                    List<Mascota> disponibles = new ArrayList<>();
                    if (pets != null) {
                        for (PetSummaryResponse pet : pets) {
                            Mascota m = ApiApp.pet(pet);
                            if ("DISPONIBLE".equals(m.getEstado())) {
                                disponibles.add(m);
                            }
                        }
                    }

                    if (disponibles.isEmpty()) {
                        recyclerUltimasMascotas.setVisibility(View.GONE);
                        txtEstadoUltimasMascotas.setVisibility(View.VISIBLE);
                        txtEstadoUltimasMascotas.setText("No hay mascotas disponibles por ahora 🐾");
                    } else {
                        disponibles.sort((a, b) -> Long.compare(
                                ApiApp.millisEvento(b.getFechaPublicacion()),
                                ApiApp.millisEvento(a.getFechaPublicacion())
                        ));
                        List<Mascota> recientes = disponibles.subList(0, Math.min(3, disponibles.size()));
                        txtEstadoUltimasMascotas.setVisibility(View.GONE);
                        recyclerUltimasMascotas.setVisibility(View.VISIBLE);
                        recyclerUltimasMascotas.setAdapter(new AdaptadorMascotas(
                                new ArrayList<>(recientes), false, requireContext()));
                    }
                }, error -> {
                    if (!isAdded()) return;
                    recyclerUltimasMascotas.setVisibility(View.GONE);
                    txtEstadoUltimasMascotas.setVisibility(View.VISIBLE);
                    txtEstadoUltimasMascotas.setText("No se pudieron cargar las mascotas recientes");
                });
    }

    private void cargarRefugiosDestacados() {
        txtEstadoRefugiosDestacados.setVisibility(View.VISIBLE);
        txtEstadoRefugiosDestacados.setText("Cargando refugios...");
        recyclerRefugiosDestacados.setVisibility(View.GONE);

        ApiPages.load(page -> ApiApp.client().shelters.getAllShelters(page, 50),
                data -> data.totalPages, data -> data.items,
                shelters -> {
                    if (!isAdded() || getContext() == null) return;
                    List<Refugio> activos = new ArrayList<>();
                    if (shelters != null) {
                        for (ShelterSummaryResponse shelter : shelters) {
                            if (Boolean.TRUE.equals(shelter.isAvailable)) {
                                activos.add(ApiApp.shelter(shelter));
                            }
                        }
                    }

                    if (activos.isEmpty()) {
                        recyclerRefugiosDestacados.setVisibility(View.GONE);
                        txtEstadoRefugiosDestacados.setVisibility(View.VISIBLE);
                        txtEstadoRefugiosDestacados.setText("No hay refugios disponibles");
                    } else {
                        List<Refugio> destacados = activos.subList(0, Math.min(2, activos.size()));
                        txtEstadoRefugiosDestacados.setVisibility(View.GONE);
                        recyclerRefugiosDestacados.setVisibility(View.VISIBLE);
                        recyclerRefugiosDestacados.setAdapter(
                                new AdaptadorRefugios(requireContext(), new ArrayList<>(destacados)));
                    }
                }, error -> {
                    if (!isAdded()) return;
                    recyclerRefugiosDestacados.setVisibility(View.GONE);
                    txtEstadoRefugiosDestacados.setVisibility(View.VISIBLE);
                    txtEstadoRefugiosDestacados.setText("No se pudieron cargar los refugios");
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
