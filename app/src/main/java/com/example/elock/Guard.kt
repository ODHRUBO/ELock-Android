package com.example.elock
import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.accessibility.AccessibilityEvent

class Guard : AccessibilityService() {
    private val h = Handler(Looper.getMainLooper())
    private var cur: String? = null
    private val poll = object : Runnable { override fun run() { cur?.let { check(it) }; h.postDelayed(this, 5000) } }

    private fun check(pkg: String) {
        val why = Store.blockReason(this, pkg) ?: return
        cur = null
        performGlobalAction(GLOBAL_ACTION_HOME)
        startActivity(Intent(this, LockActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
            .putExtra("why", why).putExtra("pkg", pkg))
    }
    override fun onServiceConnected() { h.post(poll) }
    override fun onAccessibilityEvent(e: AccessibilityEvent) {
        if (e.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return
        val p = e.packageName?.toString() ?: return
        if (p == "com.android.systemui" || p.contains("inputmethod")) return   // keyboard / shade are not "the app"
        cur = p; check(p)
    }
    override fun onInterrupt() {}
    override fun onDestroy() { h.removeCallbacksAndMessages(null); super.onDestroy() }
}
