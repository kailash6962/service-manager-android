package com.example.servicemanager.widgets

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import androidx.core.content.ContextCompat
import androidx.room.Room
import com.example.servicemanager.MainActivity
import com.example.servicemanager.R
import com.example.servicemanager.core.data.AppDatabase
import com.example.servicemanager.core.domain.ServiceStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PendingServiceWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray,
    ) {
        val pendingResult: BroadcastReceiver.PendingResult? = goAsync()
        updateWidgetsAsync(context, appWidgetManager, appWidgetIds, pendingResult)
    }

    override fun onEnabled(context: Context) {
        updateAllWidgets(context)
    }

    companion object {
        fun updateAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, PendingServiceWidgetProvider::class.java),
            )
            if (ids.isNotEmpty()) {
                updateWidgetsAsync(context, manager, ids, pendingResult = null)
            }
        }

        private fun updateWidgetsAsync(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray,
            pendingResult: BroadcastReceiver.PendingResult?,
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val rows = loadPendingServiceRows(context.applicationContext)
                    appWidgetIds.forEach { appWidgetId ->
                        val views = buildRemoteViews(context, rows)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } finally {
                    pendingResult?.finish()
                }
            }
        }

        private suspend fun loadPendingServiceRows(context: Context): List<WidgetRow> {
            val summaries = WidgetDatabaseHolder.get(context)
                .serviceManagerDao()
                .observeServiceSummaries()
                .first()
            return summaries
                .asSequence()
                .filter { it.status != ServiceStatus.COMPLETED && it.status != ServiceStatus.CANCELLED }
                .take(MAX_ROWS)
                .map { pending ->
                    val brand = pending.brand.takeIf { it.isNotBlank() }
                    val model = pending.model.takeIf { it.isNotBlank() } ?: context.getString(R.string.widget_unknown_model)
                    val deviceLabel = listOfNotNull(brand, model).joinToString(" ")
                    WidgetRow(
                        title = deviceLabel,
                        status = pending.status.toWidgetStatusLabel(),
                        tone = pending.status.toWidgetStatusTone(),
                    )
                }
                .toList()
        }

        private fun buildRemoteViews(context: Context, rows: List<WidgetRow>): RemoteViews {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            return RemoteViews(context.packageName, R.layout.widget_pending_service).apply {
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                val rowIds = intArrayOf(R.id.row_1, R.id.row_2, R.id.row_3, R.id.row_4)
                val titleIds = intArrayOf(R.id.row_title_1, R.id.row_title_2, R.id.row_title_3, R.id.row_title_4)
                val statusIds = intArrayOf(R.id.row_status_1, R.id.row_status_2, R.id.row_status_3, R.id.row_status_4)
                val checkIds = intArrayOf(R.id.row_check_1, R.id.row_check_2, R.id.row_check_3, R.id.row_check_4)
                if (rows.isEmpty()) {
                    setViewVisibility(R.id.empty_text, View.VISIBLE)
                    rowIds.forEach { setViewVisibility(it, View.GONE) }
                    titleIds.forEach { setViewVisibility(it, View.GONE) }
                    statusIds.forEach { setViewVisibility(it, View.GONE) }
                    checkIds.forEach { setViewVisibility(it, View.GONE) }
                } else {
                    setViewVisibility(R.id.empty_text, View.GONE)
                    rowIds.forEachIndexed { index, viewId ->
                        setViewVisibility(viewId, if (index < rows.size) View.VISIBLE else View.GONE)
                    }
                    titleIds.forEachIndexed { index, viewId ->
                        if (index < rows.size) {
                            setViewVisibility(viewId, View.VISIBLE)
                            setTextViewText(viewId, rows[index].title)
                        } else {
                            setViewVisibility(viewId, View.GONE)
                        }
                    }
                    statusIds.forEachIndexed { index, viewId ->
                        if (index < rows.size) {
                            setViewVisibility(viewId, View.VISIBLE)
                            setTextViewText(viewId, rows[index].status)
                            val tone = rows[index].tone
                            val background = if (tone == WidgetStatusTone.PRIMARY) {
                                R.drawable.widget_status_primary_bg
                            } else {
                                R.drawable.widget_status_muted_bg
                            }
                            val textColor = if (tone == WidgetStatusTone.PRIMARY) {
                                R.color.widget_status_primary_text
                            } else {
                                R.color.widget_status_muted_text
                            }
                            setInt(viewId, "setBackgroundResource", background)
                            setTextColor(viewId, ContextCompat.getColor(context, textColor))
                        } else {
                            setViewVisibility(viewId, View.GONE)
                        }
                    }
                    checkIds.forEachIndexed { index, viewId ->
                        if (index < rows.size) {
                            setViewVisibility(viewId, View.VISIBLE)
                            setImageViewResource(viewId, R.drawable.widget_checkbox_empty)
                        } else {
                            setViewVisibility(viewId, View.GONE)
                        }
                    }
                }
            }
        }

        private const val MAX_ROWS = 4
    }
}

private data class WidgetRow(
    val title: String,
    val status: String,
    val tone: WidgetStatusTone,
)

private enum class WidgetStatusTone {
    PRIMARY,
    MUTED,
}

private object WidgetDatabaseHolder {
    @Volatile
    private var database: AppDatabase? = null

    fun get(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            database ?: Room.databaseBuilder(
                context.applicationContext,
                AppDatabase::class.java,
                "sentinel_manager.db",
            ).build().also { database = it }
        }
    }
}

private fun ServiceStatus.toWidgetStatusLabel(): String =
    when (this) {
        ServiceStatus.QUEUED -> "To-Do"
        ServiceStatus.IN_PROGRESS, ServiceStatus.DIAGNOSTICS, ServiceStatus.WAITING_FOR_SPARE -> "In Progress"
        ServiceStatus.READY_FOR_PICKUP -> "Ready"
        else -> name
            .lowercase()
            .split("_")
            .joinToString(" ") { part ->
                part.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
            }
    }

private fun ServiceStatus.toWidgetStatusTone(): WidgetStatusTone =
    when (this) {
        ServiceStatus.QUEUED -> WidgetStatusTone.MUTED
        else -> WidgetStatusTone.PRIMARY
    }
