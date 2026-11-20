package com.example

import android.app.Service
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.fragment.app.FragmentActivity
import androidx.test.core.app.ApplicationProvider
import com.example.data.AccessoryEntity
import com.example.data.AdminUsersWrapper
import com.example.data.BackgroundNotifConfig
import com.example.data.BazmoAuthResponse
import com.example.data.BazmoBiometricHelper
import com.example.data.BazmoDbState
import com.example.data.BazmoNotificationHelper
import com.example.data.BazmoRealtimeNotifService
import com.example.data.BazmoRoles
import com.example.data.BazmoSeedData
import com.example.data.BazmoServerConfig
import com.example.data.BazmoSessionUser
import com.example.data.BoxEntity
import com.example.data.DeskRentalEntity
import com.example.data.DeskRequestEntity
import com.example.data.HwAdEntity
import com.example.data.HwCategoryEntity
import com.example.data.NotificationsWrapper
import com.example.data.OrderEntity
import com.example.data.RegistrationRequestEntity
import com.example.data.StoreEntity
import com.example.data.SwProblemEntity
import com.example.data.TutorialEntity
import com.example.data.UserEntity
import com.example.data.UserWrapper
import com.example.ui.screens.AccessoriesTabScreen
import com.example.ui.screens.AdminShellScreen
import com.example.ui.screens.DeskRentalScreen
import com.example.ui.screens.ForumScreen
import com.example.ui.screens.HardwareTabScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.MobileTabScreen
import com.example.ui.screens.MyStoreScreen
import com.example.ui.screens.SoftwareTabScreen
import com.example.ui.screens.UserShellScreen
import com.example.ui.theme.BazmoTheme
import com.example.viewmodel.AdminTab
import com.example.viewmodel.AppRole
import com.example.viewmodel.BazmoModal
import com.example.viewmodel.BazmoSessionState
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.BazmoViewModel
import com.example.viewmodel.CartItem
import com.example.viewmodel.ForumUiFilter
import com.example.viewmodel.HardwareSubMode
import com.example.viewmodel.HwUiFilter
import com.example.viewmodel.MobileSubMode
import com.example.viewmodel.MobileUiFilter
import com.example.viewmodel.SoftwareSubMode
import com.example.viewmodel.SwUiFilter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class BazmoCrashRegressionTest {

    @get:Rule
    val composeRule = createComposeRule()

    private fun testContext(): Context = ApplicationProvider.getApplicationContext()

    private fun testViewModel(isAdmin: Boolean = true): BazmoViewModel {
        return BazmoViewModel(
            appContext = testContext(),
            baseUrl = "http://127.0.0.1:1",
            authToken = "test-token",
            uid = "u_admin",
            email = "admin@example.com",
            displayName = "مدیر تست",
            initialIsSuperAdmin = isAdmin
        )
    }

    private fun sampleDbWithPotentialDuplicates(): BazmoDbState {
        val uid = "u_admin"
        return BazmoDbState(
            showDemoData = true,
            users = listOf(
                UserEntity(id = "u_1", uid = "u_1", email = "a@b.com", fullName = "علی", tier = BazmoRoles.REPAIR_TECH),
                UserEntity(id = "", uid = "", email = "c@d.com", fullName = "رضا", tier = BazmoRoles.MOBILE_SELLER),
                UserEntity(id = "", uid = "", email = "e@f.com", fullName = "سهراب", tier = BazmoRoles.SOFTWARE_TECH)
            ),
            registrationRequests = listOf(
                RegistrationRequestEntity(id = "dup1", email = "x@y.com", fullName = "کاربر ۱"),
                RegistrationRequestEntity(id = "dup1", email = "z@w.com", fullName = "کاربر ۲")
            ),
            notifications = BazmoSeedData.notifications() + BazmoSeedData.notifications(),
            stores = BazmoSeedData.stores + StoreEntity(id = "dup_shared", name = "فروشگاه تستی", province = "تهران", city = "تهران", ownerId = uid, status = "approved"),
            mobiles = BazmoSeedData.mobiles,
            storeMobiles = BazmoSeedData.storeMobiles,
            accessories = BazmoSeedData.accessories,
            swProblems = BazmoSeedData.swProblems,
            tutorials = BazmoSeedData.tutorials(uid),
            swRequests = BazmoSeedData.swRequests(uid),
            boxes = BazmoSeedData.boxes(uid),
            hwCategories = BazmoSeedData.hwCategories,
            hwAds = BazmoSeedData.hwAds(uid) + HwAdEntity(id = "dup_shared", cat = "باتری اصلی", title = "آگهی تست", ownerId = uid, status = "approved"),
            hwRequests = emptyList(),
            orders = listOf(OrderEntity(id = "dup_shared", userId = uid, userName = "تست", total = 1000L, status = "ارسال شده به فروشگاه")),
            chatThreads = emptyList(),
            forumPosts = BazmoSeedData.forumPosts(uid),
            deskRentals = listOf(DeskRentalEntity(id = "dup_shared", title = "میز تست", ownerId = uid, status = "approved")),
            deskRequests = listOf(DeskRequestEntity(id = "dup_shared", title = "درخواست میز", ownerId = uid))
        )
    }

    @Test
    fun `realtime notification service calls startForeground even when stopped or misconfigured`() {
        val ctx = testContext()
        ctx.getSharedPreferences("bazmo_notif_service", Context.MODE_PRIVATE).edit().clear().commit()

        val controller = Robolectric.buildService(BazmoRealtimeNotifService::class.java).create()
        val service = controller.get()
        val shadow = Shadows.shadowOf(service)

        // 1. Null config: must call startForeground before stopGracefully (which calls stopForeground + stopSelf)
        val resNullCfg = service.onStartCommand(Intent(ctx, BazmoRealtimeNotifService::class.java), 0, 1)
        assertEquals(Service.START_NOT_STICKY, resNullCfg)
        assertTrue(shadow.isForegroundStopped)
        assertTrue(shadow.isStoppedBySelf)

        // 2. Active config: must remain in foreground with REALTIME_SERVICE_NOTIF_ID
        BazmoNotificationHelper.saveBackgroundConfig(
            ctx,
            BackgroundNotifConfig("tok", "uid", BazmoRoles.REPAIR_TECH, false, "تهران", "تهران", "همه مناطق")
        )
        val resActive = service.onStartCommand(Intent(ctx, BazmoRealtimeNotifService::class.java), 0, 2)
        assertEquals(Service.START_STICKY, resActive)
        assertNotNull(shadow.lastForegroundNotification)
        assertEquals(BazmoNotificationHelper.REALTIME_SERVICE_NOTIF_ID, shadow.lastForegroundNotificationId)

        // 3. ACTION_STOP: must still call startForeground before stopping
        val stopIntent = Intent(ctx, BazmoRealtimeNotifService::class.java).apply {
            action = "com.example.action.STOP_REALTIME_NOTIFS"
        }
        val resStop = service.onStartCommand(stopIntent, 0, 3)
        assertEquals(Service.START_NOT_STICKY, resStop)
        assertTrue(shadow.isForegroundStopped)

        controller.destroy()
    }

    @Test
    fun `notification smallIcon uses vector drawable instead of adaptive launcher mipmap`() {
        val ctx = testContext()
        val notif = BazmoNotificationHelper.buildRealtimeServiceNotification(ctx)
        assertEquals(R.drawable.ic_stat_bazmo, notif.smallIcon.resId)
    }

    @Test
    fun `biometric helper safely handles unstarted or finishing activity without crashing`() {
        val controller = Robolectric.buildActivity(FragmentActivity::class.java).create()
        val activity = controller.get()

        // Should return cleanly without throwing
        BazmoBiometricHelper.canAuthenticate(activity)

        // Calling prompt before STARTED should return safely without throwing IllegalStateException
        var errorCalled = false
        BazmoBiometricHelper.promptBiometricUnlock(
            activity = activity,
            accountLabel = "test@bazmo.ir",
            onSuccess = {},
            onError = { errorCalled = true }
        )
        assertFalse(errorCalled)

        // Even after start+resume, promptBiometricUnlock should catch any platform exception cleanly
        controller.start().resume()
        BazmoBiometricHelper.promptBiometricUnlock(
            activity = activity,
            accountLabel = "test@bazmo.ir",
            onSuccess = {},
            onError = {}
        )

        controller.destroy()
    }

    @Test
    fun `admin shell and all admin tabs render duplicate or blank ids without crashing`() {
        val vm = testViewModel(isAdmin = true)
        val db = sampleDbWithPotentialDuplicates()
        var currentTab by mutableStateOf(AdminTab.DASHBOARD)

        composeRule.setContent {
            val state = BazmoUiState(
                isReady = true,
                currentUid = "u_admin",
                db = db,
                session = BazmoSessionState(
                    role = AppRole.ADMIN,
                    isAdminUser = true,
                    adminTab = currentTab
                )
            )
            BazmoTheme(darkTheme = false) {
                AdminShellScreen(state = state, viewModel = vm, onSignOut = {})
            }
        }

        AdminTab.entries.forEach { tab ->
            composeRule.runOnUiThread { currentTab = tab }
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("admin_tab_${tab.name.lowercase()}").assertExists()
        }
    }

    @Test
    fun `user shell tabs and missing-id submodes do not crash during composition`() {
        val vm = testViewModel(isAdmin = true)
        val db = sampleDbWithPotentialDuplicates()
        val baseSession = BazmoSessionState(
            role = AppRole.USER,
            isAdminUser = true,
            currentUser = UserEntity(id = "u_admin", uid = "u_admin", fullName = "مدیر", tier = BazmoRoles.REPAIR_TECH)
        )
        var step by mutableIntStateOf(0)

        composeRule.setContent {
            BazmoTheme(darkTheme = (step == 0)) {
                when (step) {
                    0 -> UserShellScreen(
                        state = BazmoUiState(
                            isReady = true,
                            currentUid = "u_admin",
                            db = db,
                            session = baseSession.copy(modal = BazmoModal.Notifications)
                        ),
                        viewModel = vm,
                        onSignOut = {}
                    )
                    1 -> MobileTabScreen(
                        state = BazmoUiState(
                            isReady = true,
                            currentUid = "u_admin",
                            db = db,
                            session = baseSession.copy(
                                mobileView = MobileUiFilter(mode = MobileSubMode.STORE, storeId = null)
                            )
                        ),
                        viewModel = vm
                    )
                    2 -> HardwareTabScreen(
                        state = BazmoUiState(
                            isReady = true,
                            currentUid = "u_admin",
                            db = db,
                            session = baseSession.copy(
                                hwView = HwUiFilter(mode = HardwareSubMode.AD_DETAIL, adId = "missing_id")
                            )
                        ),
                        viewModel = vm
                    )
                    3 -> ForumScreen(
                        board = "hardware",
                        state = BazmoUiState(
                            isReady = true,
                            currentUid = "u_admin",
                            db = db,
                            session = baseSession.copy(
                                hwForum = ForumUiFilter(threadId = "missing_thread")
                            )
                        ),
                        viewModel = vm,
                        onBack = {}
                    )
                    4 -> MyStoreScreen(
                        state = BazmoUiState(isReady = true, currentUid = "u_admin", db = db, session = baseSession),
                        viewModel = vm
                    )
                    else -> DeskRentalScreen(
                        state = BazmoUiState(isReady = true, currentUid = "u_admin", db = db, session = baseSession),
                        viewModel = vm
                    )
                }
            }
        }

        for (i in 0..5) {
            composeRule.runOnUiThread { step = i }
            composeRule.waitForIdle()
        }
    }

    @Test
    fun `login screen renders safely with saved biometric account`() {
        composeRule.setContent {
            BazmoTheme {
                LoginScreen(
                    isLoading = false,
                    errorMessage = null,
                    savedBiometricAccount = BazmoSessionUser(
                        uid = "u_1",
                        email = "user@bazmo.ir",
                        displayName = "همکار بازمو",
                        token = "tok"
                    ),
                    canUseBiometric = false,
                    onBiometricSignIn = {}
                )
            }
        }
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("login_email_input").assertExists()
    }

    @Test
    fun `moshi deserializes server auth and db payloads cleanly`() {
        val moshi = BazmoServerConfig.moshi

        val dbJson = """
            {
              "showDemoData": true,
              "users": [{"id":"u1","uid":"u1","email":"a@b.com","fullName":"علی","tier":"تعمیرکار","active":true}],
              "orders": [{"id":"ord1","userId":"u1","total":50000,"customer":{},"items":[{"id":"i1","name":"کابل","price":50000,"qty":1}]}],
              "notifications": [],
              "stores": [],
              "mobiles": [],
              "storeMobiles": [],
              "accessories": [],
              "inquiries": [],
              "swProblems": [],
              "tutorials": [],
              "swRequests": [],
              "boxes": [],
              "hwCategories": [],
              "hwAds": [],
              "hwRequests": [],
              "chatThreads": [],
              "forumPosts": [],
              "deskRentals": [],
              "deskRequests": []
            }
        """.trimIndent()
        val parsedDb = moshi.adapter(BazmoDbState::class.java).fromJson(dbJson)
        assertNotNull(parsedDb)
        assertEquals(1, parsedDb!!.users.size)
        assertEquals(1, parsedDb.orders.size)
        assertTrue(parsedDb.showDemoData)

        val authJson = """{"token":"jwt123","uid":"u1","email":"a@b.com","status":"ready","isAdmin":false}"""
        val parsedAuth = moshi.adapter(BazmoAuthResponse::class.java).fromJson(authJson)
        assertNotNull(parsedAuth)
        assertEquals("ready", parsedAuth!!.status)

        val usersWrapper = moshi.adapter(AdminUsersWrapper::class.java).fromJson("""{"users":[]}""")
        assertNotNull(usersWrapper)
        val userWrapper = moshi.adapter(UserWrapper::class.java).fromJson("""{"user":null}""")
        assertNotNull(userWrapper)
        val notifWrapper = moshi.adapter(NotificationsWrapper::class.java).fromJson("""{"notifications":[]}""")
        assertNotNull(notifWrapper)
    }

    @Test
    fun `unified subtabs, store detail, and modals render cleanly without crash`() {
        val vm = testViewModel(isAdmin = false)
        val hwStore = StoreEntity(
            id = "st_hw_1",
            name = "فروشگاه قطعات تست",
            province = "تهران",
            city = "تهران",
            region = "مرکز",
            phone = "09120000000",
            address = "خیابان جمهوری",
            hours = "۹ تا ۲۰",
            type = "hw",
            ownerId = "u_hw",
            status = "approved"
        )
        val pendingOwnedAd = HwAdEntity(
            id = "ad_pending_1",
            cat = "تاچ و ال‌سی‌دی",
            title = "ال‌سی‌دی تست در انتظار",
            brand = "Apple",
            model = "iPhone 13",
            series = "A2633",
            price = 4_500_000L,
            seller = "فروشگاه قطعات تست",
            phone = "09120000000",
            province = "تهران",
            city = "تهران",
            region = "مرکز",
            ownerId = "u_hw",
            status = "pending",
            section = "hw"
        )
        val db = BazmoDbState(
            stores = listOf(hwStore),
            hwCategories = listOf(HwCategoryEntity(id = "c1", name = "تاچ و ال‌سی‌دی", iconEmoji = "📱")),
            hwAds = listOf(pendingOwnedAd),
            swProblems = listOf(SwProblemEntity(id = "p1", title = "FRP و آنلاک", iconEmoji = "🔓")),
            tutorials = listOf(TutorialEntity(id = "t1", problemId = "p1", title = "آموزش تست", teacher = "استاد", price = 200_000L, terms = "آنلاین", city = "تهران", status = "approved")),
            boxes = listOf(BoxEntity(id = "b1", name = "باکس تست", price = 3_000_000L, seller = "فروشنده", city = "تهران", condition = "نو", status = "approved")),
            accessories = listOf(AccessoryEntity(id = "a1", cat = "کابل و آداپتور", name = "کابل فست", storeId = "st_hw_1", price = 150_000L, desc = "اصلی"))
        )
        val baseSession = BazmoSessionState(
            role = AppRole.USER,
            isAdminUser = false,
            currentUser = UserEntity(id = "u_hw", uid = "u_hw", fullName = "علی رضایی", phone = "09120000000", tier = BazmoRoles.REPAIR_TECH),
            cart = listOf(CartItem("i1", "کابل فست", 150_000L, "فروشگاه قطعات تست", 1))
        )
        var step by mutableIntStateOf(0)

        composeRule.setContent {
            BazmoTheme {
                when (step) {
                    0 -> SoftwareTabScreen(
                        state = BazmoUiState(isReady = true, currentUid = "u_hw", db = db, session = baseSession),
                        viewModel = vm
                    )
                    1 -> SoftwareTabScreen(
                        state = BazmoUiState(
                            isReady = true,
                            currentUid = "u_hw",
                            db = db,
                            session = baseSession.copy(swView = SwUiFilter(mode = SoftwareSubMode.BOXES))
                        ),
                        viewModel = vm
                    )
                    2 -> HardwareTabScreen(
                        state = BazmoUiState(
                            isReady = true,
                            currentUid = "u_hw",
                            db = db,
                            session = baseSession.copy(hwView = HwUiFilter(mode = HardwareSubMode.STORE_DETAIL, storeId = hwStore.id))
                        ),
                        viewModel = vm
                    )
                    3 -> HardwareTabScreen(
                        state = BazmoUiState(
                            isReady = true,
                            currentUid = "u_hw",
                            db = db,
                            session = baseSession.copy(hwView = HwUiFilter(mode = HardwareSubMode.STORE_DETAIL, storeId = "missing_store"))
                        ),
                        viewModel = vm
                    )
                    4 -> AccessoriesTabScreen(
                        state = BazmoUiState(isReady = true, currentUid = "u_hw", db = db, session = baseSession),
                        viewModel = vm
                    )
                    5 -> UserShellScreen(
                        state = BazmoUiState(isReady = true, currentUid = "u_hw", db = db, session = baseSession.copy(modal = BazmoModal.Cart)),
                        viewModel = vm,
                        onSignOut = {}
                    )
                    else -> UserShellScreen(
                        state = BazmoUiState(isReady = true, currentUid = "u_hw", db = db, session = baseSession.copy(modal = BazmoModal.ProfileSetup)),
                        viewModel = vm,
                        onSignOut = {}
                    )
                }
            }
        }

        for (i in 0..6) {
            composeRule.runOnUiThread { step = i }
            composeRule.waitForIdle()
        }
        composeRule.onNodeWithTag("profile_locked_address_box").assertExists()
    }
}
