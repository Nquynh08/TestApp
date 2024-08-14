package com.example.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import android.widget.Filter
import android.widget.Filterable

class CapBacAdapter(
    private val items: List<DanhMucCapBac>,
    private val onEditClick: (DanhMucCapBac) -> Unit,
    private val onDeleteClick: (DanhMucCapBac) -> Unit
) : RecyclerView.Adapter<CapBacAdapter.CapBacViewHolder>(), Filterable {

    private var filteredItems: List<DanhMucCapBac> = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapBacViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_cap_bac, parent, false)
        return CapBacViewHolder(view)
    }

    override fun onBindViewHolder(holder: CapBacViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.bind(item)
        holder.editButton.setOnClickListener { onEditClick(item) }
        holder.deleteButton.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = filteredItems.size

    inner class CapBacViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idTextView: TextView = itemView.findViewById(R.id.tvId)
        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val orderingTextView: TextView = itemView.findViewById(R.id.tvOrdering)
        val editButton: AppCompatImageButton = itemView.findViewById(R.id.btnEdit)
        val deleteButton: AppCompatImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: DanhMucCapBac) {
            idTextView.text = item.id
            nameTextView.text = item.name
            orderingTextView.text = item.ordering.toString()
        }
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence?): FilterResults {
                val query = charSequence?.toString()?.lowercase()

                val filteredList = if (query.isNullOrEmpty()) {
                    items
                } else {
                    items.filter {
                        it.name.lowercase().contains(query) ||
                                it.id.lowercase().contains(query) ||
                                it.ordering.toString().contains(query)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                filteredItems = filterResults?.values as List<DanhMucCapBac>
                notifyDataSetChanged()
            }
        }
    }
}