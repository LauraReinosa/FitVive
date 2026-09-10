package com.example.fitvive1

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fitvive1.notifications.NotificationScheduler

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform
expect fun obtenerFechaActual(): String
expect fun obtenerMarcaTiempoActual(): String
expect fun createNotificationScheduler(): NotificationScheduler
expect fun Modifier.platformStatusBarsPadding(): Modifier

@Composable
expect fun BackHandlerEffect(enabled: Boolean = true, onBack: () -> Unit)
