package com.example.elock
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class LockActivity : ComponentActivity() {
    private fun home() { startActivity(Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)); finish() }
    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        val why = intent.getStringExtra("why") ?: "Locked"
        setContent { MaterialTheme { Column(Modifier.fillMaxSize().padding(32.dp), Arrangement.Center, Alignment.CenterHorizontally) {
            var left by remember { mutableStateOf(Store.remainingMs(this@LockActivity)) }
            LaunchedEffect(Unit) { while (true) { left = Store.remainingMs(this@LockActivity); delay(1000) } }
            Text("🔒 Locked", style = MaterialTheme.typography.headlineLarge)
            Spacer(Modifier.height(12.dp)); Text(why)
            if (left > 0) Text("Time left: %02d:%02d:%02d".format(left / 3600000, left / 60000 % 60, left / 1000 % 60))
            Spacer(Modifier.height(8.dp)); Text("Take a breath. Do something else for a while.")
            Spacer(Modifier.height(24.dp)); Button(onClick = { home() }) { Text("Go home") }
        } } }
    }
    @Deprecated("") override fun onBackPressed() = home()
}
