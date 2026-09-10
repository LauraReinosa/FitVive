package com.example.fitvive1.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class ProgresoSemanalReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mensajes = listOf(
            "Es momento de registrar tu peso semanal ⚖️",
            "¿Has notado cambios esta semana? Registra tu progreso.",
            "Actualiza tu progreso para seguir mejorando tu plan."
        )
        val idx = (System.currentTimeMillis() / 1000 % mensajes.size).toInt()
        AndroidNotificationScheduler.mostrarNotificacion(
            context,
            AndroidNotificationScheduler.ID_NOTIF_PROGRESO,
            "FitVive — Control semanal",
            mensajes[idx],
            AndroidNotificationScheduler.CANAL_PROGRESO
        )
    }
}
