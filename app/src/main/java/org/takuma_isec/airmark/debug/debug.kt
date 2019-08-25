package org.takuma_isec.airmark.debug

import android.util.Log
import java.util.*

object debug {
    public val STATUS_SUCCEED = "SUCCEED"
    public val STATUS_FAILED = "FAILED"
    public val STATUS_QUERY = "QUERY"

    fun log(c: Objects, message: String, status: String = ""){
        when(status){
            "FAILED"->
                Log.e(c::class.java.name,"[$status] $message")
            ""->
                Log.i(c::class.java.name, message)
            else->
                Log.i(c::class.java.name, "[$status] $message")

        }
    }
}