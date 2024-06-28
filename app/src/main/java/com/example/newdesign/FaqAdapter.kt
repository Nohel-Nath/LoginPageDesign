package com.example.newdesign

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.newdesign.databinding.ItemLayoutBinding

class FaqAdapter : RecyclerView.Adapter<FaqAdapter.FaqViewHolder>() {
    inner class FaqViewHolder(val binding: ItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FaqDataClass) {
            binding.apply {
                tvQuestions.text = item.questions
                tvAnswers.text = item.answers
            }
        }
    }

    private val differCallback = object : DiffUtil.ItemCallback<FaqDataClass>() {
        override fun areItemsTheSame(oldItem: FaqDataClass, newItem: FaqDataClass): Boolean {
            return oldItem.questions == newItem.questions &&
                    oldItem.answers == newItem.answers
        }

        override fun areContentsTheSame(oldItem: FaqDataClass, newItem: FaqDataClass): Boolean {
            return oldItem == newItem
        }
    }
    val differ = AsyncListDiffer(this, differCallback)

    fun submitFaqList(faqList: List<FaqDataClass>) {
        return differ.submitList(faqList)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FaqViewHolder {
        val layoutInflate = LayoutInflater.from(parent.context)
        val binding = ItemLayoutBinding.inflate(layoutInflate, parent, false)
        return FaqViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }

    override fun onBindViewHolder(holder: FaqViewHolder, position: Int) {
        val item = differ.currentList[position]
        holder.bind(item)
    }
}


//    inner class FaqViewHolder(val itemBinding:ItemLayoutBinding):RecyclerView.ViewHolder(itemBinding.root)
//    private val differCallBack=object: DiffUtil.ItemCallback<FaqDataClass>(){
//        override fun areItemsTheSame(oldItem: FaqDataClass, newItem: FaqDataClass): Boolean {
//            return oldItem.questions == newItem.questions &&
//                    oldItem.answers ==  newItem.answers
//        }
//
//        override fun areContentsTheSame(oldItem: FaqDataClass, newItem: FaqDataClass): Boolean {
//            return oldItem == newItem
//        }
//
//    }
//
//    val differ= AsyncListDiffer(this, differCallBack)


