package com.example.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

enum class CookieFormat(val displayName: String) {
  JSON_ARRAY("JSON Cookie Array"),
  JSON_OBJECT_CONTAINER("JSON Object Wrapper"),
  NETSCAPE_TXT("Netscape HTTP Cookie File"),
  UNKNOWN("Unrecognized / Invalid")
}

enum class ExportFormat(val extension: String, val mimeType: String, val filename: String) {
  JSON("json", "application/json", "cookies-export.json"),
  NETSCAPE("txt", "text/plain", "cookies-export.txt")
}

enum class FindingSeverity {
  ERROR,
  WARNING,
  INFO
}

data class ValidationFinding(
  val id: String = UUID.randomUUID().toString(),
  val severity: FindingSeverity,
  val recordIndex: Int? = null,
  val lineNo: Int? = null,
  val field: String? = null,
  val message: String,
  val recommendation: String
)

data class ParsedCookie(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val value: String,
  val domain: String,
  val path: String = "/",
  val secure: Boolean = false,
  val httpOnly: Boolean = false,
  val session: Boolean = false,
  val expirationDate: Double? = null,
  val sameSite: String? = null,
  val hostOnly: Boolean? = null,
  val storeId: String? = null,
  val isValid: Boolean = true,
  val rawSource: String = "",
  val findings: List<ValidationFinding> = emptyList()
) {
  val formattedExpiration: String
    get() {
      if (session || expirationDate == null || expirationDate <= 0.0) {
        return "Session Cookie"
      }
      return try {
        val millis = (expirationDate * 1000).toLong()
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm:ss", Locale.US)
        sdf.format(Date(millis))
      } catch (_: Exception) {
        "Exp: ${expirationDate.toLong()}"
      }
    }

  val isExpired: Boolean
    get() {
      if (session || expirationDate == null || expirationDate <= 0.0) return false
      val currentSeconds = System.currentTimeMillis() / 1000.0
      return expirationDate < currentSeconds
    }
}

data class ActivityLogItem(
  val id: String = UUID.randomUUID().toString(),
  val timestamp: Long = System.currentTimeMillis(),
  val action: String,
  val details: String
) {
  val formattedTime: String
    get() {
      val sdf = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
      return sdf.format(Date(timestamp))
    }
}

data class WorkspaceState(
  val inputText: String = "",
  val inputFileName: String? = null,
  val inputFileSize: Long? = null,
  val detectedFormat: CookieFormat = CookieFormat.UNKNOWN,
  val cookies: List<ParsedCookie> = emptyList(),
  val findings: List<ValidationFinding> = emptyList(),
  val exportFormat: ExportFormat = ExportFormat.JSON,
  val exportOutput: String = "",
  val exportWarnings: List<String> = emptyList(),
  val activities: List<ActivityLogItem> = emptyList(),
  val lastProcessedTime: Long? = null
) {
  val totalRecords: Int get() = cookies.size
  val validRecords: Int get() = cookies.count { it.isValid }
  val invalidRecords: Int get() = cookies.count { !it.isValid }
  val warningCount: Int get() = findings.count { it.severity == FindingSeverity.WARNING }
  val errorCount: Int get() = findings.count { it.severity == FindingSeverity.ERROR }
}
