package com.example.bysafe.ui.calibrate

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.example.bysafe.R

class CalibrateFragment : Fragment() {

    private lateinit var calibrateViewModel: CalibrateViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        calibrateViewModel =
                ViewModelProviders.of(this).get(CalibrateViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_calibrate, container, false)
        val textView: TextView = root.findViewById(R.id.text_calibrate)
        calibrateViewModel.text.observe(viewLifecycleOwner, Observer {
            textView.text = it
        })
        return root
    }
}
