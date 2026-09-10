package com.example.fitvive1

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

private val FondoLogin = Color(0xFF0D0D0D)
private val NaranjaLogin = Color(0xFFFF6B00)
private val TextoSecLogin = Color(0xFF888888)
private val BordeCampos = Color(0xFF3A3A3A)

@Composable
fun LoginScreen(
    onLogin: suspend (String, String) -> Boolean,
    onRegisterClick: () -> Unit,
    onLoginSuccess: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorEmail by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    var procesando by remember { mutableStateOf(false) }

    val emailValido = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoLogin)
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FITVIVE",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = NaranjaLogin,
            letterSpacing = 6.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Bienvenido de vuelta",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Inicia sesión para continuar",
            fontSize = 15.sp,
            color = TextoSecLogin
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it; errorEmail = ""; error = "" },
            label = { Text("Correo electrónico") },
            isError = errorEmail.isNotEmpty(),
            supportingText = if (errorEmail.isNotEmpty()) {
                { Text(errorEmail, color = Color(0xFFFF4444)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposOscurosLogin(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it; error = "" },
            label = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposOscurosLogin(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                errorEmail = ""
                error = ""

                val emailNormalizado = email.trim()
                if (!emailValido.matches(emailNormalizado)) {
                    errorEmail = "Introduce un correo válido"
                    return@Button
                }

                scope.launch {
                    procesando = true
                    val autenticado = onLogin(emailNormalizado, password)
                    procesando = false
                    if (autenticado) {
                        onLoginSuccess()
                    } else {
                        error = "Credenciales incorrectas"
                    }
                }
            },
            enabled = !procesando,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NaranjaLogin,
                contentColor = Color.White
            )
        ) {
            if (procesando) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(
                    text = "ENTRAR",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        if (error.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(error, color = Color(0xFFFF4444), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))

        TextButton(onClick = onRegisterClick) {
            Text(
                text = "¿No tienes cuenta?  ",
                color = TextoSecLogin,
                fontSize = 14.sp
            )
            Text(
                text = "Regístrate →",
                color = NaranjaLogin,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun camposOscurosLogin() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NaranjaLogin,
    unfocusedBorderColor = BordeCampos,
    focusedLabelColor = NaranjaLogin,
    unfocusedLabelColor = TextoSecLogin,
    cursorColor = NaranjaLogin,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    errorBorderColor = Color(0xFFFF4444),
    errorLabelColor = Color(0xFFFF4444)
)
