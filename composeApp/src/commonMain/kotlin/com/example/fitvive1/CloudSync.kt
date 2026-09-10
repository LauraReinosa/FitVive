package com.example.fitvive1

data class EstadisticasFirestore(
    val totalSeries: Int = 0,
    val totalReps: Int = 0,
    val ejercicioMayorProgreso: String = "",
    val ejerciciosDisponibles: List<String> = emptyList(),
    val mejorEjercicioPesoInicial: Float = 0f,
    val mejorEjercicioPesoActual: Float = 0f
)

data class EstadoComidaSync(
    val usuarioEmail: String,
    val dia: Int,
    val objetivo: String,
    val tipo: String,
    val completada: Boolean,
    val updatedAt: String
) {
    val clave: String
        get() = "$dia|${objetivo.trim().lowercase()}|${tipo.trim().uppercase()}"
}

data class ResultadoSyncComidas(
    val guardarLocal: List<EstadoComidaSync>,
    val enviarRemoto: List<EstadoComidaSync>
)

fun resolverSyncComidas(
    emailUsuario: String,
    locales: List<EstadoComidaSync>,
    remotos: List<EstadoComidaSync>
): ResultadoSyncComidas {
    val emailNormalizado = emailUsuario.trim().lowercase()
    val localesUsuario = locales
        .filter { it.usuarioEmail.trim().lowercase() == emailNormalizado }
        .associateBy { it.clave }
    val remotosUsuario = remotos
        .filter { it.usuarioEmail.trim().lowercase() == emailNormalizado }
        .associateBy { it.clave }

    val guardarLocal = mutableListOf<EstadoComidaSync>()
    val enviarRemoto = mutableListOf<EstadoComidaSync>()

    (localesUsuario.keys + remotosUsuario.keys).forEach { clave ->
        val local = localesUsuario[clave]
        val remoto = remotosUsuario[clave]
        when {
            local == null && remoto != null -> guardarLocal += remoto
            local != null && remoto == null -> enviarRemoto += local
            local != null && remoto != null && remoto.updatedAt > local.updatedAt ->
                guardarLocal += remoto
            local != null && remoto != null && local.updatedAt > remoto.updatedAt ->
                enviarRemoto += local
        }
    }

    return ResultadoSyncComidas(
        guardarLocal = guardarLocal,
        enviarRemoto = enviarRemoto
    )
}

interface CloudSyncer {
    suspend fun syncPerfil(
        email: String,
        nombre: String,
        sexo: String,
        peso: String,
        altura: String,
        fechaNacimiento: String,
        objetivo: String,
        diasEntrenamiento: Int,
        grupoMuscularFoco: String = ""
    )

    suspend fun syncEntrenamiento(
        email: String,
        fecha: String,
        dia: Int,
        grupoMuscular: String
    )

    suspend fun syncConfigNotificaciones(
        email: String,
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
    ) {}

    suspend fun syncRegistroPeso(
        email: String,
        fecha: String,
        peso: String,
        valoracion: Int
    ) {}

    suspend fun syncSeriesEjercicio(
        email: String,
        fecha: String,
        dia: Int,
        grupoMuscular: String,
        registros: List<RegistroEjercicio>
    ) {}

    suspend fun leerEstadisticasEntrenamiento(email: String): EstadisticasFirestore = EstadisticasFirestore()

    suspend fun leerEvolucionEjercicio(email: String, ejercicio: String): List<Pair<String, Float>> = emptyList()

    suspend fun leerEstadosComidas(email: String): List<EstadoComidaSync>? = null

    suspend fun syncEstadoComida(email: String, estado: EstadoComidaSync): Boolean = false
}

expect fun createCloudSyncer(): CloudSyncer
