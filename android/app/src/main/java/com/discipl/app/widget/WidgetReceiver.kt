package com.discipl.app.widget

import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetReceiver

/**
 * Receiver for the small (2x2) streak widget.
 * Registered in AndroidManifest.xml with small_widget_info.xml metadata.
 */
class SmallWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = SmallWidget()
}

/**
 * Receiver for the medium (4x2) streak + milestone widget.
 * Registered in AndroidManifest.xml with medium_widget_info.xml metadata.
 */
class MediumWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = MediumWidget()
}
