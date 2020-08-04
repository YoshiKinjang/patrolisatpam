package com.example.patrolisatpam

import android.app.Service
import android.content.Intent
import android.os.CountDownTimer
import android.os.IBinder
import java.util.*


class ServiceActivity : Service() {
    override fun onBind(p0: Intent?): IBinder? {
        TODO("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val newtimer: CountDownTimer = object : CountDownTimer(1000000000, 30000) {
            override fun onTick(millisUntilFinished: Long) {
                val c: Calendar = Calendar.getInstance()
                val jam:String =  c.get(Calendar.HOUR).toString()
                val minut:String = c.get(Calendar.MINUTE).toString()
                val time:String =  c.get(Calendar.HOUR).toString() + ":" + c.get(Calendar.MINUTE) + ":" + c.get(Calendar.SECOND)
//                Toast.makeText(baseContext, time, Toast.LENGTH_SHORT).show()
                if (jam == "12" && (minut == "01" || minut =="1")){
                    val i = Intent(baseContext, MainActivity::class.java)
                    startActivity(i)
                    this.cancel();
                }
            }

            override fun onFinish() {}
        }
        val c: Calendar = Calendar.getInstance()
        val jam:String =  c.get(Calendar.HOUR).toString()
        val minut:String = c.get(Calendar.MINUTE).toString()
        if (jam < "12")
        newtimer.start();
        else
            newtimer.cancel();
        return START_STICKY
    }
}
