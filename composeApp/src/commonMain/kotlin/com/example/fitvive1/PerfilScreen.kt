package com.example.fitvive1

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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

private val OPCIONES_FOCO = listOf(
    "Pecho", "Espalda", "Hombros", "Brazos",
    "Piernas", "Glúteos", "Abdomen", "Cuerpo completo"
)

@Composable
fun PerfilScreen(
    nombre: String,
    sexo: String,
    peso: String,
    altura: String,
    fechaNacimiento: String,
    objetivo: String,
    diasEntrenamiento: Int,
    grupoMuscularFoco: String = "",
    onInicioClick: () -> Unit,
    onNotificacionesClick: () -> Unit,
    onCerrarSesionClick: () -> Unit,
    onGuardarPerfil: (peso: String, altura: String, objetivo: String, dias: Int, foco: String) -> Unit
) {
    var modoEdicion by remember { mutableStateOf(false) }
    var pesoEdit by remember(peso) { mutableStateOf(peso) }
    var alturaEdit by remember(altura) { mutableStateOf(altura) }
    var objetivoEdit by remember(objetivo) { mutableStateOf(objetivo) }
    var diasEdit by remember(diasEntrenamiento) { mutableStateOf(diasEntrenamiento) }
    var focoEdit by remember(grupoMuscularFoco) { mutableStateOf(grupoMuscularFoco.ifBlank { "Cuerpo completo" }) }
    var errorPeso by remember { mutableStateOf("") }
    var errorAltura by remember { mutableStateOf("") }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .verticalScroll(scrollState)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
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
            text = "PERFIL",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            letterSpacing = 2.sp
        )

        Spacer(Modifier.height(28.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .border(3.dp, Naranja, CircleShape)
                .background(SuperficieOscura),
            contentAlignment = Alignment.Center
        ) {
            Text("👤", fontSize = 44.sp)
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = nombre.ifBlank { "Usuario" },
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(Modifier.height(8.dp))

        if (!modoEdicion) {
            TextButton(onClick = { modoEdicion = true }) {
                Text(
                    text = "✏️  EDITAR PERFIL",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Naranja,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        if (!modoEdicion) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FilaDato("Sexo", sexo)
                FilaDato("Peso", "$peso kg")
                FilaDato("Altura", "$altura cm")
                FilaDato("Fecha de nacimiento", fechaNacimiento)
                FilaDato("Objetivo", objetivo)
                FilaDato("Grupo prioritario", grupoMuscularFoco.ifBlank { "Cuerpo completo" })
                FilaDato("Días de entrenamiento", "$diasEntrenamiento días / semana")
            }
        } else {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FilaDato("Sexo", sexo)
                FilaDato("Fecha de nacimiento", fechaNacimiento)

                Spacer(Modifier.height(4.dp))

                OutlinedTextField(
                    value = pesoEdit,
                    onValueChange = {
                        pesoEdit = it
                        errorPeso = ""
                    },
                    label = { Text("Peso (kg)", color = TextoSecundario) },
                    singleLine = true,
                    isError = errorPeso.isNotEmpty(),
                    supportingText = if (errorPeso.isNotEmpty()) {
                        { Text(errorPeso, color = Color(0xFFE57373)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                OutlinedTextField(
                    value = alturaEdit,
                    onValueChange = {
                        alturaEdit = it
                        errorAltura = ""
                    },
                    label = { Text("Altura (cm)", color = TextoSecundario) },
                    singleLine = true,
                    isError = errorAltura.isNotEmpty(),
                    supportingText = if (errorAltura.isNotEmpty()) {
                        { Text(errorAltura, color = Color(0xFFE57373)) }
                    } else null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
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

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Objetivo",
                    fontSize = 13.sp,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Medium
                )

                listOf("Volumen", "Déficit", "Mantenimiento").forEach { opcion ->
                    val seleccionado = objetivoEdit == opcion
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (seleccionado) Naranja else TarjetaOscura)
                            .border(
                                width = if (seleccionado) 0.dp else 1.dp,
                                color = if (seleccionado) Color.Transparent else BordeOscuro,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { objetivoEdit = opcion },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = opcion.uppercase(),
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (seleccionado) Color.White else TextoSecundario,
                            letterSpacing = 1.sp
                        )
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Días de entrenamiento / semana",
                    fontSize = 13.sp,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Medium
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3, 4, 5, 6).forEach { dias ->
                        val seleccionado = diasEdit == dias
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(52.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (seleccionado) Naranja else TarjetaOscura)
                                .border(
                                    width = if (seleccionado) 0.dp else 1.dp,
                                    color = if (seleccionado) Color.Transparent else BordeOscuro,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { diasEdit = dias },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$dias",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (seleccionado) Color.White else TextoSecundario
                            )
                        }
                    }
                }

                Spacer(Modifier.height(4.dp))

                Text(
                    text = "Grupo muscular prioritario",
                    fontSize = 13.sp,
                    color = TextoSecundario,
                    fontWeight = FontWeight.Medium
                )

                OPCIONES_FOCO.chunked(2).forEach { fila ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        fila.forEach { opcion ->
                            val sel = focoEdit == opcion
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (sel) Naranja else TarjetaOscura)
                                    .border(
                                        width = if (sel) 0.dp else 1.dp,
                                        color = if (sel) Color.Transparent else BordeOscuro,
                                        shape = RoundedCornerShape(10.dp)
                                    )
                                    .clickable { focoEdit = opcion },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = opcion,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (sel) Color.White else TextoSecundario
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }

                Button(
                    onClick = {
                        val pesoNum = pesoEdit.trim().toFloatOrNull()
                        val alturaNum = alturaEdit.trim().toFloatOrNull()
                        var valido = true
                        if (pesoNum == null || pesoNum < 30f || pesoNum > 300f) {
                            errorPeso = "Introduce un peso válido (30–300 kg)"
                            valido = false
                        }
                        if (alturaNum == null || alturaNum < 100f || alturaNum > 250f) {
                            errorAltura = "Introduce una altura válida (100–250 cm)"
                            valido = false
                        }
                        if (valido) {
                            onGuardarPerfil(pesoEdit.trim(), alturaEdit.trim(), objetivoEdit, diasEdit, focoEdit)
                            modoEdicion = false
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Naranja,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = "GUARDAR CAMBIOS",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                OutlinedButton(
                    onClick = {
                        pesoEdit = peso
                        alturaEdit = altura
                        objetivoEdit = objetivo
                        diasEdit = diasEntrenamiento
                        focoEdit = grupoMuscularFoco.ifBlank { "Cuerpo completo" }
                        errorPeso = ""
                        errorAltura = ""
                        modoEdicion = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BordeOscuro),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextoSecundario)
                ) {
                    Text(
                        text = "CANCELAR",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (!modoEdicion) {
            FilaAccion(
                emoji = "🔔",
                texto = "Notificaciones",
                descripcion = "Configura tus recordatorios",
                onClick = onNotificacionesClick
            )

            Spacer(Modifier.height(10.dp))

            Button(
                onClick = onInicioClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Naranja,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "IR A INICIO",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }

            Spacer(Modifier.height(12.dp))

            OutlinedButton(
                onClick = onCerrarSesionClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE57373)),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE57373))
            ) {
                Text(
                    text = "CERRAR SESIÓN",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun FilaAccion(emoji: String, texto: String, descripcion: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TarjetaOscura)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(emoji, fontSize = 18.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(texto, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                Text(descripcion, fontSize = 12.sp, color = TextoSecundario)
            }
        }
        Text("›", fontSize = 20.sp, color = TextoSecundario, fontWeight = FontWeight.Light)
    }
}

@Composable
private fun FilaDato(etiqueta: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TarjetaOscura)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = etiqueta,
            fontSize = 14.sp,
            color = TextoSecundario
        )
        Text(
            text = valor.ifBlank { "—" },
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White
        )
    }
}
