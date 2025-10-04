package com.example.escaneodematerialeskof.ui.comparacion

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.escaneodematerialeskof.R
import java.text.SimpleDateFormat
import java.util.*

/**
 * Adaptador para mostrar las diferencias encontradas en el inventario
 */
class DiferenciasAdapter(
    private var diferencias: MutableList<DiferenciaInventario> = mutableListOf()
) : RecyclerView.Adapter<DiferenciasAdapter.DiferenciaViewHolder>() {

    class DiferenciaViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvCodigo: TextView = itemView.findViewById(R.id.tv_codigo)
        val tvDescripcion: TextView = itemView.findViewById(R.id.tv_descripcion)
        val tvCantidadReferencia: TextView = itemView.findViewById(R.id.tv_cantidad_referencia)
        val tvCantidadActual: TextView = itemView.findViewById(R.id.tv_cantidad_actual)
        val tvDiferencia: TextView = itemView.findViewById(R.id.tv_diferencia)
        val tvTimestamp: TextView = itemView.findViewById(R.id.tv_timestamp)
        val viewIndicadorEstado: View = itemView.findViewById(R.id.view_indicador_estado)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiferenciaViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_diferencia_inventario, parent, false)
        return DiferenciaViewHolder(view)
    }

    override fun onBindViewHolder(holder: DiferenciaViewHolder, position: Int) {
        val diferencia = diferencias[position]
        val context = holder.itemView.context

        // Datos básicos
        holder.tvCodigo.text = diferencia.codigo
        holder.tvDescripcion.text = diferencia.descripcion
        holder.tvCantidadReferencia.text = diferencia.cantidadReferencia.toString()
        holder.tvCantidadActual.text = diferencia.cantidadActual.toString()

        // Formatear diferencia con signo
        val diferenciaTexto = when {
            diferencia.diferencia > 0 -> "+${diferencia.diferencia}"
            diferencia.diferencia < 0 -> diferencia.diferencia.toString()
            else -> "0"
        }
        holder.tvDiferencia.text = diferenciaTexto

        // Timestamp
        val timeFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
        holder.tvTimestamp.text = timeFormat.format(Date(diferencia.timestamp))

        // Colores según el estado
        when (diferencia.estado) {
            EstadoDiferencia.EXCESO -> {
                holder.tvDiferencia.setTextColor(ContextCompat.getColor(context, R.color.warning_dark))
                holder.viewIndicadorEstado.setBackgroundColor(ContextCompat.getColor(context, R.color.warning_dark))
            }
            EstadoDiferencia.FALTANTE -> {
                holder.tvDiferencia.setTextColor(ContextCompat.getColor(context, R.color.error_dark))
                holder.viewIndicadorEstado.setBackgroundColor(ContextCompat.getColor(context, R.color.error_dark))
            }
            EstadoDiferencia.COINCIDE -> {
                holder.tvDiferencia.setTextColor(ContextCompat.getColor(context, R.color.success_dark))
                holder.viewIndicadorEstado.setBackgroundColor(ContextCompat.getColor(context, R.color.success_dark))
            }
        }
    }

    override fun getItemCount(): Int = diferencias.size

    /**
     * Actualiza la lista de diferencias y notifica al RecyclerView
     */
    fun actualizarDiferencias(nuevasDiferencias: List<DiferenciaInventario>) {
        diferencias.clear()
        diferencias.addAll(nuevasDiferencias)
        notifyDataSetChanged()
    }

    /**
     * Agrega una nueva diferencia a la lista
     */
    fun agregarDiferencia(diferencia: DiferenciaInventario) {
        diferencias.add(0, diferencia) // Agregar al inicio para mostrar las más recientes primero
        notifyItemInserted(0)
    }

    /**
     * Limpia todas las diferencias
     */
    fun limpiarDiferencias() {
        diferencias.clear()
        notifyDataSetChanged()
    }
}
