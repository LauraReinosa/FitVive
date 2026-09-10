package com.example.fitvive1.database

import com.example.fitvive1.network.ComidaAPI
import com.example.fitvive1.network.DietaApiService
import com.example.fitvive1.network.EjercicioApi
import com.example.fitvive1.network.EjercicioApiService
import com.example.fitvive1.obtenerFechaActual

class ApiRepository(
    private val repo: UsuarioRepository,
    private val ejercicioService: EjercicioApiService,
    private val dietaService: DietaApiService
) {
    private val versionCacheComidas = "es-v1"

    /**
     * Devuelve nombres de ejercicios para el grupo muscular dado.
     * Orden: caché del día → API → null (la pantalla usa el fallback hardcodeado).
     */
    suspend fun getEjerciciosPorGrupo(grupoMuscular: String): List<String>? {
        val hoy = obtenerFechaActual()
        val cached = repo.getEjerciciosCache(grupoMuscular)
        if (cached.isNotEmpty() && cached.first().timestamp == hoy) {
            return cached.map { it.nombre }
        }
        return try {
            val nombres = ejercicioService.getEjercicios(grupoMuscular)
            if (nombres.isNotEmpty()) {
                repo.borrarEjerciciosCache(grupoMuscular)
                nombres.forEach { nombre -> repo.guardarEjercicioCache(grupoMuscular, nombre, hoy) }
                nombres
            } else null
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Devuelve ejercicios enriquecidos (imagen, músculo, equipamiento) sin caché.
     * Los datos se mantienen en memoria durante la sesión.
     */
    suspend fun getEjerciciosDetallados(grupoMuscular: String): List<EjercicioApi>? {
        return try {
            val lista = ejercicioService.getEjerciciosDetallados(grupoMuscular)
            lista.ifEmpty { null }
        } catch (_: Exception) {
            null
        }
    }

    /**
     * Devuelve una comida de Open Food Facts para el tipo y calorías indicados.
     * Orden: caché del día → API → null (la pantalla usa el fallback local).
     */
    suspend fun getComida(objetivo: String, tipo: String, caloriasObjetivo: Int): ComidaAPI? {
        val hoy = obtenerFechaActual()
        val cacheVigente = "$versionCacheComidas:$hoy"
        val cached = repo.getComidasCache(objetivo, tipo)
        if (
            cached.isNotEmpty() &&
            cached.first().timestamp == cacheVigente &&
            cached.first().calorias == caloriasObjetivo.toLong()
        ) {
            val c = cached.first()
            return ComidaAPI(c.nombre, c.calorias.toInt(), c.proteinas.toInt(), c.carbohidratos.toInt())
        }
        return try {
            val comida = dietaService.getComida(objetivo, tipo, caloriasObjetivo) ?: return null
            repo.borrarComidasCache(objetivo, tipo)
            repo.guardarComidaCache(
                objetivo, tipo, comida.nombre,
                comida.calorias.toLong(), comida.proteinas.toLong(), comida.carbohidratos.toLong(), cacheVigente
            )
            comida
        } catch (_: Exception) {
            null
        }
    }
}
