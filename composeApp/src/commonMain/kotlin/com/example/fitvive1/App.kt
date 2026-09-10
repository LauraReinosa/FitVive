package com.example.fitvive1

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.fitvive1.database.ApiRepository
import com.example.fitvive1.database.Entreno
import com.example.fitvive1.database.UsuarioRepository
import com.example.fitvive1.network.DietaApiService
import com.example.fitvive1.network.EjercicioApiService
import com.example.fitvive1.network.AsistenteApiService
import com.example.fitvive1.network.createHttpClient
import com.example.fitvive1.notifications.ConfigNotificaciones
import kotlinx.coroutines.launch

@Composable
fun App(repository: UsuarioRepository) {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .platformStatusBarsPadding()
        ) {
        val scope = rememberCoroutineScope()
        val syncer = remember { createCloudSyncer() }
        val scheduler = remember { createNotificationScheduler() }
        val httpClient = remember { createHttpClient() }
        val apiRepository = remember {
            ApiRepository(repository, EjercicioApiService(httpClient), DietaApiService(httpClient))
        }
        val asistenteService = remember { AsistenteApiService(httpClient) }

        var currentScreen by remember { mutableStateOf("loading") }
        var diasEntrenamiento by remember { mutableStateOf(0) }
        var diaSeleccionado by remember { mutableStateOf(1) }
        var diaDietaSeleccionado by remember { mutableStateOf(1) }
        var objetivoSeleccionado by remember { mutableStateOf("") }
        var sexoUsuario by remember { mutableStateOf("") }
        var pesoUsuario by remember { mutableStateOf("") }
        var alturaUsuario by remember { mutableStateOf("") }
        var fechaNacimientoUsuario by remember { mutableStateOf("") }
        var nombreUsuario by remember { mutableStateOf("") }
        var emailUsuario by remember { mutableStateOf("") }
        var passwordUsuario by remember { mutableStateOf("") }
        var grupoMuscularFoco by remember { mutableStateOf("") }
        var registrosEntrenamiento by remember { mutableStateOf<List<Entreno>>(emptyList()) }
        var totalEntrenamientos by remember { mutableStateOf(0L) }
        var configNotificaciones by remember { mutableStateOf(ConfigNotificaciones()) }
        var ultimoPesoRegistrado by remember { mutableStateOf<String?>(null) }
        var todosRegistrosPeso by remember { mutableStateOf<List<com.example.fitvive1.database.Peso_registro>>(emptyList()) }
        var todosEntrenamientosList by remember { mutableStateOf<List<com.example.fitvive1.database.Entreno>>(emptyList()) }
        var comidasCompletadasDieta by remember { mutableStateOf<Set<String>>(emptySet()) }
        var estadisticasFirestore by remember { mutableStateOf(EstadisticasFirestore()) }

        fun limpiarUsuarioEnMemoria() {
            diasEntrenamiento = 0
            objetivoSeleccionado = ""
            grupoMuscularFoco = ""
            sexoUsuario = ""
            pesoUsuario = ""
            alturaUsuario = ""
            fechaNacimientoUsuario = ""
            nombreUsuario = ""
            emailUsuario = ""
            passwordUsuario = ""
            registrosEntrenamiento = emptyList()
            totalEntrenamientos = 0
            todosRegistrosPeso = emptyList()
            todosEntrenamientosList = emptyList()
            comidasCompletadasDieta = emptySet()
            ultimoPesoRegistrado = null
            configNotificaciones = ConfigNotificaciones()
            estadisticasFirestore = EstadisticasFirestore()
        }

        suspend fun sincronizarEstadosComidas(email: String) {
            val emailNormalizado = email.trim().lowercase()
            if (emailNormalizado.isBlank()) return

            val remotos = syncer.leerEstadosComidas(emailNormalizado) ?: return
            val locales = repository.getTodosEstadosComidas().map { estado ->
                EstadoComidaSync(
                    usuarioEmail = emailNormalizado,
                    dia = estado.dia.toInt(),
                    objetivo = estado.objetivo,
                    tipo = estado.tipo,
                    completada = estado.completada == 1L,
                    updatedAt = estado.updated_at
                )
            }
            val resultado = resolverSyncComidas(emailNormalizado, locales, remotos)

            resultado.guardarLocal.forEach { estado ->
                repository.guardarEstadoComida(
                    dia = estado.dia,
                    objetivo = estado.objetivo,
                    tipo = estado.tipo,
                    completada = estado.completada,
                    updatedAt = estado.updatedAt
                )
            }
            resultado.enviarRemoto.forEach { estado ->
                syncer.syncEstadoComida(emailNormalizado, estado)
            }
        }

        fun abrirDieta() {
            currentScreen = "dieta"
            if (emailUsuario.isNotBlank()) {
                scope.launch { sincronizarEstadosComidas(emailUsuario) }
            }
        }

        BackHandlerEffect(enabled = currentScreen !in setOf("start", "loading", "home")) {
            when (currentScreen) {
                "login", "register"       -> currentScreen = "start"
                "step1"                   -> scope.launch {
                    repository.cerrarSesion()
                    limpiarUsuarioEnMemoria()
                    currentScreen = "start"
                }
                "step2"                   -> currentScreen = "step1"
                "step3"                   -> currentScreen = "step2"
                "step4"                   -> currentScreen = "step3"
                "entrenamientos",
                "dieta",
                "perfil",
                "progreso",
                "asistente"               -> currentScreen = "home"
                "detalleEntrenamiento"    -> currentScreen = "entrenamientos"
                "detalleDieta"            -> currentScreen = "dieta"
                "notificaciones"          -> currentScreen = "perfil"
                "registroPeso"            -> currentScreen = "progreso"
                else                      -> currentScreen = "home"
            }
        }

        LaunchedEffect(Unit) {
            val usuario = repository.obtenerUsuarioSesionActiva()
            if (usuario != null) {
                nombreUsuario = usuario.nombre
                emailUsuario = usuario.email
                passwordUsuario = usuario.password
                sexoUsuario = usuario.sexo
                pesoUsuario = usuario.peso
                alturaUsuario = usuario.altura
                fechaNacimientoUsuario = usuario.fechaNacimiento
                objetivoSeleccionado = usuario.objetivo
                diasEntrenamiento = usuario.diasEntrenamiento.toInt()
                grupoMuscularFoco = usuario.grupoMuscularFoco
                currentScreen = if (usuario.diasEntrenamiento > 0) "home" else "step1"
                if (usuario.diasEntrenamiento > 0) {
                    syncer.syncPerfil(usuario.email, usuario.nombre, usuario.sexo, usuario.peso,
                        usuario.altura, usuario.fechaNacimiento, usuario.objetivo,
                        usuario.diasEntrenamiento.toInt(), usuario.grupoMuscularFoco)
                }
                val configDb = repository.obtenerConfigNotificaciones()
                if (configDb != null) {
                    configNotificaciones = ConfigNotificaciones(
                        notifEntrenamientoActiva = configDb.notif_entrenamiento == 1L,
                        horaEntrenamiento = configDb.hora_entrenamiento.toInt(),
                        minutoEntrenamiento = configDb.minuto_entrenamiento.toInt(),
                        notifDietaActiva = configDb.notif_dieta == 1L,
                        horaDieta = configDb.hora_dieta.toInt(),
                        minutoDieta = configDb.minuto_dieta.toInt(),
                        notifProgresoActiva = configDb.notif_progreso == 1L,
                        diaProgresoSemana = configDb.dia_progreso_semana.toInt(),
                        horaProgreso = configDb.hora_progreso.toInt(),
                        minutoProgreso = configDb.minuto_progreso.toInt()
                    )
                }
                ultimoPesoRegistrado = repository.obtenerUltimoRegistroPeso()?.peso
                sincronizarEstadosComidas(usuario.email)
            } else {
                limpiarUsuarioEnMemoria()
                currentScreen = "start"
            }
        }

        when (currentScreen) {

            "loading" -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }

            "start" -> StartScreen(
                onLoginClick = { currentScreen = "login" },
                onRegisterClick = {
                    limpiarUsuarioEnMemoria()
                    currentScreen = "register"
                }
            )

            "login" -> LoginScreen(
                onLogin = { email, password ->
                    repository.iniciarSesion(email, password) != null
                },
                onRegisterClick = { currentScreen = "register" },
                onLoginSuccess = {
                    scope.launch {
                        val usuario = repository.obtenerUsuarioSesionActiva()
                        if (usuario != null) {
                            nombreUsuario = usuario.nombre
                            emailUsuario = usuario.email
                            passwordUsuario = usuario.password
                            sexoUsuario = usuario.sexo
                            pesoUsuario = usuario.peso
                            alturaUsuario = usuario.altura
                            fechaNacimientoUsuario = usuario.fechaNacimiento
                            objetivoSeleccionado = usuario.objetivo
                            diasEntrenamiento = usuario.diasEntrenamiento.toInt()
                            grupoMuscularFoco = usuario.grupoMuscularFoco
                            val configDb = repository.obtenerConfigNotificaciones()
                            configNotificaciones = if (configDb != null) {
                                ConfigNotificaciones(
                                    notifEntrenamientoActiva = configDb.notif_entrenamiento == 1L,
                                    horaEntrenamiento = configDb.hora_entrenamiento.toInt(),
                                    minutoEntrenamiento = configDb.minuto_entrenamiento.toInt(),
                                    notifDietaActiva = configDb.notif_dieta == 1L,
                                    horaDieta = configDb.hora_dieta.toInt(),
                                    minutoDieta = configDb.minuto_dieta.toInt(),
                                    notifProgresoActiva = configDb.notif_progreso == 1L,
                                    diaProgresoSemana = configDb.dia_progreso_semana.toInt(),
                                    horaProgreso = configDb.hora_progreso.toInt(),
                                    minutoProgreso = configDb.minuto_progreso.toInt()
                                )
                            } else {
                                ConfigNotificaciones()
                            }
                            ultimoPesoRegistrado = repository.obtenerUltimoRegistroPeso()?.peso
                            sincronizarEstadosComidas(usuario.email)
                        }
                        currentScreen = if ((usuario?.diasEntrenamiento ?: 0) > 0) "home" else "step1"
                    }
                }
            )

            "register" -> RegisterScreen(
                onRegister = { nombre, email, password ->
                    repository.registrarUsuarioSiNoExiste(nombre, email, password)
                },
                onRegistrationConfirmed = { currentScreen = "login" },
                onBackClick = { currentScreen = "login" }
            )

            "step1" -> Paso1Screen(
                onNextClick = { sexo, peso, altura, fechaNacimiento ->
                    sexoUsuario = sexo
                    pesoUsuario = peso
                    alturaUsuario = altura
                    fechaNacimientoUsuario = fechaNacimiento
                    scope.launch {
                        repository.guardarPaso1(sexo, peso, altura, fechaNacimiento)
                    }
                    currentScreen = "step2"
                }
            )

            "step2" -> Paso2Screen(
                onNextClick = { objetivo ->
                    objetivoSeleccionado = objetivo
                    scope.launch {
                        repository.guardarObjetivo(objetivo)
                    }
                    currentScreen = "step3"
                }
            )

            "step3" -> Paso3Screen(
                onDiasSeleccionados = { dias ->
                    diasEntrenamiento = dias
                    scope.launch {
                        repository.guardarDiasEntrenamiento(dias)
                    }
                    currentScreen = "step4"
                }
            )

            "step4" -> Paso4Screen(
                onComenzarClick = { foco ->
                    grupoMuscularFoco = foco
                    scope.launch {
                        repository.guardarGrupoMuscularFoco(foco)
                        syncer.syncPerfil(emailUsuario, nombreUsuario, sexoUsuario, pesoUsuario,
                            alturaUsuario, fechaNacimientoUsuario, objetivoSeleccionado,
                            diasEntrenamiento, foco)
                    }
                    currentScreen = "home"
                }
            )

            "home" -> HomeScreen(
                nombre = nombreUsuario,
                diasEntrenamiento = diasEntrenamiento,
                onEntrenamientosClick = { currentScreen = "entrenamientos" },
                onDietaClick = { abrirDieta() },
                onPerfilClick = { currentScreen = "perfil" },
                onProgresoClick = {
                    scope.launch {
                        totalEntrenamientos = repository.contarEntrenamientos()
                        todosRegistrosPeso = repository.obtenerTodosRegistrosPeso()
                        todosEntrenamientosList = repository.obtenerTodosEntrenamientos()
                        estadisticasFirestore = syncer.leerEstadisticasEntrenamiento(emailUsuario)
                    }
                    currentScreen = "progreso"
                },
                onAsistenteClick = { currentScreen = "asistente" }
            )

            "entrenamientos" -> EntrenamientosScreen(
                diasEntrenamiento = diasEntrenamiento,
                objetivo = objetivoSeleccionado,
                grupoMuscularFoco = grupoMuscularFoco,
                onDiaClick = { dia ->
                    diaSeleccionado = dia
                    currentScreen = "detalleEntrenamiento"
                },
                onInicioClick = { currentScreen = "home" },
                onDietaClick = { abrirDieta() },
                onPerfilClick = { currentScreen = "perfil" }
            )

            "detalleEntrenamiento" -> DetalleEntrenamientoScreen(
                dia = diaSeleccionado,
                totalDias = diasEntrenamiento,
                objetivo = objetivoSeleccionado,
                grupoMuscularFoco = grupoMuscularFoco,
                apiRepository = apiRepository,
                httpClient = httpClient,
                onFinalizarClick = { grupoMuscular, registros ->
                    val fecha = obtenerFechaActual()
                    scope.launch {
                        repository.guardarEntrenamiento(fecha, diaSeleccionado, grupoMuscular)
                        syncer.syncEntrenamiento(emailUsuario, fecha, diaSeleccionado, grupoMuscular)
                        syncer.syncSeriesEjercicio(emailUsuario, fecha, diaSeleccionado, grupoMuscular, registros)
                    }
                    currentScreen = "entrenamientos"
                }
            )

            "dieta" -> DietaScreen(
                objetivo = objetivoSeleccionado,
                onDiaClick = { dia ->
                    diaDietaSeleccionado = dia
                    comidasCompletadasDieta = emptySet()
                    scope.launch {
                        sincronizarEstadosComidas(emailUsuario)
                        comidasCompletadasDieta = repository
                            .getComidasCompletadas(dia, objetivoSeleccionado)
                            .map { it.tipo }
                            .toSet()
                    }
                    currentScreen = "detalleDieta"
                },
                onDescansoClick = {
                    diaDietaSeleccionado = 0
                    comidasCompletadasDieta = emptySet()
                    currentScreen = "detalleDieta"
                },
                onInicioClick = { currentScreen = "home" },
                onEntrenamientosClick = { currentScreen = "entrenamientos" },
                onPerfilClick = { currentScreen = "perfil" }
            )

            "detalleDieta" -> DetalleDietaScreen(
                dia = diaDietaSeleccionado,
                objetivo = objetivoSeleccionado,
                pesoUsuario = pesoUsuario,
                alturaUsuario = alturaUsuario,
                sexoUsuario = sexoUsuario,
                apiRepository = apiRepository,
                comidasCompletadas = comidasCompletadasDieta,
                onComidaCompletadaChange = { tipo, completada ->
                    scope.launch {
                        val updatedAt = obtenerMarcaTiempoActual()
                        repository.guardarEstadoComida(
                            dia = diaDietaSeleccionado,
                            objetivo = objetivoSeleccionado,
                            tipo = tipo,
                            completada = completada,
                            updatedAt = updatedAt
                        )
                        comidasCompletadasDieta = repository
                            .getComidasCompletadas(diaDietaSeleccionado, objetivoSeleccionado)
                            .map { it.tipo }
                            .toSet()
                        val estado = EstadoComidaSync(
                            usuarioEmail = emailUsuario.trim().lowercase(),
                            dia = diaDietaSeleccionado,
                            objetivo = objetivoSeleccionado,
                            tipo = tipo,
                            completada = completada,
                            updatedAt = updatedAt
                        )
                        syncer.syncEstadoComida(emailUsuario, estado)
                        sincronizarEstadosComidas(emailUsuario)
                        comidasCompletadasDieta = repository
                            .getComidasCompletadas(diaDietaSeleccionado, objetivoSeleccionado)
                            .map { it.tipo }
                            .toSet()
                    }
                },
                onFinalizarClick = { currentScreen = "dieta" }
            )

            "progreso" -> ProgresoScreen(
                pesoActual = pesoUsuario,
                totalEntrenamientos = totalEntrenamientos,
                todosEntrenamientos = todosEntrenamientosList,
                registrosPeso = todosRegistrosPeso,
                estadisticasFirestore = estadisticasFirestore,
                grupoMuscularFoco = grupoMuscularFoco,
                onRegistrarPesoClick = {
                    scope.launch {
                        val ultimo = repository.obtenerUltimoRegistroPeso()
                        ultimoPesoRegistrado = ultimo?.peso
                    }
                    currentScreen = "registroPeso"
                },
                onEliminarPeso = { id ->
                    scope.launch {
                        repository.eliminarRegistroPeso(id)
                        todosRegistrosPeso = repository.obtenerTodosRegistrosPeso()
                    }
                },
                onActualizarPeso = { id, nuevoPeso, notas ->
                    scope.launch {
                        repository.actualizarRegistroPeso(id, nuevoPeso, notas)
                        todosRegistrosPeso = repository.obtenerTodosRegistrosPeso()
                        pesoUsuario = nuevoPeso
                    }
                },
                onInicioClick = { currentScreen = "home" },
                onEntrenamientosClick = { currentScreen = "entrenamientos" },
                onDietaClick = { abrirDieta() },
                onPerfilClick = { currentScreen = "perfil" }
            )

            "registroPeso" -> RegistroPesoScreen(
                pesoActual = pesoUsuario,
                objetivo = objetivoSeleccionado,
                ultimoPesoRegistrado = ultimoPesoRegistrado,
                onGuardarPeso = { nuevoPeso, valoracion, notas ->
                    pesoUsuario = nuevoPeso
                    val fecha = obtenerFechaActual()
                    scope.launch {
                        repository.guardarRegistroPeso(fecha, nuevoPeso, valoracion, notas)
                        repository.actualizarPerfil(nuevoPeso, alturaUsuario, objetivoSeleccionado, diasEntrenamiento)
                        syncer.syncPerfil(emailUsuario, nombreUsuario, sexoUsuario, nuevoPeso,
                            alturaUsuario, fechaNacimientoUsuario, objetivoSeleccionado, diasEntrenamiento,
                            grupoMuscularFoco)
                        syncer.syncRegistroPeso(emailUsuario, fecha, nuevoPeso, valoracion)
                        ultimoPesoRegistrado = nuevoPeso
                        todosRegistrosPeso = repository.obtenerTodosRegistrosPeso()
                    }
                },
                onVolver = { currentScreen = "progreso" }
            )

            "asistente" -> AsistenteScreen(
                nombre = nombreUsuario,
                objetivo = objetivoSeleccionado,
                peso = pesoUsuario,
                diasEntrenamiento = diasEntrenamiento,
                asistenteService = asistenteService,
                onInicioClick = { currentScreen = "home" },
                onEntrenamientosClick = { currentScreen = "entrenamientos" },
                onDietaClick = { abrirDieta() },
                onPerfilClick = { currentScreen = "perfil" }
            )

            "perfil" -> PerfilScreen(
                nombre = nombreUsuario,
                sexo = sexoUsuario,
                peso = pesoUsuario,
                altura = alturaUsuario,
                fechaNacimiento = fechaNacimientoUsuario,
                objetivo = objetivoSeleccionado,
                diasEntrenamiento = diasEntrenamiento,
                grupoMuscularFoco = grupoMuscularFoco,
                onInicioClick = { currentScreen = "home" },
                onNotificacionesClick = { currentScreen = "notificaciones" },
                onCerrarSesionClick = {
                    scope.launch {
                        repository.cerrarSesion()
                        limpiarUsuarioEnMemoria()
                        currentScreen = "start"
                    }
                },
                onGuardarPerfil = { nuevoPeso, nuevaAltura, nuevoObjetivo, nuevosDias, nuevoFoco ->
                    pesoUsuario = nuevoPeso
                    alturaUsuario = nuevaAltura
                    objetivoSeleccionado = nuevoObjetivo
                    diasEntrenamiento = nuevosDias
                    grupoMuscularFoco = nuevoFoco
                    scope.launch {
                        repository.actualizarPerfil(nuevoPeso, nuevaAltura, nuevoObjetivo, nuevosDias)
                        repository.guardarGrupoMuscularFoco(nuevoFoco)
                        syncer.syncPerfil(emailUsuario, nombreUsuario, sexoUsuario, nuevoPeso,
                            nuevaAltura, fechaNacimientoUsuario, nuevoObjetivo, nuevosDias,
                            nuevoFoco)
                    }
                }
            )

            "notificaciones" -> NotificacionesConfigScreen(
                configActual = configNotificaciones,
                scheduler = scheduler,
                onGuardar = { config ->
                    configNotificaciones = config
                    scope.launch {
                        repository.guardarConfigNotificaciones(
                            notifEntrenamiento = config.notifEntrenamientoActiva,
                            horaEntrenamiento = config.horaEntrenamiento,
                            minutoEntrenamiento = config.minutoEntrenamiento,
                            notifDieta = config.notifDietaActiva,
                            horaDieta = config.horaDieta,
                            minutoDieta = config.minutoDieta,
                            notifProgreso = config.notifProgresoActiva,
                            diaProgresoSemana = config.diaProgresoSemana,
                            horaProgreso = config.horaProgreso,
                            minutoProgreso = config.minutoProgreso
                        )
                        syncer.syncConfigNotificaciones(
                            email = emailUsuario,
                            notifEntrenamiento = config.notifEntrenamientoActiva,
                            horaEntrenamiento = config.horaEntrenamiento,
                            minutoEntrenamiento = config.minutoEntrenamiento,
                            notifDieta = config.notifDietaActiva,
                            horaDieta = config.horaDieta,
                            minutoDieta = config.minutoDieta,
                            notifProgreso = config.notifProgresoActiva,
                            diaProgresoSemana = config.diaProgresoSemana,
                            horaProgreso = config.horaProgreso,
                            minutoProgreso = config.minutoProgreso
                        )
                    }
                },
                onVolver = { currentScreen = "perfil" }
            )
        }
        }
    }
}
