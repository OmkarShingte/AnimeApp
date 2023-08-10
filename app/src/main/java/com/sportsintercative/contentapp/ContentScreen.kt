package com.sportsintercative.contentapp

import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.sportsintercative.contentapp.adapter.ImagePagerAdapter
import com.sportsintercative.contentapp.constants.AppConstants
import com.sportsintercative.contentapp.models.ContentData
import com.sportsintercative.contentapp.models.ImageItem
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds


class ContentScreen : AppCompatActivity() {

    private lateinit var adapter: ImagePagerAdapter
    private var mMediaPlayer: MediaPlayer? = null
    private var imageList = listOf(
        ImageItem(R.drawable.a1),
        ImageItem(R.drawable.a2),
        ImageItem(R.drawable.a3),
        ImageItem(R.drawable.a4),
        ImageItem(R.drawable.a5)
    )
    private var isPlaying = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
        showLoader()
        setContentData(1)

        var position = 0
        ibNext.setOnClickListener {
            if (position <= 5) {
                showLoader()
                ++position
                setContentData(position)
            } else {
                Toast.makeText(this, "This is last content", Toast.LENGTH_LONG).show()
                hideLoader()
            }
        }
        ibPrevious.setOnClickListener {
            if (position > 0) {
                showLoader()
                --position
                setContentData(position)
            } else {
                Toast.makeText(this, "This is first content", Toast.LENGTH_LONG).show()
                hideLoader()
            }
        }

        Glide.with(this)
            .load(AppConstants.b1)
            .into(imageView2)
    }

    private fun setContentData(position: Int) {
        val demonImageList = listOf(
            ImageItem(R.drawable.b1),
            ImageItem(R.drawable.b2),
            ImageItem(R.drawable.b3),
            ImageItem(R.drawable.b4),
            ImageItem(R.drawable.b5)
        )
        val jujutsuList = listOf(
            ImageItem(R.drawable.c1),
            ImageItem(R.drawable.c2),
            ImageItem(R.drawable.c3),
            ImageItem(R.drawable.c4),
            ImageItem(R.drawable.c5)
        )

        when (position) {
            1 -> displayContent(
                ContentData(
                    getString(R.string.title_one_piece) + "1",
                    getString(R.string.description_one_piece),
                    imageList,
                    "S8_YwFLCh4U"
                )
            )
            2, 4 -> displayContent(
                ContentData(
                    getString(R.string.title_demon_slayer) + "2",
                    getString(R.string.description_demon_slayer),
                    demonImageList,
                    "9DhuWapDDrw"
                )
            )
            3, 5 -> displayContent(
                ContentData(
                    getString(R.string.title_jujutsu_kaisen) + "3",
                    getString(R.string.description_jujutsu),
                    jujutsuList,
                    "fDKmSkMOkIk"
                )
            )
//            4 -> displayContent(AppConstants.onePiece)
//            5 -> displayContent(AppConstants.onePiece)
            0 -> {
                Toast.makeText(this, "This is first content", Toast.LENGTH_LONG).show()
                hideLoader()
            }
            else -> hideLoader()
        }

    }

    private fun displayContent(data: ContentData) {
        txtTitle.text = data.title
        txtDescription.text = data.description

        configureWebView(data.videoId)
        adapter = ImagePagerAdapter(data.imageList)
        pager.adapter = adapter

        btPlayPause.setOnClickListener {
            if (isPlaying) {
                pauseSound()
            } else {
                playSound()
            }
            isPlaying = !isPlaying
        }

        view_back.setOnClickListener {
            var currentPosition = pager.currentItem
            if (currentPosition == adapter.count - 1) {
                currentPosition = 0
            } else {
                currentPosition--
            }
            pager.currentItem = currentPosition
        }
        view_back1.setOnClickListener {
            var currentPosition = pager.currentItem
            if (currentPosition == adapter.count - 1) {
                currentPosition = 0
            } else {
                currentPosition++
            }
            pager.currentItem = currentPosition
        }

        pager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
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
        hideLoader()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopSound()
    }

    private fun playSound() {
        if (mMediaPlayer == null) {
            var currentPosition = 0
            var total = 0
            mMediaPlayer = MediaPlayer.create(this, R.raw.song)
            mMediaPlayer!!.isLooping = false
            mMediaPlayer!!.start()
            val timer = Timer()

            mMediaPlayer!!.setOnPreparedListener {
                mMediaPlayer!!.start()
                timer.scheduleAtFixedRate(object : TimerTask() {
                    override fun run() {
                        runOnUiThread {
                            currentPosition = mMediaPlayer!!.currentPosition
                            total = mMediaPlayer!!.duration
                            val progressBar = findViewById<ProgressBar>(R.id.progressBar)
                            val progress =
                                (currentPosition.toFloat() / total.toFloat() * 100).toInt()
                            progressBar.progress = progress

                        }
                    }
                }, 0, 1000)
            }
            mMediaPlayer!!.setOnPreparedListener {
                seekBar.max = mMediaPlayer!!.duration
            }

            mMediaPlayer!!.setOnCompletionListener {
                btPlayPause.setImageResource(R.drawable.ic_play)
                isPlaying = false
                seekBar.progress = 0
            }

            seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        mMediaPlayer!!.seekTo(progress)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}

                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
            Thread {
                while (true) {
                    Thread.sleep(1000)
                    runOnUiThread {
                        if (mMediaPlayer!!.isPlaying) {
                            seekBar.progress = mMediaPlayer!!.currentPosition
//                            txtTimer.text =
//                                "${TimeUnit.MILLISECONDS.toMinutes(mMediaPlayer!!.currentPosition.toLong())}/${
//                                    TimeUnit.MILLISECONDS.toSeconds(mMediaPlayer!!.currentPosition.toLong())
//                                }"
                        }
                    }
                }
            }.start()
        } else {
            mMediaPlayer!!.start()
        }
        btPlayPause.setImageResource(R.drawable.ic_pause)
    }

    private fun pauseSound() {
        if (mMediaPlayer?.isPlaying == true) {
            mMediaPlayer?.pause()
            btPlayPause.setImageResource(R.drawable.ic_play)
        }
    }

    private fun stopSound() {
        if (mMediaPlayer != null) {
            mMediaPlayer!!.stop()
            mMediaPlayer!!.release()
            mMediaPlayer = null
        }
    }

    private fun configureWebView(videoId: String) {
        webView.webChromeClient = WebChromeClient()
        webView.webViewClient = WebViewClient()
        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true
        loadVideoWithIFramePlayer(videoId)
    }

    private fun loadVideoWithIFramePlayer(videoId: String) {
        val iframeVideoUrl = "https://www.youtube.com/embed/$videoId"
        webView.loadData(
            "<html><body><iframe width=\"100%\" height=\"100%\" src=\"$iframeVideoUrl\" frameborder=\"0\" allowfullscreen></iframe></body></html>",
            "text/html",
            "utf-8"
        )
    }

    private fun showLoader() {
        loading.visibility = View.VISIBLE
        conLoader.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        loading.visibility = View.GONE
        conLoader.visibility = View.GONE
    }
}
