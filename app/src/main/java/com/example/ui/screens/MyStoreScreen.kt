package com.example.ui.screens

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.AccessoryEntity
import com.example.data.BazmoRoles
import com.example.data.StoreMobileEntity
import com.example.data.formatToman
import com.example.data.toPersianNumber
import com.example.ui.components.BadgeTone
import com.example.ui.components.BazmoBackBar
import com.example.ui.components.BazmoBadge
import com.example.ui.components.BazmoCard
import com.example.ui.components.BazmoChip
import com.example.ui.components.BazmoDangerButton
import com.example.ui.components.BazmoEmptyState
import com.example.ui.components.BazmoGoldButton
import com.example.ui.components.BazmoInput
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoSectionHeader
import com.example.ui.components.BazmoSoftButton
import com.example.ui.components.BazmoSuccessButton
import com.example.ui.components.BazmoWarrantyBox
import com.example.ui.theme.Navy
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.BazmoModal
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.BazmoViewModel

@Composable
fun MyStoreScreen(
    state: BazmoUiState,
    viewModel: BazmoViewModel
) {
    val myStores = remember(state.db.stores, state.currentUid, state.session.isAdminUser) {
        viewModel.myOwnedStores()
    }
    var selectedStoreId by rememberSaveable { mutableStateOf(myStores.firstOrNull()?.id.orEmpty()) }
    val store = myStores.find { it.id == selectedStoreId } ?: myStores.firstOrNull()
    var subTab by rememberSaveable { mutableStateOf("products") }

    var editingMobile by remember { mutableStateOf<StoreMobileEntity?>(null) }
    var editMobPrice by remember { mutableStateOf("") }
    var editMobInfo by remember { mutableStateOf("") }

    var editingAcc by remember { mutableStateOf<AccessoryEntity?>(null) }
    var editAccPrice by remember { mutableStateOf("") }

    BackHandler {
        viewModel.toggleMyStore(false)
    }

    if (editingMobile != null) {
        val target = editingMobile!!
        AlertDialog(
            onDismissRequest = { editingMobile = null },
            title = { Text("ویرایش قیمت موبایل") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BazmoInput(editMobPrice, { editMobPrice = it }, "قیمت جدید (تومان)", keyboardType = KeyboardType.Number)
                    BazmoInput(editMobInfo, { editMobInfo = it }, "توضیحات گارانتی و اقلام")
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateStoreMobileOffer(target.id, editMobPrice, editMobInfo)
                    editingMobile = null
                }) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(onClick = { editingMobile = null }) { Text("انصراف") }
            }
        )
    }

    if (editingAcc != null) {
        val target = editingAcc!!
        AlertDialog(
            onDismissRequest = { editingAcc = null },
            title = { Text("ویرایش قیمت ${target.name}") },
            text = {
                BazmoInput(editAccPrice, { editAccPrice = it }, "قیمت جدید (تومان)", keyboardType = KeyboardType.Number)
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.updateStoreAccessoryPrice(target.id, editAccPrice)
                    editingAcc = null
                }) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(onClick = { editingAcc = null }) { Text("انصراف") }
            }
        )
    }

    val storeOffers = remember(state.db.storeMobiles, store?.id) {
        if (store == null) emptyList() else state.db.storeMobiles.filter { it.storeId == store.id }
    }
    val storeAccs = remember(state.db.accessories, store?.id) {
        if (store == null) emptyList() else state.db.accessories.filter { it.storeId == store.id }
    }
    val storeInqs = remember(state.db.inquiries, store?.name) {
        if (store == null) emptyList() else state.db.inquiries.filter { it.storeName == store.name }
    }
    val storeOrders = remember(state.db.orders, store?.name) {
        if (store == null) emptyList()
        else state.db.orders.filter { o -> o.items.any { it.storeName == store.name } }
    }
    val mobilesById = remember(state.db.mobiles) { state.db.mobiles.associateBy { it.id } }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 28.dp)
    ) {
        item(key = "ms_back") {
            BazmoBackBar(
                title = "🏪 مدیریت فروشگاه من",
                onBack = { viewModel.toggleMyStore(false) },
                trailing = {
                    BazmoGoldButton(
                        text = "+ فروشگاه جدید",
                        onClick = {
                            viewModel.openModal(
                                BazmoModal.RegisterStore(BazmoRoles.defaultStoreTypeForRole(state.currentTier))
                            )
                        },
                        compact = true,
                        modifier = Modifier.testTag("mystore_create_button")
                    )
                }
            )
        }

        if (store == null) {
            item(key = "ms_empty_store") {
                BazmoCard {
                    Text(
                        text = "هنوز فروشگاهی به نام شما ثبت نشده است",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = TextPrimary
                    )
                    Text(
                        text = "با ثبت فروشگاه، پس از تایید مدیریت می‌توانید کالاها، قیمت روز موبایل و لوازم جانبی و استعلام مشتریان را مدیریت کنید.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    BazmoPrimaryButton(
                        text = "+ ثبت اولین فروشگاه",
                        onClick = {
                            viewModel.openModal(
                                BazmoModal.RegisterStore(BazmoRoles.defaultStoreTypeForRole(state.currentTier))
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("mystore_empty_register_button")
                    )
                }
            }
            return@LazyColumn
        }

        if (myStores.size > 1) {
            item(key = "ms_selector") {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(bottom = 8.dp)
                ) {
                    items(myStores, key = { it.id }) { s ->
                        BazmoChip(
                            text = s.name,
                            selected = s.id == store.id,
                            onClick = { selectedStoreId = s.id }
                        )
                    }
                }
            }
        }

        item(key = "ms_banner") {
            val isApproved = store.status == "approved"
            val (badgeLabel, badgeTone) = when (store.status) {
                "approved" -> "✓ تاییدشده و فعال" to BadgeTone.GREEN
                "rejected" -> "✕ ردشده" to BadgeTone.RED
                else -> "⏳ در انتظار تایید مدیریت" to BadgeTone.GOLD
            }
            BazmoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(store.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                        Text(
                            text = "${store.province} • ${store.city} (${store.region}) • ${BazmoRoles.storeTypeLabel(store.type)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                    }
                    BazmoBadge(badgeLabel, badgeTone)
                }
                if (!isApproved) {
                    BazmoWarrantyBox(
                        text = "فروشگاه شما پس از تایید ادمین در بازار نمایش داده می‌شود و امکان افزودن محصول فعال خواهد شد.",
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }

        item(key = "ms_subtabs") {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(bottom = 10.dp)
            ) {
                item {
                    BazmoChip(
                        text = "📦 محصولات (${(storeOffers.size + storeAccs.size).toPersianNumber()})",
                        selected = subTab == "products",
                        onClick = { subTab = "products" }
                    )
                }
                item {
                    BazmoChip(
                        text = "📋 استعلام‌ها (${storeInqs.size.toPersianNumber()})",
                        selected = subTab == "inquiries",
                        onClick = { subTab = "inquiries" }
                    )
                }
                item {
                    BazmoChip(
                        text = "🧾 سفارش‌ها (${storeOrders.size.toPersianNumber()})",
                        selected = subTab == "orders",
                        onClick = { subTab = "orders" }
                    )
                }
                item {
                    BazmoChip(
                        text = "⚙️ مشخصات",
                        selected = subTab == "settings",
                        onClick = { subTab = "settings" }
                    )
                }
            }
        }

        when (subTab) {
            "products" -> {
                if (store.status == "approved") {
                    item(key = "ms_add_actions") {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 10.dp)
                        ) {
                            BazmoPrimaryButton(
                                text = "+ گوشی / قیمت",
                                onClick = { viewModel.openModal(BazmoModal.MyStoreAddMobile(store.id)) },
                                compact = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mystore_add_mobile_button")
                            )
                            BazmoGoldButton(
                                text = "+ کالای جانبی",
                                onClick = { viewModel.openModal(BazmoModal.MyStoreAddAccessory(store.id)) },
                                compact = true,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("mystore_add_acc_button")
                            )
                            BazmoSoftButton(
                                text = "+ قطعه",
                                onClick = { viewModel.openModal(BazmoModal.NewHwAd) },
                                compact = true
                            )
                        }
                    }
                }
                item(key = "ms_mobs_hdr") {
                    BazmoSectionHeader("📱 گوشی‌های این فروشگاه (${storeOffers.size.toPersianNumber()})")
                }
                if (storeOffers.isEmpty()) {
                    item(key = "ms_mobs_empty") { BazmoEmptyState("هنوز گوشی‌ای برای این فروشگاه ثبت نشده است.") }
                } else {
                    items(storeOffers, key = { it.id }) { o ->
                        val m = mobilesById[o.mobileId]
                        BazmoCard {
                            Text(
                                text = "${m?.brand.orEmpty()} ${m?.model ?: "گوشی"}".trim(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(o.info, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(o.price.formatToman(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = Navy)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    BazmoSoftButton(
                                        text = "✏️ قیمت",
                                        onClick = {
                                            editMobPrice = o.price.toString()
                                            editMobInfo = o.info
                                            editingMobile = o
                                        },
                                        compact = true
                                    )
                                    BazmoDangerButton(
                                        text = "حذف",
                                        onClick = { viewModel.deleteStoreMobileOffer(o.id) },
                                        compact = true
                                    )
                                }
                            }
                        }
                    }
                }

                item(key = "ms_accs_hdr") {
                    BazmoSectionHeader("🎧 لوازم جانبی این فروشگاه (${storeAccs.size.toPersianNumber()})")
                }
                if (storeAccs.isEmpty()) {
                    item(key = "ms_accs_empty") { BazmoEmptyState("هنوز کالای جانبی برای این فروشگاه ثبت نشده است.") }
                } else {
                    items(storeAccs, key = { it.id }) { a ->
                        BazmoCard {
                            BazmoBadge(a.cat, BadgeTone.BLUE)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(a.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                            if (a.desc.isNotBlank()) {
                                Text(a.desc, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(a.price.formatToman(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = Navy)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    BazmoSoftButton(
                                        text = "✏️ قیمت",
                                        onClick = {
                                            editAccPrice = a.price.toString()
                                            editingAcc = a
                                        },
                                        compact = true
                                    )
                                    BazmoDangerButton(
                                        text = "حذف",
                                        onClick = { viewModel.deleteStoreAccessory(a.id) },
                                        compact = true
                                    )
                                }
                            }
                        }
                    }
                }
            }

            "inquiries" -> {
                if (storeInqs.isEmpty()) {
                    item(key = "ms_inq_empty") { BazmoEmptyState("هنوز استعلام موجودی برای فروشگاه شما ثبت نشده است.") }
                } else {
                    items(storeInqs, key = { it.id }) { q ->
                        val tone = when {
                            q.status.contains("ناموجود") -> BadgeTone.RED
                            q.status.contains("موجود") -> BadgeTone.GREEN
                            else -> BadgeTone.GOLD
                        }
                        BazmoCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(q.accName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                                BazmoBadge(q.status, tone)
                            }
                            Text("خریدار: ${q.userName} • ${q.date}", style = MaterialTheme.typography.bodySmall, color = TextMuted, modifier = Modifier.padding(vertical = 6.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                BazmoSuccessButton(
                                    text = "✓ موجود شد",
                                    onClick = { viewModel.adminReplyInquiry(q, "موجود شد - آماده ثبت سفارش") },
                                    compact = true
                                )
                                BazmoDangerButton(
                                    text = "✕ ناموجود",
                                    onClick = { viewModel.adminReplyInquiry(q, "فعلاً ناموجود") },
                                    compact = true
                                )
                            }
                        }
                    }
                }
            }

            "orders" -> {
                if (storeOrders.isEmpty()) {
                    item(key = "ms_ord_empty") { BazmoEmptyState("سفارشی برای این فروشگاه ثبت نشده است.") }
                } else {
                    items(storeOrders, key = { it.id }) { o ->
                        BazmoCard {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("${o.id} • ${o.userName}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                                BazmoBadge(o.status, BadgeTone.BLUE)
                            }
                            Text("${o.total.formatToman()} • ${o.deliveryType} • ${o.city}", style = MaterialTheme.typography.bodySmall, color = TextMuted, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }

            else -> {
                item(key = "ms_settings_${store.id}") {
                    var editName by rememberSaveable(store.id) { mutableStateOf(store.name) }
                    var editPhone by rememberSaveable(store.id) { mutableStateOf(store.phone) }
                    var editAddr by rememberSaveable(store.id) { mutableStateOf(store.address) }
                    var editHours by rememberSaveable(store.id) { mutableStateOf(store.hours) }
                    BazmoCard {
                        Text("ویرایش مشخصات فروشگاه", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 8.dp))
                        BazmoInput(editName, { editName = it }, "نام فروشگاه", modifier = Modifier.padding(bottom = 8.dp))
                        BazmoInput(editPhone, { editPhone = it }, "تلفن تماس", keyboardType = KeyboardType.Phone, modifier = Modifier.padding(bottom = 8.dp))
                        BazmoInput(editAddr, { editAddr = it }, "آدرس دقیق", modifier = Modifier.padding(bottom = 8.dp))
                        BazmoInput(editHours, { editHours = it }, "ساعات کاری", modifier = Modifier.padding(bottom = 12.dp))
                        BazmoPrimaryButton(
                            text = "✓ ذخیره تغییرات",
                            onClick = { viewModel.updateOwnStoreInfo(store.id, editName, editPhone, editAddr, editHours) },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }
}
