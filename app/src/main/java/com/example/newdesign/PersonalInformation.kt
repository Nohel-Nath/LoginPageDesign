package com.example.newdesign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.newdesign.databinding.ActivityPersonalInformationBinding

class PersonalInformation : AppCompatActivity(), OnItemClickListener {
    private lateinit var binding: ActivityPersonalInformationBinding
    private lateinit var boTextAdapter: OpenBoAccountAdapter
    private lateinit var includeItem: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPersonalInformationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setUpRecycleView()
        includeItem=findViewById<View>(R.id.tv_faq)
        includeItem.setOnClickListener {
            startActivity(Intent(this, FaqPage::class.java))
        }
    }

    private fun setUpRecycleView() {
        val boAccountArray = resources.getStringArray(R.array.bo_account_text)
        val boTextList = boAccountArray.indices.map { index ->
            OpenBoAccount(boAccountArray[index])
        }
        boTextAdapter = OpenBoAccountAdapter(this)
        binding.rvOpenBoAccount.adapter = boTextAdapter
        boTextAdapter.submitTextList(boTextList)
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(binding.rvOpenBoAccount)

    }

    override fun onItemClick(item: OpenBoAccount) {
        when (item.boText) {
            "Personal Information" -> startActivity(Intent(this, IdentityVerification::class.java))
        }
    }

}