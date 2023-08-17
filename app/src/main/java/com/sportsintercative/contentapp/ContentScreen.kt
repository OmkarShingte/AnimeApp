package com.sportsintercative.contentapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import com.bumptech.glide.Glide
import com.sportsintercative.contentapp.adapter.DevicesAdapter
import com.sportsintercative.contentapp.adapter.ImagePagerAdapter
import com.sportsintercative.contentapp.constants.AppConstants
import com.sportsintercative.contentapp.models.ContentData
import com.sportsintercative.contentapp.models.DeviceInfo
import com.sportsintercative.contentapp.models.ImageItem
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.abs
import kotlin.math.pow


class ContentScreen : AppCompatActivity() {

    private val REQUEST_ENABLE_BT: Int = 102
    private lateinit var imagePagerAdapter: ImagePagerAdapter
    private var mMediaPlayer: MediaPlayer? = null
    private var imageList = listOf(
        ImageItem(R.drawable.a1),
        ImageItem(R.drawable.a2),
        ImageItem(R.drawable.a3),
        ImageItem(R.drawable.a4),
        ImageItem(R.drawable.a5)
    )
    private var isPlaying = false
    var tempAddress = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()
//        showLoader()
//        setContentData(1)

        showNoDeviceFoundScreen(View.VISIBLE)
        var position = 0
        ibNext.setOnClickListener {
            if (position <= 5) {
                showLoader()
                ++position
//                setContentData(position)
            } else {
                Toast.makeText(this, "This is last content", Toast.LENGTH_LONG).show()
                hideLoader()
            }
        }
        ibPrevious.setOnClickListener {
            if (position > 0) {
                showLoader()
                --position
//                setContentData(position)
            } else {
                Toast.makeText(this, "This is first content", Toast.LENGTH_LONG).show()
                hideLoader()
            }
        }

        Glide.with(this)
            .load(AppConstants.b1)
            .into(imageView2)

        if (checkBluetoothPermission()) {
            Log.d("BleDevices", "Permission granted onCreate")
            bluetoothLeScanner.startScan(scanCallback)
        } else requestPermission()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkBluetoothPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            (ContextCompat.checkSelfPermission(
                this, Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED)

        } else {
            (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_SCAN
            ) == PackageManager.PERMISSION_GRANTED)
        }
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
        imagePagerAdapter = ImagePagerAdapter(data.imageList)
        pager.adapter = imagePagerAdapter

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
            if (currentPosition == imagePagerAdapter.count - 1) {
                currentPosition = 0
            } else {
                currentPosition--
            }
            pager.currentItem = currentPosition
        }
        view_back1.setOnClickListener {
            var currentPosition = pager.currentItem
            if (currentPosition == imagePagerAdapter.count - 1) {
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

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onDestroy() {
        super.onDestroy()
        stopSound()
        if (checkBluetoothPermission()) {
            Log.d("BleDevices", "Permission granted onDestroy")
            bluetoothLeScanner.stopScan(scanCallback)
        } else
            requestPermission()
    }

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onPause() {
        super.onPause()
        if (checkBluetoothPermission()) {
            bluetoothLeScanner.stopScan(scanCallback)
        } else
            requestPermission()
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


    var dataList = ArrayList<DeviceInfo>() // Your data list
    val adapter = DevicesAdapter(dataList)

    private val bluetoothLeScanner: BluetoothLeScanner =
        BluetoothAdapter.getDefaultAdapter().bluetoothLeScanner
    private val scanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult?) {
            super.onScanResult(callbackType, result)
            if (ActivityCompat.checkSelfPermission(
                    this@ContentScreen,
                    Manifest.permission.BLUETOOTH_CONNECT
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            val deviceAddress = result?.device
            val deviceName = deviceAddress?.name
            val deviceDistance = result?.rssi
            if (!deviceName.isNullOrEmpty()) {
//                if (deviceName!!.contains("QUIN") || deviceName.contains("ColorFit")) {
                if (dataList.isEmpty())
                    dataList.add(
                        DeviceInfo(
                            deviceDistance.toString(),
                            deviceName.toString(),
                            deviceAddress.toString()
                        )
                    )
                else {
                    if (dataList.none { deviceInfo -> deviceInfo.address == deviceAddress.toString() }) {
                        dataList.add(
                            DeviceInfo(
                                deviceDistance.toString(),
                                deviceName.toString(),
                                deviceAddress.toString()
                            )
                        )
                    } else {
                        dataList.forEach {
                            if (it.address == deviceAddress.toString()) {
                                it.distance = deviceDistance.toString()
                            }
                        }
                    }
                }
                adapter.notifyDataSetChanged()

                val distance = returnDistance(calculateDistance(abs(deviceDistance!!.toInt())))
                Log.d(
                    "BleDevices",
                    "Name = $deviceName Address = $deviceAddress === $deviceDistance  ++ $distance"
                )
//                ScanResult{device=DA:C3:70:63:2B:F2, scanRecord=ScanRecord [mAdvertiseFlags=5,
            //                mServiceUuids=[0000180a-0000-1000-8000-00805f9b34fb, 8925d23d-03e4-4447-826c-418dadc7f483],
            //                mServiceSolicitationUuids=[], mManufacturerSpecificData={}, mServiceData={}, mTxPowerLevel=-2147483648,
            //                mDeviceName=QUIN PRO+, mTDSData=null], rssi=-80, timestampNanos=399613703737786, eventType=27, primaryPhy=1,
            //                secondaryPhy=0, advertisingSid=255, txPower=127, periodicAdvertisingInterval=0}
            }
//            }
            setData(dataList)
        }
    }

    private fun setData(dataList: ArrayList<DeviceInfo>) {
        if (dataList.isNotEmpty()) {
            var minDistanceLocation: DeviceInfo = dataList[0]
            for (location in dataList) {
                if (abs(location.distance.toInt()) <= abs(minDistanceLocation.distance.toInt())) {
                    minDistanceLocation = location
                }
            }
            if (getDistance(minDistanceLocation) < 50) {
                if (tempAddress != minDistanceLocation.address) {
                    tempAddress = minDistanceLocation.address
                    val id = when (minDistanceLocation.address) {
                        "DA:C3:70:63:2B:F2" -> 1
                        "D6:32:12:D7:D3:90" -> 2
                        else -> 10
                    }
                    setContentData(id)
                    Toast.makeText(this, "${minDistanceLocation.name} - ${calculateDistance(getDistance(minDistanceLocation))}", Toast.LENGTH_SHORT).show()
                }
                showNoDeviceFoundScreen(View.GONE)
            } else
                showNoDeviceFoundScreen(View.VISIBLE)
        }
    }

    private fun getDistance(minDistanceLocation: DeviceInfo) =
        abs(minDistanceLocation.distance.toInt())

    fun calculateDistance(rssi: Int): Double {
        val referenceRssi = -58 // Reference RSSI at a known distance
        val n = 2.0 // Path loss exponent
//        return String.format("%.2f", (10.0.pow((referenceRssi - rssi) / (10 * n))))
        return 10.0.pow((referenceRssi - rssi) / (10 * n))
    }

    fun returnDistance(calculateDistance: Double): String {
        return String.format("%.2f", calculateDistance * 1000000)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            101 -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.S)
    private fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                101
            )
        } else {
            ActivityCompat.requestPermissions(
                this, arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                101
            )
        }
    }

    private fun showNoDeviceFoundScreen(isVisible: Int) {
        clNoDeviceFound.visibility = isVisible
    }
}