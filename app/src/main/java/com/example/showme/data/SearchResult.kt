package com.example.showme.data

// sealed force us to check all possibilities!
sealed class SearchResult {
    data class Success(val events: List<Event>) : SearchResult()

    data class Fallback(val message: String, val events: List<Event>) : SearchResult()

    object NoResultsAtAll : SearchResult()

    object EmptyQuery : SearchResult()
}