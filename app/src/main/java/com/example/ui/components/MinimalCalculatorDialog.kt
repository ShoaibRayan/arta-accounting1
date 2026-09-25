package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.ExpenseRed
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.util.Locale

/**
 * دکمه‌ی کوچک و شیک با آیکون ماشین‌حساب برای قرارگیری در کنار فیلدهای ورودی عدد
 */
@Composable
fun CalculatorMiniButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String = "ماشین‌حساب"
) {
    Surface(
        modifier = modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .clickable(onClick = onClick)
            .testTag("btn_open_calculator"),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFFEEF2FF),
        border = androidx.compose.foundation.BorderStroke(1.dp, BentoIndigoAccent.copy(alpha = 0.25f))
    ) {
        Box(
            modifier = Modifier.size(36.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Calculate,
                contentDescription = contentDescription,
                tint = BentoIndigoAccent,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * ماشین‌حساب مینیمال و شیک پاپ‌آپ برای تمام ورودی‌های عددی در برنامه
 */
@Composable
fun MinimalCalculatorDialog(
    initialValue: String = "",
    title: String = "ماشین‌حساب",
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    // مقدار اولیه ورودی عددی را تمیز می‌کنیم
    val cleanInitial = remember(initialValue) {
        val sanitized = initialValue.trim().replace(",", "")
        val num = sanitized.toDoubleOrNull()
        if (num != null && num > 0) {
            if (num % 1.0 == 0.0) String.format(Locale.US, "%.0f", num) else sanitized
        } else {
            ""
        }
    }

    var expression by remember { mutableStateOf(if (cleanInitial.isNotEmpty()) cleanInitial else "0") }
    var evaluatedValue by remember { mutableStateOf(cleanInitial.toDoubleOrNull() ?: 0.0) }

    // محاسبه‌ی فوری مقدار
    fun recompute() {
        evaluatedValue = evaluateExpressionSafe(expression)
    }

    fun onDigit(d: String) {
        if (expression == "0" || expression == "خطا") {
            expression = d
        } else {
            expression += d
        }
        recompute()
    }

    fun onDot() {
        if (expression.isEmpty() || expression == "خطا") {
            expression = "0."
        } else {
            // آخرین توکن را بررسی می‌کنیم
            val lastToken = expression.split(" ", "+", "-", "×", "÷").lastOrNull() ?: ""
            if (!lastToken.contains(".")) {
                expression = if (lastToken.isEmpty()) expression + "0." else expression + "."
            }
        }
        recompute()
    }

    fun onOperator(op: String) {
        if (expression == "خطا" || expression.isEmpty()) {
            expression = "0 $op "
            return
        }
        val trimmed = expression.trimEnd()
        if (trimmed.endsWith("+") || trimmed.endsWith("-") || trimmed.endsWith("×") || trimmed.endsWith("÷")) {
            expression = trimmed.dropLast(1).trimEnd() + " $op "
        } else {
            expression = "$trimmed $op "
        }
        recompute()
    }

    fun onBackspace() {
        if (expression.isNotEmpty() && expression != "خطا") {
            val trimmed = expression.trimEnd()
            expression = if (trimmed.length <= 1) "0" else trimmed.dropLast(1).trimEnd()
        } else {
            expression = "0"
        }
        recompute()
    }

    fun onClear() {
        expression = "0"
        evaluatedValue = 0.0
    }

    fun onPercent() {
        val currentVal = evaluateExpressionSafe(expression)
        val percentVal = currentVal / 100.0
        expression = formatNumber(percentVal)
        recompute()
    }

    fun onToggleSign() {
        val currentVal = evaluateExpressionSafe(expression)
        val toggled = -currentVal
        expression = formatNumber(toggled)
        recompute()
    }

    fun onEquals() {
        val res = evaluateExpressionSafe(expression)
        expression = formatNumber(res)
        evaluatedValue = res
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = SurfaceWhite,
            shadowElevation = 16.dp,
            border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .testTag("minimal_calculator_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Header (عنوان و کلید بستن)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEEF2FF)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Calculate,
                                contentDescription = null,
                                tint = BentoIndigoAccent,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = title,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "بستن",
                            tint = TextSecondary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // نمایشگر فرمول و نتیجه (Screen / Display)
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFF8FAFD),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BentoBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        // عبارت محاسباتی
                        Text(
                            text = if (expression.isEmpty()) "0" else expression,
                            color = TextSecondary,
                            fontSize = 14.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // نتیجه‌ی زنده و نهایی
                        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                            Text(
                                text = formatDisplayExpression(expression, evaluatedValue),
                                color = BentoNavyDark,
                                fontSize = 28.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.End,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // صفحه کلید ماشین‌حساب مینیمال (Keypad)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // ردیف ۱: C, ⌫, %, ÷
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CalcKey(
                                text = "C",
                                onClick = { onClear() },
                                color = Color(0xFFFEE2E2),
                                textColor = ExpenseRed,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey(
                                icon = Icons.AutoMirrored.Filled.Backspace,
                                onClick = { onBackspace() },
                                color = Color(0xFFF1F5F9),
                                textColor = BentoNavyDark,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey(
                                text = "%",
                                onClick = { onPercent() },
                                color = Color(0xFFF1F5F9),
                                textColor = BentoNavyDark,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey(
                                text = "÷",
                                onClick = { onOperator("÷") },
                                color = Color(0xFFFF851B),
                                textColor = Color.White,
                                isBold = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // ردیف ۲: 7, 8, 9, ×
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CalcKey("7", onClick = { onDigit("7") }, modifier = Modifier.weight(1f))
                            CalcKey("8", onClick = { onDigit("8") }, modifier = Modifier.weight(1f))
                            CalcKey("9", onClick = { onDigit("9") }, modifier = Modifier.weight(1f))
                            CalcKey(
                                text = "×",
                                onClick = { onOperator("×") },
                                color = Color(0xFFFF851B),
                                textColor = Color.White,
                                isBold = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // ردیف ۳: 4, 5, 6, -
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CalcKey("4", onClick = { onDigit("4") }, modifier = Modifier.weight(1f))
                            CalcKey("5", onClick = { onDigit("5") }, modifier = Modifier.weight(1f))
                            CalcKey("6", onClick = { onDigit("6") }, modifier = Modifier.weight(1f))
                            CalcKey(
                                text = "−",
                                onClick = { onOperator("-") },
                                color = Color(0xFFFF851B),
                                textColor = Color.White,
                                isBold = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // ردیف ۴: 1, 2, 3, +
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CalcKey("1", onClick = { onDigit("1") }, modifier = Modifier.weight(1f))
                            CalcKey("2", onClick = { onDigit("2") }, modifier = Modifier.weight(1f))
                            CalcKey("3", onClick = { onDigit("3") }, modifier = Modifier.weight(1f))
                            CalcKey(
                                text = "+",
                                onClick = { onOperator("+") },
                                color = Color(0xFFFF851B),
                                textColor = Color.White,
                                isBold = true,
                                modifier = Modifier.weight(1f)
                            )
                        }

                        // ردیف ۵: +/-, 0, ., =
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CalcKey(
                                text = "+/-",
                                onClick = { onToggleSign() },
                                color = Color(0xFFF1F5F9),
                                textColor = BentoNavyDark,
                                modifier = Modifier.weight(1f)
                            )
                            CalcKey("0", onClick = { onDigit("0") }, modifier = Modifier.weight(1f))
                            CalcKey(".", onClick = { onDot() }, modifier = Modifier.weight(1f))
                            CalcKey(
                                text = "=",
                                onClick = { onEquals() },
                                color = Color(0xFFFF851B),
                                textColor = Color.White,
                                isBold = true,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // دکمه‌های تأیید و انصراف
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(46.dp),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("انصراف", color = TextSecondary, fontSize = 13.sp)
                    }

                    Button(
                        onClick = {
                            val finalResult = evaluateExpressionSafe(expression)
                            val formatted = formatNumber(finalResult)
                            onConfirm(formatted)
                            onDismiss()
                        },
                        modifier = Modifier
                            .weight(1.5f)
                            .height(46.dp)
                            .testTag("calc_confirm_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = BentoNavyDark)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "تأیید و درج عدد",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CalcKey(
    text: String? = null,
    icon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: Color = Color.White,
    textColor: Color = BentoNavyDark,
    isBold: Boolean = false
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = color,
        border = androidx.compose.foundation.BorderStroke(0.75.dp, Color(0xFFE2E8F0)),
        shadowElevation = 1.dp
    ) {
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = textColor,
                    modifier = Modifier.size(18.dp)
                )
            } else if (text != null) {
                Text(
                    text = text,
                    color = textColor,
                    fontSize = if (text.length > 2) 13.sp else 17.sp,
                    fontWeight = if (isBold) FontWeight.Bold else FontWeight.SemiBold,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

/**
 * فرمت‌بندی خروجی نهایی بدون صفرهای اضافی اعشاری
 */
private fun formatNumber(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    return if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%.0f", value)
    } else {
        String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.')
    }
}

/**
 * فرمت‌بندی نمایشی عدد با کاما در نمایشگر
 */
private fun formatFormattedDisplay(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "0"
    return if (value % 1.0 == 0.0) {
        String.format(Locale.US, "%,.0f", value)
    } else {
        val parts = String.format(Locale.US, "%.4f", value).trimEnd('0').trimEnd('.').split(".")
        val integerPart = parts[0].toLongOrNull() ?: 0L
        if (parts.size > 1) {
            String.format(Locale.US, "%,d.%s", integerPart, parts[1])
        } else {
            String.format(Locale.US, "%,d", integerPart)
        }
    }
}

/**
 * فرمت‌بندی هوشمند متن ورودی کاربر بدون پاک کردن نقطه اعشار یا صفرهای انتهایی
 */
private fun formatDisplayExpression(expr: String, evaluatedValue: Double): String {
    val trimmed = expr.trim()
    if (trimmed.isEmpty() || trimmed == "0") return "0"
    if (trimmed == "خطا") return "خطا"

    val hasOp = trimmed.contains("+") || trimmed.contains("-") ||
            trimmed.contains("×") || trimmed.contains("÷") ||
            trimmed.contains("*") || trimmed.contains("/")

    if (!hasOp) {
        // تک‌عدد: حفظ نقطه اعشار و ارقام بعد از آن
        return if (trimmed.contains(".")) {
            val parts = trimmed.split(".", limit = 2)
            val intPart = parts[0].toLongOrNull() ?: 0L
            val formattedInt = String.format(Locale.US, "%,d", intPart)
            val decPart = parts.getOrNull(1) ?: ""
            "$formattedInt.$decPart"
        } else {
            val num = trimmed.toLongOrNull()
            if (num != null) String.format(Locale.US, "%,d", num) else trimmed
        }
    } else {
        return formatFormattedDisplay(evaluatedValue)
    }
}

/**
 * مفسر امن عبارات حسابی برای ارزیابی سریع فرمول‌ها
 */
private fun evaluateExpressionSafe(raw: String): Double {
    if (raw.isBlank() || raw == "خطا") return 0.0
    val clean = raw.trim()
        .replace("×", "*")
        .replace("÷", "/")
        .replace("−", "-")

    // جداسازی عملوندها و عملگرها
    val tokens = mutableListOf<String>()
    var currentNumber = StringBuilder()

    var i = 0
    while (i < clean.length) {
        val ch = clean[i]
        if (ch == ' ') {
            i++
            continue
        }
        if (ch in "+-*/") {
            if (currentNumber.isNotEmpty()) {
                tokens.add(currentNumber.toString())
                currentNumber = StringBuilder()
            }
            // اگر علامت منفی ابتدای عدد باشد (مانند -5 یا *-5)
            if (ch == '-' && (tokens.isEmpty() || tokens.last() in "+-*/")) {
                currentNumber.append(ch)
            } else {
                tokens.add(ch.toString())
            }
        } else {
            currentNumber.append(ch)
        }
        i++
    }
    if (currentNumber.isNotEmpty()) {
        tokens.add(currentNumber.toString())
    }

    if (tokens.isEmpty()) return 0.0

    // اگر انتهای عبارت عملگر باز مانده باشد (مثلاً 100 +)، عملگر انتهایی را نادیده می‌گیریم
    while (tokens.isNotEmpty() && tokens.last() in "+-*/") {
        tokens.removeAt(tokens.lastIndex)
    }

    if (tokens.isEmpty()) return 0.0

    // مرحله اول: اعمال ضرب و تقسیم
    val afterMulDiv = mutableListOf<String>()
    var idx = 0
    while (idx < tokens.size) {
        val token = tokens[idx]
        if (token == "*" || token == "/") {
            val prev = afterMulDiv.removeAt(afterMulDiv.lastIndex).toDoubleOrNull() ?: 0.0
            val next = if (idx + 1 < tokens.size) tokens[idx + 1].toDoubleOrNull() ?: 1.0 else 1.0
            val res = if (token == "*") prev * next else if (next != 0.0) prev / next else prev
            afterMulDiv.add(res.toString())
            idx += 2
        } else {
            afterMulDiv.add(token)
            idx++
        }
    }

    // مرحله دوم: اعمال جمع و تفریق
    var result = afterMulDiv.firstOrNull()?.toDoubleOrNull() ?: 0.0
    var opIdx = 1
    while (opIdx < afterMulDiv.size) {
        val op = afterMulDiv[opIdx]
        val nextVal = if (opIdx + 1 < afterMulDiv.size) afterMulDiv[opIdx + 1].toDoubleOrNull() ?: 0.0 else 0.0
        if (op == "+") {
            result += nextVal
        } else if (op == "-") {
            result -= nextVal
        }
        opIdx += 2
    }

    return result
}
