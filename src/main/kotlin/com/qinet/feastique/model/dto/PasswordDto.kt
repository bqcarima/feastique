package com.qinet.feastique.model.dto

data class PasswordDto(
    val currentPassword: String,
    val newPassword: String,
    val confirmedNewPassword: String
)
