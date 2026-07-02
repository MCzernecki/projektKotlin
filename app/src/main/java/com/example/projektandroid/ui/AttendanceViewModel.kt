package com.example.projektandroid.ui

import androidx.lifecycle.ViewModel
import com.example.projektandroid.data.model.GradingThreshold
import com.example.projektandroid.data.model.Student
import com.example.projektandroid.data.model.ZadanieLaboratoryjne
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

    private val _selectedStationNumber = MutableStateFlow<Int?>(null)
    val selectedStationNumber: StateFlow<Int?> = _selectedStationNumber.asStateFlow()

    private val _selectedStationStudents = MutableStateFlow<List<Student>>(emptyList())
    val selectedStationStudents: StateFlow<List<Student>> =
        _selectedStationStudents.asStateFlow()

    private val _configuredTaskNumbers = MutableStateFlow<List<Int>>(emptyList())
    val configuredTaskNumbers: StateFlow<List<Int>> =
        _configuredTaskNumbers.asStateFlow()

    private val _gradingMessages = MutableStateFlow<Map<Long, String>>(emptyMap())
    val gradingMessages: StateFlow<Map<Long, String>> = _gradingMessages.asStateFlow()

    init {
        refreshList()
    }

    fun addStudent(numerStanowiska: String, imie: String, nazwisko: String): ValidationResult {
        if (numerStanowiska.isBlank()) {
            return ValidationResult.Error("Numer stanowiska nie może być pusty.")
        }
        val stanowisko = numerStanowiska.toIntOrNull()
            ?: return ValidationResult.Error("Numer stanowiska musi być liczbą.")

        if (imie.isBlank()) {
            return ValidationResult.Error("Imię nie może być puste.")
        }
        if (imie.trim().length < 2) {
            return ValidationResult.Error("Imię musi mieć co najmniej 2 znaki.")
        }
        if (imie.any { it.isDigit() }) {
            return ValidationResult.Error("Imię nie może zawierać cyfr.")
        }

        if (nazwisko.isBlank()) {
            return ValidationResult.Error("Nazwisko nie może być puste.")
        }
        if (nazwisko.trim().length < 2) {
            return ValidationResult.Error("Nazwisko musi mieć co najmniej 2 znaki.")
        }
        if (nazwisko.any { it.isDigit() }) {
            return ValidationResult.Error("Nazwisko nie może zawierać cyfr.")
        }

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

        val configurationResult = gradeConfigurator.setConfiguration(totalTasks, thresholds)
        if (configurationResult is ValidationResult.Error) {
            _gradingConfiguration.value = state.copy(
                errorMessage = translateConfigurationError(configurationResult.message),
                isSaved = false
            )
            return configurationResult
        }

        val tasks = (1..totalTasks).map { taskNumber ->
            ZadanieLaboratoryjne(numerZadania = taskNumber, ocena = MIN_GRADE.toInt())
        }
        val tasksResult = repository.ustawListeZadan(tasks)
        if (tasksResult is ValidationResult.Error) {
            _gradingConfiguration.value = state.copy(
                errorMessage = tasksResult.message,
                isSaved = false
            )
            return tasksResult
        }

        _configuredTaskNumbers.value = tasks.map { it.numerZadania }
        _gradingConfiguration.value = state.copy(errorMessage = null, isSaved = true)
        _currentStep.value = AppStep.STATION_SELECTION
        return ValidationResult.Success
    }

    fun selectStation(stationNumber: Int): ValidationResult {
        if (stationNumber !in MIN_STATION..MAX_STATION) {
            return ValidationResult.Error("Numer stanowiska musi byc w zakresie 1-10.")
        }

        _selectedStationNumber.value = stationNumber
        refreshSelectedStationStudents()
        _gradingMessages.value = emptyMap()
        _currentStep.value = AppStep.STUDENT_GRADING
        return ValidationResult.Success
    }

    fun toggleStudentTask(studentId: Int, taskNumber: Int): ValidationResult {
        if (taskNumber !in _configuredTaskNumbers.value) {
            return ValidationResult.Error("Nie znaleziono zadania o numerze $taskNumber.")
        }

        val student = repository.pobierzWszystkichStudentow()
            .firstOrNull { it.id == studentId.toLong() }
            ?: return ValidationResult.Error("Nie znaleziono studenta o id: $studentId.")
        val configuredTask = repository.pobierzListeZadan()
            .first { it.numerZadania == taskNumber }
        val isCompleted = student.wykonaneZadania.any { it.numerZadania == taskNumber }
        val updatedTasks = if (isCompleted) {
            student.wykonaneZadania.filterNot { it.numerZadania == taskNumber }
        } else {
            (student.wykonaneZadania + configuredTask).sortedBy { it.numerZadania }
        }

        val result = repository.przypiszWykonaneZadania(student.id, updatedTasks)
        if (result is ValidationResult.Success) {
            refreshList()
            refreshSelectedStationStudents()
            clearGradingMessage(student.id)
        }
        return result
    }

    fun getSuggestedGrade(studentId: Int): Double {
        val student = repository.pobierzWszystkichStudentow()
            .firstOrNull { it.id == studentId.toLong() }
            ?: return MIN_GRADE
        return gradeConfigurator.getSuggestedGrade(student.wykonaneZadania.size)
    }

    fun saveStudentGrade(studentId: Int, grade: Double): ValidationResult {
        if (grade !in MIN_GRADE..MAX_GRADE) {
            val result = ValidationResult.Error("Ocena musi byc w zakresie 2.0-5.0.")
            setGradingMessage(studentId.toLong(), result.message)
            return result
        }

        val result = repository.ustawOceneKoncowa(studentId.toLong(), grade)
        when (result) {
            ValidationResult.Success -> {
                refreshList()
                refreshSelectedStationStudents()
                setGradingMessage(studentId.toLong(), "Ocena zostala zapisana.")
            }
            is ValidationResult.Error -> setGradingMessage(studentId.toLong(), result.message)
        }
        return result
    }

    fun returnToStationSelection() {
        _selectedStationNumber.value = null
        _selectedStationStudents.value = emptyList()
        _gradingMessages.value = emptyMap()
        _currentStep.value = AppStep.STATION_SELECTION
    }

    private fun refreshList() {
        _studentsList.value = repository.pobierzWszystkichStudentow()
    }

    private fun refreshSelectedStationStudents() {
        val stationNumber = _selectedStationNumber.value
        _selectedStationStudents.value = if (stationNumber == null) {
            emptyList()
        } else {
            repository.pobierzStudentowZeStanowiska(stationNumber)
        }
    }

    private fun setGradingMessage(studentId: Long, message: String) {
        _gradingMessages.value = _gradingMessages.value + (studentId to message)
    }

    private fun clearGradingMessage(studentId: Long) {
        _gradingMessages.value = _gradingMessages.value - studentId
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

    companion object {
        private const val MIN_STATION = 1
        private const val MAX_STATION = 10
        private const val MIN_GRADE = 2.0
        private const val MAX_GRADE = 5.0
    }
}
