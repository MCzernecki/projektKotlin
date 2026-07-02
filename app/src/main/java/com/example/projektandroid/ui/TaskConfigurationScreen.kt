package com.example.projektandroid.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun TaskConfigurationScreen(viewModel: AttendanceViewModel) {
    val configuration by viewModel.gradingConfiguration.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Konfiguracja zadań i ocen",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = configuration.totalTasks,
            onValueChange = viewModel::setTotalTasks,
            label = { Text("Liczba zadań laboratoryjnych") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        )

        if (configuration.thresholds.isNotEmpty()) {
            Text(
                text = "Progi oceniania",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                itemsIndexed(configuration.thresholds) { index, threshold ->
                    val taskCount = index + 1
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            OutlinedTextField(
                                value = threshold.grade,
                                onValueChange = {
                                    viewModel.updateThreshold(index, grade = it)
                                },
                                label = { Text(getLabelForTaskCount(taskCount)) },
                                keyboardOptions = KeyboardOptions(
                                    keyboardType = KeyboardType.Decimal
                                ),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
        }

        configuration.errorMessage?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (configuration.isSaved) {
            Text(
                text = "Konfiguracja została zapisana.",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Button(
            onClick = viewModel::saveGradingConfiguration,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            enabled = configuration.totalTasks.isNotBlank() && configuration.thresholds.isNotEmpty()
        ) {
            Text("Zapisz konfigurację")
        }
    }
}

private fun getLabelForTaskCount(count: Int): String {
    return when {
        count == 1 -> "Ocena za $count zadanie"
        count in 2..4 -> "Ocena za $count zadania"
        else -> "Ocena za $count zadań"
    }
}
