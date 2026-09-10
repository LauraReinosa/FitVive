package com.example.fitvive1.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class DietaReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val mensajes = listOf(
            "No olvides seguir tu plan de alimentación de hoy 🥗",
            "La constancia en la dieta es clave para alcanzar tus objetivos.",
            "Tu yo del futuro te agradecerá mantener tus hábitos hoy."
        )
        val idx = (System.currentTimeMillis() / 1000 % mensajes.size).toInt()
        AndroidNotificationScheduler.mostrarNotificacion(
            context,
            AndroidNotificationScheduler.ID_NOTIF_DIETA,
            "FitVive — Dieta",
            mensajes[idx],
            AndroidNotificationScheduler.CANAL_DIETA
        )
    }
}
