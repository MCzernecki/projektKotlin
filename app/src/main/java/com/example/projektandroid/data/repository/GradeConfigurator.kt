package com.example.projektandroid.data.repository

import com.example.projektandroid.data.model.GradingThreshold
import com.example.projektandroid.data.validation.ValidationResult

/**
 * Manages laboratory grading configurations and rules.
 */
class GradeConfigurator {
    private var totalTasks: Int = 0
    private val thresholds = mutableListOf<GradingThreshold>()

    /**
     * Configures the grading rules.
     * @param totalTasks Total number of tasks in the laboratory.
     * @param rules List of thresholds mapping task counts to grades.
     */
    fun setConfiguration(totalTasks: Int, rules: List<GradingThreshold>): ValidationResult {
        if (totalTasks <= 0) {
            return ValidationResult.Error("Total number of tasks must be greater than 0.")
        }

        for (rule in rules) {
            if (rule.requiredTasks < 0) {
                return ValidationResult.Error("Required tasks count cannot be negative.")
            }
            if (rule.requiredTasks > totalTasks) {
                return ValidationResult.Error("Required tasks (${rule.requiredTasks}) cannot exceed total tasks ($totalTasks).")
            }
            if (rule.grade < 2.0 || rule.grade > 5.0) {
                return ValidationResult.Error("Grade must be between 2.0 and 5.0.")
            }
        }

        this.totalTasks = totalTasks
        this.thresholds.clear()
        // Sort thresholds by required tasks descending to simplify grade lookup
        this.thresholds.addAll(rules.sortedByDescending { it.requiredTasks })
        
        return ValidationResult.Success
    }

    /**
     * Calculates a suggested grade based on the number of completed tasks.
     * @param completedTasksCount Number of tasks completed by the student.
     * @return The highest grade the student qualifies for, or 2.0 if no threshold is met.
     */
    fun getSuggestedGrade(completedTasksCount: Int): Double {
        if (totalTasks == 0) return 2.0 // Not configured
        
        val count = completedTasksCount.coerceIn(0, totalTasks)
        
        // Find the first threshold that is met (since they are sorted descending by task count)
        val metThreshold = thresholds.find { count >= it.requiredTasks }
        
        return metThreshold?.grade ?: 2.0
    }

    /**
     * Resets the grading configuration.
     */
    fun clearConfiguration() {
        totalTasks = 0
        thresholds.clear()
    }

    fun getTotalTasks(): Int = totalTasks
    
    fun getThresholds(): List<GradingThreshold> = thresholds.toList()
}
