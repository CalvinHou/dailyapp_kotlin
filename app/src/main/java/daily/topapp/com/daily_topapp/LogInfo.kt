package daily.topapp.com.daily_topapp


import android.content.Context
import android.os.Handler
import android.util.Log
import android.widget.TextView
import java.io.File
import java.util.*

/**
 * Created by houhuihua on 2018/8/24.
 */

class LogInfo(var file:String, var handler: Handler?, var textview: TextView?, var textview2: TextView?) {
    var content:String = ""

    companion object {
        private var instance: LogInfo? = null

        @Synchronized
        fun getInstance(file:String, handler:Handler?,
                        textview:TextView?, textview2: TextView?): LogInfo{
            if (instance == null) {
                instance = LogInfo(file, handler, textview, textview2)
            }
            return instance!!
        }
    }


    fun print(info:String) {
        //content += "\n" + info;
        content = "${Date(System.currentTimeMillis())}:" + info + "\n" + content
        println(info)

        handler?.post(Runnable {
            textview2?.setText(content)
            if (file?.length > 0) {
                File(file).run {
                    appendText(content + "\n")
                }
            }
        })
    }

    fun printnosave(info:String) {
        println(info)

        handler?.post(Runnable {
            textview?.setText(info)
        })
    }

    fun printonlyhandler(info:String) {
        handler?.post(Runnable {
            textview?.setText(info)
        })
    }

}

// Access property for Context
val Context.log:LogInfo get()= LogInfo.getInstance("", null, null, null)



