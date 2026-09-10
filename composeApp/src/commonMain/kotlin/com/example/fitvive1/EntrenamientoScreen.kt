package com.example.fitvive1

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
fun EntrenamientosScreen(
    diasEntrenamiento: Int,
    objetivo: String,
    grupoMuscularFoco: String = "",
    onDiaClick: (Int) -> Unit,
    onInicioClick: () -> Unit,
    onDietaClick: () -> Unit,
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
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 24.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Entrenamientos",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text("💪", fontSize = 24.sp)
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
                    Text("🏋️", fontSize = 44.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Elige tu día de entrenamiento",
                        fontSize = 13.sp,
                        color = TextoSecundario,
                        letterSpacing = 0.5.sp
                    )
                }
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "DÍAS DE ENTRENAMIENTO",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextoSecundario,
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(12.dp))

            for (dia in 1..diasEntrenamiento) {
                val rutina = getRutina(dia, diasEntrenamiento, objetivo, grupoMuscularFoco)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(TarjetaOscura)
                        .clickable { onDiaClick(dia) }
                        .padding(horizontal = 16.dp, vertical = 14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .width(4.dp)
                                .height(38.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Naranja)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "DÍA $dia",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = rutina.grupoMuscular,
                                fontSize = 12.sp,
                                color = TextoSecundario
                            )
                        }
                        Text(
                            text = "›",
                            fontSize = 24.sp,
                            color = Naranja,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(Modifier.height(10.dp))
            }
        }

        BottomMenu(
            onInicioClick = onInicioClick,
            onEntrenamientosClick = {},
            onDietaClick = onDietaClick,
            onPerfilClick = onPerfilClick
        )
    }
}
