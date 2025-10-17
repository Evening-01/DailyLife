package com.evening.dailylife.core.util

/**
 * 根据字节流首部识别图片格式。
 */
object ImageMimeTypeDetector {

    fun detect(imageBytes: ByteArray): String? {
        val jpegSignature = byteArrayOf(0xFF.toByte(), 0xD8.toByte(), 0xFF.toByte())
        val pngSignature = byteArrayOf(0x89.toByte(), 0x50, 0x4E, 0x47)
        val gifSignature = byteArrayOf(0x47, 0x49, 0x46)

        return when {
            imageBytes.startsWith(jpegSignature) -> "image/jpeg"
            imageBytes.startsWith(pngSignature) -> "image/png"
            imageBytes.startsWith(gifSignature) -> "image/gif"
            else -> null
        }
    }

    private fun ByteArray.startsWith(signature: ByteArray): Boolean {
        return size >= signature.size && sliceArray(signature.indices).contentEquals(signature)
    }
}
