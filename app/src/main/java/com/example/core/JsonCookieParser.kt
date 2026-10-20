package com.example.core

import com.example.model.FindingSeverity
import com.example.model.ParsedCookie
import com.example.model.ValidationFinding
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Locale

data class ParseResult(
  val cookies: List<ParsedCookie>,
  val findings: List<ValidationFinding>,
  val isSuccessful: Boolean,
  val errorSummary: String? = null
)

object JsonCookieParser {

  fun parse(input: String): ParseResult {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) {
      return ParseResult(
        cookies = emptyList(),
        findings = listOf(
          ValidationFinding(
            severity = FindingSeverity.ERROR,
            message = "Input is empty.",
            recommendation = "Paste valid JSON cookie data or load a sample file."
          )
        ),
        isSuccessful = false,
        errorSummary = "Input string is empty."
      )
    }

    val jsonArray: JSONArray
    try {
      if (trimmed.startsWith("[")) {
        jsonArray = JSONArray(trimmed)
      } else if (trimmed.startsWith("{")) {
        val rootObj = JSONObject(trimmed)
        jsonArray = when {
          rootObj.has("cookies") -> rootObj.getJSONArray("cookies")
          rootObj.has("cookie") -> rootObj.getJSONArray("cookie")
          rootObj.has("data") -> rootObj.getJSONArray("data")
          rootObj.has("items") -> rootObj.getJSONArray("items")
          else -> {
            return ParseResult(
              cookies = emptyList(),
              findings = listOf(
                ValidationFinding(
                  severity = FindingSeverity.ERROR,
                  message = "JSON object does not contain a recognizable 'cookies' or 'data' array.",
                  recommendation = "Provide a JSON array or wrap cookies under a 'cookies' key."
                )
              ),
              isSuccessful = false,
              errorSummary = "Unrecognized JSON object wrapper."
            )
          }
        }
      } else {
        return ParseResult(
          cookies = emptyList(),
          findings = listOf(
            ValidationFinding(
              severity = FindingSeverity.ERROR,
              message = "Data does not start with valid JSON '[' or '{' tokens.",
              recommendation = "Verify the input is formatted as valid JSON."
            )
          ),
          isSuccessful = false,
          errorSummary = "Invalid JSON syntax."
        )
      }
    } catch (e: JSONException) {
      return ParseResult(
        cookies = emptyList(),
        findings = listOf(
          ValidationFinding(
            severity = FindingSeverity.ERROR,
            message = "Malformed JSON syntax: ${e.message}",
            recommendation = "Check for missing brackets, unescaped quotes, or trailing commas."
          )
        ),
        isSuccessful = false,
        errorSummary = "JSON syntax error: ${e.localizedMessage}"
      )
    }

    val cookiesList = mutableListOf<ParsedCookie>()
    val allFindings = mutableListOf<ValidationFinding>()

    for (i in 0 until jsonArray.length()) {
      val item = jsonArray.opt(i)
      if (item !is JSONObject) {
        val finding = ValidationFinding(
          severity = FindingSeverity.ERROR,
          recordIndex = i,
          message = "Record at index $i is not a JSON object (${item?.javaClass?.simpleName ?: "null"}).",
          recommendation = "Ensure each element in the array is a cookie object."
        )
        allFindings.add(finding)
        cookiesList.add(
          ParsedCookie(
            name = "[Invalid Record #$i]",
            value = "",
            domain = "",
            isValid = false,
            rawSource = item?.toString() ?: "null",
            findings = listOf(finding)
          )
        )
        continue
      }

      val obj = item
      val name = getStringVariant(obj, "name")
      val value = getStringVariant(obj, "value")
      val domain = getStringVariant(obj, "domain", "host")
      val path = getStringVariant(obj, "path") ?: "/"
      val secure = getBooleanVariant(obj, "secure") ?: false
      val httpOnly = getBooleanVariant(obj, "httpOnly", "httponly") ?: false
      val session = getBooleanVariant(obj, "session") ?: false
      val expirationDate = getExpirationVariant(obj, "expirationDate", "expires", "expiry")
      val sameSite = getStringVariant(obj, "sameSite", "samesite")
      val hostOnly = getBooleanVariant(obj, "hostOnly", "host_only")
      val storeId = getStringVariant(obj, "storeId", "store_id")

      var isValidRecord = true
      val itemFindings = mutableListOf<ValidationFinding>()

      if (name == null || name.isBlank()) {
        val f = ValidationFinding(
          severity = FindingSeverity.ERROR,
          recordIndex = i,
          field = "name",
          message = "Record at index $i is missing required 'name' field.",
          recommendation = "Provide a valid 'name' attribute."
        )
        itemFindings.add(f)
        isValidRecord = false
      }

      if (domain == null || domain.isBlank()) {
        val f = ValidationFinding(
          severity = FindingSeverity.ERROR,
          recordIndex = i,
          field = "domain",
          message = "Record at index $i is missing required 'domain' field.",
          recommendation = "Provide a valid domain attribute."
        )
        itemFindings.add(f)
        isValidRecord = false
      }

      val parsedCookie = ParsedCookie(
        name = name ?: "",
        value = value ?: "",
        domain = domain ?: "",
        path = path,
        secure = secure,
        httpOnly = httpOnly,
        session = session,
        expirationDate = expirationDate,
        sameSite = sameSite,
        hostOnly = hostOnly,
        storeId = storeId,
        isValid = isValidRecord,
        rawSource = obj.toString(2)
      )

      val attributeFindings = CookieValidator.validateCookie(parsedCookie, recordIndex = i)
      itemFindings.addAll(attributeFindings)

      val hasErrors = itemFindings.any { it.severity == FindingSeverity.ERROR }
      val finalCookie = parsedCookie.copy(
        isValid = isValidRecord && !hasErrors,
        findings = itemFindings
      )

      cookiesList.add(finalCookie)
      allFindings.addAll(itemFindings)
    }

    return ParseResult(
      cookies = cookiesList,
      findings = allFindings,
      isSuccessful = cookiesList.isNotEmpty()
    )
  }

  private fun getStringVariant(obj: JSONObject, vararg keys: String): String? {
    for (k in keys) {
      if (obj.has(k) && !obj.isNull(k)) {
        return obj.optString(k, "").trim()
      }
    }
    return null
  }

  private fun getBooleanVariant(obj: JSONObject, vararg keys: String): Boolean? {
    for (k in keys) {
      if (obj.has(k) && !obj.isNull(k)) {
        val raw = obj.opt(k)
        if (raw is Boolean) return raw
        if (raw is Number) return raw.toInt() != 0
        if (raw is String) {
          if (raw.equals("true", ignoreCase = true) || raw == "1") return true
          if (raw.equals("false", ignoreCase = true) || raw == "0") return false
        }
      }
    }
    return null
  }

  private fun getExpirationVariant(obj: JSONObject, vararg keys: String): Double? {
    for (k in keys) {
      if (obj.has(k) && !obj.isNull(k)) {
        val raw = obj.opt(k)
        when (raw) {
          is Number -> {
            val num = raw.toDouble()
            // If in milliseconds (e.g. > 10^11), normalize to seconds
            return if (num > 100_000_000_000.0) num / 1000.0 else num
          }
          is String -> {
            val trimmed = raw.trim()
            val parsedDouble = trimmed.toDoubleOrNull()
            if (parsedDouble != null) {
              return if (parsedDouble > 100_000_000_000.0) parsedDouble / 1000.0 else parsedDouble
            }
            // Try parsing standard HTTP date or ISO date
            try {
              val sdf = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US)
              val date = sdf.parse(trimmed)
              if (date != null) return date.time / 1000.0
            } catch (_: Exception) {}
          }
        }
      }
    }
    return null
  }
}
