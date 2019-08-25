package org.takuma_isec.airmark.presentation.ArScreen

import android.annotation.SuppressLint
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import org.takuma_isec.airmark.FrontActivity
import org.takuma_isec.airmark.R
import org.takuma_isec.airmark.domain.usecase.IARObjectPresenter
import org.takuma_isec.airmark_reader.ARCodeReader
import org.takuma_isec.airmark_reader.OnCodeReadListener
import org.takuma_isec.airmark_reader.datas.IncludingCodeData
import java.util.*
import kotlin.collections.ArrayList


class ArObjectPresenter() : IARObjectPresenter{

    private lateinit var activity: FrontActivity
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 》AR CodeReader Instance
    private var codeReader : Timer? = null


    @SuppressLint("MissingPermission")
    override fun registArCameraToReader(activity: FrontActivity) {
        codeReader = ARCodeReader.create(
                            activity =  activity,
                            listener =  onCodeReadListener,
                            duration =  7000)
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
            var enteredObjects = checkDiff(visibilityingObjects,qrCodeInfo)
            Log.i("ArObjectPresenter","Got Entered Items List.")
            var exitedObjects = checkDiff(qrCodeInfo, visibilityingObjects)
            Log.i("ArObjectPresenter","Got Exited Items List.")

            // --- 検知オブジェクトの追加・消去処理
//            var session = activity.arFragment.arSceneView.session
//
//            var anchor = session.createAnchor(Pose())
        }

        /**
         * diffにあり、src内に無いデータのArrayListを返す
         */
        private fun checkDiff(src: ArrayList<IncludingCodeData>, diff: ArrayList<IncludingCodeData>): ArrayList<IncludingCodeData>{
            var resultList = ArrayList<IncludingCodeData>()

            for(q in diff){
                var enterdObjFlag = true
                for(v in src){
                    if(v.body != q.body)
                        continue
                    enterdObjFlag = false
                    break
                }
                if(enterdObjFlag)
                    resultList.add(q)
            }

            //// For Debug (TODO DELETE HERE ///////////////////////////////////
            Log.i("ArObjectPresenter","CheckDiff> ${resultList.size} Item founded.")
            for (r in resultList) Log.i("ArObjectPresenter","           - item:${r.body}")
            ////////////////////////////////////////////////////////////////////

            return resultList
        }

        private fun debugLog(q: ArrayList<IncludingCodeData>){
            var str = "${q.size} QRCode Founded."
            for (d in q){
                str += "\n${d.body}"
            }
            Log.i("QRCodeRead",str)
            Snackbar.make(activity.findViewById(R.id.container), str, Snackbar.LENGTH_SHORT).show()
        }
    }

}