package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CookieFormat
import com.example.ui.CookieViewModel
import com.example.ui.components.DeveloperBadge
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose400

@Composable
fun ImportScreen(
  viewModel: CookieViewModel,
  modifier: Modifier = Modifier
) {
  val state by viewModel.workspaceState.collectAsState()
  val context = LocalContext.current

  // File Picker Launcher
  val filePickerLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.GetContent()
  ) { uri: Uri? ->
    uri?.let {
      try {
        val inputStream = context.contentResolver.openInputStream(it)
        val content = inputStream?.bufferedReader()?.use { reader -> reader.readText() } ?: ""
        val fileName = it.lastPathSegment?.substringAfterLast('/') ?: "imported_cookies.txt"
        val sizeBytes = content.toByteArray().size.toLong()
        viewModel.setUploadedFile(fileName, sizeBytes, content)
      } catch (_: Exception) {
        viewModel.showToast(com.example.ui.ToastType.ERROR, "File Read Error", "Failed to read the selected file.")
      }
    }
  }

  Column(
    modifier = modifier
      .fillMaxSize()
      .verticalScroll(rememberScrollState())
      .padding(16.dp),
    verticalArrangement = Arrangement.spacedBy(16.dp)
  ) {
    // Top instructions card
    Card(
      shape = RoundedCornerShape(14.dp),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
      border = CardDefaults.outlinedCardBorder().copy(
        brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
      ),
      modifier = Modifier.fillMaxWidth()
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Code, contentDescription = null, tint = Cyan400, modifier = Modifier.size(20.dp))
          Spacer(Modifier.width(8.dp))
          Text(
            text = "Cookie Import Center",
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
        }
        Text(
          text = "Paste cookie data directly or upload local .json, .txt, or .cookies files. Supported formats: JSON arrays, wrapped objects, and Netscape HTTP tab-separated cookie files.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }

    // Action buttons: File Upload, Sample Data, Clear
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Button(
        onClick = { filePickerLauncher.launch("*/*") },
        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.surfaceVariant, contentColor = MaterialTheme.colorScheme.onSurface),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("upload_file_button")
      ) {
        Icon(Icons.Default.FileUpload, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Upload File", fontSize = 12.sp)
      }

      OutlinedButton(
        onClick = { viewModel.loadSample(CookieFormat.JSON_ARRAY) },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("sample_json_button")
      ) {
        Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text("Sample JSON", fontSize = 12.sp)
      }

      OutlinedButton(
        onClick = { viewModel.loadSample(CookieFormat.NETSCAPE_TXT) },
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
          .weight(1f)
          .testTag("sample_netscape_button")
      ) {
        Icon(Icons.Default.Science, contentDescription = null, modifier = Modifier.size(14.dp))
        Spacer(Modifier.width(4.dp))
        Text("Sample Netscape", fontSize = 12.sp)
      }
    }

    // File info badge if uploaded
    if (state.inputFileName != null || state.detectedFormat != CookieFormat.UNKNOWN) {
      Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = state.inputFileName ?: "Pasted Buffer (${state.inputText.length} chars)",
              style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
              color = MaterialTheme.colorScheme.onSurface
            )
            state.inputFileSize?.let {
              Text(
                text = "${it / 1024} KB",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
              )
            }
          }

          Surface(
            shape = RoundedCornerShape(6.dp),
            color = if (state.detectedFormat != CookieFormat.UNKNOWN) Cyan400.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
          ) {
            Text(
              text = state.detectedFormat.displayName,
              style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
              color = if (state.detectedFormat != CookieFormat.UNKNOWN) Cyan400 else MaterialTheme.colorScheme.onSurfaceVariant,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
    }

    // Textarea for cookie content
    OutlinedTextField(
      value = state.inputText,
      onValueChange = { viewModel.onInputTextChanged(it) },
      modifier = Modifier
        .fillMaxWidth()
        .height(280.dp)
        .testTag("cookie_input_field"),
      label = { Text("Cookie Content (JSON or Netscape format)") },
      placeholder = { Text("Paste JSON array e.g. [{\"name\": \"...\"}] or Netscape tab-separated lines...") },
      textStyle = MaterialTheme.typography.bodySmall.copy(
        fontFamily = FontFamily.Monospace,
        fontSize = 12.sp
      ),
      trailingIcon = {
        if (state.inputText.isNotEmpty()) {
          IconButton(onClick = { viewModel.clearInput() }) {
            Icon(Icons.Default.Clear, contentDescription = "Clear input")
          }
        }
      },
      shape = RoundedCornerShape(12.dp)
    )

    // Prominent "Process & Validate" Button
    Button(
      onClick = { viewModel.processAndValidate() },
      colors = ButtonDefaults.buttonColors(containerColor = Cyan400, contentColor = MaterialTheme.colorScheme.background),
      shape = RoundedCornerShape(12.dp),
      modifier = Modifier
        .fillMaxWidth()
        .height(52.dp)
        .testTag("process_and_validate_button")
    ) {
      Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(20.dp))
      Spacer(Modifier.width(8.dp))
      Text(
        text = "Process & Validate",
        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold, fontSize = 16.sp)
      )
    }

    // Processing Summary if cookies exist
    if (state.cookies.isNotEmpty() || state.findings.isNotEmpty()) {
      Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder().copy(
          brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
        ),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Text(
            text = "Latest Processing Results",
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = MaterialTheme.colorScheme.onSurface
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Total Records Parsed:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(state.totalRecords.toString(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold))
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Valid Records:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(state.validRecords.toString(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Emerald400)
          }
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text("Invalid Records:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(state.invalidRecords.toString(), style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = if (state.invalidRecords > 0) Rose400 else MaterialTheme.colorScheme.onSurface)
          }
        }
      }
    }

    DeveloperBadge()
  }
}
