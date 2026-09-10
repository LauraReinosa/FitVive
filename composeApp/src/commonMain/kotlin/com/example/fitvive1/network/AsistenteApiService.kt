package com.example.fitvive1.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType

private const val API_URL =
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.6-flash:generateContent"

class AsistenteApiService(private val client: HttpClient) {

    suspend fun enviarMensaje(
        historialPrevio: List<Pair<String, String>>,
        nuevoMensaje: String,
        contextoUsuario: String
    ): String {
        if (GeminiConfig.API_KEY.isBlank()) {
            return "La API de Gemini no está configurada. Añade GEMINI_API_KEY a local.properties."
        }

        val contents = historialPrevio.map { (rol, texto) ->
            ChatContent(role = rol, parts = listOf(ChatPart(text = texto)))
        } + listOf(ChatContent(role = "user", parts = listOf(ChatPart(text = nuevoMensaje))))

        val systemPrompt = buildString {
            append("Eres un asistente fitness personal amigable integrado en la app FitVive. ")
            if (contextoUsuario.isNotBlank()) append(contextoUsuario)
            append("Responde siempre en español, de forma concisa (máximo 3-4 frases), práctica y motivadora. ")
            append("Cuando hables de alimentación, escribe en español todos los nombres de comidas, recetas, platos, ingredientes, cantidades, descripciones y recomendaciones destinados al usuario, aunque la pregunta o una fuente externa estén en otro idioma. ")
            append("Solo responde sobre ejercicio, nutrición, hábitos saludables y fitness. ")
            append("Si te preguntan algo fuera de ese ámbito, redirige amablemente al tema fitness.")
        }

        val request = ChatRequest(
            systemInstruction = ChatSystemInstruction(
                parts = listOf(ChatPart(text = systemPrompt))
            ),
            contents = contents
        )

        val response: ChatResponse = client.post("$API_URL?key=${GeminiConfig.API_KEY}") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()

        return response.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text
            ?: "No pude obtener una respuesta. Inténtalo de nuevo."
    }
}
