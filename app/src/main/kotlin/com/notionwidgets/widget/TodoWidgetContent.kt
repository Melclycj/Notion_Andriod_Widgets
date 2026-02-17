package com.notionwidgets.widget

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.action.actionParametersOf
import androidx.glance.action.clickable
import androidx.glance.appwidget.action.actionRunCallback
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.lazy.items
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.size
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import com.notionwidgets.R
import com.notionwidgets.domain.model.Task

@Composable
fun TodoWidgetContent(
    tasks: List<Task>,
    isLoading: Boolean,
    error: String?
) {
    Column(
        modifier = GlanceModifier
            .fillMaxSize()
            .background(GlanceTheme.colors.background)
            .padding(12.dp)
    ) {
        WidgetHeader()

        Spacer(modifier = GlanceModifier.height(8.dp))

        when {
            isLoading -> {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading...",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    )
                }
            }
            error != null -> {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = error,
                        style = TextStyle(
                            color = GlanceTheme.colors.error,
                            fontSize = 12.sp
                        )
                    )
                }
            }
            tasks.isEmpty() -> {
                Box(
                    modifier = GlanceModifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No tasks",
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant,
                            fontSize = 14.sp
                        )
                    )
                }
            }
            else -> {
                LazyColumn(modifier = GlanceModifier.fillMaxSize()) {
                    items(tasks, itemId = { it.id.hashCode().toLong() }) { task ->
                        TaskRow(task)
                    }
                }
            }
        }
    }
}

@Composable
private fun WidgetHeader() {
    Row(
        modifier = GlanceModifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "Tasks",
            style = TextStyle(
                color = GlanceTheme.colors.onBackground,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            ),
            modifier = GlanceModifier.defaultWeight()
        )

        Image(
            provider = ImageProvider(R.drawable.ic_add),
            contentDescription = "Add task",
            modifier = GlanceModifier
                .size(24.dp)
                .clickable(actionRunCallback<AddTaskAction>())
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Image(
            provider = ImageProvider(R.drawable.ic_refresh),
            contentDescription = "Refresh",
            modifier = GlanceModifier
                .size(24.dp)
                .clickable(actionRunCallback<RefreshAction>())
        )
    }
}

@Composable
private fun TaskRow(task: Task) {
    Row(
        modifier = GlanceModifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            provider = ImageProvider(
                if (task.isCompleted) R.drawable.ic_checkbox_checked
                else R.drawable.ic_checkbox_unchecked
            ),
            contentDescription = if (task.isCompleted) "Mark incomplete" else "Mark complete",
            modifier = GlanceModifier
                .size(20.dp)
                .clickable(
                    actionRunCallback<ToggleTaskAction>(
                        actionParametersOf(TodoGlanceWidget.taskIdKey to task.id)
                    )
                )
        )

        Spacer(modifier = GlanceModifier.width(8.dp))

        Text(
            text = task.title,
            style = TextStyle(
                color = if (task.isCompleted) GlanceTheme.colors.onSurfaceVariant
                else GlanceTheme.colors.onBackground,
                fontSize = 14.sp,
                textDecoration = if (task.isCompleted) TextDecoration.LineThrough
                else TextDecoration.None
            ),
            modifier = GlanceModifier.defaultWeight(),
            maxLines = 2
        )

        Image(
            provider = ImageProvider(R.drawable.ic_delete),
            contentDescription = "Delete task",
            modifier = GlanceModifier
                .size(18.dp)
                .clickable(
                    actionRunCallback<DeleteTaskAction>(
                        actionParametersOf(TodoGlanceWidget.taskIdKey to task.id)
                    )
                )
        )
    }
}
