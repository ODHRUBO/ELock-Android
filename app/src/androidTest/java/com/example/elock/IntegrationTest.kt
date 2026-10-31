package com.example.elock
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Before
import org.junit.Test
import kotlin.test.*

class IntegrationTest {
    private lateinit var c: Context
    private val ig = "com.instagram.android"; private val yt = "com.google.android.youtube"
    @Before fun reset() { c = InstrumentationRegistry.getInstrumentation().targetContext; c.getSharedPreferences("elock", 0).edit().clear().commit() }

    @Test fun defaultLimitIs60() = assertEquals(60, Store.dailyMin(c, ig))
    @Test fun settingsPersist() {
        Store.setSelected(c, setOf(ig)); Store.setDailyMin(c, ig, 30); Store.setSleep(c, true)
        assertEquals(setOf(ig), Store.selected(c)); assertEquals(30, Store.dailyMin(c, ig)); assertTrue(Store.sleepOn(c))
    }
    @Test fun lockSessionBlocksOnlySelectedApps() {
        Store.setSelected(c, setOf(ig)); Store.startLock(c, 60_000)
        assertTrue(Store.sessionActive(c)); assertTrue(Store.remainingMs(c) in 1..60_000)
        assertEquals("Lock session active", Store.blockReason(c, ig)); assertNull(Store.blockReason(c, yt))
        assertTrue(Store.frozen(c))
    }
    @Test fun noLockMeansNotBlockedAndNotFrozen() {
        Store.setSelected(c, setOf(ig)); assertFalse(Store.sessionActive(c)); assertFalse(Store.limitHit(c, ig)); assertNull(Store.blockReason(c, ig))
    }
    @Test fun lockAppsAreSnapshottedAtStart() {
        Store.setSelected(c, setOf(ig)); Store.startLock(c, 60_000); Store.setSelected(c, setOf(ig, yt))
        assertNull(Store.blockReason(c, yt))   // yt was added after the lock started
    }
    @Test fun lockScreenLaunches() {
        ActivityScenario.launch(LockActivity::class.java).use { assertEquals(Lifecycle.State.RESUMED, it.state) }
    }
    @Test fun mainScreenLaunches() {
        ActivityScenario.launch(MainActivity::class.java).use { assertEquals(Lifecycle.State.RESUMED, it.state) }
    }
}
