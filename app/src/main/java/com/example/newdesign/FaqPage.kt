package com.example.newdesign

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.newdesign.databinding.ActivityFaqPageBinding

class FaqPage : AppCompatActivity() {
    private lateinit var binding: ActivityFaqPageBinding
    private lateinit var faqAdapter: FaqAdapter

    // private var startTime: Long = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFaqPageBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        //startTime = System.currentTimeMillis()
        val faqQuestionsArray = resources.getStringArray(R.array.faq_questions)
        val faqAnswersArray = resources.getStringArray(R.array.faq_answers)

        val faqList = faqQuestionsArray.indices.map { index ->
            FaqDataClass(faqQuestionsArray[index], faqAnswersArray[index])
        }

        faqAdapter = FaqAdapter()
        binding.rvFaq.adapter = faqAdapter
        faqAdapter.submitFaqList(faqList)

//        val snapHelper = LinearSnapHelper()
//        snapHelper.attachToRecyclerView(binding.rvFaq)
        //displayReloadTime()
    }

//    private fun displayReloadTime() {
//        val endTime = System.currentTimeMillis()
//        val reloadTime = endTime - startTime
//        // Assuming you have a TextView in your layout to display the reload time
//        // For example, let's say it's called reloadTimeTextView
//        Toast.makeText(this, "Reload Time: $reloadTime ms", Toast.LENGTH_LONG).show()
//    }

//    personAdapter= PersonAdapter()
//    binding.homeRecyclerView.apply {
//        layoutManager= StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL)
//        setHasFixedSize(true)
//        adapter=personAdapter
//    }

}