package com.example.projektandroid.data.repository

import com.example.projektandroid.data.model.GradingThreshold
import com.example.projektandroid.data.validation.ValidationResult
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GradeConfiguratorTest {

    @Test
    fun setConfiguration_withValidData_returnsSuccess() {
        val configurator = GradeConfigurator()
        val rules = listOf(
            GradingThreshold(1, 3.0),
            GradingThreshold(2, 4.0),
            GradingThreshold(3, 5.0)
        )
        
        val result = configurator.setConfiguration(3, rules)
        
        assertTrue(result is ValidationResult.Success)
        assertEquals(3, configurator.getTotalTasks())
        assertEquals(3, configurator.getThresholds().size)
    }

    @Test
    fun setConfiguration_withInvalidTotalTasks_returnsError() {
        val configurator = GradeConfigurator()
        val result = configurator.setConfiguration(0, emptyList())
        
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun setConfiguration_withTaskCountExceedingTotal_returnsError() {
        val configurator = GradeConfigurator()
        val rules = listOf(GradingThreshold(5, 4.0))
        
        val result = configurator.setConfiguration(3, rules)
        
        assertTrue(result is ValidationResult.Error)
    }

    @Test
    fun getSuggestedGrade_calculatesCorrectGrades() {
        val configurator = GradeConfigurator()
        val rules = listOf(
            GradingThreshold(1, 3.0),
            GradingThreshold(2, 4.0),
            GradingThreshold(3, 5.0)
        )
        configurator.setConfiguration(3, rules)
        
        assertEquals(2.0, configurator.getSuggestedGrade(0), 0.01)
        assertEquals(3.0, configurator.getSuggestedGrade(1), 0.01)
        assertEquals(4.0, configurator.getSuggestedGrade(2), 0.01)
        assertEquals(5.0, configurator.getSuggestedGrade(3), 0.01)
        assertEquals(5.0, configurator.getSuggestedGrade(4), 0.01) // Coerced to max
    }

    @Test
    fun getSuggestedGrade_handlesUnorderedRules() {
        val configurator = GradeConfigurator()
        val rules = listOf(
            GradingThreshold(3, 5.0),
            GradingThreshold(1, 3.0),
            GradingThreshold(2, 4.0)
        )
        configurator.setConfiguration(3, rules)
        
        assertEquals(3.0, configurator.getSuggestedGrade(1), 0.01)
        assertEquals(4.0, configurator.getSuggestedGrade(2), 0.01)
        assertEquals(5.0, configurator.getSuggestedGrade(3), 0.01)
    }

    @Test
    fun clearConfiguration_resetsState() {
        val configurator = GradeConfigurator()
        configurator.setConfiguration(3, listOf(GradingThreshold(1, 3.0)))
        
        configurator.clearConfiguration()
        
        assertEquals(0, configurator.getTotalTasks())
        assertTrue(configurator.getThresholds().isEmpty())
        assertEquals(2.0, configurator.getSuggestedGrade(1), 0.01)
    }
}
