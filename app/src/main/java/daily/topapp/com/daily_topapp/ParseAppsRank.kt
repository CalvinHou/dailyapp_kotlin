package daily.topapp.com.daily_topapp

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
import java.util.*
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import java.io.IOException

/**
 * Created by houhuihua on 2018/8/23.
 */

class ParseAppsRank{

    val APP_PATH = "/sdcard/daily_app/"
    val HTTPHEAD = "https:"
    val BASEURL = HTTPHEAD + "//play.google.com"
    val BASECATEGORY = BASEURL + "/store/apps/category/"


    var topCategoryNameLists = listOf(
            "PERSONALIZATION", "MUSIC_AND_AUDIO", "COMMUNICATION", "PRODUCTIVITY", "ENTERTAINMENT", "TOOLS", "SOCIAL"
    )

    var topCategoryLists = mutableListOf<Category>()

    private fun getTop(category: String) :Category{
       return Category(category.toLowerCase(),
               BASECATEGORY + category + "/collection/topselling_free?start=0&num=120")
    }

    private fun getTopNew(category: String) :Category{
        return Category(category.toLowerCase() + "_new",
                BASECATEGORY + category + "/collection/topselling_new_free?start=0&num=120")
    }


    fun initTopCategoryList() : MutableList<Category>{
        for (i in topCategoryNameLists) {
            topCategoryLists.add(getTop(i))
            topCategoryLists.add(getTopNew(i))
            break
        }
        return topCategoryLists
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

    fun getTopApps(url:String):String {
        val client:OkHttpClient = OkHttpClient()
        val request:Request = Request.Builder().url(url).build()
        val response:Response = client?.newCall(request).execute()

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
                    //println("title is ${ele.text()}")
                    app.rank = "${i+1}"
                    when(attr("class")) {
                        "title" -> {
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


    fun getFormatDate():String {
        val current = Date(System.currentTimeMillis())
        val name = "${current.year + 1900}-${current.month + 1}-${current.date}"

        return name;
    }

    fun saveAppsToJson(list:MutableList<AppInfo>, file:String) {
        var gson = Gson()
        var jsonStr = gson.toJson(list)


        File("$file").writeText(jsonStr)
        //println(jsonStr)
    }

    fun resolveAppsFromJson(file:String): MutableList<AppInfo>? {
        var gson = Gson()

        var content = File(file).readText()
        return null
    }


    fun checkFileName(title:String):String {
        val ILLEGAL_CHARACTERS = charArrayOf('/', '\n', '\r', '\t', '\u0000', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')
        var tmp = title;

        for (i in ILLEGAL_CHARACTERS) {
            tmp = tmp.replace("$i", "")
        }
        return tmp;
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
                                    println("download over ${totalCount}:${totalCountLoad++} ${i.rank} ${i.title}: $title")
                                }

                                override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    println("download failed ${totalCount}:${totalCountFailed++} ${i.rank} ${i.title}: $title")
                                }

                                override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
                                    //TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
                                    //println("download preload ${totalCount}:${totalCountPreload++} ${i.rank} ${i.title}: $title")
                                }
                            })
                }
                else {
                    println("exist download ${totalCount++} ${i.rank} ${i.title}: $title")
                }


            }
        }


    }


    val client = OkHttpClient()
    fun downloadIconsTask(listApp:MutableList<AppInfo>, path: String): Int {

        for (i in listApp) {
            i?.run {
                var title = checkFileName(i.title.trim())
                var file = File("${path}/${i.rank}_$title.jpeg")

                if (file.exists() == false) {
                    val request = Request.Builder()
                            .url(iconurl[1])
                            .build()

                    var response: Response? = null
                    try {
                        response = client.newCall(request).execute()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }

                    if (response!!.isSuccessful) {
                        var bmp = BitmapFactory.decodeStream(response.body()!!.byteStream())
                        var fileOut = FileOutputStream(file)
                        bmp?.compress(Bitmap.CompressFormat.JPEG, 30, fileOut)
                        fileOut.close()
                        println("thread:download over ${totalCount++}:${totalCountLoad++} ${i.rank} ${i.title}: $title")
                    }
                }
                else {
                    println("thread:download exis ${totalCount++}:${totalCountLoad} ${i.rank} ${i.title}: $title")
                }
            }
        }
        return 0
    }

}