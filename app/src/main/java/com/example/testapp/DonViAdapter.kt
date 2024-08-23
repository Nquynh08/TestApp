package com.example.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.widget.AppCompatImageButton
import androidx.recyclerview.widget.RecyclerView
import android.widget.Filter
import android.widget.Filterable

class DonViAdapter(
    private val items: List<DanhMucDonVi>,
    private val onEditClick: (DanhMucDonVi) -> Unit,
    private val onDeleteClick: (DanhMucDonVi) -> Unit
) : RecyclerView.Adapter<DonViAdapter.DonViViewHolder>(), Filterable {

    private var filteredItems: List<DanhMucDonVi> = items

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DonViViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_don_vi, parent, false)
        return DonViViewHolder(view)
    }

    override fun onBindViewHolder(holder: DonViViewHolder, position: Int) {
        val item = filteredItems[position]
        holder.bind(item)
        holder.editButton.setOnClickListener { onEditClick(item) }
        holder.deleteButton.setOnClickListener { onDeleteClick(item) }
    }

    override fun getItemCount() = filteredItems.size

    inner class DonViViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val idTextView: TextView = itemView.findViewById(R.id.tvId)
        private val nameTextView: TextView = itemView.findViewById(R.id.tvName)
        private val pIDTextView: TextView = itemView.findViewById(R.id.tvpID)
        val editButton: AppCompatImageButton = itemView.findViewById(R.id.btnEdit)
        val deleteButton: AppCompatImageButton = itemView.findViewById(R.id.btnDelete)

        fun bind(item: DanhMucDonVi) {
            idTextView.text = item.id
            nameTextView.text = item.name
            pIDTextView.text = item.pId.toString()
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
                                it.pId.toString().contains(query)
                    }
                }

                val filterResults = FilterResults()
                filterResults.values = filteredList
                return filterResults
            }

            @Suppress("UNCHECKED_CAST")
            override fun publishResults(charSequence: CharSequence?, filterResults: FilterResults?) {
                filteredItems = filterResults?.values as List<DanhMucDonVi>
                notifyDataSetChanged()
            }
        }
    }
}
