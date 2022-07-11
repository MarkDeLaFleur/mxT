package com.delafleur.mxt.ui


import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.*
import androidx.camera.core.*
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.appcompat.app.ActionBar
import com.delafleur.mxt.util.CameraUtil
import com.delafleur.mxt.util.CameraUtil.checkPermissions
import com.delafleur.mxt.util.CameraUtil.userRequestPermissions
import com.delafleur.mxt.R
import com.delafleur.mxt.data.SharedViewModel
import com.delafleur.mxt.databinding.FragmentCameracaptureBinding
import com.delafleur.mxt.util.readCSV
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
            checkPermissions(binding.root.context)
            return binding.root

        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            val xcfg = CameraUtil.CameraApplication()
            Log.i("log","${xcfg.cameraXConfig.minimumLoggingLevel} is minimum logging level")
            cameraController.previewTargetSize = CameraController.OutputSize(Size(700,900))
            cameraController.setEnabledUseCases(CameraController.IMAGE_CAPTURE)
            cameraController.imageCaptureMode = ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY
            cameraController.setImageCaptureFlashMode(1)
//            cameraController.imageCaptureTargetSize = CameraController.OutputSize(Size(700,900))

            cameraController.imageCaptureTargetSize = CameraController.OutputSize(1)

            cameraController.bindToLifecycle(viewLifecycleOwner)
            preview = view.findViewById(R.id.viewPreview)
            Log.i("log","implementation is ${preview.implementationMode}")
            preview.controller = cameraController
            Log.i("log", "rotation is ${preview.rotation}")
            Log.i("log","target size ${cameraController.imageCaptureTargetSize} is image capture target size")
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
           // cameraController.imageCaptureFlashMode=1
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
