package com.example.finalproject

import android.os.Bundle
import android.net.Uri
import android.content.ContentValues
import android.support.design.widget.Snackbar
import android.support.design.widget.NavigationView
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.support.v4.content.ContextCompat
import android.support.v4.app.ActivityCompat
import android.content.pm.PackageManager
import android.telephony.SmsManager
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import android.Manifest
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.content_main.*
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.provider.Settings
import android.speech.RecognizerIntent
import android.util.Log
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

private const val PERMISSION_REQUEST = 10
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    lateinit var locationManager: LocationManager
    private var hasGps = false
    var count = 0
    companion object {
        val myhash="myhash"
        val myhash1 = "myhash1"
    }
    private var locationGps: Location? = null
    private var locationNetwork: Location? = null
    private val REQUEST_CODE_SPEECH_INPUT = 100
    private var permissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        val toggle = ActionBarDrawerToggle(
            this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, Array<String>(1) { Manifest.permission.SEND_SMS}, 1)
        }
        else {
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkPermission(permissions)) {
                enableView()
            } else {
                requestPermissions(permissions, PERMISSION_REQUEST)
            }
        } else {
            enableView()
        }
    }

    override fun onResume() {
        super.onResume()
        setVisible(true)
        Log.d("state","Resumed")
    }

    override fun onPause() {
        super.onPause()
        setVisible(false)
        serv()
        Log.d("state","Paused")

    }

    private fun enableView() {
        voiceBtn.isEnabled = true
        voiceBtn.alpha = 1F
        voiceBtn.setOnClickListener {
            speak()
           // sendsms()
        }
        Toast.makeText(this, "Done", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermission(permissionArray: Array<String>): Boolean {
        var allSuccess = true
        for (i in permissionArray.indices) {
            if (checkCallingOrSelfPermission(permissionArray[i]) == PackageManager.PERMISSION_DENIED)
                allSuccess = false
        }
        return allSuccess
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST) {
            var allSuccess = true
            for (i in permissions.indices) {
                if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                    allSuccess = false
                    val requestAgain = Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && shouldShowRequestPermissionRationale(permissions[i])
                    if (requestAgain) {
                        Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this, "Go to settings and enable the permission", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            if (allSuccess)
                enableView()

        }
    }
    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    public fun addRecord(view: View) {
        var values = ContentValues()
        if(!(name.text.toString().isEmpty()) && (!(phno.text.toString().isEmpty()))) {
            values.put(CustomContentProvider.NAME,name.text.toString())
            values.put(CustomContentProvider.PH_NO,phno.text.toString())
            contentResolver.insert(CustomContentProvider.CONTENT_URI,values)
            Toast.makeText(baseContext,"Record Inserted",Toast.LENGTH_LONG).show()}
        else {
            Toast.makeText(baseContext,"Please enter the records first",Toast.LENGTH_LONG).show()
        }
    }
    public fun showAllRecords(view: View) {
        val URL = "content://com.example.finalproject/contacts"
        var friends = Uri.parse(URL)
        var c = contentResolver.query(friends,null,null,null,"name")
        var result = "Content Provider Results:"
        if(!c.moveToFirst()) {
            Toast.makeText(this,result+"no content yet!",Toast.LENGTH_LONG).show()
        }
        else {
            do {
                result += "\n NAME:${c.getString(c.getColumnIndex(CustomContentProvider.NAME))} \n ID: ${c.getString(
                    (c.getColumnIndex(
                        CustomContentProvider.ID
                    ))
                )} PH NO:${c.getString(c.getColumnIndex(CustomContentProvider.PH_NO))}"
            }while(c.moveToNext())
            if(!result.isEmpty())
                Toast.makeText(this,result,Toast.LENGTH_LONG).show()
            else
                Toast.makeText(this,"No Records Present",Toast.LENGTH_LONG).show()
        }
    }

    public fun deleteAllRecords(view: View) {
        val URL = "content://com.example.finalproject/contacts"
        var friends = Uri.parse(URL)
        var count = contentResolver.delete(friends,null,null)
        var countNum = "$count records are deleted."
        Toast.makeText(baseContext,countNum,Toast.LENGTH_LONG).show()
    }

    public fun delete(view: View) {
        val URL = "content://com.example.finalproject/contacts"
        var friends = Uri.parse(URL)
        val ii = idup.text.toString()
        var count = contentResolver.delete(friends,ii,null)
        var countNum = "$count records are deleted."
        Toast.makeText(baseContext,countNum,Toast.LENGTH_LONG).show()
    }

    public fun updates(view: View) {
        var values = ContentValues()
        val URL = "content://com.example.finalproject/contacts"
        var friends = Uri.parse(URL)
        val ii = idup.text.toString()
        if(!(name.text.toString().isEmpty()) && (!(phno.text.toString().isEmpty()))) {
            values.put(CustomContentProvider.NAME, name.text.toString())
            values.put(CustomContentProvider.PH_NO, phno.text.toString())
            var count = contentResolver.update(friends,values,ii,null)
            var countNum = "$count records are updated."
            Toast.makeText(baseContext,countNum,Toast.LENGTH_LONG).show()
        }
        else {
            Toast.makeText(baseContext,"Please enter the records first",Toast.LENGTH_LONG).show()
        }
    }

    public fun select(view: View) {
        var values = ContentValues()
        val URL = "content://com.example.finalproject/contacts"
        var friends = Uri.parse(URL)
        var nam=name.text.toString()
        var phno=phno.text.toString()
        var ino=idup.text.toString()
        var c = contentResolver.query(friends,null,null,null,"name")
        var result = "Content Provider Results:"
        if(!c.moveToFirst()) {
            Toast.makeText(this,result+"no content yet!",Toast.LENGTH_LONG).show()
        }
        else {
            do {
                if ((c.getString(c.getColumnIndex(CustomContentProvider.NAME))== nam) || (c.getString(c.getColumnIndex(CustomContentProvider.PH_NO)) == phno)
                    || (c.getString(c.getColumnIndex(CustomContentProvider.ID)) == ino)) {
                    result += "\n NAME:${c.getString(c.getColumnIndex(CustomContentProvider.NAME))} \n ID: ${c.getString(
                        (c.getColumnIndex(
                            CustomContentProvider.ID
                        ))
                    )} PH NO:${c.getString(c.getColumnIndex(CustomContentProvider.PH_NO))}"
                }
            }while(c.moveToNext())
            if(!result.isEmpty())
                Toast.makeText(this,result,Toast.LENGTH_LONG).show()
            else
                Toast.makeText(this,"No Records Present",Toast.LENGTH_LONG).show()
        }

    }

    @SuppressLint("MissingPermission")
    public fun sendsms() {
        val URL = "content://com.example.finalproject/contacts"
        var friends = Uri.parse(URL)
        var result: String?=null
        var n1:String?=null
        var p1:String?=null
        var myarray=HashMap<String,String>()
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        hasGps = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        var c = contentResolver.query(friends,null,null,null,"name")
        var obj = SmsManager.getDefault()
        if(!c.moveToFirst()) {
            Toast.makeText(this,result+"no content yet!",Toast.LENGTH_LONG).show()
        }
        else {
            do {
                n1 = "${c.getString(c.getColumnIndex(CustomContentProvider.NAME))}"
                p1 = "${c.getString(c.getColumnIndex(CustomContentProvider.PH_NO))}"
                myarray.put(n1, p1)
            } while (c.moveToNext())
                if (hasGps) {
                        Log.d("AndroidLocation", "hasGps")
                        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 2000, 0F, object : LocationListener {
                            override fun onLocationChanged(location: Location?) {
                                if (location != null) {
                                    locationGps = location
                                    for ( x in myarray.keys) {
                                 var str = "Hey $x Help me My location is"
                                    str+="\n Latitude : "+locationGps!!.latitude
                                    str+="\n Longitude : "+locationGps!!.longitude
                                    obj.sendTextMessage("${myarray.get(x)}",null,"$str",null,null)
                                  }
                                    Log.d("AndroidLocation", " GPS Latitude : " + locationGps!!.latitude)
                                    Log.d("AndroidLocation", " GPS Longitude : " + locationGps!!.longitude)
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

                }
                else
                {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
        }
    }

    public fun serv() {
        val intent = Intent(this,MyService::class.java)
        var alist = ArrayList<String>()
        val blist = ArrayList<String>()
        var result: String?=null
        var n1:String?=null
        var p1:String?=null
        var myarray=HashMap<String,String>()
        val URL = "content://com.example.finalproject/contacts"
        var friends = Uri.parse(URL)
        var c = contentResolver.query(friends,null,null,null,"name")
        if(!c.moveToFirst()) {
            Toast.makeText(this,result+"no content yet!",Toast.LENGTH_LONG).show()
        }
        else {
            do {
                n1 = "${c.getString(c.getColumnIndex(CustomContentProvider.NAME))}"
                p1 = "${c.getString(c.getColumnIndex(CustomContentProvider.PH_NO))}"
                myarray.put(n1, p1)
            } while (c.moveToNext())
        }
        for( x in myarray.keys) {
            var str = "Hey $x Help me My location is"
            var ster = myarray.get(x).toString()
            alist.add(str)
            blist.add(ster)
        }
        intent.putExtra(myhash, alist)
        intent.putExtra(myhash1,blist)
        startService(intent)
    }

    private fun speak() {
        val mIntent=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        mIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
        try {
            startActivityForResult(mIntent,REQUEST_CODE_SPEECH_INPUT)

        }catch (e: Exception) {
            Toast.makeText(this,e.message,Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when(requestCode) {
            REQUEST_CODE_SPEECH_INPUT-> {
                if(resultCode == Activity.RESULT_OK && null != data) {
                    val result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                  val sq = result[0].trim()
                  val ss = sq.split("\\s".toRegex())
                    val i:Int
                    var myarray = ArrayList<String>()
                    var sss:String
                    for( i in 0..ss.size-1) {
                        sss = ss[i]
                        sss = sss.toLowerCase()
                        myarray.add(sss)
                    }
                    for(i in 0..myarray.size-1) {
                        if(myarray[i] == "help" || myarray[i] == "don't" || myarray[i] == "blah" || myarray[i]== "call" || myarray[i] == "bill" || myarray[i] == "well" || myarray[i] == "bobo" || myarray[i] == "bell" || myarray[i] == "bo" || myarray[i] == "bobob" || myarray[i] == "no" || myarray[i] == "nope") {
                            sendsms()
                            break
                        }
                    }
                }
            }
        }
    }
}
