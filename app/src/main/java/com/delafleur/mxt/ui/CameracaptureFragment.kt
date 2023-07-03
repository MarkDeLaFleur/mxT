package com.delafleur.mxt.ui


import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.camera.core.*
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.camera.camera2.internal.compat.workaround.TargetAspectRatio
import androidx.camera.core.ImageCapture.*
import com.delafleur.mxt.MainActivity
import com.delafleur.mxt.R
import com.delafleur.mxt.data.SharedViewModel
import com.delafleur.mxt.databinding.FragmentCameracaptureBinding

import com.delafleur.mxt.databinding.FragmentCameracapturexBinding
import com.delafleur.mxt.util.readCSV
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class CameracaptureFragment : Fragment() {
        private val sharedViewModel: SharedViewModel by activityViewModels()
        private var _binding: FragmentCameracapturexBinding? = null
        private val binding get() = _binding!!
        private lateinit var preview: PreviewView
        private lateinit var cameraExecutor: ExecutorService
        private lateinit var cameraController: LifecycleCameraController



        override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View {
            _binding = FragmentCameracapturexBinding.inflate(inflater, container, false)
            cameraController = LifecycleCameraController(binding.root.context)
            cameraExecutor = Executors.newSingleThreadExecutor()
            return binding.root

        }

        @SuppressLint("RestrictedApi")
        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            preview = view.findViewById(R.id.viewPreview)
            cameraController.setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraController.imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
            cameraController.imageCaptureTargetSize = CameraController.OutputSize(TargetAspectRatio.RATIO_4_3)
            cameraController.previewTargetSize = CameraController.OutputSize(TargetAspectRatio.RATIO_4_3)
            cameraController.imageCaptureFlashMode= FLASH_MODE_OFF
            cameraController.bindToLifecycle(viewLifecycleOwner)
            preview.controller = cameraController
            Log.i("log","preview w/h ${preview.width}/${preview.height}")
            Log.i("log", "rotation is ${preview.rotation}")
            Log.i("log","implementation is ${preview.implementationMode}")
            Log.i("log","Image capture target size ${cameraController.imageCaptureTargetSize} ")
            Log.i("log","preview target size ${cameraController.previewTargetSize}")
            binding?.apply {
                lifecycleOwner = viewLifecycleOwner
                viewModel = sharedViewModel
                cameracaptureFragment = this@CameracaptureFragment
            }


        }
        private fun Fragment.runOnUiThread(action: () -> Unit) {
            if (!isAdded) return
           activity?.runOnUiThread(action)
        }
        fun takePhoto() {
            cameraController.imageCaptureFlashMode=FLASH_MODE_AUTO
            cameraController.takePicture(
                ContextCompat.getMainExecutor(binding.root.context),
                object: ImageCapture.OnImageCapturedCallback() {
                    override fun onError(exc: ImageCaptureException){
                        Log.e("Error","in Memory Photo capture failed: ${exc.message}",exc)
                    }
                    override fun onCaptureSuccess(image: ImageProxy) : Unit {
                        super.onCaptureSuccess(image)
                        sharedViewModel.imageRect(image,preview)
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
        }
   }
   override fun onStart(){
       super.onStart()
       Log.i("start","cameracapture Start")
       if(readCSV().size == 1){Log.i("oops","listof players hosed")
       }
  }
  override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        }
  }
