package daily.topapp.com.daily_topapp

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.TextView


class MainActivity : AppCompatActivity() {

    val handler = Handler()
    var parse = ParseAppsRank()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var db = SaveAppsToDb(applicationContext)

        Thread(Runnable {
            val textBtn: TextView = findViewById(R.id.text_content)

            log.textview = textBtn
            log.handler = handler
            log.print("now begining....")

            db.initDb()
            //db.destoryDb()

            parse.run {
                var topLists= initTopCategoryList()
                for ((index, i) in topLists.withIndex()) {
                    val content = getTopApps(i.url)
                    var list = parseApps(content)
                    i.apps = list

                    var appContet = ""

                    for (i in list.indices) {
                        list[i].run {
                            appContet += "$i:$title\n$desc\n$link\n$company\n$company_link\n${iconurl[0]}\n"
                        }
                    }

                    createCacheDir(APP_PATH) // for app
                    var appPath = "$APP_PATH/${getAppDirectory(i.name)}/"
                    createCacheDir(appPath)

                    var iconPath = "$appPath/icon_${getFormatDate()}/"
                    createCacheDir(iconPath) // for icon

                    val name = "$appPath/app-${getFormatDate()}.json"
                    saveAppsToJson(list, name)

                    i.path = iconPath

                    log.file = appPath + "/appchange_log.txt"

                    handler.post(Runnable {
                        textBtn?.setText(appContet)
                        //downloadIcons(list, iconPath)

                    })

                    db.updateAppsByAppinfoList(list, i.name)
                    db.updateAppChangelogAppinfoList(list, i.name)

                }

                //split two patch, else will generate too much http connection.
                for (i in topLists) {
                    Thread(Runnable {
                        i?.run {
                            downloadIconsTask(apps, path, db)
                            checkAppSuspendTask(apps, db)
                        }
                    }).start()
                }

                //db.updateDbData(topLists)
            }

        }).start()

    }

}


