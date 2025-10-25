package com.example.showme.ui.events

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.showme.data.Event
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import android.location.Location
import com.example.showme.utils.Constants.EVENTS_COLLECTION
import com.example.showme.utils.Constants.USERS_COLLECTION
import com.example.showme.utils.Constants.FAVORITES_COLLECTION
import com.example.showme.utils.Constants.USER_LOCATION
import com.example.showme.utils.Constants.EVENT_LOCATION
import java.time.LocalDate
import android.os.Handler
import android.os.Looper

class EventsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    private val auth = FirebaseAuth.getInstance()
    private val userId = auth.currentUser?.uid

    private var currentUserLat: Double = 0.0
    private var currentUserLng: Double = 0.0

    private var eventsListener: ListenerRegistration? = null

    private val _events = MutableLiveData<List<Event>>()
    val events: LiveData<List<Event>> = _events

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private var allFetchedEvents: List<Event> = emptyList()
    private val _selectedEventTypeFilters = MutableLiveData<Set<String>>(emptySet())

    private val _favoriteEventIds = MutableLiveData<Set<String>>(emptySet())
    val favoriteEventIds: LiveData<Set<String>> = _favoriteEventIds

    private val _favoriteEvents = MutableLiveData<List<Event>>()
    val favoriteEvents: LiveData<List<Event>> = _favoriteEvents

    private val _isLoadingFavorites = MutableLiveData<Boolean>(false)
    val isLoadingFavorites: LiveData<Boolean> = _isLoadingFavorites

    private var favoritesListener: ListenerRegistration? = null

    fun loadAllEvents(userLat: Double, userLng: Double) {
        listenToEventsFromFirestore(userLat, userLng)
    }

    private fun listenToEventsFromFirestore(userLat: Double, userLng: Double) {
        currentUserLat = userLat
        currentUserLng = userLng

        _isLoading.value = true

        eventsListener?.remove()


        eventsListener = firestore.collection(EVENTS_COLLECTION)
            .addSnapshotListener { snapshot, error ->

                // delay for the illusion that it takes time to load all events
                Handler(Looper.getMainLooper()).postDelayed({
                    if (error != null) {
                        _isLoading.value = false

                        return@postDelayed
                    }

                    if (snapshot != null) {
                        allFetchedEvents = snapshot.documents.mapNotNull { doc ->
                            doc.toObject(Event::class.java)
                        }

                        applyFiltersAndSort()
                    } else {
                        allFetchedEvents = emptyList()

                        applyFiltersAndSort()
                    }

                    _isLoading.value = false
                }, 1000)
            }
    }


    private fun applyFiltersAndSort() {
        val currentFilters = _selectedEventTypeFilters.value ?: emptySet()

        // filter by different types
        val filteredByType = if (currentFilters.isEmpty()) {
            allFetchedEvents
        } else {
            allFetchedEvents.filter { event ->
                event.eventType.lowercase() in currentFilters.map { it.lowercase() }
            }
        }

        // kick out past events
        val today = LocalDate.now()
        val futureEvents = filteredByType.filter { event ->
            if (event.date.isEmpty()) return@filter false
            try {
                val eventDate = LocalDate.parse(event.date)
                eventDate.isAfter(today) || eventDate.isEqual(today)
            } catch (e: Exception) {
                false
            }
        }

        // calculate distance between user and event location
        val userLocation = Location(USER_LOCATION)
        userLocation.latitude = currentUserLat
        userLocation.longitude = currentUserLng

        futureEvents.forEach { event ->
            if (event.lat != 0.0 || event.lng != 0.0) {
                val eventLocation = Location(EVENT_LOCATION)
                eventLocation.latitude = event.lat
                eventLocation.longitude = event.lng
                event.distance = userLocation.distanceTo(eventLocation)
            } else {
                event.distance = Float.MAX_VALUE
            }
        }

        // sort the events by distance & date
        val sortedList = futureEvents.sortedWith(
            compareBy<Event> { it.distance }
                .thenBy { LocalDate.parse(it.date) }
        )

        // update LiveData
        val favorites = _favoriteEventIds.value ?: emptySet()
        _events.value = sortedList.map {
            it.copy(isFavorite = it.id in favorites)
        }
    }

    private fun mergeEventsWithFavorites() {
        val currentDisplayedEvents = _events.value ?: return

        val favorites = _favoriteEventIds.value ?: emptySet()

        if (currentDisplayedEvents.any { it.isFavorite != (it.id in favorites) }) {
            _events.value = currentDisplayedEvents.map {
                it.copy(isFavorite = it.id in favorites)
            }
        }
    }

    fun loadUserFavorites() {
        if (userId == null) return

        _isLoadingFavorites.value = true

        favoritesListener?.remove()

        favoritesListener = firestore.collection(USERS_COLLECTION).document(userId).collection(FAVORITES_COLLECTION)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    _isLoadingFavorites.value = false
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val ids = snapshot.documents.map { it.id }.toSet()
                    _favoriteEventIds.value = ids

                    val eventsList = snapshot.documents.mapNotNull { it.toObject(Event::class.java) }
                    _favoriteEvents.value = eventsList
                }

                mergeEventsWithFavorites()
            }
    }

    fun toggleFavorite(event: Event) {
        if (userId == null || event.id.isEmpty()) {
            return
        }

        val favRef = firestore.collection(USERS_COLLECTION).document(userId)
            .collection(FAVORITES_COLLECTION).document(event.id)

        val currentFavorites = _favoriteEventIds.value ?: emptySet()

        if (event.id in currentFavorites) {
            // delete event from favorites
            favRef.delete().addOnFailureListener {
            }
        } else {
            // add event to favorites
            favRef.set(event).addOnFailureListener {
            }
        }
    }

    fun updateEventTypeFilters(selectedTypes: Set<String>) {
        if (_selectedEventTypeFilters.value != selectedTypes) {
            _selectedEventTypeFilters.value = selectedTypes

            applyFiltersAndSort()
        }
    }

    override fun onCleared() {
        super.onCleared()
        favoritesListener?.remove()
        eventsListener?.remove()
    }

}
