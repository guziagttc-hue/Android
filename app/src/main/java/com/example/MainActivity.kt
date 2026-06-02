package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    private val viewModel: TypingViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val navController = rememberNavController()
                val state by viewModel.uiState.collectAsState()

                NavHost(navController = navController, startDestination = "home") {
                    composable("home") {
                        HomeScreen(
                            onStartTest = {
                                viewModel.resetGame()
                                navController.navigate("test")
                            }
                        )
                    }
                    composable("test") {
                        TypingTestScreen(
                            viewModel = viewModel,
                            state = state,
                            onFinish = { navController.navigate("result") }
                        )
                    }
                    composable("result") {
                        ResultsScreen(
                            state = state,
                            onGoHome = { navController.navigate("home") { popUpTo("home") { inclusive = true } } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun HomeScreen(onStartTest: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Typing Master", style = MaterialTheme.typography.headlineLarge)
        Button(onClick = onStartTest, modifier = Modifier.padding(top = 32.dp)) {
            Text("Start Test")
        }
    }
}

@Composable
fun TypingTestScreen(viewModel: TypingViewModel, state: TypingState, onFinish: () -> Unit) {
    LaunchedEffect(state.isFinished) {
        if (state.isFinished) onFinish()
    }
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Typing Test", style = MaterialTheme.typography.headlineMedium)
        
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Text("Time: ${state.timeLeft}s")
            Text("WPM: ${state.wpm}")
            Text("Acc: ${state.accuracy}%")
        }

        Text(
            text = buildAnnotatedString {
                state.quote.forEachIndexed { index, char ->
                    val color = when {
                        index >= state.userInput.length -> Color.Unspecified
                        state.userInput[index] == char -> Color(0xFF2ECC71)
                        else -> Color(0xFFE74C3C)
                    }
                    withStyle(SpanStyle(color = color)) { append(char) }
                }
            },
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(16.dp)
        )

        OutlinedTextField(
            value = state.userInput,
            onValueChange = { viewModel.onTextChange(it) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Start typing here...") },
            enabled = !state.isFinished
        )
    }
}

@Composable
fun ResultsScreen(state: TypingState, onGoHome: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Results", style = MaterialTheme.typography.headlineLarge)
        Text("WPM: ${state.wpm}", style = MaterialTheme.typography.headlineMedium)
        Text("Accuracy: ${state.accuracy}%", style = MaterialTheme.typography.headlineMedium)
        Button(onClick = onGoHome, modifier = Modifier.padding(top = 32.dp)) {
            Text("Go Home")
        }
    }
}
