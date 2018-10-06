package daily.topapp.com.daily_topapp.data

import android.graphics.Bitmap
import com.google.gson.Gson
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import org.jsoup.nodes.Element
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import daily.topapp.com.daily_topapp.db.AppsDb
import daily.topapp.com.daily_topapp.utils.*
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Created by houhuihua on 2018/8/23.
 */

class ParseApps {
    var topCategoryNameLists = listOf(
            "PERSONALIZATION", "MUSIC_AND_AUDIO", "COMMUNICATION", "PRODUCTIVITY", "ENTERTAINMENT", "TOOLS", "SOCIAL"
    )

    var developList = listOf("HeyEmoji",
            "crazystudio",
            "Barley+WorkShop",
            "BarleyMobile",
            "wavestudio",
            "WaveStudio",
            "Red+Free+Music",
            "Free+Music+Plus",
            "EmojiTheme",
            "EmojiStudio",
            "BarleyGame",
            "WaterwaveCenter"
            )


    var otherDevelopList = listOf("Jessie+Keyboard+Theme",
            "Cool+keyboard",
            "Fashion+Cute+Emoji",
            "Simple+Graphics+Tool",
            "Cool+keyboard+Theme+Designer",
            "Abby+Theme+Center",
            "Colorful+Keyboard+Theme+Designer",
            "Hello+Keyboard+Theme",
            "2018+Cool+Keyboard+Theme+for+Android",
            "CM+Launcher+Team",
            "Echo+Keyboard+Theme",
            "Abby+Theme+Center",
            "Hello+Keyboard+Theme",
            "3D+Live+Animated+Keyboard+Themes+for+CM+Keyboard",
            "2018+Supernova+Keyboard+Theme",
            "Personalization+Apps",
            "Launcher+and+Keyboard+Themes",
            "Theme+Design",
            "Keyboard+Theme+Master",
            "Keyboard+Wallpaper",
            "Personalization+Apps",
            "Free+Cool+Keyboard+Themes",
            "Cool+Theme+Team",
            "Super+Cool+Keyboard+Theme",
            "SY+Epic+Keyboard" //2018.08.30

    )


    var topCategoryLists = mutableListOf<Category>()
    var topmyDeveloperLists = mutableListOf<Category>()
    var topOtherDeveloperLists = mutableListOf<Category>()

    private fun getTop(category: String, start:Int) : Category {
       return Category(category.toLowerCase(),
               BASECATEGORY + category + "/collection/topselling_free?start=$start&num=120")
    }

    private fun getTopNew(category: String, start:Int) : Category {
        return Category(category.toLowerCase() + "_new",
                BASECATEGORY + category + "/collection/topselling_new_free?start=$start&num=120")
    }


    fun initTopCategoryList() : MutableList<Category>{
        for (i in topCategoryNameLists) {
            topCategoryLists.add(getTop(i, 0))
            topCategoryLists.add(getTop(i, 120))
            topCategoryLists.add(getTopNew(i, 0))
            topCategoryLists.add(getTopNew(i, 120))
        }
        return topCategoryLists
    }

    fun initMyDeveloperList() : MutableList<Category>{
        for (i in developList) {
            topmyDeveloperLists.add(Category("mydeveloper", BASE_DEV + i))
        }
        return topmyDeveloperLists
    }

    fun initOtherDeveloperList() : MutableList<Category>{
        for (i in otherDevelopList) {
            topOtherDeveloperLists.add(Category("otherdeveloper", BASE_DEV + i))
        }
        return topOtherDeveloperLists
    }

    fun getAppDirectory(url:String):String {
        /*
        return url.trim()
                .replace("https://play.google.com/store/apps/", "")
                .replace("/", "-")
                .replace("?", "-")
                .replace(":", "-")
                .replace("&", "-")
                .replace("=", "-")
                */
        return url;

    }

    var clientCategory = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    fun getTopApps(url:String):String {
        val request:Request = Request.Builder().url(url).build()
        val response:Response = clientCategory.newCall(request).execute()
        println(url)

        return response?.body()!!.string()
    }

    fun parseApps(content:String):MutableList<AppInfo> {
        var doc: Document = Jsoup.parse(content)
        var element = doc.select("div.id-card-list")
        var details = element?.select("div.details")
        var icons = element?.select("div.cover-inner-align")

        var appLists = mutableListOf<AppInfo>()

        for(i in details!!.indices) {
            var app = AppInfo()

            var childLists = details[i].childNodes()
            for (j in childLists) {
                var ele = j as? Element
                ele?.run {
                    when(attr("class")) {
                        "title" -> {
                            var pos = text().indexOf(".")
                            if (pos > -1 && pos < 5) {
                                try {
                                    var rank: String = text().substring(0, pos)
                                    if (isNumeric(rank)) {
                                        app.rank = Integer.parseInt(rank).toString()
                                    }
                                    else {
                                        app.rank = "no"
                                    }
                                }
                                catch (e:Exception) {
                                    e.printStackTrace()
                                }
                            }
                            app.title = attr("title")
                            app.link = BASEURL + attr("href")
                        }
                        "description" -> app.desc = text()
                        "subtitle-container" -> {
                            for (m in ele.allElements) {
                                (m as? Element)?.run{
                                    when(attr("class")) {
                                        "subtitle" -> {
                                            app.company = text()
                                            app.company_link = BASEURL + attr("href")
                                        }
                                    }
                                }
                            }
                        }
                        else -> {}
                    }
                }
            }
            appLists.add(app)
        }


        if (appLists.size > 0) {
            for (i in icons!!.indices) {
                var ele = icons[i]
                (ele as? Element)?.run {
                    for (j in ele.allElements) {
                        (j as? Element)?.run {
                            when (attr("class")) {
                                "cover-image" -> {
                                    var icons = listOf(attr("data-cover-large"),
                                            attr("data-cover-small"))

                                    for (index in icons.indices) {
                                        icons[index]?.run {
                                            var head = if (startsWith(HTTPHEAD) == true) "" else HTTPHEAD
                                            appLists.get(i).iconurl[index] = head + this
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }


        return appLists
    }

    fun createCacheDir(path:String?) {
        var dir = File(path)
        if (dir.exists()) return
        dir.mkdir()
    }


    /*
    fun getTodayFormatDate():String {
        val current = Date(System.currentTimeMillis())
        val name = "${current.year + 1900}-${current.month + 1}-${current.date}"

        return name;
    }

    fun getFormatPrevDate():String {
        val current = Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24)
        val name = "${current.year + 1900}-${current.month + 1}-${current.date}"

        return name;
    }
    */

    fun saveAppsToJson(list:MutableList<AppInfo>, file:String) {
        var gson = Gson()
        var jsonStr = gson.toJson(list)


        File("$file").writeText(jsonStr)
    }

    fun resolveAppsFromJson(file:String): MutableList<AppInfo>? {
        var gson = Gson()

        var content = File(file).readText()
        return null
    }



    var totalCount = 0
    var totalCountLoad = 0
    var totalCountFailed = 0

    fun downloadIcons(list:MutableList<AppInfo>, path:String) {

        for (i in list) {
            i?.run {
                var title = checkFileName(i.title.trim())
                var file = File("$path/${i.rank}_$title.jpeg")

                if (file.exists() == false) {
                    totalCount++
                    //Picasso.get().isLoggingEnabled = true
                    Picasso.get().load(i.iconurl[1])
                            .resize(128, 128)
                            .into(object : Target{
                                override fun onBitmapLoaded(bitmap: Bitmap?, from: Picasso.LoadedFrom?) {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    var file = File("$path/${i.rank}_$title.jpeg")
                                    var fileOut = FileOutputStream(file)
                                    bitmap?.compress(Bitmap.CompressFormat.JPEG, 30, fileOut)
                                    fileOut.close()
                                    println("download over ${totalCount}:${totalCountLoad++} ${i.rank} ${i.title}")
                                }

                                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    println("download failed ${totalCount}:${totalCountFailed++} ${i.rank} ${i.title}")
                                }

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    //println("download preload ${totalCount}:${totalCountPreload++} ${i.rank} ${i.title}: $title")
                                }
                            })
                }
                else {
                    //println("exist download ${totalCount++} ${i.rank} ${i.title}")
                }


            }
        }


    }


    val client = OkHttpClient()
    fun downloadIconsTask(listApp:MutableList<AppInfo>, path: String, db: AppsDb, log: LogInfo): Int {

        for (i in listApp) {
            i?.run {
                var title = checkFileName(i.title.trim())
                var p = path
                if (p.length <= 0)  {
                    p = getIconPath(getAppPath(i.category), getTodayFormatDate())
                }
                var file = File("${p}/${rank}_$title.jpeg")

                if (file.exists() == false) {
                    val request = Request.Builder()
                            .url(iconurl[1])
                            .build()

                    var response: Response? = null
                    try {
                        response = client.newCall(request)?.execute()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (response?.isSuccessful == true) {
                        /*
                        var filenoneed  = false
                        var tmpfile = File("${path}/${i.rank}_$title.tmp.jpeg")

                        var bmp = BitmapFactory.decodeStream(response.body()!!.byteStream())
                        var fileoutTmp = FileOutputStream(tmpfile)
                        bmp?.compress(Bitmap.CompressFormat.JPEG, 30, fileoutTmp)

                        var md5prev = md5check(tmpfile.readBytes(), "MD5")
                        var md5new = md5check(file.readBytes(), "MD5")

                        fileoutTmp.close()
                        tmpfile.delete()


                        if (md5new.equals(md5prev)) {
                            filenoneed = true
                        }
                        else {
                        */
                        var fileOut = FileOutputStream(file)
                        var bmp = BitmapFactory.decodeStream(response.body()!!.byteStream())
                        bmp?.compress(Bitmap.CompressFormat.JPEG, 30, fileOut)
                        fileOut.close()

                        db?.updateAppsIcon(i, file, getAppIconChangedPath())
                        log.printnosave("thread:download over ${totalCount++}:${totalCountLoad++} ${i.rank} ${i.title}")
                    }
                    else {
                        log.printnosave("thread:download failed ${totalCount++}:${totalCountFailed++} ${i.iconurl[1]} ${i.title}")
                    }
                }
                else {
                    log.printnosave("thread:download exist ${totalCount++}:${totalCountLoad} ${i.rank} ${i.title}: $title")
                }
            }
        }
        return 0
    }

    var clientSuspend = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .build();

    fun checkAppSuspendTask(listApp:MutableList<AppInfo>, db: AppsDb, log: LogInfo): Int {
        //var error = "We're sorry, the requested URL was not found on this server."

        var found = 0
        var cc = 0
        for (i in listApp) {
            i?.run {
                val request = Request.Builder()
                        //.url("https://play.google.com/store/apps/details?id=com.dywx.larkplayer")
                        .url(i.link)
                        .build()

                var response: Response? = null
                try {
                    response = clientSuspend.newCall(request)?.execute()
                } catch (e: IOException) {
                    e.printStackTrace()
                }

                if (response?.code() == 404) {
                    if (db.updateAppSuspend(i)) {
                        log.print("suspend app ${title}:[${company}]:$link")
                        found++
                    }
                }
                else {
                    log.printnosave("$cc/${listApp.size}:\n ${response?.code()} len: ${response?.body()?.contentLength()}${i.rank}: ${i.title} ${i.link} ok.")
                }
                cc++
            }
        }

        log.print("check suspend apps over, found:$found")


        return 0
    }


    fun getAppPath(category: String):String {
        var c = checkFileName(category)
        return  "$APP_PATH/${getAppDirectory(c)}/"
    }

    fun getAppIconChangedPath():String {
        return  "$APP_PATH/icon_changed/"
    }


    fun getIconPath(path:String, date:String):String {
        return "$path/icon_$date/"
    }


    fun getJsonFile(path:String):String {
        return "$path/app-${getTodayFormatDate()}.json"
    }

}