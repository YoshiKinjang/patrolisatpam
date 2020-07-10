package com.example.patrolisatpam

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.patrolisatpam.retrofit.ApiUtils
import com.example.patrolisatpam.retrofit.getQr
import com.google.zxing.Result
import kotlinx.android.synthetic.main.activity_scan.*
import me.dm7.barcodescanner.zxing.ZXingScannerView
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class ScanActivity : AppCompatActivity(), ZXingScannerView.ResultHandler {
    private var mScannerView: ZXingScannerView? = null
    var kode: String = "";
    var sp:SharedPreference? = null

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionBar = supportActionBar
        actionBar!!.title = "Patroli Satpam - Scan Barcode"
        actionBar!!.setDisplayHomeAsUpEnabled(true);
        setContentView(R.layout.activity_scan);
//        mScannerView = ZXingScannerView(this)
        mScannerView = findViewById(R.id.zxscan)
        sp = SharedPreference(this)
//        setContentView(mScannerView);
        getKode();
        btn_flash.setOnClickListener {
//            mScannerView!!.flash = true
            mScannerView!!.toggleFlash()
        }
    }

    fun getKode(){
        val id = sp!!.getValueString(sp!!.POS_BERAPA)?.toInt()
        var mAPIService: getQr? = null
        mAPIService = ApiUtils.getQrcode
        mAPIService!!.idPatroli(id).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try{
                    val obj = JSONObject(response.body()!!.string())
                    kode = obj.getString("kode")
                    Log.d("Kode QR", kode)
                }catch (e : JSONException){
                    Toast.makeText(this@ScanActivity, "Error! "+e.toString(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    public override fun onResume() {
        super.onResume()
        mScannerView!!.setResultHandler(this) // Register ourselves as a handler for scan results.
        mScannerView!!.startCamera() // Start camera on resume
    }

    public override fun onPause() {
        super.onPause()
        mScannerView!!.stopCamera() // Stop camera on pause
    }

    override fun handleResult(rawResult: Result?) {
        //Toast.makeText(this, rawResult?.text, Toast.LENGTH_LONG).show();
        Log.d("Hais Scan", rawResult!!.text)
        Log.d("Hais Scan 2", kode)
        Log.d("Hais Scan 3", (rawResult?.text == kode).toString())
        setResult(RESULT_OK, Intent());
        if (rawResult?.text == kode){
            val i = Intent(this, ScanResultActivity::class.java)
            i.putExtra("hasil", rawResult?.text)
            i.putExtra("done", false)
            startActivity(i)
            finish()
        }else{
            Toast.makeText(this, "Kode Salah", Toast.LENGTH_SHORT).show();
            val i = Intent(this, WrongActivity::class.java)
            startActivity(i)
            finish()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home ->
                finish()
        }
        return true
    }
}
