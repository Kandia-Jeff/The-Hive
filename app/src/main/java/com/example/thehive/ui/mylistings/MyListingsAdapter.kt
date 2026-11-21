package com.example.thehive.ui.mylistings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.thehive.data.local.entity.ListingEntity
import com.example.thehive.databinding.ItemMyListingBinding

class MyListingsAdapter(
    private val onEdit: (ListingEntity) -> Unit,
    private val onDelete: (ListingEntity) -> Unit,
    private val onToggleAvailability: (ListingEntity) -> Unit
) : ListAdapter<ListingEntity, MyListingsAdapter.MyListingViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyListingViewHolder {
        val binding = ItemMyListingBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MyListingViewHolder(binding, onEdit, onDelete, onToggleAvailability)
    }

    override fun onBindViewHolder(holder: MyListingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MyListingViewHolder(
        private val binding: ItemMyListingBinding,
        private val onEdit: (ListingEntity) -> Unit,
        private val onDelete: (ListingEntity) -> Unit,
        private val onToggleAvailability: (ListingEntity) -> Unit
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(listing: ListingEntity) {
            binding.tvTitle.text = listing.title
            binding.tvLocation.text = listing.location
            binding.tvRent.text = "Ksh %,.0f / mo".format(listing.rent)
            binding.tvBedrooms.text = if (listing.bedrooms == 0) "Studio" else "${listing.bedrooms} Beds"

            // Detach the listener before setting the state, otherwise recycling a row
            // would look like the landlord flipped the switch.
            binding.switchAvailable.setOnCheckedChangeListener(null)
            binding.switchAvailable.isChecked = listing.isAvailable
            binding.switchAvailable.text = if (listing.isAvailable) "Available" else "Taken"
            binding.switchAvailable.setOnCheckedChangeListener { _, _ ->
                onToggleAvailability(listing)
            }

            binding.btnEdit.setOnClickListener { onEdit(listing) }
            binding.btnDelete.setOnClickListener { onDelete(listing) }
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