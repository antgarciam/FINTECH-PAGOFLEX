package cl.duocuc.miregistro.data

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import cl.duocuc.miregistro.model.Rol
import cl.duocuc.miregistro.model.Usuario
import cl.duocuc.miregistro.model.Compromiso
import cl.duocuc.miregistro.model.ComprobantePago
import cl.duocuc.miregistro.model.Reporte

class UsuarioDbHelper(context: Context) :
    SQLiteOpenHelper(context, NOMBRE_BD, null, VERSION_BD) {

    companion object {
        private const val NOMBRE_BD = "miregistro.db"
        // Subimos a versión 9 para asegurar recreación de tabla reportes
        private const val VERSION_BD = 9

        private const val TABLA_ROLES = "roles"
        private const val ROL_ID = "id_rol"
        private const val ROL_NOMBRE = "nombre_rol"

        private const val TABLA_USUARIOS = "usuarios"
        private const val USU_ID = "id_usuario"
        private const val USU_NOMBRE = "nombre"
        private const val USU_CORREO = "correo"
        private const val USU_RUT = "rut"
        private const val USU_CONTRASENA = "contrasena"
        private const val USU_CIUDAD = "ciudad"
        private const val USU_ROL = "id_rol"
        private const val USU_AVATAR = "avatar"

        private const val TABLA_REPORTES = "reportes"
        private const val SQL_CREAR_REPORTES =
            """
                CREATE TABLE IF NOT EXISTS reportes(
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                cliente_nombre TEXT NOT NULL,
                cliente_correo TEXT NOT NULL,
                motivo TEXT NOT NULL,
                detalle TEXT NOT NULL,
                estado TEXT NOT NULL,
                fecha_hora TEXT NOT NULL,
                respuesta_agente TEXT
                )
            """

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

        private const val TABLA_COMPROMISOS = "compromisos"
        private const val SQL_CREAR_COMPROMISOS =
            """
            CREATE TABLE compromisos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            usuario_id INTEGER NOT NULL,
            empresa TEXT NOT NULL,
            concepto TEXT NOT NULL,
            cuota TEXT NOT NULL,
            monto INTEGER NOT NULL,
            vencimiento TEXT NOT NULL,
            estado TEXT NOT NULL,
            folio TEXT NOT NULL,
            FOREIGN KEY (usuario_id)
            REFERENCES usuarios(id_usuario)
            ON DELETE CASCADE
            )
            """

        private const val TABLA_PAGOS = "pagos"

        private const val SQL_CREAR_PAGOS =
            """
            CREATE TABLE pagos (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            usuario_id INTEGER NOT NULL,
            numero TEXT NOT NULL,
            fecha_hora TEXT NOT NULL,
            empresa TEXT NOT NULL,
            concepto TEXT NOT NULL,
            cuota TEXT NOT NULL,
            folio_compromiso TEXT NOT NULL,
            monto INTEGER NOT NULL,
            recargo_mora INTEGER NOT NULL,
            total_pagado INTEGER NOT NULL,
            canal TEXT NOT NULL,
            estado TEXT NOT NULL,
            FOREIGN KEY (usuario_id)
            REFERENCES usuarios(id_usuario)
            ON DELETE CASCADE
            )
            """

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
                    "('Soporte PagoFlex', 'soporte@pagoflex.cl', '6.666.666-2', 'admin123', 'Providencia', 2)"

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
        db.execSQL(SQL_CREAR_COMPROMISOS)
        db.execSQL(SQL_CREAR_PAGOS)
        db.execSQL(SQL_CREAR_REPORTES)
    }

    override fun onUpgrade(
        db: SQLiteDatabase,
        oldVersion: Int,
        newVersion: Int
    ) {
        if (oldVersion < 2) {
            db.execSQL(SQL_AGREGAR_AVATAR)
        }

        if (oldVersion < 3) {
            db.execSQL(SQL_AGREGAR_RUT)
        }

        if (oldVersion < 4) {
            db.execSQL(SQL_AGREGAR_CONTRASENA)
        }

        if (oldVersion < 5) {
            db.execSQL(
                """
            UPDATE usuarios
            SET rut = '6.666.666-2'
            WHERE correo = 'soporte@pagoflex.cl'
            """.trimIndent()
            )
        }

        if (oldVersion < 6) {
            val valores = ContentValues().apply {
                put(USU_NOMBRE, "Soporte PagoFlex")
                put(USU_CORREO, "soporte@pagoflex.cl")
                put(USU_RUT, "6.666.666-2")
                put(USU_CONTRASENA, "admin123")
                put(USU_CIUDAD, "Providencia")
                put(USU_ROL, 2)
            }

            val filasActualizadas = db.update(
                TABLA_USUARIOS,
                valores,
                "$USU_CORREO = ?",
                arrayOf("soporte@pagoflex.cl")
            )

            if (filasActualizadas == 0) {
                db.insert(TABLA_USUARIOS, null, valores)
            }
        }

        if (oldVersion < 7) {
            db.execSQL(SQL_CREAR_COMPROMISOS)
            db.execSQL(SQL_CREAR_PAGOS)
        }

        if (oldVersion < 8) {
            db.execSQL(SQL_CREAR_REPORTES)
        }

        if (oldVersion < 9) {
            db.execSQL("DROP TABLE IF EXISTS reportes")
            db.execSQL(SQL_CREAR_REPORTES)
        }
    }

    fun listarCompromisos(usuarioId: Long): List<Compromiso> {
        val lista = mutableListOf<Compromiso>()

        readableDatabase.rawQuery(
            """
        SELECT * FROM compromisos
        WHERE usuario_id = ?
        ORDER BY id
        """.trimIndent(),
            arrayOf(usuarioId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Compromiso(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        empresa = cursor.getString(cursor.getColumnIndexOrThrow("empresa")),
                        concepto = cursor.getString(cursor.getColumnIndexOrThrow("concepto")),
                        cuota = cursor.getString(cursor.getColumnIndexOrThrow("cuota")),
                        monto = cursor.getInt(cursor.getColumnIndexOrThrow("monto")),
                        vencimiento = cursor.getString(cursor.getColumnIndexOrThrow("vencimiento")),
                        estado = cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                        folio = cursor.getString(cursor.getColumnIndexOrThrow("folio"))
                    )
                )
            }
        }

        return lista
    }

    fun crearCompromisosIniciales(usuarioId: Long) {
        val db = writableDatabase

        val cursor = db.rawQuery(
            "SELECT COUNT(*) FROM compromisos WHERE usuario_id = ?",
            arrayOf(usuarioId.toString())
        )

        val yaTieneCompromisos = cursor.use {
            it.moveToFirst() && it.getInt(0) > 0
        }

        if (yaTieneCompromisos) return

        val ejemplos = listOf(
            Compromiso(
                id = 0L,
                empresa = "Altos del Maipo",
                concepto = "Crédito de consumo",
                cuota = "1 de 6",
                monto = 45000,
                vencimiento = "01-10-2026",
                estado = "Vencido",
                folio = "PF-0001"
            ),
            Compromiso(
                id = 0L,
                empresa = "Altos del Maipo",
                concepto = "Crédito de consumo",
                cuota = "2 de 6",
                monto = 45000,
                vencimiento = "01-11-2026",
                estado = "Pendiente",
                folio = "PF-0002"
            ),
            Compromiso(
                id = 0L,
                empresa = "Empresa Ejemplo",
                concepto = "Servicio mensual",
                cuota = "1 de 3",
                monto = 20000,
                vencimiento = "25-10-2026",
                estado = "Pendiente",
                folio = "PF-0003"
            )
        )

        ejemplos.forEach { compromiso ->
            val valores = ContentValues().apply {
                put("usuario_id", usuarioId)
                put("empresa", compromiso.empresa)
                put("concepto", compromiso.concepto)
                put("cuota", compromiso.cuota)
                put("monto", compromiso.monto)
                put("vencimiento", compromiso.vencimiento)
                put("estado", compromiso.estado)
                put("folio", compromiso.folio)
            }

            db.insert("compromisos", null, valores)
        }
    }

    fun insertarCompromiso(usuarioId: Long, compromiso: Compromiso): Long {
        val valores = ContentValues().apply {
            put("usuario_id", usuarioId)
            put("empresa", compromiso.empresa)
            put("concepto", compromiso.concepto)
            put("cuota", compromiso.cuota)
            put("monto", compromiso.monto)
            put("vencimiento", compromiso.vencimiento)
            put("estado", compromiso.estado)
            put("folio", compromiso.folio)
        }
        return writableDatabase.insert("compromisos", null, valores)
    }

    fun actualizarEstadoCompromiso(
        usuarioId: Long,
        compromisoId: Long,
        nuevoEstado: String
    ): Boolean {
        val valores = ContentValues().apply {
            put("estado", nuevoEstado)
        }

        return writableDatabase.update(
            "compromisos",
            valores,
            "id = ? AND usuario_id = ?",
            arrayOf(compromisoId.toString(), usuarioId.toString())
        ) > 0
    }

    fun insertarPago(
        usuarioId: Long,
        comprobante: ComprobantePago
    ): Long {
        val valores = ContentValues().apply {
            put("usuario_id", usuarioId)
            put("numero", comprobante.numero)
            put("fecha_hora", comprobante.fechaHora)
            put("empresa", comprobante.empresa)
            put("concepto", comprobante.concepto)
            put("cuota", comprobante.cuota)
            put("folio_compromiso", comprobante.folioCompromiso)
            put("monto", comprobante.monto)
            put("recargo_mora", comprobante.recargoMora)
            put("total_pagado", comprobante.totalPagado)
            put("canal", comprobante.canal)
            put("estado", comprobante.estado)
        }

        return writableDatabase.insert("pagos", null, valores)
    }

    fun listarPagos(usuarioId: Long): List<ComprobantePago> {
        val lista = mutableListOf<ComprobantePago>()

        readableDatabase.rawQuery(
            """
        SELECT * FROM pagos
        WHERE usuario_id = ?
        ORDER BY id DESC
        """.trimIndent(),
            arrayOf(usuarioId.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    ComprobantePago(
                        numero = cursor.getString(cursor.getColumnIndexOrThrow("numero")),
                        fechaHora = cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora")),
                        empresa = cursor.getString(cursor.getColumnIndexOrThrow("empresa")),
                        concepto = cursor.getString(cursor.getColumnIndexOrThrow("concepto")),
                        cuota = cursor.getString(cursor.getColumnIndexOrThrow("cuota")),
                        folioCompromiso = cursor.getString(
                            cursor.getColumnIndexOrThrow("folio_compromiso")
                        ),
                        monto = cursor.getInt(cursor.getColumnIndexOrThrow("monto")),
                        recargoMora = cursor.getInt(
                            cursor.getColumnIndexOrThrow("recargo_mora")
                        ),
                        totalPagado = cursor.getInt(
                            cursor.getColumnIndexOrThrow("total_pagado")
                        ),
                        canal = cursor.getString(cursor.getColumnIndexOrThrow("canal")),
                        estado = cursor.getString(cursor.getColumnIndexOrThrow("estado"))
                    )
                )
            }
        }

        return lista
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

    fun existeRut(rut: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT 1 FROM $TABLA_USUARIOS WHERE REPLACE(REPLACE($USU_RUT, '.', ''), '-', '') = ? LIMIT 1",
            arrayOf(
                rut.replace(".", "")
                    .replace("-", "")
                    .trim()
            )
        )

        return cursor.use {
            it.moveToFirst()
        }
    }

    fun existeCorreo(correo: String): Boolean {
        val cursor = readableDatabase.rawQuery(
            "SELECT 1 FROM $TABLA_USUARIOS WHERE LOWER($USU_CORREO) = LOWER(?) LIMIT 1",
            arrayOf(correo.trim())
        )

        return cursor.use {
            it.moveToFirst()
        }
    }

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

    fun insertarReporte(reporte: Reporte): Long {
        val valores = ContentValues().apply {
            put("cliente_nombre", reporte.clienteNombre)
            put("cliente_correo", reporte.clienteCorreo)
            put("motivo", reporte.motivo)
            put("detalle", reporte.detalle)
            put("estado", reporte.estado)
            put("fecha_hora", reporte.fechaHora)
            put("respuesta_agente", reporte.respuestaAgente)
        }
        return writableDatabase.insert(TABLA_REPORTES, null, valores)
    }

    fun listarReportes(): List<Reporte> {
        val lista = mutableListOf<Reporte>()
        readableDatabase.rawQuery(
            "SELECT * FROM $TABLA_REPORTES ORDER BY id DESC", null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                lista.add(
                    Reporte(
                        id = cursor.getLong(cursor.getColumnIndexOrThrow("id")),
                        clienteNombre = cursor.getString(cursor.getColumnIndexOrThrow("cliente_nombre")),
                        clienteCorreo = cursor.getString(cursor.getColumnIndexOrThrow("cliente_correo")),
                        motivo = cursor.getString(cursor.getColumnIndexOrThrow("motivo")),
                        detalle = cursor.getString(cursor.getColumnIndexOrThrow("detalle")),
                        estado = cursor.getString(cursor.getColumnIndexOrThrow("estado")),
                        fechaHora = cursor.getString(cursor.getColumnIndexOrThrow("fecha_hora")),
                        respuestaAgente = if (cursor.isNull(cursor.getColumnIndexOrThrow("respuesta_agente"))) null else cursor.getString(cursor.getColumnIndexOrThrow("respuesta_agente"))
                    )
                )
            }
        }
        return lista
    }

    fun actualizarEstadoReporte(id: Long, nuevoEstado: String, respuesta: String?): Int {
        val valores = ContentValues().apply {
            put("estado", nuevoEstado)
            put("respuesta_agente", respuesta)
        }
        return writableDatabase.update(
            TABLA_REPORTES, valores, "id = ?", arrayOf(id.toString())
        )
    }
}