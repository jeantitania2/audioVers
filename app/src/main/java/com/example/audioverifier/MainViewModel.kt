package com.example.audioverifier

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.nl.languagedetection.LanguageDetection
import com.google.mlkit.nl.languagedetection.LanguageDetector
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.math.roundToInt

data class UiState(
    val textContent: String = "",
    val audioText: String = "",
    val similarity: Int = 0,
    val isLoading: Boolean = false,
    val error: String? = null
)

class MainViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    private val languageDetector: LanguageDetector = LanguageDetection.getClient()

    fun loadTextFile(context: Context, uri: Uri) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true)
                val content = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                    BufferedReader(InputStreamReader(inputStream)).readText()
                } ?: throw Exception("No se pudo leer el archivo")
                
                _uiState.value = _uiState.value.copy(
                    textContent = content,
                    isLoading = false,
                    error = null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Error al cargar el archivo: ${e.message}"
                )
            }
        }
    }

    fun setAudioText(text: String) {
        _uiState.value = _uiState.value.copy(audioText = text)
        calculateSimilarity()
    }

    private fun calculateSimilarity() {
        val text1 = _uiState.value.textContent.lowercase()
        val text2 = _uiState.value.audioText.lowercase()
        
        if (text1.isEmpty() || text2.isEmpty()) {
            _uiState.value = _uiState.value.copy(similarity = 0)
            return
        }

        val words1 = text1.split("\\s+".toRegex()).toSet()
        val words2 = text2.split("\\s+".toRegex()).toSet()
        
        val intersection = words1.intersect(words2).size
        val union = words1.union(words2).size
        
        val similarity = if (union > 0) {
            ((intersection.toFloat() / union.toFloat()) * 100).roundToInt()
        } else {
            0
        }
        
        _uiState.value = _uiState.value.copy(similarity = similarity)
    }
} 