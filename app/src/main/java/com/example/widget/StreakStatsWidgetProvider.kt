package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R

class StreakStatsWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val accentColor = WidgetThemeHelper.getAccentColorHex(context)
            val bgColor = WidgetThemeHelper.getBackgroundColor(context)
            val textColor = WidgetThemeHelper.getTextColor(context)
            val subtextColor = WidgetThemeHelper.getSubtextColor(context)

            val views = RemoteViews(context.packageName, R.layout.widget_streak_stats)
            views.setInt(R.id.widget_stats_root, "setBackgroundColor", bgColor)
            views.setTextColor(R.id.widget_stats_title, textColor)
            views.setTextColor(R.id.widget_stats_sub, subtextColor)
            views.setInt(R.id.widget_stats_btn_open, "setBackgroundColor", accentColor)

            views.setOnClickPendingIntent(R.id.widget_stats_root, pendingIntent)
            views.setOnClickPendingIntent(R.id.widget_stats_btn_open, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
