package com.rotaplay.app

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppEntry(val label: String, val pkg: String, val icon: ImageBitmap, val category: String?)

private val CATEGORIES = listOf("Navigasyon", "Müzik", "Podcast & Kitap", "Mesajlaşma")

/** CarPlay / Android Auto'da desteklenen uygulama kategorileri */
private val KNOWN: Map<String, String> = buildMap {
    listOf(
        "com.google.android.apps.maps", "com.waze", "ru.yandex.yandexnavi",
        "ru.yandex.yandexmaps", "com.here.app.maps", "com.sygic.aura"
    ).forEach { put(it, "Navigasyon") }
    listOf(
        "com.spotify.music", "com.google.android.apps.youtube.music", "com.apple.android.music",
        "deezer.android.app", "com.amazon.mp3", "com.soundcloud.android", "com.aspiro.tidal",
        "com.pandora.android", "ru.yandex.music", "com.turkcell.gncplay", "org.videolan.vlc",
        "com.maxmpz.audioplayer"
    ).forEach { put(it, "Müzik") }
    listOf(
        "au.com.shiftyjelly.pocketcasts", "com.audible.application", "tunein.player",
        "fm.castbox.audiobook.radio.podcast", "com.google.android.apps.podcasts"
    ).forEach { put(it, "Podcast & Kitap") }
    listOf(
        "com.whatsapp", "org.telegram.messenger", "com.google.android.apps.messaging",
        "com.samsung.android.messaging", "com.facebook.orca"
    ).forEach { put(it, "Mesajlaşma") }
}

private fun iconOf(d: Drawable, size: Int): ImageBitmap {
    val bmp = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bmp)
    d.setBounds(0, 0, size, size)
    d.draw(canvas)
    return bmp.asImageBitmap()
}

fun loadApps(ctx: Context): List<AppEntry> {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .filter { it.activityInfo.packageName != ctx.packageName }
        .mapNotNull { ri ->
            runCatching {
                val pkg = ri.activityInfo.packageName
                AppEntry(ri.loadLabel(pm).toString(), pkg, iconOf(ri.loadIcon(pm), 96), KNOWN[pkg])
            }.getOrNull()
        }
        .distinctBy { it.pkg }
        .sortedBy { it.label.lowercase() }
}

fun launchApp(ctx: Context, pkg: String) {
    runCatching {
        ctx.packageManager.getLaunchIntentForPackage(pkg)?.let {
            it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            ctx.startActivity(it)
        }
    }
}

@Composable
fun AppsPage() {
    val ctx = LocalContext.current
    var all by remember { mutableStateOf<List<AppEntry>?>(null) }
    var showAll by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        all = withContext(Dispatchers.IO) { runCatching { loadApps(ctx) }.getOrDefault(emptyList()) }
    }

    val list = all
    if (list == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Yükleniyor…", color = Color.White.copy(alpha = 0.7f))
        }
        return
    }

    val carApps = list.filter { it.category != null }

    LazyVerticalGrid(
        columns = GridCells.Adaptive(110.dp),
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (!showAll) {
            item(span = { GridItemSpan(maxLineSpan) }, key = "title") {
                Text("CarPlay uygulamaları", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }
            if (carApps.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }, key = "empty") {
                    Text(
                        "Telefonunda CarPlay uyumlu uygulama bulunamadı (Spotify, Maps, Waze, WhatsApp vb.).",
                        color = Color.White.copy(alpha = 0.7f)
                    )
                }
            }
            CATEGORIES.forEach { cat ->
                val inCat = carApps.filter { it.category == cat }
                if (inCat.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }, key = "h_$cat") {
                        Text(cat, fontSize = 16.sp, color = Cyan, modifier = Modifier.padding(top = 8.dp))
                    }
                    items(inCat, key = { it.pkg }) { AppCell(it) { launchApp(ctx, it.pkg) } }
                }
            }
        } else {
            items(list, key = { it.pkg }) { AppCell(it) { launchApp(ctx, it.pkg) } }
        }
        item(span = { GridItemSpan(maxLineSpan) }, key = "toggle") {
            Box(
                Modifier
                    .padding(vertical = 8.dp)
                    .fillMaxWidth()
                    .height(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(CardColor)
                    .clickable { showAll = !showAll },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    if (showAll) "CarPlay uygulamalarına dön" else "Tüm uygulamalar",
                    color = Cyan, fontSize = 16.sp, fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun AppCell(app: AppEntry, onClick: () -> Unit) {
    Column(
        Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(app.icon, app.label, Modifier.size(64.dp))
        Spacer(Modifier.height(6.dp))
        Text(app.label, fontSize = 13.sp, maxLines = 1)
    }
}
