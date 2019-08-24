package org.takuma_isec.airmark.presentation.ArScreen

import android.annotation.SuppressLint
import android.app.Activity
import android.widget.Toast
import org.takuma_isec.airmark.domain.usecase.IARObjectPresenter
import org.takuma_isec.airmark_reader.ARCodeReader
import org.takuma_isec.airmark_reader.OnCodeReadListener
import org.takuma_isec.airmark_reader.datas.IncludingCodeData
import java.util.*




class ArObjectPresenter(var activity: Activity) : IARObjectPresenter{

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // 》AR CodeReader Instance
    private var codeReader : Timer? = null

    /**
     * registArCameraToReader(activity: Activity)
     *
     * > まんま
     */
    @SuppressLint("MissingPermission")
    override fun registArCameraToReader() {
        codeReader = ARCodeReader.create(
                            activity =  activity,
                            listener =  onCodeReadListener,
                            duration =  5000)
    }

    /***
     * onCodeReadListener
     *
     * > This class is called when detected objects.
     */
    private val onCodeReadListener = object : OnCodeReadListener {
        override fun read(qrCodeInfo: ArrayList<IncludingCodeData>) {
            var str = "${qrCodeInfo.size} QRCode Founded."
            for (d in qrCodeInfo){
                str += "\n${d.body}"
            }
            Toast.makeText(activity,str,Toast.LENGTH_SHORT).show()
        }
    }

    override fun movedQRObject(qrCodeList: ArrayList<IncludingCodeData>) {

    }
}