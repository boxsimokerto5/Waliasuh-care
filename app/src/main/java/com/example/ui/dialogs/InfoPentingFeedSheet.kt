package com.example.ui.dialogs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material.icons.filled.ZoomIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.model.InfoPentingComment
import com.example.data.model.InfoPentingCommentThread
import com.example.data.model.InfoPentingHelper
import com.example.data.model.InfoPentingPost
import com.example.data.model.InfoPentingReaction
import com.example.data.model.InfoPentingReactionSummary
import com.example.ui.components.FullscreenImagePreviewDialog
import com.example.ui.components.FullscreenVideoPlayerDialog
import com.example.ui.components.InfoDocumentAttachmentCard
import com.example.ui.components.InfoLinkPreviewCard
import com.example.ui.components.InfoVideoPlayerCard
import com.example.ui.components.rememberBannerGifImageLoader
import com.example.util.i18n.LocalAppStrings
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPentingFeedSheet(
    posts: List<InfoPentingPost>,
    reactions: List<InfoPentingReaction>,
    comments: List<InfoPentingComment>,
    currentUserKey: String,
    isAnakSuperAdmin: Boolean,
    onOpenCreatePost: () -> Unit,
    onRefreshSync: () -> Unit,
    onTogglePinPost: (InfoPentingPost) -> Unit,
    onDeletePost: (InfoPentingPost) -> Unit,
    onToggleReaction: (InfoPentingPost, String) -> Unit,
    onSendComment: (post: InfoPentingPost, content: String, replyTo: InfoPentingComment?) -> Unit,
    onDeleteOwnComment: (InfoPentingComment) -> Unit,
    onDismiss: () -> Unit
) {
    val isEn = LocalAppStrings.current.isEnglish
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(InfoPentingHelper.CATEGORY_SEMUA) }
    var activeCommentsPost by remember { mutableStateOf<InfoPentingPost?>(null) }
    var fullscreenVideoPost by remember { mutableStateOf<InfoPentingPost?>(null) }
    var fullscreenImagePost by remember { mutableStateOf<InfoPentingPost?>(null) }
    var postPendingDelete by remember { mutableStateOf<InfoPentingPost?>(null) }

    val filteredPosts = remember(posts, selectedCategory, searchQuery) {
        InfoPentingHelper.filterPosts(
            posts = posts,
            selectedCategory = selectedCategory,
            searchQuery = searchQuery
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = Color(0xFFF3F6F4),
        modifier = Modifier.testTag("sheet_info_penting")
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.94f),
            containerColor = Color(0xFFF3F6F4),
            floatingActionButton = {
                if (isAnakSuperAdmin) {
                    FloatingActionButton(
                        onClick = onOpenCreatePost,
                        containerColor = Color(0xFF0F766E),
                        contentColor = Color.White,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.testTag("fab_create_info_post")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = if (isEn) "Create Important Info Post" else "Buat Postingan Info Penting"
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Header Saluran WhatsApp Style
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(Color(0xFF075E54), Color(0xFF0F766E), Color(0xFF0D9488))
                            )
                        )
                        .padding(horizontal = 14.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color.White,
                                modifier = Modifier.size(42.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = Color(0xFF0F766E),
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                                ) {
                                    Text(
                                        text = "Info-Info Penting",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified Channel",
                                        tint = Color(0xFF5EEAD4),
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Text(
                                    text = if (isEn) {
                                        "Official Channel • Superadmin • All Schools"
                                    } else {
                                        "Saluran Resmi • Superadmin • Seluruh Sekolah"
                                    },
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.88f),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = onRefreshSync,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_refresh_info_penting")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = if (isEn) "Refresh Channel" else "Segarkan Saluran",
                                    tint = Color.White
                                )
                            }
                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier
                                    .size(48.dp)
                                    .testTag("btn_close_info_penting")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = if (isEn) "Close" else "Tutup",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }

                // Filter Search & Categories
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    tonalElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = if (isEn) "Search announcements, documents, links..." else "Cari info, dokumen, atau pengumuman...",
                                    fontSize = 12.5.sp
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp))
                            },
                            singleLine = true,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("input_search_info_penting")
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val allCategories = listOf(InfoPentingHelper.CATEGORY_SEMUA) + InfoPentingHelper.CATEGORIES
                            allCategories.forEach { cat ->
                                val selected = selectedCategory == cat
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedCategory = cat },
                                    label = {
                                        Text(
                                            text = InfoPentingHelper.categoryLabel(cat, isEn),
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = Color(0xFFCCFBF1),
                                        selectedLabelColor = Color(0xFF0F766E)
                                    )
                                )
                            }
                        }
                    }
                }

                if (filteredPosts.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFCCFBF1),
                                modifier = Modifier.size(64.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Campaign,
                                        contentDescription = null,
                                        tint = Color(0xFF0F766E),
                                        modifier = Modifier.size(32.dp)
                                    )
                                }
                            }
                            Text(
                                text = if (isEn) "No announcements yet" else "Belum ada Info Penting",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = if (isAnakSuperAdmin) {
                                    if (isEn) "Tap + below to broadcast announcements, photos, videos, documents or links as Superadmin."
                                    else "Ketuk tombol + di kanan bawah untuk membagikan teks, foto, video, dokumen, atau tautan sebagai Superadmin."
                                } else {
                                    if (isEn) "Important broadcasts from Superadmin will appear here for all schools."
                                    else "Pengumuman resmi dari Superadmin untuk seluruh Wali Asuh akan muncul di saluran ini."
                                },
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("list_info_penting_posts"),
                        contentPadding = PaddingValues(
                            start = 12.dp,
                            end = 12.dp,
                            top = 12.dp,
                            bottom = if (isAnakSuperAdmin) 88.dp else 28.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredPosts, key = { it.id }) { post ->
                            val summary = remember(reactions, post.id, currentUserKey) {
                                InfoPentingHelper.summarizeReactions(reactions, post.id, currentUserKey)
                            }
                            val commentsCount = remember(comments, post.id) {
                                InfoPentingHelper.countActiveCommentsForPost(comments, post.id)
                            }
                            InfoPentingPostCard(
                                post = post,
                                reactionSummary = summary,
                                commentsCount = commentsCount,
                                isAnakSuperAdmin = isAnakSuperAdmin,
                                isEn = isEn,
                                onTogglePin = { onTogglePinPost(post) },
                                onRequestDelete = { postPendingDelete = post },
                                onToggleReaction = { type -> onToggleReaction(post, type) },
                                onOpenComments = { activeCommentsPost = post },
                                onOpenFullscreenVideo = { fullscreenVideoPost = post },
                                onOpenFullscreenImage = { fullscreenImagePost = post }
                            )
                        }
                    }
                }
            }
        }
    }

    // Comments & Threaded Replies Sheet
    activeCommentsPost?.let { targetPost ->
        val livePost = posts.firstOrNull { it.id == targetPost.id } ?: targetPost
        val threads = remember(comments, livePost.id) {
            InfoPentingHelper.buildCommentThreads(comments, livePost.id)
        }
        InfoPentingCommentsSheet(
            post = livePost,
            threads = threads,
            currentUserKey = currentUserKey,
            isEn = isEn,
            onOpenFullscreenImage = { fullscreenImagePost = livePost },
            onOpenFullscreenVideo = { fullscreenVideoPost = livePost },
            onSendComment = { text, replyTo ->
                onSendComment(livePost, text, replyTo)
            },
            onDeleteOwnComment = onDeleteOwnComment,
            onDismiss = { activeCommentsPost = null }
        )
    }

    fullscreenVideoPost?.let { post ->
        FullscreenVideoPlayerDialog(
            primaryUrl = post.mediaUrl,
            fallbackUrl = post.fallbackMediaUrl,
            title = post.title.ifBlank { post.mediaName },
            isEn = isEn,
            onDismiss = { fullscreenVideoPost = null }
        )
    }

    fullscreenImagePost?.let { post ->
        FullscreenImagePreviewDialog(
            primaryUrl = post.mediaUrl,
            fallbackUrl = post.fallbackMediaUrl,
            title = post.title.ifBlank { post.mediaName },
            isEn = isEn,
            onDismiss = { fullscreenImagePost = null }
        )
    }

    postPendingDelete?.let { post ->
        AlertDialog(
            onDismissRequest = { postPendingDelete = null },
            title = { Text(if (isEn) "Delete Broadcast Post?" else "Hapus Postingan Saluran?") },
            text = {
                Text(
                    if (isEn) "This post will be removed for all schools."
                    else "Postingan ini akan dihapus bagi seluruh Wali Asuh di semua sekolah."
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDeletePost(post)
                        postPendingDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text(if (isEn) "Delete" else "Hapus", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { postPendingDelete = null }) {
                    Text(if (isEn) "Cancel" else "Batal")
                }
            }
        )
    }
}

@Composable
private fun InfoPentingPostCard(
    post: InfoPentingPost,
    reactionSummary: InfoPentingReactionSummary,
    commentsCount: Int,
    isAnakSuperAdmin: Boolean,
    isEn: Boolean,
    onTogglePin: () -> Unit,
    onRequestDelete: () -> Unit,
    onToggleReaction: (String) -> Unit,
    onOpenComments: () -> Unit,
    onOpenFullscreenVideo: () -> Unit,
    onOpenFullscreenImage: () -> Unit
) {
    val context = LocalContext.current
    val gifLoader = rememberBannerGifImageLoader(context)
    val dateFmt = remember { SimpleDateFormat("dd MMM yyyy • HH:mm", Locale.forLanguageTag("id-ID")) }
    val authorPublicName = InfoPentingHelper.resolvePublicAuthor(post.authorDisplayName)
    var useFallbackImage by remember(post.id, post.mediaUrl) { mutableStateOf(false) }

    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 600.dp)
            .testTag("card_info_post_${post.id}"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // Author Header: Always "Superadmin"
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFF0F766E),
                        modifier = Modifier.size(34.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "S",
                                color = Color.White,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = authorPublicName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F766E)
                            )
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified Superadmin",
                                tint = Color(0xFF0D9488),
                                modifier = Modifier.size(14.dp)
                            )
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = Color(0xFFCCFBF1)
                            ) {
                                Text(
                                    text = InfoPentingHelper.categoryLabel(post.category, isEn),
                                    fontSize = 9.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF0F766E),
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            if (post.isPinned) {
                                Icon(
                                    imageVector = Icons.Default.PushPin,
                                    contentDescription = "Pinned",
                                    tint = Color(0xFFD97706),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                        Text(
                            text = dateFmt.format(Date(post.createdAt)),
                            fontSize = 10.5.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                if (isAnakSuperAdmin) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onTogglePin,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_pin_post_${post.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PushPin,
                                contentDescription = if (post.isPinned) "Unpin" else "Pin",
                                tint = if (post.isPinned) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onRequestDelete,
                            modifier = Modifier
                                .size(40.dp)
                                .testTag("btn_delete_post_${post.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = if (isEn) "Delete Post" else "Hapus Postingan",
                                tint = Color(0xFFDC2626),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            if (post.title.isNotBlank()) {
                Text(
                    text = post.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (post.content.isNotBlank()) {
                Text(
                    text = post.content,
                    fontSize = 13.5.sp,
                    lineHeight = 19.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Media Attachment Renderer
            when (post.mediaType.uppercase()) {
                InfoPentingHelper.MEDIA_IMAGE, InfoPentingHelper.MEDIA_GIF -> {
                    val imgUrl = if (useFallbackImage && post.fallbackMediaUrl.isNotBlank()) {
                        post.fallbackMediaUrl
                    } else {
                        post.mediaUrl.ifBlank { post.fallbackMediaUrl }
                    }
                    if (imgUrl.isNotBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(imgUrl)
                                .crossfade(true)
                                .build(),
                            imageLoader = gifLoader,
                            contentDescription = post.title.ifBlank { post.mediaName },
                            contentScale = ContentScale.Crop,
                            onError = {
                                if (!useFallbackImage && post.fallbackMediaUrl.isNotBlank() && imgUrl != post.fallbackMediaUrl) {
                                    useFallbackImage = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 160.dp, max = 260.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onOpenFullscreenImage() }
                        )
                    }
                }

                InfoPentingHelper.MEDIA_VIDEO -> {
                    InfoVideoPlayerCard(
                        postId = post.id,
                        primaryUrl = post.mediaUrl,
                        fallbackUrl = post.fallbackMediaUrl,
                        title = post.title.ifBlank { post.mediaName },
                        isEn = isEn,
                        onOpenFullscreen = onOpenFullscreenVideo
                    )
                }

                InfoPentingHelper.MEDIA_DOCUMENT -> {
                    InfoDocumentAttachmentCard(
                        fileName = post.mediaName,
                        fileSizeBytes = post.mediaSizeBytes,
                        primaryUrl = post.mediaUrl,
                        fallbackUrl = post.fallbackMediaUrl,
                        isEn = isEn
                    )
                }
            }

            if (post.externalLink.isNotBlank()) {
                InfoLinkPreviewCard(url = post.externalLink, isEn = isEn)
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

            // Reaction (Like / Dislike) + Comments Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val isLiked = reactionSummary.myReaction == InfoPentingHelper.REACTION_LIKE
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isLiked) Color(0xFFCCFBF1) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (isLiked) Color(0xFF0F766E) else Color.Transparent
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onToggleReaction(InfoPentingHelper.REACTION_LIKE) }
                        .testTag("btn_like_post_${post.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbUp,
                            contentDescription = if (isEn) "Like" else "Suka",
                            tint = if (isLiked) Color(0xFF0F766E) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${reactionSummary.likesCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isLiked) Color(0xFF0F766E) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                val isDisliked = reactionSummary.myReaction == InfoPentingHelper.REACTION_DISLIKE
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = if (isDisliked) Color(0xFFFEE2E2) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        1.dp,
                        if (isDisliked) Color(0xFFDC2626) else Color.Transparent
                    ),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onToggleReaction(InfoPentingHelper.REACTION_DISLIKE) }
                        .testTag("btn_dislike_post_${post.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ThumbDown,
                            contentDescription = if (isEn) "Dislike" else "Tidak Suka",
                            tint = if (isDisliked) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "${reactionSummary.dislikesCount}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isDisliked) Color(0xFFDC2626) else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color(0xFFE0F2FE),
                    modifier = Modifier
                        .heightIn(min = 40.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .clickable { onOpenComments() }
                        .testTag("btn_open_comments_${post.id}")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = if (isEn) "Comments" else "Komentar",
                            tint = Color(0xFF0369A1),
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "$commentsCount ${if (isEn) "Comments" else "Komentar"}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0369A1)
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InfoPentingCommentsSheet(
    post: InfoPentingPost,
    threads: List<InfoPentingCommentThread>,
    currentUserKey: String,
    isEn: Boolean,
    onOpenFullscreenImage: () -> Unit = {},
    onOpenFullscreenVideo: () -> Unit = {},
    onSendComment: (content: String, replyTo: InfoPentingComment?) -> Unit,
    onDeleteOwnComment: (InfoPentingComment) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var commentDraft by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<InfoPentingComment?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = null,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("sheet_info_comments")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.86f)
                .navigationBarsPadding()
                .imePadding()
        ) {
            // Comments Header (Compact)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F766E))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isEn) "Discussion & Comments" else "Komentar & Balasan",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        lineHeight = 18.sp
                    )
                    Text(
                        text = post.title.ifBlank { post.content }.ifBlank { "Info-Info Penting" },
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.85f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 14.sp
                    )
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = if (isEn) "Close" else "Tutup",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            // Kartu konteks objek yang dikomentari (setinggi ~2 bubble chat, sticky di antara header & daftar chat)
            CommentedPostContextCard(
                post = post,
                isEn = isEn,
                onOpenFullscreenImage = onOpenFullscreenImage,
                onOpenFullscreenVideo = onOpenFullscreenVideo
            )
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.55f))

            if (threads.isEmpty()) {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (isEn) "No comments yet. Be the first to comment or reply!" else "Belum ada komentar. Mulai diskusi pertama di sini!",
                        fontSize = 12.5.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .testTag("list_info_comments"),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(threads, key = { it.comment.id }) { thread ->
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            CommentBubbleItem(
                                comment = thread.comment,
                                isReply = false,
                                isMine = InfoPentingHelper.canDeleteComment(thread.comment, currentUserKey),
                                isEn = isEn,
                                onReply = { replyingTo = thread.comment },
                                onDelete = { onDeleteOwnComment(thread.comment) }
                            )
                            thread.replies.forEach { reply ->
                                CommentBubbleItem(
                                    comment = reply,
                                    isReply = true,
                                    isMine = InfoPentingHelper.canDeleteComment(reply, currentUserKey),
                                    isEn = isEn,
                                    onReply = { replyingTo = reply },
                                    onDelete = { onDeleteOwnComment(reply) },
                                    modifier = Modifier.padding(start = 16.dp)
                                )
                            }
                        }
                    }
                }
            }

            HorizontalDivider()

            replyingTo?.let { target ->
                Surface(
                    color = Color(0xFFF0FDFA),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = null,
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(14.dp)
                            )
                            Text(
                                text = if (isEn) "Replying to @${target.authorName}" else "Membalas @${target.authorName}",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF0F766E),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        IconButton(
                            onClick = { replyingTo = null },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel reply",
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                OutlinedTextField(
                    value = commentDraft,
                    onValueChange = { commentDraft = it },
                    placeholder = {
                        Text(
                            text = if (isEn) "Write a comment..." else "Tulis komentar...",
                            fontSize = 12.5.sp
                        )
                    },
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_info_comment")
                )
                Button(
                    onClick = {
                        val text = commentDraft.trim()
                        if (text.isNotBlank()) {
                            onSendComment(text, replyingTo)
                            commentDraft = ""
                            replyingTo = null
                        }
                    },
                    enabled = commentDraft.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF0F766E),
                        contentColor = Color.White
                    ),
                    shape = CircleShape,
                    contentPadding = PaddingValues(0.dp),
                    modifier = Modifier
                        .size(44.dp)
                        .testTag("btn_send_info_comment")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = if (isEn) "Send Comment" else "Kirim Komentar",
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentedPostContextCard(
    post: InfoPentingPost,
    isEn: Boolean,
    onOpenFullscreenImage: () -> Unit,
    onOpenFullscreenVideo: () -> Unit
) {
    val context = LocalContext.current
    val gifLoader = rememberBannerGifImageLoader(context)
    val mediaUpper = post.mediaType.uppercase()
    var useFallbackImage by remember(post.id, post.mediaUrl) { mutableStateOf(false) }
    val resolvedMediaUrl = if (useFallbackImage && post.fallbackMediaUrl.isNotBlank()) {
        post.fallbackMediaUrl
    } else {
        post.mediaUrl.ifBlank { post.fallbackMediaUrl }
    }
    val hasVisualMedia = (mediaUpper == InfoPentingHelper.MEDIA_IMAGE || mediaUpper == InfoPentingHelper.MEDIA_GIF) &&
        resolvedMediaUrl.isNotBlank()
    val hasVideoMedia = mediaUpper == InfoPentingHelper.MEDIA_VIDEO && resolvedMediaUrl.isNotBlank()
    val hasDocumentMedia = mediaUpper == InfoPentingHelper.MEDIA_DOCUMENT &&
        (post.mediaName.isNotBlank() || resolvedMediaUrl.isNotBlank())

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 10.dp, vertical = 8.dp)
            .heightIn(max = 104.dp)
            .testTag("card_comment_post_context"),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFFF0FDFA),
        border = BorderStroke(1.dp, Color(0xFF99F6E4))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            when {
                hasVisualMedia -> {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(0xFFCCFBF1))
                            .clickable { onOpenFullscreenImage() }
                            .testTag("btn_preview_comment_post_image")
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(resolvedMediaUrl)
                                .crossfade(true)
                                .build(),
                            imageLoader = gifLoader,
                            contentDescription = post.title.ifBlank {
                                if (isEn) "Commented image preview" else "Pratinjau gambar yang dikomentari"
                            },
                            contentScale = ContentScale.Crop,
                            onError = {
                                if (!useFallbackImage && post.fallbackMediaUrl.isNotBlank() && resolvedMediaUrl != post.fallbackMediaUrl) {
                                    useFallbackImage = true
                                }
                            },
                            modifier = Modifier
                                .fillMaxSize()
                                .testTag("img_comment_post_context_thumb")
                        )
                        Surface(
                            shape = CircleShape,
                            color = Color.Black.copy(alpha = 0.55f),
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(4.dp)
                                .size(20.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.ZoomIn,
                                    contentDescription = if (isEn) "Enlarge image" else "Perbesar gambar",
                                    tint = Color.White,
                                    modifier = Modifier.size(12.dp)
                                )
                            }
                        }
                    }
                }

                hasVideoMedia -> {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(
                                Brush.linearGradient(
                                    listOf(Color(0xFF075E54), Color(0xFF0F766E))
                                )
                            )
                            .clickable { onOpenFullscreenVideo() }
                            .testTag("btn_preview_comment_post_video"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayCircleFilled,
                                contentDescription = if (isEn) "Play video" else "Putar video",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                            Text(
                                text = "Video",
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }

                hasDocumentMedia -> {
                    Box(
                        modifier = Modifier
                            .size(width = 56.dp, height = 68.dp)
                            .clip(RoundedCornerShape(9.dp))
                            .background(Color(0xFFCCFBF1)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(2.dp),
                            modifier = Modifier.padding(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Description,
                                contentDescription = if (isEn) "Document attachment" else "Lampiran dokumen",
                                tint = Color(0xFF0F766E),
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "DOC",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF0F766E)
                            )
                        }
                    }
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(5.dp),
                        color = Color(0xFFCCFBF1)
                    ) {
                        Text(
                            text = InfoPentingHelper.categoryLabel(post.category, isEn),
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F766E),
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                        )
                    }
                    Text(
                        text = InfoPentingHelper.resolvePublicAuthor(post.authorDisplayName),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF0F766E),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (post.externalLink.isNotBlank()) {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = null,
                            tint = Color(0xFF0369A1),
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                if (post.title.isNotBlank()) {
                    Text(
                        text = post.title,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 15.sp
                    )
                }

                val bodyPreview = when {
                    post.content.isNotBlank() -> post.content
                    hasDocumentMedia && post.mediaName.isNotBlank() -> post.mediaName
                    post.externalLink.isNotBlank() -> post.externalLink
                    hasVisualMedia -> if (isEn) "Tap thumbnail to view full photo" else "Ketuk foto untuk melihat ukuran penuh"
                    hasVideoMedia -> if (isEn) "Tap thumbnail to play video" else "Ketuk pratinjau untuk memutar video"
                    else -> ""
                }
                if (bodyPreview.isNotBlank()) {
                    Text(
                        text = bodyPreview,
                        fontSize = 11.5.sp,
                        lineHeight = 15.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun CommentBubbleItem(
    comment: InfoPentingComment,
    isReply: Boolean,
    isMine: Boolean,
    isEn: Boolean,
    onReply: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFmt = remember { SimpleDateFormat("dd MMM HH:mm", Locale.forLanguageTag("id-ID")) }
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("comment_item_${comment.id}"),
        shape = RoundedCornerShape(11.dp),
        color = if (isReply) Color(0xFFF8FAFC) else Color(0xFFF1F5F9),
        border = BorderStroke(
            1.dp,
            if (isReply) Color(0xFFE2E8F0) else Color(0xFFCBD5E1).copy(alpha = 0.55f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = comment.authorName.ifBlank { "Wali Asuh" },
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.ExtraBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = if (comment.authorName == InfoPentingHelper.PUBLIC_AUTHOR_NAME) {
                            Color(0xFF0F766E)
                        } else {
                            MaterialTheme.colorScheme.onSurface
                        }
                    )
                    if (comment.authorSchoolName.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(5.dp),
                            color = Color(0xFFE0F2FE),
                            modifier = Modifier.weight(1f, fill = false)
                        ) {
                            Text(
                                text = comment.authorSchoolName,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 0.5.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = timeFmt.format(Date(comment.createdAt)),
                    fontSize = 9.5.sp,
                    maxLines = 1,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (comment.replyToAuthorName.isNotBlank()) {
                Text(
                    text = "↩ @${comment.replyToAuthorName}",
                    fontSize = 10.sp,
                    lineHeight = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0F766E),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = comment.content,
                    fontSize = 12.5.sp,
                    lineHeight = 16.5.sp,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 6.dp)
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = Color(0xFFE0F2FE).copy(alpha = 0.7f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onReply)
                            .testTag("btn_reply_comment_${comment.id}")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Reply,
                                contentDescription = if (isEn) "Reply" else "Balas",
                                tint = Color(0xFF0369A1),
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = if (isEn) "Reply" else "Balas",
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF0369A1)
                            )
                        }
                    }

                    if (isMine) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = Color(0xFFFEE2E2).copy(alpha = 0.8f),
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .clickable(onClick = onDelete)
                                .testTag("btn_delete_comment_${comment.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(2.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = if (isEn) "Delete" else "Hapus",
                                    tint = Color(0xFFDC2626),
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (isEn) "Delete" else "Hapus",
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFDC2626)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
