package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.LocationPoint
import com.example.data.filterByLocation
import com.example.data.formatToman
import com.example.data.toPersianNumber
import com.example.ui.components.BadgeTone
import com.example.ui.components.BazmoBackBar
import com.example.ui.components.BazmoBadge
import com.example.ui.components.BazmoCard
import com.example.ui.components.BazmoChip
import com.example.ui.components.BazmoDangerButton
import com.example.ui.components.BazmoEmptyState
import com.example.ui.components.BazmoOutlineButton
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoSearchBar
import com.example.ui.components.BazmoSectionHeader
import com.example.ui.components.BazmoSoftButton
import com.example.ui.components.BazmoSuccessButton
import com.example.ui.components.BazmoWarrantyBox
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.BazmoModal
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.BazmoViewModel

@Composable
fun DeskRentalScreen(
    state: BazmoUiState,
    viewModel: BazmoViewModel
) {
    val context = LocalContext.current
    var subTab by rememberSaveable { mutableStateOf("rentals") }
    var selectedCat by rememberSaveable { mutableStateOf("همه") }
    var search by rememberSaveable { mutableStateOf("") }

    BackHandler {
        viewModel.toggleDeskRentals(false)
    }

    val categories = listOf(
        "همه",
        "تعمیرات سخت‌افزار",
        "نرم‌افزار و فلش",
        "فروش موبایل",
        "لوازم جانبی"
    )

    val filteredRentals = remember(
        state.db.deskRentals,
        state.session.province,
        state.session.city,
        state.session.region,
        selectedCat,
        search
    ) {
        val visible = state.db.deskRentals.filter { d ->
            (d.status == "approved" || d.ownerId == state.currentUid || state.session.isAdminUser) &&
                (selectedCat == "همه" || d.category == selectedCat) &&
                (search.isBlank() ||
                    d.title.contains(search, ignoreCase = true) ||
                    d.storeName.contains(search, ignoreCase = true) ||
                    d.equipment.contains(search, ignoreCase = true) ||
                    d.cooperationModel.contains(search, ignoreCase = true) ||
                    d.city.contains(search, ignoreCase = true))
        }
        filterByLocation(visible, state.session.province, state.session.city, state.session.region) {
            LocationPoint(it.province, it.city, it.region)
        }
    }

    val filteredRequests = remember(
        state.db.deskRequests,
        state.session.province,
        state.session.city,
        state.session.region,
        selectedCat,
        search
    ) {
        val visible = state.db.deskRequests.filter { r ->
            (selectedCat == "همه" || r.category == selectedCat) &&
                (search.isBlank() ||
                    r.title.contains(search, ignoreCase = true) ||
                    r.proposalText.contains(search, ignoreCase = true) ||
                    r.requesterName.contains(search, ignoreCase = true) ||
                    r.city.contains(search, ignoreCase = true))
        }
        filterByLocation(visible, state.session.province, state.session.city, state.session.region) {
            LocationPoint(it.province, it.city, it.region)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item(key = "desk_back") {
            BazmoBackBar(
                title = "🪑 اجاره میز کار و همکاری صنفی",
                onBack = { viewModel.toggleDeskRentals(false) }
            )
        }

        item(key = "desk_controls") {
            BazmoCard {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (subTab == "rentals") {
                        BazmoPrimaryButton(
                            text = "🪑 میزهای آماده (${filteredRentals.list.size.toPersianNumber()})",
                            onClick = { subTab = "rentals" },
                            compact = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("desk_subtab_rentals")
                        )
                        BazmoSoftButton(
                            text = "🤝 درخواست و پیشنهاد (${filteredRequests.list.size.toPersianNumber()})",
                            onClick = { subTab = "requests" },
                            compact = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("desk_subtab_requests")
                        )
                    } else {
                        BazmoSoftButton(
                            text = "🪑 میزهای آماده (${filteredRentals.list.size.toPersianNumber()})",
                            onClick = { subTab = "rentals" },
                            compact = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("desk_subtab_rentals")
                        )
                        BazmoPrimaryButton(
                            text = "🤝 درخواست و پیشنهاد (${filteredRequests.list.size.toPersianNumber()})",
                            onClick = { subTab = "requests" },
                            compact = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("desk_subtab_requests")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                BazmoSearchBar(
                    value = search,
                    onValueChange = { search = it },
                    placeholder = "جستجوی میز کار، تجهیزات، مدل همکاری یا پیشنهاد..."
                )
                Spacer(modifier = Modifier.height(8.dp))
                BazmoPrimaryButton(
                    text = "+ ثبت میز کار یا درخواست همکاری",
                    onClick = {
                        val initialKind = if (subTab == "requests") "request" else "rental"
                        viewModel.openModal(BazmoModal.NewDeskEntry(initialKind))
                    },
                    compact = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("desk_add_button")
                )
            }
        }

        item(key = "desk_cats") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 10.dp)
            ) {
                items(categories, key = { it }) { cat ->
                    BazmoChip(
                        text = cat,
                        selected = selectedCat == cat,
                        onClick = { selectedCat = cat }
                    )
                }
            }
        }

        if (subTab == "rentals") {
            item(key = "desk_rentals_hdr") {
                BazmoSectionHeader(
                    title = "میزهای آماده اجاره (${filteredRentals.scope})",
                    badgeText = "${filteredRentals.list.size.toPersianNumber()} مورد"
                )
            }
            if (filteredRentals.list.isEmpty()) {
                item(key = "desk_rentals_empty") {
                    BazmoEmptyState("در این دسته‌بندی هنوز میز کاری ثبت نشده است.")
                }
            } else {
                itemsIndexed(filteredRentals.list, key = { idx, d -> "drent_${idx}_${d.id}" }) { _, d ->
                    val statusLabel = when (d.status) {
                        "approved" -> "تاییدشده"
                        "rejected" -> "ردشده"
                        else -> "در انتظار تایید"
                    }
                    val statusTone = when (d.status) {
                        "approved" -> BadgeTone.GREEN
                        "rejected" -> BadgeTone.RED
                        else -> BadgeTone.GOLD
                    }
                    BazmoCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BazmoBadge(d.category, BadgeTone.BLUE)
                            BazmoBadge(statusLabel, statusTone)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = d.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "🏪 ${d.storeName.ifBlank { "فروشگاه همکار" }} • 📍 ${d.city} (${d.region})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                        if (d.cooperationModel.isNotBlank()) {
                            Text(
                                text = "🤝 مدل همکاری: ${d.cooperationModel}",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = NavyDeep,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        if (d.equipment.isNotBlank()) {
                            BazmoWarrantyBox(
                                text = "🔧 تجهیزات: ${d.equipment}",
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        if (d.desc.isNotBlank()) {
                            Text(
                                text = d.desc,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 6.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = if (d.rentMonthly > 0L) "${d.rentMonthly.formatToman()} / ماه" else "توافقی / درصدی",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black,
                                    color = Navy
                                )
                                if (d.rentDaily > 0L) {
                                    Text(
                                        text = "روزانه: ${d.rentDaily.formatToman()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextMuted
                                    )
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                if (d.phone.isNotBlank()) {
                                    BazmoOutlineButton(
                                        text = "📞",
                                        onClick = {
                                            runCatching {
                                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${d.phone}")))
                                            }
                                        },
                                        compact = true
                                    )
                                }
                                BazmoPrimaryButton(
                                    text = "💬 چت",
                                    onClick = {
                                        viewModel.openModal(
                                            BazmoModal.GenericChat(
                                                adId = d.id,
                                                title = d.title,
                                                partnerName = d.storeName.ifBlank { d.ownerName },
                                                sellerUid = d.ownerId
                                            )
                                        )
                                    },
                                    compact = true
                                )
                                if (d.ownerId == state.currentUid || state.session.isAdminUser) {
                                    BazmoDangerButton(
                                        text = "حذف",
                                        onClick = { viewModel.deleteDeskRental(d.id) },
                                        compact = true
                                    )
                                }
                            }
                        }
                    }
                }
            }
        } else {
            item(key = "desk_requests_hdr") {
                BazmoSectionHeader(
                    title = "پیشنهادها و درخواست‌های همکاری (${filteredRequests.scope})",
                    badgeText = "${filteredRequests.list.size.toPersianNumber()} پیشنهاد"
                )
            }
            if (filteredRequests.list.isEmpty()) {
                item(key = "desk_requests_empty") {
                    BazmoEmptyState("درخواستی در این دسته‌بندی ثبت نشده است.")
                }
            } else {
                itemsIndexed(filteredRequests.list, key = { idx, r -> "dreq_${idx}_${r.id}" }) { _, r ->
                    val statusLabel = when (r.status) {
                        "matched" -> "توافق شده"
                        "closed" -> "بسته شده"
                        else -> "در جستجوی میز"
                    }
                    val statusTone = when (r.status) {
                        "matched" -> BadgeTone.GREEN
                        "closed" -> BadgeTone.RED
                        else -> BadgeTone.GOLD
                    }
                    BazmoCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            BazmoBadge(r.category, BadgeTone.BLUE)
                            BazmoBadge(statusLabel, statusTone)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = r.title,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                        Text(
                            text = "👤 ${r.requesterName} • 📍 ${r.city} (${r.region})",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 3.dp)
                        )
                        Text(
                            text = r.proposalText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 6.dp, bottom = 8.dp)
                        )
                        Text(
                            text = if (r.proposedBudget > 0L) "سقف بودجه: ${r.proposedBudget.formatToman()}" else "پیشنهاد درصدی / توافقی",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Navy
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (r.requesterPhone.isNotBlank()) {
                                BazmoOutlineButton(
                                    text = "📞 تماس",
                                    onClick = {
                                        runCatching {
                                            context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${r.requesterPhone}")))
                                        }
                                    },
                                    compact = true
                                )
                            }
                            BazmoPrimaryButton(
                                text = "💬 چت",
                                onClick = {
                                    viewModel.openModal(
                                        BazmoModal.GenericChat(
                                            adId = r.id,
                                            title = r.title,
                                            partnerName = r.requesterName,
                                            sellerUid = r.ownerId
                                        )
                                    )
                                },
                                compact = true,
                                modifier = Modifier.weight(1f)
                            )
                            if (r.ownerId == state.currentUid || state.session.isAdminUser) {
                                if (r.status == "open") {
                                    BazmoSuccessButton(
                                        text = "✓ توافق شد",
                                        onClick = { viewModel.updateDeskRequestStatus(r.id, "matched") },
                                        compact = true
                                    )
                                }
                                BazmoDangerButton(
                                    text = "حذف",
                                    onClick = { viewModel.deleteDeskRequest(r.id) },
                                    compact = true
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
