package com.example.pm2examenherramientas72;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pm2examenherramientas72.Adapters.HerramientaAdapter;
import com.example.pm2examenherramientas72.Configuraciones.SQLiteConexion;
import com.example.pm2examenherramientas72.Configuraciones.Transacciones;
import com.example.pm2examenherramientas72.Models.HerramientaItem;

import java.util.ArrayList;
import java.util.List;

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
            // Solo permitir asignar si está DISPONIBLE
            if ("DISPONIBLE".equalsIgnoreCase(item.estado)) {
                android.content.Intent i = new android.content.Intent(this, AsignarActivity.class);
                i.putExtra("herramienta_id", item.id);
                i.putExtra("herramienta_nombre", item.nombre);
                startActivity(i);
            } else {
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setTitle("No disponible")
                        .setMessage("Esta herramienta ya está asignada.")
                        .setPositiveButton("OK", (d,w)->d.dismiss())
                        .show();
            }
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
        data.clear();
        data.addAll(obtenerHerramientasConAsignacionActiva());
        adapter.notifyDataSetChanged();
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

        // JOIN: trae herramienta + (si existe) última asignación activa (fecha_devolucion IS NULL) + técnico
        // Orden: próximas entregas primero; disponibles al final
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
                        "  CASE WHEN a.fecha_fin IS NULL THEN 1 ELSE 0 END, " +  // disponibles al final
                        "  a.fecha_fin ASC;";                                  // próximas entregas primero

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
