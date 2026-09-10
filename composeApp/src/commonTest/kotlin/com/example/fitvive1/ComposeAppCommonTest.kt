package com.example.fitvive1

import com.example.fitvive1.utils.distribuirCalorias
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ComposeAppCommonTest {

    @Test
    fun distribucionCaloricaSumaExactamenteElTdee() {
        listOf(1, 1599, 1600, 2000, 2371, 2500, 4001).forEach { tdee ->
            assertEquals(tdee, distribuirCalorias(tdee).total)
        }
    }

    @Test
    fun planLocalAjustadoSumaExactamenteElTdee() {
        val casos = listOf(
            "Volumen" to 2843,
            "Déficit" to 1769,
            "Mantenimiento" to 2371
        )

        casos.forEach { (objetivo, tdee) ->
            (1..6).forEach { dia ->
                val plan = obtenerPlanDieta(
                    dia = dia,
                    objetivo = objetivo,
                    caloriasObjetivo = tdee
                )

                assertEquals(tdee, plan.caloriasObjetivo)
                assertEquals(tdee, plan.comidas.sumOf { it.calorias })
            }
        }
    }

    @Test
    fun planesLocalesNoContienenNombresDeComidasEnIngles() {
        val terminosIngleses = listOf(
            "oatmeal", "greek yogurt", "whole grain", "chicken", "rice",
            "grilled", "salmon", "potato", "steamed", "vegetables"
        )

        listOf("Volumen", "Déficit", "Mantenimiento").forEach { objetivo ->
            (0..6).forEach { dia ->
                obtenerPlanDieta(dia, objetivo, 2200).comidas.forEach { comida ->
                    val textoVisible = (listOf(comida.nombre) + comida.ingredientes)
                        .joinToString(" ")
                        .lowercase()
                    terminosIngleses.forEach { termino ->
                        assertFalse(textoVisible.contains(termino), "Texto inglés encontrado: $termino")
                    }
                }
            }
        }
    }

    @Test
    fun registroRemotoMasRecienteSustituyeAlLocal() {
        val local = estadoComida(
            completada = true,
            updatedAt = "2026-07-28T10:00:00.000Z"
        )
        val remoto = estadoComida(
            completada = false,
            updatedAt = "2026-07-28T10:01:00.000Z"
        )

        val resultado = resolverSyncComidas(
            emailUsuario = local.usuarioEmail,
            locales = listOf(local),
            remotos = listOf(remoto)
        )

        assertEquals(listOf(remoto), resultado.guardarLocal)
        assertTrue(resultado.enviarRemoto.isEmpty())
        assertFalse(resultado.guardarLocal.single().completada)
    }

    @Test
    fun registroLocalMasRecienteSeEnviaAFirestore() {
        val local = estadoComida(
            completada = false,
            updatedAt = "2026-07-28T10:01:00.000Z"
        )
        val remoto = estadoComida(
            completada = true,
            updatedAt = "2026-07-28T10:00:00.000Z"
        )

        val resultado = resolverSyncComidas(
            emailUsuario = local.usuarioEmail,
            locales = listOf(local),
            remotos = listOf(remoto)
        )

        assertTrue(resultado.guardarLocal.isEmpty())
        assertEquals(listOf(local), resultado.enviarRemoto)
        assertFalse(resultado.enviarRemoto.single().completada)
    }

    @Test
    fun sincronizacionNoMezclaUsuarios() {
        val usuarioActivo = estadoComida(
            usuarioEmail = "usuario@fitvive.test",
            updatedAt = "2026-07-28T10:00:00.000Z"
        )
        val otroUsuario = estadoComida(
            usuarioEmail = "otro@fitvive.test",
            updatedAt = "2026-07-28T10:02:00.000Z"
        )

        val resultado = resolverSyncComidas(
            emailUsuario = usuarioActivo.usuarioEmail,
            locales = listOf(usuarioActivo, otroUsuario),
            remotos = listOf(otroUsuario)
        )

        assertEquals(listOf(usuarioActivo), resultado.enviarRemoto)
        assertTrue(resultado.guardarLocal.isEmpty())
    }

    private fun estadoComida(
        usuarioEmail: String = "usuario@fitvive.test",
        completada: Boolean = true,
        updatedAt: String
    ) = EstadoComidaSync(
        usuarioEmail = usuarioEmail,
        dia = 2,
        objetivo = "Mantenimiento",
        tipo = "CENA",
        completada = completada,
        updatedAt = updatedAt
    )
}
