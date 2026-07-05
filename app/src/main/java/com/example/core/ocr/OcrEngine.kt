package com.example.core.ocr

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

data class OcrResult(
    val text: String,
    val confidence: Float,
    val pagesText: List<String>
)

interface OcrEngine {
    suspend fun extractText(images: List<Bitmap>): OcrResult
}

/**
 * On-device OCR engine backed by ML Kit's Play Services Text Recognition client.
 */
class LocalOcrEngine : OcrEngine {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    override suspend fun extractText(images: List<Bitmap>): OcrResult {
        if (images.isEmpty()) {
            return OcrResult("", 1.0f, emptyList())
        }

        val pagesText = images.map { bitmap -> recognizeBitmap(bitmap) }
        val combinedText = pagesText.joinToString("\n\n")
        val confidence = if (pagesText.any { it.isNotBlank() }) 1.0f else 0.0f

        return OcrResult(
            text = combinedText,
            confidence = confidence,
            pagesText = pagesText
        )
    }

    private suspend fun recognizeBitmap(bitmap: Bitmap): String =
        suspendCancellableCoroutine { continuation ->
            val image = InputImage.fromBitmap(bitmap, 0)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    continuation.resume(visionText.text)
                }
                .addOnFailureListener { e ->
                    Log.e("LocalOcrEngine", "ML Kit text recognition failed", e)
                    continuation.resume("")
                }
        }
}
