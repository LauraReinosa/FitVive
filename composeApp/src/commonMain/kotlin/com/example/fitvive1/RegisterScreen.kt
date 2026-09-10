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

private val FondoRegister = Color(0xFF0D0D0D)
private val NaranjaRegister = Color(0xFFFF6B00)
private val TextoSecRegister = Color(0xFF888888)
private val BordeOscuroRegister = Color(0xFF3A3A3A)

@Composable
fun RegisterScreen(
    onRegister: suspend (String, String, String) -> Boolean,
    onRegistrationConfirmed: () -> Unit,
    onBackClick: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var nombre by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var repetirPassword by remember { mutableStateOf("") }

    var errorNombre by remember { mutableStateOf("") }
    var errorEmail by remember { mutableStateOf("") }
    var errorPassword by remember { mutableStateOf("") }
    var errorRepetirPassword by remember { mutableStateOf("") }
    var errorRegistro by remember { mutableStateOf("") }
    var procesando by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    val emailValido = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(FondoRegister)
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "FITVIVE",
            fontSize = 24.sp,
            fontWeight = FontWeight.Black,
            color = NaranjaRegister,
            letterSpacing = 6.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Crea tu cuenta",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Empieza tu camino fitness hoy",
            fontSize = 15.sp,
            color = TextoSecRegister
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = nombre,
            onValueChange = { nombre = it; errorNombre = "" },
            label = { Text("Nombre") },
            isError = errorNombre.isNotEmpty(),
            supportingText = if (errorNombre.isNotEmpty()) {
                { Text(errorNombre, color = Color(0xFFFF4444)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposOscurosRegister(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorEmail = ""
                errorRegistro = ""
            },
            label = { Text("Correo electrónico") },
            isError = errorEmail.isNotEmpty(),
            supportingText = if (errorEmail.isNotEmpty()) {
                { Text(errorEmail, color = Color(0xFFFF4444)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposOscurosRegister(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorPassword = ""
                errorRepetirPassword = ""
                errorRegistro = ""
            },
            label = { Text("Contraseña") },
            isError = errorPassword.isNotEmpty(),
            supportingText = if (errorPassword.isNotEmpty()) {
                { Text(errorPassword, color = Color(0xFFFF4444)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposOscurosRegister(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = repetirPassword,
            onValueChange = {
                repetirPassword = it
                errorRepetirPassword = ""
                errorRegistro = ""
            },
            label = { Text("Repetir contraseña") },
            isError = errorRepetirPassword.isNotEmpty(),
            supportingText = if (errorRepetirPassword.isNotEmpty()) {
                { Text(errorRepetirPassword, color = Color(0xFFFF4444)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposOscurosRegister(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                errorNombre = ""
                errorEmail = ""
                errorPassword = ""
                errorRepetirPassword = ""
                errorRegistro = ""

                var hayError = false

                if (nombre.isBlank()) {
                    errorNombre = "El nombre no puede estar vacío"
                    hayError = true
                }
                val emailNormalizado = email.trim()
                if (!emailValido.matches(emailNormalizado)) {
                    errorEmail = "Introduce un correo válido"
                    hayError = true
                }
                if (password.length < 6) {
                    errorPassword = "La contraseña debe tener al menos 6 caracteres"
                    hayError = true
                }
                if (password != repetirPassword) {
                    errorRepetirPassword = "Las contraseñas no coinciden"
                    hayError = true
                }

                if (!hayError) {
                    scope.launch {
                        procesando = true
                        val registrado = onRegister(nombre.trim(), emailNormalizado, password)
                        procesando = false
                        if (registrado) {
                            mostrarConfirmacion = true
                        } else {
                            errorEmail = "Ya existe una cuenta registrada con este correo"
                        }
                    }
                }
            },
            enabled = !procesando,
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = NaranjaRegister,
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
                    text = "REGISTRARSE",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        if (errorRegistro.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(errorRegistro, color = Color(0xFFFF4444), fontSize = 14.sp)
        }

        Spacer(modifier = Modifier.height(28.dp))

        TextButton(onClick = onBackClick) {
            Text(
                text = "¿Ya tienes cuenta?  ",
                color = TextoSecRegister,
                fontSize = 14.sp
            )
            Text(
                text = "Inicia sesión →",
                color = NaranjaRegister,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }

    if (mostrarConfirmacion) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text("Registro completado") },
            text = { Text("Tu cuenta se ha registrado correctamente.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        onRegistrationConfirmed()
                    }
                ) {
                    Text("CONTINUAR")
                }
            }
        )
    }
}

@Composable
private fun camposOscurosRegister() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = NaranjaRegister,
    unfocusedBorderColor = BordeOscuroRegister,
    focusedLabelColor = NaranjaRegister,
    unfocusedLabelColor = TextoSecRegister,
    cursorColor = NaranjaRegister,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    errorBorderColor = Color(0xFFFF4444),
    errorLabelColor = Color(0xFFFF4444)
)
