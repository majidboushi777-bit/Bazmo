package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.LocationPoint
import com.example.data.filterByLocation
import com.example.data.formatToman
import com.example.data.toPersianNumber
import com.example.ui.components.BadgeTone
import com.example.ui.components.BazmoBadge
import com.example.ui.components.BazmoCard
import com.example.ui.components.BazmoEmptyState
import com.example.ui.components.BazmoFilterChip
import com.example.ui.components.BazmoGoldButton
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoSearchBar
import com.example.ui.components.BazmoSectionHeader
import com.example.ui.components.BazmoSoftButton
import com.example.ui.components.BazmoWarrantyBox
import com.example.ui.theme.GoldAccent
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

@Composable
fun AccessoriesTabScreen(
    state: BazmoUiState,
    viewModel: BazmoViewModel
) {
    val av = state.session.accView
    val cats = remember(state.db.accessories) {
        listOf("همه") + state.db.accessories.map { it.cat }.distinct()
    }
    val approvedStoreIds = remember(state.db.stores) {
        state.db.stores.filter { it.status == "approved" }.map { it.id }.toSet()
    }
    val storesById = remember(state.db.stores) {
        state.db.stores.associateBy { it.id }
    }
    val accStoresAll = remember(state.db.stores) {
        state.db.stores.filter { it.status == "approved" && (it.type == "acc" || it.type == "both") }
    }
    val locStores = remember(accStoresAll, state.session.province, state.session.city, state.session.region) {
        filterByLocation(
            accStoresAll,
            state.session.province,
            state.session.city,
            state.session.region
        ) { LocationPoint(it.province, it.city, it.region) }
    }

    val inquiriesByAccId = remember(state.db.inquiries, state.currentUid) {
        state.db.inquiries.filter { it.userId == state.currentUid }.associateBy { it.accId }
    }

    val products = remember(state.db.accessories, storesById, approvedStoreIds, av.cat, av.selectedStoreId, av.search) {
        state.db.accessories.filter { a ->
            val st = storesById[a.storeId]
            val storeApproved = st == null || approvedStoreIds.contains(st.id)
            val matchCat = av.cat == "همه" || a.cat == av.cat
            val matchStore = av.selectedStoreId == null || a.storeId == av.selectedStoreId
            val matchSearch = av.search.isBlank() ||
                a.name.contains(av.search, ignoreCase = true) ||
                a.cat.contains(av.search, ignoreCase = true)
            storeApproved && matchCat && matchStore && matchSearch
        }
    }

    val accUserAds = remember(state.db.hwAds, av.search) {
        state.db.hwAds.filter { ad ->
            ad.status == "approved" && ad.section == "acc" &&
                (av.search.isBlank() ||
                    ad.title.contains(av.search, ignoreCase = true) ||
                    ad.cat.contains(av.search, ignoreCase = true))
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val cols = if (maxWidth >= 640.dp) 2 else 1
        val productRows = remember(products, cols) { products.chunked(cols) }
        val accAdRows = remember(accUserAds, cols) { accUserAds.chunked(cols) }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item(key = "acc_search_actions") {
                BazmoSearchBar(
                    value = av.search,
                    onValueChange = { viewModel.updateAccSearch(it) },
                    placeholder = "جستجوی لوازم جانبی...",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    BazmoPrimaryButton(
                        text = "➕ ثبت آگهی جانبی",
                        onClick = { viewModel.openModal(BazmoModal.NewMarketAd("acc")) },
                        compact = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("acc_new_ad_button")
                    )
                    if (state.canCreateStore) {
                        BazmoGoldButton(
                            text = "🏪 ثبت فروشگاه",
                            onClick = { viewModel.openModal(BazmoModal.RegisterStore("acc")) },
                            compact = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("acc_register_store_button")
                        )
                    }
                }
            }

            item(key = "acc_cats") {
                BazmoSectionHeader("دسته‌بندی لوازم جانبی")
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(cats, key = { it }) { cat ->
                        BazmoFilterChip(
                            label = cat,
                            selected = av.cat == cat,
                            onClick = { viewModel.updateAccCat(cat) }
                        )
                    }
                }
            }

            item(key = "acc_stores") {
                BazmoSectionHeader(
                    title = "فروشگاه‌های جانبی (${locStores.scope})",
                    actionLabel = if (av.selectedStoreId != null) "نمایش همه" else null,
                    onAction = if (av.selectedStoreId != null) {
                        { viewModel.clearAccStore() }
                    } else null
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    contentPadding = PaddingValues(bottom = 10.dp)
                ) {
                    items(locStores.list, key = { it.id }) { st ->
                        val selected = av.selectedStoreId == st.id
                        Column(
                            modifier = Modifier
                                .width(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Brush.linearGradient(listOf(HeaderBgDeep, HeaderBgLight)))
                                .then(
                                    if (selected) Modifier.border(BorderStroke(3.dp, GoldAccent), RoundedCornerShape(16.dp))
                                    else Modifier
                                )
                                .clickable { viewModel.toggleAccStore(st.id) }
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "🏪 ${st.name}",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                maxLines = 1
                            )
                            Text(
                                text = "📍 ${st.city} • ⭐ ${st.rating}",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFDBEAFE),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "فیلتر کالاهای فروشگاه",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFBAE6FD),
                                modifier = Modifier.padding(top = 3.dp)
                            )
                        }
                    }
                }
            }

            item(key = "acc_products_hdr") {
                BazmoSectionHeader(
                    title = "محصولات جانبی",
                    badgeText = "${products.size.toPersianNumber()} کالا"
                )
            }

            if (products.isEmpty()) {
                item(key = "acc_empty") { BazmoEmptyState("محصولی یافت نشد.") }
            } else {
                items(productRows, key = { row -> "acc_row_" + row.joinToString("_") { it.id } }) { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowItems.forEach { acc ->
                            val st = state.getStore(acc.storeId)
                            val myInq = inquiriesByAccId[acc.id]
                            BazmoCard(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    BazmoBadge(acc.cat, BadgeTone.BLUE)
                                    Text(
                                        text = "🏪 ${st.name}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = acc.name,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = acc.desc,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted,
                                    modifier = Modifier.padding(top = 3.dp, bottom = 10.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = acc.price.formatToman(),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Black,
                                        color = Navy
                                    )
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        BazmoGoldButton(
                                            text = "🔔 استعلام",
                                            onClick = { viewModel.sendAccessoryInquiry(acc) },
                                            compact = true
                                        )
                                        BazmoPrimaryButton(
                                            text = "🛒 خرید",
                                            onClick = { viewModel.addToCart(acc.id, acc.name, acc.price, st.name) },
                                            compact = true
                                        )
                                    }
                                }
                                if (myInq != null) {
                                    Spacer(modifier = Modifier.height(8.dp))
                                    BazmoWarrantyBox("وضعیت استعلام: ${myInq.status}")
                                }
                            }
                        }
                        repeat(cols - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }

            if (accUserAds.isNotEmpty()) {
                item(key = "hdr_acc_user_ads") {
                    BazmoSectionHeader(
                        title = "آگهی‌های جانبی همکاران",
                        badgeText = "${accUserAds.size.toPersianNumber()} آگهی"
                    )
                }
                items(accAdRows, key = { row -> "acc_ad_row_" + row.joinToString("_") { it.id } }) { rowItems ->
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
                                    Text("🏪 ${ad.seller} | 📍 ${ad.city}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = ad.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = TextPrimary
                                )
                                if (ad.desc.isNotBlank()) {
                                    Text(
                                        text = ad.desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        modifier = Modifier.padding(top = 3.dp, bottom = 8.dp)
                                    )
                                } else {
                                    Spacer(modifier = Modifier.height(8.dp))
                                }
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
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        BazmoSoftButton(
                                            text = "💬 چت",
                                            onClick = { viewModel.openModal(BazmoModal.Chat(ad.id)) },
                                            compact = true
                                        )
                                        BazmoPrimaryButton(
                                            text = "🛒 خرید",
                                            onClick = { viewModel.addToCart(ad.id, ad.title, ad.price, ad.seller) },
                                            compact = true
                                        )
                                    }
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
