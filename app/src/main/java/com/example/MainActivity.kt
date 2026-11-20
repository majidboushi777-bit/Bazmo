package com.example

import android.Manifest
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.os.Build
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.FragmentActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.BazmoAuthManager
import com.example.data.BazmoBiometricHelper
import com.example.data.BazmoNotificationHelper
import com.example.data.BazmoRealtimeNotifService
import com.example.ui.components.BazmoCard
import com.example.ui.components.BazmoDangerButton
import com.example.ui.components.BazmoOutlineButton
import com.example.ui.components.BazmoPrimaryButton
import com.example.ui.screens.AdminShellScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.UserShellScreen
import com.example.ui.theme.AppBackground
import com.example.ui.theme.BazmoTheme
import com.example.ui.theme.BazmoThemeManager
import com.example.ui.theme.DangerRed
import com.example.ui.theme.HeaderBgDeep
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.TextMuted
import com.example.data.BazmoSessionUser
import com.example.viewmodel.AppRole
import com.example.viewmodel.BazmoModal
import com.example.viewmodel.BazmoViewModel
import kotlinx.coroutines.launch

class MainActivity : FragmentActivity() {
    private var openNotificationsPending = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        BazmoNotificationHelper.ensureChannel(this)
        if (intent?.getBooleanExtra(BazmoNotificationHelper.EXTRA_OPEN_NOTIFICATIONS, false) == true) {
            openNotificationsPending.value = true
        }
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(
                AndroidColor.TRANSPARENT,
                AndroidColor.TRANSPARENT
            )
        )
        setContent {
            val themeMode by BazmoThemeManager.modeFlow(this).collectAsStateWithLifecycle()
            val isDarkTheme = BazmoThemeManager.resolveDarkTheme(themeMode, isSystemInDarkTheme())
            LaunchedEffect(isDarkTheme) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.dark(AndroidColor.TRANSPARENT),
                    navigationBarStyle = if (isDarkTheme) {
                        SystemBarStyle.dark(AndroidColor.TRANSPARENT)
                    } else {
                        SystemBarStyle.light(
                            AndroidColor.TRANSPARENT,
                            AndroidColor.TRANSPARENT
                        )
                    }
                )
            }
            BazmoTheme(darkTheme = isDarkTheme) {
                BazmoAppRoot(
                    openNotificationsPending = openNotificationsPending.value,
                    onConsumeOpenNotifications = { openNotificationsPending.value = false }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        if (intent.getBooleanExtra(BazmoNotificationHelper.EXTRA_OPEN_NOTIFICATIONS, false)) {
            openNotificationsPending.value = true
        }
    }
}

@Composable
fun BazmoAppRoot(
    openNotificationsPending: Boolean = false,
    onConsumeOpenNotifications: () -> Unit = {}
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentUser by BazmoAuthManager.authStateFlow(context).collectAsStateWithLifecycle(
        initialValue = BazmoAuthManager.currentSession(context)
    )
    val savedBiometricAccount by BazmoAuthManager.savedAccountFlow(context).collectAsStateWithLifecycle(
        initialValue = BazmoAuthManager.savedAccount(context)
    )
    val canUseBiometric = remember(context) { BazmoBiometricHelper.canAuthenticate(context) }
    var isSigningIn by remember { mutableStateOf(false) }
    var authError by remember { mutableStateOf<String?>(null) }
    var prefillRegEmail by remember { mutableStateOf("") }
    var prefillRegName by remember { mutableStateOf("") }
    var openRegisterOnLogin by remember { mutableStateOf(false) }
    var allowAutoBiometricPrompt by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        BazmoAuthManager.attemptAutoSignIn(context)
    }

    val triggerBiometricUnlock: () -> Unit = {
        val account = BazmoAuthManager.savedAccount(context)
        val activity = context.findFragmentActivity()
        if (account != null && activity != null && !isSigningIn) {
            val label = listOf(account.displayName, account.email)
                .filter { it.isNotBlank() }
                .distinct()
                .joinToString(" • ")
            BazmoBiometricHelper.promptBiometricUnlock(
                activity = activity,
                accountLabel = label,
                onSuccess = {
                    authError = null
                    BazmoAuthManager.unlockSavedAccountWithBiometric(context)
                        .onFailure { err -> authError = err.message }
                },
                onError = { msg ->
                    if (msg.isNotBlank()) authError = msg
                }
            )
        }
    }

    val user = currentUser
    AnimatedContent(
        targetState = user,
        contentKey = { it?.uid },
        transitionSpec = {
            fadeIn(tween(190)) togetherWith fadeOut(tween(140))
        },
        label = "authRootTransition"
    ) { activeUser ->
        if (activeUser == null) {
            LoginScreen(
                isLoading = isSigningIn,
                errorMessage = authError,
                prefillEmail = prefillRegEmail,
                prefillName = prefillRegName,
                autoOpenRegister = openRegisterOnLogin,
                savedBiometricAccount = savedBiometricAccount,
                canUseBiometric = canUseBiometric,
                autoPromptBiometric = allowAutoBiometricPrompt,
                onBiometricSignIn = triggerBiometricUnlock,
                onForgetSavedAccount = {
                    authError = null
                    scope.launch {
                        BazmoAuthManager.signOut(context, forgetSavedAccount = true)
                    }
                },
                onConsumeAutoOpenRegister = { openRegisterOnLogin = false },
                onPasswordSignIn = { emailInput, passwordInput ->
                    if (!isSigningIn) {
                        isSigningIn = true
                        authError = null
                        scope.launch {
                            val res = BazmoAuthManager.signInWithPassword(context, emailInput, passwordInput)
                            isSigningIn = false
                            res.onFailure { err ->
                                authError = err.message
                            }
                        }
                    }
                },
                onGoogleSignIn = {
                    if (!isSigningIn) {
                        isSigningIn = true
                        authError = null
                        scope.launch {
                            val res = BazmoAuthManager.signInWithGoogle(context)
                            isSigningIn = false
                            res.onFailure { err ->
                                authError = err.message
                            }
                        }
                    }
                }
            )
        } else {
            key(activeUser.uid) {
                BazmoAuthenticatedRoot(
                    user = activeUser,
                    openNotificationsPending = openNotificationsPending,
                    onConsumeOpenNotifications = onConsumeOpenNotifications,
                    onManualSignOut = { allowAutoBiometricPrompt = false },
                    onOpenRegistrationForm = { email, name ->
                        allowAutoBiometricPrompt = false
                        prefillRegEmail = email
                        prefillRegName = name
                        openRegisterOnLogin = true
                    }
                )
            }
        }
    }
}

@Composable
private fun BazmoAuthenticatedRoot(
    user: BazmoSessionUser,
    openNotificationsPending: Boolean,
    onConsumeOpenNotifications: () -> Unit,
    onManualSignOut: () -> Unit,
    onOpenRegistrationForm: (email: String, name: String) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val viewModel: BazmoViewModel = viewModel(
        key = user.uid,
        factory = BazmoViewModel.factory(context.applicationContext, user)
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.onAppResumed()
            viewModel.showToast("سرویس اعلان‌های آنی بازمو فعال شد")
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, viewModel) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.onAppResumed()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(user.uid) {
        if (BazmoAuthManager.peekCachedAuthResponse(user.uid, user.token) == null) {
            BazmoAuthManager.refreshActiveSession(context)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            !BazmoNotificationHelper.hasNotificationPermission(context)
        ) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            BazmoRealtimeNotifService.startIfPermitted(context)
        }
    }

    LaunchedEffect(openNotificationsPending, uiState.isReady) {
        if (openNotificationsPending && uiState.isReady && !uiState.isNotRegistered && !uiState.isBlocked) {
            viewModel.switchRole(AppRole.USER)
            viewModel.openModal(BazmoModal.Notifications)
            onConsumeOpenNotifications()
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.toastMessages.collect { message ->
            snackbarHostState.currentSnackbarData?.dismiss()
            snackbarHostState.showSnackbar(
                message = message,
                duration = SnackbarDuration.Short
            )
        }
    }

    val onSignOut: () -> Unit = {
        onManualSignOut()
        scope.launch {
            BazmoRealtimeNotifService.stop(context)
            BazmoNotificationHelper.clearBackgroundConfig(context)
            BazmoNotificationHelper.cancelBackgroundCheck(context)
            BazmoAuthManager.signOut(context) {
                viewModel.detachListeners()
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) { data ->
                Snackbar(
                    shape = RoundedCornerShape(14.dp),
                    containerColor = HeaderBgDeep,
                    contentColor = Color.White
                ) {
                    Text(
                        text = data.visuals.message,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        },
        containerColor = Color.Transparent
    ) { _ ->
        val routeKey = when {
            !uiState.isReady -> "loading"
            uiState.isNotRegistered -> "not_registered"
            uiState.isBlocked -> "blocked"
            uiState.session.role == AppRole.ADMIN -> "admin"
            else -> "user"
        }
        AnimatedContent(
            targetState = routeKey,
            transitionSpec = {
                fadeIn(tween(180)) togetherWith fadeOut(tween(130))
            },
            label = "authenticatedRouteTransition"
        ) { targetRoute ->
            when (targetRoute) {
                "loading" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppBackground)
                            .navigationBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HeaderBgDeep)
                                .statusBarsPadding()
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                CircularProgressIndicator(color = Navy)
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "در حال همگام‌سازی با سرور بازمو...",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }

                "not_registered" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppBackground)
                            .navigationBarsPadding()
                            .testTag("not_registered_screen")
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HeaderBgDeep)
                                .statusBarsPadding()
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BazmoCard(modifier = Modifier.widthIn(max = 440.dp)) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "ایمیل شما توسط مدیریت بازمو ثبت نشده است",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = NavyDeep,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "ورود و استفاده از امکانات بازمو فقط برای همکاران تاییدشده امکان‌پذیر است. برای عضویت و احراز هویت می‌توانید درخواست خود را ثبت کنید.\nایمیل فعلی: ${uiState.authEmail}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                    BazmoPrimaryButton(
                                        text = "📝 ثبت درخواست عضویت و احراز هویت",
                                        onClick = {
                                            val em = uiState.authEmail
                                            val nm = uiState.authDisplayName
                                            scope.launch {
                                                BazmoRealtimeNotifService.stop(context)
                                                BazmoNotificationHelper.clearBackgroundConfig(context)
                                                BazmoNotificationHelper.cancelBackgroundCheck(context)
                                                BazmoAuthManager.signOut(context, forgetSavedAccount = true) {
                                                    viewModel.detachListeners()
                                                    onOpenRegistrationForm(em, nm)
                                                }
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("not_registered_register_button")
                                    )
                                    BazmoOutlineButton(
                                        text = "🔄 بررسی مجدد دسترسی",
                                        onClick = { viewModel.recheckInviteStatus() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("recheck_invite_button")
                                    )
                                    BazmoDangerButton(
                                        text = "خروج و تغییر حساب کاربری",
                                        onClick = onSignOut,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                "blocked" -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(AppBackground)
                            .navigationBarsPadding()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(HeaderBgDeep)
                                .statusBarsPadding()
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            BazmoCard(modifier = Modifier.widthIn(max = 420.dp)) {
                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Text(
                                        text = "حساب کاربری غیرفعال است",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black,
                                        color = DangerRed
                                    )
                                    Text(
                                        text = "حساب کاربری شما توسط مدیریت بازمو مسدود شده است. پس از فعال‌سازی مجدد توسط ادمین، می‌توانید وضعیت حساب را دوباره بررسی کنید.",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        textAlign = TextAlign.Center
                                    )
                                    BazmoOutlineButton(
                                        text = "🔄 بررسی مجدد وضعیت حساب",
                                        onClick = { viewModel.refreshOwnProfileStatus() },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("refresh_account_status_button")
                                    )
                                    BazmoDangerButton(
                                        text = "خروج از حساب کاربری",
                                        onClick = onSignOut,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }

                "admin" -> {
                    AdminShellScreen(
                        state = uiState,
                        viewModel = viewModel,
                        onSignOut = onSignOut
                    )
                }

                else -> {
                    UserShellScreen(
                        state = uiState,
                        viewModel = viewModel,
                        onSignOut = onSignOut
                    )
                }
            }
        }
    }
}

private tailrec fun Context.findFragmentActivity(): FragmentActivity? = when (this) {
    is FragmentActivity -> this
    is ContextWrapper -> baseContext.findFragmentActivity()
    else -> null
}
