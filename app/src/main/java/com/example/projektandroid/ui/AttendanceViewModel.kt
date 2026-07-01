package com.example.projektandroid.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.projektandroid.data.model.Student
import com.example.projektandroid.data.repository.ListaObecnosciRepository
import com.example.projektandroid.data.validation.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel managing the state of the student attendance list.
 */
class AttendanceViewModel(
    private val repository: ListaObecnosciRepository = ListaObecnosciRepository()
) : ViewModel() {

    private val _studentsList = MutableStateFlow<List<Student>>(emptyList())
    val studentsList: StateFlow<List<Student>> = _studentsList.asStateFlow()

    init {
        refreshList()
    }

    /**
     * Adds a new student and refreshes the state.
     */
    fun addStudent(numerStanowiska: String, imie: String, nazwisko: String): ValidationResult {
        val stanowiskoInt = numerStanowiska.toIntOrNull() ?: return ValidationResult.Error("Numer stanowiska musi być liczbą.")
        
        val result = repository.dodajStudenta(stanowiskoInt, imie, nazwisko)
        if (result is ValidationResult.Success) {
            refreshList()
        }
        return result
    }

    /**
     * Updates the local state from the repository.
     */
    private fun refreshList() {
        viewModelScope.launch {
            _studentsList.value = repository.pobierzWszystkichStudentow()
        }
    }

    /**
     * Finalizes the list generation.
     */
    fun generateFinalList() {
        // Logic for final generation would go here
    }
}
