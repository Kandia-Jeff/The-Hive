package com.example.thehive.ui.favourites

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.databinding.ItemFavouriteBinding

class FavouritesAdapter(
    private val onListingClick: (ListingEntity) -> Unit,
    private val onRemoveClick: (ListingEntity) -> Unit
) : ListAdapter<ListingEntity, FavouritesAdapter.FavouriteViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavouriteViewHolder {
        val binding = ItemFavouriteBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return FavouriteViewHolder(binding, onListingClick, onRemoveClick)
    }

    override fun onBindViewHolder(holder: FavouriteViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class FavouriteViewHolder(
        private val binding: ItemFavouriteBinding,
        private val onListingClick: (ListingEntity) -> Unit,
        private val onRemoveClick: (ListingEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listing: ListingEntity) {
            binding.tvTitle.text = listing.title
            binding.tvLocation.text = listing.location
            binding.tvRent.text = "Ksh %,.0f / mo".format(listing.rent)
            binding.tvBedrooms.text = if (listing.bedrooms == 0) "Studio" else "${listing.bedrooms} Beds"

            // A saved home that has since been taken gets a note on its card.
            binding.tvTakenNote.visibility = if (listing.isAvailable) View.GONE else View.VISIBLE

            binding.root.setOnClickListener { onListingClick(listing) }
            binding.btnRemove.setOnClickListener { onRemoveClick(listing) }
        }
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ListingEntity>() {
        override fun areItemsTheSame(oldItem: ListingEntity, newItem: ListingEntity): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ListingEntity, newItem: ListingEntity): Boolean {
            return oldItem == newItem
        }
    }
}
