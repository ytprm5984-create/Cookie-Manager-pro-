package com.example

import com.example.core.CookieConverter
import com.example.core.CookieFormatDetector
import com.example.core.CookieValidator
import com.example.core.JsonCookieParser
import com.example.core.NetscapeCookieParser
import com.example.core.SampleCookies
import com.example.model.CookieFormat
import com.example.model.ExportFormat
import com.example.model.FindingSeverity
import com.example.model.ParsedCookie
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class CookieManagerTest {

  @Test
  fun testDetectFormatJsonArray() {
    val format = CookieFormatDetector.detect(SampleCookies.JSON_SAMPLE)
    assertEquals(CookieFormat.JSON_ARRAY, format)
  }

  @Test
  fun testDetectFormatNetscape() {
    val format = CookieFormatDetector.detect(SampleCookies.NETSCAPE_SAMPLE)
    assertEquals(CookieFormat.NETSCAPE_TXT, format)
  }

  @Test
  fun testDetectFormatEmptyAndUnknown() {
    assertEquals(CookieFormat.UNKNOWN, CookieFormatDetector.detect(""))
    assertEquals(CookieFormat.UNKNOWN, CookieFormatDetector.detect("   "))
    assertEquals(CookieFormat.UNKNOWN, CookieFormatDetector.detect("plain arbitrary text not cookies"))
  }

  @Test
  fun testParseJsonArraySuccess() {
    val result = JsonCookieParser.parse(SampleCookies.JSON_SAMPLE)
    assertTrue(result.isSuccessful)
    assertEquals(4, result.cookies.size)

    val sessionToken = result.cookies.first { it.name == "session_token" }
    assertEquals(".example.com", sessionToken.domain)
    assertTrue(sessionToken.secure)
    assertTrue(sessionToken.httpOnly)
    assertEquals("lax", sessionToken.sameSite)
    assertTrue(sessionToken.isValid)
  }

  @Test
  fun testParseJsonWrappedObject() {
    val wrappedJson = """{ "cookies": [ { "name": "auth_key", "value": "val123", "domain": "api.test.com" } ] }"""
    val result = JsonCookieParser.parse(wrappedJson)
    assertTrue(result.isSuccessful)
    assertEquals(1, result.cookies.size)
    assertEquals("auth_key", result.cookies[0].name)
    assertEquals("val123", result.cookies[0].value)
  }

  @Test
  fun testMalformedJsonProducesUsefulError() {
    val malformed = """[ { "name": "bad_json", """
    val result = JsonCookieParser.parse(malformed)
    assertFalse(result.isSuccessful)
    assertTrue(result.findings.any { it.severity == FindingSeverity.ERROR })
    assertNotNull(result.errorSummary)
  }

  @Test
  fun testInvalidRecordsReportedWhileValidRecordsSurvive() {
    val mixedJson = """[
      { "name": "valid_one", "value": "123", "domain": "example.com" },
      { "value": "missing_name", "domain": "example.com" },
      { "name": "valid_two", "value": "456", "domain": "example.com" }
    ]"""
    val result = JsonCookieParser.parse(mixedJson)
    assertTrue(result.isSuccessful)
    assertEquals(3, result.cookies.size)
    assertTrue(result.cookies[0].isValid)
    assertFalse(result.cookies[1].isValid)
    assertTrue(result.cookies[2].isValid)
  }

  @Test
  fun testParseNetscapeWithHttpOnlyPrefixAndComments() {
    val result = NetscapeCookieParser.parse(SampleCookies.NETSCAPE_SAMPLE)
    assertTrue(result.isSuccessful)
    assertEquals(4, result.cookies.size)

    val httpOnlyCookie = result.cookies.first { it.name == "secure_jwt" }
    assertEquals(".example.com", httpOnlyCookie.domain)
    assertTrue(httpOnlyCookie.httpOnly)
    assertTrue(httpOnlyCookie.secure)

    val sessionCookie = result.cookies.first { it.name == "preferences_theme" }
    assertTrue(sessionCookie.session)
  }

  @Test
  fun testJsonToNetscapeConversion() {
    val jsonResult = JsonCookieParser.parse(SampleCookies.JSON_SAMPLE)
    val netscapeExport = CookieConverter.toNetscape(jsonResult.cookies, validOnly = true)

    assertTrue(netscapeExport.content.contains("# Netscape HTTP Cookie File"))
    assertTrue(netscapeExport.content.contains("session_token"))
    assertTrue(netscapeExport.content.contains("#HttpOnly_") || netscapeExport.content.contains(".example.com"))
  }

  @Test
  fun testNetscapeToJsonConversion() {
    val netscapeResult = NetscapeCookieParser.parse(SampleCookies.NETSCAPE_SAMPLE)
    val jsonExport = CookieConverter.toJson(netscapeResult.cookies, validOnly = true)

    assertTrue(jsonExport.content.startsWith("["))
    assertTrue(jsonExport.content.endsWith("]"))
    assertTrue(jsonExport.content.contains("session_token"))
    assertTrue(jsonExport.content.contains("secure_jwt"))
  }

  @Test
  fun testCookieValidatorFlags() {
    val invalidCookie = ParsedCookie(
      name = "",
      value = "val",
      domain = "https://invalid.com/path",
      path = "no_slash",
      expirationDate = -50.0
    )
    val findings = CookieValidator.validateCookie(invalidCookie)
    assertTrue(findings.any { it.field == "name" && it.severity == FindingSeverity.ERROR })
    assertTrue(findings.any { it.field == "domain" && it.severity == FindingSeverity.ERROR })
    assertTrue(findings.any { it.field == "path" && it.severity == FindingSeverity.WARNING })
    assertTrue(findings.any { it.field == "expirationDate" && it.severity == FindingSeverity.ERROR })
  }
}
