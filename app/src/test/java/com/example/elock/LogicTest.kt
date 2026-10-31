package com.example.elock
import org.junit.Test
import kotlin.test.*

class LogicTest {
    @Test fun sleepWindowBoundaries() {
        assertTrue(Logic.isSleepHour(23)); assertTrue(Logic.isSleepHour(0)); assertTrue(Logic.isSleepHour(5))
        assertFalse(Logic.isSleepHour(6)); assertFalse(Logic.isSleepHour(12)); assertFalse(Logic.isSleepHour(22))
    }
    @Test fun sleepOffNeverLocks() = assertFalse(Logic.sleepNow(false, 2))
    @Test fun sleepOnLocksAtNight() = assertTrue(Logic.sleepNow(true, 2))
    @Test fun remainingCountsDownAndNeverNegative() {
        assertEquals(60_000L, Logic.remaining(100_000, 40_000))
        assertEquals(0L, Logic.remaining(100_000, 100_000)); assertEquals(0L, Logic.remaining(100_000, 999_999))
    }
    @Test fun dailyLimitHit() {
        assertFalse(Logic.limitHit(59 * 60_000L, 60)); assertTrue(Logic.limitHit(60 * 60_000L, 60)); assertTrue(Logic.limitHit(61 * 60_000L, 60))
    }
    @Test fun limitIsClamped() {
        assertEquals(5, Logic.clampLimit(-30)); assertEquals(5, Logic.clampLimit(0)); assertEquals(75, Logic.clampLimit(75)); assertEquals(1440, Logic.clampLimit(5000))
    }
    @Test fun unselectedAppIsNeverBlocked() = assertNull(Logic.reason(false, true, true, true))
    @Test fun reasonPriority() {
        assertEquals("Lock session active", Logic.reason(true, true, true, true))
        assertEquals("Sleep time (11 PM - 6 AM)", Logic.reason(true, false, true, true))
        assertEquals("Daily limit reached", Logic.reason(true, false, false, true))
        assertNull(Logic.reason(true, false, false, false))
    }
    @Test fun settingsFrozenWhileAnyLockActive() {
        assertTrue(Logic.frozen(true, false)); assertTrue(Logic.frozen(false, true)); assertFalse(Logic.frozen(false, false))
    }
}
