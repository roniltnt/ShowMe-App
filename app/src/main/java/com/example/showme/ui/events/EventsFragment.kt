package com.example.showme.ui.events

import android.Manifest
import com.example.showme.R
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.showme.databinding.FragmentEventsBinding
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class EventsFragment : Fragment() {

    private var _binding: FragmentEventsBinding? = null
    private val binding get() = _binding!!

    private lateinit var eventsViewModel: EventsViewModel

    private lateinit var eventsAdapter: EventsAdapter

    private val eventTypes = listOf("All", "Concert", "Party", "Theater", "Festival", "Show")

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEventsBinding.inflate(inflater, container, false)

        eventsViewModel = ViewModelProvider(this)[EventsViewModel::class.java]

        // placeholder for the events screen when loading
        eventsViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                binding.shimmerViewContainer.visibility = View.VISIBLE
                binding.shimmerViewContainer.startShimmer()
                binding.recyclerViewEvents.visibility = View.GONE
            } else {
                binding.shimmerViewContainer.visibility = View.GONE
                binding.shimmerViewContainer.stopShimmer()
                binding.recyclerViewEvents.visibility = View.VISIBLE
            }
        }

        eventsAdapter = EventsAdapter(emptyList()) { event ->
            eventsViewModel.toggleFavorite(event)
        }
        binding.recyclerViewEvents.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewEvents.adapter = eventsAdapter

        // filters for event types
        setupFilterChips()

        // observe LiveData
        eventsViewModel.events.observe(viewLifecycleOwner) { events ->
            mergeDataAndUpdateAdapter()
        }

        // observe changes in favorites events
        eventsViewModel.favoriteEventIds.observe(viewLifecycleOwner) { favorites ->
            mergeDataAndUpdateAdapter()
        }

        getCurrentLocation(requireContext()) { lat, lon ->
            eventsViewModel.loadAllEvents(userLat = lat, userLng = lon)
        }

        eventsViewModel.loadUserFavorites()

        return binding.root
    }

    private fun setupFilterChips() {
        val chipGroup = binding.chipGroupFilter
        chipGroup.removeAllViews()

        var allChipId = View.NO_ID

        eventTypes.forEach { type ->
            val chip = layoutInflater.inflate(R.layout.item_chip_filter, chipGroup, false) as Chip
            chip.text = type
            chip.id = View.generateViewId()
            chip.tag = type
            chip.isCheckable = true

            val iconResId = when (type.lowercase()) {
                "concert" -> R.drawable.ic_concert_24
                "party" -> R.drawable.ic_party_24
                "theater" -> R.drawable.ic_theater_24
                "festival" -> R.drawable.ic_festival_24
                "show" -> R.drawable.ic_show_24
                "all" -> R.drawable.ic_select_all_24
                else -> R.drawable.ic_show_24
            }
            chip.setChipIconResource(iconResId)

            chipGroup.addView(chip)
            if (type == "All") {
                chip.isChecked = true
                allChipId = chip.id
            }
        }

        val listenerObject = object : ChipGroup.OnCheckedStateChangeListener {
            override fun onCheckedChanged(group: ChipGroup, checkedIds: List<Int>) {
                group.setOnCheckedStateChangeListener(null)

                var newSelectedTypes = mutableSetOf<String>()

                var shouldRecheckAll = false

                // if nothing chose - put 'All' in default
                if (checkedIds.isEmpty()) {
                    shouldRecheckAll = true
                    newSelectedTypes = mutableSetOf()
                }

                // canceling 'All' when something else chosen
                else if (checkedIds.contains(allChipId) && checkedIds.size > 1) {
                    group.findViewById<Chip>(allChipId)?.isChecked = false

                    checkedIds.forEach { chipId ->
                        if (chipId != allChipId) {
                            (group.findViewById<Chip>(chipId)?.tag as? String)?.let {
                                newSelectedTypes.add(it)
                            }
                        }
                    }
                }
                // when only 'All' or multiple chosen
                else {
                    if (checkedIds.size == 1 && checkedIds.contains(allChipId)) {
                        newSelectedTypes = mutableSetOf()
                    } else {
                        checkedIds.forEach { chipId ->
                            (group.findViewById<Chip>(chipId)?.tag as? String)?.let {
                                newSelectedTypes.add(it)
                            }
                        }
                    }
                }

                // update ViewModel with filter/s
                eventsViewModel.updateEventTypeFilters(newSelectedTypes)

                if (shouldRecheckAll) {
                    group.check(allChipId)
                }

                group.setOnCheckedStateChangeListener(this)
            }
        }

        // listen to filter changes
        chipGroup.setOnCheckedStateChangeListener(listenerObject)

        // initial filter
        eventsViewModel.updateEventTypeFilters(emptySet())
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getCurrentLocation(context: Context, callback: (Double, Double) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        val cancellationTokenSource = CancellationTokenSource()

        fusedLocationClient.getCurrentLocation(
            Priority.PRIORITY_HIGH_ACCURACY,
            cancellationTokenSource.token
        )
            .addOnSuccessListener { location ->
                if (location != null) {
                    // Log for checking wrong locations
                    Log.d("LocationDebug", "Successfully got current location: ${location.latitude}, ${location.longitude}")
                    callback(location.latitude, location.longitude)
                } else {
                    // Log for null location
                    Log.w("LocationDebug", "getCurrentLocation returned null, using fallback.")
                    callback(32.0853, 34.7818) // Tel Aviv, default
                }
            }
            .addOnFailureListener { e ->
                // Log for failed of getting any location
                Log.e("LocationDebug", "Failed to get current location", e)
                callback(32.0853, 34.7818) // Tel Aviv, default
            }
    }

    private fun mergeDataAndUpdateAdapter() {
        val currentEvents = eventsViewModel.events.value ?: emptyList()

        val favorites = eventsViewModel.favoriteEventIds.value ?: emptySet()

        val mergedEvents = currentEvents.map { event ->
            event.copy(isFavorite = event.id in favorites)
        }

        eventsAdapter.updateEvents(mergedEvents)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
