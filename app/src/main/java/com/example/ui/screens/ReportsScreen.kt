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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FindingSeverity
import com.example.ui.CookieViewModel
import com.example.ui.components.DeveloperBadge
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose400

@Composable
fun ReportsScreen(
  viewModel: CookieViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.workspaceState.collectAsState()
  val currentFilter by viewModel.reportsFilter.collectAsState()
  val context = LocalContext.current

  val displayedFindings = if (currentFilter == null) {
    state.findings
  } else {
    state.findings.filter { it.severity == currentFilter }
  }

  LazyColumn(
    modifier = modifier
      .fillMaxSize()
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp)
  ) {
    // Header & Summary Card
    item {
      Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Cyan400, modifier = Modifier.size(20.dp))
              Spacer(Modifier.width(8.dp))
              Text(
                text = "Validation Reports",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
              )
            }

            // Copy Sanitized Report button
            Button(
              onClick = {
                val report = viewModel.generateSanitizedReport()
                viewModel.copyToClipboard(context, report, "Sanitized validation report")
              },
              colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.testTag("copy_report_button")
            ) {
              Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(14.dp))
              Spacer(Modifier.width(4.dp))
              Text("Copy Report", fontSize = 12.sp)
            }
          }

          Text(
            text = "Structural and RFC 6265 validation findings. Reports are sanitized by default—never containing sensitive cookie values.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )

          // 3-Stat Row
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Emerald400.copy(alpha = 0.15f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Valid", style = MaterialTheme.typography.labelSmall, color = Emerald400)
                Text(state.validRecords.toString(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Emerald400)
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Rose400.copy(alpha = 0.15f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Errors", style = MaterialTheme.typography.labelSmall, color = Rose400)
                Text(state.errorCount.toString(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Rose400)
              }
            }

            Surface(
              shape = RoundedCornerShape(8.dp),
              color = Amber400.copy(alpha = 0.15f),
              modifier = Modifier.weight(1f)
            ) {
              Column(modifier = Modifier.padding(10.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Warnings", style = MaterialTheme.typography.labelSmall, color = Amber400)
                Text(state.warningCount.toString(), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Amber400)
              }
            }
          }

          // Filter Chips: All | Errors | Warnings | Info
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            FilterChip(
              selected = currentFilter == null,
              onClick = { viewModel.reportsFilter.value = null },
              label = { Text("All (${state.findings.size})", fontSize = 11.sp) }
            )
            FilterChip(
              selected = currentFilter == FindingSeverity.ERROR,
              onClick = { viewModel.reportsFilter.value = FindingSeverity.ERROR },
              label = { Text("Errors (${state.errorCount})", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Rose400.copy(alpha = 0.2f),
                selectedLabelColor = Rose400
              )
            )
            FilterChip(
              selected = currentFilter == FindingSeverity.WARNING,
              onClick = { viewModel.reportsFilter.value = FindingSeverity.WARNING },
              label = { Text("Warnings (${state.warningCount})", fontSize = 11.sp) },
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = Amber400.copy(alpha = 0.2f),
                selectedLabelColor = Amber400
              )
            )
          }
        }
      }
    }

    // Empty findings state
    if (displayedFindings.isEmpty()) {
      item {
        Card(
          shape = RoundedCornerShape(14.dp),
          colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
          modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp)
        ) {
          Column(
            modifier = Modifier.fillMaxWidth().padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = Emerald400, modifier = Modifier.size(44.dp))
            Text(
              text = if (state.cookies.isEmpty()) "No Input Data Analyzed" else "No Issues in This Category",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Text(
              text = if (state.cookies.isEmpty())
                "Import cookie data to run structural & security validation."
              else
                "All examined cookies adhere to RFC specifications for this category.",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }
      }
    }

    // Findings Items
    items(displayedFindings, key = { it.id }) { finding ->
      val (accentColor, icon) = when (finding.severity) {
        FindingSeverity.ERROR -> Pair(Rose400, Icons.Default.Error)
        FindingSeverity.WARNING -> Pair(Amber400, Icons.Default.Warning)
        FindingSeverity.INFO -> Pair(Cyan400, Icons.Default.Info)
      }

      val locationText = when {
        finding.lineNo != null -> "Line ${finding.lineNo}"
        finding.recordIndex != null -> "Record #${finding.recordIndex}"
        else -> "General Analysis"
      }

      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(accentColor.copy(alpha = 0.4f))
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
              Icon(imageVector = icon, contentDescription = null, tint = accentColor, modifier = Modifier.size(16.dp))
              Spacer(Modifier.width(6.dp))
              Text(
                text = finding.severity.name,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = accentColor
              )
            }

            Surface(
              shape = RoundedCornerShape(4.dp),
              color = MaterialTheme.colorScheme.surfaceVariant
            ) {
              Text(
                text = locationText,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
              )
            }
          }

          Text(
            text = finding.message,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = MaterialTheme.colorScheme.onSurface
          )

          finding.field?.let {
            Text(
              text = "Target Field: $it",
              style = MaterialTheme.typography.labelSmall,
              color = Cyan400
            )
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f),
            modifier = Modifier.fillMaxWidth()
          ) {
            Text(
              text = "Recommended Fix: ${finding.recommendation}",
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(8.dp)
            )
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
