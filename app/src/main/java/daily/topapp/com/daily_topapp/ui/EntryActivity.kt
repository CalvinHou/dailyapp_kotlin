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


        val btnOther:Button = findViewById(R.id.button_personal_new)
        btnOther.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(applicationContext, DevelopersCheckActivity::class.java)
                intent.putExtra("check_type", "personalization_new")
                startActivity(intent)
            }
        })


        val btnAllNew:Button = findViewById(R.id.button_all_new)
        btnAllNew.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(applicationContext, DevelopersCheckActivity::class.java)
                intent.putExtra("check_type", "_new")
                startActivity(intent)
            }
        })


        val btnAllDev:Button = findViewById(R.id.button_alldevelopers)
        btnAllDev.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(applicationContext, DevelopersCheckActivity::class.java)
                intent.putExtra("check_type", "all")
                startActivity(intent)
            }
        })

        val btnSuspend:Button = findViewById(R.id.buttonsuspend)
        btnSuspend.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(applicationContext, SuspendCheckActivity::class.java)
                intent.putExtra("check_type", "all")
                startActivity(intent)
            }
        })

        val btnSuspendOther:Button = findViewById(R.id.buttonsuspend_other)
        btnSuspendOther.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                var intent = Intent(applicationContext, SuspendCheckActivity::class.java)
                intent.putExtra("check_type", "other")
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
