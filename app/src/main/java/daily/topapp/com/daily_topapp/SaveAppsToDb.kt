package daily.topapp.com.daily_topapp

import android.content.ContentValues
import android.content.Context
import org.jetbrains.anko.db.*
import java.io.File
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * Created by houhuihua on 2018/8/24.
 */

class SaveAppsToDb (var context: Context) {
    var table = "topapps_list"
    var table_changlog = "topapps_changelog_list"

    fun initDb() {
        createDbTables()
        querySuspendTable()
    }

    fun updateDbData(apps: MutableList<Category>) {
        updateAppsByCategoryList(apps)
        updateAppChangelogByCategoryList(apps)
    }


    fun createDbTables() {
        context.database.use {
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
                    "date" to TEXT,
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
        }
    }

    fun updateAppsByCategoryList(apps: MutableList<Category>) {
        for (i in apps) {
            updateAppsByAppinfoList(i.apps, i.name)

        }
    }

    fun updateAppChangelogByCategoryList(apps: MutableList<Category>) {
        apps[0].apps[0].title += " - only test..."
        apps[0].apps[20].title += " - only test..."

        for (i in apps) {
            updateAppChangelogAppinfoList(i.apps, i.name)
        }
    }


    fun updateAppChangelogAppinfoList(apps: MutableList<AppInfo>, name:String) {
        db.use {
            for (j in apps) {
                j?.run {
                    // check if tille/company/desc has changed.
                    select(table, "package", "title", "desc", "company")
                            .whereSimple("package = ?", getPackageName(link))
                            .exec {
                                if (count > 0) {
                                    var result = parseList(object : MapRowParser<Map<String, Any?>> {
                                        override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                            return columns
                                        }
                                    })
                                    for (m in result) {
                                        if (title.equals(m.get("title")) == false
                                                || company.equals(m.get("company")) == false
                                                || desc.equals(m.get("desc") == false)
                                                ) {
                                            insertAppChangelogByAppinfo(j)
                                            var tmp = "${title.equals(m.get("title"))}:$title\n${company.equals(m.get("company"))}$company\n${desc.equals(m.get("desc"))}$desc"
                                            println(tmp)
                                            context.log.print("$tmp")
                                        }
                                    }
                                }
                            }
                }
            }


        }
        queryTable(table_changlog)
    }


    fun updateAppsByAppinfoList(apps: MutableList<AppInfo>, name:String) {
            for (j in apps) {
                updateAppsByAppinfo(j)
            }
    }

    fun updateAppsByAppinfo(app:AppInfo) {
        db.use {
            val values = ContentValues()
            app?.run {

                var map = mapOf<String, String>("title" to title, "rank" to rank, "package" to getPackageName(link),
                        "category" to category, "link" to link, "company" to company, "company_link" to company_link,
                        "icon_link" to iconurl[0], "icon_link_small" to iconurl[1], "desc" to desc
                )

                for (m in map) {
                    values.put(m.key, m.value)
                }


                select(table, "package")
                        .whereSimple("package=?", getPackageName(link))
                        .exec {
                            if (count > 0) {
                                update(table, values, "package=?", arrayOf(getPackageName(link)))
                            } else {
                                values.put("date", getFormatDate())
                                insert(table, null, values)
                                println("$table: insert.")
                                //context.log.printnosave("$table: insert.")
                            }
                        }

            }
        }
    }



    fun insertAppChangelogByAppinfo(app:AppInfo) {
        context.database.use {
            with(app) {
                select(table_changlog, "package")
                        .whereSimple("package=? and date=?", getPackageName(link), getFormatDate())
                        .exec {
                            if (count > 0) {
                                parseList(object : MapRowParser<Map<String, Any?>> {
                                    override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                        //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                        println("$table_changlog: exist $count ${getPackageName(link)}")
                                        return columns
                                    }
                                })
                            }
                            else {
                                val values = ContentValues()
                                val map = mapOf<String, String>("title" to title,  "rank" to rank, "package" to getPackageName(link),
                                        "category" to category, "company" to company, "desc" to desc, "date" to getFormatDate()
                                )
                                for (m in map) {
                                    m.run { values.put(key, value) }
                                }

                                insert(table_changlog, null, values)
                                println("$table_changlog: insert ${getPackageName(link)}")
                            }
                        }
            }

        }
    }

    fun destoryDb() {
        context.database.use {
            dropTable(table)
            dropTable(table_changlog)
            select(table, "package")
                    .exec {
                        println("test$count")
                    }
        }
    }


    fun queryTable(table: String) {
        context.database.use {
            select(table, "*")
            //select(table, "package", "title")
                    .exec {
                        println("$table:$count:$columnCount")
                        parseList(object : MapRowParser<Map<String, Any?>> {
                            override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                println("query $table:$count ${columns.toString()}")
                                return columns
                            }
                        })

                    }
        }
    }

    fun querySuspendTable() {
        db.use {
            select(table, "*")
                    //select(table, "package", "title")
                    .exec {
                        println("suspend $table:$count:$columnCount")
                        parseList(object : MapRowParser<Map<String, Any?>> {
                            override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                if (columns.get("title").toString().indexOf("[suspend]") > -1)
                                    println("query $table:$count ${columns.toString()}")
                                return columns
                            }
                        })

                    }
        }
    }


    fun updateAppsIcon(app:AppInfo, file: File) {

        context.database.use {
            app?.run {
                select(table, "package", "icon_data")
                        .whereSimple("package = ?", getPackageName(link))
                        .exec {
                            if (count > 0) {
                                var content = file.readBytes()
                                var md51 = md5check(content, "MD5")

                                parseList(object : MapRowParser<Map<String, Any?>> {
                                    override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                        var icon_data = columns.get("icon_data")
                                        if (icon_data is ByteArray ) {
                                            var md51_new = md5check(icon_data, "MD5")
                                            if (md51?.equals(md51_new) == false) {

                                                updateAppChangelogIconByAppinfo(app, file)
                                                println("$title icon has changed!")

                                                context.log.print("$title:icon has changed! $link")
                                            }
                                        }
                                        return columns
                                    }
                                })

                                var values =  ContentValues()
                                values.put("icon_data", content)
                                update(table, values, "package=?", arrayOf(getPackageName(link)))
                            }
                        }
            }
        }
    }

    fun updateAppChangelogIconByAppinfo(app:AppInfo, file: File) {
        db.use {
            app?.run {
                select(table_changlog, "package", "icon_data")
                        .whereSimple("package=? and date=?", getPackageName(link), getFormatDate())
                        .exec {
                            if (count > 0) { // update icon_date now, it is no add before.
                                var content = file.readBytes()
                                var values =  ContentValues()
                                values.put("icon_data", content)
                                update(table, values, "package=?", arrayOf(getPackageName(link)))
                            }
                            else {
                                insertAppChangelogByAppinfo(app)
                            }
                        }
            }
        }
    }

    private fun md5check(string: ByteArray?, type: String): String {
        val md5: MessageDigest
        return try {
            md5 = MessageDigest.getInstance(type)
            val bytes = md5.digest(string)
            bytes2Hex(bytes)
        } catch (e: NoSuchAlgorithmException) {
            ""
        }
    }

    private fun bytes2Hex(bts: ByteArray): String {
        var des = ""
        var tmp: String
        for (i in bts.indices) {
            tmp = Integer.toHexString(bts[i].toInt() and 0xFF)
            if (tmp.length == 1) {
                des += "0"
            }
            des += tmp
        }
        return des
    }

    fun updateAppsTitle(app:AppInfo) {

        db.use {
            app?.run {
                select(table, "package")
                        .whereSimple("package = ?", getPackageName(link))
                        .exec {
                            if (count > 0) {
                                var values =  ContentValues()
                                values.put("title", title)
                                update(table, values, "package=?", arrayOf(getPackageName(link)))
                            }
                        }
            }
        }
    }

    val db: MySqlHelper
        get() = MySqlHelper.getInstance(context)

}
