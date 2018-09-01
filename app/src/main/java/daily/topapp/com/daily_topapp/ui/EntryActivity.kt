package daily.topapp.com.daily_topapp.ui

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import daily.topapp.com.daily_topapp.R

class EntryActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_entry)

        val btnAll:Button = findViewById(R.id.buttonall)
        btnAll.setOnClickListener(object: View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(applicationContext, AllAppsActivity::class.java)
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


        val btnSuspend:Button = findViewById(R.id.buttonsuspend)
        btnSuspend.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(applicationContext, SuspendCheckActivity::class.java)
                startActivity(intent)
            }
        })

        val btnDownIons:Button = findViewById(R.id.buttondownloadicon)
        btnDownIons.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(applicationContext, DownloadIconsActivity::class.java)
                startActivity(intent)
            }
        })

    }
}
