package com.rotaplay.app

import android.content.Context
import android.content.Intent
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap

data class AppEntry(val label: String, val pkg: String, val icon: ImageBitmap)

fun loadApps(ctx: Context): List<AppEntry> {
    val pm = ctx.packageManager
    val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
    return pm.queryIntentActivities(intent, 0)
        .filter { it.activityInfo.packageName != ctx.packageName }
        .map {
            AppEntry(
                label = it.loadLabel(pm).toString(),
                pkg = it.activityInfo.packageName,
                icon = it.loadIcon(pm).toBitmap(128, 128).asImageBitmap()
            )
        }
        .distinctBy { it.pkg }
        .sortedBy { it.label.lowercase() }
}

fun launchApp(ctx: Context, pkg: String) {
    ctx.packageManager.getLaunchIntentForPackage(pkg)?.let {
        it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        ctx.startActivity(it)
    }
}
