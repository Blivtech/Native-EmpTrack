package com.blivtech.emptrack.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAboutBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.ivBack.setOnClickListener { finish() }
        // Optional: make website/email/phone tappable — wire your own intents here.
    }
}
