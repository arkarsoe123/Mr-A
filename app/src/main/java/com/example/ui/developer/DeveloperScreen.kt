package com.example.ui.developer

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.PrimaryPurple
import com.example.ui.theme.SurfaceColor

private const val SUPPORT_NUMBER = "09691529743"
private const val GITHUB_URL = "https://github.com/arkarsoe123/Mr-A"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperScreen(onNavigateBack: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    val uriHandler = LocalUriHandler.current
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Developer & Support") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = androidx.compose.ui.res.painterResource(R.drawable.developer_profile),
                contentDescription = "Developer profile",
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(128.dp).clip(CircleShape)
            )
            Spacer(Modifier.height(14.dp))
            Text("Arkarsoe", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Developer of Mr.A AI", color = PrimaryBlue)
            Spacer(Modifier.height(18.dp))

            Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("About Mr.A", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Mr.A သည် လက်ရှိ စမ်းသပ်ဖွံ့ဖြိုးနေသော AI Assistant project ဖြစ်ပါတယ်။ " +
                            "မြန်မာဘာသာကို အဓိကထားပြီး AI chat, voice, smart UI cards နဲ့ နောက်ထပ် phone-assistant features များကို တဖြည်းဖြည်း တိုးချဲ့သွားမယ်။"
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Favorite, null, tint = MaterialTheme.colorScheme.error)
                        Spacer(Modifier.width(8.dp))
                        Text("Support Mr.A", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "ပိုမိုကောင်းမွန်တဲ့ AI features, API/server costs နဲ့ development ကို ဆက်လက်လုပ်ဆောင်နိုင်ဖို့ အထောက်အပံ့လိုအပ်ပါတယ်။ အခုအချိန်မှာ local support အနေနဲ့ KPay / Wave Money ကို အသုံးပြုနိုင်ပါတယ်။"
                    )
                    Spacer(Modifier.height(14.dp))
                    PaymentRow("KPay", SUPPORT_NUMBER, clipboard)
                    Spacer(Modifier.height(10.dp))
                    PaymentRow("Wave Money", SUPPORT_NUMBER, clipboard)
                    Spacer(Modifier.height(12.dp))
                    Text(
                        "Online payment integration: နောက်ထပ် version တွေမှာ provider ရွေးချယ်ပြီး secure payment flow ထည့်သွင်းနိုင်အောင် architecture ကို ပြင်ဆင်ထားပါတယ်။",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(14.dp))
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Development Status", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    StatusRow("AI Chat", true)
                    StatusRow("Voice input / output", true)
                    StatusRow("Smart UI Card system", true)
                    StatusRow("Keyboard-safe chat UI", true)
                    StatusRow("Developer profile / support", true)
                    StatusRow("Online payment provider", false)
                    StatusRow("Advanced phone actions", false)
                }
            }

            Spacer(Modifier.height(14.dp))
            Card(colors = CardDefaults.cardColors(containerColor = SurfaceColor), shape = RoundedCornerShape(22.dp)) {
                Column(Modifier.padding(18.dp)) {
                    Text("Project", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Button(onClick = { uriHandler.openUri(GITHUB_URL) }, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.Code, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Open GitHub Project")
                    }
                    OutlinedButton(
                        onClick = {
                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$SUPPORT_NUMBER"))
                            context.startActivity(intent)
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Phone, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Contact: $SUPPORT_NUMBER")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                "ကျေးဇူးတင်ပါတယ် ❤️\nTogether, we build a better Mr.A.",
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PaymentRow(label: String, number: String, clipboard: androidx.compose.ui.platform.ClipboardManager) {
    Surface(color = MaterialTheme.colorScheme.background, shape = RoundedCornerShape(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.Star, null, tint = PrimaryPurple)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(label, fontWeight = FontWeight.Bold)
                Text("Arkarsoe • $number", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            IconButton(onClick = { clipboard.setText(AnnotatedString(number)) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy number")
            }
        }
    }
}

@Composable
private fun StatusRow(label: String, done: Boolean) {
    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(
            if (done) Icons.Default.Star else Icons.Default.Link,
            null,
            tint = if (done) PrimaryBlue else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(label)
    }
}
