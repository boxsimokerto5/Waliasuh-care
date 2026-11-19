package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.BibleBookmark
import com.example.data.model.BibleNote
import kotlinx.coroutines.flow.Flow

@Dao
interface BibleBookmarkDao {
    @Query("SELECT * FROM bible_bookmarks WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllActiveBookmarks(): Flow<List<BibleBookmark>>

    @Query("SELECT * FROM bible_bookmarks ORDER BY createdAt DESC")
    suspend fun getAllBookmarksIncludingDeletedList(): List<BibleBookmark>

    @Query("SELECT * FROM bible_bookmarks WHERE schoolId = :schoolId AND isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getBookmarksBySchoolList(schoolId: String): List<BibleBookmark>

    @Query("SELECT * FROM bible_bookmarks WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BibleBookmark?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(bookmark: BibleBookmark): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(bookmarks: List<BibleBookmark>)

    @Update
    suspend fun update(bookmark: BibleBookmark)

    @Query("DELETE FROM bible_bookmarks WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bible_bookmarks WHERE schoolId = :schoolId")
    suspend fun deleteBySchool(schoolId: String)

    @Query("DELETE FROM bible_bookmarks WHERE schoolId = :schoolId AND LOWER(ownerWaliUsername) = LOWER(:username)")
    suspend fun deleteByOwner(schoolId: String, username: String)
}

@Dao
interface BibleNoteDao {
    @Query("SELECT * FROM bible_notes WHERE isDeleted = 0 ORDER BY updatedAt DESC, createdAt DESC")
    fun getAllActiveNotes(): Flow<List<BibleNote>>

    @Query("SELECT * FROM bible_notes ORDER BY updatedAt DESC, createdAt DESC")
    suspend fun getAllNotesIncludingDeletedList(): List<BibleNote>

    @Query("SELECT * FROM bible_notes WHERE schoolId = :schoolId AND isDeleted = 0 ORDER BY updatedAt DESC, createdAt DESC")
    suspend fun getNotesBySchoolList(schoolId: String): List<BibleNote>

    @Query("SELECT * FROM bible_notes WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): BibleNote?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(note: BibleNote): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(notes: List<BibleNote>)

    @Update
    suspend fun update(note: BibleNote)

    @Query("DELETE FROM bible_notes WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM bible_notes WHERE schoolId = :schoolId")
    suspend fun deleteBySchool(schoolId: String)

    @Query("DELETE FROM bible_notes WHERE schoolId = :schoolId AND LOWER(ownerWaliUsername) = LOWER(:username)")
    suspend fun deleteByOwner(schoolId: String, username: String)
}
