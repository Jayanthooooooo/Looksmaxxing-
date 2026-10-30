package com.example.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.ui.components.LumoraTopBar
import com.example.ui.components.PrimaryButton
import com.example.ui.components.QuietTextButton
import com.example.ui.components.SecondaryButton

@Composable
fun AuthChoiceScreen(
    onGoogleSignIn: () -> Unit,
    onEmailSignUp: () -> Unit,
    onSignIn: () -> Unit,
    onPrivacyClick: () -> Unit,
    onTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "Get Started") },
        modifier = modifier.testTag("auth_choice_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Create your private journal",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Sign in to securely sync your progress or keep it strictly on this device.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                PrimaryButton(
                    text = "Continue with Google",
                    onClick = onGoogleSignIn,
                    isLoading = isLoading,
                    testTag = "google_sign_in_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryButton(
                    text = "Continue with Email",
                    onClick = onEmailSignUp,
                    testTag = "email_sign_up_button"
                )
                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Already have an account? ",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(onClick = onSignIn) {
                        Text(
                            text = "Sign in",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    QuietTextButton(text = "Privacy Policy", onClick = onPrivacyClick)
                    Text(text = " · ", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    QuietTextButton(text = "Terms of Service", onClick = onTermsClick)
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(
    onSignUp: (String, String) -> Unit,
    onBackClick: () -> Unit,
    onTermsClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var ageConfirmed by remember { mutableStateOf(false) }
    var termsAccepted by remember { mutableStateOf(false) }

    val isPasswordValid = password.length >= 8
    val isFormValid = email.isNotBlank() && isPasswordValid && ageConfirmed && termsAccepted

    Scaffold(
        topBar = { LumoraTopBar(title = "Sign Up", onBackClick = onBackClick) },
        modifier = modifier.testTag("sign_up_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Create your Lumora account",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Your password is never stored in plain text and photos remain private by default.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().testTag("signup_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password (min 8 characters)") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().testTag("signup_password_input")
            )

            // Age confirmation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = ageConfirmed,
                    onCheckedChange = { ageConfirmed = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("age_13_checkbox")
                )
                Text(
                    text = "I confirm that I am at least 13 years old",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Terms confirmation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Checkbox(
                    checked = termsAccepted,
                    onCheckedChange = { termsAccepted = it },
                    colors = CheckboxDefaults.colors(checkedColor = MaterialTheme.colorScheme.primary),
                    modifier = Modifier.testTag("accept_terms_checkbox")
                )
                Text(
                    text = "I accept the Terms and understand photos stay private",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            PrimaryButton(
                text = "Create account",
                onClick = { onSignUp(email.trim(), password) },
                enabled = isFormValid,
                isLoading = isLoading,
                testTag = "signup_submit_button"
            )
        }
    }
}

@Composable
fun SignInScreen(
    onSignIn: (String, String) -> Unit,
    onGoogleSignIn: () -> Unit,
    onForgotPassword: () -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    errorMessage: String? = null
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { LumoraTopBar(title = "Sign In", onBackClick = onBackClick) },
        modifier = modifier.testTag("sign_in_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Welcome back to Lumora",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )

            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email address") },
                leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth().testTag("signin_email_input")
            )

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "Hide password" else "Show password"
                        )
                    }
                },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth().testTag("signin_password_input")
            )

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                QuietTextButton(
                    text = "Forgot password?",
                    onClick = onForgotPassword,
                    testTag = "forgot_password_button"
                )
            }

            PrimaryButton(
                text = "Sign in",
                onClick = { onSignIn(email.trim(), password) },
                enabled = email.isNotBlank() && password.isNotBlank(),
                isLoading = isLoading,
                testTag = "signin_submit_button"
            )

            Spacer(modifier = Modifier.height(8.dp))

            SecondaryButton(
                text = "Sign in with Google",
                onClick = onGoogleSignIn,
                isLoading = isLoading,
                testTag = "signin_google_button"
            )
        }
    }
}

@Composable
fun ForgotPasswordScreen(
    onSendReset: (String) -> Unit,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    isLoading: Boolean = false,
    confirmationSent: Boolean = false
) {
    var email by remember { mutableStateOf("") }

    Scaffold(
        topBar = { LumoraTopBar(title = "Reset Password", onBackClick = onBackClick) },
        modifier = modifier.testTag("forgot_password_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Reset your password",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Enter your account email. If an account exists, you will receive instructions to reset your password.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (confirmationSent) {
                Text(
                    text = "If an account matches that email address, a password reset email has been sent. Check your inbox and spam folder.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    modifier = Modifier.fillMaxWidth().testTag("reset_email_input")
                )

                PrimaryButton(
                    text = "Send reset email",
                    onClick = { onSendReset(email.trim()) },
                    enabled = email.isNotBlank(),
                    isLoading = isLoading,
                    testTag = "reset_submit_button"
                )
            }
        }
    }
}

@Composable
fun VerifyEmailScreen(
    onResendEmail: () -> Unit,
    onVerifiedCheck: () -> Unit,
    onContinueForNow: () -> Unit,
    modifier: Modifier = Modifier,
    resendCooldownSec: Int = 0
) {
    Scaffold(
        topBar = { LumoraTopBar(title = "Verify Email") },
        modifier = modifier.testTag("verify_email_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text(
                    text = "Check your email",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "We've sent a verification link to your email address. Verifying helps protect your account and data.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }

            Column(modifier = Modifier.fillMaxWidth()) {
                PrimaryButton(
                    text = "I've verified my email",
                    onClick = onVerifiedCheck,
                    testTag = "verified_check_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                SecondaryButton(
                    text = if (resendCooldownSec > 0) "Resend email ($resendCooldownSec s)" else "Resend email",
                    onClick = onResendEmail,
                    enabled = resendCooldownSec == 0,
                    testTag = "resend_email_button"
                )
                Spacer(modifier = Modifier.height(12.dp))
                QuietTextButton(
                    text = "Continue for now",
                    onClick = onContinueForNow,
                    testTag = "continue_for_now_button"
                )
            }
        }
    }
}
