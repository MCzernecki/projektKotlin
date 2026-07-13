package com.example.projektandroid.data.export

import com.example.projektandroid.data.model.Student
import com.example.projektandroid.data.model.ZadanieLaboratoryjne
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

class CsvExporter {
    fun generateCsv(
        students: List<Student>,
        tasks: List<ZadanieLaboratoryjne>
    ): String {
        val sortedTasks = tasks.sortedBy { it.numerZadania }
        val header = listOf("stanowisko", "imie", "nazwisko") +
            sortedTasks.map { "zadanie_${it.numerZadania}" } +
            "ocena_koncowa"

        val rows = students
            .sortedWith(
                compareBy<Student> { it.numerStanowiska }
                    .thenBy { it.nazwisko.lowercase(Locale.getDefault()) }
                    .thenBy { it.imie.lowercase(Locale.getDefault()) }
            )
            .map { student -> student.toCsvRow(sortedTasks) }

        return (listOf(header) + rows)
            .joinToString(separator = "\n") { row ->
                row.joinToString(separator = CSV_SEPARATOR) { it.escapeCsvValue() }
            }
    }

    fun generateFileName(dateTime: LocalDateTime = LocalDateTime.now()): String {
        return "lista_obecnosci_${dateTime.format(FILE_NAME_DATE_FORMAT)}.csv"
    }

    private fun Student.toCsvRow(tasks: List<ZadanieLaboratoryjne>): List<String> {
        val completedTaskNumbers = wykonaneZadania.map { it.numerZadania }.toSet()
        val taskColumns = tasks.map { task ->
            if (task.numerZadania in completedTaskNumbers) COMPLETED_TASK else NOT_COMPLETED_TASK
        }

        return listOf(
            numerStanowiska.toString(),
            imie,
            nazwisko
        ) + taskColumns + (ocenaKoncowa?.toString() ?: MISSING_FINAL_GRADE)
    }

    private fun String.escapeCsvValue(): String {
        val needsEscaping = contains(CSV_SEPARATOR) ||
            contains("\"") ||
            contains("\n") ||
            contains("\r")

        return if (needsEscaping) {
            "\"${replace("\"", "\"\"")}\""
        } else {
            this
        }
    }

    private companion object {
        const val CSV_SEPARATOR = ";"
        const val COMPLETED_TASK = "tak"
        const val NOT_COMPLETED_TASK = "nie"
        const val MISSING_FINAL_GRADE = "brak"

        val FILE_NAME_DATE_FORMAT: DateTimeFormatter =
            DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm")
    }
}
