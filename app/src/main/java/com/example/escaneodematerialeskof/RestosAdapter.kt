package com.example.escaneodematerialeskof

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.escaneodematerialeskof.model.Restos

class RestosAdapter : ListAdapter<Restos, RestosAdapter.RestoViewHolder>(RestosDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RestoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_resto, parent, false)
        return RestoViewHolder(view)
    }

    override fun onBindViewHolder(holder: RestoViewHolder, position: Int) {
        val resto = getItem(position)
        holder.bind(resto)
    }

    class RestoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val materialTextView: TextView = itemView.findViewById(R.id.textView_material)
        private val loteTextView: TextView = itemView.findViewById(R.id.textView_lote)
        private val infoTextView: TextView = itemView.findViewById(R.id.textView_info)
        private val iconMaterial: ImageView = itemView.findViewById(R.id.icon_material)

        fun bind(resto: Restos) {
            materialTextView.text = "Material: ${resto.material}"
            loteTextView.text = "Lote: ${resto.lote}"
            infoTextView.text = "Cant: ${resto.cantidad}  |  ${resto.fecha} ${resto.hora}"
            // El icono se establece estáticamente en el XML por ahora
        }
    }
}

class RestosDiffCallback : DiffUtil.ItemCallback<Restos>() {
    override fun areItemsTheSame(oldItem: Restos, newItem: Restos): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Restos, newItem: Restos): Boolean {
        return oldItem == newItem
    }
}
