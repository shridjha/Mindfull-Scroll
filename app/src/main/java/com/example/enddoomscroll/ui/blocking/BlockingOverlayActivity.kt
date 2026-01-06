package com.example.enddoomscroll.ui.blocking

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.enddoomscroll.data.database.AppDatabase
import com.example.enddoomscroll.data.entity.AppUsage
import com.example.enddoomscroll.data.repository.AppUsageRepository
import com.example.enddoomscroll.ui.challenge.MathChallengeActivity
import com.example.enddoomscroll.ui.theme.EndDoomScrollTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class BlockingOverlayActivity : ComponentActivity() {
    
    private lateinit var repository: AppUsageRepository
    private var packageName: String? = null
    private var timeLimitMinutes: Int = 30
    
    private val challengeLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK && result.data?.getBooleanExtra("challenge_success", false) == true) {
            handleChallengeSuccess()
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        packageName = intent.getStringExtra("package_name")
        timeLimitMinutes = intent.getIntExtra("time_limit", 30)
        
        val database = AppDatabase.getDatabase(this)
        repository = AppUsageRepository(
            database.appUsageDao(),
            database.appSettingsDao(),
            database.challengeHistoryDao()
        )
        
        // Set fullscreen flags
        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN,
            android.view.WindowManager.LayoutParams.FLAG_FULLSCREEN
        )
        
        // Handle back button press
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Prevent back button from closing the overlay
                // User must acknowledge the block
            }
        })
        
        setContent {
            EndDoomScrollTheme {
                BlockingOverlayScreen(
                    packageName = packageName ?: "",
                    timeLimitMinutes = timeLimitMinutes,
                    onChallengeClick = {
                        launchChallenge()
                    },
                    onDismiss = {
                        finish()
                        // Move app to background
                        moveTaskToBack(true)
                    }
                )
            }
        }
    }
    
    private fun launchChallenge() {
        packageName?.let { pkg ->
            CoroutineScope(Dispatchers.IO).launch {
                val settings = repository.getSettingsForPackage(pkg)
                val rewardMinutes = settings?.challengeTimeRewardMinutes ?: 10
                
                runOnUiThread {
                    val intent = Intent(this@BlockingOverlayActivity, MathChallengeActivity::class.java).apply {
                        putExtra("package_name", pkg)
                        putExtra("time_reward_minutes", rewardMinutes)
                    }
                    challengeLauncher.launch(intent)
                }
            }
        } ?: run {
            val intent = Intent(this, MathChallengeActivity::class.java).apply {
                putExtra("package_name", packageName)
                putExtra("time_reward_minutes", 10)
            }
            challengeLauncher.launch(intent)
        }
    }
    
    private fun handleChallengeSuccess() {
        packageName?.let { pkg ->
            CoroutineScope(Dispatchers.IO).launch {
                val settings = repository.getSettingsForPackage(pkg)
                if (settings != null) {
                    val todayStart = repository.getTodayStartTimestamp()
                    val usage = repository.getUsageForDate(todayStart, pkg)
                    
                    if (usage != null) {
                        val rewardMs = (settings.challengeTimeRewardMinutes * 60 * 1000L)
                        val updatedUsage = usage.copy(
                            extraTimeEarned = usage.extraTimeEarned + rewardMs,
                            challengesCompleted = usage.challengesCompleted + 1
                        )
                        repository.insertOrUpdateUsage(updatedUsage)
                        
                        // Record challenge history
                        val challengeHistory = com.example.enddoomscroll.data.entity.ChallengeHistory(
                            packageName = pkg,
                            date = System.currentTimeMillis(),
                            difficulty = settings.mathChallengeDifficulty,
                            wasSuccessful = true,
                            timeRewarded = rewardMs,
                            challengeNumber = repository.getChallengeCountForDate(pkg, todayStart) + 1
                        )
                        repository.insertChallenge(challengeHistory)
                    }
                }
            }
        }
        
        Toast.makeText(this, "Challenge completed! You earned extra time.", Toast.LENGTH_SHORT).show()
        finish()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockingOverlayScreen(
    packageName: String,
    timeLimitMinutes: Int,
    onChallengeClick: () -> Unit,
    onDismiss: () -> Unit
) {
    var timeUntilReset by remember { mutableStateOf(getTimeUntilMidnight()) }
    
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(1000)
            timeUntilReset = getTimeUntilMidnight()
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.errorContainer
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Icon or emoji
            Text(
                text = "⏰",
                style = MaterialTheme.typography.displayLarge,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            Text(
                text = "Time Limit Reached",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "You've used $timeLimitMinutes minutes today.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onErrorContainer,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            
            Text(
                text = "Time resets in: ${formatTimeRemaining(timeUntilReset)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 48.dp)
            )
            
            // Challenge button
            Button(
                onClick = onChallengeClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Text("Solve Math Challenge for More Time", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Acknowledge button
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Text("I Understand", fontSize = 16.sp)
            }
        }
    }
}

private fun getTimeUntilMidnight(): Long {
    val calendar = Calendar.getInstance()
    val currentTime = calendar.timeInMillis
    
    calendar.add(Calendar.DAY_OF_MONTH, 1)
    calendar.set(Calendar.HOUR_OF_DAY, 0)
    calendar.set(Calendar.MINUTE, 0)
    calendar.set(Calendar.SECOND, 0)
    calendar.set(Calendar.MILLISECOND, 0)
    
    val midnight = calendar.timeInMillis
    return midnight - currentTime
}

private fun formatTimeRemaining(millis: Long): String {
    val hours = millis / (1000 * 60 * 60)
    val minutes = (millis % (1000 * 60 * 60)) / (1000 * 60)
    return "${hours}h ${minutes}m"
}

