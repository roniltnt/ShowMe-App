package com.example.showme

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.showme.databinding.ActivityFullscreenImageBinding

class FullscreenImageActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFullscreenImageBinding

    // key to URL
    // companion = static
    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFullscreenImageBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // get URL
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)

        if (!imageUrl.isNullOrEmpty()) {
            Glide.with(this)
                .load(imageUrl)
                .into(binding.fullscreenImageView)
        } else {
            // if failed to get URL
            finish()
        }

        binding.fullscreenImageView.setOnClickListener {
            supportFinishAfterTransition()
        }
    }
}