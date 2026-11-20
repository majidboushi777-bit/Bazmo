package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.BackgroundNotifConfig
import com.example.data.BannerEntity
import com.example.data.BazmoAuthManager
import com.example.data.BazmoDbState
import com.example.data.BazmoLocations
import com.example.data.BazmoNotificationHelper
import com.example.data.BazmoRepository
import com.example.data.BazmoRoles
import com.example.data.BazmoSeedData
import com.example.data.BazmoServerConfig
import com.example.data.BazmoSessionUser
import com.example.data.ChatMessage
import com.example.data.ChatThreadEntity
import com.example.data.DeskRentalEntity
import com.example.data.DeskRequestEntity
import com.example.data.ForumPostEntity
import com.example.data.LocationPoint
import com.example.data.NotificationEntity
import com.example.data.OrderCustomer
import com.example.data.OrderEntity
import com.example.data.OrderItem
import com.example.data.RegistrationRequestEntity
import com.example.data.StoreEntity
import com.example.data.UserEntity
import com.example.data.UserInviteEntity
import com.example.data.emailToInviteId
import com.example.data.filterByLocation
import com.example.data.formatToman
import com.example.data.isValidEmailFormat
import com.example.data.normalizeEmail
import com.example.viewmodel.AdminUserFilter
import com.example.viewmodel.BazmoModal
import com.example.viewmodel.BazmoSessionState
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.resolvePromoBannerSlots
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read app name from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("بازمو", appName)
    }

    @Test
    fun `firestore seed data and create maps omit documentId and match schema`() {
        val adminUid = "admin_test_uid"
        assertEquals(8, BazmoSeedData.stores.size)
        assertEquals(7, BazmoSeedData.mobiles.size)
        assertEquals(4, BazmoSeedData.forumPosts(adminUid).size)
        assertEquals(1, BazmoSeedData.notifications().size)

        val userMap = UserEntity(
            id = "u_1",
            uid = "u_1",
            email = "test@example.com",
            fullName = "کاربر تست",
            phone = "09120000000"
        ).toCreateMap()
        assertFalse(userMap.containsKey("id"))
        assertEquals("u_1", userMap["uid"])
        assertEquals(true, userMap["active"])

        val orderMap = OrderEntity(
            id = "ORD-1001",
            userId = "u_1",
            items = listOf(OrderItem("ac1", "قاب", 480000L, 1, "فروشگاه")),
            total = 480000L,
            deliveryType = "پیک",
            customer = OrderCustomer("علی", "رضایی", "0912", "تهران"),
            status = "ارسال شده به فروشگاه"
        ).toCreateMap()
        assertFalse(orderMap.containsKey("id"))
        assertEquals("u_1", orderMap["userId"])

        val chatMap = ChatThreadEntity(
            id = "ad1_u1",
            adId = "ad1",
            buyerUid = "u_1",
            sellerUid = "seller_1",
            participants = listOf("u_1", "seller_1"),
            messages = listOf(ChatMessage("u_1", "علی", "سلام", 1000L))
        ).toCreateMap()
        assertFalse(chatMap.containsKey("id"))
        assertEquals(listOf("u_1", "seller_1"), chatMap["participants"])
    }

    @Test
    fun `email invite helpers normalize to lowercase email and omit documentId`() {
        val normalized = normalizeEmail("  Ali.Rezaei@Gmail.COM ")
        assertEquals("ali.rezaei@gmail.com", normalized)
        assertTrue(isValidEmailFormat("  Ali.Rezaei@Gmail.COM "))
        assertFalse(isValidEmailFormat("invalid_email"))

        val id1 = emailToInviteId("Ali.Rezaei@Gmail.COM")
        val id2 = emailToInviteId("  ali.rezaei@gmail.com  ")
        assertEquals("ali.rezaei@gmail.com", id1)
        assertEquals(id1, id2)

        val inviteMap = UserInviteEntity(
            id = id1,
            email = "  Ali.Rezaei@Gmail.COM ",
            fullName = "علی رضایی",
            phone = "09121112233",
            tier = BazmoRoles.MOBILE_SELLER,
            province = "تهران",
            city = "تهران",
            region = "علاءالدین و جمهوری",
            active = true,
            sourceUserDocId = "u_tmp123",
            claimedByUid = ""
        ).toCreateMap()
        assertFalse(inviteMap.containsKey("id"))
        assertEquals("ali.rezaei@gmail.com", inviteMap["email"])
        assertEquals(BazmoRoles.MOBILE_SELLER, inviteMap["tier"])
        assertEquals("u_tmp123", inviteMap["sourceUserDocId"])
        assertEquals(true, inviteMap["active"])
    }

    @Test
    fun `bazmo 7-role RBAC rules match specification and exclude legacy roles`() {
        assertEquals(7, BazmoRoles.ALL_ROLES.size)
        assertFalse(BazmoRoles.ALL_ROLES.contains("سخت‌افزارکار"))
        assertFalse(BazmoRoles.ALL_ROLES.contains("سختافزارکار"))

        // Only 3 wholesale / part-seller roles can create stores
        assertTrue(BazmoRoles.canCreateStore(BazmoRoles.WHOLESALE_MOBILE, isAdmin = false))
        assertTrue(BazmoRoles.canCreateStore(BazmoRoles.WHOLESALE_ACCESSORY, isAdmin = false))
        assertTrue(BazmoRoles.canCreateStore(BazmoRoles.PART_SELLER, isAdmin = false))
        assertFalse(BazmoRoles.canCreateStore(BazmoRoles.REPAIR_TECH, isAdmin = false))
        assertFalse(BazmoRoles.canCreateStore(BazmoRoles.SOFTWARE_TECH, isAdmin = false))
        assertFalse(BazmoRoles.canCreateStore(BazmoRoles.MOBILE_SELLER, isAdmin = false))
        assertFalse(BazmoRoles.canCreateStore(BazmoRoles.ACCESSORY_SELLER, isAdmin = false))

        // Hardware forum strictly for تعمیرکار
        assertTrue(BazmoRoles.canAccessHardwareForum(BazmoRoles.REPAIR_TECH, isAdmin = false))
        assertFalse(BazmoRoles.canAccessHardwareForum(BazmoRoles.SOFTWARE_TECH, isAdmin = false))
        assertFalse(BazmoRoles.canAccessHardwareForum(BazmoRoles.PART_SELLER, isAdmin = false))

        // Software forum & tutorials strictly for نرم‌افزارکار
        assertTrue(BazmoRoles.canAccessSoftwareSpecialized(BazmoRoles.SOFTWARE_TECH, isAdmin = false))
        assertFalse(BazmoRoles.canAccessSoftwareSpecialized(BazmoRoles.REPAIR_TECH, isAdmin = false))
        assertFalse(BazmoRoles.canAccessSoftwareSpecialized(BazmoRoles.MOBILE_SELLER, isAdmin = false))

        // Default store types
        assertEquals("mobile", BazmoRoles.defaultStoreTypeForRole(BazmoRoles.WHOLESALE_MOBILE))
        assertEquals("acc", BazmoRoles.defaultStoreTypeForRole(BazmoRoles.WHOLESALE_ACCESSORY))
        assertEquals("hw", BazmoRoles.defaultStoreTypeForRole(BazmoRoles.PART_SELLER))

        // Registration request map forces pending status and lowercase email
        val regMap = RegistrationRequestEntity(
            id = "req_1",
            email = "  User@Gmail.com ",
            fullName = "رضا محمدی",
            phone = "09123334455",
            requestedTier = BazmoRoles.WHOLESALE_MOBILE,
            status = "approved" // should still force pending in toCreateMap
        ).toCreateMap()
        assertFalse(regMap.containsKey("id"))
        assertEquals("user@gmail.com", regMap["email"])
        assertEquals("pending", regMap["status"])
        assertEquals(BazmoRoles.WHOLESALE_MOBILE, regMap["requestedTier"])
    }

    @Test
    fun `forum post vote and save helpers are uid scoped`() {
        val post = ForumPostEntity(
            id = "fp1",
            upvotedBy = listOf("alice"),
            downvotedBy = listOf("bob"),
            savedBy = listOf("alice")
        )
        assertEquals(1, post.userVoteFor("alice"))
        assertEquals(-1, post.userVoteFor("bob"))
        assertEquals(0, post.userVoteFor("charlie"))
        assertTrue(post.isSavedBy("alice"))
        assertFalse(post.isSavedBy("bob"))
    }

    @Test
    fun `location cascade and persian formatting work properly`() {
        assertTrue(BazmoLocations.provinces().contains("تهران"))
        assertEquals("۴۸۰,۰۰۰ تومان", 480000L.formatToman())

        val items = listOf(
            LocationPoint("تهران", "تهران", "علاءالدین و جمهوری"),
            LocationPoint("اصفهان", "اصفهان", "فردوسی و بازار موبایل")
        )
        val regionResult = filterByLocation(items, "تهران", "تهران", "علاءالدین و جمهوری") { it }
        assertEquals(1, regionResult.list.size)
        assertEquals("تهران (علاءالدین و جمهوری)", regionResult.scope)

        val fallbackCountry = filterByLocation(items, "فارس", "شیراز", "ملاصدرا") { it }
        assertEquals(2, fallbackCountry.list.size)
        assertEquals("سراسر کشور", fallbackCountry.scope)
    }

    @Test
    fun `newly created and reactivated users appear properly in admin user list state`() {
        val blockedUser = UserEntity(
            id = "u_abc123",
            uid = "u_abc123",
            email = "blocked@gmail.com",
            fullName = "علی احمدی",
            phone = "09120001111",
            tier = BazmoRoles.REPAIR_TECH,
            province = "تهران",
            city = "تهران",
            region = "علاءالدین و جمهوری",
            active = false
        )
        val reactivated = blockedUser.copy(active = true)
        assertTrue(reactivated.active)
        assertEquals("u_abc123", reactivated.id)

        val newUser = UserEntity(
            id = "u_new999",
            uid = "u_new999",
            email = normalizeEmail(" NewUser@Gmail.com "),
            fullName = "سارا محمدی",
            phone = "09123334444",
            tier = BazmoRoles.MOBILE_SELLER,
            province = "تهران",
            city = "تهران",
            region = "همه مناطق",
            active = true
        )
        val updatedList = (listOf(reactivated) + newUser).sortedBy { it.fullName }
        assertEquals(2, updatedList.size)
        assertTrue(updatedList.any { it.email == "newuser@gmail.com" && it.active })
        assertTrue(updatedList.any { it.id == "u_abc123" && it.active })
    }

    @Test
    fun `server config normalizes http and https urls to websocket endpoints`() {
        assertEquals("https://bazmo.weblines.ir", BazmoServerConfig.baseUrl())
        assertEquals("wss://bazmo.weblines.ir/ws", BazmoServerConfig.wsUrl("https://bazmo.weblines.ir/"))
        assertEquals("ws://localhost:3000/ws", BazmoServerConfig.wsUrl("http://localhost:3000/"))
    }

    @Test
    fun `notification helper deduplicates live notifications and avoids backlog spam`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        BazmoNotificationHelper.ensureChannel(context)

        val n1 = NotificationEntity(id = "nt_test_1", title = "اعلان ۱", body = "متن ۱")
        val n2 = NotificationEntity(id = "nt_test_2", title = "اعلان ۲", body = "متن ۲")

        val initial = BazmoNotificationHelper.dispatchNewNotifications(context, listOf(n1), isInitialSnapshot = true)
        assertTrue(initial.isEmpty())

        val duplicate = BazmoNotificationHelper.dispatchNewNotifications(context, listOf(n1), isInitialSnapshot = false)
        assertTrue(duplicate.isEmpty())

        val live = BazmoNotificationHelper.dispatchNewNotifications(context, listOf(n1, n2), isInitialSnapshot = false)
        assertEquals(1, live.size)
        assertEquals("nt_test_2", live.first().id)
    }

    @Test
    fun `location normalization and all-scope helpers match with or without zwnj`() {
        assertTrue(BazmoLocations.citiesFor("اصفهان").contains("نجف\u200cآباد"))
        assertTrue(BazmoLocations.isAllScope("همه استان\u200cها"))
        assertTrue(BazmoLocations.isAllScope("همه استانها"))
        assertTrue(BazmoLocations.isAllScope("همه بخش\u200cها"))
        assertFalse(BazmoLocations.isAllScope("تهران"))
        assertEquals(
            BazmoLocations.normalizeZwnj("نجف\u200cآباد"),
            BazmoLocations.normalizeZwnj("نجفآباد")
        )
    }

    @Test
    fun `background notification config saves loads and clears cleanly`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        BazmoNotificationHelper.clearBackgroundConfig(context)
        assertEquals(null, BazmoNotificationHelper.loadBackgroundConfig(context))

        val cfg = BackgroundNotifConfig(
            token = "tok_123",
            uid = "u_77",
            tier = BazmoRoles.REPAIR_TECH,
            isAdmin = false,
            province = "اصفهان",
            city = "نجف\u200cآباد",
            region = "مرکز شهر"
        )
        BazmoNotificationHelper.saveBackgroundConfig(context, cfg)
        val loaded = BazmoNotificationHelper.loadBackgroundConfig(context)
        assertEquals(cfg, loaded)

        BazmoNotificationHelper.clearBackgroundConfig(context)
        assertEquals(null, BazmoNotificationHelper.loadBackgroundConfig(context))
    }

    @Test
    fun `filterMatchingUnread matches region all-scope and zwnj-normalized tiers`() {
        val nSpecificRegion = NotificationEntity(
            id = "n1",
            title = "تست",
            body = "متن",
            province = "تهران",
            city = "تهران",
            region = "علاءالدین و جمهوری",
            tierFilter = "نرم افزارکار",
            readBy = emptyList()
        )
        val nAlreadyRead = nSpecificRegion.copy(id = "n2", readBy = listOf("u_1"))
        val nOtherCity = nSpecificRegion.copy(id = "n3", city = "شهریار")

        val matches = BazmoNotificationHelper.filterMatchingUnread(
            notifications = listOf(nSpecificRegion, nAlreadyRead, nOtherCity),
            uid = "u_1",
            isAdmin = false,
            tier = "نرم\u200cافزارکار",
            province = "تهران",
            city = "تهران",
            region = "همه مناطق"
        )
        assertEquals(1, matches.size)
        assertEquals("n1", matches.first().id)

        val context = ApplicationProvider.getApplicationContext<Context>()
        assertTrue(BazmoNotificationHelper.isRealtimeServiceEnabled(context))
        BazmoNotificationHelper.setRealtimeServiceEnabled(context, false)
        assertFalse(BazmoNotificationHelper.isRealtimeServiceEnabled(context))
        BazmoNotificationHelper.setRealtimeServiceEnabled(context, true)
        assertTrue(BazmoNotificationHelper.buildRealtimeServiceNotification(context).extras != null)
    }

    @Test
    fun `desk rental and store owner entities preserve fields and omit id`() {
        val store = StoreEntity(
            id = "st_custom",
            name = "موبایل نمونه",
            province = "تهران",
            city = "تهران",
            region = "علاءالدین و جمهوری",
            status = "pending",
            isDemo = true
        )
        val storeMap = store.toCreateMap()
        assertFalse(storeMap.containsKey("id"))
        assertEquals("pending", storeMap["status"])
        assertEquals("موبایل نمونه", storeMap["name"])
        assertTrue(store.isDemo)

        val rental = DeskRentalEntity(
            id = "dr_1",
            title = "اجاره میز تعمیرکار",
            category = "تعمیرات سخت‌افزار",
            storeName = "مرکز موبایل",
            province = "اصفهان",
            city = "اصفهان",
            region = "فردوسی و بازار موبایل",
            rentDaily = 400_000L,
            rentMonthly = 8_500_000L,
            cooperationModel = "درصدی یا ثابت",
            equipment = "هیتر و لوپ",
            phone = "09130001111",
            ownerId = "u_1",
            ownerName = "حسین",
            status = "pending",
            desc = "میز کامل"
        )
        val rentalMap = rental.toCreateMap()
        assertFalse(rentalMap.containsKey("id"))
        assertEquals("اجاره میز تعمیرکار", rentalMap["title"])
        assertEquals(8_500_000L, rentalMap["rentMonthly"])
        assertEquals("pending", rentalMap["status"])

        val req = DeskRequestEntity(
            id = "drq_1",
            title = "درخواست میز نرم‌افزار",
            category = "نرم‌افزار و فلش",
            requesterName = "امیر",
            requesterPhone = "09120002222",
            province = "فارس",
            city = "شیراز",
            region = "ملاصدرا",
            proposedBudget = 6_000_000L,
            proposalText = "همکاری با تجربه ۵ ساله",
            ownerId = "u_2",
            status = "open"
        )
        val reqMap = req.toCreateMap()
        assertFalse(reqMap.containsKey("id"))
        assertEquals(6_000_000L, reqMap["proposedBudget"])
        assertEquals("همکاری با تجربه ۵ ساله", reqMap["proposalText"])

        val dbState = BazmoDbState()
        assertTrue(dbState.showDemoData)
        assertTrue(dbState.deskRentals.isEmpty())
        assertTrue(dbState.deskRequests.isEmpty())
    }

    @Test
    fun `biometric saved account persists across cold start unlocks last account and clears on signOut`() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        BazmoAuthManager.signOut(context, clearCredentialManager = false)
        BazmoAuthManager.resetInMemoryForTest()
        assertNull(BazmoAuthManager.savedAccount(context))
        assertNull(BazmoAuthManager.currentSession(context))

        // Persist a signed-in user directly via SharedPreferences schema used by BazmoAuthManager
        val prefs = context.getSharedPreferences("bazmo_auth_session", Context.MODE_PRIVATE)
        prefs.edit()
            .putString("uid", "u_bio_1")
            .putString("email", "tech@bazmo.ir")
            .putString("display_name", "تعمیرکار نمونه")
            .putString("token", "jwt_test_token")
            .putBoolean("is_admin", false)
            .putBoolean("email_verified", true)
            .apply()

        BazmoAuthManager.resetInMemoryForTest()

        // Cold start: active session stays null (locked), while saved account is present
        assertNull(BazmoAuthManager.currentSession(context))
        val saved = BazmoAuthManager.savedAccount(context)
        assertEquals("u_bio_1", saved?.uid)
        assertEquals("tech@bazmo.ir", saved?.email)

        // Biometric verification unlocks that exact saved account
        val unlockResult = BazmoAuthManager.unlockSavedAccountWithBiometric(context)
        assertTrue(unlockResult.isSuccess)
        assertEquals("u_bio_1", BazmoAuthManager.currentSession(context)?.uid)

        // Overwrite with a newer account login and verify cold start uses latest account
        prefs.edit()
            .putString("uid", "u_bio_2")
            .putString("email", "latest@bazmo.ir")
            .putString("display_name", "آخرین اکانت")
            .putString("token", "jwt_latest_token")
            .apply()
        BazmoAuthManager.resetInMemoryForTest()
        assertNull(BazmoAuthManager.currentSession(context))
        assertEquals("latest@bazmo.ir", BazmoAuthManager.savedAccount(context)?.email)
        assertEquals("u_bio_2", BazmoAuthManager.unlockSavedAccountWithBiometric(context).getOrThrow().uid)

        // Normal signOut clears active session while keeping saved biometric account ready
        BazmoAuthManager.signOut(context, clearCredentialManager = false)
        assertNull(BazmoAuthManager.currentSession(context))
        assertEquals("u_bio_2", BazmoAuthManager.savedAccount(context)?.uid)
        assertTrue(BazmoAuthManager.unlockSavedAccountWithBiometric(context).isSuccess)

        // Explicit forgetSavedAccount=true clears both active and saved biometric accounts
        BazmoAuthManager.signOut(context, clearCredentialManager = false, forgetSavedAccount = true)
        assertNull(BazmoAuthManager.currentSession(context))
        assertNull(BazmoAuthManager.savedAccount(context))
        assertTrue(BazmoAuthManager.unlockSavedAccountWithBiometric(context).isFailure)
    }

    @Test
    fun `unified desk modal and seeding lock defaults work cleanly`() {
        val defaultRental = BazmoModal.NewDeskEntry()
        val requestMode = BazmoModal.NewDeskEntry("request")
        assertEquals("rental", defaultRental.initialKind)
        assertEquals("request", requestMode.initialKind)

        val session = BazmoSessionState()
        assertFalse(session.isSeedingDemo)
    }

    @Test
    fun `repository sanitizeAsciiEtag rejects non-ascii headers and accepts valid etags`() {
        val repo = BazmoRepository(authToken = "tok_test", currentUid = "u_1")
        assertNull(repo.sanitizeAsciiEtag(null))
        assertNull(repo.sanitizeAsciiEtag("   "))
        assertNull(repo.sanitizeAsciiEtag("\"v1-تعمیرکار-1-u_1\""))
        assertEquals("\"abc123_DEF-\"", repo.sanitizeAsciiEtag("  \"abc123_DEF-\"  "))
        assertEquals("W/\"abc123\"", repo.sanitizeAsciiEtag("W/\"abc123\""))
    }

    @Test
    fun `all-country and all-city location filters return expected scopes`() {
        val items = listOf(
            LocationPoint("تهران", "تهران", "علاءالدین و جمهوری"),
            LocationPoint("اصفهان", "اصفهان", "فردوسی و بازار موبایل"),
            LocationPoint("فارس", "شیراز", "ملاصدرا")
        )
        assertTrue(BazmoLocations.isAllScope(BazmoLocations.ALL_COUNTRY))
        assertTrue(BazmoLocations.provincesWithAll().first() == BazmoLocations.ALL_COUNTRY)
        assertEquals(listOf(BazmoLocations.ALL_CITIES), BazmoLocations.citiesWithAllFor(BazmoLocations.ALL_COUNTRY))

        val allCountryRes = filterByLocation(
            items,
            BazmoLocations.ALL_COUNTRY,
            BazmoLocations.ALL_CITIES,
            BazmoLocations.ALL_REGIONS
        ) { it }
        assertEquals(3, allCountryRes.list.size)
        assertEquals("سراسر کشور", allCountryRes.scope)

        val allCitiesInProvRes = filterByLocation(
            items,
            "اصفهان",
            BazmoLocations.ALL_CITIES,
            BazmoLocations.ALL_REGIONS
        ) { it }
        assertEquals(1, allCitiesInProvRes.list.size)
        assertEquals("استان اصفهان", allCitiesInProvRes.scope)

        val tehranNotif = NotificationEntity(
            id = "n_country",
            title = "اعلان تهران",
            body = "متن",
            province = "تهران",
            city = "تهران",
            region = "علاءالدین و جمهوری",
            tierFilter = BazmoRoles.REPAIR_TECH
        )
        val matched = BazmoNotificationHelper.filterMatchingUnread(
            notifications = listOf(tehranNotif),
            uid = "u_99",
            isAdmin = false,
            tier = BazmoRoles.REPAIR_TECH,
            province = BazmoLocations.ALL_COUNTRY,
            city = BazmoLocations.ALL_CITIES,
            region = BazmoLocations.ALL_REGIONS
        )
        assertEquals(1, matched.size)
    }

    @Test
    fun `theme manager toggles light and dark mode and persists preference`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("bazmo_theme_prefs", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        com.example.ui.theme.BazmoThemeManager.resetInMemoryForTest()

        assertEquals(
            com.example.ui.theme.BazmoThemeMode.SYSTEM,
            com.example.ui.theme.BazmoThemeManager.modeFlow(context).value
        )
        assertFalse(
            com.example.ui.theme.BazmoThemeManager.resolveDarkTheme(
                com.example.ui.theme.BazmoThemeMode.SYSTEM,
                systemInDarkTheme = false
            )
        )
        assertTrue(
            com.example.ui.theme.BazmoThemeManager.resolveDarkTheme(
                com.example.ui.theme.BazmoThemeMode.SYSTEM,
                systemInDarkTheme = true
            )
        )

        com.example.ui.theme.BazmoThemeManager.toggleDark(context, currentlyDark = false)
        assertEquals(
            com.example.ui.theme.BazmoThemeMode.DARK,
            com.example.ui.theme.BazmoThemeManager.modeFlow(context).value
        )

        // Simulate cold restart
        com.example.ui.theme.BazmoThemeManager.resetInMemoryForTest()
        assertEquals(
            com.example.ui.theme.BazmoThemeMode.DARK,
            com.example.ui.theme.BazmoThemeManager.modeFlow(context).value
        )

        com.example.ui.theme.BazmoThemeManager.toggleDark(context, currentlyDark = true)
        assertEquals(
            com.example.ui.theme.BazmoThemeMode.LIGHT,
            com.example.ui.theme.BazmoThemeManager.modeFlow(context).value
        )
    }

    @Test
    fun `promo banners resolve 4 fixed slots with region-city-province-nationwide fallback`() {
        val banners = listOf(
            BannerEntity(
                id = "b_nat_0",
                section = "mobile",
                slotIndex = 0,
                province = BazmoLocations.ALL_COUNTRY,
                city = BazmoLocations.ALL_CITIES,
                region = BazmoLocations.ALL_REGIONS,
                title = "Nationwide 0",
                hasImage = true,
                imageUrl = "/api/banners/b_nat_0/image"
            ),
            BannerEntity(
                id = "b_prov_0",
                section = "mobile",
                slotIndex = 0,
                province = "تهران",
                city = BazmoLocations.ALL_CITIES,
                region = BazmoLocations.ALL_REGIONS,
                title = "Tehran Prov 0",
                hasImage = true,
                imageUrl = "/api/banners/b_prov_0/image"
            ),
            BannerEntity(
                id = "b_reg_0",
                section = "mobile",
                slotIndex = 0,
                province = "تهران",
                city = "تهران",
                region = "علاءالدین و جمهوری",
                title = "Alaeddin 0",
                hasImage = true,
                imageUrl = "/api/banners/b_reg_0/image"
            ),
            BannerEntity(
                id = "b_nat_2",
                section = "mobile",
                slotIndex = 2,
                province = BazmoLocations.ALL_COUNTRY,
                city = BazmoLocations.ALL_CITIES,
                region = BazmoLocations.ALL_REGIONS,
                title = "Nationwide 2",
                hasImage = true,
                imageUrl = "/api/banners/b_nat_2/image"
            ),
            BannerEntity(
                id = "b1_empty",
                section = "mobile",
                slotIndex = 1,
                title = "Empty",
                hasImage = false,
                imageUrl = ""
            ),
            BannerEntity(
                id = "b_hw",
                section = "hw",
                slotIndex = 0,
                title = "HW0",
                hasImage = true,
                imageUrl = "/api/banners/hw0/image"
            )
        )

        // Exact region match beats province & nationwide on slot 0, falls back to nationwide on slot 2, null on empty slots 1 & 3
        val alaeddinSlots = resolvePromoBannerSlots(
            banners = banners,
            section = "mobile",
            province = "تهران",
            city = "تهران",
            region = "علاءالدین و جمهوری"
        )
        assertEquals(4, alaeddinSlots.size)
        assertEquals(listOf("b_reg_0", null, "b_nat_2", null), alaeddinSlots.map { it?.id })

        // Different region in Tehran falls back to province-wide on slot 0
        val shahriarSlots = resolvePromoBannerSlots(
            banners = banners,
            section = "mobile",
            province = "تهران",
            city = "شهریار",
            region = "مرکز شهر"
        )
        assertEquals(listOf("b_prov_0", null, "b_nat_2", null), shahriarSlots.map { it?.id })

        // Other province falls back to nationwide
        val shirazUiState = BazmoUiState(
            db = BazmoDbState(banners = banners),
            session = BazmoSessionState(province = "فارس", city = "شیراز", region = "ملاصدرا")
        )
        assertEquals(listOf("b_nat_0", null, "b_nat_2", null), shirazUiState.bannersForSection("mobile").map { it?.id })
        assertEquals(listOf("b_hw", null, null, null), shirazUiState.bannersForSection("hw").map { it?.id })
        assertEquals(listOf(null, null, null, null), shirazUiState.bannersForSection("acc"))
    }

    @Test
    fun `zwnj and digit normalization plus admin filter all-scope work reliably`() {
        assertEquals(
            BazmoLocations.normalizeZwnj("نرم\u200cافزارکار"),
            BazmoLocations.normalizeZwnj("نرم افزارکار")
        )
        assertEquals(
            BazmoLocations.normalizeZwnj("قطعه\u200cفروش"),
            BazmoLocations.normalizeZwnj("قطعه فروش")
        )
        assertEquals(
            BazmoLocations.normalizeZwnj("۰۹۱۲۳۴۵۶۷۸۹"),
            BazmoLocations.normalizeZwnj("09123456789")
        )
        assertTrue(BazmoLocations.isAllScope("همه نقش\u200cها"))
        assertTrue(BazmoLocations.isAllScope("همه وضعیت\u200cها"))
        assertTrue(BazmoLocations.isAllScope("کل کشور"))
        assertTrue(BazmoLocations.isAllScope("همه شهرهای استان"))
        assertEquals("همه شهرها", AdminUserFilter().city)
    }
}
