package com.arcadia.trivora

import java.io.Serializable

data class Question(
    val questionId: String,
    val category: String,
    val questionText: String,
    val choices: List<String>,
    val answer: String,
    val difficulty: String
) : Serializable
