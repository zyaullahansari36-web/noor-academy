package com.example.data.dao

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY id ASC")
    fun getAllCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :courseId")
    fun getCourseById(courseId: Int): Flow<Course?>

    @Query("SELECT * FROM courses WHERE category = :category")
    fun getCoursesByCategory(category: String): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE isWishlisted = 1")
    fun getWishlistedCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE isBookmarked = 1")
    fun getBookmarkedCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE isPurchased = 1")
    fun getPurchasedCourses(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE title LIKE '%' || :query || '%' OR category LIKE '%' || :query || '%'")
    fun searchCourses(query: String): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourses(courses: List<Course>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Update
    suspend fun updateCourse(course: Course)

    @Query("UPDATE courses SET isWishlisted = :isWishlisted WHERE id = :courseId")
    suspend fun updateWishlistStatus(courseId: Int, isWishlisted: Boolean)

    @Query("UPDATE courses SET isBookmarked = :isBookmarked WHERE id = :courseId")
    suspend fun updateBookmarkStatus(courseId: Int, isBookmarked: Boolean)

    @Query("UPDATE courses SET isPurchased = 1 WHERE id = :courseId")
    suspend fun markCoursePurchased(courseId: Int)
}

@Dao
interface LessonDao {
    @Query("SELECT * FROM lessons WHERE courseId = :courseId ORDER BY orderIndex ASC")
    fun getLessonsForCourse(courseId: Int): Flow<List<Lesson>>

    @Query("SELECT * FROM lessons WHERE id = :lessonId")
    fun getLessonById(lessonId: Int): Flow<Lesson?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLessons(lessons: List<Lesson>)

    @Query("UPDATE lessons SET isCompleted = :isCompleted WHERE id = :lessonId")
    suspend fun updateLessonCompletion(lessonId: Int, isCompleted: Boolean)

    @Query("UPDATE lessons SET isDownloaded = :isDownloaded WHERE id = :lessonId")
    suspend fun updateDownloadStatus(lessonId: Int, isDownloaded: Boolean)
}

@Dao
interface UserProgressDao {
    @Query("SELECT * FROM progress WHERE courseId = :courseId")
    fun getProgressForCourse(courseId: Int): Flow<UserProgress?>

    @Query("SELECT * FROM progress")
    fun getAllProgress(): Flow<List<UserProgress>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateProgress(progress: UserProgress)

    @Query("UPDATE progress SET attendanceDays = attendanceDays + 1 WHERE courseId = :courseId")
    suspend fun incrementAttendance(courseId: Int)

    @Query("UPDATE progress SET isCertificateGenerated = 1 WHERE courseId = :courseId")
    suspend fun markCertificateGenerated(courseId: Int)
}

@Dao
interface WalletDao {
    @Query("SELECT * FROM wallet_transactions ORDER BY timestamp DESC")
    fun getAllTransactions(): Flow<List<WalletTransaction>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(transaction: WalletTransaction)
}

@Dao
interface QuizDao {
    @Query("SELECT * FROM quiz_results ORDER BY timestamp DESC")
    fun getAllQuizResults(): Flow<List<QuizResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuizResult(result: QuizResult)
}
