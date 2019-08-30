package org.takuma_isec.airmark

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.ux.ArFragment
import com.google.zxing.integration.android.IntentIntegrator
import org.takuma_isec.airmark.presentation.ArScreen.ArObjectPresenter






class FrontActivity : AppCompatActivity() {

    private lateinit var ArObjPresenter: ArObjectPresenter

    /***
     * onCreate
     * > 起動時処理
     * > - ARシステム起動
     * > - Viewの起動
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        // --- [View Setting] View設定 ----------------------------------------------------------------------------------
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)

        // --- [Enable] AR-Core 起動 -----------------------------------------------------------------------------------
        checkARLibSystem()
    }


    /***
     * checkARLibSystem
     * > AR Coreの起動対象端末かを判定します
     */
    private fun checkARLibSystem() {
        val availability = ArCoreApk.getInstance().checkAvailability(this)
        if (availability.isTransient) {
            // --- [Re-Check] 再確認 ------------------------------------------------------------------------------------
            debugBottomMessage("❓ >> Re-Checking now...", Snackbar.LENGTH_SHORT)
            Handler().postDelayed(Runnable { checkARLibSystem() }, 200)
        }
        if (availability.isSupported) {
            // --- [Available] 起動可能 ---------------------------------------------------------------------------------
            debugBottomMessage("○ >> AR Core is Available", Snackbar.LENGTH_SHORT)
            enableARSystem()
        } else {
            // --- [NOT Available] 起動不可 -----------------------------------------------------------------------------
            debugBottomMessage("× >> AR Core is NOT Available", Snackbar.LENGTH_INDEFINITE)
        }
    }

    /***
     * enableARSystem
     * > ARシステムを起動
     */
    public lateinit var arFragment: ArFragment

    private fun enableARSystem() {
        arFragment = ArFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.ux_fragment, arFragment)
            .commit()
        ArObjPresenter = ArObjectPresenter()
        ArObjPresenter.registArCameraToReader(this@FrontActivity)
    }


    /***
     * リスナー
     */
    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_dashboard -> {
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifications -> {
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    /***
     * debugBottomMessage
     * > Snackbarを下部に表示します。
     *
     * >> 引数:   message : String
     *            duration : Int(SnackBar.INFINITY,SHORT,LONGのいずれか)
     */
    fun debugBottomMessage(message: String, duration: Int) {
        Snackbar.make(findViewById(R.id.container), message, duration).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("QRCode_Reader","Called onActivityResult.")
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Log.i("QRCode_Reader","canceled.")
            } else {
                // EditTextにQRコードの内容をセット
                Log.i("QRCode_Reader","--------------------------------------------------------------------------\nGot Text!!!\ntext: ${result.contents}")
                ArObjPresenter.AddQrObject(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}
