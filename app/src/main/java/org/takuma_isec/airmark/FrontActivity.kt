package org.takuma_isec.airmark

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.ArCoreApk
import com.google.ar.sceneform.ux.ArFragment
import com.google.zxing.integration.android.IntentIntegrator
import org.takuma_isec.airmark.data.Card
import org.takuma_isec.airmark.presentation.ArScreen.ArObjectPresenter
import java.net.URL


class FrontActivity : AppCompatActivity() {

    private lateinit var ArObjPresenter: ArObjectPresenter

    /***
     * onCreate
     * > 起動時処理
     * > - ARシステム起動
     * > - Viewの起動
     */
    public lateinit var arFragment: ArFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_front)
        val navView: BottomNavigationView = findViewById(R.id.nav_view)

        // --- [View Setting] View設定 ----------------------------------------------------------------------------------
        navView.setOnNavigationItemSelectedListener(onNavigationItemSelectedListener)
        initView()

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
    private fun enableARSystem() {
        arFragment = ArFragment()
        supportFragmentManager.beginTransaction()
            .replace(R.id.ux_fragment, arFragment)
            .commit()
        ArObjPresenter = ArObjectPresenter()
        ArObjPresenter.registArCameraToReader(this)
    }

    /***
     * debugBottomMessage
     * > Snackbarを下部に表示します。
     *
     * >> 引数:   message : String
     *            duration : Int(SnackBar.INFINITY,SHORT,LONGのいずれか)
     */
    fun debugBottomMessage(message: String, duration: Int) {
        Snackbar.make(findViewById(R.id.arContainer), message, duration).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Log.i("QRCode_Reader", "Called onActivityResult.")
        val result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (result != null) {
            if (result.contents == null) {
                Log.i("QRCode_Reader", "canceled.")
            } else {
                // EditTextにQRコードの内容をセット
                Log.i(
                    "QRCode_Reader",
                    "--------------------------------------------------------------------------\nGot Text!!!\ntext: ${result.contents}"
                )
                ArObjPresenter.AddQrObject(result.contents)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    public lateinit var pCard: CardView
    public lateinit var pcUsernameView: TextView
    public lateinit var pcTwitterView: TextView
    public lateinit var pcFacebookView: TextView
    public lateinit var pcGithubView: TextView
    public lateinit var pCardAnimation: Animation
    fun initView() {
        pCard = findViewById<CardView>(R.id.personalCardView)
        pcUsernameView = findViewById<TextView>(R.id.pCardUsername)
        pcTwitterView = findViewById<TextView>(R.id.pCardTwitter)
        pcFacebookView = findViewById<TextView>(R.id.pCardFacebook)
        pcGithubView = findViewById<TextView>(R.id.pCardGithub)
        pCardAnimation = AnimationUtils.loadAnimation(this, R.anim.card_animation)
        pCard.animation = pCardAnimation

        //TODO: DEBUG
        findViewById<FloatingActionButton>(R.id.captureQRFab).setOnClickListener {
            var c = Card(
                context = this,
                name = "Kaniyama_t",
                Twitter = "Kaniyama_404",
                Facebook = "https://facebook.com/kaniyama-t/",
                Github = "kaniyama-t",
                othersSNS = HashMap<String, String>(),
                ThreeDObject = null,
                ThreeDObjectURL = URL("http://192.168.11.12:8880/Gun_Bot.sfb")
            )
            pcUsernameView.text = c.name
            pcTwitterView.text = c.Twitter
            pcFacebookView.text = c.Facebook
            pcGithubView.text = c.Github
            cardAnimation(ANIM_OPEN_CARD)
        }
    }

    val ANIM_OPEN_CARD = 101
    val ANIM_CLOSE_CARD = 102
    fun cardAnimation(p: Int) {
        when (p) {
            ANIM_OPEN_CARD -> {
                pCard.startAnimation(pCardAnimation)
                pCard.visibility = View.VISIBLE
            }
            ANIM_CLOSE_CARD -> {
                pCard.visibility = View.GONE
            }
        }
    }

    /***
     * リスナー
     */
    private val onNavigationItemSelectedListener =
        BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.navigation_home -> {
                    switchFrontFragment(AR_FRAGMENT)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_dashboard -> {
                    switchFrontFragment(UTILS_FRAGMENT)
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.utilsContainer, MyCardFragment())
                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
                R.id.navigation_notifications -> {
                    switchFrontFragment(UTILS_FRAGMENT)
//                    supportFragmentManager.beginTransaction()
//                        .replace(R.id.ux_fragment, )
//                        .commit()
                    return@OnNavigationItemSelectedListener true
                }
            }
            false
        }

    val AR_FRAGMENT = 201
    val UTILS_FRAGMENT = 202
    fun switchFrontFragment(param: Int) {
        when (param) {
            AR_FRAGMENT -> {
                findViewById<FrameLayout>(R.id.arContainer).visibility = View.VISIBLE
                findViewById<FrameLayout>(R.id.utilsContainer).visibility = View.GONE
            }
            UTILS_FRAGMENT -> {
                findViewById<FrameLayout>(R.id.arContainer).visibility = View.GONE
                findViewById<FrameLayout>(R.id.utilsContainer).visibility = View.VISIBLE
            }
        }
    }
}

