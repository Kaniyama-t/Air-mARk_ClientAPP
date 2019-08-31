package org.takuma_isec.airmark


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        if(File("${context!!.filesDir}/mycard.png").exists()) {
            return inflater.inflate(R.layout.fragment_my_card, container, false)
        }else{
            return inflater.inflate(R.layout.fragment_my_card_null, container, false)
        }
    }


}
