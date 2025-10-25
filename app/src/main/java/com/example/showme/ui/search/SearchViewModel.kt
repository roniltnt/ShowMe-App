package com.example.showme.ui.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.showme.data.Event
import com.example.showme.data.SearchResult
import com.example.showme.utils.Constants.EVENTS_COLLECTION
import com.google.firebase.firestore.FirebaseFirestore

class SearchViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _searchResults = MutableLiveData<SearchResult>()
    val searchResults: LiveData<SearchResult> = _searchResults

    private val _isReadyToSearch = MutableLiveData<Boolean>(false)
    val isReadyToSearch: LiveData<Boolean> = _isReadyToSearch

    private var allEvents: List<Event> = emptyList()

    init {
        fetchAllEvents()
    }

    private fun fetchAllEvents() {
        firestore.collection(EVENTS_COLLECTION)
            .limit(100)
            .get()
            .addOnSuccessListener { result ->
                allEvents = result.documents.mapNotNull { it.toObject(Event::class.java) }

                _isReadyToSearch.value = true
            }
            .addOnFailureListener {
                _isReadyToSearch.value = true
            }
    }

    fun searchEvents(query: String, date: String?) {
        val hasQuery = query.isNotBlank()

        val hasDate = date != null

        // if all empty, clean results
        if (!hasQuery && !hasDate) {
            _searchResults.value = SearchResult.EmptyQuery

            return
        }

        val lowercaseQuery = query.lowercase()

        // search by text or date
        if (hasQuery && hasDate) {
            val primaryResults = allEvents.filter {
                (it.artistName.lowercase().contains(lowercaseQuery) || it.location.lowercase().contains(lowercaseQuery)) &&
                        it.date == date
            }

            if (primaryResults.isNotEmpty()) {
                _searchResults.value = SearchResult.Success(primaryResults)
                return
            }

            // search by text
            val fallbackByQuery = allEvents.filter {
                it.artistName.lowercase().contains(lowercaseQuery) || it.location.lowercase().contains(lowercaseQuery)
            }

            // if failed to find by text
            if (fallbackByQuery.isNotEmpty()) {
                val message = "No results for '$query' on $date.\nShowing other results for '$query':"

                _searchResults.value = SearchResult.Fallback(message, fallbackByQuery)

                return
            }

            // search by date
            val fallbackByDate = allEvents.filter { it.date == date }

            // if failed to find by date
            if (fallbackByDate.isNotEmpty()) {
                val message = "No results for '$query' on $date.\nShowing other events on $date:"

                _searchResults.value = SearchResult.Fallback(message, fallbackByDate)

                return
            }

            // if failed to find nothing
            _searchResults.value = SearchResult.NoResultsAtAll

            return
        }

        // search only by text
        if (hasQuery && !hasDate) {
            val results = allEvents.filter {
                it.artistName.lowercase().contains(lowercaseQuery) || it.location.lowercase().contains(lowercaseQuery)
            }

            if (results.isNotEmpty()) {
                _searchResults.value = SearchResult.Success(results)
            } else {
                _searchResults.value = SearchResult.NoResultsAtAll
            }

            return
        }

        // search only by date
        if (!hasQuery && hasDate) {
            val results = allEvents.filter { it.date == date }

            if (results.isNotEmpty()) {
                _searchResults.value = SearchResult.Success(results)
            } else {
                _searchResults.value = SearchResult.NoResultsAtAll
            }

            return
        }
    }
}