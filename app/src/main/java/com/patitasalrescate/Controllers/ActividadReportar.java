package com.patitasalrescate.Controllers;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.patitasalrescate.R;

public class ActividadReportar extends AppCompatActivity {

    private EditText txtAsunto, txtMensaje;
    private Button btnEnviar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.ly_reportar);

        txtAsunto = findViewById(R.id.txtAsunto);
        txtMensaje = findViewById(R.id.txtMensaje);
        btnEnviar = findViewById(R.id.btnEnviarReporte);

        btnEnviar.setOnClickListener(v -> {
            if (txtAsunto.getText().toString().isEmpty() ||
                    txtMensaje.getText().toString().isEmpty()) {

                Toast.makeText(this,
                        "Complete todos los campos",
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this,
                        "Reporte enviado correctamente ✅",
                        Toast.LENGTH_LONG).show();

                txtAsunto.setText("");
                txtMensaje.setText("");
            }
        });
    }
}
