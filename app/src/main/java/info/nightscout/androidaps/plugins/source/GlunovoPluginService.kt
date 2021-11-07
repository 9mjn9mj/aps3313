package info.nightscout.androidaps.plugins.source

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

class GlunovoPluginService : Service() {
    private val handler = Handler(Looper.getMainLooper())
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        handler.postDelayed(mgetValue, 3000)
        return super.onStartCommand(intent, flags, startId)
    }

    //180000
    private val mgetValue: Runnable = object : Runnable {
        override fun run() {
            val cr = contentResolver.query(CONTENT_URI, null, null, null, null)

            if (cr == null) { //check if cr has not data
                handler.postDelayed(this, 180000)
                return
            }
            cr.moveToLast()
            val crfirst = contentResolver.query(CONTENT_URI, null, null, null, null)
            crfirst!!.moveToFirst()
            var valuestotake = 2
            if (cr.count < valuestotake) {valuestotake = cr.count} //check if there are less than valuestotake readings and get smaller value
            cr.moveToPosition(cr.count-valuestotake)
            var time : Long
            var value : Double
            var intent : Intent
            var bundle : Bundle

            var i = valuestotake
            while (i > 1)
            {
                cr.moveToNext()
                time = cr.getLong(0)
                value = cr.getDouble(1) * 18.018 //value in mmol/l... transformed in mg/dl if value *18.018
                intent = Intent()
                intent.action = "com.glunovoservice.BgEstimate"
                intent.putExtra("Time", time)
                intent.putExtra("BgEstimate", value)
                intent.setPackage("info.nightscout.androidaps")
                bundle = Bundle()
                bundle.putLong("Time",time)
                bundle.putDouble("BgEstimate",value)
                intent.putExtra("bundle", bundle)
                sendBroadcast(intent)
                i = i - 1
            }

            val curtime = System.currentTimeMillis()
            time = (curtime - cr.getLong(0))/1000
            if (time != curtime) { //wait until next reading to enter
                cr.close()
                crfirst.close()
                handler.postDelayed(this, 180000)//-(curtime-time))
            }
            else
            {
                cr.close()
                crfirst.close()
                handler.postDelayed(this, 180000)
            }
        }
    }

    companion object {
        const val AUTHORITY = "alexpr.co.uk.infinivocgm.cgm_db.CgmExternalProvider/"
        const val TABLE_NAME = "CgmReading"
        val CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/" + TABLE_NAME)
    }
}