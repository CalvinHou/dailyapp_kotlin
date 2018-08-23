package daily.topapp.com.daily_topapp

import java.util.*

/**
 * Created by houhuihua on 2018/8/23.
 */


fun getFormatDate():String {
    val current = Date(System.currentTimeMillis())
    val name = "${current.year + 1900}-${current.month + 1}-${current.date}"

    return name;
}
