package com.example.v2

import android.content.Context
import android.content.SharedPreferences

object SessionManager {

    private const val PREF_NAME = "BehaviorSafetySession"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_EMAIL = "email"
    private const val KEY_ROLE = "role"
    private const val KEY_CHILD_ID = "child_id"
    private const val KEY_IS_LOGGED_IN = "is_logged_in"

    private fun getPreferences(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
    }

    fun saveSession(context: Context, user: User) {
        val editor = getPreferences(context).edit()
        editor.putString(KEY_USER_ID, user.id)
        editor.putString(KEY_EMAIL, user.email)
        editor.putString(KEY_ROLE, user.role.name)
        editor.putString(KEY_CHILD_ID, user.childId)
        editor.putBoolean(KEY_IS_LOGGED_IN, true)
        editor.apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getPreferences(context).getBoolean(KEY_IS_LOGGED_IN, false)
    }

    fun getCurrentUser(context: Context): User? {
        val prefs = getPreferences(context)
        if (!prefs.getBoolean(KEY_IS_LOGGED_IN, false)) {
            return null
        }

        return User(
            id = prefs.getString(KEY_USER_ID, "") ?: "",
            email = prefs.getString(KEY_EMAIL, "") ?: "",
            role = UserRole.valueOf(prefs.getString(KEY_ROLE, "CHILD") ?: "CHILD"),
            childId = prefs.getString(KEY_CHILD_ID, "") ?: ""
        )
    }

    fun clearSession(context: Context) {
        val editor = getPreferences(context).edit()
        editor.clear()
        editor.apply()
    }
}
