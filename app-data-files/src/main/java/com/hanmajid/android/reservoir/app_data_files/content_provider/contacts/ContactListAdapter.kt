package com.hanmajid.android.reservoir.app_data_files.content_provider.contacts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.hanmajid.android.reservoir.app_data_files.databinding.ItemContactBinding

class ContactListAdapter :
    ListAdapter<MyContact, RecyclerView.ViewHolder>(DIFF_CALLBACK) {

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MyContact>() {
            override fun areItemsTheSame(
                oldItem: MyContact,
                newItem: MyContact
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: MyContact,
                newItem: MyContact
            ): Boolean {
                return oldItem == newItem
            }
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ContactViewHolder).bind(getItem(position))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ContactViewHolder.create(
            parent
        )
    }

    class ContactViewHolder(val binding: ItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(contact: MyContact?) {
            contact?.apply {
                binding.contact = this
            }
        }

        companion object {
            @Suppress("unused")
            private const val TAG = "ContactListAdapter"
            fun create(parent: ViewGroup): ContactViewHolder {
                val binding = ItemContactBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                return ContactViewHolder(
                    binding
                )
            }
        }
    }
}