package com.example.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BannerEntity
import com.example.data.BazmoLocations
import com.example.data.BazmoRoles
import com.example.data.formatToman
import com.example.data.toPersianNumber
import com.example.ui.components.BadgeTone
import com.example.ui.components.BazmoBadge
import com.example.ui.components.BazmoBannerImage
import com.example.ui.components.BazmoCard
import com.example.ui.components.cacheBannerBitmap
import com.example.ui.components.BazmoDangerButton
import com.example.ui.components.BazmoDropdownSelector
import com.example.ui.components.BazmoEmptyState
import com.example.ui.components.BazmoFilterChip
import com.example.ui.components.BazmoGoldButton
import com.example.ui.components.BazmoInput
import com.example.ui.components.BazmoOutlineButton
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoSearchBar
import com.example.ui.components.BazmoSectionHeader
import com.example.ui.components.BazmoSoftButton
import com.example.ui.components.BazmoSuccessButton
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavySoft
import com.example.ui.theme.PrimaryFill
import com.example.ui.theme.SoftSurface
import com.example.ui.theme.TextMuted
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.BazmoViewModel
import java.io.ByteArrayOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private data class AdminAdsTileMeta(
    val key: String,
    val emoji: String,
    val label: String,
    val pendingCount: Int,
    val totalCount: Int,
    val badgeTone: Color = DangerRed
)

@Composable
internal fun AdminAdsStoresTab(state: BazmoUiState, viewModel: BazmoViewModel) {
    val activeSec = state.session.adminAdsSection
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var onlyPending by rememberSaveable { mutableStateOf(false) }
    var showAllItems by rememberSaveable(activeSec) { mutableStateOf(false) }

    // Create Store Form States
    var showAddStoreForm by rememberSaveable { mutableStateOf(false) }
    var stName by rememberSaveable { mutableStateOf("") }
    var stProv by rememberSaveable { mutableStateOf("تهران") }
    var stCity by rememberSaveable { mutableStateOf("تهران") }
    var stRegion by rememberSaveable { mutableStateOf("علاءالدین و جمهوری") }
    var stPhone by rememberSaveable { mutableStateOf("") }
    val storeTypes = listOf(
        "both" to "موبایل و جانبی",
        "mobile" to "فقط موبایل",
        "acc" to "فقط جانبی",
        "hw" to "فروشگاه قطعات سخت‌افزار"
    )
    var stType by rememberSaveable { mutableStateOf("both") }

    val hwAds = remember(state.db.hwAds) { state.db.hwAds.filter { it.section == "hw" || it.section.isBlank() } }
    val mobAds = remember(state.db.hwAds) { state.db.hwAds.filter { it.section == "mobile" } }
    val accAds = remember(state.db.hwAds) { state.db.hwAds.filter { it.section == "acc" } }

    val tiles = remember(hwAds, mobAds, accAds, state.db.stores, state.db.deskRentals, state.db.tutorials, state.db.boxes, state.db.banners) {
        listOf(
            AdminAdsTileMeta("hw", "🔧", "سخت‌افزار", hwAds.count { it.status == "pending" }, hwAds.size),
            AdminAdsTileMeta("mobile", "📱", "موبایل", mobAds.count { it.status == "pending" }, mobAds.size),
            AdminAdsTileMeta("acc", "🎧", "جانبی", accAds.count { it.status == "pending" }, accAds.size),
            AdminAdsTileMeta("stores", "🏪", "فروشگاه‌ها", state.db.stores.count { it.status == "pending" }, state.db.stores.size),
            AdminAdsTileMeta("desk", "🪑", "میز کار", state.db.deskRentals.count { it.status == "pending" }, state.db.deskRentals.size),
            AdminAdsTileMeta("tut", "🎓", "آموزش‌ها", state.db.tutorials.count { it.status == "pending" }, state.db.tutorials.size),
            AdminAdsTileMeta("box", "🧰", "باکس‌ها", state.db.boxes.count { it.status == "pending" }, state.db.boxes.size),
            AdminAdsTileMeta("banners", "🖼️", "۴ بنر", 0, state.db.banners.count { it.hasImage }, Color(0xFF1D4ED8))
        )
    }

    val filteredHwAds = remember(hwAds, mobAds, accAds, activeSec, searchQuery, onlyPending) {
        val base = when (activeSec) {
            "hw" -> hwAds
            "mobile" -> mobAds
            "acc" -> accAds
            else -> emptyList()
        }
        base.filter { ad ->
            (!onlyPending || ad.status == "pending") &&
                (searchQuery.isBlank() ||
                    ad.title.contains(searchQuery, ignoreCase = true) ||
                    ad.seller.contains(searchQuery, ignoreCase = true) ||
                    ad.city.contains(searchQuery, ignoreCase = true) ||
                    ad.model.contains(searchQuery, ignoreCase = true))
        }.sortedBy { it.status != "pending" }
    }

    val filteredStores = remember(state.db.stores, searchQuery, onlyPending) {
        state.db.stores.filter { s ->
            (!onlyPending || s.status == "pending") &&
                (searchQuery.isBlank() ||
                    s.name.contains(searchQuery, ignoreCase = true) ||
                    s.city.contains(searchQuery, ignoreCase = true) ||
                    s.province.contains(searchQuery, ignoreCase = true) ||
                    s.phone.contains(searchQuery, ignoreCase = true))
        }.sortedBy { it.status != "pending" }
    }

    val filteredDesks = remember(state.db.deskRentals, searchQuery, onlyPending) {
        state.db.deskRentals.filter { d ->
            (!onlyPending || d.status == "pending") &&
                (searchQuery.isBlank() ||
                    d.title.contains(searchQuery, ignoreCase = true) ||
                    d.storeName.contains(searchQuery, ignoreCase = true) ||
                    d.ownerName.contains(searchQuery, ignoreCase = true) ||
                    d.city.contains(searchQuery, ignoreCase = true))
        }.sortedBy { it.status != "pending" }
    }

    val filteredTuts = remember(state.db.tutorials, searchQuery, onlyPending) {
        state.db.tutorials.filter { t ->
            (!onlyPending || t.status == "pending") &&
                (searchQuery.isBlank() ||
                    t.title.contains(searchQuery, ignoreCase = true) ||
                    t.teacher.contains(searchQuery, ignoreCase = true))
        }.sortedBy { it.status != "pending" }
    }

    val filteredBoxes = remember(state.db.boxes, searchQuery, onlyPending) {
        state.db.boxes.filter { b ->
            (!onlyPending || b.status == "pending") &&
                (searchQuery.isBlank() ||
                    b.name.contains(searchQuery, ignoreCase = true) ||
                    b.seller.contains(searchQuery, ignoreCase = true) ||
                    b.city.contains(searchQuery, ignoreCase = true))
        }.sortedBy { it.status != "pending" }
    }

    val sliceLimit = 8
    val visibleHwAds = remember(filteredHwAds, showAllItems) {
        if (showAllItems) filteredHwAds else filteredHwAds.take(sliceLimit)
    }
    val visibleStores = remember(filteredStores, showAllItems) {
        if (showAllItems) filteredStores else filteredStores.take(sliceLimit)
    }
    val visibleDesks = remember(filteredDesks, showAllItems) {
        if (showAllItems) filteredDesks else filteredDesks.take(sliceLimit)
    }
    val visibleTuts = remember(filteredTuts, showAllItems) {
        if (showAllItems) filteredTuts else filteredTuts.take(sliceLimit)
    }
    val visibleBoxes = remember(filteredBoxes, showAllItems) {
        if (showAllItems) filteredBoxes else filteredBoxes.take(sliceLimit)
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val tileCols = if (maxWidth >= 340.dp) 4 else 2
        val tileRows = remember(tiles, tileCols) { tiles.chunked(tileCols) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
        ) {
            item(key = "ads_tiles_header") {
                BazmoSectionHeader("🛡️ دسته‌بندی مدیریت آگهی‌ها، فروشگاه‌ها و بنرها")
            }

            item(key = "ads_section_tiles") {
                Column(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 10.dp)
                ) {
                    tileRows.forEach { rowItems ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowItems.forEach { meta ->
                                val selected = activeSec == meta.key
                                AdminSectionTileCard(
                                    meta = meta,
                                    selected = selected,
                                    onClick = { viewModel.setAdminAdsSection(meta.key) },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("admin_ads_tile_${meta.key}")
                                )
                            }
                            repeat(tileCols - rowItems.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            if (activeSec == "banners") {
                item(key = "admin_banners_panel") {
                    AdminBannersManager(state = state, viewModel = viewModel)
                }
            } else {
                item(key = "ads_search_filter_bar") {
                    BazmoCard {
                        BazmoSearchBar(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = "جستجو در این بخش (عنوان، نام، شهر)...",
                            testTag = "admin_ads_search_input"
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            val currentTile = tiles.find { it.key == activeSec }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                BazmoFilterChip(
                                    label = "همه (${(currentTile?.totalCount ?: 0).toPersianNumber()})",
                                    selected = !onlyPending,
                                    onClick = { onlyPending = false }
                                )
                                BazmoFilterChip(
                                    label = "در انتظار (${(currentTile?.pendingCount ?: 0).toPersianNumber()})",
                                    selected = onlyPending,
                                    onClick = { onlyPending = true }
                                )
                            }
                            if (activeSec == "stores") {
                                BazmoGoldButton(
                                    text = if (showAddStoreForm) "بستن فرم" else "+ فروشگاه",
                                    onClick = { showAddStoreForm = !showAddStoreForm },
                                    compact = true
                                )
                            }
                        }
                    }
                }

                if (activeSec == "stores" && showAddStoreForm) {
                    item(key = "admin_add_store_card") {
                        BazmoCard(borderColor = GoldAccent, borderWidth = 1.4f) {
                            Text(
                                text = "🏪 ثبت فروشگاه جدید",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = NavyDeep,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            BazmoInput(stName, { stName = it }, "نام فروشگاه", modifier = Modifier.padding(bottom = 8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                                BazmoDropdownSelector(
                                    label = "استان:",
                                    selectedValue = stProv,
                                    options = BazmoLocations.provinces(),
                                    onSelect = { p ->
                                        stProv = p
                                        val firstC = BazmoLocations.citiesFor(p).firstOrNull() ?: ""
                                        stCity = firstC
                                        stRegion = BazmoLocations.regionsFor(p, firstC).firstOrNull() ?: "همه مناطق"
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                                BazmoDropdownSelector(
                                    label = "شهر:",
                                    selectedValue = stCity,
                                    options = BazmoLocations.citiesFor(stProv),
                                    onSelect = { c ->
                                        stCity = c
                                        stRegion = BazmoLocations.regionsFor(stProv, c).firstOrNull() ?: "همه مناطق"
                                    },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            BazmoDropdownSelector(
                                label = "منطقه:",
                                selectedValue = stRegion,
                                options = BazmoLocations.regionsFor(stProv, stCity),
                                onSelect = { stRegion = it },
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                            BazmoInput(stPhone, { stPhone = it }, "تلفن تماس", keyboardType = KeyboardType.Phone, modifier = Modifier.padding(bottom = 8.dp))
                            val curTypeLabel = storeTypes.find { it.first == stType }?.second ?: "موبایل و جانبی"
                            BazmoDropdownSelector(
                                selectedValue = curTypeLabel,
                                options = storeTypes.map { it.second },
                                onSelect = { chosen -> stType = storeTypes.find { it.second == chosen }?.first ?: "both" },
                                modifier = Modifier.padding(bottom = 10.dp)
                            )
                            BazmoPrimaryButton(
                                text = "+ ثبت فروشگاه جدید",
                                onClick = {
                                    if (viewModel.adminAddStore(stName, stProv, stCity, stRegion, stPhone, stType)) {
                                        stName = ""
                                        stPhone = ""
                                        showAddStoreForm = false
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                when (activeSec) {
                    "hw", "mobile", "acc" -> {
                        val secTitle = when (activeSec) {
                            "mobile" -> "📱 آگهی‌های موبایل"
                            "acc" -> "🎧 آگهی‌های لوازم جانبی"
                            else -> "🔧 آگهی‌های سخت‌افزار"
                        }
                        item(key = "sec_hdr_$activeSec") {
                            BazmoSectionHeader(
                                title = secTitle,
                                badgeText = "${visibleHwAds.size.toPersianNumber()} از ${filteredHwAds.size.toPersianNumber()}"
                            )
                        }
                        if (filteredHwAds.isEmpty()) {
                            item(key = "sec_empty_$activeSec") {
                                BazmoEmptyState("موردی در این بخش یافت نشد.")
                            }
                        } else {
                            itemsIndexed(visibleHwAds, key = { idx, ad -> "hwad_${idx}_${ad.id}" }) { _, ad ->
                                val (label, tone) = when (ad.status) {
                                    "approved" -> "تایید شده" to BadgeTone.GREEN
                                    "pending" -> "در انتظار" to BadgeTone.GOLD
                                    else -> "رد شده" to BadgeTone.RED
                                }
                                BazmoCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(ad.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                                        BazmoBadge(label, tone)
                                    }
                                    Text(
                                        text = "${ad.seller} | 📍 ${ad.city} | ${ad.price.formatToman()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        BazmoSuccessButton("✓ تایید", { viewModel.adminSetHwAdStatus(ad, "approved") }, compact = true)
                                        BazmoGoldButton("✕ رد", { viewModel.adminSetHwAdStatus(ad, "rejected") }, compact = true)
                                        BazmoDangerButton("🗑️ حذف", { viewModel.adminDeleteHwAd(ad.id) }, compact = true)
                                    }
                                }
                            }
                            if (filteredHwAds.size > sliceLimit) {
                                item(key = "show_more_$activeSec") {
                                    ShowMoreSliceButton(
                                        total = filteredHwAds.size,
                                        showingAll = showAllItems,
                                        onToggle = { showAllItems = !showAllItems }
                                    )
                                }
                            }
                        }
                    }

                    "stores" -> {
                        item(key = "sec_hdr_stores") {
                            BazmoSectionHeader(
                                title = "🏪 لیست فروشگاه‌ها",
                                badgeText = "${visibleStores.size.toPersianNumber()} از ${filteredStores.size.toPersianNumber()}"
                            )
                        }
                        if (filteredStores.isEmpty()) {
                            item(key = "sec_empty_stores") { BazmoEmptyState("فروشگاهی یافت نشد.") }
                        } else {
                            itemsIndexed(visibleStores, key = { idx, s -> "store_${idx}_${s.id}" }) { _, s ->
                                val (stStatusLabel, stStatusTone) = when (s.status) {
                                    "approved" -> "تاییدشده" to BadgeTone.GREEN
                                    "rejected" -> "ردشده" to BadgeTone.RED
                                    else -> "در انتظار تایید" to BadgeTone.GOLD
                                }
                                BazmoCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Row(
                                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(s.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                                                BazmoBadge(BazmoRoles.storeTypeLabel(s.type), BadgeTone.BLUE)
                                            }
                                            Text(
                                                text = "📍 ${s.province}، ${s.city} (${s.region.ifBlank { "مرکز" }}) | 📞 ${s.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = TextMuted,
                                                modifier = Modifier.padding(top = 3.dp)
                                            )
                                        }
                                        BazmoBadge(stStatusLabel, stStatusTone)
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (s.status != "approved") {
                                            BazmoSuccessButton(
                                                text = "✓ تایید",
                                                onClick = { viewModel.adminSetStoreStatus(s.id, "approved") },
                                                compact = true,
                                                modifier = Modifier.testTag("approve_store_${s.id}")
                                            )
                                        }
                                        if (s.status != "rejected") {
                                            BazmoGoldButton(
                                                text = "✕ رد",
                                                onClick = { viewModel.adminSetStoreStatus(s.id, "rejected") },
                                                compact = true,
                                                modifier = Modifier.testTag("reject_store_${s.id}")
                                            )
                                        }
                                        BazmoDangerButton("حذف", { viewModel.adminDeleteStore(s.id) }, compact = true)
                                    }
                                }
                            }
                            if (filteredStores.size > sliceLimit) {
                                item(key = "show_more_stores") {
                                    ShowMoreSliceButton(
                                        total = filteredStores.size,
                                        showingAll = showAllItems,
                                        onToggle = { showAllItems = !showAllItems }
                                    )
                                }
                            }
                        }
                    }

                    "desk" -> {
                        item(key = "sec_hdr_desk") {
                            BazmoSectionHeader(
                                title = "🪑 آگهی‌های اجاره میز کار",
                                badgeText = "${visibleDesks.size.toPersianNumber()} از ${filteredDesks.size.toPersianNumber()}"
                            )
                        }
                        if (filteredDesks.isEmpty()) {
                            item(key = "sec_empty_desk") { BazmoEmptyState("آگهی میز کاری یافت نشد.") }
                        } else {
                            itemsIndexed(visibleDesks, key = { idx, d -> "desk_${idx}_${d.id}" }) { _, d ->
                                val (dLabel, dTone) = when (d.status) {
                                    "approved" -> "تاییدشده" to BadgeTone.GREEN
                                    "rejected" -> "ردشده" to BadgeTone.RED
                                    else -> "در انتظار تایید" to BadgeTone.GOLD
                                }
                                BazmoCard {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(d.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                            BazmoBadge(d.category, BadgeTone.BLUE)
                                            BazmoBadge(dLabel, dTone)
                                        }
                                    }
                                    Text(
                                        text = "🏪 ${d.storeName.ifBlank { d.ownerName }} | 📍 ${d.city} (${d.region})",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 6.dp)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        if (d.status != "approved") {
                                            BazmoSuccessButton("✓ تایید", { viewModel.adminSetDeskRentalStatus(d.id, "approved") }, compact = true)
                                        }
                                        if (d.status != "rejected") {
                                            BazmoGoldButton("✕ رد", { viewModel.adminSetDeskRentalStatus(d.id, "rejected") }, compact = true)
                                        }
                                        BazmoDangerButton("🗑️ حذف", { viewModel.deleteDeskRental(d.id) }, compact = true)
                                    }
                                }
                            }
                            if (filteredDesks.size > sliceLimit) {
                                item(key = "show_more_desk") {
                                    ShowMoreSliceButton(
                                        total = filteredDesks.size,
                                        showingAll = showAllItems,
                                        onToggle = { showAllItems = !showAllItems }
                                    )
                                }
                            }
                        }
                    }

                    "tut" -> {
                        item(key = "sec_hdr_tut") {
                            BazmoSectionHeader(
                                title = "🎓 آموزش‌های نرم‌افزار",
                                badgeText = "${visibleTuts.size.toPersianNumber()} از ${filteredTuts.size.toPersianNumber()}"
                            )
                        }
                        if (filteredTuts.isEmpty()) {
                            item(key = "sec_empty_tut") { BazmoEmptyState("آموزشی یافت نشد.") }
                        } else {
                            itemsIndexed(visibleTuts, key = { idx, t -> "tut_${idx}_${t.id}" }) { _, t ->
                                BazmoCard {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("🎓 ${t.title}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                                        BazmoBadge(t.status, if (t.status == "approved") BadgeTone.GREEN else BadgeTone.GOLD)
                                    }
                                    Text(
                                        text = "👨‍🏫 ${t.teacher} | ${t.price.formatToman()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        BazmoSuccessButton("✓ تایید", { viewModel.adminApproveTutorial(t) }, compact = true)
                                        BazmoDangerButton("🗑️ حذف", { viewModel.adminDeleteTutorial(t.id) }, compact = true)
                                    }
                                }
                            }
                            if (filteredTuts.size > sliceLimit) {
                                item(key = "show_more_tut") {
                                    ShowMoreSliceButton(
                                        total = filteredTuts.size,
                                        showingAll = showAllItems,
                                        onToggle = { showAllItems = !showAllItems }
                                    )
                                }
                            }
                        }
                    }

                    "box" -> {
                        item(key = "sec_hdr_box") {
                            BazmoSectionHeader(
                                title = "🧰 باکس‌ها و دانگل‌ها",
                                badgeText = "${visibleBoxes.size.toPersianNumber()} از ${filteredBoxes.size.toPersianNumber()}"
                            )
                        }
                        if (filteredBoxes.isEmpty()) {
                            item(key = "sec_empty_box") { BazmoEmptyState("باکسی یافت نشد.") }
                        } else {
                            itemsIndexed(visibleBoxes, key = { idx, b -> "box_${idx}_${b.id}" }) { _, b ->
                                BazmoCard {
                                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Text("🧰 ${b.name}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                                        BazmoBadge(b.status, if (b.status == "approved") BadgeTone.GREEN else BadgeTone.GOLD)
                                    }
                                    Text(
                                        text = "${b.seller} | ${b.city} | ${b.price.formatToman()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        modifier = Modifier.padding(vertical = 4.dp)
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        BazmoSuccessButton("✓ تایید", { viewModel.adminApproveBox(b) }, compact = true)
                                        BazmoDangerButton("🗑️ حذف", { viewModel.adminDeleteBox(b.id) }, compact = true)
                                    }
                                }
                            }
                            if (filteredBoxes.size > sliceLimit) {
                                item(key = "show_more_box") {
                                    ShowMoreSliceButton(
                                        total = filteredBoxes.size,
                                        showingAll = showAllItems,
                                        onToggle = { showAllItems = !showAllItems }
                                    )
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
private fun AdminSectionTileCard(
    meta: AdminAdsTileMeta,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(16.dp)
    Box(
        modifier = modifier
            .heightIn(min = 82.dp)
            .clip(shape)
            .background(if (selected) NavySoft else CardSurface)
            .border(
                width = if (selected) 2.dp else 1.2.dp,
                color = if (selected) PrimaryFill else BorderLight,
                shape = shape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 6.dp, vertical = 10.dp)
    ) {
        if (meta.pendingCount > 0) {
            Surface(
                color = meta.badgeTone,
                shape = RoundedCornerShape(99.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = meta.pendingCount.toPersianNumber(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        } else if (meta.key == "banners" && meta.totalCount > 0) {
            Surface(
                color = PrimaryFill,
                shape = RoundedCornerShape(99.dp),
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Text(
                    text = meta.totalCount.toPersianNumber(),
                    color = Color.White,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = meta.emoji, fontSize = 22.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = meta.label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Black else FontWeight.Bold,
                color = if (selected) PrimaryFill else NavyDeep,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )
            Text(
                text = if (meta.key == "banners") "۱۶ اسلات" else "${meta.totalCount.toPersianNumber()} مورد",
                fontSize = 10.sp,
                color = TextMuted,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ShowMoreSliceButton(
    total: Int,
    showingAll: Boolean,
    onToggle: () -> Unit
) {
    BazmoOutlineButton(
        text = if (showingAll) "نمایش ۸ مورد اخیر" else "نمایش همه (${total.toPersianNumber()} مورد)",
        onClick = onToggle,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
    )
}

@Composable
private fun AdminBannersManager(state: BazmoUiState, viewModel: BazmoViewModel) {
    val bannerSections = remember {
        listOf(
            "mobile" to "📱 موبایل",
            "software" to "💻 نرم‌افزار",
            "acc" to "🎧 جانبی",
            "hw" to "🔧 سخت‌افزار"
        )
    }
    var selectedSection by rememberSaveable { mutableStateOf("mobile") }
    var scopeProvince by rememberSaveable { mutableStateOf("همه استان‌ها") }
    var scopeCity by rememberSaveable { mutableStateOf("همه شهرها") }
    var scopeRegion by rememberSaveable { mutableStateOf("همه مناطق") }

    val isProvAll = BazmoLocations.isAllScope(scopeProvince)
    val normProv = if (isProvAll) "همه استان‌ها" else scopeProvince
    val normCity = if (isProvAll || BazmoLocations.isAllScope(scopeCity)) "همه شهرها" else scopeCity
    val normRegion = if (isProvAll || BazmoLocations.isAllScope(normCity) || BazmoLocations.isAllScope(scopeRegion)) "همه مناطق" else scopeRegion

    val provOptions = remember { listOf("همه استان‌ها") + BazmoLocations.provinces() }
    val cityOptions = remember(normProv) {
        if (BazmoLocations.isAllScope(normProv)) listOf("همه شهرها")
        else listOf("همه شهرها") + BazmoLocations.citiesFor(normProv)
    }
    val regionOptions = remember(normProv, normCity) {
        if (BazmoLocations.isAllScope(normProv) || BazmoLocations.isAllScope(normCity)) listOf("همه مناطق")
        else BazmoLocations.regionsFor(normProv, normCity)
    }

    val scopeKey = remember(normProv, normCity, normRegion) {
        "${BazmoLocations.normalizeZwnj(normProv)}|${BazmoLocations.normalizeZwnj(normCity)}|${BazmoLocations.normalizeZwnj(normRegion)}"
    }

    val existingBanners = remember(state.db.banners, selectedSection, scopeKey) {
        state.db.banners.filter { b ->
            val bProvAll = BazmoLocations.isAllScope(b.province)
            val bCityAll = BazmoLocations.isAllScope(b.city)
            val bRegAll = BazmoLocations.isAllScope(b.region)
            val bProv = if (bProvAll) "همه استان‌ها" else b.province
            val bCity = if (bProvAll || bCityAll) "همه شهرها" else b.city
            val bReg = if (bProvAll || bCityAll || bRegAll) "همه مناطق" else b.region
            b.section == selectedSection &&
                BazmoLocations.normalizeZwnj(bProv) == BazmoLocations.normalizeZwnj(normProv) &&
                BazmoLocations.normalizeZwnj(bCity) == BazmoLocations.normalizeZwnj(normCity) &&
                BazmoLocations.normalizeZwnj(bReg) == BazmoLocations.normalizeZwnj(normRegion)
        }.associateBy { it.slotIndex }
    }

    val configuredScopes = remember(state.db.banners, selectedSection) {
        state.db.banners
            .filter { it.section == selectedSection && it.hasImage }
            .groupBy { b ->
                val pAll = BazmoLocations.isAllScope(b.province)
                val cAll = BazmoLocations.isAllScope(b.city)
                val rAll = BazmoLocations.isAllScope(b.region)
                Triple(
                    if (pAll) "همه استان‌ها" else b.province,
                    if (pAll || cAll) "همه شهرها" else b.city,
                    if (pAll || cAll || rAll) "همه مناطق" else b.region
                )
            }
            .map { (triple, items) -> triple to items.size }
    }

    val approvedStores = remember(state.db.stores) {
        state.db.stores.filter { it.status == "approved" }
    }

    val activeScopeLabel = when {
        BazmoLocations.isAllScope(normProv) -> "🌍 کل کشور (نمایش در سراسر ایران)"
        BazmoLocations.isAllScope(normCity) -> "📍 استان $normProv (تمام شهرها)"
        BazmoLocations.isAllScope(normRegion) -> "📍 $normProv ← شهر $normCity (تمام مناطق)"
        else -> "📍 $normProv ← $normCity ← $normRegion"
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        BazmoSectionHeader("🖼️ مدیریت ۴ بنر تبلیغاتی هر بخش")
        BazmoCard {
            Text(
                text = "برای هر بخش تا ۴ کاشی تبلیغاتی عکس‌دار را به‌صورت جداگانه برای کل کشور، استان، شهر یا منطقه تنظیم کنید:",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                items(bannerSections, key = { it.first }) { (secKey, secLabel) ->
                    BazmoFilterChip(
                        label = secLabel,
                        selected = selectedSection == secKey,
                        onClick = { selectedSection = secKey }
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "🎯 محدوده جغرافیایی بنرها:",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = NavyDeep
                )
                BazmoFilterChip(
                    label = "🌍 کل کشور",
                    selected = BazmoLocations.isAllScope(normProv),
                    onClick = {
                        scopeProvince = "همه استان‌ها"
                        scopeCity = "همه شهرها"
                        scopeRegion = "همه مناطق"
                    }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                BazmoDropdownSelector(
                    label = "استان:",
                    selectedValue = normProv,
                    options = provOptions,
                    onSelect = { p ->
                        scopeProvince = p
                        scopeCity = "همه شهرها"
                        scopeRegion = "همه مناطق"
                    },
                    modifier = Modifier.weight(1f)
                )
                BazmoDropdownSelector(
                    label = "شهر:",
                    selectedValue = normCity,
                    options = cityOptions,
                    onSelect = { c ->
                        scopeCity = c
                        scopeRegion = "همه مناطق"
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            if (!BazmoLocations.isAllScope(normProv) && !BazmoLocations.isAllScope(normCity)) {
                Spacer(modifier = Modifier.height(6.dp))
                BazmoDropdownSelector(
                    label = "منطقه / محله:",
                    selectedValue = normRegion,
                    options = regionOptions,
                    onSelect = { r -> scopeRegion = r }
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            BazmoBadge(activeScopeLabel, BadgeTone.BLUE)
            if (configuredScopes.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(configuredScopes, key = { (t, _) -> "${t.first}|${t.second}|${t.third}" }) { (triple, count) ->
                        val pillLabel = when {
                            BazmoLocations.isAllScope(triple.first) -> "🌍 کل کشور"
                            BazmoLocations.isAllScope(triple.second) -> "📍 استان ${triple.first}"
                            BazmoLocations.isAllScope(triple.third) -> "📍 ${triple.first} · ${triple.second}"
                            else -> "📍 ${triple.second} (${triple.third})"
                        }
                        val isCur = BazmoLocations.normalizeZwnj(triple.first) == BazmoLocations.normalizeZwnj(normProv) &&
                            BazmoLocations.normalizeZwnj(triple.second) == BazmoLocations.normalizeZwnj(normCity) &&
                            BazmoLocations.normalizeZwnj(triple.third) == BazmoLocations.normalizeZwnj(normRegion)
                        BazmoFilterChip(
                            label = "$pillLabel (${count.toPersianNumber()})",
                            selected = isCur,
                            onClick = {
                                scopeProvince = triple.first
                                scopeCity = triple.second
                                scopeRegion = triple.third
                            }
                        )
                    }
                }
            }
        }

        (0..3).forEach { slotIdx ->
            AdminBannerSlotCard(
                section = selectedSection,
                slotIndex = slotIdx,
                scopeProvince = normProv,
                scopeCity = normCity,
                scopeRegion = normRegion,
                scopeKey = scopeKey,
                existing = existingBanners[slotIdx],
                approvedStores = approvedStores,
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun AdminBannerSlotCard(
    section: String,
    slotIndex: Int,
    scopeProvince: String,
    scopeCity: String,
    scopeRegion: String,
    scopeKey: String,
    existing: BannerEntity?,
    approvedStores: List<com.example.data.StoreEntity>,
    viewModel: BazmoViewModel
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var title by rememberSaveable(section, slotIndex, scopeKey, existing?.updatedAt) {
        mutableStateOf(existing?.title.orEmpty())
    }
    var linkType by rememberSaveable(section, slotIndex, scopeKey, existing?.updatedAt) {
        mutableStateOf(existing?.linkType?.ifBlank { "none" } ?: "none")
    }
    var linkTarget by rememberSaveable(section, slotIndex, scopeKey, existing?.updatedAt) {
        mutableStateOf(existing?.linkTarget.orEmpty())
    }
    var pendingBase64 by rememberSaveable(section, slotIndex, scopeKey) { mutableStateOf("") }
    var pendingPreview by remember(section, slotIndex, scopeKey) { mutableStateOf<ImageBitmap?>(null) }
    var isPicking by remember { mutableStateOf(false) }

    val decodedFallbackPreview = remember(pendingBase64, pendingPreview) {
        if (pendingPreview != null) {
            pendingPreview
        } else if (pendingBase64.isNotBlank()) {
            runCatching {
                val bytes = Base64.decode(pendingBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)?.asImageBitmap()
            }.getOrNull()
        } else {
            null
        }
    }

    val pickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            isPicking = true
            scope.launch {
                val encoded = encodeBannerUriToJpegBase64(context, uri)
                isPicking = false
                if (encoded != null) {
                    pendingBase64 = encoded.first
                    pendingPreview = encoded.second
                } else {
                    viewModel.showToast("خطا در پردازش تصویر انتخاب‌شده")
                }
            }
        }
    }

    val linkTypeOptions = remember {
        listOf(
            "none" to "بدون لینک (فقط نمایش تصویر)",
            "url" to "لینک سایت خارجی (URL)",
            "store" to "لینک داخلی به فروشگاه"
        )
    }

    BazmoCard(borderColor = if (existing?.hasImage == true) PrimaryFill.copy(alpha = 0.45f) else BorderLight) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "کاشی تبلیغاتی شماره ${(slotIndex + 1).toPersianNumber()}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = NavyDeep
            )
            BazmoBadge(
                text = if (decodedFallbackPreview != null) "تصویر جدید آماده ذخیره"
                else if (existing?.hasImage == true) "فعال"
                else "خالی",
                tone = if (decodedFallbackPreview != null) BadgeTone.GOLD
                else if (existing?.hasImage == true) BadgeTone.GREEN
                else BadgeTone.BLUE
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        val previewShape = RoundedCornerShape(14.dp)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(2.0f)
                .clip(previewShape)
                .border(1.dp, BorderLight, previewShape)
                .background(SoftSurface),
            contentAlignment = Alignment.Center
        ) {
            val localBmp = decodedFallbackPreview
            if (localBmp != null) {
                Image(
                    bitmap = localBmp,
                    contentDescription = "پیش‌نمایش بنر جدید",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else if (existing?.hasImage == true && existing.imageUrl.isNotBlank()) {
                BazmoBannerImage(
                    imageUrl = existing.imageUrl,
                    contentDescription = existing.title.ifBlank { "بنر تبلیغاتی" },
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(text = "📢", fontSize = 20.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "اینجا جای تبلیغات شماست",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = NavyDeep,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "هنوز تصویری برای این محدوده بارگذاری نشده است",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        BazmoSoftButton(
            text = if (isPicking) "⏳ در حال آماده‌سازی عکس..." else "🖼️ انتخاب تصویر از گالری",
            enabled = !isPicking,
            onClick = {
                pickerLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("pick_banner_${section}_$slotIndex")
        )
        Spacer(modifier = Modifier.height(8.dp))
        BazmoInput(
            value = title,
            onValueChange = { title = it },
            placeholder = "عنوان بنر (اختیاری)"
        )
        Spacer(modifier = Modifier.height(8.dp))
        val curLinkLabel = linkTypeOptions.find { it.first == linkType }?.second ?: linkTypeOptions.first().second
        BazmoDropdownSelector(
            label = "نوع لینک کلیک روی بنر:",
            selectedValue = curLinkLabel,
            options = linkTypeOptions.map { it.second },
            onSelect = { chosen ->
                val nextType = linkTypeOptions.find { it.second == chosen }?.first ?: "none"
                if (nextType != linkType) {
                    linkType = nextType
                    linkTarget = ""
                }
            }
        )

        if (linkType == "url") {
            Spacer(modifier = Modifier.height(8.dp))
            BazmoInput(
                value = linkTarget,
                onValueChange = { linkTarget = it },
                placeholder = "آدرس سایت (مثلاً https://bazmo.ir)",
                keyboardType = KeyboardType.Uri
            )
        } else if (linkType == "store") {
            Spacer(modifier = Modifier.height(8.dp))
            if (approvedStores.isEmpty()) {
                Text(
                    text = "فروشگاه تاییدشده‌ای برای اتصال وجود ندارد.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
            } else {
                val storeOptions = approvedStores.map { "${it.name} (${it.city})" }
                val selectedIdx = approvedStores.indexOfFirst { it.id == linkTarget }.coerceAtLeast(0)
                if (linkTarget.isBlank() && approvedStores.isNotEmpty()) {
                    linkTarget = approvedStores.first().id
                }
                BazmoDropdownSelector(
                    label = "انتخاب فروشگاه مقصد:",
                    selectedValue = storeOptions.getOrElse(selectedIdx) { storeOptions.first() },
                    options = storeOptions,
                    onSelect = { chosen ->
                        val idx = storeOptions.indexOf(chosen)
                        if (idx in approvedStores.indices) {
                            linkTarget = approvedStores[idx].id
                        }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BazmoPrimaryButton(
                text = "💾 ذخیره بنر",
                onClick = {
                    val hasExistingImg = existing?.hasImage == true
                    val capturedBmp = decodedFallbackPreview
                    viewModel.adminUpsertBanner(
                        section = section,
                        slotIndex = slotIndex,
                        province = scopeProvince,
                        city = scopeCity,
                        region = scopeRegion,
                        title = title,
                        linkType = linkType,
                        linkTarget = if (linkType == "none") "" else linkTarget,
                        imageMime = "image/jpeg",
                        imageData = pendingBase64,
                        keepExistingImage = pendingBase64.isBlank() && hasExistingImg
                    ) { savedBanner ->
                        if (capturedBmp != null && savedBanner != null && savedBanner.imageUrl.isNotBlank()) {
                            cacheBannerBitmap(savedBanner.imageUrl, capturedBmp)
                        }
                        pendingBase64 = ""
                        pendingPreview = null
                    }
                },
                compact = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("save_banner_${section}_$slotIndex")
            )
            if (existing != null) {
                BazmoDangerButton(
                    text = "🗑️ حذف",
                    onClick = {
                        pendingBase64 = ""
                        pendingPreview = null
                        viewModel.adminDeleteBanner(existing.id)
                    },
                    compact = true,
                    modifier = Modifier.testTag("delete_banner_${section}_$slotIndex")
                )
            }
        }
    }
}

private suspend fun encodeBannerUriToJpegBase64(
    context: Context,
    uri: Uri
): Pair<String, ImageBitmap>? = withContext(Dispatchers.IO) {
    try {
        val rawBytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: return@withContext null
        if (rawBytes.isEmpty()) return@withContext null

        val bounds = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, bounds)
        var sample = 1
        val maxDim = 1080
        while (bounds.outWidth / sample > maxDim || bounds.outHeight / sample > maxDim) {
            sample *= 2
        }
        val opts = BitmapFactory.Options().apply { inSampleSize = sample.coerceAtLeast(1) }
        val decoded = BitmapFactory.decodeByteArray(rawBytes, 0, rawBytes.size, opts)
            ?: return@withContext null

        val scaled = if (decoded.width > maxDim || decoded.height > maxDim) {
            val ratio = minOf(maxDim.toFloat() / decoded.width, maxDim.toFloat() / decoded.height)
            val w = (decoded.width * ratio).toInt().coerceAtLeast(1)
            val h = (decoded.height * ratio).toInt().coerceAtLeast(1)
            Bitmap.createScaledBitmap(decoded, w, h, true).also {
                if (it !== decoded) decoded.recycle()
            }
        } else {
            decoded
        }

        val out = ByteArrayOutputStream()
        scaled.compress(Bitmap.CompressFormat.JPEG, 82, out)
        val jpegBytes = out.toByteArray()
        val base64 = Base64.encodeToString(jpegBytes, Base64.NO_WRAP)
        base64 to scaled.asImageBitmap()
    } catch (_: Throwable) {
        null
    }
}
