package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ParsedCookie
import com.example.ui.CookieViewModel
import com.example.ui.SortOption
import com.example.ui.components.DeveloperBadge
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Indigo400
import com.example.ui.theme.Rose400

@Composable
fun ExplorerScreen(
  viewModel: CookieViewModel,
  modifier: Modifier = Modifier
) {
  val cookies by viewModel.filteredCookies.collectAsState()
  val workspaceState by viewModel.workspaceState.collectAsState()
  val searchQuery by viewModel.searchQuery.collectAsState()
  val secureOnly by viewModel.secureOnlyFilter.collectAsState()
  val httpOnly by viewModel.httpOnlyFilter.collectAsState()
  val sessionOnly by viewModel.sessionOnlyFilter.collectAsState()
  val currentSort by viewModel.sortOption.collectAsState()
  val revealedIds by viewModel.revealedCookieIds.collectAsState()
  val context = LocalContext.current

  var showSortMenu by remember { mutableStateOf(false) }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header & Search Box
    item {
      Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = "Cookie Explorer",
              style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = "Showing ${cookies.size} of ${workspaceState.totalRecords} records",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }

          // Sort Dropdown Button
          Box {
            OutlinedButton(
              onClick = { showSortMenu = true },
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("sort_menu_button")
            ) {
              Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(6.dp))
              Text("Sort", fontSize = 12.sp)
            }
            DropdownMenu(
              expanded = showSortMenu,
              onDismissRequest = { showSortMenu = false }
            ) {
              SortOption.entries.forEach { option ->
                DropdownMenuItem(
                  text = {
                    Text(
                      text = option.label,
                      color = if (option == currentSort) Cyan400 else MaterialTheme.colorScheme.onSurface
                    )
                  },
                  onClick = {
                    viewModel.sortOption.value = option
                    showSortMenu = false
                  }
                )
              }
            }
          }
        }

        // Search Bar
        OutlinedTextField(
          value = searchQuery,
          onValueChange = { viewModel.searchQuery.value = it },
          placeholder = { Text("Search by name or domain...") },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
          trailingIcon = {
            if (searchQuery.isNotEmpty()) {
              IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                Icon(Icons.Default.Clear, contentDescription = "Clear search")
              }
            }
          },
          modifier = Modifier
            .fillMaxWidth()
            .testTag("explorer_search_input"),
          shape = RoundedCornerShape(12.dp),
          singleLine = true
        )

        // Filter Chips Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          FilterChip(
            selected = secureOnly,
            onClick = { viewModel.secureOnlyFilter.value = !secureOnly },
            label = { Text("Secure Only", fontSize = 11.sp) },
            leadingIcon = if (secureOnly) {
              { Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(12.dp)) }
            } else null,
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Cyan400.copy(alpha = 0.2f),
              selectedLabelColor = Cyan400
            ),
            modifier = Modifier.testTag("filter_secure")
          )

          FilterChip(
            selected = httpOnly,
            onClick = { viewModel.httpOnlyFilter.value = !httpOnly },
            label = { Text("HttpOnly", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Emerald400.copy(alpha = 0.2f),
              selectedLabelColor = Emerald400
            ),
            modifier = Modifier.testTag("filter_httponly")
          )

          FilterChip(
            selected = sessionOnly,
            onClick = { viewModel.sessionOnlyFilter.value = !sessionOnly },
            label = { Text("Session", fontSize = 11.sp) },
            colors = FilterChipDefaults.filterChipColors(
              selectedContainerColor = Indigo400.copy(alpha = 0.2f),
              selectedLabelColor = Indigo400
            ),
            modifier = Modifier.testTag("filter_session")
          )

          if (searchQuery.isNotEmpty() || secureOnly || httpOnly || sessionOnly) {
            Text(
              text = "Reset Filters",
              style = MaterialTheme.typography.labelSmall,
              color = Rose400,
              modifier = Modifier
                .clickable {
                  viewModel.searchQuery.value = ""
                  viewModel.secureOnlyFilter.value = false
                  viewModel.httpOnlyFilter.value = false
                  viewModel.sessionOnlyFilter.value = false
                }
                .padding(4.dp)
            )
          }
        }
      }
    }

    // Empty state
    if (cookies.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp)
        ) {
          Column(
            modifier = Modifier
              .fillMaxWidth()
              .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              imageVector = Icons.Default.FilterList,
              contentDescription = null,
              tint = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.size(48.dp)
            )
            Text(
              text = if (workspaceState.cookies.isEmpty()) "No Cookies Imported Yet" else "No Cookies Match Your Filters",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = if (workspaceState.cookies.isEmpty())
                "Import or paste cookie data in the Import tab to inspect records."
              else
                "Try clearing search terms or toggling filters above.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Cookie List
    items(cookies, key = { it.id }) { cookie ->
      val isRevealed = revealedIds.contains(cookie.id)

      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(
            if (!cookie.isValid) Rose400.copy(alpha = 0.7f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
          )
        ),
        modifier = Modifier
          .fillMaxWidth()
          .testTag("cookie_card_${cookie.name}")
      ) {
        Column(
          modifier = Modifier.padding(14.dp),
          verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Row 1: Name and Badges
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = cookie.name.ifEmpty { "[Unnamed Cookie]" },
                style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
              )
              Text(
                text = cookie.domain.ifEmpty { "[No domain]" },
                style = MaterialTheme.typography.bodySmall,
                color = Cyan400,
                maxLines = 1
              )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
              if (cookie.secure) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Cyan400.copy(alpha = 0.15f)
                ) {
                  Text("Secure", fontSize = 10.sp, color = Cyan400, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }
              if (cookie.httpOnly) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Emerald400.copy(alpha = 0.15f)
                ) {
                  Text("HttpOnly", fontSize = 10.sp, color = Emerald400, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }
              if (!cookie.isValid) {
                Surface(
                  shape = RoundedCornerShape(4.dp),
                  color = Rose400.copy(alpha = 0.2f)
                ) {
                  Text("Invalid", fontSize = 10.sp, color = Rose400, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
              }
            }
          }

          // Row 2: Value Masked / Revealed & Action Buttons
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 6.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = "Value:",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                  text = if (isRevealed) cookie.value else "••••••••••••••••••••••••",
                  fontFamily = FontFamily.Monospace,
                  fontSize = 11.sp,
                  color = if (isRevealed) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant,
                  maxLines = if (isRevealed) 3 else 1
                )
              }

              Row(verticalAlignment = Alignment.CenterVertically) {
                // Reveal / Mask toggle button
                IconButton(
                  onClick = { viewModel.requestReveal(cookie) },
                  modifier = Modifier
                    .size(32.dp)
                    .testTag("toggle_reveal_${cookie.name}")
                ) {
                  Icon(
                    imageVector = if (isRevealed) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                    contentDescription = if (isRevealed) "Hide value" else "Reveal sensitive value",
                    tint = if (isRevealed) Amber400 else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                  )
                }

                // Copy value button
                IconButton(
                  onClick = {
                    viewModel.copyToClipboard(context, cookie.value, "cookie value", isSensitive = true)
                  },
                  modifier = Modifier
                    .size(32.dp)
                    .testTag("copy_value_${cookie.name}")
                ) {
                  Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy cookie value",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
            }
          }

          // Row 3: Path, Expiration, SameSite & Details button
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Column {
              Text(
                text = "Path: ${cookie.path} • Exp: ${cookie.formattedExpiration}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
              cookie.sameSite?.let {
                Text(
                  text = "SameSite: $it",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }

            IconButton(
              onClick = { viewModel.selectCookieForDetail(cookie) },
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.Default.Info, contentDescription = "View cookie details", tint = Cyan400, modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }

    item {
      Spacer(Modifier.height(8.dp))
      DeveloperBadge()
    }
  }
}
