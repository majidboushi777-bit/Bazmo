package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.TutorialEntity
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
import com.example.ui.components.BazmoSubTabBar
import com.example.ui.components.BazmoSubTabItem
import com.example.ui.components.BazmoWarrantyBox
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
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
    val canSpecialized = state.canAccessSoftwareSpecialized
    val subTabs = remember(canSpecialized) {
        listOf(
            BazmoSubTabItem("home", if (canSpecialized) "آموزش‌ها" else "🔒 آموزش‌ها"),
            BazmoSubTabItem("boxes", "🧰 باکس و دانگل"),
            BazmoSubTabItem("req", "📋 درخواست‌ها"),
            BazmoSubTabItem("forum", if (canSpecialized) "💬 انجمن" else "🔒 انجمن")
        )
    }
    val selectedSubKey = when (sw.mode) {
        SoftwareSubMode.BOXES -> "boxes"
        SoftwareSubMode.REQUESTS -> "req"
        SoftwareSubMode.FORUM -> "forum"
        SoftwareSubMode.HOME, SoftwareSubMode.PROBLEM, SoftwareSubMode.TUTORIALS -> "home"
    }
    val onSelectSubTab: (String) -> Unit = { key ->
        when (key) {
            "home" -> viewModel.setSwMode(SoftwareSubMode.HOME)
            "boxes" -> viewModel.setSwMode(SoftwareSubMode.BOXES)
            "req" -> viewModel.setSwMode(SoftwareSubMode.REQUESTS)
            "forum" -> viewModel.setSwMode(SoftwareSubMode.FORUM)
        }
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
                        onBack = { viewModel.setSwMode(SoftwareSubMode.HOME) }
                    )
                }

                SoftwareSubMode.PROBLEM, SoftwareSubMode.TUTORIALS -> {
                    val prob = remember(state.db.swProblems, sw.problemId) {
                        state.db.swProblems.find { it.id == sw.problemId }
                    }
                    val list = remember(state.db.tutorials, canSpecialized, state.currentUid, sw.problemId, sw.search) {
                        if (canSpecialized) {
                            state.db.tutorials.filter { t ->
                                (t.status == "approved" || t.ownerId == state.currentUid) &&
                                    (sw.problemId == null || t.problemId == sw.problemId) &&
                                    (sw.search.isBlank() ||
                                        t.title.contains(sw.search, ignoreCase = true) ||
                                        t.teacher.contains(sw.search, ignoreCase = true))
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
                        item(key = "sw_tut_subtabs") {
                            BazmoSubTabBar(
                                tabs = subTabs,
                                selectedKey = selectedSubKey,
                                onSelect = onSelectSubTab
                            )
                        }
                        item(key = "sw_tut_back") {
                            BazmoBackBar(
                                title = when {
                                    !canSpecialized && prob != null -> "${prob.iconEmoji} ${prob.title}"
                                    prob != null -> "آموزش‌های: ${prob.title}"
                                    else -> "آموزش‌های نرم‌افزار"
                                },
                                onBack = { viewModel.setSwMode(SoftwareSubMode.HOME) }
                            )
                        }
                        if (!canSpecialized) {
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
                                BazmoCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
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
                                            SoftwareTutorialCard(
                                                tutorial = t,
                                                onBuy = { viewModel.addToCart(t.id, t.title, t.price, t.teacher) },
                                                modifier = Modifier.weight(1f)
                                            )
                                        }
                                        repeat(cols - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
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
                        item(key = "sw_req_subtabs") {
                            BazmoSubTabBar(
                                tabs = subTabs,
                                selectedKey = selectedSubKey,
                                onSelect = onSelectSubTab
                            )
                        }
                        item(key = "sw_req_banners") {
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
                        item(key = "sw_req_search") {
                            BazmoCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
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
                                                BazmoBadge(
                                                    text = r.status,
                                                    tone = if (r.reply.isNotBlank()) BadgeTone.GREEN else BadgeTone.GOLD
                                                )
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
                    }
                }

                SoftwareSubMode.BOXES -> {
                    val boxes = remember(state.db.boxes, state.currentUid, sw.search) {
                        state.db.boxes.filter { b ->
                            (b.status == "approved" || b.ownerId == state.currentUid) &&
                                (sw.search.isBlank() ||
                                    b.name.contains(sw.search, ignoreCase = true) ||
                                    b.seller.contains(sw.search, ignoreCase = true) ||
                                    b.city.contains(sw.search, ignoreCase = true))
                        }
                    }
                    val boxRows = remember(boxes, cols) { boxes.chunked(cols) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "sw_box_subtabs") {
                            BazmoSubTabBar(
                                tabs = subTabs,
                                selectedKey = selectedSubKey,
                                onSelect = onSelectSubTab
                            )
                        }
                        item(key = "sw_box_banners") {
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
                        item(key = "sw_box_search") {
                            BazmoCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
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
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                BazmoBadge("🧰 ${b.condition}", BadgeTone.BLUE)
                                                Row(
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    BazmoBadge(
                                                        text = if (b.status == "approved") "فعال" else "در انتظار تایید",
                                                        tone = if (b.status == "approved") BadgeTone.GREEN else BadgeTone.GOLD
                                                    )
                                                    Text(text = "📍 ${b.city}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                                }
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
                    }
                }

                SoftwareSubMode.HOME -> {
                    val filteredProblems = remember(state.db.swProblems, sw.search) {
                        state.db.swProblems.filter { p ->
                            sw.search.isBlank() || p.title.contains(sw.search, ignoreCase = true)
                        }
                    }
                    val allTutorials = remember(state.db.tutorials, canSpecialized, state.currentUid, sw.search) {
                        if (!canSpecialized) emptyList()
                        else state.db.tutorials.filter { t ->
                            (t.status == "approved" || t.ownerId == state.currentUid) &&
                                (sw.search.isBlank() ||
                                    t.title.contains(sw.search, ignoreCase = true) ||
                                    t.teacher.contains(sw.search, ignoreCase = true))
                        }
                    }
                    val rows = remember(filteredProblems, tileCols) {
                        filteredProblems.chunked(tileCols)
                    }
                    val allTutRows = remember(allTutorials, cols) {
                        allTutorials.chunked(cols)
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "sw_home_subtabs") {
                            BazmoSubTabBar(
                                tabs = subTabs,
                                selectedKey = selectedSubKey,
                                onSelect = onSelectSubTab
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
                        if (!canSpecialized) {
                            item(key = "sw_home_notice") {
                                BazmoCard {
                                    BazmoWarrantyBox(
                                        text = "🔒 آموزش‌ها و انجمن تخصصی نرم‌افزار فقط برای نقش «نرم‌افزارکار» فعال است.\nبخش درخواست‌ها و باکس و دانگل برای همه همکاران آزاد است.",
                                        modifier = Modifier.padding(bottom = 10.dp)
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        BazmoPrimaryButton(
                                            text = "📋 درخواست‌ها",
                                            onClick = { viewModel.setSwMode(SoftwareSubMode.REQUESTS) },
                                            compact = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                        BazmoGoldButton(
                                            text = "🧰 باکس و دانگل",
                                            onClick = { viewModel.setSwMode(SoftwareSubMode.BOXES) },
                                            compact = true,
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        } else {
                            item(key = "sw_home_toolbar") {
                                BazmoCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        BazmoSearchBar(
                                            value = sw.search,
                                            onValueChange = { viewModel.updateSwSearch(it) },
                                            placeholder = "جستجوی مشکل نرم‌افزاری یا آموزش...",
                                            modifier = Modifier.weight(1f)
                                        )
                                        BazmoGoldButton(
                                            text = "+ فروش آموزش",
                                            onClick = { viewModel.openModal(BazmoModal.SellTutorial) },
                                            compact = true
                                        )
                                    }
                                }
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
                                        title = if (canSpecialized) prob.title else "🔒 ${prob.title}",
                                        onClick = { viewModel.setSwMode(SoftwareSubMode.PROBLEM, prob.id) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(tileCols - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }

                        if (canSpecialized && allTutorials.isNotEmpty()) {
                            item(key = "sw_all_tut_hdr") {
                                BazmoSectionHeader("همه آموزش‌های تخصصی")
                            }
                            itemsIndexed(allTutRows, key = { idx, row -> "sw_all_tut_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { t ->
                                        SoftwareTutorialCard(
                                            tutorial = t,
                                            onBuy = { viewModel.addToCart(t.id, t.title, t.price, t.teacher) },
                                            modifier = Modifier.weight(1f)
                                        )
                                    }
                                    repeat(cols - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SoftwareTutorialCard(
    tutorial: TutorialEntity,
    onBuy: () -> Unit,
    modifier: Modifier = Modifier
) {
    BazmoCard(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BazmoBadge("👨🏫 ${tutorial.teacher}", BadgeTone.BLUE)
            if (tutorial.status == "approved") {
                BazmoBadge("⭐ ${tutorial.rating} • 📍 ${tutorial.city}", BadgeTone.GREEN)
            } else {
                BazmoBadge("در انتظار تایید", BadgeTone.GOLD)
            }
        }
        Spacer(modifier = Modifier.height(7.dp))
        Text(
            text = tutorial.title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(6.dp))
        BazmoWarrantyBox("📌 شرایط: ${tutorial.terms}")
        Spacer(modifier = Modifier.height(10.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = tutorial.price.formatToman(),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Black,
                color = Navy
            )
            BazmoPrimaryButton(
                text = "خرید آموزش",
                onClick = onBuy,
                compact = true
            )
        }
    }
}
