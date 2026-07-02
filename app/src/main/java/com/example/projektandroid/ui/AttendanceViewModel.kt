package com.example.projektandroid.ui

import androidx.lifecycle.ViewModel
import com.example.projektandroid.data.model.GradingThreshold
import com.example.projektandroid.data.model.Student
import com.example.projektandroid.data.repository.GradeConfigurator
import com.example.projektandroid.data.repository.ListaObecnosciRepository
import com.example.projektandroid.data.validation.ValidationResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class AttendanceViewModel(
    private val repository: ListaObecnosciRepository = ListaObecnosciRepository(),
    private val gradeConfigurator: GradeConfigurator = GradeConfigurator()
) : ViewModel() {

    private val _studentsList = MutableStateFlow<List<Student>>(emptyList())
    val studentsList: StateFlow<List<Student>> = _studentsList.asStateFlow()

    private val _currentStep = MutableStateFlow(AppStep.ATTENDANCE)
    val currentStep: StateFlow<AppStep> = _currentStep.asStateFlow()

    private val _isAttendanceListGenerated = MutableStateFlow(false)
    val isAttendanceListGenerated: StateFlow<Boolean> =
        _isAttendanceListGenerated.asStateFlow()

    private val _attendanceError = MutableStateFlow<String?>(null)
    val attendanceError: StateFlow<String?> = _attendanceError.asStateFlow()

    private val _gradingConfiguration = MutableStateFlow(GradingConfigurationState())
    val gradingConfiguration: StateFlow<GradingConfigurationState> =
        _gradingConfiguration.asStateFlow()

    init {
        refreshList()
    }

    fun addStudent(numerStanowiska: String, imie: String, nazwisko: String): ValidationResult {
        val stanowisko = numerStanowiska.toIntOrNull()
            ?: return ValidationResult.Error("Numer stanowiska musi byc liczba.")

        val result = repository.dodajStudenta(stanowisko, imie, nazwisko)
        if (result is ValidationResult.Success) {
            refreshList()
            _attendanceError.value = null
        }
        return result
    }

    fun generateFinalList(): ValidationResult {
        if (_studentsList.value.isEmpty()) {
            val result = ValidationResult.Error(
                "Dodaj co najmniej jednego studenta przed wygenerowaniem listy."
            )
            _attendanceError.value = result.message
            return result
        }

        _attendanceError.value = null
        _isAttendanceListGenerated.value = true
        _currentStep.value = AppStep.TASK_CONFIGURATION
        return ValidationResult.Success
    }

    fun setTotalTasks(value: String) {
        updateGradingState {
            it.copy(totalTasks = value)
        }
    }

    fun addThreshold() {
        updateGradingState {
            it.copy(thresholds = it.thresholds + GradingThresholdInput())
        }
    }

    fun updateThreshold(index: Int, requiredTasks: String? = null, grade: String? = null) {
        val currentState = _gradingConfiguration.value
        if (index !in currentState.thresholds.indices) return

        val updatedThresholds = currentState.thresholds.mapIndexed { thresholdIndex, threshold ->
            if (thresholdIndex == index) {
                threshold.copy(
                    requiredTasks = requiredTasks ?: threshold.requiredTasks,
                    grade = grade ?: threshold.grade
                )
            } else {
                threshold
            }
        }
        updateGradingState { it.copy(thresholds = updatedThresholds) }
    }

    fun removeThreshold(index: Int) {
        val currentState = _gradingConfiguration.value
        if (index !in currentState.thresholds.indices) return

        updateGradingState {
            it.copy(thresholds = it.thresholds.filterIndexed { i, _ -> i != index })
        }
    }

    fun saveGradingConfiguration(): ValidationResult {
        val state = _gradingConfiguration.value
        val totalTasks = state.totalTasks.toIntOrNull()
            ?: return configurationError("Liczba zadan musi byc poprawna liczba calkowita.")

        val thresholds = mutableListOf<GradingThreshold>()
        state.thresholds.forEachIndexed { index, input ->
            val requiredTasks = input.requiredTasks.toIntOrNull()
                ?: return configurationError(
                    "Minimalna liczba zadan w progu ${index + 1} musi byc liczba calkowita."
                )
            val grade = input.grade.replace(',', '.').toDoubleOrNull()
                ?: return configurationError("Ocena w progu ${index + 1} musi byc liczba.")
            thresholds += GradingThreshold(requiredTasks = requiredTasks, grade = grade)
        }

        val result = gradeConfigurator.setConfiguration(totalTasks, thresholds)
        _gradingConfiguration.value = when (result) {
            ValidationResult.Success -> state.copy(errorMessage = null, isSaved = true)
            is ValidationResult.Error -> state.copy(
                errorMessage = translateConfigurationError(result.message),
                isSaved = false
            )
        }
        return result
    }

    private fun refreshList() {
        _studentsList.value = repository.pobierzWszystkichStudentow()
    }

    private fun updateGradingState(
        update: (GradingConfigurationState) -> GradingConfigurationState
    ) {
        _gradingConfiguration.value = update(_gradingConfiguration.value).copy(
            errorMessage = null,
            isSaved = false
        )
    }

    private fun configurationError(message: String): ValidationResult.Error {
        _gradingConfiguration.value = _gradingConfiguration.value.copy(
            errorMessage = message,
            isSaved = false
        )
        return ValidationResult.Error(message)
    }

    private fun translateConfigurationError(message: String): String {
        return when {
            message.startsWith("Total number") -> "Liczba zadan musi byc wieksza od 0."
            message.startsWith("At least one") -> "Dodaj co najmniej jeden prog oceniania."
            message.startsWith("Required tasks count cannot") ->
                "Minimalna liczba wykonanych zadan nie moze byc ujemna."
            message.startsWith("Required tasks (") ->
                "Minimalna liczba wykonanych zadan nie moze przekraczac liczby wszystkich zadan."
            message.startsWith("Required tasks count") ->
                "Progi nie moga miec tej samej minimalnej liczby wykonanych zadan."
            message.startsWith("Grade must") -> "Ocena musi byc w zakresie 2.0-5.0."
            else -> message
        }
    }
}
