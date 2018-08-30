package daily.topapp.com.daily_topapp

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView

class OtherDeveloperActivity : AppCompatActivity() {

    val handler = Handler()
    var parse = ParseAppsRank()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var db = SaveAppsToDb(applicationContext)

        Thread(Runnable {
            val textBtn: TextView = findViewById(R.id.text_content)
            val textBtn2: TextView = findViewById(R.id.text_important)

            log.textview = textBtn
            log.textview2 = textBtn2
            log.handler = handler
            log.print("now begining....")

            db.initDb()
            //db.destoryDb()

            resolveApps(parse, log, db, parse.initOtherDeveloperList())

        }).start()

    }

}
