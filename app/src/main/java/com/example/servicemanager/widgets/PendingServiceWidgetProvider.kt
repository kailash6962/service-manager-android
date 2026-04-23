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
import com.example.servicemanager.features.Routes
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
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
                    val data = loadWidgetData(context.applicationContext)
                    appWidgetIds.forEach { appWidgetId ->
                        val views = buildRemoteViews(context, data)
                        appWidgetManager.updateAppWidget(appWidgetId, views)
                    }
                } finally {
                    pendingResult?.finish()
                }
            }
        }

        private suspend fun loadWidgetData(context: Context): WidgetData {
            val dao = WidgetDatabaseHolder.get(context).serviceManagerDao()
            val serviceRows = dao.observeServiceSummaries()
                .first()
                .asSequence()
                .filter { it.status != ServiceStatus.COMPLETED && it.status != ServiceStatus.CANCELLED }
                .take(MAX_SERVICE_ROWS)
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

            val spareRows = dao.observeSpareRequirementsForStatus(ServiceStatus.WAITING_FOR_SPARE)
                .first()
                .asSequence()
                .take(MAX_SPARE_ROWS)
                .map { requirement ->
                    val qty = requirement.inventoryLevel.toIntOrNull()?.coerceAtLeast(1) ?: 1
                    WidgetRow(
                        title = requirement.name,
                        status = "QTY $qty",
                        tone = WidgetStatusTone.PRIMARY,
                    )
                }
                .toList()

            return WidgetData(
                serviceRows = serviceRows,
                spareRows = spareRows,
            )
        }

        private fun buildRemoteViews(context: Context, data: WidgetData): RemoteViews {
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            val addIntent = Intent(context, MainActivity::class.java).apply {
                putExtra(MainActivity.EXTRA_START_ROUTE, Routes.AddService)
            }
            val addPendingIntent = PendingIntent.getActivity(
                context,
                1,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
            )
            return RemoteViews(context.packageName, R.layout.widget_pending_service).apply {
                setOnClickPendingIntent(R.id.widget_root, pendingIntent)
                setOnClickPendingIntent(R.id.widget_add, addPendingIntent)

                val hasAnyRows = data.serviceRows.isNotEmpty() || data.spareRows.isNotEmpty()
                setViewVisibility(R.id.empty_text, if (hasAnyRows) View.GONE else View.VISIBLE)
                setViewVisibility(R.id.service_heading, View.VISIBLE)
                setViewVisibility(R.id.spare_purchase_heading, View.VISIBLE)

                bindRows(
                    context = context,
                    views = this,
                    rows = data.serviceRows,
                    rowIds = intArrayOf(R.id.service_row_1, R.id.service_row_2, R.id.service_row_3, R.id.service_row_4),
                    titleIds = intArrayOf(R.id.service_row_title_1, R.id.service_row_title_2, R.id.service_row_title_3, R.id.service_row_title_4),
                    statusIds = intArrayOf(R.id.service_row_status_1, R.id.service_row_status_2, R.id.service_row_status_3, R.id.service_row_status_4),
                    checkIds = intArrayOf(R.id.service_row_check_1, R.id.service_row_check_2, R.id.service_row_check_3, R.id.service_row_check_4),
                )
                bindRows(
                    context = context,
                    views = this,
                    rows = data.spareRows,
                    rowIds = intArrayOf(R.id.spare_row_1, R.id.spare_row_2, R.id.spare_row_3, R.id.spare_row_4),
                    titleIds = intArrayOf(R.id.spare_row_title_1, R.id.spare_row_title_2, R.id.spare_row_title_3, R.id.spare_row_title_4),
                    statusIds = intArrayOf(R.id.spare_row_status_1, R.id.spare_row_status_2, R.id.spare_row_status_3, R.id.spare_row_status_4),
                    checkIds = intArrayOf(R.id.spare_row_check_1, R.id.spare_row_check_2, R.id.spare_row_check_3, R.id.spare_row_check_4),
                )
            }
        }

        private fun bindRows(
            context: Context,
            views: RemoteViews,
            rows: List<WidgetRow>,
            rowIds: IntArray,
            titleIds: IntArray,
            statusIds: IntArray,
            checkIds: IntArray,
        ) {
            rowIds.forEachIndexed { index, viewId ->
                views.setViewVisibility(viewId, if (index < rows.size) View.VISIBLE else View.GONE)
            }
            titleIds.forEachIndexed { index, viewId ->
                if (index < rows.size) {
                    views.setViewVisibility(viewId, View.VISIBLE)
                    views.setTextViewText(viewId, rows[index].title)
                } else {
                    views.setViewVisibility(viewId, View.GONE)
                }
            }
            statusIds.forEachIndexed { index, viewId ->
                if (index < rows.size) {
                    views.setViewVisibility(viewId, View.VISIBLE)
                    views.setTextViewText(viewId, rows[index].status)
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
                    views.setInt(viewId, "setBackgroundResource", background)
                    views.setTextColor(viewId, ContextCompat.getColor(context, textColor))
                } else {
                    views.setViewVisibility(viewId, View.GONE)
                }
            }
            checkIds.forEachIndexed { index, viewId ->
                if (index < rows.size) {
                    views.setViewVisibility(viewId, View.VISIBLE)
                    views.setImageViewResource(viewId, R.drawable.widget_checkbox_empty)
                } else {
                    views.setViewVisibility(viewId, View.GONE)
                }
            }
        }

        private const val MAX_SERVICE_ROWS = 4
        private const val MAX_SPARE_ROWS = 4
    }
}

private data class WidgetData(
    val serviceRows: List<WidgetRow>,
    val spareRows: List<WidgetRow>,
)

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
