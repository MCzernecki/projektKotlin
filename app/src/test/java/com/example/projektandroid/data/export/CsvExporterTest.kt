package com.example.projektandroid.data.export

import com.example.projektandroid.data.model.Student
import com.example.projektandroid.data.model.ZadanieLaboratoryjne
import java.time.LocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CsvExporterTest {
    private val exporter = CsvExporter()

    @Test
    fun `generateCsv creates header with task columns based on configured tasks`() {
        val csv = exporter.generateCsv(
            students = listOf(student(numerStanowiska = 1, imie = "Jan", nazwisko = "Kowalski")),
            tasks = tasks(1, 2, 3)
        )

        val header = csv.lines().first()

        assertEquals(
            "stanowisko;imie;nazwisko;zadanie_1;zadanie_2;zadanie_3;ocena_koncowa",
            header
        )
    }

    @Test
    fun `generateCsv writes completed and missing tasks and final grade`() {
        val csv = exporter.generateCsv(
            students = listOf(
                student(
                    numerStanowiska = 1,
                    imie = "Jan",
                    nazwisko = "Kowalski",
                    wykonaneZadania = tasks(1, 3),
                    ocenaKoncowa = 4.0
                )
            ),
            tasks = tasks(1, 2, 3)
        )

        val row = csv.lines()[1]

        assertEquals("1;Jan;Kowalski;tak;nie;tak;4.0", row)
    }

    @Test
    fun `generateCsv writes brak when final grade is missing`() {
        val csv = exporter.generateCsv(
            students = listOf(
                student(
                    numerStanowiska = 3,
                    imie = "Piotr",
                    nazwisko = "Zielinski"
                )
            ),
            tasks = tasks(1, 2, 3)
        )

        val row = csv.lines()[1]

        assertEquals("3;Piotr;Zielinski;nie;nie;nie;brak", row)
    }

    @Test
    fun `generateCsv sorts students by station and surname`() {
        val csv = exporter.generateCsv(
            students = listOf(
                student(numerStanowiska = 2, imie = "Piotr", nazwisko = "Zielinski"),
                student(numerStanowiska = 1, imie = "Jan", nazwisko = "Kowalski"),
                student(numerStanowiska = 1, imie = "Anna", nazwisko = "Nowak")
            ),
            tasks = tasks(1)
        )

        val rows = csv.lines().drop(1)

        assertEquals("1;Jan;Kowalski;nie;brak", rows[0])
        assertEquals("1;Anna;Nowak;nie;brak", rows[1])
        assertEquals("2;Piotr;Zielinski;nie;brak", rows[2])
    }

    @Test
    fun `generateFileName contains date time and csv extension`() {
        val fileName = exporter.generateFileName(
            LocalDateTime.of(2026, 7, 1, 14, 30)
        )

        assertEquals("lista_obecnosci_2026-07-01_14-30.csv", fileName)
        assertTrue(fileName.endsWith(".csv"))
    }

    @Test
    fun `generateCsv escapes values containing separator`() {
        val csv = exporter.generateCsv(
            students = listOf(
                student(numerStanowiska = 1, imie = "Jan;Adam", nazwisko = "Kowalski")
            ),
            tasks = tasks(1)
        )

        val row = csv.lines()[1]

        assertEquals("1;\"Jan;Adam\";Kowalski;nie;brak", row)
    }

    private fun student(
        numerStanowiska: Int,
        imie: String,
        nazwisko: String,
        wykonaneZadania: List<ZadanieLaboratoryjne> = emptyList(),
        ocenaKoncowa: Double? = null
    ): Student {
        return Student(
            id = nextStudentId++,
            numerStanowiska = numerStanowiska,
            imie = imie,
            nazwisko = nazwisko,
            wykonaneZadania = wykonaneZadania,
            ocenaKoncowa = ocenaKoncowa
        )
    }

    private fun tasks(vararg numbers: Int): List<ZadanieLaboratoryjne> {
        return numbers.map { number ->
            ZadanieLaboratoryjne(numerZadania = number, ocena = 2)
        }
    }

    private companion object {
        var nextStudentId = 1L
    }
}
