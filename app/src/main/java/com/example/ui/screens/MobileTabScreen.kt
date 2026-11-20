package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.LocationPoint
import com.example.data.filterByLocation
import com.example.data.formatToman
import com.example.data.toPersianNumber
import com.example.ui.components.BadgeTone
import com.example.ui.components.BazmoBackBar
import com.example.ui.components.BazmoBadge
import com.example.ui.components.BazmoCard
import com.example.ui.components.BazmoDropdownSelector
import com.example.ui.components.BazmoEmptyState
import com.example.ui.components.BazmoGoldButton
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoPromoBannerGrid
import com.example.ui.components.BazmoSearchBar
import com.example.ui.components.BazmoSectionHeader
import com.example.ui.components.BazmoSoftButton
import com.example.ui.components.BazmoWarrantyBox
import androidx.compose.foundation.layout.heightIn
import androidx.compose.ui.text.style.TextOverflow
import com.example.data.BazmoLocations
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardSurface
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HeaderBgDeep
import com.example.ui.theme.HeaderBgLight
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyLight
import com.example.ui.theme.PrimaryFill
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.BazmoModal
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.BazmoViewModel
import com.example.viewmodel.MobileSubMode

@Composable
fun MobileTabScreen(
    state: BazmoUiState,
    viewModel: BazmoViewModel
) {
    val mv = state.session.mobileView
    val uriHandler = LocalUriHandler.current
    val mobileBanners = remember(state.db.banners) { state.bannersForSection("mobile") }
    val storesById = remember(state.db.stores) {
        state.db.stores.associateBy { it.id }
    }
    val offersByMobileId = remember(state.db.storeMobiles, storesById) {
        state.db.storeMobiles
            .filter { sm ->
                val st = storesById[sm.storeId]
                st == null || st.status == "approved"
            }
            .groupBy { it.mobileId }
    }
    val mobilesById = remember(state.db.mobiles) {
        state.db.mobiles.associateBy { it.id }
    }
    val brandCounts = remember(state.db.mobiles) {
        state.db.mobiles.groupingBy { it.brand }.eachCount()
    }

    BackHandler(enabled = mv.mode != MobileSubMode.LIST) {
        if (mv.mode == MobileSubMode.STORE) viewModel.backMobileToModel()
        else viewModel.backMobileToList()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cols = if (maxWidth >= 640.dp) 2 else 1

        AnimatedContent(
            targetState = mv.mode,
            transitionSpec = {
                fadeIn(tween(180)) togetherWith fadeOut(tween(130))
            },
            label = "mobileSubModeTransition"
        ) { mode ->
            when (mode) {
                MobileSubMode.STORE -> {
                    val stId = mv.storeId
                    if (stId == null) {
                        LaunchedEffect(Unit) {
                            viewModel.backMobileToList()
                        }
                        return@AnimatedContent
                    }
                    val store = state.getStore(stId)
                    val storeOffers = remember(state.db.storeMobiles, store.id) {
                        state.db.storeMobiles.filter { it.storeId == store.id }
                    }
                    val offerRows = remember(storeOffers, cols) { storeOffers.chunked(cols) }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "store_back") {
                            BazmoBackBar(
                                title = "فروشگاه ${store.name}",
                                onBack = { viewModel.backMobileToModel() }
                            )
                        }
                        item(key = "store_hero") {
                            Surface(
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 12.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .background(Brush.linearGradient(listOf(HeaderBgDeep, HeaderBgLight)))
                                        .padding(15.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "🏪 ${store.name}",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = Color.White
                                        )
                                        BazmoBadge("⭐ ${store.rating}", BadgeTone.GOLD)
                                    }
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "📍 ${store.address}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFDBEAFE)
                                    )
                                    Text(
                                        text = "🕒 ${store.hours} | 📞 ${store.phone}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFFDBEAFE),
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                        item(key = "store_offers_hdr") {
                            BazmoSectionHeader("موبایل‌های موجود در این فروشگاه")
                        }
                        itemsIndexed(offerRows, key = { idx, row -> "mob_st_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { sm ->
                                    val mob = mobilesById[sm.mobileId]
                                    if (mob != null) {
                                        BazmoCard(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    BazmoBadge(mob.brand, BadgeTone.BLUE)
                                                    Spacer(modifier = Modifier.height(5.dp))
                                                    Text(
                                                        text = mob.model,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = TextPrimary
                                                    )
                                                    Text(
                                                        text = mob.specs,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = TextMuted
                                                    )
                                                }
                                                Text(text = mob.imgEmoji, fontSize = 32.sp)
                                            }
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Text(
                                                text = sm.price.formatToman(),
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Black,
                                                color = Navy
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            BazmoWarrantyBox("🛡️ ${sm.info}")
                                            Spacer(modifier = Modifier.height(10.dp))
                                            BazmoPrimaryButton(
                                                text = "🛒 افزودن به سبد خرید",
                                                onClick = {
                                                    viewModel.addToCart(sm.id, mob.model, sm.price, store.name)
                                                },
                                                modifier = Modifier.fillMaxWidth()
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

                MobileSubMode.MODEL -> {
                    val mobId = mv.modelId
                    val mob = mobId?.let { mobilesById[it] }
                    if (mob == null) {
                        LaunchedEffect(mobId) {
                            viewModel.backMobileToList()
                        }
                        return@AnimatedContent
                    }

                    val allOffers = remember(offersByMobileId, mob.id) {
                        offersByMobileId[mob.id].orEmpty()
                    }
                    val locOffers = remember(allOffers, state.session.province, state.session.city, state.session.region, state.db.stores) {
                        filterByLocation(
                            allOffers,
                            state.session.province,
                            state.session.city,
                            state.session.region
                        ) { sm ->
                            val st = state.getStore(sm.storeId)
                            LocationPoint(st.province, st.city, st.region)
                        }
                    }
                    val sortedOffers = remember(locOffers.list) { locOffers.list.sortedBy { it.price } }
                    val offerRows = remember(sortedOffers, cols) { sortedOffers.chunked(cols) }
                    val minOfferPrice = sortedOffers.firstOrNull()?.price ?: 0L

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "model_back") {
                            BazmoBackBar(title = mob.brand, onBack = { viewModel.backMobileToList() })
                        }
                        item(key = "model_card") {
                            BazmoCard {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        BazmoBadge(mob.brand, BadgeTone.BLUE)
                                        Spacer(modifier = Modifier.height(6.dp))
                                        Text(
                                            text = mob.model,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Black,
                                            color = NavyDeep
                                        )
                                        Text(
                                            text = mob.specs,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextMuted,
                                            modifier = Modifier.padding(top = 4.dp)
                                        )
                                    }
                                    Text(text = mob.imgEmoji, fontSize = 40.sp)
                                }
                            }
                        }
                        item(key = "model_prices_hdr") {
                            BazmoSectionHeader(
                                title = "قیمت فروشگاه‌ها (${locOffers.scope})",
                                badgeText = "${sortedOffers.size.toPersianNumber()} فروشنده"
                            )
                        }
                        itemsIndexed(offerRows, key = { idx, row -> "mob_mod_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                rowItems.forEach { sm ->
                                    val st = state.getStore(sm.storeId)
                                    val isBest = sm.price == minOfferPrice
                                    BazmoCard(
                                        borderColor = if (isBest) SuccessGreen else BorderLight,
                                        borderWidth = if (isBest) 2f else 1f,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = "🏪 ${st.name}",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = TextPrimary
                                                    )
                                                    if (isBest) {
                                                        BazmoBadge("بهترین قیمت", BadgeTone.GREEN)
                                                    }
                                                }
                                                Text(
                                                    text = "📍 ${st.city} (${st.region.ifBlank { "مرکز" }}) | ⭐ ${st.rating}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = TextMuted,
                                                    modifier = Modifier.padding(top = 2.dp)
                                                )
                                            }
                                            BazmoSoftButton(
                                                text = "ورود به فروشگاه",
                                                onClick = { viewModel.openMobileStore(st.id) },
                                                compact = true
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = sm.price.formatToman(),
                                            style = MaterialTheme.typography.titleSmall,
                                            fontWeight = FontWeight.Black,
                                            color = Navy
                                        )
                                        Spacer(modifier = Modifier.height(6.dp))
                                        BazmoWarrantyBox("📦 ${sm.info}")
                                        Spacer(modifier = Modifier.height(10.dp))
                                        BazmoPrimaryButton(
                                            text = "افزودن به سبد خرید",
                                            onClick = { viewModel.addToCart(sm.id, mob.model, sm.price, st.name) },
                                            compact = true,
                                            modifier = Modifier.fillMaxWidth()
                                        )
                                    }
                                }
                                repeat(cols - rowItems.size) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                }

                MobileSubMode.LIST -> {
                    val brands = remember(state.db.mobiles) {
                        listOf("همه") + state.db.mobiles.map { it.brand }.distinct()
                    }
                    val budgetOptions = listOf(
                        "فیلتر حداکثر بودجه (همه قیمت‌ها)" to null,
                        "تا ۲۵ میلیون تومان" to 25_000_000L,
                        "تا ۵۰ میلیون تومان" to 50_000_000L,
                        "تا ۸۵ میلیون تومان" to 85_000_000L
                    )
                    val selectedBudgetLabel = budgetOptions.find { it.second == mv.maxPrice }?.first
                        ?: budgetOptions.first().first

                    val filtered = remember(state.db.mobiles, offersByMobileId, mv.search, mv.brand, mv.maxPrice) {
                        state.db.mobiles.filter { m ->
                            val matchSearch = mv.search.isBlank() ||
                                m.model.contains(mv.search, ignoreCase = true) ||
                                m.brand.contains(mv.search, ignoreCase = true)
                            val matchBrand = mv.brand == "همه" || m.brand == mv.brand
                            val offers = offersByMobileId[m.id].orEmpty()
                            val minOffer = offers.minOfOrNull { it.price } ?: 0L
                            val matchPrice = mv.maxPrice == null || minOffer <= mv.maxPrice
                            matchSearch && matchBrand && matchPrice
                        }
                    }
                    val groupedModels = remember(filtered) {
                        filtered.groupBy { it.brand }
                    }

                    val mobileUserAds = remember(state.db.hwAds, mv.search) {
                        state.db.hwAds.filter { ad ->
                            ad.status == "approved" && ad.section == "mobile" &&
                                (mv.search.isBlank() ||
                                    ad.title.contains(mv.search, ignoreCase = true) ||
                                    ad.brand.contains(mv.search, ignoreCase = true) ||
                                    ad.model.contains(mv.search, ignoreCase = true))
                        }
                    }
                    val mobileUserAdRows = remember(mobileUserAds, cols) { mobileUserAds.chunked(cols) }
                    val approvedMobileStores = remember(state.db.stores) {
                        state.db.stores.filter { it.status == "approved" && (it.type == "mobile" || it.type == "both") }
                    }
                    val locStores = remember(approvedMobileStores, state.session.province, state.session.city, state.session.region) {
                        filterByLocation(
                            approvedMobileStores,
                            state.session.province,
                            state.session.city,
                            state.session.region
                        ) { LocationPoint(it.province, it.city, it.region) }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        item(key = "mobile_search_box") {
                            BazmoCard {
                                BazmoSearchBar(
                                    value = mv.search,
                                    onValueChange = { viewModel.updateMobileSearch(it) },
                                    placeholder = "سرچ مدل گوشی یا برند..."
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                BazmoDropdownSelector(
                                    selectedValue = selectedBudgetLabel,
                                    options = budgetOptions.map { it.first },
                                    onSelect = { chosen ->
                                        val priceVal = budgetOptions.find { it.first == chosen }?.second
                                        viewModel.updateMobileMaxPrice(priceVal)
                                    }
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    BazmoPrimaryButton(
                                        text = "➕ ثبت آگهی موبایل",
                                        onClick = { viewModel.openModal(BazmoModal.NewMarketAd("mobile")) },
                                        compact = true,
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("mobile_new_ad_button")
                                    )
                                    if (state.canCreateStore) {
                                        BazmoGoldButton(
                                            text = "🏪 ثبت فروشگاه",
                                            onClick = { viewModel.openModal(BazmoModal.RegisterStore("mobile")) },
                                            compact = true,
                                            modifier = Modifier
                                                .weight(1f)
                                                .testTag("mobile_register_store_button")
                                        )
                                    }
                                }
                            }
                        }

                        if (mobileBanners.isNotEmpty()) {
                            item(key = "mobile_promo_banners") {
                                BazmoPromoBannerGrid(
                                    banners = mobileBanners,
                                    onBannerClick = { banner ->
                                        viewModel.openPromoBanner(banner) { url ->
                                            runCatching { uriHandler.openUri(url) }
                                                .onFailure { viewModel.showToast("امکان باز کردن لینک وجود ندارد") }
                                        }
                                    }
                                )
                            }
                        }

                        item(key = "mobile_brand_strip") {
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(9.dp),
                                contentPadding = PaddingValues(bottom = 10.dp)
                            ) {
                                items(brands, key = { it }) { brand ->
                                    val isSelected = mv.brand == brand
                                    val count = if (brand == "همه") state.db.mobiles.size
                                    else brandCounts[brand] ?: 0
                                    val fillCol = PrimaryFill
                                    val cardColor by animateColorAsState(
                                        targetValue = if (isSelected) fillCol else CardSurface,
                                        animationSpec = tween(150),
                                        label = "brandBg"
                                    )
                                    val borderColor by animateColorAsState(
                                        targetValue = if (isSelected) fillCol else BorderLight,
                                        animationSpec = tween(150),
                                        label = "brandBorder"
                                    )
                                    Surface(
                                        shape = RoundedCornerShape(15.dp),
                                        color = cardColor,
                                        border = BorderStroke(1.5.dp, borderColor),
                                        modifier = Modifier
                                            .width(112.dp)
                                            .clip(RoundedCornerShape(15.dp))
                                            .clickable { viewModel.updateMobileBrand(brand) }
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(10.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = brand,
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.Black,
                                                color = if (isSelected) Color.White else TextPrimary
                                            )
                                            Text(
                                                text = "${count.toPersianNumber()} مدل",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = if (isSelected) Color(0xFFD6E4FA) else TextMuted,
                                                modifier = Modifier.padding(top = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (locStores.list.isNotEmpty()) {
                            item(key = "mobile_stores_hdr") {
                                BazmoSectionHeader(
                                    title = "🏪 عمده‌فروشان و بنکداران (${locStores.scope})",
                                    badgeText = "${locStores.list.size.toPersianNumber()} فروشگاه"
                                )
                            }
                            item(key = "mobile_stores_strip") {
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    contentPadding = PaddingValues(bottom = 10.dp)
                                ) {
                                    items(locStores.list, key = { it.id }) { st ->
                                        Column(
                                            modifier = Modifier
                                                .width(176.dp)
                                                .clip(RoundedCornerShape(16.dp))
                                                .background(Brush.linearGradient(listOf(HeaderBgDeep, HeaderBgLight)))
                                                .clickable { viewModel.openMobileStore(st.id) }
                                                .padding(12.dp)
                                        ) {
                                            Text(
                                                text = "🏪 ${st.name}",
                                                style = MaterialTheme.typography.labelLarge,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = Color.White,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "📍 ${st.city} • ⭐ ${st.rating}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color(0xFFDBEAFE),
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                modifier = Modifier.padding(top = 4.dp)
                                            )
                                            Text(
                                                text = "مشاهده لیست قیمت و موجودی",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = GoldAccent,
                                                maxLines = 1,
                                                modifier = Modifier.padding(top = 3.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        if (groupedModels.isEmpty()) {
                            item(key = "mobile_empty") { BazmoEmptyState("موردی یافت نشد.") }
                        } else {
                            groupedModels.forEach { (br, brandModels) ->
                                item(key = "hdr_$br") {
                                    BazmoSectionHeader(
                                        title = "برند $br",
                                        badgeText = "${brandModels.size.toPersianNumber()} مدل"
                                    )
                                }
                                val brandRows = brandModels.chunked(cols)
                                itemsIndexed(brandRows, key = { idx, row -> "mob_${br}_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowItems.forEach { m ->
                                            val offers = offersByMobileId[m.id].orEmpty()
                                            val minP = offers.minOfOrNull { it.price } ?: 0L
                                            BazmoCard(
                                                onClick = { viewModel.openMobileModel(m.id) },
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    horizontalArrangement = Arrangement.SpaceBetween,
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Column(modifier = Modifier.weight(1f)) {
                                                        Text(
                                                            text = m.model,
                                                            style = MaterialTheme.typography.titleSmall,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = TextPrimary
                                                        )
                                                        Text(
                                                            text = m.specs,
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = TextMuted,
                                                            modifier = Modifier.padding(vertical = 3.dp)
                                                        )
                                                        Text(
                                                            text = "شروع از ${minP.formatToman()} • ${offers.size.toPersianNumber()} فروشگاه",
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.ExtraBold,
                                                            color = Navy
                                                        )
                                                    }
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(text = m.imgEmoji, fontSize = 32.sp)
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

                        if (mobileUserAds.isNotEmpty()) {
                            item(key = "hdr_mobile_user_ads") {
                                BazmoSectionHeader(
                                    title = "آگهی‌های ثبت‌شده همکاران",
                                    badgeText = "${mobileUserAds.size.toPersianNumber()} آگهی"
                                )
                            }
                            itemsIndexed(mobileUserAdRows, key = { idx, row -> "mob_ad_row_${idx}_" + row.joinToString("_") { it.id } }) { _, rowItems ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    rowItems.forEach { ad ->
                                        BazmoCard(modifier = Modifier.weight(1f)) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                BazmoBadge(ad.cat, BadgeTone.BLUE)
                                                Text("📍 ${ad.city}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                            }
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = ad.title,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = TextPrimary
                                            )
                                            Text(
                                                text = "${ad.brand} ${ad.model} • فروشنده: ${ad.seller}",
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
                                                    text = ad.price.formatToman(),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    fontWeight = FontWeight.Black,
                                                    color = Navy
                                                )
                                                BazmoSoftButton(
                                                    text = "💬 چت / جزئیات",
                                                    onClick = { viewModel.openModal(BazmoModal.Chat(ad.id)) },
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
