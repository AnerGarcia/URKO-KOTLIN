package com.example.azterketa.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.azterketa.R
import com.example.azterketa.databinding.FragmentFavoritosBinding
import com.example.azterketa.utils.NotificationHelper
import com.example.azterketa.viewmodels.MainViewModel
import com.example.azterketa.views.adapters.PersonajeAdapter

class FavoritosFragment : Fragment() {

    private var _binding: FragmentFavoritosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: PersonajeAdapter
    private lateinit var notificationHelper: NotificationHelper

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritosBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initializeComponents()
        setupRecyclerView()
        setupSwipeToRemove()
        observeViewModel()
    }

    private fun initializeComponents() {
        notificationHelper = NotificationHelper(requireContext())
    }

    private fun setupRecyclerView() {
        binding.rvFavoritos.layoutManager = LinearLayoutManager(requireContext())

        adapter = PersonajeAdapter(requireContext()) { personaje ->
            viewModel.toggleFavorito(personaje)

            // Mostrar notificación apropiada
            val message = if (personaje.esFavorito) {
                getString(R.string.removed_from_favorites, personaje.personaje)
            } else {
                getString(R.string.added_to_favorites, personaje.personaje)
            }
            notificationHelper.showNotification(getString(R.string.favorites), message)
        }

        binding.rvFavoritos.adapter = adapter
    }

    private fun setupSwipeToRemove() {
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
                if (position != RecyclerView.NO_POSITION) {
                    val personaje = adapter.currentList[position]

                    // Remover de favoritos
                    viewModel.toggleFavorito(personaje)

                    // Mostrar notificación
                    val message = getString(R.string.removed_from_favorites, personaje.personaje)
                    notificationHelper.showNotification(
                        getString(R.string.favorites),
                        message
                    )
                }
            }

            override fun getSwipeDirs(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder
            ): Int {
                // Solo permitir swipe si hay elementos en la lista
                return if (adapter.currentList.isNotEmpty()) {
                    super.getSwipeDirs(recyclerView, viewHolder)
                } else {
                    0
                }
            }
        })

        itemTouchHelper.attachToRecyclerView(binding.rvFavoritos)
    }

    private fun observeViewModel() {
        viewModel.favoritos.observe(viewLifecycleOwner) { favoritos ->
            adapter.submitList(favoritos)
            updateEmptyState(favoritos.isEmpty())
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar?.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            error?.let { errorMessage ->
                notificationHelper.showNotification(
                    getString(R.string.error),
                    errorMessage
                )
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.rvFavoritos.visibility = View.GONE
            binding.tvNoFavoritos.visibility = View.VISIBLE

            // Si tienes un layout de estado vacío más elaborado
            binding.tvNoFavoritos.text = getString(R.string.no_favorites_message)
        } else {
            binding.rvFavoritos.visibility = View.VISIBLE
            binding.tvNoFavoritos.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        // Refrescar los favoritos cuando se vuelve al fragment
        viewModel.refreshFavoritos()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}