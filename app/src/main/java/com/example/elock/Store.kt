package com.example.elock
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.SystemClock
import android.provider.Settings
import java.util.Calendar

object Store {
    val presets = linkedMapOf(
        "Facebook" to "com.facebook.katana", "Instagram" to "com.instagram.android", "X" to "com.twitter.android",
        "YouTube" to "com.google.android.youtube", "Messenger" to "com.facebook.orca",
        "TikTok" to "com.zhiliaoapp.musically", "Snapchat" to "com.snapchat.android")
    const val DEFAULT_MIN = 60                       // default daily limit (no-hassle mode)
    private fun sp(c: Context) = c.getSharedPreferences("elock", 0)

    fun selected(c: Context): Set<String> = sp(c).getStringSet("sel", emptySet())!!
    fun setSelected(c: Context, s: Set<String>) = sp(c).edit().putStringSet("sel", s).apply()
    fun dailyMin(c: Context, pkg: String) = sp(c).getInt("d_$pkg", DEFAULT_MIN)
    fun setDailyMin(c: Context, pkg: String, m: Int) = sp(c).edit().putInt("d_$pkg", Logic.clampLimit(m)).apply()
    fun sleepOn(c: Context) = sp(c).getBoolean("sleep", false)
    fun setSleep(c: Context, v: Boolean) = sp(c).edit().putBoolean("sleep", v).apply()

    // ---- lock session: survives restart/reboot, resists clock changes ----
    private fun boot(c: Context) = Settings.Global.getInt(c.contentResolver, Settings.Global.BOOT_COUNT, -1)
    fun startLock(c: Context, ms: Long) = sp(c).edit()
        .putLong("endE", SystemClock.elapsedRealtime() + ms).putLong("endW", System.currentTimeMillis() + ms)
        .putInt("boot", boot(c)).putStringSet("lockPk", selected(c)).apply()
    fun remainingMs(c: Context): Long {
        val s = sp(c); if (s.getLong("endE", 0) == 0L) return 0
        return if (boot(c) == s.getInt("boot", -2)) Logic.remaining(s.getLong("endE", 0), SystemClock.elapsedRealtime())
               else Logic.remaining(s.getLong("endW", 0), System.currentTimeMillis())   // after reboot, wall clock
    }
    fun sessionActive(c: Context) = remainingMs(c) > 0

    // ---- daily limit + sleep ----
    fun usedMs(c: Context, pkg: String): Long {
        val m = c.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val st = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        return m.queryAndAggregateUsageStats(st, System.currentTimeMillis())[pkg]?.totalTimeInForeground ?: 0
    }
    fun limitHit(c: Context, pkg: String) = pkg in selected(c) && Logic.limitHit(usedMs(c, pkg), dailyMin(c, pkg))
    fun sleepNow(c: Context): Boolean = Logic.sleepNow(sleepOn(c), Calendar.getInstance().get(Calendar.HOUR_OF_DAY))

    /** Why is this app blocked right now? null = allowed. */
    fun blockReason(c: Context, pkg: String): String? {
        val sel = pkg in selected(c); if (!sel) return null
        val inSession = sessionActive(c) && pkg in (sp(c).getStringSet("lockPk", emptySet()) ?: emptySet())
        return Logic.reason(sel, inSession, sleepNow(c), !inSession && !sleepNow(c) && limitHit(c, pkg))
    }
    /** Settings are frozen while any lock is active. */
    fun frozen(c: Context) = Logic.frozen(sessionActive(c), sleepNow(c))
}
