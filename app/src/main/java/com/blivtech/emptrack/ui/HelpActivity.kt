package com.blivtech.emptrack.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.blivtech.emptrack.R
import com.blivtech.emptrack.databinding.ActivityHelpBinding

class HelpActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHelpBinding

    // TODO: replace with your real support details
    private val supportEmail = "support@blivtech.com"
    private val supportPhone = "+910000000000"
    private val whatsappNum  = "917373277998"   // country code + number, no +

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.ivBack.setOnClickListener { finish() }

        setupFaq()

        // quick contact
        binding.cardChat.setOnClickListener { openWhatsApp() }
        binding.cardEmail.setOnClickListener { sendEmail() }
        binding.cardCall.setOnClickListener { dial() }

        // support rows
        binding.rowWhatsapp.setOnClickListener { openWhatsApp() }
        binding.rowEmail.setOnClickListener { sendEmail() }
        binding.rowCall.setOnClickListener { dial() }

        binding.btnReport.setOnClickListener { sendEmail("EmpTrack — Problem report") }
        binding.btnFeedback.setOnClickListener { sendEmail("EmpTrack — Feedback") }
    }

    private fun setupFaq() {
        val pairs = listOf(
            binding.faqHeader1 to Triple(binding.faqAnswer1, binding.faqIcon1, false),
            binding.faqHeader2 to Triple(binding.faqAnswer2, binding.faqIcon2, false),
            binding.faqHeader3 to Triple(binding.faqAnswer3, binding.faqIcon3, false),
            binding.faqHeader4 to Triple(binding.faqAnswer4, binding.faqIcon4, false),
            binding.faqHeader5 to Triple(binding.faqAnswer5, binding.faqIcon5, false)
        )
        pairs.forEach { (header, t) ->
            val (answer, icon, _) = t
            header.setOnClickListener {
                val show = answer.visibility != View.VISIBLE
                answer.visibility = if (show) View.VISIBLE else View.GONE
                (icon as ImageView).rotation = if (show) 45f else 0f  // + turns into ×
            }
        }
    }

    private fun sendEmail(subject: String = "EmpTrack — Support") {
        val i = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$supportEmail"))
            .putExtra(Intent.EXTRA_SUBJECT, subject)
        runCatching { startActivity(i) }
            .onFailure { toast("No email app found") }
    }

    private fun dial() {
        runCatching { startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$supportPhone"))) }
            .onFailure { toast("Can't open dialer") }
    }

    private fun openWhatsApp() {
        val i = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/$whatsappNum"))
        runCatching { startActivity(i) }
            .onFailure { toast("WhatsApp not available") }
    }

    private fun toast(m: String) = Toast.makeText(this, m, Toast.LENGTH_SHORT).show()
}