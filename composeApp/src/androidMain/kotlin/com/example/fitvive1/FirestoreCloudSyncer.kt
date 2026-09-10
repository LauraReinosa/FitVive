package com.example.fitvive1

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.suspendCancellableCoroutine
import java.text.Normalizer
import kotlin.coroutines.resume

class FirestoreCloudSyncer : CloudSyncer {

    private val db = FirebaseFirestore.getInstance()

    private fun docId(email: String) =
        email.replace("@", "_at_").replace(".", "_")

    private fun normalizarId(valor: String): String =
        Normalizer.normalize(valor.trim().lowercase(), Normalizer.Form.NFD)
            .replace(Regex("\\p{M}+"), "")
            .replace(Regex("[^a-z0-9]+"), "_")
            .trim('_')

    private fun registroComidaId(estado: EstadoComidaSync): String =
        "${estado.dia}_${normalizarId(estado.objetivo)}_${normalizarId(estado.tipo)}"

    override suspend fun syncPerfil(
        email: String,
        nombre: String,
        sexo: String,
        peso: String,
        altura: String,
        fechaNacimiento: String,
        objetivo: String,
        diasEntrenamiento: Int,
        grupoMuscularFoco: String
    ) = suspendCancellableCoroutine { cont ->
        db.collection("usuarios").document(docId(email)).set(
            mapOf(
                "email" to email,
                "nombre" to nombre,
                "sexo" to sexo,
                "peso" to peso,
                "altura" to altura,
                "fechaNacimiento" to fechaNacimiento,
                "objetivo" to objetivo,
                "diasEntrenamiento" to diasEntrenamiento,
                "grupoMuscularFoco" to grupoMuscularFoco
            )
        ).addOnCompleteListener { cont.resume(Unit) }
    }

    override suspend fun syncEntrenamiento(
        email: String,
        fecha: String,
        dia: Int,
        grupoMuscular: String
    ) = suspendCancellableCoroutine { cont ->
        db.collection("usuarios").document(docId(email))
            .collection("entrenamientos")
            .add(
                mapOf(
                    "fecha" to fecha,
                    "dia" to dia,
                    "grupo_muscular" to grupoMuscular
                )
            ).addOnCompleteListener { cont.resume(Unit) }
    }

    override suspend fun syncConfigNotificaciones(
        email: String,
        notifEntrenamiento: Boolean,
        horaEntrenamiento: Int,
        minutoEntrenamiento: Int,
        notifDieta: Boolean,
        horaDieta: Int,
        minutoDieta: Int,
        notifProgreso: Boolean,
        diaProgresoSemana: Int,
        horaProgreso: Int,
        minutoProgreso: Int
    ) = suspendCancellableCoroutine { cont ->
        db.collection("usuarios").document(docId(email))
            .collection("config").document("notificaciones")
            .set(
                mapOf(
                    "notifEntrenamiento" to notifEntrenamiento,
                    "horaEntrenamiento" to horaEntrenamiento,
                    "minutoEntrenamiento" to minutoEntrenamiento,
                    "notifDieta" to notifDieta,
                    "horaDieta" to horaDieta,
                    "minutoDieta" to minutoDieta,
                    "notifProgreso" to notifProgreso,
                    "diaProgresoSemana" to diaProgresoSemana,
                    "horaProgreso" to horaProgreso,
                    "minutoProgreso" to minutoProgreso
                )
            ).addOnCompleteListener { cont.resume(Unit) }
    }

    override suspend fun syncRegistroPeso(
        email: String,
        fecha: String,
        peso: String,
        valoracion: Int
    ) = suspendCancellableCoroutine { cont ->
        db.collection("usuarios").document(docId(email))
            .collection("peso")
            .add(mapOf("fecha" to fecha, "peso" to peso, "valoracion" to valoracion))
            .addOnCompleteListener { cont.resume(Unit) }
    }

    override suspend fun syncSeriesEjercicio(
        email: String,
        fecha: String,
        dia: Int,
        grupoMuscular: String,
        registros: List<RegistroEjercicio>
    ) = suspendCancellableCoroutine { cont ->
        val datos = mapOf(
            "fecha" to fecha,
            "dia" to dia,
            "grupo_muscular" to grupoMuscular,
            "ejercicios" to registros.map { reg ->
                mapOf(
                    "nombre" to reg.nombreEjercicio,
                    "series" to reg.series.map { s ->
                        mapOf(
                            "peso" to s.peso,
                            "reps" to s.repsReales,
                            "completada" to s.completada
                        )
                    }
                )
            }
        )
        db.collection("usuarios").document(docId(email))
            .collection("registros_entrenamiento")
            .add(datos)
            .addOnCompleteListener { cont.resume(Unit) }
    }

    @Suppress("UNCHECKED_CAST")
    override suspend fun leerEstadisticasEntrenamiento(email: String): EstadisticasFirestore =
        suspendCancellableCoroutine { cont ->
            db.collection("usuarios").document(docId(email))
                .collection("registros_entrenamiento")
                .get()
                .addOnSuccessListener { snapshot ->
                    var totalSeries = 0
                    var totalReps = 0
                    val progresoEjercicio = mutableMapOf<String, Pair<Float, Float>>()
                    val ejerciciosSet = mutableSetOf<String>()

                    snapshot.documents
                        .sortedBy { it.getString("fecha") ?: "" }
                        .forEach { doc ->
                            val ejercicios = doc.get("ejercicios") as? List<Map<String, Any>>
                                ?: return@forEach
                            ejercicios.forEach { ej ->
                                val nombre = ej["nombre"] as? String ?: return@forEach
                                ejerciciosSet.add(nombre)
                                val series = ej["series"] as? List<Map<String, Any>>
                                    ?: return@forEach
                                series.forEach { s ->
                                    if (s["completada"] as? Boolean == true) {
                                        totalSeries++
                                        totalReps += (s["reps"] as? String)?.toIntOrNull() ?: 0
                                        val peso = (s["peso"] as? String)?.toFloatOrNull() ?: 0f
                                        if (peso > 0f) {
                                            val actual = progresoEjercicio[nombre]
                                            progresoEjercicio[nombre] = if (actual == null)
                                                peso to peso
                                            else
                                                actual.first to peso
                                        }
                                    }
                                }
                            }
                        }

                    val mejorEntry = progresoEjercicio
                        .filter { (_, pesos) -> pesos.first > 0f && pesos.second > pesos.first }
                        .maxByOrNull { (_, pesos) -> (pesos.second - pesos.first) / pesos.first }

                    cont.resume(
                        EstadisticasFirestore(
                            totalSeries = totalSeries,
                            totalReps = totalReps,
                            ejercicioMayorProgreso = mejorEntry?.key ?: "",
                            ejerciciosDisponibles = ejerciciosSet.sorted(),
                            mejorEjercicioPesoInicial = mejorEntry?.value?.first ?: 0f,
                            mejorEjercicioPesoActual = mejorEntry?.value?.second ?: 0f
                        )
                    )
                }
                .addOnFailureListener { cont.resume(EstadisticasFirestore()) }
        }

    @Suppress("UNCHECKED_CAST")
    override suspend fun leerEvolucionEjercicio(
        email: String,
        ejercicio: String
    ): List<Pair<String, Float>> = suspendCancellableCoroutine { cont ->
        db.collection("usuarios").document(docId(email))
            .collection("registros_entrenamiento")
            .get()
            .addOnSuccessListener { snapshot ->
                val result = mutableListOf<Pair<String, Float>>()
                snapshot.documents.forEach { doc ->
                    val fecha = doc.getString("fecha") ?: return@forEach
                    val ejercicios = doc.get("ejercicios") as? List<Map<String, Any>>
                        ?: return@forEach
                    ejercicios.forEach { ej ->
                        val nombre = ej["nombre"] as? String ?: return@forEach
                        if (nombre.equals(ejercicio, ignoreCase = true)) {
                            val series = ej["series"] as? List<Map<String, Any>> ?: return@forEach
                            val maxPeso = series
                                .filter { it["completada"] as? Boolean == true }
                                .mapNotNull { (it["peso"] as? String)?.toFloatOrNull() }
                                .maxOrNull() ?: return@forEach
                            result.add(fecha to maxPeso)
                        }
                    }
                }
                result.sortBy { it.first }
                cont.resume(result)
            }
            .addOnFailureListener { cont.resume(emptyList()) }
    }

    override suspend fun leerEstadosComidas(email: String): List<EstadoComidaSync>? =
        suspendCancellableCoroutine { cont ->
            val emailNormalizado = email.trim().lowercase()
            db.collection("usuarios").document(docId(emailNormalizado))
                .collection("comidas_completadas")
                .get(Source.SERVER)
                .addOnSuccessListener { snapshot ->
                    val estados = snapshot.documents.mapNotNull { doc ->
                        val usuarioEmail = doc.getString("usuario_email")
                            ?.trim()
                            ?.lowercase()
                            ?: return@mapNotNull null
                        if (usuarioEmail != emailNormalizado) return@mapNotNull null

                        EstadoComidaSync(
                            usuarioEmail = usuarioEmail,
                            dia = doc.getLong("dia")?.toInt() ?: return@mapNotNull null,
                            objetivo = doc.getString("objetivo") ?: return@mapNotNull null,
                            tipo = doc.getString("tipo") ?: return@mapNotNull null,
                            completada = doc.getBoolean("completada") ?: return@mapNotNull null,
                            updatedAt = doc.getString("updated_at") ?: return@mapNotNull null
                        )
                    }
                    cont.resume(estados)
                }
                .addOnFailureListener { cont.resume(null) }
        }

    override suspend fun syncEstadoComida(
        email: String,
        estado: EstadoComidaSync
    ): Boolean = suspendCancellableCoroutine { cont ->
        val emailNormalizado = email.trim().lowercase()
        if (estado.usuarioEmail.trim().lowercase() != emailNormalizado) {
            cont.resume(false)
            return@suspendCancellableCoroutine
        }

        val document = db.collection("usuarios").document(docId(emailNormalizado))
            .collection("comidas_completadas")
            .document(registroComidaId(estado))

        db.runTransaction { transaction ->
            val remoto = transaction.get(document)
            val updatedAtRemoto = remoto.getString("updated_at")
            if (updatedAtRemoto == null || estado.updatedAt > updatedAtRemoto) {
                transaction.set(
                    document,
                    mapOf(
                        "usuario_email" to emailNormalizado,
                        "dia" to estado.dia,
                        "objetivo" to estado.objetivo,
                        "tipo" to estado.tipo,
                        "completada" to estado.completada,
                        "updated_at" to estado.updatedAt
                    )
                )
            }
            true
        }
            .addOnSuccessListener { cont.resume(true) }
            .addOnFailureListener { cont.resume(false) }
    }
}

actual fun createCloudSyncer(): CloudSyncer = FirestoreCloudSyncer()
