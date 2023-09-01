package com.sportsintercative.contentapp

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
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
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.geojson.Polygon
import com.mapbox.maps.*
import com.mapbox.maps.extension.style.sources.generated.GeoJsonSource
import com.mapbox.maps.extension.style.sources.generated.geoJsonSource
import com.mapbox.maps.extension.style.sources.getSource
import com.mapbox.maps.extension.style.style
import com.mapbox.maps.plugin.animation.MapAnimationOptions
import com.mapbox.maps.plugin.animation.camera
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.CircleAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createCircleAnnotationManager
import com.mapbox.maps.plugin.gestures.*
import com.mapbox.maps.plugin.locationcomponent.*
import com.mapbox.navigation.base.options.NavigationOptions
import com.mapbox.navigation.core.MapboxNavigation
import com.mapbox.navigation.core.lifecycle.MapboxNavigationApp
import com.mapbox.navigation.core.lifecycle.MapboxNavigationObserver
import com.mapbox.navigation.core.lifecycle.requireMapboxNavigation
import com.mapbox.navigation.core.trip.session.LocationMatcherResult
import com.mapbox.navigation.core.trip.session.LocationObserver
import com.mapbox.navigation.ui.maps.location.NavigationLocationProvider
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

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: ContentScreenViewModel
    private var distance = 50
    private val REQUEST_ENABLE_BT: Int = 102
    private lateinit var imagePagerAdapter: ImagePagerAdapter
    private val isBluetoothEnabled: Boolean
        get() = bluetoothAdapter.isEnabled

    private var imageList = listOf(
        ImageItem(R.drawable.r1),
        ImageItem(R.drawable.r2)
    )
    private var tempAddress = ""
    private lateinit var mapboxMap: MapboxMap
    private val navigationLocationProvider = NavigationLocationProvider()

    companion object {
        private const val BOUNDS_ID = "BOUNDS_ID"
        private val OFFICE_BOUND: CameraBoundsOptions = CameraBoundsOptions.Builder()
            .bounds(
                CoordinateBounds(
                    Point.fromLngLat(73.78601163456085, 18.54137340267236),
                    Point.fromLngLat(73.78796500881278, 18.54037285956325),
                    false
                )
            )
            .minZoom(10.0)
            .build()

        private val INFINITE_BOUNDS: CameraBoundsOptions = CameraBoundsOptions.Builder()
            .bounds(
                CoordinateBounds(
                    Point.fromLngLat(0.0, 0.0),
                    Point.fromLngLat(0.0, 0.0),
                    true
                )
            )
            .build()
    }
    private val activityResultLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { result ->
        if (result) {
            Toast.makeText(this@ContentScreen, "Permission granted!", Toast.LENGTH_SHORT).show()
        }
    }

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
        if (ActivityCompat.checkSelfPermission(
                this@ContentScreen,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            activityResultLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

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

        enableLoc()
        initiateMapbox()
    }

    private val mapboxNavigation: MapboxNavigation by requireMapboxNavigation(
        onResumedObserver = object : MapboxNavigationObserver {
            @SuppressLint("MissingPermission")
            override fun onAttached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.registerLocationObserver(locationObserver)
                mapboxNavigation.startTripSession()
            }

            override fun onDetached(mapboxNavigation: MapboxNavigation) {
                mapboxNavigation.unregisterLocationObserver(locationObserver)
            }
        },
        onInitialize = this::initNavigation
    )
    private val locationObserver = object : LocationObserver {
        override fun onNewRawLocation(rawLocation: Location) {
            // Not implemented in this example. However, if you want you can also
            // use this callback to get location updates, but as the name suggests
            // these are raw location updates which are usually noisy.
        }

        override fun onNewLocationMatcherResult(locationMatcherResult: LocationMatcherResult) {
            Log.d("locationMatcherResult", "$locationMatcherResult")
            val enhancedLocation: Location? = locationMatcherResult.enhancedLocation
            if (locationMatcherResult.keyPoints.isEmpty()) {
                navigationLocationProvider.changePosition(
                    enhancedLocation!!,
                    locationMatcherResult.keyPoints,
                )
                updateCamera(enhancedLocation)
            }
        }
    }

    private fun initiateMapbox() {
        binding.mapView.getMapboxMap().loadStyleUri(Style.TRAFFIC_DAY)
        mapboxMap = binding.mapView.getMapboxMap()
        mapboxMap.loadStyle(
            style(Style.TRAFFIC_DAY) {
                +geoJsonSource(BOUNDS_ID) {
                    featureCollection(FeatureCollection.fromFeatures(listOf()))
                }
            }
        ) { setupBounds(OFFICE_BOUND) }
        setMapviewProperties()
    }

    private fun showBoundsArea(boundsOptions: CameraBoundsOptions) {
        val source = mapboxMap.getStyle()!!.getSource(BOUNDS_ID) as GeoJsonSource
        val bounds = boundsOptions.bounds
        val list = mutableListOf<List<Point>>()
        bounds?.let {
            if (!it.infiniteBounds) {
                val northEast = it.northeast
                val southWest = it.southwest
                val northWest = Point.fromLngLat(southWest.longitude(), northEast.latitude())
                val southEast = Point.fromLngLat(northEast.longitude(), southWest.latitude())
                list.add(
                    mutableListOf(northEast, southEast, southWest, northWest, northEast)
                )
            }
        }
        source.geometry(
            Polygon.fromLngLats(
                list
            )
        )
    }

    private fun setupBounds(bounds: CameraBoundsOptions) {
        mapboxMap.setBounds(bounds)
        showBoundsArea(bounds)
        // Create an instance of the Annotation API and get the CircleAnnotationManager.
        setPointOnMap(18.540746140968572,73.78681272113883)
        setPointOnMap(18.54071749019817, 73.7870453813003)
    }

    private fun setPointOnMap(latitude: Double, longitude: Double) {
        val annotationApi = binding.mapView.annotations
        val circleAnnotationManager = annotationApi.createCircleAnnotationManager()
        val circleAnnotationOptions: CircleAnnotationOptions = CircleAnnotationOptions()
            .withPoint(Point.fromLngLat(longitude, latitude))
            .withCircleRadius(8.0)
            .withCircleColor("#ee4e8b")
            .withCircleStrokeWidth(2.0)
            .withCircleStrokeColor("#ffffff")
        circleAnnotationManager?.create(circleAnnotationOptions)
    }

    private fun setMapviewProperties() {
        val properties = View(this)
        properties.layoutParams = FrameLayout.LayoutParams(10, 10, Gravity.CENTER)
        properties.setBackgroundColor(Color.BLUE)
        binding.mapView.addView(properties)
    }

    private fun updateCamera(location: Location) {
        val mapAnimationOptions = MapAnimationOptions.Builder().duration(1500L).build()
        binding.mapView.camera.easeTo(
            CameraOptions.Builder()
                // Centers the camera to the lng/lat specified.
                .center(Point.fromLngLat(location.longitude, location.latitude))
                // specifies the zoom value. Increase or decrease to zoom in or zoom out
                .zoom(12.0)
                // specify frame of reference from the center.
                .padding(EdgeInsets(0.0, 0.0, 0.0, 0.0))
                .build(),
            mapAnimationOptions
        )
    }

    private fun initNavigation() {
        MapboxNavigationApp.setup(
            NavigationOptions.Builder(this)
                .accessToken(getString(R.string.mapbox_access_token))
                .build()
        )
        binding.mapView.location.apply {
            setLocationProvider(navigationLocationProvider)
/*
            locationPuck = LocationPuck2D(
                bearingImage = ContextCompat.getDrawable(
                    this@MainActivity,
                    R.drawable.mapbox_navigation_puck_icon
                )
            )
*/
            enabled = true
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
        binding.mapView.onDestroy()
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
        builder.setView(dialogView)
        val alertDialog: AlertDialog = builder.create()
        alertDialog.window!!.setBackgroundDrawableResource(android.R.color.transparent)
        dialogView.findViewById<Button>(R.id.btSubmit)!!.setOnClickListener {
            distance = edtDistance.text.toString().toInt()
            alertDialog.dismiss()
        }
        dialogView.findViewById<Button>(R.id.btCancel)!!.setOnClickListener {
            alertDialog.dismiss()
        }
        alertDialog.show()
    }

    private fun enableLoc() {
        val locationRequest: LocationRequest =
            LocationRequest.create()
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 30 * 1000
        locationRequest.fastestInterval = 5 * 1000
        val builder: LocationSettingsRequest.Builder = LocationSettingsRequest.Builder()
            .addLocationRequest(locationRequest)
        builder.setAlwaysShow(true)
        val result: com.google.android.gms.tasks.Task<LocationSettingsResponse> =
            LocationServices.getSettingsClient(this).checkLocationSettings(builder.build())
        result.addOnCompleteListener {
            try {
                val response: LocationSettingsResponse =
                    it.getResult(ApiException::class.java)
                // All location settings are satisfied. The client can initialize location
                // requests here.
            } catch (exception: ApiException) {
                when (exception.statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED ->                         // Location settings are not satisfied. But could be fixed by showing the
                        // user a dialog.
                        try {
                            // Cast to a resolvable exception.
                            val resolvable: ResolvableApiException =
                                exception as ResolvableApiException
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            resolvable.startResolutionForResult(
                                this@ContentScreen,
                                100
                            )
                        } catch (e: SendIntentException) {
                            // Ignore the error.
                        } catch (e: ClassCastException) {
                            // Ignore, should be an impossible error.
                        }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {}
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        binding.mapView.onStart()
    }

    override fun onStop() {
        super.onStop()
        binding.mapView.onStop()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        binding.mapView.onLowMemory()
    }
}