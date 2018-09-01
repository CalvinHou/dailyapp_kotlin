package daily.topapp.com.daily_topapp.ui

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import daily.topapp.com.daily_topapp.*
import daily.topapp.com.daily_topapp.data.ParseApps
import daily.topapp.com.daily_topapp.db.AppsDb
import daily.topapp.com.daily_topapp.utils.getFormatDate
import daily.topapp.com.daily_topapp.utils.log


class AllAppsActivity : AppCompatActivity() {

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
            db.queryAppsInfo()

            parse.run {
                var topLists= initTopCategoryList()

                log.file = APP_PATH + "/appchange_log.txt"

                for ((index, i) in topLists.withIndex()) {
                    val content = getTopApps(i.url)
                    var list = parseApps(content)
                    i.apps = list

                    var appContent = i.name + "\n"

                    for (j in list.indices) {
                        list[j].run {
                            category = i.name
                            appContent += "$rank:$title\n$desc\n$link\n$company\n$company_link\n${iconurl[0]}\n"
                        }
                    }

                    createCacheDir(APP_PATH) // for app
                    var appPath = getAppPath(i.name)
                    createCacheDir(appPath)
                    createCacheDir(APP_PATH + "/icon_changed/") // for app

                    var iconPath = getIconPath(appPath, getFormatDate())
                    //var iconPath = getIconPath(appPath, getFormatMonth())
                    createCacheDir(iconPath) // for icon

                    val name = getJsonFile(appPath)
                    saveAppsToJson(list, name)

                    i.path = iconPath


                    db.updateAppChangelogAppinfoList(list, i.name) //first check changelog of title/desc/company
                    db.updateAppsByAppinfoList(list, i.name)

                    log.printonlyhandler(appContent)

                }
                db.queryNewChangelogTable() // print all change app log on today...

                Thread.sleep(1000 * 5)
                log.printnosave("start downloadIconsTask and checkAppSuspendTask, total:${topLists.size*120}")
                Thread.sleep(1000 * 5)

                //split two patch, else will generate too much http connection.
                /*
                for (i in topLists) {
                    Thread(Runnable {
                        i?.run {
                            downloadIconsTask(apps, path, db, log)
                        }
                    }).start()
                }

                checkAppSuspendTask(db.queryOldAppList(), db, log) // check all suspend all on today...
                */
            }

        }).start()

    }

}


