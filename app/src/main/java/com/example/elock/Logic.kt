package com.example.elock
/** Pure rules (no Android classes) so they can be unit-tested. */
object Logic {
    fun isSleepHour(h: Int) = h >= 23 || h < 6
    fun sleepNow(on: Boolean, hour: Int) = on && isSleepHour(hour)
    fun remaining(endMs: Long, nowMs: Long) = maxOf(0L, endMs - nowMs)
    fun limitHit(usedMs: Long, limitMin: Int) = usedMs >= limitMin * 60_000L
    fun clampLimit(m: Int) = m.coerceIn(5, 1440)
    fun frozen(session: Boolean, sleep: Boolean) = session || sleep
    fun reason(selected: Boolean, inSession: Boolean, sleep: Boolean, limit: Boolean): String? = when {
        !selected -> null
        inSession -> "Lock session active"
        sleep -> "Sleep time (11 PM - 6 AM)"
        limit -> "Daily limit reached"
        else -> null
    }
}
