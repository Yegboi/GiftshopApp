package com.example.showbox.data

import java.util.UUID

/** Builds validated [Entry] objects. Kept free of Android types so it can be tested. */
object Entries {

    /**
     * Returns a new entry, or null when the prompt is blank. The answer is
     * dropped for categories that do not have one.
     */
    fun create(category: Category, prompt: String, answer: String = ""): Entry? {
        val trimmedPrompt = prompt.trim()
        if (trimmedPrompt.isEmpty()) return null
        return Entry(
            id = UUID.randomUUID().toString(),
            category = category,
            prompt = trimmedPrompt,
            answer = if (category.hasAnswer) answer.trim() else "",
        )
    }
}
