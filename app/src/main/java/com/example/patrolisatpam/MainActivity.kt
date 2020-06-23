package com.example.patrolisatpam

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.location.LocationManager
import android.os.Bundle
import android.os.SystemClock
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.patrolisatpam.`object`.Round
import com.example.patrolisatpam.retrofit.ApiUtils
import com.example.patrolisatpam.retrofit.getProfile
import com.example.patrolisatpam.retrofit.getRonde
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_gridview_main.view.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    var adapter: MenuAdapter? = null
    var menusList = ArrayList<Round>()
    var sp:SharedPreference? = null
    var noindT: String? = null
    var namaT: String? = null
    var urlprofile: String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        sp = SharedPreference(this)
        statusCheck()
        getInfoPekerja()
        getListRonde()

        ivProfile.setOnClickListener {
            if (noindT != null){
                intProfile()
            }
        }
        tvNama.setOnClickListener {
            if (noindT != null){
                intProfile()
            }
        }
    }

    fun getListRonde(refresh:Boolean = true){
        var mAPIService: getRonde? = null
        mAPIService = ApiUtils.getRound
        mAPIService.ronde().enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val ob = JSONObject(response.body()!!.string())
                    val max = ob.getString("max_ronde")
                    val arr = JSONArray(ob.getString("ronde"))
                    for (x in 1..max.toInt()){
                        menusList.add(Round(x,0))
                    }
                    for (i in 0 until arr.length()) {
                        val ronde = arr.getJSONObject(i).getString("ronde").toInt()
                        val selesai = arr.getJSONObject(i).getString("selesai").toInt()
                        menusList.set(ronde-1, Round(ronde, selesai))
                    }
                    adapter = MenuAdapter(this@MainActivity, menusList)
                    if(refresh)
                    gridview.adapter = adapter
//                    for(x in 0 .. menusList.size-1){
//                        Log.d("BESAR", menusList.get(x).angka.toString())
//                        Log.d("BESAR2", menusList.get(x).status.toString())
//                    }
                }catch (e: JSONException){
                    Log.e("ERor", e.toString())
                }

            }
            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun intProfile(){
        val i = Intent(this, ProfileActivity::class.java)
        i.putExtra("noind", noindT)
        i.putExtra("nama", namaT)
        i.putExtra("url", urlprofile)
        startActivity(i)
    }

    fun getInfoPekerja(){
        val noind = sp!!.getValueString(sp!!.USERNYA)
        var mAPIService: getProfile? = null
        mAPIService = ApiUtils.getInfoProfile
        mAPIService.valuegp(noind).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val ob = JSONArray(response.body()!!.string()).getJSONObject(0)
                    Picasso.get()
                        .load(ob.getString("path_photo"))
                        .into(ivProfile)
                    val nama = ob.getString("nama");
                    val noind = ob.getString("noind");
                    tvNama.setText(noind+" - "+nama)
                    noindT = noind
                    namaT = nama
                    urlprofile = ob.getString("path_photo")
                }catch (e: JSONException){
                    Log.e("ERore", e.toString())
                }

            }
            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    override fun onBackPressed() {
        val logout = {dialog: DialogInterface, which: Int ->
            logout()
        }
        val keluar = {dialog: DialogInterface, which: Int ->
            keluar()
        }
        val builder = AlertDialog.Builder(this, R.style.AlertDialog)
        with(builder) {
            setTitle("Keluar")
            setMessage("Anda ingin Keluar?")
            setNeutralButton(" ", null)
            setPositiveButton("Keluar", DialogInterface.OnClickListener(function = keluar))
            setNegativeButton("Cancel", null)
            setIcon(resources.getDrawable(android.R.drawable.ic_dialog_alert))
        }
        val alertDialog = builder.create()
        alertDialog.show()

        val buttonPos = alertDialog.getButton(DialogInterface.BUTTON_POSITIVE)
        val buttonNeg = alertDialog.getButton(DialogInterface.BUTTON_NEGATIVE)
        val buttonNeu = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL)
        with(buttonPos) {
                setTextColor(Color.DKGRAY)
        }
        with(buttonNeg) {
            setTextColor(Color.DKGRAY)
        }
        with(buttonNeu) {
            setTextColor(Color.DKGRAY)
        }
    }

    fun keluar(){
        finishAffinity();
    }

    fun logout(){
        val sP = SharedPreference(this);
        sP.saveSPBool(sP.SUDAH_LOGIN, false)
        val inten = Intent(this, LoginActivity::class.java)
        Toast.makeText(this, "Berhasil Logout", Toast.LENGTH_SHORT).show();
        startActivity(inten);
    }

    class MenuAdapter : BaseAdapter {
        var menusList = ArrayList<Round>()
        var context: Context? = null
        constructor(context: Context, menusList: ArrayList<Round>) : super() {
            this.context = context
            this.menusList = menusList
        }
        override fun getCount(): Int {
            return menusList.size
        }
        override fun getItem(position: Int): Any {
            return menusList[position]
        }
        override fun getItemId(position: Int): Long {
            return position.toLong()
        }

        private var mLastClickTime: Long = 0
        override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
            val menu = this.menusList[position]
            val inflator = context!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val menuView = inflator.inflate(R.layout.layout_gridview_main, parent, false)
            menuView.tvAngka.text = menu.angka!!.toString()
            if (menu.status == 1){
                menuView.cvGrid.backgroundTintList = ContextCompat.getColorStateList(context!!, R.color.ijo_muda)
                menuView.cvGrid.setOnClickListener {
                    Toast.makeText(context, "Round ini telah selesai!!", Toast.LENGTH_SHORT).show()
                }
            }else{
                menuView.cvGrid.setOnClickListener {
                    menuView.cvGrid.isClickable = false
                    if (SystemClock.elapsedRealtime() - mLastClickTime < 1000) {
                        //do nothing
                    }else{
//                    val inten = Intent(context, PetaActivity::class.java)
                        val inten = Intent(context, MapActivity::class.java)
                        val angka = menu.angka;
                        inten.putExtra("round", angka)
                        buatSP().saveSPString(buatSP().ROUND_BERAPA, angka!!.toString());
                        context!!.startActivity(inten);
                        menuView.cvGrid.isClickable = true
                    }
                    mLastClickTime = SystemClock.elapsedRealtime();
                }
            }

            return menuView
        }
        fun buatSP(): SharedPreference{
            val spHelp = SharedPreference(context!!)
            return spHelp;
        }
    }

    fun statusCheck() {
        val manager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!manager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }
    }

    private fun buildAlertMessageNoGps() {
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes",
                { dialog, id -> startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) })
            .setNegativeButton("No",
                { dialog, id -> dialog.cancel() })
        val alert: AlertDialog = builder.create()
        alert.show()
    }

    override fun onRestart() {
        super.onRestart()
        menusList.clear()
        getListRonde()
    }
}

/* Refrensi
* Gridview Adapter https://grokonez.com/android/kotlin-gridview-example-show-list-of-items-on-grid-android
* */