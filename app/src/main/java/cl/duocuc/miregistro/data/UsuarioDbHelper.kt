package cl.duocuc.miregistro.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.miregistro.model.Rol
import cl.duocuc.miregistro.model.Usuario

class UsuarioDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "miregistro.db"
        // Versión 4: incluye avatar, RUT y contraseña
        private const val VERSION_BD = 4

        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "id_rol"
        private const val ROL_NOMBRE = "nombre_rol"

        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_RUT = "rut"
        private const val USU_CONTRASENA = "contrasena" // Columna de contraseña
        private const val USU_CIUDAD = "ciudad"
        private const val USU_ROL = "id_rol"
        private const val USU_AVATAR = "avatar"

        private const val SQL_CREAR_ROLES =
            "CREATE TABLE $TABLA_ROLES (" +
                    "$ROL_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$ROL_NOMBRE TEXT NOT NULL UNIQUE)"

        private const val SQL_CREAR_USUARIOS =
            "CREATE TABLE $TABLA_USUARIOS (" +
                    "$USU_ID INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "$USU_NOMBRE TEXT NOT NULL, " +
                    "$USU_CORREO TEXT NOT NULL, " +
                    "$USU_RUT TEXT NOT NULL, " +
                    "$USU_CONTRASENA TEXT NOT NULL, " +
                    "$USU_CIUDAD TEXT NOT NULL, " +
                    "$USU_AVATAR BLOB, " +
                    "$USU_ROL INTEGER NOT NULL REFERENCES $TABLA_ROLES($ROL_ID))"

        private const val SQL_AGREGAR_AVATAR =
            "ALTER TABLE $TABLA_USUARIOS ADD COLUMN $USU_AVATAR BLOB"

        private const val SQL_AGREGAR_RUT =
            "ALTER TABLE $TABLA_USUARIOS ADD COLUMN $USU_RUT TEXT NOT NULL DEFAULT '15.834.207-3'"

        private const val SQL_AGREGAR_CONTRASENA =
            "ALTER TABLE $TABLA_USUARIOS ADD COLUMN $USU_CONTRASENA TEXT NOT NULL DEFAULT '123456'"

        private const val SQL_PRECARGAR_ROLES =
            "INSERT INTO $TABLA_ROLES ($ROL_ID, $ROL_NOMBRE) VALUES " +
                    "(1, 'R-01 Usuario Final'), (2, 'R-02 Soporte / Admin')"

        private const val SQL_PRECARGAR_USUARIOS =
            "INSERT INTO $TABLA_USUARIOS ($USU_NOMBRE, $USU_CORREO, $USU_RUT, $USU_CONTRASENA, $USU_CIUDAD, $USU_ROL) VALUES " +
                    "('Camila Andrea Rojas Peña', 'crojas@correo.ejemplo', '15.834.207-3', '123456', 'Maipú', 1), " +
                    "('Luis Muñoz', 'lmunoz@correo.ejemplo', '12.476.318-5', '123456', 'Santiago', 1), " +
                    "('Rosa Contreras', 'rcontreras@correo.ejemplo', '6.893.541-5', '123456', 'Valparaíso', 1), " +
                    "('Soporte PagoFlex', 'soporte@pagoflex.cl', '9.999.999-9', 'admin123', 'Providencia', 2)"

        private const val SQL_SELECT_USUARIOS =
            "SELECT u.*, r.$ROL_NOMBRE AS $ROL_NOMBRE " +
                    "FROM $TABLA_USUARIOS u " +
                    "INNER JOIN $TABLA_ROLES r ON u.$USU_ROL = r.$ROL_ID"
    }

    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREAR_ROLES)
        db.execSQL(SQL_CREAR_USUARIOS)
        db.execSQL(SQL_PRECARGAR_ROLES)
        db.execSQL(SQL_PRECARGAR_USUARIOS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        if (oldVersion < 2) {
            db.execSQL(SQL_AGREGAR_AVATAR)
        }
        if (oldVersion < 3) {
            db.execSQL(SQL_AGREGAR_RUT)
        }
        if (oldVersion < 4) {
            db.execSQL(SQL_AGREGAR_CONTRASENA)
        }
    }

    fun listarRoles(): List<Rol> {
        val roles = mutableListOf<Rol>()
        readableDatabase.rawQuery(
            "SELECT $ROL_ID, $ROL_NOMBRE FROM $TABLA_ROLES ORDER BY $ROL_ID", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                roles.add(
                    Rol(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow(ROL_ID)),
                        nombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE))
                    )
                )
            }
        }
        return roles
    }

    fun insertarUsuario(usuario: Usuario): Long =
        writableDatabase.insert(TABLA_USUARIOS, null, valoresDe(usuario))

    fun listarUsuarios(): List<Usuario> =
        consultarUsuarios("$SQL_SELECT_USUARIOS ORDER BY u.$USU_NOMBRE", null)

    fun buscarUsuarios(texto: String): List<Usuario> {
        val patron = "%$texto%"
        return consultarUsuarios(
            "$SQL_SELECT_USUARIOS WHERE u.$USU_NOMBRE LIKE ? OR u.$USU_CORREO LIKE ? OR u.$USU_RUT LIKE ? " +
                    "ORDER BY u.$USU_NOMBRE",
            arrayOf(patron, patron, patron)
        )
    }

    fun buscarPorId(id: Long): Usuario? =
        consultarUsuarios("$SQL_SELECT_USUARIOS WHERE u.$USU_ID = ?", arrayOf(id.toString()))
            .firstOrNull()

    fun actualizarUsuario(usuario: Usuario): Int =
        writableDatabase.update(
            TABLA_USUARIOS, valoresDe(usuario), "$USU_ID = ?", arrayOf(usuario.id.toString())
        )

    fun eliminarUsuario(id: Long): Int =
        writableDatabase.delete(TABLA_USUARIOS, "$USU_ID = ?", arrayOf(id.toString()))

    fun actualizarAvatar(id: Long, avatar: ByteArray): Int {
        val valores = ContentValues().apply { put(USU_AVATAR, avatar) }
        return writableDatabase.update(TABLA_USUARIOS, valores, "$USU_ID = ?", arrayOf(id.toString()))
    }

    // Permite cambiar la contraseña buscando por RUT
    fun actualizarContrasena(rut: String, nuevaContrasena: String): Int {
        val valores = ContentValues().apply {
            put(USU_CONTRASENA, nuevaContrasena)
        }
        return writableDatabase.update(
            TABLA_USUARIOS,
            valores,
            "$USU_RUT = ?",
            arrayOf(rut)
        )
    }

    private fun valoresDe(usuario: Usuario) = ContentValues().apply {
        put(USU_NOMBRE, usuario.nombre)
        put(USU_CORREO, usuario.correo)
        put(USU_RUT, usuario.rut)
        put(USU_CONTRASENA, usuario.contrasena)
        put(USU_CIUDAD, usuario.ciudad)
        put(USU_ROL, usuario.rolId)
    }

    private fun consultarUsuarios(sql: String, args: Array<String>?): List<Usuario> {
        val lista = mutableListOf<Usuario>()
        readableDatabase.rawQuery(sql, args).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(leerUsuario(cursor))
            }
        }
        return lista
    }

    private fun leerUsuario(cursor: Cursor) = Usuario(
        id = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ID)),
        nombre = cursor.getString(cursor.getColumnIndexOrThrow(USU_NOMBRE)),
        correo = cursor.getString(cursor.getColumnIndexOrThrow(USU_CORREO)),
        rut = cursor.getString(cursor.getColumnIndexOrThrow(USU_RUT)),
        contrasena = cursor.getString(cursor.getColumnIndexOrThrow(USU_CONTRASENA)),
        ciudad = cursor.getString(cursor.getColumnIndexOrThrow(USU_CIUDAD)),
        rolId = cursor.getLong(cursor.getColumnIndexOrThrow(USU_ROL)),
        rolNombre = cursor.getString(cursor.getColumnIndexOrThrow(ROL_NOMBRE)),
        avatar = cursor.getColumnIndexOrThrow(USU_AVATAR).let { columna ->
            if (cursor.isNull(columna)) null else cursor.getBlob(columna)
        }
    )
}