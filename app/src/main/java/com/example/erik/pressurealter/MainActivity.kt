package com.example.erik.pressurealter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.hardware.SensorManager
import android.location.Location
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.ContextCompat.getSystemService
import android.view.View
import android.widget.TextView
import com.google.android.gms.location.*

class MainActivity : SensorEventListener, AppCompatActivity()   {
    private val MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1

    private val locationRequest = LocationRequest().apply {
        interval = 2000
        fastestInterval = 1000
        priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    }


    private lateinit var sensorManager: SensorManager
    private lateinit var pressure: Sensor
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private var sensing = false


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        pressure = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations){
                    findViewById<TextView>(R.id.textView_location).text =  "%.2f m".format(location.altitude)
                }
            }
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        val millibars_of_pressure = event?.values?.get(0)

        findViewById<TextView>(R.id.textView_pressure).text = "%.2f".format(millibars_of_pressure)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //do not care
    }

    override fun onResume() {
        super.onResume()
        if (sensing) {
            startSensors()
        }

    }

    override fun onPause() {
        super.onPause()
        if (sensing) {
            stopSensors()
        }
    }

    fun startPress(view: View) {
        if (sensing) {
            sensing = false
            stopSensors()
            findViewById<TextView>(R.id.startbutton).text = getString(R.string.starttext)
        } else {
            sensing = true
            startSensors()
            findViewById<TextView>(R.id.startbutton).text = getString(R.string.stoptext)
        }
    }

    private fun startSensors() {
        sensorManager.registerListener(this, pressure, SensorManager.SENSOR_DELAY_NORMAL)

        val fine = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarse = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        if ( fine  != PackageManager.PERMISSION_GRANTED || coarse != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
                    MY_PERMISSIONS_REQUEST_READ_CONTACTS)
        } else {
            startLocation()
        }
    }

    private fun stopSensors() {
        sensorManager.unregisterListener(this)

    }

    fun startLocation() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            MY_PERMISSIONS_REQUEST_READ_CONTACTS -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    startLocation()
                }
                return
            }
            else -> {
                // Ignore all other requests.
            }
        }
    }
}
