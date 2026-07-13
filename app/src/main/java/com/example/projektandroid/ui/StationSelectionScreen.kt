package com.example.projektandroid.ui

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.projektandroid.data.export.CsvUriWriteResult
import com.example.projektandroid.data.export.CsvUriWriter
import com.example.projektandroid.data.validation.ValidationResult

@Composable
fun StationSelectionScreen(viewModel: AttendanceViewModel) {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val students by viewModel.studentsList.collectAsState()
    val csvExportMessage by viewModel.csvExportMessage.collectAsState()
    val pendingCsvExport by viewModel.pendingCsvExport.collectAsState()
    val latestPendingCsvExport by rememberUpdatedState(pendingCsvExport)
    val csvUriWriter = remember { CsvUriWriter() }
    var showResetDialog by remember { mutableStateOf(false) }
    val stations = (1..10).toList()

    val createCsvDocumentLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        val export = latestPendingCsvExport
        when {
            uri == null -> viewModel.cancelCsvExport()
            export == null -> viewModel.reportCsvExportFailure()
            else -> when (
                csvUriWriter.write(
                    contentResolver = contentResolver,
                    uri = uri,
                    content = export.content
                )
            ) {
                CsvUriWriteResult.Success -> viewModel.confirmCsvExportSaved()
                is CsvUriWriteResult.Error -> viewModel.reportCsvExportFailure()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Wybierz stanowisko",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(stations) { stationNumber ->
                val studentCount = students.count {
                    it.numerStanowiska == stationNumber
                }
                Button(
                    onClick = { viewModel.selectStation(stationNumber) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(88.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Stanowisko $stationNumber",
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = studentCountLabel(studentCount),
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Button(
            onClick = {
                if (viewModel.prepareCsvExport() is ValidationResult.Success) {
                    viewModel.pendingCsvExport.value?.let { export ->
                        createCsvDocumentLauncher.launch(export.fileName)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("Zapisz listę do CSV")
        }

        OutlinedButton(
            onClick = { showResetDialog = true },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("Rozpocznij nowe zajęcia")
        }

        csvExportMessage?.let { message ->
            Text(
                text = message,
                color = if (message.startsWith("Nie")) {
                    MaterialTheme.colorScheme.error
                } else {
                    MaterialTheme.colorScheme.primary
                },
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }

    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            title = { Text("Rozpocząć nowe zajęcia?") },
            text = {
                Text(
                    "Czy na pewno chcesz rozpocząć nowe zajęcia? " +
                        "Obecna lista studentów, zadania i oceny zostaną usunięte."
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResetDialog = false
                        viewModel.resetForNewClass()
                    }
                ) {
                    Text("Tak, rozpocznij od nowa")
                }
            },
            dismissButton = {
                TextButton(onClick = { showResetDialog = false }) {
                    Text("Anuluj")
                }
            }
        )
    }
}

private fun studentCountLabel(count: Int): String {
    return when (count) {
        0 -> "Brak studentów"
        1 -> "1 student"
        else -> "$count studentów"
    }
}
