package daily.topapp.com.daily_topapp.db

import android.content.ContentValues
import android.content.Context
import daily.topapp.com.daily_topapp.data.AppInfo
import daily.topapp.com.daily_topapp.data.Category
import daily.topapp.com.daily_topapp.utils.*
import org.jetbrains.anko.db.*
import java.io.File

/**
 * Created by houhuihua on 2018/8/24.
 */

class AppsDb(var context: Context) {
    var category = "categories_list"
    var table = "topapps_list"
    var table_changlog = "topapps_changelog_list"
    val SUSPEND = "[suspend app]"
    val NEWCHANGE = "[new!!]"

    fun initDb() {
        createDbTables()
    }

    fun queryAppsInfo() {
        querySuspendTable()
        queryChangelogTable()
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

            //dropTable(category)
            createTable(category, true,
                    "id" to INTEGER + PRIMARY_KEY + UNIQUE,
                    "category" to TEXT,
                    //"company_link" to TEXT,
                    "url" to TEXT,
                    "date" to TEXT,
                    "status" to TEXT)
        }
    }

    fun updateAppsByCategoryList(apps: MutableList<Category>) {
        for (i in apps) {
            updateAppsByAppinfoList(i.apps, i.name)

        }
    }

    fun updateAppChangelogByCategoryList(apps: MutableList<Category>) {
        //apps[0].apps[0].title += " - only test..."
        //apps[0].apps[20].title += " - only test..."

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
                                        var t = m.get("title") as String
                                        var d = m.get("desc") as String
                                        var c = m.get("company") as String

                                        if ((title?.equals(t) == false && t?.length > 0 && t?.indexOf(NEWCHANGE) == -1)
                                                || (company?.equals(c) == false && c?.length > 0 && c?.indexOf(NEWCHANGE) == -1)
                                                || (desc?.equals(d) == false && d?.length > 0 && d?.indexOf(NEWCHANGE) == -1)
                                                ) {
                                            var app = j
                                            if (title?.equals(t) == false) app.title = "$title:$NEWCHANGE to:$t"
                                            if (company?.equals(c) == false) app.company= "$company:$NEWCHANGE to:$c"
                                            if (desc?.equals(d) == false) app.desc= "$desc:$NEWCHANGE to:$d"

                                            insertAppChangelogByAppinfo(app)

                                            var tmp = "$NEWCHANGE:${!title.equals(t)}: $title\n${!company.equals(c)}: $company\n${!desc.equals(d)}: $desc"
                                            println(tmp)
                                            context.log.print("$tmp")
                                        }
                                    }
                                }
                            }
                }
            }


        }
        //queryTable(table_changlog, "")
    }


    fun updateAppsByAppinfoList(apps: MutableList<AppInfo>, name:String) {
            for (j in apps) {
                updateAppsByAppinfo(j)
            }
    }

    fun updateAppsByAppinfo(app: AppInfo) {
        db.use {
            val values = ContentValues()
            app?.run {

                var map = mapOf<String, String>("title" to title, "rank" to rank, "package" to getPackageName(link),
                        "category" to category, "link" to link, "company" to company, "company_link" to company_link,
                        "icon_link" to iconurl[0], "icon_link_small" to iconurl[1], "desc" to desc, "date" to getTodayFormatDate()
                )

                for (m in map) {
                    values.put(m.key, m.value)
                }


                select(table, "package")
                        .whereSimple("package = ?", getPackageName(link))
                        .exec {
                            if (count > 0) {
                                update(table, values, "package = ?", arrayOf(getPackageName(link)))
                            } else {
                                insert(table, null, values)
                                context.log.print("$table: insert.$title:${getPackageName(link)}")
                            }
                            updateCategoryInfo(getCategory(company))
                        }

            }
        }
    }



    fun insertAppChangelogByAppinfo(app: AppInfo) {
        context.database.use {
            with(app) {
                select(table_changlog, "package")
                        .whereSimple("package = ? and date = ?", getPackageName(link), getTodayFormatDate())
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
                                        "category" to category, "company" to company, "desc" to desc, "date" to getTodayFormatDate()
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


    fun queryTable(table: String, tip:String) {
        context.database.use {
            select(table, "*")
            //select(table, "package", "title")
                    .exec {
                        println("$table:$count:$columnCount")
                        parseList(object : MapRowParser<Map<String, Any?>> {
                            override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                println("$table, $tip ${columns.toString()}")
                                return columns
                            }
                        })

                    }
        }
    }

    fun querySuspendTable() {
        db.use {
            var cc = 0
            select(table, "*")
                    //select(table, "package", "title")
                    .exec {
                        println("suspend $table:$count:$columnCount")
                        parseList(object : MapRowParser<Map<String, Any?>> {
                            override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                var title = (columns.get("title") as String)
                                var pkg = (columns.get("package") as String)
                                if (title?.indexOf("$SUSPEND") > -1
                                    || title?.indexOf("[suspend") > -1)
                                    context.log.print("suspend query $table:${cc++} $title:$pkg")
                                return columns
                            }
                        })

                    }
        }
    }

    fun queryChangelogTable() {
        db.use {
            var cc = 0
            select(table_changlog, "*")
                    //select(table, "package", "title")
                    .exec {
                        println("new change $table:$count:$columnCount")
                        parseList(object : MapRowParser<Map<String, Any?>> {
                            override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                if ((columns.get("title") as String)?.indexOf("$NEWCHANGE") > -1) {
                                    var title = columns.get("title")
                                    var pkg = columns.get("package")
                                    context.log.printnosave("$table_changlog query:${cc++} $title:$pkg")
                                }
                                return columns
                            }
                        })

                    }
        }
    }


    fun queryNewChangelogTable() {
        db.use {
            var cc = 0
            select(table_changlog, "*")
                    .whereSimple("date = ?", getTodayFormatDate())
                    //select(table, "package", "title")
                    .exec {
                        parseList(object : MapRowParser<Map<String, Any?>> {
                            override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                var title:String = (columns.get("title") as String)
                                var pkg = (columns.get("package") as String)

                                if (title?.indexOf(NEWCHANGE) > -1) {
                                    context.log.print("$table_changlog query $table:${cc++} $title:$pkg")
                                }
                                return columns
                            }
                        })

                    }
        }
    }



    fun updateAppsIcon(app: AppInfo, file: File, path:String) {

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
                                                saveAppChangedIcon(app, file, icon_data, path)
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

    fun saveAppChangedIcon(app: AppInfo, file:File, icondata: ByteArray, path: String) {
        var fileNameNew = path + checkFileName(app.title) + "-" + getTodayFormatDate() +  "-new.jpeg"
        var fileName = path + checkFileName(app.title) + "-" + getTodayFormatDate() +  ".jpeg"

        file.copyTo(File(fileNameNew))
        File(fileName).writeBytes(icondata)
    }

    fun updateAppChangelogIconByAppinfo(app: AppInfo, file: File) {
        db.use {
            app?.run {
                select(table_changlog, "package", "icon_data")
                        .whereSimple("package=? and date=?", getPackageName(link), getTodayFormatDate())
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

    fun updateAppSuspend(app: AppInfo):Boolean {
        app?.run {
            if (title.indexOf(SUSPEND) == -1) {
                title = "$SUSPEND:[${getTodayFormatDate()}]:${title}"
                updateAppTitle(app)
                return true
            }
        }
        return false
    }


    fun updateAppTitle(app: AppInfo) {
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

    fun queryLatestAppList() :MutableList<AppInfo>{
        var date = getTodayFormatDate()
        var list = mutableListOf<AppInfo>()
        context.database.use {
            select(table, "*")
                    .whereSimple("date = ?", date)
                    //select(table, "package", "title")
                    .exec {
                        println("$table:$count:$columnCount")
                        if (count > 0) {
                            parseList(object : MapRowParser<Map<String, Any?>> {
                                override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    println("query $table:$count ${columns.toString()}")

                                    var title = columns.get("title") as String
                                    if (title.indexOf(SUSPEND) <= -1) {
                                        var app = AppInfo()
                                        app.title = title
                                        app.link = columns.get("link") as String
                                        app.date = columns.get("date") as String
                                        app.iconurl = arrayOf(columns.get("icon_link") as String, columns.get("icon_link_small") as String)
                                        app.rank = columns.get("rank") as String
                                        app.company = columns.get("company") as String
                                        app.company_link = columns.get("company_link") as String
                                        app.category = columns.get("category") as String

                                        list.add(app)
                                    }
                                    return columns
                                }
                            })
                        }
                    }

        }
        return list
    }

    fun queryOldAppList() :MutableList<AppInfo>{
        var date = getTodayFormatDate()
        var list = mutableListOf<AppInfo>()
        context.database.use {
            select(table, "*")
                    .whereSimple("date != ?", date)
                    //select(table, "package", "title")
                    .exec {
                        println("$table:$count:$columnCount")
                        if (count > 0) {
                            parseList(object : MapRowParser<Map<String, Any?>> {
                                override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    println("query $table:$count ${columns.toString()}")

                                    var title = columns.get("title") as String
                                    if (title.indexOf(SUSPEND) <= -1) {
                                        var app = AppInfo()
                                        app.title = title
                                        app.link = columns.get("link") as String
                                        app.date = columns.get("date") as String

                                        list.add(app)
                                    }
                                    return columns
                                }
                            })
                        }
                    }

        }
        return list
    }

    fun queryOtherDevelopAppList() :MutableList<AppInfo>{
        var date = getTodayFormatDate()
        var list = mutableListOf<AppInfo>()
        context.database.use {
            select(table, "*")
                    .whereSimple("category = ?", "otherdeveloper")
                    //select(table, "package", "title")
                    .exec {
                        println("$table:$count:$columnCount")
                        if (count > 0) {
                            parseList(object : MapRowParser<Map<String, Any?>> {
                                override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    println("query $table:$count ${columns.toString()}")

                                    var title = columns.get("title") as String
                                    //if (title.indexOf(SUSPEND) <= -1)
                                    //{
                                        var app = AppInfo()
                                        app.title = title
                                        app.link = columns.get("link") as String
                                        app.date = columns.get("date") as String

                                        list.add(app)
                                    //}
                                    return columns
                                }
                            })
                        }
                    }

        }
        return list
    }

    fun queryAllNewDevelopersList(name:String) :MutableList<Category>{
        var list = mutableListOf<Category>()

        context.database.use {
            select(category, "*")
                    .exec {
                        println("$table:$count:$columnCount")
                        if (count > 0) {
                            parseList(object : MapRowParser<Map<String, Any?>> {
                                override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    println("query $table:$count ${columns.toString()}")
                                    var c = columns.get("category") as String

                                    if (c.contains("_new")) {
                                        addCategory(list, columns)
                                    }

                                    return columns
                                }
                            })
                        }
                    }

        }
        return list
    }

    private fun addCategory(list: MutableList<Category>, columns: Map<String, Any?>) {
        var company = columns.get("company") as String

        var info = getCategory(company)
        var find = false
        for (i in list) {
            if (i.name.equals(company)) {
                find = true
                break
            }
        }
        if (find == false) {
            list.add(info)
        }
    }

    private fun getCategory(company:String):Category {
        var link = company.replace(" ", "+")
                .replace("&", "%26")

        var info = Category(company, BASE_DEV + link)
        var pos = company.indexOf(":$NEWCHANGE")
        if (pos > -1) {
            info.name = company.substring(0, pos)
            val l = info.name.replace(" ", "+")
                    .replace("&", "%26")
            info.url = BASE_DEV + l
        }

        return info
    }


    fun queryNewDevelopersList(name:String) :MutableList<Category>{
        var list = mutableListOf<Category>()

        context.database.use {
            select(category, "*")
                    .whereSimple("category = ? and date != ?", name, getTodayFormatDate())
                    //select(table, "package", "title")
                    .exec {
                        println("$table:$count:$columnCount")
                        if (count > 0) {
                            parseList(object : MapRowParser<Map<String, Any?>> {
                                override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    println("query $table:$count ${columns.toString()}")

                                    addCategory(list, columns)

                                    return columns
                                }
                            })
                        }
                    }

        }
        return list
    }


    fun queryAllDevelopsList() :MutableList<Category>{
        var list = mutableListOf<Category>()
        context.database.use {
            select(category, "*")
                    //select(table, "package", "title")
                    .whereSimple("date != ? and status != ?", getTodayFormatDate(), "suspend")
                    .exec {
                        if (count > 0) {
                            parseList(object : MapRowParser<Map<String, Any?>> {
                                override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    println("query $category:$count ${columns.toString()}")
                                    //addCategory(list, columns)
                                    val name = columns.get("category") as String
                                    val url = columns.get("url") as String
                                    val status = columns.get("status") as String
                                    //val date = columns.get("date") as String
                                    list.add(Category(name, url, status))

                                    return columns
                                }
                            })
                        }
                    }

        }
        return list
    }


    fun queryAllDevelopsListFromApps() :MutableList<Category>{
        var list = mutableListOf<Category>()
        context.database.use {
            select(table, "*")
                    //select(table, "package", "title")
                    .whereSimple("date != ?", getTodayFormatDate())
                    .exec {
                        println("$table:$count:$columnCount")
                        if (count > 0) {
                            parseList(object : MapRowParser<Map<String, Any?>> {
                                override fun parseRow(columns: Map<String, Any?>): Map<String, Any?> {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    println("query $table:$count ${columns.toString()}")
                                    addCategory(list, columns)

                                    return columns
                                }
                            })
                        }
                    }

        }
        return list
    }

    fun isCategorySuspend(c: Category):Boolean {
        var suspend = false
        db.use {
            val values = ContentValues()
            c?.run {
                var map = mapOf<String, String>("category" to name, "url" to url,
                        "status" to status
                )
                for (m in map) {
                    values.put(m.key, m.value)
                }

                select(category, "*")
                        .whereSimple("category = ? and status = ?", name, "suspend")
                        .exec {
                            if (count > 0) {
                                suspend = true
                            }
                        }

            }
        }
        return suspend
    }



    fun updateCategoryInfo(c: Category) {
        db.use {
            val values = ContentValues()
            c?.run {
                var map = mapOf<String, String>("category" to name, "url" to url,
                        "status" to status, "date" to getTodayFormatDate()
                )
                for (m in map) {
                    values.put(m.key, m.value)
                }

                select(category, "*")
                        .whereSimple("category = ?", name)
                        .exec {
                            if (count > 0) {
                                update(category, values, "category = ?", arrayOf(name))
                            } else {
                                insert(category, null, values)
                                context.log.print("$category: insert.$name:$url")
                            }
                        }

            }
        }
    }


    fun querySpecialSQL(logInfo: LogInfo, t:String, sql:String){
        context.database.use {
            select(table, "*")
                    //select(table, "package", "title")
                    .exec {
                        println("$table:$count:$columnCount")
                        if (count > 0) {
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
    }



    val db: MySqlHelper
        get() = MySqlHelper.getInstance(context)

}
