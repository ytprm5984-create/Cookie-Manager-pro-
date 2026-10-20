package com.example.ui

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.CookieConverter
import com.example.core.CookieFormatDetector
import com.example.core.JsonCookieParser
import com.example.core.NetscapeCookieParser
import com.example.core.SampleCookies
import com.example.model.ActivityLogItem
import com.example.model.CookieFormat
import com.example.model.ExportFormat
import com.example.model.FindingSeverity
import com.example.model.ParsedCookie
import com.example.model.WorkspaceState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

enum class AppScreen(val title: String) {
  DASHBOARD("Dashboard"),
  IMPORT("Import Cookies"),
  EXPLORER("Cookie Explorer"),
  CONVERT_EXPORT("Converter & Export"),
  REPORTS("Validation Reports"),
  PRIVACY_ABOUT("Privacy & About")
}

enum class SortOption(val label: String) {
  NAME_ASC("Name (A-Z)"),
  NAME_DESC("Name (Z-A)"),
  DOMAIN_ASC("Domain (A-Z)"),
  DOMAIN_DESC("Domain (Z-A)"),
  EXP_ASC("Expiration (Soonest)"),
  EXP_DESC("Expiration (Latest)")
}

data class FilterOptions(
  val query: String,
  val secureOnly: Boolean,
  val httpOnly: Boolean,
  val sessionOnly: Boolean,
  val sort: SortOption
)


enum class ToastType {
  SUCCESS,
  ERROR,
  WARNING,
  INFO
}

data class ToastMessage(
  val id: String = UUID.randomUUID().toString(),
  val type: ToastType,
  val title: String,
  val message: String
)

class CookieViewModel : ViewModel() {

  private val _workspaceState = MutableStateFlow(WorkspaceState())
  val workspaceState: StateFlow<WorkspaceState> = _workspaceState.asStateFlow()

  private val _activeScreen = MutableStateFlow(AppScreen.DASHBOARD)
  val activeScreen: StateFlow<AppScreen> = _activeScreen.asStateFlow()

  private val _isDarkTheme = MutableStateFlow(true)
  val isDarkTheme: StateFlow<Boolean> = _isDarkTheme.asStateFlow()

  // Explorer filters & search
  val searchQuery = MutableStateFlow("")
  val secureOnlyFilter = MutableStateFlow(false)
  val httpOnlyFilter = MutableStateFlow(false)
  val sessionOnlyFilter = MutableStateFlow(false)
  val sortOption = MutableStateFlow(SortOption.NAME_ASC)

  // Reports filter: null = all, or specific FindingSeverity
  val reportsFilter = MutableStateFlow<FindingSeverity?>(null)

  // Sensitive values mask
  private val _revealedCookieIds = MutableStateFlow<Set<String>>(emptySet())
  val revealedCookieIds: StateFlow<Set<String>> = _revealedCookieIds.asStateFlow()

  private val _pendingRevealCookie = MutableStateFlow<ParsedCookie?>(null)
  val pendingRevealCookie: StateFlow<ParsedCookie?> = _pendingRevealCookie.asStateFlow()

  private val _selectedCookieForDetail = MutableStateFlow<ParsedCookie?>(null)
  val selectedCookieForDetail: StateFlow<ParsedCookie?> = _selectedCookieForDetail.asStateFlow()

  private val _showResetConfirmation = MutableStateFlow(false)
  val showResetConfirmation: StateFlow<Boolean> = _showResetConfirmation.asStateFlow()

  private val _toastNotification = MutableStateFlow<ToastMessage?>(null)
  val toastNotification: StateFlow<ToastMessage?> = _toastNotification.asStateFlow()

  private val _filterOptions = combine(
    searchQuery,
    secureOnlyFilter,
    httpOnlyFilter,
    sessionOnlyFilter,
    sortOption
  ) { query, secureOnly, httpOnly, sessionOnly, sort ->
    FilterOptions(query, secureOnly, httpOnly, sessionOnly, sort)
  }

  val filteredCookies: StateFlow<List<ParsedCookie>> = combine(
    _workspaceState,
    _filterOptions
  ) { state, filters ->
    var list = state.cookies

    if (filters.query.isNotBlank()) {
      val q = filters.query.trim().lowercase()
      list = list.filter {
        it.name.lowercase().contains(q) || it.domain.lowercase().contains(q)
      }
    }

    if (filters.secureOnly) {
      list = list.filter { it.secure }
    }

    if (filters.httpOnly) {
      list = list.filter { it.httpOnly }
    }

    if (filters.sessionOnly) {
      list = list.filter { it.session }
    }

    when (filters.sort) {
      SortOption.NAME_ASC -> list.sortedBy { it.name.lowercase() }
      SortOption.NAME_DESC -> list.sortedByDescending { it.name.lowercase() }
      SortOption.DOMAIN_ASC -> list.sortedBy { it.domain.lowercase() }
      SortOption.DOMAIN_DESC -> list.sortedByDescending { it.domain.lowercase() }
      SortOption.EXP_ASC -> list.sortedBy { it.expirationDate ?: Double.MAX_VALUE }
      SortOption.EXP_DESC -> list.sortedByDescending { it.expirationDate ?: 0.0 }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun navigateTo(screen: AppScreen) {
    _activeScreen.value = screen
  }

  fun toggleTheme() {
    _isDarkTheme.value = !_isDarkTheme.value
  }

  fun onInputTextChanged(text: String) {
    val detected = CookieFormatDetector.detect(text)
    _workspaceState.value = _workspaceState.value.copy(
      inputText = text,
      detectedFormat = detected
    )
  }

  fun setUploadedFile(fileName: String, fileSize: Long, content: String) {
    val detected = CookieFormatDetector.detect(content)
    _workspaceState.value = _workspaceState.value.copy(
      inputText = content,
      inputFileName = fileName,
      inputFileSize = fileSize,
      detectedFormat = detected
    )
    showToast(
      ToastType.INFO,
      "File Loaded",
      "Loaded '$fileName' (${fileSize / 1024} KB). Click 'Process & Validate' to analyze."
    )
  }

  fun loadSample(format: CookieFormat) {
    val sampleText = when (format) {
      CookieFormat.JSON_ARRAY, CookieFormat.JSON_OBJECT_CONTAINER -> SampleCookies.JSON_SAMPLE
      CookieFormat.NETSCAPE_TXT -> SampleCookies.NETSCAPE_SAMPLE
      else -> SampleCookies.JSON_SAMPLE
    }
    val detected = CookieFormatDetector.detect(sampleText)
    _workspaceState.value = _workspaceState.value.copy(
      inputText = sampleText,
      inputFileName = if (format == CookieFormat.NETSCAPE_TXT) "sample_netscape.txt" else "sample_cookies.json",
      inputFileSize = sampleText.toByteArray().size.toLong(),
      detectedFormat = detected
    )
    showToast(ToastType.INFO, "Sample Loaded", "Loaded harmless sample cookies for inspection.")
  }

  fun clearInput() {
    _workspaceState.value = _workspaceState.value.copy(
      inputText = "",
      inputFileName = null,
      inputFileSize = null,
      detectedFormat = CookieFormat.UNKNOWN
    )
  }

  fun processAndValidate() {
    val current = _workspaceState.value
    val input = current.inputText.trim()

    if (input.isEmpty()) {
      showToast(ToastType.ERROR, "Input Required", "Please paste cookie data or upload a file first.")
      return
    }

    val detected = CookieFormatDetector.detect(input)

    val parseResult = when (detected) {
      CookieFormat.JSON_ARRAY, CookieFormat.JSON_OBJECT_CONTAINER -> JsonCookieParser.parse(input)
      CookieFormat.NETSCAPE_TXT -> NetscapeCookieParser.parse(input)
      CookieFormat.UNKNOWN -> {
        // Try fallback to JSON first, then Netscape
        val tryJson = JsonCookieParser.parse(input)
        if (tryJson.cookies.isNotEmpty()) {
          tryJson
        } else {
          val tryNetscape = NetscapeCookieParser.parse(input)
          if (tryNetscape.cookies.isNotEmpty()) {
            tryNetscape
          } else {
            tryJson // return JSON error findings
          }
        }
      }
    }

    val exportResult = when (current.exportFormat) {
      ExportFormat.JSON -> CookieConverter.toJson(parseResult.cookies, validOnly = true)
      ExportFormat.NETSCAPE -> CookieConverter.toNetscape(parseResult.cookies, validOnly = true)
    }

    val newActivities = current.activities.toMutableList()
    val activityText = "Processed ${parseResult.cookies.size} records (${parseResult.cookies.count { it.isValid }} valid, ${parseResult.cookies.count { !it.isValid }} invalid)"
    newActivities.add(0, ActivityLogItem(action = "Parsed & Validated", details = activityText))

    _workspaceState.value = current.copy(
      detectedFormat = detected,
      cookies = parseResult.cookies,
      findings = parseResult.findings,
      exportOutput = exportResult.content,
      exportWarnings = exportResult.warnings,
      activities = newActivities.take(15),
      lastProcessedTime = System.currentTimeMillis()
    )

    val validCount = parseResult.cookies.count { it.isValid }
    val invalidCount = parseResult.cookies.count { !it.isValid }
    if (parseResult.cookies.isEmpty()) {
      showToast(ToastType.ERROR, "Processing Failed", "Could not parse any valid cookie records.")
    } else if (invalidCount > 0) {
      showToast(ToastType.WARNING, "Processed with Findings", "Parsed $validCount valid records; $invalidCount records have errors.")
    } else {
      showToast(ToastType.SUCCESS, "Processing Succeeded", "Successfully parsed and validated $validCount cookies.")
    }
  }

  fun setExportFormat(format: ExportFormat) {
    val current = _workspaceState.value
    val exportResult = when (format) {
      ExportFormat.JSON -> CookieConverter.toJson(current.cookies, validOnly = true)
      ExportFormat.NETSCAPE -> CookieConverter.toNetscape(current.cookies, validOnly = true)
    }
    _workspaceState.value = current.copy(
      exportFormat = format,
      exportOutput = exportResult.content,
      exportWarnings = exportResult.warnings
    )
  }

  fun refreshExportOutput() {
    setExportFormat(_workspaceState.value.exportFormat)
    showToast(ToastType.INFO, "Output Refreshed", "Regenerated export output for ${_workspaceState.value.exportFormat.name}.")
  }

  fun requestReveal(cookie: ParsedCookie) {
    if (_revealedCookieIds.value.contains(cookie.id)) {
      // Hide directly
      _revealedCookieIds.value = _revealedCookieIds.value - cookie.id
    } else {
      // Prompt warning dialog
      _pendingRevealCookie.value = cookie
    }
  }

  fun confirmReveal() {
    val cookie = _pendingRevealCookie.value ?: return
    _revealedCookieIds.value = _revealedCookieIds.value + cookie.id
    _pendingRevealCookie.value = null
  }

  fun dismissRevealDialog() {
    _pendingRevealCookie.value = null
  }

  fun selectCookieForDetail(cookie: ParsedCookie?) {
    _selectedCookieForDetail.value = cookie
  }

  fun showResetDialog(show: Boolean) {
    _showResetConfirmation.value = show
  }

  fun resetWorkspace() {
    _workspaceState.value = WorkspaceState(
      activities = listOf(
        ActivityLogItem(
          action = "Workspace Reset",
          details = "All in-memory cookies, findings, and inputs cleared."
        )
      )
    )
    _revealedCookieIds.value = emptySet()
    _pendingRevealCookie.value = null
    _selectedCookieForDetail.value = null
    _showResetConfirmation.value = false
    searchQuery.value = ""
    secureOnlyFilter.value = false
    httpOnlyFilter.value = false
    sessionOnlyFilter.value = false
    reportsFilter.value = null
    showToast(ToastType.INFO, "Workspace Reset", "All in-memory cookie data has been wiped.")
  }

  fun copyToClipboard(context: Context, text: String, label: String, isSensitive: Boolean = false) {
    try {
      val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
      val clip = ClipData.newPlainText(label, text)
      clipboard.setPrimaryClip(clip)
      val note = if (isSensitive) "Copied sensitive value securely." else "Copied $label to clipboard."
      showToast(ToastType.SUCCESS, "Copied", note)
    } catch (_: Exception) {
      showToast(ToastType.ERROR, "Clipboard Error", "Unable to copy to clipboard.")
    }
  }

  fun generateSanitizedReport(): String {
    val state = _workspaceState.value
    val sb = StringBuilder()
    sb.append("=== COOKIE MANAGER PRO - VALIDATION REPORT ===\n")
    sb.append("Developed with 💻 by Sheikh Sojib\n")
    sb.append("Generated: ${java.util.Date()}\n")
    sb.append("Total Records: ${state.totalRecords}\n")
    sb.append("Valid Records: ${state.validRecords}\n")
    sb.append("Invalid Records: ${state.invalidRecords}\n")
    sb.append("Total Warnings: ${state.warningCount}\n")
    sb.append("Detected Format: ${state.detectedFormat.displayName}\n\n")
    sb.append("--- FINDINGS SUMMARY (Sanitized - No Cookie Values Included) ---\n\n")

    if (state.findings.isEmpty()) {
      sb.append("No errors or warnings found. All cookies conform to standards.\n")
    } else {
      for ((i, f) in state.findings.withIndex()) {
        val loc = when {
          f.lineNo != null -> "Line ${f.lineNo}"
          f.recordIndex != null -> "Record #${f.recordIndex}"
          else -> "General"
        }
        sb.append("[${i + 1}] [${f.severity}] Location: $loc\n")
        f.field?.let { sb.append("     Field: $it\n") }
        sb.append("     Issue: ${f.message}\n")
        sb.append("     Fix:   ${f.recommendation}\n\n")
      }
    }
    return sb.toString()
  }

  fun showToast(type: ToastType, title: String, message: String) {
    _toastNotification.value = ToastMessage(type = type, title = title, message = message)
  }

  fun dismissToast() {
    _toastNotification.value = null
  }
}
