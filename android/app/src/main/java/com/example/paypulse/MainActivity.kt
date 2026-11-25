package com.example.paypulse

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.paypulse.data.AppContainer
import com.example.paypulse.domain.accessibility.AccessibilitySettings
import com.example.paypulse.ui.app.AppRoute
import com.example.paypulse.ui.app.AppViewModel
import com.example.paypulse.ui.theme.PayPulseTheme

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels { AppViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) appViewModel.onAppLink(intent?.dataString)
        setContent {
            val accessibility by AppContainer.accessibilitySettingsRepository.settings
                .collectAsStateWithLifecycle(AccessibilitySettings())
            PayPulseTheme(accessibility = accessibility) {
                AppRoute(viewModel = appViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        appViewModel.onAppLink(intent.dataString)
    }
}
