package com.example.fitvive1.notifications

import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.NSDateComponents
import platform.UserNotifications.UNAuthorizationOptionAlert
import platform.UserNotifications.UNAuthorizationOptionBadge
import platform.UserNotifications.UNAuthorizationOptionSound
import platform.UserNotifications.UNCalendarNotificationTrigger
import platform.UserNotifications.UNMutableNotificationContent
import platform.UserNotifications.UNNotificationRequest
import platform.UserNotifications.UNNotificationSound
import platform.UserNotifications.UNUserNotificationCenter
import kotlin.coroutines.resume

class IosNotificationScheduler : NotificationScheduler {

    private val center = UNUserNotificationCenter.currentNotificationCenter()

    override suspend fun requestPermission(): Boolean = suspendCancellableCoroutine { cont ->
        val options = UNAuthorizationOptionAlert or UNAuthorizationOptionSound or UNAuthorizationOptionBadge
        center.requestAuthorizationWithOptions(options) { granted, _ ->
            cont.resume(granted)
        }
    }

    override fun scheduleEntrenamientoReminder(hora: Int, minuto: Int) {
        scheduleDaily(
            id = "fitvive_entrenamiento",
            titulo = "FitVive — Entrenamiento",
            cuerpo = "Todavía no has realizado tu entrenamiento de hoy 💪",
            hora = hora,
            minuto = minuto
        )
    }

    override fun scheduleDietaReminder(hora: Int, minuto: Int) {
        scheduleDaily(
            id = "fitvive_dieta",
            titulo = "FitVive — Dieta",
            cuerpo = "No olvides seguir tu plan de alimentación de hoy 🥗",
            hora = hora,
            minuto = minuto
        )
    }

    override fun scheduleProgresoSemanal(diaSemana: Int, hora: Int, minuto: Int) {
        val content = UNMutableNotificationContent().apply {
            setTitle("FitVive — Control semanal")
            setBody("Es momento de registrar tu peso semanal ⚖️")
            setSound(UNNotificationSound.defaultSound)
        }
        val components = NSDateComponents().apply {
            setWeekday(diaAppToIos(diaSemana))
            setHour(hora.toLong())
            setMinute(minuto.toLong())
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = components,
            repeats = true
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = "fitvive_progreso",
            content = content,
            trigger = trigger
        )
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    override fun cancelEntrenamientoReminder() {
        center.removePendingNotificationRequestsWithIdentifiers(listOf("fitvive_entrenamiento"))
    }

    override fun cancelDietaReminder() {
        center.removePendingNotificationRequestsWithIdentifiers(listOf("fitvive_dieta"))
    }

    override fun cancelProgresoSemanal() {
        center.removePendingNotificationRequestsWithIdentifiers(listOf("fitvive_progreso"))
    }

    private fun scheduleDaily(id: String, titulo: String, cuerpo: String, hora: Int, minuto: Int) {
        val content = UNMutableNotificationContent().apply {
            setTitle(titulo)
            setBody(cuerpo)
            setSound(UNNotificationSound.defaultSound)
        }
        val components = NSDateComponents().apply {
            setHour(hora.toLong())
            setMinute(minuto.toLong())
        }
        val trigger = UNCalendarNotificationTrigger.triggerWithDateMatchingComponents(
            dateComponents = components,
            repeats = true
        )
        val request = UNNotificationRequest.requestWithIdentifier(
            identifier = id,
            content = content,
            trigger = trigger
        )
        center.addNotificationRequest(request, withCompletionHandler = null)
    }

    private fun diaAppToIos(dia: Int): Long = when (dia) {
        1 -> 2L
        2 -> 3L
        3 -> 4L
        4 -> 5L
        5 -> 6L
        6 -> 7L
        else -> 1L
    }
}
