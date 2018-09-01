package daily.topapp.com.daily_topapp.data

/**
 * Created by houhuihua on 2018/8/23.
 */
class Category (var name:String, var url:String, var path:String = "") {
    var apps = mutableListOf<AppInfo>()
}