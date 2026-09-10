package com.example.fitvive1.database

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class UsuarioRepository(driverFactory: DatabaseDriverFactory) {

    private val database = FitviveDatabase(driverFactory.createDriver())
    private val queries = database.usuarioQueries
    private val entrenoQueries = database.entrenoQueries
    private val sesionQueries = database.sesionQueries
    private var usuarioActivoId: Long? = null

    suspend fun obtenerUsuarioSesionActiva(): Usuario? = withContext(Dispatchers.Default) {
        obtenerUsuarioActivo()
    }

    suspend fun iniciarSesion(email: String, password: String): Usuario? =
        withContext(Dispatchers.Default) {
            val usuario = queries.obtenerUsuarioPorCredenciales(
                email = email.trim().lowercase(),
                password = password
            ).executeAsOneOrNull()
            if (usuario != null) {
                sesionQueries.guardarSesionActiva(usuario.id)
                usuarioActivoId = usuario.id
            }
            usuario
        }

    suspend fun cerrarSesion() = withContext(Dispatchers.Default) {
        sesionQueries.cerrarSesion()
        usuarioActivoId = null
    }

    suspend fun registrarUsuarioSiNoExiste(nombre: String, email: String, password: String): Boolean =
        withContext(Dispatchers.Default) {
            val emailNormalizado = email.trim().lowercase()
            database.transactionWithResult {
                if (queries.existeUsuarioConEmail(emailNormalizado).executeAsOne()) {
                    false
                } else {
                    queries.guardarNuevoUsuario(
                        nombre = nombre.trim(),
                        email = emailNormalizado,
                        password = password,
                        sexo = "",
                        peso = "",
                        altura = "",
                        fechaNacimiento = "",
                        objetivo = "",
                        diasEntrenamiento = 0,
                        grupoMuscularFoco = ""
                    )
                    true
                }
            }
        }

    suspend fun guardarRegistro(nombre: String, email: String, password: String) =
        withContext(Dispatchers.Default) {
            queries.guardarNuevoUsuario(
                nombre = nombre.trim(),
                email = email.trim().lowercase(),
                password = password,
                sexo = "",
                peso = "",
                altura = "",
                fechaNacimiento = "",
                objetivo = "",
                diasEntrenamiento = 0,
                grupoMuscularFoco = ""
            )
        }

    suspend fun guardarPaso1(sexo: String, peso: String, altura: String, fechaNacimiento: String) =
        withContext(Dispatchers.Default) {
            val actual = obtenerUsuarioActivo() ?: return@withContext
            queries.actualizarUsuario(
                id = actual.id,
                nombre = actual.nombre,
                email = actual.email,
                password = actual.password,
                sexo = sexo,
                peso = peso,
                altura = altura,
                fechaNacimiento = fechaNacimiento,
                objetivo = actual.objetivo,
                diasEntrenamiento = actual.diasEntrenamiento,
                grupoMuscularFoco = actual.grupoMuscularFoco
            )
        }

    suspend fun guardarObjetivo(objetivo: String) = withContext(Dispatchers.Default) {
        val actual = obtenerUsuarioActivo() ?: return@withContext
        queries.actualizarUsuario(
            id = actual.id,
            nombre = actual.nombre,
            email = actual.email,
            password = actual.password,
            sexo = actual.sexo,
            peso = actual.peso,
            altura = actual.altura,
            fechaNacimiento = actual.fechaNacimiento,
            objetivo = objetivo,
            diasEntrenamiento = actual.diasEntrenamiento,
            grupoMuscularFoco = actual.grupoMuscularFoco
        )
    }

    suspend fun guardarDiasEntrenamiento(dias: Int) = withContext(Dispatchers.Default) {
        val actual = obtenerUsuarioActivo() ?: return@withContext
        queries.actualizarUsuario(
            id = actual.id,
            nombre = actual.nombre,
            email = actual.email,
            password = actual.password,
            sexo = actual.sexo,
            peso = actual.peso,
            altura = actual.altura,
            fechaNacimiento = actual.fechaNacimiento,
            objetivo = actual.objetivo,
            diasEntrenamiento = dias.toLong(),
            grupoMuscularFoco = actual.grupoMuscularFoco
        )
    }

    suspend fun guardarGrupoMuscularFoco(foco: String) = withContext(Dispatchers.Default) {
        val actual = obtenerUsuarioActivo() ?: return@withContext
        queries.actualizarUsuario(
            id = actual.id,
            nombre = actual.nombre,
            email = actual.email,
            password = actual.password,
            sexo = actual.sexo,
            peso = actual.peso,
            altura = actual.altura,
            fechaNacimiento = actual.fechaNacimiento,
            objetivo = actual.objetivo,
            diasEntrenamiento = actual.diasEntrenamiento,
            grupoMuscularFoco = foco
        )
    }

    suspend fun actualizarPerfil(peso: String, altura: String, objetivo: String, diasEntrenamiento: Int) =
        withContext(Dispatchers.Default) {
            val actual = obtenerUsuarioActivo() ?: return@withContext
            queries.actualizarUsuario(
                id = actual.id,
                nombre = actual.nombre,
                email = actual.email,
                password = actual.password,
                sexo = actual.sexo,
                peso = peso,
                altura = altura,
                fechaNacimiento = actual.fechaNacimiento,
                objetivo = objetivo,
                diasEntrenamiento = diasEntrenamiento.toLong(),
                grupoMuscularFoco = actual.grupoMuscularFoco
            )
        }

    private fun obtenerUsuarioActivo(): Usuario? {
        val id = usuarioActivoId
            ?: sesionQueries.obtenerUsuarioSesionActivaId().executeAsOneOrNull()
            ?: return null
        val usuario = queries.obtenerUsuarioPorId(id).executeAsOneOrNull()
        if (usuario == null) {
            sesionQueries.cerrarSesion()
        }
        usuarioActivoId = usuario?.id
        return usuario
    }

    suspend fun guardarEntrenamiento(fecha: String, dia: Int, grupoMuscular: String) =
        withContext(Dispatchers.Default) {
            val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext
            entrenoQueries.guardarEntrenamiento(usuarioId, fecha, dia.toLong(), grupoMuscular)
        }

    suspend fun obtenerEntrenamientos(): List<Entreno> = withContext(Dispatchers.Default) {
        val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext emptyList()
        entrenoQueries.obtenerEntrenamientos(usuarioId).executeAsList()
    }

    suspend fun contarEntrenamientos(): Long = withContext(Dispatchers.Default) {
        val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext 0L
        entrenoQueries.contarEntrenamientos(usuarioId).executeAsOne()
    }

    // ─── Caché de ejercicios ──────────────────────────────────────────────────

    suspend fun getEjerciciosCache(grupoMuscular: String): List<Ejercicio_cache> =
        withContext(Dispatchers.Default) {
            database.ejercicioCacheQueries.getEjercicios(grupoMuscular).executeAsList()
        }

    suspend fun guardarEjercicioCache(grupoMuscular: String, nombre: String, timestamp: String) =
        withContext(Dispatchers.Default) {
            database.ejercicioCacheQueries.guardarEjercicio(grupoMuscular, nombre, timestamp)
        }

    suspend fun borrarEjerciciosCache(grupoMuscular: String) = withContext(Dispatchers.Default) {
        database.ejercicioCacheQueries.borrarEjercicios(grupoMuscular)
    }

    // ─── Caché de comidas ─────────────────────────────────────────────────────

    suspend fun getComidasCache(objetivo: String, tipo: String): List<Comida_cache> =
        withContext(Dispatchers.Default) {
            database.comidaCacheQueries.getComidas(objetivo, tipo).executeAsList()
        }

    suspend fun guardarComidaCache(
        objetivo: String, tipo: String, nombre: String,
        calorias: Long, proteinas: Long, carbohidratos: Long, timestamp: String
    ) = withContext(Dispatchers.Default) {
        database.comidaCacheQueries.guardarComida(
            objetivo, tipo, nombre, calorias, proteinas, carbohidratos, timestamp
        )
    }

    suspend fun borrarComidasCache(objetivo: String, tipo: String) =
        withContext(Dispatchers.Default) {
            database.comidaCacheQueries.borrarComidas(objetivo, tipo)
        }

    // ─── Seguimiento de comidas completadas ──────────────────────────────────

    suspend fun getComidasCompletadas(dia: Int, objetivo: String): List<Comida_completada> =
        withContext(Dispatchers.Default) {
            val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext emptyList()
            database.comidaCompletadaQueries
                .getComidasCompletadas(usuarioId, dia.toLong(), objetivo)
                .executeAsList()
        }

    suspend fun guardarEstadoComida(
        dia: Int,
        objetivo: String,
        tipo: String,
        completada: Boolean,
        updatedAt: String
    ) =
        withContext(Dispatchers.Default) {
            val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext
            database.comidaCompletadaQueries.guardarComidaCompletada(
                usuarioId,
                dia.toLong(),
                objetivo,
                tipo,
                if (completada) 1L else 0L,
                updatedAt
            )
        }

    suspend fun getTodosEstadosComidas(): List<Comida_completada> =
        withContext(Dispatchers.Default) {
            val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext emptyList()
            database.comidaCompletadaQueries
                .getTodosEstadosComidas(usuarioId)
                .executeAsList()
        }

    // ─── Configuración de notificaciones ─────────────────────────────────────

    suspend fun obtenerConfigNotificaciones() = withContext(Dispatchers.Default) {
        val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext null
        database.notificacionesConfigQueries.obtenerConfigNotificaciones(usuarioId).executeAsOneOrNull()
    }

    suspend fun guardarConfigNotificaciones(
        notifEntrenamiento: Boolean,
        horaEntrenamiento: Int,
        minutoEntrenamiento: Int,
        notifDieta: Boolean,
        horaDieta: Int,
        minutoDieta: Int,
        notifProgreso: Boolean,
        diaProgresoSemana: Int,
        horaProgreso: Int,
        minutoProgreso: Int
    ) = withContext(Dispatchers.Default) {
        val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext
        database.notificacionesConfigQueries.guardarConfigNotificaciones(
            usuarioId = usuarioId,
            notifEntrenamiento = if (notifEntrenamiento) 1L else 0L,
            horaEntrenamiento = horaEntrenamiento.toLong(),
            minutoEntrenamiento = minutoEntrenamiento.toLong(),
            notifDieta = if (notifDieta) 1L else 0L,
            horaDieta = horaDieta.toLong(),
            minutoDieta = minutoDieta.toLong(),
            notifProgreso = if (notifProgreso) 1L else 0L,
            diaProgresoSemana = diaProgresoSemana.toLong(),
            horaProgreso = horaProgreso.toLong(),
            minutoProgreso = minutoProgreso.toLong()
        )
    }

    // ─── Registro de peso ─────────────────────────────────────────────────────

    suspend fun guardarRegistroPeso(fecha: String, peso: String, valoracion: Int, notas: String) =
        withContext(Dispatchers.Default) {
            val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext
            database.pesoRegistroQueries.guardarRegistroPeso(
                usuarioId, fecha, peso, valoracion.toLong(), notas
            )
        }

    suspend fun obtenerUltimoRegistroPeso() = withContext(Dispatchers.Default) {
        val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext null
        database.pesoRegistroQueries.ultimoRegistroPeso(usuarioId).executeAsOneOrNull()
    }

    suspend fun obtenerRegistrosPeso() = withContext(Dispatchers.Default) {
        val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext emptyList()
        database.pesoRegistroQueries.obtenerRegistrosPeso(usuarioId).executeAsList()
    }

    suspend fun obtenerTodosRegistrosPeso() = withContext(Dispatchers.Default) {
        val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext emptyList()
        database.pesoRegistroQueries.obtenerTodosRegistrosPeso(usuarioId).executeAsList()
    }

    suspend fun eliminarRegistroPeso(id: Long) = withContext(Dispatchers.Default) {
        val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext
        database.pesoRegistroQueries.eliminarRegistroPeso(id, usuarioId)
    }

    suspend fun actualizarRegistroPeso(id: Long, peso: String, notas: String) = withContext(Dispatchers.Default) {
        val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext
        database.pesoRegistroQueries.actualizarRegistroPeso(peso, notas, id, usuarioId)
    }

    suspend fun obtenerTodosEntrenamientos(): List<Entreno> = withContext(Dispatchers.Default) {
        val usuarioId = obtenerUsuarioActivo()?.id ?: return@withContext emptyList()
        entrenoQueries.obtenerTodosEntrenamientos(usuarioId).executeAsList()
    }
}
