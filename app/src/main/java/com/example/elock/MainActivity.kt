package com.example.elock
import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    private fun usageOk(): Boolean = (getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager)
        .unsafeCheckOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), packageName) == AppOpsManager.MODE_ALLOWED
    private fun guardOk() = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)?.contains("$packageName/") == true

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        setContent { MaterialTheme { Screen() } }
    }

    @Composable fun Screen() {
        val c = this
        var tick by remember { mutableStateOf(0) }
        LaunchedEffect(Unit) { while (true) { delay(1000); tick++ } }
        val scroll = rememberScrollState()
        key(tick) {
            val frozen = Store.frozen(c)
            var sel by remember { mutableStateOf(Store.selected(c)) }
            Column(Modifier.fillMaxSize().verticalScroll(scroll).padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("E-Lock", style = MaterialTheme.typography.headlineMedium)
                if (!usageOk()) Button(onClick = { startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)) }) { Text("Step 1: Allow Usage Access") }
                if (!guardOk()) Button(onClick = { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) { Text("Step 2: Enable E-Lock Guard") }
                if (frozen) {
                    val r = Store.remainingMs(c)
                    Card { Text(if (r > 0) "🔒 Lock active: %02d:%02d:%02d left. Settings are frozen.".format(r / 3600000, r / 60000 % 60, r / 1000 % 60)
                        else "🌙 Sleep lock active. Settings are frozen.", Modifier.padding(12.dp)) }
                }
                Text("Apps to lock (default limit: ${Store.DEFAULT_MIN} min/day)", style = MaterialTheme.typography.titleMedium)
                Store.presets.forEach { (name, pkg) ->
                    val hit = Store.limitHit(c, pkg)
                    val editable = !frozen && !hit
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(pkg in sel, enabled = editable, onCheckedChange = { on -> sel = if (on) sel + pkg else sel - pkg; Store.setSelected(c, sel) })
                        Column(Modifier.weight(1f)) {
                            Text(name)
                            if (pkg in sel) Text("Used ${Store.usedMs(c, pkg) / 60000} / ${Store.dailyMin(c, pkg)} min today" + if (hit) " (locked)" else "", style = MaterialTheme.typography.bodySmall)
                        }
                        if (pkg in sel) {
                            TextButton(enabled = editable, onClick = { Store.setDailyMin(c, pkg, Store.dailyMin(c, pkg) - 15) }) { Text("-15") }
                            TextButton(enabled = editable, onClick = { Store.setDailyMin(c, pkg, Store.dailyMin(c, pkg) + 15) }) { Text("+15") }
                            TextButton(enabled = editable, onClick = { Store.setDailyMin(c, pkg, Store.dailyMin(c, pkg) + 60) }) { Text("+1h") }
                        }
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Sleep lock 11 PM - 6 AM", Modifier.weight(1f))
                    Switch(Store.sleepOn(c), enabled = !frozen, onCheckedChange = { Store.setSleep(c, it) })
                }
                Text("Start a lock session now", style = MaterialTheme.typography.titleMedium)
                Text("Selected apps will be blocked until the time ends. It cannot be undone.", style = MaterialTheme.typography.bodySmall)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(30 to "30m", 60 to "1h", 120 to "2h", 240 to "4h").forEach { (m, l) ->
                        Button(enabled = !frozen && sel.isNotEmpty(), onClick = { Store.startLock(c, m * 60_000L) }) { Text(l) }
                    }
                }
            }
        }
    }
}
