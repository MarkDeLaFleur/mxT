package com.delafleur.mxt

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import android.util.Size
import android.view.Surface
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.CameraController
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.delafleur.mxt.util.ProcessImageAnalyzer
import com.google.common.util.concurrent.ListenableFuture
import org.opencv.android.Utils
import org.opencv.core.*
import org.opencv.features2d.Features2d
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
    fun startCameraC(x: Context,y: LifecycleOwner,preview: PreviewView){

        val cameraController = LifecycleCameraController(x)
        cameraController.bindToLifecycle(y)
        cameraController.setEnabledUseCases(CameraController.IMAGE_CAPTURE)
        val preview = preview
        preview.controller = cameraController


    }

    fun startCamera(
        context: Context,
        imageAnalyzer: ProcessImageAnalyzer,
        provider: Preview.SurfaceProvider)
     {
         val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> =
            ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener(
            {
                try {
                    val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder()
                        .build()
                    val imageAnalysis = ImageAnalysis.Builder()
                      //  .setImageQueueDepth(60)
                        .setTargetResolution(Size(960,1280))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
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
    fun imageProxyToBitmap(image: ImageProxy): Bitmap {
        val planeProxy = image.planes[0]
        val buffer: ByteBuffer = planeProxy.buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
    fun blobParamsInit ():SimpleBlobDetector_Params{
        val blobParms = SimpleBlobDetector_Params()
        blobParms._filterByArea = true
        blobParms._filterByCircularity = false
        blobParms._filterByColor = true
        blobParms._filterByConvexity = true
        blobParms._filterByInertia = true
        blobParms._maxArea = 5000.0F
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
        Imgproc.cvtColor(mat, grey, Imgproc.COLOR_RGB2GRAY)
        Imgproc.threshold(
            grey,
            thresh,
            150.0,
            255.0,
            Imgproc.THRESH_BINARY
        )
        val keypts = MatOfKeyPoint()
        detector.detect(thresh, keypts)
        return keypts
    }
    fun dominoArray(imgIn: Mat) :  ArrayList<Rect> {
        val grey = Mat()
        val thresh = Mat()
        var dominoRect: ArrayList<Rect> = ArrayList()

        Imgproc.cvtColor(imgIn, grey,Imgproc.COLOR_RGB2GRAY)
        Imgproc.threshold(grey, thresh, 155.0, 255.0, Imgproc.THRESH_BINARY)
        val contours: List<MatOfPoint> = ArrayList()
        val contour2f = MatOfPoint2f()
        Imgproc.findContours(
            thresh,
            contours,
            Mat(), Imgproc.RETR_EXTERNAL,
            Imgproc.CHAIN_APPROX_NONE
        )
        contours.forEach { it ->
            it.convertTo(contour2f, CvType.CV_32FC2)
            val peri = Imgproc.arcLength(contour2f, true)
            val poly = MatOfPoint2f()
            Imgproc.approxPolyDP(contour2f, poly, 0.1 * peri, true)
            val rectWrk = Imgproc.boundingRect(poly)
            // now we have a rectangle split it and see if we have domino points
            //if(rectWrk.height*rectWrk.width > 5000 && rectWrk.height*rectWrk.width < 8000){
            dominoRect.add(Imgproc.boundingRect(poly))

            //}
        }
        if (dominoRect.size < 20) {
            val ptsOut = subMatDomino(imgIn, dominoRect)
        }

    return dominoRect

    }
    fun subMatDomino(matIn: Mat,rects: ArrayList<Rect>): ArrayList<Int>{
        val img = matIn
        val iRects = rects
        var subRectWrk: Mat
        var ptsA = 0
        var ptsB = 0
        val ptsOut = ArrayList<Int>()
        Log.i("detect","image in rows,cols ${img.rows()},${img.cols()}")
        iRects.forEach {
            //x is rows y is cols.
            if ( it.x > 6 && it.y >16) {
                val rs = it.x
                val rsW = it.x + it.width
                val cs  = it.y
                val cse = it.y + it.height
                Log.i("detect","submat rowstart, row end, col start, col end"+
                 " $cs, $cse, $rs, $rsW")
                subRectWrk = img.submat(cs,cse,rs,rsW)
                 ptsA = keypointDetector(subRectWrk).toList().size
                 ptsB = keypointDetector(subRectWrk).toList().size
            }
            else { ptsA = 0; ptsB = 0
            }
            Log.i("detect", " ptsA is ${ptsA} ptsB is ${ptsB}")

        }
      return ptsOut
    }
    fun Fragment.runOnUiThread(action: () -> Unit) {
        if (!isAdded) return
        activity?.runOnUiThread(action)
    }
    fun JPGtoRGB888(img: Bitmap): Bitmap? {
        var result: Bitmap? = null
        val numPixels = img.width * img.height
        val pixels = IntArray(numPixels)
        // get jpeg pixels, each int is the color value of one pixel
        img.getPixels(pixels, 0, img.width, 0, 0, img.width, img.height)
        // create bitmap in appropriate format either ARGB_8888 or RGB_565
        result = Bitmap.createBitmap(img.width, img.height,  Bitmap.Config.ARGB_8888)
        // Set RGB pixels
        result.setPixels(pixels, 0, result.width, 0, 0, result.width, result.height)
        return result
    }

    fun PutptsRectsonImage(rectanglesfromImage:ArrayList<Rect>,image :Mat) :Bitmap{
        val bitmapOut: Bitmap

        rectanglesfromImage.forEach { Imgproc.rectangle(image,
                    it, Scalar(255.0, 255.0, 100.0), 2, 0)
        }
        val kep = keypointDetector(image.clone())
        Features2d.drawKeypoints(image,kep,image,Scalar(255.0, 255.0,0.0)
                 , Features2d.DrawMatchesFlags_DEFAULT)

        bitmapOut = Bitmap.createBitmap(image.cols(), image.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(image, bitmapOut)
        return bitmapOut
    }
}
