package com.example.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.IncomeGreen
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.BiometricStatus
import com.example.util.LockType
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun AppLockScreen(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()

    val userName by viewModel.userName.collectAsState()
    val userAvatarEmoji by viewModel.userAvatarEmoji.collectAsState()
    val userAvatarColor by viewModel.userAvatarColor.collectAsState()

    val lockType by viewModel.lockType.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val biometricStatus by viewModel.biometricStatus.collectAsState()

    var inputCode by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var failedAttempts by remember { mutableIntStateOf(0) }
    var isPasswordVisible by remember { mutableStateOf(false) }

    val shakeOffset = remember { Animatable(0f) }

    fun triggerShake() {
        scope.launch {
            shakeOffset.snapTo(0f)
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = keyframes {
                    durationMillis = 400
                    0f at 0
                    -20f at 50
                    20f at 100
                    -15f at 150
                    15f at 200
                    -8f at 250
                    8f at 300
                    0f at 400
                }
            )
        }
    }

    fun handleUnlockAttempt(code: String) {
        if (viewModel.verifyPasscode(code)) {
            errorMessage = null
            viewModel.unlockApp()
        } else {
            failedAttempts++
            errorMessage = "رمز عبور اشتباه است"
            triggerShake()
            inputCode = ""
        }
    }

    fun launchBiometricPrompt() {
        if (activity != null && biometricStatus == BiometricStatus.AVAILABLE) {
            viewModel.securityManager.authenticateWithBiometrics(
                activity = activity,
                title = "ورود با اثر انگشت",
                subtitle = "برای دسترسی به حسابداری، حسگر اثر انگشت را لمس کنید",
                negativeButtonText = "ورود با پین‌کد",
                onSuccess = {
                    viewModel.unlockApp()
                },
                onError = { code, err ->
                    if (code != BiometricPrompt.ERROR_USER_CANCELED && code != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        errorMessage = err
                    }
                },
                onFailed = {
                    errorMessage = "اثر انگشت شناسایی نشد، دوباره تلاش کنید"
                }
            )
        }
    }

    // Automatically prompt biometrics on screen open if enabled
    LaunchedEffect(Unit) {
        if (isBiometricEnabled && biometricStatus == BiometricStatus.AVAILABLE && activity != null) {
            launchBiometricPrompt()
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BackgroundCanvas)
                .testTag("app_lock_screen"),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp)
                    .widthIn(max = 420.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header: Avatar & App Branding
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(userAvatarColor).copy(alpha = 0.15f))
                            .border(2.dp, Color(userAvatarColor).copy(alpha = 0.4f), CircleShape)
                            .shadow(8.dp, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = userAvatarEmoji.ifBlank { "👤" },
                            fontSize = 38.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = userName.ifBlank { "حسابداری شخصی" },
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = BentoNavyDark
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = BentoIndigoAccent,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = "ورود امن به برنامه",
                            fontSize = 12.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Middle: Pin indicator or Password field
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(x = shakeOffset.value.roundToInt(), y = 0) }
                ) {
                    if (lockType == LockType.PASSWORD) {
                        // Password Text Input
                        OutlinedTextField(
                            value = inputCode,
                            onValueChange = {
                                inputCode = it
                                errorMessage = null
                            },
                            placeholder = { Text("رمز عبور خود را وارد کنید", fontSize = 13.sp) },
                            singleLine = true,
                            visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Password,
                                imeAction = ImeAction.Done
                            ),
                            keyboardActions = KeyboardActions(
                                onDone = {
                                    if (inputCode.isNotBlank()) {
                                        handleUnlockAttempt(inputCode)
                                    }
                                }
                            ),
                            trailingIcon = {
                                IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                        contentDescription = "نمایش رمز",
                                        tint = TextSecondary
                                    )
                                }
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = BentoIndigoAccent,
                                unfocusedBorderColor = BentoBorder,
                                focusedContainerColor = SurfaceWhite,
                                unfocusedContainerColor = SurfaceWhite
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("lock_password_input")
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { handleUnlockAttempt(inputCode) },
                            enabled = inputCode.isNotBlank(),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("lock_submit_password_btn")
                        ) {
                            Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("ورود به برنامه", fontWeight = FontWeight.Bold)
                        }
                    } else {
                        // PIN Dots Indicator
                        val targetLen = lockType.length
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 12.dp)
                        ) {
                            repeat(targetLen) { index ->
                                val isFilled = index < inputCode.length
                                Box(
                                    modifier = Modifier
                                        .size(16.dp)
                                        .clip(CircleShape)
                                        .background(if (isFilled) BentoIndigoAccent else Color.Transparent)
                                        .border(
                                            width = 2.dp,
                                            color = if (isFilled) BentoIndigoAccent else BentoBorder,
                                            shape = CircleShape
                                        )
                                )
                            }
                        }
                    }

                    // Error message banner
                    AnimatedVisibility(
                        visible = errorMessage != null,
                        enter = fadeIn(),
                        exit = fadeOut()
                    ) {
                        Text(
                            text = errorMessage ?: "",
                            color = ExpenseRed,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    if (failedAttempts >= 3) {
                        Text(
                            text = "تلاش‌های ناموفق: $failedAttempts",
                            color = TextSecondary,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }

                // Bottom: Numeric Keypad (for PIN mode) or Biometric shortcut
                if (lockType != LockType.PASSWORD) {
                    val targetLen = lockType.length

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val keypadRows = listOf(
                            listOf("3", "2", "1"),
                            listOf("6", "5", "4"),
                            listOf("9", "8", "7"),
                            listOf("BACK", "0", "BIO")
                        )

                        keypadRows.forEach { rowKeys ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                rowKeys.forEach { key ->
                                    val isBio = key == "BIO"
                                    val isBack = key == "BACK"

                                    Surface(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(CircleShape)
                                            .clickable(
                                                interactionSource = remember { MutableInteractionSource() },
                                                indication = ripple(bounded = true, color = BentoIndigoAccent)
                                            ) {
                                                when {
                                                    isBio -> {
                                                        launchBiometricPrompt()
                                                    }
                                                    isBack -> {
                                                        if (inputCode.isNotEmpty()) {
                                                            inputCode = inputCode.dropLast(1)
                                                            errorMessage = null
                                                        }
                                                    }
                                                    else -> {
                                                        if (inputCode.length < targetLen) {
                                                            val next = inputCode + key
                                                            inputCode = next
                                                            errorMessage = null
                                                            if (next.length == targetLen) {
                                                                handleUnlockAttempt(next)
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            .testTag(
                                                if (isBio) "lock_keypad_bio"
                                                else if (isBack) "lock_keypad_back"
                                                else "lock_keypad_$key"
                                            ),
                                        shape = CircleShape,
                                        color = when {
                                            isBio -> if (isBiometricEnabled) BentoLavenderSubtle else Color.Transparent
                                            isBack -> Color(0xFFF1F5F9)
                                            else -> SurfaceWhite
                                        },
                                        border = if (!isBio || isBiometricEnabled) androidx.compose.foundation.BorderStroke(1.dp, BentoBorder) else null
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            when {
                                                isBio -> {
                                                    if (isBiometricEnabled && biometricStatus == BiometricStatus.AVAILABLE) {
                                                        Icon(
                                                            imageVector = Icons.Default.Fingerprint,
                                                            contentDescription = "اثر انگشت",
                                                            tint = BentoIndigoAccent,
                                                            modifier = Modifier.size(28.dp)
                                                        )
                                                    }
                                                }
                                                isBack -> {
                                                    Icon(
                                                        imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                        contentDescription = "حذف",
                                                        tint = BentoNavyDark,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                                else -> {
                                                    Text(
                                                        text = key,
                                                        fontSize = 22.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = BentoNavyDark
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // In Password mode: Biometric alternative button if enabled
                    if (isBiometricEnabled && biometricStatus == BiometricStatus.AVAILABLE) {
                        Surface(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .clickable { launchBiometricPrompt() }
                                .padding(vertical = 12.dp)
                                .testTag("lock_biometric_button"),
                            shape = RoundedCornerShape(16.dp),
                            color = BentoLavenderSubtle,
                            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Fingerprint,
                                    contentDescription = null,
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(24.dp)
                                )
                                Text(
                                    text = "ورود سریع با اثر انگشت",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BentoIndigoAccent
                                )
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                }
            }
        }
    }
}
