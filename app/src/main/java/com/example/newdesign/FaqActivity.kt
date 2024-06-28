package com.example.newdesign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.newdesign.databinding.ActivityFaqBinding

class FaqActivity : AppCompatActivity() {
    private lateinit var binding:ActivityFaqBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityFaqBinding.inflate(layoutInflater)
        setContentView(binding.root)
//        binding.faq.setOnClickListener {
//            val intent = Intent(this, FaqPage::class.java)
//            startActivity(intent)
//        }
    }
}