package com.example.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView

class CapBacAdapter(
    private val items: List<DanhMucCapBac>,
    private val onEditClick: (DanhMucCapBac) -> Unit,
    private val onDeleteClick: (DanhMucCapBac) -> Unit
) : RecyclerView.Adapter<CapBacAdapter.CapBacViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapBacViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cap_bac, parent, false)
        return CapBacViewHolder(view)
    }

    override fun onBindViewHolder(holder: CapBacViewHolder, position: Int) {
        val item = items[position]
        holder.bind(item)
        holder.editButton.setOnClickListener { onEditClick(item) }
        holder.deleteButton.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = items.size

    inner class CapBacViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idTextView: TextView = itemView.findViewById(R.id.tvId)
        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val orderingTextView: TextView = itemView.findViewById(R.id.tvOrdering)
        val editButton: AppCompatButton = itemView.findViewById(R.id.btnEdit)
        val deleteButton: AppCompatButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: DanhMucCapBac) {
            idTextView.text = item.id
            nameTextView.text = item.name
            orderingTextView.text = item.ordering.toString()
        }
    }
}
