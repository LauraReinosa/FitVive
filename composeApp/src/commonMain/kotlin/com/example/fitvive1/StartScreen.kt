package com.example.fitvive1

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
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

private val FondoStart = Color(0xFF0D0D0D)
private val NaranjaStart = Color(0xFFFF6B00)

@Composable
fun StartScreen(
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    listOf(Color(0xFF0D0D0D), Color(0xFF1A0A00))
                )
            )
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row {
            Text(
                text = "FIT",
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                letterSpacing = 6.sp
            )
            Text(
                text = "VIVE",
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                color = NaranjaStart,
                letterSpacing = 6.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Tu rutina. Tu progreso.",
            fontSize = 16.sp,
            color = Color(0xFF888888),
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(52.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(
                    brush = Brush.verticalGradient(
                        listOf(Color(0xFF1E1E1E), Color(0xFF0D0D0D))
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("💪", fontSize = 80.sp)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Entrena. Come. Mejora.",
                    fontSize = 14.sp,
                    color = Color(0xFF555555),
                    letterSpacing = 1.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(52.dp))

        Button(
            onClick = onLoginClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NaranjaStart,
                contentColor = Color.White
            )
        ) {
            Text(
                text = "INICIAR SESIÓN",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onRegisterClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.5.dp, Color.White),
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = Color.White
            )
        ) {
            Text(
                text = "CREAR CUENTA",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
        }
    }
}
