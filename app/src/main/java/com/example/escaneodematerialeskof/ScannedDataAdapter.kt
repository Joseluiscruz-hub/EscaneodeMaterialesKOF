package com.example.escaneodematerialeskof

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.escaneodematerialeskof.model.Pallet
import com.example.escaneodematerialeskof.model.Rumba

class ScannedDataAdapter(private var data: List<Any>) : RecyclerView.Adapter<ScannedDataAdapter.ItemViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_scanned_data, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: ItemViewHolder, position: Int) {
        val item = data[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = data.size

    fun setData(newData: List<Any>) {
        this.data = newData
        notifyDataSetChanged()
    }

    fun getData(): List<Any> {
        return data
    }

    class ItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val primaryTextView: TextView = itemView.findViewById(R.id.textView_primary)
        private val secondaryTextView: TextView = itemView.findViewById(R.id.textView_secondary)
        private val quantityTextView: TextView = itemView.findViewById(R.id.textView_quantity)
        private val icon: ImageView = itemView.findViewById(R.id.icon_item)

        fun bind(item: Any) {
            when (item) {
                is Rumba -> {
                    primaryTextView.text = "Material: ${item.material}"
                    secondaryTextView.text = "Lote: ${item.lote}"
                    quantityTextView.text = item.cantidad.toString()
                    icon.setImageResource(android.R.drawable.ic_dialog_dialer)
                }
                is Pallet -> {
                    primaryTextView.text = "HU: ${item.hu}"
                    secondaryTextView.visibility = View.GONE
                    quantityTextView.text = item.cantidad.toString()
                    icon.setImageResource(android.R.drawable.ic_input_get)
                }
            }
        }
    }
}
