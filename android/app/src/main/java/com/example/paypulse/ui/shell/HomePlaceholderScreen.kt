package com.example.paypulse.ui.shell

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.paypulse.ui.components.PayPulseTopBar

@Composable
fun HomePlaceholderScreen(onMenuClick: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(modifier = modifier, topBar = { PayPulseTopBar(onMenuClick = onMenuClick) }) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding))
    }
}
