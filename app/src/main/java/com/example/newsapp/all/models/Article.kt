package com.example.newsapp.all.models

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity("articles")
data class Article(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null, //Так как не все артиклы мы будем сохранять в бд
    val author: String,
    val content: String,
    val description: String,
    val publishedAt: String,
    val source: Source,
    val title: String,
    val url: String,
    val urlToImage: String
) : java.io.Serializable {
    override fun hashCode(): Int {
        var result = id.hashCode()
        if (url.isNullOrEmpty()) {
            result = 31 * result + url.hashCode()
        }
        return result
    }
}