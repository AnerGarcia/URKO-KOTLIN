
package com.example.azterketa.views.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.azterketa.R
import com.example.azterketa.database.PersonajeEntity
import com.example.azterketa.databinding.ItemRvPersonajeBinding
import com.google.android.material.bottomsheet.BottomSheetDialog

class PersonajeAdapter(
    private val context: Context,
    private val onFavoritoClick: (PersonajeEntity) -> Unit
) : ListAdapter<PersonajeEntity, PersonajeAdapter.ViewHolder>(PersonajeDiffCallback()) {

    class ViewHolder(private val binding: ItemRvPersonajeBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(personaje: PersonajeEntity, onFavoritoClick: (PersonajeEntity) -> Unit) {
            binding.tvNomPersonaje.text = personaje.personaje

            Glide.with(binding.root.context)
                .load(personaje.imagen)
                .centerInside()
                .into(binding.ivPersonaje)

            binding.ivFavorito.setImageResource(
                if (personaje.esFavorito) R.drawable.ic_favorite_filled
                else R.drawable.ic_favorite_border
            )

            binding.cvPersonaje.setOnClickListener {
                mostrarFrase(personaje.frase)
            }

            binding.ivFavorito.setOnClickListener {
                onFavoritoClick(personaje)
            }
        }

        private fun mostrarFrase(frase: String) {
            val bottomSheetDialog = BottomSheetDialog(binding.root.context)
            bottomSheetDialog.setContentView(R.layout.bottom_sheet_dialog_frase)

            val tvFrase = bottomSheetDialog.findViewById<android.widget.TextView>(R.id.tvFrase)
            tvFrase?.text = frase
            bottomSheetDialog.show()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemRvPersonajeBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), onFavoritoClick)
    }
}

class PersonajeDiffCallback : DiffUtil.ItemCallback<PersonajeEntity>() {
    override fun areItemsTheSame(oldItem: PersonajeEntity, newItem: PersonajeEntity): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: PersonajeEntity, newItem: PersonajeEntity): Boolean {
        return oldItem == newItem
    }
}