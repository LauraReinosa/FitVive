package com.example.fitvive1

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun Paso4Screen(
    onComenzarClick: (String) -> Unit
) {
    var focoSeleccionado by remember { mutableStateOf("") }

    val opciones = listOf(
        "Pecho"                     to "🫁",
        "Espalda"                   to "🔙",
        "Hombros"                   to "💪",
        "Brazos"                    to "🦾",
        "Piernas"                   to "🦵",
        "Glúteos"                   to "🍑",
        "Abdomen"                   to "⚡",
        "Cuerpo completo"           to "⚖️"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoOscuro)
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FITVIVE",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = Naranja,
            letterSpacing = 6.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "¿En qué grupo muscular\nquieres enfocarte más?",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Paso 4 de 4",
            fontSize = 15.sp,
            color = TextoSecundario
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "La rutina seguirá siendo completa y equilibrada,\npero dará más volumen a tu grupo elegido.",
            fontSize = 13.sp,
            color = TextoSecundario,
            lineHeight = 18.sp
        )

        Spacer(modifier = Modifier.height(28.dp))

        Column(
            modifier = Modifier.verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            opciones.chunked(2).forEach { fila ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    fila.forEach { (opcion, emoji) ->
                        val seleccionado = focoSeleccionado == opcion
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(62.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (seleccionado) Naranja else TarjetaOscura)
                                .clickable { focoSeleccionado = opcion },
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(text = emoji, fontSize = 18.sp)
                                Text(
                                    text = opcion.uppercase(),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (seleccionado) Color.White else TextoSecundario,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                    }
                    if (fila.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (focoSeleccionado.isNotBlank()) {
                    onComenzarClick(focoSeleccionado)
                }
            },
            enabled = focoSeleccionado.isNotBlank(),
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Naranja,
                contentColor = Color.White,
                disabledContainerColor = Color(0xFF2E2E2E),
                disabledContentColor = TextoSecundario
            )
        ) {
            Text(
                text = "COMENZAR",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
