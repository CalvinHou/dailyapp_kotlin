package daily.topapp.com.daily_topapp

/**
 * Created by houhuihua on 2018/8/21.
 */

class KtTest {

    private fun test_2() {
        test(10) label_test@{
            x: Int, y:Int ->
            if (x > y)
                print("aa")
            else print("bb")
            //return@test 1
            return@label_test
        }

        listOf(1, 2, 3, 4, "test").forEach for_label@{
            if (it == 3) {
                println("equal 3")
                //return
            }
            println("$it")
        }

        var intPlus: Int.(x:Int) -> Int = {x -> this + x}
        var intPlus2: Int.(x:Int) -> Int = Int::plus
        var b = 3.intPlus(10)
        intPlus.invoke(10, 12)
        intPlus(20, 22);

        //max(10, {a, b -> a.length > b.length})
        var sum = {x:Int, y:Int -> x + y}
        var sum2:(x:Int, y:Int) -> Int = {x:Int, y:Int -> x +y}
        var x = sum(1, 2)
        var y = sum2(3, 4)

        for (i in 1..10) {

        }

        var arraylist = listOf(1, 2, 3, "string")
        var arraylist2 = listOf(1, 2, 3, "test")
        for (i in arraylist2.indices) {
            if (arraylist2[i] == 3) {
                println("find an 3");
            }
            else {
                println("$i:{$arraylist2[$i]}")
            }
        }

        for (i in arraylist.indices) {
            if (arraylist[i] == 3) {

            }
        }

    }

    private fun test(a:Int,  max:(x:Int, y:Int) -> Unit) {
        var aa = 10
        var bb:Byte = 0

        println("my name is a:$a")
    }
}