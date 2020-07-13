package com.example.patrolisatpam

import android.content.Intent
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.patrolisatpam.retrofit.ApiUtils
import com.example.patrolisatpam.retrofit.insScan
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.*


class ScanResultActivity : AppCompatActivity() {
    private var locationManager : LocationManager? = null
    private var kode:String = ""
    private var waktu:String = ""
    private var lat:Double? = null
    private var long:Double? = null

    var tvHasil: TextView? = null
    var tvWaktu: TextView? = null
    var tvLokasi: TextView? = null
    var btnSimpan: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scan_result)
        val actionBar = supportActionBar
        actionBar!!.title = "Patroli Satpam - Hasil Scan"

        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager?

        tvHasil = findViewById(R.id.tvkodeBarcode) as TextView
        tvWaktu = findViewById(R.id.tvwaktuScan) as TextView
        tvLokasi = findViewById(R.id.tvlokasiScan) as TextView
        btnSimpan = findViewById(R.id.btnSimpanScan) as Button

        val hasil = intent.getStringExtra("hasil")
        val done = intent.getBooleanExtra("done", false)
        tvHasil!!.setText(hasil);
        if (done){
            tvWaktu!!.setText(intent.getStringExtra("waktu"));
            val lat = intent.getStringExtra("lat")
            val long = intent.getStringExtra("long")
            tvLokasi!!.setText("Lat "+lat+" Long "+long)
            btnSimpan!!.text = "Kembali"
            btnSimpan!!.setOnClickListener {
                finish()
            }
        }else{
            try {
                // Request location updates
                locationManager?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0L, 0f, locationListener)
            } catch(ex: SecurityException) {
                Toast.makeText(this, "Security Exception, no location available", Toast.LENGTH_LONG).show()
            }
            getLokasi(tvLokasi!!)// set text lokasi
            tvWaktu!!.setText(getTanggal());
            btnSimpan!!.setOnClickListener {
                if (kode == ""){
                    Toast.makeText(this, "Kode Barcode tidak ditemukan!", Toast.LENGTH_SHORT).show();
                }else if(waktu == ""){
                    Toast.makeText(this, "Waktu Kosong!", Toast.LENGTH_SHORT).show();
                }else if(lat.toString() == ""){
                    Toast.makeText(this, "Lokasi tidak ditemukan!", Toast.LENGTH_SHORT).show();
                }else{
                    insScan()
                }
            }
        }

        kode = hasil
        waktu = getTanggal()
    }

    fun buatSP(): SharedPreference{
        val spHelp = SharedPreference(this)
        return spHelp;
    }

    fun insScan(){
        val noind = buatSP().getValueString(buatSP().USERNYA);
        val ronde = buatSP().getValueString(buatSP().ROUND_BERAPA);
        val pos = buatSP().getValueString(buatSP().POS_BERAPA);
        var mAPIService: insScan? = null
        mAPIService = ApiUtils.insertScan
            mAPIService!!.dataPost(noind, lat.toString(), long.toString(), ronde, pos, waktu, kode).enqueue(object :
                Callback<ResponseBody> {
                override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                    try {
                        val outputJson = JSONObject(response.body()!!.string())
                        if (outputJson.getString("success").equals("true")){
                            Toast.makeText(this@ScanResultActivity, "Berhasil Input!", Toast.LENGTH_SHORT).show();
                            val i = Intent(this@ScanResultActivity, PertanyaanActivity::class.java)
                            i.putExtra("idPatroli", outputJson.getString("id").toInt())
                            startActivityForResult(i, 100)
                            finish()
                        }else{
                            Toast.makeText(this@ScanResultActivity, "Error!", Toast.LENGTH_LONG).show()
                        }
                    }catch (e :JSONException){
                        Toast.makeText(this@ScanResultActivity, "Error! "+e.toString(), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

                }
            })
        }

    fun getText(): TextView{
        val tvLokasi = findViewById(R.id.tvlokasiScan) as TextView
        return tvLokasi
    }
    //cara mendapatkan lokasi secara pendek
    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            lat = location.latitude
            long = location.longitude
            getText().text = ("Lat " + location.longitude + " Long " + location.latitude)
        }
        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    fun getTanggal(): String {
        val sdf = SimpleDateFormat("yyyy/MM/dd HH:mm:ss")
        val currentDate = sdf.format(Date())
        return currentDate;
    }

    fun getLokasi(tvLokasi: TextView){
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
                tvLokasi.setText("Lat : ${location?.latitude} Long : ${location?.longitude}");
                lat = location?.latitude
                long = location?.longitude
            }

        })
    }
}
/* refrensi
* location manager https://stackoverflow.com/questions/45958226/get-location-android-kotlin
* getLokasi https://medium.com/@rizal_hilman/mendapatkan-lokasi-koordinat-saat-ini-di-android-994d3eb555c6
* */

