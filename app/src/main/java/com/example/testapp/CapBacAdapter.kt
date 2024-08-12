package com.example.testapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CapBacAdapter : RecyclerView.Adapter<CapBacAdapter.CapBacViewHolder>() {

    private var items: List<DanhMucCapBac> = emptyList()

    fun setItems(items: List<DanhMucCapBac>) {
        this.items = items
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CapBacViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return CapBacViewHolder(view)
    }

    override fun onBindViewHolder(holder: CapBacViewHolder, position: Int) {
        val item = items[position]
        holder.textView1.text = item.name
        holder.textView2.text = item.ordering.toString()
    }

    override fun getItemCount(): Int = items.size

    class CapBacViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textView1: TextView = view.findViewById(android.R.id.text1)
        val textView2: TextView = view.findViewById(android.R.id.text2)
    }
}
