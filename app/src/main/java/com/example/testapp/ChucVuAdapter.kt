package com.example.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import android.widget.Filter
import android.widget.Filterable

class ChucVuAdapter(
    private val items: List<DanhMucChucVu>,
    private val onEditClick: (DanhMucChucVu) -> Unit,
    private val onDeleteClick: (DanhMucChucVu) -> Unit
) : RecyclerView.Adapter<ChucVuAdapter.ChucVuViewHolder>(), Filterable {

    private var filteredItems: List<DanhMucChucVu> = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChucVuViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_chuc_vu, parent, false)
        return ChucVuViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChucVuViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.bind(item)
        holder.editButton.setOnClickListener { onEditClick(item) }
        holder.deleteButton.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = filteredItems.size

    inner class ChucVuViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idTextView: TextView = itemView.findViewById(R.id.tvId)
        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val orderingTextView: TextView = itemView.findViewById(R.id.tvOrdering)
        val editButton: AppCompatImageButton = itemView.findViewById(R.id.btnEdit)
        val deleteButton: AppCompatImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: DanhMucChucVu) {
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
                filteredItems = filterResults?.values as List<DanhMucChucVu>
                notifyDataSetChanged()
            }
        }
    }
}
