package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.FindingSeverity
import com.example.model.ParsedCookie
import com.example.ui.ToastMessage
import com.example.ui.ToastType
import com.example.ui.theme.Amber400
import com.example.ui.theme.Cyan400
import com.example.ui.theme.Emerald400
import com.example.ui.theme.Rose400
import kotlinx.coroutines.delay

@Composable
fun MetricCard(
  title: String,
  value: String,
  icon: ImageVector,
  accentColor: Color,
  modifier: Modifier = Modifier,
  subtitle: String? = null
) {
  Card(
    modifier = modifier.testTag("metric_card_${title.lowercase().replace(" ", "_")}"),
    shape = RoundedCornerShape(14.dp),
    colors = CardDefaults.cardColors(
      containerColor = MaterialTheme.colorScheme.surface
    ),
    border = CardDefaults.outlinedCardBorder().copy(
      brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    )
  ) {
    Column(
      modifier = Modifier.padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = title,
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Box(
          modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(accentColor.copy(alpha = 0.15f)),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(18.dp)
          )
        }
      }
      Text(
        text = value,
        style = MaterialTheme.typography.headlineMedium.copy(
          fontWeight = FontWeight.Bold,
          fontSize = 26.sp
        ),
        color = MaterialTheme.colorScheme.onSurface
      )
      if (subtitle != null) {
        Text(
          text = subtitle,
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    }
  }
}

@Composable
fun DeveloperBadge(modifier: Modifier = Modifier) {
  Surface(
    modifier = modifier.fillMaxWidth(),
    color = MaterialTheme.colorScheme.background,
    tonalElevation = 1.dp
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 12.dp, horizontal = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Text(
        text = "Developed with 💻 by Sheikh Sojib",
        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
      Spacer(modifier = Modifier.height(2.dp))
      Text(
        text = "Client-Side Only • Zero External Requests • In-Memory Processing",
        style = MaterialTheme.typography.labelSmall.copy(fontSize = 11.sp),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
      )
    }
  }
}

@Composable
fun SensitiveValueWarningDialog(
  cookie: ParsedCookie?,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  if (cookie == null) return

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.Security,
        contentDescription = null,
        tint = Amber400,
        modifier = Modifier.size(32.dp)
      )
    },
    title = {
      Text(
        text = "Reveal Sensitive Cookie?",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
          text = "Cookie '${cookie.name}' may contain active authentication tokens, session identifiers, or personal secrets.",
          style = MaterialTheme.typography.bodyMedium
        )
        Text(
          text = "Unmasking values on screen exposes them to shoulder surfing or screen recordings. Proceed only in a private environment.",
          style = MaterialTheme.typography.bodySmall,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
      }
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(containerColor = Amber400, contentColor = Color.Black),
        modifier = Modifier.testTag("confirm_reveal_button")
      ) {
        Icon(Icons.Default.Visibility, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(6.dp))
        Text("Reveal Value")
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_reveal_button")
      ) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun ResetConfirmationDialog(
  show: Boolean,
  onConfirm: () -> Unit,
  onDismiss: () -> Unit
) {
  if (!show) return

  AlertDialog(
    onDismissRequest = onDismiss,
    icon = {
      Icon(
        imageVector = Icons.Default.Warning,
        contentDescription = null,
        tint = Rose400,
        modifier = Modifier.size(32.dp)
      )
    },
    title = {
      Text(
        text = "Reset Entire Workspace?",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
      )
    },
    text = {
      Text(
        text = "This will immediately clear all imported cookies, validation findings, export outputs, and activity logs from device memory. This action cannot be undone.",
        style = MaterialTheme.typography.bodyMedium
      )
    },
    confirmButton = {
      Button(
        onClick = onConfirm,
        colors = ButtonDefaults.buttonColors(containerColor = Rose400, contentColor = Color.White),
        modifier = Modifier.testTag("confirm_reset_button")
      ) {
        Text("Reset Workspace")
      }
    },
    dismissButton = {
      OutlinedButton(
        onClick = onDismiss,
        modifier = Modifier.testTag("cancel_reset_button")
      ) {
        Text("Cancel")
      }
    }
  )
}

@Composable
fun CookieDetailDialog(
  cookie: ParsedCookie?,
  onDismiss: () -> Unit
) {
  if (cookie == null) return

  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          text = cookie.name.ifEmpty { "Cookie Details" },
          style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
          maxLines = 1
        )
        IconButton(onClick = onDismiss) {
          Icon(Icons.Default.Close, contentDescription = "Close")
        }
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        DetailRow(label = "Domain", value = cookie.domain)
        DetailRow(label = "Path", value = cookie.path)
        DetailRow(label = "Secure", value = if (cookie.secure) "Yes (HTTPS Only)" else "No (Transmitted in HTTP plaintext)")
        DetailRow(label = "HttpOnly", value = if (cookie.httpOnly) "Yes (Hidden from document.cookie)" else "No (Accessible by JavaScript)")
        DetailRow(label = "Expiration", value = cookie.formattedExpiration)
        cookie.sameSite?.let { DetailRow(label = "SameSite", value = it) }
        cookie.hostOnly?.let { DetailRow(label = "HostOnly", value = it.toString()) }

        if (cookie.findings.isNotEmpty()) {
          Spacer(Modifier.height(8.dp))
          Text(
            text = "Validation Findings (${cookie.findings.size}):",
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface
          )
          for (f in cookie.findings) {
            val color = when (f.severity) {
              FindingSeverity.ERROR -> Rose400
              FindingSeverity.WARNING -> Amber400
              FindingSeverity.INFO -> Cyan400
            }
            Card(
              colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.1f)),
              shape = RoundedCornerShape(8.dp),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = "[${f.severity}] ${f.message}",
                  style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                  color = color
                )
                Text(
                  text = "Tip: ${f.recommendation}",
                  style = MaterialTheme.typography.labelSmall,
                  color = MaterialTheme.colorScheme.onSurfaceVariant
                )
              }
            }
          }
        }

        Spacer(Modifier.height(8.dp))
        Text(
          text = "Raw Source:",
          style = MaterialTheme.typography.labelMedium,
          color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Surface(
          shape = RoundedCornerShape(8.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Text(
            text = cookie.rawSource.ifEmpty { "N/A" },
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            modifier = Modifier.padding(8.dp),
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
      }
    },
    confirmButton = {
      Button(onClick = onDismiss) {
        Text("Close")
      }
    }
  )
}

@Composable
fun DetailRow(label: String, value: String) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.SpaceBetween
  ) {
    Text(
      text = label,
      style = MaterialTheme.typography.bodySmall,
      color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
      text = value,
      style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
      color = MaterialTheme.colorScheme.onSurface
    )
  }
}

@Composable
fun ToastNotificationOverlay(
  toast: ToastMessage?,
  onDismiss: () -> Unit
) {
  LaunchedEffect(toast?.id) {
    if (toast != null) {
      delay(3500)
      onDismiss()
    }
  }

  AnimatedVisibility(
    visible = toast != null,
    enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
    exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
  ) {
    if (toast != null) {
      val (accentColor, icon) = when (toast.type) {
        ToastType.SUCCESS -> Pair(Emerald400, Icons.Default.CheckCircle)
        ToastType.ERROR -> Pair(Rose400, Icons.Default.Error)
        ToastType.WARNING -> Pair(Amber400, Icons.Default.Warning)
        ToastType.INFO -> Pair(Cyan400, Icons.Default.Info)
      }

      Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp,
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(22.dp)
          )
          Spacer(Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = toast.title,
              style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = toast.message,
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
          IconButton(
            onClick = onDismiss,
            modifier = Modifier.size(24.dp)
          ) {
            Icon(
              Icons.Default.Close,
              contentDescription = "Dismiss",
              modifier = Modifier.size(16.dp)
            )
          }
        }
      }
    }
  }
}
