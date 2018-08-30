package daily.topapp.com.daily_topapp

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button

class EntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val btnAll:Button = findViewById(R.id.buttonall)
        btnAll.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(applicationContext, MainActivity::class.java)
                startActivity(intent)
            }
        })


        val btMy:Button = findViewById(R.id.buttonmy)
        btMy?.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(applicationContext, MyDeveloper::class.java)
                startActivity(intent)
            }
        })


        val btnOther:Button = findViewById(R.id.buttonother)
        btnOther.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(applicationContext, OtherDeveloperActivity::class.java)
                startActivity(intent)
            }
        })

    }
}
