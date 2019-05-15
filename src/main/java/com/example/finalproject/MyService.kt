package com.example.finalproject

import android.Manifest
import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.telephony.SmsManager
import android.widget.Toast
import com.example.finalproject.MainActivity;

class MyService : Service() {

    val TAG = "MyService"
    lateinit var locationManager: LocationManager
    private var hasGps = false
    private var locationGps: Location? = null
    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        ShowLog("onCreate")
        super.onCreate()

    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        ShowLog("onStartCommand")
        var msg = intent!!.getStringArrayListExtra(MainActivity.myhash)
        var phno = intent!!.getStringArrayListExtra(MainActivity.myhash1)
        var obj = SmsManager.getDefault()
        var lat = ArrayList<String>()
        var long = ArrayList<String>()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        for(i in 0..10) {
            if (hasGps) {
                    locationManager.requestLocationUpdates(
                        LocationManager.GPS_PROVIDER,
                        2000,
                        0F,
                        object : LocationListener {
                            override fun onLocationChanged(location: Location?) {
                                if (location != null) {
                                    locationGps = location
                                    lat.add(locationGps!!.latitude.toString())
                                    long.add(locationGps!!.longitude.toString())
                                    Log.d("AndroidLocation", " GPS Latitude : " + locationGps!!.latitude)
                                    Log.d("AndroidLocation", " GPS Longitude : " + locationGps!!.longitude)
                                    for (x in 0..msg.size - 1) {
                                        var str =
                                            "${msg.get(x)}" + "\n Latitude:" + locationGps!!.latitude + "\n Longitude: " + locationGps!!.longitude
                                        obj.sendTextMessage("${phno.get(x)}", null, "$str", null, null)
                                        Thread.sleep(2000)
                                    }
                                }
                            }

                            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {

                            }

                            override fun onProviderEnabled(provider: String?) {

                            }

                            override fun onProviderDisabled(provider: String?) {

                            }

                        })

                    val localGpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                    if (localGpsLocation != null)
                        locationGps = localGpsLocation

            } else {
                startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
        }
        val runable = Runnable {
            stopSelf()
        }
        val thread = Thread(runable)
        thread.start()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        ShowLog("onDestroy")
        super.onDestroy()
    }

    fun ShowLog(message: String) {
        Log.d(TAG, message)
    }
}
