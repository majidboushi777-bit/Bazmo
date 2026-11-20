package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Inbox
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentCyan
import com.example.ui.theme.AccentCyanDark
import com.example.ui.theme.BorderLight
import com.example.ui.theme.CardSurface
import com.example.ui.theme.DangerRed
import com.example.ui.theme.DangerSoft
import com.example.ui.theme.DangerText
import com.example.ui.theme.GoldAccent
import com.example.ui.theme.GoldSoft
import com.example.ui.theme.GoldText
import com.example.ui.theme.Navy
import com.example.ui.theme.NavyDeep
import com.example.ui.theme.NavyLight
import com.example.ui.theme.NavySoft
import com.example.ui.theme.PurpleSoft
import com.example.ui.theme.PurpleText
import com.example.ui.theme.SoftSurface
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.SuccessSoft
import com.example.ui.theme.SuccessText
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

enum class BadgeTone { BLUE, GREEN, GOLD, RED, PURPLE }

@Composable
fun BazmoBadge(
    text: String,
    tone: BadgeTone = BadgeTone.BLUE,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when (tone) {
        BadgeTone.BLUE -> NavySoft to Navy
        BadgeTone.GREEN -> SuccessSoft to SuccessText
        BadgeTone.GOLD -> GoldSoft to GoldText
        BadgeTone.RED -> DangerSoft to DangerText
        BadgeTone.PURPLE -> PurpleSoft to PurpleText
    }
    Surface(
        color = bg,
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Text(
            text = text,
            color = fg,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BazmoCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BorderLight,
    borderWidth: Float = 1f,
    containerColor: Color = CardSurface,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(18.dp)
    val animatedBorder by animateColorAsState(
        targetValue = borderColor,
        animationSpec = tween(160),
        label = "cardBorder"
    )
    val animatedContainer by animateColorAsState(
        targetValue = containerColor,
        animationSpec = tween(160),
        label = "cardContainer"
    )
    val cardModifier = modifier
        .fillMaxWidth()
        .padding(bottom = 10.dp)
        .let { m -> if (onClick != null) m.clip(shape).clickable(onClick = onClick) else m }

    Card(
        modifier = cardModifier,
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = animatedContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = BorderStroke(borderWidth.dp, animatedBorder)
    ) {
        Column(
            modifier = Modifier
                .animateContentSize(animationSpec = spring(stiffness = Spring.StiffnessMediumLow))
                .padding(14.dp),
            content = content
        )
    }
}

@Composable
fun BazmoInput(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    minLines: Int = 1,
    keyboardType: KeyboardType = KeyboardType.Text,
    testTag: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
        },
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyLight,
            unfocusedBorderColor = BorderLight,
            focusedContainerColor = CardSurface,
            unfocusedContainerColor = CardSurface,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .fillMaxWidth()
            .let { if (testTag != null) it.testTag(testTag) else it }
    )
}

@Composable
fun BazmoSearchBar(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    testTag: String? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        leadingIcon = {
            Icon(
                imageVector = Icons.Filled.Search,
                contentDescription = "جستجو",
                tint = TextMuted
            )
        },
        trailingIcon = {
            if (value.isNotEmpty()) {
                IconButton(
                    onClick = { onValueChange("") },
                    modifier = Modifier.minimumInteractiveComponentSize()
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "پاک کردن",
                        tint = TextMuted
                    )
                }
            }
        },
        singleLine = true,
        shape = RoundedCornerShape(14.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = NavyLight,
            unfocusedBorderColor = BorderLight,
            focusedContainerColor = CardSurface,
            unfocusedContainerColor = CardSurface,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        textStyle = MaterialTheme.typography.bodyMedium,
        modifier = modifier
            .fillMaxWidth()
            .let { if (testTag != null) it.testTag(testTag) else it }
    )
}

@Composable
fun BazmoDropdownSelector(
    label: String? = null,
    selectedValue: String,
    options: List<String>,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                modifier = Modifier.padding(bottom = 4.dp)
            )
        }
        Box {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = CardSurface,
                border = BorderStroke(1.5.dp, BorderLight),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { expanded = true }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 11.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = selectedValue,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.Filled.ArrowDropDown,
                        contentDescription = "انتخاب",
                        tint = Navy
                    )
                }
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(CardSurface)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (option == selectedValue) FontWeight.Bold else FontWeight.Normal,
                                color = if (option == selectedValue) Navy else TextPrimary
                            )
                        },
                        onClick = {
                            expanded = false
                            onSelect(option)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun BazmoSectionHeader(
    title: String,
    badgeText: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 10.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = NavyDeep,
            fontWeight = FontWeight.ExtraBold
        )
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (badgeText != null) {
                BazmoBadge(text = badgeText, tone = BadgeTone.BLUE)
            }
            if (actionLabel != null && onAction != null) {
                BazmoSoftButton(
                    text = actionLabel,
                    onClick = onAction,
                    compact = true
                )
            }
        }
    }
}

@Composable
fun BazmoBackBar(
    title: String,
    onBack: () -> Unit,
    trailing: (@Composable () -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            color = NavySoft,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .heightIn(min = 42.dp)
                .clip(RoundedCornerShape(10.dp))
                .clickable(onClick = onBack)
                .testTag("back_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = Navy,
                    modifier = Modifier.size(16.dp)
                )
                Text(
                    text = "بازگشت",
                    style = MaterialTheme.typography.labelMedium,
                    color = Navy
                )
            }
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = NavyDeep,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )

        if (trailing != null) {
            trailing()
        }
    }
}

@Composable
fun BazmoSquareTile(
    iconEmoji: String,
    title: String,
    active: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgStart by animateColorAsState(
        targetValue = if (active) Color(0xFFDBEAFE) else Color.White,
        animationSpec = tween(150),
        label = "tileBgStart"
    )
    val bgEnd by animateColorAsState(
        targetValue = if (active) Color(0xFFBFDBFE) else NavySoft,
        animationSpec = tween(150),
        label = "tileBgEnd"
    )
    val borderCol by animateColorAsState(
        targetValue = if (active) Navy else Color(0xFFD5E3F7),
        animationSpec = tween(150),
        label = "tileBorder"
    )
    val bgBrush = Brush.linearGradient(listOf(bgStart, bgEnd))
    val borderWidth = if (active) 2.dp else 1.5.dp

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(18.dp))
            .background(bgBrush)
            .border(borderWidth, borderCol, RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = iconEmoji, fontSize = 26.sp)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = NavyDeep,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun BazmoWarrantyBox(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = SoftSurface,
        shape = RoundedCornerShape(11.dp),
        border = BorderStroke(1.dp, NavyLight.copy(alpha = 0.55f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = NavyDeep,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun BazmoFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val containerColor by animateColorAsState(
        targetValue = if (selected) Navy else CardSurface,
        animationSpec = tween(150),
        label = "chipContainer"
    )
    val borderColor by animateColorAsState(
        targetValue = if (selected) Navy else BorderLight,
        animationSpec = tween(150),
        label = "chipBorder"
    )
    val labelColor by animateColorAsState(
        targetValue = if (selected) Color.White else TextMuted,
        animationSpec = tween(150),
        label = "chipLabel"
    )
    Surface(
        color = containerColor,
        shape = RoundedCornerShape(99.dp),
        border = BorderStroke(1.5.dp, borderColor),
        modifier = Modifier
            .heightIn(min = 38.dp)
            .clip(RoundedCornerShape(99.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = labelColor
            )
        }
    }
}

@Composable
fun BazmoChip(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) = BazmoFilterChip(label = text, selected = selected, onClick = onClick)

@Composable
fun BazmoPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    enabled: Boolean = true,
    testTag: String? = null
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Navy, contentColor = Color.White),
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp, vertical = 6.dp) else PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier
            .heightIn(min = if (compact) 38.dp else 48.dp)
            .let { if (testTag != null) it.testTag(testTag) else it }
    ) {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BazmoSoftButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = NavySoft, contentColor = Navy),
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp, vertical = 6.dp) else PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier.heightIn(min = if (compact) 38.dp else 46.dp)
    ) {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BazmoGoldButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = GoldAccent, contentColor = Color(0xFF111827)),
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp, vertical = 6.dp) else PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier.heightIn(min = if (compact) 38.dp else 46.dp)
    ) {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BazmoAccentButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = AccentCyanDark, contentColor = Color.White),
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp, vertical = 6.dp) else PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier.heightIn(min = if (compact) 38.dp else 46.dp)
    ) {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BazmoSuccessButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen, contentColor = Color.White),
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp, vertical = 6.dp) else PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier.heightIn(min = if (compact) 38.dp else 46.dp)
    ) {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BazmoDangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false,
    enabled: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp, vertical = 6.dp) else PaddingValues(horizontal = 14.dp, vertical = 10.dp),
        modifier = modifier.heightIn(min = if (compact) 38.dp else 46.dp)
    ) {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BazmoOutlineButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    OutlinedButton(
        onClick = onClick,
        shape = RoundedCornerShape(if (compact) 10.dp else 12.dp),
        border = BorderStroke(1.5.dp, NavyLight),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Navy),
        contentPadding = if (compact) PaddingValues(horizontal = 12.dp, vertical = 6.dp) else PaddingValues(horizontal = 16.dp, vertical = 10.dp),
        modifier = modifier.heightIn(min = if (compact) 38.dp else 48.dp)
    ) {
        Text(
            text = text,
            style = if (compact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun BazmoEmptyState(
    message: String,
    modifier: Modifier = Modifier
) {
    BazmoCard(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Filled.Inbox,
                contentDescription = null,
                tint = TextMuted,
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun BazmoOrderStepper(status: String) {
    val steps = listOf("ارسال شده به فروشگاه", "در حال پردازش", "در حال ارسال")
    val curIdx = steps.indexOf(status).coerceAtLeast(0)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        steps.forEachIndexed { idx, step ->
            val bg by animateColorAsState(
                targetValue = when {
                    idx < curIdx -> SuccessSoft
                    idx == curIdx -> Navy
                    else -> SoftSurface
                },
                animationSpec = tween(180),
                label = "stepBg"
            )
            val fg by animateColorAsState(
                targetValue = when {
                    idx < curIdx -> SuccessText
                    idx == curIdx -> Color.White
                    else -> TextMuted
                },
                animationSpec = tween(180),
                label = "stepFg"
            )
            Surface(
                color = bg,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = step,
                    color = fg,
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 7.dp),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
