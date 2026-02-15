package com.example.pm2examenherramientas72;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pm2examenherramientas72.Configuraciones.SQLiteConexion;
import com.example.pm2examenherramientas72.Configuraciones.Transacciones;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class AsignarActivity extends AppCompatActivity {

    private TextView txtHerramienta;
    private Spinner spTecnicos;
    private EditText edtInicio, edtFin, edtNotas;
    private Button btnAsignar;

    private int herramientaId;
    private String herramientaNombre;

    // Para Spinner: guardamos ids y nombres en paralelo
    private final ArrayList<Integer> tecnicosIds = new ArrayList<>();
    private final ArrayList<String> tecnicosNombres = new ArrayList<>();

    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar);

        txtHerramienta = findViewById(R.id.txtHerramienta);
        spTecnicos = findViewById(R.id.spTecnicos);
        edtInicio = findViewById(R.id.edtInicio);
        edtFin = findViewById(R.id.edtFin);
        edtNotas = findViewById(R.id.edtNotas);
        btnAsignar = findViewById(R.id.btnAsignar);

        // Recibimos herramienta desde Intent
        herramientaId = getIntent().getIntExtra("herramienta_id", -1);
        herramientaNombre = getIntent().getStringExtra("herramienta_nombre");

        txtHerramienta.setText("Herramienta: " + herramientaNombre);

        cargarTecnicos();

        // Pickers simples: DatePicker + TimePicker
        edtInicio.setOnClickListener(v -> pickFechaHora(edtInicio));
        edtFin.setOnClickListener(v -> pickFechaHora(edtFin));

        btnAsignar.setOnClickListener(v -> intentarAsignar());
    }

    private void cargarTecnicos() {
        SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.dbname, null, Transacciones.dbversion);
        SQLiteDatabase db = conexion.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT " + Transacciones.t_id + ", " + Transacciones.t_nombre +
                " FROM " + Transacciones.tbTecnicos + " ORDER BY " + Transacciones.t_nombre, null);

        tecnicosIds.clear();
        tecnicosNombres.clear();

        while (c.moveToNext()) {
            tecnicosIds.add(c.getInt(0));
            tecnicosNombres.add(c.getString(1));
        }

        c.close();
        db.close();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, tecnicosNombres);
        spTecnicos.setAdapter(adapter);
    }

    private void pickFechaHora(EditText target) {
        Calendar now = Calendar.getInstance();

        new android.app.DatePickerDialog(this, (view, year, month, day) -> {
            Calendar picked = Calendar.getInstance();
            picked.set(Calendar.YEAR, year);
            picked.set(Calendar.MONTH, month);
            picked.set(Calendar.DAY_OF_MONTH, day);

            new android.app.TimePickerDialog(this, (tp, hour, minute) -> {
                picked.set(Calendar.HOUR_OF_DAY, hour);
                picked.set(Calendar.MINUTE, minute);
                target.setText(sdf.format(picked.getTime()));
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true).show();

        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void intentarAsignar() {
        if (herramientaId == -1) {
            mostrarAlerta("Error", "No se recibió la herramienta.");
            return;
        }

        if (tecnicosIds.isEmpty()) {
            mostrarAlerta("Sin técnicos", "No hay técnicos en la base de datos.");
            return;
        }

        String inicio = edtInicio.getText().toString().trim();
        String fin = edtFin.getText().toString().trim();

        // Validaciones obligatorias
        if (inicio.isEmpty() || fin.isEmpty()) {
            mostrarAlerta("Faltan datos", "Debe seleccionar fecha inicio y fecha fin.");
            return;
        }

        // Validación: fin >= inicio (comparando strings en formato yyyy-MM-dd HH:mm funciona)
        if (fin.compareTo(inicio) < 0) {
            mostrarAlerta("Fechas inválidas", "No se permite fecha fin menor a fecha inicio.");
            return;
        }

        int pos = spTecnicos.getSelectedItemPosition();
        int tecnicoId = tecnicosIds.get(pos);
        String tecnicoNombre = tecnicosNombres.get(pos);

        // Confirmación con AlertDialog (requisito)
        new AlertDialog.Builder(this)
                .setTitle("Confirmar asignación")
                .setMessage("¿Confirmar asignación de " + herramientaNombre + " a " + tecnicoNombre +
                        " del " + inicio + " al " + fin + "?")
                .setPositiveButton("Sí", (d, w) -> asignarEnBD(tecnicoId, inicio, fin))
                .setNegativeButton("No", (d, w) -> d.dismiss())
                .show();
    }

    private void asignarEnBD(int tecnicoId, String inicio, String fin) {
        SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.dbname, null, Transacciones.dbversion);
        SQLiteDatabase db = conexion.getWritableDatabase();

        // Evitar doble asignación activa (requisito)
        Cursor c = db.rawQuery(
                "SELECT COUNT(*) FROM " + Transacciones.tbAsignaciones +
                        " WHERE " + Transacciones.a_herramienta_id + "=? AND " + Transacciones.a_fecha_devolucion + " IS NULL",
                new String[]{String.valueOf(herramientaId)}
        );

        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();

        if (count > 0) {
            db.close();
            mostrarAlerta("No permitido", "Esta herramienta ya tiene una asignación activa.");
            return;
        }

        // Insertar en Asignaciones
        ContentValues asign = new ContentValues();
        asign.put(Transacciones.a_herramienta_id, herramientaId);
        asign.put(Transacciones.a_tecnico_id, tecnicoId);
        asign.put(Transacciones.a_fecha_inicio, inicio);
        asign.put(Transacciones.a_fecha_fin, fin);
        asign.putNull(Transacciones.a_fecha_devolucion);
        asign.put(Transacciones.a_notas_entrega, edtNotas.getText().toString().trim());
        asign.putNull(Transacciones.a_foto_entrega_uri);
        asign.putNull(Transacciones.a_foto_devolucion_uri);

        long res = db.insert(Transacciones.tbAsignaciones, null, asign);

        // Cambiar estado de Herramientas a ASIGNADA
        ContentValues upd = new ContentValues();
        upd.put(Transacciones.h_estado, "ASIGNADA");
        db.update(Transacciones.tbHerramientas, upd, Transacciones.h_id + "=?",
                new String[]{String.valueOf(herramientaId)});

        db.close();

        if (res > 0) {
            mostrarAlerta("Listo", "Asignación creada.");
            finish(); // vuelve a la lista
        } else {
            mostrarAlerta("Error", "No se pudo crear la asignación.");
        }
    }

    private void mostrarAlerta(String titulo, String mensaje) {
        new AlertDialog.Builder(this)
                .setTitle(titulo)
                .setMessage(mensaje)
                .setPositiveButton("OK", (d, w) -> d.dismiss())
                .show();
    }
}
