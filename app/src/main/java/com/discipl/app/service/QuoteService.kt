package com.discipl.app.service

import android.content.Context
import com.discipl.app.data.model.Quote
import com.discipl.app.data.model.QuotesData
import com.google.gson.Gson
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class QuoteService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val quotes: List<Quote> by lazy { loadQuotes() }

    private fun loadQuotes(): List<Quote> {
        return try {
            val json = context.assets.open("quotes.json").bufferedReader().use { it.readText() }
            Gson().fromJson(json, QuotesData::class.java).quotes
        } catch (e: Exception) {
            emptyList()
        }
    }

    /**
     * Returns the quote of the day using deterministic daily rotation.
     * Same quote all day, changes at midnight local time.
     */
    fun getQuoteOfTheDay(): Quote? {
        if (quotes.isEmpty()) return null
        val dayOrdinal = LocalDate.now().toEpochDay()
        val index = (dayOrdinal % quotes.size).toInt().let { if (it < 0) it + quotes.size else it }
        return quotes[index]
    }

    fun getQuoteById(id: Int): Quote? = quotes.firstOrNull { it.id == id }

    val count: Int get() = quotes.size

    fun getQuoteByIndex(index: Int): Quote? {
        if (quotes.isEmpty()) return null
        return quotes[index % quotes.size]
    }

    fun getAllQuotes(): List<Quote> = quotes
}
