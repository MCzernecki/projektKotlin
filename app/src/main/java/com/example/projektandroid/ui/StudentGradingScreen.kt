package com.example.projektandroid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.projektandroid.data.model.Student

@Composable
fun StudentGradingScreen(viewModel: AttendanceViewModel) {
    val stationNumber by viewModel.selectedStationNumber.collectAsState()
    val students by viewModel.selectedStationStudents.collectAsState()
    val taskNumbers by viewModel.configuredTaskNumbers.collectAsState()
    val messages by viewModel.gradingMessages.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextButton(onClick = viewModel::returnToStationSelection) {
            Text("Wróć do stanowisk")
        }
        Text(
            text = "Stanowisko ${stationNumber ?: "-"}",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (students.isEmpty()) {
            Text(
                text = "Brak studentów przypisanych do tego stanowiska.",
                style = MaterialTheme.typography.bodyLarge
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(students, key = Student::id) { student ->
                    StudentGradingItem(
                        student = student,
                        taskNumbers = taskNumbers,
                        suggestedGrade = viewModel.getSuggestedGrade(student.id.toInt()),
                        message = messages[student.id],
                        onTaskToggle = { taskNumber ->
                            viewModel.toggleStudentTask(student.id.toInt(), taskNumber)
                        },
                        onSaveGrade = { grade ->
                            viewModel.saveStudentGrade(student.id.toInt(), grade)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun StudentGradingItem(
    student: Student,
    taskNumbers: List<Int>,
    suggestedGrade: Double,
    message: String?,
    onTaskToggle: (Int) -> Unit,
    onSaveGrade: (Double) -> Unit
) {
    var gradeInput by remember(student.id, student.ocenaKoncowa, suggestedGrade) {
        mutableStateOf((student.ocenaKoncowa ?: suggestedGrade).toString())
    }
    var inputError by remember(student.id) { mutableStateOf<String?>(null) }

    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "${student.imie} ${student.nazwisko}",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            taskNumbers.forEach { taskNumber ->
                val isCompleted = student.wykonaneZadania.any {
                    it.numerZadania == taskNumber
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isCompleted,
                        onCheckedChange = { onTaskToggle(taskNumber) }
                    )
                    Text("Zadanie $taskNumber")
                }
            }

            Text(
                text = "Proponowana ocena: $suggestedGrade",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            OutlinedTextField(
                value = gradeInput,
                onValueChange = {
                    gradeInput = it
                    inputError = null
                },
                label = { Text("Ocena końcowa") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Button(
                onClick = {
                    val grade = gradeInput.replace(',', '.').toDoubleOrNull()
                    if (grade == null) {
                        inputError = "Ocena musi być liczbą."
                    } else {
                        inputError = null
                        onSaveGrade(grade)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
            ) {
                Text("Zapisz ocenę")
            }

            (inputError ?: message)?.let {
                Text(
                    text = it,
                    color = if (inputError != null || it.contains("zakresie")) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.primary
                    },
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}
