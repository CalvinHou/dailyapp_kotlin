package daily.topapp.com.daily_topapp

import android.content.ContentValues
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.TextView
import org.jetbrains.anko.db.*


class MainActivity : AppCompatActivity() {

    val handler = Handler()
    var parse = ParseAppsRank()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        Thread(Runnable {
            val textBtn: TextView = findViewById(R.id.text_content)

            //destoryDb()

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

                    handler.post(Runnable {
                        textBtn?.setText(appContet)
                        //downloadIcons(list, iconPath)

                        /*
                        var download = DownloadImageTask(0)
                        with(download) {
                            listApp = list
                            execute(iconPath)
                        }
                        */
                    })

                    /*
                    Thread(Runnable {
                        downloadIconsTask(list, iconPath)
                    }).start()
                    */

                }

                initDb(topLists)
                //destoryDb()
            }

        }).start()

    }


    fun getPackageName(url:String):String {
        var pos  = url?.indexOf("id=")
        if (pos < 0 || (pos + 3) > url.length - 1) return ""

        return url.substring(pos + 3, url.length -1)
    }


    fun destoryDb() {
        var table = "topapps_list"
        var table_changlog = "topapps_changelog_list"

        database.use {
            dropTable(table)
            dropTable(table_changlog)
            select(table, "package")
                    .exec {
                        println("test$count")
                    }
        }
    }

    fun initDb(apps : MutableList<Category>) {
        var table = "topapps_list"
        var table_changlog = "topapps_changelog_list"

        database.use {
            //dropTable(table, true)
            //dropTable(table_changlog, true)

            createTable(table, true,
                    "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                    "title" to TEXT,
                    "rank" to TEXT,
                    "category" to TEXT,
                    "link" to TEXT,
                    "package" to TEXT,
                    "company" to TEXT,
                    "company_link" to TEXT,
                    "icon_link" to TEXT,
                    "icon_link_small" to TEXT,
                    "desc" to TEXT,
                    "icon_data" to BLOB)

            createTable(table_changlog, true,
                    "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                    "appid" to INTEGER,
                    "title" to TEXT,
                    "package" to TEXT,
                    "rank" to TEXT,
                    "category" to TEXT,
                    "company" to TEXT,
                    "desc" to TEXT,
                    "date" to TEXT,
                    "icon_data" to BLOB)

            //update app list

            for (i in apps) {
                apps_1@ for (j in i.apps) {
                    val values = ContentValues()
                    values.put("title", j.title)
                    values.put("rank", j.rank)
                    values.put("package", getPackageName(j.link))
                    values.put("category", i.name)
                    values.put("link", j.link)
                    values.put("company", j.company)
                    values.put("company_link", j.company_link)
                    values.put("icon_link", j.iconurl[0])
                    values.put("icon_link_small", j.iconurl[1])
                    values.put("desc", j.desc)


                    var find = false
                    select(table, "package")
                            .whereSimple("package=?", getPackageName(j.link))
                            .exec {
                                if (count > 0) {
                                    update(table, values, "package=?", arrayOf(getPackageName(j.link)))
                                    find = true;
                                }
                            }

                    if (find == false)
                        insert(table, null, values)
                }
            }


            select(table, "package")
                    .exec {
                       println("test$count")
                    }

            // update changelog...
            for (i in apps) {
                apps@ for (j in i.apps) {
                    select(table, "package", "title", "desc", "company", "id")
                            .whereSimple("package = ?", getPackageName(j.link))
                            .exec {
                                var result = parseList(object : MapRowParser<Map<String, Any?>> {
                                    override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                        Log.e("search result", columns.toString())
                                        println(columnCount)
                                        println(count)
                                        return columns
                                    }
                                })
                                for (m in result) {
                                    with(j) {
                                        if (title.equals(m.get("title")) == false
                                                || desc.equals(m.get("desc"))
                                                || company.equals(m.get("company"))
                                                ) {
                                            println("$title has changed!")

                                            val values = ContentValues()

                                            values.put("title", j.title)
                                            values.put("rank", j.rank)
                                            values.put("package", getPackageName(j.link))
                                            //values.put("appid", m.get("id"))
                                            values.put("category", i.name)
                                            values.put("company", j.company)
                                            values.put("desc", j.desc)
                                            values.put("date", getFormatDate())


                                            var find = false
                                            select(table_changlog, "package", "title", "desc", "company")
                                                    .whereSimple("package=? and date=?", getPackageName(j.link), getFormatDate())
                                                    .exec {
                                                        parseList(object :MapRowParser<Map<String, Any?>> {
                                                            override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                                                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                                                println("changelog result:${columns.toString()}")
                                                                //return@apps columns
                                                                find = true
                                                                return columns
                                                            }
                                                        })

                                                    }

                                            if (find)
                                                insert(table_changlog, null, values)
                                        }

                                    }
                                }
                            }
                }
            }
        }
    }


}
