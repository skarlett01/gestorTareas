package Data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tareas.db";
    private static final int DATABASE_VERSION = 1;

    private static final String CREATE_TABLE_ESTADO_TAREA = "create table " + TareaContract.EstadoTareaEntry.TABLE_ESTADO_TAREA + "("
            + TareaContract.EstadoTareaEntry.COLUMN_ID_ESTADO + " integer primary key autoincrement, "
            + TareaContract.EstadoTareaEntry.COLUMN_NOMBRE_ESTADO + " text not null, "
            + TareaContract.EstadoTareaEntry.COLUMN_DESCRIPCION_ESTADO + " text not null);";

    private static final String CREATE_TABLE_USUARIO = "create table " + TareaContract.UsuarioEntry.TABLE_USUARIO + "("
            + TareaContract.UsuarioEntry.COLUMN_ID_USUARIO + " integer primary key autoincrement, "
            + TareaContract.UsuarioEntry.COLUMN_NOMBRE + " text not null, "
            + TareaContract.UsuarioEntry.COLUMN_CORREO + " text not null, "
            + TareaContract.UsuarioEntry.COLUMN_CONTRASENA + " text not null);";

    private static final String CREATE_TABLE_TAREA = "create table "
            + TareaContract.TareaEntry.TABLE_TAREA + "("
            + TareaContract.TareaEntry.COLUMN_ID + " integer primary key autoincrement, "
            + TareaContract.TareaEntry.COLUMN_TITULO + " text not null, "
            + TareaContract.TareaEntry.COLUMN_DESCRIPCION + " text not null, "
            + TareaContract.TareaEntry.COLUMN_ID_ESTADO + " integer, "
            + TareaContract.TareaEntry.COLUMN_ID_USUARIO + " integer, "
            + "FOREIGN KEY(" + TareaContract.TareaEntry.COLUMN_ID_ESTADO + ") REFERENCES " + TareaContract.EstadoTareaEntry.TABLE_ESTADO_TAREA + "(" + TareaContract.EstadoTareaEntry.COLUMN_ID_ESTADO + "), "
            + "FOREIGN KEY(" + TareaContract.TareaEntry.COLUMN_ID_USUARIO + ") REFERENCES " + TareaContract.UsuarioEntry.TABLE_USUARIO + "(" + TareaContract.UsuarioEntry.COLUMN_ID_USUARIO + "));";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ESTADO_TAREA);
        db.execSQL(CREATE_TABLE_USUARIO);
        db.execSQL(CREATE_TABLE_TAREA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TareaContract.TareaEntry.TABLE_TAREA);
        db.execSQL("DROP TABLE IF EXISTS " + TareaContract.EstadoTareaEntry.TABLE_ESTADO_TAREA);
        db.execSQL("DROP TABLE IF EXISTS " + TareaContract.UsuarioEntry.TABLE_USUARIO);
        onCreate(db);
    }

    public long crearUsuario(String nombre, String correo, String contrasena) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TareaContract.UsuarioEntry.COLUMN_NOMBRE, nombre);
        values.put(TareaContract.UsuarioEntry.COLUMN_CORREO, correo);
        values.put(TareaContract.UsuarioEntry.COLUMN_CONTRASENA, contrasena);
        return db.insert(TareaContract.UsuarioEntry.TABLE_USUARIO, null, values);
    }

    public long crearTarea(String titulo, String descripcion, int idUsuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TareaContract.TareaEntry.COLUMN_TITULO, titulo);
        values.put(TareaContract.TareaEntry.COLUMN_DESCRIPCION, descripcion);
        values.put(TareaContract.TareaEntry.COLUMN_ID_ESTADO, 1);
        values.put(TareaContract.TareaEntry.COLUMN_ID_USUARIO, idUsuario);
        return db.insert(TareaContract.TareaEntry.TABLE_TAREA, null, values);
    }

    public int actualizarEstadoTarea(int idTarea, boolean completada) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(TareaContract.TareaEntry.COLUMN_ID_ESTADO, completada ? 2 : 1);
        return db.update(TareaContract.TareaEntry.TABLE_TAREA, values, TareaContract.TareaEntry.COLUMN_ID + " = ?", new String[]{String.valueOf(idTarea)});
    }

    public List<Bundle> getTareasByUser(int idUsuario) {
        List<Bundle> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        try (Cursor cursor = db.query(TareaContract.TareaEntry.TABLE_TAREA,
                new String[]{TareaContract.TareaEntry.COLUMN_ID, TareaContract.TareaEntry.COLUMN_TITULO, TareaContract.TareaEntry.COLUMN_DESCRIPCION, TareaContract.TareaEntry.COLUMN_ID_ESTADO},
                TareaContract.TareaEntry.COLUMN_ID_USUARIO + " = ?",
                new String[]{String.valueOf(idUsuario)}, null, null, null)) {

            if (cursor.moveToFirst()) {
                int idCol = cursor.getColumnIndexOrThrow(TareaContract.TareaEntry.COLUMN_ID);
                int tituloCol = cursor.getColumnIndexOrThrow(TareaContract.TareaEntry.COLUMN_TITULO);
                int descripcionCol = cursor.getColumnIndexOrThrow(TareaContract.TareaEntry.COLUMN_DESCRIPCION);
                int estadoCol = cursor.getColumnIndexOrThrow(TareaContract.TareaEntry.COLUMN_ID_ESTADO);

                do {
                    Bundle taskBundle = new Bundle();
                    taskBundle.putInt(TareaContract.TareaEntry.COLUMN_ID, cursor.getInt(idCol));
                    taskBundle.putString(TareaContract.TareaEntry.COLUMN_TITULO, cursor.getString(tituloCol));
                    taskBundle.putString(TareaContract.TareaEntry.COLUMN_DESCRIPCION, cursor.getString(descripcionCol));
                    taskBundle.putInt(TareaContract.TareaEntry.COLUMN_ID_ESTADO, cursor.getInt(estadoCol));
                    taskList.add(taskBundle);
                } while (cursor.moveToNext());
            }
        }
        return taskList;
    }
}
