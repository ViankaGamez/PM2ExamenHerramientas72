package com.example.pm2examenherramientas72.Configuraciones;

public class Transacciones {

    // Nombre y versión de la BD
    public static final String dbname = "BD_EXAMEN";
    public static final int dbversion = 1;

    // Tablas
    public static final String tbHerramientas = "Herramientas";
    public static final String tbTecnicos = "Tecnicos";
    public static final String tbAsignaciones = "Asignaciones";

    // Campos Herramientas
    public static final String h_id = "id";
    public static final String h_nombre = "nombre";
    public static final String h_descripcion = "descripcion";
    public static final String h_especificaciones = "especificaciones";
    public static final String h_foto_uri = "foto_uri";
    public static final String h_estado = "estado"; // DISPONIBLE | ASIGNADA

    // Campos Tecnicos
    public static final String t_id = "id";
    public static final String t_nombre = "nombre";
    public static final String t_telefono = "telefono";
    public static final String t_especialidad = "especialidad";

    // Campos Asignaciones
    public static final String a_id = "id";
    public static final String a_herramienta_id = "herramienta_id";
    public static final String a_tecnico_id = "tecnico_id";
    public static final String a_fecha_inicio = "fecha_inicio";
    public static final String a_fecha_fin = "fecha_fin";             // entrega programada
    public static final String a_fecha_devolucion = "fecha_devolucion"; // null si no se entregó
    public static final String a_notas_entrega = "notas_entrega";
    public static final String a_foto_entrega_uri = "foto_entrega_uri";
    public static final String a_foto_devolucion_uri = "foto_devolucion_uri";

    // ********** DDL: Create tables **********

    // Crea tabla Herramientas
    public static final String CreateTableHerramientas =
            "CREATE TABLE IF NOT EXISTS " + tbHerramientas + " (" +
                    h_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    h_nombre + " TEXT NOT NULL, " +
                    h_descripcion + " TEXT NOT NULL, " +
                    h_especificaciones + " TEXT NOT NULL, " +
                    h_foto_uri + " TEXT, " +
                    h_estado + " TEXT NOT NULL DEFAULT 'DISPONIBLE'" +
                    ");";

    // Crea tabla Tecnicos
    public static final String CreateTableTecnicos =
            "CREATE TABLE IF NOT EXISTS " + tbTecnicos + " (" +
                    t_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    t_nombre + " TEXT NOT NULL, " +
                    t_telefono + " TEXT, " +
                    t_especialidad + " TEXT" +
                    ");";

    // Crea tabla Asignaciones con llaves foráneas
    public static final String CreateTableAsignaciones =
            "CREATE TABLE IF NOT EXISTS " + tbAsignaciones + " (" +
                    a_id + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    a_herramienta_id + " INTEGER NOT NULL, " +
                    a_tecnico_id + " INTEGER NOT NULL, " +
                    a_fecha_inicio + " TEXT NOT NULL, " +
                    a_fecha_fin + " TEXT NOT NULL, " +
                    a_fecha_devolucion + " TEXT, " +
                    a_notas_entrega + " TEXT, " +
                    a_foto_entrega_uri + " TEXT, " +
                    a_foto_devolucion_uri + " TEXT, " +
                    "FOREIGN KEY(" + a_herramienta_id + ") REFERENCES " + tbHerramientas + "(" + h_id + "), " +
                    "FOREIGN KEY(" + a_tecnico_id + ") REFERENCES " + tbTecnicos + "(" + t_id + ")" +
                    ");";

    // Drops
    public static final String DropTableHerramientas = "DROP TABLE IF EXISTS " + tbHerramientas;
    public static final String DropTableTecnicos = "DROP TABLE IF EXISTS " + tbTecnicos;
    public static final String DropTableAsignaciones = "DROP TABLE IF EXISTS " + tbAsignaciones;
}
