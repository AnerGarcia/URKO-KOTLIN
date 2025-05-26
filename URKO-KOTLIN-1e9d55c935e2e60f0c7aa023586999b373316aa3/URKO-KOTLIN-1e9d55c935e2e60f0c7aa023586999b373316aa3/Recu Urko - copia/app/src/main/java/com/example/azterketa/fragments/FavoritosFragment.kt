package com.example.azterketa.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.azterketa.databinding.FragmentFavoritosBinding
import com.example.azterketa.viewmodels.MainViewModel
import com.example.azterketa.views.adapters.PersonajeAdapter

class FavoritosFragment : Fragment() {

    private var _binding: FragmentFavoritosBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter: PersonajeAdapter

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

        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        binding.rvFavoritos.layoutManager = LinearLayoutManager(requireContext())
        adapter = PersonajeAdapter(requireContext()) { personaje ->
            viewModel.toggleFavorito(personaje)
        }
        binding.rvFavoritos.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.favoritos.observe(viewLifecycleOwner) { favoritos ->
            adapter.submitList(favoritos)
            binding.tvNoFavoritos.visibility =
                if (favoritos.isEmpty()) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}