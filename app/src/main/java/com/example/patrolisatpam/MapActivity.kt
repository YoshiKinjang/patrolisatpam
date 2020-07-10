package com.example.patrolisatpam

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.patrolisatpam.`object`.markerz
import com.example.patrolisatpam.retrofit.ApiUtils
import com.example.patrolisatpam.retrofit.getPos
import com.example.patrolisatpam.retrofit.getRondeID
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    var markerList = ArrayList<markerz>()
    var angkaRound: Int = 1;
    var ggMap: GoogleMap? = null;
    var isComplete: Boolean = false
    var sp:SharedPreference? = null
    var actionBar:androidx.appcompat.app.ActionBar? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)
        angkaRound = intent.getIntExtra("round", 0)
        actionBar = supportActionBar
        actionBar!!.title = "Patroli Satpam - Round "+angkaRound
        actionBar!!.subtitle = "Silahkan Memilih Pos"
        actionBar!!.setDisplayHomeAsUpEnabled(false);
        isComplete = false
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapPeta) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)
        sp = SharedPreference(this)
        val btnHelp = findViewById(R.id.btnHelpMap) as Button
        btnHelp.setOnClickListener {
            dialog()
        }
//        cekRondeIni()
    }

    fun dialog(){
        val builder = AlertDialog.Builder(this, R.style.AlertDialog)
        val inflater:LayoutInflater = getLayoutInflater();
        with(builder) {
            setTitle(" ")
            setView(inflater.inflate(R.layout.layout_bantuan_map, null))
            setNeutralButton(" ", null)
        }
        val alertDialog = builder.create()
        alertDialog.show()
    }

    fun buatSP(): SharedPreference{
        val spHelp = SharedPreference(this)
        return spHelp;
    }

    override fun onMapReady(googleMap: GoogleMap) {
        ggMap = googleMap;
        googleMap.mapType = GoogleMap.MAP_TYPE_SATELLITE
        Log.d("MAEKERZ", markerList.size.toString())
        daftarListPos()

        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(-7.775335, 110.362575), 17.5f))
        googleMap.setOnInfoWindowClickListener {arg0 ->
            Toast.makeText(this, arg0.title, Toast.LENGTH_SHORT).show()
            val i = Intent(this, PosActivity::class.java)
            val txt = arg0.title.replace("Pos ", "")
            val sub = arg0.snippet
            buatSP().saveSPString(buatSP().POS_BERAPA, txt);
            i.putExtra("tempat", sub)
            i.putExtra("pos", txt)
            startActivityForResult(i, 100)
        }

        googleMap.setInfoWindowAdapter(object : GoogleMap.InfoWindowAdapter {
            override fun getInfoWindow(arg0: Marker): View? {
                return null
            }

            override fun getInfoContents(marker: Marker): View {
                val info = LinearLayout(this@MapActivity)
                info.orientation = LinearLayout.VERTICAL
                val title = TextView(this@MapActivity)
                title.setTextColor(Color.BLACK)
                title.gravity = Gravity.CENTER
                title.setTypeface(null, Typeface.BOLD)
                title.text = marker.title
                val snippet = TextView(this@MapActivity)
                snippet.setTextColor(Color.GRAY)
                snippet.text = marker.snippet
                snippet.gravity = Gravity.CENTER
                info.addView(title)
                info.addView(snippet)
                return info
            }
        })
    }

    fun daftarListPos(){
        isComplete = false
//        cekRondeIni()// cek juga hehe
        ggMap!!.clear()
        markerList.clear()
        var mAPIService: getPos? =  null
        //After oncreate
        mAPIService = ApiUtils.getListPos
        //Some Button click
        mAPIService.registrationGet(buatSP().getValueString(buatSP().USERNYA), angkaRound).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val St = response.body()!!.string()
                    val ob = JSONArray(St)
                    val len:Int = ob.length()
                    var len2 = 0;
                    var len4 = 0;
                    for (i in 0 until ob.length()) {
                        val obj: JSONObject = ob.getJSONObject(i)
                        val lok: String = obj.getString("lokasi")
                        val id: String = obj.getString("id")
                        val lat: Double = obj.getString("latitude").toDouble()
                        val long: Double = obj.getString("longitude").toDouble()
                        val status: Int = obj.getString("status").toInt()
                        if (status==2){
                            markerList.add(markerz(lat, long,"Pos "+id, lok, BitmapDescriptorFactory.HUE_GREEN))
                            len2++;
                        }else if (status == 1){
                            markerList.add(markerz(lat, long,"Pos "+id, lok, BitmapDescriptorFactory.HUE_YELLOW))
                        }else{
                            markerList.add(markerz(lat, long,"Pos "+id, lok, BitmapDescriptorFactory.HUE_RED))
                            len4++;
                        }
                    }
                    //intinya kalau hijau semua atau merah semua bisa kembali
                    if (len == len2 || len == len4) isComplete = true
                    else isComplete = false
                    for(x in 0..markerList.size-1){
                        ggMap!!.addMarker(
                            MarkerOptions()
                                .position(LatLng(markerList.get(x).lang!!, markerList.get(x).long!!))
                                .anchor(0.5f, 0.5f)
                                .title(markerList.get(x).title!!)
                                .snippet(markerList.get(x).snipset!!)
                                .icon(BitmapDescriptorFactory.defaultMarker(markerList.get(x).icon!!))
                        )
                    }
                }catch (e: JSONException){
                    Log.e("LOKASINYA", e.message.toString())
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun cekRondeIni(){
        val rond = sp!!.getValueString(sp!!.ROUND_BERAPA)!!.toInt()
        var mAPIService: getRondeID? =  null
        //After oncreate
        mAPIService = ApiUtils.getRoundId
        //Some Button click
        mAPIService.ronde(rond).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val St = response.body()!!.string()
                    val ob = JSONObject(St).getString("selesai")
                    Log.d("SESELUM", ob.toString())
                    if (ob.equals("1")){
                        isComplete = true
                        actionBar!!.setDisplayHomeAsUpEnabled(true);
                    }else{
                        isComplete = false
                    }

                }catch (e: JSONException){
                    Log.e("LOKASINYA", e.message.toString())
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun showAlert(){
        val builder = AlertDialog.Builder(this, R.style.AlertDialog)
        val inflater: LayoutInflater = getLayoutInflater();
        val layout = inflater.inflate(R.layout.layout_pilih_item, null)
        with(builder) {
            setTitle("Anda tidak bisa kembali!")
            setMessage("Masih ada pos yang terlewati!")
            setPositiveButton("Mengerti", null)
        }
        val alertDialog = builder.create()
        alertDialog.show()
        val buttonNeu = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        with(buttonNeu) {
            setTextColor(Color.DKGRAY)
        }
    }

    override fun onBackPressed() {
        if (isComplete){
            super.onBackPressed()
        }else{
            showAlert();
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        imageReturnedIntent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            100 -> if (resultCode == Activity.RESULT_OK) {
                daftarListPos()
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        daftarListPos()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home ->
                finish()
        }
        return true
    }
}

/* Refrensi
* Loop maker https://stackoverflow.com/questions/30569854/adding-multiple-markers-in-google-maps-api-v2-android
* change color maker https://stackoverflow.com/questions/16598169/changing-colour-of-markers-google-map-v2-android
* change action bar https://devofandroid.blogspot.com/2018/03/change-actionbar-title-of-activity.html
* */
