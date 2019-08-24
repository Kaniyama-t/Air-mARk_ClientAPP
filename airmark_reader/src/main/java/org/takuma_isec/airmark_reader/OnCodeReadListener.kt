package org.takuma_isec.airmark_reader

import org.takuma_isec.airmark_reader.datas.IncludingCodeData
import java.util.*

interface OnCodeReadListener {
    fun read(qrCodeInfo: ArrayList<IncludingCodeData>)
}