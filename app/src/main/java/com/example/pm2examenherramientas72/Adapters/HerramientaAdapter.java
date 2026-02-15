package com.example.pm2examenherramientas72.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pm2examenherramientas72.Models.HerramientaItem;
import com.example.pm2examenherramientas72.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HerramientaAdapter extends RecyclerView.Adapter<HerramientaAdapter.VH> implements Filterable {

    private final Context context;
    private final List<HerramientaItem> listaOriginal;
    private final List<HerramientaItem> listaFiltrada;

    // Formato ISO “simple” recomendado para guardar/leer (ej: 2026-02-15 14:30)
    private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

    public HerramientaAdapter(Context context, List<HerramientaItem> data) {
        this.context = context;
        this.listaOriginal = data;
        this.listaFiltrada = new ArrayList<>(data);
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_herramienta, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH h, int position) {
        HerramientaItem item = listaFiltrada.get(position);

        h.txtNombre.setText(item.nombre);
        h.txtEstado.setText("Estado: " + item.estado);

        String tecnico = (item.tecnicoNombre == null) ? "Técnico: - " : "Técnico: " + item.tecnicoNombre;
        h.txtTecnico.setText(tecnico);

        String entrega = (item.fechaFin == null) ? "Entrega: - " : "Entrega: " + item.fechaFin;
        h.txtFechaFin.setText(entrega);

        // Colores según reglas del PDF:
        // Rojo: asignada y vencida (hoy > fecha_fin, sin devolucion)
        // Ámbar: asignada y faltan <=48h
        // Verde: devuelta (tiene fecha_devolucion)  (en esta pantalla normalmente no tendrás devolución activa)
        // Gris: disponible (sin asignación activa)
        aplicarColor(h.itemView, item);
    }

    private void aplicarColor(View itemView, HerramientaItem item) {
        // Disponible: no tiene asignación activa -> gris
        if (item.tecnicoNombre == null || item.fechaFin == null) {
            itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray));
            return;
        }

        // Si en el futuro traes fechaDevolucion en lista, verde
        if (item.fechaDevolucion != null && !item.fechaDevolucion.trim().isEmpty()) {
            itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_green_light));
            return;
        }

        // Si hay fechaFin, calculamos vencida / <=48h
        try {
            Date fin = sdf.parse(item.fechaFin);
            long ahora = System.currentTimeMillis();
            long diffMs = fin.getTime() - ahora;
            long diffHoras = diffMs / (1000L * 60L * 60L);

            if (diffMs < 0) {
                // vencida
                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_red_light));
            } else if (diffHoras <= 48) {
                // <= 48 horas
                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_orange_light));
            } else {
                // asignada pero aún falta más de 48h
                itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.holo_blue_light));
            }

        } catch (ParseException e) {
            // Si no pudo parsear, deja gris para evitar crash
            itemView.setBackgroundColor(ContextCompat.getColor(context, android.R.color.darker_gray));
        }
    }

    @Override
    public int getItemCount() {
        return listaFiltrada.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView txtNombre, txtTecnico, txtFechaFin, txtEstado;

        VH(View itemView) {
            super(itemView);
            txtNombre = itemView.findViewById(R.id.txtNombre);
            txtTecnico = itemView.findViewById(R.id.txtTecnico);
            txtFechaFin = itemView.findViewById(R.id.txtFechaFin);
            txtEstado = itemView.findViewById(R.id.txtEstado);
        }
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String q = (constraint == null) ? "" : constraint.toString().toLowerCase().trim();
                List<HerramientaItem> filtrados = new ArrayList<>();

                if (q.isEmpty()) {
                    filtrados.addAll(listaOriginal);
                } else {
                    for (HerramientaItem it : listaOriginal) {
                        String tecnico = (it.tecnicoNombre == null) ? "" : it.tecnicoNombre.toLowerCase();
                        String specs = (it.especificaciones == null) ? "" : it.especificaciones.toLowerCase();
                        String nombre = (it.nombre == null) ? "" : it.nombre.toLowerCase();

                        if (nombre.contains(q) || tecnico.contains(q) || specs.contains(q)) {
                            filtrados.add(it);
                        }
                    }
                }

                FilterResults r = new FilterResults();
                r.values = filtrados;
                return r;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                listaFiltrada.clear();
                //noinspection unchecked
                listaFiltrada.addAll((List<HerramientaItem>) results.values);
                notifyDataSetChanged();
            }
        };
    }
}
