package com.example.projektandroid.data.export

import android.content.Context
import java.io.File

class CsvFileWriter {
    fun write(
        context: Context,
        fileName: String,
        content: String
    ): CsvWriteResult {
        return write(directory = context.filesDir, fileName = fileName, content = content)
    }

    fun write(
        directory: File,
        fileName: String,
        content: String
    ): CsvWriteResult {
        return try {
            if (!directory.exists()) {
                directory.mkdirs()
            }

            val file = File(directory, fileName)
            file.writeText(content, Charsets.UTF_8)
            CsvWriteResult.Success(file)
        } catch (exception: Exception) {
            CsvWriteResult.Error(exception)
        }
    }
}

sealed class CsvWriteResult {
    data class Success(val file: File) : CsvWriteResult()
    data class Error(val cause: Throwable) : CsvWriteResult()
}
