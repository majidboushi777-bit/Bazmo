package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ForumComment
import com.example.data.ForumPostEntity
import com.example.data.toPersianNumber
import com.example.ui.components.BadgeTone
import com.example.ui.components.BazmoBackBar
import com.example.ui.components.BazmoBadge
import com.example.ui.components.BazmoCard
import com.example.ui.components.BazmoEmptyState
import com.example.ui.components.BazmoFilterChip
import com.example.ui.components.BazmoGoldButton
import com.example.ui.components.BazmoInput
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoSearchBar
import com.example.ui.components.BazmoSectionHeader
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardSurface
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySoft
import com.example.ui.theme.RedditDown
import com.example.ui.theme.RedditDownSoft
import com.example.ui.theme.RedditUp
import com.example.ui.theme.RedditUpSoft
import com.example.ui.theme.SoftSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.BazmoModal
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.BazmoViewModel
import com.example.viewmodel.ForumSort

fun countAllComments(post: ForumPostEntity): Int {
    var total = post.comments.size
    post.comments.forEach { total += it.replies.size }
    return total
}

@Composable
fun ForumScreen(
    board: String,
    state: BazmoUiState,
    viewModel: BazmoViewModel,
    onBack: () -> Unit,
    bottomContent: (@Composable () -> Unit)? = null
) {
    val isHw = board == "hardware"
    val hasForumAccess = if (isHw) state.canAccessHardwareForum else state.canAccessSoftwareSpecialized
    if (!hasForumAccess) {
        BackHandler { onBack() }
        Column(modifier = Modifier.fillMaxSize()) {
            BazmoBackBar(
                title = if (isHw) "انجمن سخت‌افزار" else "انجمن نرم‌افزار",
                onBack = onBack
            )
            BazmoEmptyState(
                if (isHw) "دسترسی به انجمن سخت‌افزار فقط برای نقش تعمیرکار مجاز است."
                else "دسترسی به انجمن نرم‌افزار فقط برای نقش نرم‌افزارکار مجاز است."
            )
        }
        return
    }
    val fs = if (isHw) state.session.hwForum else state.session.swForum
    val flairs = if (isHw) {
        listOf("همه", "آی‌سی و برد", "تاچ و ال‌سی‌دی", "عیب‌یابی جریان", "شماتیک و سولوشن")
    } else {
        listOf("همه", "FRP و آنلاک", "رام و فلش", "iOS و آیکلود", "باکس و دانگل")
    }

    if (fs.threadId != null) {
        val post = state.db.forumPosts.find { it.id == fs.threadId }
        if (post == null) {
            viewModel.setForumThread(board, null)
            return
        }
        BackHandler { viewModel.setForumThread(board, null) }
        ForumThreadDetail(
            post = post,
            currentUid = state.currentUid,
            onBack = { viewModel.setForumThread(board, null) },
            viewModel = viewModel
        )
        return
    }

    BackHandler { onBack() }

    val filtered = remember(state.db.forumPosts, board, fs.flair, fs.search, fs.sort) {
        state.db.forumPosts
            .filter { p ->
                p.board == board &&
                    (fs.flair == "همه" || p.flair == fs.flair) &&
                    (fs.search.isBlank() ||
                        p.title.contains(fs.search, ignoreCase = true) ||
                        p.body.contains(fs.search, ignoreCase = true) ||
                        p.deviceTag.contains(fs.search, ignoreCase = true))
            }
            .sortedWith { a, b ->
                if (a.pinned != b.pinned) {
                    if (a.pinned) -1 else 1
                } else {
                    when (fs.sort) {
                        ForumSort.NEW -> b.createdAtMs.compareTo(a.createdAtMs)
                        ForumSort.TOP -> b.votes.compareTo(a.votes)
                        ForumSort.HOT -> (b.votes + countAllComments(b) * 3)
                            .compareTo(a.votes + countAllComments(a) * 3)
                    }
                }
            }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp)
    ) {
        item {
            BazmoBackBar(
                title = if (isHw) "انجمن تخصصی سخت‌افزار بازمو" else "انجمن تخصصی نرم‌افزار بازمو",
                onBack = onBack
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(NavyDeep, Color(0xFF274C85))))
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isHw) "🔧 r/Bazmo_Hardware" else "💻 r/Bazmo_Software",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )
                        Text(
                            text = "جامعه همکاران و تعمیرکاران تخصصی ایران",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFCFE2FF),
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    BazmoGoldButton(
                        text = "+ پست جدید",
                        onClick = { viewModel.openModal(BazmoModal.NewRedditPost(board)) },
                        compact = true
                    )
                }
            }
        }

        item {
            BazmoSearchBar(
                value = fs.search,
                onValueChange = { viewModel.setForumSearch(board, it) },
                placeholder = "جستجو در بحث‌ها، مدل گوشی یا خطاها...",
                modifier = Modifier.padding(bottom = 10.dp)
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = CardSurface,
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(5.dp),
                    horizontalArrangement = Arrangement.spacedBy(5.dp)
                ) {
                    SortPill(
                        label = "🔥 داغترین",
                        selected = fs.sort == ForumSort.HOT,
                        onClick = { viewModel.setForumSort(board, ForumSort.HOT) },
                        modifier = Modifier.weight(1f)
                    )
                    SortPill(
                        label = "🆕 جدیدترین",
                        selected = fs.sort == ForumSort.NEW,
                        onClick = { viewModel.setForumSort(board, ForumSort.NEW) },
                        modifier = Modifier.weight(1f)
                    )
                    SortPill(
                        label = "🏆 برترین‌ها",
                        selected = fs.sort == ForumSort.TOP,
                        onClick = { viewModel.setForumSort(board, ForumSort.TOP) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(7.dp),
                contentPadding = PaddingValues(bottom = 10.dp)
            ) {
                items(flairs) { flair ->
                    BazmoFilterChip(
                        label = flair,
                        selected = fs.flair == flair,
                        onClick = { viewModel.setForumFlair(board, flair) }
                    )
                }
            }
        }

        if (filtered.isEmpty()) {
            item {
                BazmoEmptyState("پستی در این موضوع یافت نشد.")
            }
        } else {
            items(filtered, key = { it.id }) { post ->
                ForumPostCard(
                    post = post,
                    currentUid = state.currentUid,
                    onOpen = { viewModel.setForumThread(board, post.id) },
                    onVote = { dir -> viewModel.votePost(post, dir) },
                    onToggleSave = { viewModel.toggleSavePost(post) }
                )
            }
        }

        if (bottomContent != null) {
            item {
                Spacer(modifier = Modifier.height(6.dp))
                bottomContent()
            }
        }
    }
}

@Composable
private fun SortPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (selected) NavySoft else Color.Transparent,
        shape = RoundedCornerShape(10.dp),
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = if (selected) Navy else TextMuted,
                fontWeight = FontWeight.ExtraBold,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ForumPostCard(
    post: ForumPostEntity,
    currentUid: String,
    onOpen: () -> Unit,
    onVote: (Int) -> Unit,
    onToggleSave: () -> Unit
) {
    BazmoCard(onClick = onOpen) {
        // Meta row
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (post.pinned) {
                BazmoBadge("📌 سنجاق", BadgeTone.GREEN)
            }
            if (post.verifiedSolution) {
                BazmoBadge("✅ تایید ادمین", BadgeTone.GREEN)
            }
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(Navy),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = post.author.take(1),
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = post.author,
                style = MaterialTheme.typography.labelMedium,
                color = NavyDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            BazmoBadge(post.flair, BadgeTone.BLUE)
            Text(
                text = "• ${post.timeLabel}",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                maxLines = 1
            )
        }

        if (post.deviceTag.isNotBlank()) {
            Spacer(modifier = Modifier.height(6.dp))
            BazmoBadge("📱 ${post.deviceTag}", BadgeTone.GOLD)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = post.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Black,
            color = NavyDeep
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = post.body,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            maxLines = 3,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            VotePill(
                score = post.votes,
                userVote = post.userVoteFor(currentUid),
                onUp = { onVote(1) },
                onDown = { onVote(-1) }
            )

            ActionChipButton(
                text = "💬 ${countAllComments(post).toPersianNumber()} نظر",
                active = false,
                onClick = onOpen
            )

            val isSaved = post.isSavedBy(currentUid)
            ActionChipButton(
                text = if (isSaved) "🔖 ذخیره" else "🔖",
                active = isSaved,
                onClick = onToggleSave
            )
        }
    }
}

@Composable
private fun ForumThreadDetail(
    post: ForumPostEntity,
    currentUid: String,
    onBack: () -> Unit,
    viewModel: BazmoViewModel
) {
    var mainCommentText by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item {
            BazmoBackBar(
                title = post.flair,
                onBack = onBack
            )
        }

        item {
            BazmoCard {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Navy),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = post.author.take(1),
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                    Text(
                        text = post.author,
                        style = MaterialTheme.typography.labelLarge,
                        color = NavyDeep
                    )
                    BazmoBadge(
                        text = post.authorBadge,
                        tone = if (post.authorBadge == "استاد کار") BadgeTone.PURPLE else BadgeTone.GREEN
                    )
                    if (post.verifiedSolution) {
                        BazmoBadge("✅ تاییدیه ادمین", BadgeTone.GREEN)
                    }
                }
                Text(
                    text = "${post.timeLabel} • 📍 ${post.city}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )

                if (post.deviceTag.isNotBlank()) {
                    Spacer(modifier = Modifier.height(6.dp))
                    BazmoBadge("📱 ${post.deviceTag}", BadgeTone.GOLD)
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Black,
                    color = NavyDeep
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = post.body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    VotePill(
                        score = post.votes,
                        userVote = post.userVoteFor(currentUid),
                        onUp = { viewModel.votePost(post, 1) },
                        onDown = { viewModel.votePost(post, -1) }
                    )
                    ActionChipButton(
                        text = "💬 ${countAllComments(post).toPersianNumber()} دیدگاه",
                        active = false,
                        onClick = {}
                    )
                    val isSaved = post.isSavedBy(currentUid)
                    ActionChipButton(
                        text = if (isSaved) "🔖 ذخیره شد" else "🔖 ذخیره",
                        active = isSaved,
                        onClick = { viewModel.toggleSavePost(post) }
                    )
                }
            }
        }

        item {
            BazmoCard {
                Text(
                    text = "افزودن دیدگاه تخصصی شما:",
                    style = MaterialTheme.typography.titleSmall,
                    color = NavyDeep,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BazmoInput(
                    value = mainCommentText,
                    onValueChange = { mainCommentText = it },
                    placeholder = "تجربه یا راه حل خود را بنویسید...",
                    singleLine = false,
                    minLines = 2
                )
                Spacer(modifier = Modifier.height(8.dp))
                BazmoPrimaryButton(
                    text = "ارسال دیدگاه",
                    onClick = {
                        if (mainCommentText.isNotBlank()) {
                            viewModel.addForumComment(post, mainCommentText)
                            mainCommentText = ""
                        }
                    },
                    compact = true
                )
            }
        }

        item {
            BazmoSectionHeader("بحث و تبادل نظر (${countAllComments(post).toPersianNumber()})")
        }

        if (post.comments.isEmpty()) {
            item {
                BazmoEmptyState("هنوز دیدگاهی ثبت نشده است.")
            }
        } else {
            items(post.comments, key = { it.id }) { comment ->
                CommentItemCard(
                    post = post,
                    comment = comment,
                    currentUid = currentUid,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun CommentItemCard(
    post: ForumPostEntity,
    comment: ForumComment,
    currentUid: String,
    viewModel: BazmoViewModel
) {
    var showReplyBox by remember { mutableStateOf(false) }
    var replyText by rememberSaveable(comment.id) { mutableStateOf("") }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        Box(
            modifier = Modifier
                .width(3.dp)
                .padding(top = 4.dp)
                .background(Color(0xFFCBD5E1), RoundedCornerShape(2.dp))
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 8.dp)
        ) {
            Surface(
                color = SoftSurface,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(11.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = comment.author,
                            style = MaterialTheme.typography.labelLarge,
                            color = Navy
                        )
                        BazmoBadge(comment.badge, BadgeTone.BLUE)
                        Text(
                            text = "• ${comment.timeLabel}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    Spacer(modifier = Modifier.height(5.dp))
                    Text(
                        text = comment.text,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VotePill(
                            score = comment.votes,
                            userVote = comment.userVoteFor(currentUid),
                            onUp = { viewModel.voteForumComment(post, comment.id, null, 1) },
                            onDown = { viewModel.voteForumComment(post, comment.id, null, -1) }
                        )
                        ActionChipButton(
                            text = "↩️ پاسخ",
                            active = showReplyBox,
                            onClick = { showReplyBox = !showReplyBox }
                        )
                    }

                    if (showReplyBox) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BazmoInput(
                                value = replyText,
                                onValueChange = { replyText = it },
                                placeholder = "پاسخ به ${comment.author}...",
                                modifier = Modifier.weight(1f)
                            )
                            BazmoPrimaryButton(
                                text = "ثبت",
                                onClick = {
                                    if (replyText.isNotBlank()) {
                                        viewModel.addNestedForumReply(post, comment.id, replyText)
                                        replyText = ""
                                        showReplyBox = false
                                    }
                                },
                                compact = true
                            )
                        }
                    }
                }
            }

            comment.replies.forEach { rep ->
                Spacer(modifier = Modifier.height(6.dp))
                Surface(
                    color = Color(0xFFF1F5F9),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFF93C5FD)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 14.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = rep.author,
                                style = MaterialTheme.typography.labelLarge,
                                color = NavyDeep
                            )
                            BazmoBadge(rep.badge, BadgeTone.PURPLE)
                            Text(
                                text = "• ${rep.timeLabel}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = rep.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        VotePill(
                            score = rep.votes,
                            userVote = rep.userVoteFor(currentUid),
                            onUp = { viewModel.voteForumComment(post, comment.id, rep.id, 1) },
                            onDown = { viewModel.voteForumComment(post, comment.id, rep.id, -1) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun VotePill(
    score: Int,
    userVote: Int,
    onUp: () -> Unit,
    onDown: () -> Unit
) {
    Surface(
        color = Color(0xFFF1F5F9),
        shape = RoundedCornerShape(99.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (userVote == 1) RedditUpSoft else Color.Transparent)
                    .clickable(onClick = onUp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▲",
                    color = if (userVote == 1) RedditUp else TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
            Text(
                text = score.toPersianNumber(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Black,
                color = NavyDeep
            )
            Box(
                modifier = Modifier
                    .size(30.dp)
                    .clip(CircleShape)
                    .background(if (userVote == -1) RedditDownSoft else Color.Transparent)
                    .clickable(onClick = onDown),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "▼",
                    color = if (userVote == -1) RedditDown else TextMuted,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun ActionChipButton(
    text: String,
    active: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (active) Color(0xFFFEF3C7) else Color(0xFFF1F5F9),
        shape = RoundedCornerShape(99.dp),
        modifier = Modifier
            .clip(RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = if (active) Color(0xFF92400E) else TextSecondary,
            modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
        )
    }
}
