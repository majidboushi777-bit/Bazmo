package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.DarkMode
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.LightMode
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.BazmoLocations
import com.example.data.BazmoNotificationHelper
import com.example.data.BazmoRoles
import com.example.data.formatToman
import com.example.data.toPersianNumber
import com.example.ui.components.BadgeTone
import com.example.ui.components.BazmoBadge
import com.example.ui.components.BazmoCard
import com.example.ui.components.BazmoDropdownSelector
import com.example.ui.components.BazmoEmptyState
import com.example.ui.components.BazmoGoldButton
import com.example.ui.components.BazmoInput
import com.example.ui.components.BazmoOutlineButton
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoSoftButton
import com.example.ui.components.BazmoWarrantyBox
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BazmoThemeManager
import com.example.ui.theme.BorderLight
import com.example.ui.theme.BrandNavy
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSoft
import com.example.ui.theme.GoldText
import com.example.ui.theme.HeaderBgDeep
import com.example.ui.theme.HeaderBgLight
import com.example.ui.theme.HeaderBgMid
import com.example.ui.theme.LocalBazmoColors
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavySoft
import com.example.ui.theme.PrimaryFill
import com.example.ui.theme.SoftSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessSoft
import com.example.ui.theme.SuccessText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.AppRole
import com.example.viewmodel.BazmoModal
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.BazmoViewModel
import com.example.viewmodel.HardwareSubMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserShellScreen(
    state: BazmoUiState,
    viewModel: BazmoViewModel,
    onSignOut: () -> Unit
) {
    val tabs = listOf("موبایل", "نرم\u200cافزار", "جانبی", "سخت\u200cافزار")
    val pagerState = rememberPagerState(initialPage = state.session.activeTab) { tabs.size }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val isDark = LocalBazmoColors.current.isDark
    var prevProvince by rememberSaveable { mutableStateOf("تهران") }
    var prevCity by rememberSaveable { mutableStateOf("تهران") }
    var prevRegion by rememberSaveable { mutableStateOf("علاءالدین و جمهوری") }

    LaunchedEffect(state.session.province, state.session.city, state.session.region) {
        if (!BazmoLocations.isAllScope(state.session.province)) {
            prevProvince = state.session.province
            prevCity = state.session.city
            prevRegion = state.session.region
        }
    }

    LaunchedEffect(state.session.activeTab) {
        if (pagerState.currentPage != state.session.activeTab) {
            pagerState.animateScrollToPage(state.session.activeTab)
        }
    }
    LaunchedEffect(pagerState.settledPage) {
        if (pagerState.settledPage != state.session.activeTab) {
            viewModel.setActiveTab(pagerState.settledPage)
        }
    }

    val cartQty = state.session.cart.sumOf { it.qty }
    val unreadCount = state.unreadNotificationCount()
    val roleLabel = if (state.session.isAdminUser) {
        "ادمین"
    } else {
        BazmoRoles.normalizeTier(state.currentTier)
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
            else -> 600.dp
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = adaptiveMaxWidth)
        ) {
            // Organized Deep Navy Top Header
            Surface(
                shape = RoundedCornerShape(bottomStart = 22.dp, bottomEnd = 22.dp),
                shadowElevation = 8.dp,
                color = Color.Transparent
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(HeaderBgDeep, HeaderBgMid, HeaderBgLight)))
                        .statusBarsPadding()
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    // Row 1: Brand Logo + Title on Start, Crisp Action Icons on End
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.White.copy(alpha = 0.14f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.25f)),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_bazmo_foreground_1794874267318),
                                    contentDescription = "لوگوی بازمو",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .clip(RoundedCornerShape(10.dp))
                                )
                            }
                            Column {
                                Text(
                                    text = "بازمو",
                                    color = Color.White,
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "صنف موبایل ایران",
                                    color = Color(0xFFCCE0FF),
                                    fontSize = 10.sp,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            if (state.session.isAdminUser) {
                                HeaderActionButton(
                                    icon = Icons.Rounded.AdminPanelSettings,
                                    contentDescription = "پنل مدیریت",
                                    tint = GoldAccent,
                                    containerColor = GoldAccent.copy(alpha = 0.16f),
                                    borderColor = GoldAccent.copy(alpha = 0.45f),
                                    badgeCount = 0,
                                    onClick = { viewModel.switchRole(AppRole.ADMIN) },
                                    tag = "switch_admin_button"
                                )
                            }
                            HeaderActionButton(
                                icon = Icons.Outlined.Notifications,
                                contentDescription = "اعلان‌ها",
                                badgeCount = unreadCount,
                                badgeColor = DangerRed,
                                badgeTextColor = Color.White,
                                onClick = { viewModel.openModal(BazmoModal.Notifications) },
                                tag = "notifications_button"
                            )
                            HeaderActionButton(
                                icon = Icons.Outlined.ReceiptLong,
                                contentDescription = "سفارش‌ها",
                                badgeCount = 0,
                                onClick = { viewModel.openModal(BazmoModal.Orders) },
                                tag = "orders_button"
                            )
                            HeaderActionButton(
                                icon = Icons.Outlined.ShoppingCart,
                                contentDescription = "سبد خرید",
                                badgeCount = cartQty,
                                badgeColor = GoldAccent,
                                badgeTextColor = Color(0xFF111827),
                                onClick = { viewModel.openModal(BazmoModal.Cart) },
                                tag = "cart_button"
                            )
                            HeaderActionButton(
                                icon = if (isDark) Icons.Rounded.LightMode else Icons.Rounded.DarkMode,
                                contentDescription = if (isDark) "حالت روشن" else "حالت تاریک",
                                tint = GoldAccent,
                                badgeCount = 0,
                                onClick = { BazmoThemeManager.toggleDark(context, isDark) },
                                tag = "theme_toggle_button"
                            )
                            HeaderActionButton(
                                icon = Icons.AutoMirrored.Outlined.Logout,
                                contentDescription = "خروج از حساب",
                                badgeCount = 0,
                                onClick = onSignOut,
                                tag = "logout_button"
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 2: Balanced Profile + Role Pill & Location Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            color = Color.White.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                            modifier = Modifier
                                .weight(1f)
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.openModal(BazmoModal.ProfileSetup) }
                                .testTag("profile_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.AccountCircle,
                                    contentDescription = null,
                                    tint = Color(0xFFCCE0FF),
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = state.activeDisplayName,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Surface(
                                    color = Color.White.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = roleLabel,
                                        color = Color(0xFFE2EEFF),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.24f)),
                            modifier = Modifier
                                .widthIn(min = 104.dp, max = 164.dp)
                                .heightIn(min = 44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.openModal(BazmoModal.LocationPicker) }
                                .testTag("location_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.LocationOn,
                                    contentDescription = null,
                                    tint = GoldAccent,
                                    modifier = Modifier.size(16.dp)
                                )
                                val locText = when {
                                    BazmoLocations.isAllScope(state.session.province) -> BazmoLocations.ALL_COUNTRY
                                    BazmoLocations.isAllScope(state.session.city) -> state.session.province
                                    state.session.region.isNotBlank() && state.session.region != BazmoLocations.ALL_REGIONS ->
                                        "${state.session.city} • ${state.session.region}"
                                    else -> state.session.city
                                }
                                Text(
                                    text = locText,
                                    color = Color.White,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f, fill = false)
                                )
                                Icon(
                                    imageVector = Icons.Rounded.KeyboardArrowDown,
                                    contentDescription = null,
                                    tint = Color(0xFFCCE0FF),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Row 3: 4-tab segmented switcher
                    Surface(
                        color = Color.Black.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(4.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            tabs.forEachIndexed { idx, title ->
                                val active = state.session.activeTab == idx
                                val tabBg by animateColorAsState(
                                    targetValue = if (active) Color.White else Color.Transparent,
                                    animationSpec = tween(150),
                                    label = "userTabBg"
                                )
                                val tabText by animateColorAsState(
                                    targetValue = if (active) BrandNavy else Color(0xFFD6E4FA),
                                    animationSpec = tween(150),
                                    label = "userTabText"
                                )
                                Surface(
                                    color = tabBg,
                                    shape = RoundedCornerShape(11.dp),
                                    shadowElevation = if (active) 3.dp else 0.dp,
                                    modifier = Modifier
                                        .weight(1f)
                                        .heightIn(min = 42.dp)
                                        .clip(RoundedCornerShape(11.dp))
                                        .clickable {
                                            viewModel.setActiveTab(idx)
                                            scope.launch { pagerState.animateScrollToPage(idx) }
                                        }
                                        .testTag("user_tab_$idx")
                                ) {
                                    Box(
                                        contentAlignment = Alignment.Center,
                                        modifier = Modifier.padding(horizontal = 4.dp)
                                    ) {
                                        Text(
                                            text = title,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = tabText,
                                            fontWeight = FontWeight.ExtraBold,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Text(
                        text = "← برای جابه‌جایی بین منوها به راست و چپ بکشید →",
                        color = Color(0xFFBFDBFE).copy(alpha = 0.85f),
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Compact Quick-Action Bar for Desk Rentals & Store Owner Dashboard
            val deskTotalCount = state.db.deskRentals.size + state.db.deskRequests.size
            val ownedStoreCount = remember(state.db.stores, state.currentUid, state.session.isAdminUser) {
                viewModel.myOwnedStores().size
            }
            val isAllCountryActive = BazmoLocations.isAllScope(state.session.province)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = if (isAllCountryActive) GoldAccent else CardSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, if (isAllCountryActive) GoldAccent else BorderLight),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            if (isAllCountryActive) {
                                val fallbackProv = prevProvince.ifBlank { "تهران" }
                                val fallbackCity = prevCity.ifBlank { "تهران" }
                                val fallbackReg = prevRegion.ifBlank { BazmoLocations.ALL_REGIONS }
                                viewModel.setLocation(fallbackProv, fallbackCity, fallbackReg)
                            } else {
                                viewModel.setLocation(
                                    BazmoLocations.ALL_COUNTRY,
                                    BazmoLocations.ALL_CITIES,
                                    BazmoLocations.ALL_REGIONS
                                )
                            }
                        }
                        .testTag("quick_all_country_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = if (isAllCountryActive) "✓ 🌍 همه کشور (سراسر ایران)" else "🌍 همه کشور",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isAllCountryActive) Color(0xFF111827) else NavyDeep,
                            maxLines = 1
                        )
                    }
                }

                val deskActive = state.session.showDeskRentals
                Surface(
                    color = if (deskActive) PrimaryFill else CardSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.2.dp, if (deskActive) PrimaryFill else BorderLight),
                    modifier = Modifier
                        .heightIn(min = 48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.toggleDeskRentals() }
                        .testTag("quick_desk_rental_button")
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                    ) {
                        Text(
                            text = "🪑 اجاره میز کار و همکاری (${deskTotalCount.toPersianNumber()})",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (deskActive) Color.White else NavyDeep,
                            maxLines = 1
                        )
                    }
                }

                if (state.canCreateStore) {
                    val myStoreActive = state.session.showMyStore
                    Surface(
                        color = if (myStoreActive) GoldAccent else NavySoft,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, if (myStoreActive) GoldAccent else BorderLight),
                        modifier = Modifier
                            .heightIn(min = 48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { viewModel.toggleMyStore() }
                            .testTag("quick_my_store_button")
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = "🏪 مدیریت فروشگاه من (${ownedStoreCount.toPersianNumber()})",
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (myStoreActive) Color(0xFF111827) else NavyDeep,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            when {
                state.session.showMyStore -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 14.dp)
                    ) {
                        MyStoreScreen(state, viewModel)
                    }
                }

                state.session.showDeskRentals -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 14.dp)
                    ) {
                        DeskRentalScreen(state, viewModel)
                    }
                }

                else -> {
                    // Swipeable Pager with all 4 tabs pre-kept & tuned snap threshold for silky swipes
                    HorizontalPager(
                        state = pagerState,
                        beyondViewportPageCount = 3,
                        flingBehavior = PagerDefaults.flingBehavior(
                            state = pagerState,
                            snapPositionalThreshold = 0.22f
                        ),
                        key = { it },
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .padding(horizontal = 14.dp)
                    ) { page ->
                        when (page) {
                            0 -> MobileTabScreen(state, viewModel)
                            1 -> SoftwareTabScreen(state, viewModel)
                            2 -> AccessoriesTabScreen(state, viewModel)
                            3 -> HardwareTabScreen(state, viewModel)
                        }
                    }
                }
            }
        }
    }

    // Bottom Sheet Modals
    val activeModal = state.session.modal
    if (activeModal != null) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { viewModel.closeModal() },
            sheetState = sheetState,
            containerColor = CardSurface,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 24.dp)
            ) {
                ModalContent(
                    modal = activeModal,
                    state = state,
                    viewModel = viewModel
                )
            }
        }
    }
}

@Composable
private fun HeaderActionButton(
    icon: ImageVector,
    contentDescription: String,
    badgeCount: Int,
    tint: Color = Color.White,
    containerColor: Color = Color.White.copy(alpha = 0.14f),
    borderColor: Color = Color.White.copy(alpha = 0.22f),
    badgeColor: Color = GoldAccent,
    badgeTextColor: Color = Color(0xFF111827),
    onClick: () -> Unit,
    tag: String
) {
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .size(40.dp)
            .testTag(tag),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            color = containerColor,
            shape = RoundedCornerShape(11.dp),
            border = BorderStroke(1.dp, borderColor),
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(11.dp))
                .clickable(onClick = onClick)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = contentDescription,
                    tint = tint,
                    modifier = Modifier.size(19.dp)
                )
            }
        }

        if (badgeCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 1.dp, y = (-1).dp)
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(badgeColor)
                    .border(1.5.dp, HeaderBgMid, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = badgeCount.toPersianNumber(),
                    color = badgeTextColor,
                    fontSize = 8.5.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
internal fun ModalHeader(title: String, onClose: () -> Unit, extraAction: (@Composable () -> Unit)? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.ExtraBold,
            color = NavyDeep,
            modifier = Modifier.weight(1f)
        )
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            if (extraAction != null) extraAction()
            BazmoSoftButton(text = "بستن", onClick = onClose, compact = true)
        }
    }
    HorizontalDivider(color = BorderLight, modifier = Modifier.padding(bottom = 12.dp))
}

@Composable
private fun ModalContent(
    modal: BazmoModal,
    state: BazmoUiState,
    viewModel: BazmoViewModel
) {
    when (modal) {
        BazmoModal.LocationPicker -> {
            var prov by rememberSaveable {
                mutableStateOf(
                    if (BazmoLocations.isAllScope(state.session.province)) BazmoLocations.ALL_COUNTRY
                    else state.session.province
                )
            }
            var city by rememberSaveable {
                mutableStateOf(
                    if (BazmoLocations.isAllScope(prov) || BazmoLocations.isAllScope(state.session.city)) BazmoLocations.ALL_CITIES
                    else state.session.city
                )
            }
            var region by rememberSaveable {
                mutableStateOf(
                    if (BazmoLocations.isAllScope(prov) || BazmoLocations.isAllScope(city)) BazmoLocations.ALL_REGIONS
                    else state.session.region
                )
            }
            val provs = BazmoLocations.provincesWithAll()
            val cities = BazmoLocations.citiesWithAllFor(prov)
            val regions = BazmoLocations.regionsWithAllFor(prov, city)

            ModalHeader("📍 انتخاب استان، شهر و منطقه", { viewModel.closeModal() })
            BazmoGoldButton(
                text = "🌍 نمایش همه کشور (سراسر ایران)",
                onClick = {
                    viewModel.setLocation(
                        BazmoLocations.ALL_COUNTRY,
                        BazmoLocations.ALL_CITIES,
                        BazmoLocations.ALL_REGIONS
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp)
                    .testTag("location_all_country_button")
            )
            BazmoDropdownSelector(
                label = "استان:",
                selectedValue = prov,
                options = provs,
                onSelect = { newP ->
                    prov = newP
                    if (BazmoLocations.isAllScope(newP)) {
                        city = BazmoLocations.ALL_CITIES
                        region = BazmoLocations.ALL_REGIONS
                    } else {
                        city = BazmoLocations.citiesFor(newP).firstOrNull() ?: BazmoLocations.ALL_CITIES
                        region = BazmoLocations.ALL_REGIONS
                    }
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BazmoDropdownSelector(
                label = "شهر:",
                selectedValue = city,
                options = cities,
                onSelect = { newC ->
                    city = newC
                    region = BazmoLocations.ALL_REGIONS
                },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BazmoDropdownSelector(
                label = "منطقه / محله:",
                selectedValue = region,
                options = regions,
                onSelect = { region = it },
                modifier = Modifier.padding(bottom = 14.dp)
            )
            BazmoPrimaryButton(
                text = "تایید موقعیت مکانی",
                onClick = { viewModel.setLocation(prov, city, region) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        BazmoModal.ProfileSetup -> {
            val curUser = state.session.currentUser
            var fullName by rememberSaveable { mutableStateOf(curUser?.fullName ?: state.activeDisplayName) }
            var phone by rememberSaveable { mutableStateOf(curUser?.phone ?: "") }
            val assignedTier = if (state.session.isAdminUser) "ادمین"
            else BazmoRoles.normalizeTier(curUser?.tier ?: BazmoRoles.REPAIR_TECH)
            val regProv = curUser?.province?.ifBlank { state.session.province } ?: state.session.province
            val regCity = curUser?.city?.ifBlank { state.session.city } ?: state.session.city
            val regRegion = curUser?.region?.ifBlank { state.session.region } ?: state.session.region

            ModalHeader("👤 پروفایل و نقش صنفی شما", { viewModel.closeModal() })
            if (state.authEmail.isNotBlank()) {
                Text(
                    text = "حساب کاربری: ${state.authEmail}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Surface(
                color = NavySoft,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "نقش صنفی تاییدشده:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                        Text(
                            text = "نقش صنفی فقط توسط مدیریت قابل تغییر است.",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted
                        )
                    }
                    BazmoBadge(assignedTier, BadgeTone.BLUE)
                }
            }
            if (state.canCreateStore) {
                BazmoGoldButton(
                    text = "🏪 ثبت فروشگاه من در شبکه بازمو",
                    onClick = {
                        viewModel.openModal(
                            BazmoModal.RegisterStore(BazmoRoles.defaultStoreTypeForRole(state.currentTier))
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
                BazmoSoftButton(
                    text = "📦 مدیریت فروشگاه و کالاهای من",
                    onClick = {
                        viewModel.closeModal()
                        viewModel.toggleMyStore(true)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp)
                        .testTag("profile_my_store_button")
                )
            }
            BazmoInput(fullName, { fullName = it }, "نام و نام خانوادگی", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(phone, { phone = it }, "شماره تماس (اختیاری)", keyboardType = KeyboardType.Phone, modifier = Modifier.padding(bottom = 10.dp))
            BazmoWarrantyBox(
                text = "📍 آدرس ثبت‌شده: استان $regProv، $regCity ($regRegion)\n🔒 تغییر آدرس ثبت‌شده حساب کاربری فقط توسط مدیر سیستم امکان‌پذیر است.",
                modifier = Modifier
                    .padding(bottom = 14.dp)
                    .testTag("profile_locked_address_box")
            )
            BazmoPrimaryButton(
                text = "✓ ذخیره اطلاعات پروفایل",
                onClick = { viewModel.saveOwnProfile(fullName, phone) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        BazmoModal.Notifications -> {
            val context = LocalContext.current
            var notifGranted by remember { mutableStateOf(BazmoNotificationHelper.hasNotificationPermission(context)) }
            var realtimeEnabled by remember { mutableStateOf(BazmoNotificationHelper.isRealtimeServiceEnabled(context)) }
            val permLauncher = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission()
            ) { granted ->
                notifGranted = granted || BazmoNotificationHelper.hasNotificationPermission(context)
                if (notifGranted) {
                    viewModel.onAppResumed()
                    viewModel.showToast("سرویس اعلان‌های آنی بازمو فعال شد")
                } else {
                    runCatching {
                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                        }
                        context.startActivity(intent)
                    }
                }
            }
            val list = state.userNotifications()
            val uname = state.currentUid
            ModalHeader(
                title = "🔔 اعلان‌ها و پیام‌های مدیریت",
                onClose = { viewModel.closeModal() },
                extraAction = {
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        BazmoSoftButton("🔄 بروزرسانی", {
                            viewModel.onAppResumed()
                            viewModel.showToast("لیست اعلان‌ها بروزرسانی شد")
                        }, compact = true)
                        if (list.isNotEmpty()) {
                            BazmoSoftButton("خواندن همه", { viewModel.markAllNotificationsRead() }, compact = true)
                        }
                    }
                }
            )
            Surface(
                color = if (notifGranted) SuccessSoft else GoldSoft,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, if (notifGranted) SuccessGreen.copy(alpha = 0.45f) else GoldAccent),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (notifGranted) "✓ سرویس اعلان‌های گوشی فعال است" else "🔔 اعلان‌های سیستم گوشی غیرفعال است",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (notifGranted) SuccessText else GoldText,
                        modifier = Modifier.weight(1f)
                    )
                    if (!notifGranted) {
                        BazmoGoldButton(
                            text = "فعال‌سازی",
                            compact = true,
                            onClick = {
                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    permLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                } else {
                                    runCatching {
                                        val intent = Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
                                            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
                                        }
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        )
                    }
                }
            }
            Surface(
                color = NavySoft,
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 10.dp)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "⚡ دریافت آنی اعلان در پس‌زمینه",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = NavyDeep
                            )
                            Text(
                                text = "اتصال زنده وب‌سوکت حتی هنگام خروج موقت از برنامه",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                        }
                        Switch(
                            checked = realtimeEnabled,
                            onCheckedChange = { checked ->
                                realtimeEnabled = checked
                                viewModel.setRealtimeNotificationsEnabled(checked)
                            }
                        )
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "اگر گوشی باتری را محدود می‌کند، بازمو را بدون محدودیت کنید:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextMuted,
                            modifier = Modifier.weight(1f)
                        )
                        BazmoSoftButton(
                            text = "تنظیمات باتری",
                            compact = true,
                            onClick = {
                                val opened = runCatching {
                                    context.startActivity(Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS))
                                }.isSuccess
                                if (!opened) {
                                    runCatching {
                                        context.startActivity(
                                            Intent(
                                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                                Uri.fromParts("package", context.packageName, null)
                                            )
                                        )
                                    }
                                }
                            }
                        )
                    }
                }
            }
            Text(
                text = "فیلتر فعال شما: ${state.session.province} / ${state.session.city} / ${state.session.region}",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            if (list.isEmpty()) {
                BazmoEmptyState("اعلان جدیدی برای منطقه شما وجود ندارد.")
            } else {
                list.forEach { n ->
                    val isUnread = !n.readBy.contains(uname)
                    Surface(
                        color = if (isUnread) NavySoft else CardSurface,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, if (isUnread) GoldAccent else BorderLight),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { viewModel.markNotificationRead(n.id) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                BazmoBadge(n.type, BadgeTone.BLUE)
                                Text(
                                    text = "${n.date}${if (isUnread) " • 🔵 جدید" else ""}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = n.title,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            )
                            Text(
                                text = n.body,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                            Text(
                                text = "🎯 هدف: ${n.province} • ${n.city} • ${n.region}",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted,
                                modifier = Modifier.padding(top = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        BazmoModal.Cart -> {
            val cart = state.session.cart
            val total = cart.sumOf { it.price * it.qty }
            var chkFirst by rememberSaveable { mutableStateOf("") }
            var chkLast by rememberSaveable { mutableStateOf("") }
            var chkPhone by rememberSaveable { mutableStateOf("") }
            var chkAddr by rememberSaveable { mutableStateOf("") }

            ModalHeader("🛒 سبد خرید شما", { viewModel.closeModal() })
            if (cart.isEmpty()) {
                BazmoEmptyState("سبد خرید شما خالی است.")
            } else {
                cart.forEach { c ->
                    BazmoCard {
                        Text(
                            text = c.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(text = c.storeName, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = (c.price * c.qty).formatToman(),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Black,
                                color = Navy
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                BazmoSoftButton("-", { viewModel.updateCartQty(c.id, -1) }, compact = true)
                                Text(
                                    text = c.qty.toPersianNumber(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Black
                                )
                                BazmoSoftButton("+", { viewModel.updateCartQty(c.id, 1) }, compact = true)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("جمع کل:", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black)
                    Text(total.formatToman(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Black, color = Navy)
                }
                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 8.dp))
                BazmoCard(containerColor = SoftSurface) {
                    Text(
                        text = "🛵 ارسال با پیک",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    BazmoInput(chkFirst, { chkFirst = it }, "نام", modifier = Modifier.padding(bottom = 8.dp))
                    BazmoInput(chkLast, { chkLast = it }, "نام خانوادگی", modifier = Modifier.padding(bottom = 8.dp))
                    BazmoInput(chkPhone, { chkPhone = it }, "شماره تلفن همراه", keyboardType = KeyboardType.Phone, modifier = Modifier.padding(bottom = 8.dp))
                    BazmoInput(chkAddr, { chkAddr = it }, "آدرس دقیق در ${state.session.city}", modifier = Modifier.padding(bottom = 10.dp))
                    BazmoPrimaryButton(
                        text = "تایید و ثبت سفارش با پیک",
                        onClick = { viewModel.submitOrder("پیک", chkFirst, chkLast, chkPhone, chkAddr) },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                BazmoOutlineButton(
                    text = "🏪 دریافت حضوری از فروشگاه",
                    onClick = { viewModel.submitOrder("حضوری") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        BazmoModal.Orders -> {
            ModalHeader("📦 پیگیری سفارش‌ها", { viewModel.closeModal() })
            if (state.db.orders.isEmpty()) {
                BazmoEmptyState("سفارشی ثبت نشده است.")
            } else {
                state.db.orders.forEach { o ->
                    BazmoCard {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("سفارش ${o.id}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                            BazmoBadge(o.deliveryType, BadgeTone.BLUE)
                        }
                        com.example.ui.components.BazmoOrderStepper(o.status)
                        Text(
                            text = "تحویل‌گیرنده: ${o.customer.firstName} ${o.customer.lastName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        o.items.forEach { item ->
                            Text(
                                text = "• ${item.name} (${item.qty.toPersianNumber()} عدد)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = o.total.formatToman(),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = Navy
                        )
                    }
                }
            }
        }

        is BazmoModal.HwMatchFound -> {
            ModalHeader("✅ این قطعه موجود است!", { viewModel.closeModal() })
            Text(
                text = "موارد زیر هم‌اکنون موجود است:",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            modal.matches.forEach { ad ->
                BazmoCard(containerColor = NavySoft) {
                    Text(ad.title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.ExtraBold)
                    Text("فروشنده: ${ad.seller} | ${ad.city}", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(ad.price.formatToman(), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Black, color = Navy)
                        BazmoPrimaryButton(
                            text = "مشاهده",
                            onClick = {
                                viewModel.setHwMode(HardwareSubMode.AD_DETAIL, ad.id)
                                viewModel.closeModal()
                            },
                            compact = true
                        )
                    }
                }
            }
            Text(
                text = "آیا باز هم درخواست را ثبت کنم؟",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                BazmoPrimaryButton(
                    text = "بله، باز هم ثبت کن",
                    onClick = { viewModel.finalizeHwRequest(modal.reqData) },
                    modifier = Modifier.weight(1f)
                )
                BazmoSoftButton(
                    text = "خیر",
                    onClick = { viewModel.closeModal() },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        is BazmoModal.NewRedditPost -> {
            val flairs = if (modal.board == "software") {
                listOf("FRP و آنلاک", "رام و فلش", "iOS و آیکلود", "باکس و دانگل")
            } else {
                listOf("آی‌سی و برد", "تاچ و ال‌سی‌دی", "عیب‌یابی جریان", "شماتیک و سولوشن")
            }
            var flair by rememberSaveable { mutableStateOf(flairs.first()) }
            var device by rememberSaveable { mutableStateOf("") }
            var title by rememberSaveable { mutableStateOf("") }
            var body by rememberSaveable { mutableStateOf("") }

            ModalHeader("📝 انتشار پست جدید", { viewModel.closeModal() })
            BazmoDropdownSelector(
                selectedValue = flair,
                options = flairs,
                onSelect = { flair = it },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BazmoInput(device, { device = it }, "مدل گوشی یا ابزار (اختیاری)", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(title, { title = it }, "عنوان پست", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(body, { body = it }, "شرح دقیق جزئیات فنی...", singleLine = false, minLines = 4, modifier = Modifier.padding(bottom = 12.dp))
            BazmoPrimaryButton(
                text = "انتشار در انجمن",
                onClick = { viewModel.submitForumPost(modal.board, flair, device, title, body) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        is BazmoModal.Chat -> {
            val ad = state.db.hwAds.find { it.id == modal.adId }
            val threads = state.chatThreadsForAd(modal.adId)
            val isSeller = ad?.ownerId == state.currentUid
            var selectedThreadId by rememberSaveable(modal.adId) {
                mutableStateOf(
                    if (isSeller) threads.firstOrNull()?.id
                    else threads.find { it.buyerUid == state.currentUid }?.id
                )
            }
            val activeThread = if (isSeller) {
                threads.find { it.id == selectedThreadId } ?: threads.firstOrNull()
            } else {
                threads.find { it.buyerUid == state.currentUid }
            }
            val msgs = activeThread?.messages.orEmpty()
            var chatInput by rememberSaveable { mutableStateOf("") }

            ModalHeader("💬 چت با ${if (isSeller) "خریدار" else (ad?.seller ?: "فروشنده")}", { viewModel.closeModal() })
            if (isSeller && threads.size > 1) {
                BazmoDropdownSelector(
                    label = "گفتگو:",
                    selectedValue = activeThread?.messages?.firstOrNull()?.senderName ?: "خریدار",
                    options = threads.mapIndexed { idx, th ->
                        th.messages.firstOrNull()?.senderName?.ifBlank { "خریدار ${idx + 1}" } ?: "خریدار ${idx + 1}"
                    },
                    onSelect = { label ->
                        val match = threads.firstOrNull { th ->
                            th.messages.firstOrNull()?.senderName == label
                        }
                        selectedThreadId = match?.id
                    },
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            Surface(
                color = SoftSurface,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 120.dp, max = 260.dp)
                    .padding(bottom = 10.dp)
            ) {
                Column(
                    modifier = Modifier
                        .verticalScroll(rememberScrollState())
                        .padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (msgs.isEmpty()) {
                        Text("پیامی ارسال نشده است. گفتگو را شروع کنید.", style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    } else {
                        msgs.forEach { msg ->
                            val isMe = msg.senderUid == state.currentUid
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = if (isMe) Alignment.CenterStart else Alignment.CenterEnd
                            ) {
                                Surface(
                                    color = if (isMe) PrimaryFill else CardSurface,
                                    border = if (isMe) null else BorderStroke(1.dp, BorderLight),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.widthIn(max = 280.dp)
                                ) {
                                    Column(modifier = Modifier.padding(horizontal = 11.dp, vertical = 8.dp)) {
                                        if (!isMe && msg.senderName.isNotBlank()) {
                                            Text(
                                                text = msg.senderName,
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = NavyDeep
                                            )
                                        }
                                        Text(
                                            text = msg.text,
                                            color = if (isMe) Color.White else TextPrimary,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BazmoInput(
                    value = chatInput,
                    onValueChange = { chatInput = it },
                    placeholder = "پیام شما...",
                    modifier = Modifier.weight(1f)
                )
                BazmoPrimaryButton(
                    text = "ارسال",
                    onClick = {
                        if (chatInput.isNotBlank()) {
                            viewModel.sendChatMessage(modal.adId, activeThread?.id, chatInput)
                            chatInput = ""
                        }
                    },
                    compact = true
                )
            }
        }

        BazmoModal.NewHwAd -> {
            val cats = state.db.hwCategories.map { it.name }.ifEmpty { listOf("تاچ و ال‌سی‌دی") }
            var title by rememberSaveable { mutableStateOf("") }
            var cat by rememberSaveable { mutableStateOf(cats.first()) }
            var brand by rememberSaveable { mutableStateOf("") }
            var model by rememberSaveable { mutableStateOf("") }
            var series by rememberSaveable { mutableStateOf("") }
            var price by rememberSaveable { mutableStateOf("") }
            var phone by rememberSaveable { mutableStateOf(state.session.currentUser?.phone ?: "") }
            var desc by rememberSaveable { mutableStateOf("") }

            ModalHeader("➕ ثبت آگهی قطعه", { viewModel.closeModal() })
            BazmoInput(title, { title = it }, "عنوان آگهی", modifier = Modifier.padding(bottom = 8.dp))
            BazmoDropdownSelector(selectedValue = cat, options = cats, onSelect = { cat = it }, modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(brand, { brand = it }, "برند", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(model, { model = it }, "مدل گوشی", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(series, { series = it }, "سری / کد قطعه", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(price, { price = it }, "قیمت (تومان)", keyboardType = KeyboardType.Number, modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(phone, { phone = it }, "شماره تماس", keyboardType = KeyboardType.Phone, modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(desc, { desc = it }, "توضیحات...", singleLine = false, minLines = 2, modifier = Modifier.padding(bottom = 12.dp))
            BazmoPrimaryButton(
                text = "ثبت آگهی",
                onClick = { viewModel.submitNewHwAd(title, cat, brand, model, series, price, phone, desc, section = "hw") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        is BazmoModal.NewMarketAd -> {
            val isMobile = modal.section == "mobile"
            val catOptions = if (isMobile) {
                listOf("گوشی نو", "گوشی کارکرده", "عمده موبایل")
            } else {
                listOf("قاب و گارد", "گلس", "کابل و شارژر", "پاوربانک", "هندزفری")
            }
            var title by rememberSaveable(modal.section) { mutableStateOf("") }
            var cat by rememberSaveable(modal.section) { mutableStateOf(catOptions.first()) }
            var brand by rememberSaveable(modal.section) { mutableStateOf("") }
            var model by rememberSaveable(modal.section) { mutableStateOf("") }
            var price by rememberSaveable(modal.section) { mutableStateOf("") }
            var phone by rememberSaveable(modal.section) { mutableStateOf(state.session.currentUser?.phone ?: "") }
            var desc by rememberSaveable(modal.section) { mutableStateOf("") }

            ModalHeader(
                title = if (isMobile) "➕ ثبت آگهی موبایل" else "➕ ثبت آگهی لوازم جانبی",
                onClose = { viewModel.closeModal() }
            )
            BazmoInput(title, { title = it }, "عنوان آگهی", modifier = Modifier.padding(bottom = 8.dp))
            BazmoDropdownSelector(selectedValue = cat, options = catOptions, onSelect = { cat = it }, modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(brand, { brand = it }, "برند (مثلاً Apple یا Samsung)", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(model, { model = it }, if (isMobile) "مدل و حافظه (مثلاً iPhone 15 256GB)" else "مدل یا مشخصات کالا", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(price, { price = it }, "قیمت (تومان)", keyboardType = KeyboardType.Number, modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(phone, { phone = it }, "شماره تماس", keyboardType = KeyboardType.Phone, modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(desc, { desc = it }, "توضیحات تکمیلی...", singleLine = false, minLines = 2, modifier = Modifier.padding(bottom = 12.dp))
            BazmoPrimaryButton(
                text = "ارسال آگهی برای تایید ادمین",
                onClick = { viewModel.submitNewHwAd(title, cat, brand, model, "-", price, phone, desc, section = modal.section) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        is BazmoModal.RegisterStore -> {
            val typeOptions = listOf(
                "mobile" to "فروشگاه موبایل",
                "acc" to "فروشگاه لوازم جانبی",
                "hw" to "فروشگاه قطعات سخت‌افزار",
                "both" to "موبایل و جانبی"
            )
            var storeName by rememberSaveable { mutableStateOf("") }
            var storePhone by rememberSaveable { mutableStateOf(state.session.currentUser?.phone ?: "") }
            var storeAddress by rememberSaveable { mutableStateOf("") }
            var storeHours by rememberSaveable { mutableStateOf("۱۰ تا ۲۰") }
            var storeType by rememberSaveable(modal.defaultType) { mutableStateOf(modal.defaultType) }
            var prov by rememberSaveable { mutableStateOf(state.session.province) }
            var city by rememberSaveable { mutableStateOf(state.session.city) }
            var region by rememberSaveable { mutableStateOf(state.session.region) }
            val provs = BazmoLocations.provinces()
            val cities = BazmoLocations.citiesFor(prov)
            val regions = BazmoLocations.regionsFor(prov, city)

            ModalHeader("🏪 ثبت فروشگاه جدید", { viewModel.closeModal() })
            BazmoBadge("دسترسی مجاز: ${state.currentTier}", BadgeTone.GREEN, modifier = Modifier.padding(bottom = 10.dp))
            BazmoInput(storeName, { storeName = it }, "نام فروشگاه", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(storePhone, { storePhone = it }, "تلفن تماس فروشگاه", keyboardType = KeyboardType.Phone, modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(storeAddress, { storeAddress = it }, "آدرس دقیق فروشگاه", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(storeHours, { storeHours = it }, "ساعت کاری (مثلاً ۱۰ تا ۲۰)", modifier = Modifier.padding(bottom = 8.dp))
            val curTypeLabel = typeOptions.find { it.first == storeType }?.second ?: typeOptions.first().second
            BazmoDropdownSelector(
                label = "حوزه فعالیت فروشگاه:",
                selectedValue = curTypeLabel,
                options = typeOptions.map { it.second },
                onSelect = { chosen -> storeType = typeOptions.find { it.second == chosen }?.first ?: modal.defaultType },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
                BazmoDropdownSelector(
                    label = "استان:",
                    selectedValue = prov,
                    options = provs,
                    onSelect = { newP ->
                        prov = newP
                        val firstC = BazmoLocations.citiesFor(newP).firstOrNull() ?: ""
                        city = firstC
                        region = BazmoLocations.regionsFor(newP, firstC).firstOrNull() ?: "همه مناطق"
                    },
                    modifier = Modifier.weight(1f)
                )
                BazmoDropdownSelector(
                    label = "شهر:",
                    selectedValue = city,
                    options = cities,
                    onSelect = { newC ->
                        city = newC
                        region = BazmoLocations.regionsFor(prov, newC).firstOrNull() ?: "همه مناطق"
                    },
                    modifier = Modifier.weight(1f)
                )
            }
            BazmoDropdownSelector(
                label = "منطقه / محله:",
                selectedValue = region,
                options = regions,
                onSelect = { region = it },
                modifier = Modifier.padding(bottom = 12.dp)
            )
            BazmoPrimaryButton(
                text = "✓ ثبت فروشگاه",
                onClick = { viewModel.submitNewStore(storeName, prov, city, region, storePhone, storeAddress, storeHours, storeType) },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("submit_store_button")
            )
        }

        BazmoModal.SellTutorial -> {
            val probs = state.db.swProblems
            var selectedProbTitle by rememberSaveable { mutableStateOf(probs.firstOrNull()?.title ?: "آنلاک شبکه و FRP") }
            var title by rememberSaveable { mutableStateOf("") }
            var teacher by rememberSaveable { mutableStateOf("") }
            var price by rememberSaveable { mutableStateOf("") }
            var terms by rememberSaveable { mutableStateOf("") }

            ModalHeader("🎓 ثبت آموزش برای فروش", { viewModel.closeModal() })
            BazmoDropdownSelector(
                selectedValue = selectedProbTitle,
                options = probs.map { it.title },
                onSelect = { selectedProbTitle = it },
                modifier = Modifier.padding(bottom = 8.dp)
            )
            BazmoInput(title, { title = it }, "عنوان دوره", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(teacher, { teacher = it }, "نام مدرس", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(price, { price = it }, "قیمت (تومان)", keyboardType = KeyboardType.Number, modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(terms, { terms = it }, "شرایط و پشتیبانی...", singleLine = false, minLines = 2, modifier = Modifier.padding(bottom = 12.dp))
            BazmoPrimaryButton(
                text = "ثبت آموزش",
                onClick = {
                    val pId = probs.find { it.title == selectedProbTitle }?.id ?: "p1"
                    viewModel.submitTutorial(pId, title, teacher, price, terms)
                },
                modifier = Modifier.fillMaxWidth()
            )
        }

        BazmoModal.SellBox -> {
            var name by rememberSaveable { mutableStateOf("") }
            var price by rememberSaveable { mutableStateOf("") }
            var cond by rememberSaveable { mutableStateOf("") }
            var seller by rememberSaveable { mutableStateOf("") }

            ModalHeader("🧰 ثبت آگهی فروش باکس", { viewModel.closeModal() })
            BazmoInput(name, { name = it }, "نام باکس یا دانگل", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(price, { price = it }, "قیمت (تومان)", keyboardType = KeyboardType.Number, modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(cond, { cond = it }, "وضعیت", modifier = Modifier.padding(bottom = 8.dp))
            BazmoInput(seller, { seller = it }, "نام فروشنده", modifier = Modifier.padding(bottom = 12.dp))
            BazmoPrimaryButton(
                text = "ثبت آگهی",
                onClick = { viewModel.submitBox(name, price, cond, seller) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        BazmoModal.NewSwReq -> {
            var title by rememberSaveable { mutableStateOf("") }
            ModalHeader("📋 ثبت درخواست نرم‌افزاری", { viewModel.closeModal() })
            BazmoInput(title, { title = it }, "شرح فایل یا خدمات مورد نیاز...", singleLine = false, minLines = 3, modifier = Modifier.padding(bottom = 12.dp))
            BazmoPrimaryButton(
                text = "ثبت درخواست",
                onClick = { viewModel.submitSwRequest(title) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        is BazmoModal.NewDeskEntry -> NewDeskEntryModalBody(modal, state, viewModel)
        is BazmoModal.GenericChat -> GenericChatModalBody(modal, state, viewModel)
        is BazmoModal.MyStoreAddMobile -> MyStoreAddMobileModalBody(modal, state, viewModel)
        is BazmoModal.MyStoreAddAccessory -> MyStoreAddAccessoryModalBody(modal, viewModel)
    }
}
