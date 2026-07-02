package com.example.projektandroid.ui

data class GradingThresholdInput(
    val requiredTasks: String = "",
    val grade: String = ""
)

data class GradingConfigurationState(
    val totalTasks: String = "",
    val thresholds: List<GradingThresholdInput> = listOf(GradingThresholdInput()),
    val errorMessage: String? = null,
    val isSaved: Boolean = false
)
