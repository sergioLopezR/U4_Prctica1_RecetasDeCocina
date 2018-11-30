package mx.edu.ittepic.u4_prctica1_recetasdecocina;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BaseDatos extends SQLiteOpenHelper {

    public BaseDatos(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version); //SQLiteOpenHelper es el equibalente al phpMyAdmin
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //Se ejecuta cuando la aplicacion se ejecuta en el celular
        //Sirve para construir en el SQLite que esta en celular las tablas que la APP requiere para funcionar.

        db.execSQL("CREATE TABLE RECETA(ID INTEGER PRIMARY KEY NOT NULL, NOMBRE VARCHAR(200), INGREDIENTES VARCHAR(1000), PREPARACION VARCHAR(1000), OBSERVACIONES VARCHAR(500))");//execSQL-----> Funciona para insert, create table, delete, update
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) { //

    }
}
