package com.example.core

import com.example.model.FindingSeverity
import com.example.model.ParsedCookie
import com.example.model.ValidationFinding

object NetscapeCookieParser {

  fun parse(input: String): ParseResult {
    val lines = input.lines()
    if (lines.isEmpty() || input.isBlank()) {
      return ParseResult(
        cookies = emptyList(),
        findings = listOf(
          ValidationFinding(
            severity = FindingSeverity.ERROR,
            message = "Netscape input is empty.",
            recommendation = "Provide Netscape format cookie records."
          )
        ),
        isSuccessful = false,
        errorSummary = "Input is blank."
      )
    }

    val cookiesList = mutableListOf<ParsedCookie>()
    val allFindings = mutableListOf<ValidationFinding>()
    var processedRecordsCount = 0

    for ((index, rawLine) in lines.withIndex()) {
      val lineNo = index + 1
      val trimmedLine = rawLine.trim()

      // Skip blank lines
      if (trimmedLine.isEmpty()) continue

      // Check comments: starts with # but NOT #HttpOnly_
      if (trimmedLine.startsWith("#")) {
        if (!trimmedLine.startsWith("#HttpOnly_")) {
          // Standard comment or header
          continue
        }
      }

      processedRecordsCount++

      // Split strictly by TAB character
      val parts = rawLine.split("\t")
      if (parts.size < 7) {
        val finding = ValidationFinding(
          severity = FindingSeverity.ERROR,
          lineNo = lineNo,
          recordIndex = processedRecordsCount - 1,
          message = "Line $lineNo has ${parts.size} tab-separated columns (expected 7).",
          recommendation = "Netscape records require exactly 7 tab-separated columns: domain, flag, path, secure, expiration, name, value."
        )
        allFindings.add(finding)
        cookiesList.add(
          ParsedCookie(
            name = "[Malformed Line $lineNo]",
            value = "",
            domain = "",
            isValid = false,
            rawSource = rawLine,
            findings = listOf(finding)
          )
        )
        continue
      }

      var rawDomain = parts[0].trim()
      val includeSubdomainsRaw = parts[1].trim()
      val path = parts[2].trim()
      val secureRaw = parts[3].trim()
      val expirationRaw = parts[4].trim()
      val name = parts[5].trim()
      // The 7th column is the value, which may contain remaining text or spaces
      val value = if (parts.size > 7) {
        parts.subList(6, parts.size).joinToString("\t")
      } else {
        parts[6]
      }

      var isHttpOnly = false
      if (rawDomain.startsWith("#HttpOnly_")) {
        isHttpOnly = true
        rawDomain = rawDomain.removePrefix("#HttpOnly_")
      }

      val secure = secureRaw.equals("TRUE", ignoreCase = true)
      val expirationTimestamp = expirationRaw.toDoubleOrNull()
      val isSession = expirationTimestamp == null || expirationTimestamp <= 0.0

      val itemFindings = mutableListOf<ValidationFinding>()
      var isValid = true

      if (name.isBlank()) {
        val f = ValidationFinding(
          severity = FindingSeverity.ERROR,
          lineNo = lineNo,
          recordIndex = processedRecordsCount - 1,
          field = "name",
          message = "Line $lineNo: Cookie name is empty.",
          recommendation = "Specify a non-empty cookie name."
        )
        itemFindings.add(f)
        isValid = false
      }

      if (rawDomain.isBlank()) {
        val f = ValidationFinding(
          severity = FindingSeverity.ERROR,
          lineNo = lineNo,
          recordIndex = processedRecordsCount - 1,
          field = "domain",
          message = "Line $lineNo: Domain is empty.",
          recommendation = "Specify a valid domain or hostname."
        )
        itemFindings.add(f)
        isValid = false
      }

      val parsedCookie = ParsedCookie(
        name = name,
        value = value,
        domain = rawDomain,
        path = if (path.isEmpty()) "/" else path,
        secure = secure,
        httpOnly = isHttpOnly,
        session = isSession,
        expirationDate = if (isSession) null else expirationTimestamp,
        hostOnly = !includeSubdomainsRaw.equals("TRUE", ignoreCase = true),
        isValid = isValid,
        rawSource = rawLine
      )

      val attributeFindings = CookieValidator.validateCookie(parsedCookie, recordIndex = processedRecordsCount - 1, lineNo = lineNo)
      itemFindings.addAll(attributeFindings)

      val hasErrors = itemFindings.any { it.severity == FindingSeverity.ERROR }
      val finalCookie = parsedCookie.copy(
        isValid = isValid && !hasErrors,
        findings = itemFindings
      )

      cookiesList.add(finalCookie)
      allFindings.addAll(itemFindings)
    }

    if (cookiesList.isEmpty() && processedRecordsCount == 0) {
      allFindings.add(
        ValidationFinding(
          severity = FindingSeverity.WARNING,
          message = "File contains only comments or headers; no cookie records found.",
          recommendation = "Verify the file contains tab-separated cookie rows."
        )
      )
    }

    return ParseResult(
      cookies = cookiesList,
      findings = allFindings,
      isSuccessful = cookiesList.isNotEmpty()
    )
  }
}
