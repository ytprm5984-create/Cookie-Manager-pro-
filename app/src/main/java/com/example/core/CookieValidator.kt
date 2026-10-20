package com.example.core

import com.example.model.FindingSeverity
import com.example.model.ParsedCookie
import com.example.model.ValidationFinding

object CookieValidator {

  // Invalid characters in cookie-name per RFC 6265 (separators and control chars)
  private val SEPARATOR_CHARS = setOf('(', ')', '<', '>', '@', ',', ';', ':', '\\', '"', '/', '[', ']', '?', '=', '{', '}', ' ', '\t')

  fun validateCookie(cookie: ParsedCookie, recordIndex: Int? = null, lineNo: Int? = null): List<ValidationFinding> {
    val findings = mutableListOf<ValidationFinding>()

    // 1. Validate Name
    if (cookie.name.isBlank()) {
      findings.add(
        ValidationFinding(
          severity = FindingSeverity.ERROR,
          recordIndex = recordIndex,
          lineNo = lineNo,
          field = "name",
          message = "Cookie name is empty or consists only of whitespace.",
          recommendation = "Provide a valid non-empty cookie name."
        )
      )
    } else {
      val invalidChars = cookie.name.filter { it in SEPARATOR_CHARS || it.code < 32 || it.code > 126 }
      if (invalidChars.isNotEmpty()) {
        findings.add(
          ValidationFinding(
            severity = FindingSeverity.WARNING,
            recordIndex = recordIndex,
            lineNo = lineNo,
            field = "name",
            message = "Cookie name '${cookie.name}' contains RFC 6265 non-token characters (${invalidChars.take(3)}).",
            recommendation = "Ensure cookie names only use ASCII alphanumeric and token symbols."
          )
        )
      }
    }

    // 2. Validate Domain
    if (cookie.domain.isBlank()) {
      findings.add(
        ValidationFinding(
          severity = FindingSeverity.ERROR,
          recordIndex = recordIndex,
          lineNo = lineNo,
          field = "domain",
          message = "Domain is missing or blank.",
          recommendation = "Specify a valid domain or hostname (e.g. '.example.com')."
        )
      )
    } else {
      if (cookie.domain.startsWith("http://") || cookie.domain.startsWith("https://")) {
        findings.add(
          ValidationFinding(
            severity = FindingSeverity.ERROR,
            recordIndex = recordIndex,
            lineNo = lineNo,
            field = "domain",
            message = "Domain '${cookie.domain}' includes URL protocol prefix.",
            recommendation = "Remove 'http://' or 'https://' from the domain string."
          )
        )
      }
      if (cookie.domain.contains("/")) {
        findings.add(
          ValidationFinding(
            severity = FindingSeverity.ERROR,
            recordIndex = recordIndex,
            lineNo = lineNo,
            field = "domain",
            message = "Domain '${cookie.domain}' contains a path separator '/'.",
            recommendation = "Move the path portion into the 'path' attribute."
          )
        )
      }
    }

    // 3. Validate Path
    if (cookie.path.isBlank()) {
      findings.add(
        ValidationFinding(
          severity = FindingSeverity.WARNING,
          recordIndex = recordIndex,
          lineNo = lineNo,
          field = "path",
          message = "Cookie path is blank.",
          recommendation = "Default path to '/' for standard domain-wide scope."
        )
      )
    } else if (!cookie.path.startsWith("/")) {
      findings.add(
        ValidationFinding(
          severity = FindingSeverity.WARNING,
          recordIndex = recordIndex,
          lineNo = lineNo,
          field = "path",
          message = "Path '${cookie.path}' does not start with a leading slash '/'.",
          recommendation = "Prepend '/' to ensure RFC 6265 path compliance."
        )
      )
    }

    // 4. Validate Expiration
    if (!cookie.session && cookie.expirationDate != null) {
      if (cookie.expirationDate < 0) {
        findings.add(
          ValidationFinding(
            severity = FindingSeverity.ERROR,
            recordIndex = recordIndex,
            lineNo = lineNo,
            field = "expirationDate",
            message = "Expiration timestamp is negative (${cookie.expirationDate}).",
            recommendation = "Set a valid positive Unix timestamp or mark as a session cookie."
          )
        )
      } else {
        val nowSec = System.currentTimeMillis() / 1000.0
        if (cookie.expirationDate < nowSec) {
          findings.add(
            ValidationFinding(
              severity = FindingSeverity.WARNING,
              recordIndex = recordIndex,
              lineNo = lineNo,
              field = "expirationDate",
              message = "Cookie '${cookie.name}' has already expired (${cookie.formattedExpiration}).",
              recommendation = "Update expiration timestamp if you intend to maintain an active session."
            )
          )
        }
      }
    }

    // 5. Validate SameSite
    cookie.sameSite?.let { sameSite ->
      val normalized = sameSite.trim().lowercase()
      if (normalized !in setOf("lax", "strict", "none", "no_restriction", "unspecified")) {
        findings.add(
          ValidationFinding(
            severity = FindingSeverity.WARNING,
            recordIndex = recordIndex,
            lineNo = lineNo,
            field = "sameSite",
            message = "Unrecognized SameSite attribute value '$sameSite'.",
            recommendation = "Set SameSite to 'Lax', 'Strict', or 'None'."
          )
        )
      } else if (normalized in setOf("none", "no_restriction") && !cookie.secure) {
        findings.add(
          ValidationFinding(
            severity = FindingSeverity.WARNING,
            recordIndex = recordIndex,
            lineNo = lineNo,
            field = "secure",
            message = "SameSite=None requires Secure flag to be true in modern browsers.",
            recommendation = "Enable the Secure attribute when SameSite is None."
          )
        )
      }
    }

    // 6. Security Information
    if (!cookie.secure) {
      findings.add(
        ValidationFinding(
          severity = FindingSeverity.INFO,
          recordIndex = recordIndex,
          lineNo = lineNo,
          field = "secure",
          message = "Cookie '${cookie.name}' is transmitted over unencrypted HTTP connections.",
          recommendation = "Enable 'secure' flag to protect against MITM eavesdropping."
        )
      )
    }
    if (!cookie.httpOnly) {
      findings.add(
        ValidationFinding(
          severity = FindingSeverity.INFO,
          recordIndex = recordIndex,
          lineNo = lineNo,
          field = "httpOnly",
          message = "Cookie '${cookie.name}' is accessible via client-side JavaScript (document.cookie).",
          recommendation = "Enable 'httpOnly' flag for sensitive session tokens to mitigate XSS theft."
        )
      )
    }

    return findings
  }
}
