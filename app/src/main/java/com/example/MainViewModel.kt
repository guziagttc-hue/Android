package com.example

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TypingState(
    val quote: String = "The quick brown fox jumps over the lazy dog.",
    val userInput: String = "",
    val timeLeft: Int = 60,
    val wpm: Int = 0,
    val accuracy: Int = 100,
    val isRunning: Boolean = false,
    val isFinished: Boolean = false
)

class TypingViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(TypingState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private val paragraphs = listOf(
        "The quick brown fox jumps over the lazy dog.",
        "Success is not final, failure is not fatal: it is the courage to continue that counts.",
        "Technology is best when it brings people together.",
        "Practice makes perfect. Typing every day will improve your speed and accuracy drastically."
    )

    fun onTextChange(input: String) {
        if (!_uiState.value.isRunning && !_uiState.value.isFinished) {
            startTimer()
        }

        val currentState = _uiState.value
        val quote = currentState.quote
        
        var errors = 0
        for (i in input.indices) {
            if (i < quote.length && input[i] != quote[i]) {
                errors++
            }
        }

        val accuracy = if (input.isEmpty()) 100 else 
            ((input.length - errors).coerceAtLeast(0) * 100 / input.length)

        _uiState.update { it.copy(userInput = input, accuracy = accuracy) }

        if (input.length == quote.length) {
            loadNewText()
        }
    }

    private fun startTimer() {
        _uiState.update { it.copy(isRunning = true) }
        timerJob = viewModelScope.launch {
            while (_uiState.value.timeLeft > 0) {
                delay(1000)
                _uiState.update { it.copy(timeLeft = it.timeLeft - 1) }
                calculateWpm()
            }
            _uiState.update { it.copy(isRunning = false, isFinished = true) }
        }
    }

    private fun calculateWpm() {
        val timeElapsed = 60 - _uiState.value.timeLeft
        if (timeElapsed == 0) return
        val wpm = ((_uiState.value.userInput.length / 5) * 60) / timeElapsed
        _uiState.update { it.copy(wpm = wpm.coerceAtLeast(0)) }
    }

    private fun loadNewText() {
        val newQuote = paragraphs.random()
        _uiState.update { it.copy(quote = newQuote, userInput = "") }
    }

    fun resetGame() {
        timerJob?.cancel()
        _uiState.update { TypingState(quote = paragraphs.random()) }
    }
}
