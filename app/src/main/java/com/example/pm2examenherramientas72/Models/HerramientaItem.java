package com.example.pm2examenherramientas72.Models;

public class HerramientaItem {
    public int id;
    public String nombre;
    public String especificaciones;
    public String estado;

    // datos del JOIN (pueden venir null si está DISPONIBLE)
    public String tecnicoNombre; // null si no hay asignación activa
    public String fechaFin;      // null si no hay asignación activa
    public String fechaDevolucion; // si quieres usarlo luego

    public HerramientaItem(int id, String nombre, String especificaciones, String estado,
                           String tecnicoNombre, String fechaFin, String fechaDevolucion) {
        this.id = id;
        this.nombre = nombre;
        this.especificaciones = especificaciones;
        this.estado = estado;
        this.tecnicoNombre = tecnicoNombre;
        this.fechaFin = fechaFin;
        this.fechaDevolucion = fechaDevolucion;
    }
}
