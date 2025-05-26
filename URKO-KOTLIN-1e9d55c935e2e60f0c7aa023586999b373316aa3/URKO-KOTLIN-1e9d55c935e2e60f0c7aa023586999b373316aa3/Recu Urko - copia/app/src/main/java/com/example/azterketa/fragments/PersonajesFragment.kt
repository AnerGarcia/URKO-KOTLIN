
package com.example.azterketa.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.example.azterketa.databinding.FragmentPersonajesBinding
import com.example.azterketa.utils.NotificationHelper
import com.example.azterketa.viewmodels.MainViewModel
import com.example.azterketa.views.adapters.PersonajeAdapter

class PersonajesFragment : Fragment() {

    private var _binding: FragmentPersonajesBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: PersonajeAdapter
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPersonajesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        notificationHelper = NotificationHelper(requireContext())
        setupRecyclerView()
        setupSwipeToDelete()
        observeViewModel()
        setupSearchButton()
    }

    private fun setupRecyclerView() {
        val columns = if (resources.configuration.orientation == 1) 2 else 4
        binding.rvPersonajes.layoutManager = GridLayoutManager(requireContext(), columns)
        adapter = PersonajeAdapter(requireContext()) { personaje ->
            viewModel.toggleFavorito(personaje)
            val message = if (personaje.esFavorito)
                "Eliminado de favoritos" else "Añadido a favoritos"
            notificationHelper.showNotification("Favoritos", message)
        }
        binding.rvPersonajes.adapter = adapter
    }

    private fun setupSwipeToDelete() {
        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(
            0, ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val personaje = adapter.currentList[position]
                viewModel.toggleFavorito(personaje)

                val message = if (personaje.esFavorito)
                    "Eliminado de favoritos" else "Añadido a favoritos"
                notificationHelper.showNotification("Swipe Action", message)
            }
        })
        itemTouchHelper.attachToRecyclerView(binding.rvPersonajes)
    }

    private fun observeViewModel() {
        viewModel.personajes.observe(viewLifecycleOwner) { personajes ->
            adapter.submitList(personajes)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let {
                notificationHelper.showNotification("Error", it)
            }
        }
    }

    private fun setupSearchButton() {
        binding.tilBuscar.setEndIconOnClickListener {
            val query = binding.tietBuscar.text.toString()
            viewModel.buscarPersonaje(query)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}