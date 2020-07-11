package com.example.patrolisatpam

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.patrolisatpam.retrofit.ApiUtils
import com.example.patrolisatpam.retrofit.getPertanyaan
import com.example.patrolisatpam.retrofit.saveJawaban
import kotlinx.android.synthetic.main.activity_pertanyaan.*
import kotlinx.android.synthetic.main.layout_listview_pertanyaan.view.*
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PertanyaanActivity : AppCompatActivity() {
    var rb = arrayListOf<RadioGroup>()
    var leng:Int = 0
    var sp:SharedPreference? = null
    var done:Boolean = false
    var yaSemua: Boolean = true
    var id_patroli:Int? = null
    var noScan:Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val actionBar = supportActionBar
        sp = SharedPreference(this)
        actionBar!!.title = "Patroli Satpam - Pertanyaan"
        actionBar.setDisplayHomeAsUpEnabled(false);
        setContentView(R.layout.activity_pertanyaan)
        id_patroli = intent.getIntExtra("idPatroli", 0)
        noScan = intent.getBooleanExtra("hideNoTemuan", false)
        done = intent.getBooleanExtra("done", false);
        addView()
    }

    fun addView(){
        val id:Int = sp!!.getValueString(sp!!.POS_BERAPA)!!.toInt()
        var mAPIService: getPertanyaan? = null
        mAPIService = ApiUtils.listPertanyaan
        mAPIService!!.getListPertanyaan(id, id_patroli).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val St = response.body()!!.string()
                    val ol = JSONArray(St)
                    leng = ol.length()
                    if (ol.length() != 0){
                        for (i in 0 until ol.length()){
                            val ob = ol.getJSONObject(i)
                            var view =  LayoutInflater.from(this@PertanyaanActivity).inflate(R.layout.layout_listview_pertanyaan, null);
                            view.tvTxtPertanyaan.text = ob.getString("pertanyaan")
                            if (done){
                                if (ob.getString("jawaban") == "1"){
                                    view.rbYa.isChecked = true
                                    view.rbTidak.isEnabled = false
                                }else{
                                    view.rbTidak.isChecked = true
                                    view.rbYa.isEnabled = false
                                }

                            }
                            rb.add(view.rgPertanyaan)
                            llPertanyaan.addView(view);
                        }
                        setButton()
                    }
                }catch (e : JSONException){
                    Toast.makeText(this@PertanyaanActivity, "Error! "+e.toString(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun setButton(){
        val btn : Button = Button(this)
        val param : LinearLayout.LayoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, // This will define text view width
            LinearLayout.LayoutParams.WRAP_CONTENT // This will define text view height
        )

        param.setMargins(0, 0,0,0)
        btn.layoutParams = param


        if (done){
            setToBack(btn)
        }else{
            setToSave(btn)
        }
        llPertanyaan.addView(btn);
    }

    fun setToBack(btn: Button){
        btn.background = ContextCompat.getDrawable(this, R.color.colorAccent)
        btn.text = getString(R.string.back)
        btn.setOnClickListener {
            finish()
        }
    }

    fun setToSave(btn: Button){
        btn.background = ContextCompat.getDrawable(this, R.color.ijo)
        btn.text = getString(R.string.simpan)
        btn.setOnClickListener {
            var x = 0;
            val listz: JSONObject = JSONObject()
            val jAll: JSONObject = JSONObject()

            for (i in rb) {
                if (i.checkedRadioButtonId == -1){
                    // ada yang blm di cek
                }else{
                    val id:Int = i.checkedRadioButtonId
                    val radio: RadioButton = findViewById(id)
                    listz.put(x.toString(), radio.text.toString())
                    x++;

                    if (radio.text.toString() == "Tidak")
                        yaSemua = false
                }
            }
            jAll.put("id", sp!!.getValueString(sp!!.POS_BERAPA))
            jAll.put("noind", sp!!.getValueString(sp!!.USERNYA))
            jAll.put("id_patroli", id_patroli)
            jAll.put("jawaban", listz)
            Log.d("ARR", jAll.toString());
            if (listz.length() == rb.size)
                alerd(jAll)
            else
                Toast.makeText(this@PertanyaanActivity, "Mohon isi semua Pertanyaan!!", Toast.LENGTH_SHORT).show()
        }
    }

    fun alerd(jAll: JSONObject){
        val kirim = {dialog: DialogInterface, which: Int ->
            simpanPertanyaan(jAll)
        }
        val builder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.AlertDialog)
        val inflater: LayoutInflater = getLayoutInflater();
        val layout = inflater.inflate(R.layout.layout_option_image, null)
        with(builder) {
            setTitle("Simpan ?")
            setMessage("Anda yakin jawaban sudah sesuai?")
            setNegativeButton("Cancel", null)
            setPositiveButton("Submit", DialogInterface.OnClickListener(function = kirim))
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

    fun simpanPertanyaan(Arr: JSONObject?){
        setResult(RESULT_OK, Intent());
        var mAPIService: saveJawaban? = null
        mAPIService = ApiUtils.simpanJawaban
        mAPIService!!.dataPostJawaban(Arr).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val res = JSONObject(response.body()!!.string())
                    if (res.getString("success") == "true"){
                        Toast.makeText(this@PertanyaanActivity, res.getString("pesan"), Toast.LENGTH_SHORT).show()
                        val i = Intent(this@PertanyaanActivity, TemuanActivity::class.java)
                        i.putExtra("idPatroli", id_patroli)
                        i.putExtra("hideNoTemuan", noScan)
                        startActivityForResult(i, 100)
                        finish()
                    }else{
                        Toast.makeText(this@PertanyaanActivity, "Error "+res.getString("pesan"), Toast.LENGTH_SHORT).show()
                    }
                }catch (e : JSONException){
                    Toast.makeText(this@PertanyaanActivity, "Error! "+e.toString(), Toast.LENGTH_LONG).show()
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
