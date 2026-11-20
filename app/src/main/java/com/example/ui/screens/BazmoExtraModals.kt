package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.BazmoLocations
import com.example.ui.components.BazmoDropdownSelector
import com.example.ui.components.BazmoInput
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoSoftButton
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardSurface
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.PrimaryFill
import com.example.ui.theme.SoftSurface
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.viewmodel.BazmoModal
import com.example.viewmodel.BazmoUiState
import com.example.viewmodel.BazmoViewModel

@Composable
internal fun NewDeskEntryModalBody(
    modal: BazmoModal.NewDeskEntry,
    state: BazmoUiState,
    viewModel: BazmoViewModel
) {
    val cats = listOf(
        "تعمیرات سخت‌افزار",
        "نرم‌افزار و فلش",
        "فروش موبایل",
        "لوازم جانبی"
    )
    var entryKind by rememberSaveable(modal.initialKind) {
        mutableStateOf(if (modal.initialKind == "request") "request" else "rental")
    }
    var title by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf(cats.first()) }
    var storeName by rememberSaveable { mutableStateOf(viewModel.myOwnedStores().firstOrNull()?.name ?: "") }
    var prov by rememberSaveable { mutableStateOf(state.session.province) }
    var city by rememberSaveable { mutableStateOf(state.session.city) }
    var region by rememberSaveable { mutableStateOf(state.session.region) }
    var rentDaily by rememberSaveable { mutableStateOf("") }
    var rentMonthly by rememberSaveable { mutableStateOf("") }
    var coopModel by rememberSaveable { mutableStateOf("اجاره ثابت ماهانه") }
    var equipment by rememberSaveable { mutableStateOf("") }
    var budget by rememberSaveable { mutableStateOf("") }
    var phone by rememberSaveable { mutableStateOf(state.session.currentUser?.phone ?: "") }
    var desc by rememberSaveable { mutableStateOf("") }

    val provs = BazmoLocations.provinces()
    val cities = BazmoLocations.citiesFor(prov)
    val regions = BazmoLocations.regionsFor(prov, city)

    ModalHeader("🪑 ثبت میز کار یا پیشنهاد همکاری", { viewModel.closeModal() })

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp)
    ) {
        if (entryKind == "rental") {
            BazmoPrimaryButton(
                text = "🪑 میز برای اجاره دارم",
                onClick = { entryKind = "rental" },
                compact = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("desk_modal_kind_rental")
            )
            BazmoSoftButton(
                text = "🤝 درخواست / پیشنهاد",
                onClick = { entryKind = "request" },
                compact = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("desk_modal_kind_request")
            )
        } else {
            BazmoSoftButton(
                text = "🪑 میز برای اجاره دارم",
                onClick = { entryKind = "rental" },
                compact = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("desk_modal_kind_rental")
            )
            BazmoPrimaryButton(
                text = "🤝 درخواست / پیشنهاد",
                onClick = { entryKind = "request" },
                compact = true,
                modifier = Modifier
                    .weight(1f)
                    .testTag("desk_modal_kind_request")
            )
        }
    }

    BazmoInput(
        value = title,
        onValueChange = { title = it },
        placeholder = if (entryKind == "rental") {
            "عنوان آگهی (مثلاً اجاره میز تعمیرکار حرفه‌ای)"
        } else {
            "عنوان درخواست (مثلاً نیازمند میز نرم‌افزار در پاساژ...)"
        },
        modifier = Modifier.padding(bottom = 8.dp)
    )
    BazmoDropdownSelector(
        label = if (entryKind == "rental") "دسته‌بندی تخصصی میز:" else "حوزه تخصص / فعالیت:",
        selectedValue = category,
        options = cats,
        onSelect = { category = it },
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (entryKind == "rental") {
        BazmoInput(storeName, { storeName = it }, "نام فروشگاه / مرکز خدمات", modifier = Modifier.padding(bottom = 8.dp))
    }

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
        modifier = Modifier.padding(bottom = 8.dp)
    )

    if (entryKind == "rental") {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(bottom = 8.dp)) {
            BazmoInput(rentDaily, { rentDaily = it }, "اجاره روزانه (تومان)", keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
            BazmoInput(rentMonthly, { rentMonthly = it }, "اجاره ماهانه (تومان)", keyboardType = KeyboardType.Number, modifier = Modifier.weight(1f))
        }
        BazmoInput(coopModel, { coopModel = it }, "مدل همکاری (اجاره ثابت، درصدی یا توافقی)", modifier = Modifier.padding(bottom = 8.dp))
        BazmoInput(equipment, { equipment = it }, "تجهیزات روی میز (لوپ، هیتر، باکس، ویترین...)", modifier = Modifier.padding(bottom = 8.dp))
    } else {
        BazmoInput(budget, { budget = it }, "سقف بودجه ماهانه (تومان - خالی برای توافقی/درصدی)", keyboardType = KeyboardType.Number, modifier = Modifier.padding(bottom = 8.dp))
    }

    BazmoInput(phone, { phone = it }, "شماره تماس هماهنگی", keyboardType = KeyboardType.Phone, modifier = Modifier.padding(bottom = 8.dp))
    BazmoInput(
        value = desc,
        onValueChange = { desc = it },
        placeholder = if (entryKind == "rental") {
            "شرایط و توضیحات کامل میز کار..."
        } else {
            "شرح پیشنهاد همکاری و تجربه کاری شما برای مغازه‌داران..."
        },
        singleLine = false,
        minLines = if (entryKind == "rental") 2 else 3,
        modifier = Modifier.padding(bottom = 12.dp)
    )
    BazmoPrimaryButton(
        text = if (entryKind == "rental") "✓ ثبت آگهی میز کار" else "✓ انتشار درخواست و پیشنهاد",
        onClick = {
            if (entryKind == "rental") {
                viewModel.submitDeskRental(
                    title = title,
                    category = category,
                    storeName = storeName,
                    province = prov,
                    city = city,
                    region = region,
                    rentDailyText = rentDaily,
                    rentMonthlyText = rentMonthly,
                    cooperationModel = coopModel,
                    equipment = equipment,
                    phone = phone,
                    desc = desc
                )
            } else {
                viewModel.submitDeskRequest(
                    title = title,
                    category = category,
                    province = prov,
                    city = city,
                    region = region,
                    budgetText = budget,
                    phone = phone,
                    proposalText = desc
                )
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("submit_desk_entry_button")
    )
}

@Composable
internal fun GenericChatModalBody(
    modal: BazmoModal.GenericChat,
    state: BazmoUiState,
    viewModel: BazmoViewModel
) {
    val threads = state.chatThreadsForAd(modal.adId)
    val isOwner = modal.sellerUid == state.currentUid
    var selectedThreadId by rememberSaveable(modal.adId) {
        mutableStateOf(
            if (isOwner) threads.firstOrNull()?.id
            else threads.find { it.buyerUid == state.currentUid }?.id
        )
    }
    val activeThread = if (isOwner) {
        threads.find { it.id == selectedThreadId } ?: threads.firstOrNull()
    } else {
        threads.find { it.buyerUid == state.currentUid }
    }
    val msgs = activeThread?.messages.orEmpty()
    var chatInput by rememberSaveable(modal.adId) { mutableStateOf("") }

    ModalHeader("💬 گفتگو: ${modal.partnerName.ifBlank { modal.title }}", { viewModel.closeModal() })
    if (isOwner && threads.size > 1) {
        BazmoDropdownSelector(
            label = "انتخاب مخاطب:",
            selectedValue = activeThread?.messages?.firstOrNull()?.senderName ?: "همکار",
            options = threads.mapIndexed { idx, th ->
                th.messages.firstOrNull()?.senderName?.ifBlank { "همکار ${idx + 1}" } ?: "همکار ${idx + 1}"
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
                    viewModel.sendGenericChatMessage(
                        adId = modal.adId,
                        sellerUid = modal.sellerUid,
                        targetThreadId = activeThread?.id,
                        text = chatInput
                    )
                    chatInput = ""
                }
            },
            compact = true
        )
    }
}

@Composable
internal fun MyStoreAddMobileModalBody(
    modal: BazmoModal.MyStoreAddMobile,
    state: BazmoUiState,
    viewModel: BazmoViewModel
) {
    val newModelLabel = "+ ثبت مدل گوشی جدید"
    val modelChoices = state.db.mobiles.map { it.id to "${it.brand} ${it.model}".trim() }
    var selectedOption by rememberSaveable(modal.storeId) {
        mutableStateOf(modelChoices.firstOrNull()?.second ?: newModelLabel)
    }
    var newBrand by rememberSaveable(modal.storeId) { mutableStateOf("") }
    var newModel by rememberSaveable(modal.storeId) { mutableStateOf("") }
    var newSpecs by rememberSaveable(modal.storeId) { mutableStateOf("") }
    var priceText by rememberSaveable(modal.storeId) { mutableStateOf("") }
    var infoText by rememberSaveable(modal.storeId) { mutableStateOf("۱۸ ماه گارانتی شرکتی + رجیسترشده") }

    val isCreatingNew = selectedOption == newModelLabel || modelChoices.isEmpty()

    ModalHeader("📱 افزودن گوشی به فروشگاه", { viewModel.closeModal() })
    BazmoDropdownSelector(
        label = "انتخاب مدل از کاتالوگ یا ثبت مدل جدید:",
        selectedValue = selectedOption,
        options = modelChoices.map { it.second } + newModelLabel,
        onSelect = { selectedOption = it },
        modifier = Modifier.padding(bottom = 8.dp)
    )
    if (isCreatingNew) {
        BazmoInput(newBrand, { newBrand = it }, "برند گوشی (مثلاً Samsung یا Apple)", modifier = Modifier.padding(bottom = 8.dp))
        BazmoInput(newModel, { newModel = it }, "نام مدل (مثلاً Galaxy S24 Ultra)", modifier = Modifier.padding(bottom = 8.dp))
        BazmoInput(newSpecs, { newSpecs = it }, "مشخصات (مثلاً 256GB / پک ویتنام)", modifier = Modifier.padding(bottom = 8.dp))
    }
    BazmoInput(priceText, { priceText = it }, "قیمت فروش شما (تومان)", keyboardType = KeyboardType.Number, modifier = Modifier.padding(bottom = 8.dp))
    BazmoInput(infoText, { infoText = it }, "شرایط گارانتی و رجیستری", modifier = Modifier.padding(bottom = 12.dp))
    BazmoPrimaryButton(
        text = "✓ ثبت در لیست محصولات فروشگاه",
        onClick = {
            val matchedId = if (isCreatingNew) "" else modelChoices.find { it.second == selectedOption }?.first.orEmpty()
            viewModel.addStoreMobileOffer(
                storeId = modal.storeId,
                mobileId = matchedId,
                priceText = priceText,
                info = infoText,
                newBrand = newBrand,
                newModel = newModel,
                newSpecs = newSpecs
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mystore_submit_mobile_button")
    )
}

@Composable
internal fun MyStoreAddAccessoryModalBody(
    modal: BazmoModal.MyStoreAddAccessory,
    viewModel: BazmoViewModel
) {
    val cats = listOf("کابل و آداپتور", "قاب و گارد", "گلس و محافظ", "هندزفری و ساعت", "پاوربانک")
    var selectedCat by rememberSaveable(modal.storeId) { mutableStateOf(cats.first()) }
    var name by rememberSaveable(modal.storeId) { mutableStateOf("") }
    var priceText by rememberSaveable(modal.storeId) { mutableStateOf("") }
    var desc by rememberSaveable(modal.storeId) { mutableStateOf("") }

    ModalHeader("🎧 افزودن کالای جانبی به فروشگاه", { viewModel.closeModal() })
    BazmoDropdownSelector(
        label = "دسته‌بندی:",
        selectedValue = selectedCat,
        options = cats,
        onSelect = { selectedCat = it },
        modifier = Modifier.padding(bottom = 8.dp)
    )
    BazmoInput(name, { name = it }, "نام محصول (مثلاً شارژر ۲۰ وات اصلی)", modifier = Modifier.padding(bottom = 8.dp))
    BazmoInput(priceText, { priceText = it }, "قیمت عمده / فروشگاهی (تومان)", keyboardType = KeyboardType.Number, modifier = Modifier.padding(bottom = 8.dp))
    BazmoInput(desc, { desc = it }, "توضیحات و حداقل تعداد سفارش...", singleLine = false, minLines = 2, modifier = Modifier.padding(bottom = 12.dp))
    BazmoPrimaryButton(
        text = "✓ ثبت کالای جانبی",
        onClick = {
            viewModel.addStoreAccessory(
                storeId = modal.storeId,
                cat = selectedCat,
                name = name,
                priceText = priceText,
                desc = desc
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .testTag("mystore_submit_acc_button")
    )
}
