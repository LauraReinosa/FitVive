package com.example.fitvive1

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

internal val FondoOscuro = Color(0xFF0D0D0D)
internal val SuperficieOscura = Color(0xFF1A1A1A)
internal val TarjetaOscura = Color(0xFF1E1E1E)
internal val Naranja = Color(0xFFFF6B00)
internal val TextoSecundario = Color(0xFF888888)
internal val BordeOscuro = Color(0xFF2E2E2E)

@Composable
fun HomeScreen(
    nombre: String = "",
    diasEntrenamiento: Int,
    onEntrenamientosClick: () -> Unit,
    onDietaClick: () -> Unit,
    onPerfilClick: () -> Unit,
    onProgresoClick: () -> Unit,
    onAsistenteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (nombre.isNotBlank()) "¡Hola, $nombre!" else "¡Hola!",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Listo para entrenar hoy",
                        fontSize = 13.sp,
                        color = TextoSecundario
                    )
                }

                Text(
                    text = "FIT",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "VIVE",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    color = Naranja
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF1E1E1E), Color(0xFF0D0D0D))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏋️", fontSize = 56.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No pain, no gain.",
                        fontSize = 13.sp,
                        color = Color(0xFF555555),
                        letterSpacing = 1.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A0D00))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "⚡  $diasEntrenamiento días de entrenamiento / semana",
                    fontSize = 14.sp,
                    color = Naranja,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            TarjetaAccion(
                icono = "💪",
                titulo = "ENTRENAMIENTOS",
                subtitulo = "Ver tu plan del día",
                onClick = onEntrenamientosClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            TarjetaAccion(
                icono = "🥗",
                titulo = "DIETA PERSONALIZADA",
                subtitulo = "Tu menú adaptado al objetivo",
                onClick = onDietaClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            TarjetaAccion(
                icono = "📊",
                titulo = "MI PROGRESO",
                subtitulo = "Ver historial de entrenamientos",
                onClick = onProgresoClick
            )

            Spacer(modifier = Modifier.height(14.dp))

            TarjetaAccion(
                icono = "🤖",
                titulo = "ASISTENTE IA",
                subtitulo = "Tu asistente fitness personal",
                onClick = onAsistenteClick
            )
        }

        BottomMenu(
            onInicioClick = { },
            onEntrenamientosClick = onEntrenamientosClick,
            onDietaClick = onDietaClick,
            onPerfilClick = onPerfilClick
        )
    }
}

@Composable
private fun TarjetaAccion(
    icono: String,
    titulo: String,
    subtitulo: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(TarjetaOscura)
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(44.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(Naranja)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(icono, fontSize = 28.sp)

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = titulo,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = subtitulo,
                    fontSize = 12.sp,
                    color = TextoSecundario
                )
            }

            Text(
                text = "›",
                fontSize = 26.sp,
                color = Naranja,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun BottomMenu(
    onInicioClick: () -> Unit,
    onEntrenamientosClick: () -> Unit,
    onDietaClick: () -> Unit,
    onPerfilClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(68.dp)
            .background(SuperficieOscura),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ItemMenu("🏠", "Inicio", onInicioClick)
        ItemMenu("💪", "Entreno", onEntrenamientosClick)
        ItemMenu("🥗", "Dieta", onDietaClick)
        ItemMenu("👤", "Perfil", onPerfilClick)
    }
}

@Composable
private fun ItemMenu(icono: String, etiqueta: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(icono, fontSize = 20.sp)
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = etiqueta,
            fontSize = 11.sp,
            color = TextoSecundario,
            fontWeight = FontWeight.Medium
        )
    }
}
