package com.delafleur.mxt.ui
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import android.graphics.Bitmap
import android.util.Log
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewmodel.compose.viewModel
import com.delafleur.mxt.data.SharedViewModel
import com.delafleur.mxt.databinding.FragmentCameraShowImageBinding
import org.opencv.android.Utils
import org.opencv.core.Mat

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
        binding?.sviewModel?.bitmapX?.observe(viewLifecycleOwner, { bitty ->
            Log.i("bitty","bitty is ${bitty.byteCount}")
            binding?.imageView?.setImageBitmap(bitty)
        })
    }
    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        Log.i("called","got here in the onCreate")
        val bitum = Observer<Bitmap> {bitThing -> binding?.imageView?.setImageBitmap(bitThing)
                    Log.i("called","setimageview ${bitThing.byteCount}")}
        sharedviewModel.bitmapX.observe(this,bitum)
    }



}