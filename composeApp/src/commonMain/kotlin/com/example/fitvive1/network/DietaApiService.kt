package com.example.fitvive1.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import kotlin.math.roundToInt

class DietaApiService(private val client: HttpClient) {

    // Los valores de tipo (DESAYUNO, COMIDA, etc.) son identificadores internos y no se traducen.
    // Solo los términos enviados al buscador y el texto mostrado al usuario están localizados.
    private fun queryParaTipo(tipo: String, objetivo: String): String {
        val esVolumen = objetivo.contains("volumen", ignoreCase = true)
        val esDeficit = objetivo.contains("ficit",  ignoreCase = true)
        return when (tipo) {
            "DESAYUNO" -> if (esVolumen) "avena" else if (esDeficit) "yogur griego" else "tostada integral"
            "COMIDA"   -> if (esVolumen) "arroz con pollo" else if (esDeficit) "pollo a la plancha" else "pasta"
            "MERIENDA" -> if (esVolumen) "batido de plátano" else if (esDeficit) "fruta con yogur" else "yogur con fruta"
            else       -> if (esVolumen) "salmón con patata" else if (esDeficit) "pescado al vapor" else "pollo con verduras"
        }
    }

    private fun nombreEnEspanol(tipo: String, objetivo: String): String =
        queryParaTipo(tipo, objetivo).replaceFirstChar { it.uppercase() }

    /** Devuelve un alimento de Open Food Facts ajustado a las calorías asignadas. */
    suspend fun getComida(objetivo: String, tipo: String, caloriasObjetivo: Int): ComidaAPI? {
        val query = queryParaTipo(tipo, objetivo)

        val response: OFFResponse = client.get("https://es.openfoodfacts.org/cgi/search.pl") {
            parameter("search_terms", query)
            parameter("lc",           "es")
            parameter("action",       "process")
            parameter("json",         1)
            parameter("page_size",    5)
            parameter("fields",       "product_name_es,nutriments")
        }.body()

        val producto = response.products.firstOrNull {
            it.nombre.isNotBlank() && it.nutriments.energiaKcal100g > 10.0
        } ?: return null

        // Calcular porción (g) para alcanzar el objetivo calórico
        val gramos = caloriasObjetivo / (producto.nutriments.energiaKcal100g / 100.0)
        val factor = gramos / 100.0

        return ComidaAPI(
            // El nombre externo puede contener marcas o textos en otro idioma incluso en el
            // catálogo español. Usamos un nombre controlado en español y solo tomamos de la
            // API los valores nutricionales.
            nombre        = "${nombreEnEspanol(tipo, objetivo)} (${gramos.roundToInt()} g)",
            calorias      = caloriasObjetivo,
            proteinas     = (producto.nutriments.proteinas100g * factor).roundToInt(),
            carbohidratos = (producto.nutriments.carbohidratos100g * factor).roundToInt()
        )
    }
}
