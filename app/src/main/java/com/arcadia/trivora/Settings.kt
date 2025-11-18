package com.arcadia.trivora

data class Settings(
val soundEnabled: Boolean = true,
val selectedDifficulties: Set<String> = setOf("Easy", "Medium", "Hard"),
val vibrationEnabled: Boolean = true,

)

