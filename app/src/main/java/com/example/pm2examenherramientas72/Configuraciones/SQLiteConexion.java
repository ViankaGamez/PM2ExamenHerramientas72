package com.example.pm2examenherramientas72.Configuraciones;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SQLiteConexion extends SQLiteOpenHelper {

    public SQLiteConexion(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(Transacciones.CreateTableHerramientas); // crea herramientas
        db.execSQL(Transacciones.CreateTableTecnicos);     // crea técnicos
        db.execSQL(Transacciones.CreateTableAsignaciones); // crea asignaciones
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(Transacciones.DropTableAsignaciones);
        db.execSQL(Transacciones.DropTableTecnicos);
        db.execSQL(Transacciones.DropTableHerramientas);
        onCreate(db);
    }

}
