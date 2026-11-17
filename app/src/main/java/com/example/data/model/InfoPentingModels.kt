package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "info_penting_posts")
data class InfoPentingPost(
    @PrimaryKey(autoGenerate = false)
    val id: Long = 0L,
    val schoolId: String = InfoPentingHelper.GLOBAL_SCHOOL_ID,
    val title: String = "",
    val content: String = "",
    val category: String = InfoPentingHelper.CATEGORY_PENGUMUMAN,
    val mediaType: String = InfoPentingHelper.MEDIA_NONE,
    val mediaUrl: String = "",
    val fallbackMediaUrl: String = "",
    val pbRecordId: String = "",
    val pbCollectionId: String = "",
    val pbFilename: String = "",
    val mediaName: String = "",
    val mediaSizeBytes: Long = 0L,
    val externalLink: String = "",
    val authorDisplayName: String = InfoPentingHelper.PUBLIC_AUTHOR_NAME,
    val authorSchoolName: String = "",
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "info_penting_reactions")
data class InfoPentingReaction(
    @PrimaryKey(autoGenerate = false)
    val id: Long = 0L,
    val schoolId: String = InfoPentingHelper.GLOBAL_SCHOOL_ID,
    val postId: Long = 0L,
    val userKey: String = "",
    val userName: String = "",
    val userSchoolId: String = "",
    val reactionType: String = InfoPentingHelper.REACTION_NONE,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "info_penting_comments")
data class InfoPentingComment(
    @PrimaryKey(autoGenerate = false)
    val id: Long = 0L,
    val schoolId: String = InfoPentingHelper.GLOBAL_SCHOOL_ID,
    val postId: Long = 0L,
    val parentCommentId: Long = 0L,
    val replyToAuthorName: String = "",
    val authorKey: String = "",
    val authorName: String = "",
    val authorSchoolId: String = "",
    val authorSchoolName: String = "",
    val content: String = "",
    val isDeleted: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

data class UploadedInfoPentingMedia(
    val pbRecordId: String,
    val pbCollectionId: String,
    val pbFilename: String,
    val cdnUrl: String,
    val fallbackApiFileUrl: String,
    val mediaType: String,
    val fileName: String,
    val fileSizeBytes: Long
)

data class InfoPentingReactionSummary(
    val likesCount: Int = 0,
    val dislikesCount: Int = 0,
    val myReaction: String = InfoPentingHelper.REACTION_NONE
)

data class InfoPentingCommentThread(
    val comment: InfoPentingComment,
    val replies: List<InfoPentingComment>
)

object InfoPentingHelper {
    const val GLOBAL_SCHOOL_ID = "__GLOBAL__"
    const val PUBLIC_AUTHOR_NAME = "Superadmin"

    const val CATEGORY_SEMUA = "SEMUA"
    const val CATEGORY_PENGUMUMAN = "PENGUMUMAN"
    const val CATEGORY_KESEHATAN = "KESEHATAN"
    const val CATEGORY_EDUKASI = "EDUKASI"
    const val CATEGORY_DARURAT = "DARURAT"
    const val CATEGORY_JADWAL = "JADWAL"

    val CATEGORIES = listOf(
        CATEGORY_PENGUMUMAN,
        CATEGORY_KESEHATAN,
        CATEGORY_EDUKASI,
        CATEGORY_DARURAT,
        CATEGORY_JADWAL
    )

    const val MEDIA_NONE = "NONE"
    const val MEDIA_IMAGE = "IMAGE"
    const val MEDIA_GIF = "GIF"
    const val MEDIA_VIDEO = "VIDEO"
    const val MEDIA_DOCUMENT = "DOCUMENT"
    const val MEDIA_LINK = "LINK"

    const val REACTION_LIKE = "LIKE"
    const val REACTION_DISLIKE = "DISLIKE"
    const val REACTION_NONE = "NONE"

    private val VIDEO_EXTENSIONS = listOf(".mp4", ".webm", ".mov", ".m4v", ".mkv", ".3gp")
    private val IMAGE_EXTENSIONS = listOf(".jpg", ".jpeg", ".png", ".webp", ".bmp", ".heic")
    private val DOC_EXTENSIONS = listOf(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx", ".txt", ".csv", ".zip", ".rar")

    /**
     * Titik 1: Meskipun pengunggah resmi adalah akun anaksuperadmin,
     * identitas pengirim postingan pada UI publik wajib selalu tampil sebagai "Superadmin".
     */
    fun resolvePublicAuthor(rawAuthor: String?): String {
        val clean = rawAuthor.orEmpty().trim()
        if (clean.isBlank() ||
            clean.equals("anaksuperadmin", ignoreCase = true) ||
            clean.contains("anaksuperadmin", ignoreCase = true) ||
            clean.equals("superadmin", ignoreCase = true)
        ) {
            return PUBLIC_AUTHOR_NAME
        }
        return PUBLIC_AUTHOR_NAME
    }

    fun resolveCommentAuthorDisplay(rawAuthorName: String?, username: String?): String {
        val user = username.orEmpty().trim()
        val name = rawAuthorName.orEmpty().trim()
        if (user.equals("anaksuperadmin", ignoreCase = true) ||
            name.equals("anaksuperadmin", ignoreCase = true)
        ) {
            return PUBLIC_AUTHOR_NAME
        }
        return name.ifBlank { user.ifBlank { "Wali Asuh" } }
    }

    fun buildUserKey(
        schoolId: String,
        username: String?,
        fallbackName: String,
        roleTag: String = "USER"
    ): String {
        val cleanSchool = schoolId.trim().uppercase().ifBlank { "SRT1-KDR" }
        val cleanUser = username.orEmpty().trim().lowercase()
        val identity = cleanUser.ifBlank {
            fallbackName.trim().lowercase().replace(Regex("\\s+"), "_")
        }.ifBlank { roleTag.lowercase() }
        return "$cleanSchool:$identity"
    }

    fun canDeleteComment(comment: InfoPentingComment, currentUserKey: String): Boolean {
        if (comment.isDeleted || currentUserKey.isBlank()) return false
        return comment.authorKey.trim().equals(currentUserKey.trim(), ignoreCase = true)
    }

    fun canManagePost(isAnakSuperAdmin: Boolean): Boolean = isAnakSuperAdmin

    fun canDeletePost(post: InfoPentingPost, isAnakSuperAdmin: Boolean): Boolean {
        return isAnakSuperAdmin && !post.isDeleted
    }

    fun categoryLabel(category: String, isEn: Boolean): String = when (category.uppercase()) {
        CATEGORY_SEMUA -> if (isEn) "All" else "Semua"
        CATEGORY_KESEHATAN -> if (isEn) "Health" else "Kesehatan"
        CATEGORY_EDUKASI -> if (isEn) "Guidance" else "Edukasi"
        CATEGORY_DARURAT -> if (isEn) "Urgent" else "Darurat"
        CATEGORY_JADWAL -> if (isEn) "Schedule" else "Jadwal"
        else -> if (isEn) "Announcement" else "Pengumuman"
    }

    fun inferMediaType(urlsAndNames: List<String?>, explicitType: String? = null): String {
        val cleanExplicit = explicitType.orEmpty().trim().uppercase()
        for (raw in urlsAndNames) {
            val clean = raw.orEmpty()
                .trim()
                .substringBefore('?')
                .substringBefore('#')
                .lowercase()
            if (clean.isBlank()) continue
            if (VIDEO_EXTENSIONS.any { clean.endsWith(it) }) return MEDIA_VIDEO
            if (clean.endsWith(".gif")) return MEDIA_GIF
            if (IMAGE_EXTENSIONS.any { clean.endsWith(it) }) return MEDIA_IMAGE
            if (DOC_EXTENSIONS.any { clean.endsWith(it) }) return MEDIA_DOCUMENT
        }
        return when (cleanExplicit) {
            MEDIA_VIDEO, MEDIA_GIF, MEDIA_IMAGE, MEDIA_DOCUMENT, MEDIA_LINK -> cleanExplicit
            else -> MEDIA_NONE
        }
    }

    fun normalizeExternalLink(raw: String): String {
        val clean = raw.trim()
        if (clean.isBlank()) return ""
        return if (clean.startsWith("http://", ignoreCase = true) ||
            clean.startsWith("https://", ignoreCase = true)
        ) {
            clean
        } else {
            "https://$clean"
        }
    }

    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0L) return ""
        val kb = bytes / 1024.0
        val mb = kb / 1024.0
        return when {
            mb >= 1.0 -> String.format("%.1f MB", mb)
            kb >= 1.0 -> String.format("%.0f KB", kb)
            else -> "$bytes B"
        }
    }

    fun filterPosts(
        posts: List<InfoPentingPost>,
        selectedCategory: String = CATEGORY_SEMUA,
        searchQuery: String = ""
    ): List<InfoPentingPost> {
        val q = searchQuery.trim()
        val cat = selectedCategory.trim().uppercase()
        return posts
            .asSequence()
            .filter { !it.isDeleted }
            .filter { post ->
                cat.isBlank() || cat == CATEGORY_SEMUA || post.category.equals(cat, ignoreCase = true)
            }
            .filter { post ->
                if (q.isBlank()) true
                else {
                    post.title.contains(q, ignoreCase = true) ||
                        post.content.contains(q, ignoreCase = true) ||
                        post.mediaName.contains(q, ignoreCase = true) ||
                        post.externalLink.contains(q, ignoreCase = true)
                }
            }
            .sortedWith(
                compareByDescending<InfoPentingPost> { it.isPinned }
                    .thenByDescending { it.createdAt }
            )
            .toList()
    }

    fun summarizeReactions(
        reactions: List<InfoPentingReaction>,
        postId: Long,
        currentUserKey: String
    ): InfoPentingReactionSummary {
        val activeForPost = reactions.filter { it.postId == postId }
        val likes = activeForPost.count { it.reactionType.equals(REACTION_LIKE, ignoreCase = true) }
        val dislikes = activeForPost.count { it.reactionType.equals(REACTION_DISLIKE, ignoreCase = true) }
        val mine = activeForPost
            .firstOrNull { it.userKey.equals(currentUserKey, ignoreCase = true) }
            ?.reactionType
            ?.uppercase()
            ?.takeIf { it == REACTION_LIKE || it == REACTION_DISLIKE }
            ?: REACTION_NONE
        return InfoPentingReactionSummary(
            likesCount = likes,
            dislikesCount = dislikes,
            myReaction = mine
        )
    }

    fun buildCommentThreads(
        comments: List<InfoPentingComment>,
        postId: Long
    ): List<InfoPentingCommentThread> {
        val activeComments = comments
            .filter { it.postId == postId && !it.isDeleted }
            .sortedBy { it.createdAt }
        if (activeComments.isEmpty()) return emptyList()

        val byId = activeComments.associateBy { it.id }

        fun findRootParentId(comment: InfoPentingComment): Long {
            var current = comment
            val visited = mutableSetOf<Long>()
            while (current.parentCommentId > 0L && current.id !in visited) {
                visited.add(current.id)
                val parent = byId[current.parentCommentId] ?: break
                current = parent
            }
            return current.id
        }

        val topLevel = activeComments.filter { it.parentCommentId <= 0L || byId[it.parentCommentId] == null }
        val repliesByRootId = activeComments
            .filter { it.parentCommentId > 0L && byId[it.parentCommentId] != null }
            .groupBy { findRootParentId(it) }

        return topLevel.map { root ->
            InfoPentingCommentThread(
                comment = root,
                replies = repliesByRootId[root.id].orEmpty().sortedBy { it.createdAt }
            )
        }
    }

    fun countActiveCommentsForPost(comments: List<InfoPentingComment>, postId: Long): Int {
        return comments.count { it.postId == postId && !it.isDeleted }
    }

    fun countUnreadPosts(posts: List<InfoPentingPost>, lastReadAt: Long): Int {
        return posts.count { !it.isDeleted && it.createdAt > lastReadAt }
    }
}
