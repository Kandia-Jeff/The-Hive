package com.example.thehive.ui.browse

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.databinding.ItemListingBinding

class ListingsAdapter(
    private val onListingClick: (ListingEntity) -> Unit,
    private val onFavouriteClick: (ListingEntity) -> Unit
) : ListAdapter<ListingEntity, ListingsAdapter.ListingViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListingViewHolder {
        val binding = ItemListingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ListingViewHolder(binding, onListingClick, onFavouriteClick)
    }

    override fun onBindViewHolder(holder: ListingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ListingViewHolder(
        private val binding: ItemListingBinding,
        private val onListingClick: (ListingEntity) -> Unit,
        private val onFavouriteClick: (ListingEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listing: ListingEntity) {
            binding.tvTitle.text = listing.title
            binding.tvLocation.text = listing.location
            binding.tvRent.text = "Ksh %,.0f / mo".format(listing.rent)
            binding.tvBedrooms.text = "${listing.bedrooms} Beds"
            binding.chipBadge.text = if (listing.isAvailable) "Available" else "Taken"

            binding.root.setOnClickListener { onListingClick(listing) }
            binding.ivFavourite.setOnClickListener { onFavouriteClick(listing) }
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
