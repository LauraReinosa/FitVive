package com.example.fitvive1.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class EntrenamientoReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mensajes = listOf(
            "Todavía no has realizado tu entrenamiento de hoy 💪",
            "Tu objetivo está más cerca. ¡No olvides entrenar!",
            "Cada entrenamiento cuenta. ¡Vamos a por ello!"
        )
        val idx = (System.currentTimeMillis() / 1000 % mensajes.size).toInt()
        AndroidNotificationScheduler.mostrarNotificacion(
            context,
            AndroidNotificationScheduler.ID_NOTIF_ENTRENAMIENTO,
            "FitVive — Entrenamiento",
            mensajes[idx],
            AndroidNotificationScheduler.CANAL_ENTRENAMIENTO
        )
    }
}
