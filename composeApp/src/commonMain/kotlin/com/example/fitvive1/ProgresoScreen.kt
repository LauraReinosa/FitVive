package com.example.fitvive1

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fitvive1.database.Entreno
import com.example.fitvive1.database.Peso_registro

// ─── Helpers ─────────────────────────────────────────────────────────────────

private fun formatDecimal1(value: Float): String {
    val rounded = kotlin.math.round(value * 10).toLong()
    val entero = rounded / 10
    val decimal = kotlin.math.abs(rounded % 10)
    return "$entero.$decimal"
}

private fun estimateDayOfYear(month: Int, day: Int): Int {
    val diasMes = intArrayOf(0, 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    var total = 0
    for (m in 1 until month.coerceAtMost(12)) total += diasMes[m]
    return total + day
}

private fun calcularDiasConsecutivos(entrenamientos: List<Entreno>): Int {
    val fechas = entrenamientos.map { it.fecha }.distinct().sorted()
    if (fechas.isEmpty()) return 0
    var maxConsec = 1
    var actual = 1
    for (i in 1 until fechas.size) {
        val p = fechas[i - 1].split("-").mapNotNull { it.toIntOrNull() }
        val c = fechas[i].split("-").mapNotNull { it.toIntOrNull() }
        if (p.size >= 3 && c.size >= 3) {
            val d1 = estimateDayOfYear(p[1], p[2]) + p[0] * 365
            val d2 = estimateDayOfYear(c[1], c[2]) + c[0] * 365
            if (d2 - d1 == 1) {
                actual++
                if (actual > maxConsec) maxConsec = actual
            } else {
                actual = 1
            }
        }
    }
    return maxConsec
}

// ─── Gráfica de líneas ────────────────────────────────────────────────────────

@Composable
private fun GraficaLineas(
    puntos: List<Pair<String, Float>>,
    colorLinea: Color = Naranja,
    unidad: String = "kg",
    modifier: Modifier = Modifier
) {
    if (puntos.size < 2) {
        SinDatosCard("Registra al menos 2 pesos para ver la gráfica")
        return
    }

    val textMeasurer = rememberTextMeasurer()
    val minVal = puntos.minOf { it.second }
    val maxVal = puntos.maxOf { it.second }
    val rango = (maxVal - minVal).coerceAtLeast(1f)
    val padL = 54f
    val padB = 30f
    val padR = 12f
    val padT = 12f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SuperficieOscura)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width - padL - padR
            val h = size.height - padT - padB

            for (i in 0..3) {
                val ratio = i.toFloat() / 3f
                val cy = padT + h - ratio * h
                drawLine(
                    color = Color(0xFF2A2A2A),
                    start = Offset(padL, cy),
                    end = Offset(padL + w, cy),
                    strokeWidth = 1f
                )
                val label = "${formatDecimal1(minVal + ratio * rango)}$unidad"
                val tm = textMeasurer.measure(label, TextStyle(fontSize = 9.sp, color = TextoSecundario))
                drawText(tm, topLeft = Offset(2f, cy - tm.size.height / 2f))
            }

            val fillPath = Path()
            puntos.forEachIndexed { idx, (_, valor) ->
                val cx = padL + (idx.toFloat() / (puntos.size - 1)) * w
                val cy = padT + h - ((valor - minVal) / rango) * h
                if (idx == 0) fillPath.moveTo(cx, cy) else fillPath.lineTo(cx, cy)
            }
            fillPath.lineTo(padL + w, padT + h)
            fillPath.lineTo(padL, padT + h)
            fillPath.close()
            drawPath(
                fillPath,
                brush = Brush.verticalGradient(
                    listOf(colorLinea.copy(alpha = 0.35f), Color.Transparent),
                    startY = padT,
                    endY = padT + h
                )
            )

            for (i in 1 until puntos.size) {
                val x0 = padL + ((i - 1).toFloat() / (puntos.size - 1)) * w
                val y0 = padT + h - ((puntos[i - 1].second - minVal) / rango) * h
                val x1 = padL + (i.toFloat() / (puntos.size - 1)) * w
                val y1 = padT + h - ((puntos[i].second - minVal) / rango) * h
                drawLine(colorLinea, Offset(x0, y0), Offset(x1, y1), strokeWidth = 2.5f, cap = StrokeCap.Round)
            }

            puntos.forEachIndexed { idx, (fecha, valor) ->
                val cx = padL + (idx.toFloat() / (puntos.size - 1)) * w
                val cy = padT + h - ((valor - minVal) / rango) * h
                drawCircle(colorLinea, 5f, Offset(cx, cy))
                drawCircle(SuperficieOscura, 3f, Offset(cx, cy))
                if (idx == 0 || idx == puntos.size - 1 || (puntos.size > 5 && idx == puntos.size / 2)) {
                    val lbl = fecha.takeLast(5).replace("-", "/")
                    val tm = textMeasurer.measure(lbl, TextStyle(fontSize = 8.sp, color = TextoSecundario))
                    drawText(
                        tm,
                        topLeft = Offset(
                            (cx - tm.size.width / 2f).coerceIn(0f, size.width - tm.size.width.toFloat()),
                            padT + h + 6f
                        )
                    )
                }
            }
        }
    }
}

// ─── Componentes auxiliares ───────────────────────────────────────────────────

@Composable
private fun SinDatosCard(mensaje: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(SuperficieOscura),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("📊", fontSize = 28.sp)
            Spacer(Modifier.height(8.dp))
            Text(mensaje, fontSize = 13.sp, color = TextoSecundario, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun SeccionHeader(emoji: String, titulo: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth().padding(top = 28.dp, bottom = 12.dp)
    ) {
        Text(emoji, fontSize = 16.sp)
        Spacer(Modifier.width(8.dp))
        Text(titulo, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextoSecundario, letterSpacing = 1.sp)
    }
}

@Composable
private fun StatCard(valor: String, etiqueta: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(TarjetaOscura)
            .padding(horizontal = 12.dp, vertical = 18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(valor, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Naranja, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(etiqueta, fontSize = 11.sp, color = TextoSecundario, textAlign = TextAlign.Center, lineHeight = 14.sp)
        }
    }
}

@Composable
private fun InfoPesoCard(label: String, valor: String, positivo: Boolean? = null, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(TarjetaOscura)
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(label, fontSize = 10.sp, color = TextoSecundario)
            Spacer(Modifier.height(4.dp))
            val color = when (positivo) {
                true -> Color(0xFF4CAF50)
                false -> Color(0xFFE57373)
                null -> Color.White
            }
            Text(valor, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun HistorialPesoItem(
    registro: Peso_registro,
    onEditar: () -> Unit,
    onEliminar: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TarjetaOscura)
            .padding(horizontal = 14.dp, vertical = 12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Naranja)
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text("${registro.peso} kg", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(formatearFechaProgreso(registro.fecha), fontSize = 11.sp, color = TextoSecundario)
                if (registro.notas.isNotBlank()) {
                    Text(registro.notas, fontSize = 11.sp, color = Color(0xFF666666), maxLines = 1)
                }
            }
            Text("✏️", fontSize = 16.sp, modifier = Modifier.clickable { onEditar() })
            Spacer(Modifier.width(8.dp))
            Text("🗑️", fontSize = 16.sp, modifier = Modifier.clickable { onEliminar() })
        }
    }
}

@Composable
private fun DialogEditarPeso(
    registroActual: Peso_registro,
    onGuardar: (peso: String, notas: String) -> Unit,
    onDismiss: () -> Unit
) {
    var pesoInput by remember { mutableStateOf(registroActual.peso) }
    var notasInput by remember { mutableStateOf(registroActual.notas) }
    var error by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(TarjetaOscura)
                .padding(24.dp)
        ) {
            Text("Editar registro", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(formatearFechaProgreso(registroActual.fecha), fontSize = 12.sp, color = TextoSecundario)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = pesoInput,
                onValueChange = { pesoInput = it; error = "" },
                label = { Text("Peso (kg)", color = TextoSecundario) },
                singleLine = true,
                isError = error.isNotEmpty(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Naranja,
                    unfocusedBorderColor = BordeOscuro,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    unfocusedContainerColor = SuperficieOscura,
                    focusedContainerColor = SuperficieOscura
                ),
                shape = RoundedCornerShape(12.dp)
            )
            if (error.isNotEmpty()) {
                Text(error, fontSize = 11.sp, color = Color(0xFFE57373))
            }

            Spacer(Modifier.height(10.dp))

            OutlinedTextField(
                value = notasInput,
                onValueChange = { notasInput = it },
                label = { Text("Notas (opcional)", color = TextoSecundario) },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Naranja,
                    unfocusedBorderColor = BordeOscuro,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    unfocusedContainerColor = SuperficieOscura,
                    focusedContainerColor = SuperficieOscura
                ),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(Modifier.height(20.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BordeOscuro),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoSecundario)
                ) { Text("CANCELAR", fontSize = 13.sp) }

                Button(
                    onClick = {
                        val num = pesoInput.trim().replace(",", ".").toFloatOrNull()
                        if (num == null || num < 30f || num > 300f) {
                            error = "Peso inválido (30–300 kg)"
                            return@Button
                        }
                        val rounded = (num.toDouble() * 10 + 0.5).toLong()
                        onGuardar("${rounded / 10}.${rounded % 10}", notasInput.trim())
                    },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Naranja, contentColor = Color.White)
                ) { Text("GUARDAR", fontSize = 13.sp, fontWeight = FontWeight.Bold) }
            }
        }
    }
}

@Composable
private fun MejorProgresoCard(ejercicio: String, pesoInicial: Float, pesoActual: Float) {
    val mejora = pesoActual - pesoInicial
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TarjetaOscura)
            .padding(18.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🏆", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "MEJOR PROGRESO",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = TextoSecundario,
                    letterSpacing = 1.sp
                )
            }
            Spacer(Modifier.height(10.dp))
            Text(ejercicio, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(Modifier.height(4.dp))
            Text(
                "+${formatDecimal1(mejora)} kg desde el inicio",
                fontSize = 14.sp,
                color = Naranja,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ─── Pantalla principal ───────────────────────────────────────────────────────

@Composable
fun ProgresoScreen(
    pesoActual: String,
    totalEntrenamientos: Long,
    todosEntrenamientos: List<Entreno>,
    registrosPeso: List<Peso_registro>,
    estadisticasFirestore: EstadisticasFirestore,
    grupoMuscularFoco: String,
    onRegistrarPesoClick: () -> Unit,
    onEliminarPeso: (Long) -> Unit,
    onActualizarPeso: (Long, String, String) -> Unit,
    onInicioClick: () -> Unit,
    onEntrenamientosClick: () -> Unit,
    onDietaClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    var dialogEditarPeso by remember { mutableStateOf<Peso_registro?>(null) }
    var dialogEliminarId by remember { mutableStateOf<Long?>(null) }

    val pesoInicialFloat = registrosPeso.firstOrNull()?.peso?.toFloatOrNull()
    val pesoActualFloat = pesoActual.toFloatOrNull()
    val difPeso = if (pesoInicialFloat != null && pesoActualFloat != null) pesoActualFloat - pesoInicialFloat else null
    val diasConsecutivos = calcularDiasConsecutivos(todosEntrenamientos)
    val puntosGraficaPeso = registrosPeso.mapNotNull { r -> r.peso.toFloatOrNull()?.let { r.fecha to it } }

    dialogEditarPeso?.let { reg ->
        DialogEditarPeso(
            registroActual = reg,
            onGuardar = { nuevoPeso, notas ->
                onActualizarPeso(reg.id, nuevoPeso, notas)
                dialogEditarPeso = null
            },
            onDismiss = { dialogEditarPeso = null }
        )
    }

    if (dialogEliminarId != null) {
        AlertDialog(
            onDismissRequest = { dialogEliminarId = null },
            containerColor = TarjetaOscura,
            title = { Text("Eliminar registro", color = Color.White) },
            text = { Text("¿Eliminar este registro de peso? Esta acción no se puede deshacer.", color = TextoSecundario) },
            confirmButton = {
                TextButton(onClick = {
                    dialogEliminarId?.let { onEliminarPeso(it) }
                    dialogEliminarId = null
                }) { Text("ELIMINAR", color = Color(0xFFE57373)) }
            },
            dismissButton = {
                TextButton(onClick = { dialogEliminarId = null }) {
                    Text("CANCELAR", color = TextoSecundario)
                }
            }
        )
    }

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro)) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {

            // ── Cabecera ──────────────────────────────────────────────────────
            Text("MI PROGRESO", fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
            Spacer(Modifier.height(4.dp))
            Text("Tu evolución fitness", fontSize = 13.sp, color = TextoSecundario)

            Spacer(Modifier.height(20.dp))

            // ── 3 tarjetas resumen ────────────────────────────────────────────
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                StatCard(
                    valor = if (pesoActual.isNotBlank()) "$pesoActual kg" else "—",
                    etiqueta = "Peso\nactual",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    valor = "$diasConsecutivos",
                    etiqueta = "Días\nconsecutivos",
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    valor = grupoMuscularFoco.ifBlank { "—" }.take(10),
                    etiqueta = "Grupo\nprioridad",
                    modifier = Modifier.weight(1f)
                )
            }

            // ── Evolución del peso ────────────────────────────────────────────
            SeccionHeader("📈", "EVOLUCIÓN DEL PESO")

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                InfoPesoCard(
                    "Peso inicial",
                    if (pesoInicialFloat != null) "${formatDecimal1(pesoInicialFloat)} kg" else "—",
                    modifier = Modifier.weight(1f)
                )
                InfoPesoCard(
                    "Peso actual",
                    if (pesoActualFloat != null) "${formatDecimal1(pesoActualFloat)} kg" else "—",
                    modifier = Modifier.weight(1f)
                )
                InfoPesoCard(
                    "Diferencia",
                    if (difPeso != null) "${if (difPeso >= 0) "+" else ""}${formatDecimal1(difPeso)} kg" else "—",
                    positivo = difPeso?.let { it <= 0 },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))
            GraficaLineas(puntos = puntosGraficaPeso, colorLinea = Naranja, unidad = "kg")

            // ── Registrar peso ────────────────────────────────────────────────
            SeccionHeader("⚖️", "REGISTRO DE PESO")

            Button(
                onClick = onRegistrarPesoClick,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Naranja, contentColor = Color.White)
            ) {
                Text("+ REGISTRAR PESO", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }

            Spacer(Modifier.height(14.dp))

            if (registrosPeso.isEmpty()) {
                SinDatosCard("No hay registros de peso aún.\nPulsa el botón para añadir el primero.")
            } else {
                registrosPeso.reversed().forEach { reg ->
                    HistorialPesoItem(
                        registro = reg,
                        onEditar = { dialogEditarPeso = reg },
                        onEliminar = { dialogEliminarId = reg.id }
                    )
                    Spacer(Modifier.height(8.dp))
                }
            }

            Spacer(Modifier.height(16.dp))
        }

        BottomMenu(
            onInicioClick = onInicioClick,
            onEntrenamientosClick = onEntrenamientosClick,
            onDietaClick = onDietaClick,
            onPerfilClick = onPerfilClick
        )
    }
}

private fun formatearFechaProgreso(iso: String): String {
    if (iso.length < 10) return iso
    return "${iso.substring(8, 10)}/${iso.substring(5, 7)}/${iso.substring(0, 4)}"
}
