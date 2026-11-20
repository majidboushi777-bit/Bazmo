package com.example.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.HwAdEntity
import com.example.data.LocationPoint
import com.example.data.filterByLocation
import com.example.data.formatToman
import com.example.ui.components.BadgeTone
import com.example.ui.components.BazmoAccentButton
import com.example.ui.components.BazmoBackBar
import com.example.ui.components.BazmoBadge
import com.example.ui.components.BazmoCard
import com.example.ui.components.BazmoDangerButton
import com.example.ui.components.BazmoDropdownSelector
import com.example.ui.components.BazmoEmptyState
import com.example.ui.components.BazmoGoldButton
import com.example.ui.components.BazmoInput
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoPromoBannerGrid
import com.example.ui.components.BazmoSearchBar
import com.example.ui.components.BazmoSectionHeader
import com.example.ui.components.BazmoSoftButton
import com.example.ui.components.BazmoSquareTile
import com.example.ui.components.BazmoSuccessButton
import com.example.ui.components.BazmoWarrantyBox
import com.example.ui.theme.BorderLight
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.BazmoModal
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.BazmoViewModel
import com.example.viewmodel.HardwareSubMode

@Composable
fun HardwareTabScreen(
    state: BazmoUiState,
    viewModel: BazmoViewModel
) {
    val hv = state.session.hwView
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val hwBanners = remember(state.db.banners) { state.bannersForSection("hw") }

    BackHandler(enabled = hv.mode != HardwareSubMode.HOME && hv.mode != HardwareSubMode.FORUM) {
        viewModel.setHwMode(HardwareSubMode.HOME)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cols = if (maxWidth >= 640.dp) 2 else 1
        val tileCols = when {
            maxWidth >= 840.dp -> 6
            maxWidth >= 600.dp -> 4
            else -> 3
        }

        AnimatedContent(
            targetState = hv.mode,
            transitionSpec = {
                fadeIn(tween(180)) togetherWith fadeOut(tween(130))
            },
            label = "hardwareSubModeTransition"
        ) { mode ->
            when (mode) {
                HardwareSubMode.FORUM -> {
                    ForumScreen(
                        board = "hardware",
                        state = state,
                        viewModel = viewModel,
                        onBack = { viewModel.setHwMode(HardwareSubMode.HOME) }
                    )
                }

                HardwareSubMode.AD_DETAIL -> {
                    val ad = remember(state.db.hwAds, hv.adId) {
                        state.db.hwAds.find { it.id == hv.adId }
                    }
                    if (ad == null) {
                        LaunchedEffect(hv.adId) {
                            viewModel.setHwMode(HardwareSubMode.HOME)
                        }
                        return@AnimatedContent
                    }
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "hw_detail_back") {
                            BazmoBackBar(
                                title = "جزئیات آگهی قطعه",
                                onBack = { viewModel.setHwMode(HardwareSubMode.HOME) }
                            )
                        }
                        item(key = "hw_detail_card") {
                            BazmoCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    BazmoBadge(ad.cat, BadgeTone.BLUE)
                                    BazmoBadge("📍 ${ad.city}", BadgeTone.GREEN)
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = ad.title,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Black,
                                    color = NavyDeep
                                )
                                Text(
                                    text = "برند: ${ad.brand} | مدل: ${ad.model} | سری/کد: ${ad.series}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 10.dp))
                                Text(
                                    text = ad.desc,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                BazmoWarrantyBox("🏪 فروشنده: ${ad.seller} | 📞 ${ad.phone}")
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = ad.price.formatToman(),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Black,
                                    color = Navy,
                                    fontSize = 18.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    BazmoPrimaryButton(
                                        text = "🛒 خرید مستقیم",
                                        onClick = { viewModel.addToCart(ad.id, ad.title, ad.price, ad.seller) },
                                        modifier = Modifier.weight(1f)
                                    )
                                    BazmoAccentButton(
                                        text = "💬 چت با فروشنده",
                                        onClick = { viewModel.openModal(BazmoModal.Chat(ad.id)) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                BazmoSuccessButton(
                                    text = "📞 تماس تلفنی با ${ad.phone}",
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${ad.phone}"))
                                        runCatching { context.startActivity(intent) }
                                            .onFailure { viewModel.showToast("برنامه تماس روی این دستگاه یافت نشد") }
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    }
                }

                HardwareSubMode.MY_ADS -> {
                    val mine = remember(state.db.hwAds, state.currentUid) {
                        state.db.hwAds.filter { it.ownerId == state.currentUid }
                    }
                    val mineRows = remember(mine, cols) { mine.chunked(cols) }
                    var editingAd by remember { mutableStateOf<HwAdEntity?>(null) }
                    var newPriceInput by remember { mutableStateOf("") }

                    if (editingAd != null) {
                        val target = editingAd!!
                        AlertDialog(
                            onDismissRequest = { editingAd = null },
                            title = { Text("ویرایش قیمت آگهی") },
                            text = {
                                BazmoInput(
                                    value = newPriceInput,
                                    onValueChange = { newPriceInput = it },
                                    placeholder = "قیمت جدید (تومان)",
                                    keyboardType = KeyboardType.Number
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = {
                                    viewModel.editMyHwAdPrice(target, newPriceInput)
                                    editingAd = null
                                }) { Text("ذخیره") }
                            },
                            dismissButton = {
                                TextButton(onClick = { editingAd = null }) { Text("انصراف") }
                            }
                        )
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "hw_myads_back") {
                            BazmoBackBar(
                                title = "مدیریت آگهی‌های من",
                                onBack = { viewModel.setHwMode(HardwareSubMode.HOME) },
                                trailing = {
                                    BazmoPrimaryButton(
                                        text = "+ آگهی جدید",
                                        onClick = { viewModel.openModal(BazmoModal.NewHwAd) },
                                        compact = true
                                    )
                                }
                            )
                        }
                        if (mine.isEmpty()) {
                            item(key = "hw_myads_empty") { BazmoEmptyState("شما هنوز آگهی ثبت نکرده‌اید.") }
                        } else {
                            itemsIndexed(mineRows, key = { idx, row -> "hw_mine_row_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { ad ->
                                        val (stLabel, tone) = when (ad.status) {
                                            "approved" -> "تایید شده" to BadgeTone.GREEN
                                            "pending" -> "در انتظار تایید" to BadgeTone.GOLD
                                            else -> "رد شده" to BadgeTone.RED
                                        }
                                        BazmoCard(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = ad.title,
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                val secTag = when (ad.section) {
                                                    "mobile" -> "موبایل"
                                                    "acc" -> "جانبی"
                                                    else -> "سخت‌افزار"
                                                }
                                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                                    BazmoBadge(secTag, BadgeTone.BLUE)
                                                    BazmoBadge(stLabel, tone)
                                                }
                                            }
                                            Text(
                                                text = "${ad.price.formatToman()} | ${ad.city}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted,
                                                modifier = Modifier.padding(vertical = 6.dp)
                                            )
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                BazmoSoftButton(
                                                    text = "✏️ تغییر قیمت",
                                                    onClick = {
                                                        newPriceInput = ad.price.toString()
                                                        editingAd = ad
                                                    },
                                                    compact = true
                                                )
                                                BazmoDangerButton(
                                                    text = "🗑️ حذف",
                                                    onClick = { viewModel.deleteMyHwAd(ad.id) },
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

                HardwareSubMode.SPECIAL_REQ -> {
                    val brandOptions = listOf("Apple (آیفون)", "Samsung (سامسونگ)", "Xiaomi (شیائومی)", "Honor / Huawei")
                    val partOptions = remember(state.db.hwCategories) {
                        state.db.hwCategories.map { it.name }.ifEmpty { listOf("تاچ و ال‌سی‌دی") }
                    }
                    var brand by rememberSaveable { mutableStateOf(brandOptions.first()) }
                    var model by rememberSaveable { mutableStateOf("") }
                    var partType by rememberSaveable { mutableStateOf(partOptions.first()) }
                    var series by rememberSaveable { mutableStateOf("") }
                    val myReqs = remember(state.db.hwRequests, state.currentUid) {
                        state.db.hwRequests.filter { it.userId == state.currentUid }
                    }
                    val reqRows = remember(myReqs, cols) { myReqs.chunked(cols) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "hw_req_back") {
                            BazmoBackBar(
                                title = "درخواست تخصصی قطعه",
                                onBack = { viewModel.setHwMode(HardwareSubMode.HOME) }
                            )
                        }
                        item(key = "hw_req_form") {
                            BazmoCard {
                                Text(
                                    text = "🔍 مشخصات دقیق قطعه مورد نیاز",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                                BazmoDropdownSelector(
                                    selectedValue = brand,
                                    options = brandOptions,
                                    onSelect = { brand = it },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                BazmoInput(
                                    value = model,
                                    onValueChange = { model = it },
                                    placeholder = "مدل دقیق (مثلاً iPhone 13 Pro یا Poco X3 Pro)",
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                BazmoDropdownSelector(
                                    selectedValue = partType,
                                    options = partOptions,
                                    onSelect = { partType = it },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                                BazmoInput(
                                    value = series,
                                    onValueChange = { series = it },
                                    placeholder = "سری ساخت / کد قطعه (مثلاً PM8150 یا A2638)",
                                    modifier = Modifier.padding(bottom = 10.dp)
                                )
                                BazmoPrimaryButton(
                                    text = "بررسی موجودی و ثبت درخواست",
                                    onClick = {
                                        val normalizedBrand = brand.substringBefore(" (")
                                        viewModel.checkAndSubmitHwRequest(normalizedBrand, model, partType, series)
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                        item(key = "hw_req_hdr") {
                            BazmoSectionHeader("پیگیری درخواست‌های قطعه من")
                        }
                        if (myReqs.isEmpty()) {
                            item(key = "hw_req_empty") { BazmoEmptyState("درخواستی ثبت نشده است.") }
                        } else {
                            itemsIndexed(reqRows, key = { idx, row -> "hw_req_row_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { req ->
                                        BazmoCard(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Text(
                                                    text = "${req.brand} ${req.model}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.ExtraBold
                                                )
                                                BazmoBadge(req.status, BadgeTone.GOLD)
                                            }
                                            Text(
                                                text = "قطعه: ${req.partType} | سری/کد: ${req.series}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted,
                                                modifier = Modifier.padding(top = 5.dp)
                                            )
                                            if (req.note.isNotBlank()) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                BazmoWarrantyBox("📢 پاسخ ادمین: ${req.note}")
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

                HardwareSubMode.HOME -> {
                    val approvedAds = remember(state.db.hwAds) {
                        state.db.hwAds.filter { it.status == "approved" && (it.section == "hw" || it.section.isBlank()) }
                    }
                    val hwStores = remember(state.db.stores, state.currentUid) {
                        state.db.stores.filter {
                            (it.status == "approved" || it.ownerId == state.currentUid) &&
                                (it.type == "hw" || it.type == "both")
                        }
                    }
                    val hwStoreRows = remember(hwStores, cols) { hwStores.chunked(cols) }
                    val locAds = remember(approvedAds, state.session.province, state.session.city, state.session.region) {
                        filterByLocation(
                            approvedAds,
                            state.session.province,
                            state.session.city,
                            state.session.region
                        ) { LocationPoint(it.province, it.city, it.region) }
                    }
                    val filteredAds = remember(locAds.list, hv.cat, hv.search) {
                        locAds.list.filter { a ->
                            (hv.cat == "همه" || a.cat == hv.cat) &&
                                (hv.search.isBlank() ||
                                    a.title.contains(hv.search, ignoreCase = true) ||
                                    a.model.contains(hv.search, ignoreCase = true) ||
                                    a.series.contains(hv.search, ignoreCase = true))
                        }
                    }
                    val adRows = remember(filteredAds, cols) { filteredAds.chunked(cols) }
                    val catRows = remember(state.db.hwCategories, tileCols) { state.db.hwCategories.chunked(tileCols) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "hw_home_search") {
                            BazmoSearchBar(
                                value = hv.search,
                                onValueChange = { viewModel.updateHwSearch(it) },
                                placeholder = "سرچ تخصصی قطعه، کد آی‌سی یا مدل...",
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                        }
                        item(key = "hw_home_actions") {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(7.dp),
                                modifier = Modifier.padding(bottom = 10.dp)
                            ) {
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                                    BazmoPrimaryButton(
                                        text = "➕ ثبت آگهی قطعه",
                                        onClick = { viewModel.openModal(BazmoModal.NewHwAd) },
                                        compact = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    BazmoSoftButton(
                                        text = "🗂️ مدیریت آگهی‌های من",
                                        onClick = { viewModel.setHwMode(HardwareSubMode.MY_ADS) },
                                        compact = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.fillMaxWidth()) {
                                    BazmoGoldButton(
                                        text = "🧩 درخواست تخصصی قطعه",
                                        onClick = { viewModel.setHwMode(HardwareSubMode.SPECIAL_REQ) },
                                        compact = true,
                                        modifier = Modifier.weight(1f)
                                    )
                                    BazmoAccentButton(
                                        text = if (state.canAccessHardwareForum) "💬 انجمن سخت‌افزار"
                                        else "🔒 انجمن سخت‌افزار (تعمیرکار)",
                                        onClick = { viewModel.setHwMode(HardwareSubMode.FORUM) },
                                        compact = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("hw_forum_button")
                                    )
                                }
                                if (state.canCreateStore) {
                                    BazmoGoldButton(
                                        text = "🏪 ثبت فروشگاه قطعات",
                                        onClick = { viewModel.openModal(BazmoModal.RegisterStore("hw")) },
                                        compact = true,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("hw_register_store_button")
                                    )
                                }
                            }
                        }
                        if (hwBanners.isNotEmpty()) {
                            item(key = "hw_promo_banners") {
                                BazmoPromoBannerGrid(
                                    banners = hwBanners,
                                    onBannerClick = { banner ->
                                        viewModel.openPromoBanner(banner) { url ->
                                            runCatching { uriHandler.openUri(url) }
                                                .onFailure { viewModel.showToast("امکان باز کردن لینک وجود ندارد") }
                                        }
                                    }
                                )
                            }
                        }
                        if (hwStores.isNotEmpty()) {
                            item(key = "hw_stores_hdr") {
                                BazmoSectionHeader("فروشگاه‌های تأمین قطعات")
                            }
                            itemsIndexed(hwStoreRows, key = { idx, row -> "hw_st_row_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { st ->
                                        BazmoCard(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "🏪 ${st.name}",
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = TextPrimary,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                BazmoBadge("⭐ ${st.rating}", BadgeTone.GOLD)
                                            }
                                            Text(
                                                text = "📍 ${st.city} (${st.region}) • 📞 ${st.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                        }
                                    }
                                    repeat(cols - rowItems.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                        item(key = "hw_cats_hdr") {
                            BazmoSectionHeader(
                                title = "دسته‌بندی قطعات",
                                actionLabel = if (hv.cat != "همه") "همه" else null,
                                onAction = if (hv.cat != "همه") {
                                    { viewModel.toggleHwCat(hv.cat) }
                                } else null
                            )
                        }
                        itemsIndexed(catRows, key = { idx, row -> "hw_cat_row_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 9.dp),
                                horizontalArrangement = Arrangement.spacedBy(9.dp)
                            ) {
                                rowItems.forEach { c ->
                                    BazmoSquareTile(
                                        iconEmoji = c.iconEmoji,
                                        title = c.name,
                                        active = hv.cat == c.name,
                                        onClick = { viewModel.toggleHwCat(c.name) },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                repeat(tileCols - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                        item(key = "hw_ads_hdr") {
                            BazmoSectionHeader("آگهی‌های قطعات (${locAds.scope})")
                        }
                        if (filteredAds.isEmpty()) {
                            item(key = "hw_ads_empty") { BazmoEmptyState("آگهی با این فیلتر یافت نشد.") }
                        } else {
                            itemsIndexed(adRows, key = { idx, row -> "hw_ad_row_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { ad ->
                                        BazmoCard(
                                            onClick = { viewModel.setHwMode(HardwareSubMode.AD_DETAIL, ad.id) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                BazmoBadge(ad.cat, BadgeTone.BLUE)
                                                Text(text = "📍 ${ad.city}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = ad.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "مدل: ${ad.model} | کد: ${ad.series}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted,
                                                modifier = Modifier.padding(top = 3.dp, bottom = 8.dp)
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = ad.price.formatToman(),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = Navy
                                                )
                                                BazmoSoftButton(
                                                    text = "مشاهده / چت / خرید",
                                                    onClick = { viewModel.setHwMode(HardwareSubMode.AD_DETAIL, ad.id) },
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
            }
        }
    }
}
