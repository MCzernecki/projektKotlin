package com.example.projektandroid.data.export

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class CsvFileWriterTest {
    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun `write saves csv content to selected directory`() {
        val writer = CsvFileWriter()
        val directory = temporaryFolder.newFolder("exports")

        val result = writer.write(
            directory = directory,
            fileName = "lista_obecnosci_2026-07-01_14-30.csv",
            content = "stanowisko;imie\n1;Jan"
        )

        assertTrue(result is CsvWriteResult.Success)
        val file = (result as CsvWriteResult.Success).file
        assertTrue(file.exists())
        assertEquals("lista_obecnosci_2026-07-01_14-30.csv", file.name)
        assertEquals("stanowisko;imie\n1;Jan", file.readText())
    }
}
