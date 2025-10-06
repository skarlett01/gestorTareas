package Data;

import android.provider.BaseColumns;

public final class TareaContract {
    private TareaContract() {}

    public static abstract class TareaEntry implements BaseColumns {
        public static final String TABLE_TAREA = "tarea";
        public static final String COLUMN_ID = "id_tareas";
        public static final String COLUMN_TITULO = "titulo";
        public static final String COLUMN_DESCRIPCION = "descripcion";
        public static final String COLUMN_ID_ESTADO = "id_estado";
        public static final String COLUMN_ID_USUARIO = "id_usuario";
    }

    public static abstract class EstadoTareaEntry implements BaseColumns {
        public static final String TABLE_ESTADO_TAREA = "estado_tarea";
        public static final String COLUMN_ID_ESTADO = "id_estado";
        public static final String COLUMN_NOMBRE_ESTADO = "nombre_estado";
        public static final String COLUMN_DESCRIPCION_ESTADO = "descripcion_tarea_estado";
    }

    public static abstract class UsuarioEntry implements BaseColumns {
        public static final String TABLE_USUARIO = "usuario";
        public static final String COLUMN_ID_USUARIO = "id_usuario";
        public static final String COLUMN_NOMBRE = "nombre";
        public static final String COLUMN_CORREO = "correo";
        public static final String COLUMN_CONTRASENA = "contrasena";
    }
}
