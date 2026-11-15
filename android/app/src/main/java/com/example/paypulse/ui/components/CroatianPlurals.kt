package com.example.paypulse.ui.components

import android.content.res.Configuration
import androidx.annotation.PluralsRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.pluralStringResource

@Composable
fun croatianPluralStringResource(@PluralsRes id: Int, count: Int, vararg formatArgs: Any): String {
    if (LocalInspectionMode.current) {
        return pluralStringResource(id, count, *formatArgs)
    }
    val context = LocalContext.current
    val configuration = LocalConfiguration.current
    val resources = remember(context, configuration) {
        val croatian = Configuration(configuration).apply { setLocale(CroatianLocale) }
        context.createConfigurationContext(croatian).resources
    }
    return resources.getQuantityString(id, count, *formatArgs)
}
