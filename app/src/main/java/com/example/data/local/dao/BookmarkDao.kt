package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.ArticleBookmarkEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BookmarkDao {
    @Query("SELECT articleId FROM article_bookmarks ORDER BY bookmarkedAt DESC")
    fun observeAllBookmarkedIds(): Flow<List<String>>

    @Query("SELECT EXISTS(SELECT 1 FROM article_bookmarks WHERE articleId = :articleId)")
    fun observeIsBookmarked(articleId: String): Flow<Boolean>

    @Query("SELECT EXISTS(SELECT 1 FROM article_bookmarks WHERE articleId = :articleId)")
    suspend fun isBookmarked(articleId: String): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addBookmark(entity: ArticleBookmarkEntity)

    @Query("DELETE FROM article_bookmarks WHERE articleId = :articleId")
    suspend fun removeBookmark(articleId: String)
}
