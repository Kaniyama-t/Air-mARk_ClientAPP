package org.takuma_isec.airmark.repository.ArObject

import android.content.Context
import android.os.Handler
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.ar.sceneform.rendering.Renderable
import org.json.JSONObject
import org.takuma_isec.airmark.data.Card
import org.takuma_isec.airmark.domain.IArObjectRepository
import java.net.URL

class ArObjectRepository(
    var context: Context,
    var listener: ArObjectRepository.Listener
) : IArObjectRepository {

    interface Listener {
        fun onLoaded(c: Card)
    }

    override fun getArObject(srcUrl: String) {
        Log.i("AeObjectRepository","-------------------------------------------------------------------------\nCalled getArObject")
        var infoReq = Volley.newRequestQueue(context)
        infoReq.add(JsonObjectRequest(
            Request.Method.POST,
            srcUrl,
            JSONObject(),

            // >>> SUCCEED <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            Response.Listener<JSONObject> { res ->
                Log.i("AeObjectRepository","-------------------------------------------------------------------------\nSUCCEED")

                val handler = Handler()
                // エラー処理 error.networkResponseで確認
                val ObjFactory = Ar3DObjectFactory.newInstance(
                    context,handler,
                    res.getString("3DObjURL"),
                    object : Ar3DObjectFactory.Listener {
                        override fun onLoaded(obj: Renderable) {
                            handler.post{
                                Log.i("ArObjectRepository","-----------------------------------------------------------------------\nLoaded 3D Object")
                                listener.onLoaded(
                                    Card(
                                        context = context,
                                        name = res.getString("name"),
                                        Twitter = res.getString("sns_twitter"),
                                        Facebook = res.getString("sns_facebook"),
                                        Github = res.getString("sns_github"),
                                        othersSNS = HashMap<String, String>(),
                                        ThreeDObject = obj,
                                        ThreeDObjectURL = URL(res.getString("3DObjURL"))
                                    )
                                )

                            }
                        }
                    })
                ObjFactory.execute()
//                // --- [3D-Object] Creating Objects ------------------------------------------------
//                var ObjFactory = Ar3DObjectFactory.newInstance(
//                    context,
//                    res.getString("3DObj_url"),
//                    object : Ar3DObjectFactory.Listener {
//                        override fun onLoaded(obj: Renderable) {
//                            Log.i("3DObj_Downloaded", srcUrl)
//                            // --- [SNS] Getting SNS information -----------------------------------------------
//                            var otherSNS = HashMap<String, String>()
//                            var otherSNSres = res.getJSONArray("Others")
//                            for (i in 0..otherSNSres.length()) {
//                                val sns = otherSNSres.getJSONObject(i)
//                                otherSNS.set(
//                                    key = sns.getString("SNS_name"),
//                                    value = sns.getString("SNS_link")
//                                )
//                            }
//
//                            // --- []
//                            listener.onLoaded(
//                                Card(
//                                    context = context,
//                                    name = res.getString("Name"),
//                                    Twitter = res.getString("Twitter"),
//                                    Facebook = res.getString("Facebook"),
//                                    Github = res.getString("Github"),
//                                    othersSNS = otherSNS,
//                                    ThreeDObject = obj,
//                                    ThreeDObjectURL = URL(srcUrl)
//                                )
//                            )
//                        }
//                    })
            },

            // >>> FAILED <<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<<
            Response.ErrorListener() {
                Log.i("AeObjectRepository","-------------------------------------------------------------------------\nFAILED\nmessage:${it.message}\ncause:${it.cause}\nresponse:${it.networkResponse}")
                // エラー処理 error.networkResponseで確認
                val handler = Handler()
                val ObjFactory = Ar3DObjectFactory.newInstance(
                    context,handler,
                    "http://192.168.11.12:8880/Gun_Bot.sfb",
                    object : Ar3DObjectFactory.Listener {
                        override fun onLoaded(obj: Renderable) {
                            Log.i("ArObjectRepository","-----------------------------------------------------------------------\nLoaded 3D Object")
                            handler.post{
                                listener.onLoaded(
                                    Card(
                                        context = context,
                                        name = "testUser",
                                        Twitter = "Kaniyama_404",
                                        Facebook = "https://facebook.com/kaniyama-t/",
                                        Github = "kaniyama-t",
                                        othersSNS = HashMap<String, String>(),
                                        ThreeDObject = obj,
                                        ThreeDObjectURL = URL("http://192.168.11.12:8880/Gun_Bot.sfb")
                                    )
                                )

                            }
                        }
                    })
                ObjFactory.execute()
            }
        ))
        infoReq.start()
    }
}