package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.BazmoLocations
import com.example.data.BazmoRepository
import com.example.data.BazmoRoles
import com.example.data.BazmoSessionUser
import com.example.data.RegistrationRequestEntity
import com.example.data.isValidEmailFormat
import com.example.data.normalizeEmail
import com.example.ui.components.BazmoDropdownSelector
import com.example.ui.components.BazmoInput
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.components.BazmoSoftButton
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BazmoThemeManager
import com.example.ui.theme.BorderLight
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
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessSoft
import com.example.ui.theme.SuccessText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    isLoading: Boolean,
    errorMessage: String?,
    prefillEmail: String = "",
    prefillName: String = "",
    autoOpenRegister: Boolean = false,
    savedBiometricAccount: BazmoSessionUser? = null,
    canUseBiometric: Boolean = false,
    onBiometricSignIn: () -> Unit = {},
    onConsumeAutoOpenRegister: () -> Unit = {},
    onPasswordSignIn: (email: String, password: String) -> Unit = { _, _ -> },
    onGoogleSignIn: () -> Unit = {}
) {
    var showRegisterDialog by rememberSaveable { mutableStateOf(false) }
    var regSubmittedNotice by rememberSaveable { mutableStateOf<String?>(null) }
    var loginEmail by rememberSaveable { mutableStateOf(prefillEmail) }
    var loginPassword by rememberSaveable { mutableStateOf("") }
    var passwordVisible by rememberSaveable { mutableStateOf(false) }
    var hasAutoPromptedBiometric by rememberSaveable(savedBiometricAccount?.uid) {
        mutableStateOf(false)
    }

    LaunchedEffect(savedBiometricAccount?.uid, canUseBiometric) {
        if (savedBiometricAccount != null && canUseBiometric && !hasAutoPromptedBiometric && !isLoading) {
            hasAutoPromptedBiometric = true
            onBiometricSignIn()
        }
    }

    LaunchedEffect(autoOpenRegister) {
        if (autoOpenRegister) {
            showRegisterDialog = true
            onConsumeAutoOpenRegister()
        }
    }

    val context = LocalContext.current
    val isDark = LocalBazmoColors.current.isDark

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppBackground)
    ) {
        // Curved deep-navy top backdrop with subtle Apple liquid-glass ambient highlights
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
                .clip(RoundedCornerShape(bottomStart = 38.dp, bottomEnd = 38.dp))
                .background(
                    Brush.verticalGradient(
                        listOf(HeaderBgDeep, HeaderBgMid, HeaderBgLight)
                    )
                )
        ) {
            Box(
                modifier = Modifier
                    .size(210.dp)
                    .align(Alignment.TopEnd)
                    .offset(x = 55.dp, y = (-30).dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFF38BDF8).copy(alpha = 0.28f), Color.Transparent)
                        )
                    )
            )
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .align(Alignment.BottomStart)
                    .offset(x = (-40).dp, y = 30.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFF59E0B).copy(alpha = 0.18f), Color.Transparent)
                        )
                    )
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Top
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 460.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Surface(
                        color = Color.White.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.28f)),
                        modifier = Modifier
                            .minimumInteractiveComponentSize()
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { BazmoThemeManager.toggleDark(context, isDark) }
                            .testTag("theme_toggle_button")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = if (isDark) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                                contentDescription = if (isDark) "تغییر به حالت روشن" else "تغییر به حالت تاریک",
                                tint = GoldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isDark) "حالت روشن" else "حالت تاریک",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color.White.copy(alpha = 0.14f),
                    border = BorderStroke(2.dp, Color.White.copy(alpha = 0.35f)),
                    shadowElevation = 10.dp,
                    modifier = Modifier.size(80.dp)
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_bazmo_foreground_1794874267318),
                        contentDescription = "لوگوی بازمو",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(22.dp))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "بازمو",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White
                )
                Text(
                    text = "مرجع تخصصی صنف موبایل ایران",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFD8E9FF),
                    modifier = Modifier.padding(top = 4.dp)
                )

                Spacer(modifier = Modifier.height(22.dp))

                Surface(
                    shape = RoundedCornerShape(28.dp),
                    color = if (isDark) CardSurface.copy(alpha = 0.92f) else Color.White.copy(alpha = 0.84f),
                    border = BorderStroke(
                        1.4.dp,
                        Brush.linearGradient(
                            if (isDark) {
                                listOf(
                                    Color(0xFF38BDF8).copy(alpha = 0.42f),
                                    BorderLight,
                                    Color(0xFF60A5FA).copy(alpha = 0.32f)
                                )
                            } else {
                                listOf(
                                    Color.White.copy(alpha = 0.98f),
                                    Color(0xFFBAE6FD).copy(alpha = 0.72f),
                                    Color.White.copy(alpha = 0.88f)
                                )
                            }
                        )
                    ),
                    shadowElevation = 14.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier.background(
                            Brush.verticalGradient(
                                if (isDark) {
                                    listOf(
                                        Color(0xFF132744).copy(alpha = 0.96f),
                                        Color(0xFF0F203A).copy(alpha = 0.92f),
                                        Color(0xFF1B2B45).copy(alpha = 0.88f)
                                    )
                                } else {
                                    listOf(
                                        Color(0xFFF0F9FF).copy(alpha = 0.94f),
                                        Color(0xFFE0F2FE).copy(alpha = 0.78f),
                                        Color(0xFFFEF3C7).copy(alpha = 0.42f)
                                    )
                                }
                            )
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(22.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                        Text(
                            text = "ورود به شبکه سراسری بازمو",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = NavyDeep,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "ورود به شبکه صنفی بازمو فقط برای همکاران تاییدشده و ثبت‌نام‌شده توسط مدیریت امکان‌پذیر است.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextMuted,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                        )

                        if (!regSubmittedNotice.isNullOrBlank()) {
                            Surface(
                                color = SuccessSoft,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, SuccessGreen.copy(alpha = 0.45f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            ) {
                                Text(
                                    text = regSubmittedNotice.orEmpty(),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SuccessText,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(11.dp)
                                )
                            }
                        }

                        if (!errorMessage.isNullOrBlank()) {
                            Surface(
                                color = DangerRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.35f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            ) {
                                Text(
                                    text = errorMessage,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = DangerRed,
                                    fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    maxLines = 6,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        if (savedBiometricAccount != null) {
                            AppleGlassBiometricCard(
                                account = savedBiometricAccount,
                                canUseBiometric = canUseBiometric,
                                enabled = !isLoading,
                                onUnlockClick = onBiometricSignIn,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 14.dp)
                            )
                        }

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            AppleGlassTextField(
                                value = loginEmail,
                                onValueChange = { loginEmail = it },
                                label = "نام کاربری (ایمیل ثبت‌شده)",
                                placeholder = "name@example.com",
                                enabled = !isLoading,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                fieldModifier = Modifier.testTag("login_email_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            AppleGlassTextField(
                                value = loginPassword,
                                onValueChange = { loginPassword = it },
                                label = "رمز عبور",
                                placeholder = "••••••••",
                                enabled = !isLoading,
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                trailingIcon = {
                                    IconButton(
                                        onClick = { passwordVisible = !passwordVisible },
                                        modifier = Modifier.minimumInteractiveComponentSize()
                                    ) {
                                        Icon(
                                            imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                            contentDescription = if (passwordVisible) "پنهان کردن رمز عبور" else "نمایش رمز عبور",
                                            tint = Navy.copy(alpha = 0.72f)
                                        )
                                    }
                                },
                                fieldModifier = Modifier.testTag("login_password_input")
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        AppleGlassLoginButton(
                            isLoading = isLoading,
                            enabled = !isLoading && loginEmail.isNotBlank() && loginPassword.isNotBlank(),
                            onClick = { onPasswordSignIn(loginEmail, loginPassword) },
                            modifier = Modifier.testTag("password_signin_button")
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        OutlinedButton(
                            onClick = { showRegisterDialog = true },
                            enabled = !isLoading,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.5.dp, GoldAccent),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = GoldSoft,
                                contentColor = GoldText
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 50.dp)
                                .testTag("open_registration_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.PersonAdd,
                                contentDescription = null,
                                tint = GoldText,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "عضویت و درخواست احراز هویت",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        }
                    }
                }
            }
        }
    }

    if (showRegisterDialog) {
        RegistrationRequestDialog(
            initialEmail = prefillEmail,
            initialName = prefillName,
            onDismiss = { showRegisterDialog = false },
            onSuccess = { fullName ->
                showRegisterDialog = false
                regSubmittedNotice = "درخواست عضویت «$fullName» ثبت شد. تایید شما فقط با احراز هویت ممکن است و همکاران ما به‌زودی با شما تماس خواهند گرفت."
            }
        )
    }
}

@Composable
private fun RegistrationRequestDialog(
    initialEmail: String,
    initialName: String,
    onDismiss: () -> Unit,
    onSuccess: (String) -> Unit
) {
    val scope = rememberCoroutineScope()

    var fullName by rememberSaveable { mutableStateOf(initialName) }
    var email by rememberSaveable { mutableStateOf(initialEmail) }
    var phone by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var requestedTier by rememberSaveable { mutableStateOf(BazmoRoles.REPAIR_TECH) }
    var prov by rememberSaveable { mutableStateOf("تهران") }
    var city by rememberSaveable { mutableStateOf("تهران") }
    var region by rememberSaveable { mutableStateOf("علاءالدین و جمهوری") }
    var note by rememberSaveable { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var formError by remember { mutableStateOf<String?>(null) }

    val provs = BazmoLocations.provinces()
    val cities = BazmoLocations.citiesFor(prov)
    val regions = BazmoLocations.regionsFor(prov, city)

    Dialog(
        onDismissRequest = { if (!isSubmitting) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = CardSurface,
            shadowElevation = 16.dp,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp)
                .widthIn(max = 480.dp)
        ) {
            Column(
                modifier = Modifier
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "📝 فرم درخواست عضویت در بازمو",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Black,
                        color = NavyDeep,
                        modifier = Modifier.weight(1f)
                    )
                    BazmoSoftButton(
                        text = "بستن",
                        onClick = { if (!isSubmitting) onDismiss() },
                        compact = true
                    )
                }

                HorizontalDivider(color = BorderLight, modifier = Modifier.padding(vertical = 10.dp))

                // Prominent verification notice required by user
                Surface(
                    color = GoldSoft,
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(2.dp, GoldAccent),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                        .testTag("registration_verification_notice")
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "⚠️ اطلاعیه مهم احراز هویت",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Black,
                            color = GoldText,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "تایید شما فقط با احراز هویت ممکن است که به‌زودی همکاران ما جهت اطلاع از نحوه احراز با شما تماس خواهند گرفت.",
                            fontSize = 15.sp,
                            lineHeight = 23.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                if (!formError.isNullOrBlank()) {
                    Surface(
                        color = DangerRed.copy(alpha = 0.1f),
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.4f)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 10.dp)
                    ) {
                        Text(
                            text = formError.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = DangerRed,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                BazmoInput(
                    value = fullName,
                    onValueChange = { fullName = it },
                    placeholder = "نام و نام خانوادگی",
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("reg_fullname_input")
                )
                BazmoInput(
                    value = email,
                    onValueChange = { email = it },
                    placeholder = "ایمیل معتبر (نام کاربری جهت ورود)",
                    keyboardType = KeyboardType.Email,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("reg_email_input")
                )
                BazmoInput(
                    value = phone,
                    onValueChange = { phone = it },
                    placeholder = "شماره تماس جهت هماهنگی احراز هویت",
                    keyboardType = KeyboardType.Phone,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("reg_phone_input")
                )
                BazmoInput(
                    value = password,
                    onValueChange = { password = it },
                    placeholder = "رمز عبور دلخواه جهت ورود (حداقل ۶ کاراکتر)",
                    keyboardType = KeyboardType.Password,
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("reg_password_input")
                )
                BazmoDropdownSelector(
                    label = "نقش صنفی درخواستی:",
                    selectedValue = requestedTier,
                    options = BazmoRoles.ALL_ROLES,
                    onSelect = { requestedTier = it },
                    modifier = Modifier
                        .padding(bottom = 8.dp)
                        .testTag("reg_role_dropdown")
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    BazmoDropdownSelector(
                        label = "استان:",
                        selectedValue = prov,
                        options = provs,
                        onSelect = { newP ->
                            prov = newP
                            val firstCity = BazmoLocations.citiesFor(newP).firstOrNull() ?: ""
                            city = firstCity
                            region = BazmoLocations.regionsFor(newP, firstCity).firstOrNull() ?: "همه مناطق"
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
                BazmoInput(
                    value = note,
                    onValueChange = { note = it },
                    placeholder = "نام فروشگاه / تعمیرگاه یا توضیحات (اختیاری)",
                    singleLine = false,
                    minLines = 2,
                    modifier = Modifier.padding(bottom = 14.dp)
                )

                BazmoPrimaryButton(
                    text = if (isSubmitting) "در حال ارسال درخواست..." else "✓ ارسال درخواست عضویت",
                    enabled = !isSubmitting,
                    onClick = {
                        val cleanName = fullName.trim().take(120)
                        val cleanEmail = normalizeEmail(email).take(200)
                        val cleanPhone = phone.trim().take(40)
                        when {
                            cleanName.isEmpty() -> formError = "لطفاً نام و نام خانوادگی خود را وارد کنید"
                            !isValidEmailFormat(cleanEmail) -> formError = "لطفاً یک ایمیل معتبر وارد کنید"
                            cleanPhone.length < 5 -> formError = "شماره تماس معتبر برای احراز هویت الزامی است"
                            password.length < 6 -> formError = "رمز عبور انتخابی باید حداقل ۶ کاراکتر باشد"
                            else -> {
                                formError = null
                                isSubmitting = true
                                scope.launch {
                                    val req = RegistrationRequestEntity(
                                        email = cleanEmail,
                                        fullName = cleanName,
                                        phone = cleanPhone,
                                        requestedTier = requestedTier,
                                        province = prov,
                                        city = city,
                                        region = region,
                                        note = note.trim().take(400),
                                        status = "pending"
                                    )
                                    BazmoRepository.submitRegistrationRequest(req, password)
                                        .onSuccess {
                                            isSubmitting = false
                                            onSuccess(cleanName)
                                        }
                                        .onFailure { err ->
                                            isSubmitting = false
                                            formError = err.message ?: "خطا در ارسال درخواست. لطفاً دوباره تلاش کنید."
                                        }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("submit_registration_button")
                )
            }
        }
    }
}

@Composable
private fun AppleGlassTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String = "",
    enabled: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    trailingIcon: (@Composable () -> Unit)? = null,
    fieldModifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(18.dp)
    val isDark = LocalBazmoColors.current.isDark
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = shape,
                ambientColor = Navy.copy(alpha = 0.14f),
                spotColor = Navy.copy(alpha = 0.16f)
            )
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    if (isDark) {
                        listOf(
                            Color(0xFF1A3152).copy(alpha = 0.94f),
                            Color(0xFF132540).copy(alpha = 0.90f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.92f),
                            Color(0xFFEAF2FC).copy(alpha = 0.78f)
                        )
                    }
                )
            )
            .border(
                width = 1.25.dp,
                brush = Brush.verticalGradient(
                    if (isDark) {
                        listOf(
                            Color(0xFF60A5FA).copy(alpha = 0.45f),
                            BorderLight
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.98f),
                            Color(0xFF93C5FD).copy(alpha = 0.50f)
                        )
                    }
                ),
                shape = shape
            )
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label, color = Navy.copy(alpha = 0.78f)) },
            placeholder = if (placeholder.isNotBlank()) {
                { Text(placeholder, color = TextMuted.copy(alpha = 0.75f)) }
            } else null,
            singleLine = true,
            enabled = enabled,
            visualTransformation = visualTransformation,
            keyboardOptions = keyboardOptions,
            trailingIcon = trailingIcon,
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                cursorColor = Navy
            ),
            modifier = fieldModifier.fillMaxWidth()
        )
    }
}

@Composable
private fun AppleGlassLoginButton(
    isLoading: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(999.dp)
    val isDark = LocalBazmoColors.current.isDark
    val isInteractive = enabled || isLoading
    val glassFill = when {
        isDark && isInteractive -> Brush.verticalGradient(
            listOf(
                Color(0xFF2563EB),
                Color(0xFF1D4ED8),
                Color(0xFF1E40AF)
            )
        )
        isDark -> Brush.verticalGradient(
            listOf(
                Color(0xFF1E3250),
                Color(0xFF172740)
            )
        )
        isInteractive -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.68f),
                Color(0xFFDBEAFE).copy(alpha = 0.48f),
                Color(0xFFBFDBFE).copy(alpha = 0.36f)
            )
        )
        else -> Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.44f),
                Color(0xFFE2E8F0).copy(alpha = 0.36f)
            )
        )
    }
    val contentTint = when {
        isDark && isInteractive -> Color.White
        isDark -> DarkTextMutedColor()
        isInteractive -> Color(0xFF0B254B)
        else -> Color(0xFF64748B)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .heightIn(min = 54.dp)
            .shadow(
                elevation = if (enabled) 10.dp else 2.dp,
                shape = shape,
                ambientColor = Color(0xFF1E40AF).copy(alpha = 0.22f),
                spotColor = Color(0xFF2563EB).copy(alpha = 0.24f)
            )
            .clip(shape)
            .background(glassFill)
            .border(
                width = 1.4.dp,
                brush = Brush.verticalGradient(
                    if (isDark) {
                        listOf(
                            Color(0xFF93C5FD).copy(alpha = 0.55f),
                            Color(0xFF3B82F6).copy(alpha = 0.45f)
                        )
                    } else {
                        listOf(
                            Color.White.copy(alpha = 0.96f),
                            Color(0xFF93C5FD).copy(alpha = 0.55f),
                            Color.White.copy(alpha = 0.72f)
                        )
                    }
                ),
                shape = shape
            )
            .clickable(enabled = enabled, onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        // Upper Apple Liquid Glass specular reflection rim
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(26.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = if (isDark) 0.18f else 0.62f),
                            Color.Transparent
                        )
                    )
                )
        )

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 14.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = contentTint,
                    strokeWidth = 2.5.dp,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "در حال احراز هویت...",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentTint
                )
            } else {
                Icon(
                    imageVector = Icons.Filled.Lock,
                    contentDescription = null,
                    tint = contentTint,
                    modifier = Modifier.size(19.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ورود با نام کاربری و رمز عبور",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = contentTint
                )
            }
        }
    }
}

@Composable
private fun AppleGlassBiometricCard(
    account: BazmoSessionUser,
    canUseBiometric: Boolean,
    enabled: Boolean,
    onUnlockClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val shape = RoundedCornerShape(22.dp)
    val isDark = LocalBazmoColors.current.isDark
    val labelName = account.displayName.ifBlank { account.email }
    Surface(
        shape = shape,
        color = if (isDark) CardSurface else Color.White.copy(alpha = 0.62f),
        border = BorderStroke(
            1.4.dp,
            Brush.linearGradient(
                if (isDark) {
                    listOf(
                        Color(0xFF38BDF8).copy(alpha = 0.55f),
                        BorderLight
                    )
                } else {
                    listOf(
                        Color.White.copy(alpha = 0.96f),
                        Color(0xFF38BDF8).copy(alpha = 0.55f),
                        Color.White.copy(alpha = 0.8f)
                    )
                }
            )
        ),
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .background(
                    Brush.verticalGradient(
                        if (isDark) {
                            listOf(
                                Color(0xFF1A3152),
                                Color(0xFF12233B)
                            )
                        } else {
                            listOf(
                                Color.White.copy(alpha = 0.72f),
                                Color(0xFFE0F2FE).copy(alpha = 0.52f)
                            )
                        }
                    )
                )
                .padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "آخرین حساب فعال: $labelName",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.ExtraBold,
                color = NavyDeep,
                textAlign = TextAlign.Center
            )
            if (account.email.isNotBlank() && account.email != labelName) {
                Text(
                    text = account.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            if (canUseBiometric) {
                val pillShape = RoundedCornerShape(999.dp)
                val pillTint = if (isDark) Color.White else Color(0xFF0B254B)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .heightIn(min = 50.dp)
                        .shadow(
                            elevation = if (enabled) 8.dp else 2.dp,
                            shape = pillShape,
                            ambientColor = Color(0xFF0284C7).copy(alpha = 0.24f)
                        )
                        .clip(pillShape)
                        .background(
                            Brush.verticalGradient(
                                if (isDark) {
                                    listOf(
                                        Color(0xFF0284C7),
                                        Color(0xFF0369A1)
                                    )
                                } else {
                                    listOf(
                                        Color(0xFFFFFFFF).copy(alpha = 0.88f),
                                        Color(0xFFBAE6FD).copy(alpha = 0.68f),
                                        Color(0xFF7DD3FC).copy(alpha = 0.48f)
                                    )
                                }
                            )
                        )
                        .border(
                            1.4.dp,
                            if (isDark) Color(0xFF38BDF8).copy(alpha = 0.7f) else Color.White.copy(alpha = 0.95f),
                            pillShape
                        )
                        .clickable(enabled = enabled, onClick = onUnlockClick)
                        .testTag("biometric_signin_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Fingerprint,
                            contentDescription = "ورود با اثر انگشت",
                            tint = pillTint,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "ورود سریع با اثر انگشت",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Black,
                            color = pillTint
                        )
                    }
                }
                Text(
                    text = "تا زمان خروج از حساب، ورود بعدی با اثر انگشت انجام می‌شود.",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            } else {
                Text(
                    text = "حسگر اثر انگشت یا قفل صفحه روی دستگاه فعال نیست؛ می‌توانید از رمز عبور زیر وارد شوید.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun DarkTextMutedColor(): Color = TextMuted



