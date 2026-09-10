package com.example.fitvive1.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class EjercicioApiService(private val client: HttpClient) {

    private fun categoriaId(grupoMuscular: String): Int = when {
        grupoMuscular.contains("Pecho",   ignoreCase = true) -> 11
        grupoMuscular.contains("Espalda", ignoreCase = true) -> 12
        grupoMuscular.contains("Piernas", ignoreCase = true) ||
        grupoMuscular.contains("Glúte",   ignoreCase = true) ||
        grupoMuscular.contains("Glute",   ignoreCase = true) ||
        grupoMuscular.contains("Femoral", ignoreCase = true) -> 9
        grupoMuscular.contains("Hombros", ignoreCase = true) -> 13
        grupoMuscular.contains("Bíceps",  ignoreCase = true) ||
        grupoMuscular.contains("Biceps",  ignoreCase = true) ||
        grupoMuscular.contains("Brazos",  ignoreCase = true) ||
        grupoMuscular.contains("Tríceps", ignoreCase = true) ||
        grupoMuscular.contains("Triceps", ignoreCase = true) -> 8
        grupoMuscular.contains("Abdomen", ignoreCase = true) ||
        grupoMuscular.contains("Core",    ignoreCase = true) -> 10
        else -> 12
    }

    private fun parametrosPorGrupo(grupo: String): Triple<Int, Int, Int> = when {
        grupo.contains("Pecho",   ignoreCase = true) -> Triple(4, 10, 90)
        grupo.contains("Espalda", ignoreCase = true) -> Triple(4, 10, 90)
        grupo.contains("Piernas", ignoreCase = true) -> Triple(4, 12, 120)
        grupo.contains("Hombros", ignoreCase = true) -> Triple(3, 12, 60)
        grupo.contains("Bíceps",  ignoreCase = true) ||
        grupo.contains("Biceps",  ignoreCase = true) -> Triple(3, 12, 60)
        grupo.contains("Tríceps", ignoreCase = true) ||
        grupo.contains("Triceps", ignoreCase = true) -> Triple(3, 12, 60)
        grupo.contains("Core",    ignoreCase = true) ||
        grupo.contains("Abdomen", ignoreCase = true) -> Triple(3, 15, 45)
        else -> Triple(3, 10, 60)
    }

    private fun grupoAbreviado(grupo: String): String = when {
        grupo.contains("Pecho",   ignoreCase = true) -> "Pecho"
        grupo.contains("Espalda", ignoreCase = true) -> "Espalda"
        grupo.contains("Piernas", ignoreCase = true) -> "Piernas"
        grupo.contains("Hombros", ignoreCase = true) -> "Hombros"
        grupo.contains("Bíceps",  ignoreCase = true) ||
        grupo.contains("Biceps",  ignoreCase = true) -> "Bíceps"
        grupo.contains("Tríceps", ignoreCase = true) ||
        grupo.contains("Triceps", ignoreCase = true) -> "Tríceps"
        else -> grupo.split("+").first().trim()
    }

    private fun resolveImageUrl(imageField: String): String? {
        if (imageField.isBlank()) return null
        return if (imageField.startsWith("http")) imageField
        else "https://wger.de$imageField"
    }

    /** Devuelve ejercicios enriquecidos: imagen, músculo, equipamiento y parámetros. */
    suspend fun getEjerciciosDetallados(grupoMuscular: String): List<EjercicioApi> {
        val catId = categoriaId(grupoMuscular)
        val (series, reps, descanso) = parametrosPorGrupo(grupoMuscular)

        val response: WgerInfoResponse = client.get("https://wger.de/api/v2/exerciseinfo/") {
            parameter("format",   "json")
            parameter("language", 2)
            parameter("category", catId)
            parameter("limit",    10)
        }.body()

        return response.results.mapNotNull { ejercicio ->
            val nombreEn = ejercicio.translations
                .firstOrNull { it.language == 2 }
                ?.name
                ?.takeIf { it.isNotBlank() }
                ?: return@mapNotNull null

            val imageUrl = ejercicio.images
                .firstOrNull { it.isMain }?.image?.let { resolveImageUrl(it) }
                ?: ejercicio.images.firstOrNull()?.image?.let { resolveImageUrl(it) }

            val musculo = ejercicio.muscles
                .firstOrNull()?.nameEn
                ?.let { traducirMusculo(it) }
                ?: grupoAbreviado(grupoMuscular)

            val equipo = ejercicio.equipment
                .firstOrNull()?.name
                ?.let { traducirEquipamiento(it) }
                ?: "Peso corporal"

            EjercicioApi(
                nombre                   = traducirNombre(nombreEn),
                imageUrl                 = imageUrl,
                musculoPrincipal         = musculo,
                equipamiento             = equipo,
                seriesRecomendadas       = series,
                repeticionesRecomendadas = reps,
                descansoSegundos         = descanso
            )
        }
    }

    /** Compatibilidad con la caché existente: solo devuelve nombres. */
    suspend fun getEjercicios(grupoMuscular: String): List<String> =
        getEjerciciosDetallados(grupoMuscular).map { it.nombre }

    companion object {

        private val NOMBRES_ESPANOL = mapOf(
            "bench press"                  to "Press de banca",
            "barbell bench press"          to "Press de banca con barra",
            "dumbbell bench press"         to "Press de banca con mancuernas",
            "incline bench press"          to "Press inclinado",
            "incline barbell bench press"  to "Press inclinado con barra",
            "incline dumbbell bench press" to "Press inclinado con mancuernas",
            "decline bench press"          to "Press declinado",
            "close grip bench press"       to "Press agarre cerrado",
            "squat"                        to "Sentadilla",
            "barbell squat"                to "Sentadilla con barra",
            "front squat"                  to "Sentadilla frontal",
            "hack squat"                   to "Sentadilla hack",
            "goblet squat"                 to "Sentadilla goblet",
            "bulgarian split squat"        to "Sentadilla búlgara",
            "deadlift"                     to "Peso muerto",
            "romanian deadlift"            to "Peso muerto rumano",
            "sumo deadlift"                to "Peso muerto sumo",
            "stiff leg deadlift"           to "Peso muerto piernas rígidas",
            "pull-up"                      to "Dominada",
            "pull-ups"                     to "Dominadas",
            "chin-up"                      to "Dominada agarre supino",
            "lat pulldown"                 to "Jalón al pecho",
            "wide grip lat pulldown"       to "Jalón al pecho agarre amplio",
            "close grip lat pulldown"      to "Jalón al pecho agarre cerrado",
            "seated cable row"             to "Remo en polea baja",
            "bent over row"                to "Remo inclinado",
            "barbell row"                  to "Remo con barra",
            "barbell bent over row"        to "Remo con barra inclinado",
            "dumbbell row"                 to "Remo con mancuerna",
            "one-arm dumbbell row"         to "Remo unilateral con mancuerna",
            "t-bar row"                    to "Remo en T",
            "cable row"                    to "Remo en polea",
            "bicep curl"                   to "Curl de bíceps",
            "biceps curl"                  to "Curl de bíceps",
            "barbell curl"                 to "Curl de bíceps con barra",
            "dumbbell curl"                to "Curl de bíceps con mancuerna",
            "hammer curl"                  to "Curl de martillo",
            "preacher curl"                to "Curl en banco Scott",
            "concentration curl"           to "Curl concentrado",
            "cable bicep curl"             to "Curl de bíceps en polea",
            "shoulder press"               to "Press de hombros",
            "overhead press"               to "Press militar",
            "military press"               to "Press militar",
            "barbell overhead press"       to "Press militar con barra",
            "dumbbell shoulder press"      to "Press de hombros con mancuernas",
            "arnold press"                 to "Press Arnold",
            "arnold dumbbell press"        to "Press Arnold con mancuernas",
            "lateral raise"                to "Elevación lateral",
            "dumbbell lateral raise"       to "Elevaciones laterales con mancuernas",
            "cable lateral raise"          to "Elevación lateral en polea",
            "front raise"                  to "Elevación frontal",
            "face pull"                    to "Face pull",
            "upright row"                  to "Remo al cuello",
            "shrug"                        to "Encogimiento de hombros",
            "barbell shrug"                to "Encogimiento con barra",
            "dumbbell shrug"               to "Encogimiento con mancuernas",
            "leg press"                    to "Prensa de piernas",
            "leg extension"                to "Extensión de cuádriceps",
            "leg curl"                     to "Curl femoral",
            "lying leg curl"               to "Curl femoral tumbado",
            "seated leg curl"              to "Curl femoral sentado",
            "calf raise"                   to "Elevación de talones",
            "standing calf raise"          to "Elevación de talones de pie",
            "seated calf raise"            to "Elevación de talones sentado",
            "hip thrust"                   to "Hip thrust",
            "barbell hip thrust"           to "Hip thrust con barra",
            "glute bridge"                 to "Puente de glúteos",
            "lunge"                        to "Zancada",
            "walking lunge"                to "Zancada caminando",
            "dumbbell lunge"               to "Zancada con mancuernas",
            "step up"                      to "Subida al cajón",
            "push-up"                      to "Flexión",
            "push-ups"                     to "Flexiones",
            "dip"                          to "Fondos en paralelas",
            "dips"                         to "Fondos en paralelas",
            "chest dip"                    to "Fondos para pecho",
            "tricep dip"                   to "Fondos para tríceps",
            "tricep pushdown"              to "Extensión de tríceps en polea",
            "triceps pushdown"             to "Extensión de tríceps en polea",
            "cable tricep pushdown"        to "Extensión de tríceps en polea",
            "skull crusher"                to "Press francés",
            "tricep extension"             to "Extensión de tríceps",
            "overhead tricep extension"    to "Extensión de tríceps sobre la cabeza",
            "tricep kickback"              to "Patada de tríceps",
            "cable fly"                    to "Aperturas en polea",
            "cable crossover"              to "Cruce en polea",
            "chest fly"                    to "Aperturas de pecho",
            "pec deck fly"                 to "Aperturas en pec-deck",
            "dumbbell fly"                 to "Aperturas con mancuernas",
            "pullover"                     to "Pull-over",
            "cable pullover"               to "Pull-over en polea",
            "plank"                        to "Plancha",
            "crunch"                       to "Crunch abdominal",
            "sit-up"                       to "Abdominal",
            "russian twist"                to "Giro ruso",
            "leg raise"                    to "Elevación de piernas",
            "hanging leg raise"            to "Elevación de piernas colgado",
            "cable crunch"                 to "Crunch en polea",
            "ab rollout"                   to "Rueda abdominal",
            "ab wheel rollout"             to "Rueda abdominal",
            "hyperextension"               to "Hiperextensión",
            "back extension"               to "Extensión de espalda",
            "good morning"                 to "Buenos días",
            "hip abduction"                to "Abducción de cadera",
            "hip adduction"                to "Aducción de cadera",
            "glute kickback"               to "Patada de glúteo",
            "reverse fly"                  to "Aperturas invertidas",
            "rear delt fly"                to "Aperturas de deltoides posterior",
            "box squat"                    to "Sentadilla en cajón",
            "zercher squat"                to "Sentadilla Zercher"
        )

        private val MUSCULOS_ESPANOL = mapOf(
            "Pectoralis major"   to "Pecho",
            "Pectoralis Minor"   to "Pecho",
            "Biceps brachii"     to "Bíceps",
            "Brachialis"         to "Braquial",
            "Brachioradialis"    to "Braquiorradial",
            "Deltoid"            to "Deltoides",
            "Anterior deltoid"   to "Deltoides anterior",
            "Latissimus dorsi"   to "Dorsal ancho",
            "Trapezius"          to "Trapecio",
            "Rhomboids"          to "Romboides",
            "Serratus anterior"  to "Serrato anterior",
            "Triceps brachii"    to "Tríceps",
            "Quadriceps femoris" to "Cuádriceps",
            "Hamstrings"         to "Isquiotibiales",
            "Biceps femoris"     to "Isquiotibiales",
            "Gluteus maximus"    to "Glúteos",
            "Gastrocnemius"      to "Gemelos",
            "Soleus"             to "Sóleo",
            "Abdominals"         to "Abdominales",
            "Rectus abdominis"   to "Abdominales",
            "Obliques"           to "Oblicuos",
            "Erector spinae"     to "Erectores de la espalda",
            "Infraspinatus"      to "Infraespinoso",
            "Teres major"        to "Redondo mayor",
            "Hip flexors"        to "Flexores de cadera",
            "Adductors"          to "Aductores",
            "Abductors"          to "Abductores",
            "Lower back"         to "Espalda baja",
            "Forearms"           to "Antebrazos"
        )

        private val EQUIPAMIENTO_ESPANOL = mapOf(
            "Barbell"         to "Barra",
            "Dumbbell"        to "Mancuerna",
            "Dumbbells"       to "Mancuernas",
            "Cable"           to "Polea",
            "Machine"         to "Máquina",
            "Body weight"     to "Peso corporal",
            "Bodyweight"      to "Peso corporal",
            "Bench"           to "Banco",
            "Pull-up bar"     to "Barra de dominadas",
            "Kettlebell"      to "Kettlebell",
            "Resistance band" to "Banda elástica",
            "EZ Bar"          to "Barra EZ",
            "Ez Bar"          to "Barra EZ",
            "Plate"           to "Disco",
            "Weight plate"    to "Disco",
            "Swiss ball"      to "Pelota suiza",
            "Bands"           to "Bandas elásticas",
            "Dip bars"        to "Paralelas",
            "Parallel bars"   to "Paralelas",
            "Gym mat"         to "Colchoneta",
            "TRX"             to "TRX",
            "Foam roll"       to "Rodillo"
        )

        fun traducirNombre(nombreIngles: String): String {
            val key = nombreIngles.lowercase().trim()
            return NOMBRES_ESPANOL[key]
                ?: NOMBRES_ESPANOL.entries.firstOrNull { key.contains(it.key) }?.value
                ?: nombreIngles
        }

        fun traducirMusculo(nameEn: String): String =
            MUSCULOS_ESPANOL[nameEn]
                ?: MUSCULOS_ESPANOL.entries.firstOrNull {
                    nameEn.contains(it.key, ignoreCase = true)
                }?.value
                ?: nameEn

        fun traducirEquipamiento(nameEn: String): String =
            EQUIPAMIENTO_ESPANOL[nameEn]
                ?: EQUIPAMIENTO_ESPANOL.entries.firstOrNull {
                    nameEn.contains(it.key, ignoreCase = true)
                }?.value
                ?: nameEn
    }
}
