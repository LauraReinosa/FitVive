package com.example.fitvive1.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// ─── wger.de — ejercicios ────────────────────────────────────────────────────

@Serializable
data class WgerInfoResponse(
    val results: List<WgerExerciseInfo> = emptyList()
)

@Serializable
data class WgerExerciseInfo(
    val id: Int = 0,
    val translations: List<WgerTranslation> = emptyList(),
    val images: List<WgerImage> = emptyList(),
    val muscles: List<WgerMuscle> = emptyList(),
    val equipment: List<WgerEquipment> = emptyList()
)

@Serializable
data class WgerTranslation(
    val language: Int = 0,
    val name: String = ""
)

@Serializable
data class WgerImage(
    val image: String = "",
    @SerialName("is_main") val isMain: Boolean = false
)

@Serializable
data class WgerMuscle(
    val id: Int = 0,
    @SerialName("name_en") val nameEn: String = ""
)

@Serializable
data class WgerEquipment(
    val id: Int = 0,
    val name: String = ""
)

// Modelo enriquecido de ejercicio (no serializable — uso interno de la app)
data class EjercicioApi(
    val nombre: String,
    val imageUrl: String?,
    val musculoPrincipal: String,
    val equipamiento: String,
    val seriesRecomendadas: Int,
    val repeticionesRecomendadas: Int,
    val descansoSegundos: Int
)

// ─── Open Food Facts — alimentos ─────────────────────────────────────────────

@Serializable
data class OFFResponse(
    val products: List<OFFProduct> = emptyList()
)

@Serializable
data class OFFProduct(
    @SerialName("product_name_es") val nombre: String = "",
    val nutriments: OFFNutriments = OFFNutriments()
)

@Serializable
data class OFFNutriments(
    @SerialName("energy-kcal_100g") val energiaKcal100g: Double = 0.0,
    @SerialName("proteins_100g")    val proteinas100g: Double = 0.0,
    @SerialName("carbohydrates_100g") val carbohidratos100g: Double = 0.0
)

// ─── Asistente IA — modelos de la API de chat ────────────────────────────────

@Serializable
data class ChatRequest(
    @SerialName("system_instruction") val systemInstruction: ChatSystemInstruction? = null,
    val contents: List<ChatContent>
)

@Serializable
data class ChatSystemInstruction(
    val parts: List<ChatPart>
)

@Serializable
data class ChatContent(
    val role: String,
    val parts: List<ChatPart>
)

@Serializable
data class ChatPart(
    val text: String
)

@Serializable
data class ChatResponse(
    val candidates: List<ChatCandidate> = emptyList()
)

@Serializable
data class ChatCandidate(
    val content: ChatContent = ChatContent("model", emptyList())
)

// ─── Modelos internos de la app ───────────────────────────────────────────────

data class ComidaAPI(
    val nombre: String,
    val calorias: Int,
    val proteinas: Int,
    val carbohidratos: Int
)
