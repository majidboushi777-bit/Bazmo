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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
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
import com.example.ui.components.BazmoEmptyState
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
import com.example.ui.theme.PrimaryFill
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
                beyondViewportPageCount = 2,
                flingBehavior = PagerDefaults.flingBehavior(
                    state = pagerState,
                    snapPositionalThreshold = 0.22f
                ),
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

private data class DashboardKpiMeta(
    val value: String,
    val label: String,
    val emoji: String,
    val accentColor: Color,
    val tag: String,
    val onClick: () -> Unit
)

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
    val activeBannersCount = remember(state.db.banners) {
        state.db.banners.count { it.hasImage }
    }
    val totalSales = remember(state.db.orders) {
        state.db.orders.sumOf { it.total }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val kpiCols = if (maxWidth >= 680.dp) 3 else 2
        val kpiItems = listOf(
            DashboardKpiMeta(
                value = state.db.users.size.toPersianNumber(),
                label = "کل کاربران ثبت‌شده",
                emoji = "👥",
                accentColor = Navy,
                tag = "dash_kpi_users",
                onClick = { viewModel.navigateToAdminSection(AdminTab.USERS, usersSubTab = "users") }
            ),
            DashboardKpiMeta(
                value = pendingRegCount.toPersianNumber(),
                label = "درخواست عضویت در انتظار",
                emoji = "📝",
                accentColor = GoldAccent,
                tag = "dash_kpi_reg_requests",
                onClick = { viewModel.navigateToAdminSection(AdminTab.USERS, usersSubTab = "requests") }
            ),
            DashboardKpiMeta(
                value = pendingAdsCount.toPersianNumber(),
                label = "آگهی و فروشگاه در انتظار",
                emoji = "🛡️",
                accentColor = GoldAccent,
                tag = "dash_kpi_pending_ads",
                onClick = { viewModel.navigateToAdminSection(AdminTab.ADS, adsSection = "hw") }
            ),
            DashboardKpiMeta(
                value = state.db.orders.size.toPersianNumber(),
                label = "سفارش‌ها و استعلام‌ها",
                emoji = "📦",
                accentColor = SuccessGreen,
                tag = "dash_kpi_orders",
                onClick = { viewModel.navigateToAdminSection(AdminTab.REQUESTS) }
            ),
            DashboardKpiMeta(
                value = state.db.notifications.size.toPersianNumber(),
                label = "نوتیفیکیشن ارسالی",
                emoji = "🔔",
                accentColor = Navy,
                tag = "dash_kpi_notifs",
                onClick = { viewModel.navigateToAdminSection(AdminTab.NOTIFICATIONS) }
            ),
            DashboardKpiMeta(
                value = state.db.stores.count { it.status == "approved" }.toPersianNumber(),
                label = "فروشگاه‌های تاییدشده",
                emoji = "🏪",
                accentColor = SuccessGreen,
                tag = "dash_kpi_stores",
                onClick = { viewModel.navigateToAdminSection(AdminTab.ADS, adsSection = "stores") }
            )
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 12.dp, bottom = 24.dp)
        ) {
            item(key = "dash_hero") {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    shadowElevation = 6.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                        .clip(RoundedCornerShape(22.dp))
                        .clickable { viewModel.navigateToAdminSection(AdminTab.REQUESTS) }
                ) {
                    Column(
                        modifier = Modifier
                            .background(Brush.linearGradient(listOf(HeaderBgDeep, HeaderBgLight)))
                            .padding(18.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "مجموع گردش مالی سفارش‌ها",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFDBEAFE)
                            )
                            BazmoBadge("مشاهده سفارش‌ها ←", BadgeTone.GOLD)
                        }
                        Text(
                            text = totalSales.formatToman(),
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Black,
                            color = Color.White,
                            modifier = Modifier.padding(top = 6.dp, bottom = 10.dp)
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Surface(
                                color = Color.White.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.navigateToAdminSection(AdminTab.USERS, usersSubTab = "requests") }
                            ) {
                                Text(
                                    text = "📋 ${pendingRegCount.toPersianNumber()} عضویت منتظر",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                                    maxLines = 1
                                )
                            }
                            Surface(
                                color = Color.White.copy(alpha = 0.14f),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable { viewModel.navigateToAdminSection(AdminTab.ADS, adsSection = "banners") }
                            ) {
                                Text(
                                    text = "🖼️ ${activeBannersCount.toPersianNumber()} بنر فعال",
                                    color = GoldAccent,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 7.dp),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            item(key = "dash_kpis") {
                BazmoSectionHeader("آمار و هدایت سریع (برای ورود لمس کنید)")
                kpiItems.chunked(kpiCols).forEachIndexed { idx, rowItems ->
                    if (idx > 0) Spacer(modifier = Modifier.height(2.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        rowItems.forEach { kpi ->
                            KpiCard(
                                value = kpi.value,
                                label = kpi.label,
                                emoji = kpi.emoji,
                                color = kpi.accentColor,
                                onClick = kpi.onClick,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag(kpi.tag)
                            )
                        }
                        repeat(kpiCols - rowItems.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            item(key = "dash_quick_sections") {
                BazmoSectionHeader("دسترسی مستقیم بخش‌ها")
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        BazmoPrimaryButton(
                            text = if (pendingRegCount > 0) "📝 درخواست‌ها (${pendingRegCount.toPersianNumber()})" else "👥 مدیریت کاربران",
                            onClick = {
                                viewModel.navigateToAdminSection(
                                    AdminTab.USERS,
                                    usersSubTab = if (pendingRegCount > 0) "requests" else "users"
                                )
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BazmoGoldButton(
                            text = "🖼️ ۴ بنر تبلیغاتی",
                            onClick = { viewModel.navigateToAdminSection(AdminTab.ADS, adsSection = "banners") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        BazmoSoftButton(
                            text = "🛡️ تایید آگهی (${pendingAdsCount.toPersianNumber()})",
                            onClick = { viewModel.navigateToAdminSection(AdminTab.ADS, adsSection = "hw") },
                            modifier = Modifier.weight(1f)
                        )
                        BazmoSoftButton(
                            text = "🔔 ارسال اعلان",
                            onClick = { viewModel.navigateToAdminSection(AdminTab.NOTIFICATIONS) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                        BazmoSoftButton(
                            text = "🏪 فروشگاه‌ها",
                            onClick = { viewModel.navigateToAdminSection(AdminTab.ADS, adsSection = "stores") },
                            modifier = Modifier.weight(1f)
                        )
                        BazmoSoftButton(
                            text = "🧩 کاتالوگ و انجمن",
                            onClick = { viewModel.navigateToAdminSection(AdminTab.CATALOG) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            item(key = "dash_demo") {
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
        }
    }
}

@Composable
private fun KpiCard(
    value: String,
    label: String,
    emoji: String,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BazmoCard(
        onClick = onClick,
        borderColor = color.copy(alpha = 0.35f),
        borderWidth = 1.2f,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, fontSize = 18.sp)
                Text(text = "←", fontSize = 13.sp, color = TextMuted, fontWeight = FontWeight.Bold)
            }
            Text(text = value, fontSize = 23.sp, fontWeight = FontWeight.Black, color = color)
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun AdminUsersTab(state: BazmoUiState, viewModel: BazmoViewModel) {
    val provs = BazmoLocations.provinces()
    var newUProv by rememberSaveable { mutableStateOf("تهران") }
    var newUCity by rememberSaveable { mutableStateOf("تهران") }
    var newURegion by rememberSaveable { mutableStateOf("علاءالدین و جمهوری") }
    var newUEmail by rememberSaveable { mutableStateOf("") }
    var newUFull by rememberSaveable { mutableStateOf("") }
    var newUPhone by rememberSaveable { mutableStateOf("") }
    var newUPassword by rememberSaveable { mutableStateOf("") }
    var newUTier by rememberSaveable { mutableStateOf(BazmoRoles.REPAIR_TECH) }

    var passwordTargetUser by remember { mutableStateOf<UserEntity?>(null) }
    var newWebPassword by rememberSaveable { mutableStateOf("") }

    var editLocationUser by remember { mutableStateOf<UserEntity?>(null) }
    var editLocProv by remember { mutableStateOf("تهران") }
    var editLocCity by remember { mutableStateOf("تهران") }
    var editLocRegion by remember { mutableStateOf("همه مناطق") }
    var editLocFull by remember { mutableStateOf("") }
    var editLocPhone by remember { mutableStateOf("") }

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

    if (editLocationUser != null) {
        val target = editLocationUser!!
        AlertDialog(
            onDismissRequest = { editLocationUser = null },
            title = { Text("📍 ویرایش آدرس و مشخصات (${target.fullName.ifBlank { target.email }})") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    BazmoDropdownSelector(
                        label = "استان:",
                        selectedValue = editLocProv,
                        options = provs,
                        onSelect = { p ->
                            editLocProv = p
                            val firstC = BazmoLocations.citiesFor(p).firstOrNull() ?: ""
                            editLocCity = firstC
                            editLocRegion = BazmoLocations.regionsFor(p, firstC).firstOrNull() ?: "همه مناطق"
                        }
                    )
                    BazmoDropdownSelector(
                        label = "شهر:",
                        selectedValue = editLocCity,
                        options = BazmoLocations.citiesFor(editLocProv),
                        onSelect = { c ->
                            editLocCity = c
                            editLocRegion = BazmoLocations.regionsFor(editLocProv, c).firstOrNull() ?: "همه مناطق"
                        }
                    )
                    BazmoDropdownSelector(
                        label = "منطقه / محله:",
                        selectedValue = editLocRegion,
                        options = BazmoLocations.regionsFor(editLocProv, editLocCity),
                        onSelect = { editLocRegion = it }
                    )
                    BazmoInput(editLocFull, { editLocFull = it }, "نام و نام خانوادگی")
                    BazmoInput(editLocPhone, { editLocPhone = it }, "شماره تماس", keyboardType = KeyboardType.Phone)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.adminUpdateUserLocation(
                        user = target,
                        province = editLocProv,
                        city = editLocCity,
                        region = editLocRegion,
                        fullName = editLocFull,
                        phone = editLocPhone
                    ) {
                        editLocationUser = null
                    }
                }) { Text("ذخیره تغییرات") }
            },
            dismissButton = {
                TextButton(onClick = { editLocationUser = null }) { Text("انصراف") }
            }
        )
    }

    val pendingRequests = remember(state.db.registrationRequests) {
        state.db.registrationRequests.filter { it.status == "pending" }
    }
    val historyRequests = remember(state.db.registrationRequests) {
        state.db.registrationRequests.filter { it.status != "pending" }
    }

    val uf = state.session.adminUserFilter
    val filteredUsers = remember(state.db.users, uf) {
        val normSearch = BazmoLocations.normalizeZwnj(uf.search.lowercase())
        val rawSearch = uf.search.trim().lowercase()
        val normFilterTier = BazmoLocations.normalizeZwnj(uf.tier)
        val normFilterProv = BazmoLocations.normalizeZwnj(uf.province)
        val normFilterCity = BazmoLocations.normalizeZwnj(uf.city)
        val provAll = BazmoLocations.isAllScope(uf.province)
        val cityAll = provAll || BazmoLocations.isAllScope(uf.city)
        val tierAll = BazmoLocations.isAllScope(uf.tier)
        val statusAll = BazmoLocations.isAllScope(uf.statusFilter)
        state.db.users.filter { u ->
            val searchOk = normSearch.isBlank() ||
                BazmoLocations.normalizeZwnj(u.fullName.lowercase()).contains(normSearch) ||
                BazmoLocations.normalizeZwnj(u.email.lowercase()).contains(normSearch) ||
                BazmoLocations.normalizeZwnj(u.phone).contains(normSearch) ||
                BazmoLocations.normalizeZwnj(u.city.lowercase()).contains(normSearch) ||
                BazmoLocations.normalizeZwnj(u.province.lowercase()).contains(normSearch) ||
                BazmoLocations.normalizeZwnj(u.tier.lowercase()).contains(normSearch) ||
                u.fullName.contains(rawSearch, ignoreCase = true) ||
                u.email.contains(rawSearch, ignoreCase = true)
            val tierOk = tierAll || BazmoLocations.normalizeZwnj(u.tier) == normFilterTier
            val provOk = provAll || BazmoLocations.normalizeZwnj(u.province) == normFilterProv
            val cityOk = cityAll || BazmoLocations.normalizeZwnj(u.city) == normFilterCity
            val statusOk = statusAll ||
                (uf.statusFilter == "فعال" && u.active) ||
                (uf.statusFilter == "مسدود" && !u.active)
            searchOk && tierOk && provOk && cityOk && statusOk
        }
    }

    val subTab = state.session.adminUsersSubTab

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item(key = "users_subtab_switcher") {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    color = if (subTab == "users") PrimaryFill else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, if (subTab == "users") PrimaryFill else NavyLight.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.setAdminUsersSubTab("users") }
                        .testTag("admin_users_subtab_users")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                        Text(
                            text = "👥 کاربران (${filteredUsers.size.toPersianNumber()})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (subTab == "users") Color.White else NavyDeep,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Surface(
                    color = if (subTab == "requests") PrimaryFill else MaterialTheme.colorScheme.surface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, if (subTab == "requests") PrimaryFill else NavyLight.copy(alpha = 0.35f)),
                    modifier = Modifier
                        .weight(1f)
                        .heightIn(min = 44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.setAdminUsersSubTab("requests") }
                        .testTag("admin_users_subtab_requests")
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(horizontal = 8.dp, vertical = 8.dp)) {
                        Text(
                            text = "📋 درخواست عضویت (${pendingRequests.size.toPersianNumber()})",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (subTab == "requests") Color.White else NavyDeep,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }

        if (subTab == "requests") {
            item(key = "hdr_pending_regs") {
                BazmoSectionHeader(
                    title = "📋 درخواست‌های عضویت در انتظار تایید",
                    badgeText = "${pendingRequests.size.toPersianNumber()} مورد"
                )
            }
            if (pendingRequests.isEmpty()) {
                item(key = "empty_pending_regs") {
                    BazmoEmptyState("در حال حاضر درخواست عضویت منتظر تایید وجود ندارد.")
                }
            } else {
                itemsIndexed(pendingRequests, key = { idx, req -> "reg_${idx}_${req.id}" }) { _, req: RegistrationRequestEntity ->
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

            if (historyRequests.isNotEmpty()) {
                item(key = "hdr_history_regs") {
                    BazmoSectionHeader(
                        title = "سوابق درخواست‌های بررسی‌شده",
                        badgeText = "${historyRequests.size.toPersianNumber()} مورد"
                    )
                }
                itemsIndexed(historyRequests, key = { idx, req -> "reghist_${idx}_${req.id}" }) { _, req ->
                    val approved = req.status == "approved"
                    BazmoCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(req.fullName, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold, modifier = Modifier.weight(1f))
                            BazmoBadge(if (approved) "تایید شده" else "رد شده", if (approved) BadgeTone.GREEN else BadgeTone.RED)
                        }
                        Text(
                            text = "${req.email} | 📍 ${req.province}، ${req.city} | نقش: ${req.requestedTier}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        } else {
            item(key = "create_user_card") {
                BazmoSectionHeader("➕ تعریف کاربر جدید در سیستم")
                BazmoCard {
                    Text(
                        text = "۱. ابتدا استان، شهر و منطقه کاربر را مشخص کنید:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Navy,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                        BazmoDropdownSelector(
                            label = "استان:",
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
                            label = "شهر:",
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
                        label = "منطقه / محله:",
                        selectedValue = newURegion,
                        options = BazmoLocations.regionsFor(newUProv, newUCity),
                        onSelect = { newURegion = it },
                        modifier = Modifier.padding(bottom = 10.dp)
                    )
                    Text(
                        text = "۲. مشخصات کاربری و دسترسی:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Navy,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
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
                        label = "نقش صنفی:",
                        selectedValue = newUTier,
                        options = BazmoRoles.ALL_ROLES,
                        onSelect = { newUTier = it },
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

            item(key = "users_filter_card") {
                val filterCities = remember(uf.province) {
                    if (BazmoLocations.isAllScope(uf.province)) listOf("همه شهرها")
                    else listOf("همه شهرها") + BazmoLocations.citiesFor(uf.province)
                }
                val hasActiveFilter = uf.search.isNotBlank() ||
                    !BazmoLocations.isAllScope(uf.tier) ||
                    !BazmoLocations.isAllScope(uf.statusFilter) ||
                    !BazmoLocations.isAllScope(uf.province) ||
                    !BazmoLocations.isAllScope(uf.city)
                BazmoSectionHeader(
                    title = "لیست کاربران (${filteredUsers.size.toPersianNumber()} از ${state.db.users.size.toPersianNumber()})",
                    actionLabel = if (hasActiveFilter) "پاک کردن فیلتر" else null,
                    onAction = if (hasActiveFilter) {
                        { viewModel.updateAdminUserFilter { com.example.viewmodel.AdminUserFilter() } }
                    } else null
                )
                BazmoCard {
                    BazmoSearchBar(
                        value = uf.search,
                        onValueChange = { q -> viewModel.updateAdminUserFilter { it.copy(search = q) } },
                        placeholder = "جستجوی نام، ایمیل، تلفن یا شهر...",
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(7.dp),
                        modifier = Modifier.padding(bottom = 8.dp)
                    ) {
                        BazmoDropdownSelector(
                            selectedValue = if (BazmoLocations.isAllScope(uf.tier)) "همه نقش‌ها" else uf.tier,
                            options = listOf("همه نقش‌ها") + BazmoRoles.ALL_ROLES,
                            onSelect = { choice ->
                                val t = if (choice == "همه نقش‌ها") "همه" else choice
                                viewModel.updateAdminUserFilter { it.copy(tier = t) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        BazmoDropdownSelector(
                            selectedValue = if (BazmoLocations.isAllScope(uf.statusFilter)) "همه وضعیت‌ها" else uf.statusFilter,
                            options = listOf("همه وضعیت‌ها", "فعال", "مسدود"),
                            onSelect = { choice ->
                                val st = if (choice == "همه وضعیت‌ها") "همه" else choice
                                viewModel.updateAdminUserFilter { it.copy(statusFilter = st) }
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(7.dp)) {
                        BazmoDropdownSelector(
                            selectedValue = uf.province,
                            options = listOf("همه استان‌ها", BazmoLocations.ALL_COUNTRY) + provs,
                            onSelect = { p -> viewModel.updateAdminUserFilter { it.copy(province = p, city = "همه شهرها") } },
                            modifier = Modifier.weight(1f)
                        )
                        if (!BazmoLocations.isAllScope(uf.province)) {
                            BazmoDropdownSelector(
                                selectedValue = uf.city,
                                options = filterCities,
                                onSelect = { c -> viewModel.updateAdminUserFilter { it.copy(city = c) } },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }

            if (filteredUsers.isEmpty()) {
                item(key = "users_empty_state") {
                    BazmoEmptyState("کاربری با این فیلتر یافت نشد.")
                }
            } else {
                itemsIndexed(filteredUsers, key = { idx, u -> "usr_${idx}_${u.id.ifBlank { u.uid }.ifBlank { u.email }}" }) { _, u ->
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
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
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
                                text = "📍 آدرس",
                                onClick = {
                                    editLocProv = u.province.ifBlank { "تهران" }
                                    editLocCity = u.city.ifBlank { BazmoLocations.citiesFor(editLocProv).firstOrNull() ?: "تهران" }
                                    editLocRegion = u.region.ifBlank { "همه مناطق" }
                                    editLocFull = u.fullName
                                    editLocPhone = u.phone
                                    editLocationUser = u
                                },
                                compact = true,
                                modifier = Modifier.testTag("edit_location_button_$targetId")
                            )
                            BazmoSoftButton(
                                text = "🔑 رمز",
                                onClick = {
                                    newWebPassword = ""
                                    passwordTargetUser = u
                                },
                                compact = true,
                                modifier = Modifier.testTag("set_password_button_$targetId")
                            )
                            BazmoDangerButton(
                                text = "🗑️",
                                onClick = { viewModel.adminDeleteUser(targetId) },
                                compact = true
                            )
                        }
                    }
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
        itemsIndexed(state.db.notifications, key = { idx, n -> "notif_${idx}_${n.id}" }) { _, n ->
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
private fun AdminRequestsTab(state: BazmoUiState, viewModel: BazmoViewModel) {
    val orderSteps = listOf("ارسال شده به فروشگاه", "در حال پردازش", "در حال ارسال")

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 8.dp, bottom = 24.dp)
    ) {
        item { BazmoSectionHeader("📦 مدیریت سفارش‌ها (${state.db.orders.size.toPersianNumber()})") }
        itemsIndexed(state.db.orders, key = { idx, o -> "ord_${idx}_${o.id}" }) { _, o ->
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
        itemsIndexed(state.db.inquiries, key = { idx, inq -> "inq_${idx}_${inq.id}" }) { _, inq ->
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
        itemsIndexed(state.db.hwRequests, key = { idx, hr -> "hwreq_${idx}_${hr.id}" }) { _, hr ->
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
        itemsIndexed(state.db.swRequests, key = { idx, sr -> "swreq_${idx}_${sr.id}" }) { _, sr ->
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
        itemsIndexed(state.db.forumPosts, key = { idx, p -> "forum_${idx}_${p.id}" }) { _, p ->
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
