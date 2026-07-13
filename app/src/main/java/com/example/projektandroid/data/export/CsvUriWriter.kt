package com.example.projektandroid.data.export

import android.content.ContentResolver
import android.net.Uri

class CsvUriWriter {
    fun write(
        contentResolver: ContentResolver,
        uri: Uri,
        content: String
    ): CsvUriWriteResult {
        return try {
            val outputStream = contentResolver.openOutputStream(uri)
                ?: return CsvUriWriteResult.Error(
                    IllegalStateException("Cannot open output stream for CSV file.")
                )

            outputStream.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            }
            CsvUriWriteResult.Success
        } catch (exception: Exception) {
            CsvUriWriteResult.Error(exception)
        }
    }
}

sealed class CsvUriWriteResult {
    data object Success : CsvUriWriteResult()
    data class Error(val cause: Throwable) : CsvUriWriteResult()
}
