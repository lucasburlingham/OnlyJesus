package com.onlyjesus

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

data class ReadingPlanChapterRef(
    val book: Int,
    val chapter: Int
) {
    fun key(): String = "$book:$chapter"
}

data class ReadingPlanDay(
    val dayIndex: Int,
    val chapters: List<ReadingPlanChapterRef>,
    val completedChapters: List<ReadingPlanChapterRef> = emptyList()
) {
    fun completedCount(): Int = chapters.count { chapter -> completedChapters.any { it.key() == chapter.key() } }
    fun isComplete(): Boolean = chapters.isNotEmpty() && completedCount() == chapters.size
}

data class ReadingPlan(
    val id: String,
    val title: String,
    val description: String,
    val versionPath: String,
    val versionLabel: String,
    val createdAt: Long,
    val updatedAt: Long,
    val days: List<ReadingPlanDay>
) {
    fun totalChapters(): Int = days.sumOf { it.chapters.size }

    fun completedChapters(): Int = days.sumOf { it.completedCount() }

    fun progressPercent(): Int {
        val total = totalChapters()
        if (total == 0) return 0
        return ((completedChapters().toFloat() / total.toFloat()) * 100f).toInt()
    }

    fun chapterCountForDay(dayIndex: Int): Int = days.firstOrNull { it.dayIndex == dayIndex }?.chapters?.size ?: 0

    fun dayForIndex(dayIndex: Int): ReadingPlanDay? = days.firstOrNull { it.dayIndex == dayIndex }
}

data class ReadingPlanTemplate(
    val id: String,
    val title: String,
    val description: String,
    val days: Int
)

val BuiltInReadingPlanTemplates = listOf(
    ReadingPlanTemplate(
        id = "starter-30",
        title = "30 Day Starter",
        description = "Read the whole Bible in 30 days.",
        days = 30
    ),
    ReadingPlanTemplate(
        id = "reading-90",
        title = "90 Day Reading Plan",
        description = "A faster whole-Bible reading schedule.",
        days = 90
    ),
    ReadingPlanTemplate(
        id = "year-365",
        title = "Year Plan",
        description = "Read the whole Bible in one year.",
        days = 365
    )
)

fun generateReadingPlan(
    template: ReadingPlanTemplate,
    versionPath: String,
    versionLabel: String,
    orderedChapters: List<ChapterLocation>,
    titleOverride: String? = null
): ReadingPlan {
    return generateReadingPlan(
        title = titleOverride?.takeIf { it.isNotBlank() } ?: template.title,
        description = template.description,
        versionPath = versionPath,
        versionLabel = versionLabel,
        orderedChapters = orderedChapters,
        days = template.days
    )
}

fun generateReadingPlan(
    title: String,
    description: String,
    versionPath: String,
    versionLabel: String,
    orderedChapters: List<ChapterLocation>,
    days: Int
): ReadingPlan {
    val safeDays = days.coerceAtLeast(1)
    val schedule = buildPlanSchedule(orderedChapters, safeDays)
    val timestamp = System.currentTimeMillis()
    return ReadingPlan(
        id = UUID.randomUUID().toString(),
        title = title.ifBlank { "Reading Plan" },
        description = description,
        versionPath = versionPath,
        versionLabel = versionLabel,
        createdAt = timestamp,
        updatedAt = timestamp,
        days = schedule
    )
}

fun toggleReadingPlanChapterCompletion(
    plan: ReadingPlan,
    dayIndex: Int,
    chapter: ReadingPlanChapterRef
): ReadingPlan {
    val updatedDays = plan.days.map { day ->
        if (day.dayIndex != dayIndex) {
            day
        } else {
            val completed = day.completedChapters.toMutableList()
            val existingIndex = completed.indexOfFirst { it.key() == chapter.key() }
            if (existingIndex >= 0) {
                completed.removeAt(existingIndex)
            } else {
                completed.add(chapter)
            }
            day.copy(completedChapters = completed)
        }
    }
    return plan.copy(days = updatedDays, updatedAt = System.currentTimeMillis())
}

fun markReadingPlanChapterCompleted(
    plan: ReadingPlan,
    dayIndex: Int,
    chapter: ReadingPlanChapterRef,
    completed: Boolean
): ReadingPlan {
    val updatedDays = plan.days.map { day ->
        if (day.dayIndex != dayIndex) {
            day
        } else {
            val completedChapters = day.completedChapters.toMutableList()
            val existingIndex = completedChapters.indexOfFirst { it.key() == chapter.key() }
            when {
                completed && existingIndex == -1 -> completedChapters.add(chapter)
                !completed && existingIndex >= 0 -> completedChapters.removeAt(existingIndex)
            }
            day.copy(completedChapters = completedChapters)
        }
    }
    return plan.copy(days = updatedDays, updatedAt = System.currentTimeMillis())
}

class ReadingPlanStore(private val context: Context) {
    private val plansFile = File(context.filesDir, "reading_plans.json")

    suspend fun loadPlans(): List<ReadingPlan> = withContext(Dispatchers.IO) {
        if (!plansFile.exists()) return@withContext emptyList()
        runCatching {
            val array = JSONArray(plansFile.readText())
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(readPlan(item))
                }
            }.sortedByDescending { it.updatedAt }
        }.getOrElse { emptyList() }
    }

    suspend fun savePlans(items: List<ReadingPlan>) = withContext(Dispatchers.IO) {
        val array = JSONArray()
        items.forEach { plan ->
            array.put(writePlan(plan))
        }
        plansFile.writeText(array.toString())
    }

    suspend fun exportPlansJson(items: List<ReadingPlan>): String = withContext(Dispatchers.IO) {
        val array = JSONArray()
        items.forEach { plan ->
            array.put(writePlan(plan))
        }
        array.toString()
    }

    suspend fun importPlansJson(jsonText: String): List<ReadingPlan> = withContext(Dispatchers.IO) {
        if (jsonText.isBlank()) return@withContext emptyList()
        runCatching {
            val array = JSONArray(jsonText)
            buildList {
                for (index in 0 until array.length()) {
                    val item = array.optJSONObject(index) ?: continue
                    add(readPlan(item))
                }
            }.sortedByDescending { it.updatedAt }
        }.getOrElse { emptyList() }
    }

    private fun readPlan(item: JSONObject): ReadingPlan {
        return ReadingPlan(
            id = item.optString("id", UUID.randomUUID().toString()),
            title = item.optString("title", "Reading Plan"),
            description = item.optString("description", ""),
            versionPath = item.optString("versionPath", ""),
            versionLabel = item.optString("versionLabel", ""),
            createdAt = item.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = item.optLong("updatedAt", System.currentTimeMillis()),
            days = readDays(item.optJSONArray("days"))
        )
    }

    private fun readDays(days: JSONArray?): List<ReadingPlanDay> {
        if (days == null) return emptyList()
        return buildList {
            for (index in 0 until days.length()) {
                val item = days.optJSONObject(index) ?: continue
                add(
                    ReadingPlanDay(
                        dayIndex = item.optInt("dayIndex", index + 1),
                        chapters = readChapterRefs(item.optJSONArray("chapters")),
                        completedChapters = readChapterRefs(item.optJSONArray("completedChapters"))
                    )
                )
            }
        }
    }

    private fun readChapterRefs(chapters: JSONArray?): List<ReadingPlanChapterRef> {
        if (chapters == null) return emptyList()
        return buildList {
            for (index in 0 until chapters.length()) {
                val item = chapters.optJSONObject(index) ?: continue
                add(
                    ReadingPlanChapterRef(
                        book = item.optInt("book", 1),
                        chapter = item.optInt("chapter", 1)
                    )
                )
            }
        }
    }

    private fun writePlan(plan: ReadingPlan): JSONObject {
        return JSONObject()
            .put("id", plan.id)
            .put("title", plan.title)
            .put("description", plan.description)
            .put("versionPath", plan.versionPath)
            .put("versionLabel", plan.versionLabel)
            .put("createdAt", plan.createdAt)
            .put("updatedAt", plan.updatedAt)
            .put("days", JSONArray().apply {
                plan.days.forEach { day ->
                    put(
                        JSONObject()
                            .put("dayIndex", day.dayIndex)
                            .put("chapters", JSONArray().apply {
                                day.chapters.forEach { chapter ->
                                    put(
                                        JSONObject()
                                            .put("book", chapter.book)
                                            .put("chapter", chapter.chapter)
                                    )
                                }
                            })
                            .put("completedChapters", JSONArray().apply {
                                day.completedChapters.forEach { chapter ->
                                    put(
                                        JSONObject()
                                            .put("book", chapter.book)
                                            .put("chapter", chapter.chapter)
                                    )
                                }
                            })
                    )
                }
            })
    }
}

private fun buildPlanSchedule(
    orderedChapters: List<ChapterLocation>,
    days: Int
): List<ReadingPlanDay> {
    if (days <= 0) return emptyList()
    if (orderedChapters.isEmpty()) {
        return (1..days).map { dayIndex -> ReadingPlanDay(dayIndex = dayIndex, chapters = emptyList()) }
    }

    val baseSize = orderedChapters.size / days
    val remainder = orderedChapters.size % days
    var cursor = 0

    return buildList {
        for (dayIndex in 0 until days) {
            val targetSize = baseSize + if (dayIndex < remainder) 1 else 0
            val nextCursor = (cursor + targetSize).coerceAtMost(orderedChapters.size)
            val chapters = orderedChapters
                .subList(cursor, nextCursor)
                .map { ReadingPlanChapterRef(it.book, it.chapter) }
            add(
                ReadingPlanDay(
                    dayIndex = dayIndex + 1,
                    chapters = chapters
                )
            )
            cursor = nextCursor
        }
    }
}