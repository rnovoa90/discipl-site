package com.discipl.app.data.model

import com.google.gson.annotations.SerializedName

// --- Quotes ---

data class QuotesData(val quotes: List<Quote>)

data class Quote(
    val id: Int,
    @SerializedName("textEs") val textEs: String,
    @SerializedName("textEn") val textEn: String,
    val author: String?
) {
    fun text(language: String): String = if (language == "en") textEn else textEs
}

// --- Benefits / Milestones ---

data class MilestonesData(val milestones: List<Milestone>)

data class Milestone(
    val day: Int,
    @SerializedName("titleEs") val titleEs: String,
    @SerializedName("titleEn") val titleEn: String,
    @SerializedName("detailEs") val detailEs: String,
    @SerializedName("detailEn") val detailEn: String,
    @SerializedName("whyEs") val whyEs: String,
    @SerializedName("whyEn") val whyEn: String,
    @SerializedName("signsEs") val signsEs: List<String>,
    @SerializedName("signsEn") val signsEn: List<String>,
    val isFree: Boolean
) {
    fun title(language: String): String = if (language == "en") titleEn else titleEs
    fun detail(language: String): String = if (language == "en") detailEn else detailEs
    fun why(language: String): String = if (language == "en") whyEn else whyEs
    fun signs(language: String): List<String> = if (language == "en") signsEn else signsEs

    fun dayLabel(language: String): String = when (day) {
        730 -> if (language == "en") "Year 2" else "Año 2"
        1095 -> if (language == "en") "Year 3" else "Año 3"
        else -> if (language == "en") "Day $day" else "Día $day"
    }

    val shortLabel: String
        get() = when (day) {
            730 -> "2A"
            1095 -> "3A"
            else -> "$day"
        }
}

// --- Insights ---

data class InsightsData(val insights: List<Insight>)

data class Insight(
    val minDay: Int,
    val maxDay: Int,
    @SerializedName("textEs") val textEs: String,
    @SerializedName("textEn") val textEn: String
) {
    fun text(language: String): String = if (language == "en") textEn else textEs
}

// --- Daily Tasks ---

data class TasksData(val tasks: List<DailyTask>)

data class DailyTask(
    val day: Int,
    val variant: Int,
    @SerializedName("textEs") val textEs: String,
    @SerializedName("textEn") val textEn: String
) {
    fun text(language: String): String = if (language == "en") textEn else textEs
}
