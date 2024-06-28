package com.example.newdesign

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.newdesign.databinding.ActivitySearchBinding
import com.example.newdesign.databinding.ItemLayoutBinding
import com.example.newdesign.databinding.SearchLayoutBinding

class SearchAdapter : RecyclerView.Adapter<SearchAdapter.SearchViewHolder>() {
    inner class SearchViewHolder(val binding: SearchLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemText: DataClassItem) {
            binding.apply {
                tvItem.text = itemText.email
            }
        }
    }

    private val differCallBack = object : DiffUtil.ItemCallback<DataClassItem>() {
        override fun areItemsTheSame(oldItem: DataClassItem, newItem: DataClassItem): Boolean {
            return oldItem.id == newItem.id && oldItem.email == newItem.email
        }

        override fun areContentsTheSame(
            oldItem: DataClassItem,
            newItem: DataClassItem
        ): Boolean {
            return oldItem == newItem
        }

    }
    val differ = AsyncListDiffer(this, differCallBack)

    fun submitSearchClass(itemList: List<DataClassItem>) {
        return differ.submitList(itemList)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SearchAdapter.SearchViewHolder {
        val layoutInflate = LayoutInflater.from(parent.context)
        val binding = SearchLayoutBinding.inflate(layoutInflate, parent, false)
        return SearchViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SearchAdapter.SearchViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}

