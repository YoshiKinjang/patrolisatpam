package com.example.patrolisatpam

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_wrong.*

class WrongActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_wrong)
        val actionBar = supportActionBar
        actionBar!!.title = "Scan Result"

        btBackWrong.setOnClickListener {
            finish()
        }
    }
}
