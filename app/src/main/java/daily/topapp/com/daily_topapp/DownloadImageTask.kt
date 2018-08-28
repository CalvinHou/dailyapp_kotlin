package daily.topapp.com.daily_topapp

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.AsyncTask
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Created by houhuihua on 2018/8/22.
 */
class DownloadImageTask(l:Int) : AsyncTask<String, Void, Int>() {
    var listApp:MutableList<AppInfo> = mutableListOf()
    var totalCount = 0
    var totalCountLoad = 0
    var db:SaveAppsToDb? = null

    override fun doInBackground(vararg  path: String): Int {
        val client = OkHttpClient()

        for (i in listApp) {
            i?.run {
                var title = checkFileName(i.title.trim())
                var file = File("${path[0]}/${i.rank}_$title.jpeg")

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
                        println("task:download over ${totalCount++}:${totalCountLoad++} ${i.rank} ${i.title}: $title")

                        ///db?.updateAppsIcon(i, file, "")
                    }
                }
                else {
                    println("task:download exis${totalCount++}:${totalCountLoad} ${i.rank} ${i.title}: $title")
                }
            }
        }
        return 0
    }

    override fun onPostExecute(result: Int) {
    }

    fun checkFileName(title:String):String {
        val ILLEGAL_CHARACTERS = charArrayOf('/', '\n', '\r', '\t', '\u0000', '`', '?', '*', '\\', '<', '>', '|', '\"', ':')
        var tmp = title;

        for (i in ILLEGAL_CHARACTERS) {
            tmp = tmp.replace("$i", "")
        }
        return tmp;
    }
}
