package daily.topapp.com.daily_topapp.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import daily.topapp.com.daily_topapp.*
import daily.topapp.com.daily_topapp.data.ParseApps
import daily.topapp.com.daily_topapp.db.AppsDb
import daily.topapp.com.daily_topapp.utils.log
import daily.topapp.com.daily_topapp.utils.resolveApps

class DevelopersCheckActivity : AppCompatActivity() {

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

            /*
            var list  = db.queryAllDevelopsListFromApps()
            for (j in list) {
                db.updateCategoryInfo(j)
            }
            */

            if (intent.getStringExtra("check_type").equals( "all")) {
                resolveApps(parse, log, db, db.queryAllDevelopsList(), false)
            }
            else if (intent.getStringExtra("check_type").equals( "personalization_new")) {
                var list = parse.initOtherDeveloperList()
                var listNew = db.queryNewDevelopersList("personalization_new")
                for (i in list) {
                    var find = false
                    for (j in listNew) {
                        if (i.name.equals(j.name)) {
                            find = true
                            break
                        }

                    }
                    if (find == false) {
                        listNew.add(i)
                    }
                }

                resolveApps(parse, log, db, listNew)
            }
            else if (intent.getStringExtra("check_type").equals( "_new")) {
                var listNew = db.queryAllNewDevelopersList("")
                resolveApps(parse, log, db, listNew, false)
            }

        }).start()

    }

}
