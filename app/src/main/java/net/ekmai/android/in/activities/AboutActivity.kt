package net.ekmai.android.`in`.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Code
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Stream
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.ekmai.android.`in`.R
import net.ekmai.android.`in`.ui.theme.EkmAITheme
import net.ekmai.android.`in`.ui.theme.ThemeOption
import net.ekmai.android.`in`.ui.theme.ThemeViewModel
import net.ekmai.android.`in`.utilities.ModelsManager
import kotlin.getValue

fun Activity.launchAbout() {
    startActivity(Intent(this, AboutActivity::class.java))
    overridePendingTransition(R.anim.slide_up_in, R.anim.fade_out_slow)
}

private fun Activity.finishWithAnim() {
    finish()
    overridePendingTransition(R.anim.fade_in_slow, R.anim.slide_down_out)
}

class AboutActivity : ComponentActivity() {
    private val viewModel by viewModels<ThemeViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ThemeViewModel(this@AboutActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(this) { finishWithAnim() }
        setContent {
            val theme by viewModel.theme.collectAsState()
            EkmAITheme(
                themeOption = theme
            ) {
                AboutScreen(onBack = { finishWithAnim() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onBack: () -> Unit = {}) {
    val accent = ModelsManager.getCurrentModel().tintColor

    val features = listOf(
        FeatureItem(Icons.Outlined.AutoAwesome,   "Multi-Model AI",     "Switch between Gemini, GPT, and more",   Color(0xFF534AB7)),
        FeatureItem(Icons.Outlined.Stream,         "Personalise Support",  "Personalise the responses according to you", Color(0xFF0F6E56)),
        FeatureItem(Icons.Outlined.Code,           "Markdown Rendering", "Code blocks, bold, italic and more",      Color(0xFF185FA5)),
        FeatureItem(Icons.Outlined.Psychology,     "Reasoning Mode",     "See the model's thought process",         Color(0xFF854F0B)),
        FeatureItem(Icons.Outlined.Palette,        "Expressive UI",      "Material You with per-model accent colors",Color(0xFF3B6D11)),
    )

    val team = listOf(
        TeamMember("EK", "Ekam Labs", "Creator & Developer",
            listOf(Color(0xFFAFA9EC), Color(0xFFD4537E))),
    )

    val interactionSource = remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.18f else 0.10f,
        label = "barAlpha"
    )
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("About", fontSize = 20.sp, fontWeight = FontWeight.Medium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        containerColor = MaterialTheme.colorScheme.surface
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(
                top = padding.calculateTopPadding() + 8.dp,
                bottom = padding.calculateBottomPadding() + 32.dp,
                start = 16.dp, end = 16.dp
            ),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {

            item {
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = ModelsManager.getCurrentModel().tintColor.copy(bgAlpha),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Animated logo
                        val infiniteTransition = rememberInfiniteTransition(label = "logo")
                        val glowAlpha by infiniteTransition.animateFloat(
                            initialValue = 0.3f, targetValue = 0.7f,
                            animationSpec = infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                            label = "glow"
                        )
                        Box(contentAlignment = Alignment.Center) {
                            Box(
                                modifier = Modifier
                                    .size(88.dp)
                                    .clip(CircleShape)
                                    .background(accent.copy(alpha = glowAlpha * 0.2f))
                            )
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(
                                        Brush.linearGradient(
                                            listOf(Color(0xFFAFA9EC), Color(0xFFD4537E))
                                        )
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "ekm",
                                    fontSize = 22.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "ekm AI",
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Your intelligent companion",
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(accent.copy(alpha = 0.12f))
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "Version 1.0.0  ·  Build 100",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = accent
                            )
                        }

                        Text(
                            text = "A powerful, privacy-first AI chat app built with Jetpack Compose. Supports multiple AI providers with a beautiful, expressive interface.",
                            fontSize = 13.sp,
                            lineHeight = 20.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        Triple("9+", "AI Models", Icons.Outlined.AutoAwesome),
                        Triple("100%", "Open Source", Icons.Outlined.Code),
                        Triple("0", "Ads", Icons.Filled.Block),
                    ).forEach { (value, label, icon) ->
                        StatCard(
                            value = value,
                            label = label,
                            icon = icon,
                            accent = accent,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            item {
                AboutSectionLabel("Features")
            }
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ModelsManager.getCurrentModel().tintColor.copy(bgAlpha),
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        features.forEachIndexed { i, feat ->
                            FeatureRow(feat)
                            if (i < features.lastIndex) {
                                HorizontalDivider(
                                    thickness = 0.5.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    modifier = Modifier.padding(start = 62.dp)
                                )
                            }
                        }
                    }
                }
            }

            item { AboutSectionLabel("Made by") }
            item {
                team.forEach { member ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ModelsManager.getCurrentModel().tintColor.copy(bgAlpha),
                        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
                        onClick = {
                            val intent = Intent()
                            intent.action = Intent.ACTION_VIEW
                            intent.data = "https://github.com/ekam-labs".toUri()
                            context.startActivity(intent)
                        }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(member.gradient)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(member.initials, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(member.name, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                                Text(member.role, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Icon(
                                Icons.AutoMirrored.Outlined.OpenInNew,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "Made with ♥ in India · ekm AI © 2026",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
private fun StatCard(value: String, label: String, icon: ImageVector, accent: Color, modifier: Modifier) {
    val interactionSource = remember {
        androidx.compose.foundation.interaction.MutableInteractionSource()
    }
    val isPressed by interactionSource.collectIsPressedAsState()
    val bgAlpha by animateFloatAsState(
        targetValue = if (isPressed) 0.18f else 0.10f,
        label = "barAlpha"
    )
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = ModelsManager.getCurrentModel().tintColor.copy(bgAlpha),
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Text(value, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Text(label, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

private data class FeatureItem(val icon: ImageVector, val title: String, val desc: String, val color: Color)
private data class TeamMember(val initials: String, val name: String, val role: String, val gradient: List<Color>)

@Composable
private fun FeatureRow(feat: FeatureItem) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                .background(feat.color.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(feat.icon, null, tint = feat.color, modifier = Modifier.size(18.dp))
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(feat.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            Text(feat.desc, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun AboutSectionLabel(text: String) {
    Text(
        text = text.uppercase(),
        fontSize = 11.sp, fontWeight = FontWeight.Medium,
        letterSpacing = 0.08.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
        modifier = Modifier.padding(start = 4.dp, top = 4.dp, bottom = 2.dp)
    )
}

@Preview(showSystemUi = true)
@Composable
fun PreviewAbout() {
    EkmAITheme(
        ThemeOption.DARK
    ) {
        AboutScreen {

        }
    }
}