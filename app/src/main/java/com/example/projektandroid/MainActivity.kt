package com.example.projektandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.projektandroid.ui.AppStep
import com.example.projektandroid.ui.AttendanceScreen
import com.example.projektandroid.ui.AttendanceViewModel
import com.example.projektandroid.ui.TaskConfigurationScreen
import com.example.projektandroid.ui.theme.ProjektAndroidTheme

class MainActivity : ComponentActivity() {
    private val attendanceViewModel: AttendanceViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ProjektAndroidTheme {
                val currentStep by attendanceViewModel.currentStep.collectAsState()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        when (currentStep) {
                            AppStep.ATTENDANCE -> AttendanceScreen(attendanceViewModel)
                            AppStep.TASK_CONFIGURATION ->
                                TaskConfigurationScreen(attendanceViewModel)
                        }
                    }
                }
            }
        }
    }
}
