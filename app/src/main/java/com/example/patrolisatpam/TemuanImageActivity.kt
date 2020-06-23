package com.example.patrolisatpam

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Parcelable
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_temuan_image.*


class TemuanImageActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_temuan_image)
        val actionBar = supportActionBar
        actionBar!!.title = "View Image"
        actionBar!!.setDisplayHomeAsUpEnabled(true);
        val intent = intent
        val bitmap = intent.getParcelableExtra<Parcelable>("img") as? Bitmap
        Log.d("BITMA", bitmap.toString())
        ivVtemuan.setImageBitmap(bitmap)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            android.R.id.home ->
                finish()
        }
        return true
    }
}
