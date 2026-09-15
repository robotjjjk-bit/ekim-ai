package net.ekmai.android.`in`.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import net.ekmai.android.`in`.utilities.ModelInfo
import net.ekmai.android.`in`.utilities.ModelsManager

@Composable
fun MessageField(
    modifier: Modifier = Modifier,
    selectedModel: ModelInfo = ModelsManager.getCurrentModel(),
    onModelClick: () -> Unit = {},
    onSend: (String) -> Unit = {},
    onVoice: () -> Unit = {},
    isError: Boolean = false,
    errorMessage: String = "Something went wrong. Please try again.",
    placeholder: String = "Message",
) {
    var text by remember { mutableStateOf("") }
    val hasText = text.isNotBlank()
    val keyboardController = LocalSoftwareKeyboardController.current

    val borderColor by animateColorAsState(
        targetValue = when {
            isError -> MaterialTheme.colorScheme.error
            hasText -> selectedModel.tintColor
            else    -> MaterialTheme.colorScheme.outlineVariant
        },
        animationSpec = tween(300),
        label = "borderColor"
    )

    Column(modifier = modifier) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            tonalElevation = 2.dp,
            shadowElevation = 8.dp,
            color = LiquidGlass.surfaceColor(),
            modifier = Modifier
                .fillMaxWidth()
                .border(
                    width = 1.5.dp,
                    color = borderColor,
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Column {
                // Liquid Glass: refleksi tepi atas, murni visual, tanpa ubah logika input.
                GlassTopHighlight(cornerRadius = 24.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 8.dp, end = 8.dp, top = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextFieldWithLabel(
                        value = text,
                        onValueChange = { text = it },
                        placeholder = placeholder,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 16.dp, bottom = 8.dp, top = 8.dp),
                        onDone = {
                            if (hasText) {
                                onSend(text)
                                text = ""
                                keyboardController?.hide()
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .padding(bottom = 6.dp, end = 4.dp)
                            .size(50.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = !hasText,
                            enter = scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessLow
                                )
                            ) + fadeIn(tween(200)),
                            exit = scaleOut(
                                animationSpec = tween(180, easing = FastOutLinearInEasing)
                            ) + fadeOut(tween(150))
                        ) {
                            AnimatedIconButton(
                                onClick = onVoice,
                                containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                                contentColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Mic,
                                    contentDescription = "Voice input",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = hasText,
                            enter = scaleIn(
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            ) + fadeIn(tween(180)),
                            exit = scaleOut(tween(150)) + fadeOut(tween(120))
                        ) {
                            AnimatedIconButton(
                                onClick = {
                                    onSend(text)
                                    text = ""
                                    keyboardController?.hide()
                                },
                                containerColor = selectedModel.tintColor,
                                contentColor = Color.White,
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.ArrowUpward,
                                    contentDescription = "Send message",
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                ModelIndicatorBar(
                    model = selectedModel,
                    onClick = onModelClick
                )
            }
        }

        AnimatedVisibility(
            visible = isError,
            enter = slideInVertically(initialOffsetY = { -it / 2 }) + fadeIn(),
            exit  = slideOutVertically(targetOffsetY = { -it / 2 }) + fadeOut()
        ) {
            Row(
                modifier = Modifier.padding(start = 16.dp, top = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.ErrorOutline,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun BasicTextFieldWithLabel(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    onDone: () -> Unit = {}
) {
    androidx.compose.foundation.text.BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        textStyle = TextStyle(
            color = MaterialTheme.colorScheme.onSurface,
            fontSize = 15.sp,
            lineHeight = 22.sp
        ),
        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            imeAction = ImeAction.Send
        ),
        keyboardActions = KeyboardActions(onSend = { onDone() }),
        maxLines = 6,
        decorationBox = { innerTextField ->
            Box {
                if (value.isEmpty()) {
                    Text(
                        text = placeholder,
                        style = TextStyle(
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 15.sp
                        )
                    )
                }
                innerTextField()
            }
        }
    )
}

@Composable
private fun AnimatedIconButton(
    onClick: () -> Unit,
    containerColor: Color,
    contentColor: Color,
    content: @Composable () -> Unit
) {
    val interactionSource = remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "buttonScale"
    )

    Box(
        modifier = Modifier
            .size(40.dp)
            .scale(scale)
            .shadow(6.dp, CircleShape, clip = false)
            .clip(CircleShape)
            .background(containerColor)
            .glassCircleBorder()
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides contentColor) {
            content()
        }
    }
}

@Composable
private fun ModelIndicatorBar(
    model: ModelInfo,
    onClick: () -> Unit
) {
    val interactionSource = remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.18f else 0.10f,
        label = "barAlpha"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = model.tintColor.copy(alpha = bgAlpha),
                shape = RoundedCornerShape(bottomStart = 23.dp, bottomEnd = 23.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 9.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(model.tintColor)
        )
        Text(
            text = model.name,
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.Medium,
                color = model.tintColor,
                fontSize = 11.sp,
                letterSpacing = 0.3.sp
            )
        )
        Spacer(modifier = Modifier.weight(1f))
        Icon(
            imageVector = Icons.Rounded.UnfoldMore,
            contentDescription = "Switch model",
            tint = model.tintColor.copy(alpha = 0.7f),
            modifier = Modifier.size(14.dp)
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun CustomMessageFieldPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            MessageField(
                selectedModel = ModelsManager.getCurrentModel(),
                onModelClick = {},
                onSend = {},
                onVoice = {}
            )
        }
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFF5F5F5)
@Composable
fun CustomMessageFieldErrorPreview() {
    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.BottomCenter
        ) {
            MessageField(
                selectedModel = ModelsManager.getCurrentModel(),
                isError = true,
                errorMessage = "Network error. Please check your connection.",
                onSend = {},
                onVoice = {}
            )
        }
    }
}