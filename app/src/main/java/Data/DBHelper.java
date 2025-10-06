package Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
}
