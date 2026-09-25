package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Today
import com.example.ui.theme.ExpenseRed
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.BackgroundCanvas
import com.example.ui.theme.BentoBorder
import com.example.ui.theme.BentoIndigoAccent
import com.example.ui.theme.BentoNavyDark
import com.example.ui.theme.SurfaceWhite
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.PersianDateHelper

@Composable
fun SolarDatePickerDialog(
    initialTimestamp: Long = System.currentTimeMillis(),
    title: String = "انتخاب تاریخ",
    onDismiss: () -> Unit,
    onDateSelected: (timestamp: Long) -> Unit
) {
    val calType = PersianDateHelper.activeCalendarType

    val (initYear, initMonth, initDay) = remember(initialTimestamp, calType) {
        when (calType) {
            com.example.util.AppCalendarType.GREGORIAN -> {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = initialTimestamp }
                Triple(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH))
            }
            com.example.util.AppCalendarType.LUNAR_HIJRI -> {
                PersianDateHelper.timestampToHijri(initialTimestamp)
            }
            else -> {
                val j = PersianDateHelper.timestampToJalali(initialTimestamp)
                Triple(j.year, j.month, j.day)
            }
        }
    }

    val (todayY, todayM, todayD) = remember(calType) {
        val now = System.currentTimeMillis()
        when (calType) {
            com.example.util.AppCalendarType.GREGORIAN -> {
                val cal = java.util.Calendar.getInstance().apply { timeInMillis = now }
                Triple(cal.get(java.util.Calendar.YEAR), cal.get(java.util.Calendar.MONTH) + 1, cal.get(java.util.Calendar.DAY_OF_MONTH))
            }
            com.example.util.AppCalendarType.LUNAR_HIJRI -> {
                PersianDateHelper.timestampToHijri(now)
            }
            else -> {
                val j = PersianDateHelper.todayJalali()
                Triple(j.year, j.month, j.day)
            }
        }
    }

    var selectedYear by remember(initYear) { mutableIntStateOf(initYear) }
    var selectedMonth by remember(initMonth) { mutableIntStateOf(initMonth) }
    var selectedDay by remember(initDay) { mutableIntStateOf(initDay) }

    val daysInMonth = remember(selectedYear, selectedMonth, calType) {
        when (calType) {
            com.example.util.AppCalendarType.GREGORIAN -> PersianDateHelper.getDaysInGregorianMonth(selectedYear, selectedMonth)
            com.example.util.AppCalendarType.LUNAR_HIJRI -> 30
            else -> PersianDateHelper.getDaysInJalaliMonth(selectedYear, selectedMonth)
        }
    }

    if (selectedDay > daysInMonth) {
        selectedDay = daysInMonth
    }

    fun calculateTimestamp(year: Int, month: Int, day: Int): Long {
        return when (calType) {
            com.example.util.AppCalendarType.GREGORIAN -> PersianDateHelper.gregorianToTimestamp(year, month, day)
            com.example.util.AppCalendarType.LUNAR_HIJRI -> PersianDateHelper.hijriToTimestamp(year, month, day)
            else -> PersianDateHelper.jalaliToTimestamp(year, month, day)
        }
    }

    val monthNames = when (calType) {
        com.example.util.AppCalendarType.SOLAR_IRANIAN -> PersianDateHelper.IRANIAN_MONTHS
        com.example.util.AppCalendarType.GREGORIAN -> PersianDateHelper.GREGORIAN_MONTHS
        com.example.util.AppCalendarType.LUNAR_HIJRI -> PersianDateHelper.LUNAR_MONTHS
        else -> PersianDateHelper.DARI_MONTHS
    }
    val monthName = monthNames.getOrElse(selectedMonth - 1) { "" }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth(0.92f)
                    .clip(RoundedCornerShape(26.dp)),
                color = SurfaceWhite,
                shadowElevation = 12.dp
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Header: Month & Year Navigation + Today Button + Close
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Today Button
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color(0xFFEEF2FF),
                            modifier = Modifier.clickable {
                                selectedYear = todayY
                                selectedMonth = todayM
                                selectedDay = todayD
                                val ts = calculateTimestamp(todayY, todayM, todayD)
                                onDateSelected(ts)
                            }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Today,
                                    contentDescription = "امروز",
                                    tint = BentoIndigoAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = "امروز",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = BentoIndigoAccent
                                )
                            }
                        }

                        // Selected Month / Year Text
                        Text(
                            text = "$monthName $selectedYear",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = BentoNavyDark
                        )

                        // Close button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFF1F5F9))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "بستن",
                                tint = BentoNavyDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Month & Year Navigation Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(Color(0xFFF8FAFD))
                            .border(1.dp, BentoBorder, RoundedCornerShape(14.dp))
                            .padding(horizontal = 8.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Previous Month
                        IconButton(
                            onClick = {
                                if (selectedMonth > 1) {
                                    selectedMonth--
                                } else {
                                    selectedMonth = 12
                                    selectedYear--
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "ماه قبل",
                                tint = BentoNavyDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Year Stepper
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            IconButton(
                                onClick = { selectedYear-- },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Remove, contentDescription = "کاهش سال", tint = BentoNavyDark, modifier = Modifier.size(14.dp))
                            }
                            Text(
                                text = "سال $selectedYear",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = BentoNavyDark
                            )
                            IconButton(
                                onClick = { selectedYear++ },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = "افزایش سال", tint = BentoNavyDark, modifier = Modifier.size(14.dp))
                            }
                        }

                        // Next Month
                        IconButton(
                            onClick = {
                                if (selectedMonth < 12) {
                                    selectedMonth++
                                } else {
                                    selectedMonth = 1
                                    selectedYear++
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "ماه بعد",
                                tint = BentoNavyDark,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    // Days of week header (شنبه تا جمعه)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("ش", "ی", "د", "س", "چ", "پ", "ج").forEach { dayName ->
                            Text(
                                text = dayName,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (dayName == "ج") ExpenseRed else TextSecondary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Days Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(7),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(190.dp),
                        contentPadding = PaddingValues(2.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        items(daysInMonth) { dayIndex ->
                            val day = dayIndex + 1
                            val isSelected = day == selectedDay
                            val isToday = day == todayD && selectedMonth == todayM && selectedYear == todayY

                            Box(
                                modifier = Modifier
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when {
                                            isSelected -> BentoIndigoAccent
                                            isToday -> Color(0xFFEEF2FF)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .border(
                                        width = if (isToday && !isSelected) 1.dp else 0.dp,
                                        color = if (isToday && !isSelected) BentoIndigoAccent else Color.Transparent,
                                        shape = CircleShape
                                    )
                                    .clickable {
                                        selectedDay = day
                                        val ts = calculateTimestamp(selectedYear, selectedMonth, day)
                                        onDateSelected(ts)
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$day",
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                                    color = when {
                                        isSelected -> Color.White
                                        isToday -> BentoIndigoAccent
                                        else -> TextPrimary
                                    },
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}
