package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.WaliGalleryLocalSave
import com.example.data.model.WaliGalleryPost
import kotlinx.coroutines.flow.Flow

@Dao
interface WaliGalleryPostDao {
    @Query("SELECT * FROM wali_gallery_posts WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllActivePosts(): Flow<List<WaliGalleryPost>>

    @Query("SELECT * FROM wali_gallery_posts ORDER BY createdAt DESC")
    suspend fun getAllPostsIncludingDeletedList(): List<WaliGalleryPost>

    @Query("SELECT * FROM wali_gallery_posts WHERE schoolId = :schoolId AND isDeleted = 0 ORDER BY createdAt DESC")
    suspend fun getActivePostsBySchoolList(schoolId: String): List<WaliGalleryPost>

    @Query("SELECT * FROM wali_gallery_posts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): WaliGalleryPost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: WaliGalleryPost): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<WaliGalleryPost>)

    @Update
    suspend fun update(post: WaliGalleryPost)

    @Query("DELETE FROM wali_gallery_posts WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM wali_gallery_posts WHERE schoolId = :schoolId")
    suspend fun deleteBySchoolId(schoolId: String)
}

@Dao
interface WaliGalleryLocalSaveDao {
    @Query("SELECT * FROM wali_gallery_local_saves ORDER BY savedAt DESC")
    fun getAllLocalSaves(): Flow<List<WaliGalleryLocalSave>>

    @Query("SELECT * FROM wali_gallery_local_saves WHERE postId = :postId LIMIT 1")
    suspend fun getByPostId(postId: Long): WaliGalleryLocalSave?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(save: WaliGalleryLocalSave)

    @Query("DELETE FROM wali_gallery_local_saves WHERE postId = :postId")
    suspend fun deleteByPostId(postId: Long)
}
