package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.ListAlt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.CookieDetailDialog
import com.example.ui.components.ResetConfirmationDialog
import com.example.ui.components.SensitiveValueWarningDialog
import com.example.ui.components.ToastNotificationOverlay
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.ExportScreen
import com.example.ui.screens.ExplorerScreen
import com.example.ui.screens.ImportScreen
import com.example.ui.screens.PrivacyAboutScreen
import com.example.ui.screens.ReportsScreen
import com.example.ui.theme.CookieManagerTheme
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Rose400

data class NavItem(
  val screen: AppScreen,
  val label: String,
  val icon: ImageVector,
  val testTag: String
)

val NAV_ITEMS = listOf(
  NavItem(AppScreen.DASHBOARD, "Dashboard", Icons.Default.Dashboard, "nav_dashboard"),
  NavItem(AppScreen.IMPORT, "Import", Icons.Default.FileUpload, "nav_import"),
  NavItem(AppScreen.EXPLORER, "Explorer", Icons.Default.ListAlt, "nav_explorer"),
  NavItem(AppScreen.CONVERT_EXPORT, "Export", Icons.Default.SwapHoriz, "nav_export"),
  NavItem(AppScreen.REPORTS, "Reports", Icons.Default.AssignmentTurnedIn, "nav_reports"),
  NavItem(AppScreen.PRIVACY_ABOUT, "Privacy", Icons.Default.Security, "nav_privacy")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CookieManagerApp(
  viewModel: CookieViewModel = viewModel()
) {
  val activeScreen by viewModel.activeScreen.collectAsState()
  val isDarkTheme by viewModel.isDarkTheme.collectAsState()
  val workspaceState by viewModel.workspaceState.collectAsState()
  val pendingRevealCookie by viewModel.pendingRevealCookie.collectAsState()
  val selectedDetailCookie by viewModel.selectedCookieForDetail.collectAsState()
  val showResetDialog by viewModel.showResetConfirmation.collectAsState()
  val toast by viewModel.toastNotification.collectAsState()

  CookieManagerTheme(darkTheme = isDarkTheme) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
      val isWideScreen = maxWidth >= 700.dp

      if (isWideScreen) {
        // Desktop / Tablet Landscape: Left Sidebar / NavigationRail + Main Content
        Row(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
          NavigationRail(
            modifier = Modifier
              .fillMaxHeight()
              .widthIn(min = 90.dp, max = 220.dp),
            containerColor = MaterialTheme.colorScheme.surface,
            header = {
              Column(
                modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
              ) {
                Icon(
                  imageVector = Icons.Default.Security,
                  contentDescription = null,
                  tint = Cyan400,
                  modifier = Modifier.size(28.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                  text = "Cookie Manager",
                  style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                  color = MaterialTheme.colorScheme.onSurface
                )
              }
            }
          ) {
            NAV_ITEMS.forEach { item ->
              val isSelected = activeScreen == item.screen
              val badgeCount = when (item.screen) {
                AppScreen.EXPLORER -> workspaceState.totalRecords
                AppScreen.REPORTS -> workspaceState.findings.size
                else -> 0
              }

              NavigationRailItem(
                selected = isSelected,
                onClick = { viewModel.navigateTo(item.screen) },
                icon = {
                  if (badgeCount > 0) {
                    BadgedBox(badge = { Badge { Text(badgeCount.toString()) } }) {
                      Icon(item.icon, contentDescription = item.label)
                    }
                  } else {
                    Icon(item.icon, contentDescription = item.label)
                  }
                },
                label = { Text(item.label, fontSize = 11.sp) },
                colors = NavigationRailItemDefaults.colors(
                  selectedIconColor = MaterialTheme.colorScheme.background,
                  indicatorColor = Cyan400,
                  unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.testTag(item.testTag)
              )
            }

            Spacer(Modifier.weight(1f))

            // Theme toggle button in Rail
            IconButton(
              onClick = { viewModel.toggleTheme() },
              modifier = Modifier.testTag("theme_toggle_button")
            ) {
              Icon(
                imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                contentDescription = "Toggle theme",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }

            // Reset workspace button in Rail
            IconButton(
              onClick = { viewModel.showResetDialog(true) },
              modifier = Modifier.testTag("reset_action_button")
            ) {
              Icon(
                Icons.Default.DeleteSweep,
                contentDescription = "Reset workspace",
                tint = Rose400
              )
            }
            Spacer(Modifier.height(12.dp))
          }

          // Main Content Area
          Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
            when (activeScreen) {
              AppScreen.DASHBOARD -> DashboardScreen(viewModel)
              AppScreen.IMPORT -> ImportScreen(viewModel)
              AppScreen.EXPLORER -> ExplorerScreen(viewModel)
              AppScreen.CONVERT_EXPORT -> ExportScreen(viewModel)
              AppScreen.REPORTS -> ReportsScreen(viewModel)
              AppScreen.PRIVACY_ABOUT -> PrivacyAboutScreen(viewModel)
            }
            ToastNotificationOverlay(toast = toast, onDismiss = { viewModel.dismissToast() })
          }
        }
      } else {
        // Mobile Layout: Scaffold with TopAppBar and Bottom Navigation Bar
        Scaffold(
          topBar = {
            TopAppBar(
              title = {
                Text(
                  text = activeScreen.title,
                  style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
              },
              colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface,
                titleContentColor = MaterialTheme.colorScheme.onSurface
              ),
              actions = {
                // Theme toggle button
                IconButton(
                  onClick = { viewModel.toggleTheme() },
                  modifier = Modifier.testTag("theme_toggle_button")
                ) {
                  Icon(
                    imageVector = if (isDarkTheme) Icons.Default.LightMode else Icons.Default.DarkMode,
                    contentDescription = "Toggle theme",
                    tint = Cyan400
                  )
                }

                // Reset workspace shortcut
                IconButton(
                  onClick = { viewModel.showResetDialog(true) },
                  modifier = Modifier.testTag("reset_action_button")
                ) {
                  Icon(
                    Icons.Default.DeleteSweep,
                    contentDescription = "Reset workspace",
                    tint = Rose400
                  )
                }
              }
            )
          },
          bottomBar = {
            NavigationBar(
              containerColor = MaterialTheme.colorScheme.surface,
              tonalElevation = 6.dp
            ) {
              NAV_ITEMS.forEach { item ->
                val isSelected = activeScreen == item.screen
                val badgeCount = when (item.screen) {
                  AppScreen.EXPLORER -> workspaceState.totalRecords
                  AppScreen.REPORTS -> workspaceState.findings.size
                  else -> 0
                }

                NavigationBarItem(
                  selected = isSelected,
                  onClick = { viewModel.navigateTo(item.screen) },
                  icon = {
                    if (badgeCount > 0) {
                      BadgedBox(badge = { Badge { Text(badgeCount.toString()) } }) {
                        Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(20.dp))
                      }
                    } else {
                      Icon(item.icon, contentDescription = item.label, modifier = Modifier.size(20.dp))
                    }
                  },
                  label = { Text(item.label, fontSize = 10.sp, maxLines = 1) },
                  colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.background,
                    indicatorColor = Cyan400,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                  ),
                  modifier = Modifier.testTag(item.testTag)
                )
              }
            }
          }
        ) { innerPadding ->
          Box(
            modifier = Modifier
              .fillMaxSize()
              .padding(innerPadding)
          ) {
            when (activeScreen) {
              AppScreen.DASHBOARD -> DashboardScreen(viewModel)
              AppScreen.IMPORT -> ImportScreen(viewModel)
              AppScreen.EXPLORER -> ExplorerScreen(viewModel)
              AppScreen.CONVERT_EXPORT -> ExportScreen(viewModel)
              AppScreen.REPORTS -> ReportsScreen(viewModel)
              AppScreen.PRIVACY_ABOUT -> PrivacyAboutScreen(viewModel)
            }
            ToastNotificationOverlay(toast = toast, onDismiss = { viewModel.dismissToast() })
          }
        }
      }

      // Dialogs
      SensitiveValueWarningDialog(
        cookie = pendingRevealCookie,
        onConfirm = { viewModel.confirmReveal() },
        onDismiss = { viewModel.dismissRevealDialog() }
      )

      ResetConfirmationDialog(
        show = showResetDialog,
        onConfirm = { viewModel.resetWorkspace() },
        onDismiss = { viewModel.showResetDialog(false) }
      )

      CookieDetailDialog(
        cookie = selectedDetailCookie,
        onDismiss = { viewModel.selectCookieForDetail(null) }
      )
    }
  }
}
