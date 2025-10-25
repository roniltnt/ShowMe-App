package com.example.showme.data

import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.Exclude

data class Event(
    @DocumentId val id: String = "",
    val name: String = "",
    val location: String = "",
    val date: String = "",
    val artistName: String = "",
    val ticketUrl: String = "",
    val mapUrl: String = "",
    val playlistUrl: String = "",
    val imageUrl: String = "",
    val lat: Double = 0.0, // Latitude
    val lng: Double = 0.0, // Longitude
    val eventType: String = "Show",

    // this variables write or read from Firebase!
    @get:Exclude @set:Exclude var isFavorite: Boolean = false,
    @get:Exclude @set:Exclude var distance: Float? = null
)

