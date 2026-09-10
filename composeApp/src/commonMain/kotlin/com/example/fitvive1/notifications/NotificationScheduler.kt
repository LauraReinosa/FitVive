package com.example.fitvive1.notifications

data class ConfigNotificaciones(
    val notifEntrenamientoActiva: Boolean = true,
    val horaEntrenamiento: Int = 9,
    val minutoEntrenamiento: Int = 0,
    val notifDietaActiva: Boolean = true,
    val horaDieta: Int = 12,
    val minutoDieta: Int = 0,
    val notifProgresoActiva: Boolean = true,
    val diaProgresoSemana: Int = 1,
    val horaProgreso: Int = 10,
    val minutoProgreso: Int = 0
)

interface NotificationScheduler {
    suspend fun requestPermission(): Boolean
    fun scheduleEntrenamientoReminder(hora: Int, minuto: Int)
    fun scheduleDietaReminder(hora: Int, minuto: Int)
    fun scheduleProgresoSemanal(diaSemana: Int, hora: Int, minuto: Int)
    fun cancelEntrenamientoReminder()
    fun cancelDietaReminder()
    fun cancelProgresoSemanal()
}

private fun formatDecimal1(valor: Double): String {
    val rounded = (valor * 10 + 0.5).toLong()
    return "${rounded / 10}.${rounded % 10}"
}

fun generarRecomendacionDieta(pesoAnterior: String?, pesoActual: String, objetivo: String): String {
    if (pesoAnterior == null) {
        return "¡Peso registrado! Sigue así para ver tu evolución la próxima semana."
    }
    val anterior = pesoAnterior.toDoubleOrNull()
        ?: return "No se pudo analizar el peso anterior."
    val actual = pesoActual.toDoubleOrNull()
        ?: return "Peso no válido."
    val diferencia = actual - anterior
    val abs = kotlin.math.abs(diferencia)
    val difStr = formatDecimal1(abs)

    return when {
        objetivo.equals("déficit", ignoreCase = true) -> when {
            diferencia <= -0.5 -> "¡Excelente progreso! Perdiste ${difStr} kg. Mantén el plan, estás en el camino correcto."
            diferencia in -0.5..0.0 -> "Cambio mínimo esta semana. Considera reducir 100-150 kcal o añadir 20 min de cardio."
            diferencia in 0.0..0.5 -> "Ligero aumento de ${difStr} kg. Revisa la adherencia a tu dieta y reduce los tentempiés."
            else -> "Ganaste ${difStr} kg. Revisa tus calorías totales y la consistencia en los entrenamientos."
        }
        objetivo.equals("volumen", ignoreCase = true) -> when {
            diferencia >= 0.5 -> "¡Buen progreso! Ganaste ${difStr} kg. Entrena con intensidad para maximizar la calidad muscular."
            diferencia in 0.0..0.5 -> "Ganancia moderada de ${difStr} kg. Considera añadir 100-200 kcal para acelerar el volumen."
            diferencia in -0.5..0.0 -> "Apenas hay cambio. Aumenta los carbohidratos, especialmente post-entrenamiento."
            else -> "Perdiste ${difStr} kg en fase de volumen. Aumenta significativamente la ingesta calórica."
        }
        else -> when {
            abs <= 0.5 -> "¡Perfecto! Tu peso se mantiene estable. El plan de mantenimiento funciona correctamente."
            diferencia > 0.5 -> "Ganaste ${difStr} kg. Ajusta ligeramente las calorías hacia abajo esta semana."
            else -> "Perdiste ${difStr} kg. Añade 100-150 kcal para estabilizar tu peso."
        }
    }
}
