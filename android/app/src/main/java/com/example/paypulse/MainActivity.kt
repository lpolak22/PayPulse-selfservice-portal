package com.example.paypulse

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.paypulse.ui.app.AppRoute
import com.example.paypulse.ui.app.AppViewModel
import com.example.paypulse.ui.theme.PayPulseTheme

class MainActivity : ComponentActivity() {

    private val appViewModel: AppViewModel by viewModels { AppViewModel.Factory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (savedInstanceState == null) appViewModel.onActivationLink(intent?.dataString)
        setContent {
            PayPulseTheme {
                AppRoute(viewModel = appViewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        appViewModel.onActivationLink(intent.dataString)
    }
}
