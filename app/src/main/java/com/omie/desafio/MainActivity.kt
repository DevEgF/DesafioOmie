package com.omie.desafio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.omie.desafio.core.designsystem.OmieDesafioTheme
import com.omie.desafio.core.domain.AnalyticsTracker
import com.omie.desafio.navigation.OmieNavHost
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var analyticsTracker: AnalyticsTracker

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OmieDesafioTheme {
                val navController = rememberNavController()
                OmieNavHost(navController = navController, analyticsTracker = analyticsTracker)
            }
        }
    }
}
