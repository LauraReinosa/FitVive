package com.example.fitvive1

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.fitvive1.database.ApiRepository
import com.example.fitvive1.network.EjercicioApi
import com.example.fitvive1.utils.toImageBitmap
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.withTimeoutOrNull

// ─── Modelos de registro (usados también en CloudSync y App) ─────────────────

data class SerieRealizada(
    val peso: String,
    val repsReales: String,
    val completada: Boolean
)

data class RegistroEjercicio(
    val nombreEjercicio: String,
    val series: List<SerieRealizada>
)

// Estado mutable por serie (observable por Compose)
class SerieUiState {
    var peso by mutableStateOf("")
    var reps by mutableStateOf("")
    var completada by mutableStateOf(false)
}

// ─── Modelo de visualización ─────────────────────────────────────────────────

data class EjercicioDisplay(
    val nombre: String,
    val imageUrl: String?,
    val musculoPrincipal: String,
    val equipamiento: String,
    val seriesRecomendadas: Int,
    val repeticionesRecomendadas: Int,
    val descansoSegundos: Int
)

private fun EjercicioApi.toDisplay() = EjercicioDisplay(
    nombre                   = nombre,
    imageUrl                 = imageUrl,
    musculoPrincipal         = musculoPrincipal,
    equipamiento             = equipamiento,
    seriesRecomendadas       = seriesRecomendadas,
    repeticionesRecomendadas = repeticionesRecomendadas,
    descansoSegundos         = descansoSegundos
)

private fun Ejercicio.toDisplay(grupoMuscular: String) = EjercicioDisplay(
    nombre                   = nombre,
    imageUrl                 = null,
    musculoPrincipal         = grupoMuscular.split("+", "•", "-").first().trim(),
    equipamiento             = "—",
    seriesRecomendadas       = series,
    repeticionesRecomendadas = repeticiones,
    descansoSegundos         = 60
)

// ─── Modelos de rutina ───────────────────────────────────────────────────────

data class Ejercicio(
    val nombre: String,
    val series: Int,
    val repeticiones: Int
)

data class RutinaDelDia(
    val grupoMuscular: String,
    val ejercicios: List<Ejercicio>
)

private fun parametrosPorObjetivo(objetivo: String): Pair<Int, Int> = when {
    objetivo.contains("volumen", ignoreCase = true) -> Pair(4, 10)
    objetivo.contains("ficit",   ignoreCase = true) -> Pair(3, 12)
    else                                             -> Pair(3, 10)
}

fun getRutina(dia: Int, totalDias: Int, objetivo: String, grupoMuscularFoco: String = ""): RutinaDelDia {
    val (s, r) = parametrosPorObjetivo(objetivo)
    val foco = grupoMuscularFoco.ifBlank { "Cuerpo completo" }
    return when (totalDias) {
        3    -> distribucion3Dias(dia, foco, s, r)
        4    -> distribucion4Dias(dia, foco, s, r)
        5    -> distribucion5Dias(dia, foco, s, r)
        6    -> distribucion6Dias(dia, foco, s, r)
        else -> distribucion3Dias(((dia - 1) % 3) + 1, foco, s, r)
    }
}

// ─── Helpers de clasificación ─────────────────────────────────────────────────

private fun esFocoSuperior(foco: String) = foco in setOf("Pecho", "Espalda", "Hombros", "Brazos")
private fun esFocoInferior(foco: String) = foco in setOf("Piernas", "Glúteos")

// Ejercicio extra que se añade según el foco (variante 0/1/2 para no repetir el mismo)
private fun extraPorFoco(foco: String, variante: Int, s: Int, r: Int): Ejercicio? = when (foco) {
    "Pecho"   -> when (variante % 3) {
        0    -> Ejercicio("Aperturas con mancuernas planas",    s + 1, r + 2)
        1    -> Ejercicio("Aperturas en pec-deck",              s + 1, r + 2)
        else -> Ejercicio("Cruce en polea",                     s + 1, r + 2)
    }
    "Espalda" -> when (variante % 3) {
        0    -> Ejercicio("Remo unilateral con mancuerna",      s + 1, r)
        1    -> Ejercicio("Face pull en polea alta",            s + 1, r + 4)
        else -> Ejercicio("Pull-over en polea",                 s + 1, r + 2)
    }
    "Hombros" -> when (variante % 3) {
        0    -> Ejercicio("Face pull en polea alta",            s + 1, r + 4)
        1    -> Ejercicio("Elevaciones frontales con disco",    s + 1, r + 2)
        else -> Ejercicio("Remo al cuello con barra",           s + 1, r + 2)
    }
    "Brazos"  -> when (variante % 3) {
        0    -> Ejercicio("Superserie bíceps + tríceps en polea", s, r + 2)
        1    -> Ejercicio("Curl en polea con cuerda",             s + 1, r + 2)
        else -> Ejercicio("Fondos en banco trasero",              s + 1, r)
    }
    "Piernas" -> when (variante % 3) {
        0    -> Ejercicio("Sentadilla búlgara con mancuernas",  s + 1, r)
        1    -> Ejercicio("Extensión de cuádriceps en máquina", s + 1, r + 2)
        else -> Ejercicio("Step-up al cajón con mancuernas",    s + 1, r)
    }
    "Glúteos" -> when (variante % 3) {
        0    -> Ejercicio("Hip thrust con barra",               s + 1, r)
        1    -> Ejercicio("Patada de glúteo en máquina",        s + 1, r + 4)
        else -> Ejercicio("Abducción en máquina",               s + 1, r + 4)
    }
    "Abdomen" -> when (variante % 3) {
        0    -> Ejercicio("Crunch en polea",                    3, 20)
        1    -> Ejercicio("Elevación de piernas colgado",       3, r)
        else -> Ejercicio("Giro ruso con disco",                3, 20)
    }
    else -> null // Cuerpo completo — sin extra
}

// ─── 3 DÍAS — Full Body ───────────────────────────────────────────────────────

private fun distribucion3Dias(dia: Int, foco: String, s: Int, r: Int): RutinaDelDia {
    val rutina = when (dia) {
        1 -> RutinaDelDia(
            grupoMuscular = "Full Body — Día A",
            ejercicios = listOf(
                Ejercicio("Sentadilla con barra",                  s, r),
                Ejercicio("Press de banca con barra",              s, r),
                Ejercicio("Jalón al pecho agarre amplio",          s, r),
                Ejercicio("Press militar con mancuernas",          s, r),
                Ejercicio("Curl de bíceps con barra",              s, r + 2),
                Ejercicio("Extensión de tríceps en polea",         s, r + 2),
                Ejercicio("Crunch abdominal",                      s, r + 4)
            )
        )
        2 -> RutinaDelDia(
            grupoMuscular = "Full Body — Día B",
            ejercicios = listOf(
                Ejercicio("Peso muerto rumano",                    s, r),
                Ejercicio("Press inclinado con mancuernas",        s, r),
                Ejercicio("Remo con barra inclinado",              s, r),
                Ejercicio("Elevaciones laterales con mancuernas",  s, r + 4),
                Ejercicio("Curl de martillo con mancuerna",        s, r + 2),
                Ejercicio("Press francés con barra",               s, r + 2),
                Ejercicio("Plancha abdominal (segundos)",          s, 45)
            )
        )
        else -> RutinaDelDia(
            grupoMuscular = "Full Body — Día C",
            ejercicios = listOf(
                Ejercicio("Prensa de piernas",                     s, r),
                Ejercicio("Fondos en paralelas",                   s, r),
                Ejercicio("Dominadas asistidas",                   s, r),
                Ejercicio("Press Arnold con mancuernas",           s, r),
                Ejercicio("Curl concentrado con mancuerna",        s, r + 2),
                Ejercicio("Zancadas caminando con mancuernas",     s, r),
                Ejercicio("Rueda abdominal",                       s, r + 4)
            )
        )
    }
    val extra = extraPorFoco(foco, dia - 1, s, r) ?: return rutina
    return rutina.copy(ejercicios = rutina.ejercicios + extra)
}

// ─── Bloques de tren superior e inferior (compartidos por 4/5/6 días) ─────────

private fun trenSuperiorA(foco: String, s: Int, r: Int): RutinaDelDia {
    val base = listOf(
        Ejercicio("Press de banca con barra",              s, r),
        Ejercicio("Jalón al pecho agarre amplio",          s, r),
        Ejercicio("Press militar con barra",               s, r),
        Ejercicio("Remo con barra inclinado",              s, r),
        Ejercicio("Extensión de tríceps en polea",         s, r + 2),
        Ejercicio("Curl de bíceps con barra",              s, r + 2)
    )
    val extra = if (esFocoSuperior(foco) || foco == "Abdomen") extraPorFoco(foco, 0, s, r) else null
    return RutinaDelDia("Tren Superior — Día A", if (extra != null) base + extra else base)
}

private fun trenSuperiorB(foco: String, s: Int, r: Int): RutinaDelDia {
    val base = listOf(
        Ejercicio("Press inclinado con mancuernas",        s, r),
        Ejercicio("Dominadas o jalón agarre estrecho",     s, r),
        Ejercicio("Press Arnold con mancuernas",           s, r),
        Ejercicio("Remo en polea baja",                    s, r),
        Ejercicio("Press francés con barra",               s, r + 2),
        Ejercicio("Curl de martillo con mancuerna",        s, r + 2),
        Ejercicio("Elevaciones laterales con mancuernas",  s, r + 4)
    )
    val extra = if (esFocoSuperior(foco) || foco == "Abdomen") extraPorFoco(foco, 1, s, r) else null
    return RutinaDelDia("Tren Superior — Día B", if (extra != null) base + extra else base)
}

private fun trenInferiorA(foco: String, s: Int, r: Int): RutinaDelDia {
    val base = listOf(
        Ejercicio("Sentadilla con barra",                  s, r),
        Ejercicio("Peso muerto rumano",                    s, r),
        Ejercicio("Prensa de piernas",                     s, r),
        Ejercicio("Curl femoral tumbado",                  s, r + 2),
        Ejercicio("Elevación de talones de pie",           s, r + 4),
        Ejercicio("Abducción en máquina",                  s, r + 4)
    )
    val extra = if (esFocoInferior(foco) || foco == "Abdomen") extraPorFoco(foco, 0, s, r) else null
    return RutinaDelDia("Tren Inferior — Día A", if (extra != null) base + extra else base)
}

private fun trenInferiorB(foco: String, s: Int, r: Int): RutinaDelDia {
    val base = listOf(
        Ejercicio("Sentadilla frontal",                    s, r),
        Ejercicio("Hip thrust con barra",                  s, r),
        Ejercicio("Zancadas caminando con mancuernas",     s, r),
        Ejercicio("Peso muerto sumo",                      s, r),
        Ejercicio("Extensión de cuádriceps en máquina",    s, r + 2),
        Ejercicio("Elevación de talones sentado",          s, r + 4)
    )
    val extra = if (esFocoInferior(foco) || foco == "Abdomen") extraPorFoco(foco, 1, s, r) else null
    return RutinaDelDia("Tren Inferior — Día B", if (extra != null) base + extra else base)
}

// ─── 4 DÍAS — Tren Superior / Tren Inferior ──────────────────────────────────

private fun distribucion4Dias(dia: Int, foco: String, s: Int, r: Int): RutinaDelDia = when (dia) {
    1    -> trenSuperiorA(foco, s, r)
    2    -> trenInferiorA(foco, s, r)
    3    -> trenSuperiorB(foco, s, r)
    else -> trenInferiorB(foco, s, r)
}

// ─── Día de especialización (5.º y 6.º día) ───────────────────────────────────

private fun diaEspecializacion(foco: String, s: Int, r: Int): RutinaDelDia = when (foco) {
    "Pecho"   -> RutinaDelDia(
        grupoMuscular = "Especialización — Pecho + Hombros + Tríceps",
        ejercicios = listOf(
            Ejercicio("Press de banca con barra",              s + 1, r),
            Ejercicio("Press inclinado con mancuernas",        s, r),
            Ejercicio("Press declinado con mancuernas",        s, r),
            Ejercicio("Aperturas en pec-deck",                 s, r + 2),
            Ejercicio("Elevaciones laterales con mancuernas",  s, r + 4),
            Ejercicio("Extensión de tríceps sobre la cabeza",  s, r + 2),
            Ejercicio("Press francés con barra",               s, r + 2)
        )
    )
    "Espalda" -> RutinaDelDia(
        grupoMuscular = "Especialización — Espalda + Bíceps",
        ejercicios = listOf(
            Ejercicio("Dominadas con peso",                    s + 1, r),
            Ejercicio("Remo con barra inclinado",              s, r),
            Ejercicio("Remo en T con barra",                   s, r),
            Ejercicio("Remo unilateral con mancuerna",         s, r),
            Ejercicio("Pull-over en polea",                    s, r + 2),
            Ejercicio("Curl de bíceps con barra",              s, r + 2),
            Ejercicio("Curl en banco Scott",                   s, r + 2)
        )
    )
    "Hombros" -> RutinaDelDia(
        grupoMuscular = "Especialización — Hombros + Core",
        ejercicios = listOf(
            Ejercicio("Press militar con barra",               s + 1, r),
            Ejercicio("Press Arnold con mancuernas",           s, r),
            Ejercicio("Elevaciones laterales con mancuernas",  s, r + 4),
            Ejercicio("Elevaciones frontales con disco",       s, r + 2),
            Ejercicio("Face pull en polea alta",               s, r + 4),
            Ejercicio("Remo al cuello con barra",              s, r + 2),
            Ejercicio("Plancha abdominal (segundos)",          s, 45)
        )
    )
    "Brazos"  -> RutinaDelDia(
        grupoMuscular = "Especialización — Bíceps + Tríceps + Core",
        ejercicios = listOf(
            Ejercicio("Curl de bíceps con barra",              s + 1, r + 2),
            Ejercicio("Extensión de tríceps en polea",         s + 1, r + 2),
            Ejercicio("Curl de martillo con mancuerna",        s, r + 2),
            Ejercicio("Press francés con barra",               s, r + 2),
            Ejercicio("Curl concentrado con mancuerna",        s, r + 2),
            Ejercicio("Fondos en banco trasero",               s, r),
            Ejercicio("Elevaciones laterales con mancuernas",  s, r + 4),
            Ejercicio("Crunch abdominal",                      s, r + 4)
        )
    )
    "Piernas" -> RutinaDelDia(
        grupoMuscular = "Especialización — Piernas",
        ejercicios = listOf(
            Ejercicio("Sentadilla con barra",                  s + 1, r),
            Ejercicio("Peso muerto sumo",                      s, r),
            Ejercicio("Prensa de piernas",                     s, r),
            Ejercicio("Extensión de cuádriceps en máquina",    s, r + 2),
            Ejercicio("Curl femoral tumbado",                  s, r + 2),
            Ejercicio("Sentadilla búlgara con mancuernas",     s, r),
            Ejercicio("Elevación de talones de pie",           s, r + 4)
        )
    )
    "Glúteos" -> RutinaDelDia(
        grupoMuscular = "Especialización — Glúteos + Femoral + Core",
        ejercicios = listOf(
            Ejercicio("Hip thrust con barra",                  s + 1, r),
            Ejercicio("Peso muerto rumano",                    s, r),
            Ejercicio("Patada de glúteo en máquina",           s, r + 4),
            Ejercicio("Abducción en máquina",                  s, r + 4),
            Ejercicio("Sentadilla sumo con mancuerna",         s, r),
            Ejercicio("Curl femoral tumbado",                  s, r + 2),
            Ejercicio("Plancha abdominal (segundos)",          s, 45)
        )
    )
    "Abdomen" -> RutinaDelDia(
        grupoMuscular = "Core + Movilidad + Cardio suave",
        ejercicios = listOf(
            Ejercicio("Crunch en polea",                       s, 20),
            Ejercicio("Plancha abdominal (segundos)",          s, 45),
            Ejercicio("Elevación de piernas colgado",          s, r + 2),
            Ejercicio("Giro ruso con disco",                   s, 20),
            Ejercicio("Rueda abdominal",                       s, r),
            Ejercicio("Puente de glúteos (movilidad)",         s, r + 4),
            Ejercicio("Cardio suave (minutos)",                1, 20)
        )
    )
    else -> RutinaDelDia( // Cuerpo completo
        grupoMuscular = "Core + Cardio + Movilidad",
        ejercicios = listOf(
            Ejercicio("Plancha abdominal (segundos)",          s, 45),
            Ejercicio("Crunch en polea",                       s, 20),
            Ejercicio("Giro ruso con disco",                   s, 20),
            Ejercicio("Rueda abdominal",                       s, r),
            Ejercicio("Puente de glúteos (movilidad)",         s, r + 4),
            Ejercicio("Cardio suave (minutos)",                1, 20),
            Ejercicio("Elevación de piernas colgado",          s, r + 2)
        )
    )
}

// ─── 5 DÍAS — 4 días base + 1 día de especialización ─────────────────────────

private fun distribucion5Dias(dia: Int, foco: String, s: Int, r: Int): RutinaDelDia = when (dia) {
    1    -> trenSuperiorA(foco, s, r)
    2    -> trenInferiorA(foco, s, r)
    3    -> trenSuperiorB(foco, s, r)
    4    -> trenInferiorB(foco, s, r)
    else -> diaEspecializacion(foco, s, r)
}

// ─── 6 DÍAS — Split completo + especialización + cardio/core/movilidad ────────

private fun diaCardioCore(s: Int, r: Int): RutinaDelDia = RutinaDelDia(
    grupoMuscular = "Cardio + Core + Movilidad",
    ejercicios = listOf(
        Ejercicio("Cardio suave (minutos)",                1, 20),
        Ejercicio("Plancha abdominal (segundos)",          s, 45),
        Ejercicio("Crunch en polea",                       s, 20),
        Ejercicio("Elevación de piernas colgado",          s, r + 2),
        Ejercicio("Giro ruso con disco",                   s, 20),
        Ejercicio("Puente de glúteos (movilidad)",         s, r + 4),
        Ejercicio("Rueda abdominal",                       s, r)
    )
)

private fun distribucion6Dias(dia: Int, foco: String, s: Int, r: Int): RutinaDelDia = when (dia) {
    1    -> trenSuperiorA(foco, s, r)
    2    -> trenInferiorA(foco, s, r)
    3    -> trenSuperiorB(foco, s, r)
    4    -> trenInferiorB(foco, s, r)
    5    -> diaEspecializacion(foco, s, r)
    else -> diaCardioCore(s, r)
}

// ─── Pantalla principal ──────────────────────────────────────────────────────

@Composable
fun DetalleEntrenamientoScreen(
    dia: Int,
    totalDias: Int,
    objetivo: String,
    grupoMuscularFoco: String = "",
    apiRepository: ApiRepository,
    httpClient: HttpClient,
    onFinalizarClick: (grupoMuscular: String, registros: List<RegistroEjercicio>) -> Unit
) {
    val rutinaBase = remember(dia, totalDias, objetivo, grupoMuscularFoco) {
        getRutina(dia, totalDias, objetivo, grupoMuscularFoco)
    }
    var cargando by remember { mutableStateOf(true) }
    var ejerciciosDisplay by remember { mutableStateOf<List<EjercicioDisplay>>(emptyList()) }
    val seriesPorEjercicio = remember { mutableStateMapOf<Int, List<SerieUiState>>() }

    val gruposConApi = listOf("Pecho", "Espalda", "Piernas", "Hombros", "Bíceps", "Biceps",
        "Tríceps", "Triceps", "Abdomen", "Core")
    LaunchedEffect(dia, totalDias, objetivo, grupoMuscularFoco) {
        cargando = true
        val tieneApiGrupo = gruposConApi.any { rutinaBase.grupoMuscular.contains(it, ignoreCase = true) }
        val apiData = if (tieneApiGrupo) apiRepository.getEjerciciosDetallados(rutinaBase.grupoMuscular) else null
        val lista = apiData?.map { it.toDisplay() }
            ?: rutinaBase.ejercicios.map { it.toDisplay(rutinaBase.grupoMuscular) }
        ejerciciosDisplay = lista
        seriesPorEjercicio.clear()
        lista.forEachIndexed { i, ej ->
            seriesPorEjercicio[i] = List(ej.seriesRecomendadas) { SerieUiState() }
        }
        cargando = false
    }

    Column(modifier = Modifier.fillMaxSize().background(FondoOscuro)) {

        // ── Cabecera ─────────────────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF0D0D0D))
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            Text(
                text = "Día $dia",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = rutinaBase.grupoMuscular,
                fontSize = 14.sp,
                color = TextoSecundario
            )
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFF1A0D00))
                    .padding(horizontal = 12.dp, vertical = 5.dp)
            ) {
                Text(
                    text = "Objetivo: $objetivo",
                    fontSize = 12.sp,
                    color = Naranja,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // ── Lista de ejercicios ───────────────────────────────────────────────
        if (cargando) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(color = Naranja)
                    Spacer(Modifier.height(12.dp))
                    Text("Cargando ejercicios...", color = TextoSecundario, fontSize = 13.sp)
                }
            }
        } else {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                ejerciciosDisplay.forEachIndexed { index, ejercicio ->
                    EjercicioDetalleCard(
                        ejercicio  = ejercicio,
                        series     = seriesPorEjercicio[index] ?: emptyList(),
                        httpClient = httpClient
                    )
                    Spacer(Modifier.height(14.dp))
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF1A0D00))
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "⚡ Si podías hacer más reps de las indicadas, sube el peso en la próxima sesión.",
                        color = Naranja,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                }
                Spacer(Modifier.height(16.dp))
            }
        }

        // ── Botón finalizar ───────────────────────────────────────────────────
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
            Button(
                onClick = {
                    val registros = ejerciciosDisplay.mapIndexed { i, ej ->
                        RegistroEjercicio(
                            nombreEjercicio = ej.nombre,
                            series = seriesPorEjercicio[i]?.map { s ->
                                SerieRealizada(s.peso, s.reps, s.completada)
                            } ?: emptyList()
                        )
                    }
                    onFinalizarClick(rutinaBase.grupoMuscular, registros)
                },
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Naranja,
                    contentColor   = Color.White
                ),
                modifier = Modifier.fillMaxWidth().height(54.dp)
            ) {
                Text(
                    text = "FINALIZAR ENTRENAMIENTO",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
            }
        }
    }
}

// ─── Tarjeta de ejercicio ─────────────────────────────────────────────────────

@Composable
private fun EjercicioDetalleCard(
    ejercicio: EjercicioDisplay,
    series: List<SerieUiState>,
    httpClient: HttpClient
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(TarjetaOscura)
    ) {
        EjercicioImagen(
            imageUrl   = ejercicio.imageUrl,
            httpClient = httpClient,
            modifier   = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp))
        )

        Column(modifier = Modifier.padding(14.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .width(3.dp).height(22.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(Naranja)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = ejercicio.nombre,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                if (ejercicio.musculoPrincipal.isNotBlank()) {
                    InfoChip("🎯 ${ejercicio.musculoPrincipal}")
                }
                if (ejercicio.equipamiento.isNotBlank() && ejercicio.equipamiento != "—") {
                    InfoChip("🔧 ${ejercicio.equipamiento}")
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                MetaInfo("SERIES", "${ejercicio.seriesRecomendadas}")
                MetaDivider()
                MetaInfo("REPS", "${ejercicio.repeticionesRecomendadas}")
                MetaDivider()
                MetaInfo("DESCANSO", "${ejercicio.descansoSegundos}s")
            }

            Spacer(Modifier.height(12.dp))
            HorizontalDivider(color = Color(0xFF2A2A2A))
            Spacer(Modifier.height(12.dp))

            Text(
                text = "SERIES REALIZADAS",
                color = TextoSecundario,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.height(8.dp))

            series.forEachIndexed { i, state ->
                SerieRow(numero = i + 1, state = state)
                if (i < series.lastIndex) Spacer(Modifier.height(8.dp))
            }
        }
    }
}

// ─── Fila de serie ────────────────────────────────────────────────────────────

@Composable
private fun SerieRow(numero: Int, state: SerieUiState) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(30.dp)
                .clip(CircleShape)
                .background(if (state.completada) Naranja else Color(0xFF2A2A2A)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$numero",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }

        OutlinedTextField(
            value           = state.peso,
            onValueChange   = { state.peso = it },
            label           = { Text("kg", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine      = true,
            shape           = RoundedCornerShape(8.dp),
            colors          = camposOscuros(),
            modifier        = Modifier.weight(1f)
        )

        OutlinedTextField(
            value           = state.reps,
            onValueChange   = { state.reps = it },
            label           = { Text("reps", fontSize = 11.sp) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine      = true,
            shape           = RoundedCornerShape(8.dp),
            colors          = camposOscuros(),
            modifier        = Modifier.weight(1f)
        )

        Checkbox(
            checked         = state.completada,
            onCheckedChange = { state.completada = it },
            colors          = CheckboxDefaults.colors(
                checkedColor   = Naranja,
                uncheckedColor = Color(0xFF555555),
                checkmarkColor = Color.White
            )
        )
    }
}

// ─── Imagen remota con placeholder ───────────────────────────────────────────

@Composable
private fun EjercicioImagen(
    imageUrl: String?,
    httpClient: HttpClient,
    modifier: Modifier = Modifier
) {
    val bitmap by produceState<ImageBitmap?>(null, imageUrl) {
        value = if (imageUrl != null) {
            try {
                val bytes = withTimeoutOrNull(10_000L) {
                    httpClient.get(imageUrl).body<ByteArray>()
                }
                bytes?.toImageBitmap()
            } catch (_: Exception) { null }
        } else null
    }

    if (bitmap != null) {
        Image(
            bitmap             = bitmap!!,
            contentDescription = null,
            contentScale       = ContentScale.Crop,
            modifier           = modifier
        )
    } else {
        Box(
            modifier         = modifier.background(Color(0xFF1A1A1A)),
            contentAlignment = Alignment.Center
        ) {
            if (imageUrl != null) {
                CircularProgressIndicator(
                    color       = Naranja,
                    modifier    = Modifier.size(32.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("🏋️", fontSize = 44.sp)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "Sin imagen disponible",
                        color = Color(0xFF444444),
                        fontSize = 11.sp
                    )
                }
            }
        }
    }
}

// ─── Componentes auxiliares ───────────────────────────────────────────────────

@Composable
private fun InfoChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color(0xFF2A2A2A))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text, color = TextoSecundario, fontSize = 11.sp)
    }
}

@Composable
private fun MetaInfo(label: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, color = Naranja, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text(label, color = TextoSecundario, fontSize = 9.sp, letterSpacing = 0.5.sp)
    }
}

@Composable
private fun MetaDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(30.dp)
            .background(Color(0xFF2A2A2A))
    )
}

@Composable
private fun camposOscuros() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor   = Naranja,
    unfocusedBorderColor = Color(0xFF3A3A3A),
    focusedLabelColor    = Naranja,
    unfocusedLabelColor  = TextoSecundario,
    cursorColor          = Naranja,
    focusedTextColor     = Color.White,
    unfocusedTextColor   = Color.White
)
