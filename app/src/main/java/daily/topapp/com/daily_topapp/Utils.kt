package daily.topapp.com.daily_topapp

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*

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



