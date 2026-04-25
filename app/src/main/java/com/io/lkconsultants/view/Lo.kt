package com.io.lkconsultants.view

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lint.kotlin.metadata.Visibility
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.io.lkconsultants.R
import com.io.lkconsultants.color.LKColorScheme
import com.io.lkconsultants.color.lkColors

import com.io.lkconsultants.ui.theme.LkConsultantsTheme
import com.io.lkconsultants.view.LoginScreen
import com.io.lkconsultants.view.SplaceScreen

import com.io.lkconsultants.navscreen.Screens
import com.io.lkconsultants.ui.theme.AccentBlueMid
import com.io.lkconsultants.ui.theme.Background
import com.io.lkconsultants.ui.theme.BrandRed
import com.io.lkconsultants.ui.theme.Divider
import com.io.lkconsultants.ui.theme.PrimaryBlue
import com.io.lkconsultants.ui.theme.Subtitle
import com.io.lkconsultants.viewmodel.LoginState
import com.io.lkconsultants.viewmodel.LoginViewModel



@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    var email       by remember { mutableStateOf("") }
    var password    by remember { mutableStateOf("") }
    var passVisible by remember { mutableStateOf(false) }
    val context     = LocalContext.current
    val colors      = lkColors                          // ✅ lowercase — dynamic accessor

    val state     by viewModel.state.collectAsStateWithLifecycle()
    val isLoading  = state is LoginState.Loading

    LaunchedEffect(state) {
        when (state) {
            is LoginState.Success -> onLoginSuccess()
            is LoginState.Error   -> Toast.makeText(
                context, (state as LoginState.Error).message, Toast.LENGTH_LONG
            ).show()
            else -> Unit
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colors.background)              // ✅ camelCase
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter            = painterResource(R.drawable.lklogo),
                contentScale       = ContentScale.Crop,
                contentDescription = "",
                modifier           = Modifier
                    .size(150.dp)
                    .background(colors.background)      // ✅ camelCase
            )

            Spacer(Modifier.height(40.dp))

            // Email
            OutlinedTextField(
                value           = email,
                onValueChange   = { email = it },
                label           = { Text("Email", color = colors.subtitle) },
                leadingIcon     = { Icon(Icons.Outlined.Email, null, tint = colors.accentBlueMid) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                singleLine      = true,
                enabled         = !isLoading,
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(10.dp),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = colors.primaryBlue,
                    unfocusedBorderColor = colors.divider,
                    cursorColor          = colors.primaryBlue,
                    focusedLabelColor    = colors.primaryBlue,
                    unfocusedLabelColor  = colors.subtitle,
                    focusedTextColor     = colors.onSurface,  // ✅ onSurface not subtitle
                    unfocusedTextColor   = colors.onSurface
                )
            )

            Spacer(Modifier.height(14.dp))

            // Password
            OutlinedTextField(
                value           = password,
                onValueChange   = { password = it },
                label           = { Text("Password", color = colors.subtitle) },
                leadingIcon     = { Icon(Icons.Outlined.Lock, null, tint = colors.accentBlueMid) },
                visualTransformation = if (passVisible) VisualTransformation.None
                else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                singleLine      = true,
                enabled         = !isLoading,
                modifier        = Modifier.fillMaxWidth(),
                shape           = RoundedCornerShape(10.dp),
                colors          = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor   = colors.primaryBlue,
                    unfocusedBorderColor = colors.divider,
                    cursorColor          = colors.primaryBlue,
                    focusedLabelColor    = colors.primaryBlue,
                    unfocusedLabelColor  = colors.subtitle,
                    focusedTextColor     = colors.onSurface,
                    unfocusedTextColor   = colors.onSurface
                )
            )

            Spacer(Modifier.height(28.dp))

            // Login button
            Button(
                onClick  = { viewModel.login(email, password) },
                enabled  = !isLoading,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape    = RoundedCornerShape(10.dp),
                colors   = ButtonDefaults.buttonColors(containerColor = colors.primaryBlue)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color       = colors.white,
                        modifier    = Modifier.size(22.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        "Login",
                        style = TextStyle(
                            fontSize      = 15.sp,
                            fontWeight    = FontWeight.SemiBold,
                            color         = colors.white,
                            letterSpacing = 0.5.sp
                        )
                    )
                }
            }
        }
    }
}

//@Composable
//fun LoginScreen(
//    onLoginSuccess: () -> Unit,
//    viewModel: LoginViewModel = viewModel()
//) {
//    var email    by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var passVisible by remember { mutableStateOf(false) }
//    val context = LocalContext.current
//
//    val state by viewModel.state.collectAsStateWithLifecycle()
//
//
//
//    // React to state changes
//    LaunchedEffect(state) {
//        when (state) {
//            is LoginState.Success -> onLoginSuccess()
//            is LoginState.Error   -> Toast.makeText(
//                context, (state as LoginState.Error).message, Toast.LENGTH_LONG
//            ).show()
//            else -> Unit
//        }
//    }
//
//    val isLoading = state is LoginState.Loading
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Color.White)
//    )
//    {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.Center)
//                .verticalScroll(rememberScrollState())
//                .imePadding()
//                .padding(horizontal = 32.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // Logo
//            Image(
//                painter = painterResource(R.drawable.lklogo),
//                contentScale = ContentScale.Crop,
//                contentDescription = "",
//                modifier = Modifier.size(150.dp).background(color = Color.White)
//            )
//
//            Spacer(Modifier.height(40.dp))
//
//            // Email (API uses email, not phone)
//
//            OutlinedTextField(
//                value = email,
//                onValueChange = { email = it },
//                label = { Text("Email", color = Subtitle) },
//                leadingIcon = { Icon(Icons.Outlined.Email, null, tint = AccentBlueMid) },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
//                singleLine = true,
//                enabled = !isLoading,
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(10.dp),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = PrimaryBlue,
//                    unfocusedBorderColor = Divider,
//                    cursorColor = PrimaryBlue,
//                    focusedLabelColor = PrimaryBlue,
//                    unfocusedLabelColor = Subtitle
//                )
//            )
//
//            Spacer(Modifier.height(14.dp))
//
//
//
//            // Password
//            OutlinedTextField(
//                value = password,
//                onValueChange = { password = it },
//                label = { Text("Password", color = Subtitle) },
//                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = AccentBlueMid) },
//                visualTransformation = if (passVisible) VisualTransformation.None
//                else PasswordVisualTransformation(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                singleLine = true,
//                enabled = !isLoading,
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(10.dp),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = PrimaryBlue,
//                    unfocusedBorderColor = Divider,
//                    cursorColor = PrimaryBlue,
//                    focusedLabelColor = PrimaryBlue
//                )
//            )
//
//            Spacer(Modifier.height(28.dp))
//
//            // Login button
//            Button(
//                onClick = { viewModel.login(email, password) },
//                enabled = !isLoading,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp),
//                shape = RoundedCornerShape(10.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
//            ) {
//                if (isLoading) {
//                    CircularProgressIndicator(
//                        color = PrimaryBlue,
//                        modifier = Modifier.size(22.dp),
//                        strokeWidth = 2.dp
//                    )
//                } else {
//                    Text(
//                        "Login",
//                        style = TextStyle(
//                            fontSize = 15.sp,
//                            fontWeight = FontWeight.SemiBold,
//                            color = Color.White,
//                            letterSpacing = 0.5.sp
//                        )
//                    )
//                }
//            }
//        }
//    }
//}

//@Composable
//fun LoginScreen(onLoginSuccess: () -> Unit) {
//    var phone    by remember { mutableStateOf("") }
//    var password by remember { mutableStateOf("") }
//    var passVisible by remember { mutableStateOf(false) }
//
//    Box(
//        modifier = Modifier
//            .fillMaxSize()
//            .background(Background)
//    ) {
//        Column(
//            modifier = Modifier
//                .fillMaxWidth()
//                .align(Alignment.Center)
//                .padding(horizontal = 32.dp),
//            horizontalAlignment = Alignment.CenterHorizontally
//        ) {
//            // Logo
//            Image(painter = painterResource(R.drawable.lklogo), contentDescription = "",
//                modifier = Modifier.size(64.dp))
//            Spacer(Modifier.height(10.dp))
//            Text(
//                "LK",
//                fontSize = 30.sp,
//                fontWeight = FontWeight.Bold,
//                color = PrimaryBlue,
//                letterSpacing = 3.sp
//            )
//            Text(
//                "DESIGN CONSULTANT",
//                fontSize = 11.sp,
//                fontWeight = FontWeight.Medium,
//                color = BrandRed,
//                letterSpacing = 2.sp
//            )
//
//            Spacer(Modifier.height(40.dp))
//
//            // Phone
//            OutlinedTextField(
//                value = phone,
//                onValueChange = { phone = it },
//                label = { Text("Phone Number", color = Subtitle) },
//                leadingIcon = { Icon(Icons.Outlined.Phone, null, tint = AccentBlueMid) },
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(10.dp),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = PrimaryBlue,
//                    unfocusedBorderColor = Divider,
//                    cursorColor = PrimaryBlue,
//                    focusedLabelColor =PrimaryBlue,
//                    unfocusedLabelColor = Subtitle
//                )
//            )
//
//            Spacer(Modifier.height(14.dp))
//
//            // Password
//            OutlinedTextField(
//                value = password,
//                onValueChange = { password = it },
//                label = { Text("Password", color = Subtitle) },
//                leadingIcon = { Icon(Icons.Outlined.Lock, null, tint = AccentBlueMid) },
//                visualTransformation = if (passVisible) VisualTransformation.None else PasswordVisualTransformation(),
//                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
//                singleLine = true,
//                modifier = Modifier.fillMaxWidth(),
//                shape = RoundedCornerShape(10.dp),
//                colors = OutlinedTextFieldDefaults.colors(
//                    focusedBorderColor = PrimaryBlue,
//                    unfocusedBorderColor = Divider,
//                    cursorColor = PrimaryBlue,
//                    focusedLabelColor = PrimaryBlue
//                )
//            )
//
//            Spacer(Modifier.height(28.dp))
//
//            // Login button
//            Button(
//                onClick = onLoginSuccess,
//                modifier = Modifier
//                    .fillMaxWidth()
//                    .height(50.dp),
//                shape = RoundedCornerShape(10.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
//            ) {
//                Text("Login",
//                    style = TextStyle(
//                        fontSize = 15.sp,
//                        fontWeight = FontWeight.SemiBold,
//                        color = Color.White,
//                        letterSpacing = 0.5.sp
//                    )
//                )
//
//            }
//        }
//    }
//}