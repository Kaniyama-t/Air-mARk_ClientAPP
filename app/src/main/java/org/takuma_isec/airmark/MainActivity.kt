package org.takuma_isec.airmark

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startActivity(Intent(this,FrontActivity::class.java))
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.MainActivity_ARFrame, ArFragment())
//            .commit()

        // --- AR core INIT PROCESS ------------------------------------------------------------------------------------
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.MainActivity_ARFrame, ArFragment())
//            .commit()
        Toast.makeText(this,"Welcome to Air mARk",Toast.LENGTH_SHORT).show()
        finish()
    }
}
