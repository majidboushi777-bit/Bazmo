package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.BazmoLocations
import com.example.data.BazmoRoles
import com.example.data.RegistrationRequestEntity
import com.example.data.UserEntity
import com.example.data.formatToman
import com.example.data.toPersianNumber
import kotlinx.coroutines.launch
import com.example.ui.components.BadgeTone
import com.example.ui.components.BazmoAccentButton
import com.example.ui.components.BazmoBadge
import com.example.ui.components.BazmoCard
import com.example.ui.components.BazmoDangerButton
import com.example.ui.components.BazmoDropdownSelector
import com.example.ui.components.BazmoGoldButton
import com.example.ui.components.BazmoInput
import com.example.ui.components.BazmoOutlineButton
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoSearchBar
import com.example.ui.components.BazmoSectionHeader
import com.example.ui.components.BazmoSoftButton
import com.example.ui.components.BazmoSuccessButton
import com.example.ui.components.BazmoWarrantyBox
import androidx.compose.ui.platform.LocalContext
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BazmoThemeManager
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.HeaderBgDeep
import com.example.ui.theme.HeaderBgLight
import com.example.ui.theme.LocalBazmoColors
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyLight
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.viewmodel.AdminTab
import com.example.viewmodel.AppRole
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.BazmoViewModel

@Composable
fun AdminShellScreen(
    state: BazmoUiState,
    viewModel: BazmoViewModel,
    onSignOut: () -> Unit
) {
    val adminTabs = remember {
        listOf(
            AdminTab.DASHBOARD to "📊 داشبورد",
            AdminTab.USERS to "👥 کاربران",
            AdminTab.NOTIFICATIONS to "🔔 نوتیفیکیشن",
            AdminTab.ADS to "🛡️ آگهی و فروشگاه",
            AdminTab.REQUESTS to "📦 سفارش و استعلام",
            AdminTab.CATALOG to "🧩 کالا و انجمن"
        )
    }
    val tabOrder = remember(adminTabs) { adminTabs.map { it.first } }
    val selectedTabIndex = tabOrder.indexOf(state.session.adminTab).coerceAtLeast(0)
    val pagerState = rememberPagerState(initialPage = selectedTabIndex) { adminTabs.size }
    val tabRowState = rememberLazyListState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDark = LocalBazmoColors.current.isDark

    LaunchedEffect(state.session.adminTab) {
        val targetIdx = tabOrder.indexOf(state.session.adminTab).coerceAtLeast(0)
        if (pagerState.currentPage != targetIdx) {
            pagerState.animateScrollToPage(targetIdx)
        }
        tabRowState.animateScrollToItem(targetIdx)
    }

    LaunchedEffect(pagerState.settledPage) {
        val settledTab = tabOrder.getOrNull(pagerState.settledPage)
        if (settledTab != null && settledTab != state.session.adminTab) {
            viewModel.setAdminTab(settledTab)
        }
        tabRowState.animateScrollToItem(pagerState.settledPage)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground),
        contentAlignment = Alignment.TopCenter
    ) {
        val adaptiveMaxWidth = when {
            maxWidth >= 960.dp -> 1040.dp
            maxWidth >= 600.dp -> 780.dp
            else -> 620.dp
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = adaptiveMaxWidth)
        ) {
            // Admin Header
            Surface(
                shape = RoundedCornerShape(bottomStart = 20.dp, bottomEnd = 20.dp),
                shadowElevation = 8.dp,
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF1E3A5F))))
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.Start,
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "پنل ادمین بازمو",
                                    color = Color.White,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                BazmoBadge("مدیریت کل", BadgeTone.GOLD)
                            }
                            Text(
                                text = "کنترل کامل کاربران، نوتیفیکیشن و بخش‌ها",
                                color = Color(0xFFCCE0FF),
                                fontSize = 10.sp,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(11.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .clickable { BazmoThemeManager.toggleDark(context, isDark) }
                                    .testTag("theme_toggle_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                        contentDescription = if (isDark) "حالت روشن" else "حالت تاریک",
                                        tint = GoldAccent,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(11.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .clickable { viewModel.switchRole(AppRole.USER) }
                                    .testTag("switch_user_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Rounded.Storefront,
                                        contentDescription = "نمای کاربر",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(11.dp),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.22f)),
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(11.dp))
                                    .clickable(onClick = onSignOut)
                                    .testTag("admin_logout_button")
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Rounded.Logout,
                                        contentDescription = "خروج از حساب",
                                        tint = Color.White,
                                        modifier = Modifier.size(19.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Surface(
                        color = Color.Black.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        LazyRow(
                            state = tabRowState,
                            modifier = Modifier.padding(5.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(adminTabs, key = { it.first.name }) { (tab, label) ->
                                val active = state.session.adminTab == tab
                                val tabBg by animateColorAsState(
                                    targetValue = if (active) Color.White else Color.Transparent,
                                    animationSpec = tween(150),
                                    label = "adminTabBg"
                                )
                                val tabText by animateColorAsState(
                                    targetValue = if (active) BrandNavy else Color(0xFFD6E4FA),
                                    animationSpec = tween(150),
                                    label = "adminTabText"
                                )
                                Surface(
                                    color = tabBg,
                                    shape = RoundedCornerShape(11.dp),
                                    modifier = Modifier
                                        .heightIn(min = 38.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .clickable {
                                            viewModel.setAdminTab(tab)
                                            val idx = tabOrder.indexOf(tab).coerceAtLeast(0)
                                            scope.launch { pagerState.animateScrollToPage(idx) }
                                        }
                                        .testTag("admin_tab_${tab.name.lowercase()}")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                    ) {
                                        Text(
                                            text = label,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = tabText,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Text(
                        text = "← برای جابه‌جایی بین بخش‌ها به راست و چپ بکشید →",
                        color = Color(0xFFBFDBFE).copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )
                }
            }

            // Swipeable Admin Tab Content
            HorizontalPager(
                state = pagerState,
                beyondViewportPageCount = 1,
                key = { adminTabs[it].first.name },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp)
            ) { page ->
                when (tabOrder[page]) {
                    AdminTab.DASHBOARD -> AdminDashboardTab(state, viewModel)
                    AdminTab.USERS -> AdminUsersTab(state, viewModel)
                    AdminTab.NOTIFICATIONS -> AdminNotificationsTab(state, viewModel)
                    AdminTab.ADS -> AdminAdsStoresTab(state, viewModel)
                    AdminTab.REQUESTS -> AdminRequestsTab(state, viewModel)
                    AdminTab.CATALOG -> AdminCatalogForumTab(state, viewModel)
                }
            }
        }
    }
}

@Composable
private fun AdminDashboardTab(state: BazmoUiState, viewModel: BazmoViewModel) {
    val pendingAdsCount = remember(
        state.db.hwAds,
        state.db.tutorials,
        state.db.boxes,
        state.db.stores,
        state.db.deskRentals
    ) {
        state.db.hwAds.count { it.status == "pending" } +
            state.db.tutorials.count { it.status == "pending" } +
            state.db.boxes.count { it.status == "pending" } +
            state.db.stores.count { it.status == "pending" } +
            state.db.deskRentals.count { it.status == "pending" }
    }
    val pendingRegCount = remember(state.db.registrationRequests) {
        state.db.registrationRequests.count { it.status == "pending" }
    }
    val totalSales = remember(state.db.orders) {
        state.db.orders.sumOf { it.total }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val kpiCols = if (maxWidth >= 680.dp) 3 else 2
        val kpiItems = listOf(
            Triple(state.db.users.size.toPersianNumber(), "کل کاربران ثبت‌شده", Navy),
            Triple(pendingRegCount.toPersianNumber(), "درخواست عضویت منتظر تایید", GoldAccent),
            Triple(pendingAdsCount.toPersianNumber(), "آگهی در انتظار تایید", GoldAccent),
            Triple(state.db.orders.size.toPersianNumber(), "سفارش‌های کل", SuccessGreen),
            Triple(state.db.notifications.size.toPersianNumber(), "نوتیفیکیشن ارسالی", Navy),
            Triple(state.db.stores.size.toPersianNumber(), "فروشگاه‌های فعال", SuccessGreen)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            item {
                kpiItems.chunked(kpiCols).forEachIndexed { idx, rowItems ->
                    if (idx > 0) Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { (value, label, color) ->
                            KpiCard(value, label, color, Modifier.weight(1f))
                        }
                        repeat(kpiCols - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Brush.linearGradient(listOf(HeaderBgDeep, HeaderBgLight)))
                        .padding(16.dp)
                ) {
                    Text(
                        text = "مجموع گردش مالی سفارش‌ها",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFFCBD5E1)
                    )
                    Text(
                        text = totalSales.formatToman(),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        }

        item {
            BazmoSectionHeader("🎭 کنترل داده‌های نمونه کل ایران (۴۸ منطقه)")
            BazmoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f).padding(end = 8.dp)) {
                        Text(
                            text = if (state.db.showDemoData) "نمایش داده‌های نمونه فعال است" else "داده‌های نمونه موقتاً مخفی شده‌اند",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyDeep
                        )
                        Text(
                            text = "با خاموش کردن این دکمه، تمام اطلاعات واقعی کاربران و سفارش‌ها حفظ شده و فقط داده‌های نمایشی ۴۸ منطقه پنهان می‌شوند.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    Switch(
                        checked = state.db.showDemoData,
                        enabled = !state.session.isSeedingDemo,
                        onCheckedChange = { viewModel.adminToggleDemoData(it) },
                        modifier = Modifier.testTag("admin_demo_toggle_switch")
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                BazmoSoftButton(
                    text = if (state.session.isSeedingDemo) {
                        "⏳ در حال ساخت نمونه ۴۸ منطقه..."
                    } else {
                        "🔄 ساخت / بروزرسانی نمونه ۴۸ منطقه"
                    },
                    enabled = !state.session.isSeedingDemo,
                    onClick = { viewModel.adminSeedDemoDataset() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("admin_seed_demo_button")
                )
            }
        }

        item {
            BazmoSectionHeader("دسترسی سریع مدیریت")
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    BazmoPrimaryButton(
                        text = if (pendingRegCount > 0) "👤 درخواست‌ها (${pendingRegCount.toPersianNumber()})" else "👤 تعریف کاربر جدید",
                        onClick = { viewModel.setAdminTab(AdminTab.USERS) },
                        modifier = Modifier.weight(1f)
                    )
                    BazmoGoldButton("🔔 ارسال نوتیفیکیشن", { viewModel.setAdminTab(AdminTab.NOTIFICATIONS) }, modifier = Modifier.weight(1f))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    BazmoSoftButton("🛡️ تایید آگهی‌ها (${pendingAdsCount.toPersianNumber()})", { viewModel.setAdminTab(AdminTab.ADS) }, modifier = Modifier.weight(1f))
                    BazmoSoftButton("🧩 استعلام و قطعات", { viewModel.setAdminTab(AdminTab.REQUESTS) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
    }
}

@Composable
private fun KpiCard(value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    BazmoCard(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Black, color = color)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun AdminUsersTab(state: BazmoUiState, viewModel: BazmoViewModel) {
    val provs = BazmoLocations.provinces()
    var newUEmail by rememberSaveable { mutableStateOf("") }
    var newUFull by rememberSaveable { mutableStateOf("") }
    var newUPhone by rememberSaveable { mutableStateOf("") }
    var newUPassword by rememberSaveable { mutableStateOf("") }
    var newUTier by rememberSaveable { mutableStateOf(BazmoRoles.REPAIR_TECH) }
    var newUProv by rememberSaveable { mutableStateOf("تهران") }
    var newUCity by rememberSaveable { mutableStateOf("تهران") }
    var newURegion by rememberSaveable { mutableStateOf("علاءالدین و جمهوری") }
    var passwordTargetUser by remember { mutableStateOf<UserEntity?>(null) }
    var newWebPassword by rememberSaveable { mutableStateOf("") }

    if (passwordTargetUser != null) {
        val target = passwordTargetUser!!
        AlertDialog(
            onDismissRequest = { passwordTargetUser = null },
            title = { Text("تنظیم رمز عبور ورود (${target.fullName.ifBlank { target.email }})") },
            text = {
                BazmoInput(
                    value = newWebPassword,
                    onValueChange = { newWebPassword = it },
                    placeholder = "رمز عبور جدید (حداقل ۶ کاراکتر)",
                    keyboardType = KeyboardType.Password
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.adminSetUserPassword(target, newWebPassword) {
                        newWebPassword = ""
                        passwordTargetUser = null
                    }
                }) { Text("ذخیره رمز") }
            },
            dismissButton = {
                TextButton(onClick = {
                    newWebPassword = ""
                    passwordTargetUser = null
                }) { Text("انصراف") }
            }
        )
    }

    val pendingRequests = remember(state.db.registrationRequests) {
        state.db.registrationRequests.filter { it.status == "pending" }
    }

    val uf = state.session.adminUserFilter
    val filteredUsers = remember(state.db.users, uf) {
        state.db.users.filter { u ->
            (uf.search.isBlank() ||
                u.fullName.contains(uf.search, ignoreCase = true) ||
                u.email.contains(uf.search, ignoreCase = true) ||
                u.phone.contains(uf.search, ignoreCase = true)) &&
                (uf.tier == "همه" || u.tier == uf.tier) &&
                (BazmoLocations.isAllScope(uf.province) ||
                    BazmoLocations.normalizeZwnj(u.province) == BazmoLocations.normalizeZwnj(uf.province)) &&
                (uf.statusFilter == "همه" ||
                    (uf.statusFilter == "فعال" && u.active) ||
                    (uf.statusFilter == "مسدود" && !u.active))
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        if (pendingRequests.isNotEmpty()) {
            item(key = "hdr_pending_regs") {
                BazmoSectionHeader(
                    title = "📋 درخواست‌های عضویت در انتظار تایید",
                    badgeText = "${pendingRequests.size.toPersianNumber()} مورد"
                )
            }
            items(pendingRequests, key = { "reg_${it.id}" }) { req: RegistrationRequestEntity ->
                var chosenTier by rememberSaveable(req.id) {
                    mutableStateOf(BazmoRoles.normalizeTier(req.requestedTier))
                }
                BazmoCard(borderColor = GoldAccent, borderWidth = 1.5f) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = req.fullName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.weight(1f)
                        )
                        BazmoBadge("⏳ منتظر احراز هویت", BadgeTone.GOLD)
                    }
                    Text(
                        text = "${req.email} | 📞 ${req.phone}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 5.dp)
                    )
                    Text(
                        text = "📍 ${req.province}، ${req.city} (${req.region})",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    if (req.note.isNotBlank()) {
                        BazmoWarrantyBox(
                            text = "📝 ${req.note}",
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "تعیین نقش صنفی جهت تایید نهایی:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Navy,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    BazmoDropdownSelector(
                        selectedValue = chosenTier,
                        options = BazmoRoles.ALL_ROLES,
                        onSelect = { chosenTier = it },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BazmoSuccessButton(
                            text = "✓ تایید و فعال‌سازی",
                            onClick = { viewModel.adminApproveRegistration(req, chosenTier) },
                            compact = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("approve_reg_${req.id}")
                        )
                        BazmoDangerButton(
                            text = "✕ رد درخواست",
                            onClick = { viewModel.adminRejectRegistration(req.id) },
                            compact = true,
                            modifier = Modifier.testTag("reject_reg_${req.id}")
                        )
                    }
                }
            }
        }

        item {
            BazmoSectionHeader("➕ تعریف کاربر جدید در سیستم")
            BazmoCard {
                BazmoInput(
                    value = newUEmail,
                    onValueChange = { newUEmail = it },
                    placeholder = "ایمیل معتبر کاربر (نام کاربری جهت ورود)",
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    BazmoInput(newUFull, { newUFull = it }, "نام و نام خانوادگی", modifier = Modifier.weight(1f))
                    BazmoInput(newUPhone, { newUPhone = it }, "شماره موبایل", keyboardType = KeyboardType.Phone, modifier = Modifier.weight(1f))
                }
                BazmoInput(
                    value = newUPassword,
                    onValueChange = { newUPassword = it },
                    placeholder = "رمز عبور ورود کاربر (حداقل ۶ کاراکتر - اختیاری)",
                    keyboardType = KeyboardType.Password,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BazmoDropdownSelector(
                    selectedValue = newUTier,
                    options = BazmoRoles.ALL_ROLES,
                    onSelect = { newUTier = it },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    BazmoDropdownSelector(
                        selectedValue = newUProv,
                        options = provs,
                        onSelect = { p ->
                            newUProv = p
                            val firstC = BazmoLocations.citiesFor(p).firstOrNull() ?: ""
                            newUCity = firstC
                            newURegion = BazmoLocations.regionsFor(p, firstC).firstOrNull() ?: "همه مناطق"
                        },
                        modifier = Modifier.weight(1f)
                    )
                    BazmoDropdownSelector(
                        selectedValue = newUCity,
                        options = BazmoLocations.citiesFor(newUProv),
                        onSelect = { c ->
                            newUCity = c
                            newURegion = BazmoLocations.regionsFor(newUProv, c).firstOrNull() ?: "همه مناطق"
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                BazmoDropdownSelector(
                    selectedValue = newURegion,
                    options = BazmoLocations.regionsFor(newUProv, newUCity),
                    onSelect = { newURegion = it },
                    modifier = Modifier.padding(bottom = 10.dp)
                )
                BazmoPrimaryButton(
                    text = "✓ ثبت و فعال‌سازی کاربر",
                    onClick = {
                        viewModel.adminCreateUser(newUEmail, newUFull, newUPhone, newUTier, newUProv, newUCity, newURegion, newUPassword) {
                            newUEmail = ""
                            newUFull = ""
                            newUPhone = ""
                            newUPassword = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            BazmoSectionHeader("لیست کاربران (${filteredUsers.size.toPersianNumber()})")
            BazmoCard {
                BazmoSearchBar(
                    value = uf.search,
                    onValueChange = { q -> viewModel.updateAdminUserFilter { it.copy(search = q) } },
                    placeholder = "جستجوی نام، ایمیل یا تلفن...",
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    BazmoDropdownSelector(
                        selectedValue = if (uf.tier == "همه") "همه نقش‌ها" else uf.tier,
                        options = listOf("همه نقش‌ها") + BazmoRoles.ALL_ROLES,
                        onSelect = { choice ->
                            val t = if (choice == "همه نقش‌ها") "همه" else choice
                            viewModel.updateAdminUserFilter { it.copy(tier = t) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    BazmoDropdownSelector(
                        selectedValue = if (uf.statusFilter == "همه") "همه وضعیت‌ها" else uf.statusFilter,
                        options = listOf("همه وضعیت‌ها", "فعال", "مسدود"),
                        onSelect = { choice ->
                            val st = if (choice == "همه وضعیت‌ها") "همه" else choice
                            viewModel.updateAdminUserFilter { it.copy(statusFilter = st) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                BazmoDropdownSelector(
                    selectedValue = uf.province,
                    options = listOf("همه استان‌ها", BazmoLocations.ALL_COUNTRY) + provs,
                    onSelect = { p -> viewModel.updateAdminUserFilter { it.copy(province = p) } }
                )
            }
        }

        items(filteredUsers, key = { it.id.ifBlank { it.uid }.ifBlank { it.email } }) { u ->
            val targetId = u.id.ifBlank { u.uid }
            val isPendingInvite = u.uid.startsWith("u_")
            BazmoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = u.fullName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        BazmoBadge(u.tier, BadgeTone.BLUE)
                    }
                    BazmoBadge(if (u.active) "فعال" else "مسدود", if (u.active) BadgeTone.GREEN else BadgeTone.RED)
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(top = 6.dp)
                ) {
                    BazmoBadge(
                        text = if (isPendingInvite) "⏳ در انتظار اولین ورود" else "✓ متصل به سیستم",
                        tone = if (isPendingInvite) BadgeTone.GOLD else BadgeTone.PURPLE
                    )
                    if (BazmoRoles.canCreateStore(u.tier, isAdmin = false)) {
                        BazmoBadge("🏪 مجاز به ثبت فروشگاه", BadgeTone.GREEN)
                    }
                }
                Text(
                    text = "${u.email.ifBlank { "شناسه: ${u.uid.take(8)}" }} | 📞 ${u.phone.ifBlank { "-" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 6.dp)
                )
                Text(
                    text = "📍 ${u.province}، ${u.city} (${u.region})",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )
                if (u.tier != "ادمین") {
                    Text(
                        text = "تغییر نقش کاربر:",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    BazmoDropdownSelector(
                        selectedValue = u.tier,
                        options = BazmoRoles.ALL_ROLES,
                        onSelect = { newRole ->
                            if (newRole != u.tier) {
                                viewModel.adminUpdateUserTier(u, newRole)
                            }
                        },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (u.active) {
                        BazmoGoldButton(
                            text = "🚫 مسدود",
                            onClick = { viewModel.adminToggleUserActive(u) },
                            compact = true,
                            modifier = Modifier.testTag("block_user_button_$targetId")
                        )
                    } else {
                        BazmoSuccessButton(
                            text = "✓ فعال‌سازی",
                            onClick = { viewModel.adminToggleUserActive(u) },
                            compact = true,
                            modifier = Modifier.testTag("reactivate_user_button_$targetId")
                        )
                    }
                    BazmoSoftButton(
                        text = "🔑 رمز وب",
                        onClick = {
                            newWebPassword = ""
                            passwordTargetUser = u
                        },
                        compact = true,
                        modifier = Modifier.testTag("set_password_button_$targetId")
                    )
                    BazmoDangerButton(
                        text = "🗑️ حذف",
                        onClick = { viewModel.adminDeleteUser(targetId) },
                        compact = true
                    )
                }
            }
        }
    }
}

@Composable
private fun AdminNotificationsTab(state: BazmoUiState, viewModel: BazmoViewModel) {
    val nf = state.session.adminNotifFilter
    val provs = listOf("همه استان‌ها", BazmoLocations.ALL_COUNTRY) + BazmoLocations.provinces()
    val cities = if (BazmoLocations.isAllScope(nf.province)) listOf("همه شهرها") else listOf("همه شهرها") + BazmoLocations.citiesFor(nf.province)
    val regions = if (!BazmoLocations.isAllScope(nf.province) && !BazmoLocations.isAllScope(nf.city)) BazmoLocations.regionsFor(nf.province, nf.city) else listOf("همه مناطق")
    val targetCount = state.targetedUsersCount(nf)

    val typeOptions = listOf(
        "اطلاعیه صنفی" to "📢 اطلاعیه صنفی",
        "فرصت خرید ویژه" to "🔥 فرصت خرید ویژه",
        "هشدار و موجودی قطعه" to "🧩 هشدار و موجودی قطعه",
        "آپدیت نرم‌افزار و باکس" to "💻 آپدیت نرم‌افزار و باکس"
    )
    var selectedType by rememberSaveable { mutableStateOf(typeOptions.first().first) }
    var title by rememberSaveable { mutableStateOf("") }
    var body by rememberSaveable { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item {
            BazmoSectionHeader("🔔 ارسال نوتیفیکیشن هدفمند")
            BazmoCard {
                Text(
                    text = "فیلتر مخاطبان بر اساس موقعیت و صنف:",
                    style = MaterialTheme.typography.titleSmall,
                    color = Navy,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    BazmoDropdownSelector(
                        selectedValue = nf.province,
                        options = provs,
                        onSelect = { p ->
                            viewModel.updateAdminNotifFilter { it.copy(province = p, city = "همه شهرها", region = "همه مناطق") }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    BazmoDropdownSelector(
                        selectedValue = nf.city,
                        options = cities,
                        onSelect = { c ->
                            viewModel.updateAdminNotifFilter { it.copy(city = c, region = "همه مناطق") }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    BazmoDropdownSelector(
                        selectedValue = nf.region,
                        options = regions,
                        onSelect = { r -> viewModel.updateAdminNotifFilter { it.copy(region = r) } },
                        modifier = Modifier.weight(1f)
                    )
                    val tierLabels = listOf("همه" to "همه نقش‌ها") + BazmoRoles.ALL_ROLES.map { it to "فقط $it" }
                    val curTierLabel = tierLabels.find { it.first == nf.tierFilter }?.second ?: "همه نقش‌ها"
                    BazmoDropdownSelector(
                        selectedValue = curTierLabel,
                        options = tierLabels.map { it.second },
                        onSelect = { chosen ->
                            val raw = tierLabels.find { it.second == chosen }?.first ?: "همه"
                            viewModel.updateAdminNotifFilter { it.copy(tierFilter = raw) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                }

                BazmoWarrantyBox(
                    text = "🎯 تعداد کاربران فعال منطبق با این فیلتر: ${targetCount.toPersianNumber()} کاربر",
                    modifier = Modifier.padding(bottom = 10.dp)
                )

                val curTypeLabel = typeOptions.find { it.first == selectedType }?.second ?: typeOptions.first().second
                BazmoDropdownSelector(
                    selectedValue = curTypeLabel,
                    options = typeOptions.map { it.second },
                    onSelect = { chosen ->
                        selectedType = typeOptions.find { it.second == chosen }?.first ?: "اطلاعیه صنفی"
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BazmoInput(title, { title = it }, "عنوان نوتیفیکیشن...", modifier = Modifier.padding(bottom = 8.dp))
                BazmoInput(body, { body = it }, "متن کامل پیام ارسالی به کاربران...", singleLine = false, minLines = 3, modifier = Modifier.padding(bottom = 10.dp))
                BazmoPrimaryButton(
                    text = "🚀 ارسال فوری نوتیفیکیشن",
                    onClick = {
                        if (viewModel.adminSendNotification(selectedType, title, body)) {
                            title = ""
                            body = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            BazmoSectionHeader("تاریخچه نوتیفیکیشن‌های ارسالی (${state.db.notifications.size.toPersianNumber()})")
        }
        items(state.db.notifications, key = { it.id }) { n ->
            BazmoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BazmoBadge(n.type, BadgeTone.BLUE)
                    BazmoDangerButton("حذف", { viewModel.adminDeleteNotification(n.id) }, compact = true)
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(n.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                Text(n.body, style = MaterialTheme.typography.bodySmall, color = TextMuted, modifier = Modifier.padding(vertical = 4.dp))
                BazmoBadge("📍 ${n.province} / ${n.city} / ${n.region} • نقش: ${n.tierFilter}", BadgeTone.GOLD)
            }
        }
    }
}

@Composable
private fun AdminAdsStoresTab(state: BazmoUiState, viewModel: BazmoViewModel) {
    var stName by rememberSaveable { mutableStateOf("") }
    var stProv by rememberSaveable { mutableStateOf("") }
    var stCity by rememberSaveable { mutableStateOf("") }
    var stRegion by rememberSaveable { mutableStateOf("") }
    var stPhone by rememberSaveable { mutableStateOf("") }
    val storeTypes = listOf(
        "both" to "موبایل و جانبی",
        "mobile" to "فقط موبایل",
        "acc" to "فقط جانبی",
        "hw" to "فروشگاه قطعات سخت‌افزار"
    )
    var stType by rememberSaveable { mutableStateOf("both") }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item { BazmoSectionHeader("تایید / حذف آگهی‌های همکاران") }
        items(state.db.hwAds, key = { it.id }) { ad ->
            val (label, tone) = when (ad.status) {
                "approved" -> "تایید شده" to BadgeTone.GREEN
                "pending" -> "در انتظار" to BadgeTone.GOLD
                else -> "رد شده" to BadgeTone.RED
            }
            val secLabel = when (ad.section) {
                "mobile" -> "📱 موبایل"
                "acc" -> "🎧 جانبی"
                else -> "🔧 سخت‌افزار"
            }
            BazmoCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(ad.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                    Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                        BazmoBadge(secLabel, BadgeTone.BLUE)
                        BazmoBadge(label, tone)
                    }
                }
                Text(
                    text = "${ad.seller} | ${ad.city} | ${ad.price.formatToman()}",
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

        item { BazmoSectionHeader("آموزش‌ها و باکس‌های نرم‌افزار") }
        items(state.db.tutorials, key = { it.id }) { t ->
            BazmoCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🎓 ${t.title}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    BazmoBadge(t.status, if (t.status == "approved") BadgeTone.GREEN else BadgeTone.GOLD)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BazmoSuccessButton("✓ تایید", { viewModel.adminApproveTutorial(t) }, compact = true)
                    BazmoDangerButton("🗑️ حذف", { viewModel.adminDeleteTutorial(t.id) }, compact = true)
                }
            }
        }
        items(state.db.boxes, key = { it.id }) { b ->
            BazmoCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("🧰 ${b.name}", style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                    BazmoBadge(b.status, if (b.status == "approved") BadgeTone.GREEN else BadgeTone.GOLD)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BazmoSuccessButton("✓ تایید", { viewModel.adminApproveBox(b) }, compact = true)
                    BazmoDangerButton("🗑️ حذف", { viewModel.adminDeleteBox(b.id) }, compact = true)
                }
            }
        }

        item {
            BazmoSectionHeader("🏪 مدیریت و افزودن فروشگاه‌ها")
            BazmoCard {
                BazmoInput(stName, { stName = it }, "نام فروشگاه", modifier = Modifier.padding(bottom = 8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                    BazmoInput(stProv, { stProv = it }, "استان (مثلاً تهران)", modifier = Modifier.weight(1f))
                    BazmoInput(stCity, { stCity = it }, "شهر (مثلاً تهران)", modifier = Modifier.weight(1f))
                }
                BazmoInput(stRegion, { stRegion = it }, "منطقه (مثلاً علاءالدین و جمهوری)", modifier = Modifier.padding(bottom = 8.dp))
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
                            stProv = ""
                            stCity = ""
                            stRegion = ""
                            stPhone = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        items(state.db.stores, key = { it.id }) { s ->
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

        item {
            BazmoSectionHeader("🪑 آگهی‌های اجاره میز کار (${state.db.deskRentals.size.toPersianNumber()})")
        }
        items(state.db.deskRentals, key = { it.id }) { d ->
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
    }
}

@Composable
private fun AdminRequestsTab(state: BazmoUiState, viewModel: BazmoViewModel) {
    val orderSteps = listOf("ارسال شده به فروشگاه", "در حال پردازش", "در حال ارسال")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item { BazmoSectionHeader("📦 مدیریت سفارش‌ها (${state.db.orders.size.toPersianNumber()})") }
        items(state.db.orders, key = { it.id }) { o ->
            BazmoCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${o.id} (${o.deliveryType})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                    BazmoBadge(o.status, BadgeTone.BLUE)
                }
                Text(
                    text = "مشتری: ${o.customer.firstName} ${o.customer.lastName} | 📞 ${o.customer.phone}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Text(
                    text = o.total.formatToman(),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Black,
                    color = Navy,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    BazmoDropdownSelector(
                        selectedValue = o.status,
                        options = orderSteps,
                        onSelect = { viewModel.adminUpdateOrderStatus(o, it) },
                        modifier = Modifier.weight(1f)
                    )
                    BazmoDangerButton("حذف", { viewModel.adminDeleteOrder(o.id) }, compact = true)
                }
            }
        }

        item { BazmoSectionHeader("🔔 استعلام‌های موجودی جانبی") }
        items(state.db.inquiries, key = { it.id }) { inq ->
            BazmoCard {
                Text(inq.accName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                Text("${inq.storeName} | وضعیت: ${inq.status}", style = MaterialTheme.typography.bodySmall, color = TextMuted, modifier = Modifier.padding(vertical = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BazmoSuccessButton("✓ موجود شد", { viewModel.adminReplyInquiry(inq, "موجود شد! آماده ثبت سفارش") }, compact = true)
                    BazmoDangerButton("✕ ناموجود", { viewModel.adminReplyInquiry(inq, "ناموجود") }, compact = true)
                }
            }
        }

        item { BazmoSectionHeader("🧩 درخواست‌های تخصصی قطعه سخت‌افزار") }
        items(state.db.hwRequests, key = { it.id }) { hr ->
            var noteText by rememberSaveable(hr.id) { mutableStateOf(hr.note) }
            BazmoCard {
                Text("${hr.brand} ${hr.model} (${hr.partType})", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                Text("سری/کد: ${hr.series} | شهر: ${hr.city}", style = MaterialTheme.typography.bodySmall, color = TextMuted, modifier = Modifier.padding(vertical = 4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    BazmoInput(noteText, { noteText = it }, "پاسخ و قیمت...", modifier = Modifier.weight(1f))
                    BazmoPrimaryButton("ثبت", { viewModel.adminReplyHwRequest(hr, noteText) }, compact = true)
                }
            }
        }

        item { BazmoSectionHeader("📋 درخواست‌های نرم‌افزار") }
        items(state.db.swRequests, key = { it.id }) { sr ->
            var replyText by rememberSaveable(sr.id) { mutableStateOf(sr.reply) }
            BazmoCard {
                Text(sr.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    BazmoInput(replyText, { replyText = it }, "پاسخ ادمین...", modifier = Modifier.weight(1f))
                    BazmoPrimaryButton("ارسال", { viewModel.adminReplySwRequest(sr, replyText) }, compact = true)
                }
            }
        }
    }
}

@Composable
private fun AdminCatalogForumTab(state: BazmoUiState, viewModel: BazmoViewModel) {
    var mobBrand by rememberSaveable { mutableStateOf("") }
    var mobModel by rememberSaveable { mutableStateOf("") }
    var mobPrice by rememberSaveable { mutableStateOf("") }
    var mobInfo by rememberSaveable { mutableStateOf("") }

    var accCat by rememberSaveable { mutableStateOf("") }
    var accName by rememberSaveable { mutableStateOf("") }
    var accPrice by rememberSaveable { mutableStateOf("") }

    var showResetDialog by remember { mutableStateOf(false) }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("بازنشانی داده‌ها") },
            text = { Text("آیا مطمئن هستید تمام داده‌ها به حالت اولیه بازگردانده شوند؟") },
            confirmButton = {
                TextButton(onClick = {
                    showResetDialog = false
                    viewModel.adminResetDatabase()
                }) { Text("بله، بازنشانی شود") }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) { Text("خیر") }
            }
        )
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item {
            BazmoSectionHeader("📱 افزودن موبایل جدید")
            BazmoCard {
                BazmoInput(mobBrand, { mobBrand = it }, "برند (مثلاً Samsung)", modifier = Modifier.padding(bottom = 8.dp))
                BazmoInput(mobModel, { mobModel = it }, "نام مدل", modifier = Modifier.padding(bottom = 8.dp))
                BazmoInput(mobPrice, { mobPrice = it }, "قیمت (تومان)", keyboardType = KeyboardType.Number, modifier = Modifier.padding(bottom = 8.dp))
                BazmoInput(mobInfo, { mobInfo = it }, "گارانتی و اقلام همراه", modifier = Modifier.padding(bottom = 10.dp))
                BazmoPrimaryButton(
                    text = "+ افزودن به بخش موبایل",
                    onClick = {
                        if (viewModel.adminAddMobile(mobBrand, mobModel, mobPrice, mobInfo)) {
                            mobBrand = ""
                            mobModel = ""
                            mobPrice = ""
                            mobInfo = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item {
            BazmoSectionHeader("🎧 افزودن محصول جانبی جدید")
            BazmoCard {
                BazmoInput(accCat, { accCat = it }, "دسته‌بندی (مثلاً پاوربانک)", modifier = Modifier.padding(bottom = 8.dp))
                BazmoInput(accName, { accName = it }, "نام محصول جانبی", modifier = Modifier.padding(bottom = 8.dp))
                BazmoInput(accPrice, { accPrice = it }, "قیمت (تومان)", keyboardType = KeyboardType.Number, modifier = Modifier.padding(bottom = 10.dp))
                BazmoAccentButton(
                    text = "+ افزودن محصول جانبی",
                    onClick = {
                        if (viewModel.adminAddAccessory(accCat, accName, accPrice)) {
                            accCat = ""
                            accName = ""
                            accPrice = ""
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        item { BazmoSectionHeader("💬 مدیریت انجمن‌های سخت‌افزار و نرم‌افزار") }
        items(state.db.forumPosts, key = { it.id }) { p ->
            BazmoCard {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    BazmoBadge(
                        text = if (p.board == "hardware") "سخت‌افزار" else "نرم‌افزار",
                        tone = if (p.board == "hardware") BadgeTone.GOLD else BadgeTone.BLUE
                    )
                    Text(
                        text = "▲ ${p.votes.toPersianNumber()} | 💬 ${countAllComments(p).toPersianNumber()}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(p.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    BazmoSoftButton(
                        text = if (p.pinned) "برداشتن سنجاق" else "📌 سنجاق",
                        onClick = { viewModel.adminTogglePinPost(p) },
                        compact = true
                    )
                    BazmoSuccessButton(
                        text = if (p.verifiedSolution) "حذف تاییدیه" else "✅ تایید راه حل",
                        onClick = { viewModel.adminToggleVerifyPost(p) },
                        compact = true
                    )
                    BazmoDangerButton(
                        text = "🗑️ حذف",
                        onClick = { viewModel.adminDeleteForumPost(p.id) },
                        compact = true
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            BazmoOutlineButton(
                text = "🔄 بازنشانی داده‌های پایه",
                onClick = { showResetDialog = true },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
