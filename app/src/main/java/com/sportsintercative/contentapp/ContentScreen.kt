package com.sportsintercative.contentapp

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.sportsintercative.contentapp.adapter.DevicesAdapter
import com.sportsintercative.contentapp.adapter.ImagePagerAdapter
import com.sportsintercative.contentapp.databinding.ActivityMainBinding
import com.sportsintercative.contentapp.models.ContentData
import com.sportsintercative.contentapp.models.DeviceInfo
import com.sportsintercative.contentapp.models.ImageItem
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*
import kotlin.math.abs

// Name = Nordic_2 Address = FD:47:3C:F7:2B:D3 === -47
//Name = QUIN PRO_1 Address = EF:BD:35:CF:E5:D9 === -66

class ContentScreen : AppCompatActivity() {

    lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ContentScreenViewModel
    private var distance = 50
    private val REQUEST_ENABLE_BT: Int = 102
    private lateinit var imagePagerAdapter: ImagePagerAdapter
    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter?.isEnabled == true

    private var imageList = listOf(
        ImageItem(R.drawable.r1),
        ImageItem(R.drawable.r2)
    )
    private var tempAddress = ""

    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportActionBar?.hide()

        viewModel =
            ViewModelProvider(
                this,
                ContentScreenViewModelFactory(this.applicationContext)
            )[ContentScreenViewModel::class.java]
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setContentData(1)

        SharedPref.init(this)
        showNoDeviceFoundScreen(View.GONE)

        val enableBluetoothLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {}

        val permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { perms ->
            val canEnableBluetooth = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                perms[Manifest.permission.BLUETOOTH_CONNECT] == true
            } else true
            Log.d("canEnableBluetooth", "$canEnableBluetooth")

            if (canEnableBluetooth && !isBluetoothEnabled) {
                enableBluetoothLauncher.launch(
                    Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                )
            } else {
                if (checkBluetoothPermission()) {
                    bluetoothLeScanner.startScan(scanCallback)
                } else requestPermission()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT,
                )
            )
        }

        btSetDistance.setOnClickListener {
            distance = edtDistance.text.toString().toInt()
            SharedPref.write("Distance", distance.toString())
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Device does not supports bluetooth.", Toast.LENGTH_SHORT).show()
            return
        }
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
            ImageItem(R.drawable.p1),
            ImageItem(R.drawable.p2)
        )
        val tuljapurList = listOf(
            ImageItem(R.drawable.t1),
            ImageItem(R.drawable.t2)
        )

        when (position) {
            1 -> displayContent(
                ContentData(
                    getString(R.string.title_ramtek),
                    getString(R.string.description_ramtek),
                    imageList,
                    getString(R.string.youtube_ramtek)
                )
            )
            2, 4 -> displayContent(
                ContentData(
                    getString(R.string.title_panchavati),
                    getString(R.string.description_panchavati),
                    demonImageList,
                    getString(R.string.youtube_panchwati)
                )
            )
            3, 5 -> displayContent(
                ContentData(
                    getString(R.string.title_tuljapur),
                    getString(R.string.description_tuljapur),
                    tuljapurList,
                    getString(R.string.youtube_tuljapur)
                )
            )
            0 -> {
                hideLoader()
            }
            else -> hideLoader()
        }

    }

    private fun displayContent(data: ContentData) {
        binding.txtTitle.text = data.title
        binding.txtDescription.text = data.description

        configureWebView(data.videoId)
        imagePagerAdapter = ImagePagerAdapter(data.imageList)
        pager.adapter = imagePagerAdapter

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

    private fun configureWebView(videoId: String) {
        binding.webView.webChromeClient = WebChromeClient()
        binding.webView.webViewClient = WebViewClient()
        binding.webView.settings.javaScriptEnabled = true
        binding.webView.settings.domStorageEnabled = true
        loadVideoWithIFramePlayer(videoId)
    }

    private fun loadVideoWithIFramePlayer(videoId: String) {
        val iframeVideoUrl = "https://www.youtube.com/embed/$videoId"
        binding.webView.loadData(
            "<html><body><iframe width=\"100%\" height=\"100%\" src=\"$iframeVideoUrl\" frameborder=\"0\" allowfullscreen></iframe></body></html>",
            "text/html",
            "utf-8"
        )
    }

    private fun showLoader() {
        binding.loading.visibility = View.VISIBLE
        binding.conLoader.visibility = View.VISIBLE
    }

    private fun hideLoader() {
        binding.loading.visibility = View.GONE
        binding.conLoader.visibility = View.GONE
    }

    var dataList = ArrayList<DeviceInfo>()
    val adapter = DevicesAdapter(dataList)
    private lateinit var bluetoothAdapter: BluetoothAdapter

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

                Log.d(
                    "BleDevices",
                    "Name = $deviceName Address = $deviceAddress === $deviceDistance"
                )
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
                    txtNearestDevice.text =
                        "${minDistanceLocation.name} - ${abs(minDistanceLocation.distance.toInt())}"
                }
            }

            if (getDistance(minDistanceLocation) < distance) {
                if (tempAddress != minDistanceLocation.address) {
                    tempAddress = minDistanceLocation.address
                    val id = when (minDistanceLocation.address) {//FD:47:3C:F7:2B:D3
                        "DA:C3:70:63:2B:F2" -> 1
                        "EF:BD:35:CF:E5:D9" -> 2
                        "FD:47:3C:F7:2B:D3" -> 3
                        else -> 10
                    }
                    setContentData(id)
                }
                showNoDeviceFoundScreen(View.GONE)
            } /*else
                showNoDeviceFoundScreen(View.VISIBLE)*/
        }
    }

    private fun getDistance(minDistanceLocation: DeviceInfo) =
        abs(minDistanceLocation.distance.toInt())

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

    private fun showDistanceDialog() {
        val builder = AlertDialog.Builder(this)
        val viewGroup: ViewGroup = findViewById(android.R.id.content)
        val dialogView: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.dialog_distance, viewGroup, false)
        builder?.setView(dialogView)
        val alertDialog: AlertDialog = builder!!.create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent);
        dialogView.findViewById<Button>(R.id.btSubmit)!!.setOnClickListener {
            distance = edtDistance.text.toString().toInt()
            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btCancel)!!.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }
}