package com.example.v2

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

object AuthRepository {

    private val db = FirebaseFirestore.getInstance()
    private const val USERS_COLLECTION = "users"

    // Register a new user
    suspend fun registerUser(user: User): Result<User> {
        return try {
            // Check if email already exists
            val existingUser = db.collection(USERS_COLLECTION)
                .whereEqualTo("email", user.email)
                .get()
                .await()

            if (!existingUser.isEmpty) {
                return Result.failure(Exception("Email already registered"))
            }

            // Create new user document
            val userId = db.collection(USERS_COLLECTION).document().id
            val newUser = user.copy(id = userId)

            db.collection(USERS_COLLECTION)
                .document(userId)
                .set(newUser)
                .await()

            Log.d("AUTH", "User registered: ${newUser.email}")
            Result.success(newUser)
        } catch (e: Exception) {
            Log.e("AUTH", "Registration failed", e)
            Result.failure(e)
        }
    }

    // Login user
    suspend fun loginUser(email: String, password: String, role: UserRole): Result<User> {
        return try {
            val querySnapshot = db.collection(USERS_COLLECTION)
                .whereEqualTo("email", email)
                .whereEqualTo("password", password)
                .whereEqualTo("role", role.name)
                .get()
                .await()

            if (querySnapshot.isEmpty) {
                return Result.failure(Exception("Invalid credentials or role"))
            }

            val document = querySnapshot.documents.first()
            val user = User(
                id = document.id,
                email = document.getString("email") ?: "",
                password = document.getString("password") ?: "",
                role = UserRole.valueOf(document.getString("role") ?: "CHILD"),
                childId = document.getString("childId") ?: ""
            )

            Log.d("AUTH", "Login successful: ${user.email}")
            Result.success(user)
        } catch (e: Exception) {
            Log.e("AUTH", "Login failed", e)
            Result.failure(e)
        }
    }

    // Get user by ID
    suspend fun getUserById(userId: String): Result<User> {
        return try {
            val document = db.collection(USERS_COLLECTION)
                .document(userId)
                .get()
                .await()

            if (!document.exists()) {
                return Result.failure(Exception("User not found"))
            }

            val user = User(
                id = document.id,
                email = document.getString("email") ?: "",
                password = document.getString("password") ?: "",
                role = UserRole.valueOf(document.getString("role") ?: "CHILD"),
                childId = document.getString("childId") ?: ""
            )

            Result.success(user)
        } catch (e: Exception) {
            Log.e("AUTH", "Failed to get user", e)
            Result.failure(e)
        }
    }
}
