package com.delafleur.mxt.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.graphics.Bitmap
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.delafleur.mxt.R
import com.delafleur.mxt.data.SharedViewModel
import com.delafleur.mxt.databinding.FragmentCameraShowImageBinding

class CameraShowImageFragment : Fragment() {
    private val sharedviewModel: SharedViewModel by activityViewModels()
    private var binding: FragmentCameraShowImageBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentBinding = FragmentCameraShowImageBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        setHasOptionsMenu(true)
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            lifecycleOwner = viewLifecycleOwner
            sviewModel = sharedviewModel
            camerashowimageFragment = this@CameraShowImageFragment
        }
        binding?.sviewModel?.bitmapX?.observe(viewLifecycleOwner, { bitmapofDominos ->
            binding?.imageView?.setImageBitmap(bitmapofDominos)
            //Got here from cameracapture fragment when it updated the bitmapX image
        })
    }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        val bitmapUpdate = Observer<Bitmap> {bmU -> binding?.imageView?.setImageBitmap(bmU)
                    Log.i("called","setimageview ${bmU.byteCount}")}
        sharedviewModel.bitmapX.observe(this,bitmapUpdate)



    }
    fun onClickProcess(){
        // user choses to process.
        // setProcess updates the points that the detector found
        Log.i("player","player ${sharedviewModel.playerIndex} called the Process camera button")
        findNavController().navigate((R.id.action_cameraShowImageFragment_to_playersFragment))

    }



}