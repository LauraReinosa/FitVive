package com.example.fitvive1

class NoOpCloudSyncer : CloudSyncer {
    override suspend fun syncPerfil(
        email: String, nombre: String, sexo: String, peso: String,
        altura: String, fechaNacimiento: String, objetivo: String,
        diasEntrenamiento: Int, grupoMuscularFoco: String
    ) {}

    override suspend fun syncEntrenamiento(
        email: String, fecha: String, dia: Int, grupoMuscular: String
    ) {}

    override suspend fun leerEstadosComidas(email: String): List<EstadoComidaSync>? = null

    override suspend fun syncEstadoComida(email: String, estado: EstadoComidaSync): Boolean = false
}

actual fun createCloudSyncer(): CloudSyncer = NoOpCloudSyncer()
