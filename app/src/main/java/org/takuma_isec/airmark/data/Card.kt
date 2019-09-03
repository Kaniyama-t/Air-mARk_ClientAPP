package org.takuma_isec.airmark.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.ar.sceneform.rendering.Renderable
import org.takuma_isec.airmark.R
import java.net.URL


class Card (var context: Context, val name: String, val desc: String, val Twitter: String?, val Facebook: String?, val Github:String?, val othersSNS: HashMap<String,String>?, val ThreeDObjectURL: URL?, val ThreeDObject: Renderable?){
    val TwitterURL = "https://twitter.com/$Twitter"
    val FacebookURL = Facebook
    val GithubURL = "https://github.com/$Github"

    fun getIcon(): Bitmap {
        return BitmapFactory.decodeResource(context.resources, R.drawable.material_qr_reader)
    }
}