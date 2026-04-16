package com.scaffold.app.presentation.ui.login.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.scaffold.app.presentation.ui.login.components.EmailField
import com.scaffold.app.presentation.ui.login.components.PasswordField
import com.scaffold.app.presentation.ui.login.viewmodels.LoginUiEvent
import com.scaffold.app.presentation.ui.login.viewmodels.LoginUiState
import com.scaffold.app.presentation.ui.login.viewmodels.LoginViewModel
import com.scaffold.app.ui.theme.AppTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is LoginUiEvent.NavigateToHome -> onLoginSuccess()
            }
        }
    }

    LoginContent(
        uiState = uiState,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onRememberMeChange = viewModel::onRememberMeChange,
        onLoginClick = viewModel::onLoginClick,
        onDismissError = { /* Implementar no ViewModel se necessário */ }
    )
}

@Composable
private fun LoginContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRememberMeChange: (Boolean) -> Unit,
    onLoginClick: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Entrar",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )

        EmailField(
            value = uiState.email,
            onValueChange = onEmailChange,
            isError = uiState.emailError != null,
            errorMessage = uiState.emailError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        PasswordField(
            value = uiState.password,
            onValueChange = onPasswordChange,
            isError = uiState.passwordError != null,
            errorMessage = uiState.passwordError,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.align(Alignment.Start)
        ) {
            Checkbox(
                checked = uiState.rememberMe,
                onCheckedChange = onRememberMeChange
            )
            Text("Lembrar-me")
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = onLoginClick,
            enabled = uiState.email.isNotEmpty() && uiState.password.isNotEmpty() && !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp))
            } else {
                Text("Entrar")
            }
        }
    }

    if (uiState.generalError != null) {
        AlertDialog(
            onDismissRequest = onDismissError,
            title = { Text("Erro") },
            text = { Text(uiState.generalError) },
            confirmButton = {
                TextButton(onClick = onDismissError) { Text("OK") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginScreenPreview() {
    AppTheme {
        LoginContent(
            uiState = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onRememberMeChange = {},
            onLoginClick = {},
            onDismissError = {}
        )
    }
}
