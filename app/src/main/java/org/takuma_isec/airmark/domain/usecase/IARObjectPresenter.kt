package org.takuma_isec.airmark.domain.usecase

import org.takuma_isec.airmark.FrontActivity

interface IARObjectPresenter {

    /***
     * > AR Coreで取得している映像を、AirmARk専用QRコードリーダ(別モジュール)に渡すためもの
     * > Activity側が、ARCore使用可とした時点で呼び出すのが好ましい
     *
     * @param activity activity for context
     * @return Unit
     */
    fun registArCameraToReader(activity: FrontActivity)

}