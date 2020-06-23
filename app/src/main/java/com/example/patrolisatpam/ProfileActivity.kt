package com.example.patrolisatpam

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_profile.*
import java.text.SimpleDateFormat
import java.util.*

class ProfileActivity : AppCompatActivity() {

    @SuppressLint("SimpleDateFormat")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()?.setTitle("Profile");

        val noind = intent.getStringExtra("noind")
        val nama = intent.getStringExtra("nama")
        val url = intent.getStringExtra("url")

        Picasso.get()
            .load(url)
            .into(ivProfile2)
        tvPnama.setText(noind+" - "+nama)

        val c: Date = Calendar.getInstance().getTime()
        println("Current time => $c")

        val df = SimpleDateFormat("dd-MMM-yyyy")
        val tm = SimpleDateFormat("HH:mm")
        val formattedDate: String = df.format(c)
        val formattedTime: String = tm.format(c)
        val startTime = formattedTime
        val endTime = "12:00"
        val sdf = SimpleDateFormat("HH:mm")
        val d1 = sdf.parse(startTime)
        val d2 = sdf.parse(endTime)
        val elapsed = d2!!.time - d1!!.time
        if (d1 > d2){
            Log.d("ELPS", elapsed.toString())
            tvPshift.setText(formattedDate)
        }else{
            val cal = Calendar.getInstance()
            cal.add(Calendar.DAY_OF_MONTH, -1)
            val dff = SimpleDateFormat("dd-MMM-yyyy")
            val formattedDate2: String = dff.format(cal.time)
            tvPshift.setText(formattedDate2)
        }

        btLogout.setOnClickListener {
            tryLogout()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home ->
            finish()
        }
        return true
    }

    fun tryLogout(){
        val logout = { dialog: DialogInterface, which: Int ->
            logout()
        }
        val builder = AlertDialog.Builder(this, R.style.AlertDialog)
        with(builder) {
            setTitle("Logout")
            setMessage("Anda ingin Logout?")
            setPositiveButton("Logout", DialogInterface.OnClickListener(function = logout))
            setNeutralButton(" ", null)
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

    fun logout(){
        val sP = SharedPreference(this);
        sP.saveSPBool(sP.SUDAH_LOGIN, false)
        val inten = Intent(this, LoginActivity::class.java)
        Toast.makeText(this, "Berhasil Logout", Toast.LENGTH_SHORT).show();
        startActivity(inten);
        finish()
    }

}
