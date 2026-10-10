package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** The last finished game and the best score, saved on this phone when a game ends. */
object KbcWidgetStore {
    private const val PREFS = "kbc_widget_snapshot"

    fun save(context: Context, points: Long, questionReached: Int, correct: Int, outcome: String, atMs: Long, bestPoints: Long) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putLong("points", points).putInt("q", questionReached).putInt("correct", correct)
            .putString("outcome", outcome).putLong("at", atMs).putLong("best", bestPoints).apply()
    }

    /** Plain words for the end reason; an unknown reason is shown as written, never guessed. */
    fun outcomeLabel(raw: String): String = when (raw.uppercase()) {
        "CLEARED_7_CRORE" -> "Cleared all questions"
        "LOCKED_CHECKPOINT" -> "Stopped at a checkpoint"
        "QUIT" -> "Quit"
        "TIME_OUT", "TIMEOUT" -> "Time ran out"
        "WRONG_ANSWER" -> "Wrong answer"
        "" -> "Unavailable"
        else -> raw
    }
}

/**
 * KBC widget: last game score and best score, from what the app saved when a game ended. No game yet shows
 * "Unavailable". No timer and no background work: the app redraws it when a game ends.
 */
class KbcWidgetProvider : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (id in appWidgetIds) draw(context, appWidgetManager, id)
    }

    companion object {
        fun refresh(context: Context) {
            val mgr = AppWidgetManager.getInstance(context) ?: return
            for (id in mgr.getAppWidgetIds(ComponentName(context, KbcWidgetProvider::class.java))) draw(context, mgr, id)
        }

        private fun draw(context: Context, mgr: AppWidgetManager, id: Int) {
            val p = context.getSharedPreferences("kbc_widget_snapshot", Context.MODE_PRIVATE)
            val v = RemoteViews(context.packageName, R.layout.widget_kbc)
            if (!p.contains("at")) {
                v.setTextViewText(R.id.kbc_last, "Last game: Unavailable")
                v.setTextViewText(R.id.kbc_best, "Best score: Unavailable")
                v.setTextViewText(R.id.kbc_detail, "No finished game yet. Tap to play.")
            } else {
                v.setTextViewText(R.id.kbc_last, "Last game: ${p.getLong("points", 0L)} points")
                v.setTextViewText(R.id.kbc_best, "Best score: ${p.getLong("best", 0L)} points")
                val at = SimpleDateFormat("d MMM HH:mm", Locale.getDefault()).format(Date(p.getLong("at", 0L)))
                v.setTextViewText(
                    R.id.kbc_detail,
                    "Reached question ${p.getInt("q", 0)}, ${p.getInt("correct", 0)} correct. " +
                        KbcWidgetStore.outcomeLabel(p.getString("outcome", "") ?: "") + ". " + at
                )
            }
            val intent = com.example.Brand.launchIntent(context).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP }
            v.setOnClickPendingIntent(R.id.kbc_root, PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE))
            mgr.updateAppWidget(id, v)
        }
    }
}
