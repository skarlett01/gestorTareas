package Data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "tareas.db";
    private static final int DATABASE_VERSION = 1;

    public static final String TABLE_TAREAS = "tareas";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_TAREA = "tarea";
    public static final String COLUMN_COMPLETADA = "completada";

    private static final String DATABASE_CREATE = "create table "
            + TABLE_TAREAS + "(" + COLUMN_ID
            + " integer primary key autoincrement, " + COLUMN_TAREA
            + " text not null, " + COLUMN_COMPLETADA
            + " integer not null);";

    public SQLHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TAREAS);
        onCreate(db);
    }
}
