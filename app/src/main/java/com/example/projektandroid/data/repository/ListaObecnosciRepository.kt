package com.example.projektandroid.data.repository

import com.example.projektandroid.data.model.Student
import com.example.projektandroid.data.model.ZadanieLaboratoryjne
import com.example.projektandroid.data.validation.ValidationResult

class ListaObecnosciRepository {
    private val studenci = mutableListOf<Student>()
    private val zadaniaLaboratoryjne = mutableListOf<ZadanieLaboratoryjne>()
    private var nextStudentId = 1L

    fun dodajStudenta(
        numerStanowiska: Int,
        imie: String,
        nazwisko: String
    ): ValidationResult {
        val validationResult = validateStudent(numerStanowiska, imie, nazwisko)
        if (!validationResult.isValid) {
            return validationResult
        }

        val student = Student(
            id = nextStudentId++,
            numerStanowiska = numerStanowiska,
            imie = imie.trim(),
            nazwisko = nazwisko.trim()
        )

        studenci.add(student)
        return ValidationResult.Success
    }

    fun dodajStudenta(student: Student): ValidationResult {
        val validationResult = validateStudent(
            numerStanowiska = student.numerStanowiska,
            imie = student.imie,
            nazwisko = student.nazwisko
        )
        if (!validationResult.isValid) {
            return validationResult
        }

        val zadaniaValidationResult = validateZadania(student.wykonaneZadania)
        if (student.wykonaneZadania.isNotEmpty() && !zadaniaValidationResult.isValid) {
            return zadaniaValidationResult
        }

        val ocenaKoncowa = student.ocenaKoncowa
        if (ocenaKoncowa != null) {
            val ocenaValidationResult = validateOceneKoncowa(ocenaKoncowa)
            if (!ocenaValidationResult.isValid) {
                return ocenaValidationResult
            }
        }

        studenci.add(
            student.copy(
                imie = student.imie.trim(),
                nazwisko = student.nazwisko.trim(),
                wykonaneZadania = student.wykonaneZadania.toList()
            )
        )
        nextStudentId = maxOf(nextStudentId, student.id + 1)
        return ValidationResult.Success
    }

    fun pobierzWszystkichStudentow(): List<Student> = studenci.map { it.copy() }

    fun pobierzStudentowZeStanowiska(numerStanowiska: Int): List<Student> {
        return studenci
            .filter { it.numerStanowiska == numerStanowiska }
            .map { it.copy() }
    }

    fun usunStudenta(studentId: Long): ValidationResult {
        val removed = studenci.removeIf { it.id == studentId }
        return if (removed) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Nie znaleziono studenta o id: $studentId.")
        }
    }

    fun ustawListeZadan(zadania: List<ZadanieLaboratoryjne>): ValidationResult {
        val validationResult = validateZadania(zadania)
        if (!validationResult.isValid) {
            return validationResult
        }

        zadaniaLaboratoryjne.clear()
        zadaniaLaboratoryjne.addAll(zadania.map { it.copy() })
        return ValidationResult.Success
    }

    fun pobierzListeZadan(): List<ZadanieLaboratoryjne> {
        return zadaniaLaboratoryjne.map { it.copy() }
    }

    fun przypiszWykonaneZadania(
        studentId: Long,
        wykonaneZadania: List<ZadanieLaboratoryjne>
    ): ValidationResult {
        if (wykonaneZadania.isNotEmpty()) {
            val validationResult = validateZadania(wykonaneZadania)
            if (!validationResult.isValid) {
                return validationResult
            }
        }

        return updateStudent(studentId) { student ->
            student.copy(wykonaneZadania = wykonaneZadania.map { it.copy() })
        }
    }

    fun ustawOceneKoncowa(studentId: Long, ocenaKoncowa: Double): ValidationResult {
        val validationResult = validateOceneKoncowa(ocenaKoncowa)
        if (!validationResult.isValid) {
            return validationResult
        }

        return updateStudent(studentId) { student ->
            student.copy(ocenaKoncowa = ocenaKoncowa)
        }
    }

    fun wyczyscWszystkieDane() {
        studenci.clear()
        zadaniaLaboratoryjne.clear()
        nextStudentId = 1L
    }

    fun dodajPrzykladoweDane(): ValidationResult {
        wyczyscWszystkieDane()

        val zadaniaResult = ustawListeZadan(
            listOf(
                ZadanieLaboratoryjne(numerZadania = 1, ocena = 3),
                ZadanieLaboratoryjne(numerZadania = 2, ocena = 4),
                ZadanieLaboratoryjne(numerZadania = 3, ocena = 5)
            )
        )
        if (!zadaniaResult.isValid) {
            return zadaniaResult
        }

        val studenciDoDodania = listOf(
            Triple(1, "Anna", "Kowalska"),
            Triple(1, "Piotr", "Nowak"),
            Triple(2, "Marta", "Wisniewska")
        )

        studenciDoDodania.forEach { (stanowisko, imie, nazwisko) ->
            val result = dodajStudenta(stanowisko, imie, nazwisko)
            if (!result.isValid) {
                return result
            }
        }

        return przypiszWykonaneZadania(
            studentId = 1L,
            wykonaneZadania = listOf(
                ZadanieLaboratoryjne(numerZadania = 1, ocena = 3),
                ZadanieLaboratoryjne(numerZadania = 2, ocena = 4)
            )
        )
    }

    private fun updateStudent(
        studentId: Long,
        update: (Student) -> Student
    ): ValidationResult {
        val studentIndex = studenci.indexOfFirst { it.id == studentId }
        if (studentIndex == -1) {
            return ValidationResult.Error("Nie znaleziono studenta o id: $studentId.")
        }

        studenci[studentIndex] = update(studenci[studentIndex])
        return ValidationResult.Success
    }

    private fun validateStudent(
        numerStanowiska: Int,
        imie: String,
        nazwisko: String
    ): ValidationResult {
        if (numerStanowiska !in MIN_STANOWISKO..MAX_STANOWISKO) {
            return ValidationResult.Error("Numer stanowiska musi być w zakresie 1-10.")
        }

        if (imie.isBlank()) {
            return ValidationResult.Error("Imię nie może być puste.")
        }

        if (nazwisko.isBlank()) {
            return ValidationResult.Error("Nazwisko nie może być puste.")
        }

        return ValidationResult.Success
    }

    private fun validateZadania(zadania: List<ZadanieLaboratoryjne>): ValidationResult {
        if (zadania.isEmpty()) {
            return ValidationResult.Error("Lista zadań musi zawierać co najmniej jedno zadanie.")
        }

        zadania.forEach { zadanie ->
            if (zadanie.numerZadania <= 0) {
                return ValidationResult.Error("Numer zadania musi być większy od 0.")
            }

            val ocenaValidationResult = validateOcena(zadanie.ocena)
            if (!ocenaValidationResult.isValid) {
                return ocenaValidationResult
            }
        }

        return ValidationResult.Success
    }

    private fun validateOcena(ocena: Int): ValidationResult {
        return if (ocena in MIN_OCENA..MAX_OCENA) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Ocena musi być w zakresie 2-5.")
        }
    }

    private fun validateOceneKoncowa(ocena: Double): ValidationResult {
        return if (ocena in MIN_OCENA.toDouble()..MAX_OCENA.toDouble()) {
            ValidationResult.Success
        } else {
            ValidationResult.Error("Ocena końcowa musi być w zakresie 2.0-5.0.")
        }
    }

    companion object {
        private const val MIN_STANOWISKO = 1
        private const val MAX_STANOWISKO = 10
        private const val MIN_OCENA = 2
        private const val MAX_OCENA = 5
    }
}
