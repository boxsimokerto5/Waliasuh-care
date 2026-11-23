package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.SharedJournalNote
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedJournalNoteDao {
    @Query("SELECT * FROM shared_journal_notes WHERE isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC, createdAt DESC")
    fun getAllActiveNotes(): Flow<List<SharedJournalNote>>

    @Query("SELECT * FROM shared_journal_notes ORDER BY isPinned DESC, updatedAt DESC, createdAt DESC")
    suspend fun getAllNotesIncludingDeletedList(): List<SharedJournalNote>

    @Query("SELECT * FROM shared_journal_notes WHERE schoolId = :schoolId AND isDeleted = 0 ORDER BY isPinned DESC, updatedAt DESC, createdAt DESC")
    suspend fun getNotesBySchoolList(schoolId: String): List<SharedJournalNote>

    @Query("SELECT * FROM shared_journal_notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): SharedJournalNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: SharedJournalNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<SharedJournalNote>)

    @Update
    suspend fun update(note: SharedJournalNote)

    @Query("DELETE FROM shared_journal_notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM shared_journal_notes WHERE schoolId = :schoolId")
    suspend fun deleteBySchool(schoolId: String)
}
