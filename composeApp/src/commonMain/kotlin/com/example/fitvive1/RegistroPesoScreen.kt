package com.example.fitvive1

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.fitvive1.notifications.generarRecomendacionDieta

@Composable
fun RegistroPesoScreen(
    pesoActual: String,
    objetivo: String,
    ultimoPesoRegistrado: String?,
    onGuardarPeso: (peso: String, valoracion: Int, notas: String) -> Unit,
    onVolver: () -> Unit
) {
    var pesoInput by remember { mutableStateOf(pesoActual) }
    var valoracion by remember { mutableStateOf(3) }
    var notas by remember { mutableStateOf("") }
    var errorPeso by remember { mutableStateOf("") }
    var mostrarDialogo by remember { mutableStateOf(false) }
    var recomendacion by remember { mutableStateOf("") }

    if (mostrarDialogo) {
        DialogRecomendacion(
            recomendacion = recomendacion,
            onDismiss = {
                mostrarDialogo = false
                onVolver()
            }
        )
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
            "FITVIVE",
            fontSize = 14.sp,
            fontWeight = FontWeight.Black,
            color = Naranja,
            letterSpacing = 4.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "REGISTRO SEMANAL",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 2.sp
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Registra tu evolución esta semana",
            fontSize = 13.sp,
            color = TextoSecundario
        )

        Spacer(Modifier.height(28.dp))

        if (ultimoPesoRegistrado != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(TarjetaOscura)
                    .padding(14.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("📊", fontSize = 18.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("Último registro", fontSize = 12.sp, color = TextoSecundario)
                        Text("$ultimoPesoRegistrado kg", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        Text("Tu peso esta semana", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = pesoInput,
            onValueChange = {
                pesoInput = it
                errorPeso = ""
            },
            label = { Text("Peso actual (kg)", color = TextoSecundario) },
            singleLine = true,
            isError = errorPeso.isNotEmpty(),
            supportingText = if (errorPeso.isNotEmpty()) {
                { Text(errorPeso, color = Color(0xFFE57373)) }
            } else null,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Naranja,
                unfocusedBorderColor = BordeOscuro,
                focusedLabelColor = Naranja,
                cursorColor = Naranja,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedContainerColor = TarjetaOscura,
                focusedContainerColor = TarjetaOscura
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(24.dp))

        Text("¿Cómo ha ido tu semana?", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(4.dp))
        Text("Valora tu semana del 1 al 5", fontSize = 12.sp, color = TextoSecundario)
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            (1..5).forEach { nivel ->
                val seleccionado = valoracion == nivel
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (seleccionado) Naranja else TarjetaOscura)
                        .clickable { valoracion = nivel },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "★".repeat(nivel),
                        fontSize = if (nivel <= 2) 12.sp else 10.sp,
                        color = if (seleccionado) Color.White else TextoSecundario,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        val etiquetaValoracion = when (valoracion) {
            1 -> "Semana muy difícil"
            2 -> "Por debajo de lo esperado"
            3 -> "Semana normal"
            4 -> "Buena semana"
            5 -> "Semana excelente"
            else -> ""
        }
        Spacer(Modifier.height(6.dp))
        Text(etiquetaValoracion, fontSize = 12.sp, color = Naranja, fontWeight = FontWeight.Medium)

        Spacer(Modifier.height(24.dp))

        Text("Notas (opcional)", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = notas,
            onValueChange = { notas = it },
            label = { Text("¿Algo que destacar esta semana?", color = TextoSecundario) },
            modifier = Modifier.fillMaxWidth().height(100.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Naranja,
                unfocusedBorderColor = BordeOscuro,
                focusedLabelColor = Naranja,
                cursorColor = Naranja,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                unfocusedContainerColor = TarjetaOscura,
                focusedContainerColor = TarjetaOscura
            ),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(Modifier.height(28.dp))

        Button(
            onClick = {
                val pesoNum = pesoInput.trim().replace(",", ".").toFloatOrNull()
                if (pesoNum == null || pesoNum < 30f || pesoNum > 300f) {
                    errorPeso = "Introduce un peso válido (30–300 kg)"
                    return@Button
                }
                val rounded = (pesoNum.toDouble() * 10 + 0.5).toLong()
                val pesoStr = "${rounded / 10}.${rounded % 10}"
                onGuardarPeso(pesoStr, valoracion, notas.trim())
                recomendacion = generarRecomendacionDieta(ultimoPesoRegistrado, pesoStr, objetivo)
                mostrarDialogo = true
            },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Naranja, contentColor = Color.White)
        ) {
            Text("GUARDAR REGISTRO", fontSize = 16.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(Modifier.height(12.dp))

        OutlinedButton(
            onClick = onVolver,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BordeOscuro),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoSecundario)
        ) {
            Text("CANCELAR", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun DialogRecomendacion(recomendacion: String, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(TarjetaOscura)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("📊", fontSize = 40.sp)
            Spacer(Modifier.height(12.dp))
            Text(
                "Análisis de tu progreso",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(SuperficieOscura)
                    .padding(16.dp)
            ) {
                Text(
                    recomendacion,
                    fontSize = 14.sp,
                    color = Color.White,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start
                )
            }
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Naranja, contentColor = Color.White)
            ) {
                Text("ENTENDIDO", fontSize = 15.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            }
        }
    }
}
