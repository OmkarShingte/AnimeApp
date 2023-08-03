package com.sportsintercative.contentapp

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.sportsintercative.contentapp.adapter.ImagePagerAdapter
import com.sportsintercative.contentapp.models.ImageItem
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class ContentScreen : AppCompatActivity() {

    private lateinit var viewPager: ViewPager
    private lateinit var viewPagerFull: ViewPager
    private lateinit var adapter: ImagePagerAdapter
    private var mMediaPlayer: MediaPlayer? = null
    private var imageList = listOf(
        ImageItem(R.drawable.a1),
        ImageItem(R.drawable.a2),
        ImageItem(R.drawable.a3),
        ImageItem(R.drawable.a4),
        ImageItem(R.drawable.a5)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        viewPager = findViewById(R.id.pager)
        viewPagerFull = findViewById(R.id.pager)
        adapter = ImagePagerAdapter(imageList)
        viewPager.adapter = adapter
        viewPagerFull.adapter = adapter


        var isPlaying = false
        btPlayPause.setOnClickListener {
            if (isPlaying) {
                isPlaying = false
                pauseSound()
            }
            else {
                isPlaying = true
                playSound()
            }
        }
        findViewById<View>(R.id.view_back).setOnClickListener {
            var currentPosition = viewPagerFull.currentItem
            if (currentPosition == adapter.count - 1) {
                currentPosition = 0
            } else {
                currentPosition--
            }
            viewPagerFull.currentItem = currentPosition
        }
        findViewById<View>(R.id.view_back1).setOnClickListener {
            var currentPosition = viewPagerFull.currentItem
            if (currentPosition == adapter.count - 1) {
                currentPosition = 0
            } else {
                currentPosition++
            }
            viewPagerFull.currentItem = currentPosition
        }

        viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {}

            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {
            }

            override fun onPageSelected(position: Int) {
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSound()
    }

    // Play audio
    private fun playSound() {
        if (mMediaPlayer == null) {
            mMediaPlayer = MediaPlayer.create(this, R.raw.song)
            mMediaPlayer!!.isLooping = true
            mMediaPlayer!!.start()
            val timer = Timer()

            mMediaPlayer!!.setOnPreparedListener {
                mMediaPlayer!!.start()
                timer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            val currentPosition = mMediaPlayer!!.currentPosition
                            val total = mMediaPlayer!!.duration
                            val current = String.format("%02d:%02d", currentPosition / 1000 / 60, currentPosition / 1000 % 60)
                            val duration = String.format("%02d:%02d", total / 1000 / 60, total / 1000 % 60)
//                            findViewById<Button>(R.id.button).text = "$current/$duration"
                            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                            val progress = (currentPosition.toFloat() / total.toFloat() * 100).toInt()
                            progressBar.progress = progress

                        }
                    }
                }, 0, 1000)
            }
        } else {
            mMediaPlayer!!.start()
        }
        btPlayPause.setImageResource(R.drawable.ic_pause)
    }

    // Pause audio
    private fun pauseSound() {
        if (mMediaPlayer?.isPlaying == true) {
            mMediaPlayer?.pause()
            btPlayPause.setImageResource(R.drawable.ic_play)
        }
    }

    // Stop audio
    fun stopSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }
}
