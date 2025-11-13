package com.example.practicasegundoparcial.adapters

import android.app.AlertDialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.practicasegundoparcial.R
import com.example.practicasegundoparcial.models.Alumno

class AlumnoAdapter(
    private var lista: List<Alumno>,
    private val eliminarCallback: (Alumno) -> Unit
) : RecyclerView.Adapter<AlumnoAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvNombreCompleto: TextView = view.findViewById(R.id.tvNombreCompleto)
        val tvCi: TextView = view.findViewById(R.id.tvCi)
        val tvFecha: TextView = view.findViewById(R.id.tvFecha)
        val tvLatLng: TextView = view.findViewById(R.id.tvLatLng)
        val btnEliminar: Button = view.findViewById(R.id.btnEliminar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val vista = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_alumno, parent, false)
        return ViewHolder(vista)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val alumno = lista[position]

        holder.tvNombreCompleto.text = "${alumno.nombres} ${alumno.apellidos}"
        holder.tvCi.text = "CI: ${alumno.ci}"
        holder.tvFecha.text = "Nacimiento: ${alumno.fechaNacimiento}"
        holder.tvLatLng.text = "(${alumno.latitud}, ${alumno.longitud})"

        holder.btnEliminar.setOnClickListener {
            AlertDialog.Builder(holder.itemView.context)
                .setTitle("¿Estás seguro?")
                .setMessage("¿Desea eliminar este alumno?")
                .setPositiveButton("Sí") { _, _ -> eliminarCallback(alumno) }
                .setNegativeButton("No", null)
                .show()
        }
    }

    override fun getItemCount(): Int = lista.size

    fun actualizar(nuevaLista: List<Alumno>) {
        lista = nuevaLista
        notifyDataSetChanged()
    }
}
