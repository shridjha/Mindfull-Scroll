
package com.example.enddoomscroll.ui.challenge

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
//import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.enddoomscroll.ui.theme.EndDoomScrollTheme
import com.example.enddoomscroll.util.MathChallenge
import com.example.enddoomscroll.util.MathChallengeGenerator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MathChallengeActivity : ComponentActivity() {

    private var packageName: String? = null
    private var timeRewardMinutes: Int = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        packageName = intent.getStringExtra("package_name")
        timeRewardMinutes = intent.getIntExtra("time_reward_minutes", 10)

        setContent {
            EndDoomScrollTheme {
                MathChallengeScreen(
                    timeRewardMinutes = timeRewardMinutes,
                    onChallengeComplete = { success ->
                        if (success) {
                            setResult(
                                RESULT_OK,
                                Intent().putExtra("challenge_success", true)
                            )
                            finish()
                        } else {
                            Toast.makeText(
                                this,
                                "Incorrect answer. Try again!",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    onDismiss = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MathChallengeScreen(
    timeRewardMinutes: Int,
    onChallengeComplete: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    viewModel: MathChallengeViewModel = viewModel()
) {
    val challenge by viewModel.challenge.collectAsState()
    val timeRemaining by viewModel.timeRemaining.collectAsState()
    val userAnswer by viewModel.userAnswer.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.startChallenge()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Math Challenge",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Solve this to earn $timeRewardMinutes more minutes",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "${timeRemaining}s",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = if (timeRemaining <= 10)
                    MaterialTheme.colorScheme.error
                else
                    MaterialTheme.colorScheme.primary
            )

            challenge?.let { challenge ->
                Spacer(modifier = Modifier.height(24.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(4.dp)
                ) {
                    Text(
                        text = challenge.problem,
                        modifier = Modifier.padding(32.dp),
                        style = MaterialTheme.typography.displayMedium,
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold
                    )
                }


//                    keyboardOptions = KeyboardOptions(
//                        keyboardType = KeyboardType.Number
//                    )
                    OutlinedTextField(
                        value = userAnswer,
                        onValueChange = { viewModel.updateAnswer(it) },
                        label = { Text("Your answer") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        singleLine = true
                    )



                Button(
                    onClick = {
                        viewModel.submitAnswer { success ->
                            onChallengeComplete(success)
                        }
                    },
                    enabled = userAnswer.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text("Submit Answer", fontSize = 18.sp)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    }

    LaunchedEffect(timeRemaining) {
        if (timeRemaining <= 0 && challenge != null) {
            onChallengeComplete(false)
        }
    }
}

class MathChallengeViewModel(
    private val challengeGenerator: MathChallengeGenerator = MathChallengeGenerator(),
    private val difficulty: String = "Medium"
) : ViewModel() {

    private val _challenge = MutableStateFlow<MathChallenge?>(null)
    val challenge: StateFlow<MathChallenge?> = _challenge

    private val _timeRemaining = MutableStateFlow(30)
    val timeRemaining: StateFlow<Int> = _timeRemaining

    private val _userAnswer = MutableStateFlow("")
    val userAnswer: StateFlow<String> = _userAnswer

    private var timer: CountDownTimer? = null

    fun startChallenge() {
        _challenge.value = challengeGenerator.generateChallenge(difficulty)
        _userAnswer.value = ""

        timer = object : CountDownTimer(30_000, 1_000) {
            override fun onTick(millisUntilFinished: Long) {
                _timeRemaining.value = (millisUntilFinished / 1000).toInt()
            }

            override fun onFinish() {
                _timeRemaining.value = 0
            }
        }.start()
    }

    fun updateAnswer(answer: String) {
        _userAnswer.value = answer.filter { it.isDigit() || it == '-' }
    }

    fun submitAnswer(onComplete: (Boolean) -> Unit) {
        timer?.cancel()
        val userAnswerInt = _userAnswer.value.toIntOrNull()
        val correct = userAnswerInt == _challenge.value?.answer
        onComplete(correct)
    }

    override fun onCleared() {
        timer?.cancel()
    }
}
