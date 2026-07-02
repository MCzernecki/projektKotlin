package com.example.projektandroid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projektandroid.data.model.Student
import com.example.projektandroid.data.validation.ValidationResult

@Composable
fun AttendanceScreen(viewModel: AttendanceViewModel) {
    val students by viewModel.studentsList.collectAsState()
    val attendanceError by viewModel.attendanceError.collectAsState()
    var formError by remember { mutableStateOf<String?>(null) }
    var numerStanowiska by remember { mutableStateOf("") }
    var imie by remember { mutableStateOf("") }
    var nazwisko by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Lista obecności",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = numerStanowiska,
                    onValueChange = { numerStanowiska = it },
                    label = { Text("Stanowisko (1-10)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imie,
                    onValueChange = { imie = it },
                    label = { Text("Imię") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nazwisko,
                    onValueChange = { nazwisko = it },
                    label = { Text("Nazwisko") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Button(
                    onClick = {
                        when (val result = viewModel.addStudent(
                            numerStanowiska,
                            imie,
                            nazwisko
                        )) {
                            is ValidationResult.Error -> formError = result.message
                            ValidationResult.Success -> {
                                formError = null
                                numerStanowiska = ""
                                imie = ""
                                nazwisko = ""
                            }
                        }
                    },
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(top = 8.dp)
                ) {
                    Text("Dodaj")
                }

                formError?.let {
                    Text(
                        text = it,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
        Text(
            text = "Dodani studenci:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(students, key = Student::id) { student ->
                StudentItem(student)
            }
        }

        Button(
            onClick = { viewModel.generateFinalList() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Generuj listę")
        }

        attendanceError?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}

@Composable
fun StudentItem(student: Student) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "${student.imie} ${student.nazwisko}",
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = "Stanowisko: ${student.numerStanowiska}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            Text(text = "#${student.id}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
