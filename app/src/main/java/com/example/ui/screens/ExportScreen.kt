package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ExportFormat
import com.example.ui.CookieViewModel
import com.example.ui.ToastType
import com.example.ui.components.DeveloperBadge
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400

@Composable
fun ExportScreen(
  viewModel: CookieViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.workspaceState.collectAsState()
  val context = LocalContext.current

  // Document Creator Launcher for downloading file
  val createDocumentLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.CreateDocument(state.exportFormat.mimeType)
  ) { uri: Uri? ->
    uri?.let {
      try {
        context.contentResolver.openOutputStream(it)?.use { stream ->
          stream.write(state.exportOutput.toByteArray())
        }
        viewModel.showToast(
          ToastType.SUCCESS,
          "File Saved",
          "Export saved as ${state.exportFormat.filename} successfully."
        )
      } catch (_: Exception) {
        viewModel.showToast(ToastType.ERROR, "Save Failed", "Could not write export file.")
      }
    }
  }

  val hasValidRecords = state.validRecords > 0 && state.exportOutput.isNotBlank()

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Header & Format Selector Card
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
            Icon(Icons.Default.SwapHoriz, contentDescription = null, tint = Cyan400, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(8.dp))
            Text(
              text = "Converter & Export",
              style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
          }

          // Refresh Output Button
          OutlinedButton(
            onClick = { viewModel.refreshExportOutput() },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier.testTag("refresh_output_button")
          ) {
            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(4.dp))
            Text("Refresh", fontSize = 12.sp)
          }
        }

        Text(
          text = "Select output format to convert and serialize your valid cookie records instantly.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        // Format Toggle Buttons: JSON | NETSCAPE TXT
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Button(
            onClick = { viewModel.setExportFormat(ExportFormat.JSON) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (state.exportFormat == ExportFormat.JSON) Cyan400 else MaterialTheme.colorScheme.surfaceVariant,
              contentColor = if (state.exportFormat == ExportFormat.JSON) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("select_format_json")
          ) {
            Text("JSON Array (.json)", fontWeight = FontWeight.Bold)
          }

          Button(
            onClick = { viewModel.setExportFormat(ExportFormat.NETSCAPE) },
            colors = ButtonDefaults.buttonColors(
              containerColor = if (state.exportFormat == ExportFormat.NETSCAPE) Cyan400 else MaterialTheme.colorScheme.surfaceVariant,
              contentColor = if (state.exportFormat == ExportFormat.NETSCAPE) MaterialTheme.colorScheme.background else MaterialTheme.colorScheme.onSurface
            ),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
              .weight(1f)
              .testTag("select_format_netscape")
          ) {
            Text("NETSCAPE TXT (.txt)", fontWeight = FontWeight.Bold)
          }
        }
      }
    }

    // Format Metadata & Statistics Pill
    Surface(
      shape = RoundedCornerShape(10.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 14.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "Export: ${state.exportFormat.name} (${state.exportFormat.filename})",
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Text(
            text = "${state.validRecords} records • ${state.exportOutput.length} characters",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }

        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Emerald400.copy(alpha = 0.15f)
        ) {
          Text(
            text = if (hasValidRecords) "Ready to Export" else "No Valid Records",
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            color = if (hasValidRecords) Emerald400 else Amber400,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
          )
        }
      }
    }

    // Warnings if Netscape conversion omitted SameSite
    if (state.exportWarnings.isNotEmpty()) {
      for (warning in state.exportWarnings) {
        Card(
          shape = RoundedCornerShape(10.dp),
          colors = CardDefaults.cardColors(containerColor = Amber400.copy(alpha = 0.15f)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = Amber400, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text(
              text = warning,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurface
            )
          }
        }
      }
    }

    // Read-only Code Viewer
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = MaterialTheme.colorScheme.surfaceVariant,
      border = CardDefaults.outlinedCardBorder().copy(
        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
      ),
      modifier = Modifier
        .fillMaxWidth()
        .height(300.dp)
        .testTag("export_code_viewer")
    ) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(14.dp)
      ) {
        Text(
          text = if (state.exportOutput.isBlank()) {
            "// Output preview is empty.\n// Import and process cookies to generate JSON or Netscape output."
          } else {
            state.exportOutput
          },
          fontFamily = FontFamily.Monospace,
          fontSize = 11.sp,
          color = MaterialTheme.colorScheme.onSurface
        )
      }
    }

    // Export Action Buttons: Copy to Clipboard & Download File & Share
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Button(
        onClick = {
          viewModel.copyToClipboard(
            context,
            state.exportOutput,
            "${state.exportFormat.name} export",
            isSensitive = true
          )
        },
        enabled = hasValidRecords,
        colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("copy_export_button")
      ) {
        Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Copy All", fontSize = 13.sp)
      }

      Button(
        onClick = {
          createDocumentLauncher.launch(state.exportFormat.filename)
        },
        enabled = hasValidRecords,
        colors = ButtonDefaults.buttonColors(containerColor = Emerald400, contentColor = MaterialTheme.colorScheme.background),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("download_export_button")
      ) {
        Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Download File", fontSize = 13.sp)
      }

      OutlinedButton(
        onClick = {
          try {
            val sendIntent = Intent().apply {
              action = Intent.ACTION_SEND
              putExtra(Intent.EXTRA_TEXT, state.exportOutput)
              type = state.exportFormat.mimeType
            }
            val shareIntent = Intent.createChooser(sendIntent, "Share Cookies (${state.exportFormat.filename})")
            context.startActivity(shareIntent)
          } catch (_: Exception) {
            viewModel.showToast(ToastType.ERROR, "Share Error", "Could not trigger share intent.")
          }
        },
        enabled = hasValidRecords,
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.testTag("share_export_button")
      ) {
        Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
      }
    }

    DeveloperBadge()
  }
}
