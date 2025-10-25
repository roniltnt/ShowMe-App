package com.example.showme.ui.search

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.showme.R
import com.example.showme.data.Event
import com.example.showme.data.SearchResult
import com.example.showme.databinding.FragmentSearchBinding
import com.example.showme.ui.events.EventsAdapter
import com.example.showme.ui.events.EventsViewModel
import java.util.Calendar

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    private val searchViewModel: SearchViewModel by viewModels()

    // need fo search events
    private val eventsViewModel: EventsViewModel by viewModels()

    private lateinit var adapter: EventsAdapter

    private var selectedDate: String? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSearchBinding.inflate(inflater, container, false)

        val root = binding.root

        setupRecyclerView()

        setupObservers()

        setupSearch()

        setupDateButton()

        eventsViewModel.loadUserFavorites()

        binding.editTextSearch.isEnabled = false

        binding.buttonPickDate.isEnabled = false

        binding.editTextSearch.hint = "Loading events..."

        return root
    }

    private fun setupRecyclerView() {
        adapter = EventsAdapter(emptyList()) { event ->
            eventsViewModel.toggleFavorite(event)
        }

        binding.recyclerViewSearchResults.layoutManager = LinearLayoutManager(requireContext())

        binding.recyclerViewSearchResults.adapter = adapter
    }

    private fun setupDateButton() {
        binding.buttonPickDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(requireContext(), { _, selectedYear, selectedMonth, selectedDay ->
                // add 0 for one digit month number
                val monthStr = (selectedMonth + 1).toString().padStart(2, '0')

                // add 0 for one digit day number
                val dayStr = selectedDay.toString().padStart(2, '0')

                selectedDate = "$selectedYear-$monthStr-$dayStr"

                binding.buttonPickDate.text = selectedDate

                binding.buttonClearDate.visibility = View.VISIBLE

                triggerSearch()

            }, year, month, day).show()
        }

        binding.buttonClearDate.setOnClickListener {
            selectedDate = null

            binding.buttonPickDate.text = "Select Date"

            binding.buttonClearDate.visibility = View.GONE

            triggerSearch()
        }
    }

    // 3 options - after text was written, before text was written & while text is written
    private fun setupSearch() {
        binding.editTextSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                triggerSearch()
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })
    }

    // call to ViewModel with update search
    private fun triggerSearch() {
        if (searchViewModel.isReadyToSearch.value == true) {
            val query = binding.editTextSearch.text.toString().trim()
            searchViewModel.searchEvents(query, selectedDate)
        }
    }

    private fun updateAdapterWithEvents(events: List<Event>) {
        val favorites = eventsViewModel.favoriteEventIds.value ?: emptySet()

        val mergedEvents = events.map { event ->
            event.copy(isFavorite = event.id in favorites)
        }

        adapter.updateEvents(mergedEvents)
    }

    private fun setupObservers() {
        // observe search results
        searchViewModel.searchResults.observe(viewLifecycleOwner) { result ->
            binding.initialSearchLayout.visibility = View.GONE

            binding.searchStatusLayout.visibility = View.GONE

            binding.recyclerViewSearchResults.visibility = View.VISIBLE

            when (result) {
                is SearchResult.Success -> {
                    updateAdapterWithEvents(result.events)
                }

                is SearchResult.Fallback -> {
                    binding.searchStatusLayout.visibility = View.VISIBLE

                    binding.imageSearchStatus.setImageResource(R.drawable.ic_search_off_24)

                    binding.textSearchStatus.text = result.message

                    updateAdapterWithEvents(result.events)
                }

                is SearchResult.NoResultsAtAll -> {
                    binding.searchStatusLayout.visibility = View.VISIBLE

                    binding.imageSearchStatus.setImageResource(R.drawable.ic_search_off_24)

                    binding.textSearchStatus.text = "No results found."

                    updateAdapterWithEvents(emptyList())

                    binding.recyclerViewSearchResults.visibility = View.GONE
                }

                is SearchResult.EmptyQuery -> {
                    binding.initialSearchLayout.visibility = View.VISIBLE

                    updateAdapterWithEvents(emptyList())

                    binding.recyclerViewSearchResults.visibility = View.GONE
                }
            }
        }

        // observe changes in favorites events
        eventsViewModel.favoriteEventIds.observe(viewLifecycleOwner) { favorites ->
            val currentResult = searchViewModel.searchResults.value

            if (currentResult is SearchResult.Success) {
                updateAdapterWithEvents(currentResult.events)
            } else if (currentResult is SearchResult.Fallback) {
                updateAdapterWithEvents(currentResult.events)
            }
        }

        // observe when ready to search
        searchViewModel.isReadyToSearch.observe(viewLifecycleOwner) { isReady ->
            if (isReady) {
                binding.editTextSearch.isEnabled = true

                binding.buttonPickDate.isEnabled = true

                binding.editTextSearch.hint = "Search by artist or location"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _binding = null
    }
}