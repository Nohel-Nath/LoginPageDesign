package com.example.newdesign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.newdesign.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var includeItem: View
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        includeItem=findViewById<View>(R.id.faq)
        binding.btnCreateNewBo.setOnClickListener {
            startActivity(Intent(this, PersonalInformation::class.java))
        }
        includeItem.setOnClickListener {
            startActivity(Intent(this, FaqPage::class.java))
        }
    }
}