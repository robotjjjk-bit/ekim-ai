package net.ekmai.android.`in`.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.ekmai.android.`in`.R
import net.ekmai.android.`in`.ui.theme.EkmAITheme
import net.ekmai.android.`in`.ui.theme.ThemeManager
import net.ekmai.android.`in`.utilities.ApiManager
import net.ekmai.android.`in`.utilities.ModelsManager
import net.ekmai.android.`in`.utilities.PromptPreference
import net.ekmai.android.`in`.ui.theme.ThemeViewModel
import kotlin.getValue

fun Activity.launchSystemPrompt() {
    startActivity(Intent(this, SystemPromptActivity::class.java))
    overridePendingTransition(R.anim.slide_up_in, R.anim.fade_out_slow)
}

class SystemPromptActivity : ComponentActivity() {
    private val viewModel by viewModels<ThemeViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ThemeViewModel(this@SystemPromptActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(this) { finishWithAnimation() }
        ThemeManager.themeState.value = net.ekmai.android.`in`.ui.theme.getTheme(this)
        setContent {
            val theme by ThemeManager.themeState.collectAsState()
            EkmAITheme(
                themeOption = theme
            ) {
                SystemPromptScreen(onBack = { finishWithAnimation() })
            }
        }
    }
    private fun finishWithAnimation() {
        finish()
        overridePendingTransition(R.anim.fade_in_slow, R.anim.slide_down_out)
    }
}

private data class PromptTemplate(
    val label: String,
    val icon: ImageVector,
    val prompt: String,
    val color: Color
)

private val Purple600 = Color(0xFF534AB7)
private val Teal600   = Color(0xFF0F6E56)
private val Amber600  = Color(0xFF854F0B)
private val Blue600   = Color(0xFF185FA5)
private val Coral600  = Color(0xFF993C1D)
private val Pink600   = Color(0xFF993556)

private val templates = listOf(
    PromptTemplate(
        label = "Default",
        icon = Icons.Outlined.AutoAwesome,
        color = Purple600,
        prompt = "You are a helpful assistant. Respond directly and concisely.\n" +
                "Do NOT explain your reasoning process, do NOT repeat conversation history,\n" +
                "do NOT use bullet points to analyze the conversation. Just respond naturally."
    ),
    PromptTemplate(
        label = "Coder",
        icon = Icons.Outlined.Code,
        color = Blue600,
        prompt = "You are an expert software engineer. Provide clean, well-commented code.\n" +
                "Always explain what the code does after providing it.\n" +
                "Prefer concise, idiomatic solutions. Point out potential bugs or improvements."
    ),
    PromptTemplate(
        label = "Teacher",
        icon = Icons.Outlined.School,
        color = Teal600,
        prompt = "You are a patient and knowledgeable teacher. Break down complex topics\n" +
                "into simple, digestible explanations. Use analogies and real-world examples.\n" +
                "Always check for understanding and encourage questions."
    ),
    PromptTemplate(
        label = "Writer",
        icon = Icons.Outlined.EditNote,
        color = Amber600,
        prompt = "You are a creative writing assistant. Help craft compelling narratives,\n" +
                "suggest vivid descriptions, and improve prose style.\n" +
                "Match the tone and voice the user establishes."
    ),
    PromptTemplate(
        label = "Concise",
        icon = Icons.Outlined.Compress,
        color = Coral600,
        prompt = "Reply in as few words as possible. No filler, no preamble.\n" +
                "Get straight to the point. Bullet points are fine when listing things.\n" +
                "If unsure, ask one short clarifying question."
    ),
    PromptTemplate(
        label = "Friend",
        icon = Icons.Outlined.Mood,
        color = Pink600,
        prompt = "You are a friendly, casual conversational partner. Use informal language,\n" +
                "be warm and supportive. Feel free to use light humor when appropriate.\n" +
                "Never be overly formal or robotic."
    )
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SystemPromptScreen(onBack: () -> Unit = {}) {
    val accentColor = ModelsManager.getCurrentModel().tintColor
    val context = LocalContext.current
    var promptText by remember { mutableStateOf(ApiManager.systemPrompt) }
    var savedText  by remember { mutableStateOf(ApiManager.systemPrompt) }
    val isDirty = promptText != savedText
    val wordCount = remember(promptText) {
        promptText.trim().split(Regex("\\s+")).count { it.isNotEmpty() }
    }
    val charCount = promptText.length

    val fieldInteraction = remember { MutableInteractionSource() }
    val isFocused by fieldInteraction.collectIsFocusedAsState()

    val borderColor by animateColorAsState(
        targetValue = when {
            isFocused -> accentColor
            else -> Color.Transparent
        },
        animationSpec = tween(250),
        label = "border"
    )

    var showResetDialog by remember { mutableStateOf(false) }
    var showDiscardDialog by remember { mutableStateOf(false) }

    BackHandler(enabled = isDirty) {
        showDiscardDialog = true
    }

    val handleBack = { if (isDirty) showDiscardDialog = true else onBack() }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard changes?") },
            text = { Text("You have unsaved changes to your system prompt. Are you sure you want to discard them?") },
            confirmButton = {
                TextButton(onClick = {
                    onBack()
                }) {
                    Text("Discard", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) {
                    Text("Keep Editing")
                }
            }
        )
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = {
                Icon(
                    Icons.Outlined.RestartAlt,
                    contentDescription = null,
                    tint = accentColor
                )
            },
            title = { Text("Reset to default?") },
            text = { Text("This will replace your current system prompt with the default one.") },
            confirmButton = {
                TextButton(onClick = {
                    promptText = templates.first().prompt
                    showResetDialog = false
                }) {
                    Text("Reset", color = accentColor)
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "System Prompt",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = ModelsManager.getCurrentModel().name,
                            fontSize = 12.sp,
                            color = accentColor,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = handleBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showResetDialog = true }) {
                        Icon(
                            Icons.Outlined.RestartAlt,
                            contentDescription = "Reset",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            BottomSaveBar(
                isDirty = isDirty,
                accentColor = accentColor,
                onSave = {
                    val pref = PromptPreference(context)
                    pref.saveData("prompt", promptText)
                    ApiManager.systemPrompt = promptText
                    savedText = promptText
                },
                onDiscard = {
                    promptText = savedText
                }
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->

        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 12.dp,
                bottom = padding.calculateBottomPadding() + 16.dp,
                start = 16.dp,
                end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                InfoCard(accentColor = accentColor)
            }

            item {
                SectionLabel(text = "Quick Templates")
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    items(templates) { template ->
                        TemplateChip(
                            template = template,
                            isActive = promptText.trim() == template.prompt.trim(),
                            onClick = { promptText = template.prompt }
                        )
                    }
                }
            }

            item {
                SectionLabel(text = "Editor")
            }
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.5.dp,
                            color = borderColor,
                            shape = RoundedCornerShape(16.dp)
                        )
                ) {
                    Column {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(accentColor.copy(alpha = 0.06f))
                                .padding(horizontal = 16.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isFocused) accentColor
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                            Text(
                                text = "system_prompt.txt",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(Modifier.weight(1f))
                            if (promptText.isNotEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .clickable { promptText = "" }
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "Clear",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        BasicTextField(
                            value = promptText,
                            onValueChange = { promptText = it },
                            modifier = Modifier
                                .fillMaxWidth()
                                .defaultMinSize(minHeight = 220.dp)
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            textStyle = TextStyle(
                                fontSize = 14.sp,
                                lineHeight = 22.sp,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                            ),
                            cursorBrush = SolidColor(accentColor),
                            interactionSource = fieldInteraction,
                            decorationBox = { inner ->
                                Box {
                                    if (promptText.isEmpty()) {
                                        Text(
                                            text = "Enter your system prompt here...\n\nTip: Be specific about tone, format, and behavior.",
                                            fontSize = 14.sp,
                                            lineHeight = 22.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                                        )
                                    }
                                    inner()
                                }
                            }
                        )

                        HorizontalDivider(
                            thickness = 0.5.dp,
                            color = MaterialTheme.colorScheme.outlineVariant
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            StatChip(label = "$wordCount words")
                            StatChip(label = "$charCount chars")
                            Spacer(Modifier.weight(1f))
                            if (isDirty) {
                                StatChip(
                                    label = "unsaved",
                                    color = Color(0xFFBA7517)
                                )
                            }
                        }
                    }
                }
            }

            item {
                SectionLabel(text = "Tips")
            }
            item {
                TipsCard(accentColor = accentColor)
            }
        }
    }
}

@Composable
private fun InfoCard(accentColor: Color) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = accentColor.copy(alpha = 0.08f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
            Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(
                    text = "What is a system prompt?",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "It's a hidden instruction sent before every conversation that shapes how the AI behaves — its tone, format, and personality.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TemplateChip(
    template: PromptTemplate,
    isActive: Boolean,
    onClick: () -> Unit
) {
    val bgAlpha by animateFloatAsState(
        targetValue = if (isActive) 0.18f else 0.08f,
        animationSpec = tween(200),
        label = "chipAlpha"
    )
    val borderAlpha by animateFloatAsState(
        targetValue = if (isActive) 1f else 0f,
        animationSpec = tween(200),
        label = "borderAlpha"
    )

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(template.color.copy(alpha = bgAlpha))
            .border(
                width = 1.5.dp,
                color = template.color.copy(alpha = borderAlpha),
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = template.icon,
                contentDescription = null,
                tint = template.color,
                modifier = Modifier.size(15.dp)
            )
            Text(
                text = template.label,
                fontSize = 13.sp,
                fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                color = template.color
            )
        }
    }
}

@Composable
private fun TipsCard(accentColor: Color) {
    val tips = listOf(
        "Be specific about tone — 'respond formally' beats 'be professional'",
        "Specify output format — 'use bullet points' or 'write in prose'",
        "Set boundaries — 'never use emojis' or 'always cite sources'",
        "Define persona — 'you are a senior iOS engineer with 10 years experience'"
    )

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(
            0.5.dp,
            MaterialTheme.colorScheme.outlineVariant
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            tips.forEachIndexed { index, tip ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.5f))
                    )
                    Text(
                        text = tip,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (index < tips.lastIndex) {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.padding(start = 16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomSaveBar(
    isDirty: Boolean,
    accentColor: Color,
    onSave: () -> Unit,
    onDiscard: () -> Unit
) {
    AnimatedVisibility(
        visible = isDirty,
        enter = fadeIn(tween(200)) + scaleIn(tween(200)),
        exit = fadeOut(tween(180)) + scaleOut(tween(180))
    ) {
        Surface(
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Discard
                OutlinedButton(
                    onClick = onDiscard,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                ) {
                    Icon(
                        Icons.Outlined.Close,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Discard", fontWeight = FontWeight.Medium)
                }

                Button(
                    onClick = onSave,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(2f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(
                        Icons.Outlined.Check,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("Save Prompt", fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        letterSpacing = 0.08.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun StatChip(label: String, color: Color = Color.Unspecified) {
    Text(
        text = label,
        fontSize = 11.sp,
        color = if (color == Color.Unspecified)
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        else color,
        fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
    )
}