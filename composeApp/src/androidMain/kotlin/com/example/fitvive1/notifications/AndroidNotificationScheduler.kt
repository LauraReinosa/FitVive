package com.example.fitvive1.notifications

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import com.example.fitvive1.MainActivity
import java.util.Calendar

class AndroidNotificationScheduler : NotificationScheduler {

    private val context: Context get() = MainActivity.appContext
    private val alarmManager: AlarmManager
        get() = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val prefs: SharedPreferences
        get() = context.getSharedPreferences("fitvive_notif_prefs", Context.MODE_PRIVATE)

    override suspend fun requestPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.areNotificationsEnabled()
        } else {
            true
        }
    }

    override fun scheduleEntrenamientoReminder(hora: Int, minuto: Int) {
        prefs.edit()
            .putBoolean("notif_entrenamiento", true)
            .putInt("hora_entrenamiento", hora)
            .putInt("minuto_entrenamiento", minuto)
            .apply()
        scheduleAlarm(RC_ENTRENAMIENTO, hora, minuto, EntrenamientoReminderReceiver::class.java)
    }

    override fun scheduleDietaReminder(hora: Int, minuto: Int) {
        prefs.edit()
            .putBoolean("notif_dieta", true)
            .putInt("hora_dieta", hora)
            .putInt("minuto_dieta", minuto)
            .apply()
        scheduleAlarm(RC_DIETA, hora, minuto, DietaReminderReceiver::class.java)
    }

    override fun scheduleProgresoSemanal(diaSemana: Int, hora: Int, minuto: Int) {
        prefs.edit()
            .putBoolean("notif_progreso", true)
            .putInt("dia_progreso", diaSemana)
            .putInt("hora_progreso", hora)
            .putInt("minuto_progreso", minuto)
            .apply()
        scheduleWeeklyAlarm(RC_PROGRESO, diaSemana, hora, minuto, ProgresoSemanalReceiver::class.java)
    }

    override fun cancelEntrenamientoReminder() {
        prefs.edit().putBoolean("notif_entrenamiento", false).apply()
        cancelAlarm(RC_ENTRENAMIENTO, EntrenamientoReminderReceiver::class.java)
    }

    override fun cancelDietaReminder() {
        prefs.edit().putBoolean("notif_dieta", false).apply()
        cancelAlarm(RC_DIETA, DietaReminderReceiver::class.java)
    }

    override fun cancelProgresoSemanal() {
        prefs.edit().putBoolean("notif_progreso", false).apply()
        cancelAlarm(RC_PROGRESO, ProgresoSemanalReceiver::class.java)
    }

    fun rescheduleFromPrefs() {
        if (prefs.getBoolean("notif_entrenamiento", false)) {
            scheduleAlarm(
                RC_ENTRENAMIENTO,
                prefs.getInt("hora_entrenamiento", 9),
                prefs.getInt("minuto_entrenamiento", 0),
                EntrenamientoReminderReceiver::class.java
            )
        }
        if (prefs.getBoolean("notif_dieta", false)) {
            scheduleAlarm(
                RC_DIETA,
                prefs.getInt("hora_dieta", 12),
                prefs.getInt("minuto_dieta", 0),
                DietaReminderReceiver::class.java
            )
        }
        if (prefs.getBoolean("notif_progreso", false)) {
            scheduleWeeklyAlarm(
                RC_PROGRESO,
                prefs.getInt("dia_progreso", 1),
                prefs.getInt("hora_progreso", 10),
                prefs.getInt("minuto_progreso", 0),
                ProgresoSemanalReceiver::class.java
            )
        }
    }

    private fun scheduleAlarm(requestCode: Int, hora: Int, minuto: Int, receiverClass: Class<*>) {
        val intent = Intent(context, receiverClass)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.DAY_OF_YEAR, 1)
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun scheduleWeeklyAlarm(requestCode: Int, diaSemana: Int, hora: Int, minuto: Int, receiverClass: Class<*>) {
        val intent = Intent(context, receiverClass)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val cal = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_WEEK, diaAppToCalendar(diaSemana))
            set(Calendar.HOUR_OF_DAY, hora)
            set(Calendar.MINUTE, minuto)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
            if (timeInMillis <= System.currentTimeMillis()) add(Calendar.WEEK_OF_YEAR, 1)
        }
        alarmManager.setInexactRepeating(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            AlarmManager.INTERVAL_DAY * 7,
            pendingIntent
        )
    }

    private fun cancelAlarm(requestCode: Int, receiverClass: Class<*>) {
        val intent = Intent(context, receiverClass)
        val pendingIntent = PendingIntent.getBroadcast(
            context, requestCode, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        pendingIntent?.let { alarmManager.cancel(it) }
    }

    private fun diaAppToCalendar(dia: Int): Int = when (dia) {
        1 -> Calendar.MONDAY
        2 -> Calendar.TUESDAY
        3 -> Calendar.WEDNESDAY
        4 -> Calendar.THURSDAY
        5 -> Calendar.FRIDAY
        6 -> Calendar.SATURDAY
        else -> Calendar.SUNDAY
    }

    companion object {
        const val RC_ENTRENAMIENTO = 1001
        const val RC_DIETA = 1002
        const val RC_PROGRESO = 1003

        const val CANAL_ENTRENAMIENTO = "fitvive_entrenamiento"
        const val CANAL_DIETA = "fitvive_dieta"
        const val CANAL_PROGRESO = "fitvive_progreso"

        const val ID_NOTIF_ENTRENAMIENTO = 101
        const val ID_NOTIF_DIETA = 102
        const val ID_NOTIF_PROGRESO = 103

        fun mostrarNotificacion(context: Context, id: Int, titulo: String, cuerpo: String, canalId: String) {
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                android.app.Notification.Builder(context, canalId)
            } else {
                @Suppress("DEPRECATION")
                android.app.Notification.Builder(context)
            }
            builder
                .setSmallIcon(android.R.drawable.ic_popup_reminder)
                .setContentTitle(titulo)
                .setContentText(cuerpo)
                .setAutoCancel(true)
                .setStyle(android.app.Notification.BigTextStyle().bigText(cuerpo))
            manager.notify(id, builder.build())
        }
    }
}
