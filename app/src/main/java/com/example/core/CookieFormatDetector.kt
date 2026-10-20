package com.example.core

import com.example.model.CookieFormat
import org.json.JSONArray
import org.json.JSONObject

object CookieFormatDetector {

  fun detect(input: String): CookieFormat {
    val trimmed = input.trim()
    if (trimmed.isEmpty()) {
      return CookieFormat.UNKNOWN
    }

    // Try Netscape check first if headers or lines match
    val lines = trimmed.lines()
    val isExplicitNetscapeHeader = lines.any {
      it.contains("# Netscape HTTP Cookie File", ignoreCase = true) ||
      it.contains("# HTTP Cookie File", ignoreCase = true)
    }

    if (isExplicitNetscapeHeader) {
      return CookieFormat.NETSCAPE_TXT
    }

    // Try JSON checks
    if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
      return try {
        JSONArray(trimmed)
        CookieFormat.JSON_ARRAY
      } catch (_: Exception) {
        CookieFormat.UNKNOWN
      }
    }

    if (trimmed.startsWith("{") && trimmed.endsWith("}")) {
      return try {
        val obj = JSONObject(trimmed)
        if (obj.has("cookies") || obj.has("cookie") || obj.has("data") || obj.has("items")) {
          CookieFormat.JSON_OBJECT_CONTAINER
        } else {
          // If it contains cookie-like keys, check if it's a wrapped object
          CookieFormat.UNKNOWN
        }
      } catch (_: Exception) {
        CookieFormat.UNKNOWN
      }
    }

    // Check if lines contain tab-separated cookie records
    var tabSeparatedLineCount = 0
    for (line in lines) {
      val lineTrim = line.trim()
      if (lineTrim.isEmpty() || (lineTrim.startsWith("#") && !lineTrim.startsWith("#HttpOnly_"))) {
        continue
      }
      val parts = lineTrim.split("\t")
      if (parts.size >= 7) {
        tabSeparatedLineCount++
      }
    }

    if (tabSeparatedLineCount > 0) {
      return CookieFormat.NETSCAPE_TXT
    }

    return CookieFormat.UNKNOWN
  }
}
