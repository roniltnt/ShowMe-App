package com.example.showme.ui.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.showme.databinding.FragmentFavoritesBinding
import com.example.showme.ui.events.EventsAdapter
import com.example.showme.ui.events.EventsViewModel

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val eventsViewModel: EventsViewModel by activityViewModels()

    private lateinit var adapter: EventsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = EventsAdapter(emptyList()) { event ->
            eventsViewModel.toggleFavorite(event)
        }

        binding.recyclerViewFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerViewFavorites.adapter = adapter

        setupObservers()

        eventsViewModel.loadUserFavorites()
    }

    private fun setupObservers() {
        eventsViewModel.favoriteEvents.observe(viewLifecycleOwner) { favorites ->
            binding.progressBarFavorites.visibility = View.GONE

            if (favorites.isEmpty()) {
                binding.emptyFavoritesLayout.visibility = View.VISIBLE
                binding.recyclerViewFavorites.visibility = View.GONE
            } else {
                binding.emptyFavoritesLayout.visibility = View.GONE
                binding.recyclerViewFavorites.visibility = View.VISIBLE
            }

            val updatedList = favorites.map { it.copy(isFavorite = true) }

            adapter.updateEvents(updatedList)
        }

        eventsViewModel.isLoadingFavorites.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBarFavorites.visibility = if (isLoading) View.VISIBLE else View.GONE

            if (isLoading) {
                binding.recyclerViewFavorites.visibility = View.GONE
                binding.textEmptyFavorites.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}