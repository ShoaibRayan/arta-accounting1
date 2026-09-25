package com.example.ui.screens

import android.widget.Toast
import androidx.biometric.BiometricPrompt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.PrivacyTip
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
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
import com.example.util.AutoLockDuration
import com.example.util.BiometricStatus
import com.example.util.LockType

@Composable
fun SecuritySettingsContent(
    viewModel: FinanceViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity

    val isAppLockEnabled by viewModel.isAppLockEnabled.collectAsState()
    val lockType by viewModel.lockType.collectAsState()
    val hasPasscode by viewModel.hasPasscode.collectAsState()
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()
    val autoLockDuration by viewModel.autoLockDuration.collectAsState()
    val isScreenSecurityEnabled by viewModel.isScreenSecurityEnabled.collectAsState()
    val biometricStatus by viewModel.biometricStatus.collectAsState()

    var showSetPasscodeDialog by remember { mutableStateOf(false) }
    var showDisableLockConfirmDialog by remember { mutableStateOf(false) }
    var confirmPasswordInput by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }

    LazyColumn(
        modifier = modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Top Security Status Banner ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (isAppLockEnabled) Color(0xFFEFF6FF) else Color(0xFFFFFBEB)
                ),
                border = BorderStroke(
                    1.dp,
                    if (isAppLockEnabled) BentoIndigoAccent.copy(alpha = 0.3f) else Color(0xFFFDE68A)
                ),
                modifier = Modifier.fillMaxWidth().testTag("security_status_card")
            ) {
                Row(
                    modifier = Modifier
                        .clickable {
                            val enable = !isAppLockEnabled
                            if (enable) {
                                if (hasPasscode) {
                                    viewModel.setAppLockEnabled(true)
                                    Toast.makeText(context, "قفل برنامه فعال شد", Toast.LENGTH_SHORT).show()
                                } else {
                                    showSetPasscodeDialog = true
                                }
                            } else {
                                if (hasPasscode) {
                                    confirmPasswordInput = ""
                                    confirmPasswordError = null
                                    showDisableLockConfirmDialog = true
                                } else {
                                    viewModel.setAppLockEnabled(false)
                                }
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isAppLockEnabled) BentoIndigoAccent else Color(0xFFF59E0B)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = if (isAppLockEnabled) Icons.Default.Security else Icons.Default.LockReset,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = if (isAppLockEnabled) "قفل امنیتی برنامه فعال است" else "قفل برنامه غیرفعال است",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isAppLockEnabled)
                                    "اطلاعات مالی با ${lockType.title} ${if (isBiometricEnabled) "و اثر انگشت" else ""} محافظت می‌شود."
                                else
                                    "برای جلوگیری از دسترسی دیگران، قفل و رمز عبور را فعال کنید.",
                                fontSize = 11.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = isAppLockEnabled,
                        onCheckedChange = { enable ->
                            if (enable) {
                                if (hasPasscode) {
                                    viewModel.setAppLockEnabled(true)
                                    Toast.makeText(context, "قفل برنامه فعال شد", Toast.LENGTH_SHORT).show()
                                } else {
                                    showSetPasscodeDialog = true
                                }
                            } else {
                                if (hasPasscode) {
                                    confirmPasswordInput = ""
                                    confirmPasswordError = null
                                    showDisableLockConfirmDialog = true
                                } else {
                                    viewModel.setAppLockEnabled(false)
                                }
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BentoIndigoAccent
                        ),
                        modifier = Modifier.testTag("app_lock_switch")
                    )
                }
            }
        }

        // --- 2. Passcode & Lock Type Settings ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth().testTag("passcode_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = BentoIndigoAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "رمز عبور و شیوه قفل‌گذاری",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = Color(0xFFF8FAFC),
                        border = BorderStroke(0.5.dp, BentoBorder),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "نوع قفل فعلی:",
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                Text(
                                    text = if (hasPasscode) lockType.title else "هنوز رمزی تنظیم نشده است",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (hasPasscode) BentoNavyDark else ExpenseRed
                                )
                            }

                            Button(
                                onClick = { showSetPasscodeDialog = true },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                                modifier = Modifier.testTag("set_change_passcode_btn")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Pin,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (hasPasscode) "تغییر رمز" else "تنظیم رمز",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 3. Biometric & Fingerprint Authentication Card ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth().testTag("biometric_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    val isBiometricToggleable = isAppLockEnabled && hasPasscode && biometricStatus == BiometricStatus.AVAILABLE
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .clickable(enabled = isBiometricToggleable) {
                                val nextState = !isBiometricEnabled
                                viewModel.setBiometricEnabled(nextState)
                                Toast.makeText(
                                    context,
                                    if (nextState) "ورود با اثر انگشت فعال شد" else "ورود با اثر انگشت غیرفعال شد",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(22.dp)
                            )
                            Text(
                                text = "احراز هویت بیومتریک و اثر انگشت",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                        }

                        Switch(
                            checked = isBiometricEnabled,
                            enabled = isBiometricToggleable,
                            onCheckedChange = { enabled ->
                                viewModel.setBiometricEnabled(enabled)
                                Toast.makeText(
                                    context,
                                    if (enabled) "ورود با اثر انگشت فعال شد" else "ورود با اثر انگشت غیرفعال شد",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = BentoIndigoAccent
                            ),
                            modifier = Modifier.testTag("biometric_toggle_switch")
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Hardware Status Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = when (biometricStatus) {
                            BiometricStatus.AVAILABLE -> Color(0xFFF0FDF4)
                            BiometricStatus.NOT_ENROLLED -> Color(0xFFFFFBEB)
                            BiometricStatus.NO_HARDWARE, BiometricStatus.HARDWARE_UNAVAILABLE -> Color(0xFFF8FAFC)
                        },
                        border = BorderStroke(
                            0.5.dp,
                            when (biometricStatus) {
                                BiometricStatus.AVAILABLE -> Color(0xFF86EFAC)
                                BiometricStatus.NOT_ENROLLED -> Color(0xFFFDE68A)
                                else -> BentoBorder
                            }
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = when (biometricStatus) {
                                    BiometricStatus.AVAILABLE -> Icons.Default.CheckCircle
                                    BiometricStatus.NOT_ENROLLED -> Icons.Default.Warning
                                    else -> Icons.Default.Info
                                },
                                contentDescription = null,
                                tint = when (biometricStatus) {
                                    BiometricStatus.AVAILABLE -> IncomeGreen
                                    BiometricStatus.NOT_ENROLLED -> Color(0xFFD97706)
                                    else -> TextSecondary
                                },
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = when (biometricStatus) {
                                    BiometricStatus.AVAILABLE -> "حسگر اثر انگشت دستگاه آماده به کار است."
                                    BiometricStatus.NOT_ENROLLED -> "حسگر موجود است اما اثر انگشتی در تنظیمات گوشی تعریف نشده است."
                                    BiometricStatus.NO_HARDWARE -> "این دستگاه فاقد حسگر اثر انگشت سخت‌افزاری است."
                                    BiometricStatus.HARDWARE_UNAVAILABLE -> "حسگر بیومتریک دستگاه موقتاً در دسترس نیست."
                                },
                                fontSize = 11.sp,
                                color = when (biometricStatus) {
                                    BiometricStatus.AVAILABLE -> BentoNavyDark
                                    BiometricStatus.NOT_ENROLLED -> Color(0xFF92400E)
                                    else -> TextSecondary
                                },
                                lineHeight = 16.sp
                            )
                        }
                    }

                    // Test Biometric Button
                    if (biometricStatus == BiometricStatus.AVAILABLE && activity != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        OutlinedButton(
                            onClick = {
                                viewModel.securityManager.authenticateWithBiometrics(
                                    activity = activity,
                                    title = "آزمایش حسگر اثر انگشت",
                                    subtitle = "لطفاً انگشت خود را روی حسگر قرار دهید",
                                    negativeButtonText = "انصراف",
                                    onSuccess = {
                                        Toast.makeText(context, "احراز هویت اثر انگشت موفقیت‌آمیز بود ✅", Toast.LENGTH_SHORT).show()
                                    },
                                    onError = { code, err ->
                                        if (code != BiometricPrompt.ERROR_USER_CANCELED && code != BiometricPrompt.ERROR_NEGATIVE_BUTTON) {
                                            Toast.makeText(context, "خطا: $err", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    onFailed = {
                                        Toast.makeText(context, "اثر انگشت مطابقت نداشت", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, BentoIndigoAccent),
                            modifier = Modifier.fillMaxWidth().testTag("test_biometric_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fingerprint,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "آزمایش و تست عملکرد حسگر اثر انگشت",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = BentoIndigoAccent
                            )
                        }
                    }
                }
            }
        }

        // --- 4. Auto-Lock Duration Settings ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth().testTag("autolock_settings_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = null,
                            tint = BentoIndigoAccent,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "زمان قفل خودکار",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "مدت زمانی که پس از خروج یا خاموش شدن صفحه، برنامه مجدداً قفل می‌شود:",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    AutoLockDuration.values().forEach { duration ->
                        val isSelected = autoLockDuration == duration
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) BentoLavenderSubtle else Color.Transparent,
                            border = BorderStroke(1.dp, if (isSelected) BentoIndigoAccent else BentoBorder),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    viewModel.setAutoLockDuration(duration)
                                    Toast.makeText(context, "زمان قفل به «${duration.title}» تغییر یافت", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.setAutoLockDuration(duration) },
                                    colors = RadioButtonDefaults.colors(selectedColor = BentoIndigoAccent)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = duration.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = BentoNavyDark
                                )
                            }
                        }
                    }
                }
            }
        }

        // --- 5. Screen Security & Screenshot Protection (FLAG_SECURE) ---
        item {
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
                border = BorderStroke(1.dp, BentoBorder),
                modifier = Modifier.fillMaxWidth().testTag("screen_security_card")
            ) {
                Row(
                    modifier = Modifier
                        .clickable {
                            val nextState = !isScreenSecurityEnabled
                            viewModel.setScreenSecurityEnabled(nextState)
                            Toast.makeText(
                                context,
                                if (nextState) "حفاظت ضد اسکرین‌شات فعال شد" else "حفاظت ضد اسکرین‌شات غیرفعال شد",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BentoLavenderSubtle),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PrivacyTip,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "محافظت از صفحه و ضد اسکرین‌شات",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "جلوگیری از اسکرین‌شات و مخفی‌کردن اطلاعات مالی در فهرست برنامه‌های اخیر اندروید.",
                                fontSize = 10.sp,
                                color = TextSecondary,
                                lineHeight = 15.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Switch(
                        checked = isScreenSecurityEnabled,
                        onCheckedChange = { enabled ->
                            viewModel.setScreenSecurityEnabled(enabled)
                            Toast.makeText(
                                context,
                                if (enabled) "حفاظت ضد اسکرین‌شات فعال شد" else "حفاظت ضد اسکرین‌شات غیرفعال شد",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = BentoIndigoAccent
                        ),
                        modifier = Modifier.testTag("screen_security_switch")
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(30.dp))
        }
    }

    // --- Dialog: Set or Change Passcode ---
    if (showSetPasscodeDialog) {
        SetPasscodeDialog(
            currentHasPasscode = hasPasscode,
            initialLockType = lockType,
            onDismiss = { showSetPasscodeDialog = false },
            onVerifyOldPasscode = { old -> viewModel.verifyPasscode(old) },
            onSaveNewPasscode = { newCode, newType ->
                viewModel.setPasscode(newCode, newType)
                showSetPasscodeDialog = false
                Toast.makeText(context, "رمز عبور با موفقیت ذخیره و قفل برنامه فعال شد ✅", Toast.LENGTH_LONG).show()
            }
        )
    }

    // --- Dialog: Confirm Password to Disable Lock ---
    if (showDisableLockConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDisableLockConfirmDialog = false },
            title = {
                Text(
                    text = "غیرفعال‌سازی قفل برنامه",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = BentoNavyDark
                )
            },
            text = {
                Column {
                    Text(
                        text = "برای غیرفعال‌سازی قفل، لطفاً رمز عبور فعلی خود را وارد کنید:",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = confirmPasswordInput,
                        onValueChange = {
                            confirmPasswordInput = it
                            confirmPasswordError = null
                        },
                        placeholder = { Text("رمز عبور فعلی") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        isError = confirmPasswordError != null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("disable_lock_password_input")
                    )
                    if (confirmPasswordError != null) {
                        Text(
                            text = confirmPasswordError ?: "",
                            color = ExpenseRed,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (viewModel.verifyPasscode(confirmPasswordInput)) {
                            viewModel.setAppLockEnabled(false)
                            showDisableLockConfirmDialog = false
                            Toast.makeText(context, "قفل برنامه غیرفعال شد", Toast.LENGTH_SHORT).show()
                        } else {
                            confirmPasswordError = "رمز وارد شده اشتباه است"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ExpenseRed),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("غیرفعال‌سازی", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDisableLockConfirmDialog = false }) {
                    Text("انصراف")
                }
            },
            containerColor = SurfaceWhite,
            shape = RoundedCornerShape(18.dp)
        )
    }
}

@Composable
fun SetPasscodeDialog(
    currentHasPasscode: Boolean,
    initialLockType: LockType,
    onDismiss: () -> Unit,
    onVerifyOldPasscode: (String) -> Boolean,
    onSaveNewPasscode: (String, LockType) -> Unit
) {
    var selectedType by remember { mutableStateOf(initialLockType) }
    var oldPasscode by remember { mutableStateOf("") }
    var newPasscode by remember { mutableStateOf("") }
    var confirmPasscode by remember { mutableStateOf("") }

    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(Icons.Default.Lock, contentDescription = null, tint = BentoIndigoAccent)
                Text(
                    text = if (currentHasPasscode) "تغییر رمز ورود" else "تنظیم رمز ورود جدید",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = BentoNavyDark
                )
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Type selector
                Text(
                    text = "نوع رمز را انتخاب کنید:",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BentoNavyDark
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    LockType.values().forEach { type ->
                        val isSel = selectedType == type
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .clickable {
                                    selectedType = type
                                    newPasscode = ""
                                    confirmPasscode = ""
                                    errorMessage = null
                                },
                            shape = RoundedCornerShape(10.dp),
                            color = if (isSel) BentoIndigoAccent else Color(0xFFF1F5F9),
                            border = BorderStroke(1.dp, if (isSel) BentoIndigoAccent else BentoBorder)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (type) {
                                        LockType.PIN_4 -> "پین ۴ رقم"
                                        LockType.PIN_6 -> "پین ۶ رقم"
                                        LockType.PASSWORD -> "گذرواژه"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSel) Color.White else BentoNavyDark
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Old Passcode (if changing)
                if (currentHasPasscode) {
                    OutlinedTextField(
                        value = oldPasscode,
                        onValueChange = {
                            oldPasscode = it
                            errorMessage = null
                        },
                        label = { Text("رمز عبور فعلی", fontSize = 11.sp) },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth().testTag("old_passcode_input")
                    )
                }

                // New Passcode
                OutlinedTextField(
                    value = newPasscode,
                    onValueChange = {
                        val filtered = if (selectedType != LockType.PASSWORD) it.filter { char -> char.isDigit() } else it
                        val maxLen = if (selectedType != LockType.PASSWORD) selectedType.length else 32
                        if (filtered.length <= maxLen) {
                            newPasscode = filtered
                            errorMessage = null
                        }
                    },
                    label = {
                        Text(
                            text = if (selectedType != LockType.PASSWORD) "رمز جدید (${selectedType.length} رقم)" else "گذرواژه جدید",
                            fontSize = 11.sp
                        )
                    },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                            Icon(
                                imageVector = if (isPasswordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextSecondary
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("new_passcode_input")
                )

                // Confirm New Passcode
                OutlinedTextField(
                    value = confirmPasscode,
                    onValueChange = {
                        val filtered = if (selectedType != LockType.PASSWORD) it.filter { char -> char.isDigit() } else it
                        val maxLen = if (selectedType != LockType.PASSWORD) selectedType.length else 32
                        if (filtered.length <= maxLen) {
                            confirmPasscode = filtered
                            errorMessage = null
                        }
                    },
                    label = { Text("تکرار رمز جدید", fontSize = 11.sp) },
                    singleLine = true,
                    visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().testTag("confirm_passcode_input")
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = ExpenseRed,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (currentHasPasscode && !onVerifyOldPasscode(oldPasscode)) {
                        errorMessage = "رمز عبور فعلی نادرست است"
                        return@Button
                    }
                    if (selectedType != LockType.PASSWORD && newPasscode.length != selectedType.length) {
                        errorMessage = "رمز جدید باید دقیقاً ${selectedType.length} رقم باشد"
                        return@Button
                    }
                    if (selectedType == LockType.PASSWORD && newPasscode.length < 4) {
                        errorMessage = "گذرواژه باید حداقل ۴ کاراکتر باشد"
                        return@Button
                    }
                    if (newPasscode != confirmPasscode) {
                        errorMessage = "تکرار رمز با رمز جدید مطابقت ندارد"
                        return@Button
                    }

                    onSaveNewPasscode(newPasscode, selectedType)
                },
                colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("save_passcode_btn")
            ) {
                Text("ذخیره و فعال‌سازی", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("انصراف")
            }
        },
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(20.dp)
    )
}
