package com.example.fitvive1

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
fun Paso2Screen(
    onNextClick: (String) -> Unit
) {
    var objetivoSeleccionado by remember { mutableStateOf("") }

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
            text = "¿Cuál es tu objetivo?",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Paso 2 de 4",
            fontSize = 15.sp,
            color = TextoSecundario
        )

        Spacer(modifier = Modifier.height(40.dp))

        listOf("Volumen", "Déficit", "Mantenimiento").forEach { opcion ->
            val seleccionado = objetivoSeleccionado == opcion
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(62.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (seleccionado) Naranja else TarjetaOscura)
                    .clickable { objetivoSeleccionado = opcion },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = opcion.uppercase(),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (seleccionado) Color.White else TextoSecundario,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                if (objetivoSeleccionado.isNotBlank()) {
                    onNextClick(objetivoSeleccionado)
                }
            },
            enabled = objetivoSeleccionado.isNotBlank(),
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
                text = "SIGUIENTE",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
