package org.takuma_isec.airmark

import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.ux.ArFragment
import org.takuma_isec.airmark.presentation.ArScreen.ArObjectPresenter


class FrontActivity : AppCompatActivity() {

    private lateinit var ArObjPresenter : ArObjectPresenter

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

        // --- [init] 後始末 --------------------------------------------------------------------------------------------

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
            debugBottomMessage("❓ >> Re-Checking now...",Snackbar.LENGTH_SHORT)
            Handler().postDelayed(Runnable { checkARLibSystem() }, 200)
        }
        if (availability.isSupported) {
            // --- [Available] 起動可能 ---------------------------------------------------------------------------------
            debugBottomMessage("○ >> AR Core is Available",Snackbar.LENGTH_SHORT)
            enableARSystem()
        } else {
            // --- [NOT Available] 起動不可 -----------------------------------------------------------------------------
            debugBottomMessage("× >> AR Core is NOT Available",Snackbar.LENGTH_INDEFINITE)
        }
    }

    /***
     * enableARSystem
     * > ARシステムを起動
     */
    private lateinit var arFragment: ArFragment
    private fun enableARSystem(){
        arFragment = ArFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.ux_fragment, arFragment)
            .commit()
        // --- [init] Architecture's Presenter 生成 / 初期化 ------------------------------------------------------------
        ArObjPresenter = ArObjectPresenter(findViewById(R.id.container))
        ArObjPresenter.registArCameraToReader(this)
    }


    /***
     * リスナー
     */
    private lateinit var textMessage: TextView
    private val onNavigationItemSelectedListener = BottomNavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.navigation_home -> {
                textMessage.setText(R.string.title_home)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_dashboard -> {
                textMessage.setText(R.string.title_dashboard)
                return@OnNavigationItemSelectedListener true
            }
            R.id.navigation_notifications -> {
                textMessage.setText(R.string.title_notifications)
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
    fun debugBottomMessage(message: String, duration: Int){
        Snackbar.make(findViewById(R.id.container),message,duration).show()
    }
}
