package com.example.projektandroid.data.model

data class Student(
    val id: Long,
    val numerStanowiska: Int,
    val imie: String,
    val nazwisko: String,
    val wykonaneZadania: List<ZadanieLaboratoryjne> = emptyList(),
    val ocenaKoncowa: Int? = null
)
