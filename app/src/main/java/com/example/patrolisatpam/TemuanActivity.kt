package com.example.patrolisatpam

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.patrolisatpam.retrofit.ApiUtils
import com.example.patrolisatpam.retrofit.getImg
import com.example.patrolisatpam.retrofit.insTemuan
import com.example.patrolisatpam.retrofit.upImg
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_temuan.*
import kotlinx.android.synthetic.main.layout_option_image.view.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


class TemuanActivity : AppCompatActivity() {

    var idIMG: Int = 0
    val hashMap:HashMap<Int,File?> = HashMap<Int,File?>()

    var btSaveTemuan:Button? = null
    var btAddImage:Button? = null
    var tvlTemuan:TextView? = null
    var etTemuan:EditText? = null

    var lat:String? = null
    var long:String? = null
    var sp:SharedPreference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temuan)
        val actionBar = supportActionBar
        actionBar!!.title = "Patroli Satpam - Temuan"

        sp = SharedPreference(this)

        tvlTemuan = findViewById(R.id.tvLokasiTemuan) as TextView
        etTemuan = findViewById(R.id.etTemuan) as EditText
        btSaveTemuan = findViewById(R.id.btnSaveTemuan) as Button
        btAddImage = findViewById(R.id.btnAddImage) as Button
        val Vie = findViewById(R.id.v_penengah) as View

        val done = intent.getBooleanExtra("done", false)
        if (done){
            actionBar.setDisplayHomeAsUpEnabled(true);
            lat = intent.getStringExtra("lat")
            long = intent.getStringExtra("long")
            val isi = intent.getStringExtra("isi")
            val idPatroli = intent.getIntExtra("id_patroli", 0)
            tvlTemuan!!.setText("Lat "+lat+" Long "+long)
            etTemuan!!.setText(isi)
            btNotemuan.visibility = View.GONE
            etTemuan!!.isFocusable = false
            etTemuan!!.isFocusableInTouchMode = false
            btSaveTemuan!!.text = "Kembali"
            btSaveTemuan!!.setOnClickListener { finish() }
            btAddImage!!.visibility = View.GONE
            Vie.visibility = View.GONE
            getGambar(idPatroli)
        }else{
            getLokasi(tvlTemuan!!)
            isLocationEnabled()
            initToSave()
            initToNotemuan()
        }


        btAddImage!!.setOnClickListener {
            if (hashMap.size < 4)
                dialog()
            else
                Toast.makeText(this@TemuanActivity, "Maksimal 4 foto", Toast.LENGTH_LONG).show()
        }
    }

    fun getGambar(id: Int){
        var mAPIService: getImg? = null
        mAPIService = ApiUtils.getGambar
        mAPIService!!.value(id).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                try {
                    val BES = "http://192.168.168.128/khs-erp-patroli/assets/upload/PatroliSatpam/"
                    val arr = JSONArray(response.body()!!.string())
                    for(i in 0 until arr.length()){
                        val obj = arr.getJSONObject(i)
                        val layout = findViewById<View>(R.id.ll_forImage) as LinearLayout
                        val img = ImageView(this@TemuanActivity);
                        img.setPadding(5,5,0,5)
                        img.setLayoutParams(ViewGroup.LayoutParams(180, 180))

                        Picasso.get()
                            .load(BES+obj.getString("nama_file"))
                            .into(img, object: com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    val drawable: BitmapDrawable =
                                        img.getDrawable() as BitmapDrawable
                                    val bitmap: Bitmap = drawable.getBitmap()
                                    img.setOnClickListener { view: View? ->
                                        popupImage(img, view, bitmap, false)
                                    }
                                }
                                override fun onError(e: java.lang.Exception?) {
                                    //do smth when there is picture loading error
                                }
                            })
                        img.id = idIMG
                        img.scaleType = ImageView.ScaleType.CENTER_INSIDE
                        img.adjustViewBounds = true
                        layout.addView(img)
                    }
                }catch (e : JSONException){
                    Toast.makeText(this@TemuanActivity, "Error!1 "+e.toString(), Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun initToNotemuan(){
        btNotemuan.setOnClickListener {
            val toSimpan = {dialog: DialogInterface, which: Int ->
                to_simpan()
            }
            val builder = AlertDialog.Builder(this, R.style.AlertDialog)
            with(builder) {
                setTitle("Tidak Ada Temuan ?")
                setMessage("Anda yakin?\nSetelah men-Klik tombol \"Ya\" halaman ini tidak bisa di edit!")
                setNeutralButton(" ", null)
                setPositiveButton("Ya", DialogInterface.OnClickListener(function = toSimpan))
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
    }

    fun to_simpan(){
        etTemuan!!.setText("Tidak ada Temuan")
        btSaveTemuan!!.performClick()
    }

    fun initToSave(){
        btSaveTemuan!!.setOnClickListener {
            Log.i("Temuan", etTemuan!!.text.toString())
            val isi = etTemuan!!.text.toString()
            val noind = sp!!.getValueString(sp!!.USERNYA)
            val id = intent.getIntExtra("idPatroli", 0)
            if(isi.length > 9){
                setResult(Activity.RESULT_OK, Intent())
                var mAPIService: insTemuan? = null
                mAPIService = ApiUtils.insertTemuan
                mAPIService!!.dataPostTemuan(noind, lat, long, isi, id).enqueue(object :
                    Callback<ResponseBody> {
                    override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                        if(hashMap.size > 0)
                            uploadImg()
                        try {
                            val ob = JSONObject(response.body()!!.string())
                            if (ob.getString("success").equals("true")){
                                Toast.makeText(this@TemuanActivity, "Berhasil Input Data!", Toast.LENGTH_SHORT).show()
                                finish()
                            }
                        }catch (e : JSONException){
                            Toast.makeText(this@TemuanActivity, "Error!2 "+e.toString(), Toast.LENGTH_LONG).show()
                        }
                    }

                    override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

                    }
                })
            }else{
                Toast.makeText(this@TemuanActivity, "Error! Deskripsi minimal 10 karakter", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun uploadImg(){
        val builder: MultipartBody.Builder = MultipartBody.Builder()
        builder.setType(MultipartBody.FORM)

        for ((key, value) in hashMap) {
            val file = File(value!!.toURI())
            val requestImage: RequestBody =
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            builder.addFormDataPart(
                "event_images[]",
                file.name,
                file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            )
        }
        val requestBody = builder.build()
        var mAPIService: upImg?
        mAPIService = ApiUtils.uploadImg
        mAPIService.dataPostImg(requestBody).enqueue(object :
            Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d("GAMBAR", "Berhasil Insert gambar ke database")
            }

            override fun onFailure(call: retrofit2.Call<ResponseBody>, t: Throwable) {

            }
        })
    }

    fun getLokasiText(): TextView{
        val tvlTemuan = findViewById(R.id.tvLokasiTemuan) as TextView
        return  tvlTemuan
    }

    fun getContext(): Context{
        val mContext: Context = this
        return mContext
    }
    //cara mendapatkan lokasi secara penjangggggggggggggggggggggg
    fun getlokasim(): LocationManager{
        val locationManager: LocationManager = getContext().getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 0)
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            2000,
            0f, locationListenerGPS
        )
        return locationManager
    }

    var locationListenerGPS: LocationListener =  object : LocationListener {
        override fun onLocationChanged(location: Location) {
            val latitude = location.latitude
            val longitude = location.longitude

            if (lat.equals("")){
                getLokasiText().setText("Lat "+ latitude+"\nLong "+longitude)
                lat = latitude.toString()
                long = longitude.toString()
            }
        }

        override fun onStatusChanged(
            provider: String?,
            status: Int,
            extras: Bundle?
        ) {
        }

        override fun onProviderEnabled(provider: String?) {}
        override fun onProviderDisabled(provider: String?) {}
    }


    override fun onResume() {
        super.onResume()
        isLocationEnabled()
    }

    private fun isLocationEnabled() {
        try {
            if (!getlokasim().isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                val alertDialog: AlertDialog.Builder = AlertDialog.Builder(getContext())
                alertDialog.setTitle("Enable Location")
                alertDialog.setMessage("Your locations setting is not enabled. Please enabled it in settings menu.")
                alertDialog.setPositiveButton(
                    "Location Settings",
                    DialogInterface.OnClickListener { dialog, which ->
                        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                        startActivity(intent)
                    })
                alertDialog.setNegativeButton("Cancel",
                    DialogInterface.OnClickListener { dialog, which -> dialog.cancel() })
                val alert: AlertDialog = alertDialog.create()
                alert.show()
            }
        }catch (ex: SecurityException){
            Log.e("ERROR LOKASI", ex.message)
        }
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
                    Toast.LENGTH_SHORT).show()
                if (location?.latitude.toString() == ""){
                    getLokasi(tvLokasi)
                }else{
                    tvLokasi.setText("Lat : ${location?.latitude} Long : ${location?.longitude}");
                    lat = location?.latitude.toString()
                    long = location?.longitude.toString()
                }
            }

        })
    }

    fun dialog(){
        val builder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.AlertDialog)
        val inflater: LayoutInflater = getLayoutInflater();
        val layout = inflater.inflate(R.layout.layout_pilih_item, null)
        with(builder) {
            setTitle("")
            setView(layout)
            setMessage("Choose one!")
            setNeutralButton("Cancel", null)
        }
        val alertDialog = builder.create()
        alertDialog.show()
        val buttonNeu = alertDialog.getButton(DialogInterface.BUTTON_NEUTRAL)
        with(buttonNeu) {
            setTextColor(android.graphics.Color.DKGRAY)
        }

        val btnGaleri = layout.findViewById(R.id.btGaleri) as Button
        val btnCamera = layout.findViewById(R.id.btCamera) as Button

        btnGaleri.setOnClickListener {
            val pickPhoto = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(pickPhoto, 1)
        }
        btnCamera.setOnClickListener {
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(
                takePicture,
                0
            ) //zero can be replaced with any action code (called requestCode)
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        imageReturnedIntent: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        when (requestCode) {
            0 -> if (resultCode == Activity.RESULT_OK) {
                val photo: Bitmap = imageReturnedIntent?.getExtras()?.get("data") as Bitmap
                setImg2(photo)
            }
            1 -> if (resultCode == Activity.RESULT_OK) {
                val selectedImage: Uri? = imageReturnedIntent?.data
                setImg(selectedImage)
            }
        }
            Log.i("HASMAP",""+hashMap.count())
    }

    fun setImg(imgUri : Uri?){
        val layout = findViewById<View>(R.id.ll_forImage) as LinearLayout
        val img = ImageView(this);
        img.setPadding(5,5,0,5)
        img.setLayoutParams(ViewGroup.LayoutParams(180, 180))
        img.setImageURI(imgUri)
        img.id = idIMG
        img.scaleType = ImageView.ScaleType.CENTER_INSIDE
        img.adjustViewBounds = true
        layout.addView(img)
        img.setOnClickListener { view: View? ->
            Toast.makeText(this, view!!.id.toString(), Toast.LENGTH_SHORT).show()
            img.visibility = View.GONE
            hashMap.remove(view.id)
        }

        val bitmap2 = MediaStore.Images.Media.getBitmap(this.contentResolver, imgUri)
        //konvert ke base64
        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap2.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
        val encoded: String = Base64.encodeToString(byteArray, Base64.DEFAULT)
//        hashMap.put(idIMG, encoded)

        idIMG++;
    }
    fun setImg2(imgUri : Bitmap?){
        val layout = findViewById<View>(R.id.ll_forImage) as LinearLayout
        val img = ImageView(this);
        img.setPadding(5,5,0,5)
        img.setLayoutParams(ViewGroup.LayoutParams(180, 180))
        img.setImageBitmap(imgUri)
        img.id = idIMG
        img.scaleType = ImageView.ScaleType.CENTER_INSIDE
        img.adjustViewBounds = true
        layout.addView(img)
        img.setOnClickListener { view: View? ->
            popupImage(img, view, imgUri, true)
        }

        hashMap.put(idIMG, createTempFile(imgUri))
//        Toast.makeText(this, ""+ hashMap.size, Toast.LENGTH_SHORT).show()
        idIMG++
    }

    fun popupImage(foto: ImageView, view: View?, sumber: Bitmap? , hapus: Boolean){
        val builder = androidx.appcompat.app.AlertDialog.Builder(this, R.style.AlertDialog)
        val inflater: LayoutInflater = getLayoutInflater();
        val layout = inflater.inflate(R.layout.layout_option_image, null)
        if (!hapus){
            layout.btDelimg.visibility=View.GONE
        }
        with(builder) {
            setTitle("")
            setView(layout)
            setMessage("Choose one!")
        }
        val alertDialog = builder.create()
        alertDialog.show()

        layout.btDelimg.setOnClickListener {
            foto.visibility = View.GONE
            hashMap.remove(view!!.id)
            alertDialog.dismiss()
        }
        layout.btViewimg.setOnClickListener {
            val i = Intent(this, TemuanImageActivity::class.java)
            i.putExtra("img", sumber)
            startActivity(i)
        }
    }

    private fun createTempFile(bitmap: Bitmap?): File? {
        val id = intent.getIntExtra("idPatroli", 0)
        val file = File(
            getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            , System.currentTimeMillis().toString() + "_image_"+id+".png"
        )
        val bos = ByteArrayOutputStream()
        bitmap!!.compress(Bitmap.CompressFormat.PNG, 100, bos)
        val bitmapdata = bos.toByteArray()
        //write the bytes in file
        try {
            val fos = FileOutputStream(file)
            fos.write(bitmapdata)
            fos.flush()
            fos.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
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
* locatonmanager https://stackoverflow.com/questions/46906017/getting-location-offline
* ambil file https://stackoverflow.com/questions/10165302/dialog-to-pick-image-from-gallery-or-from-camera
* upload image https://stackoverflow.com/questions/45828401/how-to-post-a-bitmap-to-a-server-using-retrofit-android
* callback picasso https://stackoverflow.com/questions/55829753/picasso-callback-with-kotlin
* */