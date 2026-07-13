package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.database.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = AppRepository(
        db.courseDao(),
        db.lessonDao(),
        db.userProgressDao(),
        db.walletDao(),
        db.quizDao()
    )

    // UI Configuration States
    private val _currentLanguage = MutableStateFlow("en") // en, hi, ur
    val currentLanguage: StateFlow<String> = _currentLanguage.asStateFlow()

    private val _isDarkMode = MutableStateFlow(true)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Auth States
    private val _currentUser = MutableStateFlow<User?>(null) // null means guest/logged out
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError: StateFlow<String?> = _authError.asStateFlow()

    // Search and Filters
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    // Reactive Room Caches
    val courses: StateFlow<List<Course>> = combine(
        _searchQuery,
        _selectedCategory,
        repository.allCourses
    ) { query, category, allCourses ->
        var filtered = allCourses
        if (query.isNotEmpty()) {
            filtered = filtered.filter { 
                it.title.contains(query, ignoreCase = true) || 
                it.description.contains(query, ignoreCase = true) ||
                it.instructor.contains(query, ignoreCase = true)
            }
        }
        if (category != "All") {
            filtered = filtered.filter { it.category.equals(category, ignoreCase = true) }
        }
        filtered
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wishlistedCourses: StateFlow<List<Course>> = repository.wishlistedCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val bookmarkedCourses: StateFlow<List<Course>> = repository.bookmarkedCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val purchasedCourses: StateFlow<List<Course>> = repository.purchasedCourses
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val walletTransactions: StateFlow<List<WalletTransaction>> = repository.allTransactions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val quizResults: StateFlow<List<QuizResult>> = repository.allQuizResults
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val progressList: StateFlow<List<UserProgress>> = repository.allProgress
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Wallet Balance State (Calculated dynamically)
    val walletBalance: StateFlow<Double> = repository.allTransactions
        .map { list ->
            list.fold(0.0) { acc, tx ->
                if (tx.type == "CREDIT") acc + tx.amount else acc - tx.amount
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50.0)

    // Detail Screen Targets
    private val _activeCourse = MutableStateFlow<Course?>(null)
    val activeCourse: StateFlow<Course?> = _activeCourse.asStateFlow()

    val activeLessons: StateFlow<List<Lesson>> = _activeCourse
        .flatMapLatest { course ->
            if (course != null) repository.getLessonsForCourse(course.id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProgress: StateFlow<UserProgress?> = _activeCourse
        .flatMapLatest { course ->
            if (course != null) repository.getProgressForCourse(course.id) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // Quiz taking states
    private val _currentQuizQuestions = MutableStateFlow<List<QuizQuestion>>(emptyList())
    val currentQuizQuestions: StateFlow<List<QuizQuestion>> = _currentQuizQuestions.asStateFlow()

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex.asStateFlow()

    private val _selectedAnswerIndex = MutableStateFlow<Int?>(null)
    val selectedAnswerIndex: StateFlow<Int?> = _selectedAnswerIndex.asStateFlow()

    private val _quizScore = MutableStateFlow(0)
    val quizScore: StateFlow<Int> = _quizScore.asStateFlow()

    private val _quizCompleted = MutableStateFlow(false)
    val quizCompleted: StateFlow<Boolean> = _quizCompleted.asStateFlow()

    // Notification State Simulation
    private val _notifications = MutableStateFlow<List<AppNotification>>(emptyList())
    val notifications: StateFlow<List<AppNotification>> = _notifications.asStateFlow()

    // Coupon System State
    private val _activeCouponError = MutableStateFlow<String?>(null)
    val activeCouponError: StateFlow<String?> = _activeCouponError.asStateFlow()

    private val _appliedCouponDiscount = MutableStateFlow(0.0) // percentage
    val appliedCouponDiscount: StateFlow<Double> = _appliedCouponDiscount.asStateFlow()

    init {
        viewModelScope.launch {
            repository.prepopulateIfEmpty()
            // Add some initial mock notifications
            _notifications.value = listOf(
                AppNotification("Welcome!", "Start your spiritual learning journey today with Noor Academy.", System.currentTimeMillis()),
                AppNotification("Live Class Alert!", "Tajweed Rules Masterclass begins in 30 minutes. Join now!", System.currentTimeMillis() - 1000 * 60 * 15)
            )
        }
    }

    // Language Toggle
    fun setLanguage(lang: String) {
        _currentLanguage.value = lang
    }

    // Dark Mode Toggle
    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    // Setters for UI states
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    fun updateProfileDetails(name: String, phone: String) {
        val current = _currentUser.value
        if (current != null) {
            _currentUser.value = current.copy(name = name, phone = phone)
        }
    }

    // Authentications
    fun loginWithEmail(email: String, pass: String): Boolean {
        if (email.contains("@") && pass.length >= 6) {
            val role = if (email.startsWith("admin")) "ADMIN" else "STUDENT"
            _currentUser.value = User(
                uid = UUID.randomUUID().toString(),
                email = email,
                name = if (role == "ADMIN") "Academy Admin" else email.substringBefore("@").replaceFirstChar { it.uppercase() },
                role = role,
                referralCode = "NOOR" + (100..999).random().toString()
            )
            _authError.value = null
            // Trigger auto purchase for free items
            viewModelScope.launch {
                val all = repository.allCourses.first()
                for (c in all) {
                    if (c.isFree) {
                        repository.purchaseCourse(c.id)
                    }
                }
            }
            addNotification("Login Successful", "Welcome back, ${_currentUser.value?.name}!")
            return true
        } else {
            _authError.value = "Invalid email format or password too short (min 6 chars)."
            return false
        }
    }

    fun signup(email: String, name: String, pass: String, phone: String = ""): Boolean {
        if (email.contains("@") && pass.length >= 6 && name.isNotEmpty()) {
            _currentUser.value = User(
                uid = UUID.randomUUID().toString(),
                email = email,
                name = name,
                role = "STUDENT",
                phone = phone,
                referralCode = "NOOR" + (100..999).random().toString()
            )
            _authError.value = null
            addNotification("Account Created", "Welcome to Noor Academy, $name!")
            return true
        } else {
            _authError.value = "Please fill in all fields correctly."
            return false
        }
    }

    fun loginWithPhoneOTP(phone: String, otp: String): Boolean {
        if (phone.length >= 10 && otp == "123456") {
            _currentUser.value = User(
                uid = UUID.randomUUID().toString(),
                email = "user" + (100..999).random() + "@nooracademy.com",
                name = "Student " + phone.takeLast(4),
                role = "STUDENT",
                phone = phone,
                referralCode = "NOOR" + (100..999).random().toString()
            )
            _authError.value = null
            addNotification("OTP Verified", "Logged in securely via Phone OTP.")
            return true
        } else {
            _authError.value = "Incorrect OTP. Try '123456' for testing."
            return false
        }
    }

    fun loginAsGuest() {
        _currentUser.value = User(
            uid = "GUEST_USER",
            email = "guest@nooracademy.com",
            name = "Guest Learner",
            role = "STUDENT",
            isGuest = true,
            referralCode = "GUEST"
        )
        addNotification("Guest Mode", "Explore Noor Academy course library.")
    }

    fun logout() {
        _currentUser.value = null
        _activeCourse.value = null
    }

    // Wishlist & Bookmark toggle
    fun toggleWishlist(courseId: Int, isWishlisted: Boolean) {
        viewModelScope.launch {
            repository.toggleWishlist(courseId, isWishlisted)
            // update local active course reference if needed
            val active = _activeCourse.value
            if (active != null && active.id == courseId) {
                _activeCourse.value = active.copy(isWishlisted = !isWishlisted)
            }
        }
    }

    fun toggleBookmark(courseId: Int, isBookmarked: Boolean) {
        viewModelScope.launch {
            repository.toggleBookmark(courseId, isBookmarked)
            val active = _activeCourse.value
            if (active != null && active.id == courseId) {
                _activeCourse.value = active.copy(isBookmarked = !isBookmarked)
            }
        }
    }

    // Set Active Course
    fun selectCourse(course: Course) {
        _activeCourse.value = course
    }

    // Course Checkout & Payment Simulation
    fun applyCoupon(code: String): Boolean {
        if (code.equals("NOOR20", ignoreCase = true)) {
            _appliedCouponDiscount.value = 20.0
            _activeCouponError.value = null
            return true
        } else if (code.equals("FREE50", ignoreCase = true)) {
            _appliedCouponDiscount.value = 50.0
            _activeCouponError.value = null
            return true
        } else {
            _activeCouponError.value = "Invalid coupon code."
            _appliedCouponDiscount.value = 0.0
            return false
        }
    }

    fun clearCoupon() {
        _appliedCouponDiscount.value = 0.0
        _activeCouponError.value = null
    }

    fun purchaseCourseWithWallet(courseId: Int, finalPrice: Double): Boolean {
        val balance = walletBalance.value
        if (balance >= finalPrice) {
            viewModelScope.launch {
                // Deduct from wallet
                if (finalPrice > 0) {
                    repository.addWalletTransaction(
                        WalletTransaction(
                            id = "TX_" + System.currentTimeMillis(),
                            type = "DEBIT",
                            amount = finalPrice,
                            description = "Purchased course ID $courseId"
                        )
                    )
                }
                repository.purchaseCourse(courseId)
                // Refresh active course reference
                val active = _activeCourse.value
                if (active != null && active.id == courseId) {
                    _activeCourse.value = active.copy(isPurchased = true)
                }
                addNotification("Course Enrolled", "Successfully purchased and unlocked your course!")
            }
            return true
        } else {
            return false
        }
    }

    fun purchaseCourseExternal(courseId: Int, method: String) {
        viewModelScope.launch {
            repository.purchaseCourse(courseId)
            val active = _activeCourse.value
            if (active != null && active.id == courseId) {
                _activeCourse.value = active.copy(isPurchased = true)
            }
            addNotification("Payment Successful", "Course unlocked via $method payment gateway.")
        }
    }

    fun addWalletBalance(amount: Double, description: String = "Added Money to Wallet") {
        viewModelScope.launch {
            repository.addWalletTransaction(
                WalletTransaction(
                    id = "TX_" + System.currentTimeMillis(),
                    type = "CREDIT",
                    amount = amount,
                    description = description
                )
            )
            addNotification("Wallet Credited", "₹$amount successfully added to your Noor Wallet.")
        }
    }

    // Referral code submission
    fun submitReferral(code: String): Boolean {
        if (code.startsWith("NOOR") && code.length >= 7) {
            addWalletBalance(25.0, "Referral Bonus Code: $code")
            addNotification("Referral Applied", "Congratulations! You and your friend earned ₹25 bonus.")
            return true
        }
        return false
    }

    // Video lesson progress
    fun markLessonCompleted(courseId: Int, lessonId: Int, isCompleted: Boolean) {
        viewModelScope.launch {
            repository.updateLessonCompletion(courseId, lessonId, isCompleted)
        }
    }

    // Local downloads simulate
    fun downloadLessonPDF(lessonId: Int) {
        viewModelScope.launch {
            repository.updateDownloadStatus(lessonId, true)
            addNotification("PDF Downloaded", "PDF Note is saved locally in Downloads.")
        }
    }

    fun downloadLessonVideo(lessonId: Int) {
        viewModelScope.launch {
            repository.updateDownloadStatus(lessonId, true)
            addNotification("Video Downloaded", "Lesson video downloaded for offline viewing.")
        }
    }

    // Attendance trigger
    fun checkInAttendance(courseId: Int) {
        viewModelScope.launch {
            repository.incrementAttendance(courseId)
            addNotification("Attendance Marked", "Your attendance has been recorded successfully.")
        }
    }

    // Quiz and Exams Setup
    fun startQuizForCourse(courseId: Int) {
        _quizCompleted.value = false
        _currentQuestionIndex.value = 0
        _quizScore.value = 0
        _selectedAnswerIndex.value = null
        
        // Setup mock questions based on course ID
        _currentQuizQuestions.value = listOf(
            QuizQuestion(
                question = "What is the primary meaning of Tajweed linguistically?",
                options = listOf("To correct", "To make beautiful or better", "To read fast", "To memorise"),
                correctIndex = 1
            ),
            QuizQuestion(
                question = "How many letters of Makhraj (articulation points) are generally counted in classical Tajweed?",
                options = listOf("17", "28", "29", "10"),
                correctIndex = 0
            ),
            QuizQuestion(
                question = "Which letter rules are altered when preceding a Noon Sakinah?",
                options = listOf("Izhar, Idgham, Iqlaab, Ikhfaa", "Madd, Qalqalah, Ghunnah", "Waqf, Saktah", "None of the above"),
                correctIndex = 0
            )
        )
    }

    fun selectQuizAnswer(optionIndex: Int) {
        _selectedAnswerIndex.value = optionIndex
    }

    fun submitQuizAnswer() {
        val currentIdx = _currentQuestionIndex.value
        val questions = _currentQuizQuestions.value
        val selectedIdx = _selectedAnswerIndex.value
        
        if (selectedIdx != null && currentIdx < questions.size) {
            if (selectedIdx == questions[currentIdx].correctIndex) {
                _quizScore.value += 1
            }
            if (currentIdx + 1 < questions.size) {
                _currentQuestionIndex.value = currentIdx + 1
                _selectedAnswerIndex.value = null
            } else {
                _quizCompleted.value = true
                saveQuizResult()
            }
        }
    }

    private fun saveQuizResult() {
        val course = _activeCourse.value ?: return
        val score = _quizScore.value
        val total = _currentQuizQuestions.value.size
        val passed = (score.toFloat() / total) >= 0.70f
        
        viewModelScope.launch {
            repository.addQuizResult(
                QuizResult(
                    courseId = course.id,
                    courseTitle = course.title,
                    score = score,
                    totalQuestions = total,
                    passed = passed
                )
            )
            if (passed) {
                // Instantly generate certificate!
                repository.generateCertificate(course.id)
                addNotification("Certificate Unlocked!", "Congratulations! You passed the exam and earned your Noor Academy Certificate.")
            } else {
                addNotification("Exam Completed", "You scored $score/$total. Keep practicing to earn your certificate!")
            }
        }
    }

    // Admin Dashboard uploads and management
    fun adminAddCourse(title: String, category: String, desc: String, price: Double, lessonsCount: Int) {
        viewModelScope.launch {
            val isPaid = price > 0.0
            val newId = (11..999).random()
            val newCourse = Course(
                id = newId,
                title = title,
                category = category,
                description = desc,
                duration = "${lessonsCount * 3} Hours",
                price = price,
                rating = 5.0f,
                instructor = _currentUser.value?.name ?: "Academy Faculty",
                isPaid = isPaid,
                isFree = !isPaid,
                totalLessons = lessonsCount,
                isPurchased = true // auto own for admin creator
            )
            val listLessons = mutableListOf<Lesson>()
            for (i in 1..lessonsCount) {
                listLessons.add(
                    Lesson(
                        courseId = newId,
                        title = "Unit $i: Chapter and Detailed Exercise",
                        duration = "15:00",
                        videoUrl = "https://example.com/vid.mp4",
                        pdfUrl = "unit_$i.pdf",
                        orderIndex = i - 1
                    )
                )
            }
            repository.addCourse(newCourse, listLessons)
            addNotification("Course Created", "New Course '$title' is now published successfully!")
        }
    }

    private fun addNotification(title: String, message: String) {
        val current = _notifications.value.toMutableList()
        current.add(0, AppNotification(title, message, System.currentTimeMillis()))
        _notifications.value = current
    }

    // Translation maps dynamically fetched in Compose
    fun getTranslation(key: String): String {
        val lang = _currentLanguage.value
        return translations[lang]?.get(key) ?: translations["en"]?.get(key) ?: key
    }

    private val translations = mapOf(
        "en" to mapOf(
            "app_name" to "Noor Academy",
            "tagline" to "Illuminating Minds, Nurturing Souls",
            "dashboard" to "Dashboard",
            "courses" to "Courses",
            "wallet" to "Wallet",
            "profile" to "Profile",
            "search_placeholder" to "Search Quran, Arabic, English, Computer...",
            "categories" to "Categories",
            "continue_watching" to "Continue Watching",
            "popular_courses" to "Popular Courses",
            "free" to "FREE",
            "paid" to "PAID",
            "buy_now" to "Unlock Course",
            "start_learning" to "Start Learning",
            "course_details" to "Course Details",
            "curriculum" to "Curriculum",
            "about" to "About",
            "exams" to "Exams & Quizzes",
            "discussion" to "Discussions",
            "live_badge" to "LIVE CLASS",
            "attendance" to "Attendance",
            "progress" to "Progress",
            "certificate" to "Certificate",
            "edit_profile" to "Edit Profile",
            "contact_us" to "Contact Us",
            "referral" to "Referral",
            "faq" to "FAQ",
            "terms" to "Terms & Conditions",
            "login" to "Sign In",
            "signup" to "Sign Up",
            "logout" to "Log Out",
            "admin_panel" to "Admin Control"
        ),
        "hi" to mapOf(
            "app_name" to "नूर अकादमी",
            "tagline" to "मस्तिष्क को रोशन करना, आत्माओं का पोषण",
            "dashboard" to "डैशबोर्ड",
            "courses" to "पाठ्यक्रम",
            "wallet" to "वॉलेट",
            "profile" to "प्रोफ़ाइल",
            "search_placeholder" to "खोजें कुरान, अरबी, अंग्रेजी, कंप्यूटर...",
            "categories" to "श्रेणियाँ",
            "continue_watching" to "देखना जारी रखें",
            "popular_courses" to "लोकप्रिय पाठ्यक्रम",
            "free" to "मुफ़्त",
            "paid" to "सशुल्क",
            "buy_now" to "पाठ्यक्रम खोलें",
            "start_learning" to "सीखना शुरू करें",
            "course_details" to "पाठ्यक्रम का विवरण",
            "curriculum" to "पाठ्यचर्या",
            "about" to "के बारे में",
            "exams" to "क्विज़ और परीक्षा",
            "discussion" to "चर्चा",
            "live_badge" to "लाइव क्लास",
            "attendance" to "उपस्थिति",
            "progress" to "प्रगति",
            "certificate" to "प्रमाणपत्र",
            "edit_profile" to "प्रोफ़ाइल संपादित करें",
            "contact_us" to "संपर्क करें",
            "referral" to "रेफरल",
            "faq" to "अक्सर पूछे जाने वाले सवाल",
            "terms" to "नियम और शर्तें",
            "login" to "लॉग इन करें",
            "signup" to "साइन अप करें",
            "logout" to "लॉग आउट",
            "admin_panel" to "व्यवस्थापक नियंत्रण"
        ),
        "ur" to mapOf(
            "app_name" to "نور اکیڈمی",
            "tagline" to "ذہنوں کو منور کرنا، روحوں کی پرورش",
            "dashboard" to "ڈیش بورڈ",
            "courses" to "کورسز",
            "wallet" to "بٹوہ",
            "profile" to "پروفائل",
            "search_placeholder" to "قرآن، عربی، انگریزی، کمپیوٹر تلاش کریں...",
            "categories" to "اقسام",
            "continue_watching" to "دیکھنا جاری رکھیں",
            "popular_courses" to "مقبول کورسز",
            "free" to "مفت",
            "paid" to "ادا شدہ",
            "buy_now" to "کورس انلاک کریں",
            "start_learning" to "سیکھنا شروع کریں",
            "course_details" to "کورس کی تفصیلات",
            "curriculum" to "نصاب",
            "about" to "تعارف",
            "exams" to "امتحانات اور کوئز",
            "discussion" to "گفتگو",
            "live_badge" to "لائیو کلاس",
            "attendance" to "حاضری",
            "progress" to "ترقی",
            "certificate" to "سرٹیفکیٹ",
            "edit_profile" to "پروفائل ایڈٹ کریں",
            "contact_us" to "ہم سے رابطہ کریں",
            "referral" to "ریفرل",
            "faq" to "اکثر پوچھے گئے سوالات",
            "terms" to "شرائط و ضوابط",
            "login" to "لاگ ان کریں",
            "signup" to "سائن اپ کریں",
            "logout" to "لاگ آؤٹ",
            "admin_panel" to "ایڈمن پینل"
        )
    )
}

// Support Data Models
data class User(
    val uid: String,
    val email: String,
    val name: String,
    val role: String, // STUDENT, ADMIN
    val phone: String = "",
    val isGuest: Boolean = false,
    val referralCode: String = ""
)

data class AppNotification(
    val title: String,
    val body: String,
    val timestamp: Long
)

data class QuizQuestion(
    val question: String,
    val options: List<String>,
    val correctIndex: Int
)
