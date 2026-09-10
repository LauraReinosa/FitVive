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

@Composable
fun DietaScreen(
    objetivo: String,
    onDiaClick: (Int) -> Unit,
    onDescansoClick: () -> Unit,
    onInicioClick: () -> Unit,
    onEntrenamientosClick: () -> Unit,
    onPerfilClick: () -> Unit
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
                Text(
                    text = "Dieta",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text("🥗", fontSize = 24.sp)
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            listOf(Color(0xFF1E1E1E), Color(0xFF0D0D0D))
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🥗", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nutrición adaptada a tu objetivo",
                        fontSize = 13.sp,
                        color = TextoSecundario,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF1A0D00))
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Text(
                    text = "⚡  Objetivo: $objetivo",
                    fontSize = 14.sp,
                    color = Naranja,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(20.dp))

            Text(
                text = "ELIGE UN DÍA",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoSecundario,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(12.dp))

            val nombresDia = listOf("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado")

            for (dia in 1..6) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(TarjetaOscura)
                        .clickable { onDiaClick(dia) }
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
                                .height(28.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Naranja)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = nombresDia.getOrElse(dia - 1) { "Día $dia" },
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "›",
                            fontSize = 24.sp,
                            color = Naranja,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(TarjetaOscura)
                    .clickable { onDescansoClick() }
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
                            .height(28.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(TextoSecundario)
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = "Domingo",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextoSecundario,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "›",
                        fontSize = 24.sp,
                        color = TextoSecundario,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        BottomMenu(
            onInicioClick = onInicioClick,
            onEntrenamientosClick = onEntrenamientosClick,
            onDietaClick = { },
            onPerfilClick = onPerfilClick
        )
    }
}
