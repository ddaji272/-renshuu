package com.example.n.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.n.R
import com.example.n.viewmodel.AuthViewModel

@Composable
fun AuthScreen(
    viewModel: AuthViewModel = viewModel(),
    onLoginSuccess: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val successMessage by viewModel.successMessage.collectAsState()
    val loginSuccess by viewModel.loginSuccess.collectAsState()

    var isLoginMode by remember { mutableStateOf(true) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(loginSuccess) {
        if (loginSuccess) onLoginSuccess()
    }

    // THÊM: Tự động chuyển qua tab Đăng Nhập nếu Đăng ký thành công
    LaunchedEffect(successMessage) {
        if (successMessage?.contains("Đăng ký thành công") == true) {
            isLoginMode = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 30.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = if (isLoginMode) "Sign in to Renshuu" else "Join Renshuu",
            fontSize = 28.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(bottom = 40.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("username/ email") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("password") },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation('*'),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Ẩn/Hiện mật khẩu")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        )

        if (isLoginMode) {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                Text(
                    text = "Forgot password?",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .clickable { viewModel.forgotPassword(email) }
                )
            }
        }

        // Báo lỗi (Màu đỏ)
        if (errorMessage != null) {
            Text(text = errorMessage!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
        }

        // CẬP NHẬT: Hộp thông báo thành công Đẹp mắt
        if (successMessage != null) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                color = Color(0xFFE8F5E9), // Nền xanh lá nhạt
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFF4CAF50)) // Viền xanh lá đậm
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Thành công",
                        tint = Color(0xFF4CAF50),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = successMessage!!,
                        color = Color(0xFF2E7D32),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (isLoginMode) viewModel.login(email, password)
                else viewModel.register(email, password)
            },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
            } else {
                Text(if (isLoginMode) "Sign in" else "Sign up", fontSize = 18.sp)
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            HorizontalDivider(modifier = Modifier.weight(1f))
            Text(" or ", color = Color.Gray, modifier = Modifier.padding(horizontal = 8.dp))
            HorizontalDivider(modifier = Modifier.weight(1f))
        }
        Spacer(modifier = Modifier.height(24.dp))

        OutlinedButton(
            onClick = { viewModel.loginWithGoogle(context) },
            modifier = Modifier.fillMaxWidth().height(55.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = !isLoading
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.ic_google_logo),
                    contentDescription = "Google Logo",
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text("continue with Google", color = Color.Black, fontSize = 16.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
        TextButton(onClick = {
            isLoginMode = !isLoginMode
            viewModel.resetState()
        }, enabled = !isLoading) {
            Text(
                text = if (isLoginMode) "New to Renshuu? Create an account"
                else "Already have an account? Sign in",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}