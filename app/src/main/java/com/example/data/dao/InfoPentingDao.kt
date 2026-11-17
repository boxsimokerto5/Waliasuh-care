package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.InfoPentingComment
import com.example.data.model.InfoPentingPost
import com.example.data.model.InfoPentingReaction
import kotlinx.coroutines.flow.Flow

@Dao
interface InfoPentingPostDao {
    @Query("SELECT * FROM info_penting_posts WHERE isDeleted = 0 ORDER BY isPinned DESC, createdAt DESC")
    fun getAllActivePosts(): Flow<List<InfoPentingPost>>

    @Query("SELECT * FROM info_penting_posts ORDER BY isPinned DESC, createdAt DESC")
    suspend fun getAllPostsIncludingDeletedList(): List<InfoPentingPost>

    @Query("SELECT * FROM info_penting_posts WHERE id = :id LIMIT 1")
    suspend fun getPostById(id: Long): InfoPentingPost?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: InfoPentingPost): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(posts: List<InfoPentingPost>)

    @Update
    suspend fun update(post: InfoPentingPost)

    @Query("DELETE FROM info_penting_posts WHERE id = :id")
    suspend fun deleteById(id: Long)
}

@Dao
interface InfoPentingReactionDao {
    @Query("SELECT * FROM info_penting_reactions ORDER BY updatedAt DESC")
    fun getAllReactions(): Flow<List<InfoPentingReaction>>

    @Query("SELECT * FROM info_penting_reactions")
    suspend fun getAllReactionsList(): List<InfoPentingReaction>

    @Query("SELECT * FROM info_penting_reactions WHERE postId = :postId AND userKey = :userKey LIMIT 1")
    suspend fun getByPostAndUser(postId: Long, userKey: String): InfoPentingReaction?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(reaction: InfoPentingReaction): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(reactions: List<InfoPentingReaction>)

    @Query("DELETE FROM info_penting_reactions WHERE postId = :postId")
    suspend fun deleteByPostId(postId: Long)
}

@Dao
interface InfoPentingCommentDao {
    @Query("SELECT * FROM info_penting_comments WHERE isDeleted = 0 ORDER BY createdAt ASC")
    fun getAllActiveComments(): Flow<List<InfoPentingComment>>

    @Query("SELECT * FROM info_penting_comments ORDER BY createdAt ASC")
    suspend fun getAllCommentsIncludingDeletedList(): List<InfoPentingComment>

    @Query("SELECT * FROM info_penting_comments WHERE id = :id LIMIT 1")
    suspend fun getCommentById(id: Long): InfoPentingComment?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(comment: InfoPentingComment): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(comments: List<InfoPentingComment>)

    @Update
    suspend fun update(comment: InfoPentingComment)

    @Query("DELETE FROM info_penting_comments WHERE postId = :postId")
    suspend fun deleteByPostId(postId: Long)
}
