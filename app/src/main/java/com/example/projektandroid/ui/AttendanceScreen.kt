package com.example.projektandroid.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.projektandroid.data.model.Student
import com.example.projektandroid.data.validation.ValidationResult

@Composable
fun AttendanceScreen(viewModel: AttendanceViewModel) {
    val students by viewModel.studentsList.collectAsState()
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Form inputs (colleague's part integrated for functionality)
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

        // Form Section
        Card(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = numerStanowiska,
                    onValueChange = { numerStanowiska = it },
                    label = { Text("Stanowisko (1-10)") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = imie,
                    onValueChange = { imie = it },
                    label = { Text("Imię") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = nazwisko,
                    onValueChange = { nazwisko = it },
                    label = { Text("Nazwisko") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                Button(
                    onClick = {
                        val result = viewModel.addStudent(numerStanowiska, imie, nazwisko)
                        if (result is ValidationResult.Error) {
                            errorMessage = result.message
                        } else {
                            errorMessage = null
                            // Clear fields on success
                            numerStanowiska = ""
                            imie = ""
                            nazwisko = ""
                        }
                    },
                    modifier = Modifier.align(Alignment.End).padding(top = 8.dp)
                ) {
                    Text("Dodaj")
                }

                errorMessage?.let {
                    Text(text = it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
            }
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // List Section
        Text(
            text = "Dodani studenci:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(students) { student ->
                StudentItem(student)
            }
        }

        Button(
            onClick = { viewModel.generateFinalList() },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
            enabled = students.isNotEmpty()
        ) {
            Text("Generuj listę")
        }
    }
}

@Composable
fun StudentItem(student: Student) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = "${student.imie} ${student.nazwisko}", style = MaterialTheme.typography.bodyLarge)
                Text(text = "Stanowisko: ${student.numerStanowiska}", style = MaterialTheme.typography.bodySmall)
            }
            Text(text = "#${student.id}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
