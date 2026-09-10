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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val BordeCampos1 = Color(0xFF3A3A3A)

@Composable
fun Paso1Screen(
    onNextClick: (
        sexo: String,
        peso: String,
        altura: String,
        fechaNacimiento: String
    ) -> Unit
) {
    var sexo by remember { mutableStateOf("") }
    var peso by remember { mutableStateOf("") }
    var altura by remember { mutableStateOf("") }
    var fechaNacimiento by remember { mutableStateOf("") }

    var errorSexo by remember { mutableStateOf("") }
    var errorPeso by remember { mutableStateOf("") }
    var errorAltura by remember { mutableStateOf("") }
    var errorFecha by remember { mutableStateOf("") }

    val formatoFecha = Regex("^\\d{2}/\\d{2}/\\d{4}$")

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
            text = "Cuéntanos sobre ti",
            fontSize = 26.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Paso 1 de 4",
            fontSize = 15.sp,
            color = TextoSecundario
        )

        Spacer(modifier = Modifier.height(40.dp))

        OutlinedTextField(
            value = sexo,
            onValueChange = { sexo = it; errorSexo = "" },
            label = { Text("Sexo (M/F)") },
            isError = errorSexo.isNotEmpty(),
            supportingText = if (errorSexo.isNotEmpty()) {
                { Text(errorSexo, color = Color(0xFFFF4444)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposOscurosPaso1(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = peso,
            onValueChange = { peso = it; errorPeso = "" },
            label = { Text("Peso (kg)") },
            isError = errorPeso.isNotEmpty(),
            supportingText = if (errorPeso.isNotEmpty()) {
                { Text(errorPeso, color = Color(0xFFFF4444)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposOscurosPaso1(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = altura,
            onValueChange = { altura = it; errorAltura = "" },
            label = { Text("Altura (cm)") },
            isError = errorAltura.isNotEmpty(),
            supportingText = if (errorAltura.isNotEmpty()) {
                { Text(errorAltura, color = Color(0xFFFF4444)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposOscurosPaso1(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(14.dp))

        OutlinedTextField(
            value = fechaNacimiento,
            onValueChange = { fechaNacimiento = it; errorFecha = "" },
            label = { Text("Fecha nacimiento (DD/MM/AAAA)") },
            isError = errorFecha.isNotEmpty(),
            supportingText = if (errorFecha.isNotEmpty()) {
                { Text(errorFecha, color = Color(0xFFFF4444)) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = camposOscurosPaso1(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = {
                errorSexo = ""
                errorPeso = ""
                errorAltura = ""
                errorFecha = ""

                var hayError = false

                if (sexo.isBlank()) {
                    errorSexo = "Indica tu sexo"
                    hayError = true
                }

                val pesoNum = peso.toDoubleOrNull()
                if (pesoNum == null || pesoNum < 30 || pesoNum > 300) {
                    errorPeso = "Introduce un peso válido (30–300 kg)"
                    hayError = true
                }

                val alturaNum = altura.toDoubleOrNull()
                if (alturaNum == null || alturaNum < 100 || alturaNum > 250) {
                    errorAltura = "Introduce una altura válida (100–250 cm)"
                    hayError = true
                }

                if (!formatoFecha.matches(fechaNacimiento)) {
                    errorFecha = "Usa el formato DD/MM/AAAA"
                    hayError = true
                }

                if (!hayError) {
                    onNextClick(sexo, peso, altura, fechaNacimiento)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Naranja,
                contentColor = Color.White
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

@Composable
private fun camposOscurosPaso1() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = Naranja,
    unfocusedBorderColor = BordeCampos1,
    focusedLabelColor = Naranja,
    unfocusedLabelColor = TextoSecundario,
    cursorColor = Naranja,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    errorBorderColor = Color(0xFFFF4444),
    errorLabelColor = Color(0xFFFF4444)
)
