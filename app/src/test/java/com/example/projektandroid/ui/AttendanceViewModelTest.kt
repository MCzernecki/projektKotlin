package com.example.projektandroid.ui

import com.example.projektandroid.data.repository.ListaObecnosciRepository
import com.example.projektandroid.data.repository.GradeConfigurator
import com.example.projektandroid.data.validation.ValidationResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.*
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AttendanceViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var repository: ListaObecnosciRepository
    private lateinit var viewModel: AttendanceViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        repository = ListaObecnosciRepository()
        viewModel = AttendanceViewModel(repository)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `addStudent updates studentsList on success`() = runTest {
        // Initially empty
        assertEquals(0, viewModel.studentsList.value.size)

        // Add a student
        val result = viewModel.addStudent("1", "Jan", "Kowalski")
        
        assertTrue(result is ValidationResult.Success)
        
        // Advance coroutines to handle refreshList launch
        advanceUntilIdle()

        // List should now have 1 student
        assertEquals(1, viewModel.studentsList.value.size)
        assertEquals("Jan", viewModel.studentsList.value[0].imie)
    }

    @Test
    fun `addStudent returns error for invalid station number`() = runTest {
        val result = viewModel.addStudent("abc", "Jan", "Kowalski")
        
        assertTrue(result is ValidationResult.Error)
        assertEquals("Numer stanowiska musi być liczbą.", (result as ValidationResult.Error).message)
        assertEquals(0, viewModel.studentsList.value.size)
    }

    @Test
    fun `addStudent returns error for short name`() = runTest {
        val result = viewModel.addStudent("1", "J", "Kowalski")
        
        assertTrue(result is ValidationResult.Error)
        assertEquals("Imię musi mieć co najmniej 2 znaki.", (result as ValidationResult.Error).message)
    }

    @Test
    fun `addStudent returns error for name with digits`() = runTest {
        val result = viewModel.addStudent("1", "Jan2", "Kowalski")
        
        assertTrue(result is ValidationResult.Error)
        assertEquals("Imię nie może zawierać cyfr.", (result as ValidationResult.Error).message)
    }

    @Test
    fun `addStudent returns error for empty surname`() = runTest {
        val result = viewModel.addStudent("1", "Jan", "")
        
        assertTrue(result is ValidationResult.Error)
        assertEquals("Nazwisko nie może być puste.", (result as ValidationResult.Error).message)
    }

    @Test
    fun `addStudent returns error from repository validation`() = runTest {
        // Station 11 is out of range (1-10)
        val result = viewModel.addStudent("11", "Jan", "Kowalski")
        
        assertTrue(result is ValidationResult.Error)
        assertEquals("Numer stanowiska musi być w zakresie 1-10.", (result as ValidationResult.Error).message)
        assertEquals(0, viewModel.studentsList.value.size)
    }

    @Test
    fun `generateFinalList keeps attendance step when student list is empty`() {
        val result = viewModel.generateFinalList()

        assertTrue(result is ValidationResult.Error)
        assertEquals(AppStep.ATTENDANCE, viewModel.currentStep.value)
        assertTrue(!viewModel.isAttendanceListGenerated.value)
        assertTrue(viewModel.attendanceError.value != null)
    }

    @Test
    fun `generateFinalList moves to task configuration when students exist`() {
        viewModel.addStudent("1", "Jan", "Kowalski")

        val result = viewModel.generateFinalList()

        assertTrue(result is ValidationResult.Success)
        assertEquals(AppStep.TASK_CONFIGURATION, viewModel.currentStep.value)
        assertTrue(viewModel.isAttendanceListGenerated.value)
    }

    @Test
    fun `saveGradingConfiguration stores valid thresholds`() {
        val configurator = GradeConfigurator()
        viewModel = AttendanceViewModel(repository, configurator)
        viewModel.setTotalTasks("3")
        viewModel.updateThreshold(0, requiredTasks = "1", grade = "3.0")
        viewModel.addThreshold()
        viewModel.updateThreshold(1, requiredTasks = "2", grade = "4.0")
        viewModel.addThreshold()
        viewModel.updateThreshold(2, requiredTasks = "3", grade = "5.0")

        val result = viewModel.saveGradingConfiguration()

        assertTrue(result is ValidationResult.Success)
        assertTrue(viewModel.gradingConfiguration.value.isSaved)
        assertEquals(3, configurator.getTotalTasks())
        assertEquals(3, configurator.getThresholds().size)
    }

    @Test
    fun `saveGradingConfiguration rejects duplicated thresholds`() {
        val configurator = GradeConfigurator()
        viewModel = AttendanceViewModel(repository, configurator)
        viewModel.setTotalTasks("3")
        viewModel.updateThreshold(0, requiredTasks = "1", grade = "3.0")
        viewModel.addThreshold()
        viewModel.updateThreshold(1, requiredTasks = "1", grade = "4.0")

        val result = viewModel.saveGradingConfiguration()

        assertTrue(result is ValidationResult.Error)
        assertTrue(!viewModel.gradingConfiguration.value.isSaved)
        assertTrue(viewModel.gradingConfiguration.value.errorMessage != null)
        assertEquals(0, configurator.getTotalTasks())
    }

    @Test
    fun `selectStation sets station and loads its students`() {
        viewModel.addStudent("7", "Jan", "Kowalski")
        configureThreeTasks()

        val result = viewModel.selectStation(7)

        assertTrue(result is ValidationResult.Success)
        assertEquals(7, viewModel.selectedStationNumber.value)
        assertEquals(AppStep.STUDENT_GRADING, viewModel.currentStep.value)
        assertEquals(1, viewModel.selectedStationStudents.value.size)
    }

    @Test
    fun `selectStation rejects number outside allowed range`() {
        configureThreeTasks()

        val result = viewModel.selectStation(11)

        assertTrue(result is ValidationResult.Error)
        assertEquals(null, viewModel.selectedStationNumber.value)
        assertEquals(AppStep.STATION_SELECTION, viewModel.currentStep.value)
    }

    @Test
    fun `toggleStudentTask adds completed task`() {
        prepareStudentForGrading()

        val result = viewModel.toggleStudentTask(studentId = 1, taskNumber = 1)

        assertTrue(result is ValidationResult.Success)
        assertEquals(
            listOf(1),
            viewModel.selectedStationStudents.value.first()
                .wykonaneZadania.map { it.numerZadania }
        )
    }

    @Test
    fun `toggleStudentTask removes task after second toggle`() {
        prepareStudentForGrading()
        viewModel.toggleStudentTask(studentId = 1, taskNumber = 1)

        val result = viewModel.toggleStudentTask(studentId = 1, taskNumber = 1)

        assertTrue(result is ValidationResult.Success)
        assertTrue(
            viewModel.selectedStationStudents.value.first().wykonaneZadania.isEmpty()
        )
    }

    @Test
    fun `suggested grade changes when tasks are toggled`() {
        prepareStudentForGrading()
        assertEquals(2.0, viewModel.getSuggestedGrade(1), 0.0)

        viewModel.toggleStudentTask(studentId = 1, taskNumber = 1)
        assertEquals(3.0, viewModel.getSuggestedGrade(1), 0.0)

        viewModel.toggleStudentTask(studentId = 1, taskNumber = 2)
        assertEquals(4.0, viewModel.getSuggestedGrade(1), 0.0)
        assertEquals(
            null,
            viewModel.selectedStationStudents.value.first().ocenaKoncowa
        )
    }

    @Test
    fun `saveStudentGrade stores valid grade`() {
        prepareStudentForGrading()

        val result = viewModel.saveStudentGrade(studentId = 1, grade = 4.5)

        assertTrue(result is ValidationResult.Success)
        assertEquals(
            4.5,
            viewModel.selectedStationStudents.value.first().ocenaKoncowa!!,
            0.0
        )
        assertTrue(viewModel.gradingMessages.value[1L] != null)
    }

    @Test
    fun `saveStudentGrade rejects grade outside allowed range`() {
        prepareStudentForGrading()

        val result = viewModel.saveStudentGrade(studentId = 1, grade = 5.5)

        assertTrue(result is ValidationResult.Error)
        assertEquals(
            null,
            viewModel.selectedStationStudents.value.first().ocenaKoncowa
        )
        assertTrue(viewModel.gradingMessages.value[1L] != null)
    }

    @Test
    fun `returnToStationSelection returns from student grading`() {
        prepareStudentForGrading()

        viewModel.returnToStationSelection()

        assertEquals(AppStep.STATION_SELECTION, viewModel.currentStep.value)
        assertEquals(null, viewModel.selectedStationNumber.value)
        assertTrue(viewModel.selectedStationStudents.value.isEmpty())
    }

    private fun prepareStudentForGrading() {
        viewModel.addStudent("1", "Jan", "Kowalski")
        configureThreeTasks()
        viewModel.selectStation(1)
    }

    private fun configureThreeTasks() {
        viewModel.setTotalTasks("3")
        viewModel.updateThreshold(0, requiredTasks = "1", grade = "3.0")
        viewModel.addThreshold()
        viewModel.updateThreshold(1, requiredTasks = "2", grade = "4.0")
        viewModel.addThreshold()
        viewModel.updateThreshold(2, requiredTasks = "3", grade = "5.0")
        val result = viewModel.saveGradingConfiguration()
        assertTrue(result is ValidationResult.Success)
    }
}
