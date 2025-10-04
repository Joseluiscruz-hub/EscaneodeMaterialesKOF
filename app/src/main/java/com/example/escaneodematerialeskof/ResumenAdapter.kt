package com.example.escaneodematerialeskof

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import com.example.escaneodematerialeskof.model.Pallet
import com.example.escaneodematerialeskof.model.Rumba

class ResumenAdapter : ListAdapter<Any, ScannedDataAdapter.ItemViewHolder>(ResumenDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ScannedDataAdapter.ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scanned_data, parent, false)
        return ScannedDataAdapter.ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ScannedDataAdapter.ItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class ResumenDiffCallback : DiffUtil.ItemCallback<Any>() {
    override fun areItemsTheSame(oldItem: Any, newItem: Any): Boolean {
        return when {
            oldItem is Rumba && newItem is Rumba -> oldItem.id == newItem.id
            oldItem is Pallet && newItem is Pallet -> oldItem.id == newItem.id
            else -> false
        }
    }

    override fun areContentsTheSame(oldItem: Any, newItem: Any): Boolean {
        return oldItem == newItem
    }
}
