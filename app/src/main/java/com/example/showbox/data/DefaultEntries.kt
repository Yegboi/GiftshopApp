package com.example.showbox.data

/**
 * Placeholder content seeded on first launch, so every section has something
 * to show before the user adds their own.
 */
object DefaultEntries {

    val all: List<Entry> = buildList {
        addAll(
            quiz(
                "Welcher Planet ist der Sonne am nächsten?" to "Merkur",
                "Wie viele Bundesländer hat Deutschland?" to "16",
                "Wer schrieb „Faust“?" to "Johann Wolfgang von Goethe",
                "Welches Element hat das Symbol „Au“?" to "Gold",
                "In welchem Jahr fiel die Berliner Mauer?" to "1989",
                "Wie heißt der längste Fluss der Welt?" to "Der Nil (nach anderer Zählung der Amazonas)",
                "Wie viele Minuten dauert ein Fußballspiel ohne Verlängerung?" to "90",
            ),
        )
        addAll(
            speedDating(
                "Was war dein schönster Moment im letzten Jahr?",
                "Wofür würdest du sofort alles stehen und liegen lassen?",
                "Welchen Song hörst du, wenn niemand zuhört?",
                "Was kannst du richtig gut, verrätst es aber selten?",
                "Wohin würdest du morgen fliegen, wenn es nichts kosten würde?",
                "Was ist der beste Rat, den du je bekommen hast?",
                "Welche Kleinigkeit rettet dir regelmäßig den Tag?",
            ),
        )
        addAll(
            estimation(
                "Wie viele Knochen hat ein erwachsener Mensch?" to "206",
                "Wie hoch ist der Eiffelturm?" to "330 Meter",
                "Wie viele Einwohner hat Deutschland ungefähr?" to "Rund 84 Millionen",
                "Wie tief ist der Marianengraben?" to "Etwa 11.000 Meter",
                "Wie viele Tasten hat ein Klavier?" to "88",
                "Wie schnell ist ein Gepard maximal?" to "Etwa 110 km/h",
                "Wie lange braucht Licht von der Sonne zur Erde?" to "Rund 8 Minuten",
            ),
        )
        addAll(
            podcast(
                "Woran erkennt man einen guten Kompromiss?",
                "Ist Multitasking ein Mythos?",
                "Warum fällt Nichtstun so schwer?",
                "Was macht Musik mit unserer Erinnerung?",
                "Brauchen wir Small Talk?",
                "Wie verändert sich Freundschaft mit dem Alter?",
            ),
        )
    }

    private fun quiz(vararg pairs: Pair<String, String>) = pairs.map {
        Entry(id = "seed-quiz-${it.first.hashCode()}", category = Category.QUIZ, prompt = it.first, answer = it.second)
    }

    private fun estimation(vararg pairs: Pair<String, String>) = pairs.map {
        Entry(
            id = "seed-est-${it.first.hashCode()}",
            category = Category.ESTIMATION,
            prompt = it.first,
            answer = it.second,
        )
    }

    private fun speedDating(vararg prompts: String) = prompts.map {
        Entry(id = "seed-sd-${it.hashCode()}", category = Category.SPEED_DATING, prompt = it)
    }

    private fun podcast(vararg prompts: String) = prompts.map {
        Entry(id = "seed-pod-${it.hashCode()}", category = Category.PODCAST, prompt = it)
    }
}
