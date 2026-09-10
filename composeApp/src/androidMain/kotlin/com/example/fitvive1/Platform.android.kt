package com.example.fitvive1

import android.os.Build
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fitvive1.notifications.AndroidNotificationScheduler
import com.example.fitvive1.notifications.NotificationScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

actual fun obtenerFechaActual(): String {
    val cal = java.util.Calendar.getInstance()
    val dia = cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')
    val mes = (cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')
    val anio = cal.get(java.util.Calendar.YEAR).toString()
    return "$anio-$mes-$dia"
}

actual fun obtenerMarcaTiempoActual(): String =
    SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }.format(Date())

actual fun createNotificationScheduler(): NotificationScheduler = AndroidNotificationScheduler()
actual fun Modifier.platformStatusBarsPadding(): Modifier = this

@Composable
actual fun BackHandlerEffect(enabled: Boolean, onBack: () -> Unit) {
    BackHandler(enabled = enabled, onBack = onBack)
}
