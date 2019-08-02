package org.takuma_isec.airmark

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.ar.sceneform.ux.ArFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        supportFragmentManager.beginTransaction()
            .replace(R.id.MainActivity_ARFrame, ArFragment())
            .commit()
        Toast.makeText(this,"Welcome to Air mARk",Toast.LENGTH_SHORT).show()
    }
}
