package org.takuma_isec.airmark.repository.ArObject

import android.content.Context
import android.net.Uri
import android.os.Handler
import android.os.HandlerThread
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.Volley
import com.google.ar.sceneform.rendering.ModelRenderable
import com.google.ar.sceneform.rendering.Renderable
import org.takuma_isec.airmark.repository.Network.InputStreamVolleyRequest
import java.io.File
import java.io.FileOutputStream


class Ar3DObjectFactory private constructor(
    public var context: Context,
    public var uihandler: Handler,
    public val objURL: String,
    private val listener: Listener
) : Response.Listener<ByteArray>, Response.ErrorListener {

    // TODO : URLの規格が決まったら書く！！！
    val objFileName = objURL.substring(objURL.lastIndexOf("/") + 1, objURL.length)
    val objPath = "${context.filesDir}/${objFileName}"

    public var Object: Renderable? = null

    companion object {
        fun newInstance(
            activity: Context,
            handler: Handler,
            objectURL: String,
            listener: Listener
        ): Ar3DObjectFactory {
            var obj = Ar3DObjectFactory(activity, handler, objectURL, listener)
            obj.settingInstance()
            return obj
        }
    }

    interface Listener {
        fun onLoaded(obj: Renderable)
    }

    public val TYPE_NETWORK = 1
    public val TYPE_LOCALFILE = 2

    public var GET_TYPE: Int = -1
    private fun settingInstance() {
        // ファイルシステム動作
        when (File(objPath).exists()) {
            true ->
                GET_TYPE = TYPE_LOCALFILE
            false ->
                GET_TYPE = TYPE_NETWORK
        }
    }

    fun execute() {
        when (GET_TYPE) {
            TYPE_NETWORK ->
                get3DObjFileFromNet()
            TYPE_LOCALFILE ->
                get3DObjectFromFile()
        }
    }

    private fun get3DObjFileFromNet() {
        Log.i(
            "Ar3DObjectFactory",
            "----------------------------------------------------------------\nRequested 3DObject\n$objURL"
        )
        var thread = HandlerThread("3DObjDownloader")
        thread.start()
        var backgroundHandler = Handler(thread.looper)

        backgroundHandler.post {
            val queue = Volley.newRequestQueue(context)
            val req = InputStreamVolleyRequest(
                Request.Method.POST,
                objURL,
                this,
                this,
                HashMap()
            )
            queue.add(req)
        }
    }

    override fun onResponse(response: ByteArray?) {
        Log.i(
            "Ar3DObjectFactory",
            "----------------------------------------------------------------\nGot 3DObject"
        )
        try {
            if (response != null) {
                val name = objFileName//<FILE_NAME_WITH_EXTENSION e.g reference.txt>;
                var outputStream: FileOutputStream =
                    context.openFileOutput(name, Context.MODE_PRIVATE)
                outputStream.write(response)
                outputStream.close()

                get3DObjectFromFile()
            }
        } catch (e: Exception) {
            // TODO Auto-generated catch block
            Log.d("KEY_ERROR", "UNABLE TO DOWNLOAD FILE")
            e.printStackTrace()
        }
    }

    override fun onErrorResponse(error: VolleyError?) {
        Log.e(
            "Ar3DObjectFactory",
            "-------------------------------------------------------------------------------------\n ERROR: ${error.toString()}"
        )
    }

    private fun get3DObjectFromFile() {
        Log.i(
            "Ar3DObjectFactory",
            "-------------------------------------------------------------------\nFilePath:${objPath}"
        )

        uihandler.post {
            ModelRenderable.builder()
                // To load as an asset from the 'assets' folder ('src/main/assets/andy.sfb'):
                .setSource(context, Uri.parse(objPath))
                // Instead, load as a resource from the 'res/raw' folder ('src/main/res/raw/andy.sfb'):
                //.setSource(this, R.raw.andy)
                .build()
                .thenAccept { renderable ->
                    Log.i("Ar3DObjectFactory", "LOADED RENDERABLE.")
                    listener.onLoaded(renderable)
                }
                .exceptionally { throwable ->
                    Log.e("Ar3DObjectFactory", "[FAILED] Unable to load Renderable.", throwable)
                    throw throwable
                }
            Log.i("DEBUG_AR_LOAD", "SAT RENDERABLE.")
        }
    }

}