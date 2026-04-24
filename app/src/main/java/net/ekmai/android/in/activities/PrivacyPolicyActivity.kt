package net.ekmai.android.`in`.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.Api
import androidx.compose.material.icons.outlined.CloudOff
import androidx.compose.material.icons.outlined.DeleteOutline
import androidx.compose.material.icons.outlined.FreeBreakfast
import androidx.compose.material.icons.outlined.MailOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.VerifiedUser
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import net.ekmai.android.`in`.R
import net.ekmai.android.`in`.ui.theme.EkmAITheme
import net.ekmai.android.`in`.ui.theme.ThemeOption
import net.ekmai.android.`in`.ui.theme.ThemeViewModel
import net.ekmai.android.`in`.utilities.ModelsManager
import kotlin.getValue

fun Activity.launchPrivacyPolicy() {
    startActivity(Intent(this, PrivacyPolicyActivity::class.java))
    overridePendingTransition(R.anim.slide_up_in, R.anim.fade_out_slow)
}

private fun Activity.finishWithAnim() {
    finish()
    overridePendingTransition(R.anim.fade_in_slow, R.anim.slide_down_out)
}

class PrivacyPolicyActivity : ComponentActivity() {
    private val viewModel by viewModels<ThemeViewModel> {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return ThemeViewModel(this@PrivacyPolicyActivity) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        onBackPressedDispatcher.addCallback(this) { finishWithAnim() }
        setContent {
            val theme by viewModel.theme.collectAsState()
            EkmAITheme(theme) { PrivacyPolicyScreen(onBack = { finishWithAnim() }) }
        }
    }
}

private data class PolicySection(
    val icon: ImageVector,
    val title: String,
    val color: Color,
    val points: List<String>
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrivacyPolicyScreen(onBack: () -> Unit = {}) {
    val accent = ModelsManager.getCurrentModel().tintColor

    val sections = listOf(
        PolicySection(
            Icons.Outlined.FreeBreakfast, "Ease of services", Color(0xFFAD123F),
            listOf(
                "No Logins: You really don't need to login when you want to ask anything to ai",
                "Team Support: Our team is continuously working to provide best app services"
            )
        ),
        PolicySection(
            Icons.Outlined.Storage, "Data We Store", Color(0xFF185FA5),
            listOf(
                "Chat History: Saved in a temporary linked-list which automatically get's deleted after closing session; it is never uploaded to our servers.",
                "User Profiles: No account, email, or sign-in is required to use the app."
            )
        ),
        PolicySection(
            Icons.Outlined.CloudOff, "Data We Don't Collect", Color(0xFF0F6E56),
            listOf(
                "We do not collect personal identifiers (IP addresses, names, or device IDs).",
                "We do not track usage, sessions, or analytics.",
                "We do not share any data with third parties.",
                "We have no servers — your data never leaves your device."
            )
        ),
        PolicySection(
            Icons.Outlined.Api, "Third-Party APIs", Color(0xFF854F0B),
            listOf(
                "When chatting, your prompts are sent directly to the AI provider you chose (e.g., OpenAI, Google).",
                "These providers process data according to their own privacy policies.",
                "The app acts only as a client interface between you and the AI provider."
            )
        ),
        PolicySection(
            Icons.Outlined.DeleteOutline, "Your Rights", Color(0xFF993556),
            listOf(
                "You can delete all data by clearing the app's storage in Android settings.",
                "You can remove individual API keys from the Keys settings page.",
                "Uninstalling the app removes all stored data permanently."
            )
        ),
        PolicySection(
            Icons.Outlined.Shield, "Children's Privacy", Color(0xFF4A6572),
            listOf(
                "Our services are not directed to children under 13.",
                "We do not knowingly collect data from children.",
                "Since all data is local, parents can delete it by clearing app storage."
            )
        ),
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Privacy Policy", fontSize = 20.sp, fontWeight = FontWeight.Medium)
                        Text("Last updated: April 2026", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
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
                    shape = RoundedCornerShape(20.dp),
                    color = accent.copy(alpha = 0.09f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(48.dp).clip(CircleShape)
                                .background(accent.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.VerifiedUser, null, tint = accent, modifier = Modifier.size(24.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Privacy-first by design", fontSize = 15.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text(
                                "ekm AI is built with a simple principle: your data belongs to you. We store nothing on our servers because we have none.",
                                fontSize = 12.sp, lineHeight = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            items(sections) { section ->
                PolicySectionCard(section)
            }

            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF185FA5).copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Outlined.MailOutline, null, tint = Color(0xFF185FA5), modifier = Modifier.size(18.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Questions?", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
                            Text("contact@ekmlabs.dev", fontSize = 12.sp, color = accent)
                        }
                        Icon(Icons.AutoMirrored.Outlined.OpenInNew, null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.size(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PolicySectionCard(section: PolicySection) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(section.color.copy(alpha = 0.07f))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Box(
                    modifier = Modifier.size(32.dp).clip(RoundedCornerShape(8.dp))
                        .background(section.color.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(section.icon, null, tint = section.color, modifier = Modifier.size(16.dp))
                }
                Text(section.title, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MaterialTheme.colorScheme.onSurface)
            }
            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                section.points.forEachIndexed { i, point ->
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier.padding(top = 5.dp).size(6.dp).clip(CircleShape)
                                .background(section.color.copy(alpha = 0.5f))
                        )
                        Text(point, fontSize = 13.sp, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
                    }
                    if (i < section.points.lastIndex) {
                        HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outlineVariant)
                    }
                }
            }
        }
    }
}

@Preview(showSystemUi = true)
@Composable
fun PreviewPolicy() {
    EkmAITheme(
        ThemeOption.DARK
    ) {
        PrivacyPolicyScreen {

        }
    }
}