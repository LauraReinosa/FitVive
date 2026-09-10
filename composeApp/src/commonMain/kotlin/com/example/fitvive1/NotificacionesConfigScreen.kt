package com.example.fitvive1

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitvive1.notifications.ConfigNotificaciones
import com.example.fitvive1.notifications.NotificationScheduler
import kotlinx.coroutines.launch

@Composable
fun NotificacionesConfigScreen(
    configActual: ConfigNotificaciones,
    scheduler: NotificationScheduler,
    onGuardar: (ConfigNotificaciones) -> Unit,
    onVolver: () -> Unit
) {
    val scope = rememberCoroutineScope()

    var notifEntrenamiento by remember { mutableStateOf(configActual.notifEntrenamientoActiva) }
    var horaEntrenamientoStr by remember { mutableStateOf(configActual.horaEntrenamiento.toString()) }
    var minutoEntrenamiento by remember { mutableStateOf(configActual.minutoEntrenamiento) }

    var notifDieta by remember { mutableStateOf(configActual.notifDietaActiva) }
    var horaDietaStr by remember { mutableStateOf(configActual.horaDieta.toString()) }
    var minutoDieta by remember { mutableStateOf(configActual.minutoDieta) }

    var notifProgreso by remember { mutableStateOf(configActual.notifProgresoActiva) }
    var diaProgreso by remember { mutableStateOf(configActual.diaProgresoSemana) }
    var horaProgresoStr by remember { mutableStateOf(configActual.horaProgreso.toString()) }
    var minutoProgreso by remember { mutableStateOf(configActual.minutoProgreso) }

    var guardado by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        scope.launch { scheduler.requestPermission() }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text = "FITVIVE",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Naranja,
            letterSpacing = 4.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "NOTIFICACIONES",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Configura tus recordatorios diarios",
            fontSize = 13.sp,
            color = TextoSecundario
        )

        Spacer(Modifier.height(28.dp))

        SeccionNotificacion(
            emoji = "💪",
            titulo = "Entrenamiento",
            subtitulo = "Recordatorio diario para entrenar",
            activa = notifEntrenamiento,
            onActivaChange = { notifEntrenamiento = it },
            horaStr = horaEntrenamientoStr,
            onHoraChange = { horaEntrenamientoStr = it },
            minuto = minutoEntrenamiento,
            onMinutoChange = { minutoEntrenamiento = it }
        )

        Spacer(Modifier.height(16.dp))

        SeccionNotificacion(
            emoji = "🥗",
            titulo = "Dieta",
            subtitulo = "Recordatorio diario para seguir tu plan",
            activa = notifDieta,
            onActivaChange = { notifDieta = it },
            horaStr = horaDietaStr,
            onHoraChange = { horaDietaStr = it },
            minuto = minutoDieta,
            onMinutoChange = { minutoDieta = it }
        )

        Spacer(Modifier.height(16.dp))

        SeccionProgresoSemanal(
            activa = notifProgreso,
            onActivaChange = { notifProgreso = it },
            diaSeleccionado = diaProgreso,
            onDiaChange = { diaProgreso = it },
            horaStr = horaProgresoStr,
            onHoraChange = { horaProgresoStr = it },
            minuto = minutoProgreso,
            onMinutoChange = { minutoProgreso = it }
        )

        Spacer(Modifier.height(28.dp))

        if (guardado) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1B3A1F))
                    .padding(14.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "✓  Configuración guardada",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF4CAF50)
                )
            }
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = {
                val horaE = horaEntrenamientoStr.trim().toIntOrNull()?.coerceIn(0, 23) ?: configActual.horaEntrenamiento
                val horaD = horaDietaStr.trim().toIntOrNull()?.coerceIn(0, 23) ?: configActual.horaDieta
                val horaP = horaProgresoStr.trim().toIntOrNull()?.coerceIn(0, 23) ?: configActual.horaProgreso

                val config = ConfigNotificaciones(
                    notifEntrenamientoActiva = notifEntrenamiento,
                    horaEntrenamiento = horaE,
                    minutoEntrenamiento = minutoEntrenamiento,
                    notifDietaActiva = notifDieta,
                    horaDieta = horaD,
                    minutoDieta = minutoDieta,
                    notifProgresoActiva = notifProgreso,
                    diaProgresoSemana = diaProgreso,
                    horaProgreso = horaP,
                    minutoProgreso = minutoProgreso
                )

                scope.launch {
                    if (notifEntrenamiento) scheduler.scheduleEntrenamientoReminder(horaE, minutoEntrenamiento)
                    else scheduler.cancelEntrenamientoReminder()

                    if (notifDieta) scheduler.scheduleDietaReminder(horaD, minutoDieta)
                    else scheduler.cancelDietaReminder()

                    if (notifProgreso) scheduler.scheduleProgresoSemanal(diaProgreso, horaP, minutoProgreso)
                    else scheduler.cancelProgresoSemanal()
                }

                onGuardar(config)
                guardado = true
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Naranja, contentColor = Color.White)
        ) {
            Text("GUARDAR CONFIGURACIÓN", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BordeOscuro),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoSecundario)
        ) {
            Text("VOLVER", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun SeccionNotificacion(
    emoji: String,
    titulo: String,
    subtitulo: String,
    activa: Boolean,
    onActivaChange: (Boolean) -> Unit,
    horaStr: String,
    onHoraChange: (String) -> Unit,
    minuto: Int,
    onMinutoChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TarjetaOscura)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(emoji, fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text(titulo, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text(subtitulo, fontSize = 12.sp, color = TextoSecundario)
                }
            }
            Switch(
                checked = activa,
                onCheckedChange = onActivaChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Naranja,
                    uncheckedThumbColor = TextoSecundario,
                    uncheckedTrackColor = SuperficieOscura
                )
            )
        }

        if (activa) {
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = BordeOscuro)
            Spacer(Modifier.height(14.dp))

            Text("Hora del recordatorio", fontSize = 13.sp, color = TextoSecundario, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = horaStr,
                    onValueChange = onHoraChange,
                    label = { Text("Hora (0-23)", color = TextoSecundario, fontSize = 11.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(110.dp),
                    colors = campoOscuroColors(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)

                SelectorMinutos(seleccionado = minuto, onSeleccionar = onMinutoChange)
            }
        }
    }
}

@Composable
private fun SeccionProgresoSemanal(
    activa: Boolean,
    onActivaChange: (Boolean) -> Unit,
    diaSeleccionado: Int,
    onDiaChange: (Int) -> Unit,
    horaStr: String,
    onHoraChange: (String) -> Unit,
    minuto: Int,
    onMinutoChange: (Int) -> Unit
) {
    val diasNombres = listOf("L", "M", "X", "J", "V", "S", "D")

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TarjetaOscura)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("⚖️", fontSize = 22.sp)
                Spacer(Modifier.width(10.dp))
                Column {
                    Text("Control semanal", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Recordatorio para registrar tu peso", fontSize = 12.sp, color = TextoSecundario)
                }
            }
            Switch(
                checked = activa,
                onCheckedChange = onActivaChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Naranja,
                    uncheckedThumbColor = TextoSecundario,
                    uncheckedTrackColor = SuperficieOscura
                )
            )
        }

        if (activa) {
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = BordeOscuro)
            Spacer(Modifier.height(14.dp))

            Text("Día de la semana", fontSize = 13.sp, color = TextoSecundario, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                diasNombres.forEachIndexed { idx, nombre ->
                    val dia = idx + 1
                    val seleccionado = diaSeleccionado == dia
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (seleccionado) Naranja else SuperficieOscura)
                            .border(
                                width = if (seleccionado) 0.dp else 1.dp,
                                color = if (seleccionado) Color.Transparent else BordeOscuro,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onDiaChange(dia) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(nombre, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = if (seleccionado) Color.White else TextoSecundario)
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Text("Hora del recordatorio", fontSize = 13.sp, color = TextoSecundario, fontWeight = FontWeight.Medium)
            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = horaStr,
                    onValueChange = onHoraChange,
                    label = { Text("Hora (0-23)", color = TextoSecundario, fontSize = 11.sp) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.width(110.dp),
                    colors = campoOscuroColors(),
                    shape = RoundedCornerShape(10.dp)
                )
                Text(":", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                SelectorMinutos(seleccionado = minuto, onSeleccionar = onMinutoChange)
            }
        }
    }
}

@Composable
private fun SelectorMinutos(seleccionado: Int, onSeleccionar: (Int) -> Unit) {
    val minutos = listOf(0, 15, 30, 45)
    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        minutos.forEach { min ->
            val sel = seleccionado == min
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (sel) Naranja else SuperficieOscura)
                    .border(
                        width = if (sel) 0.dp else 1.dp,
                        color = if (sel) Color.Transparent else BordeOscuro,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSeleccionar(min) },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    ":${min.toString().padStart(2, '0')}",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (sel) Color.White else TextoSecundario
                )
            }
        }
    }
}

@Composable
private fun campoOscuroColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Naranja,
    unfocusedBorderColor = BordeOscuro,
    focusedLabelColor = Naranja,
    cursorColor = Naranja,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    unfocusedContainerColor = SuperficieOscura,
    focusedContainerColor = SuperficieOscura
)
