package com.example.patrolisatpam

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.patrolisatpam.retrofit.ApiUtils
import com.example.patrolisatpam.retrofit.cekPertanyaan
import com.example.patrolisatpam.retrofit.cekPos
import com.example.patrolisatpam.retrofit.cekTemuan
import kotlinx.android.synthetic.main.activity_pos.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class PosActivity : AppCompatActivity() {

    var cvScan: CardView? = null
    var cvpertanyaan: CardView? = null
    var cvTemuan: CardView? = null
    var sp:SharedPreference? = null
    var ll:LinearLayout? = null
    var pertanyaan:Boolean = false
    var scan:Boolean = false
    var idPatroli:Int? = null
    var btCantScan: Button? = null
    var hideNoTemuan: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pos)

        ll = findViewById<View>(R.id.ll_posItem) as LinearLayout
        ll!!.visibility = View.GONE
        pbPos.visibility = View.VISIBLE

        sp = SharedPreference(this)
        val angka = intent.getStringExtra("pos")
        val tempat = intent.getStringExtra("tempat")
        val actionBar = supportActionBar
        actionBar!!.title = "Patroli Satpam - Pos "+angka
        actionBar!!.setDisplayHomeAsUpEnabled(true);

        val tvPos = findViewById(R.id.tvPosnum) as TextView;
        cvScan = findViewById(R.id.cvScan) as CardView;
        cvpertanyaan = findViewById(R.id.cvPertanyaan) as CardView;
        cvTemuan = findViewById(R.id.cvTemuan) as CardView;
        btCantScan = findViewById(R.id.btCantScan) as Button;
        val btKembali = findViewById(R.id.btPosback) as Button

        tvPos.setText("Pos "+angka);
        tvPosnumSub.setText(tempat)

        btKembali.setOnClickListener {
            setResult(Activity.RESULT_OK, Intent())
            finish();
        }

        btCantScan!!.setOnClickListener {
            val i = Intent(this, TidakScanActivity::class.java)
            i.putExtra("tempat", tempat)
            startActivity(i)
        }

        cvpertanyaan!!.setOnClickListener {
            if (scan){
                val i = Intent(this, PertanyaanActivity::class.java)
                i.putExtra("idPatroli", idPatroli)
                startActivityForResult(i, 100)
            }else{
                Toast.makeText(this,"Harap Melakukan Scan dahulu!!", Toast.LENGTH_SHORT).show()
            }
        }

        cvTemuan!!.setOnClickListener {
            if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
            }else if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            }else {
                if (scan){
                    val i = Intent(this, TemuanActivity::class.java)
                    i.putExtra("idPatroli", idPatroli)
                    i.putExtra("hideNoTemuan", hideNoTemuan)
                    startActivityForResult(i, 100)
                }else{
                    Toast.makeText(this,"Harap Melakukan Scan dahulu!!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        getCekPos()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_OK, Intent())
        finish()
    }

    fun camera()
    {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION), 0)
        }else if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }else if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 0)
        }else if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 0)
        }
        else{
            val i = Intent(this, ScanActivity::class.java)
            i.putExtra("id", idPatroli)
            startActivityForResult(i,100);
        }
    }

    fun getCekPos(){
        val noind = sp!!.getValueString(sp!!.USERNYA)
        val round = sp!!.getValueString(sp!!.ROUND_BERAPA)
        val pos = sp!!.getValueString(sp!!.POS_BERAPA)
        var mAPIService: cekPos? = null
        mAPIService = ApiUtils.checkPos
        mAPIService!!.cekAktivityPos(noind, round, pos).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val St = response.body()!!.string()
                    val ol = JSONArray(St)
                    if (ol.length() != 0){
                        ivCvscan.setColorFilter(ContextCompat.getColor(this@PosActivity, R.color.putih), android.graphics.PorterDuff.Mode.SRC_IN);
                        tvCvscan.setTextColor(Color.WHITE)
                        scan = true
                        val ob = JSONArray(St).getJSONObject(0)
                        idPatroli = ob.getString("id_patroli").toInt()
                        cvScan!!.backgroundTintList = ContextCompat.getColorStateList(this@PosActivity, R.color.ijo_muda)
                        btCantScan!!.visibility = View.GONE
                        cvScan!!.setOnClickListener {
                            val i = Intent(this@PosActivity, ScanResultActivity::class.java)
                            i.putExtra("done", true)
                            i.putExtra("noind", ob.getString("noind"))
                            i.putExtra("hasil", ob.getString("kode"))
                            i.putExtra("waktu", ob.getString("tgl_patroli"))
                            i.putExtra("lat", ob.getString("latitude"))
                            i.putExtra("long", ob.getString("longitude"))
                            startActivity(i);
                        }
                        if (ob.getString("kode").equals("Tidak Scan", true))
                            hideNoTemuan = true

                        this@PosActivity.getCekTemuan()
                        this@PosActivity.getCekPertanyaan()
                    }else{
                        pbPos.visibility = View.GONE
                        ll!!.visibility = View.VISIBLE
                        cvScan!!.setOnClickListener {
                            camera()
                        }
                    }
                }catch (e : JSONException){
                    Toast.makeText(this@PosActivity, "Error! "+e.toString(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun getCekPertanyaan(){
        var mAPIService: cekPertanyaan? = null
        mAPIService = ApiUtils.checkPertanyaan
        mAPIService!!.cekAktivityPertanyaan(idPatroli).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val St = response.body()!!.string()
                    val ol = JSONArray(St)
                    if (ol.length() != 0){
                        ivCvask.setColorFilter(ContextCompat.getColor(this@PosActivity, R.color.putih), android.graphics.PorterDuff.Mode.SRC_IN);
                        tvCvask.setTextColor(Color.WHITE)
                        cvpertanyaan!!.backgroundTintList = ContextCompat.getColorStateList(this@PosActivity, R.color.ijo_muda)
                        cvpertanyaan!!.setOnClickListener {
                            val i = Intent(this@PosActivity, PertanyaanActivity::class.java)
                            i.putExtra("done", true)
                            i.putExtra("idPatroli", idPatroli)
                            startActivity(i)
                        }
                    }
                }catch (e : JSONException){
                    Toast.makeText(this@PosActivity, "Error! "+e.toString(), Toast.LENGTH_LONG).show()
                }
                ll!!.visibility = View.VISIBLE
                pbPos.visibility = View.GONE
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun getCekTemuan(){
        var mAPIService: cekTemuan? = null
        mAPIService = ApiUtils.checkTemuan
        mAPIService!!.cekAktivityTemuan(idPatroli).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val St = response.body()!!.string()
                    val ol = JSONArray(St)
                    if (ol.length() != 0){
                        ivCvfound.setColorFilter(ContextCompat.getColor(this@PosActivity, R.color.putih), android.graphics.PorterDuff.Mode.SRC_IN);
                        tvCvfound.setTextColor(Color.WHITE)
                        cvTemuan!!.backgroundTintList = ContextCompat.getColorStateList(this@PosActivity, R.color.ijo_muda)
                        val ob = JSONArray(St).getJSONObject(0)
                        cvTemuan!!.setOnClickListener {
                            val i = Intent(this@PosActivity, TemuanActivity::class.java)
                            i.putExtra("done", true)
                            i.putExtra("id_patroli", idPatroli)
                            i.putExtra("isi", ob.getString("deskripsi"))
                            i.putExtra("lat", ob.getString("latitude"))
                            i.putExtra("long", ob.getString("longitude"))
                            startActivity(i)
                        }
                    }
                }catch (e : JSONException){
                    Toast.makeText(this@PosActivity, "Error! "+e.toString(), Toast.LENGTH_LONG).show()
                }
                ll!!.visibility = View.VISIBLE
                pbPos.visibility = View.GONE
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        imageReturnedIntent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            100 -> if (resultCode == Activity.RESULT_OK) {
                finish();
                startActivity(getIntent());
                setResult(Activity.RESULT_OK, Intent())
            }
            1 -> if (resultCode == Activity.RESULT_OK) {

            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        finish();
        startActivity(getIntent());
        setResult(Activity.RESULT_OK, Intent())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home ->
                finish()
        }
        return true
    }
}
