package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: Int,
    val title: String,
    val category: String,
    val description: String,
    val duration: String,
    val price: Double,
    val rating: Float,
    val instructor: String,
    val isPaid: Boolean,
    val isFree: Boolean,
    val imageUrl: String = "",
    val totalLessons: Int,
    val language: String = "English", // English, Urdu, Farsi, Arabic, Computer, English
    val isWishlisted: Boolean = false,
    val isBookmarked: Boolean = false,
    val isPurchased: Boolean = false
)

@Entity(tableName = "lessons")
data class Lesson(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val title: String,
    val duration: String,
    val videoUrl: String = "",
    val pdfUrl: String = "",
    val isCompleted: Boolean = false,
    val isDownloaded: Boolean = false,
    val isLive: Boolean = false,
    val scheduledTime: String = "",
    val orderIndex: Int = 0
)

@Entity(tableName = "progress")
data class UserProgress(
    @PrimaryKey val courseId: Int,
    val progressPercentage: Int = 0,
    val lastWatchedLessonId: Int = 0,
    val attendanceDays: Int = 0,
    val isCertificateGenerated: Boolean = false
)

@Entity(tableName = "wallet_transactions")
data class WalletTransaction(
    @PrimaryKey val id: String,
    val type: String, // CREDIT, DEBIT
    val amount: Double,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "quiz_results")
data class QuizResult(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val courseId: Int,
    val courseTitle: String,
    val score: Int,
    val totalQuestions: Int,
    val passed: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)
