package com.example.projektandroid.data.model

/**
 * Represent a grading threshold.
 * @param requiredTasks Number of tasks required to achieve the grade.
 * @param grade The grade achieved (e.g., 3.0, 4.0, 5.0).
 */
data class GradingThreshold(
    val requiredTasks: Int,
    val grade: Double
)
