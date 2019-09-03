package org.takuma_isec.airmark.presentation.ArScreen

import android.annotation.SuppressLint
import android.util.Log
import android.view.MotionEvent
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.ar.core.Anchor
import com.google.ar.core.HitResult
import com.google.ar.core.Plane
import com.google.ar.sceneform.AnchorNode
import com.google.ar.sceneform.rendering.Renderable
import com.google.ar.sceneform.ux.ArFragment
import com.google.ar.sceneform.ux.TransformableNode
import com.google.zxing.integration.android.IntentIntegrator
import org.takuma_isec.airmark.FrontActivity
import org.takuma_isec.airmark.R
import org.takuma_isec.airmark.data.Card
import org.takuma_isec.airmark.domain.usecase.IARObjectPresenter
import org.takuma_isec.airmark.repository.ArObject.ArObjectRepository
import org.takuma_isec.airmark_reader.ARCodeReader
import org.takuma_isec.airmark_reader.OnCodeReadListener
import org.takuma_isec.airmark_reader.datas.IncludingCodeData


class ArObjectPresenter() : IARObjectPresenter {


    /**
     * 臨時で設定しているインターふゅーす
     */
    public var CardInfo: Card? = null

    override fun AddQrObject(url: String) {
        //add!!!
        var repository = ArObjectRepository(activity, onCardLoadedListener)
        var card = repository.getArObject(url)
    }

    public var anchor: Anchor? = null
    public var card: Card? = null

    val onCardLoadedListener = object : ArObjectRepository.Listener {
        override fun onLoaded(c: Card) {
            card = c
            with(activity) {
                pcUsernameView.text = c.name
                pcDescriptionView.text = c.desc
                pcTwitterView.text = c.Twitter
                pcFacebookView.text = c.Facebook
                pcGithubView.text = c.Github

                cardAnimation(ANIM_OPEN_CARD)
            }
//            activity.arFragment.arSceneView.session!!.update()
//            if (activity.arFragment.arSceneView.arFrame!!.camera.trackingState == TrackingState.TRACKING) {
//                addNodeToScene(
//                    activity.arFragment,
//                    activity.arFragment.arSceneView.session!!.createAnchor(activity.arFragment.arSceneView.arFrame!!.androidSensorPose.extractTranslation()),
//                    c.ThreeDObject
//                )
//            }else{
//                Log.e("OhNO!!!!!","================================================================")
//            }
        }
    }

    private fun addNodeToScene(fragment: ArFragment, anchor: Anchor, renderable: Renderable) {
        val anchorNode = AnchorNode(anchor)
        val node = TransformableNode(fragment.transformationSystem)
        node.renderable = renderable
        node.setParent(anchorNode)
        fragment.arSceneView.scene.addChild(anchorNode)
        node.select()
    }

    private lateinit var activity: FrontActivity
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 》AR CodeReader Instance
    private var codeReader: ARCodeReader? = null


    @SuppressLint("MissingPermission")
    override fun registArCameraToReader(activity: FrontActivity) {
        Log.i("AirmARk_app", "Called ArObjectPresenter#registArCameraToReader()")
//        var thread = HandlerThread("AR_Handler")
//        thread.start()
//
//        codeReader = ARCodeReader(
//            context = context,
//            listener = onCodeReadListener,
//            uithireadHandler = Handler(),
//            backgroundHandler = Handler(thread.looper),
//            duration = 7000
//        )
        activity.arFragment.setOnTapArPlaneListener { hitResult: HitResult, plane: Plane, motionEvent: MotionEvent ->
            if (card == null) return@setOnTapArPlaneListener
            var obj = card!!.ThreeDObject
            if (obj is Renderable) {
                if (plane.type != Plane.Type.HORIZONTAL_UPWARD_FACING) {
                    return@setOnTapArPlaneListener
                }
                val anchor = hitResult.createAnchor()
                addNodeToScene(this.activity.arFragment, anchor, obj)
            }
        }


        activity.findViewById<FloatingActionButton>(R.id.captureQRFab).setOnClickListener {
            val integrator = IntentIntegrator(activity)
//            integrator.setDesiredBarcodeFormats(IntentIntegrator.ONE_D_CODE_TYPES)
//            integrator.setPrompt("Scan a barcode")
//            integrator.setCameraId(0) // Use a specific camera of the device
//            integrator.setBeepEnabled(false)
//            integrator.setBarcodeImageEnabled(true)
            integrator.initiateScan(0)
        }

        // -----------------------------------------------------------------------------------------

        this.activity = activity
    }

    /***
     * > This class is called when detected objects.
     */
    private var visibilityingObjects: ArrayList<IncludingCodeData> = ArrayList()
    private val onCodeReadListener = object : OnCodeReadListener {
        override fun read(qrCodeInfo: ArrayList<IncludingCodeData>) {
            debugLog(qrCodeInfo)

            // --- 新しく認識したQRコードを確認
            var enteredObjects = checkDiff(visibilityingObjects, qrCodeInfo)
            Log.i("ArObjectPresenter", "Got Entered Items List.")
            var exitedObjects = checkDiff(qrCodeInfo, visibilityingObjects)
            Log.i("ArObjectPresenter", "Got Exited Items List.")

            // --- 検知オブジェクトの追加・消去処理
//            var session = context.arFragment.arSceneView.session
//
//            var anchor = session.createAnchor(Pose())
        }

        /**
         * diffにあり、src内に無いデータのArrayListを返す
         */
        private fun checkDiff(
            src: ArrayList<IncludingCodeData>,
            diff: ArrayList<IncludingCodeData>
        ): ArrayList<IncludingCodeData> {
            var resultList = ArrayList<IncludingCodeData>()

            for (q in diff) {
                var enterdObjFlag = true
                for (v in src) {
                    if (v.body != q.body)
                        continue
                    enterdObjFlag = false
                    break
                }
                if (enterdObjFlag)
                    resultList.add(q)
            }

            //// For Debug (TODO DELETE HERE ///////////////////////////////////
            Log.i("ArObjectPresenter", "CheckDiff> ${resultList.size} Item founded.")
            for (r in resultList) Log.i("ArObjectPresenter", "           - item:${r.body}")
            ////////////////////////////////////////////////////////////////////

            return resultList
        }

        private fun debugLog(q: ArrayList<IncludingCodeData>) {
            var str = "${q.size} QRCode Founded."
            for (d in q) {
                str += "\n${d.body}"
            }
            Log.i("QRCodeRead", str)
            Snackbar.make(activity.findViewById(R.id.container), str, Snackbar.LENGTH_SHORT).show()
        }
    }

}