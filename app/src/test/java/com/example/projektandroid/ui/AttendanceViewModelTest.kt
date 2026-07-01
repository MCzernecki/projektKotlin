package com.example.projektandroid.ui

import com.example.projektandroid.data.repository.ListaObecnosciRepository
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
        assertEquals(0, viewModel.studentsList.value.size)
    }

    @Test
    fun `addStudent returns error from repository validation`() = runTest {
        // Station 11 is out of range (1-10)
        val result = viewModel.addStudent("11", "Jan", "Kowalski")
        
        assertTrue(result is ValidationResult.Error)
        assertEquals(0, viewModel.studentsList.value.size)
    }
}
