package com.example.v2

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    var selectedRole by remember { mutableStateOf(UserRole.CHILD) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        // Logo
        Image(
            painter = painterResource(id = R.drawable.loggggoo),
            contentDescription = "App Logo",
            modifier = Modifier.size(120.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Create Account",
            style = MaterialTheme.typography.headlineLarge
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Register a new account",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Role Selection Tabs
        TabRow(selectedTabIndex = if (selectedRole == UserRole.CHILD) 0 else 1) {
            Tab(
                selected = selectedRole == UserRole.CHILD,
                onClick = { selectedRole = UserRole.CHILD },
                text = { Text("Child") }
            )
            Tab(
                selected = selectedRole == UserRole.PARENT,
                onClick = { selectedRole = UserRole.PARENT },
                text = { Text("Parent") }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Registration Form
        RegistrationForm(
            role = selectedRole,
            onRegisterSuccess = onRegisterSuccess,
            onNavigateToLogin = onNavigateToLogin
        )
    }
}

@Composable
fun RegistrationForm(
    role: UserRole,
    onRegisterSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var childId by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                errorMessage = null
            },
            label = { Text("Email") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                errorMessage = null
            },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                errorMessage = null
            },
            label = { Text("Confirm Password") },
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // For parent role, ask for child ID
        if (role == UserRole.PARENT) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = childId,
                onValueChange = {
                    childId = it
                    errorMessage = null
                },
                label = { Text("Child ID (to monitor)") },
                supportingText = { Text("Enter the child's user ID to monitor") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (errorMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = errorMessage ?: "",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        if (successMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = successMessage ?: "",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                // Validation
                if (email.isBlank() || password.isBlank() || confirmPassword.isBlank()) {
                    errorMessage = "Please fill in all fields"
                    return@Button
                }

                if (password != confirmPassword) {
                    errorMessage = "Passwords do not match"
                    return@Button
                }

                if (password.length < 6) {
                    errorMessage = "Password must be at least 6 characters"
                    return@Button
                }

                if (role == UserRole.PARENT && childId.isBlank()) {
                    errorMessage = "Please enter a child ID to monitor"
                    return@Button
                }

                isLoading = true
                scope.launch {
                    val user = User(
                        email = email,
                        password = password,
                        role = role,
                        childId = if (role == UserRole.PARENT) childId else ""
                    )

                    val result = AuthRepository.registerUser(user)
                    isLoading = false

                    result.onSuccess {
                        successMessage = "Registration successful! Please login."
                        // Clear form
                        email = ""
                        password = ""
                        confirmPassword = ""
                        childId = ""

                        // Navigate to login after a short delay
                        kotlinx.coroutines.delay(1500)
                        onRegisterSuccess()
                    }.onFailure { error ->
                        errorMessage = error.message ?: "Registration failed"
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Register as ${role.name}")
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = onNavigateToLogin) {
            Text("Already have an account? Login")
        }
    }
}
