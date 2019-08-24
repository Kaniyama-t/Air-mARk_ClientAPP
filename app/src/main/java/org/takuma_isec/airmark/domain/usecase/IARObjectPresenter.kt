package org.takuma_isec.airmark.domain.usecase

import android.app.Activity
import org.takuma_isec.airmark_reader.datas.IncludingCodeData
import java.util.*

interface IARObjectPresenter {

    /***
     * registArCameraToReader
     * > AR Coreで取得している映像を、AirmARk専用QRコードリーダ(別モジュール)に渡すためもの
     * > Activity側が、ARCore使用可とした時点で呼び出すのが好ましい
     *
     * >> 引　数: Activity
     * >> 返り値: 無し
     */
    fun registArCameraToReader(activity: Activity)

    //fun
    fun movedQRObject(qrCodeList: ArrayList<IncludingCodeData>)
}