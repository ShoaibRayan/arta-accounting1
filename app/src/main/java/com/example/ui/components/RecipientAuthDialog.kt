package com.example.ui.components

import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.fragment.app.FragmentActivity
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoLavenderSubtle
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.FinanceViewModel
import com.example.util.BiometricStatus
import com.example.util.LockType
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun RecipientAuthDialog(
    recipientName: String,
    viewModel: FinanceViewModel,
    promptTitle: String = "احراز هویت برای نمایش مبلغ",
    promptSubtitle: String? = null,
    onAuthenticated: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val scope = rememberCoroutineScope()

    val hasPasscode = viewModel.securityManager.hasPasscode()
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
            onAuthenticated()
            onDismiss()
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
                title = "احراز هویت بیومتریک",
                subtitle = "برای نمایش مبلغ «$recipientName»، حسگر اثر انگشت را لمس کنید",
                negativeButtonText = "ورود با رمز عبور",
                onSuccess = {
                    onAuthenticated()
                    onDismiss()
                },
                onError = { code, err ->
                    if (code != BiometricPrompt.ERROR_USER_CANCELED && code != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                        errorMessage = err
                    }
                },
                onFailed = {
                    errorMessage = "اثر انگشت شناسایی نشد"
                }
            )
        }
    }

    // Auto-prompt biometrics if available
    LaunchedEffect(Unit) {
        if (hasPasscode && isBiometricEnabled && biometricStatus == BiometricStatus.AVAILABLE && activity != null) {
            launchBiometricPrompt()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth(0.92f)
                        .clip(RoundedCornerShape(24.dp))
                        .testTag("recipient_auth_dialog"),
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                    border = BorderStroke(1.dp, BentoBorder),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Header Row with Close
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(BentoLavenderSubtle),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (hasPasscode) Icons.Default.Lock else Icons.Default.Security,
                                    contentDescription = null,
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            IconButton(
                                onClick = onDismiss,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "بستن",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        if (!hasPasscode) {
                            // Case 1: App lock is NOT configured yet
                            Text(
                                text = "قفل برنامه فعال نیست",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "برای محافظت از مبالغ، ابتدا قفل برنامه را در تنظیمات فعال کنید.",
                                fontSize = 13.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp,
                                modifier = Modifier.padding(horizontal = 12.dp)
                            )

                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = {
                                    onDismiss()
                                    onNavigateToSettings()
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(46.dp)
                                    .testTag("recipient_auth_go_to_settings_btn")
                            ) {
                                Icon(Icons.Default.Settings, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("رفتن به تنظیمات", fontWeight = FontWeight.Bold)
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, BentoBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                            ) {
                                Text("انصراف", color = TextSecondary)
                            }
                        } else {
                            // Case 2: Passcode is active, prompt authentication
                            Text(
                                text = promptTitle,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = promptSubtitle ?: "برای مشاهده مبلغ «$recipientName» رمز عبور را وارد کنید",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Indicator / Input area
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .offset { IntOffset(x = shakeOffset.value.roundToInt(), y = 0) }
                            ) {
                                if (lockType == LockType.PASSWORD) {
                                    OutlinedTextField(
                                        value = inputCode,
                                        onValueChange = {
                                            inputCode = it
                                            errorMessage = null
                                        },
                                        placeholder = { Text("رمز عبور", fontSize = 13.sp) },
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
                                        shape = RoundedCornerShape(14.dp),
                                        colors = OutlinedTextFieldDefaults.colors(
                                            focusedBorderColor = BentoIndigoAccent,
                                            unfocusedBorderColor = BentoBorder,
                                            focusedContainerColor = SurfaceWhite,
                                            unfocusedContainerColor = SurfaceWhite
                                        ),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("recipient_auth_password_input")
                                    )

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Button(
                                        onClick = { handleUnlockAttempt(inputCode) },
                                        enabled = inputCode.isNotBlank(),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = BentoIndigoAccent),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(44.dp)
                                            .testTag("recipient_auth_submit_btn")
                                    ) {
                                        Icon(Icons.Default.LockOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text("تأیید و نمایش", fontWeight = FontWeight.Bold)
                                    }
                                } else {
                                    // PIN mode dots
                                    val targetLen = lockType.length
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(vertical = 8.dp)
                                    ) {
                                        repeat(targetLen) { index ->
                                            val isFilled = index < inputCode.length
                                            Box(
                                                modifier = Modifier
                                                    .size(14.dp)
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

                                AnimatedVisibility(
                                    visible = errorMessage != null,
                                    enter = fadeIn(),
                                    exit = fadeOut()
                                ) {
                                    Text(
                                        text = errorMessage ?: "",
                                        color = ExpenseRed,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }

                            // PIN Keypad
                            if (lockType != LockType.PASSWORD) {
                                val targetLen = lockType.length
                                Spacer(modifier = Modifier.height(12.dp))

                                val keypadRows = listOf(
                                    listOf("3", "2", "1"),
                                    listOf("6", "5", "4"),
                                    listOf("9", "8", "7"),
                                    listOf("BACK", "0", "BIO")
                                )

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
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
                                                        .size(54.dp)
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
                                                            if (isBio) "recipient_auth_keypad_bio"
                                                            else if (isBack) "recipient_auth_keypad_back"
                                                            else "recipient_auth_keypad_$key"
                                                        ),
                                                    shape = CircleShape,
                                                    color = when {
                                                        isBio -> if (isBiometricEnabled && biometricStatus == BiometricStatus.AVAILABLE) BentoLavenderSubtle else Color.Transparent
                                                        isBack -> Color(0xFFF1F5F9)
                                                        else -> SurfaceWhite
                                                    },
                                                    border = if (!isBio || (isBiometricEnabled && biometricStatus == BiometricStatus.AVAILABLE)) BorderStroke(1.dp, BentoBorder) else null
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
                                                                        modifier = Modifier.size(24.dp)
                                                                    )
                                                                }
                                                            }
                                                            isBack -> {
                                                                Icon(
                                                                    imageVector = Icons.AutoMirrored.Filled.Backspace,
                                                                    contentDescription = "حذف",
                                                                    tint = BentoNavyDark,
                                                                    modifier = Modifier.size(20.dp)
                                                                )
                                                            }
                                                            else -> {
                                                                Text(
                                                                    text = key,
                                                                    fontSize = 20.sp,
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
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            OutlinedButton(
                                onClick = onDismiss,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, BentoBorder),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                                    .testTag("recipient_auth_cancel_btn")
                            ) {
                                Text("انصراف", color = TextSecondary, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
