package com.example.audioverifier

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle

class MainActivity : ComponentActivity() {
    private val viewModel: SpeechRecognitionViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(viewModel)
                }
            }
        }
    }
}

@Composable
fun MainScreen(viewModel: SpeechRecognitionViewModel) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showTextContent by remember { mutableStateOf(false) }
    
    val textFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadTextFile(LocalContext.current, it) }
    }

    val audioFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.loadAudioFile(LocalContext.current, it) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Botón para cargar archivo de texto
        Button(
            onClick = { textFilePicker.launch("text/plain") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cargar archivo TXT")
        }

        // Mostrar contenido del archivo de texto
        if (uiState.textContent.isNotEmpty()) {
            Button(
                onClick = { showTextContent = !showTextContent },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (showTextContent) "Ocultar contenido" else "Mostrar contenido")
            }

            if (showTextContent) {
                Text(
                    text = uiState.textContent,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                )
            }
        }

        // Botón para cargar archivo de audio
        Button(
            onClick = { audioFilePicker.launch("audio/*") },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Cargar archivo de audio")
        }

        // Mostrar texto reconocido del audio
        if (uiState.audioText.isNotEmpty()) {
            Text(
                text = "Texto reconocido del audio:",
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = uiState.audioText,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            )
        }

        // Mostrar similitud
        if (uiState.similarity > 0) {
            Text(
                text = "Similitud: ${uiState.similarity}%",
                fontSize = 24.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Mostrar errores
        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(8.dp)
            )
        }

        // Indicador de carga
        if (uiState.isLoading) {
            CircularProgressIndicator()
        }
    }
} 