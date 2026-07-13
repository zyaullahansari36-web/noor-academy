package com.example

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.*
import com.example.ui.AppViewModel
import com.example.ui.User
import com.example.ui.AppNotification
import com.example.ui.QuizQuestion
import com.example.ui.theme.NoorAcademyTheme
import com.example.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.data.model.Course
import com.example.data.model.Lesson

// Shadowing GoldAccent from theme so it dynamically switches to the Elegant Dark mint color or the light theme color
val GoldAccent: Color
    @Composable
    get() = MaterialTheme.colorScheme.primary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val appViewModel: AppViewModel = viewModel()
            val isDark by appViewModel.isDarkMode.collectAsState()

            NoorAcademyTheme(darkTheme = isDark) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "splash"
                    ) {
                        composable("splash") {
                            SplashScreen(navController, appViewModel)
                        }
                        composable("login") {
                            LoginScreen(navController, appViewModel)
                        }
                        composable("signup") {
                            SignupScreen(navController, appViewModel)
                        }
                        composable("dashboard") {
                            DashboardScreen(navController, appViewModel)
                        }
                        composable("course_detail") {
                            CourseDetailScreen(navController, appViewModel)
                        }
                        composable("lesson_play") {
                            LessonPlayScreen(navController, appViewModel)
                        }
                        composable("quiz") {
                            QuizScreen(navController, appViewModel)
                        }
                    }
                }
            }
        }
    }
}

// 1. SPLASH SCREEN
@Composable
fun SplashScreen(navController: NavController, viewModel: AppViewModel) {
    val scale = remember { Animatable(0.2f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(key1 = true) {
        launch {
            scale.animateTo(
                targetValue = 1f,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow
                )
            )
        }
        launch {
            alpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1000)
            )
        }
        delay(2500)
        
        // Navigate based on auth status
        val user = viewModel.currentUser.value
        if (user != null) {
            navController.navigate("dashboard") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.25f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer(
                scaleX = scale.value,
                scaleY = scale.value,
                alpha = alpha.value
            )
        ) {
            // Elegant Gold Arabesque Logo
            Image(
                painter = painterResource(id = R.drawable.img_app_icon_1783931802194),
                contentDescription = "Noor Academy Logo",
                modifier = Modifier
                    .size(140.dp)
                    .clip(RoundedCornerShape(32.dp))
                    .border(2.dp, GoldAccent, RoundedCornerShape(32.dp))
                    .shadow(16.dp, RoundedCornerShape(32.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "NOOR ACADEMY",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = viewModel.getTranslation("tagline"),
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = androidx.compose.ui.text.font.FontFamily.Serif,
                    color = GoldAccent,
                    fontWeight = FontWeight.Medium
                ),
                textAlign = TextAlign.Center
            )
        }
    }
}

// 2. LOGIN SCREEN
@Composable
fun LoginScreen(navController: NavController, viewModel: AppViewModel) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var otp by remember { mutableStateOf("") }
    
    var loginMode by remember { mutableStateOf(0) } // 0: Email, 1: Phone OTP
    val authError by viewModel.authError.collectAsState()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(id = R.drawable.img_app_icon_1783931802194),
                    contentDescription = "Noor Academy Logo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.5.dp, GoldAccent, RoundedCornerShape(24.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Sign In to Noor Academy",
                    style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Your premier gate to spiritual and technical wisdom",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.65f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Tab Switcher
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .padding(4.dp)
                ) {
                    TabButton(
                        text = "Email & Pass",
                        isSelected = loginMode == 0,
                        modifier = Modifier.weight(1f)
                    ) { loginMode = 0 }
                    TabButton(
                        text = "Phone OTP",
                        isSelected = loginMode == 1,
                        modifier = Modifier.weight(1f)
                    ) { loginMode = 1 }
                }

                Spacer(modifier = Modifier.height(24.dp))

                if (loginMode == 0) {
                    // Email & Pass form
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        label = { Text("Email Address") },
                        leadingIcon = { Icon(Icons.Default.Mail, contentDescription = "Mail Icon") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input"),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Lock Icon") },
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("password_input"),
                        shape = RoundedCornerShape(12.dp)
                    )
                } else {
                    // Phone & OTP Form
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone Icon") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        label = { Text("OTP (Enter '123456')") },
                        leadingIcon = { Icon(Icons.Default.Security, contentDescription = "OTP Shield") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                if (authError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = authError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val success = if (loginMode == 0) {
                            viewModel.loginWithEmail(email, password)
                        } else {
                            viewModel.loginWithPhoneOTP(phone, otp)
                        }
                        if (success) {
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("login_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Authenticate Now", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Google sign in simulation
                OutlinedButton(
                    onClick = {
                        viewModel.loginWithEmail("student@nooracademy.com", "password123")
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = "Google Icon", tint = GoldAccent)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Continue with Google", color = MaterialTheme.colorScheme.onBackground)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = { viewModel.loginAsGuest(); navController.navigate("dashboard") }
                ) {
                    Text("Continue as Guest Learner", color = GoldAccent, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Don't have an account?")
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { navController.navigate("signup") }) {
                        Text("Sign Up", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// 3. SIGNUP SCREEN
@Composable
fun SignupScreen(navController: NavController, viewModel: AppViewModel) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val authError by viewModel.authError.collectAsState()

    Scaffold { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(32.dp))

                Image(
                    painter = painterResource(id = R.drawable.img_app_icon_1783931802194),
                    contentDescription = "Noor Academy Logo",
                    modifier = Modifier
                        .size(96.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.5.dp, GoldAccent, RoundedCornerShape(24.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Create Noor Account",
                    style = MaterialTheme.typography.headlineMedium.copy(color = MaterialTheme.colorScheme.primary),
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name") },
                    leadingIcon = { Icon(Icons.Default.AccountCircle, contentDescription = "Name Icon") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email Address") },
                    leadingIcon = { Icon(Icons.Default.Mail, contentDescription = "Email Icon") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = phone,
                    onValueChange = { phone = it },
                    label = { Text("Phone Number") },
                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = "Phone Icon") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password (min 6 characters)") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password Lock") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                if (authError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = authError ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val success = viewModel.signup(email, name, password, phone)
                        if (success) {
                            navController.navigate("dashboard") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Register Account", style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold))
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Already registered?")
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { navController.navigate("login") }) {
                        Text("Sign In", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// HELPERS
@Composable
fun TabButton(text: String, isSelected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent)
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
        )
    }
}

// 4. STUDENT & ADMIN DASHBOARD
@Composable
fun DashboardScreen(navController: NavController, viewModel: AppViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val activeLang by viewModel.currentLanguage.collectAsState()
    val isDark by viewModel.isDarkMode.collectAsState()

    var selectedTab by remember { mutableStateOf(0) } // 0: Dashboard, 1: Courses, 2: Wallet, 3: Profile, 4: Admin

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                windowInsets = WindowInsets.navigationBars
            ) {
                NavigationBarItem(
                    icon = { Icon(Icons.Default.School, contentDescription = "Dashboard Icon") },
                    label = { Text(viewModel.getTranslation("dashboard")) },
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Book, contentDescription = "Courses Icon") },
                    label = { Text(viewModel.getTranslation("courses")) },
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = "Wallet Icon") },
                    label = { Text(viewModel.getTranslation("wallet")) },
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 }
                )
                NavigationBarItem(
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile Icon") },
                    label = { Text(viewModel.getTranslation("profile")) },
                    selected = selectedTab == 3,
                    onClick = { selectedTab = 3 }
                )
                if (user?.role == "ADMIN") {
                    NavigationBarItem(
                        icon = { Icon(Icons.Default.Security, contentDescription = "Admin Icon") },
                        label = { Text("Admin") },
                        selected = selectedTab == 4,
                        onClick = { selectedTab = 4 }
                    )
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Elegant Brand Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Image(
                        painter = painterResource(id = R.drawable.img_app_icon_1783931802194),
                        contentDescription = "Logo",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(8.dp))
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = viewModel.getTranslation("app_name"),
                            style = MaterialTheme.typography.titleLarge.copy(color = MaterialTheme.colorScheme.primary),
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Assalamu Alaikum, ${user?.name ?: "Learner"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Language Switcher Toggle
                    IconButton(onClick = {
                        val nextLang = when (activeLang) {
                            "en" -> "hi"
                            "hi" -> "ur"
                            else -> "en"
                        }
                        viewModel.setLanguage(nextLang)
                    }) {
                        Icon(Icons.Default.Translate, contentDescription = "Language", tint = GoldAccent)
                    }

                    // Dark Mode Toggle
                    IconButton(onClick = { viewModel.toggleDarkMode() }) {
                        Icon(
                            imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = "Toggle Theme",
                            tint = GoldAccent
                        )
                    }
                }
            }

            Divider(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))

            Box(modifier = Modifier.weight(1f)) {
                when (selectedTab) {
                    0 -> DashboardTab(navController, viewModel)
                    1 -> CoursesTab(navController, viewModel)
                    2 -> WalletTab(viewModel)
                    3 -> ProfileTab(navController, viewModel)
                    4 -> AdminTab(viewModel)
                }
            }

            // SIMULATED ADMOB BANNER (Show only for non-admin, non-premium/free simulated users)
            if (user?.isGuest == true || user?.role != "ADMIN") {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(GoldAccent.copy(alpha = 0.15f))
                        .border(1.dp, GoldAccent.copy(alpha = 0.3f))
                        .padding(vertical = 6.dp, horizontal = 12.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .background(GoldAccent, RoundedCornerShape(4.dp))
                                .padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Text("Ad", style = MaterialTheme.typography.labelSmall.copy(color = Color.Black, fontWeight = FontWeight.Bold))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Upgrade to Premium to remove ads & unlock all live classes!",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onBackground,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

// 4A. HOME DASHBOARD TAB
@Composable
fun DashboardTab(navController: NavController, viewModel: AppViewModel) {
    val purchased by viewModel.purchasedCourses.collectAsState()
    val allCourses by viewModel.courses.collectAsState()
    val notificationList by viewModel.notifications.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            // Beautiful Dashboard Hero Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .clip(RoundedCornerShape(16.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_hero_banner_1783931818041),
                    contentDescription = "Quran Academy Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                // Overlay Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                colors = listOf(Color.Transparent, Color.Black.copy(alpha = 0.8f))
                            )
                        )
                )
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                ) {
                    Text(
                        text = viewModel.getTranslation("app_name"),
                        color = GoldAccent,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Learn holy scriptures and digital skills together in one place.",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        // Daily Verse of the Day / Hadith Box
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.08f)),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, GoldAccent.copy(alpha = 0.3f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hadith of the Day", style = MaterialTheme.typography.titleLarge.copy(color = GoldAccent))
                        Icon(Icons.Filled.Star, contentDescription = "Star Decoration", tint = GoldAccent)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "\"The best among you are those who learn the Qur'an and teach it.\"",
                        style = MaterialTheme.typography.bodyLarge.copy(fontFamily = androidx.compose.ui.text.font.FontFamily.Serif),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "- Sahih Al-Bukhari 5027",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                    )
                }
            }
        }

        // Continued Watching / Enrolled Courses
        if (purchased.isNotEmpty()) {
            item {
                Text(
                    text = viewModel.getTranslation("continue_watching"),
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(purchased) { course ->
                        Card(
                            modifier = Modifier
                                .width(220.dp)
                                .clickable {
                                    viewModel.selectCourse(course)
                                    navController.navigate("course_detail")
                                },
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        Icons.Default.PlayCircle,
                                        contentDescription = "Play icon",
                                        modifier = Modifier.size(40.dp),
                                        tint = GoldAccent
                                    )
                                    // Language Badge
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .background(GoldAccent, RoundedCornerShape(4.dp))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(course.language, style = MaterialTheme.typography.labelSmall.copy(color = Color.Black))
                                    }
                                }
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        course.title,
                                        style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "Instructor: ${course.instructor}",
                                        style = MaterialTheme.typography.labelSmall,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    LinearProgressIndicator(
                                        progress = 0.45f,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(4.dp)
                                            .clip(CircleShape),
                                        color = GoldAccent
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Classes Section
        item {
            Text(
                text = "Live Classes Today",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .background(Color.Red.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.LiveTv, contentDescription = "Live Class Stream", tint = Color.Red)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Interactive Tajweed Correction", fontWeight = FontWeight.Bold)
                        Text("Live with Qari Abdul Basit - 6:00 PM", style = MaterialTheme.typography.bodyMedium)
                    }
                    Button(
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        onClick = {
                            val active = allCourses.firstOrNull { it.id == 1 }
                            if (active != null) {
                                viewModel.selectCourse(active)
                                navController.navigate("course_detail")
                            }
                        }
                    ) {
                        Text("Join", color = Color.White)
                    }
                }
            }
        }

        // Notification Log Sim
        item {
            Text(
                text = "Notifications & Alerts",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp),
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                notificationList.take(3).forEach { notif ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Bell", tint = GoldAccent)
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(notif.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge.copy(fontSize = 14.sp))
                                Text(notif.body, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

// 4B. COURSES TAB (SEARCH & CATEGORIES)
@Composable
fun CoursesTab(navController: NavController, viewModel: AppViewModel) {
    val coursesList by viewModel.courses.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()

    val categories = listOf("All", "Quran", "Arabic", "Urdu", "Farsi", "Islamic Studies", "Computer", "Spoken English")

    Column(modifier = Modifier.fillMaxSize()) {
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { viewModel.setSearchQuery(it) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .testTag("course_search"),
            placeholder = { Text(viewModel.getTranslation("search_placeholder")) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search Icon") },
            shape = RoundedCornerShape(12.dp)
        )

        // Categories scroller
        LazyRow(
            modifier = Modifier.padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(categories) { cat ->
                val isSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        )
                        .clickable {
                            viewModel.setSelectedCategory(cat)
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onBackground
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Courses grid/list
        if (coursesList.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text("No courses found matches this search.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(coursesList) { course ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                viewModel.selectCourse(course)
                                navController.navigate("course_detail")
                            },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Category Icon representation
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                                contentAlignment = Alignment.Center
                            ) {
                                val icon = when (course.category) {
                                    "Quran" -> Icons.Default.Book
                                    "Computer" -> Icons.Default.Tv
                                    "Arabic" -> Icons.Default.Language
                                    else -> Icons.Default.School
                                }
                                Icon(icon, contentDescription = "Category Icon", modifier = Modifier.size(36.dp), tint = GoldAccent)
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(course.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp))
                                Text("Instructor: ${course.instructor}", style = MaterialTheme.typography.bodyMedium)
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Rating: ${course.rating} ⭐", style = MaterialTheme.typography.labelSmall)
                                    Text(
                                        text = if (course.isFree) "FREE" else "₹${course.price}",
                                        fontWeight = FontWeight.Bold,
                                        color = if (course.isFree) GoldAccent else MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// 4C. NOOR WALLET TAB
@Composable
fun WalletTab(viewModel: AppViewModel) {
    val balance by viewModel.walletBalance.collectAsState()
    val transactions by viewModel.walletTransactions.collectAsState()
    var couponText by remember { mutableStateOf("") }
    var walletAddText by remember { mutableStateOf("") }

    val discount by viewModel.appliedCouponDiscount.collectAsState()
    val couponError by viewModel.activeCouponError.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Elegant Wallet Gold Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.linearGradient(
                                colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.surfaceVariant)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column(modifier = Modifier.align(Alignment.TopStart)) {
                        Text("Noor Wallet Balance", color = Color.White.copy(alpha = 0.7f), style = MaterialTheme.typography.labelLarge)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("₹${String.format("%.2f", balance)}", color = GoldAccent, style = MaterialTheme.typography.displayLarge.copy(fontWeight = FontWeight.Bold))
                    }
                    Text(
                        "Referral Code Active",
                        modifier = Modifier.align(Alignment.BottomEnd),
                        color = Color.White.copy(alpha = 0.5f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Add Balance Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Simulate Adding Balance", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row {
                        OutlinedTextField(
                            value = walletAddText,
                            onValueChange = { walletAddText = it },
                            placeholder = { Text("Enter Amount") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                val amt = walletAddText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    viewModel.addWalletBalance(amt, "Loaded via Simulated Gateway")
                                    walletAddText = ""
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Load")
                        }
                    }
                }
            }
        }

        // Coupon Code section
        item {
            Card {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Apply Coupon Code", fontWeight = FontWeight.Bold)
                    Text("Use coupon 'NOOR20' (20% off) or 'FREE50' (50% off) for testing", style = MaterialTheme.typography.labelSmall)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row {
                        OutlinedTextField(
                            value = couponText,
                            onValueChange = { couponText = it },
                            placeholder = { Text("Coupon Code") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Button(
                            onClick = {
                                viewModel.applyCoupon(couponText)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Apply")
                        }
                    }
                    if (discount > 0.0) {
                        Text("Applied! Discount: ${discount}% off", color = Color.Green, style = MaterialTheme.typography.labelLarge)
                    }
                    if (couponError != null) {
                        Text(couponError ?: "", color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }

        // Transactions log
        item {
            Text("Wallet History", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp))
            Spacer(modifier = Modifier.height(8.dp))
            if (transactions.isEmpty()) {
                Text("No transactions record yet.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    transactions.forEach { tx ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(tx.description, fontWeight = FontWeight.Medium)
                                Text("ID: ${tx.id.take(15)}", style = MaterialTheme.typography.labelSmall)
                            }
                            Text(
                                text = if (tx.type == "CREDIT") "+₹${tx.amount}" else "-₹${tx.amount}",
                                color = if (tx.type == "CREDIT") Color.Green else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

// 4D. PROFILE & CERTIFICATES TAB
@Composable
fun ProfileTab(navController: NavController, viewModel: AppViewModel) {
    val user by viewModel.currentUser.collectAsState()
    val progressList by viewModel.progressList.collectAsState()
    val quizResults by viewModel.quizResults.collectAsState()
    val context = LocalContext.current

    var showEditProfile by remember { mutableStateOf(false) }
    var editName by remember { mutableStateOf(user?.name ?: "") }
    var editPhone by remember { mutableStateOf(user?.phone ?: "") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Avatar Header
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        user?.name?.firstOrNull()?.toString()?.uppercase() ?: "S",
                        color = Color.White,
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 32.sp)
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                Text(user?.name ?: "Student Learner", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 20.sp))
                Text(user?.email ?: "student@nooracademy.com", style = MaterialTheme.typography.bodyMedium)
                if (user?.phone?.isNotEmpty() == true) {
                    Text("Phone: ${user?.phone}", style = MaterialTheme.typography.bodyMedium)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(onClick = { showEditProfile = true }) {
                        Text("Edit Profile")
                    }
                    OutlinedButton(onClick = {
                        val whatsappUrl = "https://api.whatsapp.com/send?phone=919999999999&text=Assalamu%20Alaikum%20Noor%20Academy"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(whatsappUrl))
                        context.startActivity(intent)
                    }) {
                        Text("WhatsApp Chat")
                    }
                }
            }
        }

        // Referral Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = GoldAccent.copy(alpha = 0.15f)),
                border = BorderStroke(1.dp, GoldAccent)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Refer & Earn ₹25", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    Text("Share your code with friends. Both get reward balance upon first signup.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("My Code: ${user?.referralCode ?: "NOOR123"}", fontWeight = FontWeight.Bold)
                        Button(onClick = {
                            Toast.makeText(context, "Referral Code Copied!", Toast.LENGTH_SHORT).show()
                        }) {
                            Text("Copy")
                        }
                    }
                }
            }
        }

        // Certificate center
        item {
            Text("Completed Certificates", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp))
            Spacer(modifier = Modifier.height(8.dp))
            val certs = progressList.filter { it.isCertificateGenerated }
            if (certs.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                        Text("No certificates earned yet. Complete 100% of courses and pass quizzes to unlock.", textAlign = TextAlign.Center)
                    }
                }
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    certs.forEach { progress ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f)),
                            border = BorderStroke(1.dp, GoldAccent)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Icon(Icons.Default.CardMembership, contentDescription = "Certificate Icon", tint = GoldAccent, modifier = Modifier.size(36.dp))
                                Spacer(modifier = Modifier.height(8.dp))
                                Text("CERTIFICATE OF COMPLETION", fontWeight = FontWeight.Bold, color = GoldAccent)
                                Text("This certifies that ${user?.name} has successfully completed course ID ${progress.courseId} with 100% progress and attendance.", style = MaterialTheme.typography.bodyMedium)
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    modifier = Modifier.fillMaxWidth(),
                                    onClick = { Toast.makeText(context, "Certificate PDF downloaded to device storage!", Toast.LENGTH_LONG).show() }
                                ) {
                                    Text("Download PDF Certificate")
                                }
                            }
                        }
                    }
                }
            }
        }

        // Quiz achievements
        item {
            Text("Exam Results", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp))
            Spacer(modifier = Modifier.height(8.dp))
            if (quizResults.isEmpty()) {
                Text("No exam attempts yet.")
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quizResults.forEach { result ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(result.courseTitle, fontWeight = FontWeight.Bold)
                                Text("Score: ${result.score}/${result.totalQuestions}", style = MaterialTheme.typography.bodyMedium)
                            }
                            Text(
                                if (result.passed) "PASSED" else "FAILED",
                                color = if (result.passed) Color.Green else Color.Red,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // About & FAQs accordion
        item {
            Text("FAQ & Support", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.headlineMedium.copy(fontSize = 18.sp))
            Spacer(modifier = Modifier.height(8.dp))
            Card {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Q: Is the certificate authentic?", fontWeight = FontWeight.Bold)
                    Text("A: Yes! Verified by Noor Academy Academic Board.", style = MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Q: How can I access offline videos?", fontWeight = FontWeight.Bold)
                    Text("A: Tap the download icon next to any purchased course chapters.", style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        item {
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                onClick = { viewModel.logout(); navController.navigate("login") { popUpTo(0) } },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Sign Out", color = Color.White)
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Edit Profile Modal simulator
    if (showEditProfile) {
        AlertDialog(
            onDismissRequest = { showEditProfile = false },
            title = { Text("Edit Profile Details") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(value = editName, onValueChange = { editName = it }, label = { Text("Display Name") })
                    OutlinedTextField(value = editPhone, onValueChange = { editPhone = it }, label = { Text("Phone") })
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.updateProfileDetails(editName, editPhone)
                    showEditProfile = false
                }) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfile = false }) { Text("Cancel") }
            }
        )
    }
}

// 4E. ADMIN CONTROL PANEL TAB
@Composable
fun AdminTab(viewModel: AppViewModel) {
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Quran") }
    var desc by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("0.0") }
    var lessonsCountText by remember { mutableStateOf("4") }

    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text("Admin Course Publisher", style = MaterialTheme.typography.headlineMedium.copy(color = GoldAccent), fontWeight = FontWeight.Bold)
            Text("Publish real interactive courses instantly into Noor Academy global catalog.", style = MaterialTheme.typography.bodyMedium)
        }

        item {
            OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Course Title") }, modifier = Modifier.fillMaxWidth())
        }

        item {
            Text("Category:", fontWeight = FontWeight.Bold)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                listOf("Quran", "Arabic", "Urdu", "Farsi", "Islamic Studies", "Computer", "Spoken English").forEach { cat ->
                    val isSel = category == cat
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant)
                            .clickable { category = cat }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(cat, color = if (isSel) Color.White else Color.Black)
                    }
                }
            }
        }

        item {
            OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
        }

        item {
            OutlinedTextField(value = priceText, onValueChange = { priceText = it }, label = { Text("Price (0 for free)") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        }

        item {
            OutlinedTextField(value = lessonsCountText, onValueChange = { lessonsCountText = it }, label = { Text("Chapters count") }, keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number), modifier = Modifier.fillMaxWidth())
        }

        item {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                onClick = {
                    val price = priceText.toDoubleOrNull() ?: 0.0
                    val count = lessonsCountText.toIntOrNull() ?: 4
                    if (title.isNotEmpty() && desc.isNotEmpty()) {
                        viewModel.adminAddCourse(title, category, desc, price, count)
                        Toast.makeText(context, "Course Published Successfully!", Toast.LENGTH_SHORT).show()
                        title = ""
                        desc = ""
                        priceText = "0.0"
                        lessonsCountText = "4"
                    } else {
                        Toast.makeText(context, "Please enter all fields.", Toast.LENGTH_SHORT).show()
                    }
                }
            ) {
                Text("Publish Course to Platform")
            }
        }
    }
}

// 5. COURSE DETAIL SCREEN
@Composable
fun CourseDetailScreen(navController: NavController, viewModel: AppViewModel) {
    val course by viewModel.activeCourse.collectAsState()
    val lessons by viewModel.activeLessons.collectAsState()
    val progress by viewModel.activeProgress.collectAsState()
    val walletBal by viewModel.walletBalance.collectAsState()
    val discount by viewModel.appliedCouponDiscount.collectAsState()

    val context = LocalContext.current
    var activeSubTab by remember { mutableStateOf(0) } // 0: Syllabus, 1: Quiz/Exam

    val finalPrice = course?.let { c ->
        if (c.isFree) 0.0 else c.price * (1 - (discount / 100))
    } ?: 0.0

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text(
                    text = viewModel.getTranslation("course_details"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 12.dp)
                )
            }
        }
    ) { padding ->
        course?.let { c ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Details
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .background(GoldAccent, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(c.category, color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(c.title, style = MaterialTheme.typography.displayLarge.copy(fontSize = 20.sp), textAlign = TextAlign.Center, fontWeight = FontWeight.Bold)
                        Text("Led by: ${c.instructor}", style = MaterialTheme.typography.bodyMedium)
                    }
                }

                // Wishlist & Bookmark buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row {
                        IconButton(onClick = { viewModel.toggleWishlist(c.id, c.isWishlisted) }) {
                            Icon(
                                imageVector = if (c.isWishlisted) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = "Wishlist",
                                tint = if (c.isWishlisted) Color.Red else MaterialTheme.colorScheme.onBackground
                            )
                        }
                        IconButton(onClick = { viewModel.toggleBookmark(c.id, c.isBookmarked) }) {
                            Icon(
                                imageVector = if (c.isBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Bookmark",
                                tint = if (c.isBookmarked) GoldAccent else MaterialTheme.colorScheme.onBackground
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Star, contentDescription = "Star", tint = GoldAccent)
                        Text("${c.rating}", fontWeight = FontWeight.Bold)
                    }
                }

                Divider()

                // Lock/Unlock Control Pane
                Box(modifier = Modifier.padding(16.dp)) {
                    if (c.isPurchased) {
                        // Show Progress Ring and Start Learning
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.05f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text("Progress Checked", fontWeight = FontWeight.Bold)
                                    Text("Current Status: ${progress?.progressPercentage ?: 0}% Done", style = MaterialTheme.typography.bodyMedium)
                                }
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(CircleShape)
                                        .background(GoldAccent),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("${progress?.progressPercentage ?: 0}%", color = Color.Black, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    } else {
                        // Payment Portal Trigger Form
                        Card(
                            border = BorderStroke(1.dp, GoldAccent),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("This is a premium course.", fontWeight = FontWeight.Bold)
                                Text("Course fee: ₹${c.price}", style = MaterialTheme.typography.bodyMedium)
                                if (discount > 0.0) {
                                    Text("Discounted Price: ₹$finalPrice", color = Color.Green, fontWeight = FontWeight.Bold)
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    // Pay via Wallet
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            val ok = viewModel.purchaseCourseWithWallet(c.id, finalPrice)
                                            if (ok) {
                                                Toast.makeText(context, "Purchased Successfully via Noor Wallet!", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Insufficient Wallet Balance! Load money first.", Toast.LENGTH_LONG).show()
                                            }
                                        }
                                    ) {
                                        Text("Noor Wallet")
                                    }
                                    // UPI payment simulation
                                    Button(
                                        modifier = Modifier.weight(1f),
                                        onClick = {
                                            viewModel.purchaseCourseExternal(c.id, "UPI Secure Channel")
                                            Toast.makeText(context, "Simulated Razorpay UPI checkout successful!", Toast.LENGTH_SHORT).show()
                                        }
                                    ) {
                                        Text("UPI / Cards")
                                    }
                                }
                            }
                        }
                    }
                }

                // Tab Selector Syllabus / Exam
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (activeSubTab == 0) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { activeSubTab = 0 }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Syllabus Curriculum", color = if (activeSubTab == 0) Color.White else MaterialTheme.colorScheme.onBackground)
                    }
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(if (activeSubTab == 1) MaterialTheme.colorScheme.primary else Color.Transparent, RoundedCornerShape(8.dp))
                            .clickable { activeSubTab = 1 }
                            .padding(vertical = 12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Quiz & Exams", color = if (activeSubTab == 1) Color.White else MaterialTheme.colorScheme.onBackground)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                if (activeSubTab == 0) {
                    // Syllabus Lesson Items
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        lessons.forEach { lesson ->
                            Card {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        IconButton(onClick = {
                                            if (c.isPurchased) {
                                                navController.navigate("lesson_play")
                                            } else {
                                                Toast.makeText(context, "Unlock course to watch chapters!", Toast.LENGTH_SHORT).show()
                                            }
                                        }) {
                                            Icon(
                                                imageVector = if (lesson.isCompleted) Icons.Default.CheckCircle else Icons.Default.PlayCircle,
                                                tint = if (lesson.isCompleted) Color.Green else GoldAccent,
                                                contentDescription = "Play"
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Column {
                                            Text(lesson.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                                            Text(
                                                text = if (lesson.isLive) "Scheduled Live: ${lesson.scheduledTime}" else "Duration: ${lesson.duration}",
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                    }

                                    Row {
                                        // PDF Notes Download Actions
                                        if (lesson.pdfUrl.isNotEmpty()) {
                                            IconButton(onClick = {
                                                if (c.isPurchased) {
                                                    viewModel.downloadLessonPDF(lesson.id)
                                                } else {
                                                    Toast.makeText(context, "Unlock course to download resources!", Toast.LENGTH_SHORT).show()
                                                }
                                            }) {
                                                Icon(Icons.Default.Download, contentDescription = "PDF Download", tint = GoldAccent)
                                            }
                                        }
                                        // Offline Video Download Simulation
                                        if (!lesson.isLive) {
                                            IconButton(onClick = {
                                                if (c.isPurchased) {
                                                    viewModel.downloadLessonVideo(lesson.id)
                                                } else {
                                                    Toast.makeText(context, "Unlock course to download videos offline!", Toast.LENGTH_SHORT).show()
                                                }
                                            }) {
                                                Icon(Icons.Default.DownloadDone, contentDescription = "Offline download", tint = MaterialTheme.colorScheme.primary)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // Quiz Exam screen trigger
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.Timer, contentDescription = "Timer", modifier = Modifier.size(48.dp), tint = GoldAccent)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Official Assessment Quiz", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
                            Text("Pass score: 70%. Earn valid Certificate of Noor Academy instantly.", textAlign = TextAlign.Center)
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(onClick = {
                                if (c.isPurchased) {
                                    viewModel.startQuizForCourse(c.id)
                                    navController.navigate("quiz")
                                } else {
                                    Toast.makeText(context, "Unlock course first to take exams!", Toast.LENGTH_SHORT).show()
                                }
                            }) {
                                Text("Begin Official Exam")
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

// 6. LESSON PLAY & LIVE CLASS STREAM SCREEN
@Composable
fun LessonPlayScreen(navController: NavController, viewModel: AppViewModel) {
    val course by viewModel.activeCourse.collectAsState()
    val lessons by viewModel.activeLessons.collectAsState()
    val context = LocalContext.current

    var commentText by remember { mutableStateOf("") }
    val comments = remember { mutableStateListOf("Assalamu Alaikum! Excited for this lecture.", "The voice clarity is amazing.", "Beautiful explanation of tajweed rules.") }

    var isMuted by remember { mutableStateOf(false) }
    var streamProgress by remember { mutableStateOf(0.35f) }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Streaming Classroom", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Immersive Video Player Simulator
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.PlayCircle, contentDescription = "Playback", modifier = Modifier.size(64.dp), tint = GoldAccent)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Noor Classroom Stream Engine Active", color = Color.White)
                    Text("Quality: 1080p Auto", color = GoldAccent, style = MaterialTheme.typography.labelSmall)
                }

                // Controls row overlays
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                        .background(Color.Black.copy(alpha = 0.6f))
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { isMuted = !isMuted }) {
                        Icon(if (isMuted) Icons.Default.Lock else Icons.Default.VolumeUp, tint = Color.White, contentDescription = "Mute")
                    }
                    Slider(
                        value = streamProgress,
                        onValueChange = { streamProgress = it },
                        modifier = Modifier.weight(1f)
                    )
                    Text("12:04 / 35:20", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Discussion and Comments section
            Text("Classroom Live Discussion", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))

            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(comments) { comment ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .background(MaterialTheme.colorScheme.primary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("S", color = Color.White)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(comment, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            // Input comments row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = commentText,
                    onValueChange = { commentText = it },
                    placeholder = { Text("Ask doubt / send message...") },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = {
                    if (commentText.isNotEmpty()) {
                        comments.add(commentText)
                        commentText = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Send Comment", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// 7. QUIZ / EXAM SCREEN SYSTEM
@Composable
fun QuizScreen(navController: NavController, viewModel: AppViewModel) {
    val questions by viewModel.currentQuizQuestions.collectAsState()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val selectedIndex by viewModel.selectedAnswerIndex.collectAsState()
    val score by viewModel.quizScore.collectAsState()
    val completed by viewModel.quizCompleted.collectAsState()

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .statusBarsPadding()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { navController.popBackStack() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Text("Assessments Center", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (!completed && questions.isNotEmpty() && currentIndex < questions.size) {
                val q = questions[currentIndex]
                Text("Question ${currentIndex + 1} of ${questions.size}", style = MaterialTheme.typography.labelSmall, color = GoldAccent)
                Spacer(modifier = Modifier.height(12.dp))
                Text(q.question, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                Spacer(modifier = Modifier.height(24.dp))

                // Options list
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    q.options.forEachIndexed { idx, opt ->
                        val isSelected = selectedIndex == idx
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) GoldAccent else MaterialTheme.colorScheme.primary.copy(alpha = 0.3f),
                                    RoundedCornerShape(12.dp)
                                )
                                .background(if (isSelected) GoldAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface)
                                .clickable { viewModel.selectQuizAnswer(idx) }
                                .padding(16.dp)
                        ) {
                            Text(opt, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    onClick = { viewModel.submitQuizAnswer() },
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Submit Answer")
                }
            } else {
                // Exam Finished Screen
                Icon(Icons.Default.CardMembership, contentDescription = "Certificate Award", tint = GoldAccent, modifier = Modifier.size(80.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Exam Finished!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Your Total Score is $score / ${questions.size}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(12.dp))
                if (score >= 2) {
                    Text("Congratulations! You passed! Noor Academy Certificate is now unlocked in your Profile tab.", textAlign = TextAlign.Center, color = Color.Green)
                } else {
                    Text("Score under 70%. Practice core chapters and try again to unlock your certificate.", textAlign = TextAlign.Center, color = Color.Red)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Button(onClick = { navController.popBackStack() }) {
                    Text("Return to Course detail")
                }
            }
        }
    }
}
