package com.example.v2

enum class UserRole {
    PARENT,
    CHILD
}

data class User(
    val id: String = "",
    val email: String = "",
    val password: String = "",
    val role: UserRole = UserRole.CHILD,
    val childId: String = "" // For parent accounts, this links to their child
)
