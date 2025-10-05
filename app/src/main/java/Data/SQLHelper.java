package Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tareas.db";
    private static final int DATABASE_VERSION = 1;
    //tabla de tarea
    public static final String TABLE_TAREA = "tarea";
    public static final String COLUMN_ID = "id_tareas";
    public static final String COLUMN_TITULO = "titulo";
    public static final String COLUMN_DESCRIPCION = "descripcion";
    //tabla de estados de la tarea
    public static final String COLUMN_ID_ESTADO = "id_estado";
    public static final String TABLE_ESTADO_TAREA = "estado_tarea";
    public static final String COLUMN_NOMBRE_ESTADO = "nombre_estado";
    public static final String COLUMN_DESCRIPCION_ESTADO = "descripcion_tarea_estado";
    //tabla de usuarios
    public static final String TABLE_USUARIO = "usuario";
    public static final String COLUMN_ID_USUARIO = "id_usuario";
    public static final String COLUMN_NOMBRE = "nombre";
    public static final String COLUMN_CORREO = "correo";
    public static final String COLUMN_CONTRASENA = "contrasena";

    private static final String CREATE_TABLE_TAREA = "create table "
            + TABLE_TAREA + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TITULO + " text not null, "
            + COLUMN_DESCRIPCION + " text not null, "
            + COLUMN_ID_ESTADO + " integer, "
            + COLUMN_ID_USUARIO + " integer, "
            + "FOREIGN KEY(" + COLUMN_ID_ESTADO + ") REFERENCES " + TABLE_ESTADO_TAREA + "(" + COLUMN_ID_ESTADO + "), "
            + "FOREIGN KEY(" + COLUMN_ID_USUARIO + ") REFERENCES " + TABLE_USUARIO + "(" + COLUMN_ID_USUARIO + "));";

    private static final String CREATE_TABLE_ESTADO_TAREA = "create table " + TABLE_ESTADO_TAREA + "("
            + COLUMN_ID_ESTADO + " integer primary key autoincrement, "
            + COLUMN_NOMBRE_ESTADO + " text not null, "
            + COLUMN_DESCRIPCION_ESTADO + " text not null);";

    private static final String CREATE_TABLE_USUARIO = "create table " + TABLE_USUARIO + "("
            + COLUMN_ID_USUARIO + " integer primary key autoincrement, "
            + COLUMN_NOMBRE + " text not null, "
            + COLUMN_CORREO + " text not null, "
            + COLUMN_CONTRASENA + " text not null);";

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(CREATE_TABLE_ESTADO_TAREA);
        database.execSQL(CREATE_TABLE_USUARIO);
        database.execSQL(CREATE_TABLE_TAREA);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAREA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ESTADO_TAREA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USUARIO);
        onCreate(db);
    }
}
