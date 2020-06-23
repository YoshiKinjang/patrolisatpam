package com.example.patrolisatpam


import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.patrolisatpam.retrofit.APIService
import com.example.patrolisatpam.retrofit.ApiLogin
import com.google.maps.android.ui.IconGenerator
import okhttp3.ResponseBody
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Callback
import retrofit2.Response


class LoginActivity : AppCompatActivity() {
    var btnLogin: Button? = null;
    private var doubleBackToExitPressedOnce = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        cekLogin();
        buatSP()
        btnLogin = findViewById(R.id.btnLogin) as Button;
        val etUsername = findViewById(R.id.etUsername) as EditText;
        val etPassword = findViewById(R.id.etPassword) as EditText;


        btnLogin!!.setOnClickListener {
            val username = etUsername.text.toString();
            val password = etPassword.text.toString();
            if(username.trim().equals("")){
                etUsername.error = "Username tidak boleh kosong";
                etUsername.requestFocus();
            }else if(password.trim().equals("")){
                etPassword.error = "Password tidak boleh kosong";
                etPassword.requestFocus();
            }else{
                cobaLogin(username, password);
                btnLogin!!.isClickable = false;
            }
        }
    }

    fun buatSP(): SharedPreference{
        val spHelp = SharedPreference(this)
        return spHelp;
    }

    fun cekLogin()
    {
        val sudahLogin: Boolean = buatSP().getSudahLogin();
        if (sudahLogin) {
            val txt = buatSP().getValueString(buatSP().USERNYA);
            Toast.makeText(this, "Login Sebagai "+txt, Toast.LENGTH_SHORT).show();
            val inten = Intent(this, MainActivity::class.java);
            startActivity(inten);
            finish();
        }
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce) {
            finishAffinity()
        }
        this.doubleBackToExitPressedOnce = true
        Toast.makeText(this, "Please click BACK again to exit", Toast.LENGTH_SHORT).show()
        Handler().postDelayed(Runnable { doubleBackToExitPressedOnce = false }, 2000)
    }

    fun cobaLogin(username: String, password: String): String
    {
        var kembali = "";
        var mAPIService: APIService? = null

        //After oncreate
        mAPIService = ApiLogin.apiService

        //Some Button click
        mAPIService!!.registrationPost(username, password).enqueue(object :
            Callback<ResponseBody> {
           override fun onResponse(call: retrofit2.Call<ResponseBody>, response: Response<ResponseBody>) {
                try{
                    btnLogin!!.isClickable = true
                    val outputJson = JSONObject(response.body()?.string())
                    Log.i("JSONNYA", outputJson.getString("error"))
                    if (outputJson.getString("error").equals("false")){
                        kembali = "oke";
                    }else{
                        kembali = outputJson.getString("error_msg")
                    }
                }catch (e: JSONException) {
                    Log.e("JSON Parser", "Error parsing data [" + e.message+"] ");
                    kembali = "Login Gagal"
                }
                if (response.isSuccessful()) {
                    Log.i("", "post registration to API" + response.body()!!.toString())
                }
                Log.i("APALAH","WTF")
               if(kembali.equals("oke")){
                   Toast.makeText(baseContext,"Berhasil Login",Toast.LENGTH_SHORT).show();
                   val inten = Intent(baseContext,MainActivity::class.java)
                   buatSP().saveSPBool(buatSP().SUDAH_LOGIN, true);
                   buatSP().saveSPString(buatSP().USERNYA, username.toUpperCase());
                   startActivity(inten);
               }else{
                   Toast.makeText(baseContext,kembali,Toast.LENGTH_SHORT).show();
               }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
        Log.i("KEMBALI", kembali);
        return kembali;
    }
}

/* Refrensi
* Retrofit https://medium.com/binar-academy/menggunakan-retrofit-http-client-untuk-pembuatan-aplikasi-android-dengan-bahasa-kotlin-10c3b8601fca
* retrofit https://stackoverflow.com/questions/48914516/kotlin-retrofit-simple-post-without-response
* */
