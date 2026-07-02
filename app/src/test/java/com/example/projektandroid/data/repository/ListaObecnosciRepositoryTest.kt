package com.example.projektandroid.data.repository

import com.example.projektandroid.data.model.ZadanieLaboratoryjne
import com.example.projektandroid.data.validation.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ListaObecnosciRepositoryTest {
    @Test
    fun dodajStudenta_dodajePoprawnegoStudenta() {
        val repository = ListaObecnosciRepository()

        val result = repository.dodajStudenta(
            numerStanowiska = 1,
            imie = "Anna",
            nazwisko = "Kowalska"
        )

        assertTrue(result is ValidationResult.Success)
        assertEquals(1, repository.pobierzWszystkichStudentow().size)
        assertEquals("Anna", repository.pobierzWszystkichStudentow().first().imie)
    }

    @Test
    fun dodajStudenta_zNiepoprawnymStanowiskiem_zwracaBlad() {
        val repository = ListaObecnosciRepository()

        val result = repository.dodajStudenta(
            numerStanowiska = 11,
            imie = "Anna",
            nazwisko = "Kowalska"
        )

        assertTrue(result is ValidationResult.Error)
        assertTrue(repository.pobierzWszystkichStudentow().isEmpty())
    }

    @Test
    fun ustawListeZadan_zNiepoprawnaOcena_zwracaBlad() {
        val repository = ListaObecnosciRepository()

        val result = repository.ustawListeZadan(
            listOf(ZadanieLaboratoryjne(numerZadania = 1, ocena = 6))
        )

        assertTrue(result is ValidationResult.Error)
        assertTrue(repository.pobierzListeZadan().isEmpty())
    }

    @Test
    fun przypiszWykonaneZadania_iUstawOceneKoncowa_aktualizujaStudenta() {
        val repository = ListaObecnosciRepository()
        repository.dodajStudenta(
            numerStanowiska = 2,
            imie = "Piotr",
            nazwisko = "Nowak"
        )

        val zadania = listOf(
            ZadanieLaboratoryjne(numerZadania = 1, ocena = 4),
            ZadanieLaboratoryjne(numerZadania = 2, ocena = 5)
        )

        val zadaniaResult = repository.przypiszWykonaneZadania(studentId = 1L, zadania)
        val ocenaResult = repository.ustawOceneKoncowa(studentId = 1L, ocenaKoncowa = 4.5)
        val student = repository.pobierzWszystkichStudentow().first()

        assertTrue(zadaniaResult is ValidationResult.Success)
        assertTrue(ocenaResult is ValidationResult.Success)
        assertEquals(2, student.wykonaneZadania.size)
        assertEquals(4.5, student.ocenaKoncowa!!, 0.0)
    }

    @Test
    fun dodajPrzykladoweDane_wypelniaRepository() {
        val repository = ListaObecnosciRepository()

        val result = repository.dodajPrzykladoweDane()

        assertTrue(result is ValidationResult.Success)
        assertEquals(3, repository.pobierzListeZadan().size)
        assertEquals(3, repository.pobierzWszystkichStudentow().size)
        assertEquals(2, repository.pobierzStudentowZeStanowiska(1).size)
    }
}
