package com.delafleur.mxt

import android.Manifest
import android.app.Activity
import android.content.Context
import org.opencv.core.Size as opencvSize
import android.content.pm.PackageManager
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.runtime.Composable
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.delafleur.mxt.util.ProcessImageAnalyzer
import com.google.common.util.concurrent.ListenableFuture
import org.opencv.core.Core
import org.opencv.core.KeyPoint
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.core.MatOfKeyPoint
import org.opencv.dnn.KeypointsModel
import org.opencv.features2d.SimpleBlobDetector
import org.opencv.features2d.SimpleBlobDetector_Params
import org.opencv.imgproc.Imgproc
import java.nio.ByteBuffer
import java.util.concurrent.Executors


private val REQUIRED_PERMISSIONS = arrayOf(
    "android.permission.CAMERA",
    "android.permission.WRITE_EXTERNAL_STORAGE"
)




object CameraUtil {
    fun startCamera(
        context: Context,
        imageAnalyzer: ProcessImageAnalyzer,
        provider: Preview.SurfaceProvider
    ) {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                try {
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder()
                        .build()
                    val imageAnalysis = ImageAnalysis.Builder()
                        .setImageQueueDepth(60)
                      //  .setTargetResolution(Size(600,800))
                        // /*.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)*/
                        .build()
                    imageAnalysis.setAnalyzer(Executors.newSingleThreadExecutor(), imageAnalyzer)
                    val cameraSelector =
                        CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK)
                            .build()
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(
                        (context as LifecycleOwner),
                        cameraSelector,
                        preview ,
                        imageAnalysis
                    )
                    preview.setSurfaceProvider(provider)
                } catch (e: Exception) {
                    Log.e("error", "[startCamera] Use case binding failed", e)
                }

            },
            ContextCompat.getMainExecutor(context)
        )
    }

    fun getMatFromImage(image: ImageProxy): Mat {
        val yBuffer: ByteBuffer = image.planes[0].buffer
        val uBuffer: ByteBuffer = image.planes[1].buffer
        val vBuffer: ByteBuffer = image.planes[2].buffer
        val ySize: Int = yBuffer.remaining()
        val uSize: Int = uBuffer.remaining()
        val vSize: Int = vBuffer.remaining()
        val nv21 = ByteArray(ySize + uSize + vSize)
        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)
        val yuv = Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1)
        yuv.put(0, 0, nv21)
        val mat = Mat()
        Imgproc.cvtColor(yuv, mat, Imgproc.COLOR_YUV2RGB_NV21, 3)
        return mat
    }

    fun fixMatRotation(matOrg: Mat, previewView: PreviewView?): Mat {
        val mat: Mat
        when (previewView?.display?.rotation) {
            Surface.ROTATION_0 -> {
                mat = Mat(matOrg.cols(), matOrg.rows(), matOrg.type())
                Core.transpose(matOrg, mat)
                Core.flip(mat, mat, 1)
            }
            Surface.ROTATION_90 -> mat = matOrg
            Surface.ROTATION_270 -> {
                mat = matOrg
                Core.flip(mat, mat, -1)
            }
            else -> {
                mat = Mat(matOrg.cols(), matOrg.rows(), matOrg.type())
                Core.transpose(matOrg, mat)
                Core.flip(mat, mat, 1)
            }
        }
        return mat
    }
    fun blobParamsInit ():SimpleBlobDetector_Params{
        val blobParms = SimpleBlobDetector_Params()
        blobParms._filterByArea = true
        blobParms._filterByCircularity = false
        blobParms._filterByColor = true
        blobParms._filterByConvexity = true
        blobParms._filterByInertia = true
        blobParms._maxArea = 5000F
        blobParms._maxConvexity = 3.4028234663852886e+38F
        blobParms._minCircularity = 0.800000011920929F
        blobParms._maxInertiaRatio = 3.4028234663852886e+38F
        blobParms._minArea = 25F
        blobParms._minConvexity = 0.800000011920929F
        blobParms._minDistBetweenBlobs =  10.0F
        blobParms._minInertiaRatio = 0.10000000149011612F
        blobParms._minRepeatability = 2
        blobParms._maxThreshold = 200.0F
        blobParms._minThreshold = 100.0F
        blobParms._thresholdStep = 10.0F
        return blobParms
    }
    fun checkPermissions(context: Context): Boolean {
        for (permission in REQUIRED_PERMISSIONS) {
            if (context.let {
                    ContextCompat.checkSelfPermission(
                        it,
                        permission
                    )
                } != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    fun userRequestPermissions(activity: Activity) {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
            2000
        )
    }
    fun keypointDetector(mat: Mat): MatOfKeyPoint {
        val detector: SimpleBlobDetector =
            SimpleBlobDetector.create(blobParamsInit())
        val grey = Mat()
        val thresh  = Mat()
        val imgSize = opencvSize(960.toDouble(),1280.toDouble())
        //Imgproc.resize(mat,mat,imgSize,0.toDouble(),0.toDouble(),Imgproc.INTER_AREA  )
        Imgproc.cvtColor(mat, grey, Imgproc.COLOR_RGB2GRAY)
        Imgproc.threshold(
            grey,
            thresh,
            150.toDouble(),
            255.toDouble(),
            Imgproc.THRESH_BINARY
        )
        val keypts: MatOfKeyPoint = MatOfKeyPoint()
        detector.detect(thresh, keypts)
        return keypts
    }

}
