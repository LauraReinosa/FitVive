package com.example.fitvive1

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.fitvive1.database.DatabaseDriverFactory
import com.example.fitvive1.database.UsuarioRepository
import com.example.fitvive1.notifications.AndroidNotificationScheduler

class MainActivity : ComponentActivity() {

    private lateinit var repository: UsuarioRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        appContext = applicationContext
        repository = UsuarioRepository(DatabaseDriverFactory(this))
        crearCanalesNotificacion()
        pedirPermisoNotificaciones()
        setContent {
            App(repository)
        }
    }

    private fun crearCanalesNotificacion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            listOf(
                Triple(AndroidNotificationScheduler.CANAL_ENTRENAMIENTO, "Entrenamiento", "Recordatorios diarios de entrenamiento"),
                Triple(AndroidNotificationScheduler.CANAL_DIETA, "Dieta", "Recordatorios diarios del plan de alimentación"),
                Triple(AndroidNotificationScheduler.CANAL_PROGRESO, "Progreso semanal", "Recordatorio para registrar el peso semanal")
            ).forEach { (id, nombre, descripcion) ->
                manager.createNotificationChannel(
                    NotificationChannel(id, nombre, NotificationManager.IMPORTANCE_DEFAULT).apply {
                        description = descripcion
                    }
                )
            }
        }
    }

    private fun pedirPermisoNotificaciones() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
    }

    companion object {
        lateinit var appContext: Context
    }
}
