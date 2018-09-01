package daily.topapp.com.daily_topapp.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import daily.topapp.com.daily_topapp.R
import daily.topapp.com.daily_topapp.data.ParseApps
import daily.topapp.com.daily_topapp.db.AppsDb
import daily.topapp.com.daily_topapp.utils.log

class SuspendCheckActivity : AppCompatActivity() {
    val handler = Handler()
    var parse = ParseApps()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        var db = AppsDb(applicationContext)


        Thread(Runnable {
            val textBtn: TextView = findViewById(R.id.text_content)
            val textBtn2: TextView = findViewById(R.id.text_important)

            log.textview = textBtn
            log.textview2 = textBtn2
            log.handler = handler
            log.print("now begining....")

            db.initDb()
            //db.destoryDb()

            parse?.checkAppSuspendTask(db.queryOldAppList(), db, log) // check all suspend all on today...

        }).start()


    }
}
