package com.example.patrolisatpam

import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.patrolisatpam.retrofit.ApiUtils
import com.example.patrolisatpam.retrofit.insScan
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import kotlinx.android.synthetic.main.activity_tidak_scan.*
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*

class TidakScanActivity : AppCompatActivity() {
    private var waktu:String = ""
    private var lat:Double? = null
    private var long:Double? = null
    private var lokasi:String = ""
    private var locationManager : LocationManager? = null

    private  var tvWaktu: TextView? = null
    private var tvLokasi: TextView? = null
    private var tvKoordinat: TextView? = null
    private var btnSimpan: Button? = null
    var sp:SharedPreference? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tidak_scan)
        val actionBar = supportActionBar
        actionBar!!.title = "Tidak bisa Scan"
        actionBar!!.setDisplayHomeAsUpEnabled(true);

        tvWaktu = findViewById(R.id.tvCWaktu) as TextView
        tvLokasi = findViewById(R.id.tvCLokasi) as TextView
        tvKoordinat = findViewById(R.id.tvCKoordinat) as TextView
        btnSimpan = findViewById(R.id.btConfirm) as Button
        sp = SharedPreference(this)
        val pos = sp!!.getValueString(sp!!.POS_BERAPA);
        waktu = getTanggal();
        tvWaktu!!.text = waktu
        lokasi = intent.getStringExtra("tempat");
        tvLokasi!!.text = "Pos "+pos+"\n"+lokasi

        btNoscanBack.setOnClickListener {
            finish();
        }

        btnSimpan!!.setOnClickListener {
            saveData()
        }
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?
        try {
            // Request location updates
            locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
        } catch(ex: SecurityException) {
            Toast.makeText(this, "Security Exception, no location available", Toast.LENGTH_LONG).show()
        }

        getLokasi()
    }

    fun getTanggal(): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd hh:mm:ss")
        val currentDate = sdf.format(Date())
        return currentDate;
    }

    fun getLokasi(){
        // GET MY CURRENT LOCATION
        val contex = this;
        val mFusedLocation = LocationServices.getFusedLocationProviderClient(this)
        mFusedLocation.lastLocation.addOnSuccessListener(this, object :
            OnSuccessListener<Location> {
            override fun onSuccess(location: Location?) {
                // Do it all with location
                Log.d("My Current location", "Lat : ${location?.latitude} Long : ${location?.longitude}")
                // Display in Toast
                Toast.makeText(contex, "Lat : ${location?.latitude} Long : ${location?.longitude}",
                    Toast.LENGTH_LONG).show()
                tvKoordinat!!.setText("Lat : ${location?.latitude} Long : ${location?.longitude}");
                lat = location?.latitude
                long = location?.longitude
            }

        })
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lat = location.latitude
            long = location.longitude
            tvKoordinat!!.text = ("Lat " + location.longitude + " Long " + location.latitude)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun saveData() {
        val noind = sp!!.getValueString(sp!!.USERNYA);
        val ronde = sp!!.getValueString(sp!!.ROUND_BERAPA);
        val pos = sp!!.getValueString(sp!!.POS_BERAPA);
        var mAPIService: insScan? = null
        mAPIService = ApiUtils.insertScan
        mAPIService!!.dataPost(noind, lat.toString(), long.toString(), ronde, pos, waktu, "Tidak Scan").enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val outputJson = JSONObject(response.body()!!.string())
                    if (outputJson.getString("success").equals("true")){
                        Toast.makeText(this@TidakScanActivity, "Berhasil Input!", Toast.LENGTH_SHORT).show();
                        val i = Intent(this@TidakScanActivity, PertanyaanActivity::class.java)
                        i.putExtra("idPatroli", outputJson.getString("id").toInt())
                        i.putExtra("hideNoTemuan",true)
                        startActivityForResult(i, 100)
                        finish()
                    }else{
                        Toast.makeText(this@TidakScanActivity, "Error!", Toast.LENGTH_LONG).show()
                    }
                }catch (e : JSONException){
                    Toast.makeText(this@TidakScanActivity, "Error! "+e.toString(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home ->
                finish()
        }
        return true
    }
}
