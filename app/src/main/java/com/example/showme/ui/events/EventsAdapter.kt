package com.example.showme.ui.events

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.showme.R
import com.example.showme.data.Event
import com.bumptech.glide.Glide
import androidx.core.net.toUri
import com.example.showme.FullscreenImageActivity
import com.example.showme.data.YouTubeResponse
import com.example.showme.utils.ApiClient
import com.example.showme.utils.Constants.YOUTUBE_API_KEY
import com.google.android.material.button.MaterialButton

class EventsAdapter(private var events: List<Event>,
                    private val onFavoriteClick: (Event) -> Unit) : RecyclerView.Adapter<EventsAdapter.EventViewHolder>() {

    class EventViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val eventImage: ImageView = itemView.findViewById(R.id.eventImage)

        val iconEventType: ImageView = itemView.findViewById(R.id.iconEventType)

        val eventName: TextView = itemView.findViewById(R.id.eventName)

        val eventDate: TextView = itemView.findViewById(R.id.eventDate)

        val eventLocation: TextView = itemView.findViewById(R.id.eventLocation)

        val buttonTicket: Button = itemView.findViewById(R.id.buttonTicket)

        val buttonDirections: Button = itemView.findViewById(R.id.buttonDirections)

        val buttonPlaylist: Button = itemView.findViewById(R.id.buttonPlaylist)

        val buttonFavorite: ImageButton = itemView.findViewById(R.id.buttonFavorite)

        val buttonShare: MaterialButton = itemView.findViewById(R.id.buttonShare)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EventViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_event, parent, false)

        return EventViewHolder(view)
    }

    override fun onBindViewHolder(holder: EventViewHolder, position: Int) {
        val event = events[position]
        holder.eventName.text = event.name
        holder.eventDate.text = event.date
        holder.eventLocation.text = event.location

        // for showing the event image
        Glide.with(holder.itemView.context)
            .load(event.imageUrl)
            .into(holder.eventImage)

        holder.eventImage.setOnClickListener { view ->
            val context = view.context

            val intent = Intent(context, FullscreenImageActivity::class.java).apply {
                // click on the image will show it in fullscreen
                putExtra(FullscreenImageActivity.EXTRA_IMAGE_URL, event.imageUrl)
            }

            context.startActivity(intent)
        }

        // choose the icon that used depends on the event type
        val eventTypeIconRes = when (event.eventType.lowercase()) {
            "concert" -> R.drawable.ic_concert_24
            "party" -> R.drawable.ic_party_24
            "theater" -> R.drawable.ic_theater_24
            "festival" -> R.drawable.ic_festival_24
            "show" -> R.drawable.ic_show_24
            else -> R.drawable.ic_show_24 // default
        }

        holder.iconEventType.setImageResource(eventTypeIconRes)

        holder.buttonTicket.setOnClickListener {
            openUrl(holder.itemView.context, event.ticketUrl)
        }

        holder.buttonDirections.setOnClickListener {
            val context = holder.itemView.context
            val locationName = event.location

            // open the directions to the event
            val intentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${Uri.encode(locationName)}")

            val mapIntent = Intent(Intent.ACTION_VIEW, intentUri)

            context.startActivity(mapIntent)
        }

        holder.buttonPlaylist.setOnClickListener {
            val context = holder.itemView.context

            // Youtube API call
            val call = ApiClient.youtubeApi.searchVideos(
                apiKey = YOUTUBE_API_KEY,
                query = "${event.artistName} official playlist"
            )

            call.enqueue(object : retrofit2.Callback<YouTubeResponse> {
                override fun onResponse(
                    call: retrofit2.Call<YouTubeResponse>,
                    response: retrofit2.Response<YouTubeResponse>
                ) {
                    if (response.isSuccessful) {
                        val item = response.body()?.items?.firstOrNull()
                        var url = ""

                        // playlist/video was found, general search if wasn't
                        if (item?.id?.playlistId != null) {
                            url = "https://www.youtube.com/playlist?list=${item.id.playlistId}"
                        } else if (item?.id?.videoId != null) {
                            url = "https://www.youtube.com/watch?v=${item.id.videoId}"
                        } else {
                            url = "https://www.youtube.com/results?search_query=${Uri.encode(event.artistName)}"
                        }

                        // open found link
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)

                    } else {
                        val url = "https://www.youtube.com/results?search_query=${Uri.encode(event.artistName)}"
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        context.startActivity(intent)
                    }
                }

                override fun onFailure(call: retrofit2.Call<YouTubeResponse>, t: Throwable) {
                    val url = "https://www.youtube.com/results?search_query=${Uri.encode(event.artistName)}"
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                    context.startActivity(intent)
                }
            })
        }

        updateFavoriteIcon(holder.buttonFavorite, event.isFavorite)

        holder.buttonFavorite.setOnClickListener {
            onFavoriteClick(event)
        }

        holder.buttonShare.setOnClickListener {
            val context = holder.itemView.context

            val ticketLink = if (event.ticketUrl.isNotEmpty()) {
                "Get tickets here: ${event.ticketUrl}"
            } else {
                "No ticket link available."
            }

            val shareText = "Check out this event: ${event.name} in ${event.location}!\n\n$ticketLink"

            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareText)
                type = "text/plain"
            }
            context.startActivity(Intent.createChooser(intent, "Share event via"))
        }

    }

    override fun getItemCount() = events.size

    fun updateEvents(newEvents: List<Event>) {
        events = newEvents
        notifyDataSetChanged()
    }

    fun openUrl(context: Context, url: String) {
        if (url.isNotEmpty()) {
            val intent = Intent(Intent.ACTION_VIEW, url.toUri())
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "Link not available", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateFavoriteIcon(button: ImageButton, isFavorite: Boolean) {
        val iconRes = if (isFavorite) R.drawable.ic_favorite_filled_24 else R.drawable.ic_favorite_border_24
        button.setImageResource(iconRes)
    }

}
