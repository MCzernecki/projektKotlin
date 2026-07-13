package com.example.projektandroid.data.model

data class ZadanieLaboratoryjne(
    val numerZadania: Int,
    // Kept as task metadata; final grades are currently calculated by GradeConfigurator.
    val ocena: Int
)
