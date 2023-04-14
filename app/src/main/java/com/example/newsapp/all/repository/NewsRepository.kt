package com.example.newsapp.all.repository

import com.example.newsapp.all.api.RetrofitInstance
import com.example.newsapp.all.db.ArticleDatabase
import com.example.newsapp.all.models.Article
import com.example.newsapp.all.models.NewsResponse
import retrofit2.Response

class NewsRepository(
    val db: ArticleDatabase
) {

    suspend fun getBreakingNews(country: String, pageNumber: Int): Response<NewsResponse> {
        return RetrofitInstance.api.getBreakingNews(country, pageNumber)
    }

    suspend fun searchNews(searchQuery: String, pageNumber: Int): Response<NewsResponse> {
        return RetrofitInstance.api.searchNews(searchQuery, pageNumber)
    }

    suspend fun insertArticle(article: Article) = db.getArticleDao().insertArticle(article)

    fun getAllArticles() = db.getArticleDao().getAllArticles()

    suspend fun deleteArticle(article: Article) = db.getArticleDao().deleteArticle(article)
}