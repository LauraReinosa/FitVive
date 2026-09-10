package com.example.fitvive1

import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.fitvive1.notifications.IosNotificationScheduler
import com.example.fitvive1.notifications.NotificationScheduler
import platform.UIKit.UIDevice
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSDate
import platform.Foundation.NSISO8601DateFormatWithFractionalSeconds
import platform.Foundation.NSISO8601DateFormatWithInternetDateTime
import platform.Foundation.NSISO8601DateFormatter

class IOSPlatform : Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

actual fun obtenerFechaActual(): String {
    val formatter = NSDateFormatter()
    formatter.dateFormat = "yyyy-MM-dd"
    return formatter.stringFromDate(NSDate())
}

actual fun obtenerMarcaTiempoActual(): String {
    val formatter = NSISO8601DateFormatter()
    formatter.formatOptions =
        NSISO8601DateFormatWithInternetDateTime or NSISO8601DateFormatWithFractionalSeconds
    return formatter.stringFromDate(NSDate())
}

actual fun createNotificationScheduler(): NotificationScheduler = IosNotificationScheduler()
actual fun Modifier.platformStatusBarsPadding(): Modifier = statusBarsPadding()

@Composable
actual fun BackHandlerEffect(enabled: Boolean, onBack: () -> Unit) {
    // iOS gestiona la navegación atrás nativamente con el gesto de deslizamiento
}
