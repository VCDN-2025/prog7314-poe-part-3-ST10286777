package com.arcadia.trivora

data class UpdateFCMTokenRequest(
    val fcmToken: String
)

data class TestNotificationRequest(
    val title: String,
    val message: String
)