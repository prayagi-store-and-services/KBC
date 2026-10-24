package com.example.util

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager

/**
 * Battery and temperature check before a game. Reads this phone's own battery data (the same data every Netra app sees on
 * the same phone). No permission, no network, nothing stored. If the temperature is unknown the game is never blocked.
 * Checking with the other Netra apps is not part of this version.
 */
object PhoneHealth {
    const val HOT_C = 42f
    const val LOW_PCT = 15

    data class Reading(val pct: Int?, val tempC: Float?, val charging: Boolean)
    enum class Verdict { OK, HOT, LOW_BATTERY }

    fun decide(r: Reading): Verdict = when {
        r.tempC != null && r.tempC >= HOT_C -> Verdict.HOT
        r.pct != null && r.pct < LOW_PCT && !r.charging -> Verdict.LOW_BATTERY
        else -> Verdict.OK
    }

    /** From the sticky battery intent. Temperature arrives in tenths of a degree; 0 or missing = unknown. */
    fun fromIntent(i: Intent?): Reading {
        if (i == null) return Reading(null, null, false)
        val level = i.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = i.getIntExtra(BatteryManager.EXTRA_SCALE, 100)
        val pct = if (level >= 0 && scale > 0) level * 100 / scale else null
        val t = i.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE)
        val temp = if (t == Int.MIN_VALUE || t == 0) null else t / 10f
        val st = i.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
        return Reading(pct, temp, st == BatteryManager.BATTERY_STATUS_CHARGING || st == BatteryManager.BATTERY_STATUS_FULL)
    }

    fun read(c: Context): Reading = fromIntent(c.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED)))

    fun describe(r: Reading): String =
        "Battery " + (r.pct?.let { "$it%" } ?: "unavailable") + (if (r.charging) " (charging)" else "") +
            ", temperature " + (r.tempC?.let { "%.1f C".format(it) } ?: "unavailable")

    fun hotMessage(r: Reading): String =
        "Phone is hot (" + "%.1f".format(r.tempC ?: 0f) + " C, limit " + HOT_C.toInt() + " C). Let it cool down for a few minutes, then start the game again."

    /** What the TEST button shows. Clearly labelled; it never starts or blocks a game. */
    fun testText(r: Reading): String = "TEST (no game started): " + describe(r) + ". Verdict: " + when (decide(r)) {
        Verdict.OK -> "a game would start."
        Verdict.HOT -> "a game would be held until the phone cools below " + HOT_C.toInt() + " C."
        Verdict.LOW_BATTERY -> "a game would start; battery is low, consider charging."
    }
}
