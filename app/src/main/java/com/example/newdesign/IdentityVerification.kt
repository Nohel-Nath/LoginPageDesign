package com.example.newdesign

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.newdesign.databinding.ActivityIdentityVerificationBinding

class IdentityVerification : AppCompatActivity() {
    private lateinit var binding: ActivityIdentityVerificationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityIdentityVerificationBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.viewForNid.setOnClickListener {
            val intent = Intent(this, NIDVerification::class.java)
            startActivity(intent)
        }
    }
}