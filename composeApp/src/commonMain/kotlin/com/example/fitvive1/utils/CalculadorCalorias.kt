package com.example.fitvive1.utils

import kotlin.math.roundToInt

data class DistribucionCalorica(
    val desayuno: Int,
    val comida: Int,
    val merienda: Int,
    val cena: Int
) {
    fun paraTipo(tipo: String): Int = when (tipo) {
        "DESAYUNO" -> desayuno
        "COMIDA" -> comida
        "MERIENDA" -> merienda
        else -> cena
    }

    val total: Int
        get() = desayuno + comida + merienda + cena
}

/** Calcula el TDEE del usuario usando la fórmula de Harris-Benedict (actividad moderada). */
fun calcularTDEE(peso: String, altura: String, sexo: String, objetivo: String): Int {
    val pesoKg  = peso.toDoubleOrNull()   ?: 70.0
    val alturaCm = altura.toDoubleOrNull() ?: 170.0

    val bmr = if (sexo.contains("Masculino", ignoreCase = true))
        88.362 + (13.397 * pesoKg) + (4.799 * alturaCm) - 447.0
    else
        447.593 + (9.247 * pesoKg) + (3.098 * alturaCm) - 447.0

    val tdee = (bmr * 1.55).roundToInt()

    return when {
        objetivo.contains("volumen", ignoreCase = true) -> tdee + 300
        objetivo.contains("ficit",  ignoreCase = true)  -> tdee - 400
        else                                             -> tdee
    }
}

fun distribuirCalorias(tdee: Int): DistribucionCalorica {
    val desayuno = (tdee * 0.25).roundToInt()
    val comida = (tdee * 0.35).roundToInt()
    val merienda = (tdee * 0.10).roundToInt()
    val cena = tdee - desayuno - comida - merienda

    return DistribucionCalorica(
        desayuno = desayuno,
        comida = comida,
        merienda = merienda,
        cena = cena
    )
}
