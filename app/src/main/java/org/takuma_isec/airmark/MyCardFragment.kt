package org.takuma_isec.airmark


import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.Fragment
import java.io.File


/**
 * A simple [Fragment] subclass.
 */
class MyCardFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val mycarduri = context!!.getSharedPreferences(context!!.getString(R.string.app_setting_prefname),
            Context.MODE_PRIVATE).getString("MyCard.path","")
        val file = File(mycarduri)
        if(mycarduri != "") {
            var view = inflater.inflate(R.layout.fragment_my_card, container, false)
            val stream = context!!.contentResolver.openInputStream(Uri.parse(mycarduri))
            val bm = BitmapFactory.decodeStream(stream)
            view.findViewById<ImageView>(R.id.myCardView).setImageBitmap(bm)
            return view//
        }else{
            var view = inflater.inflate(R.layout.fragment_my_card_null, container, false)
            view.findViewById<Button>(R.id.registMyCardButton).setOnClickListener {
                //カメラの起動Intentの用意
                val photoName = System.currentTimeMillis().toString() + ".jpg"
                val contentValues = ContentValues()
                contentValues.put(MediaStore.Images.Media.TITLE, photoName)
                contentValues.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                m_uri = context!!.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

                val intentCamera = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                intentCamera.putExtra(MediaStore.EXTRA_OUTPUT, m_uri)

                val i = Intent(Intent.ACTION_OPEN_DOCUMENT)
                i.addCategory(Intent.CATEGORY_OPENABLE)
                i.setType("image/*")

                val intent = Intent.createChooser(intentCamera, "名刺の選択");
                intent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(i))
                startActivityForResult(intent, 1000)
            }
            return view
        }
    }

    private lateinit var m_uri:Uri
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 1000) {

            if (resultCode != RESULT_OK) {
                // キャンセル時
                return
            }

            val resultUri = (if (data != null) data.data else m_uri) ?: return /* 取得失敗 */

            // ギャラリーへスキャンを促す
            MediaScannerConnection.scanFile(
                context,
                arrayOf(resultUri.path),
                arrayOf("image/*"), null
            )

            context?.getSharedPreferences(context?.getString(R.string.app_setting_prefname), Context.MODE_PRIVATE)
                ?.edit()
                ?.putString("MyCard.path",resultUri.toString())
                ?.commit()
                // NullPointerException発生時
                ?: Toast.makeText(context,"画像保存時にエラーが発生しました",Toast.LENGTH_SHORT).show()

            activity!!.supportFragmentManager
                .beginTransaction()
                .replace(R.id.utilsContainer, MyCardFragment())
                .commit()
            // 画像を保存・再生成
//            val imageView = findViewById(R.id.imageView) as ImageView
//            imageView.setImageURI(resultUri)

        }
    }


}
