package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.formatToman
import com.example.ui.components.BadgeTone
import com.example.ui.components.BazmoBackBar
import com.example.ui.components.BazmoBadge
import com.example.ui.components.BazmoCard
import com.example.ui.components.BazmoEmptyState
import com.example.ui.components.BazmoGoldButton
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoPromoBannerGrid
import com.example.ui.components.BazmoSearchBar
import com.example.ui.components.BazmoSectionHeader
import com.example.ui.components.BazmoSquareTile
import com.example.ui.components.BazmoWarrantyBox
import com.example.ui.theme.HeaderBgDeep
import com.example.ui.theme.HeaderBgLight
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyLight
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.BazmoModal
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.BazmoViewModel
import com.example.viewmodel.SoftwareSubMode

@Composable
fun SoftwareTabScreen(
    state: BazmoUiState,
    viewModel: BazmoViewModel
) {
    val sw = state.session.swView
    val uriHandler = LocalUriHandler.current
    val swBanners = remember(state.db.banners, state.session.province, state.session.city, state.session.region) {
        state.bannersForSection("software")
    }

    BackHandler(enabled = sw.mode != SoftwareSubMode.HOME && sw.mode != SoftwareSubMode.FORUM) {
        viewModel.setSwMode(SoftwareSubMode.HOME)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cols = if (maxWidth >= 640.dp) 2 else 1
        val tileCols = when {
            maxWidth >= 840.dp -> 6
            maxWidth >= 600.dp -> 4
            else -> 3
        }

        AnimatedContent(
            targetState = sw.mode,
            transitionSpec = {
                fadeIn(tween(180)) togetherWith fadeOut(tween(130))
            },
            label = "softwareSubModeTransition"
        ) { mode ->
            when (mode) {
                SoftwareSubMode.FORUM -> {
                    ForumScreen(
                        board = "software",
                        state = state,
                        viewModel = viewModel,
                        onBack = { viewModel.setSwMode(SoftwareSubMode.HOME) },
                        bottomContent = { SoftwareBottomDock(state, viewModel) }
                    )
                }

                SoftwareSubMode.PROBLEM, SoftwareSubMode.TUTORIALS -> {
                    val prob = remember(state.db.swProblems, sw.problemId) {
                        state.db.swProblems.find { it.id == sw.problemId }
                    }
                    val canViewTutorials = state.canAccessSoftwareSpecialized
                    val list = remember(state.db.tutorials, canViewTutorials, sw.problemId, sw.search) {
                        if (canViewTutorials) {
                            state.db.tutorials.filter { t ->
                                t.status == "approved" &&
                                    (sw.problemId == null || t.problemId == sw.problemId) &&
                                    (sw.search.isBlank() || t.title.contains(sw.search, ignoreCase = true) || t.teacher.contains(sw.search, ignoreCase = true))
                            }
                        } else {
                            emptyList()
                        }
                    }
                    val tutRows = remember(list, cols) { list.chunked(cols) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "sw_tut_back") {
                            BazmoBackBar(
                                title = when {
                                    !canViewTutorials && prob != null -> "${prob.iconEmoji} ${prob.title}"
                                    prob != null -> "آموزش‌های: ${prob.title}"
                                    else -> "آموزش‌های نرم‌افزار"
                                },
                                onBack = { viewModel.setSwMode(SoftwareSubMode.HOME) }
                            )
                        }
                        if (!canViewTutorials) {
                            item(key = "sw_tut_locked") {
                                BazmoCard {
                                    Text(
                                        text = prob?.let { "${it.iconEmoji} ${it.title}" } ?: "بخش مشکلات نرم‌افزاری",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = NavyDeep
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    BazmoWarrantyBox(
                                        "🔒 دوره‌ها و آموزش‌های تخصصی این بخش و انجمن نرم‌افزار فقط برای نقش «نرم‌افزارکار» فعال است. سایر همکاران می‌توانند درخواست نرم‌افزاری ثبت کنند یا از بخش باکس و دانگل استفاده کنند."
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BazmoPrimaryButton(
                                            text = "+ ثبت درخواست نرم‌افزاری",
                                            onClick = { viewModel.openModal(BazmoModal.NewSwReq) },
                                            compact = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        BazmoGoldButton(
                                            text = "🧰 باکس و دانگل‌ها",
                                            onClick = { viewModel.setSwMode(SoftwareSubMode.BOXES) },
                                            compact = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        } else {
                            item(key = "sw_tut_search") {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 10.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BazmoSearchBar(
                                        value = sw.search,
                                        onValueChange = { viewModel.updateSwSearch(it) },
                                        placeholder = "جستجو در آموزش‌ها...",
                                        modifier = Modifier.weight(1f)
                                    )
                                    BazmoGoldButton(
                                        text = "+ فروش آموزش",
                                        onClick = { viewModel.openModal(BazmoModal.SellTutorial) },
                                        compact = true
                                    )
                                }
                            }
                            if (list.isEmpty()) {
                                item(key = "sw_tut_empty") { BazmoEmptyState("آموزشی یافت نشد.") }
                            } else {
                                itemsIndexed(tutRows, key = { idx, row -> "tut_row_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowItems.forEach { t ->
                                            BazmoCard(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    BazmoBadge("👨🏫 ${t.teacher}", BadgeTone.BLUE)
                                                    BazmoBadge("⭐ ${t.rating} • 📍 ${t.city}", BadgeTone.GREEN)
                                                }
                                                Spacer(modifier = Modifier.height(7.dp))
                                                Text(
                                                    text = t.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = TextPrimary
                                                )
                                                Spacer(modifier = Modifier.height(6.dp))
                                                BazmoWarrantyBox("📌 شرایط: ${t.terms}")
                                                Spacer(modifier = Modifier.height(10.dp))
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Text(
                                                        text = t.price.formatToman(),
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.Black,
                                                        color = Navy
                                                    )
                                                    BazmoPrimaryButton(
                                                        text = "خرید آموزش",
                                                        onClick = { viewModel.addToCart(t.id, t.title, t.price, t.teacher) },
                                                        compact = true
                                                    )
                                                }
                                            }
                                        }
                                        repeat(cols - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                        item(key = "sw_tut_dock") { SoftwareBottomDock(state, viewModel) }
                    }
                }

                SoftwareSubMode.REQUESTS -> {
                    val reqs = remember(state.db.swRequests, sw.search) {
                        state.db.swRequests.filter { r ->
                            sw.search.isBlank() || r.title.contains(sw.search, ignoreCase = true)
                        }
                    }
                    val reqRows = remember(reqs, cols) { reqs.chunked(cols) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "sw_req_back") {
                            BazmoBackBar(
                                title = "درخواست‌های نرم‌افزاری",
                                onBack = { viewModel.setSwMode(SoftwareSubMode.HOME) }
                            )
                        }
                        item(key = "sw_req_search") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BazmoSearchBar(
                                    value = sw.search,
                                    onValueChange = { viewModel.updateSwSearch(it) },
                                    placeholder = "جستجو در درخواست‌ها...",
                                    modifier = Modifier.weight(1f)
                                )
                                BazmoPrimaryButton(
                                    text = "+ ثبت درخواست",
                                    onClick = { viewModel.openModal(BazmoModal.NewSwReq) },
                                    compact = true
                                )
                            }
                        }
                        if (reqs.isEmpty()) {
                            item(key = "sw_req_empty") { BazmoEmptyState("درخواستی یافت نشد.") }
                        } else {
                            itemsIndexed(reqRows, key = { idx, row -> "sw_req_row_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { r ->
                                        BazmoCard(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                BazmoBadge(r.status, BadgeTone.GOLD)
                                                Text(text = "📍 ${r.city}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                            }
                                            Spacer(modifier = Modifier.height(7.dp))
                                            Text(
                                                text = r.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                            if (r.reply.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                BazmoWarrantyBox("✅ پاسخ ادمین: ${r.reply}")
                                            }
                                        }
                                    }
                                    repeat(cols - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        item(key = "sw_req_dock") { SoftwareBottomDock(state, viewModel) }
                    }
                }

                SoftwareSubMode.BOXES -> {
                    val boxes = remember(state.db.boxes, sw.search) {
                        state.db.boxes.filter { b ->
                            b.status == "approved" && (sw.search.isBlank() || b.name.contains(sw.search, ignoreCase = true) || b.city.contains(sw.search, ignoreCase = true))
                        }
                    }
                    val boxRows = remember(boxes, cols) { boxes.chunked(cols) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "sw_box_back") {
                            BazmoBackBar(
                                title = "خرید و فروش باکس‌های تعمیراتی",
                                onBack = { viewModel.setSwMode(SoftwareSubMode.HOME) }
                            )
                        }
                        item(key = "sw_box_search") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 10.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                BazmoSearchBar(
                                    value = sw.search,
                                    onValueChange = { viewModel.updateSwSearch(it) },
                                    placeholder = "جستجوی باکس یا دانگل...",
                                    modifier = Modifier.weight(1f)
                                )
                                BazmoGoldButton(
                                    text = "+ فروش باکس",
                                    onClick = { viewModel.openModal(BazmoModal.SellBox) },
                                    compact = true
                                )
                            }
                        }
                        if (boxes.isEmpty()) {
                            item(key = "sw_box_empty") { BazmoEmptyState("باکسی یافت نشد.") }
                        } else {
                            itemsIndexed(boxRows, key = { idx, row -> "sw_box_row_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { b ->
                                        BazmoCard(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                BazmoBadge("🧰 ${b.condition}", BadgeTone.BLUE)
                                                Text(text = "📍 ${b.city}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = b.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "فروشنده: ${b.seller}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted,
                                                modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = b.price.formatToman(),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = Navy
                                                )
                                                BazmoPrimaryButton(
                                                    text = "خرید باکس",
                                                    onClick = { viewModel.addToCart(b.id, b.name, b.price, b.seller) },
                                                    compact = true
                                                )
                                            }
                                        }
                                    }
                                    repeat(cols - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        item(key = "sw_box_dock") { SoftwareBottomDock(state, viewModel) }
                    }
                }

                SoftwareSubMode.HOME -> {
                    val filteredProblems = remember(state.db.swProblems, sw.search) {
                        state.db.swProblems.filter { p ->
                            sw.search.isBlank() || p.title.contains(sw.search, ignoreCase = true)
                        }
                    }
                    val rows = remember(filteredProblems, tileCols) {
                        filteredProblems.chunked(tileCols)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "sw_home_search") {
                            BazmoSearchBar(
                                value = sw.search,
                                onValueChange = { viewModel.updateSwSearch(it) },
                                placeholder = "جستجوی مشکل نرم‌افزاری، آموزش یا باکس...",
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                        item(key = "sw_promo_banners") {
                            BazmoPromoBannerGrid(
                                slots = swBanners,
                                onBannerClick = { banner ->
                                    viewModel.openPromoBanner(banner) { url ->
                                        runCatching { uriHandler.openUri(url) }
                                            .onFailure { viewModel.showToast("امکان باز کردن لینک وجود ندارد") }
                                    }
                                },
                                onEmptySlotClick = {
                                    viewModel.showToast("اینجا جای تبلیغات شماست؛ جهت ثبت بنر با مدیریت بازمو تماس بگیرید.")
                                }
                            )
                        }
                        if (!state.canAccessSoftwareSpecialized) {
                            item(key = "sw_home_notice") {
                                BazmoWarrantyBox(
                                    text = "🔒 آموزش‌ها و انجمن تخصصی نرم‌افزار فقط برای نقش نرم‌افزارکار فعال است. بخش درخواست‌ها و باکس‌ها برای همه همکاران آزاد است.",
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                        }
                        item(key = "sw_home_hdr") {
                            BazmoSectionHeader("کاشی مشکلات نرم‌افزاری")
                        }
                        itemsIndexed(rows, key = { idx, row -> "sw_prob_row_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 9.dp),
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                rowItems.forEach { prob ->
                                    BazmoSquareTile(
                                        iconEmoji = prob.iconEmoji,
                                        title = prob.title,
                                        onClick = { viewModel.setSwMode(SoftwareSubMode.PROBLEM, prob.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(tileCols - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        item(key = "sw_home_dock") {
                            BazmoSectionHeader("بخش‌های تخصصی نرم‌افزار")
                            SoftwareBottomDock(state, viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SoftwareBottomDock(state: BazmoUiState, viewModel: BazmoViewModel) {
    val canSpecialized = state.canAccessSoftwareSpecialized
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalArrangement = Arrangement.spacedBy(9.dp)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
            DockTileButton(
                label = if (canSpecialized) "🎓 آموزش‌ها" else "🔒 آموزش‌ها (نرم‌افزارکار)",
                locked = !canSpecialized,
                modifier = Modifier.weight(1f)
            ) { viewModel.setSwMode(SoftwareSubMode.TUTORIALS) }
            DockTileButton(
                label = if (canSpecialized) "💬 انجمن تخصصی" else "🔒 انجمن تخصصی (نرم‌افزارکار)",
                locked = !canSpecialized,
                modifier = Modifier.weight(1f)
            ) { viewModel.setSwMode(SoftwareSubMode.FORUM) }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(9.dp), modifier = Modifier.fillMaxWidth()) {
            DockTileButton("📋 درخواست‌ها", modifier = Modifier.weight(1f)) { viewModel.setSwMode(SoftwareSubMode.REQUESTS) }
            DockTileButton("🧰 باکس‌ها", modifier = Modifier.weight(1f)) { viewModel.setSwMode(SoftwareSubMode.BOXES) }
        }
    }
}

@Composable
private fun DockTileButton(
    label: String,
    modifier: Modifier = Modifier,
    locked: Boolean = false,
    onClick: () -> Unit
) {
    val gradient = if (locked) {
        Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF64748B)))
    } else {
        Brush.linearGradient(listOf(HeaderBgDeep, HeaderBgLight))
    }
    Box(
        modifier = modifier
            .heightIn(min = 50.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(gradient)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 13.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
