package com.example.weatherforecast

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.*
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import kotlin.math.ceil


class MainActivity : AppCompatActivity() {

    val API_KEY = "078e57815980b17a2701f6d7ab0c0b52"
    lateinit var mfusedlocation:FusedLocationProviderClient
    private var myrequestcode=1010

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mfusedlocation = LocationServices.getFusedLocationProviderClient(this)
        getLastLocation()

        cityFinder.setOnClickListener{
            val intent = Intent(this@MainActivity, cityFinder::class.java)
            startActivity(intent)
        }
    }
    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if(chekPermission()){
            if(locationEnable()){
                mfusedlocation.lastLocation.addOnCompleteListener { task->
                    var location:Location?=task.result
                    if(location==null){
                        newLocation()
                    }else{
                        val lat = location.latitude.toString()
                        val longi = location.longitude.toString()
                        getJsonData(lat, longi)

                    }
                }
            }else{
                Toast.makeText(this, "Please Turn on your GPS location", Toast.LENGTH_LONG).show()
            }
        }else{
            requestPermission()
        }
    }

    private fun getJsonData(lat: String?, longi: String?) {
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${longi}&appid=${API_KEY}"
        val jsonRequest = JsonObjectRequest(
            Request.Method.GET,
            url,
            null,
            Response.Listener { response ->
                setValues(response)
            },
            Response.ErrorListener { Toast.makeText(this, "ERROR", Toast.LENGTH_LONG).show() })

        queue.add(jsonRequest)
    }

    private fun setValues(response: JSONObject?) {
        if(response!=null){
            cityName.text=response.getString("name")
            weatherCondition.text=response.getJSONArray("weather").getJSONObject(0).getString("main")
            var tempr=response.getJSONObject("main").getString("temp")
            tempr=((((tempr).toFloat()-273.15)).toInt()).toString()
            temperature.text="Today : "+"${tempr}°C"
            var mintemp=response.getJSONObject("main").getString("temp_min")
            mintemp=((((mintemp).toFloat()-273.15)).toInt()).toString()
            //min_temp.text="Min : "+mintemp+"°C"
            var maxtemp=response.getJSONObject("main").getString("temp_max")
            maxtemp=((ceil((maxtemp).toFloat() - 273.15)).toInt()).toString()
            minTemp.text="Min : "+mintemp+"°C"+"                     "+"Max : "+maxtemp+"°C"
            var humidi=response.getJSONObject("main").getString("humidity")
            humidi=((((humidi).toFloat()-15.00)).toInt()).toString()
            humidity.text="Humidity : "+"${humidi}%"
            windspeed.text="Wind Speed : "+response.getJSONObject("wind").getString("speed")+"km/hr"
            degree.text="Degree : "+response.getJSONObject("wind").getString("deg")+"°"
            val condition=response.getJSONArray("weather").getJSONObject(0).getInt("id")
            if(condition>=0 && condition<=300){
                weatherIcon.setImageResource(R.drawable.thunderstorm1)
            }
            else if(condition>=300 && condition<=500)
            {
                weatherIcon.setImageResource(R.drawable.lightrain)
            }
            else if(condition>=500 && condition<=600)
            {
                weatherIcon.setImageResource(R.drawable.shower)
            }
            else  if(condition>=600 && condition<=700)
            {
                weatherIcon.setImageResource(R.drawable.snow2)
            }
            else if(condition>=701 && condition<=771)
            {
                weatherIcon.setImageResource(R.drawable.fog)
            }

            else if(condition>=772 && condition<=800)
            {
                weatherIcon.setImageResource(R.drawable.overcast)
            }
            else if(condition==800)
            {
                weatherIcon.setImageResource(R.drawable.sunny)
            }
            else if(condition>=801 && condition<=804)
            {
                weatherIcon.setImageResource(R.drawable.cloudy)
            }
            else  if(condition>=900 && condition<=902)
            {
                weatherIcon.setImageResource(R.drawable.thunderstorm1)
            }
            if(condition==903)
            {
                weatherIcon.setImageResource(R.drawable.snow1)
            }
            if(condition==904)
            {
                weatherIcon.setImageResource(R.drawable.sunny)
            }
            if(condition>=905 && condition<=1000)
            {
                weatherIcon.setImageResource(R.drawable.thunderstorm2)
            }

        }
    }

    @SuppressLint("MissingPermission")
    private fun newLocation() {
        var locationRequest=LocationRequest()
        locationRequest.priority=LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval=0
        locationRequest.fastestInterval=0
        locationRequest.numUpdates=1
        mfusedlocation=LocationServices.getFusedLocationProviderClient(this)
        mfusedlocation.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper())

    }

    private val locationCallback=object:LocationCallback(){
        override fun onLocationResult(p0: LocationResult) {
            var lastLocation:Location=p0.lastLocation
        }
    }

    private fun locationEnable(): Boolean {
        var locationManager=getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this, arrayOf(
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            ), myrequestcode
        )
    }

    private fun chekPermission(): Boolean {
        if((ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            )==PackageManager.PERMISSION_GRANTED) || (ActivityCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_COARSE_LOCATION
            )==PackageManager.PERMISSION_GRANTED)){
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode==myrequestcode){
            if(grantResults.isNotEmpty() && grantResults[0]==PackageManager.PERMISSION_GRANTED){
                getLastLocation()
            }
        }
    }
}