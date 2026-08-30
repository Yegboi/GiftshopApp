package com.example.showbox.ui

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.showbox.data.Category
import com.example.showbox.data.DefaultEntries
import com.example.showbox.data.Entries
import com.example.showbox.data.Entry
import com.example.showbox.data.LibraryData
import com.example.showbox.data.LibraryStore
import com.example.showbox.data.Song
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File

/**
 * Owns the questions, topics and the song list, and writes every change back
 * to disk. Seeded with [DefaultEntries] the first time the app runs.
 */
class LibraryViewModel(app: Application) : AndroidViewModel(app) {

    private val store = LibraryStore(File(app.filesDir, "showbox-library.json"))

    private val entries = mutableStateListOf<Entry>()
    private val songList = mutableStateListOf<Song>()

    val songs: List<Song> get() = songList

    init {
        val data = store.load() ?: LibraryData(entries = DefaultEntries.all)
        entries.addAll(data.entries)
        songList.addAll(data.songs)
    }

    fun entriesOf(category: Category): List<Entry> = entries.filter { it.category == category }

    fun countOf(category: Category): Int = entries.count { it.category == category }

    /** Returns false when the prompt was blank, so the UI can keep the dialog open. */
    fun addEntry(category: Category, prompt: String, answer: String): Boolean {
        val entry = Entries.create(category, prompt, answer) ?: return false
        entries.add(entry)
        persist()
        return true
    }

    fun removeEntry(id: String) {
        if (entries.removeAll { it.id == id }) persist()
    }

    /** Adds songs the user picked, skipping any URI already in the list. */
    fun addSongs(picked: List<Song>) {
        val known = songList.mapTo(mutableSetOf()) { it.uri }
        val fresh = picked.filter { it.uri !in known }
        if (fresh.isEmpty()) return
        songList.addAll(fresh)
        persist()
    }

    fun removeSong(uri: String) {
        if (songList.removeAll { it.uri == uri }) persist()
    }

    private fun persist() {
        val snapshot = LibraryData(entries.toList(), songList.toList())
        viewModelScope.launch(Dispatchers.IO) { store.save(snapshot) }
    }
}
