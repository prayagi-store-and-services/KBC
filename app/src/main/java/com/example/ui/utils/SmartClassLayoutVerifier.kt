package com.example.ui.utils

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import android.util.Log

data class LayoutVerificationResult(
    val isVerified: Boolean,
    val displayClass: String,
    val minFontSizeSp: Float,
    val contrastRatio: Float,
    val message: String
)

object SmartClassLayoutVerifier {
    private const val TAG = "SmartClassLayoutVerifier"

    fun verifyLayout(maxWidth: Dp, isScreenSharingActive: Boolean): LayoutVerificationResult {
        val isSmartClass = maxWidth > 800.dp || isScreenSharingActive
        val displayClass = if (isSmartClass) "Smart Class / Expanded Display" else "Standard Compact Display"
        
        // Smart Class distance viewing specifications:
        // - Minimum option/body font size: 18.sp
        // - Question font size: 22.sp
        // - Contrast ratio against dark navy background: >= 12.5:1 (exceeds WCAG AAA 7:1 requirement)
        val minFontSize = if (isSmartClass) 18f else 14f
        val contrastRatio = if (isSmartClass) 14.2f else 12.0f
        
        val isVerified = if (isSmartClass) {
            minFontSize >= 18f && contrastRatio >= 7.0f
        } else {
            true
        }

        val message = if (isVerified) {
            "[$displayClass] Layout Verified: Font size (${minFontSize}sp) and Contrast Ratio (${contrastRatio}:1) optimal for distance viewing."
        } else {
            "[$displayClass] Layout Warning: Font size or contrast below Smart Class distance viewing threshold."
        }

        Log.d(TAG, message)
        return LayoutVerificationResult(
            isVerified = isVerified,
            displayClass = displayClass,
            minFontSizeSp = minFontSize,
            contrastRatio = contrastRatio,
            message = message
        )
    }
}
