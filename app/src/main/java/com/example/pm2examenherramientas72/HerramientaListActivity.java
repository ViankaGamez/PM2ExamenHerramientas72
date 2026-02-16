package com.example.pm2examenherramientas72;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pm2examenherramientas72.Adapters.HerramientaAdapter;
import com.example.pm2examenherramientas72.Configuraciones.SQLiteConexion;
import com.example.pm2examenherramientas72.Configuraciones.Transacciones;
import com.example.pm2examenherramientas72.Models.HerramientaItem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HerramientaListActivity extends AppCompatActivity {

    private RecyclerView recycler;
    private SearchView searchView;
    private HerramientaAdapter adapter;
    private List<HerramientaItem> data = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_herramienta_list);

        recycler = findViewById(R.id.recyclerHerramientas);
        searchView = findViewById(R.id.searchView);

        recycler.setLayoutManager(new LinearLayoutManager(this));

        seedTecnicosSiVacio();

        // Cargar desde SQLite usando JOIN
        data = obtenerHerramientasConAsignacionActiva();

        adapter = new HerramientaAdapter(this, data, item -> {

            // Si está DISPONIBLE -> asignar
            if ("DISPONIBLE".equalsIgnoreCase(item.estado)) {
                Intent i = new Intent(this, AsignarActivity.class);
                i.putExtra("herramienta_id", item.id);
                i.putExtra("herramienta_nombre", item.nombre);
                startActivity(i);
                return;
            }

            // Si está ASIGNADA -> acciones (Devolver / Compartir)
            new AlertDialog.Builder(this)
                    .setTitle("Acciones")
                    .setMessage("Herramienta: " + item.nombre)
                    .setPositiveButton("Marcar devolución", (d, w) -> marcarDevolucion(item.id))
                    .setNeutralButton("Compartir resumen", (d, w) -> compartirResumen(item))
                    .setNegativeButton("Cancelar", (d, w) -> d.dismiss())
                    .show();
        });

        recycler.setAdapter(adapter);

        // SearchView filtra por nombre herramienta, técnico o especificaciones
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        recargarLista();
    }

    private void recargarLista() {
        List<HerramientaItem> nuevos = obtenerHerramientasConAsignacionActiva();
        adapter.updateData(nuevos);
    }

    private void marcarDevolucion(int herramientaId) {
        SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.dbname, null, Transacciones.dbversion);
        SQLiteDatabase db = conexion.getWritableDatabase();

        // Fecha actual en formato yyyy-MM-dd HH:mm
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        String ahora = sdf.format(new Date());

        // 1) Set fecha_devolucion a la asignación activa (la que no tiene devolución)
        ContentValues updAsign = new ContentValues();
        updAsign.put(Transacciones.a_fecha_devolucion, ahora);

        int filas = db.update(
                Transacciones.tbAsignaciones,
                updAsign,
                Transacciones.a_herramienta_id + "=? AND " + Transacciones.a_fecha_devolucion + " IS NULL",
                new String[]{String.valueOf(herramientaId)}
        );

        // 2) Cambiar estado de la herramienta a DISPONIBLE
        ContentValues updHerr = new ContentValues();
        updHerr.put(Transacciones.h_estado, "DISPONIBLE");

        db.update(
                Transacciones.tbHerramientas,
                updHerr,
                Transacciones.h_id + "=?",
                new String[]{String.valueOf(herramientaId)}
        );

        db.close();

        if (filas > 0) {
            new AlertDialog.Builder(this)
                    .setTitle("Devolución")
                    .setMessage("Devolución registrada correctamente.")
                    .setPositiveButton("OK", (d,w)-> d.dismiss())
                    .show();

            recargarLista();
        } else {
            new AlertDialog.Builder(this)
                    .setTitle("Aviso")
                    .setMessage("No se encontró asignación activa para devolver.")
                    .setPositiveButton("OK", (d,w)-> d.dismiss())
                    .show();
        }
    }

    private void compartirResumen(HerramientaItem item) {
        String tecnico = (item.tecnicoNombre == null) ? "-" : item.tecnicoNombre;
        String fin = (item.fechaFin == null) ? "-" : item.fechaFin;

        String resumen =
                "Resumen de Asignación\n" +
                        "Herramienta: " + item.nombre + "\n" +
                        "Técnico: " + tecnico + "\n" +
                        "Entrega programada: " + fin + "\n" +
                        "Estado: " + item.estado + "\n";

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_SUBJECT, "Resumen de herramienta");
        i.putExtra(Intent.EXTRA_TEXT, resumen);

        startActivity(Intent.createChooser(i, "Compartir con..."));
    }

    private void seedTecnicosSiVacio() {
        SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.dbname, null, Transacciones.dbversion);
        SQLiteDatabase db = conexion.getWritableDatabase();

        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + Transacciones.tbTecnicos, null);
        int count = 0;
        if (c.moveToFirst()) count = c.getInt(0);
        c.close();

        if (count == 0) {
            ContentValues t1 = new ContentValues();
            t1.put(Transacciones.t_nombre, "Juan Pérez");
            t1.put(Transacciones.t_telefono, "8888-8888");
            t1.put(Transacciones.t_especialidad, "Electricidad");

            ContentValues t2 = new ContentValues();
            t2.put(Transacciones.t_nombre, "Ana López");
            t2.put(Transacciones.t_telefono, "9999-9999");
            t2.put(Transacciones.t_especialidad, "Mecánica");

            ContentValues t3 = new ContentValues();
            t3.put(Transacciones.t_nombre, "Suyapa Melgar");
            t3.put(Transacciones.t_telefono, "9876-5432");
            t3.put(Transacciones.t_especialidad, "Mecánica");

            ContentValues t4 = new ContentValues();
            t4.put(Transacciones.t_nombre, "Miguel Sanchez");
            t4.put(Transacciones.t_telefono, "8765-4321");
            t4.put(Transacciones.t_especialidad, "Mecánica");

            db.insert(Transacciones.tbTecnicos, null, t1);
            db.insert(Transacciones.tbTecnicos, null, t2);
            db.insert(Transacciones.tbTecnicos, null, t3);
            db.insert(Transacciones.tbTecnicos, null, t4);
        }

        db.close();
    }

    private List<HerramientaItem> obtenerHerramientasConAsignacionActiva() {
        List<HerramientaItem> lista = new ArrayList<>();

        SQLiteConexion conexion = new SQLiteConexion(this, Transacciones.dbname, null, Transacciones.dbversion);
        SQLiteDatabase db = conexion.getReadableDatabase();

        String sql =
                "SELECT h.id, h.nombre, h.especificaciones, h.estado, " +
                        "       t.nombre AS tecnico_nombre, a.fecha_fin, a.fecha_devolucion " +
                        "FROM " + Transacciones.tbHerramientas + " h " +
                        "LEFT JOIN ( " +
                        "    SELECT a1.* FROM " + Transacciones.tbAsignaciones + " a1 " +
                        "    WHERE a1.fecha_devolucion IS NULL " +
                        "      AND a1.id = ( " +
                        "          SELECT MAX(a2.id) FROM " + Transacciones.tbAsignaciones + " a2 " +
                        "          WHERE a2.herramienta_id = a1.herramienta_id " +
                        "            AND a2.fecha_devolucion IS NULL " +
                        "      ) " +
                        ") a ON a.herramienta_id = h.id " +
                        "LEFT JOIN " + Transacciones.tbTecnicos + " t ON t.id = a.tecnico_id " +
                        "ORDER BY " +
                        "  CASE WHEN a.fecha_fin IS NULL THEN 1 ELSE 0 END, " +
                        "  a.fecha_fin ASC;";

        Cursor c = db.rawQuery(sql, null);

        while (c.moveToNext()) {
            int id = c.getInt(0);
            String nombre = c.getString(1);
            String especificaciones = c.getString(2);
            String estado = c.getString(3);
            String tecnicoNombre = c.isNull(4) ? null : c.getString(4);
            String fechaFin = c.isNull(5) ? null : c.getString(5);
            String fechaDevolucion = c.isNull(6) ? null : c.getString(6);

            lista.add(new HerramientaItem(id, nombre, especificaciones, estado, tecnicoNombre, fechaFin, fechaDevolucion));
        }

        c.close();
        db.close();

        return lista;
    }
}
