package org.takuma_isec.airmark.repository.Network

import com.android.volley.NetworkResponse
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.VolleyError
import com.android.volley.toolbox.HttpHeaderParser
import java.util.*

class InputStreamVolleyRequest(
    post: Int, mUrl: String, private val mListener: Response.Listener<ByteArray>,
    errorListener: Response.ErrorListener, params: HashMap<String, String>
) : Request<ByteArray>(post, mUrl, errorListener) {
    private val mParams: Map<String, String>
    //create a static map for directly accessing headers
    var responseHeaders = mutableMapOf<String, String>()

    init {
        // this request would never use cache.
        setShouldCache(false)
        mParams = params
    }

    @Throws(com.android.volley.AuthFailureError::class)
    override fun getParams(): Map<String, String> {
        return mParams
    }


    override fun deliverResponse(response: ByteArray) {
        mListener.onResponse(response)
    }

    override fun deliverError(error: VolleyError?) {
        super.deliverError(error)
    }

    override fun parseNetworkResponse(response: NetworkResponse): Response<ByteArray> {

        //Initialise local responseHeaders map with response headers received
        responseHeaders = response.headers

        //Pass the response data here
        return Response.success(response.data, HttpHeaderParser.parseCacheHeaders(response))
    }
}