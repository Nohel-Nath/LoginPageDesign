package com.example.newdesign


import android.view.LayoutInflater

import android.view.ViewGroup

import androidx.constraintlayout.widget.ConstraintLayout

import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.newdesign.databinding.BoAccountLayoutBinding

class OpenBoAccountAdapter(private val listener: OnItemClickListener) :
    RecyclerView.Adapter<OpenBoAccountAdapter.OpenBoAccountViewHolder>() {
    private val selectedPositions = mutableSetOf<Int>()

    inner class OpenBoAccountViewHolder(val binding: BoAccountLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val constraintLayoutCLick: ConstraintLayout = binding.constraint

        init {
            constraintLayoutCLick.setOnClickListener {
                val position = adapterPosition
                val item = differ.currentList[position]
                if (selectedPositions.contains(position)) {
                    selectedPositions.remove(position)
                } else {
                    selectedPositions.add(position)
                }
                notifyItemChanged(position)
                listener.onItemClick(item)
            }
        }

        fun bind(item: OpenBoAccount, position: Int) {
            binding.apply {
                tvQuestions.text = item.boText
                if (selectedPositions.contains(position)) {
                    tvQuestions.setBackgroundResource(R.drawable.textview_drawable_white)
                    tvQuestions
                    tvQuestions.alpha = 1f
                } else {
                    tvQuestions.setBackgroundResource(R.drawable.textview_drawable)
                    tvQuestions.alpha = 0.5f

                }
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<OpenBoAccount>() {
        override fun areItemsTheSame(oldItem: OpenBoAccount, newItem: OpenBoAccount): Boolean {
            return oldItem.boText == newItem.boText
        }

        override fun areContentsTheSame(oldItem: OpenBoAccount, newItem: OpenBoAccount): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)

    fun submitTextList(textList: List<OpenBoAccount>) {
        return differ.submitList(textList)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OpenBoAccountViewHolder {
        val layoutInflate = LayoutInflater.from(parent.context)
        val binding = BoAccountLayoutBinding.inflate(layoutInflate, parent, false)
        return OpenBoAccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OpenBoAccountViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.bind(item, position)

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}