package com.patitasalrescate.controllers.management;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.patitasalrescate.R;

/**
 * BottomSheet que pregunta al adoptante cómo desea contactar a la asociación.
 * Delega la acción concreta al listener (sin lógica de navegación propia).
 * La mascota/refugio se pasan desde ActividadPerfilMascota y se reenvían al listener.
 */
public class BottomSheetContactoAdopcion extends BottomSheetDialogFragment {

    /** Identifica el fragment en el back-stack del FragmentManager. */
    public static final String TAG = "BottomSheetContactoAdopcion";

    private static final String ARG_NOMBRE_MASCOTA   = "nombre_mascota";
    private static final String ARG_NOMBRE_REFUGIO   = "nombre_refugio";
    private static final String ARG_TELEFONO_REFUGIO  = "telefono_refugio";
    private static final String ARG_PET_ID           = "pet_id";
    private static final String ARG_SHELTER_ID       = "shelter_id";

    /** Acciones que la pantalla padre debe implementar. */
    public interface OnOpcionSeleccionadaListener {
        /** El adoptante elige enviar un mensaje rápido por WhatsApp. */
        void onMensajeRapido(String telefonoRefugio, String nombreMascota);
        /** El adoptante elige completar el formulario formal (genera PDF). */
        void onFormularioFormal(String petId, String shelterId,
                                String telefonoRefugio, String nombreMascota, String nombreRefugio);
    }

    private OnOpcionSeleccionadaListener listener;

    // -------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------

    public static BottomSheetContactoAdopcion newInstance(
            String nombreMascota, String nombreRefugio,
            String telefonoRefugio, String petId, String shelterId) {

        BottomSheetContactoAdopcion sheet = new BottomSheetContactoAdopcion();
        Bundle args = new Bundle();
        args.putString(ARG_NOMBRE_MASCOTA,   nombreMascota   == null ? "" : nombreMascota);
        args.putString(ARG_NOMBRE_REFUGIO,   nombreRefugio   == null ? "" : nombreRefugio);
        args.putString(ARG_TELEFONO_REFUGIO,  telefonoRefugio == null ? "" : telefonoRefugio);
        args.putString(ARG_PET_ID,           petId           == null ? "" : petId);
        args.putString(ARG_SHELTER_ID,       shelterId       == null ? "" : shelterId);
        sheet.setArguments(args);
        return sheet;
    }

    // -------------------------------------------------------------------
    // Ciclo de vida
    // -------------------------------------------------------------------

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnOpcionSeleccionadaListener) {
            listener = (OnOpcionSeleccionadaListener) context;
        } else if (getParentFragment() instanceof OnOpcionSeleccionadaListener) {
            listener = (OnOpcionSeleccionadaListener) getParentFragment();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.ly_bottom_sheet_contacto, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = requireArguments();
        final String nombreMascota   = args.getString(ARG_NOMBRE_MASCOTA,  "");
        final String nombreRefugio   = args.getString(ARG_NOMBRE_REFUGIO,  "");
        final String telefonoRefugio = args.getString(ARG_TELEFONO_REFUGIO, "");
        final String petId           = args.getString(ARG_PET_ID,          "");
        final String shelterId       = args.getString(ARG_SHELTER_ID,      "");

        Button btnMensaje    = view.findViewById(R.id.btn_contacto_whatsapp);
        Button btnFormulario = view.findViewById(R.id.btn_contacto_formulario);
        Button btnCancelar   = view.findViewById(R.id.btn_contacto_cancelar);

        btnMensaje.setOnClickListener(v -> {
            dismiss();
            if (listener != null) listener.onMensajeRapido(telefonoRefugio, nombreMascota);
        });

        btnFormulario.setOnClickListener(v -> {
            dismiss();
            if (listener != null)
                listener.onFormularioFormal(petId, shelterId, telefonoRefugio, nombreMascota, nombreRefugio);
        });

        btnCancelar.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }
}
