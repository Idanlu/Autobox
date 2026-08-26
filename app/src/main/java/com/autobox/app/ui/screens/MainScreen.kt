package com.autobox.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.autobox.app.ui.theme.DarkBackground
import com.autobox.app.ui.theme.DarkSurface
import com.autobox.app.ui.theme.OrangePrimary
import com.autobox.app.ui.theme.TextMuted
import com.autobox.app.ui.theme.TextPrimary
import com.autobox.app.ui.viewmodels.AuthViewModel
import com.autobox.app.ui.viewmodels.RulesViewModel
import com.autobox.app.ui.viewmodels.ScheduleViewModel
import com.autobox.app.ui.viewmodels.SettingsViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    SCHEDULE("Schedule", Icons.Default.CalendarMonth),
    RULES("Rules", Icons.Default.Alarm),
    LOGS("Logs", Icons.Default.History),
    SETTINGS("Settings", Icons.Default.Settings),
    ACCOUNT("Account", Icons.Default.AccountCircle)
}

@Composable
fun MainScreen(
    authViewModel: AuthViewModel = viewModel(),
    scheduleViewModel: ScheduleViewModel = viewModel(),
    rulesViewModel: RulesViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val authState by authViewModel.uiState.collectAsState()
    var selectedTab by remember { mutableIntStateOf(if (authState.isLoggedIn) NavigationTab.SCHEDULE.ordinal else NavigationTab.ACCOUNT.ordinal) }

    Scaffold(
        containerColor = DarkBackground,
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 0.dp
            ) {
                NavigationTab.values().forEachIndexed { index, tab ->
                    val isSelected = selectedTab == index
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = tab.title,
                                modifier = Modifier.size(22.dp)
                            )
                        },
                        label = {
                            Text(
                                text = tab.title,
                                fontSize = 11.sp,
                                color = if (isSelected) OrangePrimary else TextMuted
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = OrangePrimary,
                            unselectedIconColor = TextMuted,
                            indicatorColor = OrangePrimary.copy(alpha = 0.15f)
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (NavigationTab.values()[selectedTab]) {
                NavigationTab.SCHEDULE -> {
                    if (!authState.isLoggedIn) {
                        AuthScreen(
                            viewModel = authViewModel,
                            onLoginSuccess = { selectedTab = NavigationTab.SCHEDULE.ordinal }
                        )
                    } else {
                        ScheduleScreen(viewModel = scheduleViewModel)
                    }
                }
                NavigationTab.RULES -> {
                    RulesScreen(viewModel = rulesViewModel)
                }
                NavigationTab.LOGS -> {
                    LogsScreen(viewModel = settingsViewModel)
                }
                NavigationTab.SETTINGS -> {
                    SettingsScreen(viewModel = settingsViewModel)
                }
                NavigationTab.ACCOUNT -> {
                    AuthScreen(
                        viewModel = authViewModel,
                        onLoginSuccess = { selectedTab = NavigationTab.SCHEDULE.ordinal }
                    )
                }
            }
        }
    }
}
