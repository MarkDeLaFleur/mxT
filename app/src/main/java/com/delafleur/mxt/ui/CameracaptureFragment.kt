package com.delafleur.mxt.ui

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.Surface
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.core.impl.ImageCaptureConfig
import androidx.camera.core.impl.ImageOutputConfig
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.findNavController
import com.delafleur.mxt.CameraUtil.checkPermissions
import com.delafleur.mxt.CameraUtil.getMatFromImage
import com.delafleur.mxt.CameraUtil.imageProxyToBitmap
import com.delafleur.mxt.R
import com.delafleur.mxt.data.SharedViewModel
import com.delafleur.mxt.databinding.FragmentCameracaptureBinding
import com.delafleur.mxt.util.readCSV
import org.opencv.android.Utils
import org.opencv.core.Mat
import java.nio.ByteBuffer
import java.nio.channels.Selector
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameracaptureFragment : Fragment() {
        private val sharedViewModel: SharedViewModel by activityViewModels()
        private var _binding: FragmentCameracaptureBinding? = null
        private val binding get() = _binding!!
        private lateinit var preview: PreviewView
        private lateinit var cameraExecutor: ExecutorService
        private lateinit var cameraController: LifecycleCameraController



        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentCameracaptureBinding.inflate(inflater, container, false)
            cameraController = LifecycleCameraController(binding.root.context)
            cameraExecutor = Executors.newSingleThreadExecutor()
            //binding.imageCaptureButton.setOnClickListener { takePhoto() }
            checkPermissions(binding.root.context)
            setHasOptionsMenu(true)
            return binding.root

        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            cameraController.bindToLifecycle(viewLifecycleOwner)
            cameraController.setImageCaptureTargetSize(CameraController.OutputSize(Size(700,900)))

            //cameraController.setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            preview = view.findViewById<PreviewView>(R.id.viewPreview)
            preview.controller = cameraController
            Log.i("rotate", "rotation is ${preview.rotation}")


            binding?.apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = sharedViewModel
                cameracaptureFragment = this@CameracaptureFragment
            }

        }
        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

        }

        private fun Fragment.runOnUiThread(action: () -> Unit) {
            if (!isAdded) return
           activity?.runOnUiThread(action)
        }
        fun takePhoto() {
            cameraController.takePicture(
                // outputOptions,

                ContextCompat.getMainExecutor(binding.root.context),
                object: ImageCapture.OnImageCapturedCallback() {
                    override fun onError(exc: ImageCaptureException){
                        Log.e("Error","in Memory Photo capture failed: ${exc.message}",exc)
                    }
                    override fun onCaptureSuccess(image: ImageProxy) : Unit {
                        super.onCaptureSuccess(image)
                        sharedViewModel.imageRect(image)
                        image.close()
                        cameraController.unbind()
                       view?.findNavController()?.navigate(R.id.action_cameracaptureFragment_to_cameraShowImageFragment)

                    }

                }
            )
        }

    override fun onResume() {
        super.onResume()
        Log.i("resume","cameracapture Resume")
        if(readCSV().size == 1){Log.i("oops","listof players hosed")
        //   view?.findNavController()?.navigate(R.id.action_cameracaptureFragment_to_startFragment)
        }


   }
   override fun onStart(){
       super.onStart()
       Log.i("start","cameracapture Start")
       if(readCSV().size == 1){Log.i("oops","listof players hosed")
        //   view?.findNavController()?.navigate(R.id.action_cameracaptureFragment_to_startFragment)
       }


  }

        /*private fun bigTime(image: Mat){
            _binding?.viewPreview?.isInvisible = false
            val rectanglesfromImage = CameraUtil.dominoArray(image.clone())

             /*/   runOnUiThread {
                    _binding?.imageView?.setImageBitmap(
                        PutptsRectsonImage(
                            rectanglesfromImage,
                            image
                        )
                    )
                }*/

        }*/
    }
