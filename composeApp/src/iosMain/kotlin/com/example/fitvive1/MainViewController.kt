package com.example.fitvive1

import androidx.compose.ui.window.ComposeUIViewController
import com.example.fitvive1.database.DatabaseDriverFactory
import com.example.fitvive1.database.UsuarioRepository

private val repository = UsuarioRepository(DatabaseDriverFactory())

fun MainViewController() = ComposeUIViewController {
    App(repository)
}
