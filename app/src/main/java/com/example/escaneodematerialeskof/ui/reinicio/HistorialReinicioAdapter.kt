package com.example.escaneodematerialeskof.ui.reinicio

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.escaneodematerialeskof.R

class HistorialReinicioAdapter : RecyclerView.Adapter<HistorialReinicioAdapter.HistorialViewHolder>() {

    private var historialList = mutableListOf<HistorialReinicioItem>()

    fun updateHistorial(nuevoHistorial: List<HistorialReinicioItem>) {
        historialList.clear()
        historialList.addAll(nuevoHistorial)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistorialViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_historial_reinicio, parent, false)
        return HistorialViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistorialViewHolder, position: Int) {
        val item = historialList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = historialList.size

    class HistorialViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTipoReinicio: TextView = itemView.findViewById(R.id.tvTipoReinicio)
        private val tvTituloReinicio: TextView = itemView.findViewById(R.id.tvTituloReinicio)
        private val tvFechaReinicio: TextView = itemView.findViewById(R.id.tvFechaReinicio)
        private val tvUsuarioReinicio: TextView = itemView.findViewById(R.id.tvUsuarioReinicio)
        private val tvEstadoReinicio: TextView = itemView.findViewById(R.id.tvEstadoReinicio)

        fun bind(item: HistorialReinicioItem) {
            itemView.findViewById<TextView>(R.id.tvTipoReinicio).text = item.tipo
            itemView.findViewById<TextView>(R.id.tvTituloReinicio).text = item.titulo
            tvFechaReinicio.text = item.fecha
            tvUsuarioReinicio.text = "Usuario: ${item.usuario}"

            if (item.exito) {
                itemView.findViewById<TextView>(R.id.tvEstadoReinicio).text = "Exitoso"
                tvEstadoReinicio.setBackgroundResource(R.drawable.rounded_background_green)
            } else {
                itemView.findViewById<TextView>(R.id.tvEstadoReinicio).text = "Error"
                tvEstadoReinicio.setBackgroundResource(R.drawable.rounded_background_red)
            }
        }
    }
}
