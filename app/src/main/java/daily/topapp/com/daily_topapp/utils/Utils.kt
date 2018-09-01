package daily.topapp.com.daily_topapp.utils

import daily.topapp.com.daily_topapp.data.ParseApps
import daily.topapp.com.daily_topapp.data.Category
import daily.topapp.com.daily_topapp.db.AppsDb
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import java.util.regex.Pattern

/**
 * Created by houhuihua on 2018/8/23.
 */


fun getFormatMonth():String {
    val current = Date(System.currentTimeMillis())
    val name = "${current.year + 1900}-${current.month + 1}"

    return name;
}


fun getFormatDate():String {
    val current = Date(System.currentTimeMillis())
    val name = "${current.year + 1900}-${current.month + 1}-${current.date}"

    return name;
}

fun getFormatPrevDate():String {
    val current = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)
    val name = "${current.year + 1900}-${current.month + 1}-${current.date}"

    return name;
}


fun getPackageName(url:String):String {
    var pos  = url?.indexOf("id=")
    if (pos < 0 || (pos + 3) > url.length - 1) return ""

    return url.substring(pos + 3, url.length)
}


fun md5check(string: ByteArray?, type: String): String {
    val md5: MessageDigest
    return try {
        md5 = MessageDigest.getInstance(type)
        val bytes = md5.digest(string)
        bytes2Hex(bytes)
    } catch (e: NoSuchAlgorithmException) {
        ""
    }
}

fun bytes2Hex(bts: ByteArray): String {
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

fun checkFileName(title:String):String {
    val ILLEGAL_CHARACTERS = charArrayOf('/', '\n', '\r', '\t', '\u0000', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')
    var tmp = title;

    for (i in ILLEGAL_CHARACTERS) {
        tmp = tmp.replace("$i", "")
    }
    return tmp;
}


fun isNumeric(str: String): Boolean {
    val pattern = Pattern.compile("[0-9]*")
    return pattern.matcher(str).matches()
}

fun resolveApps(parse: ParseApps, log: LogInfo, db: AppsDb, topLists:MutableList<Category>) {
    parse?.run {
        //var topLists= initOtherDeveloperList()

        log.file = APP_PATH + "/appchange_log.txt"

        var cc = 0
        for (i in topLists) {

            val content = getTopApps(i.url)
            var list = parseApps(content)

            var appContent = "\n"

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

            cc += list.size
        }

        Thread.sleep(1000 * 5)
        log.printnosave("start checkAppSuspendTask, total:$cc")
        Thread.sleep(1000 * 5)
        log.print("")

        /*
        for (i in topLists) {
            Thread(Runnable {
                i?.run {
                    downloadIconsTask(apps, path, db, log)
                    checkAppSuspendTask(apps, db, log)
                }
            }).start()
        }
        */

    }
}



