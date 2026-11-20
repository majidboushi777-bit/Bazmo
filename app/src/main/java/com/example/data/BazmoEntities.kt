package com.example.data

import androidx.annotation.Keep

@Keep
data class OrderItem(
    val id: String = "",
    val name: String = "",
    val price: Long = 0L,
    val qty: Int = 1,
    val storeName: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "name" to name,
        "price" to price,
        "qty" to qty,
        "storeName" to storeName
    )
}

@Keep
data class OrderCustomer(
    val firstName: String = "",
    val lastName: String = "",
    val phone: String = "",
    val address: String = ""
) {
    fun toMap(): Map<String, Any> = mapOf(
        "firstName" to firstName,
        "lastName" to lastName,
        "phone" to phone,
        "address" to address
    )
}

@Keep
data class ChatMessage(
    val senderUid: String = "",
    val senderName: String = "",
    val text: String = "",
    val sentAtMs: Long = 0L
) {
    fun toMap(): Map<String, Any> = mapOf(
        "senderUid" to senderUid,
        "senderName" to senderName,
        "text" to text,
        "sentAtMs" to sentAtMs
    )
}

@Keep
data class ForumReply(
    val id: String = "",
    val author: String = "",
    val badge: String = "",
    val text: String = "",
    val votes: Int = 1,
    val upvotedBy: List<String> = emptyList(),
    val downvotedBy: List<String> = emptyList(),
    val timeLabel: String = ""
) {
    fun userVoteFor(uid: String): Int = when {
        upvotedBy.contains(uid) -> 1
        downvotedBy.contains(uid) -> -1
        else -> 0
    }

    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "author" to author,
        "badge" to badge,
        "text" to text,
        "votes" to votes,
        "upvotedBy" to upvotedBy,
        "downvotedBy" to downvotedBy,
        "timeLabel" to timeLabel
    )
}

@Keep
data class ForumComment(
    val id: String = "",
    val author: String = "",
    val badge: String = "",
    val text: String = "",
    val votes: Int = 1,
    val upvotedBy: List<String> = emptyList(),
    val downvotedBy: List<String> = emptyList(),
    val timeLabel: String = "",
    val replies: List<ForumReply> = emptyList()
) {
    fun userVoteFor(uid: String): Int = when {
        upvotedBy.contains(uid) -> 1
        downvotedBy.contains(uid) -> -1
        else -> 0
    }

    fun toMap(): Map<String, Any> = mapOf(
        "id" to id,
        "author" to author,
        "badge" to badge,
        "text" to text,
        "votes" to votes,
        "upvotedBy" to upvotedBy,
        "downvotedBy" to downvotedBy,
        "timeLabel" to timeLabel,
        "replies" to replies.map { it.toMap() }
    )
}

@Keep
data class UserEntity(
    val id: String = "",
    val uid: String = "",
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val tier: String = "تعمیرکار",
    val province: String = "تهران",
    val city: String = "تهران",
    val region: String = "همه مناطق",
    val active: Boolean = true,
    val createdDateLabel: String = "امروز",
    val createdAtSeconds: Long = 0L
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "uid" to uid,
        "email" to email,
        "fullName" to fullName,
        "phone" to phone,
        "tier" to tier,
        "province" to province,
        "city" to city,
        "region" to region,
        "active" to active,
        "createdDateLabel" to createdDateLabel
    )
}

@Keep
data class UserInviteEntity(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val tier: String = "تعمیرکار",
    val province: String = "تهران",
    val city: String = "تهران",
    val region: String = "همه مناطق",
    val active: Boolean = true,
    val createdDateLabel: String = "امروز",
    val sourceUserDocId: String = "",
    val claimedByUid: String = "",
    val createdAtSeconds: Long = 0L
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "email" to normalizeEmail(email),
        "fullName" to fullName,
        "phone" to phone,
        "tier" to tier,
        "province" to province,
        "city" to city,
        "region" to region,
        "active" to active,
        "createdDateLabel" to createdDateLabel,
        "sourceUserDocId" to sourceUserDocId,
        "claimedByUid" to claimedByUid
    )
}

@Keep
data class RegistrationRequestEntity(
    val id: String = "",
    val email: String = "",
    val fullName: String = "",
    val phone: String = "",
    val requestedTier: String = BazmoRoles.REPAIR_TECH,
    val province: String = "تهران",
    val city: String = "تهران",
    val region: String = "همه مناطق",
    val note: String = "",
    val status: String = "pending",
    val createdAtSeconds: Long = 0L
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "email" to normalizeEmail(email),
        "fullName" to fullName,
        "phone" to phone,
        "requestedTier" to BazmoRoles.normalizeTier(requestedTier),
        "province" to province,
        "city" to city,
        "region" to region,
        "note" to note,
        "status" to "pending"
    )
}

@Keep
data class NotificationEntity(
    val id: String = "",
    val title: String = "",
    val body: String = "",
    val type: String = "",
    val province: String = "همه استان‌ها",
    val city: String = "همه شهرها",
    val region: String = "همه مناطق",
    val tierFilter: String = "همه",
    val sectionFilter: String = "همه بخش‌ها",
    val date: String = "امروز",
    val readBy: List<String> = emptyList(),
    val published: Boolean = true,
    val sortOrder: Long = System.currentTimeMillis()
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "body" to body,
        "type" to type,
        "province" to province,
        "city" to city,
        "region" to region,
        "tierFilter" to tierFilter,
        "sectionFilter" to sectionFilter,
        "date" to date,
        "readBy" to readBy,
        "published" to true,
        "sortOrder" to sortOrder
    )
}

@Keep
data class StoreEntity(
    val id: String = "",
    val name: String = "",
    val province: String = "",
    val city: String = "",
    val region: String = "",
    val phone: String = "",
    val address: String = "",
    val hours: String = "",
    val rating: String = "۴.۸",
    val type: String = "both",
    val ownerId: String = "",
    val published: Boolean = true,
    val status: String = "approved",
    val isDemo: Boolean = false
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "province" to province,
        "city" to city,
        "region" to region,
        "phone" to phone,
        "address" to address,
        "hours" to hours,
        "rating" to rating,
        "type" to type,
        "ownerId" to ownerId,
        "published" to published,
        "status" to status
    )
}

@Keep
data class MobileEntity(
    val id: String = "",
    val brand: String = "",
    val model: String = "",
    val specs: String = "",
    val imgEmoji: String = "📱",
    val sortOrder: Long = 0L,
    val published: Boolean = true
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "brand" to brand,
        "model" to model,
        "specs" to specs,
        "imgEmoji" to imgEmoji,
        "sortOrder" to sortOrder,
        "published" to true
    )
}

@Keep
data class StoreMobileEntity(
    val id: String = "",
    val storeId: String = "",
    val mobileId: String = "",
    val price: Long = 0L,
    val info: String = "",
    val published: Boolean = true
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "storeId" to storeId,
        "mobileId" to mobileId,
        "price" to price,
        "info" to info,
        "published" to true
    )
}

@Keep
data class AccessoryEntity(
    val id: String = "",
    val cat: String = "",
    val name: String = "",
    val storeId: String = "",
    val price: Long = 0L,
    val desc: String = "",
    val published: Boolean = true
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "cat" to cat,
        "name" to name,
        "storeId" to storeId,
        "price" to price,
        "desc" to desc,
        "published" to true
    )
}

@Keep
data class InquiryEntity(
    val id: String = "",
    val accId: String = "",
    val accName: String = "",
    val storeName: String = "",
    val userId: String = "",
    val userName: String = "",
    val status: String = "",
    val date: String = "امروز",
    val sortOrder: Long = System.currentTimeMillis()
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "accId" to accId,
        "accName" to accName,
        "storeName" to storeName,
        "userId" to userId,
        "userName" to userName,
        "status" to status,
        "date" to date,
        "sortOrder" to sortOrder
    )
}

@Keep
data class SwProblemEntity(
    val id: String = "",
    val title: String = "",
    val iconEmoji: String = "🔧",
    val published: Boolean = true
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "iconEmoji" to iconEmoji,
        "published" to true
    )
}

@Keep
data class TutorialEntity(
    val id: String = "",
    val problemId: String = "",
    val title: String = "",
    val teacher: String = "",
    val rating: String = "۵.۰",
    val price: Long = 0L,
    val terms: String = "",
    val city: String = "",
    val status: String = "pending",
    val ownerId: String = ""
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "problemId" to problemId,
        "title" to title,
        "teacher" to teacher,
        "rating" to rating,
        "price" to price,
        "terms" to terms,
        "city" to city,
        "status" to status,
        "ownerId" to ownerId
    )
}

@Keep
data class SwRequestEntity(
    val id: String = "",
    val title: String = "",
    val userId: String = "",
    val userName: String = "",
    val city: String = "",
    val status: String = "",
    val reply: String = "",
    val published: Boolean = true,
    val sortOrder: Long = System.currentTimeMillis()
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "userId" to userId,
        "userName" to userName,
        "city" to city,
        "status" to status,
        "reply" to reply,
        "published" to true,
        "sortOrder" to sortOrder
    )
}

@Keep
data class BoxEntity(
    val id: String = "",
    val name: String = "",
    val price: Long = 0L,
    val seller: String = "",
    val city: String = "",
    val condition: String = "",
    val status: String = "pending",
    val ownerId: String = ""
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "price" to price,
        "seller" to seller,
        "city" to city,
        "condition" to condition,
        "status" to status,
        "ownerId" to ownerId
    )
}

@Keep
data class HwCategoryEntity(
    val id: String = "",
    val name: String = "",
    val iconEmoji: String = "🧩",
    val published: Boolean = true
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "name" to name,
        "iconEmoji" to iconEmoji,
        "published" to true
    )
}

@Keep
data class HwAdEntity(
    val id: String = "",
    val cat: String = "",
    val title: String = "",
    val brand: String = "",
    val model: String = "",
    val series: String = "",
    val price: Long = 0L,
    val seller: String = "",
    val phone: String = "",
    val city: String = "",
    val province: String = "",
    val region: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val status: String = "pending",
    val desc: String = "",
    val section: String = "hw",
    val sortOrder: Long = System.currentTimeMillis()
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "cat" to cat,
        "title" to title,
        "brand" to brand,
        "model" to model,
        "series" to series,
        "price" to price,
        "seller" to seller,
        "phone" to phone,
        "city" to city,
        "province" to province,
        "region" to region,
        "ownerId" to ownerId,
        "ownerName" to ownerName,
        "status" to status,
        "desc" to desc,
        "section" to section,
        "sortOrder" to sortOrder
    )
}

@Keep
data class HwRequestEntity(
    val id: String = "",
    val brand: String = "",
    val model: String = "",
    val partType: String = "",
    val series: String = "",
    val city: String = "",
    val userId: String = "",
    val userName: String = "",
    val status: String = "",
    val note: String = "",
    val sortOrder: Long = System.currentTimeMillis()
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "brand" to brand,
        "model" to model,
        "partType" to partType,
        "series" to series,
        "city" to city,
        "userId" to userId,
        "userName" to userName,
        "status" to status,
        "note" to note,
        "sortOrder" to sortOrder
    )
}

@Keep
data class OrderEntity(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val items: List<OrderItem> = emptyList(),
    val total: Long = 0L,
    val deliveryType: String = "پیک",
    val customer: OrderCustomer = OrderCustomer(),
    val status: String = "",
    val date: String = "امروز",
    val city: String = "",
    val sortOrder: Long = System.currentTimeMillis()
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "userName" to userName,
        "items" to items.map { it.toMap() },
        "total" to total,
        "deliveryType" to deliveryType,
        "customer" to customer.toMap(),
        "status" to status,
        "date" to date,
        "city" to city,
        "sortOrder" to sortOrder
    )
}

@Keep
data class ChatThreadEntity(
    val id: String = "",
    val adId: String = "",
    val buyerUid: String = "",
    val sellerUid: String = "",
    val participants: List<String> = emptyList(),
    val messages: List<ChatMessage> = emptyList()
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "adId" to adId,
        "buyerUid" to buyerUid,
        "sellerUid" to sellerUid,
        "participants" to participants,
        "messages" to messages.map { it.toMap() }
    )
}

@Keep
data class ForumPostEntity(
    val id: String = "",
    val board: String = "software",
    val flair: String = "",
    val author: String = "",
    val authorUid: String = "",
    val authorBadge: String = "",
    val city: String = "",
    val deviceTag: String = "",
    val pinned: Boolean = false,
    val verifiedSolution: Boolean = false,
    val title: String = "",
    val body: String = "",
    val votes: Int = 1,
    val upvotedBy: List<String> = emptyList(),
    val downvotedBy: List<String> = emptyList(),
    val savedBy: List<String> = emptyList(),
    val createdAtMs: Long = System.currentTimeMillis(),
    val timeLabel: String = "هم‌اکنون",
    val comments: List<ForumComment> = emptyList(),
    val published: Boolean = true
) {
    fun userVoteFor(uid: String): Int = when {
        upvotedBy.contains(uid) -> 1
        downvotedBy.contains(uid) -> -1
        else -> 0
    }

    fun isSavedBy(uid: String): Boolean = savedBy.contains(uid)

    fun toCreateMap(): Map<String, Any?> = mapOf(
        "board" to board,
        "flair" to flair,
        "author" to author,
        "authorUid" to authorUid,
        "authorBadge" to authorBadge,
        "city" to city,
        "deviceTag" to deviceTag,
        "pinned" to pinned,
        "verifiedSolution" to verifiedSolution,
        "title" to title,
        "body" to body,
        "votes" to votes,
        "upvotedBy" to upvotedBy,
        "downvotedBy" to downvotedBy,
        "savedBy" to savedBy,
        "createdAtMs" to createdAtMs,
        "timeLabel" to timeLabel,
        "comments" to comments.map { it.toMap() },
        "published" to true
    )
}

@Keep
data class DeskRentalEntity(
    val id: String = "",
    val title: String = "",
    val category: String = "تعمیرات سخت‌افزار",
    val storeName: String = "",
    val province: String = "تهران",
    val city: String = "تهران",
    val region: String = "همه مناطق",
    val rentDaily: Long = 0L,
    val rentMonthly: Long = 0L,
    val cooperationModel: String = "",
    val equipment: String = "",
    val phone: String = "",
    val ownerId: String = "",
    val ownerName: String = "",
    val status: String = "pending",
    val desc: String = "",
    val isDemo: Boolean = false,
    val sortOrder: Long = System.currentTimeMillis()
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "category" to category,
        "storeName" to storeName,
        "province" to province,
        "city" to city,
        "region" to region,
        "rentDaily" to rentDaily,
        "rentMonthly" to rentMonthly,
        "cooperationModel" to cooperationModel,
        "equipment" to equipment,
        "phone" to phone,
        "ownerId" to ownerId,
        "ownerName" to ownerName,
        "status" to status,
        "desc" to desc,
        "sortOrder" to sortOrder
    )
}

@Keep
data class DeskRequestEntity(
    val id: String = "",
    val title: String = "",
    val category: String = "تعمیرات سخت‌افزار",
    val requesterName: String = "",
    val requesterPhone: String = "",
    val province: String = "تهران",
    val city: String = "تهران",
    val region: String = "همه مناطق",
    val proposedBudget: Long = 0L,
    val proposalText: String = "",
    val ownerId: String = "",
    val status: String = "open",
    val isDemo: Boolean = false,
    val sortOrder: Long = System.currentTimeMillis()
) {
    fun toCreateMap(): Map<String, Any?> = mapOf(
        "title" to title,
        "category" to category,
        "requesterName" to requesterName,
        "requesterPhone" to requesterPhone,
        "province" to province,
        "city" to city,
        "region" to region,
        "proposedBudget" to proposedBudget,
        "proposalText" to proposalText,
        "ownerId" to ownerId,
        "status" to status,
        "sortOrder" to sortOrder
    )
}

@Keep
data class BannerEntity(
    val id: String = "",
    val section: String = "mobile",
    val slotIndex: Int = 0,
    val province: String = "همه استان‌ها",
    val city: String = "همه شهرها",
    val region: String = "همه مناطق",
    val title: String = "",
    val hasImage: Boolean = false,
    val imageUrl: String = "",
    val linkType: String = "none",
    val linkTarget: String = "",
    val updatedAt: Long = 0L
)


