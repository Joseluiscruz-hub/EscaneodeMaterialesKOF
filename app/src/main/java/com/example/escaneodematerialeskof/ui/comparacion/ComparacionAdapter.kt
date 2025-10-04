package com.example.escaneodematerialeskof.ui.comparacion

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.escaneodematerialeskof.R
import com.example.escaneodematerialeskof.databinding.ItemComparacionBinding
import com.example.escaneodematerialeskof.dashboard.ComparacionInventario

/**
 * Adapter para mostrar la comparación de inventario en tiempo real.
 */
class ComparacionAdapter : ListAdapter<ComparacionInventario, ComparacionAdapter.ComparacionViewHolder>(ComparacionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ComparacionViewHolder {
        val binding = ItemComparacionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ComparacionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ComparacionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ComparacionViewHolder(private val binding: ItemComparacionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ComparacionInventario) {
            binding.apply {
                textSku.text = item.sku
                textDescripcion.text = item.descripcion.takeIf { it.isNotEmpty() } ?: "Sin descripción"
                textTipoTarima.text = item.tipoTarima.takeIf { it.isNotEmpty() } ?: "N/A"

                // Mostrar cantidades
                textEscaneado.text = "Escaneado: ${item.escaneado ?: 0}"
                textInventario.text = "Sistema: ${item.inventario ?: 0}"

                // Mostrar diferencia y estado
                val diferencia = item.diferencia ?: 0
                textDiferencia.text = when {
                    diferencia > 0 -> "+$diferencia"
                    diferencia < 0 -> "$diferencia"
                    else -> "0"
                }
                textEstado.text = item.estado

                // Aplicar colores según el estado
                val context = binding.root.context
                when {
                    item.estado == "OK" -> {
                        // Verde para coincidencias
                        binding.cardContainer.setCardBackgroundColor(
                            ContextCompat.getColor(context, android.R.color.holo_green_light)
                        )
                        textEstado.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    }
                    item.estado.contains("Faltante") -> {
                        // Rojo para faltantes
                        binding.cardContainer.setCardBackgroundColor(
                            ContextCompat.getColor(context, android.R.color.holo_red_light)
                        )
                        textEstado.setTextColor(ContextCompat.getColor(context, android.R.color.white))
                    }
                    item.estado.contains("Sobrante") -> {
                        // Amarillo para sobrantes
                        binding.cardContainer.setCardBackgroundColor(
                            ContextCompat.getColor(context, android.R.color.holo_orange_light)
                        )
                        textEstado.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    }
                    else -> {
                        // Color neutro para otros estados
                        binding.cardContainer.setCardBackgroundColor(
                            ContextCompat.getColor(context, android.R.color.background_light)
                        )
                        textEstado.setTextColor(ContextCompat.getColor(context, android.R.color.black))
                    }
                }

                // Añadir indicador visual para diferencias significativas
                val indicadorDiferencia = when {
                    diferencia > 0 -> "📈" // Sobrante
                    diferencia < 0 -> "📉" // Faltante
                    else -> "✅" // Coincide
                }
                textDiferencia.text = "$indicadorDiferencia ${textDiferencia.text}"
            }
        }
    }

    class ComparacionDiffCallback : DiffUtil.ItemCallback<ComparacionInventario>() {
        override fun areItemsTheSame(oldItem: ComparacionInventario, newItem: ComparacionInventario): Boolean {
            return oldItem.sku == newItem.sku
        }

        override fun areContentsTheSame(oldItem: ComparacionInventario, newItem: ComparacionInventario): Boolean {
            return oldItem == newItem
        }
    }

    /**
     * Actualiza la lista con animaciones
     */
    fun actualizarLista(nuevaLista: List<ComparacionInventario>) {
        submitList(nuevaLista.toMutableList()) // Crear nueva instancia para forzar la actualización
    }

    /**
     * Limpia la lista
     */
    fun limpiarLista() {
        submitList(emptyList())
    }

    /**
     * Obtiene el número de elementos por estado
     */
    fun getEstadisticas(): Triple<Int, Int, Int> {
        val lista = currentList
        val coincidencias = lista.count { it.estado == "OK" }
        val faltantes = lista.count { it.estado.contains("Faltante") }
        val sobrantes = lista.count { it.estado.contains("Sobrante") }
        return Triple(coincidencias, faltantes, sobrantes)
    }
}
