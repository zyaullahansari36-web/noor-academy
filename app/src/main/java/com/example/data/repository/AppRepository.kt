package com.example.data.repository

import com.example.data.dao.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.firstOrNull

class AppRepository(
    private val courseDao: CourseDao,
    private val lessonDao: LessonDao,
    private val userProgressDao: UserProgressDao,
    private val walletDao: WalletDao,
    private val quizDao: QuizDao
) {
    val allCourses: Flow<List<Course>> = courseDao.getAllCourses()
    val wishlistedCourses: Flow<List<Course>> = courseDao.getWishlistedCourses()
    val bookmarkedCourses: Flow<List<Course>> = courseDao.getBookmarkedCourses()
    val purchasedCourses: Flow<List<Course>> = courseDao.getPurchasedCourses()
    val allTransactions: Flow<List<WalletTransaction>> = walletDao.getAllTransactions()
    val allQuizResults: Flow<List<QuizResult>> = quizDao.getAllQuizResults()
    val allProgress: Flow<List<UserProgress>> = userProgressDao.getAllProgress()

    fun getCourseById(courseId: Int): Flow<Course?> = courseDao.getCourseById(courseId)
    fun getCoursesByCategory(category: String): Flow<List<Course>> = courseDao.getCoursesByCategory(category)
    fun searchCourses(query: String): Flow<List<Course>> = courseDao.searchCourses(query)
    fun getLessonsForCourse(courseId: Int): Flow<List<Lesson>> = lessonDao.getLessonsForCourse(courseId)
    fun getLessonById(lessonId: Int): Flow<Lesson?> = lessonDao.getLessonById(lessonId)
    fun getProgressForCourse(courseId: Int): Flow<UserProgress?> = userProgressDao.getProgressForCourse(courseId)

    suspend fun insertCourse(course: Course) = courseDao.insertCourse(course)
    suspend fun updateCourse(course: Course) = courseDao.updateCourse(course)
    suspend fun toggleWishlist(courseId: Int, currentStatus: Boolean) {
        courseDao.updateWishlistStatus(courseId, !currentStatus)
    }
    suspend fun toggleBookmark(courseId: Int, currentStatus: Boolean) {
        courseDao.updateBookmarkStatus(courseId, !currentStatus)
    }
    suspend fun purchaseCourse(courseId: Int) {
        courseDao.markCoursePurchased(courseId)
        userProgressDao.insertOrUpdateProgress(
            UserProgress(courseId = courseId, progressPercentage = 0)
        )
    }

    suspend fun updateLessonCompletion(courseId: Int, lessonId: Int, isCompleted: Boolean) {
        lessonDao.updateLessonCompletion(lessonId, isCompleted)
        
        // Recalculate course progress
        val lessons = lessonDao.getLessonsForCourse(courseId).first()
        if (lessons.isNotEmpty()) {
            val completedCount = lessons.count { it.isCompleted || (it.id == lessonId && isCompleted) }
            val progressPercentage = (completedCount.toFloat() / lessons.size * 100).toInt()
            
            val existingProgress = userProgressDao.getProgressForCourse(courseId).firstOrNull()
            userProgressDao.insertOrUpdateProgress(
                UserProgress(
                    courseId = courseId,
                    progressPercentage = progressPercentage,
                    lastWatchedLessonId = lessonId,
                    attendanceDays = existingProgress?.attendanceDays ?: 0,
                    isCertificateGenerated = existingProgress?.isCertificateGenerated ?: (progressPercentage == 100)
                )
            )
        }
    }

    suspend fun updateDownloadStatus(lessonId: Int, isDownloaded: Boolean) {
        lessonDao.updateDownloadStatus(lessonId, isDownloaded)
    }

    suspend fun incrementAttendance(courseId: Int) {
        userProgressDao.incrementAttendance(courseId)
    }

    suspend fun generateCertificate(courseId: Int) {
        userProgressDao.markCertificateGenerated(courseId)
    }

    suspend fun addWalletTransaction(transaction: WalletTransaction) {
        walletDao.insertTransaction(transaction)
    }

    suspend fun addQuizResult(result: QuizResult) {
        quizDao.insertQuizResult(result)
    }

    suspend fun addCourse(course: Course, lessons: List<Lesson>) {
        courseDao.insertCourse(course)
        lessonDao.insertLessons(lessons)
    }

    suspend fun prepopulateIfEmpty() {
        val currentCourses = courseDao.getAllCourses().first()
        if (currentCourses.isEmpty()) {
            val defaultCourses = listOf(
                Course(
                    id = 1,
                    title = "Tajweed Rules & Beautiful Quran Recitation",
                    category = "Quran",
                    description = "Learn correct pronunciation, articulations, and melodic rules of Tajweed to recite Quran with beauty and precision.",
                    duration = "24 Hours",
                    price = 0.0,
                    rating = 4.9f,
                    instructor = "Qari Abdul Basit",
                    isPaid = false,
                    isFree = true,
                    totalLessons = 6,
                    language = "English"
                ),
                Course(
                    id = 2,
                    title = "Modern Fus'ha Arabic Level 1",
                    category = "Arabic",
                    description = "Master Arabic speaking, writing, and grammar from absolute scratch. Best for reading classic literature and media.",
                    duration = "45 Hours",
                    price = 49.99,
                    rating = 4.8f,
                    instructor = "Dr. Imran Al-Alawi",
                    isPaid = true,
                    isFree = false,
                    totalLessons = 5,
                    language = "English"
                ),
                Course(
                    id = 3,
                    title = "Tafseer of Surah Al-Fatihah & Juz Amma",
                    category = "Tafseer",
                    description = "Uncover deeper meanings, historic contexts, and spiritual lessons of the opening Surah and daily short prayers.",
                    duration = "18 Hours",
                    price = 0.0,
                    rating = 4.9f,
                    instructor = "Sheikh Yasir Qadhi",
                    isPaid = false,
                    isFree = true,
                    totalLessons = 4,
                    language = "English"
                ),
                Course(
                    id = 4,
                    title = "Forty Hadith of Imam Nawawi with Commentary",
                    category = "Hadith",
                    description = "A profound study of Imam Nawawi’s selection of forty essential sayings of the Prophet (PBUH) summarizing the entire deen.",
                    duration = "30 Hours",
                    price = 0.0,
                    rating = 4.7f,
                    instructor = "Mufti Ismail Menk",
                    isPaid = false,
                    isFree = true,
                    totalLessons = 5,
                    language = "English"
                ),
                Course(
                    id = 5,
                    title = "Introduction to Kotlin & Modern Android Development",
                    category = "Computer",
                    description = "Kickstart your technical career by learning the fundamentals of Kotlin, Jetpack Compose, and building interactive mobile applications.",
                    duration = "50 Hours",
                    price = 99.99,
                    rating = 4.9f,
                    instructor = "Eng. Zyaullah Ansari",
                    isPaid = true,
                    isFree = false,
                    totalLessons = 8,
                    language = "English"
                ),
                Course(
                    id = 6,
                    title = "Essential Fiqh of Worship (Ibadaat)",
                    category = "Fiqh",
                    description = "A practical, simple-to-understand explanation of the rules of purification, prayers, fasting, charity, and pilgrimage according to main schools.",
                    duration = "22 Hours",
                    price = 19.99,
                    rating = 4.6f,
                    instructor = "Sheikh Assim Al-Hakeem",
                    isPaid = true,
                    isFree = false,
                    totalLessons = 4,
                    language = "English"
                ),
                Course(
                    id = 7,
                    title = "Beautiful Urdu Script and Poetry",
                    category = "Urdu",
                    description = "Immerse yourself in Urdu calligraphy, conversational fluency, and a spiritual journey through the writings of Iqbal and Ghalib.",
                    duration = "15 Hours",
                    price = 0.0,
                    rating = 4.8f,
                    instructor = "Ustadh Javed Akhtar",
                    isPaid = false,
                    isFree = true,
                    totalLessons = 4,
                    language = "Urdu"
                ),
                Course(
                    id = 8,
                    title = "Classical Persian (Farsi) Grammar",
                    category = "Farsi",
                    description = "Learn the elegant language of Rumi and Saadi. Focuses on classical grammar, vocabulary, and literary translations.",
                    duration = "32 Hours",
                    price = 29.99,
                    rating = 4.7f,
                    instructor = "Dr. Ali Reza",
                    isPaid = true,
                    isFree = false,
                    totalLessons = 4,
                    language = "Farsi"
                ),
                Course(
                    id = 9,
                    title = "Comprehensive Islamic Creed and Ethics",
                    category = "Islamic Studies",
                    description = "Build a solid moral foundation with Islamic morals, ethics, character building, and core Aqeedah frameworks for everyday life.",
                    duration = "25 Hours",
                    price = 0.0,
                    rating = 4.9f,
                    instructor = "Sheikh Nouman Ali Khan",
                    isPaid = false,
                    isFree = true,
                    totalLessons = 5,
                    language = "English"
                ),
                Course(
                    id = 10,
                    title = "Spoken English & Professional Communication",
                    category = "Spoken English",
                    description = "Gain instant confidence in interviews, group presentations, daily speaking, and professional writing with clean grammatical concepts.",
                    duration = "20 Hours",
                    price = 14.99,
                    rating = 4.5f,
                    instructor = "Prof. Robert Carter",
                    isPaid = true,
                    isFree = false,
                    totalLessons = 4,
                    language = "English"
                )
            )
            courseDao.insertCourses(defaultCourses)

            // Add lessons for courses
            val defaultLessons = mutableListOf<Lesson>()
            
            // Course 1 (Quran): 6 lessons
            defaultLessons.add(Lesson(courseId = 1, title = "Introduction to Tajweed & Makhraj", duration = "15:20", videoUrl = "https://example.com/vid1.mp4", pdfUrl = "tajweed_makhraj.pdf", isLive = false, orderIndex = 0))
            defaultLessons.add(Lesson(courseId = 1, title = "Gunnah Rules and Noon Sakin", duration = "22:10", videoUrl = "https://example.com/vid2.mp4", pdfUrl = "noon_sakin.pdf", isLive = false, orderIndex = 1))
            defaultLessons.add(Lesson(courseId = 1, title = "Meem Sakin Rules and Echoing Letters", duration = "18:45", videoUrl = "https://example.com/vid3.mp4", pdfUrl = "meem_sakin.pdf", isLive = false, orderIndex = 2))
            defaultLessons.add(Lesson(courseId = 1, title = "Rules of Mudood (Elongation)", duration = "25:30", videoUrl = "https://example.com/vid4.mp4", pdfUrl = "mudood.pdf", isLive = false, orderIndex = 3))
            defaultLessons.add(Lesson(courseId = 1, title = "LIVE: Masterclass - Interactive Correction", duration = "1 hr", videoUrl = "", pdfUrl = "", isLive = true, scheduledTime = "Today, 6:00 PM", orderIndex = 4))
            defaultLessons.add(Lesson(courseId = 1, title = "Practical Recitation of Surah Mulk", duration = "35:15", videoUrl = "https://example.com/vid5.mp4", pdfUrl = "surah_mulk.pdf", isLive = false, orderIndex = 5))

            // Course 2 (Arabic): 5 lessons
            defaultLessons.add(Lesson(courseId = 2, title = "Arabic Alphabet & Short Vowels", duration = "20:00", videoUrl = "https://example.com/vid6.mp4", pdfUrl = "arabic_vowels.pdf", isLive = false, orderIndex = 0))
            defaultLessons.add(Lesson(courseId = 2, title = "Pronouns and Basic Greetings", duration = "18:30", videoUrl = "https://example.com/vid7.mp4", pdfUrl = "arabic_greetings.pdf", isLive = false, orderIndex = 1))
            defaultLessons.add(Lesson(courseId = 2, title = "Nouns, Gender and Plurals", duration = "25:10", videoUrl = "https://example.com/vid8.mp4", pdfUrl = "nouns_gender.pdf", isLive = false, orderIndex = 2))
            defaultLessons.add(Lesson(courseId = 2, title = "Verbs Conjugation Level 1", duration = "30:00", videoUrl = "https://example.com/vid9.mp4", pdfUrl = "verbs_conjugation.pdf", isLive = false, orderIndex = 3))
            defaultLessons.add(Lesson(courseId = 2, title = "LIVE: Speaking Arabic Fluently Q&A", duration = "1 hr", videoUrl = "", pdfUrl = "", isLive = true, scheduledTime = "Tomorrow, 4:00 PM", orderIndex = 4))

            // Course 3 (Tafseer)
            defaultLessons.add(Lesson(courseId = 3, title = "Linguistic Wonders of Surah Fatihah", duration = "25:00", videoUrl = "https://example.com/v10.mp4", pdfUrl = "tafseer_fatihah.pdf", isLive = false, orderIndex = 0))
            defaultLessons.add(Lesson(courseId = 3, title = "Spiritual Dimensions of daily prayers", duration = "22:00", videoUrl = "https://example.com/v11.mp4", pdfUrl = "spiritual_prayers.pdf", isLive = false, orderIndex = 1))
            defaultLessons.add(Lesson(courseId = 3, title = "Tafseer of Surah Al-Asr", duration = "19:00", videoUrl = "https://example.com/v12.mp4", pdfUrl = "surah_asr.pdf", isLive = false, orderIndex = 2))
            defaultLessons.add(Lesson(courseId = 3, title = "Tafseer of Surah Al-Ikhlas & Falak/Naas", duration = "28:15", videoUrl = "https://example.com/v13.mp4", pdfUrl = "muawwidhatayn.pdf", isLive = false, orderIndex = 3))

            // Course 5 (Computer)
            defaultLessons.add(Lesson(courseId = 5, title = "Introduction to Computer Science & Programming", duration = "18:20", videoUrl = "https://example.com/v14.mp4", pdfUrl = "cs_intro.pdf", isLive = false, orderIndex = 0))
            defaultLessons.add(Lesson(courseId = 5, title = "Kotlin Variables, Data Types & Control Flow", duration = "25:40", videoUrl = "https://example.com/v15.mp4", pdfUrl = "kotlin_basics.pdf", isLive = false, orderIndex = 1))
            defaultLessons.add(Lesson(courseId = 5, title = "Kotlin Functions & OOP Concepts", duration = "28:10", videoUrl = "https://example.com/v16.mp4", pdfUrl = "kotlin_oop.pdf", isLive = false, orderIndex = 2))
            defaultLessons.add(Lesson(courseId = 5, title = "Getting Started with Jetpack Compose UI", duration = "35:00", videoUrl = "https://example.com/v17.mp4", pdfUrl = "compose_ui.pdf", isLive = false, orderIndex = 3))
            defaultLessons.add(Lesson(courseId = 5, title = "LIVE: Real-time Debugging & UI Crafting", duration = "1.5 hrs", videoUrl = "", pdfUrl = "", isLive = true, scheduledTime = "Wed, 7:00 PM", orderIndex = 4))
            defaultLessons.add(Lesson(courseId = 5, title = "State Management in Compose with ViewModel", duration = "29:50", videoUrl = "https://example.com/v18.mp4", pdfUrl = "compose_state.pdf", isLive = false, orderIndex = 5))
            defaultLessons.add(Lesson(courseId = 5, title = "Integrating Room SQLite Database", duration = "32:10", videoUrl = "https://example.com/v19.mp4", pdfUrl = "compose_room.pdf", isLive = false, orderIndex = 6))
            defaultLessons.add(Lesson(courseId = 5, title = "Deploying App to Google Play Store", duration = "40:00", videoUrl = "https://example.com/v20.mp4", pdfUrl = "play_store.pdf", isLive = false, orderIndex = 7))

            // Generic fallback lessons for others
            val otherCourseIds = listOf(4, 6, 7, 8, 9, 10)
            for (cid in otherCourseIds) {
                defaultLessons.add(Lesson(courseId = cid, title = "Lesson 1: Introduction and Core Goals", duration = "12:15", videoUrl = "https://example.com/v.mp4", pdfUrl = "intro.pdf", isLive = false, orderIndex = 0))
                defaultLessons.add(Lesson(courseId = cid, title = "Lesson 2: Core Concepts Deep Dive", duration = "18:40", videoUrl = "https://example.com/v.mp4", pdfUrl = "concepts.pdf", isLive = false, orderIndex = 1))
                defaultLessons.add(Lesson(courseId = cid, title = "LIVE: Weekly Interactive Q&A and Review", duration = "1 hr", videoUrl = "", pdfUrl = "", isLive = true, scheduledTime = "Saturday, 5:00 PM", orderIndex = 2))
                defaultLessons.add(Lesson(courseId = cid, title = "Lesson 3: Final Synthesis & Real-world Practice", duration = "24:30", videoUrl = "https://example.com/v.mp4", pdfUrl = "synthesis.pdf", isLive = false, orderIndex = 3))
            }

            lessonDao.insertLessons(defaultLessons)
            
            // Add a welcome transaction to Wallet
            walletDao.insertTransaction(
                WalletTransaction(
                    id = "TX_WELCOME_BONUS",
                    type = "CREDIT",
                    amount = 50.0,
                    description = "Welcome Bonus Credited to Noor Wallet"
                )
            )
        }
    }
}
