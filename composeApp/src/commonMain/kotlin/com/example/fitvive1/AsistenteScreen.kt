package com.example.fitvive1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitvive1.network.AsistenteApiService
import kotlinx.coroutines.launch

private data class ChatMessage(val esUsuario: Boolean, val texto: String)

@Composable
fun AsistenteScreen(
    nombre: String,
    objetivo: String,
    peso: String,
    diasEntrenamiento: Int,
    asistenteService: AsistenteApiService,
    onInicioClick: () -> Unit,
    onEntrenamientosClick: () -> Unit,
    onDietaClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val mensajes: SnapshotStateList<ChatMessage> = remember {
        mutableStateListOf(
            ChatMessage(
                esUsuario = false,
                texto = "¡Hola${if (nombre.isNotBlank()) ", $nombre" else ""}! Soy tu asistente fitness. " +
                        "Pregúntame sobre ejercicios, nutrición o cualquier duda sobre tu plan."
            )
        )
    }
    var textoInput by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }

    val contexto = buildString {
        if (nombre.isNotBlank()) append("El usuario se llama $nombre. ")
        if (objetivo.isNotBlank()) append("Su objetivo fitness es $objetivo. ")
        if (peso.isNotBlank()) append("Su peso actual es ${peso}kg. ")
        if (diasEntrenamiento > 0) append("Entrena $diasEntrenamiento días a la semana. ")
    }

    LaunchedEffect(mensajes.size) {
        if (mensajes.isNotEmpty()) {
            listState.animateScrollToItem(mensajes.size - 1)
        }
    }

    fun enviar() {
        val texto = textoInput.trim()
        if (texto.isBlank() || cargando) return
        val historialPrevio = mensajes.toList()
            .map { if (it.esUsuario) "user" to it.texto else "model" to it.texto }
        textoInput = ""
        mensajes.add(ChatMessage(esUsuario = true, texto = texto))
        cargando = true
        scope.launch {
            try {
                val respuesta = asistenteService.enviarMensaje(historialPrevio, texto, contexto)
                mensajes.add(ChatMessage(esUsuario = false, texto = respuesta))
            } catch (e: Exception) {
                mensajes.add(
                    ChatMessage(
                        esUsuario = false,
                        texto = "No se pudo obtener una respuesta. Inténtalo de nuevo en unos segundos."
                    )
                )
            } finally {
                cargando = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SuperficieOscura)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🤖", fontSize = 26.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "ASISTENTE IA",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Asistente fitness personal",
                    fontSize = 12.sp,
                    color = TextoSecundario
                )
            }
        }

        // Lista de mensajes
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(mensajes) { mensaje ->
                BurbujaMensaje(mensaje)
            }
            if (cargando) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp))
                                .background(TarjetaOscura)
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = Naranja,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }

        // Input area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(SuperficieOscura)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textoInput,
                onValueChange = { textoInput = it },
                placeholder = {
                    Text(
                        text = "Pregunta sobre fitness...",
                        color = TextoSecundario,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.weight(1f),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Naranja,
                    unfocusedBorderColor = BordeOscuro,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = Naranja,
                    focusedContainerColor = TarjetaOscura,
                    unfocusedContainerColor = TarjetaOscura
                ),
                shape = RoundedCornerShape(16.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { enviar() })
            )
            Spacer(modifier = Modifier.width(10.dp))
            Button(
                onClick = { enviar() },
                enabled = textoInput.isNotBlank() && !cargando,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Naranja,
                    disabledContainerColor = Color(0xFF3D2000)
                ),
                shape = RoundedCornerShape(14.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Text(
                    text = "➤",
                    fontSize = 18.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        BottomMenu(
            onInicioClick = onInicioClick,
            onEntrenamientosClick = onEntrenamientosClick,
            onDietaClick = onDietaClick,
            onPerfilClick = onPerfilClick
        )
    }
}

@Composable
private fun BurbujaMensaje(mensaje: ChatMessage) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (mensaje.esUsuario) Arrangement.End else Arrangement.Start
    ) {
        if (!mensaje.esUsuario) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(50))
                    .background(Color(0xFF1A0D00)),
                contentAlignment = Alignment.Center
            ) {
                Text("🤖", fontSize = 16.sp)
            }
            Spacer(modifier = Modifier.width(8.dp))
        }

        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(
                    if (mensaje.esUsuario)
                        RoundedCornerShape(topStart = 16.dp, topEnd = 4.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                    else
                        RoundedCornerShape(topStart = 4.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 16.dp)
                )
                .background(if (mensaje.esUsuario) Naranja else TarjetaOscura)
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = mensaje.texto,
                fontSize = 14.sp,
                color = Color.White,
                lineHeight = 20.sp
            )
        }
    }
}
