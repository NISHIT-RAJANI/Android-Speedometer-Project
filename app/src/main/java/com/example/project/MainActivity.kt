package com.example.project

import `in`.unicodelabs.kdgaugeview.KdGaugeView
import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.telephony.SmsManager
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import androidx.core.app.ActivityCompat.requestPermissions

import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.core.location.LocationManagerCompat

import com.google.android.gms.tasks.OnCompleteListener

import androidx.core.location.LocationManagerCompat.isLocationEnabled
import com.google.android.gms.location.LocationResult

import com.google.android.gms.location.LocationCallback
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.math.roundToInt
import com.google.firebase.database.DatabaseReference

import com.google.firebase.database.FirebaseDatabase





@RequiresApi(Build.VERSION_CODES.M)
class MainActivity : AppCompatActivity() {
    var speedoMeterView: KdGaugeView? = null
    var mFusedLocationClient: FusedLocationProviderClient? = null
    var PERMISSION_ID = 44
    var p1 = 0f
    var p2 = 0f
    var p3 = 0f
    var p4 = 0f
    private val INTERVAL = (1000 * 2).toLong()
    private val FASTEST_INTERVAL = (1000 * 1).toLong()
    var flag = true
    var smS = SmsManager.getDefault() as SmsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        speedoMeterView = findViewById<View>(R.id.speedMeter) as KdGaugeView

        if((ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_SMS)!= PackageManager.PERMISSION_GRANTED))
        {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_SMS, android.Manifest.permission.SEND_SMS), 111)
        }

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()
    }

    @SuppressLint("MissingPermission", "SetTextI18n")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                mFusedLocationClient!!.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        p1 = location.longitude.toFloat()
                        p2 = location.latitude.toFloat()
                        val dSpeed: Float = location.speed
                        val a = 3.6 * dSpeed
                        val kmhSpeed = Math.round(a).toInt()
                        textLongitude.text = "Longitude= " + location.longitude
                        textLatitude.text = "Latitude= " + location.latitude
                        textSpeed.text = "SPEED= $kmhSpeed"
                        speedoMeterView!!.setSpeed(kmhSpeed.toFloat())
                        requestNewLocationData()
                    }
                }
            } else {
                Toast.makeText(this, "Please turn on" + " your location...", Toast.LENGTH_LONG)
                    .show()
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        val mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = INTERVAL
        mLocationRequest.fastestInterval = FASTEST_INTERVAL
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocationClient?.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback: LocationCallback = object : LocationCallback() {

        @SuppressLint("SetTextI18n")
        override fun onLocationResult(locationResult: LocationResult) {
            val mLastLocation = locationResult.lastLocation
            p3 = mLastLocation.longitude.toFloat()
            p4 = mLastLocation.latitude.toFloat()
            val dSpeed = mLastLocation.speed.toDouble()
            val a = 3.6 * dSpeed
            val kmhSpeed = a.roundToInt()

            textLongitude.text = "Longitude= " + mLastLocation.longitude
            textLatitude.text = "Latitude= " + mLastLocation.latitude
            textSpeed.text = "SPEED= $kmhSpeed"
            // Write a message to the database
            // Write a message to the database
            val database = FirebaseDatabase.getInstance()
            val myRef = database.getReference("1")
            myRef.setValue(kmhSpeed.toString())

            if (kmhSpeed > 90 && flag){
//                smS.sendTextMessage("+916353059208",null,"Roll number 23 Sends me SMS",null,null)
                smS.sendTextMessage("+919327052373",null,"Vehicle speed is above 90 Km/H.",null,null)
                flag = false
            } else flag = true

            speedoMeterView!!.setSpeed(kmhSpeed.toFloat())
            getLastLocation()
        }
    }

    private fun checkPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // If we want background location
        // on Android 10.0 and higher,
        // use:
        // ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissions() {
        requestPermissions(
            this, arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ), PERMISSION_ID
        )
    }

    private fun isLocationEnabled(): Boolean {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_ID) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (checkPermissions()) {
            getLastLocation()
        }
    }


}