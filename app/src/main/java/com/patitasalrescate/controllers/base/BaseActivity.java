package com.patitasalrescate.controllers.base;

import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.patitasalrescate.utils.ApiApp;

/**
 * Base de todas las pantallas: muestra un banner rojo superior cuando no hay internet,
 * con botón Reintentar. Los fragments quedan cubiertos por su activity anfitriona.
 */
public abstract class BaseActivity extends AppCompatActivity {

    private LinearLayout bannerOffline;
    private ConnectivityManager.NetworkCallback networkCallback;

    @Override
    protected void onResume() {
        super.onResume();
        actualizarBanner();
        registrarCallback();
    }

    @Override
    protected void onPause() {
        desregistrarCallback();
        super.onPause();
    }

    private void registrarCallback() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (cm == null) return;
            desregistrarCallback();
            networkCallback = new ConnectivityManager.NetworkCallback() {
                @Override public void onAvailable(Network network) {
                    runOnUiThread(() -> actualizarBanner());
                }
                @Override public void onLost(Network network) {
                    runOnUiThread(() -> actualizarBanner());
                }
            };
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            cm.registerNetworkCallback(request, networkCallback);
        } catch (Exception ignored) { }
    }

    private void desregistrarCallback() {
        if (networkCallback == null) return;
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            if (cm != null) cm.unregisterNetworkCallback(networkCallback);
        } catch (Exception ignored) { }
        networkCallback = null;
    }

    private void actualizarBanner() {
        if (ApiApp.esOnline()) {
            ocultarBanner();
        } else {
            mostrarBanner();
        }
    }

    private void mostrarBanner() {
        if (bannerOffline != null) return;
        ViewGroup raiz = findViewById(android.R.id.content);
        if (raiz == null) return;

        LinearLayout barra = new LinearLayout(this);
        barra.setOrientation(LinearLayout.HORIZONTAL);
        barra.setGravity(Gravity.CENTER_VERTICAL);
        barra.setBackgroundColor(0xFFD32F2F);
        barra.setElevation(dp(8));
        int pad = dp(12);
        barra.setPadding(pad, pad, pad, pad);

        TextView texto = new TextView(this);
        texto.setText("Sin internet: verás datos guardados. Tus cambios no se guardarán.");
        texto.setTextColor(0xFFFFFFFF);
        texto.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        texto.setTypeface(texto.getTypeface(), Typeface.BOLD);
        LinearLayout.LayoutParams paramsTexto = new LinearLayout.LayoutParams(
                0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        texto.setLayoutParams(paramsTexto);

        Button reintentar = new Button(this);
        reintentar.setText("Reintentar");
        reintentar.setAllCaps(false);
        reintentar.setTextColor(0xFFD32F2F);
        reintentar.setBackgroundColor(0xFFFFFFFF);
        reintentar.setOnClickListener(v -> {
            if (ApiApp.esOnline()) {
                ocultarBanner();
                Toast.makeText(this, "Conexión restablecida", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Sigue sin internet", Toast.LENGTH_SHORT).show();
            }
        });

        barra.addView(texto);
        barra.addView(reintentar);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP;
        params.topMargin = altoBarraEstado() + dp(8);
        params.leftMargin = dp(12);
        params.rightMargin = dp(12);
        barra.setLayoutParams(params);

        raiz.addView(barra);
        bannerOffline = barra;
    }

    private void ocultarBanner() {
        if (bannerOffline == null) return;
        try {
            ViewGroup raiz = findViewById(android.R.id.content);
            if (raiz != null) raiz.removeView(bannerOffline);
        } catch (Exception ignored) { }
        bannerOffline = null;
    }

    private int dp(int valor) {
        return (int) (valor * getResources().getDisplayMetrics().density);
    }

    private int altoBarraEstado() {
        int id = getResources().getIdentifier("status_bar_height", "dimen", "android");
        return id > 0 ? getResources().getDimensionPixelSize(id) : 0;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
